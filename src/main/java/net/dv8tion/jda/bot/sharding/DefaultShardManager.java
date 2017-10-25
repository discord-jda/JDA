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
package net.dv8tion.jda.bot.sharding;

import com.neovisionaries.ws.client.WebSocketFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.bot.utils.cache.ShardCacheView;
import net.dv8tion.jda.bot.utils.cache.impl.ShardCacheViewImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.ShardedRateLimiter;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.utils.tuple.Pair;
import okhttp3.OkHttpClient;

/**
 * JDA's default {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} implementation.
 * To create new instances use the {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder}.
 *
 * @since  3.4
 * @author Aljoscha Grebe
 */
public class DefaultShardManager implements ShardManager
{
    public static final SimpleLog LOG = SimpleLog.getLog(ShardManager.class);

    /**
     * The factory used to create {@link net.dv8tion.jda.core.audio.factory.IAudioSendSystem IAudioSendSystem}
     * objects which handle the sending loop for audio packets.
     */
    protected final IAudioSendFactory audioSendFactory;

    /**
     * Whether or not JDA should try to reconnect if a connection-error is encountered.
     * <br>This will use an incremental reconnect (timeouts are increased each time an attempt fails).
     */
    protected final boolean autoReconnect;

    /**
     * The core pool size for the global JDA
     * {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} which is used
     * in various locations throughout JDA.
     */
    protected final int corePoolSize;

    /**
     * If enabled, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be.
     */
    protected final boolean enableBulkDeleteSplitting;

    /**
     * Whether or not Voice functionality is enabled.
     */
    protected final boolean enableVoice;

    /**
     * The {@link net.dv8tion.jda.core.hooks.IEventManager IEventManager} used by the ShardManager.
     */
    protected final IEventManager eventManager;

    /**
     * The game new JDA instances should have on startup.
     */
    protected final Game game;

    /**
     * Whether or not new JDA instances should be marked as afk on startup.
     */
    protected final boolean idle;

    /**
     * The event listeners for new JDA instances.
     */
    protected final List<Object> listeners;

    /**
     * The maximum amount of time that JDA will back off to wait when attempting to reconnect the MainWebsocket.
     */
    protected final int maxReconnectDelay;

