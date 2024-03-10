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

import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.JDA;
import okhttp3.Response;
import org.jetbrains.annotations.Blocking;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interface used to handle requests to the Discord API.
 * <p>Requests are handed to the rate-limiter via {@link #enqueue(Work)} and executed using {@link Work#execute()}.
 * The rate-limiter is responsible to ensure that requests do not exceed the rate-limit set by Discord.
 */
public interface RestRateLimiter
{
    /** Total time (in seconds) of when the current rate limit bucket will reset. Can have decimals to match previous millisecond ratelimit precision */
    String RESET_AFTER_HEADER = "X-RateLimit-Reset-After";
    /** Epoch time (seconds since 00:00:00 UTC on January 1, 1970) at which the rate limit resets */
    String RESET_HEADER = "X-RateLimit-Reset";
    /** The number of requests that can be made */
    String LIMIT_HEADER = "X-RateLimit-Limit";
    /** The number of remaining requests that can be made */
    String REMAINING_HEADER = "X-RateLimit-Remaining";
    /** Returned only on HTTP 429 responses if the rate limit encountered is the global rate limit (not per-route) */
    String GLOBAL_HEADER = "X-RateLimit-Global";
    /** A unique string denoting the rate limit being encountered (non-inclusive of top-level resources in the path) */
    String HASH_HEADER = "X-RateLimit-Bucket";
    /** The number of seconds to wait before submitting another request */
    String RETRY_AFTER_HEADER = "Retry-After";
    /** Returned only on HTTP 429 responses. Value can be user (per bot or user limit), global (per bot or user global limit), or shared (per resource limit) */
    String SCOPE_HEADER = "X-RateLimit-Scope";

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

    /**
     * Global rate-limit store.
     * <br>This can be used to share the global rate-limit information between multiple instances.
     */
    interface GlobalRateLimit
    {
        /**
         * The current global rate-limit reset time.
         * <br>This is the rate-limit applied on the bot token.
         *
         * @return The timestamp when the global rate-limit expires (unix timestamp in milliseconds)
         */
        long getClassic();

        /**
         * Set the current global rate-limit reset time.
         * <br>This is the rate-limit applied on the bot token.
         *
         * @param timestamp
         *        The timestamp when the global rate-limit expires (unix timestamp in milliseconds)
         */
        void setClassic(long timestamp);

        /**
         * The current cloudflare rate-limit reset time.
         * <br>This is the rate-limit applied on the current IP.
         *
         * @return The timestamp when the cloudflare rate-limit expires (unix timestamp in milliseconds)
         */
        long getCloudflare();

        /**
         * Set the current cloudflare rate-limit reset time.
         * <br>This is the rate-limit applied on the current IP.
         *
         * @param timestamp
         *        The timestamp when the cloudflare rate-limit expires (unix timestamp in milliseconds)
         */
        void setCloudflare(long timestamp);

        /**
         * Creates a default instance of this interface.
         * <br>This uses {@link AtomicLong} to keep track of rate-limits.
         *
         * @return The default implementation
         */
        @Nonnull
        static GlobalRateLimit create()
        {
            return new GlobalRateLimit()
            {
                private final AtomicLong classic = new AtomicLong(-1);
                private final AtomicLong cloudflare = new AtomicLong(-1);

                @Override
                public long getClassic()
                {
                    return classic.get();
                }

                @Override
                public void setClassic(long timestamp)
                {
                    classic.set(timestamp);
                }

                @Override
                public long getCloudflare()
                {
                    return cloudflare.get();
                }

                @Override
                public void setCloudflare(long timestamp)
                {
                    cloudflare.set(timestamp);
                }
            };
        }
    }

    /**
     * Configuration for the rate-limiter.
     */
    class RateLimitConfig
    {
        private final ScheduledExecutorService scheduler;
        private final ExecutorService elastic;
        private final GlobalRateLimit globalRateLimit;
        private final boolean isRelative;

        public RateLimitConfig(@Nonnull ScheduledExecutorService scheduler, @Nonnull GlobalRateLimit globalRateLimit, boolean isRelative)
        {
            this(scheduler, scheduler, globalRateLimit, isRelative);
        }

        public RateLimitConfig(@Nonnull ScheduledExecutorService scheduler, @Nonnull ExecutorService elastic, @Nonnull GlobalRateLimit globalRateLimit, boolean isRelative)
        {
            this.scheduler = scheduler;
            this.elastic = elastic;
            this.globalRateLimit = globalRateLimit;
            this.isRelative = isRelative;
        }

        /**
         * The {@link ScheduledExecutorService} used to schedule rate-limit tasks.
         *
         * @return The {@link ScheduledExecutorService}
         */
        @Nonnull
        @Deprecated
        @ReplaceWith("getScheduler() or getElastic()")
        public ScheduledExecutorService getPool()
        {
            return scheduler;
        }

        /**
         * The {@link ScheduledExecutorService} used to schedule rate-limit tasks.
         *
         * @return The {@link ScheduledExecutorService}
         */
        @Nonnull
        public ScheduledExecutorService getScheduler()
        {
            return scheduler;
        }

        /**
         * The elastic {@link ExecutorService} used to execute rate-limit tasks.
         * <br>This pool can potentially scale up and down depending on use.
         *
         * <p>It is also possible that this pool is identical to {@link #getScheduler()}.
         *
         * @return The elastic {@link ExecutorService}
         */
        @Nonnull
        public ExecutorService getElastic()
        {
            return elastic;
        }

        /**
         * The global rate-limit store.
         *
         * @return The global rate-limit store
         */
        @Nonnull
        public GlobalRateLimit getGlobalRateLimit()
        {
            return globalRateLimit;
        }

        /**
         * Whether to use {@link #RESET_AFTER_HEADER}.
         * <br>This is primarily to avoid NTP sync issues.
         *
         * @return True, if {@link #RESET_AFTER_HEADER} should be used instead of {@link #RESET_HEADER}
         */
        public boolean isRelative()
        {
            return isRelative;
        }
    }
}
