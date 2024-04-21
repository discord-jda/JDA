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
package net.dv8tion.jda.api

import com.neovisionaries.ws.client.WebSocketFactory
import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.JDA.ShardInfo
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.GatewayIntent.Companion.getIntents
import net.dv8tion.jda.api.requests.GatewayIntent.Companion.getRaw
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.RestConfig
import net.dv8tion.jda.api.utils.*
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.api.utils.cache.CacheFlag.Companion.privileged
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.managers.PresenceImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.IOUtil
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig
import net.dv8tion.jda.internal.utils.config.MetaConfig
import net.dv8tion.jda.internal.utils.config.SessionConfig
import net.dv8tion.jda.internal.utils.config.ThreadingConfig
import net.dv8tion.jda.internal.utils.config.flags.ConfigFlag
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder.build
import java.util.*
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull
import kotlin.math.max
import kotlin.math.min

/**
 * Used to create new [net.dv8tion.jda.api.JDA] instances. This is also useful for making sure all of
 * your [EventListeners][net.dv8tion.jda.api.hooks.EventListener] are registered
 * before [net.dv8tion.jda.api.JDA] attempts to log in.
 *
 *
 * A single JDABuilder can be reused multiple times. Each call to
 * [build()][net.dv8tion.jda.api.JDABuilder.build]
 * creates a new [net.dv8tion.jda.api.JDA] instance using the same information.
 * This means that you can have listeners easily registered to multiple [net.dv8tion.jda.api.JDA] instances.
 */
class JDABuilder private constructor(token: String?, intents: Int) {
    protected val listeners: MutableList<Any> = LinkedList()
    protected val automaticallyDisabled: EnumSet<CacheFlag?> = EnumSet.noneOf(CacheFlag::class.java)
    protected var rateLimitScheduler: ScheduledExecutorService? = null
    protected var shutdownRateLimitScheduler: Boolean = true
    protected var rateLimitElastic: ExecutorService? = null
    protected var shutdownRateLimitElastic: Boolean = true
    protected var mainWsPool: ScheduledExecutorService? = null
    protected var shutdownMainWsPool: Boolean = true
    protected var callbackPool: ExecutorService? = null
    protected var shutdownCallbackPool: Boolean = true
    protected var eventPool: ExecutorService? = null
    protected var shutdownEventPool: Boolean = true
    protected var audioPool: ScheduledExecutorService? = null
    protected var shutdownAudioPool: Boolean = true
    protected var cacheFlags: EnumSet<CacheFlag?> = EnumSet.allOf(CacheFlag::class.java)
    protected var contextMap: ConcurrentMap<String, String>? = null
    protected var controller: SessionController? = null
    protected var voiceDispatchInterceptor: VoiceDispatchInterceptor? = null
    protected var httpClientBuilder: Builder? = null
    protected var httpClient: OkHttpClient? = null
    protected var wsFactory: WebSocketFactory? = null
    protected var token: String? = null
    protected var eventManager: IEventManager? = null
    protected var audioSendFactory: IAudioSendFactory? = null
    protected var shardInfo: ShardInfo? = null
    protected var compression: Compression = Compression.ZLIB
    protected var activity: Activity? = null
    protected var status: OnlineStatus = OnlineStatus.ONLINE
    protected var idle: Boolean = false
    protected var maxReconnectDelay: Int = 900
    protected var largeThreshold: Int = 250
    protected var maxBufferSize: Int = 2048
    protected var intents: Int = -1 // don't use intents by default
    protected var flags: EnumSet<ConfigFlag> = ConfigFlag.getDefault()
    protected var chunkingFilter: ChunkingFilter = ChunkingFilter.ALL
    protected var memberCachePolicy: MemberCachePolicy = MemberCachePolicy.ALL
    protected var encoding: GatewayEncoding = GatewayEncoding.JSON
    protected var restConfig: RestConfig = RestConfig()

    init {
        this.token = token
        this.intents = 1 or intents
    }

    private fun applyDefault(): JDABuilder {
        return setMemberCachePolicy(MemberCachePolicy.DEFAULT)
            .setChunkingFilter(ChunkingFilter.NONE)
            .disableCache(privileged)
            .setLargeThreshold(250)
    }

    private fun applyLight(): JDABuilder {
        return setMemberCachePolicy(MemberCachePolicy.NONE)
            .setChunkingFilter(ChunkingFilter.NONE)
            .disableCache(EnumSet.allOf(CacheFlag::class.java))
            .setLargeThreshold(50)
    }

    private fun applyIntents(): JDABuilder {
        val disabledCache: EnumSet<CacheFlag?> = EnumSet.allOf(CacheFlag::class.java)
        for (flag: CacheFlag in CacheFlag.entries) {
            val requiredIntent: GatewayIntent? = flag.requiredIntent
            if (requiredIntent == null || (requiredIntent.rawValue and intents) != 0) disabledCache.remove(flag)
        }
        val enableMembers: Boolean = (intents and GatewayIntent.GUILD_MEMBERS.rawValue) != 0
        return setChunkingFilter(if (enableMembers) ChunkingFilter.ALL else ChunkingFilter.NONE)
            .setMemberCachePolicy(if (enableMembers) MemberCachePolicy.ALL else MemberCachePolicy.DEFAULT)
            .setDisabledCache(disabledCache)
    }

    private fun setDisabledCache(flags: EnumSet<CacheFlag?>): JDABuilder {
        disableCache(flags)
        automaticallyDisabled.addAll(flags)
        return this
    }

    /**
     * Choose which [GatewayEncoding] JDA should use.
     *
     * @param  encoding
     * The [GatewayEncoding] (default: JSON)
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since  4.2.1
     */
    @Nonnull
    fun setGatewayEncoding(@Nonnull encoding: GatewayEncoding): JDABuilder {
        Checks.notNull(encoding, "GatewayEncoding")
        this.encoding = encoding
        return this
    }

    /**
     * Whether JDA should fire [net.dv8tion.jda.api.events.RawGatewayEvent] for every discord event.
     * <br></br>Default: `false`
     *
     * @param  enable
     * True, if JDA should fire [net.dv8tion.jda.api.events.RawGatewayEvent].
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     */
    @Nonnull
    fun setRawEventsEnabled(enable: Boolean): JDABuilder {
        return setFlag(ConfigFlag.RAW_EVENTS, enable)
    }

    /**
     * Whether JDA should store the raw [DataObject][net.dv8tion.jda.api.utils.data.DataObject] for every discord event, accessible through [getRawData()][net.dv8tion.jda.api.events.GenericEvent.getRawData].
     * <br></br>You can expect to receive the full gateway message payload, including sequence, event name and dispatch type of the events
     * <br></br>You can read more about payloads [here](https://discord.com/developers/docs/topics/gateway) and the different events [here](https://discord.com/developers/docs/topics/gateway#commands-and-events-gateway-events).
     * <br></br>Warning: be aware that enabling this could consume a lot of memory if your event objects have a long lifetime.
     * <br></br>Default: `false`
     *
     * @param  enable
     * True, if JDA should add the raw [DataObject][net.dv8tion.jda.api.utils.data.DataObject] to every discord event.
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see Event.getRawData
     */
    @Nonnull
    fun setEventPassthrough(enable: Boolean): JDABuilder {
        return setFlag(ConfigFlag.EVENT_PASSTHROUGH, enable)
    }