    /**
     * The executor that is used by the ShardManager internally to create new JDA instances.
     */
    protected final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r ->
    {
        final Thread t = new Thread(r, "DefaultShardManager");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY + 1);
        return t;
    });

    /**
     * The queue of new shards waiting for creation.
     */
    protected final Queue<Integer> queue = new ConcurrentLinkedQueue<>();

    /**
     * The {@link net.dv8tion.jda.bot.utils.cache.ShardCacheView ShardCacheView} that holds all shards.
     */
    protected ShardCacheViewImpl shards;

    /**
     * The total number of shards.
     *
     * <p></p><b>DO NOT CHANGE THIS WHILE SOME SHARD ARE STILL RUNNING !</b>
     */
    protected int shardsTotal;

    /**
     * The {@link okhttp3.OkHttpClient.Builder OkHttpClient.Builder} that will be used by JDA's requester.
     */
    protected final OkHttpClient.Builder httpClientBuilder;

    /**
     * The {@link com.neovisionaries.ws.client.WebSocketFactory WebSocketFactory} that will be used by JDA's websocket client.
     */
    protected final WebSocketFactory wsFactory;

    /**
     * The queue that will be used by all shards to ensure reconnects don't hit the ratelimit.
     */
    protected final SessionReconnectQueue reconnectQueue;

    /**
     * Whether the Requester should retry when
     * a {@link java.net.SocketTimeoutException SocketTimeoutException} occurs.
     */
    protected final boolean retryOnTimeout;

    /**
     * The {@link net.dv8tion.jda.core.ShardedRateLimiter ShardedRateLimiter} that will be used to keep
     * track of rate limits across all shards.
     */
    protected final ShardedRateLimiter shardedRateLimiter;

    /**
     * This can be used to check if the ShardManager is shutting down.
     */
    protected final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * The shutdown hook used by this ShardManager. If this is null the shutdown hook is disabled.
     */
    protected final Thread shutdownHook;

    /**
     * The status new JDA instances should have on startup.
     */
    protected final OnlineStatus status;

    /**
     * The token of the account associated with this ShardManager.
     */
    protected final String token;

    /**
     * The worker running on the {@link #executor ScheduledExecutorService} that spawns new shards.
     */
    protected ScheduledFuture<?> worker;

    /**
     * The gateway url for JDA to use. Will be {@code nul} until the first shard is created.
     */
    protected String gatewayURL;

    /**
     * Creates a new DefaultShardManager instance.
     *
     * @param  shardsTotal
     *         The total amount of shards or {@code -1} to retrieve the recommended amount from discord.
     * @param  shardIds
     *         A {@link java.util.Collection Collection} of all shard ids that should be started in the beginning or {@code null}
     *         to start all possible shards. This will be ignored if shardsTotal is {@code -1}.
     * @param  listeners
     *         The event listeners for new JDA instances.
     * @param  token
     *         The token
     * @param  eventManager
     *         The event manager
     * @param  audioSendFactory
     *         The {@link net.dv8tion.jda.core.audio.factory.IAudioSendFactory IAudioSendFactory}
     * @param  game
     *         The games used at startup of new JDA instances
     * @param  status
     *         The status used at startup of new JDA instances
     * @param  httpClientBuilder
     *         The {@link okhttp3.OkHttpClient.Builder OkHttpClient.Builder}
     * @param  wsFactory
     *         The {@link com.neovisionaries.ws.client.WebSocketFactory WebSocketFactory}
     * @param  shardedRateLimiter
     *         The {@link net.dv8tion.jda.core.ShardedRateLimiter ShardedRateLimiter}
     * @param  maxReconnectDelay
     *         The max reconnect delay
     * @param  corePoolSize
     *         The core pool size for JDA's internal executor
     * @param  enableVoice
     *         Whether or not Voice should be enabled
     * @param  enableShutdownHook
     *         Whether or not the shutdown hook should be enabled
     * @param  enableBulkDeleteSplitting
     *         Whether or not {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setBulkDeleteSplittingEnabled(boolean) bulk delete splitting}
     *         should be enabled
     * @param  autoReconnect
     *         Whether or not auto reconnect should be enabled
     * @param  idle
     *         Whether or not new sessions should be marked as afk on startup
     * @param  reconnectQueue
     *         The {@link net.dv8tion.jda.core.requests.SessionReconnectQueue SessionReconnectQueue}
     */
    protected DefaultShardManager(final int shardsTotal, final Collection<Integer> shardIds, final List<Object> listeners, final String token,
                               final IEventManager eventManager, final IAudioSendFactory audioSendFactory, final Game game, final OnlineStatus status,
                               final OkHttpClient.Builder httpClientBuilder, final WebSocketFactory wsFactory, ShardedRateLimiter shardedRateLimiter,
                               final int maxReconnectDelay, final int corePoolSize, final boolean enableVoice, final boolean enableShutdownHook,
                               final boolean enableBulkDeleteSplitting, final boolean autoReconnect, final boolean idle, final boolean retryOnTimeout,
                               final SessionReconnectQueue reconnectQueue)
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
        this.shardedRateLimiter = shardedRateLimiter == null ? new ShardedRateLimiter() : shardedRateLimiter;
        this.maxReconnectDelay = maxReconnectDelay;
        this.corePoolSize = corePoolSize;
        this.enableVoice = enableVoice;
        this.shutdownHook = enableShutdownHook ? new Thread(this::shutdown, "JDA Shutdown Hook") : null;
        this.enableBulkDeleteSplitting = enableBulkDeleteSplitting;
        this.autoReconnect = autoReconnect;
        this.idle = idle;
        this.retryOnTimeout = retryOnTimeout;
        this.reconnectQueue = reconnectQueue == null ? new SessionReconnectQueue() : reconnectQueue;

        if (shardsTotal != -1)
        {
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
    }

    @Override
    public void addEventListener(final Object... listeners)
    {
        Collections.addAll(this.listeners, listeners);
        ShardManager.super.addEventListener(listeners);
    }

    @Override
    public int getAmountOfQueuedShards()
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
        // building the first one in the current thread ensures that LoginException and IllegalArgumentException can be thrown on login
        JDAImpl jda = null;
        try
        {
            if (this.queue.isEmpty())
            {
                this.shards = new ShardCacheViewImpl(this.shardsTotal);
                for (int i = 0; i < this.shardsTotal; i++)
                    this.queue.offer(i);
            }

            final int shardId = this.queue.peek();

            jda = this.buildInstance(shardId);
            this.shards.getMap().put(shardId, jda);
            this.queue.remove(shardId);
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

        this.worker = this.executor.scheduleWithFixedDelay(this::processQueue, 5000, 5000, TimeUnit.MILLISECONDS); // 5s for ratelimit

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

        this.queue.add(shardId);
    }

    @Override
    public void shutdown()
    {
        if (this.shutdown.getAndSet(true))
            return; // shutdown has already been requested

        if (this.worker != null && !this.worker.isDone())
            this.worker.cancel(true);

        if (this.shutdownHook != null)
        {
            try
            {
                Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
            }
            catch (final Exception e) { /* ignored */ }
        }

        this.executor.shutdown();

        if (this.shards != null)
        {
            for (final JDA jda : this.shards)
                jda.shutdown();
        }
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
        this.queue.add(shardId);
    }

    protected void processQueue()
    {
        JDAImpl api = null;
        try
        {
            int shardId = -1;

            if (shards == null)
            {
                shardId = 0;
            }
            else
            {
                do
                {
                    final int i = this.queue.peek();
                    if (this.shards.getMap().containsKey(i))
                        this.queue.poll();
                    else
                        shardId = i;
                }
                while (shardId == -1 && !this.queue.isEmpty());
            }

            if (shardId == -1)
                return;

            api = this.buildInstance(shardId);
            this.shards.getMap().put(shardId, api);
            this.queue.remove(shardId);
        }
        // as this happens before removing the shardId if from the queue just try again after 5 seconds
        catch (RateLimitedException e)
        {
            LOG.warn("Hit the login ratelimit while creating new JDA instances");
        }
        catch (LoginException e)
        {
            // this can only happen if the token has been changed
            // in this case the ShardManager will just shutdown itself as there currently is no way of hot-swapping the token on a running JDA instance.
            LOG.warn("The token has been invalidated and the ShardManager will shutdown!");
            LOG.warn(e);
            this.shutdown();
        }
        catch (final Exception e)
        {
            if (api != null)
                api.shutdown();
        }
    }

        protected JDAImpl buildInstance(final int shardId) throws LoginException, RateLimitedException
    {
        final JDAImpl jda = new JDAImpl(AccountType.BOT, this.token, this.httpClientBuilder, this.wsFactory, this.shardedRateLimiter,
            this.autoReconnect, this.enableVoice, this.enableBulkDeleteSplitting, this.enableBulkDeleteSplitting, retryOnTimeout,
            this.corePoolSize, this.maxReconnectDelay);

        jda.asBot().setShardManager(this);

        if (this.eventManager != null)
            jda.setEventManager(this.eventManager);

        if (this.audioSendFactory != null)
            jda.setAudioSendFactory(this.audioSendFactory);

        this.listeners.forEach(jda::addEventListener);
        jda.setStatus(JDA.Status.INITIALIZED); //This is already set by JDA internally, but this is to make sure the listeners catch it.

        // Set the presence information before connecting to have the correct information ready when sending IDENTIFY
        ((PresenceImpl) jda.getPresence()).setCacheGame(this.game).setCacheIdle(this.idle).setCacheStatus(this.status);

        if (this.gatewayURL == null)
        {
            Pair<String, Integer> gateway = jda.getGatewayBot().complete();

            this.gatewayURL = gateway.getLeft();

            if (shardsTotal == -1)
            {
                this.shardsTotal = gateway.getRight();

                for (int i = 1; i < shardsTotal; i++)
                    queue.offer(i);
            }
        }

        final JDA.ShardInfo shardInfo = new JDA.ShardInfo(shardId, this.shardsTotal);

        final int shardTotal = jda.login(this.gatewayURL, shardInfo, this.reconnectQueue);
        if (this.shardsTotal == -1)
            this.shardsTotal = shardTotal;

        JDA.Status waitUntil = JDA.Status.AWAITING_LOGIN_CONFIRMATION;

        while (!jda.getStatus().isInit()                      // JDA might disconnect while starting
            || jda.getStatus().ordinal() < waitUntil.ordinal()) // Wait until status is bypassed
        {
            if (jda.getStatus() == JDA.Status.SHUTDOWN)
                throw new IllegalStateException("JDA was unable to finish starting up!");

            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e) { /* ignored */ }
        }

        return jda;
    }
}
