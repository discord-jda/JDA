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

public class RestConfig
{
    public static final String USER_AGENT = "DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    public static final String DEFAULT_BASE_URL = "https://discord.com/api/v" + JDAInfo.DISCORD_REST_VERSION + "/";

    private String userAgent = USER_AGENT;
    private String baseUrl = DEFAULT_BASE_URL;
    private boolean relativeRateLimit = true;
    private Consumer<? super Request.Builder> customBuilder;
    private RestRateLimiter rateLimiter = new SequentialRestRateLimiter();

    @Nonnull
    public RestConfig setRelativeRateLimit(boolean relativeRateLimit)
    {
        this.relativeRateLimit = relativeRateLimit;
        return this;
    }

    @Nonnull
    public RestConfig setRateLimiter(@Nonnull RestRateLimiter rateLimiter)
    {
        Checks.notNull(rateLimiter, "RateLimiter");
        this.rateLimiter = rateLimiter;
        return this;
    }

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

    @Nonnull
    public RestConfig setUserAgentSuffix(@Nonnull String suffix)
    {
        Checks.notNull(suffix, "Suffix");
        if (Helpers.isBlank(suffix))
            this.userAgent = USER_AGENT;
        else
            this.userAgent = USER_AGENT + " " + suffix;
        return this;
    }

    @Nonnull
    public RestConfig setCustomBuilder(@Nullable Consumer<? super Request.Builder> customBuilder)
    {
        this.customBuilder = customBuilder;
        return this;
    }

    @Nonnull
    public String getUserAgent()
    {
        return userAgent;
    }

    @Nonnull
    public String getBaseUrl()
    {
        return baseUrl;
    }

    @Nonnull
    public RestRateLimiter getRateLimiter()
    {
        return rateLimiter;
    }

    @Nullable
    public Consumer<? super Request.Builder> getCustomBuilder()
    {
        return customBuilder;
    }

    public boolean isRelativeRateLimit()
    {
        return relativeRateLimit;
    }
}
