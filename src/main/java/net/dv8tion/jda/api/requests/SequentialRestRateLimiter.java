/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.utils.JDALogger;
import okhttp3.Headers;
import okhttp3.Response;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>A bucket is determined via the Path+Method+Major in the following way:
 * <ol>
 *     <li>Get Hash from Path+Method (we call this route)</li>
 *     <li>Get bucket from Hash+Major (we call this bucketid)</li>
 * </ol>
 *
 * <p>If no hash is known we default to the constant "unlimited" hash. The hash is loaded from HTTP responses using the "X-RateLimit-Bucket" response header.
 * This hash is per Method+Path and can be stored indefinitely once received.
 * Some endpoints don't return a hash, this means that the endpoint is <b>unlimited</b> and will be in queue with only the major parameter.
 *
 * <p>To explain this further, lets look at the example of message history. The endpoint to fetch message history is {@code GET/channels/{channel.id}/messages}.
 * This endpoint does not have any rate limit (unlimited) and will thus use the hash {@code unlimited+GET/channels/{channel.id}/messages}.
 * The bucket id for this will be {@code unlimited+GET/channels/{channel.id}/messages:guild_id:{channel.id}:webhook_id} where {@code {channel.id}} would be replaced with the respective id.
 * This means you can fetch history concurrently for multiple channels, but it will be in sequence for the same channel.
 *
 * <p>If the endpoint is not unlimited we will receive a hash on the first response.
 * Once this happens every unlimited bucket will start moving its queue to the correct bucket.
 * This is done during the queue work iteration so many requests to one endpoint would be moved correctly.
 *
 * <p>For example, the first message sending:
 * <pre>{@code
 * public void onReady(ReadyEvent event) {
 *   TextChannel channel = event.getJDA().getTextChannelById("123");
 *   for (int i = 1; i <= 100; i++) {
 *     channel.sendMessage("Message: " + i).queue();
 *   }
 * }
 * }</pre>
 *
 * <p>This will send 100 messages on startup. At this point we don't yet know the hash for this route, so we put them all in {@code unlimited+POST/channels/{channel.id}/messages:guild_id:123:webhook_id}.
 * The bucket iterates the requests in sync and gets the first response. This response provides the hash for this route, and we create a bucket for it.
 * Once the response is handled we continue with the next request in the unlimited bucket and notice the new bucket. We then move all related requests to this bucket.
 */
public final class SequentialRestRateLimiter implements RestRateLimiter
{
    private static final Logger log = JDALogger.getLog(RestRateLimiter.class);
    private static final String UNLIMITED_BUCKET = "unlimited"; // we generate an unlimited bucket for every major parameter configuration

    private final CompletableFuture<?> shutdownHandle = new CompletableFuture<>();

    private Future<?> cleanupWorker;
    private RateLimitConfig config;

    private boolean isStopped, isShutdown;

    private final ReentrantLock bucketLock = new ReentrantLock();
    // Route -> Should we print warning for 429? AKA did we already hit it once before
    private final Set<Route> hitRatelimit = ConcurrentHashMap.newKeySet(5);
    // Route -> Hash
    private final Map<Route, String> hashes = new ConcurrentHashMap<>();
    // Hash + Major Parameter -> Bucket
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    // Bucket -> Rate-Limit Worker
    private final Map<Bucket, Future<?>> rateLimitQueue = new ConcurrentHashMap<>();

