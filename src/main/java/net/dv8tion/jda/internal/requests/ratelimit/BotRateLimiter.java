/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.requests.ratelimit;

import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Method;
import net.dv8tion.jda.internal.requests.RateLimiter;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.Headers;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class BotRateLimiter extends RateLimiter
{
    private static final String RESET_AFTER_HEADER = "X-RateLimit-Reset-After";
    private static final String RESET_HEADER = "X-RateLimit-Reset";
    private static final String LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String GLOBAL_HEADER = "X-RateLimit-Global";
    private static final String HASH_HEADER = "X-RateLimit-Bucket";
    private static final String UNLIMITED_BUCKET = "unlimited";

    private final ReentrantLock bucketLock = new ReentrantLock();
    // Method + Route -> Hash
    private final Map<String, String> hash = new ConcurrentHashMap<>();
    // Hash + Major Parameter -> Bucket
    private final Map<String, Bucket> bucket = new ConcurrentHashMap<>();
    // Bucket -> Rate-Limit Worker
    private final Map<Bucket, Future<?>> rateLimitQueue = new ConcurrentHashMap<>();
    private final Future<?> cleanupWorker;

    public BotRateLimiter(Requester requester)
    {
        super(requester);
        bucket.put("unlimited", new Bucket("unlimited"));
        cleanupWorker = getScheduler().scheduleAtFixedRate(this::cleanup, 30, 30, TimeUnit.SECONDS);
    }

    private ScheduledExecutorService getScheduler()
    {
        return requester.getJDA().getRateLimitPool();
    }

    private void cleanup()
    {
        MiscUtil.locked(bucketLock, () -> {
            Iterator<String> keys = bucket.keySet().iterator();

            while (keys.hasNext())
            {
                String key = keys.next();
                Bucket bucket = this.bucket.get(key);
                if (bucket.isUnlimited())
                    continue;

                if (bucket.requests.isEmpty() && bucket.reset <= getNow())
                    keys.remove();
            }
        });
    }

    private String getRouteHash(Method method, String baseRoute)
    {
        return hash.getOrDefault(method + "/" + baseRoute, UNLIMITED_BUCKET);
    }

    @Override
    protected void shutdown()
    {
        super.shutdown();
        cleanupWorker.cancel(false);
    }

    @Override
    public Long getRateLimit(Route.CompiledRoute route)
    {
        Bucket bucket = getBucket(route, false);
        return bucket == null ? 0L : bucket.getRateLimit();
    }

    @Override
    protected void queueRequest(Request request)
    {
        MiscUtil.locked(bucketLock, () -> {
            Bucket bucket = getBucket(request.getRoute(), true);
            bucket.enqueue(request);
            runBucket(bucket);
        });
    }

    @Override
    protected Long handleResponse(Route.CompiledRoute route, okhttp3.Response response)
    {
        bucketLock.lock();
        try
        {
            long rateLimit = updateBucket(route, response).getRateLimit();
            if (response.code() == 429)
                return rateLimit;
            else
                return null;
        }
        finally
        {
            bucketLock.unlock();
        }
    }

    private String getBucketId(Route.CompiledRoute route)
    {
        return route.getBaseRoute().getRoute() + ":" + route.getMajorParameters();
    }

    private Bucket updateBucket(Route.CompiledRoute route, okhttp3.Response response)
    {
        return MiscUtil.locked(bucketLock, () -> {
            try
            {
                Bucket bucket = getBucket(route, true);

                Headers headers = response.headers();

                boolean global = headers.get(GLOBAL_HEADER) != null;
                String hash = headers.get(HASH_HEADER);
                long now = getNow();

                if (hash != null)
                {
                    this.hash.put(route.getMethod() + "/" + route.getBaseRoute().getRoute(), hash);
                    bucket = getBucket(route, true);
                }

                if (response.code() == 429)
                {
                    Requester.LOG.warn("Encountered 429 on bucket {}", bucket.bucketId);
                }

                if (global)
                {
                    DataObject body = DataObject.fromJson(IOUtil.getBody(response));
                    requester.getJDA().getSessionController().setGlobalRatelimit(now + body.getLong("retry_after"));
                }

                String limitHeader = headers.get(LIMIT_HEADER);
                String remainingHeader = headers.get(REMAINING_HEADER);
                String resetHeader = headers.get(RESET_AFTER_HEADER);

                bucket.limit = (int) Math.max(1L, parseLong(limitHeader));
                bucket.remaining = (int) parseLong(remainingHeader);
                bucket.reset = now + parseDouble(resetHeader);
                Requester.LOG.trace("Updated bucket {} to ({}/{}, {})", bucket.bucketId, bucket.remaining, bucket.limit, bucket.reset - now);
                return bucket;
            }
            catch (Exception e)
            {
                Requester.LOG.error("Encountered Exception while updating a bucket", e);
                return getBucket(route, true);
            }
        });
    }

    private Bucket getBucket(Route.CompiledRoute route, boolean create)
    {
        return MiscUtil.locked(bucketLock, () ->
        {
            String hash = getRouteHash(route.getMethod(), route.getBaseRoute().getRoute());
            if (hash.equals(UNLIMITED_BUCKET))
                return this.bucket.get(UNLIMITED_BUCKET);
            String bucketId = hash + ":" + route.getMajorParameters();
            Bucket bucket = this.bucket.get(bucketId);
            if (bucket == null && create)
                this.bucket.put(bucketId, bucket = new Bucket(bucketId));

            return bucket;
        });
    }

    private void runBucket(Bucket bucket)
    {
        if (isShutdown)
            return;
        MiscUtil.locked(bucketLock, () ->
            rateLimitQueue.computeIfAbsent(bucket,
                (k) -> getScheduler().schedule(bucket, bucket.getRateLimit(), TimeUnit.MILLISECONDS)));
    }

    private long parseLong(String input)
    {
        return input == null ? 0L : Long.parseLong(input);
    }

    private long parseDouble(String input)
    {
        return input == null ? 0L : (long) (Double.parseDouble(input) * 1000);
    }

    public long getNow()
    {
        return System.currentTimeMillis();
    }

    private class Bucket implements IBucket, Runnable
    {
        private final String bucketId;
        private final Queue<Request> requests = new ConcurrentLinkedQueue<>();

        private long reset = 0;
        private int remaining = 1;
        private int limit = 1;

        public Bucket(String bucketId)
        {
            this.bucketId = bucketId;
        }

        public void enqueue(Request request)
        {
            requests.add(request);
        }

        public long getRateLimit()
        {
            long now = getNow();
            long global = requester.getJDA().getSessionController().getGlobalRatelimit();
            if (global > now)
                return global - now;
            if (reset <= now)
            {
                remaining = limit;
                return 0L;
            }

            return remaining < 1 ? reset - now : 0L;
        }

        public long getReset()
        {
            return reset;
        }

        public int getRemaining()
        {
            return remaining;
        }

        public int getLimit()
        {
            return limit;
        }

        private boolean isUnlimited()
        {
            return bucketId.equals("unlimited");
        }

        private void backoff()
        {
            MiscUtil.locked(bucketLock, () -> {
                rateLimitQueue.remove(this);
                if (!requests.isEmpty())
                    runBucket(this);
            });
        }

        @Override
        public void run()
        {
            Iterator<Request> iterator = requests.iterator();
            while (iterator.hasNext())
            {
                Request request = iterator.next();
                if (isUnlimited())
                {
                    boolean shouldSkip = MiscUtil.locked(bucketLock, () -> {
                        // Attempt moving request to correct bucket if it has been created
                        Bucket bucket = getBucket(request.getRoute(), true);
                        if (bucket != null && bucket != this)
                        {
                            bucket.enqueue(request);
                            iterator.remove();
                            runBucket(bucket);
                            return true;
                        }
                        return false;
                    });
                    if (shouldSkip) continue;
                }

                if (isSkipped(iterator, request))
                    continue;

                try
                {
                    Long rateLimit = requester.execute(request).get();
                    if (rateLimit != null)
                        break;

                    iterator.remove();
                    rateLimit = getRateLimit();
                    if (rateLimit > 0L)
                    {
                        Requester.LOG.debug("Backing off {} ms for bucket {}", rateLimit, bucketId);
                        break;
                    }
                }
                catch (InterruptedException | ExecutionException ex)
                {
                    Requester.LOG.warn("Interrupted while working on requests", ex);
                    break;
                }
            }

            backoff();
        }

        @Override
        public Queue<Request> getRequests()
        {
            return requests;
        }
    }
}
