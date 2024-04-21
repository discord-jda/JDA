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
package net.dv8tion.jda.api.requests

import net.dv8tion.jda.api.requests.RestRateLimiter.RateLimitConfig
import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.utils.JDALogger
import okhttp3.Response
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import java.util.function.Supplier
import javax.annotation.Nonnull
import kotlin.math.max

/**
 * A bucket is determined via the Path+Method+Major in the following way:
 *
 *  1. Get Hash from Path+Method (we call this route)
 *  1. Get bucket from Hash+Major (we call this bucketid)
 *
 *
 *
 * If no hash is known we default to the constant "uninit" hash. The hash is loaded from HTTP responses using the "X-RateLimit-Bucket" response header.
 * This hash is per Method+Path and can be stored indefinitely once received.
 * Some endpoints don't return a hash, this means that the endpoint is **uninit** and will be in queue with only the major parameter.
 *
 *
 * To explain this further, lets look at the example of message history. The endpoint to fetch message history is `GET/channels/{channel.id}/messages`.
 * This endpoint does not have any rate limit (uninit) and will thus use the hash `uninit+GET/channels/{channel.id}/messages`.
 * The bucket id for this will be `uninit+GET/channels/{channel.id}/messages:channel_id={channel.id}` where `{channel.id}` would be replaced with the respective id.
 * This means you can fetch history concurrently for multiple channels, but it will be in sequence for the same channel.
 *
 *
 * If the endpoint is not uninit we will receive a hash on the first response.
 * Once this happens every uninit bucket will start moving its queue to the correct bucket.
 * This is done during the queue work iteration so many requests to one endpoint would be moved correctly.
 *
 *
 * For example, the first message sending:
 * <pre>`public void onReady(ReadyEvent event) {
 * TextChannel channel = event.getJDA().getTextChannelById("123");
 * for (int i = 1; i <= 100; i++) {
 * channel.sendMessage("Message: " + i).queue();
 * }
 * }
`</pre> *
 *
 *
 * This will send 100 messages on startup. At this point we don't yet know the hash for this route, so we put them all in `uninit+POST/channels/{channel.id}/messages:channel_id=123`.
 * The bucket iterates the requests in sync and gets the first response. This response provides the hash for this route, and we create a bucket for it.
 * Once the response is handled we continue with the next request in the uninit bucket and notice the new bucket. We then move all related requests to this bucket.
 */
class SequentialRestRateLimiter(@param:Nonnull private val config: RateLimitConfig) : RestRateLimiter {
    private val shutdownHandle: CompletableFuture<*> = CompletableFuture<Any>()
    private val cleanupWorker: Future<*>
    override var isStopped = false
        private set
    private var isShutdown = false
    private val lock = ReentrantLock()

    // Route -> Should we print warning for 429? AKA did we already hit it once before
    private val hitRatelimit: MutableSet<Route?> = HashSet(5)

    // Route -> Hash
    private val hashes: MutableMap<Route?, String> = HashMap()

    // Hash + Major Parameter -> Bucket
    private val buckets: MutableMap<String, Bucket> = HashMap()

    // Bucket -> Rate-Limit Worker
    private val rateLimitQueue: MutableMap<Bucket, Future<*>> = HashMap()

    init {
        cleanupWorker = config.scheduler.scheduleAtFixedRate({ cleanup() }, 30, 30, TimeUnit.SECONDS)
    }

    override fun enqueue(@Nonnull task: RestRateLimiter.Work) {
        MiscUtil.locked(lock) {
            val bucket = getBucket(task.getRoute())
            bucket.enqueue(task)
            runBucket(bucket)
        }
    }

    override fun stop(shutdown: Boolean, @Nonnull callback: Runnable?) {
        MiscUtil.locked(lock) {
            var doShutdown = shutdown
            if (!isStopped) {
                isStopped = true
                shutdownHandle.thenRun(callback)
                if (!doShutdown) {
                    val count = buckets.values.stream()
                        .mapToInt { bucket: Bucket -> bucket.getRequests().size }
                        .sum()
                    if (count > 0) log.info("Waiting for {} requests to finish.", count)
                    doShutdown = count == 0
                }
            }
            if (doShutdown && !isShutdown) shutdown()
        }
    }

