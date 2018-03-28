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
package net.dv8tion.jda.bot.sharding;

import com.neovisionaries.ws.client.WebSocketFactory;
import gnu.trove.map.TIntObjectMap;
import net.dv8tion.jda.bot.utils.cache.ShardCacheView;
import net.dv8tion.jda.bot.utils.cache.impl.ShardCacheViewImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.SessionController;
import net.dv8tion.jda.core.utils.SessionControllerAdapter;
import net.dv8tion.jda.core.utils.tuple.Pair;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

/**
 * JDA's default {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager} implementation.
 * To create new instances use the {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder}.
 *
 * @since  3.4
 * @author Aljoscha Grebe
 */
public class DefaultShardManager implements ShardManager
{
    public static final Logger LOG = JDALogger.getLog(ShardManager.class);
    public static final ThreadFactory DEFAULT_THREAD_FACTORY = r ->
    {
        final Thread t = new Thread(r, "DefaultShardManager");
        t.setPriority(Thread.NORM_PRIORITY + 1);
        return t;
    };
    /**
     * The {@link net.dv8tion.jda.core.utils.SessionController SessionController} for this manager.
     */
    protected final SessionController controller;

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
     * The event listeners for new JDA instances.
     */
    protected final List<Object> listeners;

    /**
     * The event listener providers for new and restarted JDA instances.
     */
    protected final List<IntFunction<Object>> listenerProviders;

    /**
     * The maximum amount of time that JDA will back off to wait when attempting to reconnect the MainWebsocket.
     */
    protected final int maxReconnectDelay;

    /**
     * The executor that is used by the ShardManager internally to create new JDA instances.
     */
    protected final ScheduledExecutorService executor;

    /**
     * The queue of shards waiting for creation.
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
     * Whether the Requester should retry when
     * a {@link java.net.SocketTimeoutException SocketTimeoutException} occurs.
     */
    protected final boolean retryOnTimeout;

    /**
     * Whether this ShardManager should use {@link net.dv8tion.jda.core.JDA#shutdownNow() JDA#shutdownNow()} instead of
     * {@link net.dv8tion.jda.core.JDA#shutdown() JDA#shutdown()} to shutdown it's shards.
     */
    protected final boolean useShutdownNow;

    /**
     * This can be used to check if the ShardManager is shutting down.
     */
    protected final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * The shutdown hook used by this ShardManager. If this is null the shutdown hook is disabled.
     */
    protected final Thread shutdownHook;

    /**
     * The token of the account associated with this ShardManager.
     */
    protected final String token;

    /**
     * The worker running on the {@link #executor ScheduledExecutorService} that spawns new shards.
     */
    protected Future<?> worker;

    /**
     * The gateway url for JDA to use. Will be {@code nul} until the first shard is created.
     */
    protected String gatewayURL;

    /**
     * The gameProvider new JDA instances should have on startup.
     */
    protected IntFunction<Game> gameProvider;

    /**
     * Whether or not new JDA instances should be marked as afk on startup.
     */
    protected IntFunction<Boolean> idleProvider;

    /**
     * The statusProvider new JDA instances should have on startup.
     */
    protected IntFunction<OnlineStatus> statusProvider;

    /**
     * The MDC context provider new JDA instances should use on startup.
     */
    protected IntFunction<ConcurrentMap<String, String>> contextProvider;

    /**
     * Whether to use the MDC context provider.
     */
    protected boolean enableMDC;

    /**
     * Whether to enable transport compression
     */
    protected boolean enableCompression;

