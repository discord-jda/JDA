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

package net.dv8tion.jda.core.requests;

import com.mashape.unirest.http.HttpResponse;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import net.dv8tion.jda.core.requests.ratelimit.IBucket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class RateLimiter
{
    //Implementations of this class exist in the net.dv8tion.jda.core.requests.ratelimit package.

    protected final Requester requester;
    protected final ExecutorService pool;
    protected volatile boolean isShutdown;
    protected volatile ConcurrentHashMap<String, IBucket> buckets = new ConcurrentHashMap<>();
    protected volatile ConcurrentLinkedQueue<IBucket> submittedBuckets = new ConcurrentLinkedQueue<>();

    protected RateLimiter(Requester requester, int poolSize)
    {
        this.requester = requester;
        this.pool = Executors.newFixedThreadPool(poolSize);
        this.isShutdown = false;
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

        try
        {
            while (!submittedBuckets.isEmpty())
            {
                Thread.sleep(100);
            }
        }
        catch (InterruptedException ignored) {}

        pool.shutdownNow();
    }

    protected List<IBucket> shutdownNow()
    {
        isShutdown = true;
        pool.shutdownNow(); //We don't get the runnable list returned here because some buckets might've failed to actually finish properly and aren't in this list.

        try
        {
            while (!pool.awaitTermination(100, TimeUnit.MILLISECONDS));
        }
        catch (InterruptedException ignored) {}

        return buckets.values().stream().filter(b -> !b.getRequests().isEmpty()).collect(Collectors.toList());
    }
}
