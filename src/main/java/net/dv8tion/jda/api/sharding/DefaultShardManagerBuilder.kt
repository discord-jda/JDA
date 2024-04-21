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

import com.neovisionaries.ws.client.WebSocketFactory
import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.ReplaceWith
import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.GatewayIntent.Companion.getIntents
import net.dv8tion.jda.api.requests.GatewayIntent.Companion.getRaw
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.RestConfig
import net.dv8tion.jda.api.utils.*
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory
import net.dv8tion.jda.internal.utils.config.flags.ConfigFlag
import net.dv8tion.jda.internal.utils.config.flags.ShardingConfigFlag
import net.dv8tion.jda.internal.utils.config.sharding.*
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.*
import java.util.function.Consumer
import java.util.function.IntFunction
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/**
 * Used to create new instances of JDA's default [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] implementation.
 *
 *
 * A single DefaultShardManagerBuilder can be reused multiple times. Each call to [.build]
 * creates a new [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] instance using the same information.
 *
 * @author Aljoscha Grebe
 *
 * @since  3.4.0
 */
class DefaultShardManagerBuilder protected constructor(token: String?, intents: Int) {
    protected val listeners: MutableList<Any?> = ArrayList()
    protected val listenerProviders: MutableList<IntFunction<Any>?> = ArrayList()
    protected val automaticallyDisabled = EnumSet.noneOf(CacheFlag::class.java)
    protected var sessionController: SessionController? = null
    protected var voiceDispatchInterceptor: VoiceDispatchInterceptor? = null
    protected var cacheFlags = EnumSet.allOf(CacheFlag::class.java)
    protected var flags = ConfigFlag.getDefault()
    protected var shardingFlags = ShardingConfigFlag.getDefault()
    protected var compression = Compression.ZLIB
    protected var encoding = GatewayEncoding.JSON
    protected var shardsTotal = -1
    protected var maxReconnectDelay = 900
    protected var largeThreshold = 250
    protected var maxBufferSize = 2048
    protected var intents = -1
    protected var token: String? = null
    protected var idleProvider: IntFunction<Boolean>? = null
    protected var statusProvider: IntFunction<OnlineStatus?>? = null
    protected var activityProvider: IntFunction<out Activity?>? = null
    protected var contextProvider: IntFunction<out ConcurrentMap<String, String>?>? = null
    protected var eventManagerProvider: IntFunction<out IEventManager?>? = null
    protected var rateLimitSchedulerProvider: ThreadPoolProvider<out ScheduledExecutorService>? =
        ThreadPoolProvider.Companion.lazy<ScheduledExecutorService>(
            IntFunction<ScheduledExecutorService> { total: Int ->
                Executors.newScheduledThreadPool(
                    max(2.0, (2 * ln(total.toDouble()).toInt()).toDouble()).toInt(), CountingThreadFactory(
                        { "JDA" }, "RateLimit-Scheduler", true
                    )
                )
            }
        )
    protected var rateLimitElasticProvider: ThreadPoolProvider<out ExecutorService>? =
        ThreadPoolProvider.Companion.lazy<ExecutorService>(
            IntFunction<ExecutorService> { total: Int ->
                val pool = Executors.newCachedThreadPool(
                    CountingThreadFactory(
                        { "JDA" }, "RateLimit-Elastic", true
                    )
                )
                if (pool is ThreadPoolExecutor) {
                    pool.setCorePoolSize(
                        max(1.0, ln(total.toDouble()).toInt().toDouble()).toInt()
                    )
                    pool.setKeepAliveTime(2, TimeUnit.MINUTES)
                }
                pool
            }
        )
    protected var gatewayPoolProvider: ThreadPoolProvider<out ScheduledExecutorService>? =
        ThreadPoolProvider.Companion.lazy<ScheduledExecutorService>(
            IntFunction<ScheduledExecutorService> { total: Int ->
                Executors.newScheduledThreadPool(
                    max(1.0, ln(total.toDouble()).toInt().toDouble()).toInt(), CountingThreadFactory(
                        { "JDA" }, "Gateway"
                    )
                )
            }
        )
    protected var callbackPoolProvider: ThreadPoolProvider<out ExecutorService>? = null
    protected var eventPoolProvider: ThreadPoolProvider<out ExecutorService>? = null
    protected var audioPoolProvider: ThreadPoolProvider<out ScheduledExecutorService>? = null
    protected var restConfigProvider: IntFunction<out RestConfig>? = null
    protected var shards: Collection<Int>? = null
    protected var httpClientBuilder: Builder? = null
    protected var httpClient: OkHttpClient? = null
    protected var wsFactory: WebSocketFactory? = null
    protected var audioSendFactory: IAudioSendFactory? = null
    protected var threadFactory: ThreadFactory? = null
    protected var chunkingFilter = ChunkingFilter.ALL
    protected var memberCachePolicy = MemberCachePolicy.ALL

    init {
        this.token = token
        this.intents = 1 or intents
    }

    private fun applyDefault(): DefaultShardManagerBuilder {
        return setMemberCachePolicy(MemberCachePolicy.DEFAULT)
            .setChunkingFilter(ChunkingFilter.NONE)
            .disableCache(CacheFlag.getPrivileged())
            .setLargeThreshold(250)
    }

    private fun applyLight(): DefaultShardManagerBuilder {
        return setMemberCachePolicy(MemberCachePolicy.NONE)
            .setChunkingFilter(ChunkingFilter.NONE)
            .disableCache(EnumSet.allOf(CacheFlag::class.java))
            .setLargeThreshold(50)
    }

    private fun applyIntents(): DefaultShardManagerBuilder {
        val disabledCache = EnumSet.allOf(CacheFlag::class.java)
        for (flag in CacheFlag.entries) {
            val requiredIntent = flag.requiredIntent
            if (requiredIntent == null || requiredIntent.rawValue and intents != 0) disabledCache.remove(flag)
        }
        val enableMembers = intents and GatewayIntent.GUILD_MEMBERS.rawValue != 0
        return setChunkingFilter(if (enableMembers) ChunkingFilter.ALL else ChunkingFilter.NONE)
            .setMemberCachePolicy(if (enableMembers) MemberCachePolicy.ALL else MemberCachePolicy.DEFAULT)
            .setDisabledCache(disabledCache)
    }