    /**
     * Whether the rate-limit should be relative to the current time plus latency.
     * <br></br>By default we use the `X-RateLimit-Reset-After` header to determine when
     * a rate-limit is no longer imminent. This has the disadvantage that it might wait longer than needed due
     * to the latency which is ignored by the reset-after relative delay.
     *
     *
     * When disabled, we will use the `X-RateLimit-Reset` absolute timestamp instead which accounts for
     * latency but requires a properly NTP synchronized clock to be present.
     * If your system does have this feature you might gain a little quicker rate-limit handling than the default allows.
     *
     *
     * Default: **true**
     *
     * @param  enable
     * True, if the relative `X-RateLimit-Reset-After` header should be used.
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    @Deprecated("")
    @ForRemoval(deadline = "5.1.0")
    @ReplaceWith("setRestConfig(new RestConfig().setRelativeRateLimit(enable))")
    fun setRelativeRateLimit(enable: Boolean): JDABuilder {
        return setFlag(ConfigFlag.USE_RELATIVE_RATELIMIT, enable)
    }

    /**
     * Custom [RestConfig] to use for this JDA instance.
     * <br></br>This can be used to customize how rate-limits are handled and configure a custom http proxy.
     *
     * @param  config
     * The [RestConfig] to use
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRestConfig(@Nonnull config: RestConfig): JDABuilder {
        Checks.notNull(config, "RestConfig")
        restConfig = config
        return this
    }

    /**
     * Enable specific cache flags.
     * <br></br>This will not disable any currently set cache flags.
     *
     * @param  flags
     * The [CacheFlags][CacheFlag] to enable
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .enableCache
     * @see .disableCache
     */
    @Nonnull
    fun enableCache(@Nonnull flags: Collection<CacheFlag?>?): JDABuilder {
        Checks.noneNull(flags, "CacheFlags")
        cacheFlags.addAll((flags)!!)
        return this
    }

    /**
     * Enable specific cache flags.
     * <br></br>This will not disable any currently set cache flags.
     *
     * @param  flag
     * [CacheFlag] to enable
     * @param  flags
     * Other flags to enable
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .enableCache
     * @see .disableCache
     */
    @Nonnull
    fun enableCache(@Nonnull flag: CacheFlag?, @Nonnull vararg flags: CacheFlag?): JDABuilder {
        Checks.notNull(flag, "CacheFlag")
        Checks.noneNull(flags, "CacheFlag")
        cacheFlags.addAll(EnumSet.of(flag, *flags))
        return this
    }

    /**
     * Disable specific cache flags.
     * <br></br>This will not enable any currently unset cache flags.
     *
     * @param  flags
     * The [CacheFlags][CacheFlag] to disable
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .disableCache
     * @see .enableCache
     */
    @Nonnull
    fun disableCache(@Nonnull flags: Collection<CacheFlag?>?): JDABuilder {
        Checks.noneNull(flags, "CacheFlags")
        automaticallyDisabled.removeAll((flags)!!)
        cacheFlags.removeAll((flags)!!)
        return this
    }

    /**
     * Disable specific cache flags.
     * <br></br>This will not enable any currently unset cache flags.
     *
     * @param  flag
     * [CacheFlag] to disable
     * @param  flags
     * Other flags to disable
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .disableCache
     * @see .enableCache
     */
    @Nonnull
    fun disableCache(@Nonnull flag: CacheFlag?, @Nonnull vararg flags: CacheFlag?): JDABuilder {
        Checks.notNull(flag, "CacheFlag")
        Checks.noneNull(flags, "CacheFlag")
        return disableCache(EnumSet.of(flag, *flags))
    }

    /**
     * Configure the member caching policy.
     * This will decide whether to cache a member (and its respective user).
     * <br></br>All members are cached by default. If a guild is enabled for chunking, all members will be cached for it.
     *
     *
     * You can use this to define a custom caching policy that will greatly improve memory usage.
     *
     * It is not recommended to disable [GatewayIntent.GUILD_MEMBERS] when
     * using [MemberCachePolicy.ALL] as the members cannot be removed from cache by a leave event without this intent.
     *
     *
     * **Example**<br></br>
     * <pre>`public void configureCache(JDABuilder builder) {
     * // Cache members who are in a voice channel
     * MemberCachePolicy policy = MemberCachePolicy.VOICE;
     * // Cache members who are in a voice channel
     * // AND are also online
     * policy = policy.and(MemberCachePolicy.ONLINE);
     * // Cache members who are in a voice channel
     * // AND are also online
     * // OR are the owner of the guild
     * policy = policy.or(MemberCachePolicy.OWNER);
     *
     * builder.setMemberCachePolicy(policy);
     * }
    `</pre> *
     *
     * @param  policy
     * The [MemberCachePolicy] or null to use default [MemberCachePolicy.ALL]
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see MemberCachePolicy
     *
     * @see .setEnabledIntents
     * @since  4.2.0
     */
    @Nonnull
    fun setMemberCachePolicy(policy: MemberCachePolicy?): JDABuilder {
        if (policy == null) memberCachePolicy = MemberCachePolicy.ALL else memberCachePolicy = policy
        return this
    }

    /**
     * Sets the [MDC][org.slf4j.MDC] mappings to use in JDA.
     * <br></br>If sharding is enabled JDA will automatically add a `jda.shard` context with the format `[SHARD_ID / TOTAL]`
     * where `SHARD_ID` and `TOTAL` are the shard configuration.
     * Additionally it will provide context for the id via `jda.shard.id` and the total via `jda.shard.total`.
     *
     *
     * If provided with non-null map this automatically enables MDC context using [setContextEnable(true)][.setContextEnabled]!
     *
     * @param  map
     * The **modifiable** context map to use in JDA, or `null` to reset
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see [MDC Javadoc](https://www.slf4j.org/api/org/slf4j/MDC.html)
     *
     * @see .setContextEnabled
     */
    @Nonnull
    fun setContextMap(map: ConcurrentMap<String, String>?): JDABuilder {
        contextMap = map
        if (map != null) setContextEnabled(true)
        return this
    }

    /**
     * Whether JDA should use a synchronized MDC context for all of its controlled threads.
     * <br></br>Default: `true`
     *
     * @param  enable
     * True, if JDA should provide an MDC context map
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see [MDC Javadoc](https://www.slf4j.org/api/org/slf4j/MDC.html)
     *
     * @see .setContextMap
     */
    @Nonnull
    fun setContextEnabled(enable: Boolean): JDABuilder {
        return setFlag(ConfigFlag.MDC_CONTEXT, enable)
    }

    /**
     * Sets the compression algorithm used with the gateway connection,
     * this will decrease the amount of used bandwidth for the running bot instance
     * for the cost of a few extra cycles for decompression.
     * Compression can be entirely disabled by setting this to [net.dv8tion.jda.api.utils.Compression.NONE].
     * <br></br>**Default: [net.dv8tion.jda.api.utils.Compression.ZLIB]**
     *
     *
     * **We recommend to keep this on the default unless you have issues with the decompression.**
     * <br></br>This mode might become obligatory in a future version, do not rely on this switch to stay.
     *
     * @param  compression
     * The compression algorithm to use with the gateway connection
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining
     *
     * @see [Official Discord Documentation - Transport Compression](https://discord.com/developers/docs/topics/gateway.transport-compression)
     */
    @Nonnull
    fun setCompression(@Nonnull compression: Compression): JDABuilder {
        Checks.notNull(compression, "Compression")
        this.compression = compression
        return this
    }

