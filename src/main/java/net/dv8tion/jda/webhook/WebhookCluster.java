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

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.requests.RequestFuture;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;

/**
 * A central collection of {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
 * which allows to broadcast ({@link #broadcast(WebhookMessage)}) or multicast ({@link #multicast(Predicate, WebhookMessage)})
 * to all registered clients (receivers).
 *
 * <h2>Registering existing WebhookClients</h2>
 * To register an existing WebhookClient instance (that is not currently shutdown)
 * you either use {@link #addWebhooks(WebhookClient...)} or {@link #addWebhooks(java.util.Collection) addWebhooks(Collection&lt;WebhookClient&gt;)}.
 * <br>These methods allow to register multiple clients in one call
 * and will <b>fail if the client has been closed via {@link WebhookClient#close() WebhookClient.close()}</b>.
 *
 * <p><i><b>Note that you should always remove a WebhookClient from this cluster when you close it directly!</b></i>
 *
 * <h2>Removing already registered WebhookClients</h2>
 * The cluster allows to remove existing clients in batch via {@link #removeWebhooks(WebhookClient...)}, {@link #removeWebhooks(Collection) removeWebhooks(Collection&lt;WebhookClient&gt;)}
 * and {@link #removeIf(java.util.function.Predicate) removeIf(Predicate&lt;WebhookClient&gt;)}.
 * It does not matter if the specified clients are actually registered to this cluster when using these methods.
 *
 * <p><i><b>Note that removing a WebhookClient from the cluster does not close it!</b></i>
 *
 * <h2>Building WebhookClients from the Cluster</h2>
 * This class allows to set default values that will be provided to {@link net.dv8tion.jda.webhook.WebhookClientBuilder WebhookClientBuilder}
 * when building via {@link #buildWebhooks(Webhook...)} and {@link #buildWebhooks(Collection) buildWebhooks(Collection&lt;Webhook&gt;)}.
 * <br>The following settings can be used:
 * <ul>
 *     <li>{@link #setDefaultExecutorService(ScheduledExecutorService)}</li>
 *     <li>{@link #setDefaultHttpClientBuilder(OkHttpClient.Builder)}</li>
 *     <li>{@link #setDefaultHttpClient(OkHttpClient)}</li>
 *     <li>{@link #setDefaultThreadFactory(ThreadFactory)}</li>
 *     <li>{@link #setDefaultDaemon(boolean)}</li>
 * </ul>
 *
 * <p>Note that when you provide your own {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} you are able to shut it down
 * outside of the clients which will cause them to fail.
 * <br><i><b>Do not shutdown the pool before closing all clients!</b></i>
 *
 * <h2>Sending to multiple Webhooks at once</h2>
 * This cluster allows to both broadcast and multicast to registered clients.
 * <br>When broadcasting a message is created before iterating each client to save performance which makes the broadcast
 * method superior to direct for-loops.
 *
 * <p>Multicasting will send a message to all clients which meet a set filter.
 * The filter is specified using a {@link java.util.function.Predicate Predicate} which is provided to {@link #multicast(Predicate, WebhookMessage)}.
 * <br>The predicate decides whether the client should receive the message (returning true) or should be ignored (returning false).
 *
 * <h2>Closing all connections</h2>
 * Each {@link net.dv8tion.jda.webhook.WebhookClient WebhookClient} is a {@link java.io.Closeable Closable} resource which means
 * it must be closed to free resources and enhance performance of the JVM.
 * <br>The WebhookCluster allows to close all registered clients using {@link #close()}.
 * Calling close on the cluster means it will <i><b>remove and close</b></i> all currently registered webhooks.
 * <br>The cluster may still be used after closing.
 */
public class WebhookCluster implements AutoCloseable
{
    protected final List<WebhookClient> webhooks;
    protected OkHttpClient.Builder defaultHttpClientBuilder;
    protected OkHttpClient defaultHttpClient;
    protected ScheduledExecutorService defaultPool;
    protected ThreadFactory threadFactory;
    protected boolean isDaemon;

    /**
     * Creates a new WebhookCluster with the provided
     * {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} as initial client cache.
     *
     * @param  initialClients
     *         Collection of WebhookClients that should be added
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided clients is {@code null} or closed
     */
    public WebhookCluster(@Nonnull Collection<? extends WebhookClient> initialClients)
    {
        webhooks = new ArrayList<>(initialClients.size());
        for (WebhookClient client : initialClients)
            addWebhooks(client);
    }

