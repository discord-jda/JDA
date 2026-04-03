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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Collector interface for REST and rate-limit metrics.
 * <br>This can be used as a bridge to metrics backends such as Micrometer or OpenTelemetry.
 *
 * <p>Configure this collector through {@link RestConfig#setMetricsCollector(RestMetricsCollector)}.
 * Events are emitted on request and rate-limit execution threads and should avoid blocking behavior.
 */
public interface RestMetricsCollector {
    /**
     * Called for REST request executions.
     *
     * @param metric
     *        The request metric
     */
    void onRequest(@Nonnull RequestMetric metric);

    /**
     * Called for rate-limit updates and backoff events.
     *
     * @param event
     *        The rate-limit event
     */
    default void onRateLimit(@Nonnull RestRateLimiter.RateLimitEvent event) {}

    /**
     * Request execution metric model.
     */
    class RequestMetric {
        private final JDA jda;
        private final Route.CompiledRoute route;
        private final int statusCode;
        private final int attempts;
        private final long durationMillis;
        private final boolean success;
        private final boolean queued;
        private final Throwable error;

        public RequestMetric(
                @Nonnull JDA jda,
                @Nonnull Route.CompiledRoute route,
                int statusCode,
                int attempts,
                long durationMillis,
                boolean success,
                boolean queued,
                @Nullable Throwable error) {
            this.jda = jda;
            this.route = route;
            this.statusCode = statusCode;
            this.attempts = attempts;
            this.durationMillis = durationMillis;
            this.success = success;
            this.queued = queued;
            this.error = error;
        }

        /**
         * The JDA instance which executed this request.
         *
         * @return The JDA instance
         */
        @Nonnull
        public JDA getJDA() {
            return jda;
        }

        /**
         * The route for this request execution.
         *
         * @return The compiled route
         */
        @Nonnull
        public Route.CompiledRoute getRoute() {
            return route;
        }

        /**
         * HTTP response status code.
         * <br>Returns {@code -1} when no HTTP response was received.
         *
         * @return The HTTP status code, or {@code -1}
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * Number of HTTP attempts used for this execution.
         *
         * @return The attempt count
         */
        public int getAttempts() {
            return attempts;
        }

        /**
         * End-to-end execution time in milliseconds.
         *
         * @return Duration in milliseconds
         */
        public long getDurationMillis() {
            return durationMillis;
        }

        /**
         * Whether the request completed with a non-error status code.
         * <br>This uses the HTTP status class and does not reflect business-level success.
         *
         * @return True, if status code is below 400
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Whether the request was executed through the queue/rate-limiter.
         *
         * @return True, if the request was queued
         */
        public boolean isQueued() {
            return queued;
        }

        /**
         * The execution error if no HTTP response could be received.
         *
         * @return The error, or null
         */
        @Nullable
        public Throwable getError() {
            return error;
        }
    }
}