    /**
     * Creates a new DefaultShardManager instance.
     * @param  shardsTotal
     *         The total amount of shards or {@code -1} to retrieve the recommended amount from discord.
     * @param  shardIds
     *         A {@link java.util.Collection Collection} of all shard ids that should be started in the beginning or {@code null}
     *         to start all possible shards. This will be ignored if shardsTotal is {@code -1}.
     * @param  controller
     *         The {@link net.dv8tion.jda.core.utils.SessionController SessionController}
     * @param  listeners
     *         The event listeners for new JDA instances.
     * @param  listenerProviders
     *         Providers of event listeners for JDA instances. Each will have the shard id applied to them upon
     *         shard creation (including shard restarts) and must return an event listener
     * @param  token
     *         The token
     * @param  eventManager
     *         The event manager
     * @param  audioSendFactory
     *         The {@link net.dv8tion.jda.core.audio.factory.IAudioSendFactory IAudioSendFactory}
     * @param  gameProvider
     *         The games used at startup of new JDA instances
     * @param  statusProvider
     *         The statusProvider used at startup of new JDA instances
     * @param  httpClientBuilder
     *         The {@link okhttp3.OkHttpClient.Builder OkHttpClient.Builder}
     * @param  wsFactory
     *         The {@link com.neovisionaries.ws.client.WebSocketFactory WebSocketFactory}
     * @param  threadFactory
     *         The {@link java.util.concurrent.ThreadFactory ThreadFactory}
     * @param  maxReconnectDelay
     *         The max reconnect delay
     * @param  corePoolSize
     *         The core pool size for JDA's internal executor
     * @param  enableVoice
     *         Whether or not Voice should be enabled
     * @param  enableShutdownHook
     *         Whether or not the shutdown hook should be enabled
     * @param  enableBulkDeleteSplitting
     *         Whether or not {@link net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder#setBulkDeleteSplittingEnabled(boolean)
     *         bulk delete splitting} should be enabled
     * @param  autoReconnect
     *         Whether or not auto reconnect should be enabled
     * @param  idleProvider
     *         The Function that is used to set a shards idle state
     * @param  retryOnTimeout
     *         hether the Requester should retry when a {@link java.net.SocketTimeoutException SocketTimeoutException} occurs.
     * @param  useShutdownNow
     *         Whether the ShardManager should use JDA#shutdown() or not
     * @param  enableMDC
     *         Whether MDC should be enabled
     * @param  contextProvider
     *         The MDC context provider new JDA instances should use on startup
     * @param  enableCompression
     *         Whether to enable transport compression
     */
    protected DefaultShardManager(final int shardsTotal, final Collection<Integer> shardIds,
                                  final SessionController controller, final List<Object> listeners,
                                  final List<IntFunction<Object>> listenerProviders,
                                  final String token, final IEventManager eventManager, final IAudioSendFactory audioSendFactory,
                                  final IntFunction<Game> gameProvider, final IntFunction<OnlineStatus> statusProvider,
                                  final OkHttpClient.Builder httpClientBuilder, final WebSocketFactory wsFactory,
                                  final ThreadFactory threadFactory,
                                  final int maxReconnectDelay, final int corePoolSize, final boolean enableVoice,
                                  final boolean enableShutdownHook, final boolean enableBulkDeleteSplitting,
                                  final boolean autoReconnect, final IntFunction<Boolean> idleProvider,
                                  final boolean retryOnTimeout, final boolean useShutdownNow,
                                  final boolean enableMDC, final IntFunction<ConcurrentMap<String, String>> contextProvider,
                                  final boolean enableCompression)
    {
        this.shardsTotal = shardsTotal;
        this.listeners = listeners;
        this.listenerProviders = listenerProviders;
        this.token = token;
        this.eventManager = eventManager;
        this.audioSendFactory = audioSendFactory;
        this.gameProvider = gameProvider;
        this.statusProvider = statusProvider;
        this.httpClientBuilder = httpClientBuilder == null ? new OkHttpClient.Builder() : httpClientBuilder;
        this.wsFactory = wsFactory == null ? new WebSocketFactory() : wsFactory;
        this.executor = createExecutor(threadFactory);
        this.controller = controller == null ? new SessionControllerAdapter() : controller;
        this.maxReconnectDelay = maxReconnectDelay;
        this.corePoolSize = corePoolSize;
        this.enableVoice = enableVoice;
        this.shutdownHook = enableShutdownHook ? new Thread(this::shutdown, "JDA Shutdown Hook") : null;
        this.enableBulkDeleteSplitting = enableBulkDeleteSplitting;
        this.autoReconnect = autoReconnect;
        this.idleProvider = idleProvider;
        this.retryOnTimeout = retryOnTimeout;
        this.useShutdownNow = useShutdownNow;
        this.contextProvider = contextProvider;
        this.enableMDC = enableMDC;
        this.enableCompression = enableCompression;

        synchronized (queue)
        {
            if (shardsTotal != -1)
            {
                if (shardIds == null)
                {
                    this.shards = new ShardCacheViewImpl(shardsTotal);
                    for (int i = 0; i < this.shardsTotal; i++)
                        this.queue.add(i);
                }
                else
                {
                    this.shards = new ShardCacheViewImpl(shardIds.size());
                    shardIds.stream().distinct().sorted().forEach(this.queue::add);
                }
            }
        }
    }

    @Override
    public void addEventListener(final Object... listeners)
    {
        ShardManager.super.addEventListener(listeners);
        Collections.addAll(this.listeners, listeners);
    }

