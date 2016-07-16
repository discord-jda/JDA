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
package net.dv8tion.jda.audio;

import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.audio.AudioDisconnectEvent;
import net.dv8tion.jda.events.audio.AudioRegionChangeEvent;
import net.dv8tion.jda.events.audio.AudioUnableToConnectEvent;
import net.dv8tion.jda.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.utils.SimpleLog;
import org.apache.http.HttpHost;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AudioWebSocket extends WebSocketAdapter
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDAAudioSocket");
    public static final int DISCORD_SECRET_KEY_LENGTH = 32;
    public static final int INITIAL_CONNECTION_RESPONSE = 2;
    public static final int HEARTBEAT_PING_RETURN = 3;
    public static final int CONNECTING_COMPLETED = 4;
    public static final int USER_SPEAKING_UPDATE = 5;

    public static final int WEBSOCKET_READ_TIMEOUT = 1008;
    public static final int CONNECTION_SETUP_TIMEOUT = -41;
    public static final int UDP_UNABLE_TO_CONNECT = -42;

    protected AudioConnection audioConnection;

    private final JDAImpl api;
    private final Guild guild;
    private final HttpHost proxy;
    private boolean connected = false;
    private boolean ready = false;
    private Thread keepAliveThread;
    public WebSocket socket;
    private String endpoint;
    private String wssEndpoint;
    private boolean shutdown;

    private int ssrc;
    private String sessionId;
    private String token;
    private byte[] secretKey;


    private DatagramSocket udpSocket;
    private InetSocketAddress address;
    private Thread udpKeepAliveThread;

    public AudioWebSocket(String endpoint, JDAImpl api, Guild guild, String sessionId, String token)
    {
        this.endpoint = endpoint;
        this.api = api;
        this.guild = guild;
        this.sessionId = sessionId;
        this.token = token;

        //Append the Secure Websocket scheme so that our websocket library knows how to connect
        if (!endpoint.startsWith("wss://"))
            wssEndpoint = "wss://" + endpoint;

        if (sessionId == null || sessionId.isEmpty())
            throw new IllegalArgumentException("Cannot create a voice connection using a null/empty sessionId!");
        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("Cannot create a voice connection using a null/empty token!");

        proxy = api.getGlobalProxy();
        WebSocketFactory factory = new WebSocketFactory();
        if (proxy != null)
        {
            ProxySettings settings = factory.getProxySettings();
            settings.setHost(proxy.getHostName());
            settings.setPort(proxy.getPort());
        }
        try
        {
            socket = factory.createSocket(wssEndpoint)
                    .addListener(this);
            socket.connect();
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
                .put("op", 0)
                .put("d", new JSONObject()
                        .put("server_id", guild.getId())
                        .put("user_id", api.getSelfInfo().getId())
                        .put("session_id", sessionId)
                        .put("token", token)
                );
        send(connectObj.toString());
        connected = true;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message)
    {
        JSONObject contentAll = new JSONObject(message);
        int opCode = contentAll.getInt("op");

        switch(opCode)
        {
            case INITIAL_CONNECTION_RESPONSE:
            {
                JSONObject content = contentAll.getJSONObject("d");
                ssrc = content.getInt("ssrc");
                int port = content.getInt("port");
                int heartbeatInterval = content.getInt("heartbeat_interval");

                //Find our external IP and Port using Discord
                InetSocketAddress externalIpAndPort = null;

                int tries = 0;
                do
                {
                    externalIpAndPort = handleUdpDiscovery(new InetSocketAddress(endpoint, port), ssrc);
                    tries++;
                    if (externalIpAndPort == null && tries > 5)
                    {
                        close(false, UDP_UNABLE_TO_CONNECT);
                        return;
                    }
                } while (externalIpAndPort == null);

                setupUdpKeepAliveThread();

                send(new JSONObject()
                        .put("op", 1)
                        .put("d", new JSONObject()
                            .put("protocol", "udp")
                            .put("data", new JSONObject()
                                .put("address", externalIpAndPort.getHostString())
                                .put("port", externalIpAndPort.getPort())
                                .put("mode", "xsalsa20_poly1305")   //Discord requires encryption
                            )
                        )
                        .toString());
                setupKeepAliveThread(heartbeatInterval);

                break;
            }
            case HEARTBEAT_PING_RETURN:
            {
                if (LOG.getEffectiveLevel().getPriority() <= SimpleLog.Level.TRACE.getPriority())
                {
                    long timePingSent  = contentAll.getLong("d");
                    long ping = System.currentTimeMillis() - timePingSent;
                    LOG.trace("ping: " + ping + "ms");
                }
                break;
            }
            case CONNECTING_COMPLETED:
            {
                //secret_key is an array of 32 ints that are less than 256, so they are bytes.
                JSONArray keyArray = contentAll.getJSONObject("d").getJSONArray("secret_key");

                secretKey = new byte[DISCORD_SECRET_KEY_LENGTH];
                for (int i = 0; i < keyArray.length(); i++)
                    secretKey[i] = (byte) keyArray.getInt(i);

                LOG.trace("Audio connection has finished connecting!");
                ready = true;
                break;
            }
            case USER_SPEAKING_UPDATE:
            {
                JSONObject content = contentAll.getJSONObject("d");
                boolean speaking = content.getBoolean("speaking");
                int ssrc = content.getInt("ssrc");
                String userId = content.getString("user_id");

                User user = api.getUserById(userId);
                if (user == null)
                {
                    LOG.warn("Got an Audio USER_SPEAKING_UPDATE for a non-existent User. JSON: " + contentAll);
                    return;
                }

                audioConnection.updateUserSSRC(ssrc, userId, speaking);
                if (speaking)
                    LOG.log(SimpleLog.Level.ALL, user.getUsername() + " started transmitting audio.");    //Replace with event.
                else
                    LOG.log(SimpleLog.Level.ALL, user.getUsername() + " stopped transmitting audio.");    //Replace with event.
                break;
            }
            default:
                LOG.debug("Unknown Audio OP code.\n" + contentAll.toString(4));
        }
    }


    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
    {
        LOG.debug("The Audio connection was closed!");
        LOG.debug("By remote? " + closedByServer);
        if (serverCloseFrame != null)
        {
            LOG.debug("Reason: " + serverCloseFrame.getCloseReason());
            LOG.debug("Close code: " + serverCloseFrame.getCloseCode());
        }
        if (clientCloseFrame != null)
        {
            LOG.debug("ClientReason: " + clientCloseFrame.getCloseReason());
            LOG.debug("ClientCode: " + clientCloseFrame.getCloseCode());
            this.close(false, clientCloseFrame.getCloseCode());
        }
        else
            this.close(false, -1);
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

    public void close(boolean regionChange, int disconnectCode)
    {
        //Makes sure we don't run this method again after the socket.close(1000) call fires onDisconnect
        if (shutdown)
            return;
        connected = false;
        ready = false;
        shutdown = true;
        if (!regionChange)
        {
            JSONObject obj = new JSONObject()
                    .put("op", 4)
                    .put("d", new JSONObject()
                            .put("guild_id", guild.getId())
                            .put("channel_id", JSONObject.NULL)
                            .put("self_mute", false)
                            .put("self_deaf", false)
                    );
            api.getClient().send(obj.toString());
        }
        if (keepAliveThread != null)
        {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        if (udpKeepAliveThread != null)
        {
            udpKeepAliveThread.interrupt();
            udpKeepAliveThread = null;
        }
        if (udpSocket != null)
            udpSocket.close();
        if (socket != null && socket.isOpen())
            socket.sendClose(1000);

        AudioManagerImpl manager = (AudioManagerImpl) guild.getAudioManager();
        VoiceChannel disconnectedChannel = manager.getConnectedChannel();
        manager.setAudioConnection(null);

        if (disconnectCode == WEBSOCKET_READ_TIMEOUT || disconnectCode == UDP_UNABLE_TO_CONNECT)
        {
            LOG.warn("Unexpected disconnect of Audio Connection to guild: " + guild.getId());
            manager.setUnexpectedDisconnectChannel(disconnectedChannel);
        }
        if (regionChange)
        {
            api.getEventManager().handle(new AudioRegionChangeEvent(api, disconnectedChannel));
        }
        else
        {
            switch (disconnectCode)
            {
                case CONNECTION_SETUP_TIMEOUT:
                    //Handled in AudioConnection
                    break;
                case UDP_UNABLE_TO_CONNECT:
                    api.getEventManager().handle(new AudioUnableToConnectEvent(api, disconnectedChannel));
                    break;
                default:
                    api.getEventManager().handle(new AudioDisconnectEvent(api, disconnectedChannel));
            }
        }
    }

    public DatagramSocket getUdpSocket()
    {
        return udpSocket;
    }

    public InetSocketAddress getAddress()
    {
        return address;
    }

    public byte[] getSecretKey()
    {
        return Arrays.copyOf(secretKey, secretKey.length);
    }

    public int getSSRC()
    {
        return ssrc;
    }
    public boolean isConnected()
    {
        return connected;
    }
    public boolean isReady()
    {
        return ready;
    }

    private InetSocketAddress handleUdpDiscovery(InetSocketAddress address, int ssrc)
    {
        //We will now send a packet to discord to punch a port hole in the NAT wall.
        //This is called UDP hole punching.
        try
        {
            udpSocket = new DatagramSocket();   //Use UDP, not TCP.

            //Create a byte array of length 70 containing our ssrc.
            ByteBuffer buffer = ByteBuffer.allocate(70);    //70 taken from https://github.com/Rapptz/discord.py/blob/async/discord/voice_client.py#L208
            buffer.putInt(ssrc);                            //Put the ssrc that we were given into the packet to send back to discord.

            //Construct our packet to be sent loaded with the byte buffer we store the ssrc in.
            DatagramPacket discoveryPacket = new DatagramPacket(buffer.array(), buffer.array().length, address);
            udpSocket.send(discoveryPacket);

            //Discord responds to our packet, returning a packet containing our external ip and the port we connected through.
            DatagramPacket receivedPacket = new DatagramPacket(new byte[70], 70);   //Give a buffer the same size as the one we sent.
            udpSocket.setSoTimeout(1000);
            udpSocket.receive(receivedPacket);

            //The byte array returned by discord containing our external ip and the port that we used
            //to connect to discord with.
            byte[] received = receivedPacket.getData();

            //Example string:"   121.83.253.66                                                   ��"
            //You'll notice that there are 4 leading nulls and a large amount of nulls between the the ip and
            // the last 2 bytes. Not sure why these exist.  The last 2 bytes are the port. More info below.
            String ourIP = new String(receivedPacket.getData());//Puts the entire byte array in. nulls are converted to spaces.
            ourIP = ourIP.substring(0, ourIP.length() - 2); //Removes the port that is stuck on the end of this string. (last 2 bytes are the port)
            ourIP = ourIP.trim();                           //Removes the extra whitespace(nulls) attached to both sides of the IP

            //The port exists as the last 2 bytes in the packet data, and is encoded as an UNSIGNED short.
            //Furthermore, it is stored in Little Endian instead of normal Big Endian.
            //We will first need to convert the byte order from Little Endian to Big Endian (reverse the order)
            //Then we will need to deal with the fact that the bytes represent an unsigned short.
            //Java cannot deal with unsigned types, so we will have to promote the short to a higher type.
            //Options:  char or int.  I will be doing int because it is just easier to work with.
            byte[] portBytes = new byte[2];                 //The port is exactly 2 bytes in size.
            portBytes[0] = received[received.length - 1];   //Get the second byte and store as the first
            portBytes[1] = received[received.length - 2];   //Get the first byte and store as the second.
            //We have now effectively converted from Little Endian -> Big Endian by reversing the order.

            //For more information on how this is converting from an unsigned short to an int refer to:
            //http://www.darksleep.com/player/JavaAndUnsignedTypes.html
            int firstByte = (0x000000FF & ((int) portBytes[0]));    //Promotes to int and handles the fact that it was unsigned.
            int secondByte = (0x000000FF & ((int) portBytes[1]));   //

            //Combines the 2 bytes back together.
            int ourPort = (firstByte << 8) | secondByte;

            this.address = address;

            return new InetSocketAddress(ourIP, ourPort);
        }
        catch (SocketException e)
        {
            return null;
        }
        catch (IOException e)
        {
            LOG.log(e);
            return null;
        }
    }

    private void setupUdpKeepAliveThread()
    {
        udpKeepAliveThread = new Thread("AudioWebSocket UDP-KeepAlive Guild: " + guild.getId())
        {
            @Override
            public void run()
            {
                while (socket.isOpen() && !udpSocket.isClosed() && !this.isInterrupted())
                {
                    long seq = 0;
                    try
                    {
                        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + 1);
                        buffer.put((byte)0xC9);
                        buffer.putLong(seq);
                        DatagramPacket keepAlivePacket = new DatagramPacket(buffer.array(), buffer.array().length, address);
                        udpSocket.send(keepAlivePacket);

                        Thread.sleep(5000); //Wait 5 seconds to send next keepAlivePacket.
                    }
                    catch (NoRouteToHostException e)
                    {
                        LOG.warn("Closing AudioConnection due to inability to ping audio packets.");
                        LOG.warn("Cannot send audio packet because JDA navigate the route to Discord.\n" +
                                "Are you sure you have internet connection? It is likely that you've lost connection.");
                        AudioWebSocket.this.close(true, -1);
                        break;
                    }
                    catch (IOException e)
                    {
                        LOG.log(e);
                    }
                    catch (InterruptedException e)
                    {
                        //We were asked to close.
//                        e.printStackTrace();
                    }
                }
            }
        };
        udpKeepAliveThread.setPriority(Thread.NORM_PRIORITY + 1);
        udpKeepAliveThread.setDaemon(true);
        udpKeepAliveThread.start();
    }

    private void setupKeepAliveThread(int keepAliveInterval)
    {
        keepAliveThread = new Thread("AudioWebSocket WS-KeepAlive Guild: " + guild.getId())
        {
            @Override
            public void run()
            {
                while (socket.isOpen() && !this.isInterrupted())
                {
                    send(new JSONObject()
                            .put("op", 3)
                            .put("d", System.currentTimeMillis())
                            .toString());
                    try
                    {
                        Thread.sleep(keepAliveInterval);
                    }
                    catch (InterruptedException e)
                    {
                        //We were asked to close.
//                        e.printStackTrace();
                    }
                }
            }
        };
        keepAliveThread.setPriority(Thread.MAX_PRIORITY);
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }
}

