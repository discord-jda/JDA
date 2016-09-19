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
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotRateLimiter implements IRateLimiter
{
    final Requester requester;
    ExecutorService pool = Executors.newFixedThreadPool(5);
    volatile Long timeOffset = null;
    volatile Long globalCooldown = null;
    volatile ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    volatile ConcurrentLinkedQueue<Bucket> submittedBuckets = new ConcurrentLinkedQueue<>();

    public BotRateLimiter(Requester requester)
    {
        this.requester = requester;
    }

    @Override
    public void queueRequest(Request request)
    {
        Bucket bucket = getBucket(request.getRoute().getRatelimitRoute());
        synchronized (bucket)
        {
            bucket.addToQueue(request);
        }
    }

    @Override
    public Long getRateLimit(CompiledRoute route)
    {
        Bucket bucket = getBucket(route.getRatelimitRoute());
        synchronized (bucket)
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
            if (bucket.routeUsageRemaining <= 0)
            {
                if (getNow() > bucket.resetTime)
                {
                    bucket.routeUsageRemaining = bucket.routeUsageLimit;
                    bucket.resetTime = 0;
                }
            }
            if (bucket.routeUsageRemaining > 0)
                return null;
            else
                return bucket.resetTime - getNow();
        }
    }

    @Override
    public Long handleResponse(CompiledRoute route, HttpResponse<String> response)
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
                String global = headers.getFirst("x-ratelimit-global");
                long retryAfter = Long.parseLong(headers.getFirst("retry-after"));
                if (!Boolean.parseBoolean(global))  //Not global ratelimit
                {
                    updateBucket(bucket, headers);
                } else
                {
                    globalCooldown = getNow() + retryAfter;
                    return retryAfter;
                    //If it is global, lock down the threads.
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
        Bucket bucket = buckets.get(route);
        if (bucket == null)
        {
            synchronized (buckets)
            {
                bucket = buckets.get(route);
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
            String date = headers.getFirst("date");
            if (date != null)
            {
                OffsetDateTime tDate = OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
                long lDate = tDate.toEpochSecond() * 1000;             //We want to work in milliseconds, not seconds
                timeOffset = Math.floorDiv(lDate - time, 1000) * 1000; //Get offset, convert to seconds, round it down, convert to milliseconds.
            }
        }
    }

    private void updateBucket(Bucket bucket, Headers headers)
    {
        try
        {
            bucket.resetTime = Long.parseLong(headers.getFirst("x-ratelimit-reset")) * 1000; //Seconds to milliseconds
            bucket.routeUsageLimit = Integer.parseInt(headers.getFirst("x-ratelimit-limit"));
            bucket.routeUsageRemaining = Integer.parseInt(headers.getFirst("x-ratelimit-remaining"));

        }
        catch (NumberFormatException ignored) {}
    }

    private class Bucket implements Runnable
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
                    submittedBuckets.add(this);
                    pool.submit(this);
                }
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Bucket))
                return false;

            Bucket oBucket = (Bucket) o;
            return route.equals(((Bucket) o).route);
        }

        @Override
        public int hashCode()
        {
            return route.hashCode();
        }

        @Override
        public void run()
        {
            synchronized (requests)
            {
                for (Iterator<Request> it = requests.iterator(); it.hasNext(); )
                {
                    Request request = it.next();
                    Long retryAfter = requester.execute(request);
                    if (retryAfter != null)
                    {
                        break;
                    } else
                    {
                        it.remove();
                    }
                }

                synchronized (submittedBuckets)
                {
                    submittedBuckets.remove(this);
                    if (!requests.isEmpty())
                    {
                        this.submitForProcessing();
                    }
                }
            }
        }
    }
}