    /**
     * Whether the Requester should retry when
     * a [SocketTimeoutException][java.net.SocketTimeoutException] occurs.
     * <br></br>**Default**: `true`
     *
     *
     * This value can be changed at any time with [JDA.setRequestTimeoutRetry(boolean)][net.dv8tion.jda.api.JDA.setRequestTimeoutRetry]!
     *
     * @param  retryOnTimeout
     * True, if the Request should retry once on a socket timeout
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRequestTimeoutRetry(retryOnTimeout: Boolean): JDABuilder {
        return setFlag(ConfigFlag.RETRY_TIMEOUT, retryOnTimeout)
    }

    /**
     * Sets the token that will be used by the [net.dv8tion.jda.api.JDA] instance to log in when
     * [build()][net.dv8tion.jda.api.JDABuilder.build] is called.
     *
     *
     * To get a bot token:<br></br>
     *
     *  1. Go to your [Discord Applications](https://discord.com/developers/applications/me)
     *  1. Create or select an already existing application
     *  1. Verify that it has already been turned into a Bot. If you see the "Create a Bot User" button, click it.
     *  1. Click the *click to reveal* link beside the **Token** label to show your Bot's `token`
     *
     *
     * @param  token
     * The token of the account that you would like to login with.
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setToken(token: String?): JDABuilder {
        this.token = token
        return this
    }

    /**
     * Sets the [Builder][okhttp3.OkHttpClient.Builder] that will be used by JDAs requester.
     * <br></br>This can be used to set things such as connection timeout and proxy.
     *
     * @param  builder
     * The new [Builder][okhttp3.OkHttpClient.Builder] to use
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setHttpClientBuilder(builder: Builder?): JDABuilder {
        httpClientBuilder = builder
        return this
    }

    /**
     * Sets the [OkHttpClient][okhttp3.OkHttpClient] that will be used by JDAs requester.
     * <br></br>This can be used to set things such as connection timeout and proxy.
     *
     * @param  client
     * The new [OkHttpClient][okhttp3.OkHttpClient] to use
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setHttpClient(client: OkHttpClient?): JDABuilder {
        httpClient = client
        return this
    }

    /**
     * Sets the [WebSocketFactory][com.neovisionaries.ws.client.WebSocketFactory] that will be used by JDA's websocket client.
     * This can be used to set things such as connection timeout and proxy.
     *
     * @param  factory
     * The new [WebSocketFactory][com.neovisionaries.ws.client.WebSocketFactory] to use.
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setWebsocketFactory(factory: WebSocketFactory?): JDABuilder {
        wsFactory = factory
        return this
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>**This automatically disables the automatic shutdown of the rate-limit pool, you can enable
     * it using [setRateLimitPool(executor, true)][.setRateLimitPool]**
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods.
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 5 threads.
     *
     * @param  pool
     * The thread-pool to use for rate-limit handling
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     */
    @Nonnull
    @ReplaceWith("setRateLimitScheduler(pool)")
    @Deprecated("This pool is now split into two pools.\n" + "                  You should use {@link #setRateLimitScheduler(ScheduledExecutorService)} and {@link #setRateLimitElastic(ExecutorService)} instead.")
    fun setRateLimitPool(pool: ScheduledExecutorService?): JDABuilder {
        return setRateLimitPool(pool, pool == null)
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods.
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 5 threads.
     *
     * @param  pool
     * The thread-pool to use for rate-limit handling
     * @param  automaticShutdown
     * Whether [JDA.shutdown] should shutdown this pool
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     */
    @Nonnull
    @ReplaceWith("setRateLimitScheduler(pool, automaticShutdown)")
    @Deprecated("This pool is now split into two pools.\n" + "                  You should use {@link #setRateLimitScheduler(ScheduledExecutorService, boolean)} and {@link #setRateLimitElastic(ExecutorService, boolean)} instead.")
    fun setRateLimitPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): JDABuilder {
        rateLimitScheduler = pool
        shutdownRateLimitScheduler = automaticShutdown
        return this
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>**This automatically disables the automatic shutdown of the rate-limit pool, you can enable
     * it using [setRateLimitPool(executor, true)][.setRateLimitPool]**
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods. Requests are handed off to the [elastic pool][.setRateLimitElastic] for blocking execution.
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 2 threads.
     *
     * @param  pool
     * The thread-pool to use for rate-limit handling
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitScheduler(pool: ScheduledExecutorService?): JDABuilder {
        return setRateLimitScheduler(pool, pool == null)
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods. Requests are handed off to the [elastic pool][.setRateLimitElastic] for blocking execution.
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 2 threads.
     *
     * @param  pool
     * The thread-pool to use for rate-limit handling
     * @param  automaticShutdown
     * Whether [JDA.shutdown] should shutdown this pool
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitScheduler(pool: ScheduledExecutorService?, automaticShutdown: Boolean): JDABuilder {
        rateLimitScheduler = pool
        shutdownRateLimitScheduler = automaticShutdown
        return this
    }

    /**
     * Sets the [ExecutorService] that should be used in
     * the JDA request handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>**This automatically disables the automatic shutdown of the rate-limit elastic pool, you can enable
     * it using [setRateLimitElastic(executor, true)][.setRateLimitElastic]**
     *
     *
     * This is used mostly by the Rate-Limiter to execute the blocking HTTP requests at runtime.
     *
     *
     * Default: [Executors.newCachedThreadPool].
     *
     * @param  pool
     * The thread-pool to use for executing http requests
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitElastic(pool: ExecutorService?): JDABuilder {
        return setRateLimitElastic(pool, pool == null)
    }

    /**
     * Sets the [ExecutorService] that should be used in
     * the JDA request handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     *
     *
     * This is used mostly by the Rate-Limiter to execute the blocking HTTP requests at runtime.
     *
     *
     * Default: [Executors.newCachedThreadPool].
     *
     * @param  pool
     * The thread-pool to use for executing http requests
     * @param  automaticShutdown
     * Whether [JDA.shutdown] should shutdown this pool
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitElastic(pool: ExecutorService?, automaticShutdown: Boolean): JDABuilder {
        rateLimitElastic = pool
        shutdownRateLimitElastic = automaticShutdown
        return this
    }

    /**
     * Sets the [ScheduledExecutorService] used by
     * the main WebSocket connection for workers. These workers spend most of their lifetime
     * sleeping because they only activate for sending messages over the gateway.
     * <br></br>**Only change this pool if you know what you're doing.
     * <br></br>This automatically disables the automatic shutdown of the main-ws pool, you can enable
     * it using [setGatewayPool(pool, true)][.setGatewayPool]**
     *
     *
     * This is used to send various forms of session updates such as:
     *
     *  * Voice States - (Dis-)Connecting from channels
     *  * Presence - Changing current activity or online status
     *  * Guild Setup - Requesting Members of newly joined guilds
     *  * Heartbeats - Regular updates to keep the connection alive (usually once a minute)
     *
     * When nothing has to be sent the pool will only be used every 500 milliseconds to check the queue for new payloads.
     * Once a new payload is sent we switch to "rapid mode" which means more tasks will be submitted until no more payloads
     * have to be sent.
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 1 thread
     *
     * @param  pool
     * The thread-pool to use for WebSocket workers
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setGatewayPool(pool: ScheduledExecutorService?): JDABuilder {
        return setGatewayPool(pool, pool == null)
    }

    /**
     * Sets the [ScheduledExecutorService] used by
     * the main WebSocket connection for workers. These workers spend most of their lifetime
     * sleeping because they only activate for sending messages over the gateway.
     * <br></br>**Only change this pool if you know what you're doing.**
     *
     *
     * This is used to send various forms of session updates such as:
     *
     *  * Voice States - (Dis-)Connecting from channels
     *  * Presence - Changing current activity or online status
     *  * Guild Setup - Requesting Members of newly joined guilds
     *  * Heartbeats - Regular updates to keep the connection alive (usually once a minute)
     *
     * When nothing has to be sent the pool will only be used every 500 milliseconds to check the queue for new payloads.
     * Once a new payload is sent we switch to "rapid mode" which means more tasks will be submitted until no more payloads
     * have to be sent.
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 1 thread
     *
     * @param  pool
     * The thread-pool to use for WebSocket workers
     * @param  automaticShutdown
     * Whether [JDA.shutdown] should shutdown this pool
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setGatewayPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): JDABuilder {
        mainWsPool = pool
        shutdownMainWsPool = automaticShutdown
        return this
    }

    /**
     * Sets the [ExecutorService] that should be used in
     * the JDA callback handler which mostly consists of [RestAction][net.dv8tion.jda.api.requests.RestAction] callbacks.
     * By default JDA will use [ForkJoinPool.commonPool]
     * <br></br>**Only change this pool if you know what you're doing.
     * <br></br>This automatically disables the automatic shutdown of the callback pool, you can enable
     * it using [setCallbackPool(executor, true)][.setCallbackPool]**
     *
     *
     * This is used to handle callbacks of [RestAction.queue], similarly it is used to
     * finish [RestAction.submit] and [RestAction.complete] tasks which build on queue.
     *
     *
     * Default: [ForkJoinPool.commonPool]
     *
     * @param  executor
     * The thread-pool to use for callback handling
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setCallbackPool(executor: ExecutorService?): JDABuilder {
        return setCallbackPool(executor, executor == null)
    }

    /**
     * Sets the [ExecutorService] that should be used in
     * the JDA callback handler which mostly consists of [RestAction][net.dv8tion.jda.api.requests.RestAction] callbacks.
     * By default JDA will use [ForkJoinPool.commonPool]
     * <br></br>**Only change this pool if you know what you're doing.**
     *
     *
     * This is used to handle callbacks of [RestAction.queue], similarly it is used to
     * finish [RestAction.submit] and [RestAction.complete] tasks which build on queue.
     *
     *
     * Default: [ForkJoinPool.commonPool]
     *
     * @param  executor
     * The thread-pool to use for callback handling
     * @param  automaticShutdown
     * Whether [JDA.shutdown] should shutdown this executor
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setCallbackPool(executor: ExecutorService?, automaticShutdown: Boolean): JDABuilder {
        callbackPool = executor
        shutdownCallbackPool = automaticShutdown
        return this
    }

    /**
     * Sets the [ExecutorService] that should be used by the
     * event proxy to schedule events. This will be done on the calling thread by default.
     *
     *
     * The executor will not be shutdown automatically when JDA is shutdown.
     * To shut it down automatically use [.setEventPool].
     *
     * @param  executor
     * The executor for the event proxy, or null to use calling thread
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since  4.2.0
     */
    @Nonnull
    fun setEventPool(executor: ExecutorService?): JDABuilder {
        return setEventPool(executor, executor == null)
    }

    /**
     * Sets the [ExecutorService] that should be used by the
     * event proxy to schedule events. This will be done on the calling thread by default.
     *
     * @param  executor
     * The executor for the event proxy, or null to use calling thread
     * @param  automaticShutdown
     * True, if the executor should be shutdown when JDA shuts down
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since  4.2.0
     */
    @Nonnull
    fun setEventPool(executor: ExecutorService?, automaticShutdown: Boolean): JDABuilder {
        eventPool = executor
        shutdownEventPool = automaticShutdown
        return this
    }

    /**
     * Sets the [ScheduledExecutorService] used by
     * the audio WebSocket connection. Used for sending keepalives and closing the connection.
     * <br></br>**Only change this pool if you know what you're doing.**
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 1 thread
     *
     * @param  pool
     * The thread-pool to use for the audio WebSocket
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since  4.2.1
     */
    @Nonnull
    fun setAudioPool(pool: ScheduledExecutorService?): JDABuilder {
        return setAudioPool(pool, pool == null)
    }

    /**
     * Sets the [ScheduledExecutorService] used by
     * the audio WebSocket connection. Used for sending keepalives and closing the connection.
     * <br></br>**Only change this pool if you know what you're doing.**
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 1 thread
     *
     * @param  pool
     * The thread-pool to use for the audio WebSocket
     * @param  automaticShutdown
     * Whether [JDA.shutdown] should shutdown this pool
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since  4.2.1
     */
    @Nonnull
    fun setAudioPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): JDABuilder {
        audioPool = pool
        shutdownAudioPool = automaticShutdown
        return this
    }

    /**
     * If enabled, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the [MessageBulkDeleteEvent][net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent]
     *
     *
     * Default: **true (enabled)**
     *
     * @param  enabled
     * True - The MESSAGE_DELETE_BULK will be split into multiple individual MessageDeleteEvents.
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setBulkDeleteSplittingEnabled(enabled: Boolean): JDABuilder {
        return setFlag(ConfigFlag.BULK_DELETE_SPLIT, enabled)
    }

    /**
     * Enables/Disables the use of a Shutdown hook to clean up JDA.
     * <br></br>When the Java program closes shutdown hooks are run. This is used as a last-second cleanup
     * attempt by JDA to properly close connections.
     *
     *
     * Default: **true (enabled)**
     *
     * @param  enable
     * True (default) - use shutdown hook to clean up JDA if the Java program is closed.
     *
     * @return Return the [JDABuilder ][net.dv8tion.jda.api.JDABuilder] instance. Useful for chaining.
     */
    @Nonnull
    fun setEnableShutdownHook(enable: Boolean): JDABuilder {
        return setFlag(ConfigFlag.SHUTDOWN_HOOK, enable)
    }

    /**
     * Sets whether or not JDA should try to reconnect if a connection-error is encountered.
     * <br></br>This will use an incremental reconnect (timeouts are increased each time an attempt fails).
     *
     *
     * Default: **true (enabled)**
     *
     * @param  autoReconnect
     * If true - enables autoReconnect
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setAutoReconnect(autoReconnect: Boolean): JDABuilder {
        return setFlag(ConfigFlag.AUTO_RECONNECT, autoReconnect)
    }

    /**
     * Changes the internally used EventManager.
     * <br></br>There are 2 provided Implementations:
     *
     *  * [InterfacedEventManager][net.dv8tion.jda.api.hooks.InterfacedEventManager] which uses the Interface
     * [EventListener][net.dv8tion.jda.api.hooks.EventListener] (tip: use the [ListenerAdapter][net.dv8tion.jda.api.hooks.ListenerAdapter]).
     * <br></br>This is the default EventManager.
     *
     *  * [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager] which uses the Annotation
     * [@SubscribeEvent][net.dv8tion.jda.api.hooks.SubscribeEvent] to mark the methods that listen for events.
     *
     * <br></br>You can also create your own EventManager (See [net.dv8tion.jda.api.hooks.IEventManager]).
     *
     * @param  manager
     * The new [net.dv8tion.jda.api.hooks.IEventManager] to use.
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setEventManager(manager: IEventManager?): JDABuilder {
        eventManager = manager
        return this
    }

    /**
     * Changes the factory used to create [IAudioSendSystem][net.dv8tion.jda.api.audio.factory.IAudioSendSystem]
     * objects which handle the sending loop for audio packets.
     * <br></br>By default, JDA uses [DefaultSendFactory][net.dv8tion.jda.api.audio.factory.DefaultSendFactory].
     *
     * @param  factory
     * The new [IAudioSendFactory][net.dv8tion.jda.api.audio.factory.IAudioSendFactory] to be used
     * when creating new [net.dv8tion.jda.api.audio.factory.IAudioSendSystem] objects.
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setAudioSendFactory(factory: IAudioSendFactory?): JDABuilder {
        audioSendFactory = factory
        return this
    }

    /**
     * Sets whether or not we should mark our session as afk
     * <br></br>This value can be changed at any time in the [Presence][net.dv8tion.jda.api.managers.Presence] from a JDA instance.
     *
     * @param  idle
     * boolean value that will be provided with our IDENTIFY package to mark our session as afk or not. **(default false)**
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setIdle
     */
    @Nonnull
    fun setIdle(idle: Boolean): JDABuilder {
        this.idle = idle
        return this
    }

    /**
     * Sets the [Activity][net.dv8tion.jda.api.entities.Activity] for our session.
     * <br></br>This value can be changed at any time in the [Presence][net.dv8tion.jda.api.managers.Presence] from a JDA instance.
     *
     *
     * **Hint:** You can create an [Activity][net.dv8tion.jda.api.entities.Activity] object using
     * [net.dv8tion.jda.api.entities.Activity.playing] or [net.dv8tion.jda.api.entities.Activity.streaming].
     *
     * @param  activity
     * An instance of [Activity][net.dv8tion.jda.api.entities.Activity] (null allowed)
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setActivity
     */
    @Nonnull
    fun setActivity(activity: Activity?): JDABuilder {
        this.activity = activity
        return this
    }

    /**
     * Sets the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] our connection will display.
     * <br></br>This value can be changed at any time in the [Presence][net.dv8tion.jda.api.managers.Presence] from a JDA instance.
     *
     * @param  status
     * Not-null OnlineStatus (default online)
     *
     * @throws IllegalArgumentException
     * if the provided OnlineStatus is null or [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setStatus
     */
    @Nonnull  // we have to enforce the nonnull at runtime
    fun setStatus(@Nonnull status: OnlineStatus?): JDABuilder {
        if (status == null || status == OnlineStatus.UNKNOWN) throw IllegalArgumentException("OnlineStatus cannot be null or unknown!")
        this.status = status
        return this
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the [JDA][net.dv8tion.jda.api.JDA] object.
     * <br></br>This uses the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] by default.
     * <br></br>To switch to the [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager],
     * use [setEventManager(new AnnotatedEventManager())][.setEventManager].
     *
     *
     * **Note:** When using the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] (default),
     * given listener(s) **must** be instance of [EventListener][net.dv8tion.jda.api.hooks.EventListener]!
     *
     * @param   listeners
     * The listener(s) to add to the list.
     *
     * @throws java.lang.IllegalArgumentException
     * If either listeners or one of it's objects is `null`.
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.JDA.addEventListener
     */
    @Nonnull
    fun addEventListeners(@Nonnull vararg listeners: Any?): JDABuilder {
        Checks.noneNull(listeners, "listeners")
        Collections.addAll(this.listeners, *listeners)
        return this
    }

