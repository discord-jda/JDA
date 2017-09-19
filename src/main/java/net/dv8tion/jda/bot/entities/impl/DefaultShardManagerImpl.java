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
package net.dv8tion.jda.bot.entities.impl;

import com.neovisionaries.ws.client.WebSocketFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.bot.utils.cache.ShardCacheView;
import net.dv8tion.jda.bot.utils.cache.impl.ShardCacheViewImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.OkHttpClient;

public class DefaultShardManagerImpl implements ShardManager
{
    protected final IAudioSendFactory audioSendFactory;
    protected final boolean autoReconnect;
    protected final int corePoolSize;
    protected final boolean enableBulkDeleteSplitting;
    protected final boolean enableVoice;
    protected final IEventManager eventManager;
    protected final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r ->
    {
        final Thread t = new Thread(r, "DefaultShardManagerImpl");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY + 1);
        return t;
    });
    protected final Game game;
    protected final boolean idle;
    protected final List<Object> listeners;
    protected final int maxReconnectDelay;
    protected final Queue<Integer> queue = new ConcurrentLinkedQueue<>();
    protected ShardCacheViewImpl shards = new ShardCacheViewImpl();
    protected int shardsTotal;
    protected final int backoff;
    protected final OkHttpClient.Builder httpClientBuilder;
    protected final WebSocketFactory wsFactory;
    protected final SessionReconnectQueue reconnectQueue;
    protected final AtomicBoolean shutdown = new AtomicBoolean(false);
    protected final Thread shutdownHook;
    protected final OnlineStatus status;
    protected final String token;
    protected ScheduledFuture<?> worker;

    public DefaultShardManagerImpl(final int shardsTotal, final Collection<Integer> shardIds, final List<Object> listeners, final String token, final IEventManager eventManager, final IAudioSendFactory audioSendFactory, final Game game, final OnlineStatus status, final OkHttpClient.Builder httpClientBuilder, final WebSocketFactory wsFactory, final int maxReconnectDelay, final int corePoolSize, final boolean enableVoice, final boolean enableShutdownHook, final boolean enableBulkDeleteSplitting, final boolean autoReconnect, final boolean idle, final SessionReconnectQueue reconnectQueue, final int backoff)
    {
        this.shardsTotal = shardsTotal;
        this.listeners = listeners;
        this.token = token;
        this.eventManager = eventManager;
        this.audioSendFactory = audioSendFactory;
        this.game = game;
        this.status = status;
        this.httpClientBuilder = httpClientBuilder == null ? new OkHttpClient.Builder() : httpClientBuilder;
        this.wsFactory = wsFactory == null ? new WebSocketFactory() : wsFactory;
        this.maxReconnectDelay = maxReconnectDelay;
        this.corePoolSize = corePoolSize;
        this.enableVoice = enableVoice;
        this.shutdownHook = enableShutdownHook ? new Thread(() -> DefaultShardManagerImpl.this.shutdown(), "JDA Shutdown Hook") : null;
        this.enableBulkDeleteSplitting = enableBulkDeleteSplitting;
        this.autoReconnect = autoReconnect;
        this.idle = idle;
        this.reconnectQueue = reconnectQueue == null ? new SessionReconnectQueue() : reconnectQueue;
        this.backoff = backoff;

        if (shardsTotal != -1)
            if (shardIds == null)
            {
                this.shards = new ShardCacheViewImpl(shardsTotal);
                for (int i = 0; i < this.shardsTotal; i++)
                    this.queue.offer(i);
            }
            else
            {
                this.shards = new ShardCacheViewImpl(shardIds.size());
                shardIds.stream().distinct().sorted().forEach(this.queue::offer);
            }
    }

    @Override
    public void addEventListener(final Object... listeners)
    {
        this.listeners.addAll(Arrays.asList(listeners));
        ShardManager.super.addEventListener(listeners);
    }

    @Override
    public int getAmountQueuedShards()
    {
        return this.queue.size();
    }

    @Override
    public ShardCacheView getShardCache()
    {
        return this.shards;
    }

    public void login() throws LoginException, IllegalArgumentException
    {
        // building the first one in the currrent thread ensures that LoginException and IllegalArgumentException can be thrown on login
        JDAImpl jda = null;
        try
        {
            if (this.queue.isEmpty())
            {
                jda = this.buildInstance(0);

                this.shards = new ShardCacheViewImpl(this.shardsTotal);
                for (int i = 0; i < this.shardsTotal; i++)
                    this.queue.offer(i);

                this.shards.getMap().put(0, jda);
            }
            else
            {
                final int shardId = this.queue.peek();

                this.shards.getMap().put(shardId, jda = this.buildInstance(shardId));
                this.queue.remove(shardId);
            }
        }
        catch (final RateLimitedException e)
        {
            // do not remove 'shardId' from the queue and try the first one again after 5 seconds in the async thread
        }
        catch (final Exception e)
        {
            if (jda != null)
                jda.shutdown();
            throw e;
        }

        this.worker = this.executor.scheduleWithFixedDelay(() ->
        {
            if (this.queue.isEmpty())
                return;

            JDAImpl api = null;
            try
            {
                int shardId = -1;
                do
                {
                    final int i = this.queue.peek();
                    if (this.shards.getMap().containsKey(i))
                        this.queue.poll();
                    else
                        shardId = i;
                }
                while (shardId == -1 && !this.queue.isEmpty());

                if (shardId == -1)
                {
                    this.queue.poll();
                    return;
                }

                this.shards.getMap().put(shardId, api = this.buildInstance(shardId));
                this.queue.remove(shardId);
            }
            catch (LoginException | IllegalArgumentException e)
            {
                // TODO: this should never happen unless the token changes between, still needs to be handled somehow
                e.printStackTrace();
            }
            catch (final Exception e)
            {
                // do not remove 'shardId' from the queue and try again after 5 seconds
                if (api != null)
                    api.shutdown();
                throw new RuntimeException(e);
            }
        }, 5000 + this.backoff, 5000 + this.backoff, TimeUnit.MILLISECONDS); // 5s for ratelimit + backoff for safety

        if (this.shutdownHook != null)
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    @Override
    public void removeEventListener(final Object... listeners)
    {
        this.listeners.removeAll(Arrays.asList(listeners));
        ShardManager.super.removeEventListener(listeners);
    }

    @Override
    public void restart()
    {
        for (final int shardId : this.shards.getMap().keys())
        {
            final JDA jda = this.shards.getMap().remove(shardId);
            if (jda != null)
            {
                jda.shutdown();
                this.queue.offer(shardId);
            }
        }
    }

    @Override
    public void restart(final int shardId)
    {
        Checks.notNegative(shardId, "shardId");
        Checks.check(shardId < this.shardsTotal, "shardId must be lower than shardsTotal");

        final JDA jda = this.shards.getMap().remove(shardId);
        if (jda != null)
            jda.shutdown();

        this.queue.offer(shardId);
    }

    @Override
    public void shutdown()
    {
        if (this.shutdown.getAndSet(true))
            return; // shutdown has already been requested

        if (this.worker != null && !this.worker.isDone())
            this.worker.cancel(true);

        if (this.shutdownHook != null)
            try
            {
                Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
            }
            catch (final Exception ignored) {}

        this.executor.shutdown();

        if (this.shards != null)
            for (final JDA jda : this.shards)
                jda.shutdown();
    }

    @Override
    public void shutdown(final int shardId)
    {
        final JDA jda = this.shards.getMap().remove(shardId);
        if (jda != null)
            jda.shutdown();
    }

    @Override
    public void start(final int shardId)
    {
        Checks.notNegative(shardId, "shardId");
        Checks.check(shardId < this.shardsTotal, "shardId must be lower than shardsTotal");
        this.queue.offer(shardId);
    }

    protected JDAImpl buildInstance(final int shardId) throws LoginException, RateLimitedException
    {
        final JDAImpl jda = new JDAImpl(AccountType.BOT, this.httpClientBuilder, this.wsFactory, this.autoReconnect, this.enableVoice, false, this.enableBulkDeleteSplitting, this.corePoolSize, this.maxReconnectDelay);

        jda.asBot().setShardManager(this);

        if (this.eventManager != null)
            jda.setEventManager(this.eventManager);

        if (this.audioSendFactory != null)
            jda.setAudioSendFactory(this.audioSendFactory);

        this.listeners.forEach(jda::addEventListener);
        jda.setStatus(JDA.Status.INITIALIZED); //This is already set by JDA internally, but this is to make sure the listeners catch it.

        // Set the presence information before connecting to have the correct information ready when sending IDENTIFY
        ((PresenceImpl) jda.getPresence()).setCacheGame(this.game).setCacheIdle(this.idle).setCacheStatus(this.status);

        final JDAImpl.ShardInfoImpl shardInfo = new JDAImpl.ShardInfoImpl(shardId, this.shardsTotal);

        final int shardTotal = jda.login(this.token, shardInfo, this.reconnectQueue);
        if (this.shardsTotal == -1)
            this.shardsTotal = shardTotal;
        return jda;
    }
}
