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
import gnu.trove.map.TIntObjectMap;
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
import net.dv8tion.jda.core.requests.WebSocketClient;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.tuple.Pair;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
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
     * The maximum amount of time that JDA will back off to wait when attempting to reconnect the MainWebsocket.
     */
    protected final int maxReconnectDelay;

    /**
     * The executor that is used by the ShardManager internally to create new JDA instances.
     */
    protected final ScheduledExecutorService executor;

    /**
     * The queue of shards waiting for creation or to be reconnected.
     */
    protected final Queue<Integer> queue = new ConcurrentLinkedQueue<>();

    /**
     * The queue that will be used by all shards to ensure reconnects don't hit the ratelimit.
     */
    protected final SessionReconnectQueue sessionReconnectQueue = createReconnectQueue();

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
     * Creates a new DefaultShardManager instance.
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
     */
    protected DefaultShardManager(final int shardsTotal, final Collection<Integer> shardIds, final List<Object> listeners,
                                  final String token, final IEventManager eventManager, final IAudioSendFactory audioSendFactory,
                                  final IntFunction<Game> gameProvider, final IntFunction<OnlineStatus> statusProvider,
                                  final OkHttpClient.Builder httpClientBuilder, final WebSocketFactory wsFactory,
                                  final ThreadFactory threadFactory, final ShardedRateLimiter shardedRateLimiter,
                                  final int maxReconnectDelay, final int corePoolSize, final boolean enableVoice,
                                  final boolean enableShutdownHook, final boolean enableBulkDeleteSplitting,
                                  final boolean autoReconnect, final IntFunction<Boolean> idleProvider,
                                  final boolean retryOnTimeout, boolean useShutdownNow)
    {
        this.shardsTotal = shardsTotal;
        this.listeners = listeners;
        this.token = token;
        this.eventManager = eventManager;
        this.audioSendFactory = audioSendFactory;
        this.gameProvider = gameProvider;
        this.statusProvider = statusProvider;
        this.httpClientBuilder = httpClientBuilder == null ? new OkHttpClient.Builder() : httpClientBuilder;
        this.wsFactory = wsFactory == null ? new WebSocketFactory() : wsFactory;
        this.executor = createExecutor(threadFactory);
        this.shardedRateLimiter = shardedRateLimiter == null ? new ShardedRateLimiter() : shardedRateLimiter;
        this.maxReconnectDelay = maxReconnectDelay;
        this.corePoolSize = corePoolSize;
        this.enableVoice = enableVoice;
        this.shutdownHook = enableShutdownHook ? new Thread(this::shutdown, "JDA Shutdown Hook") : null;
        this.enableBulkDeleteSplitting = enableBulkDeleteSplitting;
        this.autoReconnect = autoReconnect;
        this.idleProvider = idleProvider;
        this.retryOnTimeout = retryOnTimeout;
        this.useShutdownNow = useShutdownNow;

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
    public int getShardsQueued()
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
            final int shardId = this.queue.isEmpty() ? 0 : this.queue.peek();

            jda = this.buildInstance(shardId);
            this.shards.getMap().put(shardId, jda);
            this.queue.remove(shardId);
        }
        catch (final RateLimitedException e)
        {
            // add not remove 'shardId' from the queue and try the first one again after 5 seconds in the async thread
        }
        catch (final Exception e)
        {
            if (jda != null)
                if (this.useShutdownNow)
                    jda.shutdownNow();
                else
                    jda.shutdown();

            throw e;
        }

        this.worker = this.executor.scheduleWithFixedDelay(this::processQueue, 5000, 5000, TimeUnit.MILLISECONDS); // 5s for ratelimit

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
            if (this.useShutdownNow)
                jda.shutdownNow();
            else
                jda.shutdown();

        this.queue.add(shardId);
    }

    @Override
    public void restart()
    {
        TIntObjectMap<JDA> map = this.shards.getMap();
        synchronized (map)
        {
            Arrays.stream(map.keys())
                .sorted() // this ensures shards are started in natural order
                .forEach(id ->
                {
                    final JDA jda = map.remove(id);
                    if (jda != null)
                        if (this.useShutdownNow)
                            jda.shutdownNow();
                        else
                            jda.shutdown();

                    this.queue.add(id);
                });
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
                if (this.useShutdownNow)
                    jda.shutdownNow();
                else
                    jda.shutdown();
        }
    }

    @Override
    public void shutdown(final int shardId)
    {
        final JDA jda = this.shards.getMap().remove(shardId);
        if (jda != null)
            if (this.useShutdownNow)
                jda.shutdownNow();
            else
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

        JDAImpl api = null;
        try
        {
            api = this.shards == null ? null : (JDAImpl) this.shards.getElementById(shardId);

            if (api == null)
                api = this.buildInstance(shardId);
            else if (api.getStatus() == JDA.Status.RECONNECT_QUEUED)
                api.getClient().reconnect(true, true);

            // as this happens before removing the shardId if from the queue just try again after 5 seconds
        }
        catch (RateLimitedException e)
        {
            LOG.warn("Hit the login ratelimit while creating new JDA instances");
        }
        catch (LoginException e)
        {
            // this can only happen if the token has been changed
            // in this case the ShardManager will just shutdown itself as there currently is no way of hot-swapping the token on a running JDA instance.
            LOG.warn("The token has been invalidated and the ShardManager will shutdown!", e);
            this.shutdown();
        }
        catch (final Exception e)
        {
            LOG.error("Caught an exception in the queue processing thread", e);
        }

        this.shards.getMap().put(shardId, api);
        this.queue.remove(shardId);
    }

    protected JDAImpl buildInstance(final int shardId) throws LoginException, RateLimitedException
    {
        final JDAImpl jda = new JDAImpl(AccountType.BOT, this.token, this.httpClientBuilder, this.wsFactory, this.shardedRateLimiter,
            this.autoReconnect, this.enableVoice, false, this.enableBulkDeleteSplitting, retryOnTimeout,
            this.corePoolSize, this.maxReconnectDelay);

        jda.asBot().setShardManager(this);

        if (this.eventManager != null)
            jda.setEventManager(this.eventManager);

        if (this.audioSendFactory != null)
            jda.setAudioSendFactory(this.audioSendFactory);

        this.listeners.forEach(jda::addEventListener);
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
                Pair<String, Integer> gateway = jda.getGatewayBot().complete();
                this.gatewayURL = gateway.getLeft();

                if (this.shardsTotal == -1)
                {
                    this.shardsTotal = gateway.getRight();
                    this.shards = new ShardCacheViewImpl(this.shardsTotal);

                    for (int i = 0; i < shardsTotal; i++)
                        queue.add(i);
                }
            }
            catch (RuntimeException e)
            {
                //We check if the LoginException is masked inside of a ExecutionException which is masked inside of the RuntimeException
                Throwable ex = e.getCause() instanceof ExecutionException ? e.getCause().getCause() : null;
                if (ex instanceof LoginException)
                    throw new LoginException(ex.getMessage());
                else
                    throw e;
            }
        }

        final JDA.ShardInfo shardInfo = new JDA.ShardInfo(shardId, this.shardsTotal);

        final int shardTotal = jda.login(this.gatewayURL, shardInfo, this.sessionReconnectQueue);
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
            catch (InterruptedException ignored) {}
        }

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
            ? r ->
                {
                    final Thread t = new Thread(r, "DefaultShardManager");
                    t.setDaemon(true);
                    t.setPriority(Thread.NORM_PRIORITY + 1);
                    return t;
                }
            : threadFactory;

        return Executors.newSingleThreadScheduledExecutor(factory);
    }

    /**
     * This method creates the internal {@link net.dv8tion.jda.core.requests.SessionReconnectQueue SessionReconnectQueue}.
     * It is intended as a hook for custom implementations to create their own queue.
     *
     * <p><b>NOTE: The default implementation will add reconnects to the same queue as connects so they don't interfere with each other
     * (they share the same rate limit). If you override this you need to take care of it yourself.</b>
     *
     * @return A new ScheduledExecutorService
     */
    protected SessionReconnectQueue createReconnectQueue()
    {
        return new ForwardingSessionReconnectQueue(
            jda -> queue.add(jda.getShardInfo().getShardId()),
            jda -> queue.remove(jda.getShardInfo().getShardId()));
    }

    public class ForwardingSessionReconnectQueue extends SessionReconnectQueue
    {
        private final Consumer<JDA> appender;
        private final Consumer<JDA> remover;

        public ForwardingSessionReconnectQueue(Consumer<JDA> appender, Consumer<JDA> remover)
        {
            super(null);

            this.appender = appender == null ? jda -> {} : appender;
            this.remover = remover == null ? jda -> {} : remover;
        }

        @Override
        protected void appendSession(final WebSocketClient client)
        {
            this.appender.accept(client.getJDA());
        }

        @Override
        protected void removeSession(final WebSocketClient client)
        {
            this.remover.accept(client.getJDA());
        }

        @Override
        protected void runWorker() { /* just to overwrite */ }
    }
}
