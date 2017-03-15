/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.requests;

import com.mashape.unirest.http.HttpResponse;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import net.dv8tion.jda.core.requests.Route.RateLimit;
import net.dv8tion.jda.core.requests.ratelimit.IBucket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class RateLimiter
{
    //Implementations of this class exist in the net.dv8tion.jda.core.requests.ratelimit package.

    protected final Requester requester;
    protected final ScheduledExecutorService pool;
    protected volatile boolean isShutdown;
    protected volatile ConcurrentHashMap<String, IBucket> buckets = new ConcurrentHashMap<>();
    protected volatile ConcurrentLinkedQueue<IBucket> submittedBuckets = new ConcurrentLinkedQueue<>();

    protected RateLimiter(Requester requester, int poolSize)
    {
        this.requester = requester;
        this.isShutdown = false;
        this.pool = Executors.newScheduledThreadPool(poolSize, new RateLimitThreadFactory(requester.getJDA()));
    }


    // -- Required Implementations --
    public abstract Long getRateLimit(CompiledRoute route);
    protected abstract void queueRequest(Request request);
    protected abstract Long handleResponse(CompiledRoute route, HttpResponse<String> response);


    // --- Default Implementations --

    public boolean isRateLimited(CompiledRoute route)
    {
        return getRateLimit(route) != null;
    }

    public List<IBucket> getRouteBuckets()
    {
        synchronized (buckets)
        {
            return Collections.unmodifiableList(new ArrayList<>(buckets.values()));
        }
    }

    public List<IBucket> getQueuedRouteBuckets()
    {
        synchronized (submittedBuckets)
        {
            return Collections.unmodifiableList(new ArrayList<>(submittedBuckets));
        }
    }

    protected void shutdown()
    {
        isShutdown = true;

        pool.shutdownNow();
    }

    private class RateLimitThreadFactory implements ThreadFactory
    {
        final String identifier;
        AtomicInteger threadCount = new AtomicInteger(1);

        public RateLimitThreadFactory(JDAImpl api)
        {
            identifier = api.getIdentifierString() + " RateLimit-Queue Pool";
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r, identifier + " - Thread " + threadCount.getAndIncrement());
            t.setDaemon(true);

            return t;
        }
    }
}
