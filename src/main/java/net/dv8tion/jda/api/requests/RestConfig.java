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

import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import okhttp3.Request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Configuration for REST-request handling.
 *
 * <p>This can be used to replace the {@link #setRateLimiterFactory(Function) rate-limit handling}
 * or to use a different {@link #setBaseUrl(String) base url} for requests, e.g. for mocked HTTP responses or proxies.
 */
public class RestConfig
{
    /**
     * The User-Agent used by JDA for all REST-api requests.
     */
    public static final String USER_AGENT = "DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    /**
     * The default base url used by JDA for all REST-api requests.
     * This URL uses the API version defined by {@link JDAInfo#DISCORD_REST_VERSION} (v{@value JDAInfo#DISCORD_REST_VERSION}).
     */
    public static final String DEFAULT_BASE_URL = "https://discord.com/api/v" + JDAInfo.DISCORD_REST_VERSION + "/";

    private String userAgent = USER_AGENT;
    private String baseUrl = DEFAULT_BASE_URL;
    private boolean relativeRateLimit = true;
    private Consumer<? super Request.Builder> customBuilder;
    private Function<? super RestRateLimiter.RateLimitConfig, ? extends RestRateLimiter> rateLimiter = SequentialRestRateLimiter::new;

    /**
     * Whether to use {@code X-RateLimit-Reset-After} to determine the rate-limit backoff.
     * <br>If this is disabled, the default {@link RestRateLimiter} will use the {@code X-RateLimit-Reset} header timestamp to compute the relative backoff.
     *
     * @param  relativeRateLimit
     *         True, to use relative reset after
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    public RestConfig setRelativeRateLimit(boolean relativeRateLimit)
    {
        this.relativeRateLimit = relativeRateLimit;
        return this;
    }

    /**
     * Provide a custom implementation of {@link RestRateLimiter}.
     * <br>By default, this will use the {@link SequentialRestRateLimiter}.
     *
     * @param  rateLimiter
     *         The new implementation
     *
     * @throws IllegalArgumentException
     *         If the provided rate-limiter is null
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    public RestConfig setRateLimiterFactory(@Nonnull Function<? super RestRateLimiter.RateLimitConfig, ? extends RestRateLimiter> rateLimiter)
    {
        Checks.notNull(rateLimiter, "RateLimiter");
        this.rateLimiter = rateLimiter;
        return this;
    }

    /**
     * Provide a custom base URL for REST-api requests.
     * <br>This uses {@link #DEFAULT_BASE_URL} by default.
     *
     * <p>It is important that the new URL uses the correct API version for JDA.
     * The correct version is currently {@value JDAInfo#DISCORD_REST_VERSION}.
     *
     * <p>It is not required for this URL to be HTTPS, because local proxies do not require signed connections.
     * However, if the URL points to an external server, it is highly recommended to use HTTPS for security.
     *
     * @param  baseUrl
     *         The new base url
     *
     * @throws IllegalArgumentException
     *         If the provided base url is null, empty, or not an HTTP(s) url
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    public RestConfig setBaseUrl(@Nonnull String baseUrl)
    {
        Checks.notEmpty(baseUrl, "URL");
        Checks.check(baseUrl.length() > 4 && baseUrl.substring(0, 4).equalsIgnoreCase("http"), "URL must be HTTP");
        if (baseUrl.endsWith("/"))
            this.baseUrl = baseUrl;
        else
            this.baseUrl = baseUrl + "/";
        return this;
    }

    /**
     * Provide a custom User-Agent suffix which is appended to {@link #USER_AGENT}.
     * <br>You can theoretically replace the User-Agent entirely with {@link #setCustomBuilder(Consumer)},
     * however this is not recommended as Discord blocks requests with invalid or misbehaving User-Agents.
     *
     * @param  suffix
     *         The suffix to append to the User-Agent, null to unset
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    public RestConfig setUserAgentSuffix(@Nullable String suffix)
    {
        if (suffix == null || Helpers.isBlank(suffix))
            this.userAgent = USER_AGENT;
        else
            this.userAgent = USER_AGENT + " " + suffix;
        return this;
    }

    /**
     * Provide an interceptor to update outgoing requests with custom headers or other modifications.
     * <br>Be careful not to replace any important headers, like authorization or content-type.
     * This is allowed by JDA, to allow proper use of {@link #setBaseUrl(String)} with any exotic proxy.
     *
     * <p><b>Example</b>
     * <pre>{@code
     * setCustomBuilder((request) -> {
     *     request.header("X-My-Header", "MyValue");
     * })
     * }</pre>
     *
     * @param  customBuilder
     *         The request interceptor, or null to disable
     *
     * @return The current RestConfig for chaining convenience
     */
    @Nonnull
    public RestConfig setCustomBuilder(@Nullable Consumer<? super Request.Builder> customBuilder)
    {
        this.customBuilder = customBuilder;
        return this;
    }

    /**
     * The adapted user-agent with the custom {@link #setUserAgentSuffix(String) suffix}.
     *
     * @return The user-agent
     */
    @Nonnull
    public String getUserAgent()
    {
        return userAgent;
    }

    /**
     * The configured base-url for REST-api requests.
     *
     * @return The base-url
     */
    @Nonnull
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * The configured rate-limiter implementation.
     *
     * @return The rate-limiter
     */
    @Nonnull
    public Function<? super RestRateLimiter.RateLimitConfig, ? extends RestRateLimiter> getRateLimiterFactory()
    {
        return rateLimiter;
    }

    /**
     * The custom request interceptor.
     *
     * @return The custom interceptor, or null if none is configured
     */
    @Nullable
    public Consumer<? super Request.Builder> getCustomBuilder()
    {
        return customBuilder;
    }

    /**
     * Whether to use {@code X-RateLimit-Reset-After} to determine the rate-limit backoff.
     * <br>If this is disabled, the default {@link RestRateLimiter} will use the {@code X-RateLimit-Reset} header timestamp to compute the relative backoff.
     *
     * @return True, if relative reset after is enabled
     */
    public boolean isRelativeRateLimit()
    {
        return relativeRateLimit;
    }
}