    /**
     * Removes all provided listeners from the list of listeners.
     *
     * @param  listeners
     * The listener(s) to remove from the list.
     *
     * @throws java.lang.IllegalArgumentException
     * If either listeners or one of it's objects is `null`.
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.JDA.removeEventListener
     */
    @Nonnull
    fun removeEventListeners(@Nonnull vararg listeners: Any?): JDABuilder {
        Checks.noneNull(listeners, "listeners")
        this.listeners.removeAll(Arrays.asList(*listeners))
        return this
    }

    /**
     * Sets the maximum amount of time that JDA will back off to wait when attempting to reconnect the MainWebsocket.
     * <br></br>Provided value must be 32 or greater.
     *
     *
     * Default: `900`
     *
     * @param  maxReconnectDelay
     * The maximum amount of time that JDA will wait between reconnect attempts in seconds.
     *
     * @throws java.lang.IllegalArgumentException
     * Thrown if the provided `maxReconnectDelay` is less than 32.
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setMaxReconnectDelay(maxReconnectDelay: Int): JDABuilder {
        Checks.check(
            maxReconnectDelay >= 32,
            "Max reconnect delay must be 32 seconds or greater. You provided %d.",
            maxReconnectDelay
        )
        this.maxReconnectDelay = maxReconnectDelay
        return this
    }

    /**
     * This will enable sharding mode for JDA.
     * <br></br>In sharding mode, guilds are split up and assigned one of multiple shards (clients).
     * <br></br>The shardId that receives all stuff related to given bot is calculated as follows: shardId == (guildId &gt;&gt; 22) % shardTotal;
     * <br></br>**PMs are only sent to shard 0.**
     *
     *
     * Please note, that a shard will not know about guilds which are not assigned to it.
     *
     * @param  shardId
     * The id of this shard (starting at 0).
     * @param  shardTotal
     * The number of overall shards.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided shard configuration is invalid
     * (`0 <= shardId < shardTotal` with `shardTotal > 0`)
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.JDA.getShardInfo
     * @see net.dv8tion.jda.api.sharding.ShardManager ShardManager
     */
    @Nonnull
    fun useSharding(shardId: Int, shardTotal: Int): JDABuilder {
        Checks.notNegative(shardId, "Shard ID")
        Checks.positive(shardTotal, "Shard Total")
        Checks.check(
            shardId < shardTotal,
            "The shard ID must be lower than the shardTotal! Shard IDs are 0-based."
        )
        shardInfo = ShardInfo(shardId, shardTotal)
        return this
    }

