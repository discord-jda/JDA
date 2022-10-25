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

import net.dv8tion.jda.api.JDA;
import okhttp3.Response;
import org.jetbrains.annotations.Blocking;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ScheduledExecutorService;

public interface RestRateLimiter
{
    String RESET_AFTER_HEADER = "X-RateLimit-Reset-After";
    String RESET_HEADER = "X-RateLimit-Reset";
    String LIMIT_HEADER = "X-RateLimit-Limit";
    String REMAINING_HEADER = "X-RateLimit-Remaining";
    String GLOBAL_HEADER = "X-RateLimit-Global";
    String HASH_HEADER = "X-RateLimit-Bucket";
    String RETRY_AFTER_HEADER = "Retry-After";

    /**
     * Initialize your rate limiter with the provided configuration.
     *
     * @param config
     *        The {@link RestConfig} to use
     */
    void init(@Nonnull RateLimitConfig config);

    /**
     * Enqueue a new request.
     *
     * <p>Use {@link Work#getRoute()} to determine the correct bucket.
     *
     * @param task
     *        The {@link Work} to enqueue
     */
    void enqueue(@Nonnull Work task);

    /**
     * Indication to stop accepting new requests.
     *
     * @param shutdown
     *        Whether to also cancel previously queued request
     * @param callback
     *        Function to call once all requests are completed, used for final cleanup
     */
    void stop(boolean shutdown, @Nonnull Runnable callback);

    /**
     * Whether the queue has stopped accepting new requests.
     *
     * @return True, if the queue is stopped
     */
    boolean isStopped();

    /**
     * Cancel all currently queued requests, which are not marked as {@link Work#isPriority() priority}.
     *
     * @return The number of cancelled requests
     */
    int cancelRequests();

    /**
     * Type representing a pending request.
     *
     * <p>Use {@link #execute()} to run the request (on the calling thread) and {@link #isDone()} to discard it once completed.
     */
    interface Work
    {
        /**
         * The {@link Route.CompiledRoute compiled route} of the request.
         * <br>This is primarily used to handle rate-limit buckets.
         *
         * <p>To correctly handle rate-limits, it is recommended to use the {@link #HASH_HEADER bucket hash} header from the response.
         *
         * @return The {@link Route.CompiledRoute compiled route}
         */
        @Nonnull
        Route.CompiledRoute getRoute();

        /**
         * The JDA instance which started the request.
         *
         * @return The JDA instance
         */
        @Nonnull
        JDA getJDA();

        /**
         * Executes the request on the calling thread (blocking).
         * <br>This might return null when the request has been skipped while executing.
         * Retries for certain response codes are already handled by this method.
         *
         * <p>After completion, it is advised to use {@link #isDone()} to check whether the request should be retried.
         *
         * @return {@link Response} instance, used to update the rate-limit data
         */
        @Nullable
        @Blocking
        Response execute();

        /**
         * Whether the request should be skipped.
         * <br>This can be caused by user cancellation.
         *
         * <p>The rate-limiter should handle by simply discarding the task without further action.
         *
         * @return True, if this request is skipped
         */
        boolean isSkipped();

        /**
         * Whether the request is completed.
         * <br>This means you should not try using {@link #execute()} again.
         *
         * @return True, if the request has completed.
         */
        boolean isDone();

        /**
         * Requests marked as priority should not be cancelled.
         *
         * @return True, if this request is marked as priority
         */
        boolean isPriority();

        /**
         * Whether this request was cancelled.
         * <br>Similar to {@link #isSkipped()}, but only checks cancellation.
         *
         * @return True, if this request was cancelled
         */
        boolean isCancelled();

        /**
         * Cancel the request.
         * <br>Primarily used for {@link JDA#cancelRequests()}.
         */
        void cancel();
    }

    interface GlobalRateLimit
    {
        long get();
        void set(long rateLimit);
    }

    class RateLimitConfig
    {
        private final ScheduledExecutorService pool;
        private final GlobalRateLimit globalRateLimit;
        private final boolean isRelative;

        public RateLimitConfig(@Nonnull ScheduledExecutorService pool, @Nonnull GlobalRateLimit globalRateLimit, boolean isRelative)
        {
            this.pool = pool;
            this.globalRateLimit = globalRateLimit;
            this.isRelative = isRelative;
        }

        @Nonnull
        public ScheduledExecutorService getPool()
        {
            return pool;
        }

        @Nonnull
        public GlobalRateLimit getGlobalRateLimit()
        {
            return globalRateLimit;
        }

        public boolean isRelative()
        {
            return isRelative;
        }
    }
}
