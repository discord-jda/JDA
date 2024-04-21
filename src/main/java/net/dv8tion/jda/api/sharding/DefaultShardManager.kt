/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.sharding

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDA.ShardInfo
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import net.dv8tion.jda.api.requests.*
import net.dv8tion.jda.api.requests.GatewayIntent.Companion.getIntents
import net.dv8tion.jda.api.requests.RestRateLimiter.GlobalRateLimit.Companion.create
import net.dv8tion.jda.api.requests.RestRateLimiter.RateLimitConfig
import net.dv8tion.jda.api.requests.Route.CompiledRoute
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.cache.ShardCacheView
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.SelfUserImpl
import net.dv8tion.jda.internal.managers.PresenceImpl
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.IOUtil
import net.dv8tion.jda.internal.utils.JDALogger
import net.dv8tion.jda.internal.utils.cache.ShardCacheViewImpl
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig
import net.dv8tion.jda.internal.utils.config.MetaConfig
import net.dv8tion.jda.internal.utils.config.ThreadingConfig
import net.dv8tion.jda.internal.utils.config.sharding.*
import okhttp3.Headers.Builder.build
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder.build
import okhttp3.Request.Builder.build
import okhttp3.Request.Builder.get
import okhttp3.Request.Builder.header
import okhttp3.Request.Builder.url
import okhttp3.Response
import okhttp3.Response.Builder.build
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import java.util.function.IntFunction
import java.util.stream.Stream
import javax.annotation.Nonnull

/**
 * JDA's default [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] implementation.
 * To create new instances use the [DefaultShardManagerBuilder][net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder].
 *
 * @since  3.4
 * @author Aljoscha Grebe
 */
