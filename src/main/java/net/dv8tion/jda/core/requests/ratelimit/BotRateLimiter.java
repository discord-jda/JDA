/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.requests.ratelimit;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import net.dv8tion.jda.core.requests.RateLimiter;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class BotRateLimiter extends RateLimiter
{
    volatile Long timeOffset = null;
    volatile Long globalCooldown = null;

    public BotRateLimiter(Requester requester, int poolSize)
    {
        super(requester, poolSize);
    }

    @Override
    public Long getRateLimit(CompiledRoute route)
    {
        Bucket bucket = getBucket(route.getRatelimitRoute());
        synchronized (bucket)
        {
            return bucket.getRateLimit();
        }
    }

    @Override
    protected void queueRequest(Request request)
    {
        if (isShutdown)
            throw new RejectedExecutionException("Cannot queue a request after shutdown");
        Bucket bucket = getBucket(request.getRoute().getRatelimitRoute());
        synchronized (bucket)
        {
            bucket.addToQueue(request);
        }
    }

    @Override
    protected Long handleResponse(CompiledRoute route, HttpResponse<String> response)
    {
        Bucket bucket = getBucket(route.getRatelimitRoute());
        synchronized (bucket)
        {
            Headers headers = response.getHeaders();
            int code = response.getStatus();
            if (timeOffset == null)
                setTimeOffset(headers);

            if (code == 429)
            {
                String global = headers.getFirst("X-RateLimit-Global");
                String retry = headers.getFirst("Retry-After");
                if (retry == null || retry.isEmpty())
                {
                    JSONObject limitObj = new JSONObject(response.getBody());
                    retry = limitObj.get("retry_after").toString();
                }
                long retryAfter = Long.parseLong(retry);
                if (!Boolean.parseBoolean(global))  //Not global ratelimit
                {
                    updateBucket(bucket, headers);
                }
                else
                {
                    //If it is global, lock down the threads.
                    globalCooldown = getNow() + retryAfter;
                }
                return retryAfter;
            }
            else
            {
                updateBucket(bucket, headers);
                return null;
            }
        }

    }

    private Bucket getBucket(String route)
    {
        Bucket bucket = (Bucket) buckets.get(route);
        if (bucket == null)
        {
            synchronized (buckets)
            {
                bucket = (Bucket) buckets.get(route);
                if (bucket == null)
                {
                    bucket = new Bucket(route);
                    buckets.put(route, bucket);
                }
            }
        }
        return bucket;
    }

    public long getNow()
    {
        return System.currentTimeMillis() + getTimeOffset();
    }

    public long getTimeOffset()
    {
        return timeOffset == null ? 0 : timeOffset;
    }

    private void setTimeOffset(Headers headers)
    {
        //Store as soon as possible to get the most accurate time difference;
        long time = System.currentTimeMillis();
        if (timeOffset == null)
        {
            //Get the date header provided by Discord.
            //Format:  "date" : "Fri, 16 Sep 2016 05:49:36 GMT"
            String date = headers.getFirst("Date");
            if (date != null)
            {
                OffsetDateTime tDate = OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
                long lDate = tDate.toInstant().toEpochMilli(); //We want to work in milliseconds, not seconds
                timeOffset = lDate - time; //Get offset in milliseconds.
            }
        }
    }

    private void updateBucket(Bucket bucket, Headers headers)
    {
        try
        {
            bucket.resetTime = Long.parseLong(headers.getFirst("X-RateLimit-Reset")) * 1000; //Seconds to milliseconds
            bucket.routeUsageLimit = Integer.parseInt(headers.getFirst("X-RateLimit-Limit"));
            bucket.routeUsageRemaining = Integer.parseInt(headers.getFirst("X-RateLimit-Remaining"));

        }
        catch (NumberFormatException ex)
        {
            if (!bucket.getRoute().equals("gateway")
                    && !bucket.getRoute().equals("users/@me")
                    && Requester.LOG.getEffectiveLevel().getPriority() <= SimpleLog.Level.DEBUG.getPriority())
            {
                Requester.LOG.fatal("Encountered issue with headers when updating a bucket"
                                  + "\nRoute: " + bucket.getRoute()
                                  + "\nHeaders: " + headers);
                Requester.LOG.log(ex);
            }

        }
    }

    private class Bucket implements IBucket, Runnable
    {
        final String route;
        volatile long resetTime = 0;
        volatile int routeUsageRemaining = 1;    //These are default values to only allow 1 request until we have properly
        volatile int routeUsageLimit = 1;        // ratelimit information.
        volatile ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();

        public Bucket(String route)
        {
            this.route = route;
        }

        void addToQueue(Request request)
        {
            requests.add(request);
            submitForProcessing();
        }

        void submitForProcessing()
        {
            synchronized (submittedBuckets)
            {
                if (!submittedBuckets.contains(this))
                {
                    Long delay = getRateLimit();
                    if (delay == null)
                        delay = 0L;

                    pool.schedule(this, delay, TimeUnit.MILLISECONDS);
                    submittedBuckets.add(this);
                }
            }
        }

        Long getRateLimit()
        {
            if (globalCooldown != null) //Are we on global cooldown?
            {
                long now = getNow();
                if (now > globalCooldown)   //Verify that we should still be on cooldown.
                {
                    globalCooldown = null;  //If we are done cooling down, reset the globalCooldown and continue.
                } else
                {
                    return globalCooldown - now;    //If we should still be on cooldown, return when we can go again.
                }
            }
            if (this.routeUsageRemaining <= 0)
            {
                if (getNow() > this.resetTime)
                {
                    this.routeUsageRemaining = this.routeUsageLimit;
                    this.resetTime = 0;
                }
            }
            if (this.routeUsageRemaining > 0)
                return null;
            else
                return this.resetTime - getNow();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Bucket))
                return false;

            Bucket oBucket = (Bucket) o;
            return route.equals(oBucket.route);
        }

        @Override
        public int hashCode()
        {
            return route.hashCode();
        }

        @Override
        public void run()
        {
            try
            {
                synchronized (requests)
                {
                    for (Iterator<Request> it = requests.iterator(); it.hasNext(); )
                    {
                        Request request = null;
                        try
                        {
                            request = it.next();
                            Long retryAfter = requester.execute(request);
                            if (retryAfter != null)
                            {
                                break;
                            }
                            else
                            {
                                it.remove();
                            }
                        }
                        catch (Throwable t)
                        {
                            Requester.LOG.fatal("Requester system encountered an internal error");
                            Requester.LOG.log(t);
                            it.remove();
                            if (request != null)
                                request.onFailure(t);
                        }
                    }

                    synchronized (submittedBuckets)
                    {
                        submittedBuckets.remove(this);
                        if (!requests.isEmpty())
                        {
                            try
                            {
                                this.submitForProcessing();
                            }
                            catch (RejectedExecutionException e)
                            {
                                Requester.LOG.debug("Caught RejectedExecutionException when re-queuing a ratelimited request. The requester is probably shutdown, thus, this can be ignored.");
                            }
                        }
                    }
                }
            }
            catch (Throwable err)
            {
                Requester.LOG.fatal("Requester system encountered an internal error from beyond the sychronized execution blocks. NOT GOOD!");
                Requester.LOG.log(err);
            }
        }

        @Override
        public String getRoute()
        {
            return route;
        }

        @Override
        public Queue<Request> getRequests()
        {
            return requests;
        }
    }
}
