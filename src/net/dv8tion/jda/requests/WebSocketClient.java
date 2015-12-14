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

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.handle.MessageCreateHandler;
import net.dv8tion.jda.handle.PresenceUpdateHandler;
import net.dv8tion.jda.handle.ReadyHandler;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;


public class WebSocketClient extends org.java_websocket.client.WebSocketClient
{

    private boolean connected;
    private long keepAliveInterval;
    private JDA api;

    public WebSocketClient(String url, JDA api)
    {
        super(URI.create(url.replace("wss", "ws")));
        this.api = api;
        this.connect();
        // TODO Auto-generated constructor stub
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
        int responseTotal = content.getInt("s");    //TODO: Give this to the handlers so they can pass to events.
        content = content.getJSONObject("d");
        if (type.equals("READY"))
        {
            keepAliveInterval = content.getLong("heartbeat_interval");
            new Thread(() -> {
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
            }).start();
        }

        boolean printUnimplemented = true;    //TODO: Remove, just for development debug.
        switch (type)
        {
            case "READY":
                new ReadyHandler(api).handle(content);
                break;
            case "PRESENCE_UPDATE":
                new PresenceUpdateHandler(api).handle(content);
                break;
            case "TYPING_START":
                if (printUnimplemented) System.out.println(message);
                break;
            case "MESSAGE_CREATE":
                new MessageCreateHandler(api).handle(content);
                break;
            case "MESSAGE_UPDATE":
                if (printUnimplemented) System.out.println(message);
                break;
            case "MESSAGE_DELETE":
                if (printUnimplemented) System.out.println(message);
                break;
            case "VOICE_STATE_UPDATE":
                if (printUnimplemented) System.out.println(message);
                break;
            case "CHANNEL_UPDATE":
                if (printUnimplemented) System.out.println(message);
                break;
            case "GUILD_ROLE_UPDATE":
                if (printUnimplemented) System.out.println(message);
                break;
            case "GUILD_BAN_ADD":
                if (printUnimplemented) System.out.println(message);
                break;
            case "GUILD_BAN_REMOVE":
                if (printUnimplemented) System.out.println(message);
                break;
            case "GUILD_MEMBER_ADD":
                if (printUnimplemented) System.out.println(message);
                break;
            case "GUILD_MEMBER_REMOVE":
                if (printUnimplemented) System.out.println(message);
                break;
            default:
                System.out.println(message);    //TODO: Replace with "we don't know this type"
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

    public boolean isConnected()
    {
        return connected;
    }
}