    /**
     * Creates a new WebhookCluster with the specified initial capacity.
     * <br>For more information about this see {@link java.util.ArrayList ArrayList}.
     *
     * @param  initialCapacity
     *         The initial capacity for this cluster
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided capacity is negative
     */
    public WebhookCluster(int initialCapacity)
    {
        webhooks = new ArrayList<>(initialCapacity);
    }

    /**
     * Creates a new WebhookCluster with default initial capacity
     * and no registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
     */
    public WebhookCluster()
    {
        webhooks = new ArrayList<>();
    }

    // Default builder values

    /**
     * Sets the default {@link okhttp3.OkHttpClient.Builder OkHttpClient.Builder} that should be
     * used when building {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} via
     * {@link #buildWebhooks(Webhook...)} or {@link #buildWebhooks(Collection)}.
     *
     * @param  builder
     *         The default builder, {@code null} to reset
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster setDefaultHttpClientBuilder(@Nullable OkHttpClient.Builder builder)
    {
        this.defaultHttpClientBuilder = builder;
        return this;
    }

    /**
     * Sets the default {@link okhttp3.OkHttpClient OkHttpClient} that should be
     * used when building {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} via
     * {@link #buildWebhooks(Webhook...)} or {@link #buildWebhooks(Collection)}.
     *
     * @param  defaultHttpClient
     *         The default client, {@code null} to reset
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster setDefaultHttpClient(@Nullable OkHttpClient defaultHttpClient)
    {
        this.defaultHttpClient = defaultHttpClient;
        return this;
    }

    /**
     * Sets the default {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} that should be
     * used when building {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} via
     * {@link #buildWebhooks(Webhook...)} or {@link #buildWebhooks(Collection)}.
     *
     * @param  executorService
     *         The default executor service, {@code null} to reset
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster setDefaultExecutorService(@Nullable ScheduledExecutorService executorService)
    {
        this.defaultPool = executorService;
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
     *         has been set via {@link #setDefaultExecutorService(ScheduledExecutorService)}
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster setDefaultThreadFactory(@Nullable ThreadFactory factory)
    {
        this.threadFactory = factory;
        return this;
    }

    /**
     * Whether rate limit threads of created {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
     * should be treated as {@link Thread#isDaemon()} or not.
     * <br><b>Default: false</b>
     *
     * <p>This will not be used when the default thread pool has been set via {@link #setDefaultExecutorService(ScheduledExecutorService)}!
     *
     * @param  isDaemon
     *         True, if the threads should be daemon
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster setDefaultDaemon(boolean isDaemon)
    {
        this.isDaemon = isDaemon;
        return this;
    }

    // Webhook creation/add/remove

    /**
     * Creates new {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} and adds them
     * to this cluster.
     * <br>The {@link net.dv8tion.jda.webhook.WebhookClientBuilder WebhookClientBuilders}
     * will be supplied with the default settings of this cluster.
     *
     * @param  webhooks
     *         Webhooks to target (duplicates will not be filtered)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided array or any of the contained
     *         webhooks is {@code null}
     *
     * @return The current WebhookCluster for chaining convenience
     *
     * @see    #buildWebhooks(Webhook...)
     * @see    #newBuilder(Webhook)
     */
    public WebhookCluster buildWebhooks(Webhook... webhooks)
    {
        Checks.notNull(webhooks, "Webhooks");
        for (Webhook webhook : webhooks)
        {
            Checks.notNull(webhook, "Webhook");
            buildWebhook(webhook.getIdLong(), webhook.getToken());
        }
        return this;
    }

    /**
     * Creates new {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} and adds them
     * to this cluster.
     * <br>The {@link net.dv8tion.jda.webhook.WebhookClientBuilder WebhookClientBuilders}
     * will be supplied with the default settings of this cluster.
     *
     * @param  webhooks
     *         Webhooks to target (duplicates will not be filtered)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided collection or any of the contained
     *         webhooks is {@code null}
     *
     * @return The current WebhookCluster for chaining convenience
     *
     * @see    #buildWebhooks(Webhook...)
     * @see    #newBuilder(Webhook)
     */
    public WebhookCluster buildWebhooks(Collection<Webhook> webhooks)
    {
        Checks.notNull(webhooks, "Webhooks");
        for (Webhook webhook : webhooks)
        {
            Checks.notNull(webhook, "Webhook");
            buildWebhook(webhook.getIdLong(), webhook.getToken());
        }
        return this;
    }

