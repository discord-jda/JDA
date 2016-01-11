/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities.impl;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.handle.EntityBuilder;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.hooks.EventManager;
import net.dv8tion.jda.managers.AccountManager;
import net.dv8tion.jda.managers.GuildManager;
import net.dv8tion.jda.requests.Requester;
import net.dv8tion.jda.requests.WebSocketClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Represents the core of the Discord API. All functionality is connected through this.
 */
public class JDAImpl implements JDA
{
    private final HttpHost proxy;
    private final Map<String, User> userMap = new HashMap<>();
    private final Map<String, Guild> guildMap = new HashMap<>();
    private final Map<String, TextChannel> textChannelMap = new HashMap<>();
    private final Map<String, VoiceChannel> voiceChannelMap = new HashMap<>();
    private final Map<String, PrivateChannel> pmChannelMap = new HashMap<>();
    private final Map<String, String> offline_pms = new HashMap<>();    //Userid -> channelid
    private final EventManager eventManager = new EventManager();
    private SelfInfo selfInfo = null;
    private AccountManager accountManager;
    private String authToken = null;
    private WebSocketClient client;
    private final Requester requester = new Requester(this);
    private boolean debug;
    private boolean enableAck;
    private int responseTotal;

    public JDAImpl()
    {
        proxy = null;
    }

    public JDAImpl(String proxyUrl, int proxyPort)
    {
        if (proxyUrl == null || proxyUrl.isEmpty() || proxyPort == -1)
            throw new IllegalArgumentException("The provided proxy settings cannot be used to make a proxy. Settings: URL: '" + proxyUrl + "'  Port: " + proxyPort);
        proxy = new HttpHost(proxyUrl, proxyPort);
        Unirest.setProxy(proxy);
    }