    @Override
    public void removeEventListener(final Object... listeners)
    {
        ShardManager.super.removeEventListener(listeners);
        this.listeners.removeAll(Arrays.asList(listeners));
    }

    @Override
    public void addEventListeners(IntFunction<Object> eventListenerProvider)
    {
        ShardManager.super.addEventListeners(eventListenerProvider);
        this.listenerProviders.add(eventListenerProvider);
    }

    @Override
    public void removeEventListenerProvider(IntFunction<Object> eventListenerProvider)
    {
        this.listenerProviders.remove(eventListenerProvider);
    }

    @Override
    public int getShardsQueued()
    {
        return this.queue.size();
    }

    @Override
    public ShardCacheView getShardCache()
    {
        return this.shards;
    }

    public void login() throws LoginException
    {
        // building the first one in the current thread ensures that LoginException and IllegalArgumentException can be thrown on login
        JDAImpl jda = null;
        try
        {
            final int shardId = this.queue.isEmpty() ? 0 : this.queue.peek();

            jda = this.buildInstance(shardId);
            this.shards.getMap().put(shardId, jda);
            synchronized (queue)
            {
                this.queue.remove(shardId);
            }
        }
        catch (final InterruptedException e)
        {
            LOG.error("Interrupted Startup", e);
            throw new IllegalStateException(e);
        }
        catch (final Exception e)
        {
            if (jda != null)
            {
                if (this.useShutdownNow)
                    jda.shutdownNow();
                else
                    jda.shutdown();
            }

            throw e;
        }

        runQueueWorker();
        //this.worker = this.executor.scheduleWithFixedDelay(this::processQueue, 5000, 5000, TimeUnit.MILLISECONDS); // 5s for ratelimit

        if (this.shutdownHook != null)
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    @Override
    public void restart(final int shardId)
    {
        Checks.notNegative(shardId, "shardId");
        Checks.check(shardId < this.shardsTotal, "shardId must be lower than shardsTotal");

        final JDA jda = this.shards.getMap().remove(shardId);
        if (jda != null)
        {
            if (this.useShutdownNow)
                jda.shutdownNow();
            else
                jda.shutdown();
        }

        enqueueShard(shardId);
    }

    @Override
    public void restart()
    {
        TIntObjectMap<JDA> map = this.shards.getMap();
        synchronized (map)
        {
            Arrays.stream(map.keys())
                  .sorted() // this ensures shards are started in natural order
                  .forEach(this::restart);
        }
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
            catch (final Exception ignored) {}
        }

        this.executor.shutdown();

        if (this.shards != null)
        {
            for (final JDA jda : this.shards)
            {
                if (this.useShutdownNow)
                    jda.shutdownNow();
                else
                    jda.shutdown();
            }
        }
    }

    @Override
    public void shutdown(final int shardId)
    {
        final JDA jda = this.shards.getMap().remove(shardId);
        if (jda != null)
        {
            if (this.useShutdownNow)
                jda.shutdownNow();
            else
                jda.shutdown();
        }
    }

    @Override
    public void start(final int shardId)
    {
        Checks.notNegative(shardId, "shardId");
        Checks.check(shardId < this.shardsTotal, "shardId must be lower than shardsTotal");
        enqueueShard(shardId);
    }

    protected void enqueueShard(final int shardId)
    {
        synchronized (queue)
        {
            queue.add(shardId);
            runQueueWorker();
        }
    }

    protected void runQueueWorker()
    {
        if (worker != null)
            return;
        try
        {
            worker = executor.submit(() ->
            {
                while (!queue.isEmpty())
                    processQueue();
                this.gatewayURL = null;
                synchronized (queue)
                {
                    worker = null;
                    if (!shutdown.get() && !queue.isEmpty())
                        runQueueWorker();
                }
            });
        }
        catch (RejectedExecutionException ex)
        {
            LOG.debug("ThreadPool rejected queue worker thread", ex);
        }
    }

