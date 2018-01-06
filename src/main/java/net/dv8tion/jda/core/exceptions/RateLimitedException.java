/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.exceptions;

import net.dv8tion.jda.core.requests.Route;

/**
 * Indicates that we received a {@code 429: Too Many Requests} response
 */
public class RateLimitedException extends Exception
{
    private final String rateLimitedRoute;
    private final long retryAfter;

    public RateLimitedException(Route.CompiledRoute route, long retryAfter)
    {
        this(route.getRatelimitRoute(), retryAfter);
    }

    public RateLimitedException(String route, long retryAfter)
    {
        super(String.format("The request was ratelimited! Retry-After: %d  Route: %s", retryAfter, route));
        this.rateLimitedRoute = route;
        this.retryAfter = retryAfter;
    }

    /**
     * The route responsible for the rate limit bucket that is used in
     * the responsible {@link net.dv8tion.jda.core.requests.RateLimiter RateLimiter}
     *
     * @return The corresponding route
     */
    public String getRateLimitedRoute()
    {
        return rateLimitedRoute;
    }

    /**
     * The back-off delay in milliseconds that should be respected
     * before trying to query the {@link #getRateLimitedRoute() route} again
     *
     * @return The back-off delay in milliseconds
     */
    public long getRetryAfter()
    {
        return retryAfter;
    }
}