    /**
     * Attempts to login to Discord.
     * Upon successful auth with Discord, a token is generated and stored in token.json.
     *
     * @param email
     *          The email of the account attempting to log in.
     * @param password
     *          The password of the account attempting to log in.
     * @throws IllegalArgumentException
     *          Thrown if this email or password provided are empty or null.
     * @throws LoginException
     *          Thrown if the email-password combination fails the auth check with the Discord servers.
     */
    public void login(String email, String password) throws IllegalArgumentException, LoginException
    {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            throw new IllegalArgumentException("The provided email or password as empty / null.");

        accountManager=new AccountManager(this, password);
        
        Path tokenFile = Paths.get("tokens.json");
        JSONObject configs = null;
        String gateway = null;
        if (Files.exists(tokenFile))
        {
            configs = readJson(tokenFile);
        }
        if (configs == null)
        {
            configs = new JSONObject().put("tokens", new JSONObject()).put("version", 1);
        }

        if (configs.getJSONObject("tokens").has(email))
        {
            try
            {
                authToken = configs.getJSONObject("tokens").getString(email);
                if (getRequester().get("https://discordapp.com/api/users/@me/guilds") == null)
                {
                    //token is valid (returns array, cant be returned as JSONObject)
                    gateway = getRequester().get("https://discordapp.com/api/gateway").getString("url");
                    System.out.println("Using cached Token: " + authToken);
                }
            }
            catch (JSONException ex)
            {
                System.out.println("Token-file misformatted. Please delete it for recreation");
            }
        }

        if (gateway == null)                                    //no token saved or invalid
        {
            try
            {
                authToken = null;
                JSONObject response = getRequester().post("https://discordapp.com/api/auth/login", new JSONObject().put("email", email).put("password", password));

                if (response == null || !response.has("token"))
                    throw new LoginException("The provided email / password combination was incorrect. Please provide valid details.");
                System.out.println("Login Successful!"); //TODO: Replace with Logger.INFO

                authToken = response.getString("token");
                configs.getJSONObject("tokens").put(email, authToken);
                System.out.println("Created new Token: " + authToken);

                gateway = getRequester().get("https://discordapp.com/api/gateway").getString("url");
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            System.out.println("Login Successful!"); //TODO: Replace with Logger.INFO
        }

        writeJson(tokenFile, configs);
        client = new WebSocketClient(gateway, this);
    }

    /**
     * Takes a provided json file, reads all lines and constructs a {@link org.json.JSONObject JSONObject} from it.
     *
     * @param file
     *          The json file to read.
     * @return
     *      The {@link org.json.JSONObject JSONObject} representation of the json in the file.
     */
    private static JSONObject readJson(Path file)
    {
        try
        {
            return new JSONObject(StringUtils.join(Files.readAllLines(file, StandardCharsets.UTF_8), ""));
        }
        catch (IOException e)
        {
            System.out.println("Error reading token-file. Defaulting to standard");
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            System.out.println("Token-file misformatted. Creating default one");
        }
        return null;
    }

    /**
     * Writes the json representation of the provided {@link org.json.JSONObject JSONObject} to the provided file.
     *
     * @param file
     *          The file which will have the json representation of object written into.
     * @param object
     *          The {@link org.json.JSONObject JSONObject} to write to file.
     */
    private static void writeJson(Path file, JSONObject object)
    {
        try
        {
            Files.write(file, Arrays.asList(object.toString(4).split("\n")), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e)
        {
            System.out.println("Error creating token-file");
        }
    }

    @Override
    public String getAuthToken()
    {
        return authToken;
    }

    public void setAuthToken(String token)
    {
        this.authToken = token;
    }

    @Override
    public void addEventListener(EventListener listener)
    {
        getEventManager().register(listener);
    }

    @Override
    public void removeEventListener(EventListener listener)
    {
        getEventManager().unregister(listener);
    }

    public EventManager getEventManager()
    {
        return eventManager;
    }

    public WebSocketClient getClient()
    {
        return client;
    }

    public Map<String, User> getUserMap()
    {
        return userMap;
    }

    @Override
    public List<User> getUsers()
    {
        return Collections.unmodifiableList(new LinkedList<>(userMap.values()));
    }

    @Override
    public User getUserById(String id)
    {
        return userMap.get(id);
    }

    @Override
    public List<User> getUsersByName(String name)
    {
        return Collections.unmodifiableList(
                userMap.values().stream().filter(
                        u -> u.getUsername().equals(name))
                        .collect(Collectors.toList()));
    }

    public Map<String, Guild> getGuildMap()
    {
        return guildMap;
    }

    @Override
    public List<Guild> getGuilds()
    {
        return Collections.unmodifiableList(new LinkedList<>(guildMap.values()));
    }

    @Override
    public List<Guild> getGuildsByName(String name)
    {
        return Collections.unmodifiableList(
                guildMap.values().stream().filter(
                        guild -> guild.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    @Override
    public List<TextChannel> getTextChannelsByName(String name)
    {
        return Collections.unmodifiableList(
                textChannelMap.values().stream().filter(
                        channel -> channel.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    @Override
    public List<VoiceChannel> getVoiceChannelByName(String name)
    {
        return Collections.unmodifiableList(
                voiceChannelMap.values().stream().filter(
                        channel -> channel.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    @Override
    public List<PrivateChannel> getPrivateChannels()
    {
        return Collections.unmodifiableList(new LinkedList<>(pmChannelMap.values()));
    }

    @Override
    public GuildManager createGuild(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Guild name must not be null");
        }
        JSONObject response = getRequester().post("https://discordapp.com/api/guilds",
                new JSONObject()).put("name", name);
        if (response == null || !response.has("id"))
        {
            //error creating guild
            throw new RuntimeException("Creating a new Guild failed. Reason: " + (response == null ? "Unknown" : response.toString()));
        }
        else
        {
            Guild g = new EntityBuilder(this).createGuild(response);
            return new GuildManager(g);
        }
    }

    @Override
    public void createGuildAsync(String name, Consumer<GuildManager> callback)
    {
        if (name == null)
            throw new IllegalArgumentException("Guild name must not be null");

        JSONObject response = getRequester().post("https://discordapp.com/api/guilds",
                new JSONObject().put("name", name));
        if (response == null || !response.has("id"))
        {
            //error creating guild
            throw new RuntimeException("Creating a new Guild failed. Reason: " + (response == null ? "Unknown" : response.toString()));
        }
        else
        {
            addEventListener(new AsyncCallback(callback, response.getString("id")));
        }
    }

    @Override
    public Guild getGuildById(String id)
    {
        return guildMap.get(id);
    }

    public Map<String, TextChannel> getChannelMap()
    {
        return textChannelMap;
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        return Collections.unmodifiableList(new LinkedList<>(textChannelMap.values()));
    }

    @Override
    public TextChannel getTextChannelById(String id)
    {
        return textChannelMap.get(id);
    }

    public Map<String, VoiceChannel> getVoiceChannelMap()
    {
        return voiceChannelMap;
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(new LinkedList<>(voiceChannelMap.values()));
    }

    @Override
    public VoiceChannel getVoiceChannelById(String id)
    {
        return voiceChannelMap.get(id);
    }

    @Override
    public PrivateChannel getPrivateChannelById(String id)
    {
        return pmChannelMap.get(id);
    }

    public Map<String, PrivateChannel> getPmChannelMap()
    {
        return pmChannelMap;
    }

    public Map<String, String> getOffline_pms()
    {
        return offline_pms;
    }

    @Override
    public SelfInfo getSelfInfo()
    {
        return selfInfo;
    }

    public void setSelfInfo(SelfInfo selfInfo)
    {
        this.selfInfo = selfInfo;
    }

    @Override
    public int getResponseTotal()
    {
        return responseTotal;
    }

    public void setResponseTotal(int responseTotal)
    {
        this.responseTotal = responseTotal;
    }

    public Requester getRequester()
    {
        return requester;
    }

    @Override
    public HttpHost getGlobalProxy()
    {
        return proxy;
    }

    @Override
    public AccountManager getAccountManager()
    {
        return accountManager;
    }

    @Override
    public void setDebug(boolean enableDebug)
    {
        this.debug = enableDebug;
    }

    @Override
    public boolean isDebug()
    {
        return debug;
    }

    /**
     * Enables or disables the ack functionality of JDA.<br>
     * <b>Read the Javadocs of {@link #ack(Message)}</b> for guidelines on how and when to ack!
     *
     * @param enable
     *      whether or not to enable ack functionality
     */
    public void setAllowAck(boolean enable)
    {
        this.enableAck = enable;
    }

    public boolean isAckAllowed()
    {
        return enableAck;
    }

    /**
     * Acks a specific message. This feature is disabled by default.
     * To enable them, call {@link #setAllowAck(boolean)}.<br>
     * IMPORTANT: It is highly discouraged to ack every message and may lead to rate-limits and other bad stuff.
     * Use this wisely (only if needed, or on a long enough interval).
     * Acking a specific Message also acks every Message before (only ack last one if possible)
     *
     * @param msg
     *      the message to ack
     */
    public void ack(Message msg)
    {
        if (!enableAck)
        {
            throw new RuntimeException("Acking is disabled by default. <b>READ THE JAVADOCS</b> for how to use them!");
        }
        getRequester().post("https://discordapp.com/api/channels/"+msg.getChannelId()+"/messages/"+msg.getId()+"/ack", new JSONObject());
    }

    private static class AsyncCallback implements EventListener
    {
        private final Consumer<GuildManager> cb;
        private final String id;

        public AsyncCallback(Consumer<GuildManager> cb, String guildId)
        {
            this.cb = cb;
            this.id = guildId;
        }

        @Override
        public void onEvent(Event event)
        {
            if (event instanceof GuildJoinEvent && ((GuildJoinEvent) event).getGuild().getId().equals(id))
            {
                event.getJDA().removeEventListener(this);
                cb.accept(((GuildJoinEvent) event).getGuild().getManager());
            }
        }
    }
}
