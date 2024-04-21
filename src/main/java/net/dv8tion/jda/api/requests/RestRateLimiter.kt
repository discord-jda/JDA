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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.RestRateLimiter.Work
import net.dv8tion.jda.api.requests.Route.CompiledRoute
import okhttp3.Response
import org.jetbrains.annotations.Blocking
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.Nonnull

/**
 * Interface used to handle requests to the Discord API.
 *
 * Requests are handed to the rate-limiter via [.enqueue] and executed using [Work.execute].
 * The rate-limiter is responsible to ensure that requests do not exceed the rate-limit set by Discord.
 */
interface RestRateLimiter {
    /**
     * Enqueue a new request.
     *
     *
     * Use [Work.getRoute] to determine the correct bucket.
     *
     * @param task
     * The [Work] to enqueue
     */
    fun enqueue(@Nonnull task: Work)

    /**
     * Indication to stop accepting new requests.
     *
     * @param shutdown
     * Whether to also cancel previously queued request
     * @param callback
     * Function to call once all requests are completed, used for final cleanup
     */
    fun stop(shutdown: Boolean, @Nonnull callback: Runnable?)

    /**
     * Whether the queue has stopped accepting new requests.
     *
     * @return True, if the queue is stopped
     */
    @JvmField
    val isStopped: Boolean

    /**
     * Cancel all currently queued requests, which are not marked as [priority][Work.isPriority].
     *
     * @return The number of cancelled requests
     */
    fun cancelRequests(): Int

    /**
     * Type representing a pending request.
     *
     *
     * Use [.execute] to run the request (on the calling thread) and [.isDone] to discard it once completed.
     */
    interface Work {
        @get:Nonnull
        val route: CompiledRoute

        @get:Nonnull
        val jDA: JDA?

        /**
         * Executes the request on the calling thread (blocking).
         * <br></br>This might return null when the request has been skipped while executing.
         * Retries for certain response codes are already handled by this method.
         *
         *
         * After completion, it is advised to use [.isDone] to check whether the request should be retried.
         *
         * @return [Response] instance, used to update the rate-limit data
         */
        @Blocking
        fun execute(): Response?

        /**
         * Whether the request should be skipped.
         * <br></br>This can be caused by user cancellation.
         *
         *
         * The rate-limiter should handle by simply discarding the task without further action.
         *
         * @return True, if this request is skipped
         */
        val isSkipped: Boolean

        /**
         * Whether the request is completed.
         * <br></br>This means you should not try using [.execute] again.
         *
         * @return True, if the request has completed.
         */
        val isDone: Boolean

        /**
         * Requests marked as priority should not be cancelled.
         *
         * @return True, if this request is marked as priority
         */
        val isPriority: Boolean

        /**
         * Whether this request was cancelled.
         * <br></br>Similar to [.isSkipped], but only checks cancellation.
         *
         * @return True, if this request was cancelled
         */
        val isCancelled: Boolean

        /**
         * Cancel the request.
         * <br></br>Primarily used for [JDA.cancelRequests].
         */
        fun cancel()
    }

    /**
     * Global rate-limit store.
     * <br></br>This can be used to share the global rate-limit information between multiple instances.
     */
    interface GlobalRateLimit {
        /**
         * The current global rate-limit reset time.
         * <br></br>This is the rate-limit applied on the bot token.
         *
         * @return The timestamp when the global rate-limit expires (unix timestamp in milliseconds)
         */
        /**
         * Set the current global rate-limit reset time.
         * <br></br>This is the rate-limit applied on the bot token.
         *
         * @param timestamp
         * The timestamp when the global rate-limit expires (unix timestamp in milliseconds)
         */
        var classic: Long
        /**
         * The current cloudflare rate-limit reset time.
         * <br></br>This is the rate-limit applied on the current IP.
         *
         * @return The timestamp when the cloudflare rate-limit expires (unix timestamp in milliseconds)
         */
        /**
         * Set the current cloudflare rate-limit reset time.
         * <br></br>This is the rate-limit applied on the current IP.
         *
         * @param timestamp
         * The timestamp when the cloudflare rate-limit expires (unix timestamp in milliseconds)
         */
        var cloudflare: Long