    /**
     * Sets the [SessionController][net.dv8tion.jda.api.utils.SessionController]
     * for this JDABuilder instance. This can be used to sync behaviour and state between shards
     * of a bot and should be one and the same instance on all builders for the shards.
     * <br></br>When [.useSharding] is enabled, this is set by default.
     *
     *
     * When set, this allows the builder to build shards with respect to the login ratelimit automatically.
     *
     * @param  controller
     * The [SessionController][net.dv8tion.jda.api.utils.SessionController] to use
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.utils.SessionControllerAdapter SessionControllerAdapter
     */
    @Nonnull
    fun setSessionController(controller: SessionController?): JDABuilder {
        this.controller = controller
        return this
    }

    /**
     * Configures a custom voice dispatch handler which handles audio connections.
     *
     * @param  interceptor
     * The new voice dispatch handler, or null to use the default
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since 4.0.0
     *
     * @see VoiceDispatchInterceptor
     */
    @Nonnull
    fun setVoiceDispatchInterceptor(interceptor: VoiceDispatchInterceptor?): JDABuilder {
        voiceDispatchInterceptor = interceptor
        return this
    }

    /**
     * The [ChunkingFilter] to filter which guilds should use member chunking.
     *
     *
     * Use [.setMemberCachePolicy] to configure which members to keep in cache from chunking.
     *
     * @param  filter
     * The filter to apply
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since  4.1.0
     *
     * @see ChunkingFilter.NONE
     *
     * @see ChunkingFilter.include
     * @see ChunkingFilter.exclude
     */
    @Nonnull
    fun setChunkingFilter(filter: ChunkingFilter?): JDABuilder {
        chunkingFilter = if (filter == null) ChunkingFilter.ALL else filter
        return this
    }

    /**
     * Configures which events will be disabled.
     * Bots which did not enable presence/member updates in the developer dashboard are required to disable [GatewayIntent.GUILD_PRESENCES] and [GatewayIntent.GUILD_MEMBERS]!
     *
     *
     * It is not recommended to disable [GatewayIntent.GUILD_MEMBERS] when
     * using [MemberCachePolicy.ALL] as the members cannot be removed from cache by a leave event without this intent.
     *
     *
     * If you disable certain intents you also have to disable related [CacheFlags][CacheFlag].
     * This can be achieved using [.disableCache]. The required intents for each
     * flag are documented in the [CacheFlag] enum.
     *
     * @param  intent
     * The first intent to disable
     * @param  intents
     * Any other intents to disable
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .setMemberCachePolicy
     * @since  4.2.0
     */
    @Nonnull
    fun setDisabledIntents(@Nonnull intent: GatewayIntent, @Nonnull vararg intents: GatewayIntent?): JDABuilder {
        Checks.notNull(intent, "Intents")
        Checks.noneNull(intents, "Intents")
        return setDisabledIntents(EnumSet.of(intent, *intents))
    }

    /**
     * Configures which events will be disabled.
     * Bots which did not enable presence/member updates in the developer dashboard are required to disable [GatewayIntent.GUILD_PRESENCES] and [GatewayIntent.GUILD_MEMBERS]!
     *
     *
     * It is not recommended to disable [GatewayIntent.GUILD_MEMBERS] when
     * using [MemberCachePolicy.ALL] as the members cannot be removed from cache by a leave event without this intent.
     *
     *
     * If you disable certain intents you also have to disable related [CacheFlags][CacheFlag].
     * This can be achieved using [.disableCache]. The required intents for each
     * flag are documented in the [CacheFlag] enum.
     *
     * @param  intents
     * The intents to disable (default: none)
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .setMemberCachePolicy
     * @since  4.2.0
     */
    @Nonnull
    fun setDisabledIntents(intents: Collection<GatewayIntent>?): JDABuilder {
        this.intents = GatewayIntent.ALL_INTENTS
        if (intents != null) this.intents = this.intents and getRaw(intents).inv()
        return this
    }

    /**
     * Disable the specified [GatewayIntents][GatewayIntent].
     * <br></br>This will not enable any currently unset intents.
     *
     *
     * If you disable certain intents you also have to disable related [CacheFlags][CacheFlag].
     * This can be achieved using [.disableCache]. The required intents for each
     * flag are documented in the [CacheFlag] enum.
     *
     * @param  intents
     * The intents to disable
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .enableIntents
     */
    @Nonnull
    fun disableIntents(@Nonnull intents: Collection<GatewayIntent?>?): JDABuilder {
        Checks.noneNull(intents, "GatewayIntent")
        val raw: Int = getRaw(intents)
        this.intents = this.intents and raw.inv()
        return this
    }

