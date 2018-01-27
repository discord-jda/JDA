/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests.ratelimit;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ExceptionEvent;
import net.dv8tion.jda.core.requests.RateLimiter;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.Route.RateLimit;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ClientRateLimiter extends RateLimiter
{
    volatile Long globalCooldown = null;

    public ClientRateLimiter(Requester requester, int poolSize)
    {
        super(requester, poolSize);
    }

    @Override
    public Long getRateLimit(Route.CompiledRoute route)
    {
        Bucket bucket = getBucket(route);
        synchronized (bucket)
        {
           return bucket.getRateLimit();
        }
    }

    @Override
    protected void queueRequest(Request request)
    {
        Bucket bucket = getBucket(request.getRoute());
        synchronized (bucket)
        {
            bucket.addToQueue(request);
        }
    }

    @Override
    protected Long handleResponse(Route.CompiledRoute route, okhttp3.Response response)
    {
        Bucket bucket = getBucket(route);
        synchronized (bucket)
        {
            long now = System.currentTimeMillis();
            int code = response.code();
            if (code == 429)
            {
                try (InputStream in = Requester.getBody(response))
                {
                    JSONObject limitObj = new JSONObject(new JSONTokener(in));
                    long retryAfter = limitObj.getLong("retry_after");

                    if (limitObj.has("global") && limitObj.getBoolean("global"))    //Global ratelimit
                        globalCooldown = now + retryAfter;
                    else
                        bucket.retryAfter = now + retryAfter;

                    return retryAfter;                    
                }
                catch (IOException e)
                {
                    throw new IllegalStateException(e);
                }
            }
            else
            {
                return null;
            }
        }
    }

    private Bucket getBucket(Route.CompiledRoute route)
    {
        String baseRoute = route.getBaseRoute().getRoute();
        Bucket bucket = (Bucket) buckets.get(baseRoute);
        if (bucket == null)
        {
            synchronized (buckets)
            {
                bucket = (Bucket) buckets.get(baseRoute);
                if (bucket == null)
                {
                    bucket = new Bucket(baseRoute, route.getBaseRoute().getRatelimit());
                    buckets.put(baseRoute, bucket);
                }
            }
        }
        return bucket;
    }

    private class Bucket implements IBucket, Runnable
    {
        final String route;
        final RateLimit rateLimit;
        final ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();
        volatile long retryAfter = 0;

        public Bucket(String route, RateLimit rateLimit)
        {
            this.route = route;
            this.rateLimit = rateLimit;
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
            long now = System.currentTimeMillis();
            if (globalCooldown != null) //Are we on global cooldown?
            {
                if (now > globalCooldown)   //Verify that we should still be on cooldown.
                {
                    globalCooldown = null;  //If we are done cooling down, reset the globalCooldown and continue.
                } else
                {
                    return globalCooldown - now;    //If we should still be on cooldown, return when we can go again.
                }
            }
            if (this.retryAfter > now)
            {
                return this.retryAfter - now;
            }
            else
            {
                return null;
            }
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
                            if (isSkipped(it, request))
                                continue;
                            Long retryAfter = requester.execute(request);
                            if (retryAfter != null)
                                break;
                            else
                                it.remove();
                        }
                        catch (Throwable t)
                        {
                            Requester.LOG.error("Error executing REST request", t);
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
                Requester.LOG.error("There was some exception in the ClientRateLimiter", err);
                if (err instanceof Error)
                {
                    JDAImpl api = requester.getJDA();
                    api.getEventManager().handle(new ExceptionEvent(api, err, true));
                }
            }
        }

        @Override
        public RateLimit getRatelimit()
        {
            return rateLimit;
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
