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

    void init(@Nonnull RateLimitConfig config);

    void enqueue(@Nonnull Work task);

    void stop(boolean shutdown, @Nonnull Runnable callback);

    boolean isStopped();

    int cancelRequests();

    interface Work
    {
        @Nonnull
        Route.CompiledRoute getRoute();

        @Nonnull
        JDA getJDA();

        @Nullable
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

        boolean isDone();

        boolean isPriority();

        boolean isCancelled();

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
