/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.RequestBuilder;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.requests.WebSocketClient;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.http.HttpHost;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class JDAImpl implements JDA
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDA");

    protected final HashMap<String, User> users = new HashMap<>(200);
    protected final HashMap<String, Guild> guilds = new HashMap<>(10);
    protected final HashMap<String, TextChannel> textChannels = new HashMap<>();
    protected final HashMap<String, VoiceChannel> voiceChannels = new HashMap<>();
    protected final HashMap<String, PrivateChannel> privateChannels = new HashMap<>();

    protected HttpHost proxy;
    protected WebSocketClient client;
    protected Requester requester = new Requester();
    protected Status status = Status.INITIALIZING;
    protected SelfInfo selfInfo;
    protected ShardInfo shardInfo;
    protected String token = null;
    protected boolean audioEnabled;
    protected boolean useShutdownHook;
    protected boolean bulkDeleteSplittingEnabled;
    protected boolean autoReconnect;
    protected long responseTotal;

    public JDAImpl(HttpHost proxy, boolean autoReconnect, boolean audioEnabled, boolean useShutdownHook, boolean bulkDeleteSplittingEnabled)
    {
        this.proxy = proxy;
        this.autoReconnect = autoReconnect;
        this.audioEnabled = audioEnabled;
        this.useShutdownHook = useShutdownHook;
        this.bulkDeleteSplittingEnabled = bulkDeleteSplittingEnabled;

        if (audioEnabled)
            ;   //TODO: setup audio system
    }

    public void login(String token, ShardInfo shardInfo) throws LoginException
    {
        setStatus(Status.LOGGING_IN);
        if (token == null || token.isEmpty())
            throw new LoginException("Provided token was null or empty!");

        verifyToken(token);
        this.token = token;
        this.shardInfo = shardInfo;
        LOG.info("Login Successful!");

        //TODO: Implement sharding
        client = new WebSocketClient(this);

        if (useShutdownHook)
        {
            Runtime.getRuntime().addShutdownHook(new Thread("JDA Shutdown Hook")
            {
                @Override
                public void run()
                {
                    JDAImpl.this.shutdown();
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

            //TODO: Fire status change event.
//            eventManager.handle(new StatusChangeEvent(this, status, oldStatus));
        }
    }

    public void setAuthToken(String token)
    {
        this.token = token;
    }

    public void verifyToken(String token) throws LoginException
    {
        Request request = new RequestBuilder(RequestBuilder.RequestType.GET)
                .setUrl(Requester.DISCORD_API_PREFIX + "users/@me")
                .addHeader("authorization", token)
                .build();
        Requester.Response response = requester.handle(request);
        if (response.isOk())
        {
            JSONObject json = response.getObject();
            if (getAccountType() == AccountType.BOT)
            {
                if (!json.has("bot") || !json.getBoolean("bot"))
                    throw new AccountTypeException(AccountType.BOT, "Attempted to login as a BOT with a CLIENT token!");
            }
            else
            {
                if (json.has("bot") && json.getBoolean("bot"))
                    throw new AccountTypeException(AccountType.CLIENT, "Attempted to login as a CLIENT with a BOT token!");
            }
        }
        else if (response.isRateLimit())
            throw new RateLimitedException("auth/login");//TODO: Do more here somehow.
        else
        {
            if (response.code == 401)
            {
                throw new LoginException("The provided token was invalid!");
            }
            else
            {
                throw new LoginException("When verifying the authenticity of the provided token, Discord returned an unknown response:\n" +
                        response.toString());
            }
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
    public List<User> getUsersByName(String name, boolean ignoreCase)
    {
        return users.values().stream().filter(u ->
            ignoreCase
            ? name.equalsIgnoreCase(u.getName())
            : name.equals(u.getName()))
        .collect(Collectors.toList());
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
    public SelfInfo getSelfInfo()
    {
        return selfInfo;
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
        //TODO: Shutdown ASYNC system
        //TODO: Shutdown audio connections.
        //TODO: Shutdown Main Websocket.

        if (free)
        {
            try
            {
                Unirest.shutdown();
            }
            catch (IOException ignored) {}
        }
        setStatus(Status.SHUTTING_DOWN);
    }

    @Override
    public JDAClient asClient()
    {
        throw new AccountTypeException(AccountType.BOT);
    }

    @Override
    public JDABot asBot()
    {
        throw new AccountTypeException(AccountType.CLIENT);
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
    public void installAuxiliaryCable(int port) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Nice try m8!");
    }

    public void setResponseTotal(int responseTotal)
    {
        this.responseTotal = responseTotal;
    }

    public Requester getRequester()
    {
        return requester;
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

    public void setSelfInfo(SelfInfo selfInfo)
    {
        this.selfInfo = selfInfo;
    }
}
