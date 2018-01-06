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

package net.dv8tion.jda.webhook;

import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import okhttp3.OkHttpClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder that creates a new {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient} instance
 */
public class WebhookClientBuilder
{
    public static final OkHttpClient.Builder DEFAULT_HTTP_BUILDER = new OkHttpClient.Builder();
    private static final Pattern WEBHOOK_PATTERN = Pattern.compile("(?:https?://)?(?:\\w+\\.)?discordapp\\.com/api(?:/v\\d+)?/webhooks/(\\d+)/([\\w-]+)(?:/(?:\\w+)?)?");

    protected final long id;
    protected final String token;
    protected ScheduledExecutorService pool;
    protected OkHttpClient.Builder builder;
    protected OkHttpClient client;
    protected ThreadFactory threadFactory;
    protected boolean isDaemon;

    /**
     * Creates a new WebhookClientBuilder with the provided id and token
     *
     * @param  id
     *         The snowflake id of the target webhook
     * @param  token
     *         The authorization token of the target webhook
     *         <br><b>This is not a bot/client token!</b>
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided token is {@code null}
     *         or contains any whitespace!
     */
    public WebhookClientBuilder(final long id, final String token)
    {
        Checks.noWhitespace(token, "Token");
        this.id = id;
        this.token = token;
    }

    /**
     * Creates a new WebhookClientBuilder with the provided webhook URL
     *
     * @param  url
     *         The URL of the webhook. May be directly copied from Discord's UI
     *         <br>Example: {@code https://discordapp.com/api/webhooks/123456789012345678/my-webhook-token}
     *         <br>This constructor also parses URLs pointing to subdomains of {@code discordapp.com}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided URL is {@code null}
     *         or is incorrectly formatted
     */
    public WebhookClientBuilder(@Nonnull String url)
    {
        Matcher matcher = WEBHOOK_PATTERN.matcher(url);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Failed to parse webhook URL");
        }

        this.id = MiscUtil.parseSnowflake(matcher.group(1));
        this.token = matcher.group(2);
    }

    /**
     * Creates a new WebhookClientBuilder with the provided id and token
     *
     * @param  webhook
     *         The target {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *
     * @throws java.lang.NullPointerException
     *         If the provided {@link net.dv8tion.jda.core.entities.Webhook Webhook} is {@code null}
     */
    public WebhookClientBuilder(@Nonnull Webhook webhook)
    {
        this(webhook.getIdLong(), webhook.getToken());
    }

    /**
     * The {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService}
     * that should be used to handle rate limits!
     * <br>By default this creates a new executor with 1 core thread!
     *
     * <p><b><u>Closing the {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient} will close this
     * executor service!</u></b>
     *
     * @param  executorService
     *         The executor service that should be used
     *
     * @return The current WebhookClientBuilder for chaining convenience
     */
    public WebhookClientBuilder setExecutorService(@Nullable ScheduledExecutorService executorService)
    {
        this.pool = executorService;
        return this;
    }

    /**
     * The {@link okhttp3.OkHttpClient OkHttpClient}
     * that should be used to make HTTP requests!
     *
     * <p>Setting this will skip the {@link #setHttpClientBuilder(okhttp3.OkHttpClient.Builder) setHttpClientBuilder(OkHttpClient.Builder)}
     * setting and directly use the provided client!
     *
     * @param  client
     *         The client that should be used
     *
     * @return The current WebhookClientBuilder for chaining convenience
     */
    public WebhookClientBuilder setHttpClient(@Nullable OkHttpClient client)
    {
        this.client = client;
        return this;
    }

    /**
     * The {@link okhttp3.OkHttpClient.Builder OkHttpClient.Builder}
     * that should be used to create the {@link okhttp3.OkHttpClient OkHttpClient} of the resulting {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient}!
     * <br>If not set or {@code null} this builder will use the {@link #DEFAULT_HTTP_BUILDER} instance.
     *
     * <p>This setting is ignored if {@link #setHttpClient(okhttp3.OkHttpClient)} is set!
     *
     * @param  builder
     *         The builder that should be used
     *
     * @return The current WebhookClientBuilder for chaining convenience
     */
    public WebhookClientBuilder setHttpClientBuilder(@Nullable OkHttpClient.Builder builder)
    {
        Checks.notNull(builder, "Builder");
        this.builder = builder;
        return this;
    }

    /**
     * Factory that should be used by the default {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService}
     * to create Threads for rate limitation handling of the created {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient}!
     * <br>This allows changing thread information such as name without having to create your own executor.
     *
     * @param  factory
     *         The {@link java.util.concurrent.ThreadFactory ThreadFactory} that will
     *         be used when no {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService}
     *         has been set via {@link #setExecutorService(ScheduledExecutorService)}
     *
     * @return The current WebhookClientBuilder for chaining convenience
     */
    public WebhookClientBuilder setThreadFactory(@Nullable ThreadFactory factory)
    {
        this.threadFactory = factory;
        return this;
    }

    /**
     * Whether rate limit threads of the created {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient}
     * should be treated as {@link Thread#isDaemon()} or not.
     * <br><b>Default: false</b>
     *
     * <p>This will not be used when the default thread pool has been set via {@link #setExecutorService(ScheduledExecutorService)}!
     *
     * @param  isDaemon
     *         True, if the threads should be daemon
     *
     * @return The current WebhookClientBuilder for chaining convenience
     */
    public WebhookClientBuilder setDaemon(boolean isDaemon)
    {
        this.isDaemon = isDaemon;
        return this;
    }


    /**
     * Builds a new {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient} instance
     * with the current state of this builder.
     *
     * <p><b><u>Remember to close the WebhookClient once you don't need it anymore to free resources!</u></b>
     *
     * @return The new WebhookClient instance
     */
    public WebhookClient build()
    {
        OkHttpClient client = this.client;
        if (client == null)
        {
            if (builder == null)
                builder = DEFAULT_HTTP_BUILDER;
            client = builder.build();
        }
        if (pool == null)
        {
            if (threadFactory == null)
                threadFactory = new DefaultWebhookThreadFactory();
            pool = Executors.newSingleThreadScheduledExecutor(threadFactory);
        }
        return new WebhookClient(id, token, client, pool);
    }

    public final class DefaultWebhookThreadFactory implements ThreadFactory
    {
        @Override
        public Thread newThread(Runnable r)
        {
            final Thread thread = new Thread(r, "Webhook-RateLimit Thread WebhookID: " + id);
            thread.setDaemon(isDaemon);
            return thread;
        }
    }
}
