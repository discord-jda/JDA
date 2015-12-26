/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.handle.*;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


public class WebSocketClient extends org.java_websocket.client.WebSocketClient
{
    private Thread keepAliveThread;
    private boolean connected;
    private long keepAliveInterval;
    private JDAImpl api;

    public WebSocketClient(String url, JDAImpl api, InetSocketAddress proxy)
    {
        super(URI.create(url));
        if (proxy != null)
            this.setProxy(proxy);
        if (url.startsWith("wss"))
        {
            try
            {
                SSLContext sslContext;
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                this.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
            }
            catch (NoSuchAlgorithmException | KeyManagementException e)
            {
                e.printStackTrace();
            }
        }
        this.api = api;
        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshake)
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
                    .put("v", 3));
        send(connectObj.toString());
        connected = true;
    }

    @Override
    public void onMessage(String message)
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
                while (!getConnection().isClosed())
                {
                    send(new JSONObject().put("op", 1).put("d", System.currentTimeMillis()).toString());
                    try
                    {
                        Thread.sleep(keepAliveInterval);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
            });
            keepAliveThread.setDaemon(true);
            keepAliveThread.start();
        }

        boolean printUnimplemented = true;    //TODO: Remove, just for development debug.
        try
        {
            switch (type)
            {
                case "READY":
                    new ReadyHandler(api, responseTotal).handle(content);
                    break;
                case "PRESENCE_UPDATE":
                    new PresenceUpdateHandler(api, responseTotal).handle(content);
                    break;
                case "TYPING_START":
                    new UserTypingHandler(api, responseTotal).handle(content);
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
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {
        System.out.println("The connection was closed!");
        System.out.println("By remote? " + remote);
        System.out.println("Reason: " + reason);
        System.out.println("Close code: " + code);
        connected = false;
    }

    @Override
    public void onError(Exception ex)
    {
        ex.printStackTrace();
    }

    @Override
    public void close()
    {
        if (keepAliveThread != null)
        {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        super.close();
    }

    public boolean isConnected()
    {
        return connected;
    }
}
