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
package net.dv8tion.jda.api.exceptions

import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.internal.utils.Helpers

/**
 * Indicates that we received a `429: Too Many Requests` response
 */
class RateLimitedException(
    /**
     * The route responsible for the rate limit bucket that is used in
     * the responsible RateLimiter
     *
     * @return The corresponding route
     */
    val rateLimitedRoute: String,
    /**
     * The back-off delay in milliseconds that should be respected
     * before trying to query the [route][.getRateLimitedRoute] again
     *
     * @return The back-off delay in milliseconds
     */
    val retryAfter: Long
) : Exception(Helpers.format("The request was ratelimited! Retry-After: %d  Route: %s", retryAfter, rateLimitedRoute)) {

    constructor(route: CompiledRoute, retryAfter: Long) : this(
        route.baseRoute.route + ":" + route.majorParameters,
        retryAfter
    )
}
