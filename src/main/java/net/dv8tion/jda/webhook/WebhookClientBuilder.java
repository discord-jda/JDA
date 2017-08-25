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

public class WebhookClientBuilder
{
    public static final OkHttpClient.Builder DEFAULT_BUILDER = new OkHttpClient.Builder();

    protected final long id;
    protected final String token;
    protected ScheduledExecutorService pool;
    protected OkHttpClient.Builder builder;
    protected OkHttpClient client;

    public WebhookClientBuilder(long id, String token)
    {
        this.id = id;
        this.token = token;
    }

    public WebhookClientBuilder(Webhook webhook)
    {
        this(webhook.getIdLong(), webhook.getToken());
    }

    public WebhookClientBuilder setExecutorService(ScheduledExecutorService executorService)
    {
        this.pool = executorService;
        return this;
    }

    public WebhookClientBuilder setHttpClient(OkHttpClient client)
    {
        this.client = client;
        return this;
    }

    public WebhookClientBuilder setHttpClientBuilder(OkHttpClient.Builder builder)
    {
        Checks.notNull(builder, "Builder");
        this.builder = builder;
        return this;
    }

    public WebhookClient build()
    {
        OkHttpClient client = this.client;
        if (client == null)
        {
            if (builder == null)
                builder = DEFAULT_BUILDER;
            client = builder.build();
        }
        if (pool == null)
            pool = Executors.newScheduledThreadPool(1);
        return new WebhookClient(id, token, client, pool);
    }
}