    override fun cancelRequests(): Int {
        return MiscUtil.locked<Int>(lock) {

            // Empty buckets will be removed by the cleanup worker, which also checks for rate limit parameters
            val cancelled = buckets.values
                .stream()
                .map { obj: Bucket -> obj.getRequests() }
                .flatMap { obj: Queue<RestRateLimiter.Work> -> obj.stream() }
                .filter { request: RestRateLimiter.Work -> !request.isPriority() && !request.isCancelled() }
                .peek { obj: RestRateLimiter.Work -> obj.cancel() }
                .count().toInt()
            if (cancelled == 1) log.warn("Cancelled 1 request!") else if (cancelled > 1) log.warn(
                "Cancelled {} requests!",
                cancelled
            )
            cancelled
        }
    }

    private fun shutdown() {
        isShutdown = true
        cleanupWorker.cancel(false)
        cleanup()
        shutdownHandle.complete(null)
    }

    private fun cleanup() {
        // This will remove buckets that are no longer needed every 30 seconds to avoid memory leakage
        // We will keep the hashes in memory since they are very limited (by the amount of possible routes)
        MiscUtil.locked(lock) {
            var size = buckets.size
            val entries: MutableIterator<Map.Entry<String, Bucket>> = buckets.entries.iterator()
            while (entries.hasNext()) {
                val (_, bucket) = entries.next()
                if (isShutdown) bucket.requests.forEach(Consumer { obj: RestRateLimiter.Work -> obj.cancel() }) // Cancel all requests
                bucket.requests.removeIf { obj: RestRateLimiter.Work -> obj.isSkipped() } // Remove cancelled requests

                // Check if the bucket is empty
                if (bucket.requests.isEmpty() && !rateLimitQueue.containsKey(bucket)) {
                    // remove uninit if requests are empty
                    if (bucket.isUninit()) entries.remove() else if (bucket.reset <= now) entries.remove() else if (isStopped) entries.remove()
                }
            }

            // Log how many buckets were removed
            size -= buckets.size
            if (size > 0) log.debug("Removed {} expired buckets", size) else if (isStopped && !isShutdown) shutdown()
        }
    }

    private fun getRouteHash(route: Route?): String {
        return hashes.getOrDefault(route, UNINIT_BUCKET + "+" + route)
    }

    private fun getBucket(route: CompiledRoute?): Bucket {
        return MiscUtil.locked<Bucket>(lock) {

            // Retrieve the hash via the route
            val hash = getRouteHash(route.getBaseRoute())
            // Get or create a bucket for the hash + major parameters
            val bucketId = hash + ":" + route.getMajorParameters()
            buckets.computeIfAbsent(bucketId) { id: String ->
                if (route.getBaseRoute().isInteractionBucket) return@computeIfAbsent InteractionBucket(
                    id
                ) else return@computeIfAbsent ClassicBucket(id)
            }
        }
    }

    private fun scheduleElastic(bucket: Bucket) {
        if (isShutdown) return
        val elastic = config.elastic
        val scheduler = config.scheduler
        try {
            // Avoid context switch if unnecessary
            if (elastic === scheduler) bucket.run() else elastic.execute(bucket)
        } catch (ex: RejectedExecutionException) {
            if (!isShutdown) log.error("Failed to execute bucket worker", ex)
        } catch (t: Throwable) {
            log.error("Caught throwable in bucket worker", t)
            if (t is Error) throw t
        }
    }

    private fun runBucket(bucket: Bucket) {
        if (isShutdown) return
        // Schedule a new bucket worker if no worker is running
        MiscUtil.locked(lock, Supplier<Future<Any>> {
            rateLimitQueue.computeIfAbsent(
                bucket
            ) { k: Bucket? ->
                config.scheduler.schedule(
                    { scheduleElastic(bucket) },
                    bucket.getRateLimit(), TimeUnit.MILLISECONDS
                )
            }
        }
        )
    }

    private fun parseLong(input: String?): Long {
        return input?.toLong() ?: 0L
    }

    private fun parseDouble(input: String?): Long {
        //The header value is using a double to represent milliseconds and seconds:
        // 5.250 this is 5 seconds and 250 milliseconds (5250 milliseconds)
        return if (input == null) 0L else (input.toDouble() * 1000).toLong()
    }

