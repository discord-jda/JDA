package net.dv8tion.jda.requests;

import java.net.URI;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;


public class WebSocketClient extends org.java_websocket.client.WebSocketClient
{

    private boolean connected;
    public WebSocketClient(String url)
    {
        super(URI.create(url.replace("wss", "ws")));
        this.connect();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onOpen(ServerHandshake handshake)
    {
        System.out.println("Handshake request returned code: " + handshake.getHttpStatus());
        System.out.println("HTTP Status: " + handshake.getHttpStatusMessage());
        connected = true;
    }

    @Override
    public void onMessage(String message)
    {
        System.out.println(new JSONObject(message).toString(4));
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
}
