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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.requests;

import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.audio.AudioReceiveHandler;
import net.dv8tion.jda.audio.AudioSendHandler;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.TextChannelImpl;
import net.dv8tion.jda.events.*;
import net.dv8tion.jda.handle.*;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.utils.SimpleLog;
import org.apache.http.HttpHost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class WebSocketClient extends WebSocketAdapter implements WebSocketListener
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDASocket");

    protected final JDAImpl api;

    protected final int[] sharding;
    protected final HttpHost proxy;
    protected WebSocket socket;
    protected String gatewayUrl = null;

    protected String sessionId = null;

    protected volatile Thread keepAliveThread;
    protected boolean connected;

    protected boolean initiating;             //cache all events?
    protected final List<JSONObject> cachedEvents = new LinkedList<>();

    protected boolean shouldReconnect = true;
    protected int reconnectTimeoutS = 2;

    protected final List<VoiceChannel> dcAudioConnections = new LinkedList<>();
    protected final Map<String, AudioSendHandler> audioSendHandlers = new HashMap<>();
    protected final Map<String, AudioReceiveHandler> audioReceivedHandlers = new HashMap<>();

    protected boolean firstInit = true;

    protected WebSocketCustomHandler customHandler;

    public WebSocketClient(JDAImpl api, HttpHost proxy, int[] sharding)
    {
        this.api = api;
        this.sharding = sharding;
        this.proxy = proxy;
        connect();
    }

    public void setAutoReconnect(boolean reconnect)
    {
        this.shouldReconnect = reconnect;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void setCustomHandler(WebSocketCustomHandler customHandler)
    {
        this.customHandler = customHandler;
    }

    public void ready()
    {
        if (initiating)
        {
            initiating = false;
            reconnectTimeoutS = 2;
            if (firstInit)
            {
                firstInit = false;
                JDAImpl.LOG.info("Finished Loading!");
                if (api.getGuilds().size() >= 2500) //Show large warning when connected to >2500 guilds
                {
                    JDAImpl.LOG.warn(" __      __ _    ___  _  _  ___  _  _   ___  _ ");
                    JDAImpl.LOG.warn(" \\ \\    / //_\\  | _ \\| \\| ||_ _|| \\| | / __|| |");
                    JDAImpl.LOG.warn("  \\ \\/\\/ // _ \\ |   /| .` | | | | .` || (_ ||_|");
                    JDAImpl.LOG.warn("   \\_/\\_//_/ \\_\\|_|_\\|_|\\_||___||_|\\_| \\___|(_)");
                    JDAImpl.LOG.warn("You're running a session with over 2500 connected");
                    JDAImpl.LOG.warn("guilds. You should shard the connection in order");
                    JDAImpl.LOG.warn("to split the load or things like resuming");
                    JDAImpl.LOG.warn("connection might not work as expected.");
                    JDAImpl.LOG.warn("For more info see https://git.io/vrFWP");
                }
                api.getEventManager().handle(new ReadyEvent(api, api.getResponseTotal()));
            }
            else
            {
                restoreAudioHandlers();
                reconnectAudioConnections();
                JDAImpl.LOG.info("Finished (Re)Loading!");
                api.getEventManager().handle(new ReconnectedEvent(api, api.getResponseTotal()));
            }
        }
        else
        {
            reconnectAudioConnections();
            JDAImpl.LOG.info("Successfully resumed Session!");
            api.getEventManager().handle(new ResumedEvent(api, api.getResponseTotal()));
        }
        api.setStatus(JDA.Status.CONNECTED);
        LOG.debug("Resending " + cachedEvents.size() + " cached events...");
        handle(cachedEvents);
        LOG.debug("Sending of cached events finished.");
        cachedEvents.clear();
    }

    public boolean isReady()
    {
        return !initiating;
    }

    public void handle(List<JSONObject> events)
    {
        events.forEach(this::handleEvent);
    }

    public void send(String message)
    {
        LOG.trace("<- " + message);
        socket.sendText(message);
    }

    public void close()
    {
        socket.sendClose(1000);
    }

    /*
        ### Start Internal methods ###
     */

    protected void connect()
    {
        if (api.getStatus() != JDA.Status.ATTEMPTING_TO_RECONNECT)
            api.setStatus(JDA.Status.CONNECTING_TO_WEBSOCKET);
        initiating = true;
        WebSocketFactory factory = new WebSocketFactory();
        if (proxy != null)
        {
            ProxySettings settings = factory.getProxySettings();
            settings.setHost(proxy.getHostName());
            settings.setPort(proxy.getPort());
        }
        try
        {
            if (gatewayUrl == null)
            {
                gatewayUrl = getGateway();
                if (gatewayUrl == null)
                {
                    throw new RuntimeException("Could not fetch WS-Gateway!");
                }
            }
            socket = factory.createSocket(gatewayUrl)
                    .addHeader("Accept-Encoding", "gzip")
                    .addListener(this);
            socket.connect();
        }
        catch (IOException | WebSocketException e)
        {
            //Completely fail here. We couldn't make the connection.
            throw new RuntimeException(e);
        }
    }

    protected String getGateway()
    {
        try
        {
            return api.getRequester().get(Requester.DISCORD_API_PREFIX + "gateway").getObject().getString("url") + "?encoding=json&v=6";
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
    {
        api.setStatus(JDA.Status.LOADING_SUBSYSTEMS);
        LOG.info("Connected to WebSocket");
        if (sessionId == null)
        {
            sendIdentify();
        }
        else
        {
            sendResume();
        }
        connected = true;
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
    {
        connected = false;
        api.setStatus(JDA.Status.DISCONNECTED);
        if (keepAliveThread != null)
        {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        if (!shouldReconnect)        //we should not reconnect
        {
            LOG.info("The connection was closed!");
            LOG.info("By remote? " + closedByServer);
            if (serverCloseFrame != null)
            {
                LOG.info("Reason: " + serverCloseFrame.getCloseReason());
                LOG.info("Close code: " + serverCloseFrame.getCloseCode());
            }
            api.setStatus(JDA.Status.SHUTDOWN);
            api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), dcAudioConnections));
        }
        else
        {
            for (AudioManager mng : api.getAudioManagersMap().values())
            {
                AudioManagerImpl mngImpl = (AudioManagerImpl) mng;
                VoiceChannel channel = null;
                if (mngImpl.isConnected())
                {
                    //This causes the AudioDisconnectEvent to be fired.. maybe we should reevaluate that.
                    channel = mng.getConnectedChannel();
                    mngImpl.closeAudioConnection();
                }
                else if (mngImpl.isAttemptingToConnect())
                    channel = mng.getQueuedAudioConnection();
                else if (mngImpl.wasUnexpectedlyDisconnected())
                    channel = mngImpl.getUnexpectedDisconnectedChannel();

                if (channel != null)
                {
                    dcAudioConnections.add(channel);
                }
            }
            api.getEventManager().handle(new DisconnectEvent(api, serverCloseFrame, clientCloseFrame, closedByServer, OffsetDateTime.now(), dcAudioConnections));
            reconnect();
        }
    }

    protected void reconnect()
    {
        LOG.warn("Got disconnected from WebSocket (Internet?!)... Attempting to reconnect in " + reconnectTimeoutS + "s");
        while(shouldReconnect)
        {
            try
            {
                api.setStatus(JDA.Status.WAITING_TO_RECONNECT);
                Thread.sleep(reconnectTimeoutS * 1000);
                api.setStatus(JDA.Status.ATTEMPTING_TO_RECONNECT);
            }
            catch(InterruptedException ignored) {}
            LOG.warn("Attempting to reconnect!");
            try
            {
                connect();
                break;
            }
            catch (RuntimeException ex)
            {
                reconnectTimeoutS = Math.min(reconnectTimeoutS << 1, 900);      //*2, cap at 15min max
                LOG.warn("Reconnect failed! Next attempt in " + reconnectTimeoutS + "s");
            }
        }
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message)
    {
        JSONObject content = new JSONObject(message);
        int opCode = content.getInt("op");

        if (content.has("s") && !content.isNull("s"))
        {
            api.setResponseTotal(content.getInt("s"));
        }

        //Allows for custom event handling.
        if (customHandler != null && customHandler.handle(content))
            return;

        switch (opCode)
        {
            case 0:
                handleEvent(content);
                break;
            case 1:
                LOG.debug("Got Keep-Alive request (OP 1). Sending response...");
                sendKeepAlive();
                break;
            case 7:
                LOG.debug("Got Reconnect request (OP 7). Closing connection now...");
                close();
                break;
            case 9:
                LOG.debug("Got Invalidate request (OP 9). Invalidating...");
                invalidate();
                sendIdentify();
                break;
            case 10:
                LOG.debug("Got HELLO packet (OP 10). Initializing keep-alive.");
                setupKeepAlive(content.getJSONObject("d").getLong("heartbeat_interval"));
                break;
            case 11:
                LOG.trace("Got Heartbeat Ack (OP 11).");
                break;
            default:
                LOG.debug("Got unknown op-code: " + opCode + " with content: " + message);
        }
    }

    protected void setupKeepAlive(long timeout)
    {
        keepAliveThread = new Thread(() ->
        {
            while (connected)
            {
                try
                {
                    sendKeepAlive();

                    //Sleep for heartbeat interval
                    Thread.sleep(timeout);
                }
                catch (InterruptedException ex)
                {
                    //connection got cut... terminating keepAliveThread
                    break;
                }
            }
        });
        keepAliveThread.setName("JDA MainWS-KeepAlive" + (sharding != null
                    ? " Shard [" + sharding[0] + " / " + sharding[1] + "]"
                    : ""));
        keepAliveThread.setPriority(Thread.MAX_PRIORITY);
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    protected void sendKeepAlive()
    {
        send(new JSONObject().put("op", 1).put("d", api.getResponseTotal()).toString());
    }

    protected void sendIdentify()
    {
        LOG.debug("Sending Identify-packet...");
        JSONObject identify = new JSONObject()
                .put("op", 2)
                .put("d", new JSONObject()
                        .put("token", api.getAuthToken())
                        .put("properties", new JSONObject()
                                .put("$os", System.getProperty("os.name"))
                                .put("$browser", "JDA")
                                .put("$device", "")
                                .put("$referring_domain", "")
                                .put("$referrer", "")
                        )
                        .put("v", 5)
                        .put("large_threshold", 250)
                        .put("compress", true));
        if (sharding != null)
        {
            identify.getJSONObject("d").put("shard", new JSONArray().put(sharding[0]).put(sharding[1]));
        }
        send(identify.toString()); //Used to make the READY event be given as compressed binary data when over a certain size. TY @ShadowLordAlpha
    }

    protected void sendResume()
    {
        LOG.debug("Sending Resume-packet...");
        send(new JSONObject()
                .put("op", 6)
                .put("d", new JSONObject()
                        .put("session_id", sessionId)
                        .put("token", api.getAuthToken())
                        .put("seq", api.getResponseTotal()))
                .toString());
    }

    protected void invalidate()
    {
        sessionId = null;

        //Preserve the audio handlers through registry invalidation
        api.getAudioManagersMap().values().forEach(
            mng ->
            {
                String guildId = mng.getGuild().getId();
                if (mng.getSendingHandler() != null)
                    audioSendHandlers.put(guildId, mng.getSendingHandler());
                if (mng.getReceiveHandler() != null)
                    audioReceivedHandlers.put(guildId, mng.getReceiveHandler());
            }
        );

        //clearing the registry...
        api.getAudioManagersMap().clear();
        api.getChannelMap().clear();
        api.getVoiceChannelMap().clear();
        api.getGuildMap().clear();
        api.getUserMap().clear();
        api.getPmChannelMap().clear();
        api.getOffline_pms().clear();
        new EntityBuilder(api).clearCache();
        new ReadyHandler(api, 0).clearCache();
        EventCache.get(api).clear();
        GuildLock.get(api).clear();
        TextChannelImpl.AsyncMessageSender.stopAll(api);
    }

    protected void restoreAudioHandlers()
    {
        LOG.trace("Restoring cached AudioHandlers.");

        audioSendHandlers.forEach((guildId, handler) ->
        {
            Guild guild = api.getGuildMap().get(guildId);
            if (guild != null)
            {
                AudioManager mng = api.getAudioManager(guild);
                mng.setSendingHandler(handler);
            }
            else
            {
                LOG.warn("Could not restore an AudioSendHandler after reconnect due to the Guild it was connected to " +
                        "no longer existing in JDA's registry. Guild Id: " + guildId);
            }
        });

        audioReceivedHandlers.forEach((guildId, handler) ->
        {
            Guild guild = api.getGuildMap().get(guildId);
            if (guild != null)
            {
                AudioManager mng = api.getAudioManager(guild);
                mng.setReceivingHandler(handler);
            }
            else
            {
                LOG.warn("Could not restore an AudioReceiveHandler after reconnect due to the Guild it was connected to " +
                        "no longer existing in JDA's registry. Guild Id: " + guildId);
            }
        });
        audioSendHandlers.clear();
        audioReceivedHandlers.clear();
        LOG.trace("Finished restoring cached AudioHandlers");
    }

    protected void reconnectAudioConnections()
    {
        if (dcAudioConnections.size() == 0)
            return;

        LOG.trace("Cleaning up previous Audio Connections.");
        for (VoiceChannel chan : dcAudioConnections)
        {
            JSONObject obj = new JSONObject()
                    .put("op", 4)
                    .put("d", new JSONObject()
                            .put("guild_id", chan.getGuild().getId())
                            .put("channel_id", JSONObject.NULL)
                            .put("self_mute", false)
                            .put("self_deaf", false)
                    );
            send(obj.toString());
        }

        LOG.trace("Attempting to reconnect previous Audio Connections...");
        for (VoiceChannel chan : dcAudioConnections)
        {
            String guildId = chan.getGuild().getId();
            String chanId = chan.getId();

            Guild guild = api.getGuildMap().get(guildId);
            if (guild == null)
            {
                JDAImpl.LOG.warn("Could not reestablish audio connection during reconnect due to the previous " +
                        "connection being connected to a Guild that we are no longer connected to. " +
                        "Guild Id: " + guildId
                );
                continue;
            }

            VoiceChannel channel = api.getVoiceChannelMap().get(chanId);
            if (channel == null)
            {
                JDAImpl.LOG.warn("Could not reestablish audio connection during reconnect due to the previous " +
                        "connection being connected to a VoiceChannel that no longer exists. " +
                        "VChannel Id: " + chanId);
                continue;
            }


            AudioManager manager = api.getAudioManager(guild);
            manager.openAudioConnection(channel);
        }
        LOG.debug("Finished sending packets to reopen previous Audio Connections.");
        dcAudioConnections.clear();
    }

    protected void handleEvent(JSONObject raw)
    {
        String type = raw.getString("t");
        int responseTotal = api.getResponseTotal();

        if (type.equals("GUILD_MEMBER_ADD"))
            GuildMembersChunkHandler.modifyExpectedGuildMember(api, raw.getJSONObject("d").getString("guild_id"), 1);
        if (type.equals("GUILD_MEMBER_REMOVE"))
            GuildMembersChunkHandler.modifyExpectedGuildMember(api, raw.getJSONObject("d").getString("guild_id"), -1);

        if (initiating && !(type.equals("READY") || type.equals("GUILD_MEMBERS_CHUNK") || type.equals("GUILD_CREATE") || type.equals("RESUMED")))
        {
            LOG.debug("Caching " + type + " event during init!");
            cachedEvents.add(raw);
            return;
        }

        // Needs special handling due to content of "d" being an array
        if(type.equals("PRESENCE_REPLACE"))
        {
            JSONArray presences = raw.getJSONArray("d");
            LOG.trace(String.format("%s -> %s", type, presences.toString()));
            PresenceUpdateHandler handler = new PresenceUpdateHandler(api, responseTotal);
            for (int i = 0; i < presences.length(); i++)
            {
                JSONObject presence = presences.getJSONObject(i);
                handler.handle(presence);
            }
            return;
        }

        JSONObject content = raw.getJSONObject("d");
        LOG.trace(String.format("%s -> %s", type, content.toString()));

        try {
            switch (type) {
                //INIT types
                case "READY":
                    sessionId = content.getString("session_id");
                    new ReadyHandler(api, responseTotal).handle(raw);
                    break;
                case "RESUMED":
                    initiating = false;
                    ready();
                    break;
                case "GUILD_MEMBERS_CHUNK":
                    new GuildMembersChunkHandler(api, responseTotal).handle(raw);
                    break;
                case "PRESENCE_UPDATE":
                    new PresenceUpdateHandler(api, responseTotal).handle(raw);
                    break;
                case "TYPING_START":
                    new UserTypingHandler(api, responseTotal).handle(raw);
                    break;
                case "MESSAGE_CREATE":
                    new MessageReceivedHandler(api, responseTotal).handle(raw);
                    break;
                case "MESSAGE_UPDATE":
                    if (content.has("author"))
                        new MessageUpdateHandler(api, responseTotal).handle(raw);
                    else
                        new MessageEmbedHandler(api, responseTotal).handle(raw);
                    break;
                case "MESSAGE_DELETE":
                    new MessageDeleteHandler(api, responseTotal).handle(raw);
                    break;
                case "MESSAGE_DELETE_BULK":
                    new MessageBulkDeleteHandler(api, responseTotal).handle(raw);
                    break;
                case "VOICE_STATE_UPDATE":
                    new VoiceChangeHandler(api, responseTotal).handle(raw);
                    break;
                case "VOICE_SERVER_UPDATE":
                    if (api.isAudioEnabled())
                        new VoiceServerUpdateHandler(api, responseTotal).handle(raw);
                    else
                        LOG.debug("Received VOICE_SERVER_UPDATE event but ignoring due to audio being disabled/not supported.");
                    break;
                case "CHANNEL_CREATE":
                    new ChannelCreateHandler(api, responseTotal).handle(raw);
                    break;
                case "CHANNEL_UPDATE":
                    new ChannelUpdateHandler(api, responseTotal).handle(raw);
                    break;
                case "CHANNEL_DELETE":
                    new ChannelDeleteHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_CREATE":
                    new GuildJoinHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_UPDATE":
                    new GuildUpdateHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_DELETE":
                    new GuildLeaveHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_MEMBER_ADD":
                    new GuildMemberAddHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_MEMBER_UPDATE":
                    new GuildMemberUpdateHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_MEMBER_REMOVE":
                    new GuildMemberRemoveHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_BAN_ADD":
                    new GuildMemberBanHandler(api, responseTotal, true).handle(raw);
                    break;
                case "GUILD_BAN_REMOVE":
                    new GuildMemberBanHandler(api, responseTotal, false).handle(raw);
                    break;
                case "GUILD_ROLE_CREATE":
                    new GuildRoleCreateHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_ROLE_UPDATE":
                    new GuildRoleUpdateHandler(api, responseTotal).handle(raw);
                    break;
                case "GUILD_ROLE_DELETE":
                    new GuildRoleDeleteHandler(api, responseTotal).handle(raw);
                    break;
                case "USER_UPDATE":
                    new UserUpdateHandler(api, responseTotal).handle(raw);
                    break;
                case "USER_GUILD_SETTINGS_UPDATE":
                    //TODO: handle notification updates...
                    break;
                //Events that Bots shouldn't care about.
                case "MESSAGE_ACK":
                    break;
                default:
                    LOG.debug("Unrecognized event:\n" + raw);
            }
        }
        catch (JSONException ex)
        {
            LOG.warn("Got an unexpected Json-parse error. Please redirect following message to the devs:\n\t"
                    + ex.getMessage() + "\n\t" + type + " -> " + content);
        }
        catch (Exception ex)
        {
            LOG.log(ex);
        }
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws UnsupportedEncodingException, DataFormatException
    {
        //Thanks to ShadowLordAlpha for code and debugging.
        //Get the compressed message and inflate it
        StringBuilder builder = new StringBuilder();
        Inflater decompresser = new Inflater();
        decompresser.setInput(binary, 0, binary.length);
        byte[] result = new byte[128];
        while(!decompresser.finished())
        {
            int resultLength = decompresser.inflate(result);
            builder.append(new String(result, 0, resultLength, "UTF-8"));
        }
        decompresser.end();

        // send the inflated message to the TextMessage method
        onTextMessage(websocket, builder.toString());
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception
    {
        handleCallbackError(websocket, cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause)
    {
        LOG.log(cause);
    }
}
