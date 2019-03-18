/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.factory.DefaultSendFactory;
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.exceptions.AccountTypeException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.handle.EventCache;
import net.dv8tion.jda.internal.handle.GuildSetupController;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.managers.DirectAudioControllerImpl;
import net.dv8tion.jda.internal.managers.PresenceImpl;
import net.dv8tion.jda.internal.requests.*;
import net.dv8tion.jda.internal.requests.restaction.GuildActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.UpstreamReference;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import net.dv8tion.jda.internal.utils.config.MetaConfig;
import net.dv8tion.jda.internal.utils.config.SessionConfig;
import net.dv8tion.jda.internal.utils.config.ThreadingConfig;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class JDAImpl implements JDA
{
    public static final Logger LOG = JDALogger.getLog(JDA.class);

    protected final Object audioLifeCycleLock = new Object();
    protected ScheduledThreadPoolExecutor audioLifeCyclePool;

    protected final SnowflakeCacheViewImpl<User> userCache = new SnowflakeCacheViewImpl<>(User.class, User::getName);
    protected final SnowflakeCacheViewImpl<Guild> guildCache = new SnowflakeCacheViewImpl<>(Guild.class, Guild::getName);
    protected final SnowflakeCacheViewImpl<Category> categories = new SnowflakeCacheViewImpl<>(Category.class, GuildChannel::getName);
    protected final SnowflakeCacheViewImpl<StoreChannel> storeChannelCache = new SnowflakeCacheViewImpl<>(StoreChannel.class, GuildChannel::getName);
    protected final SnowflakeCacheViewImpl<TextChannel> textChannelCache = new SnowflakeCacheViewImpl<>(TextChannel.class, GuildChannel::getName);
    protected final SnowflakeCacheViewImpl<VoiceChannel> voiceChannelCache = new SnowflakeCacheViewImpl<>(VoiceChannel.class, GuildChannel::getName);
    protected final SnowflakeCacheViewImpl<PrivateChannel> privateChannelCache = new SnowflakeCacheViewImpl<>(PrivateChannel.class, MessageChannel::getName);

    protected final TLongObjectMap<User> fakeUsers = MiscUtil.newLongMap();
    protected final TLongObjectMap<PrivateChannel> fakePrivateChannels = MiscUtil.newLongMap();

    protected final AbstractCacheView<AudioManager> audioManagers = new CacheView.SimpleCacheView<>(AudioManager.class, m -> m.getGuild().getName());

    protected final PresenceImpl presence;
    protected final Thread shutdownHook;
    protected final EntityBuilder entityBuilder = new EntityBuilder(this);
    protected final EventCache eventCache = new EventCache();

    protected final GuildSetupController guildSetupController;
    protected final DirectAudioControllerImpl audioController;

    protected final AuthorizationConfig authConfig;
    protected final ThreadingConfig threadConfig;
    protected final SessionConfig sessionConfig;
    protected final MetaConfig metaConfig;

    protected UpstreamReference<WebSocketClient> client;
    protected Requester requester;
    protected IEventManager eventManager = new InterfacedEventManager();
    protected IAudioSendFactory audioSendFactory = new DefaultSendFactory();
    protected Status status = Status.INITIALIZING;
    protected SelfUser selfUser;
    protected ShardInfo shardInfo;
    protected long responseTotal;
    protected long ping = -1;
    protected String gatewayUrl;

    protected String clientId = null;
    protected ShardManager shardManager = null;

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
    }

    public boolean isCacheFlagSet(CacheFlag flag)
    {
        return metaConfig.getCacheFlags().contains(flag);
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

    public int login() throws LoginException
    {
        return login(null, null, true, true);
    }

    public int login(ShardInfo shardInfo, boolean compression, boolean validateToken) throws LoginException
    {
        return login(null, shardInfo, compression, validateToken);
    }

    public int login(String gatewayUrl, ShardInfo shardInfo, boolean compression, boolean validateToken) throws LoginException
    {
        this.shardInfo = shardInfo;
        threadConfig.init(this::getIdentifierString);
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

        client = new UpstreamReference<>(new WebSocketClient(this, compression));
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
    public Pair<String, Integer> getGatewayBot()
    {
        return getSessionController().getGatewayBot(this);
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

            eventManager.handle(new StatusChangeEvent(this, status, oldStatus));
        }
    }

    public void verifyToken() throws LoginException
    {
        this.verifyToken(false);
    }

    // @param alreadyFailed If has already been a failed attempt with the current configuration
    public void verifyToken(boolean alreadyFailed) throws LoginException
    {

        RestActionImpl<JSONObject> login = new RestActionImpl<JSONObject>(this, Route.Self.GET_SELF.compile())
        {
            @Override
            public void handleResponse(Response response, Request<JSONObject> request)
            {
                if (response.isOk())
                    request.onSuccess(response.getObject());
                else if (response.isRateLimit())
                    request.onFailure(new RateLimitedException(request.getRoute(), response.retryAfter));
                else if (response.code == 401)
                    request.onSuccess(null);
                else
                    request.onFailure(new LoginException("When verifying the authenticity of the provided token, Discord returned an unknown response:\n" +
                        response.toString()));
            }
        };

        JSONObject userResponse;

        if (!alreadyFailed)
        {
            userResponse = checkToken(login);
            if (userResponse != null)
            {
                verifyAccountType(userResponse);
                return;
            }
        }

        //If we received a null return for userResponse, then that means we hit a 401.
        // 401 occurs when we attempt to access the users/@me endpoint with the wrong token prefix.
        // e.g: If we use a Client token and prefix it with "Bot ", or use a bot token and don't prefix it.
        // It also occurs when we attempt to access the endpoint with an invalid token.
        //The code below already knows that something is wrong with the token. We want to determine if it is invalid
        // or if the developer attempted to login with a token using the wrong AccountType.

        //If we attempted to login as a Bot, remove the "Bot " prefix and set the Requester to be a client.
        String token;
        if (getAccountType() == AccountType.BOT)
        {
            token = getToken().substring("Bot ".length());
            requester = new Requester(this, new AuthorizationConfig(AccountType.CLIENT, token));
        }
        else    //If we attempted to login as a Client, prepend the "Bot " prefix and set the Requester to be a Bot
        {
            requester = new Requester(this, new AuthorizationConfig(AccountType.BOT, getToken()));
        }

        userResponse = checkToken(login);
        shutdownNow();

        //If the response isn't null (thus it didn't 401) send it to the secondary verify method to determine
        // which account type the developer wrongly attempted to login as
        if (userResponse != null)
            verifyAccountType(userResponse);
        else    //We 401'd again. This is an invalid token
            throw new LoginException("The provided token is invalid!");
    }

    private void verifyAccountType(JSONObject userResponse)
    {
        if (getAccountType() == AccountType.BOT)
        {
            if (!userResponse.has("bot") || !userResponse.getBoolean("bot"))
                throw new AccountTypeException(AccountType.BOT, "Attempted to login as a BOT with a CLIENT token!");
        }
        else
        {
            if (userResponse.has("bot") && userResponse.getBoolean("bot"))
                throw new AccountTypeException(AccountType.CLIENT, "Attempted to login as a CLIENT with a BOT token!");
        }
    }

    private JSONObject checkToken(RestActionImpl<JSONObject> login) throws LoginException
    {
        JSONObject userResponse;
        try
        {
            userResponse = login.complete();
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
        return userResponse;
    }

    public AuthorizationConfig getAuthorizationConfig()
    {
        return authConfig;
    }

    @Override
    public String getToken()
    {
        return authConfig.getToken();
    }

    @Override
    public boolean isAudioEnabled()
    {
        return sessionConfig.isAudioEnabled();
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

    @Override
    public Status getStatus()
    {
        return status;
    }

    @Override
    public long getGatewayPing()
    {
        return ping;
    }

    @Override
    public JDA awaitStatus(Status status) throws InterruptedException
    {
        Checks.notNull(status, "Status");
        Checks.check(status.isInit(), "Cannot await the status %s as it is not part of the login cycle!", status);
        if (getStatus() == Status.CONNECTED)
            return this;
        while (!getStatus().isInit()                         // JDA might disconnect while starting
                || getStatus().ordinal() < status.ordinal()) // Wait until status is bypassed
        {
            if (getStatus() == Status.SHUTDOWN)
                throw new IllegalStateException("Was shutdown trying to await status");
            Thread.sleep(50);
        }
        return this;
    }

    @Override
    public ScheduledExecutorService getRateLimitPool()
    {
        return threadConfig.getRateLimitPool();
    }

    @Override
    public ScheduledExecutorService getGatewayPool()
    {
        return threadConfig.getGatewayPool();
    }

    @Override
    public ExecutorService getCallbackPool()
    {
        return threadConfig.getCallbackPool();
    }

    @Override
    public OkHttpClient getHttpClient()
    {
        return sessionConfig.getHttpClient();
    }

    @Override
    public DirectAudioControllerImpl getDirectAudioController()
    {
        return this.audioController;
    }

    @Override
    public List<String> getCloudflareRays()
    {
        WebSocketClient client = getClient();
        return client == null ? Collections.emptyList() : Collections.unmodifiableList(new LinkedList<>(client.getCfRays()));
    }

    @Override
    public List<String> getWebSocketTrace()
    {
        WebSocketClient client = getClient();
        return client == null ? Collections.emptyList() : Collections.unmodifiableList(new LinkedList<>(client.getTraces()));
    }

    @Override
    public List<Guild> getMutualGuilds(User... users)
    {
        Checks.notNull(users, "users");
        return getMutualGuilds(Arrays.asList(users));
    }

    @Override
    public List<Guild> getMutualGuilds(Collection<User> users)
    {
        Checks.notNull(users, "users");
        for(User u : users)
            Checks.notNull(u, "All users");
        return Collections.unmodifiableList(getGuilds().stream()
                .filter(guild -> users.stream().allMatch(guild::isMember))
                .collect(Collectors.toList()));
    }

    @Override
    public RestAction<User> retrieveUserById(String id)
    {
        return retrieveUserById(MiscUtil.parseSnowflake(id));
    }

    @Override
    public RestAction<User> retrieveUserById(long id)
    {
        AccountTypeException.check(getAccountType(), AccountType.BOT);

        // check cache
        User user = this.getUserById(id);
        if (user != null)
            return new EmptyRestAction<>(this, user);

        Route.CompiledRoute route = Route.Users.GET_USER.compile(Long.toUnsignedString(id));
        return new RestActionImpl<>(this, route,
            (response, request) -> getEntityBuilder().createFakeUser(response.getObject(), false));
    }

    @Override
    public CacheView<AudioManager> getAudioManagerCache()
    {
        return audioManagers;
    }

    @Override
    public SnowflakeCacheView<Guild> getGuildCache()
    {
        return guildCache;
    }

    @Override
    public SnowflakeCacheView<Role> getRoleCache()
    {
        return CacheView.allSnowflakes(() -> guildCache.stream().map(Guild::getRoleCache));
    }

    @Override
    public SnowflakeCacheView<Emote> getEmoteCache()
    {
        return CacheView.allSnowflakes(() -> guildCache.stream().map(Guild::getEmoteCache));
    }

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

    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        return textChannelCache;
    }

    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        return voiceChannelCache;
    }

    @Override
    public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache()
    {
        return privateChannelCache;
    }

    @Override
    public SnowflakeCacheView<User> getUserCache()
    {
        return userCache;
    }

    public SelfUser getSelfUser()
    {
        return selfUser;
    }

    @Override
    public synchronized void shutdownNow()
    {
        shutdown();
        threadConfig.shutdownNow();
    }

    @Override
    public synchronized void shutdown()
    {
        if (status == Status.SHUTDOWN || status == Status.SHUTTING_DOWN)
            return;

        setStatus(Status.SHUTTING_DOWN);

        WebSocketClient client = getClient();
        if (client != null)
            client.shutdown();

        shutdownInternals();
    }

    public synchronized void shutdownInternals()
    {
        if (status == Status.SHUTDOWN)
            return;
        //so we can shutdown from WebSocketClient properly
        closeAudioConnections();
        guildSetupController.close();

        getRequester().shutdown();
        if (audioLifeCyclePool != null)
            audioLifeCyclePool.shutdownNow();
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

    private void closeAudioConnections()
    {
        AbstractCacheView<AudioManager> view = getAudioManagersView();
        try (UnlockHook hook = view.writeLock())
        {
            TLongObjectMap<AudioManager> map = view.getMap();
            map.valueCollection().stream()
               .map(AudioManagerImpl.class::cast)
               .forEach(m -> m.closeAudioConnection(ConnectionStatus.SHUTTING_DOWN));
            map.clear();
        }
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

    @Override
    public ShardInfo getShardInfo()
    {
        return shardInfo;
    }

    @Override
    public Presence getPresence()
    {
        return presence;
    }

    @Override
    public IEventManager getEventManager()
    {
        return eventManager;
    }

    @Override
    public AccountType getAccountType()
    {
        return authConfig.getAccountType();
    }

    @Override
    public void setEventManager(IEventManager eventManager)
    {
        this.eventManager = eventManager == null ? new InterfacedEventManager() : eventManager;
    }

    @Override
    public void addEventListener(Object... listeners)
    {
        Checks.noneNull(listeners, "listeners");

        for (Object listener: listeners)
            eventManager.register(listener);
    }

    @Override
    public void removeEventListener(Object... listeners)
    {
        Checks.noneNull(listeners, "listeners");

        for (Object listener: listeners)
            eventManager.unregister(listener);
    }

    @Override
    public List<Object> getRegisteredListeners()
    {
        return Collections.unmodifiableList(eventManager.getRegisteredListeners());
    }

    @Override
    public GuildActionImpl createGuild(String name)
    {
        switch (getAccountType())
        {
            case BOT:
                if (guildCache.size() >= 10)
                    throw new IllegalStateException("Cannot create a Guild with a Bot in 10 or more guilds!");
                break;
            case CLIENT:
                if (guildCache.size() >= 100)
                    throw new IllegalStateException("Cannot be in more than 100 guilds with AccountType.CLIENT!");
        }
        return new GuildActionImpl(this, name);
    }

    @Override
    public RestAction<Webhook> retrieveWebhookById(String webhookId)
    {
        Checks.isSnowflake(webhookId, "Webhook ID");

        Route.CompiledRoute route = Route.Webhooks.GET_WEBHOOK.compile(webhookId);

        return new RestActionImpl<>(this, route, (response, request) ->
        {
            JSONObject object = response.getObject();
            EntityBuilder builder = getEntityBuilder();
            return builder.createWebhook(object);
        });
    }

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

    @Override
    public String getInviteUrl(Permission... permissions)
    {
        StringBuilder builder = buildBaseInviteUrl();
        if (permissions != null && permissions.length > 0)
            builder.append("&permissions=").append(Permission.getRaw(permissions));
        return builder.toString();
    }

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
            retrieveApplicationInfo().complete();
        StringBuilder builder = new StringBuilder("https://discordapp.com/oauth2/authorize?scope=bot&client_id=");
        builder.append(clientId);
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

    public void setPing(long ping)
    {
        this.ping = ping;
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
        return client == null ? null : client.get();
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

    public SnowflakeCacheViewImpl<VoiceChannel> getVoiceChannelsView()
    {
        return voiceChannelCache;
    }

    public SnowflakeCacheViewImpl<PrivateChannel> getPrivateChannelsView()
    {
        return privateChannelCache;
    }

    public AbstractCacheView<AudioManager> getAudioManagersView()
    {
        return audioManagers;
    }

    public TLongObjectMap<User> getFakeUserMap()
    {
        return fakeUsers;
    }

    public TLongObjectMap<PrivateChannel> getFakePrivateChannelMap()
    {
        return fakePrivateChannels;
    }

    public void setSelfUser(SelfUser selfUser)
    {
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
        return gatewayUrl;
    }

    public void resetGatewayUrl()
    {
        this.gatewayUrl = getGateway();
    }

    public ScheduledThreadPoolExecutor getAudioLifeCyclePool()
    {
        ScheduledThreadPoolExecutor pool = audioLifeCyclePool;
        if (pool == null)
        {
            synchronized (audioLifeCycleLock)
            {
                pool = audioLifeCyclePool;
                if (pool == null)
                    pool = audioLifeCyclePool = ThreadingConfig.newScheduler(1, this::getIdentifierString, "AudioLifeCycle");
            }
        }
        return pool;
    }
}
