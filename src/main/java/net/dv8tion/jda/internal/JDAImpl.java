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

package net.dv8tion.jda.internal;

import com.neovisionaries.ws.client.WebSocketFactory;
import gnu.trove.set.TLongSet;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.factory.DefaultSendFactory;
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.exceptions.AccountTypeException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.*;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.handle.EventCache;
import net.dv8tion.jda.internal.handle.GuildSetupController;
import net.dv8tion.jda.internal.hooks.EventManagerProxy;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.managers.DirectAudioControllerImpl;
import net.dv8tion.jda.internal.managers.PresenceImpl;
import net.dv8tion.jda.internal.requests.*;
import net.dv8tion.jda.internal.requests.restaction.CommandCreateActionImpl;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;
import net.dv8tion.jda.internal.requests.restaction.CommandListUpdateActionImpl;
import net.dv8tion.jda.internal.requests.restaction.GuildActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import net.dv8tion.jda.internal.utils.config.MetaConfig;
import net.dv8tion.jda.internal.utils.config.SessionConfig;
import net.dv8tion.jda.internal.utils.config.ThreadingConfig;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class JDAImpl implements JDA
{
    public static final Logger LOG = JDALogger.getLog(JDA.class);

    protected final SnowflakeCacheViewImpl<User> userCache = new SnowflakeCacheViewImpl<>(User.class, User::getName);
    protected final SnowflakeCacheViewImpl<Guild> guildCache = new SnowflakeCacheViewImpl<>(Guild.class, Guild::getName);
    protected final SnowflakeCacheViewImpl<Category> categories = new SnowflakeCacheViewImpl<>(Category.class, Channel::getName);
    protected final SnowflakeCacheViewImpl<StoreChannel> storeChannelCache = new SnowflakeCacheViewImpl<>(StoreChannel.class, Channel::getName);
    protected final SnowflakeCacheViewImpl<TextChannel> textChannelCache = new SnowflakeCacheViewImpl<>(TextChannel.class, Channel::getName);
    protected final SnowflakeCacheViewImpl<NewsChannel> newsChannelCache = new SnowflakeCacheViewImpl<>(NewsChannel.class, Channel::getName);
    protected final SnowflakeCacheViewImpl<VoiceChannel> voiceChannelCache = new SnowflakeCacheViewImpl<>(VoiceChannel.class, Channel::getName);
    protected final SnowflakeCacheViewImpl<StageChannel> stageChannelCache = new SnowflakeCacheViewImpl<>(StageChannel.class, Channel::getName);
    protected final SnowflakeCacheViewImpl<ThreadChannel> threadChannelsCache = new SnowflakeCacheViewImpl<>(ThreadChannel.class, Channel::getName);
    protected final SnowflakeCacheViewImpl<PrivateChannel> privateChannelCache = new SnowflakeCacheViewImpl<>(PrivateChannel.class, Channel::getName);
    protected final LinkedList<Long> privateChannelLRU = new LinkedList<>();

    protected final AbstractCacheView<AudioManager> audioManagers = new CacheView.SimpleCacheView<>(AudioManager.class, m -> m.getGuild().getName());

    protected final PresenceImpl presence;
    protected final Thread shutdownHook;
    protected final EntityBuilder entityBuilder = new EntityBuilder(this);
    protected final EventCache eventCache;
    protected final EventManagerProxy eventManager;

    protected final GuildSetupController guildSetupController;
    protected final DirectAudioControllerImpl audioController;

    protected final AuthorizationConfig authConfig;
    protected final ThreadingConfig threadConfig;
    protected final SessionConfig sessionConfig;
    protected final MetaConfig metaConfig;

    protected WebSocketClient client;
    protected Requester requester;
    protected IAudioSendFactory audioSendFactory = new DefaultSendFactory();
    protected Status status = Status.INITIALIZING;
    protected SelfUser selfUser;
    protected ShardInfo shardInfo;
    protected long responseTotal;
    protected long gatewayPing = -1;
    protected String gatewayUrl;
    protected ChunkingFilter chunkingFilter;

    protected String clientId = null,  requiredScopes = "bot";
    protected ShardManager shardManager = null;
    protected MemberCachePolicy memberCachePolicy = MemberCachePolicy.ALL;

    public JDAImpl(AuthorizationConfig authConfig)
    {
        this(authConfig, null, null, null);
    }

    public JDAImpl(
            AuthorizationConfig authConfig, SessionConfig sessionConfig,
            ThreadingConfig threadConfig, MetaConfig metaConfig)
    {
        this.authConfig = authConfig;
        this.threadConfig = threadConfig == null ? ThreadingConfig.getDefault() : threadConfig;
        this.sessionConfig = sessionConfig == null ? SessionConfig.getDefault() : sessionConfig;
        this.metaConfig = metaConfig == null ? MetaConfig.getDefault() : metaConfig;
        this.shutdownHook = this.metaConfig.isUseShutdownHook() ? new Thread(this::shutdown, "JDA Shutdown Hook") : null;
        this.presence = new PresenceImpl(this);
        this.requester = new Requester(this);
        this.requester.setRetryOnTimeout(this.sessionConfig.isRetryOnTimeout());
        this.guildSetupController = new GuildSetupController(this);
        this.audioController = new DirectAudioControllerImpl(this);
        this.eventCache = new EventCache();
        this.eventManager = new EventManagerProxy(new InterfacedEventManager(), this.threadConfig.getEventPool());
    }

    public void handleEvent(@Nonnull GenericEvent event)
    {
        eventManager.handle(event);
    }

    public boolean isRawEvents()
    {
        return sessionConfig.isRawEvents();
    }

    public boolean isRelativeRateLimit()
    {
        return sessionConfig.isRelativeRateLimit();
    }

    public boolean isCacheFlagSet(CacheFlag flag)
    {
        return metaConfig.getCacheFlags().contains(flag);
    }

    public boolean isIntent(GatewayIntent intent)
    {
        int raw = intent.getRawValue();
        return (client.getGatewayIntents() & raw) == raw;
    }

    public int getLargeThreshold()
    {
        return sessionConfig.getLargeThreshold();
    }

    public int getMaxBufferSize()
    {
        return metaConfig.getMaxBufferSize();
    }

    public boolean chunkGuild(long id)
    {
        try
        {
            return isIntent(GatewayIntent.GUILD_MEMBERS) && chunkingFilter.filter(id);
        }
        catch (Exception e)
        {
            LOG.error("Uncaught exception from chunking filter", e);
            return true;
        }
    }

    public void setChunkingFilter(ChunkingFilter filter)
    {
        this.chunkingFilter = filter;
    }

    public boolean cacheMember(Member member)
    {
        try
        {
            return member.getUser().equals(getSelfUser()) // always cache self
                    || chunkGuild(member.getGuild().getIdLong())  // always cache if chunking
                    || memberCachePolicy.cacheMember(member); // ask policy, should we cache?
        }
        catch (Exception e)
        {
            LOG.error("Uncaught exception from member cache policy", e);
            return true;
        }
    }

    public void setMemberCachePolicy(MemberCachePolicy policy)
    {
        this.memberCachePolicy = policy;
    }

    public SessionController getSessionController()
    {
        return sessionConfig.getSessionController();
    }

    public GuildSetupController getGuildSetupController()
    {
        return guildSetupController;
    }

    public VoiceDispatchInterceptor getVoiceInterceptor()
    {
        return sessionConfig.getVoiceDispatchInterceptor();
    }

    public void usedPrivateChannel(long id)
    {
        synchronized (privateChannelLRU)
        {
            privateChannelLRU.remove(id); // We could probably make a special LRU cache view too, might not be worth it though
            privateChannelLRU.addFirst(id);
            if (privateChannelLRU.size() > 10) // This could probably be a config option
            {
                long removed = privateChannelLRU.removeLast();
                privateChannelCache.remove(removed);
            }
        }
    }

    public int login() throws LoginException
    {
        return login(null, null, Compression.ZLIB, true, GatewayIntent.ALL_INTENTS, GatewayEncoding.JSON);
    }

    public int login(ShardInfo shardInfo, Compression compression, boolean validateToken, int intents, GatewayEncoding encoding) throws LoginException
    {
        return login(null, shardInfo, compression, validateToken, intents, encoding);
    }

    public int login(String gatewayUrl, ShardInfo shardInfo, Compression compression, boolean validateToken, int intents, GatewayEncoding encoding) throws LoginException
    {
        this.shardInfo = shardInfo;
        threadConfig.init(this::getIdentifierString);
        requester.getRateLimiter().init();
        this.gatewayUrl = gatewayUrl == null ? getGateway() : gatewayUrl;
        Checks.notNull(this.gatewayUrl, "Gateway URL");

        String token = authConfig.getToken();
        setStatus(Status.LOGGING_IN);
        if (token == null || token.isEmpty())
            throw new LoginException("Provided token was null or empty!");

        Map<String, String> previousContext = null;
        ConcurrentMap<String, String> contextMap = metaConfig.getMdcContextMap();
        if (contextMap != null)
        {
            if (shardInfo != null)
            {
                contextMap.put("jda.shard", shardInfo.getShardString());
                contextMap.put("jda.shard.id", String.valueOf(shardInfo.getShardId()));
                contextMap.put("jda.shard.total", String.valueOf(shardInfo.getShardTotal()));
            }
            // set MDC metadata for build thread
            previousContext = MDC.getCopyOfContextMap();
            contextMap.forEach(MDC::put);
            requester.setContextReady(true);
        }
        if (validateToken)
        {
            verifyToken();
            LOG.info("Login Successful!");
        }

        client = new WebSocketClient(this, compression, intents, encoding);
        // remove our MDC metadata when we exit our code
        if (previousContext != null)
            previousContext.forEach(MDC::put);

        if (shutdownHook != null)
            Runtime.getRuntime().addShutdownHook(shutdownHook);

        return shardInfo == null ? -1 : shardInfo.getShardTotal();
    }

    public String getGateway()
    {
        return getSessionController().getGateway(this);
    }


    // This method also checks for a valid bot token as it is required to get the recommended shard count.
    public SessionController.ShardedGateway getShardedGateway()
    {
        return getSessionController().getShardedGateway(this);
    }

    public ConcurrentMap<String, String> getContextMap()
    {
        return metaConfig.getMdcContextMap() == null ? null : new ConcurrentHashMap<>(metaConfig.getMdcContextMap());
    }

    public void setContext()
    {
        if (metaConfig.getMdcContextMap() != null)
            metaConfig.getMdcContextMap().forEach(MDC::put);
    }

    public void setToken(String token)
    {
        this.authConfig.setToken(token);
    }

    public void setStatus(Status status)
    {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.status)
        {
            Status oldStatus = this.status;
            this.status = status;

            handleEvent(new StatusChangeEvent(this, status, oldStatus));
        }
    }

    public void verifyToken() throws LoginException
    {
        RestActionImpl<DataObject> login = new RestActionImpl<DataObject>(this, Route.Self.GET_SELF.compile())
        {
            @Override
            public void handleResponse(Response response, Request<DataObject> request)
            {
                if (response.isOk())
                    request.onSuccess(response.getObject());
                else if (response.isRateLimit())
                    request.onFailure(new RateLimitedException(request.getRoute(), response.retryAfter));
                else if (response.code == 401)
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        }.priority();

        DataObject userResponse = login.complete();
        if (userResponse != null)
        {
            getEntityBuilder().createSelfUser(userResponse);
            return;
        }
        shutdownNow();
        throw new LoginException("The provided token is invalid!");
    }

    public AuthorizationConfig getAuthorizationConfig()
    {
        return authConfig;
    }

    @Nonnull
    @Override
    public String getToken()
    {
        return authConfig.getToken();
    }


    @Override
    public boolean isBulkDeleteSplittingEnabled()
    {
        return sessionConfig.isBulkDeleteSplittingEnabled();
    }

    @Override
    public void setAutoReconnect(boolean autoReconnect)
    {
        sessionConfig.setAutoReconnect(autoReconnect);
        WebSocketClient client = getClient();
        if (client != null)
            client.setAutoReconnect(autoReconnect);
    }

    @Override
    public void setRequestTimeoutRetry(boolean retryOnTimeout)
    {
        requester.setRetryOnTimeout(retryOnTimeout);
    }

    @Override
    public boolean isAutoReconnect()
    {
        return sessionConfig.isAutoReconnect();
    }

    @Nonnull
    @Override
    public Status getStatus()
    {
        return status;
    }

    @Nonnull
    @Override
    public EnumSet<GatewayIntent> getGatewayIntents()
    {
        return GatewayIntent.getIntents(client.getGatewayIntents());
    }

    @Nonnull
    @Override
    public EnumSet<CacheFlag> getCacheFlags()
    {
        return Helpers.copyEnumSet(CacheFlag.class, metaConfig.getCacheFlags());
    }

    @Override
    public boolean unloadUser(long userId)
    {
        if (userId == selfUser.getIdLong())
            return false;
        User user = getUserById(userId);
        if (user == null)
            return false;

        // We avoid to lock both the guild cache and member cache to make a deadlock impossible
        return getGuildCache().stream()
                .filter(guild -> guild.unloadMember(userId)) // this also removes it from user cache
                .count() > 0L; // we use count to make sure it iterates all guilds not just one
    }

    @Override
    public long getGatewayPing()
    {
        return gatewayPing;
    }

    @Nonnull
    @Override
    public JDA awaitStatus(@Nonnull Status status, @Nonnull Status... failOn) throws InterruptedException
    {
        Checks.notNull(status, "Status");
        Checks.check(status.isInit(), "Cannot await the status %s as it is not part of the login cycle!", status);
        if (getStatus() == Status.CONNECTED)
            return this;
        List<Status> failStatus = Arrays.asList(failOn);
        while (!getStatus().isInit()                         // JDA might disconnect while starting
                || getStatus().ordinal() < status.ordinal()) // Wait until status is bypassed
        {
            if (getStatus() == Status.SHUTDOWN)
                throw new IllegalStateException("Was shutdown trying to await status");
            else if (failStatus.contains(getStatus()))
                return this;
            Thread.sleep(50);
        }
        return this;
    }

    @Override
    public int cancelRequests()
    {
        return requester.getRateLimiter().cancelRequests();
    }

    @Nonnull
    @Override
    public ScheduledExecutorService getRateLimitPool()
    {
        return threadConfig.getRateLimitPool();
    }

    @Nonnull
    @Override
    public ScheduledExecutorService getGatewayPool()
    {
        return threadConfig.getGatewayPool();
    }

    @Nonnull
    @Override
    public ExecutorService getCallbackPool()
    {
        return threadConfig.getCallbackPool();
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions") // this can't really happen unless you pass bad configs
    public OkHttpClient getHttpClient()
    {
        return sessionConfig.getHttpClient();
    }

    @Nonnull
    @Override
    public DirectAudioControllerImpl getDirectAudioController()
    {
        if (!isIntent(GatewayIntent.GUILD_VOICE_STATES))
            throw new IllegalStateException("Cannot use audio features with disabled GUILD_VOICE_STATES intent!");
        return this.audioController;
    }

    @Nonnull
    @Override
    public List<Guild> getMutualGuilds(@Nonnull User... users)
    {
        Checks.notNull(users, "users");
        return getMutualGuilds(Arrays.asList(users));
    }

    @Nonnull
    @Override
    public List<Guild> getMutualGuilds(@Nonnull Collection<User> users)
    {
        Checks.notNull(users, "users");
        for(User u : users)
            Checks.notNull(u, "All users");
        return Collections.unmodifiableList(getGuilds().stream()
                .filter(guild -> users.stream().allMatch(guild::isMember))
                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public RestAction<User> retrieveUserById(@Nonnull String id)
    {
        return retrieveUserById(MiscUtil.parseSnowflake(id));
    }

    @Nonnull
    @Override
    public RestAction<User> retrieveUserById(long id, boolean update)
    {
        if (id == getSelfUser().getIdLong())
            return new CompletedRestAction<>(this, getSelfUser());

        AccountTypeException.check(getAccountType(), AccountType.BOT);
        return new DeferredRestAction<>(this, User.class,
                () -> !update || isIntent(GatewayIntent.GUILD_MEMBERS) || isIntent(GatewayIntent.GUILD_PRESENCES) ? getUserById(id) : null,
                () -> {
                    Route.CompiledRoute route = Route.Users.GET_USER.compile(Long.toUnsignedString(id));
                    return new RestActionImpl<>(this, route,
                            (response, request) -> getEntityBuilder().createUser(response.getObject()));
                });
    }

    @Nonnull
    @Override
    public CacheView<AudioManager> getAudioManagerCache()
    {
        return audioManagers;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<Guild> getGuildCache()
    {
        return guildCache;
    }

    @Nonnull
    @Override
    public Set<String> getUnavailableGuilds()
    {
        TLongSet unavailableGuilds = guildSetupController.getUnavailableGuilds();
        Set<String> copy = new HashSet<>();
        unavailableGuilds.forEach(id -> copy.add(Long.toUnsignedString(id)));
        return copy;
    }

    @Override
    public boolean isUnavailable(long guildId)
    {
        return guildSetupController.isUnavailable(guildId);
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<Role> getRoleCache()
    {
        return CacheView.allSnowflakes(() -> guildCache.stream().map(Guild::getRoleCache));
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<Emote> getEmoteCache()
    {
        return CacheView.allSnowflakes(() -> guildCache.stream().map(Guild::getEmoteCache));
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<GuildScheduledEvent> getGuildScheduledEventCache()
    {
        return CacheView.allSnowflakes(() -> guildCache.stream().map(Guild::getScheduledEventCache));
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<Category> getCategoryCache()
    {
        return categories;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<StoreChannel> getStoreChannelCache()
    {
        return storeChannelCache;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        return textChannelCache;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<NewsChannel> getNewsChannelCache()
    {
        return newsChannelCache;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        return voiceChannelCache;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<StageChannel> getStageChannelCache()
    {
        return stageChannelCache;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<ThreadChannel> getThreadChannelCache()
    {
        return threadChannelsCache;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache()
    {
        return privateChannelCache;
    }

    @Override
    public PrivateChannel getPrivateChannelById(@Nonnull String id)
    {
        return getPrivateChannelById(MiscUtil.parseSnowflake(id));
    }

    @Override
    public PrivateChannel getPrivateChannelById(long id)
    {
        PrivateChannel channel = JDA.super.getPrivateChannelById(id);
        if (channel != null)
            usedPrivateChannel(id);
        return channel;
    }

    @Nonnull
    @Override
    public RestAction<PrivateChannel> openPrivateChannelById(long userId)
    {
        if (selfUser != null && userId == selfUser.getIdLong())
            throw new UnsupportedOperationException("Cannot open private channel with yourself!");
        return new DeferredRestAction<>(this, PrivateChannel.class, () -> {
            User user = getUserById(userId);
            if (user instanceof UserImpl)
                return ((UserImpl) user).getPrivateChannel();
            return null;
        }, () -> {
            Route.CompiledRoute route = Route.Self.CREATE_PRIVATE_CHANNEL.compile();
            DataObject body = DataObject.empty().put("recipient_id", userId);
            return new RestActionImpl<>(this, route, body,
                (response, request) -> getEntityBuilder().createPrivateChannel(response.getObject()));
        });
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<User> getUserCache()
    {
        return userCache;
    }

    public boolean hasSelfUser()
    {
        return selfUser != null;
    }

    @Nonnull
    @Override
    public SelfUser getSelfUser()
    {
        if (selfUser == null)
            throw new IllegalStateException("Session is not yet ready!");
        return selfUser;
    }

    @Override
    public synchronized void shutdownNow()
    {
        requester.shutdown(); // stop all requests
        shutdown();
        threadConfig.shutdownNow();
    }

    @Override
    public synchronized void shutdown()
    {
        if (status == Status.SHUTDOWN || status == Status.SHUTTING_DOWN)
            return;

        setStatus(Status.SHUTTING_DOWN);
        shutdownInternals();

        WebSocketClient client = getClient();
        if (client != null)
        {
            client.getChunkManager().shutdown();
            client.shutdown();
        }
    }

    public synchronized void shutdownInternals()
    {
        if (status == Status.SHUTDOWN)
            return;
        //so we can shutdown from WebSocketClient properly
        closeAudioConnections();
        guildSetupController.close();

        // stop accepting new requests
        if (requester.stop()) // returns true if no more requests will be executed
            shutdownRequester(); // in that case shutdown entirely
        threadConfig.shutdown();

        if (shutdownHook != null)
        {
            try
            {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            catch (Exception ignored) {}
        }

        setStatus(Status.SHUTDOWN);
    }

    public synchronized void shutdownRequester()
    {
        // Stop all request processing
        requester.shutdown();
        threadConfig.shutdownRequester();
    }

    private void closeAudioConnections()
    {
        getAudioManagerCache()
            .stream()
            .map(AudioManagerImpl.class::cast)
            .forEach(m -> m.closeAudioConnection(ConnectionStatus.SHUTTING_DOWN));
    }

    @Override
    public long getResponseTotal()
    {
        return responseTotal;
    }

    @Override
    public int getMaxReconnectDelay()
    {
        return sessionConfig.getMaxReconnectDelay();
    }

    @Nonnull
    @Override
    public ShardInfo getShardInfo()
    {
        return shardInfo == null ? ShardInfo.SINGLE : shardInfo;
    }

    @Nonnull
    @Override
    public Presence getPresence()
    {
        return presence;
    }

    @Nonnull
    @Override
    public IEventManager getEventManager()
    {
        return eventManager.getSubject();
    }

    @Nonnull
    @Override
    public AccountType getAccountType()
    {
        return authConfig.getAccountType();
    }

    @Override
    public void setEventManager(IEventManager eventManager)
    {
        this.eventManager.setSubject(eventManager);
    }

    @Override
    public void addEventListener(@Nonnull Object... listeners)
    {
        Checks.noneNull(listeners, "listeners");

        for (Object listener: listeners)
            eventManager.register(listener);
    }

    @Override
    public void removeEventListener(@Nonnull Object... listeners)
    {
        Checks.noneNull(listeners, "listeners");

        for (Object listener: listeners)
            eventManager.unregister(listener);
    }

    @Nonnull
    @Override
    public List<Object> getRegisteredListeners()
    {
        return eventManager.getRegisteredListeners();
    }

    @Nonnull
    @Override
    public RestAction<List<Command>> retrieveCommands()
    {
        Route.CompiledRoute route = Route.Interactions.GET_COMMANDS.compile(getSelfUser().getApplicationId());
        return new RestActionImpl<>(this, route,
            (response, request) ->
                response.getArray()
                        .stream(DataArray::getObject)
                        .map(json -> new Command(this, null, json))
                        .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public RestAction<Command> retrieveCommandById(@Nonnull String id)
    {
        Checks.isSnowflake(id);
        Route.CompiledRoute route = Route.Interactions.GET_COMMAND.compile(getSelfUser().getApplicationId(), id);
        return new RestActionImpl<>(this, route, (response, request) -> new Command(this, null, response.getObject()));
    }

    @Nonnull
    @Override
    public CommandCreateAction upsertCommand(@Nonnull CommandData command)
    {
        Checks.notNull(command, "CommandData");
        return new CommandCreateActionImpl(this, command);
    }

    @Nonnull
    @Override
    public CommandListUpdateAction updateCommands()
    {
        Route.CompiledRoute route = Route.Interactions.UPDATE_COMMANDS.compile(getSelfUser().getApplicationId());
        return new CommandListUpdateActionImpl(this, null, route);
    }

    @Nonnull
    @Override
    public CommandEditAction editCommandById(@Nonnull String id)
    {
        Checks.isSnowflake(id);
        return new CommandEditActionImpl(this, id);
    }

    @Nonnull
    @Override
    public RestAction<Void> deleteCommandById(@Nonnull String commandId)
    {
        Checks.isSnowflake(commandId);
        Route.CompiledRoute route = Route.Interactions.DELETE_COMMAND.compile(getSelfUser().getApplicationId(), commandId);
        return new RestActionImpl<>(this, route);
    }

    @Nonnull
    @Override
    public GuildActionImpl createGuild(@Nonnull String name)
    {
        if (guildCache.size() >= 10)
            throw new IllegalStateException("Cannot create a Guild with a Bot in 10 or more guilds!");
        return new GuildActionImpl(this, name);
    }

    @Nonnull
    @Override
    public RestAction<Void> createGuildFromTemplate(@Nonnull String code, @Nonnull String name, Icon icon)
    {
        if (guildCache.size() >= 10)
            throw new IllegalStateException("Cannot create a Guild with a Bot in 10 or more guilds!");

        Checks.notBlank(code, "Template code");
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, 100, "Name");

        final Route.CompiledRoute route = Route.Templates.CREATE_GUILD_FROM_TEMPLATE.compile(code);

        DataObject object = DataObject.empty();
        object.put("name", name);
        if (icon != null)
            object.put("icon", icon.getEncoding());

        return new RestActionImpl<>(this, route, object);
    }

    @Nonnull
    @Override
    public RestAction<Webhook> retrieveWebhookById(@Nonnull String webhookId)
    {
        Checks.isSnowflake(webhookId, "Webhook ID");

        Route.CompiledRoute route = Route.Webhooks.GET_WEBHOOK.compile(webhookId);

        return new RestActionImpl<>(this, route, (response, request) ->
        {
            DataObject object = response.getObject();
            EntityBuilder builder = getEntityBuilder();
            return builder.createWebhook(object);
        });
    }

    @Nonnull
    @Override
    public RestAction<ApplicationInfo> retrieveApplicationInfo()
    {
        AccountTypeException.check(getAccountType(), AccountType.BOT);
        Route.CompiledRoute route = Route.Applications.GET_BOT_APPLICATION.compile();
        return new RestActionImpl<>(this, route, (response, request) ->
        {
            ApplicationInfo info = getEntityBuilder().createApplicationInfo(response.getObject());
            this.clientId = info.getId();
            return info;
        });
    }

    @Nonnull
    @Override
    public JDA setRequiredScopes(@Nonnull Collection<String> scopes)
    {
        Checks.noneNull(scopes, "Scopes");
        this.requiredScopes = String.join("+", scopes);
        if (!requiredScopes.contains("bot"))
        {
            if (requiredScopes.isEmpty())
                requiredScopes = "bot";
            else
                requiredScopes += "+bot";
        }
        return this;
    }

    @Nonnull
    @Override
    public String getInviteUrl(Permission... permissions)
    {
        StringBuilder builder = buildBaseInviteUrl();
        if (permissions != null && permissions.length > 0)
            builder.append("&permissions=").append(Permission.getRaw(permissions));
        return builder.toString();
    }

    @Nonnull
    @Override
    public String getInviteUrl(Collection<Permission> permissions)
    {
        StringBuilder builder = buildBaseInviteUrl();
        if (permissions != null && !permissions.isEmpty())
            builder.append("&permissions=").append(Permission.getRaw(permissions));
        return builder.toString();
    }

    private StringBuilder buildBaseInviteUrl()
    {
        if (clientId == null)
        {
            if (selfUser != null)
                clientId = selfUser.getApplicationId(); // populated by READY event
            else
                retrieveApplicationInfo().complete();
        }
        StringBuilder builder = new StringBuilder("https://discord.com/oauth2/authorize?client_id=");
        builder.append(clientId);
        builder.append("&scope=").append(requiredScopes);
        return builder;
    }

    public void setShardManager(ShardManager shardManager)
    {
        this.shardManager = shardManager;
    }

    @Override
    public ShardManager getShardManager()
    {
        return shardManager;
    }

    public EntityBuilder getEntityBuilder()
    {
        return entityBuilder;
    }

    public IAudioSendFactory getAudioSendFactory()
    {
        return audioSendFactory;
    }

    public void setAudioSendFactory(IAudioSendFactory factory)
    {
        Checks.notNull(factory, "Provided IAudioSendFactory");
        this.audioSendFactory = factory;
    }

    public void setGatewayPing(long ping)
    {
        long oldPing = this.gatewayPing;
        this.gatewayPing = ping;
        handleEvent(new GatewayPingEvent(this, oldPing));
    }

    public Requester getRequester()
    {
        return requester;
    }

    public WebSocketFactory getWebSocketFactory()
    {
        return sessionConfig.getWebSocketFactory();
    }

    public WebSocketClient getClient()
    {
        return client;
    }

    public SnowflakeCacheViewImpl<User> getUsersView()
    {
        return userCache;
    }

    public SnowflakeCacheViewImpl<Guild> getGuildsView()
    {
        return guildCache;
    }

    public SnowflakeCacheViewImpl<Category> getCategoriesView()
    {
        return categories;
    }

    public SnowflakeCacheViewImpl<StoreChannel> getStoreChannelsView()
    {
        return storeChannelCache;
    }

    public SnowflakeCacheViewImpl<TextChannel> getTextChannelsView()
    {
        return textChannelCache;
    }

    public SnowflakeCacheViewImpl<NewsChannel> getNewsChannelView()
    {
        return newsChannelCache;
    }

    public SnowflakeCacheViewImpl<VoiceChannel> getVoiceChannelsView()
    {
        return voiceChannelCache;
    }

    public SnowflakeCacheViewImpl<StageChannel> getStageChannelView()
    {
        return stageChannelCache;
    }

    public SnowflakeCacheViewImpl<ThreadChannel> getThreadChannelsView()
    {
        return threadChannelsCache;
    }

    public SnowflakeCacheViewImpl<PrivateChannel> getPrivateChannelsView()
    {
        return privateChannelCache;
    }

    public AbstractCacheView<AudioManager> getAudioManagersView()
    {
        return audioManagers;
    }

    public void setSelfUser(SelfUser selfUser)
    {
        try (UnlockHook hook = userCache.writeLock())
        {
            userCache.getMap().put(selfUser.getIdLong(), selfUser);
        }
        this.selfUser = selfUser;
    }

    public void setResponseTotal(int responseTotal)
    {
        this.responseTotal = responseTotal;
    }

    public String getIdentifierString()
    {
        if (shardInfo != null)
            return "JDA " + shardInfo.getShardString();
        else
            return "JDA";
    }

    public EventCache getEventCache()
    {
        return eventCache;
    }

    public String getGatewayUrl()
    {
        if (gatewayUrl == null)
            return gatewayUrl = getGateway();
        return gatewayUrl;
    }

    public void resetGatewayUrl()
    {
        this.gatewayUrl = null;
    }

    public ScheduledExecutorService getAudioLifeCyclePool()
    {
        return threadConfig.getAudioPool(this::getIdentifierString);
    }
}
