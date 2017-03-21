/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.entities.impl;

import com.mashape.unirest.http.Unirest;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.bot.entities.impl.JDABotImpl;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.AudioWebSocket;
import net.dv8tion.jda.core.audio.factory.DefaultSendFactory;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.Presence;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.requests.*;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.http.HttpHost;
import org.apache.http.util.Args;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JDAImpl implements JDA
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDA");

    protected final HashMap<String, User> users = new HashMap<>(200);
    protected final HashMap<String, Guild> guilds = new HashMap<>(10);
    protected final HashMap<String, TextChannel> textChannels = new HashMap<>();
    protected final HashMap<String, VoiceChannel> voiceChannels = new HashMap<>();
    protected final HashMap<String, PrivateChannel> privateChannels = new HashMap<>();

    protected final HashMap<String, User> fakeUsers = new HashMap<>();
    protected final HashMap<String, PrivateChannel> fakePrivateChannels = new HashMap<>();

    protected final HashMap<String, AudioManager> audioManagers = new HashMap<>();

    protected final HttpHost proxy;
    protected final WebSocketFactory wsFactory;
    protected final AccountType accountType;
    protected final PresenceImpl presence;
    protected final JDAClient jdaClient;
    protected final JDABot jdaBot;

    protected WebSocketClient client;
    protected Requester requester;
    protected IEventManager eventManager = new InterfacedEventManager();
    protected IAudioSendFactory audioSendFactory = new DefaultSendFactory();
    protected Status status = Status.INITIALIZING;
    protected SelfUser selfUser;
    protected ShardInfo shardInfo;
    protected String token = null;
    protected boolean audioEnabled;
    protected boolean useShutdownHook;
    protected boolean bulkDeleteSplittingEnabled;
    protected boolean autoReconnect;
    protected long responseTotal;
    protected long ping = -1;

    public JDAImpl(AccountType accountType, HttpHost proxy, WebSocketFactory wsFactory, boolean autoReconnect, boolean audioEnabled, boolean useShutdownHook, boolean bulkDeleteSplittingEnabled)
    {
        this.presence = new PresenceImpl(this);
        this.accountType = accountType;
        this.requester = new Requester(this);
        this.proxy = proxy;
        this.wsFactory = wsFactory;
        this.autoReconnect = autoReconnect;
        this.audioEnabled = audioEnabled;
        this.useShutdownHook = useShutdownHook;
        this.bulkDeleteSplittingEnabled = bulkDeleteSplittingEnabled;

        this.jdaClient = accountType == AccountType.CLIENT ? new JDAClientImpl(this) : null;
        this.jdaBot = accountType == AccountType.BOT ? new JDABotImpl(this) : null;
    }

    public void login(String token, ShardInfo shardInfo) throws LoginException, RateLimitedException
    {
        setStatus(Status.LOGGING_IN);
        if (token == null || token.isEmpty())
            throw new LoginException("Provided token was null or empty!");

        setToken(token);
        verifyToken();
        this.shardInfo = shardInfo;
        LOG.info("Login Successful!");

        client = new WebSocketClient(this);

        if (useShutdownHook)
        {
            Runtime.getRuntime().addShutdownHook(new Thread("JDA Shutdown Hook")
            {
                @Override
                public void run()
                {
                    JDAImpl.this.shutdown(true);
                }
            });
        }
    }

    public void setStatus(Status status)
    {
        synchronized (this.status)
        {
            Status oldStatus = this.status;
            this.status = status;

            eventManager.handle(new StatusChangeEvent(this, status, oldStatus));
        }
    }

    public void setToken(String token)
    {
        if (getAccountType() == AccountType.BOT)
            this.token = "Bot " + token;
        else
            this.token = token;
    }

    public void verifyToken() throws LoginException, RateLimitedException
    {
        RestAction<JSONObject> login = new RestAction<JSONObject>(this, Route.Self.GET_SELF.compile(), null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
        try
        {
            userResponse = login.complete(false);
        }
        catch (RuntimeException e)
        {
            //We check if the LoginException is masked inside of a ExecutionException which is masked inside of the RuntimeException
            Throwable ex = e.getCause() != null ? e.getCause().getCause() : null;
            if (ex instanceof LoginException)
                throw (LoginException) ex;
            else
                throw e;
        }

        if (userResponse != null)
        {
            verifyToken(userResponse);
        }
        else
        {
            //If we received a null return for userResponse, then that means we hit a 401.
            // 401 occurs we attempt to access the users/@me endpoint with the wrong token prefix.
            // e.g: If we use a Client token and prefix it with "Bot ", or use a bot token and don't prefix it.
            // It also occurs when we attempt to access the endpoint with an invalid token.
            //The code below already knows that something is wrong with the token. We want to determine if it is invalid
            // or if the developer attempted to login with a token using the wrong AccountType.

            //If we attempted to login as a Bot, remove the "Bot " prefix and set the Requester to be a client.
            if (getAccountType() == AccountType.BOT)
            {
                token = token.replace("Bot ", "");
                requester = new Requester(this, AccountType.CLIENT);
            }
            else    //If we attempted to login as a Client, prepend the "Bot " prefix and set the Requester to be a Bot
            {
                token = "Bot " + token;
                requester = new Requester(this, AccountType.BOT);
            }

            try
            {
                //Now that we have reversed the AccountTypes, attempt to get User info again.
                userResponse = login.complete(false);
            }
            catch (RuntimeException e)
            {
                //We check if the LoginException is masked inside of a ExecutionException which is masked inside of the RuntimeException
                Throwable ex = e.getCause() != null ? e.getCause().getCause() : null;
                if (ex instanceof LoginException)
                    throw (LoginException) ex;
                else
                    throw e;
            }

            //If the response isn't null (thus it didn't 401) send it to the secondary verify method to determine
            // which account type the developer wrongly attempted to login as
            if (userResponse != null)
                verifyToken(userResponse);
            else    //We 401'd again. This is an invalid token
                throw new LoginException("The provided token is invalid!");
        }
    }

    private void verifyToken(JSONObject userResponse)
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

    @Override
    public String getToken()
    {
        return token;
    }

    @Override
    public HttpHost getGlobalProxy()
    {
        return proxy;
    }

    @Override
    public boolean isAudioEnabled()
    {
        return audioEnabled;
    }

    @Override
    public boolean isBulkDeleteSplittingEnabled()
    {
        return bulkDeleteSplittingEnabled;
    }

    @Override
    public void setAutoReconnect(boolean autoReconnect)
    {
        this.autoReconnect = autoReconnect;
        if (client != null)
        {
            client.setAutoReconnect(autoReconnect);
        }
    }

    @Override
    public boolean isAutoReconnect()
    {
        return autoReconnect;
    }

    @Override
    public Status getStatus()
    {
        return status;
    }

    @Override
    public long getPing()
    {
        return ping;
    }

    @Override
    public List<User> getUsers()
    {
        return Collections.unmodifiableList(new ArrayList<>(users.values()));
    }

    @Override
    public User getUserById(String id)
    {
        return users.get(id);
    }

    @Override
    public List<Guild> getMutualGuilds(User... users)
    {
        Args.notNull(users, "users");
        return getMutualGuilds(Arrays.asList(users));
    }

    @Override
    public List<Guild> getMutualGuilds(Collection<User> users)
    {
        Args.notNull(users, "users");
        for(User u : users)
        {
            Args.notNull(u, "All users");
        }
        return Collections.unmodifiableList(getGuilds().stream()
                .filter(guild -> users.stream().allMatch(guild::isMember))
                .collect(Collectors.toList()));
    }

    @Override
    public List<User> getUsersByName(String name, boolean ignoreCase)
    {
        return users.values().stream().filter(u ->
            ignoreCase
            ? name.equalsIgnoreCase(u.getName())
            : name.equals(u.getName()))
        .collect(Collectors.toList());
    }

    @Override
    public RestAction<User> retrieveUserById(String id)
    {
        if (accountType != AccountType.BOT)
            throw new AccountTypeException(AccountType.BOT);
        Args.notEmpty(id, "User id");

        // check cache
        User user = this.getUserById(id);
        if (user != null)
            return new RestAction.EmptyRestAction<>(user);

        Route.CompiledRoute route = Route.Users.GET_USER.compile(id);
        return new RestAction<User>(this, route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }
                JSONObject user = response.getObject();
                request.onSuccess(EntityBuilder.get(api).createFakeUser(user, false));
            }
        };
    }

    @Override
    public List<Guild> getGuilds()
    {
        return Collections.unmodifiableList(new ArrayList<>(guilds.values()));
    }

    @Override
    public Guild getGuildById(String id)
    {
        return guilds.get(id);
    }

    @Override
    public List<Guild> getGuildsByName(String name, boolean ignoreCase)
    {
        return guilds.values().stream().filter(g ->
                ignoreCase
                        ? name.equalsIgnoreCase(g.getName())
                        : name.equals(g.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        return Collections.unmodifiableList(new ArrayList<>(textChannels.values()));
    }

    @Override
    public TextChannel getTextChannelById(String id)
    {
        return textChannels.get(id);
    }

    @Override
    public List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase)
    {
        return textChannels.values().stream().filter(tc ->
                ignoreCase
                        ? name.equalsIgnoreCase(tc.getName())
                        : name.equals(tc.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(new ArrayList<>(voiceChannels.values()));
    }

    @Override
    public VoiceChannel getVoiceChannelById(String id)
    {
        return voiceChannels.get(id);
    }

    @Override
    public List<VoiceChannel> getVoiceChannelByName(String name, boolean ignoreCase)
    {
        return voiceChannels.values().stream().filter(vc ->
                ignoreCase
                        ? name.equalsIgnoreCase(vc.getName())
                        : name.equals(vc.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PrivateChannel> getPrivateChannels()
    {
        return Collections.unmodifiableList(new ArrayList<>(privateChannels.values()));
    }

    @Override
    public PrivateChannel getPrivateChannelById(String id)
    {
        return privateChannels.get(id);
    }

    @Override
    public List<Emote> getEmotes()
    {
        List<Emote> emotes = new ArrayList<>();
        getGuilds().parallelStream().forEach(g -> emotes.addAll(g.getEmotes()));
        return Collections.unmodifiableList(emotes);
    }

    @Override
    public List<Emote> getEmotesByName(String name, boolean ignoreCase)
    {
        List<Emote> emotes = new ArrayList<>();
        getGuilds().parallelStream().forEach(g -> emotes.addAll(g.getEmotesByName(name, ignoreCase)));
        return Collections.unmodifiableList(emotes);
    }

    @Override
    public Emote getEmoteById(String id)
    {
        for (Guild guild : getGuilds())
        {
            Emote emote = guild.getEmoteById(id);
            if (emote != null)
                return emote;
        }
        return null;
    }

    public SelfUser getSelfUser()
    {
        return selfUser;
    }

    @Override
    public void shutdown()
    {
        shutdown(true);
    }

    @Override
    public void shutdown(boolean free)
    {
        setStatus(Status.SHUTTING_DOWN);
        audioManagers.forEach((guildId, mng) -> mng.closeAudioConnection());
        if (AudioWebSocket.KEEP_ALIVE_POOLS.containsKey(this))
            AudioWebSocket.KEEP_ALIVE_POOLS.get(this).shutdownNow();
        getClient().setAutoReconnect(false);
        getClient().close();
        getRequester().shutdown();

        if (free)
        {
            try
            {
                Unirest.shutdown();
            }
            catch (IOException ignored) {}
        }
        setStatus(Status.SHUTDOWN);
    }

    @Override
    public JDAClient asClient()
    {
        if (getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);

        return jdaClient;
    }

    @Override
    public JDABot asBot()
    {
        if (getAccountType() != AccountType.BOT)
            throw new AccountTypeException(AccountType.BOT);

        return jdaBot;
    }

    @Override
    public long getResponseTotal()
    {
        return responseTotal;
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
    public void installAuxiliaryCable(int port) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Nice try m8!");
    }

    @Override
    public AccountType getAccountType()
    {
        return accountType;
    }

    @Override
    public void setEventManager(IEventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    @Override
    public void addEventListener(Object... listeners)
    {
        for (Object listener: listeners)
            eventManager.register(listener);
    }

    @Override
    public void removeEventListener(Object... listeners)
    {
        for (Object listener: listeners)
            eventManager.unregister(listener);
    }

    @Override
    public List<Object> getRegisteredListeners()
    {
        return Collections.unmodifiableList(eventManager.getRegisteredListeners());
    }

    public IAudioSendFactory getAudioSendFactory()
    {
        return audioSendFactory;
    }

    public void setAudioSendFactory(IAudioSendFactory factory)
    {
        Args.notNull(factory, "Provided IAudioSendFactory");
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

    public IEventManager getEventManager()
    {
        return eventManager;
    }

    public WebSocketFactory getWebSocketFactory()
    {
        return wsFactory;
    }

    public WebSocketClient getClient()
    {
        return client;
    }

    public HashMap<String, User> getUserMap()
    {
        return users;
    }

    public HashMap<String, Guild> getGuildMap()
    {
        return guilds;
    }

    public HashMap<String, TextChannel> getTextChannelMap()
    {
        return textChannels;
    }

    public HashMap<String, VoiceChannel> getVoiceChannelMap()
    {
        return voiceChannels;
    }

    public HashMap<String, PrivateChannel> getPrivateChannelMap()
    {
        return privateChannels;
    }

    public HashMap<String, User> getFakeUserMap()
    {
        return fakeUsers;
    }

    public HashMap<String, PrivateChannel> getFakePrivateChannelMap()
    {
        return fakePrivateChannels;
    }

    public HashMap<String, AudioManager> getAudioManagerMap()
    {
        return audioManagers;
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


}