class DefaultShardManager @JvmOverloads constructor(
    /**
     * The token of the account associated with this ShardManager.
     */
    @param:Nonnull protected val token: String?,
    shardIds: Collection<Int>? = null,
    shardingConfig: ShardingConfig? = null,
    eventConfig: EventConfig? = null,
    presenceConfig: PresenceProviderConfig? = null,
    threadingConfig: ThreadingProviderConfig? = null,
    sessionConfig: ShardingSessionConfig? = null,
    metaConfig: ShardingMetaConfig? = null,
    restConfigProvider: IntFunction<out RestConfig>? = null,
    chunkingFilter: ChunkingFilter? = null
) : ShardManager {
    /**
     * The executor that is used by the ShardManager internally to create new JDA instances.
     */
    protected val executor: ScheduledExecutorService

    /**
     * The queue of shards waiting for creation.
     */
    protected val queue: Queue<Int> = ConcurrentLinkedQueue()

    /**
     * The [ShardCacheView] that holds all shards.
     */
    protected override var shards: ShardCacheViewImpl? = null

    /**
     * This can be used to check if the ShardManager is shutting down.
     */
    protected val shutdown = AtomicBoolean(false)

    /**
     * The shutdown hook used by this ShardManager. If this is null the shutdown hook is disabled.
     */
    protected val shutdownHook: Thread?

    /**
     * The worker running on the [ScheduledExecutorService][.executor] that spawns new shards.
     */
    protected var worker: Future<*>? = null

    /**
     * The gateway url for JDA to use. Will be `nul` until the first shard is created.
     */
    protected var gatewayURL: String? = null

    /**
     * [PresenceProviderConfig] containing providers for activity and other presence information.
     */
    protected val presenceConfig: PresenceProviderConfig

    /**
     * [EventConfig] containing listeners and possibly a custom event manager.
     */
    protected val eventConfig: EventConfig

    /**
     * [ShardingConfig] containing information on shard specific meta information.
     */
    protected val shardingConfig: ShardingConfig

    /**
     * [ThreadingProviderConfig] containing a series of [ThreadPoolProvider] instances for shard specific configuration.
     */
    protected val threadingConfig: ThreadingProviderConfig

    /**
     * [ShardingSessionConfig] containing general configurations for sessions of shards like the http client.
     */
    protected val sessionConfig: ShardingSessionConfig

    /**
     * [ShardingMetaConfig] containing details on logging configuration, compression mode and shutdown behavior of the manager.
     */
    protected val metaConfig: ShardingMetaConfig

    /**
     * [ChunkingFilter] used to determine whether a guild should be lazy loaded or chunk members by default.
     */
    protected val chunkingFilter: ChunkingFilter
    protected val restConfigProvider: IntFunction<out RestConfig>

    init {
        this.eventConfig = eventConfig ?: EventConfig.getDefault()
        this.shardingConfig = shardingConfig ?: ShardingConfig.getDefault()
        this.threadingConfig = threadingConfig ?: ThreadingProviderConfig.getDefault()
        this.sessionConfig = sessionConfig ?: ShardingSessionConfig.getDefault()
        this.presenceConfig = presenceConfig ?: PresenceProviderConfig.getDefault()
        this.metaConfig = metaConfig ?: ShardingMetaConfig.getDefault()
        this.chunkingFilter = chunkingFilter ?: ChunkingFilter.ALL
        this.restConfigProvider = restConfigProvider ?: IntFunction { i: Int -> RestConfig() }
        executor = createExecutor(this.threadingConfig.threadFactory)
        shutdownHook = if (this.metaConfig.isUseShutdownHook) Thread({ this.shutdown() }, "JDA Shutdown Hook") else null
        synchronized(queue) {
            if (shardsTotal != -1) {
                if (shardIds == null) {
                    shards = ShardCacheViewImpl(shardsTotal)
                    for (i in 0 until shardsTotal) queue.add(i)
                } else {
                    shards = ShardCacheViewImpl(shardIds.size)
                    shardIds.stream().distinct().sorted().forEach { e: Int -> queue.add(e) }
                }
            }
        }
    }

    @get:Nonnull
    override val gatewayIntents: EnumSet<GatewayIntent?>
        get() = getIntents(shardingConfig.intents)

    override fun addEventListener(@Nonnull vararg listeners: Any?) {
        super.addEventListener(*listeners)
        for (o in listeners) eventConfig.addEventListener(o!!)
    }

    override fun removeEventListener(@Nonnull vararg listeners: Any?) {
        super.removeEventListener(*listeners)
        for (o in listeners) eventConfig.removeEventListener(o!!)
    }

    override fun addEventListeners(@Nonnull eventListenerProvider: IntFunction<Any?>) {
        super.addEventListeners(eventListenerProvider)
        eventConfig.addEventListenerProvider(eventListenerProvider)
    }

    override fun removeEventListenerProvider(@Nonnull eventListenerProvider: IntFunction<Any?>?) {
        eventConfig.removeEventListenerProvider(eventListenerProvider!!)
    }

    override val shardsQueued: Int
        get() = queue.size
    override val shardsTotal: Int
        get() = shardingConfig.shardsTotal

    override fun getGuildById(id: Long): Guild? {
        val shardId = MiscUtil.getShardForGuild(id, shardsTotal)
        val shard = this.getShardById(shardId)
        return shard?.getGuildById(id)
    }

    @get:Nonnull
    override val shardCache: ShardCacheView?
        get() = shards

    override fun login() {
        // building the first one in the current thread ensures that InvalidTokenException and IllegalArgumentException can be thrown on login
        var jda: JDAImpl? = null
        try {
            val shardId = if (queue.isEmpty()) 0 else queue.peek()
            jda = buildInstance(shardId)
            shards!!.writeLock().use { hook -> shards!!.getMap().put(shardId, jda) }
            synchronized(queue) { queue.remove(shardId) }
        } catch (e: Exception) {
            if (jda != null) {
                if (shardingConfig.isUseShutdownNow) jda.shutdownNow() else jda.shutdown()
            }
            throw e
        }
        runQueueWorker()
        //this.worker = this.executor.scheduleWithFixedDelay(this::processQueue, 5000, 5000, TimeUnit.MILLISECONDS); // 5s for ratelimit
        if (shutdownHook != null) Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    override fun restart(shardId: Int) {
        Checks.notNegative(shardId, "shardId")
        Checks.check(shardId < shardsTotal, "shardId must be lower than shardsTotal")
        val jda = shards!!.remove(shardId)
        if (jda != null) {
            if (shardingConfig.isUseShutdownNow) jda.shutdownNow() else jda.shutdown()
        }
        enqueueShard(shardId)
    }

    override fun restart() {
        val map = shards!!.keySet()
        Arrays.stream(map.toArray())
            .sorted() // this ensures shards are started in natural order
            .forEach { shardId: Int -> this.restart(shardId) }
    }

    override fun shutdown() {
        if (shutdown.getAndSet(true)) return  // shutdown has already been requested
        if (worker != null && !worker!!.isDone) worker!!.cancel(true)
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook)
            } catch (ignored: Exception) {
            }
        }
        if (shards != null) {
            executor.execute {
                synchronized(queue) {
                    shards!!.forEach(Consumer { jda: JDA -> if (shardingConfig.isUseShutdownNow) jda.shutdownNow() else jda.shutdown() })
                    queue.clear()
                }
                executor.shutdown()
            }
        } else {
            executor.shutdown()
        }

        // Shutdown shared pools
        threadingConfig.shutdown()
    }

    override fun shutdown(shardId: Int) {
        val jda = shards!!.remove(shardId)
        if (jda != null) {
            if (shardingConfig.isUseShutdownNow) jda.shutdownNow() else jda.shutdown()
        }
    }

    override fun start(shardId: Int) {
        Checks.notNegative(shardId, "shardId")
        Checks.check(shardId < shardsTotal, "shardId must be lower than shardsTotal")
        enqueueShard(shardId)
    }

    protected fun enqueueShard(shardId: Int) {
        synchronized(queue) {
            queue.add(shardId)
            runQueueWorker()
        }
    }

    protected fun runQueueWorker() {
        if (shutdown.get()) throw RejectedExecutionException("ShardManager is already shutdown!")
        if (worker != null) return
        worker = executor.submit {
            while (!queue.isEmpty() && !Thread.currentThread().isInterrupted) processQueue()
            gatewayURL = null
            synchronized(queue) {
                worker = null
                if (!shutdown.get() && !queue.isEmpty()) runQueueWorker()
            }
        }
    }

    protected fun processQueue() {
        val shardId: Int
        shardId = if (shards == null) {
            0
        } else {
            val tmp = queue.peek()
            tmp ?: -1
        }
        if (shardId == -1) return
        var api: JDAImpl?
        try {
            api = if (shards == null) null else shards!!.getElementById(shardId) as JDAImpl?
            if (api == null) api = buildInstance(shardId)
        } catch (e: CompletionException) {
            if (e.cause is InterruptedException) LOG.debug("The worker thread was interrupted") else LOG.error(
                "Caught an exception in queue processing thread",
                e
            )
            return
        } catch (e: InvalidTokenException) {
            // this can only happen if the token has been changed
            // in this case the ShardManager will just shutdown itself as there currently is no way of hot-swapping the token on a running JDA instance.
            LOG.warn("The token has been invalidated and the ShardManager will shutdown!", e)
            this.shutdown()
            return
        } catch (e: Exception) {
            LOG.error("Caught an exception in the queue processing thread", e)
            return
        }
        shards!!.writeLock().use { hook -> shards!!.getMap().put(shardId, api) }
        synchronized(queue) { queue.remove(shardId) }
    }

    protected fun buildInstance(shardId: Int): JDAImpl {
        var httpClient = sessionConfig.httpClient
        if (httpClient == null) {
            //httpClient == null implies we have a builder
            httpClient = sessionConfig.httpBuilder!!.build()
        }
        retrieveShardTotal(httpClient)
        threadingConfig.init(if (queue.isEmpty()) shardsTotal else queue.size)

        // imagine if we had macros or closures or destructuring :)
        val rateLimitSchedulerPair: ExecutorPair<ScheduledExecutorService> =
            resolveExecutor(threadingConfig.rateLimitSchedulerProvider, shardId)
        val rateLimitScheduler = rateLimitSchedulerPair.executor
        val shutdownRateLimitScheduler = rateLimitSchedulerPair.automaticShutdown
        val rateLimitElasticPair: ExecutorPair<ExecutorService> =
            resolveExecutor(threadingConfig.rateLimitElasticProvider, shardId)
        val rateLimitElastic = rateLimitElasticPair.executor
        val shutdownRateLimitElastic = rateLimitElasticPair.automaticShutdown
        val gatewayPair: ExecutorPair<ScheduledExecutorService> =
            resolveExecutor(threadingConfig.gatewayPoolProvider, shardId)
        val gatewayPool = gatewayPair.executor
        val shutdownGatewayPool = gatewayPair.automaticShutdown
        val callbackPair: ExecutorPair<ExecutorService> = resolveExecutor(threadingConfig.callbackPoolProvider, shardId)
        val callbackPool = callbackPair.executor
        val shutdownCallbackPool = callbackPair.automaticShutdown
        val eventPair: ExecutorPair<ExecutorService> = resolveExecutor(threadingConfig.eventPoolProvider, shardId)
        val eventPool = eventPair.executor
        val shutdownEventPool = eventPair.automaticShutdown
        val audioPair: ExecutorPair<ScheduledExecutorService> =
            resolveExecutor(threadingConfig.audioPoolProvider, shardId)
        val audioPool = audioPair.executor
        val shutdownAudioPool = audioPair.automaticShutdown
        val authConfig = AuthorizationConfig(token!!)
        val sessionConfig = sessionConfig.toSessionConfig(httpClient)
        val threadingConfig = ThreadingConfig()
        threadingConfig.setRateLimitScheduler(rateLimitScheduler, shutdownRateLimitScheduler)
        threadingConfig.setRateLimitElastic(rateLimitElastic, shutdownRateLimitElastic)
        threadingConfig.setGatewayPool(gatewayPool, shutdownGatewayPool)
        threadingConfig.setCallbackPool(callbackPool, shutdownCallbackPool)
        threadingConfig.setEventPool(eventPool, shutdownEventPool)
        threadingConfig.setAudioPool(audioPool, shutdownAudioPool)
        val metaConfig = MetaConfig(
            metaConfig.maxBufferSize,
            metaConfig.getContextMap(shardId),
            metaConfig.cacheFlags,
            this.sessionConfig.flags
        )
        var restConfig = restConfigProvider.apply(shardId)
        if (restConfig == null) restConfig = RestConfig()
        val jda = JDAImpl(authConfig, sessionConfig, threadingConfig, metaConfig, restConfig)
        jda.setMemberCachePolicy(shardingConfig.memberCachePolicy)
        threadingConfig.init { jda.identifierString }
        jda.initRequester()

        // We can only do member chunking with the GUILD_MEMBERS intent
        if (shardingConfig.intents and GatewayIntent.GUILD_MEMBERS.rawValue == 0) jda.setChunkingFilter(ChunkingFilter.NONE) else jda.setChunkingFilter(
            chunkingFilter
        )
        jda.setShardManager(this)
        if (eventConfig.eventManagerProvider != null) jda.setEventManager(
            eventConfig.eventManagerProvider!!.apply(shardId)
        )
        if (this.sessionConfig.audioSendFactory != null) jda.setAudioSendFactory(this.sessionConfig.audioSendFactory)
        jda.addEventListener(*eventConfig.listeners.toTypedArray())
        eventConfig.listenerProviders.forEach(Consumer { provider: IntFunction<Any?> ->
            jda.addEventListener(
                provider.apply(
                    shardId
                )
            )
        })

        // Set the presence information before connecting to have the correct information ready when sending IDENTIFY
        val presence = jda.presence as PresenceImpl
        if (presenceConfig.activityProvider != null) presence.setCacheActivity(
            presenceConfig.activityProvider!!.apply(shardId)
        )
        if (presenceConfig.idleProvider != null) presence.setCacheIdle(presenceConfig.idleProvider!!.apply(shardId))
        if (presenceConfig.statusProvider != null) presence.setCacheStatus(presenceConfig.statusProvider!!.apply(shardId))
        if (gatewayURL == null) {
            val gateway = jda.shardedGateway
            this.sessionConfig.sessionController.setConcurrency(gateway.concurrency)
            gatewayURL = gateway.url
            checkNotNull(gatewayURL) { "Acquired null gateway url from SessionController" }
            LOG.info("Login Successful!")
        }
        val shardInfo = ShardInfo(shardId, shardsTotal)

        // Initialize SelfUser instance before logging in
        var selfUser = shardCache!!.applyStream { s: Stream<JDA> ->
            s.map { obj: JDA -> obj.getSelfUser() } // this should never throw!
                .findFirst().orElse(null)
        }

        // Copy from other JDA instance or do initial fetch
        selfUser = if (selfUser == null) retrieveSelfUser(jda) else SelfUserImpl.copyOf(selfUser as SelfUserImpl, jda)
        jda.setSelfUser(selfUser)
        jda.setStatus(JDA.Status.INITIALIZED) //This is already set by JDA internally, but this is to make sure the listeners catch it.
        jda.login(
            gatewayURL,
            shardInfo,
            this.metaConfig.compression,
            false,
            shardingConfig.intents,
            this.metaConfig.encoding
        )
        return jda
    }

    private fun retrieveSelfUser(jda: JDAImpl): SelfUser? {
        val route = Route.Self.GET_SELF.compile()
        return RestActionImpl(
            jda, route
        ) { response: net.dv8tion.jda.api.requests.Response, request: Request<SelfUser?>? ->
            jda.entityBuilder.createSelfUser(
                response.`object`
            )
        }.complete()
    }

    override fun setActivityProvider(activityProvider: IntFunction<out Activity?>?) {
        super.setActivityProvider(activityProvider)
        presenceConfig.activityProvider = activityProvider
    }

    override fun setIdleProvider(@Nonnull idleProvider: IntFunction<Boolean?>) {
        super.setIdleProvider(idleProvider)
        presenceConfig.idleProvider = idleProvider
    }

    override fun setPresenceProvider(
        statusProvider: IntFunction<OnlineStatus?>?,
        activityProvider: IntFunction<out Activity?>?
    ) {
        super.setPresenceProvider(statusProvider, activityProvider)
        presenceConfig.statusProvider = statusProvider
        presenceConfig.activityProvider = activityProvider
    }

    override fun setStatusProvider(statusProvider: IntFunction<OnlineStatus?>?) {
        super.setStatusProvider(statusProvider)
        presenceConfig.statusProvider = statusProvider
    }

    @Synchronized
    private fun retrieveShardTotal(httpClient: OkHttpClient) {
        if (shardsTotal != -1) return
        LOG.debug("Fetching shard total using temporary rate-limiter")
        val future = CompletableFuture<Int>()
        val pool = Executors.newSingleThreadScheduledExecutor { task: Runnable? ->
            val thread = Thread(task, "DefaultShardManager retrieveShardTotal")
            thread.setDaemon(true)
            thread
        }
        try {
            val rateLimitConfig = RateLimitConfig(pool, create()!!, true)
            val rateLimiter = SequentialRestRateLimiter(rateLimitConfig)
            rateLimiter.enqueue(ShardTotalTask(future, httpClient))
            val shardTotal = future.join()
            shardingConfig.shardsTotal = shardTotal
            shards = ShardCacheViewImpl(shardTotal)
            synchronized(queue) { for (i in 0 until shardTotal) queue.add(i) }
        } catch (ex: CompletionException) {
            if (ex.cause is RuntimeException) throw (ex.cause as RuntimeException?)!!
            if (ex.cause is Error) throw (ex.cause as Error?)!!
            throw ex
        } finally {
            future.cancel(false)
            pool.shutdownNow()
        }
    }

    /**
     * This method creates the internal [ScheduledExecutorService][java.util.concurrent.ScheduledExecutorService].
     * It is intended as a hook for custom implementations to create their own executor.
     *
     * @return A new ScheduledExecutorService
     */
    protected fun createExecutor(threadFactory: ThreadFactory?): ScheduledExecutorService {
        val factory = threadFactory ?: DEFAULT_THREAD_FACTORY
        return Executors.newSingleThreadScheduledExecutor(factory)
    }

    protected class ExecutorPair<E : ExecutorService?>(val executor: E, val automaticShutdown: Boolean)
    protected inner class ShardTotalTask(
        private val future: CompletableFuture<Int>,
        private val httpClient: OkHttpClient
    ) : RestRateLimiter.Work {
        private var failedAttempts = 0

        @get:Nonnull
        override val route: CompiledRoute
            get() = Route.Misc.GATEWAY_BOT.compile()

        @get:Nonnull
        override val jDA: JDA
            get() {
                throw UnsupportedOperationException()
            }

        override fun execute(): Response? {
            return try {
                val config = restConfigProvider.apply(0)
                val url = config.baseUrl + route.getCompiledRoute()
                LOG.debug("Requesting shard total with url {}", url)
                val builder: Builder = Builder()
                    .get()
                    .url(url)
                    .header("authorization", "Bot $token")
                    .header("accept-encoding", "gzip")
                    .header("user-agent", config.userAgent)
                val customBuilder: Consumer<in Builder?>? = config.getCustomBuilder()
                customBuilder?.accept(builder)
                val call = httpClient.newCall(builder.build())
                var response = call.execute()
                try {
                    LOG.debug("Received response with code {}", response.code())
                    val body = IOUtil.getBody(response)
                    if (response.isSuccessful) {
                        val json = DataObject.fromJson(body)
                        val shardTotal = json.getInt("shards")
                        future.complete(shardTotal)
                    } else if (response.code() == 401) {
                        future.completeExceptionally(InvalidTokenException())
                    } else if (response.code() != 429 && response.code() < 500 || ++failedAttempts > 4) {
                        future.completeExceptionally(
                            IllegalStateException(
                                """
                        Failed to fetch recommended shard total! Code: ${response.code()}
                        ${String(IOUtil.readFully(body), StandardCharsets.UTF_8)}
                        """.trimIndent()
                            )
                        )
                    } else if (response.code() >= 500) {
                        val backoff = 1 shl failedAttempts
                        LOG.warn(
                            "Failed to retrieve recommended shard total. Code: {} ... retrying in {}s",
                            response.code(),
                            backoff
                        )
                        response = response.newBuilder()
                            .headers(
                                response.headers()
                                    .newBuilder()
                                    .set(RestRateLimiter.RESET_AFTER_HEADER, backoff.toString())
                                    .set(RestRateLimiter.REMAINING_HEADER, 0.toString())
                                    .set(RestRateLimiter.LIMIT_HEADER, 1.toString())
                                    .set(RestRateLimiter.SCOPE_HEADER, "custom")
                                    .build()
                            )
                            .build()
                    }
                    response
                } finally {
                    response.close()
                }
            } catch (e: IOException) {
                future.completeExceptionally(e)
                throw UncheckedIOException(e)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
                throw e
            }
        }

        override val isSkipped: Boolean
            get() = isCancelled
        override val isDone: Boolean
            get() = future.isDone
        override val isPriority: Boolean
            get() = true
        override val isCancelled: Boolean
            get() = future.isCancelled()

        override fun cancel() {
            future.cancel(false)
        }
    }

    companion object {
        val LOG = JDALogger.getLog(ShardManager::class.java)
        val DEFAULT_THREAD_FACTORY = ThreadFactory { r: Runnable? ->
            val t = Thread(r, "DefaultShardManager")
            t.setPriority(Thread.NORM_PRIORITY + 1)
            t
        }

        protected fun <E : ExecutorService?> resolveExecutor(
            provider: ThreadPoolProvider<out E>?,
            shardId: Int
        ): ExecutorPair<E?> {
            var executor: E? = null
            var automaticShutdown = true
            if (provider != null) {
                executor = provider.provide(shardId)
                automaticShutdown = provider.shouldShutdownAutomatically(shardId)
            }
            return ExecutorPair(executor, automaticShutdown)
        }
    }
}