        companion object {
            /**
             * Creates a default instance of this interface.
             * <br></br>This uses [AtomicLong] to keep track of rate-limits.
             *
             * @return The default implementation
             */
            @JvmStatic
            @Nonnull
            fun create(): GlobalRateLimit? {
                return object : GlobalRateLimit {
                    private override val classic = AtomicLong(-1)
                    private override val cloudflare = AtomicLong(-1)
                    override fun getClassic(): Long {
                        return classic.get()
                    }

                    override fun setClassic(timestamp: Long) {
                        classic.set(timestamp)
                    }

                    override fun getCloudflare(): Long {
                        return cloudflare.get()
                    }

                    override fun setCloudflare(timestamp: Long) {
                        cloudflare.set(timestamp)
                    }
                }
            }
        }
    }

    /**
     * Configuration for the rate-limiter.
     */
    class RateLimitConfig(
        /**
         * The [ScheduledExecutorService] used to schedule rate-limit tasks.
         *
         * @return The [ScheduledExecutorService]
         */
        /**
         * The [ScheduledExecutorService] used to schedule rate-limit tasks.
         *
         * @return The [ScheduledExecutorService]
         */
        @get:Nonnull
        @param:Nonnull val pool: ScheduledExecutorService,
        /**
         * The elastic [ExecutorService] used to execute rate-limit tasks.
         * <br></br>This pool can potentially scale up and down depending on use.
         *
         *
         * It is also possible that this pool is identical to [.getScheduler].
         *
         * @return The elastic [ExecutorService]
         */
        @get:Nonnull
        @param:Nonnull val elastic: ExecutorService,
        /**
         * The global rate-limit store.
         *
         * @return The global rate-limit store
         */
        @get:Nonnull
        @param:Nonnull val globalRateLimit: GlobalRateLimit,
        /**
         * Whether to use [.RESET_AFTER_HEADER].
         * <br></br>This is primarily to avoid NTP sync issues.
         *
         * @return True, if [.RESET_AFTER_HEADER] should be used instead of [.RESET_HEADER]
         */
        val isRelative: Boolean
    ) {
        /**
         * The [ScheduledExecutorService] used to schedule rate-limit tasks.
         *
         * @return The [ScheduledExecutorService]
         */

        constructor(
            @Nonnull scheduler: ScheduledExecutorService,
            @Nonnull globalRateLimit: GlobalRateLimit,
            isRelative: Boolean
        ) : this(scheduler, scheduler, globalRateLimit, isRelative)
    }

    companion object {
        /** Total time (in seconds) of when the current rate limit bucket will reset. Can have decimals to match previous millisecond ratelimit precision  */
        const val RESET_AFTER_HEADER = "X-RateLimit-Reset-After"

        /** Epoch time (seconds since 00:00:00 UTC on January 1, 1970) at which the rate limit resets  */
        const val RESET_HEADER = "X-RateLimit-Reset"

        /** The number of requests that can be made  */
        const val LIMIT_HEADER = "X-RateLimit-Limit"

        /** The number of remaining requests that can be made  */
        const val REMAINING_HEADER = "X-RateLimit-Remaining"

        /** Returned only on HTTP 429 responses if the rate limit encountered is the global rate limit (not per-route)  */
        const val GLOBAL_HEADER = "X-RateLimit-Global"

        /** A unique string denoting the rate limit being encountered (non-inclusive of top-level resources in the path)  */
        const val HASH_HEADER = "X-RateLimit-Bucket"

        /** The number of seconds to wait before submitting another request  */
        const val RETRY_AFTER_HEADER = "Retry-After"

        /** Returned only on HTTP 429 responses. Value can be user (per bot or user limit), global (per bot or user global limit), or shared (per resource limit)  */
        const val SCOPE_HEADER = "X-RateLimit-Scope"
    }
}