    @Override
    public void init(@Nonnull RateLimitConfig config)
    {
        this.config = config;
        this.cleanupWorker = config.getPool().scheduleAtFixedRate(this::cleanup, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void enqueue(@NotNull RestRateLimiter.Work task)
    {
        Bucket bucket = getBucket(task.getRoute(), true);
        bucket.enqueue(task);
        runBucket(bucket);
    }

    @Override
    public void stop(boolean shutdown, @Nonnull Runnable callback)
    {
        if (!isStopped)
        {
            isStopped = true;
            shutdownHandle.thenRun(callback);
        }
        if (shutdown && !isShutdown)
            shutdown();
    }

    @Override
    public boolean isStopped()
    {
        return isStopped;
    }

    @Override
    public int cancelRequests()
    {
        return MiscUtil.locked(bucketLock, () -> {
            // Empty buckets will be removed by the cleanup worker, which also checks for rate limit parameters
            AtomicInteger count = new AtomicInteger(0);
            buckets.values()
                    .stream()
                    .map(Bucket::getRequests)
                    .flatMap(Collection::stream)
                    .filter(request -> !request.isPriority() && !request.isCancelled())
                    .forEach(request -> {
                        request.cancel();
                        count.incrementAndGet();
                    });

            int cancelled = count.get();
            if (cancelled == 1)
                log.warn("Cancelled 1 request!");
            else if (cancelled > 1)
                log.warn("Cancelled {} requests!", cancelled);
            return cancelled;
        });
    }

    private void shutdown()
    {
        isShutdown = true;
        cleanupWorker.cancel(false);
        shutdownHandle.complete(null);
    }

    private void cleanup()
    {
        // This will remove buckets that are no longer needed every 30 seconds to avoid memory leakage
        // We will keep the hashes in memory since they are very limited (by the amount of possible routes)
        MiscUtil.locked(bucketLock, () -> {
            int size = buckets.size();
            Iterator<Map.Entry<String, Bucket>> entries = buckets.entrySet().iterator();

            while (entries.hasNext())
            {
                Map.Entry<String, Bucket> entry = entries.next();
                String key = entry.getKey();
                Bucket bucket = entry.getValue();
                // Remove cancelled requests
                bucket.requests.removeIf(Work::isSkipped);

                // Check if the bucket is empty
                if (bucket.isUnlimited() && bucket.requests.isEmpty())
                    entries.remove(); // remove unlimited if requests are empty
                    // If the requests of the bucket are drained and the reset is expired the bucket has no valuable information
                else if (bucket.requests.isEmpty() && bucket.reset <= getNow())
                    entries.remove();
                    // Remove empty buckets when the rate limiter is stopped
                else if (bucket.requests.isEmpty() && isStopped)
                    entries.remove();
            }
            // Log how many buckets were removed
            size -= buckets.size();
            if (size > 0)
                log.debug("Removed {} expired buckets", size);
        });
    }

    private String getRouteHash(Route route)
    {
        return hashes.getOrDefault(route, UNLIMITED_BUCKET + "+" + route);
    }

    @Contract("_,true->!null")
    private Bucket getBucket(Route.CompiledRoute route, boolean create)
    {
        return MiscUtil.locked(bucketLock, () ->
        {
            // Retrieve the hash via the route
            String hash = getRouteHash(route.getBaseRoute());
            // Get or create a bucket for the hash + major parameters
            String bucketId = hash + ":" + route.getMajorParameters();
            Bucket bucket = this.buckets.get(bucketId);
            if (bucket == null && create)
                this.buckets.put(bucketId, bucket = new Bucket(bucketId));

            return bucket;
        });
    }

    private void runBucket(Bucket bucket)
    {
        if (isShutdown)
            return;
        // Schedule a new bucket worker if no worker is running
        MiscUtil.locked(bucketLock, () ->
                rateLimitQueue.computeIfAbsent(bucket,
                        (k) -> config.getPool().schedule(bucket, bucket.getRateLimit(), TimeUnit.MILLISECONDS)));
    }

    private long parseLong(String input)
    {
        return input == null ? 0L : Long.parseLong(input);
    }

    private long parseDouble(String input)
    {
        //The header value is using a double to represent milliseconds and seconds:
        // 5.250 this is 5 seconds and 250 milliseconds (5250 milliseconds)
        return input == null ? 0L : (long) (Double.parseDouble(input) * 1000);
    }

    private long getNow()
    {
        return System.currentTimeMillis();
    }

    private Bucket updateBucket(Route.CompiledRoute route, okhttp3.Response response)
    {
        return MiscUtil.locked(bucketLock, () -> {
            try
            {
                Bucket bucket = getBucket(route, true);
                Headers headers = response.headers();

                boolean global = headers.get(GLOBAL_HEADER) != null;
                boolean cloudflare = headers.get("via") == null;
                String hash = headers.get(HASH_HEADER);
                long now = getNow();

                // Create a new bucket for the hash if needed
                Route baseRoute = route.getBaseRoute();
                if (hash != null)
                {
                    if (!this.hashes.containsKey(baseRoute))
                    {
                        this.hashes.put(baseRoute, hash);
                        log.debug("Caching bucket hash {} -> {}", baseRoute, hash);
                    }

                    bucket = getBucket(route, true);
                }

                if (response.code() == 429)
                {
                    String retryAfterHeader = headers.get(RETRY_AFTER_HEADER);
                    long retryAfter = parseLong(retryAfterHeader) * 1000; // seconds precision
                    // Handle global rate limit if necessary
                    if (global)
                    {
                        config.getGlobalRateLimit().set(now + retryAfter);
                        log.error("Encountered global rate limit! Retry-After: {} ms", retryAfter);
                    }
                    // Handle cloudflare rate limits, this applies to all routes and uses seconds for retry-after
                    else if (cloudflare)
                    {
                        config.getGlobalRateLimit().set(now + retryAfter);
                        log.error("Encountered cloudflare rate limit! Retry-After: {} s", retryAfter / 1000);
                    }
                    // Handle hard rate limit, pretty much just log that it happened
                    else
                    {
                        boolean firstHit = hitRatelimit.add(baseRoute) && retryAfter < 60000;
                        // Update the bucket to the new information
                        bucket.remaining = 0;
                        bucket.reset = getNow() + retryAfter;
                        // don't log warning if we hit the rate limit for the first time, likely due to initialization of the bucket
                        // unless its a long retry-after delay (more than a minute)
                        if (firstHit)
                            log.debug("Encountered 429 on route {} with bucket {} Retry-After: {} ms", baseRoute, bucket.bucketId, retryAfter);
                        else
                            log.warn("Encountered 429 on route {} with bucket {} Retry-After: {} ms", baseRoute, bucket.bucketId, retryAfter);
                    }
                    return bucket;
                }

                // If hash is null this means we didn't get enough information to update a bucket
                if (hash == null)
                    return bucket;

                // Update the bucket parameters with new information
                String limitHeader = headers.get(LIMIT_HEADER);
                String remainingHeader = headers.get(REMAINING_HEADER);
                String resetAfterHeader = headers.get(RESET_AFTER_HEADER);
                String resetHeader = headers.get(RESET_HEADER);

                bucket.limit = (int) Math.max(1L, parseLong(limitHeader));
                bucket.remaining = (int) parseLong(remainingHeader);
                if (config.isRelative())
                    bucket.reset = now + parseDouble(resetAfterHeader);
                else
                    bucket.reset = parseDouble(resetHeader);
                log.trace("Updated bucket {} to ({}/{}, {})", bucket.bucketId, bucket.remaining, bucket.limit, bucket.reset - now);
                return bucket;
            }
            catch (Exception e)
            {
                Bucket bucket = getBucket(route, true);
                log.error("Encountered Exception while updating a bucket. Route: {} Bucket: {} Code: {} Headers:\n{}",
                        route.getBaseRoute(), bucket, response.code(), response.headers(), e);
                return bucket;
            }
        });
    }

    private class Bucket implements Runnable
    {
        private final String bucketId;
        private final Deque<Work> requests = new ConcurrentLinkedDeque<>();

        private long reset = 0;
        private int remaining = 1;
        private int limit = 1;

        public Bucket(String bucketId)
        {
            this.bucketId = bucketId;
        }

        public void enqueue(Work request)
        {
            requests.addLast(request);
        }

        public void retry(Work request)
        {
            requests.addFirst(request);
        }

        private boolean isGlobalRateLimit()
        {
            return config.getGlobalRateLimit().get() > getNow();
        }

        public long getRateLimit()
        {
            long now = getNow();
            long global = config.getGlobalRateLimit().get();
            // Global rate limit is more important to handle
            if (global > now)
                return global - now;
            // Check if the bucket reset time has expired
            if (reset <= now)
            {
                // Update the remaining uses to the limit (we don't know better)
                remaining = limit;
                return 0L;
            }

            // If there are remaining requests we don't need to do anything, otherwise return backoff in milliseconds
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
            return bucketId.startsWith("unlimited");
        }

        private void backoff()
        {
            // Schedule backoff if requests are not done
            MiscUtil.locked(bucketLock, () -> {
                rateLimitQueue.remove(this);
                if (!requests.isEmpty())
                    runBucket(this);
                else if (isStopped)
                    buckets.remove(bucketId);
                if (isStopped && buckets.isEmpty())
                    shutdown();
            });
        }

        @Override
        public void run()
        {
            log.trace("Bucket {} is running {} requests", bucketId, requests.size());
            while (!requests.isEmpty())
            {
                long rateLimit = getRateLimit();
                if (rateLimit > 0L)
                {
                    // We need to backoff since we ran out of remaining uses or hit the global rate limit
                    Work request = requests.peekFirst(); // this *should* not be null
                    String baseRoute = request != null ? request.getRoute().getBaseRoute().toString() : "N/A";
                    if (!isGlobalRateLimit() && rateLimit >= 1000 * 60 * 30) // 30 minutes
                        log.warn("Encountered long {} minutes Rate-Limit on route {}", TimeUnit.MILLISECONDS.toMinutes(rateLimit), baseRoute);
                    log.debug("Backing off {} ms for bucket {} on route {}", rateLimit, bucketId, baseRoute);
                    break;
                }

                Work request = requests.removeFirst();
                if (request.isSkipped())
                    continue;
                if (isUnlimited())
                {
                    boolean shouldSkip = MiscUtil.locked(bucketLock, () -> {
                        // Attempt moving request to correct bucket if it has been created
                        Bucket bucket = getBucket(request.getRoute(), true);
                        if (bucket != this)
                        {
                            bucket.enqueue(request);
                            runBucket(bucket);
                            return true;
                        }
                        return false;
                    });
                    if (shouldSkip) continue;
                }

                try
                {
                    Response response = request.execute();
                    if (response != null)
                        updateBucket(request.getRoute(), response);
                    if (!request.isDone())
                        retry(request);
                }
                catch (Throwable ex)
                {
                    log.error("Encountered exception trying to execute request", ex);
                    if (ex instanceof Error)
                        throw (Error) ex;
                    break;
                }
            }

            backoff();
        }

        public Queue<Work> getRequests()
        {
            return requests;
        }

        @Override
        public String toString()
        {
            return bucketId;
        }
    }
}
