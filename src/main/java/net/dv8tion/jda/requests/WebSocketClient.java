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

import com.mashape.unirest.http.Unirest;
import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.handle.*;
import org.apache.http.HttpHost;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class WebSocketClient extends WebSocketAdapter
{
    private Thread keepAliveThread;
    private WebSocket socket;
    private boolean connected;
    private long keepAliveInterval;
    private final JDAImpl api;

    public WebSocketClient(String url, JDAImpl api)
    {
        this.api = api;
        WebSocketFactory factory = new WebSocketFactory();
        HttpHost proxy = api.getGlobalProxy();
        if (proxy != null)
        {
            ProxySettings settings = factory.getProxySettings();
            settings.setHost(proxy.getHostName());
            settings.setPort(proxy.getPort());
        }
        try
        {
            socket = factory.createSocket(url)
                    .addHeader("Accept-Encoding", "gzip")
                    .addListener(this)
                    .connect();
        }
        catch (IOException | WebSocketException e)
        {
            //Completely fail here. We couldn't make the connection.
            throw new RuntimeException(e);
        }
    }

    public void send(String message)
    {
        socket.sendText(message);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
    {
        JSONObject connectObj = new JSONObject()
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
                    .put("compress", true)); //Used to make the READY event be given as compressed binary data when over a certain size. TY @ShadowLordAlpha
        send(connectObj.toString());
        connected = true;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message)
    {
        JSONObject content = new JSONObject(message);
        String type = content.getString("t");
        int responseTotal = content.getInt("s");
        api.setResponseTotal(responseTotal);
        content = content.getJSONObject("d");
        if (type.equals("READY"))
        {
            keepAliveInterval = content.getLong("heartbeat_interval");
            keepAliveThread = new Thread(() -> {
                while (socket.isOpen()) {
                    send(new JSONObject().put("op", 1).put("d", System.currentTimeMillis()).toString());
                    try {
                        Thread.sleep(keepAliveInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
            });
            keepAliveThread.setDaemon(true);
            keepAliveThread.start();
        }

        try {
            switch (type) {
                case "READY":
                    new ReadyHandler(api, responseTotal).handle(content);
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
                default:
                    System.out.println("Unrecognized event:\n" + message);    //TODO: Replace with "we don't know this type"
            }
        }
        catch (JSONException ex)
        {
            System.err.println("Got an unexpected Json-parse error. Please redirect following message to the devs:");
            System.err.println('\t' + ex.getMessage());
            System.err.println('\t' + type + " -> " + content);
        }
        catch (IllegalArgumentException ex)
        {
            System.err.println("JDA encountered an internal error.");
            if (api.isDebug())
                ex.printStackTrace();
            else
                System.err.println(ex.getMessage());
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
        System.out.println("The connection was closed!");
        System.out.println("By remote? " + closedByServer);
        System.out.println("Reason: " + serverCloseFrame.getCloseReason());
        System.out.println("Close code: " + serverCloseFrame.getCloseCode());
        try
        {
            Unirest.shutdown();
        }
        catch (IOException e)
        {
            System.err.print("JDA's REST requester was unable to properly shutdown! This application will have to be force-quit'd!. Sorry.");
            e.printStackTrace();
        }
        connected = false;
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause)
    {
        handleCallbackError(websocket, cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause)
    {
        cause.printStackTrace();
    }

    public void close()
    {
        if (keepAliveThread != null)
        {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        socket.sendClose();
    }

    public boolean isConnected()
    {
        return connected;
    }
}