    private fun setDisabledCache(flags: EnumSet<CacheFlag?>): DefaultShardManagerBuilder {
        this.disableCache(flags)
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.2.1
     */
    @Nonnull
    fun setGatewayEncoding(@Nonnull encoding: GatewayEncoding): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     */
    @Nonnull
    fun setRawEventsEnabled(enable: Boolean): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see Event.getRawData
     */
    @Nonnull
    fun setEventPassthrough(enable: Boolean): DefaultShardManagerBuilder {
        return setFlag(ConfigFlag.EVENT_PASSTHROUGH, enable)
    }

    /**
     * Whether the rate-limit should be relative to the current time plus latency.
     * <br></br>By default we use the `X-RateLimit-Rest-After` header to determine when
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.1.0
     */
    @Nonnull
    @Deprecated("")
    @ForRemoval(deadline = "5.1.0")
    @ReplaceWith("setRestConfig(new RestConfig().setRelativeRateLimit(enable))")
    fun setRelativeRateLimit(enable: Boolean): DefaultShardManagerBuilder {
        return setFlag(ConfigFlag.USE_RELATIVE_RATELIMIT, enable)
    }

    /**
     * Custom [RestConfig] to use.
     * <br></br>This can be used to customize how rate-limits are handled and configure a custom http proxy.
     *
     * @param  provider
     * The [RestConfig] provider to use
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRestConfigProvider(@Nonnull provider: IntFunction<out RestConfig>?): DefaultShardManagerBuilder {
        Checks.notNull(provider, "RestConfig Provider")
        restConfigProvider = provider
        return this
    }

    /**
     * Custom [RestConfig] to use.
     * <br></br>This can be used to customize how rate-limits are handled and configure a custom http proxy.
     *
     * @param  config
     * The [RestConfig] to use
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRestConfig(@Nonnull config: RestConfig): DefaultShardManagerBuilder {
        Checks.notNull(config, "RestConfig")
        return setRestConfigProvider { ignored: Int -> config }
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .enableCache
     * @see .disableCache
     */
    @Nonnull
    fun enableCache(@Nonnull flags: Collection<CacheFlag?>?): DefaultShardManagerBuilder {
        Checks.noneNull(flags, "CacheFlags")
        cacheFlags.addAll(flags!!)
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .enableCache
     * @see .disableCache
     */
    @Nonnull
    fun enableCache(@Nonnull flag: CacheFlag?, @Nonnull vararg flags: CacheFlag?): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .disableCache
     * @see .enableCache
     */
    @Nonnull
    fun disableCache(@Nonnull flags: Collection<CacheFlag?>?): DefaultShardManagerBuilder {
        Checks.noneNull(flags, "CacheFlags")
        automaticallyDisabled.removeAll(flags!!)
        cacheFlags.removeAll(flags)
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .disableCache
     * @see .enableCache
     */
    @Nonnull
    fun disableCache(@Nonnull flag: CacheFlag?, @Nonnull vararg flags: CacheFlag?): DefaultShardManagerBuilder {
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
     * <pre>`public void configureCache(DefaultShardManagerBuilder builder) {
     * // Cache members who are in a voice channel
     * MemberCachePolicy policy = MemberCachePolicy.VOICE;
     * // Cache members who are in a voice channel
     * // AND are also online
     * policy = policy.and(MemberCachePolicy.ONLINE);
     * // Cache members who are in a voice channel
     * // AND are also online
     * // OR are the owner of the guild
     * policy = policy.or(MemberCachePolicy.OWNER);
     * // Cache members who have a role with the name "Moderator"
     * policy = (member) -> member.getRoles().stream().map(Role::getName).anyMatch("Moderator"::equals);
     *
     * builder.setMemberCachePolicy(policy);
     * }
    `</pre> *
     *
     * @param  policy
     * The [MemberCachePolicy] or null to use default [MemberCachePolicy.ALL]
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see MemberCachePolicy
     *
     * @see .setEnabledIntents
     * @since  4.2.0
     */
    @Nonnull
    fun setMemberCachePolicy(policy: MemberCachePolicy?): DefaultShardManagerBuilder {
        if (policy == null) memberCachePolicy = MemberCachePolicy.ALL else memberCachePolicy = policy
        return this
    }

    /**
     * Sets the [SessionController][net.dv8tion.jda.api.utils.SessionController]
     * for the resulting ShardManager instance. This can be used to sync behaviour and state between shards
     * of a bot and should be one and the same instance on all builders for the shards.
     *
     * @param  controller
     * The [SessionController][net.dv8tion.jda.api.utils.SessionController] to use
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.utils.SessionControllerAdapter SessionControllerAdapter
     */
    @Nonnull
    fun setSessionController(controller: SessionController?): DefaultShardManagerBuilder {
        sessionController = controller
        return this
    }

    /**
     * Configures a custom voice dispatch handler which handles audio connections.
     *
     * @param  interceptor
     * The new voice dispatch handler, or null to use the default
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     *
     * @see VoiceDispatchInterceptor
     */
    @Nonnull
    fun setVoiceDispatchInterceptor(interceptor: VoiceDispatchInterceptor?): DefaultShardManagerBuilder {
        voiceDispatchInterceptor = interceptor
        return this
    }

    /**
     * Sets the [MDC][org.slf4j.MDC] mappings provider to use in JDA.
     * <br></br>If sharding is enabled JDA will automatically add a `jda.shard` context with the format `[SHARD_ID / TOTAL]`
     * where `SHARD_ID` and `TOTAL` are the shard configuration.
     * Additionally it will provide context for the id via `jda.shard.id` and the total via `jda.shard.total`.
     *
     *
     * **The manager will call this with a shardId and it is recommended to provide a different context map for each shard!**
     * <br></br>This automatically switches [.setContextEnabled] to true if the provided function is not null!
     *
     * @param  provider
     * The provider for **modifiable** context maps to use in JDA, or `null` to reset
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see [MDC Javadoc](https://www.slf4j.org/api/org/slf4j/MDC.html)
     */
    @Nonnull
    fun setContextMap(provider: IntFunction<out ConcurrentMap<String, String>?>?): DefaultShardManagerBuilder {
        contextProvider = provider
        if (provider != null) setContextEnabled(true)
        return this
    }

    /**
     * Whether JDA should use a synchronized MDC context for all of its controlled threads.
     * <br></br>Default: `true`
     *
     * @param  enable
     * True, if JDA should provide an MDC context map
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see [MDC Javadoc](https://www.slf4j.org/api/org/slf4j/MDC.html)
     *
     * @see .setContextMap
     */
    @Nonnull
    fun setContextEnabled(enable: Boolean): DefaultShardManagerBuilder {
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
     * **We recommend to keep this on the default unless you have issues with the decompression**
     * <br></br>This mode might become obligatory in a future version, do not rely on this switch to stay.
     *
     * @param  compression
     * The compression algorithm to use for the gateway connection
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see [Official Discord Documentation - Transport Compression](https://discord.com/developers/docs/topics/gateway.transport-compression)
     */
    @Nonnull
    fun setCompression(@Nonnull compression: Compression): DefaultShardManagerBuilder {
        Checks.notNull(compression, "Compression")
        this.compression = compression
        return this
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the [DefaultShardManager] object.
     * <br></br>This uses the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] by default.
     * <br></br>To switch to the [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager],
     * use [setEventManagerProvider(id -&gt; new AnnotatedEventManager())][.setEventManagerProvider].
     *
     *
     * **Note:** When using the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] (default),
     * given listener(s) **must** be instance of [EventListener][net.dv8tion.jda.api.hooks.EventListener]!
     *
     * @param  listeners
     * The listener(s) to add to the list.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see DefaultShardManager.addEventListener
     */
    @Nonnull
    fun addEventListeners(@Nonnull vararg listeners: Any?): DefaultShardManagerBuilder {
        return this.addEventListeners(Arrays.asList(*listeners))
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the [DefaultShardManager] object.
     * <br></br>This uses the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] by default.
     * <br></br>To switch to the [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager],
     * use [setEventManager(id -&gt; new AnnotatedEventManager())][.setEventManagerProvider].
     *
     *
     * **Note:** When using the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] (default),
     * given listener(s) **must** be instance of [EventListener][net.dv8tion.jda.api.hooks.EventListener]!
     *
     * @param  listeners
     * The listener(s) to add to the list.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see DefaultShardManager.addEventListener
     */
    @Nonnull
    fun addEventListeners(@Nonnull listeners: Collection<Any?>?): DefaultShardManagerBuilder {
        Checks.noneNull(listeners, "listeners")
        this.listeners.addAll(listeners!!)
        return this
    }

    /**
     * Removes all provided listeners from the list of listeners.
     *
     * @param  listeners
     * The listener(s) to remove from the list.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.JDA.removeEventListener
     */
    @Nonnull
    fun removeEventListeners(@Nonnull vararg listeners: Any?): DefaultShardManagerBuilder {
        return this.removeEventListeners(Arrays.asList(*listeners))
    }

    /**
     * Removes all provided listeners from the list of listeners.
     *
     * @param  listeners
     * The listener(s) to remove from the list.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.JDA.removeEventListener
     */
    @Nonnull
    fun removeEventListeners(@Nonnull listeners: Collection<Any?>?): DefaultShardManagerBuilder {
        Checks.noneNull(listeners, "listeners")
        this.listeners.removeAll(listeners!!)
        return this
    }

    /**
     * Adds the provided listener provider to the list of listener providers that will be used to create listeners.
     * On shard creation (including shard restarts) the provider will have the shard id applied and must return a listener,
     * which will be used, along all other listeners, to populate the listeners of the JDA object of that shard.
     *
     * <br></br>This uses the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] by default.
     * <br></br>To switch to the [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager],
     * use [setEventManager(id -&gt; new AnnotatedEventManager())][.setEventManagerProvider].
     *
     *
     * **Note:** When using the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] (default),
     * given listener(s) **must** be instance of [EventListener][net.dv8tion.jda.api.hooks.EventListener]!
     *
     * @param  listenerProvider
     * The listener provider to add to the list of listener providers.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun addEventListenerProvider(@Nonnull listenerProvider: IntFunction<Any>): DefaultShardManagerBuilder {
        return addEventListenerProviders(setOf(listenerProvider))
    }

    /**
     * Adds the provided listener providers to the list of listener providers that will be used to create listeners.
     * On shard creation (including shard restarts) each provider will have the shard id applied and must return a listener,
     * which will be used, along all other listeners, to populate the listeners of the JDA object of that shard.
     *
     * <br></br>This uses the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] by default.
     * <br></br>To switch to the [AnnotatedEventManager][net.dv8tion.jda.api.hooks.AnnotatedEventManager],
     * use [setEventManager(id -&gt; new AnnotatedEventManager())][.setEventManagerProvider].
     *
     *
     * **Note:** When using the [InterfacedEventListener][net.dv8tion.jda.api.hooks.InterfacedEventManager] (default),
     * given listener(s) **must** be instance of [EventListener][net.dv8tion.jda.api.hooks.EventListener]!
     *
     * @param  listenerProviders
     * The listener provider to add to the list of listener providers.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun addEventListenerProviders(@Nonnull listenerProviders: Collection<IntFunction<Any>?>?): DefaultShardManagerBuilder {
        Checks.noneNull(listenerProviders, "listener providers")
        this.listenerProviders.addAll(listenerProviders!!)
        return this
    }

    /**
     * Removes the provided listener provider from the list of listener providers.
     *
     * @param  listenerProvider
     * The listener provider to remove from the list of listener providers.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun removeEventListenerProvider(@Nonnull listenerProvider: IntFunction<Any>): DefaultShardManagerBuilder {
        return removeEventListenerProviders(setOf(listenerProvider))
    }

    /**
     * Removes all provided listener providers from the list of listener providers.
     *
     * @param  listenerProviders
     * The listener provider(s) to remove from the list of listener providers.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun removeEventListenerProviders(@Nonnull listenerProviders: Collection<IntFunction<Any>?>?): DefaultShardManagerBuilder {
        Checks.noneNull(listenerProviders, "listener providers")
        this.listenerProviders.removeAll(listenerProviders!!)
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setAudioSendFactory(factory: IAudioSendFactory?): DefaultShardManagerBuilder {
        audioSendFactory = factory
        return this
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setAutoReconnect(autoReconnect: Boolean): DefaultShardManagerBuilder {
        return setFlag(ConfigFlag.AUTO_RECONNECT, autoReconnect)
    }

    /**
     * If enabled, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the [MessageBulkDeleteEvent][net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent].
     *
     *
     * Default: **true (enabled)**
     *
     * @param  enabled
     * True - The MESSAGE_DELETE_BULK will be split into multiple individual MessageDeleteEvents.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setBulkDeleteSplittingEnabled(enabled: Boolean): DefaultShardManagerBuilder {
        return setFlag(ConfigFlag.BULK_DELETE_SPLIT, enabled)
    }

    /**
     * Enables/Disables the use of a Shutdown hook to clean up the ShardManager and it's JDA instances.
     * <br></br>When the Java program closes shutdown hooks are run. This is used as a last-second cleanup
     * attempt by JDA to properly close connections.
     *
     *
     * Default: **true (enabled)**
     *
     * @param  enable
     * True (default) - use shutdown hook to clean up the ShardManager and it's JDA instances if the Java program is closed.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setEnableShutdownHook(enable: Boolean): DefaultShardManagerBuilder {
        return setFlag(ConfigFlag.SHUTDOWN_HOOK, enable)
    }

    /**
     * Sets a provider to change the internally used EventManager.
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
     * @param  eventManagerProvider
     * A supplier for the new [net.dv8tion.jda.api.hooks.IEventManager] to use.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setEventManagerProvider(@Nonnull eventManagerProvider: IntFunction<out IEventManager?>?): DefaultShardManagerBuilder {
        Checks.notNull(eventManagerProvider, "eventManagerProvider")
        this.eventManagerProvider = eventManagerProvider
        return this
    }

    /**
     * Sets the [Activity][net.dv8tion.jda.api.entities.Activity] for our session.
     * <br></br>This value can be changed at any time in the [Presence][net.dv8tion.jda.api.managers.Presence] from a JDA instance.
     *
     *
     * **Hint:** You can create an [Activity][net.dv8tion.jda.api.entities.Activity] object using
     * [Activity.playing(String)][net.dv8tion.jda.api.entities.Activity.playing] or
     * [net.dv8tion.jda.api.entities.Activity.streaming] Activity.streaming(String, String)}.
     *
     * @param  activity
     * An instance of [Activity][net.dv8tion.jda.api.entities.Activity] (null allowed)
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setActivity
     */
    @Nonnull
    fun setActivity(activity: Activity?): DefaultShardManagerBuilder {
        return setActivityProvider { id: Int -> activity }
    }

    /**
     * Sets the [Activity][net.dv8tion.jda.api.entities.Activity] for our session.
     * <br></br>This value can be changed at any time in the [Presence][net.dv8tion.jda.api.managers.Presence] from a JDA instance.
     *
     *
     * **Hint:** You can create an [Activity][net.dv8tion.jda.api.entities.Activity] object using
     * [Activity.playing(String)][net.dv8tion.jda.api.entities.Activity.playing] or
     * [Activity.streaming(String, String)][net.dv8tion.jda.api.entities.Activity.streaming].
     *
     * @param  activityProvider
     * An instance of [Activity][net.dv8tion.jda.api.entities.Activity] (null allowed)
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setActivity
     */
    @Nonnull
    fun setActivityProvider(activityProvider: IntFunction<out Activity?>?): DefaultShardManagerBuilder {
        this.activityProvider = activityProvider
        return this
    }

    /**
     * Sets whether or not we should mark our sessions as afk
     * <br></br>This value can be changed at any time using
     * [DefaultShardManager#setIdleProvider(boolean)][DefaultShardManager.setIdle].
     *
     * @param  idle
     * boolean value that will be provided with our IDENTIFY packages to mark our sessions as afk or not. **(default false)**
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setIdle
     */
    @Nonnull
    fun setIdle(idle: Boolean): DefaultShardManagerBuilder {
        return setIdleProvider { id: Int -> idle }
    }

    /**
     * Sets whether or not we should mark our sessions as afk
     * <br></br>This value can be changed at any time using
     * [DefaultShardManager#setIdleProvider(boolean)][DefaultShardManager.setIdle].
     *
     * @param  idleProvider
     * boolean value that will be provided with our IDENTIFY packages to mark our sessions as afk or not. **(default false)**
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setIdle
     */
    @Nonnull
    fun setIdleProvider(idleProvider: IntFunction<Boolean>?): DefaultShardManagerBuilder {
        this.idleProvider = idleProvider
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setStatus
     */
    @Nonnull
    fun setStatus(status: OnlineStatus?): DefaultShardManagerBuilder {
        Checks.notNull(status, "status")
        Checks.check(status != OnlineStatus.UNKNOWN, "OnlineStatus cannot be unknown!")
        return setStatusProvider { id: Int -> status }
    }

    /**
     * Sets the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] our connection will display.
     * <br></br>This value can be changed at any time in the [Presence][net.dv8tion.jda.api.managers.Presence] from a JDA instance.
     *
     * @param  statusProvider
     * Not-null OnlineStatus (default online)
     *
     * @throws IllegalArgumentException
     * if the provided OnlineStatus is null or [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.managers.Presence.setStatus
     */
    @Nonnull
    fun setStatusProvider(statusProvider: IntFunction<OnlineStatus?>?): DefaultShardManagerBuilder {
        this.statusProvider = statusProvider
        return this
    }

    /**
     * Sets the [ThreadFactory][java.util.concurrent.ThreadFactory] that will be used by the internal executor
     * of the ShardManager.
     *
     * Note: This will not affect Threads created by any JDA instance.
     *
     * @param  threadFactory
     * The ThreadFactory or `null` to reset to the default value.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setThreadFactory(threadFactory: ThreadFactory?): DefaultShardManagerBuilder {
        this.threadFactory = threadFactory
        return this
    }

    /**
     * Sets the [Builder][okhttp3.OkHttpClient.Builder] that will be used by JDA's requester.
     * This can be used to set things such as connection timeout and proxy.
     *
     * @param  builder
     * The new [OkHttpClient.Builder][okhttp3.OkHttpClient.Builder] to use.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setHttpClientBuilder(builder: Builder?): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setHttpClient(client: OkHttpClient?): DefaultShardManagerBuilder {
        httpClient = client
        return this
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>This will override the rate-limit pool provider set from [.setRateLimitPoolProvider].
     * <br></br>**This automatically disables the automatic shutdown of the rate-limit pool, you can enable
     * it using [setRateLimiPool(executor, true)][.setRateLimitPool]**
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods.
     *
     *
     * Default: Shared [ScheduledThreadPoolExecutor] with (`2 * `[shard_total][.setShardsTotal]) threads.
     *
     * @param  pool
     * The thread-pool to use for rate-limit handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     */
    @Nonnull
    @ReplaceWith("setRateLimitScheduler(pool) or setRateLimitElastic(pool)")
    @Deprecated(
        """This pool is now split into two pools.
                  You should use {@link #setRateLimitScheduler(ScheduledExecutorService)} and {@link #setRateLimitElastic(ExecutorService)} instead."""
    )
    fun setRateLimitPool(pool: ScheduledExecutorService?): DefaultShardManagerBuilder {
        return setRateLimitPool(pool, pool == null)
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>This will override the rate-limit pool provider set from [.setRateLimitPoolProvider].
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods.
     *
     *
     * Default: Shared [ScheduledThreadPoolExecutor] with (`2 * `[shard_total][.setShardsTotal]) threads.
     *
     * @param  pool
     * The thread-pool to use for rate-limit handling
     * @param  automaticShutdown
     * Whether [net.dv8tion.jda.api.JDA.shutdown] should automatically shutdown this pool
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     */
    @Nonnull
    @ReplaceWith("setRateLimitScheduler(pool, automaticShutdown) or setRateLimitElastic(pool, automaticShutdown)")
    @Deprecated(
        """This pool is now split into two pools.
                  You should use {@link #setRateLimitScheduler(ScheduledExecutorService, boolean)} and {@link #setRateLimitElastic(ExecutorService, boolean)} instead."""
    )
    fun setRateLimitPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): DefaultShardManagerBuilder {
        return setRateLimitPoolProvider(pool?.let { ThreadPoolProviderImpl(it, automaticShutdown) })
    }

    /**
     * Sets the [ScheduledExecutorService] provider that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods.
     *
     *
     * Default: Shared [ScheduledThreadPoolExecutor] with (`2 * `[shard_total][.setShardsTotal]) threads.
     *
     * @param  provider
     * The thread-pool provider to use for rate-limit handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     */
    @Nonnull
    @ReplaceWith("setRateLimitSchedulerProvider(provider) or setRateLimitElasticProvider(provider)")
    @Deprecated(
        """This pool is now split into two pools.
                  You should use {@link #setRateLimitPoolProvider(ThreadPoolProvider)} and {@link #setRateLimitElasticProvider(ThreadPoolProvider)} instead."""
    )
    fun setRateLimitPoolProvider(provider: ThreadPoolProvider<out ScheduledExecutorService>?): DefaultShardManagerBuilder {
        rateLimitSchedulerProvider = provider
        return this
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>This will override the rate-limit pool provider set from [.setRateLimitPoolProvider].
     * <br></br>**This automatically disables the automatic shutdown of the rate-limit pool, you can enable
     * it using [setRateLimiPool(executor, true)][.setRateLimitPool]**
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods. Requests are handed off to the [elastic pool][.setRateLimitElastic] for blocking execution.
     *
     *
     * Default: Shared [ScheduledThreadPoolExecutor] with (`2 * ` log([shard_total][.setShardsTotal])) threads.
     *
     * @param  pool
     * The thread-pool to use for rate-limit handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitScheduler(pool: ScheduledExecutorService?): DefaultShardManagerBuilder {
        return setRateLimitScheduler(pool, pool == null)
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>This will override the rate-limit pool provider set from [.setRateLimitPoolProvider].
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods. Requests are handed off to the [elastic pool][.setRateLimitElastic] for blocking execution.
     *
     *
     * Default: Shared [ScheduledThreadPoolExecutor] with (`2 * ` log([shard_total][.setShardsTotal])) threads.
     *
     * @param  pool
     * The thread-pool to use for rate-limit handling
     * @param  automaticShutdown
     * Whether [net.dv8tion.jda.api.JDA.shutdown] should automatically shutdown this pool
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitScheduler(pool: ScheduledExecutorService?, automaticShutdown: Boolean): DefaultShardManagerBuilder {
        return setRateLimitSchedulerProvider(pool?.let { ThreadPoolProviderImpl(it, automaticShutdown) })
    }

    /**
     * Sets the [ScheduledExecutorService] provider that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     *
     *
     * This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for [net.dv8tion.jda.api.requests.RestAction.queueAfter]
     * and similar methods. Requests are handed off to the [elastic pool][.setRateLimitElastic] for blocking execution.
     *
     *
     * Default: Shared [ScheduledThreadPoolExecutor] with (`2 * ` log([shard_total][.setShardsTotal])) threads.
     *
     * @param  provider
     * The thread-pool provider to use for rate-limit handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitSchedulerProvider(provider: ThreadPoolProvider<out ScheduledExecutorService>?): DefaultShardManagerBuilder {
        rateLimitSchedulerProvider = provider
        return this
    }

    /**
     * Sets the [ExecutorService] that should be used in
     * the JDA request handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>This will override the rate-limit pool provider set from [.setRateLimitElasticProvider].
     * <br></br>**This automatically disables the automatic shutdown of the rate-limit elastic pool, you can enable
     * it using [setRateLimitElastic(executor, true)][.setRateLimitElastic]**
     *
     *
     * This is used mostly by the Rate-Limiter to execute the blocking HTTP requests at runtime.
     *
     *
     * Default: [Executors.newCachedThreadPool] shared between all shards.
     *
     * @param  pool
     * The thread-pool to use for executing http requests
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitElastic(pool: ExecutorService?): DefaultShardManagerBuilder {
        return setRateLimitElastic(pool, pool == null)
    }

    /**
     * Sets the [ExecutorService] that should be used in
     * the JDA request handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. **Only change this pool if you know what you're doing.**
     * <br></br>This will override the rate-limit pool provider set from [.setRateLimitElasticProvider].
     * <br></br>**This automatically disables the automatic shutdown of the rate-limit elastic pool, you can enable
     * it using [setRateLimitElastic(executor, true)][.setRateLimitElastic]**
     *
     *
     * This is used mostly by the Rate-Limiter to execute the blocking HTTP requests at runtime.
     *
     *
     * Default: [Executors.newCachedThreadPool] shared between all shards.
     *
     * @param  pool
     * The thread-pool to use for executing http requests
     * @param  automaticShutdown
     * Whether [net.dv8tion.jda.api.JDA.shutdown] should automatically shutdown this pool
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitElastic(pool: ExecutorService?, automaticShutdown: Boolean): DefaultShardManagerBuilder {
        return setRateLimitElasticProvider(pool?.let { ThreadPoolProviderImpl(it, automaticShutdown) })
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
     * Default: [Executors.newCachedThreadPool] shared between all shards.
     *
     * @param  provider
     * The thread-pool provider to use for executing http requests
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRateLimitElasticProvider(provider: ThreadPoolProvider<out ExecutorService>?): DefaultShardManagerBuilder {
        rateLimitElasticProvider = provider
        return this
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used for
     * the JDA main WebSocket workers.
     * <br></br>**Only change this pool if you know what you're doing.**
     * <br></br>This will override the worker pool provider set from [.setGatewayPoolProvider].
     * <br></br>**This automatically disables the automatic shutdown of the main-ws pools, you can enable
     * it using [setGatewayPoolProvider(pool, true)][.setGatewayPool]**
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
     * Default: Shared [ScheduledThreadPoolExecutor] with (`log`([shard_total][.setShardsTotal])) threads.
     *
     * @param  pool
     * The thread-pool to use for main WebSocket workers
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setGatewayPool(pool: ScheduledExecutorService?): DefaultShardManagerBuilder {
        return setGatewayPool(pool, pool == null)
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used for
     * the JDA main WebSocket workers.
     * <br></br>**Only change this pool if you know what you're doing.**
     * <br></br>This will override the worker pool provider set from [.setGatewayPoolProvider].
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
     * Default: Shared [ScheduledThreadPoolExecutor] with (`log`([shard_total][.setShardsTotal])) threads.
     *
     * @param  pool
     * The thread-pool to use for main WebSocket workers
     * @param  automaticShutdown
     * Whether [net.dv8tion.jda.api.JDA.shutdown] should automatically shutdown this pool
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setGatewayPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): DefaultShardManagerBuilder {
        return setGatewayPoolProvider(pool?.let { ThreadPoolProviderImpl(it, automaticShutdown) })
    }

    /**
     * Sets the [ScheduledExecutorService] that should be used for
     * the JDA main WebSocket workers.
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
     * Default: Shared [ScheduledThreadPoolExecutor] with (`log`([shard_total][.setShardsTotal])) threads.
     *
     * @param  provider
     * The thread-pool provider to use for main WebSocket workers
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setGatewayPoolProvider(provider: ThreadPoolProvider<out ScheduledExecutorService>?): DefaultShardManagerBuilder {
        gatewayPoolProvider = provider
        return this
    }

    /**
     * Sets the [ExecutorService] that should be used in
     * the JDA callback handler which mostly consists of [RestAction][net.dv8tion.jda.api.requests.RestAction] callbacks.
     * By default JDA will use [ForkJoinPool.commonPool]
     * <br></br>**Only change this pool if you know what you're doing.
     * <br></br>This automatically disables the automatic shutdown of the callback pools, you can enable
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setCallbackPool(executor: ExecutorService?): DefaultShardManagerBuilder {
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
     * Whether [net.dv8tion.jda.api.JDA.shutdown] should automatically shutdown this pool
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setCallbackPool(executor: ExecutorService?, automaticShutdown: Boolean): DefaultShardManagerBuilder {
        return setCallbackPoolProvider(executor?.let { ThreadPoolProviderImpl(it, automaticShutdown) })
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
     * @param  provider
     * The thread-pool provider to use for callback handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setCallbackPoolProvider(provider: ThreadPoolProvider<out ExecutorService>?): DefaultShardManagerBuilder {
        callbackPoolProvider = provider
        return this
    }

    /**
     * Sets the [ExecutorService] that should be used by the
     * event proxy to schedule events. This will be done on the calling thread by default.
     *
     *
     * The executor will not be shutdown automatically when the shard is shutdown.
     * To shut it down automatically use [.setEventPool].
     *
     *
     * Default: Disabled
     *
     * @param  executor
     * The executor for the event proxy, or null to use calling thread
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.2.0
     */
    @Nonnull
    fun setEventPool(executor: ExecutorService?): DefaultShardManagerBuilder {
        return setEventPool(executor, executor == null)
    }

    /**
     * Sets the [ExecutorService] that should be used by the
     * event proxy to schedule events. This will be done on the calling thread by default.
     *
     *
     * Default: Disabled
     *
     * @param  executor
     * The executor for the event proxy, or null to use calling thread
     * @param  automaticShutdown
     * True, if the executor should be shutdown when JDA shuts down
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.2.0
     */
    @Nonnull
    fun setEventPool(executor: ExecutorService?, automaticShutdown: Boolean): DefaultShardManagerBuilder {
        return setEventPoolProvider(executor?.let { ThreadPoolProviderImpl(it, automaticShutdown) })
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
     * Default: Disabled
     *
     * @param  provider
     * The thread-pool provider to use for callback handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.2.0
     */
    @Nonnull
    fun setEventPoolProvider(provider: ThreadPoolProvider<out ExecutorService>?): DefaultShardManagerBuilder {
        eventPoolProvider = provider
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since 4.2.1
     */
    @Nonnull
    fun setAudioPool(pool: ScheduledExecutorService?): DefaultShardManagerBuilder {
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
     * True, if the executor should be shutdown when JDA shuts down
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since 4.2.1
     */
    @Nonnull
    fun setAudioPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): DefaultShardManagerBuilder {
        return setAudioPoolProvider(pool?.let { ThreadPoolProviderImpl(it, automaticShutdown) })
    }

    /**
     * Sets the [ScheduledExecutorService] used by
     * the audio WebSocket connection. Used for sending keepalives and closing the connection.
     * <br></br>**Only change this pool if you know what you're doing.**
     *
     *
     * Default: [ScheduledThreadPoolExecutor] with 1 thread
     *
     * @param  provider
     * The thread-pool provider to use for the audio WebSocket
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since 4.2.1
     */
    @Nonnull
    fun setAudioPoolProvider(provider: ThreadPoolProvider<out ScheduledExecutorService>?): DefaultShardManagerBuilder {
        audioPoolProvider = provider
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setMaxReconnectDelay(maxReconnectDelay: Int): DefaultShardManagerBuilder {
        Checks.check(
            maxReconnectDelay >= 32,
            "Max reconnect delay must be 32 seconds or greater. You provided %d.",
            maxReconnectDelay
        )
        this.maxReconnectDelay = maxReconnectDelay
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setRequestTimeoutRetry(retryOnTimeout: Boolean): DefaultShardManagerBuilder {
        return setFlag(ConfigFlag.RETRY_TIMEOUT, retryOnTimeout)
    }

    /**
     * Sets the list of shards the [DefaultShardManager] should contain.
     *
     *
     * **This does not have any effect if the total shard count is set to `-1` (get recommended shards from discord).**
     *
     * @param  shardIds
     * The list of shard ids
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setShards(vararg shardIds: Int): DefaultShardManagerBuilder {
        Checks.notNull(shardIds, "shardIds")
        for (id in shardIds) {
            Checks.notNegative(id, "minShardId")
            Checks.check(id < shardsTotal, "maxShardId must be lower than shardsTotal")
        }
        shards = Arrays.stream(shardIds).boxed().collect(Collectors.toSet())
        return this
    }

    /**
     * Sets the range of shards the [DefaultShardManager] should contain.
     * This is useful if you want to split your shards between multiple JVMs or servers.
     *
     *
     * **This does not have any effect if the total shard count is set to `-1` (get recommended shards from discord).**
     *
     * @param  minShardId
     * The lowest shard id the DefaultShardManager should contain
     *
     * @param  maxShardId
     * The highest shard id the DefaultShardManager should contain
     *
     * @throws IllegalArgumentException
     * If either minShardId is negative, maxShardId is lower than shardsTotal or
     * minShardId is lower than or equal to maxShardId
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setShards(minShardId: Int, maxShardId: Int): DefaultShardManagerBuilder {
        Checks.notNegative(minShardId, "minShardId")
        Checks.check(maxShardId < shardsTotal, "maxShardId must be lower than shardsTotal")
        Checks.check(minShardId <= maxShardId, "minShardId must be lower than or equal to maxShardId")
        val shards: MutableList<Int> = ArrayList(maxShardId - minShardId + 1)
        for (i in minShardId..maxShardId) shards.add(i)
        this.shards = shards
        return this
    }

    /**
     * Sets the range of shards the [DefaultShardManager] should contain.
     * This is useful if you want to split your shards between multiple JVMs or servers.
     *
     *
     * **This does not have any effect if the total shard count is set to `-1` (get recommended shards from discord).**
     *
     * @param  shardIds
     * The list of shard ids
     *
     * @throws IllegalArgumentException
     * If either minShardId is negative, maxShardId is lower than shardsTotal or
     * minShardId is lower than or equal to maxShardId
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setShards(@Nonnull shardIds: Collection<Int>): DefaultShardManagerBuilder {
        Checks.notNull(shardIds, "shardIds")
        for (id in shardIds) {
            Checks.notNegative(id, "minShardId")
            Checks.check(id < shardsTotal, "maxShardId must be lower than shardsTotal")
        }
        shards = ArrayList(shardIds)
        return this
    }

    /**
     * This will set the total amount of shards the [DefaultShardManager] should use.
     *
     *  If this is set to `-1` JDA will automatically retrieve the recommended amount of shards from discord (default behavior).
     *
     * @param  shardsTotal
     * The number of overall shards or `-1` if JDA should use the recommended amount from discord.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .setShards
     */
    @Nonnull
    fun setShardsTotal(shardsTotal: Int): DefaultShardManagerBuilder {
        Checks.check(shardsTotal == -1 || shardsTotal > 0, "shardsTotal must either be -1 or greater than 0")
        this.shardsTotal = shardsTotal
        return this
    }

    /**
     * Sets the token that will be used by the [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] instance to log in when
     * [build()][net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.build] is called.
     *
     *
     * To get a bot token:
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
     * @throws java.lang.IllegalArgumentException
     * If the token is either null or empty
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setToken(@Nonnull token: String?): DefaultShardManagerBuilder {
        Checks.notBlank(token, "token")
        this.token = token
        return this
    }

    /**
     * Whether the [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] should use
     * [JDA#shutdownNow()][net.dv8tion.jda.api.JDA.shutdownNow] instead of
     * [JDA#shutdown()][net.dv8tion.jda.api.JDA.shutdown] to shutdown it's shards.
     * <br></br>**Default**: `false`
     *
     * @param  useShutdownNow
     * Whether the ShardManager should use JDA#shutdown() or not
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.JDA.shutdown
     * @see net.dv8tion.jda.api.JDA.shutdownNow
     */
    @Nonnull
    fun setUseShutdownNow(useShutdownNow: Boolean): DefaultShardManagerBuilder {
        return setFlag(ShardingConfigFlag.SHUTDOWN_NOW, useShutdownNow)
    }

    /**
     * Sets the [WebSocketFactory][com.neovisionaries.ws.client.WebSocketFactory] that will be used by JDA's websocket client.
     * This can be used to set things such as connection timeout and proxy.
     *
     * @param  factory
     * The new [WebSocketFactory][com.neovisionaries.ws.client.WebSocketFactory] to use.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setWebsocketFactory(factory: WebSocketFactory?): DefaultShardManagerBuilder {
        wsFactory = factory
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     *
     * @see ChunkingFilter.NONE
     *
     * @see ChunkingFilter.include
     * @see ChunkingFilter.exclude
     */
    @Nonnull
    fun setChunkingFilter(filter: ChunkingFilter?): DefaultShardManagerBuilder {
        chunkingFilter = filter
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .setMemberCachePolicy
     * @since  4.2.0
     */
    @Nonnull
    fun setDisabledIntents(
        @Nonnull intent: GatewayIntent,
        @Nonnull vararg intents: GatewayIntent?
    ): DefaultShardManagerBuilder {
        Checks.notNull(intent, "Intent")
        Checks.noneNull(intents, "Intent")
        val set = EnumSet.of(intent, *intents)
        return setDisabledIntents(set)
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
     * The intents to disable, or null to disable all intents (default: none)
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .setMemberCachePolicy
     * @since  4.2.0
     */
    @Nonnull
    fun setDisabledIntents(intents: Collection<GatewayIntent>?): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .enableIntents
     */
    @Nonnull
    fun disableIntents(@Nonnull intents: Collection<GatewayIntent?>?): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .enableIntents
     */
    @Nonnull
    fun disableIntents(
        @Nonnull intent: GatewayIntent?,
        @Nonnull vararg intents: GatewayIntent?
    ): DefaultShardManagerBuilder {
        Checks.notNull(intent, "GatewayIntent")
        Checks.noneNull(intents, "GatewayIntent")
        val raw = getRaw(intent!!, *intents)
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .setMemberCachePolicy
     * @since  4.2.0
     */
    @Nonnull
    fun setEnabledIntents(
        @Nonnull intent: GatewayIntent,
        @Nonnull vararg intents: GatewayIntent?
    ): DefaultShardManagerBuilder {
        Checks.notNull(intent, "Intent")
        Checks.noneNull(intents, "Intent")
        val set = EnumSet.of(intent, *intents)
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
     * The intents to enable, or null to enable no intents (default: all)
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .setMemberCachePolicy
     * @since  4.2.0
     */
    @Nonnull
    fun setEnabledIntents(intents: Collection<GatewayIntent>?): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .disableIntents
     */
    @Nonnull
    fun enableIntents(@Nonnull intents: Collection<GatewayIntent?>?): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see .enableIntents
     */
    @Nonnull
    fun enableIntents(
        @Nonnull intent: GatewayIntent?,
        @Nonnull vararg intents: GatewayIntent?
    ): DefaultShardManagerBuilder {
        Checks.notNull(intent, "GatewayIntent")
        Checks.noneNull(intents, "GatewayIntent")
        val raw = getRaw(intent!!, *intents)
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     */
    @Nonnull
    fun setLargeThreshold(threshold: Int): DefaultShardManagerBuilder {
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
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    fun setMaxBufferSize(bufferSize: Int): DefaultShardManagerBuilder {
        Checks.notNegative(bufferSize, "The buffer size")
        maxBufferSize = bufferSize
        return this
    }

    /**
     * Builds a new [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] instance and uses the provided token to start the login process.
     * <br></br>The login process runs in a different thread, so while this will return immediately, [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] has not
     * finished loading, thus many [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] methods have the chance to return incorrect information.
     * <br></br>The main use of this method is to start the JDA connect process and do other things in parallel while startup is
     * being performed like database connection or local resource loading.
     *
     *
     * Note that this method is async and as such will **not** block until all shards are started.
     *
     * @throws  InvalidTokenException
     * If the provided token is invalid.
     * @throws  IllegalArgumentException
     * If the provided token is empty or null. Or the provided intents/cache configuration is not possible.
     * @throws  net.dv8tion.jda.api.exceptions.ErrorResponseException
     * If some other HTTP error occurred.
     *
     * @return A [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] instance that has started the login process. It is unknown as
     * to whether or not loading has finished when this returns.
     */
    @Nonnull
    @Throws(IllegalArgumentException::class)
    fun build(): ShardManager {
        return build(true)
    }

    /**
     * Builds a new [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] instance. If the login parameter is true, then it will start the login process.
     * <br></br>The login process runs in a different thread, so while this will return immediately, [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] has not
     * finished loading, thus many [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] methods have the chance to return incorrect information.
     * <br></br>The main use of this method is to start the JDA connect process and do other things in parallel while startup is
     * being performed like database connection or local resource loading.
     *
     *
     * Note that this method is async and as such will **not** block until all shards are started.
     *
     * @param  login
     * Whether the login process will be started. If this is false, then you will need to manually call
     * [net.dv8tion.jda.api.sharding.ShardManager.login] to start it.
     *
     * @throws  InvalidTokenException
     * If the provided token is invalid and `login` is true
     * @throws  IllegalArgumentException
     * If the provided token is empty or null. Or the provided intents/cache configuration is not possible.
     *
     * @return A [ShardManager][net.dv8tion.jda.api.sharding.ShardManager] instance. If `login` is set to
     * true, then the instance will have started the login process. It is unknown as to whether or not loading has
     * finished when this returns.
     */
    @Nonnull
    @Throws(IllegalArgumentException::class)
    fun build(login: Boolean): ShardManager {
        checkIntents()
        val useShutdownNow = shardingFlags.contains(ShardingConfigFlag.SHUTDOWN_NOW)
        val shardingConfig = ShardingConfig(shardsTotal, useShutdownNow, intents, memberCachePolicy)
        val eventConfig = EventConfig(eventManagerProvider)
        listeners.forEach(Consumer { listener: Any? ->
            eventConfig.addEventListener(
                listener!!
            )
        })
        listenerProviders.forEach(Consumer { provider: IntFunction<Any>? ->
            eventConfig.addEventListenerProvider(
                provider!!
            )
        })
        val presenceConfig = PresenceProviderConfig()
        presenceConfig.activityProvider = activityProvider
        presenceConfig.statusProvider = statusProvider
        presenceConfig.idleProvider = idleProvider
        val threadingConfig = ThreadingProviderConfig(
            rateLimitSchedulerProvider,
            rateLimitElasticProvider,
            gatewayPoolProvider,
            callbackPoolProvider,
            eventPoolProvider,
            audioPoolProvider,
            threadFactory
        )
        val sessionConfig = ShardingSessionConfig(
            sessionController,
            voiceDispatchInterceptor,
            httpClient,
            httpClientBuilder,
            wsFactory,
            audioSendFactory,
            flags,
            shardingFlags,
            maxReconnectDelay,
            largeThreshold
        )
        val metaConfig = ShardingMetaConfig(maxBufferSize, contextProvider, cacheFlags, flags, compression, encoding)
        val manager = DefaultShardManager(
            token,
            shards,
            shardingConfig,
            eventConfig,
            presenceConfig,
            threadingConfig,
            sessionConfig,
            metaConfig,
            restConfigProvider,
            chunkingFilter
        )
        if (login) manager.login()
        return manager
    }

    private fun setFlag(flag: ConfigFlag, enable: Boolean): DefaultShardManagerBuilder {
        if (enable) flags.add(flag) else flags.remove(flag)
        return this
    }

    private fun setFlag(flag: ShardingConfigFlag, enable: Boolean): DefaultShardManagerBuilder {
        if (enable) shardingFlags.add(flag) else shardingFlags.remove(flag)
        return this
    }

    private fun checkIntents() {
        val membersIntent = intents and GatewayIntent.GUILD_MEMBERS.rawValue != 0
        check(!(!membersIntent && memberCachePolicy === MemberCachePolicy.ALL)) { "Cannot use MemberCachePolicy.ALL without GatewayIntent.GUILD_MEMBERS enabled!" }
        if (!membersIntent && chunkingFilter !== ChunkingFilter.NONE) DefaultShardManager.Companion.LOG.warn("Member chunking is disabled due to missing GUILD_MEMBERS intent.")
        if (!automaticallyDisabled.isEmpty()) {
            JDAImpl.LOG.warn("Automatically disabled CacheFlags due to missing intents")
            // List each missing intent
            automaticallyDisabled.stream()
                .map { it: CacheFlag? -> "Disabled CacheFlag." + it + " (missing GatewayIntent." + it!!.requiredIntent + ")" }
                .forEach { msg: String? -> JDAImpl.LOG.warn(msg) }

            // Tell user how to disable this warning
            JDAImpl.LOG.warn("You can manually disable these flags to remove this warning by using disableCache({}) on your DefaultShardManagerBuilder",
                automaticallyDisabled.stream()
                    .map { it: CacheFlag? -> "CacheFlag.$it" }
                    .collect(Collectors.joining(", ")))
            // Only print this warning once
            automaticallyDisabled.clear()
        }
        if (cacheFlags.isEmpty()) return
        val providedIntents = getIntents(intents)
        for (flag in cacheFlags) {
            val intent = flag!!.requiredIntent
            require(!(intent != null && !providedIntents.contains(intent))) { "Cannot use CacheFlag.$flag without GatewayIntent.$intent!" }
        }
    }

    //Avoid having multiple anonymous classes
    private class ThreadPoolProviderImpl<T : ExecutorService?>(private val pool: T, private val autoShutdown: Boolean) :
        ThreadPoolProvider<T> {
        override fun provide(shardId: Int): T {
            return pool
        }

        override fun shouldShutdownAutomatically(shardId: Int): Boolean {
            return autoShutdown
        }
    }

    companion object {
        /**
         * Creates a DefaultShardManagerBuilder with recommended default settings.
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
         * @return The new DefaultShardManagerBuilder
         *
         * @see .disableIntents
         * @see .enableIntents
         */
        @Nonnull
        @CheckReturnValue
        fun createDefault(token: String?): DefaultShardManagerBuilder {
            return DefaultShardManagerBuilder(token, GatewayIntent.DEFAULT).applyDefault()
        }

        /**
         * Creates a DefaultShardManagerBuilder with recommended default settings.
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
         * @return The new DefaultShardManagerBuilder
         */
        @Nonnull
        @CheckReturnValue
        fun createDefault(
            token: String?,
            @Nonnull intent: GatewayIntent,
            @Nonnull vararg intents: GatewayIntent?
        ): DefaultShardManagerBuilder {
            Checks.notNull(intent, "GatewayIntent")
            Checks.noneNull(intents, "GatewayIntent")
            return createDefault(token, EnumSet.of(intent, *intents))
        }

        /**
         * Creates a DefaultShardManagerBuilder with recommended default settings.
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
         * @return The new DefaultShardManagerBuilder
         */
        @Nonnull
        @CheckReturnValue
        fun createDefault(token: String?, @Nonnull intents: Collection<GatewayIntent>?): DefaultShardManagerBuilder {
            return create(token, intents).applyDefault()
        }

        /**
         * Creates a DefaultShardManagerBuilder with low memory profile settings.
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
         * @return The new DefaultShardManagerBuilder
         *
         * @see .disableIntents
         * @see .enableIntents
         */
        @Nonnull
        @CheckReturnValue
        fun createLight(token: String?): DefaultShardManagerBuilder {
            return DefaultShardManagerBuilder(token, GatewayIntent.DEFAULT).applyLight()
        }

        /**
         * Creates a DefaultShardManagerBuilder with low memory profile settings.
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
         * @return The new DefaultShardManagerBuilder
         */
        @Nonnull
        @CheckReturnValue
        fun createLight(
            token: String?,
            @Nonnull intent: GatewayIntent,
            @Nonnull vararg intents: GatewayIntent?
        ): DefaultShardManagerBuilder {
            Checks.notNull(intent, "GatewayIntent")
            Checks.noneNull(intents, "GatewayIntent")
            return createLight(token, EnumSet.of(intent, *intents))
        }

        /**
         * Creates a DefaultShardManagerBuilder with low memory profile settings.
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
         * @return The new DefaultShardManagerBuilder
         */
        @Nonnull
        @CheckReturnValue
        fun createLight(token: String?, @Nonnull intents: Collection<GatewayIntent>?): DefaultShardManagerBuilder {
            return create(token, intents).applyLight()
        }

        /**
         * Creates a completely empty DefaultShardManagerBuilder with the predefined intents.
         * <br></br>You can use [DefaultShardManagerBuilder.create(EnumSet.noneOf(GatewayIntent.class))][.create] to disable all intents.
         *
         * <br></br>If you use this, you need to set the token using
         * [setToken(String)][.setToken]
         * before calling [build()][.build]
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
         * @return The DefaultShardManagerBuilder instance
         *
         * @see .setToken
         */
        @Nonnull
        @CheckReturnValue
        fun create(
            @Nonnull intent: GatewayIntent?,
            @Nonnull vararg intents: GatewayIntent?
        ): DefaultShardManagerBuilder {
            return create(null, intent, *intents)
        }

        /**
         * Creates a completely empty DefaultShardManagerBuilder with the predefined intents.
         *
         * <br></br>If you use this, you need to set the token using
         * [setToken(String)][.setToken] before calling [build()][.build]
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
         * @return The DefaultShardManagerBuilder instance
         *
         * @see .setToken
         */
        @Nonnull
        @CheckReturnValue
        fun create(@Nonnull intents: Collection<GatewayIntent>?): DefaultShardManagerBuilder {
            return create(null, intents)
        }

        /**
         * Creates a DefaultShardManagerBuilder with the predefined token.
         * <br></br>You can use [DefaultShardManagerBuilder.create(token, EnumSet.noneOf(GatewayIntent.class))][.create] to disable all intents.
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
         * @return The DefaultShardManagerBuilder instance
         *
         * @see .setToken
         */
        @Nonnull
        @CheckReturnValue
        fun create(
            token: String?,
            @Nonnull intent: GatewayIntent?,
            @Nonnull vararg intents: GatewayIntent?
        ): DefaultShardManagerBuilder {
            return DefaultShardManagerBuilder(token, getRaw(intent!!, *intents)).applyIntents()
        }

        /**
         * Creates a DefaultShardManagerBuilder with the predefined token.
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
         * @return The DefaultShardManagerBuilder instance
         *
         * @see .setToken
         */
        @Nonnull
        @CheckReturnValue
        fun create(token: String?, @Nonnull intents: Collection<GatewayIntent>?): DefaultShardManagerBuilder {
            return DefaultShardManagerBuilder(token, getRaw(intents!!)).applyIntents()
        }
    }
}
