/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.audio;

import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.core.audio.hooks.ConnectionListener;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ExceptionEvent;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioWebSocket extends WebSocketAdapter
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDAAudioSocket");
    public static final int DISCORD_SECRET_KEY_LENGTH = 32;

    public static final int INITIAL_CONNECTION_RESPONSE = 2;
    public static final int HEARTBEAT_PING_RETURN = 3;
    public static final int CONNECTING_COMPLETED = 4;
    public static final int USER_SPEAKING_UPDATE = 5;
    public static final int HEARTBEAT_START = 8;

    protected final ConnectionListener listener;
    protected final ScheduledThreadPoolExecutor keepAlivePool;
    protected AudioConnection audioConnection;
    protected ConnectionStatus connectionStatus = ConnectionStatus.NOT_CONNECTED;

    private final JDAImpl api;
    private final Guild guild;
    private final String endpoint;
    private final String sessionId;
    private final String token;
    private boolean connected = false;
    private boolean ready = false;
    private boolean shutdown = false;
    private Future<?> keepAliveHandle;
    private String wssEndpoint;
    private boolean shouldReconnect;

    private int ssrc;
    private byte[] secretKey;
    private DatagramSocket udpSocket;
    private InetSocketAddress address;

    public WebSocket socket;

    public AudioWebSocket(ConnectionListener listener, String endpoint, JDAImpl api, Guild guild, String sessionId, String token, boolean shouldReconnect)
    {
        this.listener = listener;
        this.endpoint = endpoint;
        this.api = api;
        this.guild = guild;
        this.sessionId = sessionId;
        this.token = token;
        this.shouldReconnect = shouldReconnect;

        keepAlivePool = api.getAudioKeepAlivePool();

        //Append the Secure Websocket scheme so that our websocket library knows how to connect
        if (!endpoint.startsWith("wss://"))
            wssEndpoint = "wss://" + endpoint;

        if (sessionId == null || sessionId.isEmpty())
            throw new IllegalArgumentException("Cannot create a voice connection using a null/empty sessionId!");
        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("Cannot create a voice connection using a null/empty token!");
    }

    public void send(String message)
    {
        socket.sendText(message);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
    {
        if (shutdown)
        {
            //Somehow this AudioWebSocket was shutdown before we finished connecting....
            // thus we just disconnect here since we were asked to shutdown
            socket.sendClose(1006);
            return;
        }

        JSONObject connectObj = new JSONObject()
                .put("op", 0)
                .put("d", new JSONObject()
                        .put("server_id", guild.getId())
                        .put("user_id", api.getSelfUser().getId())
                        .put("session_id", sessionId)
                        .put("token", token)
                );
        send(connectObj.toString());
        connected = true;
        changeStatus(ConnectionStatus.CONNECTING_AWAITING_AUTHENTICATING);
        audioConnection.ready();
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

                changeStatus(ConnectionStatus.CONNECTING_ATTEMPTING_UDP_DISCOVERY);
                int tries = 0;
                do
                {
                    externalIpAndPort = handleUdpDiscovery(new InetSocketAddress(endpoint, port), ssrc);
                    tries++;
                    if (externalIpAndPort == null && tries > 5)
                    {
                        close(ConnectionStatus.ERROR_UDP_UNABLE_TO_CONNECT);
                        return;
                    }
                } while (externalIpAndPort == null);

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

                setupKeepAlive(heartbeatInterval);
                changeStatus(ConnectionStatus.CONNECTING_AWAITING_READY);
                break;
            }
            case HEARTBEAT_START:
            {
                break;
            }
            case HEARTBEAT_PING_RETURN:
            {
                long timePingSent  = contentAll.getLong("d");
                long ping = System.currentTimeMillis() - timePingSent;
                listener.onPing(ping);
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
                changeStatus(ConnectionStatus.CONNECTED);
                break;
            }
            case USER_SPEAKING_UPDATE:
            {
                JSONObject content = contentAll.getJSONObject("d");
                boolean speaking = content.getBoolean("speaking");
                int ssrc = content.getInt("ssrc");
                final long userId = content.getLong("user_id");

                User user;
                if (!api.getUserMap().containsKey(userId))
                {
                    if (!api.getFakeUserMap().containsKey(userId))
                    {
                        LOG.warn("Got an Audio USER_SPEAKING_UPDATE for a non-existent User. JSON: " + contentAll);
                        return;
                    }
                    user = api.getFakeUserMap().get(userId);
                }
                else
                {
                    user = api.getUserById(userId);
                }

                audioConnection.updateUserSSRC(ssrc, userId, speaking);
                listener.onUserSpeaking(user, speaking);
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
            if (clientCloseFrame.getCloseCode() != 1000)
                this.close(ConnectionStatus.ERROR_LOST_CONNECTION);
        }
        else
            this.close(ConnectionStatus.NOT_CONNECTED);
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
        api.getEventManager().handle(new ExceptionEvent(api, cause, true));
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception
    {
        String identifier = api.getIdentifierString();
        String guildId = guild.getId();
        switch (threadType)
        {
            case CONNECT_THREAD:
                thread.setName(identifier + " AudioWS-ConnectThread (guildId: " + guildId + ')');
                break;
            case FINISH_THREAD:
                thread.setName(identifier + " AudioWS-FinishThread (guildId: " + guildId + ')');
                break;
            case WRITING_THREAD:
                thread.setName(identifier + " AudioWS-WriteThread (guildId: " + guildId + ')');
                break;
            case READING_THREAD:
                thread.setName(identifier + " AudioWS-ReadThread (guildId: " + guildId + ')');
                break;
            default:
                thread.setName(identifier + " AudioWS-" + threadType + " (guildId: " + guildId + ')');
        }
    }

    @Override
    public void onConnectError(WebSocket webSocket, WebSocketException e)
    {
        LOG.warn("Failed to establish websocket connection: " + e.getError() + " - " + e.getMessage()
                + "\nClosing connection and attempting to reconnect.");
        this.close(ConnectionStatus.ERROR_WEBSOCKET_UNABLE_TO_CONNECT);
    }

    public void startConnection()
    {
        if (socket != null)
            throw new RuntimeException("Somehow, someway, this AudioWebSocket has already attempted to start a connection!");

        try
        {
            socket = api.getWebSocketFactory()
                    .createSocket(wssEndpoint)
                    .addListener(this);
            changeStatus(ConnectionStatus.CONNECTING_AWAITING_WEBSOCKET_CONNECT);
            socket.connectAsynchronously();
        }
        catch (IOException e)
        {
            LOG.warn("Encountered IOException while attempting to connect: " + e.getMessage()
                    + "\nClosing connection and attempting to reconnect.");
            this.close(ConnectionStatus.ERROR_WEBSOCKET_UNABLE_TO_CONNECT);
        }
    }

    public void close(ConnectionStatus closeStatus)
    {
        //Makes sure we don't run this method again after the socket.close(1000) call fires onDisconnect
        if (shutdown)
            return;
        connected = false;
        ready = false;
        shutdown = true;
        if (closeStatus != ConnectionStatus.AUDIO_REGION_CHANGE)
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
        if (keepAliveHandle != null)
        {
            keepAliveHandle.cancel(false);
            keepAliveHandle = null;
        }

        if (audioConnection != null)
            audioConnection.shutdown();
        if (udpSocket != null)
            udpSocket.close();
        if (socket != null && socket.isOpen())
            socket.sendClose(1000);

        VoiceChannel disconnectedChannel;
        AudioManagerImpl manager = (AudioManagerImpl) guild.getAudioManager();

        if (manager.isConnected())
            disconnectedChannel = manager.getConnectedChannel();
        else
            disconnectedChannel = manager.getQueuedAudioConnection();

        manager.setAudioConnection(null);

        //Verify that it is actually a lost of connection and not due the connected channel being deleted.
        if (closeStatus == ConnectionStatus.ERROR_LOST_CONNECTION)
        {
            //Get guild from JDA, don't use [guild] field to make sure that we don't have
            // a problem of an out of date guild stored in [guild] during a possible mWS invalidate.
            Guild connGuild = api.getGuildById(guild.getId());
            if (connGuild != null)
            {
                if (connGuild.getVoiceChannelById(audioConnection.getChannel().getIdLong()) == null)
                    closeStatus = ConnectionStatus.DISCONNECTED_CHANNEL_DELETED;
            }
        }

        changeStatus(closeStatus);

        //decide if we reconnect.
        if (shouldReconnect
                && closeStatus != ConnectionStatus.NOT_CONNECTED    //indicated that the connection was purposely closed. don't reconnect.
                && closeStatus != ConnectionStatus.DISCONNECTED_CHANNEL_DELETED
                && closeStatus != ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD
                && closeStatus != ConnectionStatus.AUDIO_REGION_CHANGE) //Already handled.
        {
            manager.setQueuedAudioConnection(disconnectedChannel);
            api.getClient().queueAudioConnect(disconnectedChannel);
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
            return null;
        }
    }

    private void setupKeepAlive(final int keepAliveInterval)
    {
        if (keepAliveHandle != null)
            LOG.fatal("Setting up a KeepAlive runnable while the previous one seems to still be active!!");

        Runnable keepAliveRunnable = () ->
        {
            if (socket.isOpen() && socket.isOpen() && !udpSocket.isClosed())
            {
                send(new JSONObject()
                        .put("op", 3)
                        .put("d", System.currentTimeMillis())
                        .toString());

                long seq = 0;
                try
                {
                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + 1);
                    buffer.put((byte)0xC9);
                    buffer.putLong(seq);
                    DatagramPacket keepAlivePacket = new DatagramPacket(buffer.array(), buffer.array().length, address);
                    udpSocket.send(keepAlivePacket);

                }
                catch (NoRouteToHostException e)
                {
                    LOG.warn("Closing AudioConnection due to inability to ping audio packets.");
                    LOG.warn("Cannot send audio packet because JDA navigate the route to Discord.\n" +
                            "Are you sure you have internet connection? It is likely that you've lost connection.");
                    AudioWebSocket.this.close(ConnectionStatus.ERROR_LOST_CONNECTION);
                }
                catch (IOException e)
                {
                    LOG.log(e);
                }
            }
        };

        try
        {
            keepAliveHandle = keepAlivePool.scheduleAtFixedRate(keepAliveRunnable, 0, keepAliveInterval, TimeUnit.MILLISECONDS);
        }
        catch (RejectedExecutionException ignored) {} //ignored because this is probably caused due to a race condition
                                                      // related to the threadpool shutdown.
    }

    public void changeStatus(ConnectionStatus newStatus)
    {
        connectionStatus = newStatus;
        listener.onStatusChange(newStatus);
    }

    public ConnectionStatus getConnectionStatus()
    {
        return connectionStatus;
    }

    public void setAutoReconnect(boolean shouldReconnect)
    {
        this.shouldReconnect = shouldReconnect;
    }

    @Override
    protected void finalize() throws Throwable
    {
        if (!shutdown)
        {
            LOG.fatal("Finalization hook of AudioWebSocket was triggered without properly shutting down");
            close(ConnectionStatus.ERROR_LOST_CONNECTION);
        }
    }

    public static class KeepAliveThreadFactory implements ThreadFactory
    {
        final String identifier;
        AtomicInteger threadCount = new AtomicInteger(1);

        public KeepAliveThreadFactory(JDAImpl api)
        {
            identifier = api.getIdentifierString() + " Audio-KeepAlive Pool";
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(AudioManagerImpl.AUDIO_THREADS, r, identifier + " - Thread " + threadCount.getAndIncrement());
            t.setDaemon(true);

            return t;
        }
    }
}