    /**
     * Disable the specified [GatewayIntents][GatewayIntent].
     * <br></br>This will not enable any currently unset intents.
     *
     *
     * If you disable certain intents you also have to disable related [CacheFlags][CacheFlag].
     * This can be achieved using [.disableCache]. The required intents for each
     * flag are documented in the [CacheFlag] enum.
     *
     * @param  intent
     * The intent to disable
     * @param  intents
     * Other intents to disable
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .enableIntents
     */
    @Nonnull
    fun disableIntents(@Nonnull intent: GatewayIntent?, @Nonnull vararg intents: GatewayIntent?): JDABuilder {
        Checks.notNull(intent, "GatewayIntent")
        Checks.noneNull(intents, "GatewayIntent")
        val raw: Int = getRaw((intent)!!, *intents)
        this.intents = this.intents and raw.inv()
        return this
    }

    /**
     * Configures which events will be enabled.
     * Bots which did not enable presence/member updates in the developer dashboard are required to disable [GatewayIntent.GUILD_PRESENCES] and [GatewayIntent.GUILD_MEMBERS]!
     *
     *
     * It is not recommended to disable [GatewayIntent.GUILD_MEMBERS] when
     * using [MemberCachePolicy.ALL] as the members cannot be removed from cache by a leave event without this intent.
     *
     *
     * If you disable certain intents you also have to disable related [CacheFlags][CacheFlag].
     * This can be achieved using [.disableCache]. The required intents for each
     * flag are documented in the [CacheFlag] enum.
     *
     * @param  intent
     * The intent to enable
     * @param  intents
     * Any other intents to enable
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .setMemberCachePolicy
     * @since  4.2.0
     */
    @Nonnull
    fun setEnabledIntents(@Nonnull intent: GatewayIntent, @Nonnull vararg intents: GatewayIntent?): JDABuilder {
        Checks.notNull(intent, "Intents")
        Checks.noneNull(intents, "Intents")
        val set: EnumSet<GatewayIntent> = EnumSet.of(intent, *intents)
        return setDisabledIntents(EnumSet.complementOf(set))
    }

    /**
     * Configures which events will be enabled.
     * Bots which did not enable presence/member updates in the developer dashboard are required to disable [GatewayIntent.GUILD_PRESENCES] and [GatewayIntent.GUILD_MEMBERS]!
     *
     *
     * It is not recommended to disable [GatewayIntent.GUILD_MEMBERS] when
     * using [MemberCachePolicy.ALL] as the members cannot be removed from cache by a leave event without this intent.
     *
     *
     * If you disable certain intents you also have to disable related [CacheFlags][CacheFlag].
     * This can be achieved using [.disableCache]. The required intents for each
     * flag are documented in the [CacheFlag] enum.
     *
     * @param  intents
     * The intents to enable (default: all)
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .setMemberCachePolicy
     * @since  4.2.0
     */
    @Nonnull
    fun setEnabledIntents(intents: Collection<GatewayIntent>?): JDABuilder {
        if (intents == null || intents.isEmpty()) setDisabledIntents(EnumSet.allOf(GatewayIntent::class.java)) else if (intents is EnumSet<*>) setDisabledIntents(
            EnumSet.complementOf(intents as EnumSet<GatewayIntent>?)
        ) else setDisabledIntents(EnumSet.complementOf(EnumSet.copyOf(intents)))
        return this
    }

    /**
     * Enable the specified [GatewayIntents][GatewayIntent].
     * <br></br>This will not disable any currently set intents.
     *
     * @param  intents
     * The intents to enable
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .disableIntents
     */
    @Nonnull
    fun enableIntents(@Nonnull intents: Collection<GatewayIntent?>?): JDABuilder {
        Checks.noneNull(intents, "GatewayIntent")
        val raw: Int = getRaw(intents)
        this.intents = this.intents or raw
        return this
    }

    /**
     * Enable the specified [GatewayIntents][GatewayIntent].
     * <br></br>This will not disable any currently set intents.
     *
     * @param  intent
     * The intent to enable
     * @param  intents
     * Other intents to enable
     *
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @see .enableIntents
     */
    @Nonnull
    fun enableIntents(@Nonnull intent: GatewayIntent?, @Nonnull vararg intents: GatewayIntent?): JDABuilder {
        Checks.notNull(intent, "GatewayIntent")
        Checks.noneNull(intents, "GatewayIntent")
        val raw: Int = getRaw((intent)!!, *intents)
        this.intents = this.intents or raw
        return this
    }

    /**
     * Decides the total number of members at which a guild should start to use lazy loading.
     * <br></br>This is limited to a number between 50 and 250 (inclusive).
     * If the [chunking filter][.setChunkingFilter] is set to [ChunkingFilter.ALL]
     * this should be set to `250` (default) to minimize the amount of guilds that need to request members.
     *
     * @param  threshold
     * The threshold in `[50, 250]`
     *
     * @return The JDABuilder instance. Useful for chaining.
     *
     * @since  4.1.0
     */
    @Nonnull
    fun setLargeThreshold(threshold: Int): JDABuilder {
        largeThreshold = max(50.0, min(250.0, threshold.toDouble())).toInt() // enforce 50 <= t <= 250
        return this
    }

    /**
     * The maximum size, in bytes, of the buffer used for decompressing discord payloads.
     * <br></br>If the maximum buffer size is exceeded a new buffer will be allocated instead.
     * <br></br>Setting this to [Integer.MAX_VALUE] would imply the buffer will never be resized unless memory starvation is imminent.
     * <br></br>Setting this to `0` would imply the buffer would need to be allocated again for every payload (not recommended).
     *
     *
     * Default: `2048`
     *
     * @param  bufferSize
     * The maximum size the buffer should allow to retain
     *
     * @throws IllegalArgumentException
     * If the provided buffer size is negative
     *
     * @return The JDABuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setMaxBufferSize(bufferSize: Int): JDABuilder {
        Checks.notNegative(bufferSize, "The buffer size")
        maxBufferSize = bufferSize
        return this
    }

    /**
     * Builds a new [net.dv8tion.jda.api.JDA] instance and uses the provided token to start the login process.
     * <br></br>The login process runs in a different thread, so while this will return immediately, [net.dv8tion.jda.api.JDA] has not
     * finished loading, thus many [net.dv8tion.jda.api.JDA] methods have the chance to return incorrect information.
     * For example [JDA.getGuilds] might return an empty list or [net.dv8tion.jda.api.JDA.getUserById] might return null
     * for arbitrary user IDs.
     *
     *
     * If you wish to be sure that the [net.dv8tion.jda.api.JDA] information is correct, please use
     * [JDA.awaitReady()][net.dv8tion.jda.api.JDA.awaitReady] or register an
     * [EventListener][net.dv8tion.jda.api.hooks.EventListener] to listen for the
     * [ReadyEvent].
     *
     * @throws InvalidTokenException
     * If the provided token is invalid.
     * @throws IllegalArgumentException
     * If the provided token is empty or null. Or the provided intents/cache configuration is not possible.
     *
     * @return A [net.dv8tion.jda.api.JDA] instance that has started the login process. It is unknown as
     * to whether or not loading has finished when this returns.
     *
     * @see net.dv8tion.jda.api.JDA.awaitReady
     */
    @Nonnull
    fun build(): JDA {
        checkIntents()
        var httpClient: OkHttpClient? = httpClient
        if (httpClient == null) {
            if (httpClientBuilder == null) httpClientBuilder = IOUtil.newHttpClientBuilder()
            httpClient = httpClientBuilder.build()
        }
        val wsFactory: WebSocketFactory = if (wsFactory == null) WebSocketFactory() else wsFactory!!
        if (controller == null && shardInfo != null) controller = ConcurrentSessionController()
        val authConfig: AuthorizationConfig = AuthorizationConfig((token)!!)
        val threadingConfig: ThreadingConfig = ThreadingConfig()
        threadingConfig.setCallbackPool(callbackPool, shutdownCallbackPool)
        threadingConfig.setGatewayPool(mainWsPool, shutdownMainWsPool)
        threadingConfig.setRateLimitScheduler(rateLimitScheduler, shutdownRateLimitScheduler)
        threadingConfig.setRateLimitElastic(rateLimitElastic, shutdownRateLimitElastic)
        threadingConfig.setEventPool(eventPool, shutdownEventPool)
        threadingConfig.setAudioPool(audioPool, shutdownAudioPool)
        val sessionConfig: SessionConfig = SessionConfig(
            controller,
            httpClient,
            wsFactory,
            voiceDispatchInterceptor,
            flags,
            maxReconnectDelay,
            largeThreshold
        )
        val metaConfig: MetaConfig = MetaConfig(maxBufferSize, contextMap, cacheFlags, flags)
        val jda: JDAImpl = JDAImpl(authConfig, sessionConfig, threadingConfig, metaConfig, restConfig)
        jda.setMemberCachePolicy(memberCachePolicy)
        // We can only do member chunking with the GUILD_MEMBERS intent
        if ((intents and GatewayIntent.GUILD_MEMBERS.rawValue) == 0) jda.setChunkingFilter(ChunkingFilter.NONE) else jda.setChunkingFilter(
            chunkingFilter
        )
        if (eventManager != null) jda.setEventManager(eventManager)
        if (audioSendFactory != null) jda.setAudioSendFactory(audioSendFactory)
        jda.addEventListener(*listeners.toTypedArray())
        jda.setStatus(JDA.Status.INITIALIZED) //This is already set by JDA internally, but this is to make sure the listeners catch it.

        // Set the presence information before connecting to have the correct information ready when sending IDENTIFY
        (jda.getPresence() as PresenceImpl)
            .setCacheActivity(activity)
            .setCacheIdle(idle)
            .setCacheStatus(status)
        jda.login(shardInfo, compression, true, intents, encoding)
        return jda
    }