    protected void processQueue()
    {
        int shardId;

        if (this.shards == null)
        {
            shardId = 0;
        }
        else
        {
            Integer tmp = this.queue.peek();

            shardId = tmp == null ? -1 : tmp;
        }

        if (shardId == -1)
            return;

        JDAImpl api;
        try
        {
            api = this.shards == null ? null : (JDAImpl) this.shards.getElementById(shardId);

            if (api == null)
                api = this.buildInstance(shardId);
        }
        catch (InterruptedException e)
        {
            //caused by shutdown
            LOG.debug("Queue has been interrupted", e);
            return;
        }
        catch (LoginException e)
        {
            // this can only happen if the token has been changed
            // in this case the ShardManager will just shutdown itself as there currently is no way of hot-swapping the token on a running JDA instance.
            LOG.warn("The token has been invalidated and the ShardManager will shutdown!", e);
            this.shutdown();
            return;
        }
        catch (final Exception e)
        {
            LOG.error("Caught an exception in the queue processing thread", e);
            return;
        }

        this.shards.getMap().put(shardId, api);
        synchronized (queue)
        {
            this.queue.remove(shardId);
        }
    }

    protected JDAImpl buildInstance(final int shardId) throws LoginException, InterruptedException
    {
        final JDAImpl jda = new JDAImpl(AccountType.BOT, this.token, this.controller, this.httpClientBuilder, this.wsFactory,
            this.autoReconnect, this.enableVoice, false, this.enableBulkDeleteSplitting, this.retryOnTimeout, this.enableMDC,
            this.corePoolSize, this.maxReconnectDelay, this.contextProvider == null || !this.enableMDC ? null : contextProvider.apply(shardId));

        jda.asBot().setShardManager(this);

        if (this.eventManager != null)
            jda.setEventManager(this.eventManager);

        if (this.audioSendFactory != null)
            jda.setAudioSendFactory(this.audioSendFactory);

        this.listeners.forEach(jda::addEventListener);
        this.listenerProviders.forEach(provider -> jda.addEventListener(provider.apply(shardId)));
        jda.setStatus(JDA.Status.INITIALIZED); //This is already set by JDA internally, but this is to make sure the listeners catch it.

        // Set the presence information before connecting to have the correct information ready when sending IDENTIFY
        PresenceImpl presence = ((PresenceImpl) jda.getPresence());
        if (gameProvider != null)
            presence.setCacheGame(this.gameProvider.apply(shardId));
        if (idleProvider != null)
            presence.setCacheIdle(this.idleProvider.apply(shardId));
        if (statusProvider != null)
            presence.setCacheStatus(this.statusProvider.apply(shardId));

        if (this.gatewayURL == null)
        {
            try
            {
                Pair<String, Integer> gateway = jda.getGatewayBot();
                this.gatewayURL = gateway.getLeft();

                if (this.shardsTotal == -1)
                {
                    this.shardsTotal = gateway.getRight();
                    this.shards = new ShardCacheViewImpl(this.shardsTotal);

                    synchronized (queue)
                    {
                        for (int i = 0; i < shardsTotal; i++)
                            queue.add(i);
                    }
                }
            }
            catch (RuntimeException e)
            {
                if (e.getCause() instanceof InterruptedException)
                    throw (InterruptedException) e.getCause();
                //We check if the LoginException is masked inside of a ExecutionException which is masked inside of the RuntimeException
                Throwable ex = e.getCause() instanceof ExecutionException ? e.getCause().getCause() : null;
                if (ex instanceof LoginException)
                    throw new LoginException(ex.getMessage());
                else
                    throw e;
            }
        }

        final JDA.ShardInfo shardInfo = new JDA.ShardInfo(shardId, this.shardsTotal);

        final int shardTotal = jda.login(this.gatewayURL, shardInfo, this.enableCompression);
        if (this.shardsTotal == -1)
            this.shardsTotal = shardTotal;

        return jda;
    }

    @Override
    public void setGameProvider(IntFunction<Game> gameProvider)
    {
        ShardManager.super.setGameProvider(gameProvider);

        this.gameProvider = gameProvider;
    }

    @Override
    public void setIdleProvider(IntFunction<Boolean> idleProvider)
    {
        ShardManager.super.setIdleProvider(idleProvider);

        this.idleProvider = idleProvider;
    }

    @Override
    public void setStatusProvider(IntFunction<OnlineStatus> statusProvider)
    {
        ShardManager.super.setStatusProvider(statusProvider);

        this.statusProvider = statusProvider;
    }

    /**
     * This method creates the internal {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService}.
     * It is intended as a hook for custom implementations to create their own executor.
     *
     * @return A new ScheduledExecutorService
     */
    protected ScheduledExecutorService createExecutor(ThreadFactory threadFactory)
    {
        ThreadFactory factory = threadFactory == null
            ? DEFAULT_THREAD_FACTORY
            : threadFactory;

        return Executors.newSingleThreadScheduledExecutor(factory);
    }
}
