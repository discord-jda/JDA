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
package net.dv8tion.jda.api.sharding;

import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.config.flags.ConfigFlag;
import net.dv8tion.jda.internal.utils.config.flags.ShardingConfigFlag;
import net.dv8tion.jda.internal.utils.config.sharding.*;
import okhttp3.OkHttpClient;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Used to create new instances of JDA's default {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} implementation.
 *
 * <p>A single DefaultShardManagerBuilder can be reused multiple times. Each call to {@link #build()}
 * creates a new {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} instance using the same information.
 *
 * @author Aljoscha Grebe
 *
 * @since  3.4.0
 */
public class  DefaultShardManagerBuilder
{
    protected final List<Object> listeners = new ArrayList<>();
    protected final List<IntFunction<Object>> listenerProviders = new ArrayList<>();
    protected final EnumSet<CacheFlag> automaticallyDisabled = EnumSet.noneOf(CacheFlag.class);
    protected SessionController sessionController = null;
    protected VoiceDispatchInterceptor voiceDispatchInterceptor = null;
    protected EnumSet<CacheFlag> cacheFlags = EnumSet.allOf(CacheFlag.class);
    protected EnumSet<ConfigFlag> flags = ConfigFlag.getDefault();
    protected EnumSet<ShardingConfigFlag> shardingFlags = ShardingConfigFlag.getDefault();
    protected Compression compression = Compression.ZLIB;
    protected GatewayEncoding encoding = GatewayEncoding.JSON;
    protected int shardsTotal = -1;
    protected int maxReconnectDelay = 900;
    protected int largeThreshold = 250;
    protected int maxBufferSize = 2048;
    protected int intents = -1;
    protected String token = null;
    protected IntFunction<Boolean> idleProvider = null;
    protected IntFunction<OnlineStatus> statusProvider = null;
    protected IntFunction<? extends Activity> activityProvider = null;
    protected IntFunction<? extends ConcurrentMap<String, String>> contextProvider = null;
    protected IntFunction<? extends IEventManager> eventManagerProvider = null;
    protected ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitPoolProvider = null;
    protected ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider = null;
    protected ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider = null;
    protected ThreadPoolProvider<? extends ExecutorService> eventPoolProvider = null;
    protected ThreadPoolProvider<? extends ScheduledExecutorService> audioPoolProvider = null;
    protected Collection<Integer> shards = null;
    protected OkHttpClient.Builder httpClientBuilder = null;
    protected OkHttpClient httpClient = null;
    protected WebSocketFactory wsFactory = null;
    protected IAudioSendFactory audioSendFactory = null;
    protected ThreadFactory threadFactory = null;
    protected ChunkingFilter chunkingFilter;
    protected MemberCachePolicy memberCachePolicy = MemberCachePolicy.ALL;

    private DefaultShardManagerBuilder(@Nullable String token, int intents)
    {
        this.token = token;
        this.intents = 1 | intents;
    }

    /**
     * Creates a DefaultShardManagerBuilder with recommended default settings.
     * <br>Note that these defaults can potentially change in the future.
     *
     * <ul>
     *     <li>{@link #setMemberCachePolicy(MemberCachePolicy)} is set to {@link MemberCachePolicy#DEFAULT}</li>
     *     <li>{@link #setChunkingFilter(ChunkingFilter)} is set to {@link ChunkingFilter#NONE}</li>
     *     <li>{@link #setEnabledIntents(Collection)} is set to {@link GatewayIntent#DEFAULT}</li>
     *     <li>This disables {@link CacheFlag#ACTIVITY} and {@link CacheFlag#CLIENT_STATUS}</li>
     * </ul>
     *
     * @param  token
     *         The bot token to use
     *
     * @return The new DefaultShardManagerBuilder
     *
     * @see    #disableIntents(GatewayIntent, GatewayIntent...)
     * @see    #enableIntents(GatewayIntent, GatewayIntent...)
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder createDefault(@Nullable String token)
    {
        return new DefaultShardManagerBuilder(token, GatewayIntent.DEFAULT).applyDefault();
    }

    /**
     * Creates a DefaultShardManagerBuilder with recommended default settings.
     * <br>Note that these defaults can potentially change in the future.
     *
     * <ul>
     *     <li>{@link #setMemberCachePolicy(MemberCachePolicy)} is set to {@link MemberCachePolicy#DEFAULT}</li>
     *     <li>{@link #setChunkingFilter(ChunkingFilter)} is set to {@link ChunkingFilter#NONE}</li>
     *     <li>This disables {@link CacheFlag#ACTIVITY} and {@link CacheFlag#CLIENT_STATUS}</li>
     * </ul>
     *
     * <p>You can omit intents in this method to use {@link GatewayIntent#DEFAULT} and enable additional intents with
     * {@link #enableIntents(Collection)}.
     *
     * <p>If you don't enable certain intents, the cache will be disabled.
     * For instance, if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled, then members will only
     * be cached when a voice state is available.
     * If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} are disabled
     * then no members will be cached.
     *
     * <p>The individual {@link CacheFlag CacheFlags} will also be disabled
     * if the {@link CacheFlag#getRequiredIntent() required intent} is not enabled.
     *
     * @param  token
     *         The bot token to use
     * @param  intent
     *         The intent to enable
     * @param  intents
     *         Any other intents to enable
     *
     * @throws IllegalArgumentException
     *         If provided with null intents
     *
     * @return The new DefaultShardManagerBuilder
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder createDefault(@Nullable String token, @Nonnull GatewayIntent intent, @Nonnull GatewayIntent... intents)
    {
        Checks.notNull(intent, "GatewayIntent");
        Checks.noneNull(intents, "GatewayIntent");
        return createDefault(token, EnumSet.of(intent, intents));
    }

    /**
     * Creates a DefaultShardManagerBuilder with recommended default settings.
     * <br>Note that these defaults can potentially change in the future.
     *
     * <ul>
     *     <li>{@link #setMemberCachePolicy(MemberCachePolicy)} is set to {@link MemberCachePolicy#DEFAULT}</li>
     *     <li>{@link #setChunkingFilter(ChunkingFilter)} is set to {@link ChunkingFilter#NONE}</li>
     *     <li>This disables {@link CacheFlag#ACTIVITY} and {@link CacheFlag#CLIENT_STATUS}</li>
     * </ul>
     *
     * <p>You can omit intents in this method to use {@link GatewayIntent#DEFAULT} and enable additional intents with
     * {@link #enableIntents(Collection)}.
     *
     * <p>If you don't enable certain intents, the cache will be disabled.
     * For instance, if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled, then members will only
     * be cached when a voice state is available.
     * If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} are disabled
     * then no members will be cached.
     *
     * <p>The individual {@link CacheFlag CacheFlags} will also be disabled
     * if the {@link CacheFlag#getRequiredIntent() required intent} is not enabled.
     *
     * @param  token
     *         The bot token to use
     * @param  intents
     *         The intents to enable
     *
     * @throws IllegalArgumentException
     *         If provided with null intents
     *
     * @return The new DefaultShardManagerBuilder
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder createDefault(@Nullable String token, @Nonnull Collection<GatewayIntent> intents)
    {
        return create(token, intents).applyDefault();
    }

    private DefaultShardManagerBuilder applyDefault()
    {
        return this.setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                   .setChunkingFilter(ChunkingFilter.NONE)
                   .disableCache(CacheFlag.getPrivileged())
                   .setLargeThreshold(250);
    }

    /**
     * Creates a DefaultShardManagerBuilder with low memory profile settings.
     * <br>Note that these defaults can potentially change in the future.
     *
     * <ul>
     *     <li>{@link #setEnabledIntents(Collection)} is set to {@link GatewayIntent#DEFAULT}</li>
     *     <li>{@link #setMemberCachePolicy(MemberCachePolicy)} is set to {@link MemberCachePolicy#NONE}</li>
     *     <li>{@link #setChunkingFilter(ChunkingFilter)} is set to {@link ChunkingFilter#NONE}</li>
     *     <li>This disables all existing {@link CacheFlag CacheFlags}</li>
     * </ul>
     *
     * @param  token
     *         The bot token to use
     *
     * @return The new DefaultShardManagerBuilder
     *
     * @see    #disableIntents(GatewayIntent, GatewayIntent...)
     * @see    #enableIntents(GatewayIntent, GatewayIntent...)
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder createLight(@Nullable String token)
    {
        return new DefaultShardManagerBuilder(token, GatewayIntent.DEFAULT).applyLight();
    }

    /**
     * Creates a DefaultShardManagerBuilder with low memory profile settings.
     * <br>Note that these defaults can potentially change in the future.
     *
     * <ul>
     *     <li>{@link #setMemberCachePolicy(MemberCachePolicy)} is set to {@link MemberCachePolicy#NONE}</li>
     *     <li>{@link #setChunkingFilter(ChunkingFilter)} is set to {@link ChunkingFilter#NONE}</li>
     *     <li>This disables all existing {@link CacheFlag CacheFlags}</li>
     * </ul>
     *
     * <p>You can omit intents in this method to use {@link GatewayIntent#DEFAULT} and enable additional intents with
     * {@link #enableIntents(Collection)}.
     *
     * <p>If you don't enable certain intents, the cache will be disabled.
     * For instance, if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled, then members will only
     * be cached when a voice state is available.
     * If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} are disabled
     * then no members will be cached.
     *
     * <p>The individual {@link CacheFlag CacheFlags} will also be disabled
     * if the {@link CacheFlag#getRequiredIntent() required intent} is not enabled.
     *
     * @param  token
     *         The bot token to use
     * @param  intent
     *         The first intent to use
     * @param  intents
     *         The other gateway intents to use
     *
     * @return The new DefaultShardManagerBuilder
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder createLight(@Nullable String token, @Nonnull GatewayIntent intent, @Nonnull GatewayIntent... intents)
    {
        Checks.notNull(intent, "GatewayIntent");
        Checks.noneNull(intents, "GatewayIntent");
        return createLight(token, EnumSet.of(intent, intents));
    }

    /**
     * Creates a DefaultShardManagerBuilder with low memory profile settings.
     * <br>Note that these defaults can potentially change in the future.
     *
     * <ul>
     *     <li>{@link #setMemberCachePolicy(MemberCachePolicy)} is set to {@link MemberCachePolicy#NONE}</li>
     *     <li>{@link #setChunkingFilter(ChunkingFilter)} is set to {@link ChunkingFilter#NONE}</li>
     *     <li>This disables all existing {@link CacheFlag CacheFlags}</li>
     * </ul>
     *
     * <p>You can omit intents in this method to use {@link GatewayIntent#DEFAULT} and enable additional intents with
     * {@link #enableIntents(Collection)}.
     *
     * <p>If you don't enable certain intents, the cache will be disabled.
     * For instance, if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled, then members will only
     * be cached when a voice state is available.
     * If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} are disabled
     * then no members will be cached.
     *
     * <p>The individual {@link CacheFlag CacheFlags} will also be disabled
     * if the {@link CacheFlag#getRequiredIntent() required intent} is not enabled.
     *
     * @param  token
     *         The bot token to use
     * @param  intents
     *         The gateway intents to use
     *
     * @return The new DefaultShardManagerBuilder
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder createLight(@Nullable String token, @Nonnull Collection<GatewayIntent> intents)
    {
        return create(token, intents).applyLight();
    }

    private DefaultShardManagerBuilder applyLight()
    {
        return this.setMemberCachePolicy(MemberCachePolicy.NONE)
                   .setChunkingFilter(ChunkingFilter.NONE)
                   .disableCache(EnumSet.allOf(CacheFlag.class))
                   .setLargeThreshold(50);
    }

    /**
     * Creates a completely empty DefaultShardManagerBuilder with the predefined intents.
     * <br>You can use {@link #create(Collection) DefaultShardManagerBuilder.create(EnumSet.noneOf(GatewayIntent.class))} to disable all intents.
     *
     * <br>If you use this, you need to set the token using
     * {@link #setToken(String) setToken(String)}
     * before calling {@link #build() build()}
     *
     * <p>If you don't enable certain intents, the cache will be disabled.
     * For instance, if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled, then members will only
     * be cached when a voice state is available.
     * If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} are disabled
     * then no members will be cached.
     *
     * <p>The individual {@link CacheFlag CacheFlags} will also be disabled
     * if the {@link CacheFlag#getRequiredIntent() required intent} is not enabled.
     *
     * @param intent
     *        The first intent
     * @param intents
     *        The gateway intents to use
     *
     * @throws IllegalArgumentException
     *         If the provided intents are null
     *
     * @return The DefaultShardManagerBuilder instance
     *
     * @see   #setToken(String)
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder create(@Nonnull GatewayIntent intent, @Nonnull GatewayIntent... intents)
    {
        return create(null, intent, intents);
    }

    /**
     * Creates a completely empty DefaultShardManagerBuilder with the predefined intents.
     *
     * <br>If you use this, you need to set the token using
     * {@link #setToken(String) setToken(String)} before calling {@link #build() build()}
     *
     * <p>If you don't enable certain intents, the cache will be disabled.
     * For instance, if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled, then members will only
     * be cached when a voice state is available.
     * If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} are disabled
     * then no members will be cached.
     *
     * <p>The individual {@link CacheFlag CacheFlags} will also be disabled
     * if the {@link CacheFlag#getRequiredIntent() required intent} is not enabled.
     *
     * @param intents
     *        The gateway intents to use
     *
     * @throws IllegalArgumentException
     *         If the provided intents are null
     *
     * @return The DefaultShardManagerBuilder instance
     *
     * @see   #setToken(String)
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder create(@Nonnull Collection<GatewayIntent> intents)
    {
        return create(null, intents);
    }

    /**
     * Creates a DefaultShardManagerBuilder with the predefined token.
     * <br>You can use {@link #create(String, Collection) DefaultShardManagerBuilder.create(token, EnumSet.noneOf(GatewayIntent.class))} to disable all intents.
     *
     * <p>If you don't enable certain intents, the cache will be disabled.
     * For instance, if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled, then members will only
     * be cached when a voice state is available.
     * If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} are disabled
     * then no members will be cached.
     *
     * <p>The individual {@link CacheFlag CacheFlags} will also be disabled
     * if the {@link CacheFlag#getRequiredIntent() required intent} is not enabled.
     *
     * @param token
     *        The bot token to use
     * @param intent
     *        The first gateway intent to use
     * @param intents
     *        Additional gateway intents to use
     *
     * @throws IllegalArgumentException
     *         If the provided intents are null
     *
     * @return The DefaultShardManagerBuilder instance
     *
     * @see   #setToken(String)
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder create(@Nullable String token, @Nonnull GatewayIntent intent, @Nonnull GatewayIntent... intents)
    {
        return new DefaultShardManagerBuilder(token, GatewayIntent.getRaw(intent, intents)).applyIntents();
    }

    /**
     * Creates a DefaultShardManagerBuilder with the predefined token.
     *
     * <p>If you don't enable certain intents, the cache will be disabled.
     * For instance, if the {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent is disabled, then members will only
     * be cached when a voice state is available.
     * If both {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} and {@link GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} are disabled
     * then no members will be cached.
     *
     * <p>The individual {@link CacheFlag CacheFlags} will also be disabled
     * if the {@link CacheFlag#getRequiredIntent() required intent} is not enabled.
     *
     * @param token
     *        The bot token to use
     * @param intents
     *        The gateway intents to use
     *
     * @throws IllegalArgumentException
     *         If the provided intents are null
     *
     * @return The DefaultShardManagerBuilder instance
     *
     * @see   #setToken(String)
     */
    @Nonnull
    @CheckReturnValue
    public static DefaultShardManagerBuilder create(@Nullable String token, @Nonnull Collection<GatewayIntent> intents)
    {
        return new DefaultShardManagerBuilder(token, GatewayIntent.getRaw(intents)).applyIntents();
    }

    private DefaultShardManagerBuilder applyIntents()
    {
        EnumSet<CacheFlag> disabledCache = EnumSet.allOf(CacheFlag.class);
        for (CacheFlag flag : CacheFlag.values())
        {
            GatewayIntent requiredIntent = flag.getRequiredIntent();
            if (requiredIntent == null || (requiredIntent.getRawValue() & intents) != 0)
                disabledCache.remove(flag);
        }

        boolean enableMembers = (intents & GatewayIntent.GUILD_MEMBERS.getRawValue()) != 0;
        return setChunkingFilter(enableMembers ? ChunkingFilter.ALL : ChunkingFilter.NONE)
                .setMemberCachePolicy(enableMembers ? MemberCachePolicy.ALL : MemberCachePolicy.DEFAULT)
                .setDisabledCache(disabledCache);
    }

    private DefaultShardManagerBuilder setDisabledCache(EnumSet<CacheFlag> flags)
    {
        this.disableCache(flags);
        this.automaticallyDisabled.addAll(flags);
        return this;
    }

    /**
     * Choose which {@link GatewayEncoding} JDA should use.
     *
     * @param  encoding
     *         The {@link GatewayEncoding} (default: JSON)
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.2.1
     */
    @Nonnull
    public DefaultShardManagerBuilder setGatewayEncoding(@Nonnull GatewayEncoding encoding)
    {
        Checks.notNull(encoding, "GatewayEncoding");
        this.encoding = encoding;
        return this;
    }

    /**
     * Whether JDA should fire {@link net.dv8tion.jda.api.events.RawGatewayEvent} for every discord event.
     * <br>Default: {@code false}
     *
     * @param  enable
     *         True, if JDA should fire {@link net.dv8tion.jda.api.events.RawGatewayEvent}.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setRawEventsEnabled(boolean enable)
    {
        return setFlag(ConfigFlag.RAW_EVENTS, enable);
    }

    /**
     * Whether the rate-limit should be relative to the current time plus latency.
     * <br>By default we use the {@code X-RateLimit-Rest-After} header to determine when
     * a rate-limit is no longer imminent. This has the disadvantage that it might wait longer than needed due
     * to the latency which is ignored by the reset-after relative delay.
     *
     * <p>When disabled, we will use the {@code X-RateLimit-Reset} absolute timestamp instead which accounts for
     * latency but requires a properly NTP synchronized clock to be present.
     * If your system does have this feature you might gain a little quicker rate-limit handling than the default allows.
     *
     * <p>Default: <b>true</b>
     *
     * @param  enable
     *         True, if the relative {@code X-RateLimit-Reset-After} header should be used.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.1.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setRelativeRateLimit(boolean enable)
    {
        return setFlag(ConfigFlag.USE_RELATIVE_RATELIMIT, enable);
    }

    /**
     * Enable specific cache flags.
     * <br>This will not disable any currently set cache flags.
     *
     * @param  flags
     *         The {@link CacheFlag CacheFlags} to enable
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #enableCache(CacheFlag, CacheFlag...)
     * @see    #disableCache(Collection)
     */
    @Nonnull
    public DefaultShardManagerBuilder enableCache(@Nonnull Collection<CacheFlag> flags)
    {
        Checks.noneNull(flags, "CacheFlags");
        cacheFlags.addAll(flags);
        return this;
    }

    /**
     * Enable specific cache flags.
     * <br>This will not disable any currently set cache flags.
     *
     * @param  flag
     *         {@link CacheFlag} to enable
     * @param  flags
     *         Other flags to enable
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #enableCache(Collection)
     * @see    #disableCache(CacheFlag, CacheFlag...)
     */
    @Nonnull
    public DefaultShardManagerBuilder enableCache(@Nonnull CacheFlag flag, @Nonnull CacheFlag... flags)
    {
        Checks.notNull(flag, "CacheFlag");
        Checks.noneNull(flags, "CacheFlag");
        cacheFlags.addAll(EnumSet.of(flag, flags));
        return this;
    }

    /**
     * Disable specific cache flags.
     * <br>This will not enable any currently unset cache flags.
     *
     * @param  flags
     *         The {@link CacheFlag CacheFlags} to disable
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #disableCache(CacheFlag, CacheFlag...)
     * @see    #enableCache(Collection)
     */
    @Nonnull
    public DefaultShardManagerBuilder disableCache(@Nonnull Collection<CacheFlag> flags)
    {
        Checks.noneNull(flags, "CacheFlags");
        automaticallyDisabled.removeAll(flags);
        cacheFlags.removeAll(flags);
        return this;
    }

    /**
     * Disable specific cache flags.
     * <br>This will not enable any currently unset cache flags.
     *
     * @param  flag
     *         {@link CacheFlag} to disable
     * @param  flags
     *         Other flags to disable
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #disableCache(Collection)
     * @see    #enableCache(CacheFlag, CacheFlag...)
     */
    @Nonnull
    public DefaultShardManagerBuilder disableCache(@Nonnull CacheFlag flag, @Nonnull CacheFlag... flags)
    {
        Checks.notNull(flag, "CacheFlag");
        Checks.noneNull(flags, "CacheFlag");
        return disableCache(EnumSet.of(flag, flags));
    }

    /**
     * Configure the member caching policy.
     * This will decide whether to cache a member (and its respective user).
     * <br>All members are cached by default. If a guild is enabled for chunking, all members will be cached for it.
     *
     * <p>You can use this to define a custom caching policy that will greatly improve memory usage.
     * <p>It is not recommended to disable {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} when
     * using {@link MemberCachePolicy#ALL MemberCachePolicy.ALL} as the members cannot be removed from cache by a leave event without this intent.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * public void configureCache(DefaultShardManagerBuilder builder) {
     *     // Cache members who are in a voice channel
     *     MemberCachePolicy policy = MemberCachePolicy.VOICE;
     *     // Cache members who are in a voice channel
     *     // AND are also online
     *     policy = policy.and(MemberCachePolicy.ONLINE);
     *     // Cache members who are in a voice channel
     *     // AND are also online
     *     // OR are the owner of the guild
     *     policy = policy.or(MemberCachePolicy.OWNER);
     *     // Cache members who have a role with the name "Moderator"
     *     policy = (member) -> member.getRoles().stream().map(Role::getName).anyMatch("Moderator"::equals);
     *
     *     builder.setMemberCachePolicy(policy);
     * }
     * }</pre>
     *
     * @param  policy
     *         The {@link MemberCachePolicy} or null to use default {@link MemberCachePolicy#ALL}
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    MemberCachePolicy
     * @see    #setEnabledIntents(Collection)
     *
     * @since  4.2.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setMemberCachePolicy(@Nullable MemberCachePolicy policy)
    {
        if (policy == null)
            this.memberCachePolicy = MemberCachePolicy.ALL;
        else
            this.memberCachePolicy = policy;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.api.utils.SessionController SessionController}
     * for the resulting ShardManager instance. This can be used to sync behaviour and state between shards
     * of a bot and should be one and the same instance on all builders for the shards.
     *
     * @param  controller
     *         The {@link net.dv8tion.jda.api.utils.SessionController SessionController} to use
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.utils.SessionControllerAdapter SessionControllerAdapter
     */
    @Nonnull
    public DefaultShardManagerBuilder setSessionController(@Nullable SessionController controller)
    {
        this.sessionController = controller;
        return this;
    }

    /**
     * Configures a custom voice dispatch handler which handles audio connections.
     *
     * @param  interceptor
     *         The new voice dispatch handler, or null to use the default
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     *
     * @see    VoiceDispatchInterceptor
     */
    @Nonnull
    public DefaultShardManagerBuilder setVoiceDispatchInterceptor(@Nullable VoiceDispatchInterceptor interceptor)
    {
        this.voiceDispatchInterceptor = interceptor;
        return this;
    }

    /**
     * Sets the {@link org.slf4j.MDC MDC} mappings provider to use in JDA.
     * <br>If sharding is enabled JDA will automatically add a {@code jda.shard} context with the format {@code [SHARD_ID / TOTAL]}
     * where {@code SHARD_ID} and {@code TOTAL} are the shard configuration.
     * Additionally it will provide context for the id via {@code jda.shard.id} and the total via {@code jda.shard.total}.
     *
     * <p><b>The manager will call this with a shardId and it is recommended to provide a different context map for each shard!</b>
     * <br>This automatically switches {@link #setContextEnabled(boolean)} to true if the provided function is not null!
     *
     * @param  provider
     *         The provider for <b>modifiable</b> context maps to use in JDA, or {@code null} to reset
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    <a href="https://www.slf4j.org/api/org/slf4j/MDC.html" target="_blank">MDC Javadoc</a>
     */
    @Nonnull
    public DefaultShardManagerBuilder setContextMap(@Nullable IntFunction<? extends ConcurrentMap<String, String>> provider)
    {
        this.contextProvider = provider;
        if (provider != null)
            setContextEnabled(true);
        return this;
    }

    /**
     * Whether JDA should use a synchronized MDC context for all of its controlled threads.
     * <br>Default: {@code true}
     *
     * @param  enable
     *         True, if JDA should provide an MDC context map
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    <a href="https://www.slf4j.org/api/org/slf4j/MDC.html" target="_blank">MDC Javadoc</a>
     * @see    #setContextMap(java.util.function.IntFunction)
     */
    @Nonnull
    public DefaultShardManagerBuilder setContextEnabled(boolean enable)
    {
        return setFlag(ConfigFlag.MDC_CONTEXT, enable);
    }

    /**
     * Sets the compression algorithm used with the gateway connection,
     * this will decrease the amount of used bandwidth for the running bot instance
     * for the cost of a few extra cycles for decompression.
     * Compression can be entirely disabled by setting this to {@link net.dv8tion.jda.api.utils.Compression#NONE}.
     * <br><b>Default: {@link net.dv8tion.jda.api.utils.Compression#ZLIB}</b>
     *
     * <p><b>We recommend to keep this on the default unless you have issues with the decompression</b>
     * <br>This mode might become obligatory in a future version, do not rely on this switch to stay.
     *
     * @param  compression
     *         The compression algorithm to use for the gateway connection
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    <a href="https://discord.com/developers/docs/topics/gateway#transport-compression" target="_blank">Official Discord Documentation - Transport Compression</a>
     */
    @Nonnull
    public DefaultShardManagerBuilder setCompression(@Nonnull Compression compression)
    {
        Checks.notNull(compression, "Compression");
        this.compression = compression;
        return this;
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the {@link DefaultShardManager DefaultShardManager} object.
     * <br>This uses the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.api.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManagerProvider(IntFunction) setEventManagerProvider(id -> new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) to add to the list.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    DefaultShardManager#addEventListener(Object...) JDA.addEventListeners(Object...)
     */
    @Nonnull
    public DefaultShardManagerBuilder addEventListeners(@Nonnull final Object... listeners)
    {
        return this.addEventListeners(Arrays.asList(listeners));
    }

    /**
     * Adds all provided listeners to the list of listeners that will be used to populate the {@link DefaultShardManager DefaultShardManager} object.
     * <br>This uses the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.api.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManagerProvider(IntFunction) setEventManager(id -> new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
     *
     * @param  listeners
     *         The listener(s) to add to the list.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    DefaultShardManager#addEventListener(Object...) JDA.addEventListeners(Object...)
     */
    @Nonnull
    public DefaultShardManagerBuilder addEventListeners(@Nonnull final Collection<Object> listeners)
    {
        Checks.noneNull(listeners, "listeners");

        this.listeners.addAll(listeners);
        return this;
    }

    /**
     * Removes all provided listeners from the list of listeners.
     *
     * @param  listeners
     *         The listener(s) to remove from the list.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.JDA#removeEventListener(Object...) JDA.removeEventListeners(Object...)
     */
    @Nonnull
    public DefaultShardManagerBuilder removeEventListeners(@Nonnull final Object... listeners)
    {
        return this.removeEventListeners(Arrays.asList(listeners));
    }

    /**
     * Removes all provided listeners from the list of listeners.
     *
     * @param  listeners
     *         The listener(s) to remove from the list.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.JDA#removeEventListener(Object...) JDA.removeEventListeners(Object...)
     */
    @Nonnull
    public DefaultShardManagerBuilder removeEventListeners(@Nonnull final Collection<Object> listeners)
    {
        Checks.noneNull(listeners, "listeners");

        this.listeners.removeAll(listeners);
        return this;
    }

    /**
     * Adds the provided listener provider to the list of listener providers that will be used to create listeners.
     * On shard creation (including shard restarts) the provider will have the shard id applied and must return a listener,
     * which will be used, along all other listeners, to populate the listeners of the JDA object of that shard.
     *
     * <br>This uses the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.api.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManagerProvider(IntFunction) setEventManager(id -> new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
     *
     * @param  listenerProvider
     *         The listener provider to add to the list of listener providers.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder addEventListenerProvider(@Nonnull final IntFunction<Object> listenerProvider)
    {
        return this.addEventListenerProviders(Collections.singleton(listenerProvider));
    }

    /**
     * Adds the provided listener providers to the list of listener providers that will be used to create listeners.
     * On shard creation (including shard restarts) each provider will have the shard id applied and must return a listener,
     * which will be used, along all other listeners, to populate the listeners of the JDA object of that shard.
     *
     * <br>This uses the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * <br>To switch to the {@link net.dv8tion.jda.api.hooks.AnnotatedEventManager AnnotatedEventManager},
     * use {@link #setEventManagerProvider(IntFunction) setEventManager(id -> new AnnotatedEventManager())}.
     *
     * <p><b>Note:</b> When using the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener(s) <b>must</b> be instance of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
     *
     * @param  listenerProviders
     *         The listener provider to add to the list of listener providers.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder addEventListenerProviders(@Nonnull final Collection<IntFunction<Object>> listenerProviders)
    {
        Checks.noneNull(listenerProviders, "listener providers");

        this.listenerProviders.addAll(listenerProviders);
        return this;
    }

    /**
     * Removes the provided listener provider from the list of listener providers.
     *
     * @param  listenerProvider
     *         The listener provider to remove from the list of listener providers.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder removeEventListenerProvider(@Nonnull final IntFunction<Object> listenerProvider)
    {
        return this.removeEventListenerProviders(Collections.singleton(listenerProvider));
    }

    /**
     * Removes all provided listener providers from the list of listener providers.
     *
     * @param  listenerProviders
     *         The listener provider(s) to remove from the list of listener providers.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder removeEventListenerProviders(@Nonnull final Collection<IntFunction<Object>> listenerProviders)
    {
        Checks.noneNull(listenerProviders, "listener providers");

        this.listenerProviders.removeAll(listenerProviders);
        return this;
    }

    /**
     * Changes the factory used to create {@link net.dv8tion.jda.api.audio.factory.IAudioSendSystem IAudioSendSystem}
     * objects which handle the sending loop for audio packets.
     * <br>By default, JDA uses {@link net.dv8tion.jda.api.audio.factory.DefaultSendFactory DefaultSendFactory}.
     *
     * @param  factory
     *         The new {@link net.dv8tion.jda.api.audio.factory.IAudioSendFactory IAudioSendFactory} to be used
     *         when creating new {@link net.dv8tion.jda.api.audio.factory.IAudioSendSystem} objects.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setAudioSendFactory(@Nullable final IAudioSendFactory factory)
    {
        this.audioSendFactory = factory;
        return this;
    }

    /**
     * Sets whether or not JDA should try to reconnect if a connection-error is encountered.
     * <br>This will use an incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * Default: <b>true (enabled)</b>
     *
     * @param  autoReconnect
     *         If true - enables autoReconnect
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setAutoReconnect(final boolean autoReconnect)
    {
        return setFlag(ConfigFlag.AUTO_RECONNECT, autoReconnect);
    }

    /**
     * If enabled, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the {@link net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent}.
     *
     * <p>Default: <b>true (enabled)</b>
     *
     * @param  enabled
     *         True - The MESSAGE_DELETE_BULK will be split into multiple individual MessageDeleteEvents.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setBulkDeleteSplittingEnabled(final boolean enabled)
    {
        return setFlag(ConfigFlag.BULK_DELETE_SPLIT, enabled);
    }

    /**
     * Enables/Disables the use of a Shutdown hook to clean up the ShardManager and it's JDA instances.
     * <br>When the Java program closes shutdown hooks are run. This is used as a last-second cleanup
     * attempt by JDA to properly close connections.
     *
     * <p>Default: <b>true (enabled)</b>
     *
     * @param  enable
     *         True (default) - use shutdown hook to clean up the ShardManager and it's JDA instances if the Java program is closed.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setEnableShutdownHook(final boolean enable)
    {
        return setFlag(ConfigFlag.SHUTDOWN_HOOK, enable);
    }

    /**
     * Sets a provider to change the internally used EventManager.
     * <br>There are 2 provided Implementations:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.hooks.InterfacedEventManager InterfacedEventManager} which uses the Interface
     *     {@link net.dv8tion.jda.api.hooks.EventListener EventListener} (tip: use the {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter}).
     *     <br>This is the default EventManager.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.hooks.AnnotatedEventManager AnnotatedEventManager} which uses the Annotation
     *         {@link net.dv8tion.jda.api.hooks.SubscribeEvent @SubscribeEvent} to mark the methods that listen for events.</li>
     * </ul>
     * <br>You can also create your own EventManager (See {@link net.dv8tion.jda.api.hooks.IEventManager}).
     *
     * @param  eventManagerProvider
     *         A supplier for the new {@link net.dv8tion.jda.api.hooks.IEventManager} to use.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setEventManagerProvider(@Nonnull final IntFunction<? extends IEventManager> eventManagerProvider)
    {
        Checks.notNull(eventManagerProvider, "eventManagerProvider");
        this.eventManagerProvider = eventManagerProvider;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Activity Activity} for our session.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.api.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Hint:</b> You can create an {@link net.dv8tion.jda.api.entities.Activity Activity} object using
     * {@link net.dv8tion.jda.api.entities.Activity#playing(String) Activity.playing(String)} or
     * {@link net.dv8tion.jda.api.entities.Activity#streaming(String, String)} Activity.streaming(String, String)}.
     *
     * @param  activity
     *         An instance of {@link net.dv8tion.jda.api.entities.Activity Activity} (null allowed)
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.managers.Presence#setActivity(net.dv8tion.jda.api.entities.Activity)
     */
    @Nonnull
    public DefaultShardManagerBuilder setActivity(@Nullable final Activity activity)
    {
        return this.setActivityProvider(id -> activity);
    }

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Activity Activity} for our session.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.api.managers.Presence Presence} from a JDA instance.
     *
     * <p><b>Hint:</b> You can create an {@link net.dv8tion.jda.api.entities.Activity Activity} object using
     * {@link net.dv8tion.jda.api.entities.Activity#playing(String) Activity.playing(String)} or
     * {@link net.dv8tion.jda.api.entities.Activity#streaming(String, String) Activity.streaming(String, String)}.
     *
     * @param  activityProvider
     *         An instance of {@link net.dv8tion.jda.api.entities.Activity Activity} (null allowed)
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.managers.Presence#setActivity(net.dv8tion.jda.api.entities.Activity)
     */
    @Nonnull
    public DefaultShardManagerBuilder setActivityProvider(@Nullable final IntFunction<? extends Activity> activityProvider)
    {
        this.activityProvider = activityProvider;
        return this;
    }

    /**
     * Sets whether or not we should mark our sessions as afk
     * <br>This value can be changed at any time using
     * {@link DefaultShardManager#setIdle(boolean) DefaultShardManager#setIdleProvider(boolean)}.
     *
     * @param  idle
     *         boolean value that will be provided with our IDENTIFY packages to mark our sessions as afk or not. <b>(default false)</b>
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.managers.Presence#setIdle(boolean)
     */
    @Nonnull
    public DefaultShardManagerBuilder setIdle(final boolean idle)
    {
        return this.setIdleProvider(id -> idle);
    }

    /**
     * Sets whether or not we should mark our sessions as afk
     * <br>This value can be changed at any time using
     * {@link DefaultShardManager#setIdle(boolean) DefaultShardManager#setIdleProvider(boolean)}.
     *
     * @param  idleProvider
     *         boolean value that will be provided with our IDENTIFY packages to mark our sessions as afk or not. <b>(default false)</b>
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.managers.Presence#setIdle(boolean)
     */
    @Nonnull
    public DefaultShardManagerBuilder setIdleProvider(@Nullable final IntFunction<Boolean> idleProvider)
    {
        this.idleProvider = idleProvider;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} our connection will display.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.api.managers.Presence Presence} from a JDA instance.
     *
     * @param  status
     *         Not-null OnlineStatus (default online)
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is null or {@link net.dv8tion.jda.api.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.managers.Presence#setStatus(OnlineStatus) Presence.setStatusProvider(OnlineStatus)
     */
    @Nonnull
    public DefaultShardManagerBuilder setStatus(@Nullable final OnlineStatus status)
    {
        Checks.notNull(status, "status");
        Checks.check(status != OnlineStatus.UNKNOWN, "OnlineStatus cannot be unknown!");

        return this.setStatusProvider(id -> status);
    }

    /**
     * Sets the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} our connection will display.
     * <br>This value can be changed at any time in the {@link net.dv8tion.jda.api.managers.Presence Presence} from a JDA instance.
     *
     * @param  statusProvider
     *         Not-null OnlineStatus (default online)
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is null or {@link net.dv8tion.jda.api.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    net.dv8tion.jda.api.managers.Presence#setStatus(OnlineStatus) Presence.setStatusProvider(OnlineStatus)
     */
    @Nonnull
    public DefaultShardManagerBuilder setStatusProvider(@Nullable final IntFunction<OnlineStatus> statusProvider)
    {
        this.statusProvider = statusProvider;
        return this;
    }

    /**
     * Sets the {@link java.util.concurrent.ThreadFactory ThreadFactory} that will be used by the internal executor
     * of the ShardManager.
     * <p>Note: This will not affect Threads created by any JDA instance.
     *
     * @param  threadFactory
     *         The ThreadFactory or {@code null} to reset to the default value.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setThreadFactory(@Nullable final ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * Sets the {@link okhttp3.OkHttpClient.Builder Builder} that will be used by JDA's requester.
     * This can be used to set things such as connection timeout and proxy.
     *
     * @param  builder
     *         The new {@link okhttp3.OkHttpClient.Builder OkHttpClient.Builder} to use.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setHttpClientBuilder(@Nullable OkHttpClient.Builder builder)
    {
        this.httpClientBuilder = builder;
        return this;
    }

    /**
     * Sets the {@link okhttp3.OkHttpClient OkHttpClient} that will be used by JDAs requester.
     * <br>This can be used to set things such as connection timeout and proxy.
     *
     * @param  client
     *         The new {@link okhttp3.OkHttpClient OkHttpClient} to use
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setHttpClient(@Nullable OkHttpClient client)
    {
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. <b>Only change this pool if you know what you're doing.</b>
     * <br>This will override the rate-limit pool provider set from {@link #setRateLimitPoolProvider(ThreadPoolProvider)}.
     * <br><b>This automatically disables the automatic shutdown of the rate-limit pool, you can enable
     * it using {@link #setRateLimitPool(ScheduledExecutorService, boolean) setRateLimiPool(executor, true)}</b>
     *
     * <p>This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for {@link net.dv8tion.jda.api.requests.RestAction#queueAfter(long, TimeUnit)}
     * and similar methods.
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 5 threads (per shard).
     *
     * @param  pool
     *         The thread-pool to use for rate-limit handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setRateLimitPool(@Nullable ScheduledExecutorService pool)
    {
        return setRateLimitPool(pool, pool == null);
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. <b>Only change this pool if you know what you're doing.</b>
     * <br>This will override the rate-limit pool provider set from {@link #setRateLimitPoolProvider(ThreadPoolProvider)}.
     *
     * <p>This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for {@link net.dv8tion.jda.api.requests.RestAction#queueAfter(long, TimeUnit)}
     * and similar methods.
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 5 threads (per shard).
     *
     * @param  pool
     *         The thread-pool to use for rate-limit handling
     * @param  automaticShutdown
     *         Whether {@link net.dv8tion.jda.api.JDA#shutdown()} should automatically shutdown this pool
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setRateLimitPool(@Nullable ScheduledExecutorService pool, boolean automaticShutdown)
    {
        return setRateLimitPoolProvider(pool == null ? null : new ThreadPoolProviderImpl<>(pool, automaticShutdown));
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} provider that should be used in
     * the JDA rate-limit handler. Changing this can drastically change the JDA behavior for RestAction execution
     * and should be handled carefully. <b>Only change this pool if you know what you're doing.</b>
     *
     * <p>This is used mostly by the Rate-Limiter to handle backoff delays by using scheduled executions.
     * Besides that it is also used by planned execution for {@link net.dv8tion.jda.api.requests.RestAction#queueAfter(long, TimeUnit)}
     * and similar methods.
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 5 threads (per shard).
     *
     * @param  provider
     *         The thread-pool provider to use for rate-limit handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setRateLimitPoolProvider(@Nullable ThreadPoolProvider<? extends ScheduledExecutorService> provider)
    {
        this.rateLimitPoolProvider = provider;
        return this;
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} that should be used for
     * the JDA main WebSocket workers.
     * <br><b>Only change this pool if you know what you're doing.</b>
     * <br>This will override the worker pool provider set from {@link #setGatewayPoolProvider(ThreadPoolProvider)}.
     * <br><b>This automatically disables the automatic shutdown of the main-ws pools, you can enable
     * it using {@link #setGatewayPool(ScheduledExecutorService, boolean) setGatewayPoolProvider(pool, true)}</b>
     *
     * <p>This is used to send various forms of session updates such as:
     * <ul>
     *     <li>Voice States - (Dis-)Connecting from channels</li>
     *     <li>Presence - Changing current activity or online status</li>
     *     <li>Guild Setup - Requesting Members of newly joined guilds</li>
     *     <li>Heartbeats - Regular updates to keep the connection alive (usually once a minute)</li>
     * </ul>
     * When nothing has to be sent the pool will only be used every 500 milliseconds to check the queue for new payloads.
     * Once a new payload is sent we switch to "rapid mode" which means more tasks will be submitted until no more payloads
     * have to be sent.
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 1 thread (per shard)
     *
     * @param  pool
     *         The thread-pool to use for main WebSocket workers
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setGatewayPool(@Nullable ScheduledExecutorService pool)
    {
        return setGatewayPool(pool, pool == null);
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} that should be used for
     * the JDA main WebSocket workers.
     * <br><b>Only change this pool if you know what you're doing.</b>
     * <br>This will override the worker pool provider set from {@link #setGatewayPoolProvider(ThreadPoolProvider)}.
     *
     * <p>This is used to send various forms of session updates such as:
     * <ul>
     *     <li>Voice States - (Dis-)Connecting from channels</li>
     *     <li>Presence - Changing current activity or online status</li>
     *     <li>Guild Setup - Requesting Members of newly joined guilds</li>
     *     <li>Heartbeats - Regular updates to keep the connection alive (usually once a minute)</li>
     * </ul>
     * When nothing has to be sent the pool will only be used every 500 milliseconds to check the queue for new payloads.
     * Once a new payload is sent we switch to "rapid mode" which means more tasks will be submitted until no more payloads
     * have to be sent.
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 1 thread (per shard)
     *
     * @param  pool
     *         The thread-pool to use for main WebSocket workers
     * @param  automaticShutdown
     *         Whether {@link net.dv8tion.jda.api.JDA#shutdown()} should automatically shutdown this pool
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setGatewayPool(@Nullable ScheduledExecutorService pool, boolean automaticShutdown)
    {
        return setGatewayPoolProvider(pool == null ? null : new ThreadPoolProviderImpl<>(pool, automaticShutdown));
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} that should be used for
     * the JDA main WebSocket workers.
     * <br><b>Only change this pool if you know what you're doing.</b>
     *
     * <p>This is used to send various forms of session updates such as:
     * <ul>
     *     <li>Voice States - (Dis-)Connecting from channels</li>
     *     <li>Presence - Changing current activity or online status</li>
     *     <li>Guild Setup - Requesting Members of newly joined guilds</li>
     *     <li>Heartbeats - Regular updates to keep the connection alive (usually once a minute)</li>
     * </ul>
     * When nothing has to be sent the pool will only be used every 500 milliseconds to check the queue for new payloads.
     * Once a new payload is sent we switch to "rapid mode" which means more tasks will be submitted until no more payloads
     * have to be sent.
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 1 thread (per shard)
     *
     * @param  provider
     *         The thread-pool provider to use for main WebSocket workers
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setGatewayPoolProvider(@Nullable ThreadPoolProvider<? extends ScheduledExecutorService> provider)
    {
        this.gatewayPoolProvider = provider;
        return this;
    }

    /**
     * Sets the {@link ExecutorService ExecutorService} that should be used in
     * the JDA callback handler which mostly consists of {@link net.dv8tion.jda.api.requests.RestAction RestAction} callbacks.
     * By default JDA will use {@link ForkJoinPool#commonPool()}
     * <br><b>Only change this pool if you know what you're doing.
     * <br>This automatically disables the automatic shutdown of the callback pools, you can enable
     * it using {@link #setCallbackPool(ExecutorService, boolean) setCallbackPool(executor, true)}</b>
     *
     * <p>This is used to handle callbacks of {@link RestAction#queue()}, similarly it is used to
     * finish {@link RestAction#submit()} and {@link RestAction#complete()} tasks which build on queue.
     *
     * <p>Default: {@link ForkJoinPool#commonPool()}
     *
     * @param  executor
     *         The thread-pool to use for callback handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setCallbackPool(@Nullable ExecutorService executor)
    {
        return setCallbackPool(executor, executor == null);
    }

    /**
     * Sets the {@link ExecutorService ExecutorService} that should be used in
     * the JDA callback handler which mostly consists of {@link net.dv8tion.jda.api.requests.RestAction RestAction} callbacks.
     * By default JDA will use {@link ForkJoinPool#commonPool()}
     * <br><b>Only change this pool if you know what you're doing.</b>
     *
     * <p>This is used to handle callbacks of {@link RestAction#queue()}, similarly it is used to
     * finish {@link RestAction#submit()} and {@link RestAction#complete()} tasks which build on queue.
     *
     * <p>Default: {@link ForkJoinPool#commonPool()}
     *
     * @param  executor
     *         The thread-pool to use for callback handling
     * @param  automaticShutdown
     *         Whether {@link net.dv8tion.jda.api.JDA#shutdown()} should automatically shutdown this pool
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setCallbackPool(@Nullable ExecutorService executor, boolean automaticShutdown)
    {
        return setCallbackPoolProvider(executor == null ? null : new ThreadPoolProviderImpl<>(executor, automaticShutdown));
    }

    /**
     * Sets the {@link ExecutorService ExecutorService} that should be used in
     * the JDA callback handler which mostly consists of {@link net.dv8tion.jda.api.requests.RestAction RestAction} callbacks.
     * By default JDA will use {@link ForkJoinPool#commonPool()}
     * <br><b>Only change this pool if you know what you're doing.</b>
     *
     * <p>This is used to handle callbacks of {@link RestAction#queue()}, similarly it is used to
     * finish {@link RestAction#submit()} and {@link RestAction#complete()} tasks which build on queue.
     *
     * <p>Default: {@link ForkJoinPool#commonPool()}
     *
     * @param  provider
     *         The thread-pool provider to use for callback handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setCallbackPoolProvider(@Nullable ThreadPoolProvider<? extends ExecutorService> provider)
    {
        this.callbackPoolProvider = provider;
        return this;
    }

    /**
     * Sets the {@link ExecutorService ExecutorService} that should be used by the
     * event proxy to schedule events. This will be done on the calling thread by default.
     *
     * <p>The executor will not be shutdown automatically when the shard is shutdown.
     * To shut it down automatically use {@link #setEventPool(ExecutorService, boolean)}.
     *
     * @param  executor
     *         The executor for the event proxy, or null to use calling thread
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.2.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setEventPool(@Nullable ExecutorService executor)
    {
        return setEventPool(executor, executor == null);
    }

    /**
     * Sets the {@link ExecutorService ExecutorService} that should be used by the
     * event proxy to schedule events. This will be done on the calling thread by default.
     *
     * @param  executor
     *         The executor for the event proxy, or null to use calling thread
     * @param  automaticShutdown
     *         True, if the executor should be shutdown when JDA shuts down
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.2.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setEventPool(@Nullable ExecutorService executor, boolean automaticShutdown)
    {
        return setEventPoolProvider(executor == null ? null : new ThreadPoolProviderImpl<>(executor, automaticShutdown));
    }

    /**
     * Sets the {@link ExecutorService ExecutorService} that should be used in
     * the JDA callback handler which mostly consists of {@link net.dv8tion.jda.api.requests.RestAction RestAction} callbacks.
     * By default JDA will use {@link ForkJoinPool#commonPool()}
     * <br><b>Only change this pool if you know what you're doing.</b>
     *
     * <p>This is used to handle callbacks of {@link RestAction#queue()}, similarly it is used to
     * finish {@link RestAction#submit()} and {@link RestAction#complete()} tasks which build on queue.
     *
     * <p>Default: {@link ForkJoinPool#commonPool()}
     *
     * @param  provider
     *         The thread-pool provider to use for callback handling
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.2.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setEventPoolProvider(@Nullable ThreadPoolProvider<? extends ExecutorService> provider)
    {
        this.eventPoolProvider = provider;
        return this;
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} used by
     * the audio WebSocket connection. Used for sending keepalives and closing the connection.
     * <br><b>Only change this pool if you know what you're doing.</b>
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 1 thread
     *
     * @param  pool
     *         The thread-pool to use for the audio WebSocket
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since 4.2.1
     */
    @Nonnull
    public DefaultShardManagerBuilder setAudioPool(@Nullable ScheduledExecutorService pool)
    {
        return setAudioPool(pool, pool == null);
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} used by
     * the audio WebSocket connection. Used for sending keepalives and closing the connection.
     * <br><b>Only change this pool if you know what you're doing.</b>
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 1 thread
     *
     * @param  pool
     *         The thread-pool to use for the audio WebSocket
     * @param  automaticShutdown
     *         True, if the executor should be shutdown when JDA shuts down
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since 4.2.1
     */
    @Nonnull
    public DefaultShardManagerBuilder setAudioPool(@Nullable ScheduledExecutorService pool, boolean automaticShutdown)
    {
        return setAudioPoolProvider(pool == null ? null : new ThreadPoolProviderImpl<>(pool, automaticShutdown));
    }

    /**
     * Sets the {@link ScheduledExecutorService ScheduledExecutorService} used by
     * the audio WebSocket connection. Used for sending keepalives and closing the connection.
     * <br><b>Only change this pool if you know what you're doing.</b>
     *
     * <p>Default: {@link ScheduledThreadPoolExecutor} with 1 thread
     *
     * @param  provider
     *         The thread-pool provider to use for the audio WebSocket
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since 4.2.1
     */
    @Nonnull
    public DefaultShardManagerBuilder setAudioPoolProvider(@Nullable ThreadPoolProvider<? extends ScheduledExecutorService> provider)
    {
        this.audioPoolProvider = provider;
        return this;
    }

    /**
     * Sets the maximum amount of time that JDA will back off to wait when attempting to reconnect the MainWebsocket.
     * <br>Provided value must be 32 or greater.
     *
     * <p>Default: {@code 900}
     *
     * @param  maxReconnectDelay
     *         The maximum amount of time that JDA will wait between reconnect attempts in seconds.
     *
     * @throws java.lang.IllegalArgumentException
     *         Thrown if the provided {@code maxReconnectDelay} is less than 32.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setMaxReconnectDelay(final int maxReconnectDelay)
    {
        Checks.check(maxReconnectDelay >= 32, "Max reconnect delay must be 32 seconds or greater. You provided %d.", maxReconnectDelay);

        this.maxReconnectDelay = maxReconnectDelay;
        return this;
    }

    /**
     * Whether the Requester should retry when
     * a {@link java.net.SocketTimeoutException SocketTimeoutException} occurs.
     * <br><b>Default</b>: {@code true}
     *
     * <p>This value can be changed at any time with {@link net.dv8tion.jda.api.JDA#setRequestTimeoutRetry(boolean) JDA.setRequestTimeoutRetry(boolean)}!
     *
     * @param  retryOnTimeout
     *         True, if the Request should retry once on a socket timeout
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setRequestTimeoutRetry(boolean retryOnTimeout)
    {
        return setFlag(ConfigFlag.RETRY_TIMEOUT, retryOnTimeout);
    }

    /**
     * Sets the list of shards the {@link DefaultShardManager DefaultShardManager} should contain.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  shardIds
     *         The list of shard ids
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setShards(final int... shardIds)
    {
        Checks.notNull(shardIds, "shardIds");
        for (int id : shardIds)
        {
            Checks.notNegative(id, "minShardId");
            Checks.check(id < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        }

        this.shards = Arrays.stream(shardIds).boxed().collect(Collectors.toSet());

        return this;
    }

    /**
     * Sets the range of shards the {@link DefaultShardManager DefaultShardManager} should contain.
     * This is useful if you want to split your shards between multiple JVMs or servers.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  minShardId
     *         The lowest shard id the DefaultShardManager should contain
     *
     * @param  maxShardId
     *         The highest shard id the DefaultShardManager should contain
     *
     * @throws IllegalArgumentException
     *         If either minShardId is negative, maxShardId is lower than shardsTotal or
     *         minShardId is lower than or equal to maxShardId
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setShards(final int minShardId, final int maxShardId)
    {
        Checks.notNegative(minShardId, "minShardId");
        Checks.check(maxShardId < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        Checks.check(minShardId <= maxShardId, "minShardId must be lower than or equal to maxShardId");

        List<Integer> shards = new ArrayList<>(maxShardId - minShardId + 1);
        for (int i = minShardId; i <= maxShardId; i++)
            shards.add(i);

        this.shards = shards;

        return this;
    }

    /**
     * Sets the range of shards the {@link DefaultShardManager DefaultShardManager} should contain.
     * This is useful if you want to split your shards between multiple JVMs or servers.
     *
     * <p><b>This does not have any effect if the total shard count is set to {@code -1} (get recommended shards from discord).</b>
     *
     * @param  shardIds
     *         The list of shard ids
     *
     * @throws IllegalArgumentException
     *         If either minShardId is negative, maxShardId is lower than shardsTotal or
     *         minShardId is lower than or equal to maxShardId
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setShards(@Nonnull Collection<Integer> shardIds)
    {
        Checks.notNull(shardIds, "shardIds");
        for (Integer id : shardIds)
        {
            Checks.notNegative(id, "minShardId");
            Checks.check(id < this.shardsTotal, "maxShardId must be lower than shardsTotal");
        }

        this.shards = new ArrayList<>(shardIds);

        return this;
    }

    /**
     * This will set the total amount of shards the {@link DefaultShardManager DefaultShardManager} should use.
     * <p> If this is set to {@code -1} JDA will automatically retrieve the recommended amount of shards from discord (default behavior).
     *
     * @param  shardsTotal
     *         The number of overall shards or {@code -1} if JDA should use the recommended amount from discord.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #setShards(int, int)
     */
    @Nonnull
    public DefaultShardManagerBuilder setShardsTotal(final int shardsTotal)
    {
        Checks.check(shardsTotal == -1 || shardsTotal > 0, "shardsTotal must either be -1 or greater than 0");
        this.shardsTotal = shardsTotal;

        return this;
    }

    /**
     * Sets the token that will be used by the {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} instance to log in when
     * {@link net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder#build() build()} is called.
     *
     * <p>To get a bot token:
     * <ol>
     *     <li>Go to your <a href="https://discord.com/developers/applications/me">Discord Applications</a></li>
     *     <li>Create or select an already existing application</li>
     *     <li>Verify that it has already been turned into a Bot. If you see the "Create a Bot User" button, click it.</li>
     *     <li>Click the <i>click to reveal</i> link beside the <b>Token</b> label to show your Bot's {@code token}</li>
     * </ol>
     *
     * @param  token
     *         The token of the account that you would like to login with.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the token is either null or empty
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setToken(@Nonnull final String token)
    {
        Checks.notBlank(token, "token");

        this.token = token;
        return this;
    }

    /**
     * Whether the {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} should use
     * {@link net.dv8tion.jda.api.JDA#shutdownNow() JDA#shutdownNow()} instead of
     * {@link net.dv8tion.jda.api.JDA#shutdown() JDA#shutdown()} to shutdown it's shards.
     * <br><b>Default</b>: {@code false}
     *
     * @param  useShutdownNow
     *         Whether the ShardManager should use JDA#shutdown() or not
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see net.dv8tion.jda.api.JDA#shutdown()
     * @see net.dv8tion.jda.api.JDA#shutdownNow()
     */
    @Nonnull
    public DefaultShardManagerBuilder setUseShutdownNow(final boolean useShutdownNow)
    {
        return setFlag(ShardingConfigFlag.SHUTDOWN_NOW, useShutdownNow);
    }

    /**
     * Sets the {@link com.neovisionaries.ws.client.WebSocketFactory WebSocketFactory} that will be used by JDA's websocket client.
     * This can be used to set things such as connection timeout and proxy.
     *
     * @param  factory
     *         The new {@link com.neovisionaries.ws.client.WebSocketFactory WebSocketFactory} to use.
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setWebsocketFactory(@Nullable WebSocketFactory factory)
    {
        this.wsFactory = factory;
        return this;
    }

    /**
     * The {@link ChunkingFilter} to filter which guilds should use member chunking.
     * <br>By default this uses {@link ChunkingFilter#ALL}.
     *
     * <p>If a guild is configured for chunking the {@link #setMemberCachePolicy(MemberCachePolicy)} will be ignored.
     *
     * @param  filter
     *         The filter to apply
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     *
     * @see    ChunkingFilter#NONE
     * @see    ChunkingFilter#include(long...)
     * @see    ChunkingFilter#exclude(long...)
     */
    @Nonnull
    public DefaultShardManagerBuilder setChunkingFilter(@Nullable ChunkingFilter filter)
    {
        this.chunkingFilter = filter;
        return this;
    }

    /**
     * Configures which events will be disabled.
     * Bots which did not enable presence/member updates in the developer dashboard are required to disable {@link GatewayIntent#GUILD_PRESENCES} and {@link GatewayIntent#GUILD_MEMBERS}!
     *
     * <p>It is not recommended to disable {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} when
     * using {@link MemberCachePolicy#ALL MemberCachePolicy.ALL} as the members cannot be removed from cache by a leave event without this intent.
     *
     * <p>If you disable certain intents you also have to disable related {@link CacheFlag CacheFlags}.
     * This can be achieved using {@link #disableCache(CacheFlag, CacheFlag...)}. The required intents for each
     * flag are documented in the {@link CacheFlag} enum.
     *
     * @param  intent
     *         The first intent to disable
     * @param  intents
     *         Any other intents to disable
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #setMemberCachePolicy(MemberCachePolicy)
     *
     * @since  4.2.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setDisabledIntents(@Nonnull GatewayIntent intent, @Nonnull GatewayIntent... intents)
    {
        Checks.notNull(intent, "Intent");
        Checks.noneNull(intents, "Intent");
        EnumSet<GatewayIntent> set = EnumSet.of(intent, intents);
        return setDisabledIntents(set);
    }

    /**
     * Configures which events will be disabled.
     * Bots which did not enable presence/member updates in the developer dashboard are required to disable {@link GatewayIntent#GUILD_PRESENCES} and {@link GatewayIntent#GUILD_MEMBERS}!
     *
     * <p>It is not recommended to disable {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} when
     * using {@link MemberCachePolicy#ALL MemberCachePolicy.ALL} as the members cannot be removed from cache by a leave event without this intent.
     *
     * <p>If you disable certain intents you also have to disable related {@link CacheFlag CacheFlags}.
     * This can be achieved using {@link #disableCache(CacheFlag, CacheFlag...)}. The required intents for each
     * flag are documented in the {@link CacheFlag} enum.
     *
     * @param  intents
     *         The intents to disable, or null to disable all intents (default: none)
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #setMemberCachePolicy(MemberCachePolicy)
     *
     * @since  4.2.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setDisabledIntents(@Nullable Collection<GatewayIntent> intents)
    {
        this.intents = GatewayIntent.ALL_INTENTS;
        if (intents != null)
            this.intents &= ~GatewayIntent.getRaw(intents);
        return this;
    }

    /**
     * Disable the specified {@link GatewayIntent GatewayIntents}.
     * <br>This will not enable any currently unset intents.
     *
     * <p>If you disable certain intents you also have to disable related {@link CacheFlag CacheFlags}.
     * This can be achieved using {@link #disableCache(CacheFlag, CacheFlag...)}. The required intents for each
     * flag are documented in the {@link CacheFlag} enum.
     *
     * @param  intents
     *         The intents to disable
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #enableIntents(Collection)
     */
    @Nonnull
    public DefaultShardManagerBuilder disableIntents(@Nonnull Collection<GatewayIntent> intents)
    {
        Checks.noneNull(intents, "GatewayIntent");
        int raw = GatewayIntent.getRaw(intents);
        this.intents &= ~raw;
        return this;
    }

    /**
     * Disable the specified {@link GatewayIntent GatewayIntents}.
     * <br>This will not enable any currently unset intents.
     *
     * <p>If you disable certain intents you also have to disable related {@link CacheFlag CacheFlags}.
     * This can be achieved using {@link #disableCache(CacheFlag, CacheFlag...)}. The required intents for each
     * flag are documented in the {@link CacheFlag} enum.
     *
     * @param  intent
     *         The intent to disable
     * @param  intents
     *         Other intents to disable
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #enableIntents(GatewayIntent, GatewayIntent...)
     */
    @Nonnull
    public DefaultShardManagerBuilder disableIntents(@Nonnull GatewayIntent intent, @Nonnull GatewayIntent... intents)
    {
        Checks.notNull(intent, "GatewayIntent");
        Checks.noneNull(intents, "GatewayIntent");
        int raw = GatewayIntent.getRaw(intent, intents);
        this.intents &= ~raw;
        return this;
    }

    /**
     * Configures which events will be enabled.
     * Bots which did not enable presence/member updates in the developer dashboard are required to disable {@link GatewayIntent#GUILD_PRESENCES} and {@link GatewayIntent#GUILD_MEMBERS}!
     *
     * <p>It is not recommended to disable {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} when
     * using {@link MemberCachePolicy#ALL MemberCachePolicy.ALL} as the members cannot be removed from cache by a leave event without this intent.
     *
     * <p>If you disable certain intents you also have to disable related {@link CacheFlag CacheFlags}.
     * This can be achieved using {@link #disableCache(CacheFlag, CacheFlag...)}. The required intents for each
     * flag are documented in the {@link CacheFlag} enum.
     *
     * @param  intent
     *         The intent to enable
     * @param  intents
     *         Any other intents to enable
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #setMemberCachePolicy(MemberCachePolicy)
     *
     * @since  4.2.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setEnabledIntents(@Nonnull GatewayIntent intent, @Nonnull GatewayIntent... intents)
    {
        Checks.notNull(intent, "Intent");
        Checks.noneNull(intents, "Intent");
        EnumSet<GatewayIntent> set = EnumSet.of(intent, intents);
        return setDisabledIntents(EnumSet.complementOf(set));
    }

    /**
     * Configures which events will be enabled.
     * Bots which did not enable presence/member updates in the developer dashboard are required to disable {@link GatewayIntent#GUILD_PRESENCES} and {@link GatewayIntent#GUILD_MEMBERS}!
     *
     * <p>It is not recommended to disable {@link GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} when
     * using {@link MemberCachePolicy#ALL MemberCachePolicy.ALL} as the members cannot be removed from cache by a leave event without this intent.
     *
     * <p>If you disable certain intents you also have to disable related {@link CacheFlag CacheFlags}.
     * This can be achieved using {@link #disableCache(CacheFlag, CacheFlag...)}. The required intents for each
     * flag are documented in the {@link CacheFlag} enum.
     *
     * @param  intents
     *         The intents to enable, or null to enable no intents (default: all)
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #setMemberCachePolicy(MemberCachePolicy)
     *
     * @since  4.2.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setEnabledIntents(@Nullable Collection<GatewayIntent> intents)
    {
        if (intents == null || intents.isEmpty())
            setDisabledIntents(EnumSet.allOf(GatewayIntent.class));
        else if (intents instanceof EnumSet)
            setDisabledIntents(EnumSet.complementOf((EnumSet<GatewayIntent>) intents));
        else
            setDisabledIntents(EnumSet.complementOf(EnumSet.copyOf(intents)));
        return this;
    }

    /**
     * Enable the specified {@link GatewayIntent GatewayIntents}.
     * <br>This will not disable any currently set intents.
     *
     * @param  intents
     *         The intents to enable
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #disableIntents(Collection)
     */
    @Nonnull
    public DefaultShardManagerBuilder enableIntents(@Nonnull Collection<GatewayIntent> intents)
    {
        Checks.noneNull(intents, "GatewayIntent");
        int raw = GatewayIntent.getRaw(intents);
        this.intents |= raw;
        return this;
    }

    /**
     * Enable the specified {@link GatewayIntent GatewayIntents}.
     * <br>This will not disable any currently set intents.
     *
     * @param  intent
     *         The intent to enable
     * @param  intents
     *         Other intents to enable
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @see    #enableIntents(GatewayIntent, GatewayIntent...)
     */
    @Nonnull
    public DefaultShardManagerBuilder enableIntents(@Nonnull GatewayIntent intent, @Nonnull GatewayIntent... intents)
    {
        Checks.notNull(intent, "GatewayIntent");
        Checks.noneNull(intents, "GatewayIntent");
        int raw = GatewayIntent.getRaw(intent, intents);
        this.intents |= raw;
        return this;
    }

    /**
     * Decides the total number of members at which a guild should start to use lazy loading.
     * <br>This is limited to a number between 50 and 250 (inclusive).
     * If the {@link #setChunkingFilter(ChunkingFilter) chunking filter} is set to {@link ChunkingFilter#ALL}
     * this should be set to {@code 250} (default) to minimize the amount of guilds that need to request members.
     *
     * @param  threshold
     *         The threshold in {@code [50, 250]}
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     *
     * @since  4.0.0
     */
    @Nonnull
    public DefaultShardManagerBuilder setLargeThreshold(int threshold)
    {
        this.largeThreshold = Math.max(50, Math.min(250, threshold)); // enforce 50 <= t <= 250
        return this;
    }

    /**
     * The maximum size, in bytes, of the buffer used for decompressing discord payloads.
     * <br>If the maximum buffer size is exceeded a new buffer will be allocated instead.
     * <br>Setting this to {@link Integer#MAX_VALUE} would imply the buffer will never be resized unless memory starvation is imminent.
     * <br>Setting this to {@code 0} would imply the buffer would need to be allocated again for every payload (not recommended).
     *
     * <p>Default: {@code 2048}
     *
     * @param  bufferSize
     *         The maximum size the buffer should allow to retain
     *
     * @throws IllegalArgumentException
     *         If the provided buffer size is negative
     *
     * @return The DefaultShardManagerBuilder instance. Useful for chaining.
     */
    @Nonnull
    public DefaultShardManagerBuilder setMaxBufferSize(int bufferSize)
    {
        Checks.notNegative(bufferSize, "The buffer size");
        this.maxBufferSize = bufferSize;
        return this;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} instance and uses the provided token to start the login process.
     * <br>The login process runs in a different thread, so while this will return immediately, {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} has not
     * finished loading, thus many {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} methods have the chance to return incorrect information.
     * <br>The main use of this method is to start the JDA connect process and do other things in parallel while startup is
     * being performed like database connection or local resource loading.
     *
     * <p>Note that this method is async and as such will <b>not</b> block until all shards are started.
     *
     * @throws  LoginException
     *          If the provided token is invalid.
     * @throws  IllegalArgumentException
     *          If the provided token is empty or null. Or the provided intents/cache configuration is not possible.
     * @throws  net.dv8tion.jda.api.exceptions.ErrorResponseException
     *          If some other HTTP error occurred.
     *
     * @return A {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} instance that has started the login process. It is unknown as
     *         to whether or not loading has finished when this returns.
     */
    @Nonnull
    public ShardManager build() throws LoginException, IllegalArgumentException
    {
        return build(true);
    }

    /**
     * Builds a new {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} instance. If the login parameter is true, then it will start the login process.
     * <br>The login process runs in a different thread, so while this will return immediately, {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} has not
     * finished loading, thus many {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} methods have the chance to return incorrect information.
     * <br>The main use of this method is to start the JDA connect process and do other things in parallel while startup is
     * being performed like database connection or local resource loading.
     *
     * <p>Note that this method is async and as such will <b>not</b> block until all shards are started.
     *
     * @param  login
     *         Whether the login process will be started. If this is false, then you will need to manually call
     *         {@link net.dv8tion.jda.api.sharding.ShardManager#login()} to start it.
     *
     * @throws  LoginException
     *          If the provided token is invalid and {@code login} is true
     * @throws  IllegalArgumentException
     *          If the provided token is empty or null. Or the provided intents/cache configuration is not possible.
     *
     * @return A {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} instance. If {@code login} is set to
     * true, then the instance will have started the login process. It is unknown as to whether or not loading has
     * finished when this returns.
     */
    @Nonnull
    public ShardManager build(boolean login) throws LoginException, IllegalArgumentException
    {
        checkIntents();
        boolean useShutdownNow = shardingFlags.contains(ShardingConfigFlag.SHUTDOWN_NOW);
        final ShardingConfig shardingConfig = new ShardingConfig(shardsTotal, useShutdownNow, intents, memberCachePolicy);
        final EventConfig eventConfig = new EventConfig(eventManagerProvider);
        listeners.forEach(eventConfig::addEventListener);
        listenerProviders.forEach(eventConfig::addEventListenerProvider);
        final PresenceProviderConfig presenceConfig = new PresenceProviderConfig();
        presenceConfig.setActivityProvider(activityProvider);
        presenceConfig.setStatusProvider(statusProvider);
        presenceConfig.setIdleProvider(idleProvider);
        final ThreadingProviderConfig threadingConfig = new ThreadingProviderConfig(rateLimitPoolProvider, gatewayPoolProvider, callbackPoolProvider, eventPoolProvider, audioPoolProvider, threadFactory);
        final ShardingSessionConfig sessionConfig = new ShardingSessionConfig(sessionController, voiceDispatchInterceptor, httpClient, httpClientBuilder, wsFactory, audioSendFactory, flags, shardingFlags, maxReconnectDelay, largeThreshold);
        final ShardingMetaConfig metaConfig = new ShardingMetaConfig(maxBufferSize, contextProvider, cacheFlags, flags, compression, encoding);
        final DefaultShardManager manager = new DefaultShardManager(this.token, this.shards, shardingConfig, eventConfig, presenceConfig, threadingConfig, sessionConfig, metaConfig, chunkingFilter);

        if (login)
             manager.login();

        return manager;
    }

    private DefaultShardManagerBuilder setFlag(ConfigFlag flag, boolean enable)
    {
        if (enable)
            this.flags.add(flag);
        else
            this.flags.remove(flag);
        return this;
    }

    private DefaultShardManagerBuilder setFlag(ShardingConfigFlag flag, boolean enable)
    {
        if (enable)
            this.shardingFlags.add(flag);
        else
            this.shardingFlags.remove(flag);
        return this;
    }

    private void checkIntents()
    {
        boolean membersIntent = (intents & GatewayIntent.GUILD_MEMBERS.getRawValue()) != 0;
        if (!membersIntent && memberCachePolicy == MemberCachePolicy.ALL)
            throw new IllegalStateException("Cannot use MemberCachePolicy.ALL without GatewayIntent.GUILD_MEMBERS enabled!");
        else if (!membersIntent && chunkingFilter != ChunkingFilter.NONE)
            DefaultShardManager.LOG.warn("Member chunking is disabled due to missing GUILD_MEMBERS intent.");

        if (!automaticallyDisabled.isEmpty())
        {
            JDAImpl.LOG.warn("Automatically disabled CacheFlags due to missing intents");
            // List each missing intent
            automaticallyDisabled.stream()
                .map(it -> "Disabled CacheFlag." + it + " (missing GatewayIntent." + it.getRequiredIntent() + ")")
                .forEach(JDAImpl.LOG::warn);

            // Tell user how to disable this warning
            JDAImpl.LOG.warn("You can manually disable these flags to remove this warning by using disableCache({}) on your DefaultShardManagerBuilder",
                automaticallyDisabled.stream()
                        .map(it -> "CacheFlag." + it)
                        .collect(Collectors.joining(", ")));
            // Only print this warning once
            automaticallyDisabled.clear();
        }

        if (cacheFlags.isEmpty())
            return;

        EnumSet<GatewayIntent> providedIntents = GatewayIntent.getIntents(intents);
        for (CacheFlag flag : cacheFlags)
        {
            GatewayIntent intent = flag.getRequiredIntent();
            if (intent != null && !providedIntents.contains(intent))
                throw new IllegalArgumentException("Cannot use CacheFlag." + flag + " without GatewayIntent." + intent + "!");
        }
    }
    //Avoid having multiple anonymous classes
    private static class ThreadPoolProviderImpl<T extends ExecutorService> implements ThreadPoolProvider<T>
    {
        private final boolean autoShutdown;
        private final T pool;

        public ThreadPoolProviderImpl(T pool, boolean autoShutdown)
        {
            this.autoShutdown = autoShutdown;
            this.pool = pool;
        }

        @Override
        public T provide(int shardId)
        {
            return pool;
        }

        @Override
        public boolean shouldShutdownAutomatically(int shardId)
        {
            return autoShutdown;
        }
    }
}
