/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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
import okhttp3.OkHttpClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builder that creates a new {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient} instance
 */
public class WebhookClientBuilder
{
    public static final OkHttpClient.Builder DEFAULT_HTTP_BUILDER = new OkHttpClient.Builder();

    protected final long id;
    protected final String token;
    protected ScheduledExecutorService pool;
    protected OkHttpClient.Builder builder;
    protected OkHttpClient client;

    /**
     * Creates a new WebhookClientBuilder with the provided id and token
     *
     * @param id
     *        The snowflake id of the target webhook
     * @param token
     *        The authorization token of the target webhook
     *        <br><b>This is not a bot/client token!</b>
     */
    public WebhookClientBuilder(long id, String token)
    {
        this.id = id;
        this.token = token;
    }

    /**
     * Creates a new WebhookClientBuilder with the provided id and token
     *
     * @param webhook
     *        The target {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     *
     * @throws java.lang.NullPointerException
     *         If the provided {@link net.dv8tion.jda.core.entities.Webhook Webhook} is {@code null}
     */
    public WebhookClientBuilder(Webhook webhook)
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
    public WebhookClientBuilder setExecutorService(ScheduledExecutorService executorService)
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
    public WebhookClientBuilder setHttpClient(OkHttpClient client)
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
    public WebhookClientBuilder setHttpClientBuilder(OkHttpClient.Builder builder)
    {
        Checks.notNull(builder, "Builder");
        this.builder = builder;
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
            pool = Executors.newScheduledThreadPool(1);
        return new WebhookClient(id, token, client, pool);
    }
}