    private fun setFlag(flag: ConfigFlag, enable: Boolean): JDABuilder {
        if (enable) flags.add(flag) else flags.remove(flag)
        return this
    }

    private fun checkIntents() {
        val membersIntent: Boolean = (intents and GatewayIntent.GUILD_MEMBERS.rawValue) != 0
        if (!membersIntent && memberCachePolicy === MemberCachePolicy.ALL) throw IllegalStateException("Cannot use MemberCachePolicy.ALL without GatewayIntent.GUILD_MEMBERS enabled!") else if (!membersIntent && chunkingFilter !== ChunkingFilter.NONE) JDAImpl.LOG.warn(
            "Member chunking is disabled due to missing GUILD_MEMBERS intent."
        )
        if (!automaticallyDisabled.isEmpty()) {
            JDAImpl.LOG.warn("Automatically disabled CacheFlags due to missing intents")
            // List each missing intent
            automaticallyDisabled.stream()
                .map(Function({ it: CacheFlag? -> "Disabled CacheFlag." + it + " (missing GatewayIntent." + it!!.requiredIntent + ")" }))
                .forEach(Consumer({ msg: String? -> JDAImpl.LOG.warn(msg) }))

            // Tell user how to disable this warning
            JDAImpl.LOG.warn(
                "You can manually disable these flags to remove this warning by using disableCache({}) on your JDABuilder",
                automaticallyDisabled.stream()
                    .map(Function({ it: CacheFlag? -> "CacheFlag." + it }))
                    .collect(Collectors.joining(", "))
            )
            // Only print this warning once
            automaticallyDisabled.clear()
        }
        if (cacheFlags.isEmpty()) return
        val providedIntents: EnumSet<GatewayIntent> = getIntents(intents)
        for (flag: CacheFlag? in cacheFlags) {
            val intent: GatewayIntent? = flag!!.requiredIntent
            if (intent != null && !providedIntents.contains(intent)) throw IllegalArgumentException("Cannot use CacheFlag." + flag + " without GatewayIntent." + intent + "!")
        }
    }

