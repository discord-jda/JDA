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

import com.mashape.unirest.http.HttpResponse;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRateLimiter implements IRateLimiter
{
    private final Requester requester;
    ExecutorService pool = Executors.newFixedThreadPool(5);
    volatile ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    volatile ConcurrentLinkedQueue<Bucket> submittedBuckets = new ConcurrentLinkedQueue<>();
    volatile Long globalCooldown = null;

    public ClientRateLimiter(Requester requester)
    {
        this.requester = requester;
    }

    @Override
    public void queueRequest(Request request)
    {
        Bucket bucket = getBucket(request.getRoute().getBaseRoute().getRoute());
        synchronized (bucket)
        {
            bucket.addToQueue(request);
        }
    }

    @Override
    public Long getRateLimit(Route.CompiledRoute route)
    {
        Bucket bucket = getBucket(route.getBaseRoute().getRoute());
        synchronized (bucket)
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
            if (bucket.retryAfter > now)
            {
                return bucket.retryAfter - now;
            }
            else
            {
                return null;
            }
        }
    }

    @Override
    public Long handleResponse(Route.CompiledRoute route, HttpResponse<String> response)
    {
        Bucket bucket = getBucket(route.getBaseRoute().getRoute());
        synchronized (bucket)
        {
            long now = System.currentTimeMillis();
            int code = response.getStatus();
            if (code == 429)
            {
                JSONObject limitObj = new JSONObject(response.getBody());
                long retryAfter = limitObj.getLong("retry_after");
                if (limitObj.has("global") && limitObj.getBoolean("global"))    //Global ratelimit
                {
                    globalCooldown = now + retryAfter;
                }
                else
                {
                    bucket.retryAfter = now + retryAfter;
                }
                return retryAfter;
            }
            else
            {
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

    private class Bucket implements Runnable
    {
        final String route;
        volatile long retryAfter = 0;
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