    /**
     * Creates new {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} and adds them
     * to this cluster.
     * <br>The {@link net.dv8tion.jda.webhook.WebhookClientBuilder WebhookClientBuilders}
     * will be supplied with the default settings of this cluster.
     *
     * @param  id
     *         The id for the webhook
     * @param  token
     *         The token for the webhook
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided webhooks token is {@code null} or contains whitespace
     *
     * @return The current WebhookCluster for chaining convenience
     *
     * @see    #newBuilder(long, String)
     */
    public WebhookCluster buildWebhook(long id, String token)
    {
        this.webhooks.add(newBuilder(id, token).build());
        return this;
    }

    /**
     * Creates a new {@link net.dv8tion.jda.webhook.WebhookClientBuilder WebhookClientBuilder}
     * with the defined default settings of this cluster.
     *
     * @param  id
     *         The webhook id
     * @param  token
     *         The webhook token
     *
     * @throws java.lang.IllegalArgumentException
     *         If the token is {@code null}, empty or contains blanks
     *
     * @return The WebhookClientBuilder with default settings
     *
     * @see    net.dv8tion.jda.webhook.WebhookClientBuilder#WebhookClientBuilder(long, String) new WebhookClientBuilder(long, String)
     */
    public WebhookClientBuilder newBuilder(long id, String token)
    {
        WebhookClientBuilder builder = new WebhookClientBuilder(id, token);
        builder.setExecutorService(defaultPool)
               .setHttpClient(defaultHttpClient)
               .setThreadFactory(threadFactory)
               .setDaemon(isDaemon);
        if (defaultHttpClientBuilder != null)
            builder.setHttpClientBuilder(defaultHttpClientBuilder);
        return builder;
    }