    private val now: Long
        private get() = System.currentTimeMillis()

    private fun updateBucket(route: CompiledRoute?, response: Response): Bucket {
        return MiscUtil.locked<Bucket>(lock) {
            try {
                var bucket = getBucket(route)
                val headers = response.headers()
                val global = headers[RestRateLimiter.Companion.GLOBAL_HEADER] != null
                val cloudflare = headers["via"] == null
                val hash = headers[RestRateLimiter.Companion.HASH_HEADER]
                val scope = headers[RestRateLimiter.Companion.SCOPE_HEADER]
                val now = now

                // Create a new bucket for the hash if needed
                val baseRoute = route.getBaseRoute()
                if (hash != null) {
                    if (!hashes.containsKey(baseRoute)) {
                        hashes[baseRoute] = hash
                        log.debug("Caching bucket hash {} -> {}", baseRoute, hash)
                    }
                    bucket = getBucket(route)
                }
                if (response.code() == 429) {
                    val retryAfterHeader = headers[RestRateLimiter.Companion.RETRY_AFTER_HEADER]
                    val retryAfter = parseLong(retryAfterHeader) * 1000 // seconds precision
                    // Handle global rate limit if necessary
                    if (global) {
                        config.globalRateLimit.setClassic(now + retryAfter)
                        log.error("Encountered global rate limit! Retry-After: {} ms Scope: {}", retryAfter, scope)
                    } else if (cloudflare) {
                        config.globalRateLimit.setCloudflare(now + retryAfter)
                        log.error("Encountered cloudflare rate limit! Retry-After: {} s", retryAfter / 1000)
                    } else {
                        val firstHit = hitRatelimit.add(baseRoute) && retryAfter < 60000
                        // Update the bucket to the new information
                        bucket.remaining = 0
                        bucket.reset = now + retryAfter
                        // don't log warning if we hit the rate limit for the first time, likely due to initialization of the bucket
                        // unless its a long retry-after delay (more than a minute)
                        if (firstHit) log.debug(
                            "Encountered 429 on route {} with bucket {} Retry-After: {} ms Scope: {}",
                            baseRoute,
                            bucket.bucketId,
                            retryAfter,
                            scope
                        ) else log.warn(
                            "Encountered 429 on route {} with bucket {} Retry-After: {} ms Scope: {}",
                            baseRoute,
                            bucket.bucketId,
                            retryAfter,
                            scope
                        )
                    }
                    log.trace("Updated bucket {} to retry after {}", bucket.bucketId, bucket.reset - now)
                    return@locked bucket
                }

                // If hash is null this means we didn't get enough information to update a bucket
                if (hash == null) return@locked bucket

                // Update the bucket parameters with new information
                val limitHeader = headers[RestRateLimiter.Companion.LIMIT_HEADER]
                val remainingHeader = headers[RestRateLimiter.Companion.REMAINING_HEADER]
                val resetAfterHeader = headers[RestRateLimiter.Companion.RESET_AFTER_HEADER]
                val resetHeader = headers[RestRateLimiter.Companion.RESET_HEADER]

//                bucket.limit = (int) Math.max(1L, parseLong(limitHeader));
                bucket.remaining = parseLong(remainingHeader).toInt()
                if (config.isRelative) bucket.reset = now + parseDouble(resetAfterHeader) else bucket.reset =
                    parseDouble(resetHeader)
                log.trace(
                    "Updated bucket {} to ({}/{}, {})",
                    bucket.bucketId,
                    bucket.remaining,
                    limitHeader,
                    bucket.reset - now
                )
                return@locked bucket
            } catch (e: Exception) {
                val bucket = getBucket(route)
                log.error(
                    "Encountered Exception while updating a bucket. Route: {} Bucket: {} Code: {} Headers:\n{}",
                    route.getBaseRoute(), bucket, response.code(), response.headers(), e
                )
                return@locked bucket
            }
        }
    }

