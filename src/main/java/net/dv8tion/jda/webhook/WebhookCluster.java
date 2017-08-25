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

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

public class WebhookCluster implements Closeable
{
    protected final List<WebhookClient> webhooks = new ArrayList<>();
    protected OkHttpClient.Builder defaultHttpClientBuilder;
    protected OkHttpClient defaultHttpClient;
    protected ScheduledExecutorService defaultPool;

    public WebhookCluster setDefaultHttpClientBuilder(OkHttpClient.Builder builder)
    {
        this.defaultHttpClientBuilder = builder;
        return this;
    }

    public WebhookCluster setDefaultHttpClient(OkHttpClient defaultHttpClient)
    {
        this.defaultHttpClient = defaultHttpClient;
        return this;
    }

    public WebhookCluster setDefaultExecutorService(ScheduledExecutorService defaultPool)
    {
        this.defaultPool = defaultPool;
        return this;
    }

    public WebhookCluster buildWebhooks(Webhook... webhooks)
    {
        Checks.notNull(webhooks, "Webhooks");
        for (Webhook webhook : webhooks)
        {
            Checks.notNull(webhook, "Webhook");
            WebhookClientBuilder builder = new WebhookClientBuilder(webhook);
            builder.setExecutorService(defaultPool).setHttpClient(defaultHttpClient);
            if (defaultHttpClientBuilder != null)
                builder.setHttpClientBuilder(defaultHttpClientBuilder);
            this.webhooks.add(builder.build());
        }
        return this;
    }

    public WebhookCluster buildWebhooks(Collection<Webhook> webhooks)
    {
        Checks.notNull(webhooks, "Webhooks");
        for (Webhook webhook : webhooks)
        {
            Checks.notNull(webhook, "Webhook");
            WebhookClientBuilder builder = new WebhookClientBuilder(webhook);
            builder.setExecutorService(defaultPool).setHttpClient(defaultHttpClient);
            if (defaultHttpClientBuilder != null)
                builder.setHttpClientBuilder(defaultHttpClientBuilder);
            this.webhooks.add(builder.build());
        }
        return this;
    }

    public WebhookCluster addWebhooks(WebhookClient... clients)
    {
        Checks.notNull(clients, "Clients");
        for (WebhookClient client : clients)
        {
            Checks.notNull(client, "Client");
            webhooks.add(client);
        }
        return this;
    }

    public WebhookCluster addWebhooks(Collection<WebhookClient> clients)
    {
        Checks.notNull(clients, "Clients");
        for (WebhookClient client : clients)
        {
            Checks.notNull(client, "Client");
            webhooks.add(client);
        }
        return this;
    }

    public WebhookCluster removeWebhooks(WebhookClient... clients)
    {
        Checks.notNull(clients, "Clients");
        for (WebhookClient client : clients)
            webhooks.remove(client);
        return this;
    }

    public WebhookCluster removeWebhooks(Collection<WebhookClient> clients)
    {
        Checks.notNull(clients, "Clients");
        for (WebhookClient client : clients)
            webhooks.remove(client);
        return this;
    }

    public WebhookCluster removeIf(Predicate<WebhookClient> predicate)
    {
        Checks.notNull(predicate, "Predicate");
        webhooks.removeIf(predicate);
        return this;
    }

    public List<WebhookClient> getWebhooks()
    {
        return Collections.unmodifiableList(new ArrayList<>(webhooks));
    }

    public List<Future<?>> multicast(Predicate<WebhookClient> filter, WebhookMessage message)
    {
        Checks.notNull(filter, "Filter");
        Checks.notNull(message, "Message");
        final RequestBody body = message.getBody();
        final List<Future<?>> callbacks = new ArrayList<>();
        for (WebhookClient client : webhooks)
        {
            if (filter.test(client))
                callbacks.add(client.execute(body));
        }
        return callbacks;
    }

    public List<Future<?>> broadcast(WebhookMessage message)
    {
        Checks.notNull(message, "Message");
        final RequestBody body = message.getBody();
        final List<Future<?>> callbacks = new ArrayList<>(webhooks.size());
        for (int i = 0; i < webhooks.size(); i++)
            callbacks.add(webhooks.get(i).execute(body));
        return callbacks;
    }

    public List<Future<?>> broadcast(Message message)
    {
        return broadcast(WebhookMessage.from(message));
    }

    public List<Future<?>> broadcast(MessageEmbed... embeds)
    {
        return broadcast(WebhookMessage.of(embeds));
    }

    public List<Future<?>> broadcast(Collection<MessageEmbed> embeds)
    {
        return broadcast(WebhookMessage.of(embeds));
    }

    public List<Future<?>> broadcast(String content)
    {
        final RequestBody body = WebhookClient.newBody(new JSONObject().put("content", content).toString());
        final List<Future<?>> callbacks = new ArrayList<>(webhooks.size());
        for (int i = 0; i < webhooks.size(); i++)
            callbacks.add(webhooks.get(i).execute(body));
        return callbacks;
    }

    public List<Future<?>> broadcast(File file)
    {
        Checks.notNull(file, "File");
        return broadcast(file, file.getName());
    }

    public List<Future<?>> broadcast(File file, String fileName)
    {
        return broadcast(new WebhookMessageBuilder().setFile(file, fileName).build());
    }

    public List<Future<?>> broadcast(InputStream data, String fileName)
    {
        return broadcast(new WebhookMessageBuilder().setFile(data, fileName).build());
    }

    public List<Future<?>> broadcast(byte[] data, String fileName)
    {
        return broadcast(new WebhookMessageBuilder().setFile(data, fileName).build());
    }

    @Override
    public void close() throws IOException
    {
        webhooks.forEach(WebhookClient::close);
    }
}