    companion object {
        /**
         * Creates a JDABuilder with recommended default settings.
         * <br></br>Note that these defaults can potentially change in the future.
         *
         *
         *  * [.setMemberCachePolicy] is set to [MemberCachePolicy.DEFAULT]
         *  * [.setChunkingFilter] is set to [ChunkingFilter.NONE]
         *  * [.setEnabledIntents] is set to [GatewayIntent.DEFAULT]
         *  * This disables [CacheFlag.ACTIVITY] and [CacheFlag.CLIENT_STATUS]
         *
         *
         * @param  token
         * The bot token to use
         *
         * @return The new JDABuilder
         *
         * @see .disableIntents
         * @see .enableIntents
         */
        @Nonnull
        @CheckReturnValue
        fun createDefault(token: String?): JDABuilder {
            return JDABuilder(token, GatewayIntent.DEFAULT).applyDefault()
        }

        /**
         * Creates a JDABuilder with recommended default settings.
         * <br></br>Note that these defaults can potentially change in the future.
         *
         *
         *  * [.setMemberCachePolicy] is set to [MemberCachePolicy.DEFAULT]
         *  * [.setChunkingFilter] is set to [ChunkingFilter.NONE]
         *  * This disables [CacheFlag.ACTIVITY] and [CacheFlag.CLIENT_STATUS]
         *
         *
         *
         * You can omit intents in this method to use [GatewayIntent.DEFAULT] and enable additional intents with
         * [.enableIntents].
         *
         *
         * If you don't enable certain intents, the cache will be disabled.
         * For instance, if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled, then members will only
         * be cached when a voice state is available.
         * If both [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] and [GUILD_VOICE_STATES][GatewayIntent.GUILD_VOICE_STATES] are disabled
         * then no members will be cached.
         *
         *
         * The individual [CacheFlags][CacheFlag] will also be disabled
         * if the [required intent][CacheFlag.getRequiredIntent] is not enabled.
         *
         * @param  token
         * The bot token to use
         * @param  intent
         * The intent to enable
         * @param  intents
         * Any other intents to enable
         *
         * @throws IllegalArgumentException
         * If provided with null intents
         *
         * @return The new JDABuilder
         */
        @Nonnull
        @CheckReturnValue
        fun createDefault(
            token: String?,
            @Nonnull intent: GatewayIntent,
            @Nonnull vararg intents: GatewayIntent?
        ): JDABuilder {
            Checks.notNull(intent, "GatewayIntent")
            Checks.noneNull(intents, "GatewayIntent")
            return createDefault(token, EnumSet.of(intent, *intents))
        }

        /**
         * Creates a JDABuilder with recommended default settings.
         * <br></br>Note that these defaults can potentially change in the future.
         *
         *
         *  * [.setMemberCachePolicy] is set to [MemberCachePolicy.DEFAULT]
         *  * [.setChunkingFilter] is set to [ChunkingFilter.NONE]
         *  * This disables [CacheFlag.ACTIVITY] and [CacheFlag.CLIENT_STATUS]
         *
         *
         *
         * You can omit intents in this method to use [GatewayIntent.DEFAULT] and enable additional intents with
         * [.enableIntents].
         *
         *
         * If you don't enable certain intents, the cache will be disabled.
         * For instance, if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled, then members will only
         * be cached when a voice state is available.
         * If both [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] and [GUILD_VOICE_STATES][GatewayIntent.GUILD_VOICE_STATES] are disabled
         * then no members will be cached.
         *
         *
         * The individual [CacheFlags][CacheFlag] will also be disabled
         * if the [required intent][CacheFlag.getRequiredIntent] is not enabled.
         *
         * @param  token
         * The bot token to use
         * @param  intents
         * The intents to enable
         *
         * @throws IllegalArgumentException
         * If provided with null intents
         *
         * @return The new JDABuilder
         */
        @Nonnull
        @CheckReturnValue
        fun createDefault(token: String?, @Nonnull intents: Collection<GatewayIntent>?): JDABuilder {
            return create(token, intents).applyDefault()
        }

        /**
         * Creates a JDABuilder with low memory profile settings.
         * <br></br>Note that these defaults can potentially change in the future.
         *
         *
         *  * [.setEnabledIntents] is set to [GatewayIntent.DEFAULT]
         *  * [.setMemberCachePolicy] is set to [MemberCachePolicy.NONE]
         *  * [.setChunkingFilter] is set to [ChunkingFilter.NONE]
         *  * This disables all existing [CacheFlags][CacheFlag]
         *
         *
         * @param  token
         * The bot token to use
         *
         * @return The new JDABuilder
         *
         * @see .disableIntents
         * @see .enableIntents
         */
        @JvmStatic
        @Nonnull
        @CheckReturnValue
        fun createLight(token: String?): JDABuilder {
            return JDABuilder(token, GatewayIntent.DEFAULT).applyLight()
        }

        /**
         * Creates a JDABuilder with low memory profile settings.
         * <br></br>Note that these defaults can potentially change in the future.
         *
         *
         *  * [.setMemberCachePolicy] is set to [MemberCachePolicy.NONE]
         *  * [.setChunkingFilter] is set to [ChunkingFilter.NONE]
         *  * This disables all existing [CacheFlags][CacheFlag]
         *
         *
         *
         * You can omit intents in this method to use [GatewayIntent.DEFAULT] and enable additional intents with
         * [.enableIntents].
         *
         *
         * If you don't enable certain intents, the cache will be disabled.
         * For instance, if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled, then members will only
         * be cached when a voice state is available.
         * If both [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] and [GUILD_VOICE_STATES][GatewayIntent.GUILD_VOICE_STATES] are disabled
         * then no members will be cached.
         *
         *
         * The individual [CacheFlags][CacheFlag] will also be disabled
         * if the [required intent][CacheFlag.getRequiredIntent] is not enabled.
         *
         * @param  token
         * The bot token to use
         * @param  intent
         * The first intent to use
         * @param  intents
         * The other gateway intents to use
         *
         * @return The new JDABuilder
         */
        @Nonnull
        @CheckReturnValue
        fun createLight(
            token: String?,
            @Nonnull intent: GatewayIntent,
            @Nonnull vararg intents: GatewayIntent?
        ): JDABuilder {
            Checks.notNull(intent, "GatewayIntent")
            Checks.noneNull(intents, "GatewayIntent")
            return createLight(token, EnumSet.of(intent, *intents))
        }

        /**
         * Creates a JDABuilder with low memory profile settings.
         * <br></br>Note that these defaults can potentially change in the future.
         *
         *
         *  * [.setMemberCachePolicy] is set to [MemberCachePolicy.NONE]
         *  * [.setChunkingFilter] is set to [ChunkingFilter.NONE]
         *  * This disables all existing [CacheFlags][CacheFlag]
         *
         *
         *
         * You can omit intents in this method to use [GatewayIntent.DEFAULT] and enable additional intents with
         * [.enableIntents].
         *
         *
         * If you don't enable certain intents, the cache will be disabled.
         * For instance, if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled, then members will only
         * be cached when a voice state is available.
         * If both [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] and [GUILD_VOICE_STATES][GatewayIntent.GUILD_VOICE_STATES] are disabled
         * then no members will be cached.
         *
         *
         * The individual [CacheFlags][CacheFlag] will also be disabled
         * if the [required intent][CacheFlag.getRequiredIntent] is not enabled.
         *
         * @param  token
         * The bot token to use
         * @param  intents
         * The gateway intents to use
         *
         * @return The new JDABuilder
         */
        @Nonnull
        @CheckReturnValue
        fun createLight(token: String?, @Nonnull intents: Collection<GatewayIntent>?): JDABuilder {
            return create(token, intents).applyLight()
        }

        /**
         * Creates a completely empty JDABuilder with the predefined intents.
         * <br></br>You can use [JDABuilder.create(EnumSet.noneOf(GatewayIntent.class))][.create] to disable all intents.
         *
         * <br></br>If you use this, you need to set the token using
         * [setToken(String)][net.dv8tion.jda.api.JDABuilder.setToken]
         * before calling [build()][net.dv8tion.jda.api.JDABuilder.build]
         *
         *
         * If you don't enable certain intents, the cache will be disabled.
         * For instance, if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled, then members will only
         * be cached when a voice state is available.
         * If both [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] and [GUILD_VOICE_STATES][GatewayIntent.GUILD_VOICE_STATES] are disabled
         * then no members will be cached.
         *
         *
         * The individual [CacheFlags][CacheFlag] will also be disabled
         * if the [required intent][CacheFlag.getRequiredIntent] is not enabled.
         *
         * @param intent
         * The first intent
         * @param intents
         * The gateway intents to use
         *
         * @throws IllegalArgumentException
         * If the provided intents are null
         *
         * @return The JDABuilder instance
         *
         * @see .setToken
         */
        @Nonnull
        @CheckReturnValue
        fun create(@Nonnull intent: GatewayIntent?, @Nonnull vararg intents: GatewayIntent?): JDABuilder {
            return create(null, intent, *intents)
        }

        /**
         * Creates a completely empty JDABuilder with the predefined intents.
         *
         * <br></br>If you use this, you need to set the token using
         * [setToken(String)][net.dv8tion.jda.api.JDABuilder.setToken]
         * before calling [build()][net.dv8tion.jda.api.JDABuilder.build]
         *
         *
         * If you don't enable certain intents, the cache will be disabled.
         * For instance, if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled, then members will only
         * be cached when a voice state is available.
         * If both [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] and [GUILD_VOICE_STATES][GatewayIntent.GUILD_VOICE_STATES] are disabled
         * then no members will be cached.
         *
         *
         * The individual [CacheFlags][CacheFlag] will also be disabled
         * if the [required intent][CacheFlag.getRequiredIntent] is not enabled.
         *
         * @param intents
         * The gateway intents to use
         *
         * @throws IllegalArgumentException
         * If the provided intents are null
         *
         * @return The JDABuilder instance
         *
         * @see .setToken
         */
        @Nonnull
        @CheckReturnValue
        fun create(@Nonnull intents: Collection<GatewayIntent>?): JDABuilder {
            return create(null, intents)
        }

        /**
         * Creates a JDABuilder with the predefined token.
         * <br></br>You can use [JDABuilder.create(token, EnumSet.noneOf(GatewayIntent.class))][.create] to disable all intents.
         *
         *
         * If you don't enable certain intents, the cache will be disabled.
         * For instance, if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled, then members will only
         * be cached when a voice state is available.
         * If both [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] and [GUILD_VOICE_STATES][GatewayIntent.GUILD_VOICE_STATES] are disabled
         * then no members will be cached.
         *
         *
         * The individual [CacheFlags][CacheFlag] will also be disabled
         * if the [required intent][CacheFlag.getRequiredIntent] is not enabled.
         *
         * @param token
         * The bot token to use
         * @param intent
         * The first gateway intent to use
         * @param intents
         * Additional gateway intents to use
         *
         * @throws IllegalArgumentException
         * If the provided intents are null
         *
         * @return The JDABuilder instance
         *
         * @see .setToken
         */
        @Nonnull
        @CheckReturnValue
        fun create(
            token: String?,
            @Nonnull intent: GatewayIntent?,
            @Nonnull vararg intents: GatewayIntent?
        ): JDABuilder {
            return JDABuilder(token, getRaw((intent)!!, *intents)).applyIntents()
        }

        /**
         * Creates a JDABuilder with the predefined token.
         *
         *
         * If you don't enable certain intents, the cache will be disabled.
         * For instance, if the [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] intent is disabled, then members will only
         * be cached when a voice state is available.
         * If both [GUILD_MEMBERS][GatewayIntent.GUILD_MEMBERS] and [GUILD_VOICE_STATES][GatewayIntent.GUILD_VOICE_STATES] are disabled
         * then no members will be cached.
         *
         *
         * The individual [CacheFlags][CacheFlag] will also be disabled
         * if the [required intent][CacheFlag.getRequiredIntent] is not enabled.
         *
         * @param token
         * The bot token to use
         * @param intents
         * The gateway intents to use
         *
         * @throws IllegalArgumentException
         * If the provided intents are null
         *
         * @return The JDABuilder instance
         *
         * @see .setToken
         */
        @Nonnull
        @CheckReturnValue
        fun create(token: String?, @Nonnull intents: Collection<GatewayIntent>?): JDABuilder {
            return JDABuilder(token, getRaw((intents)!!)).applyIntents()
        }
    }
}
