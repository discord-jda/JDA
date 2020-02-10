/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.sharding;

import gnu.trove.set.TIntSet;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.api.utils.cache.ShardCacheView;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.PresenceImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.ShardCacheViewImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import net.dv8tion.jda.internal.utils.config.MetaConfig;
import net.dv8tion.jda.internal.utils.config.SessionConfig;
import net.dv8tion.jda.internal.utils.config.ThreadingConfig;
import net.dv8tion.jda.internal.utils.config.sharding.*;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

/**
 * JDA's default {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} implementation.
 * To create new instances use the {@link net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder DefaultShardManagerBuilder}.
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
     * The executor that is used by the ShardManager internally to create new JDA instances.
     */
    protected final ScheduledExecutorService executor;

    /**
     * The queue of shards waiting for creation.
     */
    protected final Queue<Integer> queue = new ConcurrentLinkedQueue<>();

    /**
     * The {@link ShardCacheView ShardCacheView} that holds all shards.
     */
    protected ShardCacheViewImpl shards;

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
     * {@link PresenceProviderConfig} containing providers for activity and other presence information.
     */
    protected final PresenceProviderConfig presenceConfig;

    /**
     * {@link EventConfig} containing listeners and possibly a custom event manager.
     */
    protected final EventConfig eventConfig;

    /**
     * {@link ShardingConfig} containing information on shard specific meta information.
     */
    protected final ShardingConfig shardingConfig;

    /**
     * {@link ThreadingProviderConfig} containing a series of {@link ThreadPoolProvider} instances for shard specific configuration.
     */
    protected final ThreadingProviderConfig threadingConfig;

    /**
     * {@link ShardingSessionConfig} containing general configurations for sessions of shards like the http client.
     */
    protected final ShardingSessionConfig sessionConfig;

    /**
     * {@link ShardingMetaConfig} containing details on logging configuration, compression mode and shutdown behavior of the manager.
     */
    protected final ShardingMetaConfig metaConfig;

    /**
     * {@link ChunkingFilter} used to determine whether a guild should be lazy loaded or chunk members by default.
     */
    protected final ChunkingFilter chunkingFilter;

    public DefaultShardManager(@Nonnull String token)
    {
        this(token, null);
    }

    public DefaultShardManager(@Nonnull String token, @Nullable Collection<Integer> shardIds)
    {
        this(token, shardIds, null, null, null, null, null, null, null);
    }

    public DefaultShardManager(
        @Nonnull String token, @Nullable Collection<Integer> shardIds,
        @Nullable ShardingConfig shardingConfig, @Nullable EventConfig eventConfig,
        @Nullable PresenceProviderConfig presenceConfig, @Nullable ThreadingProviderConfig threadingConfig,
        @Nullable ShardingSessionConfig sessionConfig, @Nullable ShardingMetaConfig metaConfig,
        @Nullable ChunkingFilter chunkingFilter)
    {
        this.token = token;
        this.eventConfig = eventConfig == null ? EventConfig.getDefault() : eventConfig;
        this.shardingConfig = shardingConfig == null ? ShardingConfig.getDefault() : shardingConfig;
        this.threadingConfig = threadingConfig == null ? ThreadingProviderConfig.getDefault() : threadingConfig;
        this.sessionConfig = sessionConfig == null ? ShardingSessionConfig.getDefault() : sessionConfig;
        this.presenceConfig = presenceConfig == null ? PresenceProviderConfig.getDefault() : presenceConfig;
        this.metaConfig = metaConfig == null ? ShardingMetaConfig.getDefault() : metaConfig;
        this.chunkingFilter = chunkingFilter == null ? ChunkingFilter.ALL : chunkingFilter;
        this.executor = createExecutor(this.threadingConfig.getThreadFactory());
        this.shutdownHook = this.metaConfig.isUseShutdownHook() ? new Thread(this::shutdown, "JDA Shutdown Hook") : null;

        synchronized (queue)
        {
            if (getShardsTotal() != -1)
            {
                if (shardIds == null)
                {
                    this.shards = new ShardCacheViewImpl(getShardsTotal());
                    for (int i = 0; i < getShardsTotal(); i++)
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
    public void addEventListener(@Nonnull final Object... listeners)
    {
        ShardManager.super.addEventListener(listeners);
        for (Object o : listeners)
            eventConfig.addEventListener(o);
    }

    @Override
    public void removeEventListener(@Nonnull final Object... listeners)
    {
        ShardManager.super.removeEventListener(listeners);
        for (Object o : listeners)
            eventConfig.removeEventListener(o);
    }

    @Override
    public void addEventListeners(@Nonnull IntFunction<Object> eventListenerProvider)
    {
        ShardManager.super.addEventListeners(eventListenerProvider);
        eventConfig.addEventListenerProvider(eventListenerProvider);
    }

    @Override
    public void removeEventListenerProvider(@Nonnull IntFunction<Object> eventListenerProvider)
    {
        eventConfig.removeEventListenerProvider(eventListenerProvider);
    }

    @Override
    public int getShardsQueued()
    {
        return this.queue.size();
    }

    @Override
    public int getShardsTotal()
    {
        return shardingConfig.getShardsTotal();
    }

    @Override
    public Guild getGuildById(long id)
    {
        int shardId = MiscUtil.getShardForGuild(id, getShardsTotal());
        JDA shard = this.getShardById(shardId);
        return shard == null ? null : shard.getGuildById(id);
    }

    @Nonnull
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
            try (UnlockHook hook = this.shards.writeLock())
            {
                this.shards.getMap().put(shardId, jda);
            }
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
                if (shardingConfig.isUseShutdownNow())
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
        Checks.check(shardId < getShardsTotal(), "shardId must be lower than shardsTotal");

        try (UnlockHook hook = this.shards.writeLock())
        {
            final JDA jda = this.shards.getMap().remove(shardId);
            if (jda != null)
            {
                if (shardingConfig.isUseShutdownNow())
                    jda.shutdownNow();
                else
                    jda.shutdown();
            }
        }

        enqueueShard(shardId);
    }

    @Override
    public void restart()
    {
        TIntSet map = this.shards.keySet();

        Arrays.stream(map.toArray())
              .sorted() // this ensures shards are started in natural order
              .forEach(this::restart);
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
            this.shards.forEach(jda ->
            {
                if (shardingConfig.isUseShutdownNow())
                    jda.shutdownNow();
                else
                    jda.shutdown();
            });
        }
    }

    @Override
    public void shutdown(final int shardId)
    {
        try (UnlockHook hook = this.shards.writeLock())
        {
            final JDA jda = this.shards.getMap().remove(shardId);
            if (jda != null)
            {
                if (shardingConfig.isUseShutdownNow())
                    jda.shutdownNow();
                else
                    jda.shutdown();
            }
        }
    }

    @Override
    public void start(final int shardId)
    {
        Checks.notNegative(shardId, "shardId");
        Checks.check(shardId < getShardsTotal(), "shardId must be lower than shardsTotal");
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

        try (UnlockHook hook = this.shards.writeLock())
        {
            this.shards.getMap().put(shardId, api);
        }
        synchronized (queue)
        {
            this.queue.remove(shardId);
        }
    }

    protected JDAImpl buildInstance(final int shardId) throws LoginException, InterruptedException
    {
        OkHttpClient httpClient = sessionConfig.getHttpClient();
        if (httpClient == null)
        {
            //httpClient == null implies we have a builder
            //noinspection ConstantConditions
            httpClient = sessionConfig.getHttpBuilder().build();
        }

        // imagine if we had macros or closures or destructuring :)
        ExecutorPair<ScheduledExecutorService> rateLimitPair = resolveExecutor(threadingConfig.getRateLimitPoolProvider(), shardId);
        ScheduledExecutorService rateLimitPool = rateLimitPair.executor;
        boolean shutdownRateLimitPool = rateLimitPair.automaticShutdown;

        ExecutorPair<ScheduledExecutorService> gatewayPair = resolveExecutor(threadingConfig.getGatewayPoolProvider(), shardId);
        ScheduledExecutorService gatewayPool = gatewayPair.executor;
        boolean shutdownGatewayPool = gatewayPair.automaticShutdown;

        ExecutorPair<ExecutorService> callbackPair = resolveExecutor(threadingConfig.getCallbackPoolProvider(), shardId);
        ExecutorService callbackPool = callbackPair.executor;
        boolean shutdownCallbackPool = callbackPair.automaticShutdown;

        AuthorizationConfig authConfig = new AuthorizationConfig(AccountType.BOT, token);
        SessionConfig sessionConfig = this.sessionConfig.toSessionConfig(httpClient);
        ThreadingConfig threadingConfig = new ThreadingConfig();
        threadingConfig.setRateLimitPool(rateLimitPool, shutdownRateLimitPool);
        threadingConfig.setGatewayPool(gatewayPool, shutdownGatewayPool);
        threadingConfig.setCallbackPool(callbackPool, shutdownCallbackPool);
        MetaConfig metaConfig = new MetaConfig(this.metaConfig.getMaxBufferSize(), this.metaConfig.getContextMap(shardId), this.metaConfig.getCacheFlags(), this.sessionConfig.getFlags());
        final JDAImpl jda = new JDAImpl(authConfig, sessionConfig, threadingConfig, metaConfig);
        jda.setChunkingFilter(chunkingFilter);
        threadingConfig.init(jda::getIdentifierString);

        jda.setShardManager(this);

        if (eventConfig.getEventManagerProvider() != null)
            jda.setEventManager(this.eventConfig.getEventManagerProvider().apply(shardId));

        if (this.sessionConfig.getAudioSendFactory() != null)
            jda.setAudioSendFactory(this.sessionConfig.getAudioSendFactory());

        this.eventConfig.getListeners().forEach(jda::addEventListener);
        this.eventConfig.getListenerProviders().forEach(provider -> jda.addEventListener(provider.apply(shardId)));
        jda.setStatus(JDA.Status.INITIALIZED); //This is already set by JDA internally, but this is to make sure the listeners catch it.

        // Set the presence information before connecting to have the correct information ready when sending IDENTIFY
        PresenceImpl presence = ((PresenceImpl) jda.getPresence());
        if (presenceConfig.getActivityProvider() != null)
            presence.setCacheActivity(presenceConfig.getActivityProvider().apply(shardId));
        if (presenceConfig.getIdleProvider() != null)
            presence.setCacheIdle(presenceConfig.getIdleProvider().apply(shardId));
        if (presenceConfig.getStatusProvider() != null)
            presence.setCacheStatus(presenceConfig.getStatusProvider().apply(shardId));

        if (this.gatewayURL == null)
        {
            try
            {
                SessionController.ShardedGateway gateway = jda.getShardedGateway();
                this.gatewayURL = gateway.getUrl();
                if (this.gatewayURL == null)
                    LOG.error("Acquired null gateway url from SessionController");
                else
                    LOG.info("Login Successful!");

                if (getShardsTotal() == -1)
                {
                    shardingConfig.setShardsTotal(gateway.getShardTotal());
                    this.shards = new ShardCacheViewImpl(getShardsTotal());

                    synchronized (queue)
                    {
                        for (int i = 0; i < getShardsTotal(); i++)
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

        final JDA.ShardInfo shardInfo = new JDA.ShardInfo(shardId, getShardsTotal());

        final int shardTotal = jda.login(this.gatewayURL, shardInfo, this.metaConfig.getCompression(), false);
        if (getShardsTotal() == -1)
            shardingConfig.setShardsTotal(shardTotal);

        return jda;
    }

    @Override
    public void setActivityProvider(IntFunction<? extends Activity> activityProvider)
    {
        ShardManager.super.setActivityProvider(activityProvider);
        presenceConfig.setActivityProvider(activityProvider);
    }

    @Override
    public void setIdleProvider(@Nonnull IntFunction<Boolean> idleProvider)
    {
        ShardManager.super.setIdleProvider(idleProvider);
        presenceConfig.setIdleProvider(idleProvider);
    }

    @Override
    public void setPresenceProvider(IntFunction<OnlineStatus> statusProvider, IntFunction<? extends Activity> activityProvider)
    {
        ShardManager.super.setPresenceProvider(statusProvider, activityProvider);
        presenceConfig.setStatusProvider(statusProvider);
        presenceConfig.setActivityProvider(activityProvider);
    }

    @Override
    public void setStatusProvider(IntFunction<OnlineStatus> statusProvider)
    {
        ShardManager.super.setStatusProvider(statusProvider);
        presenceConfig.setStatusProvider(statusProvider);
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

    protected static <E extends ExecutorService> ExecutorPair<E> resolveExecutor(ThreadPoolProvider<? extends E> provider, int shardId)
    {
        E executor = null;
        boolean automaticShutdown = true;
        if (provider != null)
        {
            executor = provider.provide(shardId);
            automaticShutdown = provider.shouldShutdownAutomatically(shardId);
        }
        return new ExecutorPair<>(executor, automaticShutdown);
    }

    protected static class ExecutorPair<E extends ExecutorService>
    {
        protected final E executor;
        protected final boolean automaticShutdown;

        protected ExecutorPair(E executor, boolean automaticShutdown)
        {
            this.executor = executor;
            this.automaticShutdown = automaticShutdown;
        }
    }
}