    private abstract inner class Bucket(val bucketId: String) : Runnable {
        val requests: Deque<RestRateLimiter.Work> = ConcurrentLinkedDeque()
        var reset: Long = 0
        var remaining = 1

        val isUninit: Boolean
            get() = bucketId.startsWith(UNINIT_BUCKET)

        fun enqueue(request: RestRateLimiter.Work) {
            requests.addLast(request)
        }

        fun retry(request: RestRateLimiter.Work) {
            if (!moveRequest(request)) requests.addFirst(request)
        }

        abstract fun getGlobalRateLimit(now: Long): Long
        val rateLimit: Long
            get() {
                val now: Long = now
                val global = getGlobalRateLimit(now)

                // Check if the bucket reset time has expired
                if (reset <= now) {
                    // Update the remaining uses to the limit (we don't know better)
                    remaining = 1
                }

                // If there are remaining requests we don't need to do anything, otherwise return backoff in milliseconds
                return max(global.toDouble(), (if (remaining < 1) reset - now else 0L).toDouble()).toLong()
            }
        protected val isGlobalRateLimit: Boolean
            protected get() = getGlobalRateLimit(now) > 0

        protected fun backoff() {
            // Schedule backoff if requests are not done
            MiscUtil.locked(lock) {
                rateLimitQueue.remove(this)
                if (!requests.isEmpty()) runBucket(this) else if (isStopped) buckets.remove(bucketId)
                if (isStopped && buckets.isEmpty()) shutdown()
            }
        }

        fun getRequests(): Queue<RestRateLimiter.Work> {
            return requests
        }

        protected fun moveRequest(request: RestRateLimiter.Work): Boolean {
            return MiscUtil.locked<Boolean>(lock) {

                // Attempt moving request to correct bucket if it has been created
                val bucket = getBucket(request.getRoute())
                if (bucket !== this) {
                    bucket.enqueue(request)
                    runBucket(bucket)
                }
                bucket !== this
            }
        }

        protected fun execute(request: RestRateLimiter.Work): Boolean {
            try {
                val response = request.execute()
                if (response != null) updateBucket(request.getRoute(), response)
                if (!request.isDone()) retry(request)
            } catch (ex: Throwable) {
                log.error("Encountered exception trying to execute request", ex)
                if (ex is Error) throw ex
                return true
            }
            return false
        }

        override fun run() {
            log.trace("Bucket {} is running {} requests", bucketId, requests.size)
            while (!requests.isEmpty()) {
                val rateLimit: Long = getRateLimit()
                if (rateLimit > 0L) {
                    // We need to backoff since we ran out of remaining uses or hit the global rate limit
                    val request = requests.peekFirst() // this *should* not be null
                    val baseRoute = request?.getRoute()?.baseRoute?.toString() ?: "N/A"
                    if (!isGlobalRateLimit() && rateLimit >= 1000 * 60 * 30) // 30 minutes
                        log.warn(
                            "Encountered long {} minutes Rate-Limit on route {}",
                            TimeUnit.MILLISECONDS.toMinutes(rateLimit),
                            baseRoute
                        )
                    log.debug("Backing off {} ms for bucket {} on route {}", rateLimit, bucketId, baseRoute)
                    break
                }
                val request = requests.removeFirst()
                if (request.isSkipped()) continue
                if (isUninit() && moveRequest(request)) continue
                if (execute(request)) break
            }
            backoff()
        }

        override fun toString(): String {
            return bucketId
        }

        override fun hashCode(): Int {
            return bucketId.hashCode()
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) return true
            return if (obj !is Bucket) false else bucketId == obj.bucketId
        }
    }

    private inner class ClassicBucket(bucketId: String) : Bucket(bucketId) {
        override fun getGlobalRateLimit(now: Long): Long {
            val holder = config.globalRateLimit
            val global: Long = max(holder.getClassic().toDouble(), holder.getCloudflare().toDouble())
            return global - now
        }
    }

    private inner class InteractionBucket(@Nonnull bucketId: String) : Bucket(bucketId) {
        override fun getGlobalRateLimit(now: Long): Long {
            // Only cloudflare bans apply to interactions
            return config.globalRateLimit.getCloudflare() - now
        }
    }

    companion object {
        private val log = JDALogger.getLog(RestRateLimiter::class.java)
        private const val UNINIT_BUCKET =
            "uninit" // we generate an uninit bucket for every major parameter configuration
    }
}
