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
package net.dv8tion.jda.requests;

import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.DisconnectEvent;
import net.dv8tion.jda.events.ShutdownEvent;
import net.dv8tion.jda.handle.*;
import net.dv8tion.jda.utils.SimpleLog;
import org.apache.http.HttpHost;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class WebSocketClient extends WebSocketAdapter
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDASocket");
    private Thread keepAliveThread;
    private WebSocket socket;
    private long keepAliveInterval;
    private final JDAImpl api;
    private final HttpHost proxy;
    private String sessionId;
    private boolean ready = false;
    private boolean connected;
    private final List<String> cachedEvents = new LinkedList<>();
    private String url = null;
    private int reconnectTimeout = 2;
    private boolean reconnecting = false;           //for internal information (op7)
    private boolean shouldReconnect = false;        //for configuration (connection loss)

    public WebSocketClient(JDAImpl api, HttpHost proxy)
    {
        this.api = api;
        this.proxy = proxy;
        connect();
    }

    public void send(String message)
    {
        socket.sendText(message);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
    {
        JSONObject connectObj;
        LOG.info("Connected to WebSocket");
        if(!reconnecting)
        {
            connectObj = new JSONObject()
                    .put("op", 2)
                    .put("d", new JSONObject()
                            .put("token", api.getAuthToken())
                            .put("properties", new JSONObject()
                                    .put("$os", System.getProperty("os.name"))
                                    .put("$browser", "Java Discord API")
                                    .put("$device", "")
                                    .put("$referring_domain", "t.co")
                                    .put("$referrer", "")
                            )
                            .put("v", 3)
                            .put("large_threshold", 250)
                            .put("compress", true)); //Used to make the READY event be given as compressed binary data when over a certain size. TY @ShadowLordAlpha
        }
        else
        {
            connectObj = new JSONObject()
                    .put("op", 6)
                    .put("d", new JSONObject()
                            .put("session_id", sessionId)
                            .put("seq", api.getResponseTotal()));
        }
        send(connectObj.toString());
        reconnecting = false;
        connected = true;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message)
    {
        JSONObject content = new JSONObject(message);

        if (content.getInt("op") == 7)
        {
            url = content.getJSONObject("d").getString("url");
            reconnecting = true;
            close();
            return;
        }

        String type = content.getString("t");
        int responseTotal = content.getInt("s");
        api.setResponseTotal(responseTotal);
        content = content.getJSONObject("d");
        if (type.equals("READY") || type.equals("RESUMED"))
        {
            keepAliveInterval = content.getLong("heartbeat_interval");
            keepAliveThread = new Thread(() -> {
                while (connected) {
                    try {
                        send(new JSONObject().put("op", 1).put("d", System.currentTimeMillis()).toString());
                        Thread.sleep(keepAliveInterval);
                    } catch (InterruptedException ignored) {}
                    catch (Exception ex) {
                        //connection got cut... terminating keepAliveThread
                        break;
                    }
                }
            });
            keepAliveThread.setDaemon(true);
            keepAliveThread.start();
        }

        LOG.trace(String.format("%s -> %s\n", type, content.toString()));
        if (!ready && !(type.equals("READY") || type.equals("GUILD_MEMBERS_CHUNK")))
        {
            cachedEvents.add(message);
            return;
        }

        try {
            switch (type) {
                case "READY":
                    sessionId = content.getString("session_id");
                    new ReadyHandler(api, ready, responseTotal).handle(content);
                case "RESUMED":
                    reconnectTimeout = 2;
                    break;
                case "GUILD_MEMBERS_CHUNK":
                    new GuildMembersChunkHandler(api, responseTotal).handle(content);
                    break;
                case "PRESENCE_UPDATE":
                    new PresenceUpdateHandler(api, responseTotal).handle(content);
                    break;
                case "TYPING_START":
                    new UserTypingHandler(api, responseTotal).handle(content);
                    break;
                case "MESSAGE_ACK":
                    new MessageAcknowledgedHandler(api, responseTotal).handle(content);
                    break;
                case "MESSAGE_CREATE":
                    new MessageReceivedHandler(api, responseTotal).handle(content);
                    break;
                case "MESSAGE_UPDATE":
                    if (content.has("author"))
                        new MessageUpdateHandler(api, responseTotal).handle(content);
                    else
                        new MessageEmbedHandler(api, responseTotal).handle(content);
                    break;
                case "MESSAGE_DELETE":
                    new MessageDeleteHandler(api, responseTotal).handle(content);
                    break;
                case "VOICE_STATE_UPDATE":
                    new VoiceChangeHandler(api, responseTotal).handle(content);
                    break;
                case "VOICE_SERVER_UPDATE":
                    new VoiceServerUpdateHandler(api, responseTotal).handle(content);
                    break;
                case "CHANNEL_CREATE":
                    new ChannelCreateHandler(api, responseTotal).handle(content);
                    break;
                case "CHANNEL_UPDATE":
                    new ChannelUpdateHandler(api, responseTotal).handle(content);
                    break;
                case "CHANNEL_DELETE":
                    new ChannelDeleteHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_CREATE":
                    new GuildJoinHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_UPDATE":
                    new GuildUpdateHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_DELETE":
                    new GuildLeaveHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_MEMBER_ADD":
                    new GuildMemberAddHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_MEMBER_UPDATE":
                    new GuildMemberRoleHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_MEMBER_REMOVE":
                    new GuildMemberRemoveHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_BAN_ADD":
                    new GuildMemberBanHandler(api, responseTotal, true).handle(content);
                    break;
                case "GUILD_BAN_REMOVE":
                    new GuildMemberBanHandler(api, responseTotal, false).handle(content);
                    break;
                case "GUILD_ROLE_CREATE":
                    new GuildRoleCreateHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_ROLE_UPDATE":
                    new GuildRoleUpdateHandler(api, responseTotal).handle(content);
                    break;
                case "GUILD_ROLE_DELETE":
                    new GuildRoleDeleteHandler(api, responseTotal).handle(content);
                    break;
                case "USER_UPDATE":
                    new UserUpdateHandler(api, responseTotal).handle(content);
                    break;
                case "USER_GUILD_SETTINGS_UPDATE":
                    //TODO: handle notification updates...
                    break;
                default:
                    LOG.debug("Unrecognized event:\n" + message);    //TODO: Replace with "we don't know this type"
            }
        }
        catch (JSONException ex)
        {
            LOG.warn("Got an unexpected Json-parse error. Please redirect following message to the devs:\n\t"
                    + ex.getMessage() + "\n\t" + type + " -> " + content);
        }
        catch (IllegalArgumentException ex)
        {
            LOG.fatal("JDA encountered an internal error.");
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
        byte[] result = new byte[100];
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
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
    {
        connected = false;
        if (keepAliveThread != null)
        {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        if (reconnecting)           //we issued a reconnect (got op 7)
        {
            connect();
        }
        else if (!shouldReconnect)        //we should not reconnect
        {
            LOG.info("The connection was closed!");
            LOG.info("By remote? " + closedByServer);
            if (serverCloseFrame != null)
            {
                LOG.info("Reason: " + serverCloseFrame.getCloseReason());
                LOG.info("Close code: " + serverCloseFrame.getCloseCode());
            }
            api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now()));
        }
        else
        {
            api.getEventManager().handle(new DisconnectEvent(api, serverCloseFrame, clientCloseFrame, closedByServer, OffsetDateTime.now()));
            reconnect();
        }
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause)
    {
        handleCallbackError(websocket, cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause)
    {
        LOG.log(cause);
    }

    private void connect()
    {
        WebSocketFactory factory = new WebSocketFactory();
        if (proxy != null)
        {
            ProxySettings settings = factory.getProxySettings();
            settings.setHost(proxy.getHostName());
            settings.setPort(proxy.getPort());
        }
        try
        {
            if (url == null)
            {
                url = getGateway();
                if (url == null)
                {
                    throw new RuntimeException();
                }
            }
            socket = factory.createSocket(url)
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

    private void reconnect()
    {
//        reconnecting = true;      //we don't want to send resume headers
        LOG.warn("Got disconnected from WebSocket (Internet?!)... Attempting to reconnect in " + reconnectTimeout + "s");
        url = null;         //force refetch of gateway
        while(shouldReconnect)
        {
            try
            {
                Thread.sleep(reconnectTimeout * 1000);
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
                reconnectTimeout <<= 1;         //*2 each time
                LOG.warn("Reconnect failed! Next attempt in " + reconnectTimeout + "s");
            }
        }
    }

    private String getGateway()
    {
        try
        {
            return api.getRequester().get("https://discordapp.com/api/gateway").getString("url");
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public void close()
    {
        socket.sendClose();
    }

    public void setAutoReconnect(boolean reconnect)
    {
        this.shouldReconnect = reconnect;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void ready()
    {
        ready = true;
        LOG.debug("Resending Cached events...");
        for (String event : cachedEvents)
        {
            onTextMessage(socket, event);
        }
        cachedEvents.clear();
        LOG.debug("Sending of cached events finished.");
    }
}
