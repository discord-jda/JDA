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

import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.requests.RestRateLimiter.RateLimitConfig
import net.dv8tion.jda.internal.utils.*
import java.util.function.Consumer
import java.util.function.Function
import javax.annotation.Nonnull

/**
 * Configuration for REST-request handling.
 *
 *
 * This can be used to replace the [rate-limit handling][.setRateLimiterFactory]
 * or to use a different [base url][.setBaseUrl] for requests, e.g. for mocked HTTP responses or proxies.
 */
class RestConfig {
    /**
     * The adapted user-agent with the custom [suffix][.setUserAgentSuffix].
     *
     * @return The user-agent
     */
    @get:Nonnull
    var userAgent = USER_AGENT
        private set

    /**
     * The configured base-url for REST-api requests.
     *
     * @return The base-url
     */
    @get:Nonnull
    var baseUrl = DEFAULT_BASE_URL
        private set

    /**
     * Whether to use `X-RateLimit-Reset-After` to determine the rate-limit backoff.
     * <br></br>If this is disabled, the default [RestRateLimiter] will use the `X-RateLimit-Reset` header timestamp to compute the relative backoff.
     *
     * @return True, if relative reset after is enabled
     */
    var isRelativeRateLimit = true
        private set
    private var customBuilder: Consumer<in Builder?>? = null

    /**
     * The configured rate-limiter implementation.
     *
     * @return The rate-limiter
     */
    @get:Nonnull
    var rateLimiterFactory: Function<in RateLimitConfig, out RestRateLimiter> =
        Function<RateLimitConfig, RestRateLimiter> { config: RateLimitConfig -> SequentialRestRateLimiter(config) }
        private set

    /**
     * Whether to use `X-RateLimit-Reset-After` to determine the rate-limit backoff.
     * <br></br>If this is disabled, the default [RestRateLimiter] will use the `X-RateLimit-Reset` header timestamp to compute the relative backoff.
     *
     * @param  relativeRateLimit
     * True, to use relative reset after
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    fun setRelativeRateLimit(relativeRateLimit: Boolean): RestConfig {
        isRelativeRateLimit = relativeRateLimit
        return this
    }

    /**
     * Provide a custom implementation of [RestRateLimiter].
     * <br></br>By default, this will use the [SequentialRestRateLimiter].
     *
     * @param  rateLimiter
     * The new implementation
     *
     * @throws IllegalArgumentException
     * If the provided rate-limiter is null
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    fun setRateLimiterFactory(@Nonnull rateLimiter: Function<in RateLimitConfig, out RestRateLimiter>): RestConfig {
        Checks.notNull(rateLimiter, "RateLimiter")
        rateLimiterFactory = rateLimiter
        return this
    }

    /**
     * Provide a custom base URL for REST-api requests.
     * <br></br>This uses [.DEFAULT_BASE_URL] by default.
     *
     *
     * It is important that the new URL uses the correct API version for JDA.
     * The correct version is currently {@value JDAInfo#DISCORD_REST_VERSION}.
     *
     *
     * It is not required for this URL to be HTTPS, because local proxies do not require signed connections.
     * However, if the URL points to an external server, it is highly recommended to use HTTPS for security.
     *
     * @param  baseUrl
     * The new base url
     *
     * @throws IllegalArgumentException
     * If the provided base url is null, empty, or not an HTTP(s) url
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    fun setBaseUrl(@Nonnull baseUrl: String): RestConfig {
        Checks.notEmpty(baseUrl, "URL")
        Checks.check(
            baseUrl.length > 4 && baseUrl.substring(0, 4).equals("http", ignoreCase = true),
            "URL must be HTTP"
        )
        if (baseUrl.endsWith("/")) this.baseUrl = baseUrl else this.baseUrl = "$baseUrl/"
        return this
    }

    /**
     * Provide a custom User-Agent suffix which is appended to [.USER_AGENT].
     * <br></br>You can theoretically replace the User-Agent entirely with [.setCustomBuilder],
     * however this is not recommended as Discord blocks requests with invalid or misbehaving User-Agents.
     *
     * @param  suffix
     * The suffix to append to the User-Agent, null to unset
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    fun setUserAgentSuffix(suffix: String?): RestConfig {
        if (suffix == null || Helpers.isBlank(suffix)) userAgent = USER_AGENT else userAgent = USER_AGENT + " " + suffix
        return this
    }

    /**
     * Provide an interceptor to update outgoing requests with custom headers or other modifications.
     * <br></br>Be careful not to replace any important headers, like authorization or content-type.
     * This is allowed by JDA, to allow proper use of [.setBaseUrl] with any exotic proxy.
     *
     *
     * **Example**
     * <pre>`setCustomBuilder((request) -> {
     * request.header("X-My-Header", "MyValue");
     * })
    `</pre> *
     *
     * @param  customBuilder
     * The request interceptor, or null to disable
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    fun setCustomBuilder(customBuilder: Consumer<in Builder?>?): RestConfig {
        this.customBuilder = customBuilder
        return this
    }

    /**
     * The custom request interceptor.
     *
     * @return The custom interceptor, or null if none is configured
     */
    fun getCustomBuilder(): Consumer<in Builder?>? {
        return customBuilder
    }

    companion object {
        /**
         * The User-Agent used by JDA for all REST-api requests.
         */
        @JvmField
        val USER_AGENT = "DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")"

        /**
         * The default base url used by JDA for all REST-api requests.
         * This URL uses the API version defined by [JDAInfo.DISCORD_REST_VERSION] (v{@value JDAInfo#DISCORD_REST_VERSION}).
         */
        const val DEFAULT_BASE_URL = "https://discord.com/api/v" + JDAInfo.DISCORD_REST_VERSION + "/"
    }
}