    /**
     * Creates a new {@link net.dv8tion.jda.webhook.WebhookClientBuilder WebhookClientBuilder}
     * with the defined default settings of this cluster.
     *
     * @param  webhook
     *         The target webhook
     *
     * @throws java.lang.IllegalArgumentException
     *         If the webhook is {@code null}
     *
     * @return The WebhookClientBuilder with default settings
     *
     * @see    net.dv8tion.jda.webhook.WebhookClientBuilder#WebhookClientBuilder(Webhook) new WebhookClientBuilder(Webhook)
     */
    public WebhookClientBuilder newBuilder(Webhook webhook)
    {
        Checks.notNull(webhook, "Webhook");
        return newBuilder(webhook.getIdLong(), webhook.getToken());
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
     * to this cluster's list of receivers.
     * <br>Duplicate clients are supported and will not be filtered automatically.
     *
     * @param  clients
     *         WebhookClients to add
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided array or any of the contained
     *         clients is {@code null} or closed
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster addWebhooks(WebhookClient... clients)
    {
        Checks.notNull(clients, "Clients");
        for (WebhookClient client : clients)
        {
            Checks.notNull(client, "Client");
            Checks.check(!client.isShutdown,
                "One of the provided WebhookClients has been closed already!");
            webhooks.add(client);
        }
        return this;
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
     * to this cluster's list of receivers.
     * <br>Duplicate clients are supported and will not be filtered automatically.
     *
     * @param  clients
     *         WebhookClients to add
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided collection or any of the contained
     *         clients is {@code null} or closed
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster addWebhooks(Collection<WebhookClient> clients)
    {
        Checks.notNull(clients, "Clients");
        for (WebhookClient client : clients)
        {
            Checks.notNull(client, "Client");
            Checks.check(!client.isShutdown,
                "One of the provided WebhookClients has been closed already!");
            webhooks.add(client);
        }
        return this;
    }

    /**
     * Removes the specified {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
     * from this cluster's list of receivers.
     * <br>It does not matter whether any of the provided clients is actually in the list of receivers.
     *
     * <p><b>Note that the removed clients are not closed by this operation!</b>
     *
     * @param  clients
     *         WebhookClients to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided array is {@code null}
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster removeWebhooks(WebhookClient... clients)
    {
        Checks.notNull(clients, "Clients");
        webhooks.removeAll(Arrays.asList(clients));
        return this;
    }

    /**
     * Removes the specified {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
     * from this cluster's list of receivers.
     * <br>It does not matter whether any of the provided clients is actually in the list of receivers.
     *
     * <p><b>Note that the removed clients are not closed by this operation!</b>
     *
     * @param  clients
     *         WebhookClients to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided collection is {@code null}
     *
     * @return The current WebhookCluster for chaining convenience
     */
    public WebhookCluster removeWebhooks(Collection<WebhookClient> clients)
    {
        Checks.notNull(clients, "Clients");
        webhooks.removeAll(clients);
        return this;
    }

    /**
     * Removes the specified {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
     * from this cluster's list of receivers under the conditions of the provided filter.
     * <br>The filter should return {@code true} to remove provided clients and {@code false} to retain them.
     *
     * <p><b>Note that the removed clients are not closed by this operation!</b>
     *
     * @param  predicate
     *         The filter
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided filter is {@code null}
     *
     * @return List of removed clients
     */
    public List<WebhookClient> removeIf(Predicate<WebhookClient> predicate)
    {
        Checks.notNull(predicate, "Predicate");
        List<WebhookClient> clients = new ArrayList<>();
        for (WebhookClient client : webhooks)
        {
            if (predicate.test(client))
                clients.add(client);
        }
        removeWebhooks(clients);
        return clients;
    }

    /**
     * Closes all {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} that meet
     * the specified filter.
     * <br>The filter may return {@code true} for all clients that should be <b>removed and closed</b>.
     *
     * @param  predicate
     *         The filter to decide which clients to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided filter is {@code null}
     *
     * @return List of removed and closed clients
     */
    public List<WebhookClient> closeIf(Predicate<WebhookClient> predicate)
    {
        Checks.notNull(predicate, "Filter");
        List<WebhookClient> clients = new ArrayList<>();
        for (WebhookClient client : webhooks)
        {
            if (predicate.test(client))
                clients.add(client);
        }
        removeWebhooks(clients);
        clients.forEach(WebhookClient::close);
        return clients;
    }

    /**
     * The current list of receivers for this WebhookCluster instance.
     * <br>The provided list is an immutable copy of the actual stored list of {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     *
     * @return Immutable list of registered receivers
     */
    public List<WebhookClient> getWebhooks()
    {
        return Collections.unmodifiableList(new ArrayList<>(webhooks));
    }

    // Broadcasting / Multicasting

    /**
     * Sends the provided {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage}
     * to all {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients} that meet the specified
     * filter.
     * <br>The filter should return {@code true} for all clients that should receive the message.
     *
     * <p>Hint: Use {@link net.dv8tion.jda.webhook.WebhookMessageBuilder WebhookMessageBuilder} to
     * create a {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage} instance!
     *
     * @param  filter
     *         The filter that decides what clients receive the message
     * @param  message
     *         The message that should be sent to the filtered clients
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> multicast(Predicate<WebhookClient> filter, WebhookMessage message)
    {
        Checks.notNull(filter, "Filter");
        Checks.notNull(message, "Message");
        final RequestBody body = message.getBody();
        final List<RequestFuture<?>> callbacks = new ArrayList<>();
        for (WebhookClient client : webhooks)
        {
            if (filter.test(client))
                callbacks.add(client.execute(body));
        }
        return callbacks;
    }

    /**
     * Sends the provided {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage}
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     *
     * <p>Hint: Use {@link net.dv8tion.jda.webhook.WebhookMessageBuilder WebhookMessageBuilder} to
     * create a {@link net.dv8tion.jda.webhook.WebhookMessage WebhookMessage} instance!
     *
     * @param  message
     *         The message that should be sent to the clients
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(WebhookMessage message)
    {
        Checks.notNull(message, "Message");
        RequestBody body = message.getBody();
        final List<RequestFuture<?>> callbacks = new ArrayList<>(webhooks.size());
        for (WebhookClient webhook : webhooks)
        {
            callbacks.add(webhook.execute(body));
            if (message.isFile()) // for files we have to make new data sets
                body = message.getBody();
        }
        return callbacks;
    }

    /**
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message}
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     *
     * <p>Hint: Use {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} to
     * create a {@link net.dv8tion.jda.core.entities.Message Message} instance!
     *
     * @param  message
     *         The message that should be sent to the clients
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided message is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(Message message)
    {
        return broadcast(WebhookMessage.from(message));
    }

    /**
     * Sends the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     *
     * <p><b>You can send up to 10 embeds per message! If more are sent they will not be displayed.</b>
     *
     * <p>Hint: Use {@link net.dv8tion.jda.core.EmbedBuilder EmbedBuilder} to
     * create a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds} instance!
     *
     * @param  embeds
     *         The embeds that should be sent to the clients
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(MessageEmbed[] embeds)
    {
        return broadcast(WebhookMessage.embeds(Arrays.asList(embeds)));
    }

    /**
     * Sends the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     *
     * <p><b>You can send up to 10 embeds per message! If more are sent they will not be displayed.</b>
     *
     * <p>Hint: Use {@link net.dv8tion.jda.core.EmbedBuilder EmbedBuilder} to
     * create a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds} instance!
     *
     * @param  first
     *         The first embed to send to the clients
     * @param  embeds
     *         The other embeds that should be sent to the clients
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(MessageEmbed first, MessageEmbed... embeds)
    {
        return broadcast(WebhookMessage.embeds(first, embeds));
    }


    /**
     * Sends the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     *
     * <p><b>You can send up to 10 embeds per message! If more are sent they will not be displayed.</b>
     *
     * <p>Hint: Use {@link net.dv8tion.jda.core.EmbedBuilder EmbedBuilder} to
     * create a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds} instance!
     *
     * @param  embeds
     *         The embeds that should be sent to the clients
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(Collection<MessageEmbed> embeds)
    {
        return broadcast(WebhookMessage.embeds(embeds));
    }

    /**
     * Sends the provided text message
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     *
     * @param  content
     *         The text that should be sent to the clients
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided content is {@code null} or blank
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(String content)
    {
        Checks.notBlank(content, "Content");
        Checks.check(content.length() <= 2000, "Content may not exceed 2000 characters!");
        final RequestBody body = WebhookClient.newBody(new JSONObject().put("content", content).toString());
        final List<RequestFuture<?>> callbacks = new ArrayList<>(webhooks.size());
        for (WebhookClient webhook : webhooks)
            callbacks.add(webhook.execute(body));
        return callbacks;
    }

    /**
     * Sends the provided {@link java.io.File File}
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     * <br>Use {@link WebhookMessage#files(String, Object, Object...)} to send up to 10 files!
     *
     * <p><b>The provided data should not exceed 8MB in size!</b>
     *
     * @param  file
     *         The file that should be sent to the clients
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null}, does not exist or ist not readable
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(File file)
    {
        Checks.notNull(file, "File");
        return broadcast(file, file.getName());
    }

    /**
     * Sends the provided {@link java.io.File File}
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     * <br>Use {@link WebhookMessage#files(String, Object, Object...)} to send up to 10 files!
     *
     * <p><b>The provided data should not exceed 8MB in size!</b>
     *
     * @param  file
     *         The file that should be sent to the clients
     * @param  fileName
     *         The name that should be given to the file
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null}, does not exist or ist not readable
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(File file, String fileName)
    {
        Checks.notNull(file, "File");
        Checks.check(file.length() <= Message.MAX_FILE_SIZE, "Provided File exceeds the maximum size of 8MB!");
        return broadcast(new WebhookMessageBuilder().addFile(fileName, file).build());
    }

    /**
     * Sends the provided {@link java.io.InputStream InputStream} as an attachment
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     * <br>Use {@link WebhookMessage#files(String, Object, Object...)} to send up to 10 files!
     *
     * <p><b>The provided data should not exceed 8MB in size!</b>
     *
     * @param  data
     *         The data that should be sent to the clients
     * @param  fileName
     *         The name that should be given to the attachment
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided data is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(InputStream data, String fileName)
    {
        return broadcast(new WebhookMessageBuilder().addFile(fileName, data).build());
    }

    /**
     * Sends the provided {@code byte[]} data as an attachment
     * to all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}.
     * <br>Use {@link WebhookMessage#files(String, Object, Object...)} to send up to 10 files!
     *
     * <p><b>The provided data should not exceed 8MB in size!</b>
     *
     * @param  data
     *         The data that should be sent to the clients
     * @param  fileName
     *         The name that should be given to the attachment
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided data is {@code null}
     * @throws java.util.concurrent.RejectedExecutionException
     *         If any of the receivers has been shutdown
     *
     * @return A list of {@link java.util.concurrent.Future Future} instances
     *         representing all message tasks.
     */
    public List<RequestFuture<?>> broadcast(byte[] data, String fileName)
    {
        Checks.notNull(data, "Data");
        Checks.check(data.length < Message.MAX_FILE_SIZE, "Provided data exceeds the maximum size of 8MB!");
        return broadcast(new WebhookMessageBuilder().addFile(fileName, data).build());
    }

    /**
     * Closes all registered {@link net.dv8tion.jda.webhook.WebhookClient WebhookClients}
     * and removes the from this cluster!
     */
    @Override
    public void close()
    {
        webhooks.forEach(WebhookClient::close);
        webhooks.clear();
    }
}
