package net.dv8tion.jda.requests;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.SelfInfo;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;


public class WebSocketClient extends org.java_websocket.client.WebSocketClient
{

    private boolean connected;
    private long keepAliveInterval;
    private JDA api;
    private MessageHandler handler;

    public WebSocketClient(String url, JDA api)
    {
        super(URI.create(url.replace("wss", "ws")));
        this.api = api;
        this.handler = new MessageHandler();
        this.connect();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onOpen(ServerHandshake handshake)
    {
        System.out.println("Handshake request returned code: " + handshake.getHttpStatus());
        System.out.println("HTTP Status: " + handshake.getHttpStatusMessage());
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
        System.out.println(message);
        JSONObject content = new JSONObject(message);
        String type = content.getString("t");
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
        handler.handle(type, content);
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {
        System.out.println("The connection was closed!");
        System.out.println("By remote? " + remote);
        System.out.println("Reason: " + reason);
        System.out.println("Close code: " + code);
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

    private class MessageHandler
    {
        public void handle(String type, JSONObject content)
        {
            switch (type)
            {
                case "READY":
                    handleReady(content);
                    break;
                default:
            }
        }

        public void handleReady(JSONObject content)
        {
//            api.setSelfInfo(EntityBuilder.createSelfInfo(content.getJSONObject("user")));
//            JSONArray priv_chats = content.getJSONArray("private_channels");
//            for (int i = 0; i < priv_chats.length(); i++)
//            {
//                api.getPrivChannels().add(EntityBuilder.createPrivateChannel(priv_chats.getJSONObject(i)));
//            }
//            JSONArray guilds = content.getJSONArray("guilds");
//            for (int i = 0; i < guilds.length(); i++)
//            {
//                api.getServers().add(EntityBuilder.createGuild(guilds.getJSONObject(i)));
//            }
        }
    }

    private static class EntityBuilder
    {
        public static Guild createGuild(JSONObject guild)
        {
            return null;
        }

        public static PrivateChannel createPrivateChannel(JSONObject privatechat)
        {
            return null;
        }

        public static SelfInfo createSelfInfo(JSONObject self)
        {
            return null;
        }
    }
}
