/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.audio;

import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.audio.dave.DaveProtocolCallbacks;
import net.dv8tion.jda.api.audio.dave.DaveSession;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

class AudioWebSocket extends WebSocketAdapter implements DaveProtocolCallbacks {
    public static final Logger LOG = JDALogger.getLog(AudioWebSocket.class);
    public static final int DISCORD_SECRET_KEY_LENGTH = 32;
    private static final byte[] UDP_KEEP_ALIVE = {(byte) 0xC9, 0, 0, 0, 0, 0, 0, 0, 0};

    protected volatile AudioEncryption encryption;
    protected volatile CryptoAdapter crypto;
    protected WebSocket socket;

    private DaveSession daveSession;
    private final AudioConnection audioConnection;
    private final ConnectionListener listener;
    private final ScheduledExecutorService keepAlivePool;
    private final Guild guild;
    private final String sessionId;
    private final String token;
    private final String wssEndpoint;

    private volatile ConnectionStatus connectionStatus = ConnectionStatus.NOT_CONNECTED;
    private boolean ready = false;
    private boolean reconnecting = false;
    private boolean shouldReconnect;
    private int ssrc;
    private byte[] secretKey;
    private Future<?> keepAliveHandle;
    private InetSocketAddress address;
    private long sequence;

    private volatile boolean shutdown = false;

    protected AudioWebSocket(
            AudioConnection audioConnection,
            ConnectionListener listener,
            String endpoint,
            Guild guild,
            String sessionId,
            String token,
            boolean shouldReconnect) {
        this.audioConnection = audioConnection;
        this.listener = listener;
        this.guild = guild;
        this.sessionId = sessionId;
        this.token = token;
        this.shouldReconnect = shouldReconnect;

        this.keepAlivePool = getJDA().getAudioLifeCyclePool();

        // Add the version query parameter
        String url = IOUtil.addQuery(endpoint, "v", JDAInfo.AUDIO_GATEWAY_VERSION);
        // Append the Secure Websocket scheme so that our websocket library knows how to connect
        if (url.startsWith("wss://")) {
            wssEndpoint = url;
        } else {
            wssEndpoint = "wss://" + url;
        }

        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot create a audio websocket connection using a null/empty sessionId!");
        }
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a audio websocket connection using a null/empty token!");
        }
    }

    void setDaveSession(DaveSession daveSession) {
        this.daveSession = daveSession;
    }

    /* Used by AudioConnection */

    protected void send(String message) {
        LOG.trace("<- {}", message);
        socket.sendText(message);
    }

    protected void send(int op, Object data) {
        send(DataObject.empty().put("op", op).put("d", data).toString());
    }

    protected void startConnection() {
        if (!reconnecting && socket != null) {
            throw new IllegalStateException(
                    "Somehow, someway, this AudioWebSocket has already attempted to start a connection!");
        }

        try {
            WebSocketFactory socketFactory = new WebSocketFactory(getJDA().getWebSocketFactory());
            IOUtil.setServerName(socketFactory, wssEndpoint);
            if (socketFactory.getSocketTimeout() > 0) {
                socketFactory.setSocketTimeout(Math.max(1000, socketFactory.getSocketTimeout()));
            } else {
                socketFactory.setSocketTimeout(10000);
            }
            socket = socketFactory.createSocket(wssEndpoint);
            socket.setDirectTextMessage(true);
            socket.addListener(this);
            changeStatus(ConnectionStatus.CONNECTING_AWAITING_WEBSOCKET_CONNECT);
            socket.connectAsynchronously();
        } catch (IOException e) {
            LOG.warn(
                    "Encountered IOException while attempting to connect to {}: {}\n"
                            + "Closing connection and attempting to reconnect.",
                    wssEndpoint,
                    e.getMessage());
            this.close(ConnectionStatus.ERROR_WEBSOCKET_UNABLE_TO_CONNECT);
        }
    }

    protected void close(ConnectionStatus closeStatus) {
        // Makes sure we don't run this method again
        // after the socket.close(1000) call fires onDisconnect
        if (shutdown) {
            return;
        }
        locked((manager) -> {
            if (shutdown) {
                return;
            }
            ConnectionStatus status = closeStatus;
            ready = false;
            shutdown = true;
            stopKeepAlive();

            if (audioConnection.udpSocket != null) {
                audioConnection.udpSocket.close();
            }
            if (socket != null) {
                socket.sendClose();
            }

            audioConnection.shutdown();
            daveSession.destroy();

            AudioChannel disconnectedChannel = manager.getConnectedChannel();
            manager.setAudioConnection(null);

            // Verify that it is actually a lost of connection
            // and not due the connected channel being deleted.
            JDAImpl api = getJDA();
            if (status == ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL
                    && (!api.getClient().isSession() || !api.getClient().isConnected())) {
                LOG.debug("Connection was closed due to session invalidate!");
                status = ConnectionStatus.ERROR_CANNOT_RESUME;
            } else if (status == ConnectionStatus.ERROR_LOST_CONNECTION
                    || status == ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL) {
                // Get guild from JDA, don't use [guild] field to make sure
                // that we don't have a problem of an out of date guild stored in [guild]
                // during a possible mWS invalidate.
                Guild connGuild = api.getGuildById(guild.getIdLong());
                if (connGuild != null) {
                    AudioChannel channel = (AudioChannel) connGuild.getGuildChannelById(
                            audioConnection.getChannel().getIdLong());
                    if (channel == null) {
                        status = ConnectionStatus.DISCONNECTED_CHANNEL_DELETED;
                    }
                }
            }

            changeStatus(status);

            // decide if we reconnect.
            if (shouldReconnect
                    // indicated that the connection was purposely closed. don't reconnect.
                    && status.shouldReconnect()
                    // Already handled.
                    && status != ConnectionStatus.AUDIO_REGION_CHANGE) {
                if (disconnectedChannel == null) {
                    LOG.debug("Cannot reconnect due to null audio channel");
                    return;
                }
                api.getDirectAudioController().reconnect(disconnectedChannel);
            } else if (status == ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD) {
                // Remove audio manager as we are no longer in the guild
                api.getAudioManagersView().remove(guild.getIdLong());
            } else if (status != ConnectionStatus.AUDIO_REGION_CHANGE
                    && status != ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL) {
                api.getDirectAudioController().disconnect(guild);
            }
        });
    }

    protected void changeStatus(ConnectionStatus newStatus) {
        connectionStatus = newStatus;
        listener.onStatusChange(newStatus);
    }

    protected void setAutoReconnect(boolean shouldReconnect) {
        this.shouldReconnect = shouldReconnect;
    }

    protected ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    protected InetSocketAddress getAddress() {
        return address;
    }

    protected byte[] getSecretKey() {
        return secretKey;
    }

    protected int getSSRC() {
        return ssrc;
    }

    protected boolean isReady() {
        return ready;
    }

    /* TCP Listeners */

    @Override
    public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) {
        getJDA().setContext();
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        if (shutdown) {
            // Somehow this AudioWebSocket was shutdown before we finished connecting....
            // thus we just disconnect here since we were asked to shutdown
            socket.sendClose(1000);
            return;
        }

        if (reconnecting) {
            resume();
        } else {
            identify();
        }
        changeStatus(ConnectionStatus.CONNECTING_AWAITING_AUTHENTICATION);
        audioConnection.prepareReady();
        reconnecting = false;
    }

    @Override
    public void onTextMessage(WebSocket websocket, byte[] data) {
        try {
            handleEvent(DataObject.fromJson(data));
        } catch (Exception ex) {
            String message = "malformed";
            try {
                message = new String(data, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
            LOG.error("Encountered exception trying to handle an event message: {}", message, ex);
        }
    }

    @Override
    public void onDisconnected(
            WebSocket websocket,
            WebSocketFrame serverCloseFrame,
            WebSocketFrame clientCloseFrame,
            boolean closedByServer) {
        if (shutdown) {
            return;
        }
        LOG.debug("The Audio connection was closed!\nBy remote? {}", closedByServer);
        if (serverCloseFrame != null) {
            LOG.debug("Reason: {}\nClose code: {}", serverCloseFrame.getCloseReason(), serverCloseFrame.getCloseCode());
            int code = serverCloseFrame.getCloseCode();
            VoiceCode.Close closeCode = VoiceCode.Close.from(code);
            switch (closeCode) {
                case RATE_LIMIT_EXCEEDED:
                case SERVER_NOT_FOUND:
                case SERVER_CRASH:
                case INVALID_SESSION:
                    this.close(ConnectionStatus.ERROR_CANNOT_RESUME);
                    break;
                case AUTHENTICATION_FAILED:
                    this.close(ConnectionStatus.DISCONNECTED_AUTHENTICATION_FAILURE);
                    break;
                case DISCONNECTED_ALL_CLIENTS:
                case DISCONNECTED:
                    this.close(ConnectionStatus.DISCONNECTED_KICKED_FROM_CHANNEL);
                    break;
                default:
                    this.reconnect();
            }
            return;
        }
        if (clientCloseFrame != null) {
            LOG.debug(
                    "ClientReason: {}\nClientCode: {}",
                    clientCloseFrame.getCloseReason(),
                    clientCloseFrame.getCloseCode());
            if (clientCloseFrame.getCloseCode() != 1000) {
                // unexpected close -> error -> attempt resume
                this.reconnect();
                return;
            }
        }
        this.close(ConnectionStatus.NOT_CONNECTED);
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) {
        handleCallbackError(websocket, cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) {
        LOG.error("There was some audio websocket error", cause);
        JDAImpl api = getJDA();
        api.handleEvent(new ExceptionEvent(api, cause, true));
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) {
        String identifier = getJDA().getIdentifierString();
        String guildId = guild.getId();
        switch (threadType) {
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
    public void onConnectError(WebSocket webSocket, WebSocketException e) {
        LOG.warn(
                "Failed to establish websocket connection to {}: {} - {}\n"
                        + "Closing connection and attempting to reconnect.",
                wssEndpoint,
                e.getError(),
                e.getMessage());
        this.close(ConnectionStatus.ERROR_WEBSOCKET_UNABLE_TO_CONNECT);
    }

    /* Dave Protocol */

    public DaveSession getDaveSession() {
        return daveSession;
    }

    private void sendBinary(int opcode, ByteBuffer payload) {
        ByteBuffer buffer =
                ByteBuffer.allocate(1 + payload.remaining()).put((byte) opcode).put(payload);
        buffer.flip();
        socket.sendBinary(buffer.array());
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) {
        ByteBuffer message = ByteBuffer.allocateDirect(binary.length);
        message.put(binary);
        message.flip();

        short sequence = message.getShort();
        this.sequence = ((long) sequence) & 0xFFFF;
        int opcode = ((int) message.get()) & 0xFF;

        switch (opcode) {
            case VoiceCode.MLS_EXTERNAL_SENDER: {
                LOG.trace("-> MLS_EXTERNAL_SENDER");
                daveSession.onDaveProtocolMLSExternalSenderPackage(message);
                break;
            }
            case VoiceCode.MLS_PROPOSALS: {
                LOG.trace("-> MLS_PROPOSALS");
                daveSession.onMLSProposals(message);
                break;
            }
            case VoiceCode.MLS_ANNOUNCE_COMMIT_TRANSITION: {
                LOG.trace("-> MLS_ANNOUNCE_COMMIT_TRANSITION");
                int transitionId = ((int) message.getShort()) & 0xFFFF;
                daveSession.onMLSPrepareCommitTransition(transitionId, message);
                break;
            }
            case VoiceCode.MLS_WELCOME: {
                LOG.trace("-> MLS_WELCOME");
                int transitionId = ((int) message.getShort()) & 0xFFFF;
                daveSession.onMLSWelcome(transitionId, message);
                break;
            }
            default:
                LOG.trace("-> UNKNOWN OP {}", opcode);
        }
    }

    @Override
    public void sendMLSKeyPackage(@Nonnull ByteBuffer mlsKeyPackage) {
        LOG.trace("<- MLS_KEY_PACKAGE");
        sendBinary(VoiceCode.MLS_KEY_PACKAGE, mlsKeyPackage);
    }

    @Override
    public void sendDaveProtocolReadyForTransition(int transitionId) {
        LOG.trace("<- DAVE_TRANSITION_READY");
        send(VoiceCode.DAVE_TRANSITION_READY, DataObject.empty().put("transition_id", transitionId));
    }

    @Override
    public void sendMLSCommitWelcome(@Nonnull ByteBuffer commitWelcomeMessage) {
        LOG.trace("<- MLS_COMMIT_WELCOME");
        sendBinary(VoiceCode.MLS_COMMIT_WELCOME, commitWelcomeMessage);
    }

    @Override
    public void sendMLSInvalidCommitWelcome(int transitionId) {
        LOG.trace("<- MLS_INVALID_COMMIT_WELCOME");
        send(VoiceCode.MLS_INVALID_COMMIT_WELCOME, DataObject.empty().put("transition_id", transitionId));
    }

    /* Internals */

    private void handleEvent(DataObject contentAll) {
        int opCode = contentAll.getInt("op");
        sequence = contentAll.getLong("seq", sequence);

        switch (opCode) {
            case VoiceCode.HELLO: {
                LOG.trace("-> HELLO {}", contentAll);
                DataObject payload = contentAll.getObject("d");
                int interval = payload.getInt("heartbeat_interval");
                stopKeepAlive();
                setupKeepAlive(interval);
                daveSession.initialize();
                break;
            }
            case VoiceCode.READY: {
                LOG.trace("-> READY {}", contentAll);
                DataObject content = contentAll.getObject("d");
                ssrc = content.getInt("ssrc");
                int port = content.getInt("port");
                String ip = content.getString("ip");
                DataArray modes = content.getArray("modes");
                encryption = CryptoAdapter.negotiate(AudioEncryption.fromArray(modes));
                if (encryption == null) {
                    close(ConnectionStatus.ERROR_UNSUPPORTED_ENCRYPTION_MODES);
                    LOG.error("None of the provided encryption modes are supported: {}", modes);
                    return;
                } else {
                    LOG.debug("Using encryption mode " + encryption.getKey());
                }

                // Find our external IP and Port using Discord
                InetSocketAddress externalIpAndPort;

                changeStatus(ConnectionStatus.CONNECTING_ATTEMPTING_UDP_DISCOVERY);
                int tries = 0;
                do {
                    externalIpAndPort = handleUdpDiscovery(new InetSocketAddress(ip, port), ssrc);
                    tries++;
                    if (externalIpAndPort == null && tries > 5) {
                        close(ConnectionStatus.ERROR_UDP_UNABLE_TO_CONNECT);
                        return;
                    }
                } while (externalIpAndPort == null);

                daveSession.assignSsrcToCodec(DaveSession.Codec.OPUS, ssrc);

                DataObject object = DataObject.empty()
                        .put("protocol", "udp")
                        .put(
                                "data",
                                DataObject.empty()
                                        .put("address", externalIpAndPort.getHostString())
                                        .put("port", externalIpAndPort.getPort())
                                        .put("mode", encryption.getKey())); // Discord requires encryption
                send(VoiceCode.SELECT_PROTOCOL, object);
                changeStatus(ConnectionStatus.CONNECTING_AWAITING_READY);
                break;
            }
            case VoiceCode.RESUMED: {
                LOG.trace("-> RESUMED {}", contentAll);
                LOG.debug("Successfully resumed session!");
                changeStatus(ConnectionStatus.CONNECTED);
                ready = true;
                MiscUtil.locked(audioConnection.readyLock, audioConnection.readyCondvar::signalAll);
                break;
            }
            case VoiceCode.SESSION_DESCRIPTION: {
                LOG.trace("-> SESSION_DESCRIPTION {}", contentAll);
                send(
                        VoiceCode.USER_SPEAKING_UPDATE, // required to receive audio?
                        DataObject.empty().put("delay", 0).put("speaking", 0).put("ssrc", ssrc));
                // secret_key is an array of 32 ints that are less than 256, so they are bytes.
                DataArray keyArray = contentAll.getObject("d").getArray("secret_key");

                secretKey = new byte[DISCORD_SECRET_KEY_LENGTH];
                for (int i = 0; i < keyArray.length(); i++) {
                    secretKey[i] = (byte) keyArray.getInt(i);
                }

                crypto = new DaveCryptoAdapter(CryptoAdapter.getAdapter(encryption, secretKey), daveSession, ssrc);
                daveSession.onSelectProtocolAck(contentAll.getObject("d").getInt("dave_protocol_version"));

                LOG.debug("Audio connection has finished connecting!");
                ready = true;
                MiscUtil.locked(audioConnection.readyLock, audioConnection.readyCondvar::signalAll);
                changeStatus(ConnectionStatus.CONNECTED);
                break;
            }
            case VoiceCode.HEARTBEAT: {
                LOG.trace("-> HEARTBEAT {}", contentAll);
                send(VoiceCode.HEARTBEAT, System.currentTimeMillis());
                break;
            }
            case VoiceCode.HEARTBEAT_ACK: {
                LOG.trace("-> HEARTBEAT_ACK {}", contentAll);
                long ping =
                        System.currentTimeMillis() - contentAll.getObject("d").getLong("t");
                listener.onPing(ping);
                break;
            }
            case VoiceCode.USER_SPEAKING_UPDATE: {
                LOG.trace("-> USER_SPEAKING_UPDATE {}", contentAll);
                DataObject content = contentAll.getObject("d");
                int ssrc = content.getInt("ssrc");
                long userId = content.getUnsignedLong("user_id");
                audioConnection.updateUserSSRC(ssrc, userId);
                daveSession.addUser(userId);

                EnumSet<SpeakingMode> speaking = SpeakingMode.getModes(content.getInt("speaking"));
                User user = getUser(userId);
                if (user == null) {
                    // more relevant for audio connection
                    LOG.trace("Got an Audio USER_SPEAKING_UPDATE for a non-existent User. JSON: {}", contentAll);
                    listener.onUserSpeakingModeUpdate(UserSnowflake.fromId(userId), speaking);
                } else {
                    listener.onUserSpeakingModeUpdate((UserSnowflake) user, speaking);
                }

                break;
            }
            case VoiceCode.USER_BULK_CONNECT: {
                LOG.trace("-> USER_BULK_CONNECT {}", contentAll);
                DataObject payload = contentAll.getObject("d");
                DataArray userIds = payload.getArray("user_ids");
                for (int i = 0; i < userIds.length(); i++) {
                    long userId = userIds.getUnsignedLong(i);
                    daveSession.addUser(userId);
                }
                break;
            }
            case VoiceCode.USER_DISCONNECT: {
                LOG.trace("-> USER_DISCONNECT {}", contentAll);
                DataObject payload = contentAll.getObject("d");
                long userId = payload.getUnsignedLong("user_id");
                audioConnection.removeUserSSRC(userId);
                daveSession.removeUser(userId);
                break;
            }
            case VoiceCode.DAVE_PREPARE_TRANSITION: {
                LOG.trace("-> DAVE_PREPARE_TRANSITION {}", contentAll);
                DataObject payload = contentAll.getObject("d");
                daveSession.onDaveProtocolPrepareTransition(
                        payload.getInt("transition_id"), payload.getInt("protocol_version"));
                break;
            }
            case VoiceCode.DAVE_EXECUTE_TRANSITION: {
                LOG.trace("-> DAVE_EXECUTE_TRANSITION {}", contentAll);
                DataObject payload = contentAll.getObject("d");
                daveSession.onDaveProtocolExecuteTransition(payload.getInt("transition_id"));
                break;
            }
            case VoiceCode.DAVE_PREPARE_EPOCH: {
                LOG.trace("-> DAVE_PREPARE_EPOCH {}", contentAll);
                DataObject payload = contentAll.getObject("d");
                daveSession.onDaveProtocolPrepareEpoch(
                        payload.getUnsignedLong("epoch"), payload.getInt("protocol_version"));
                break;
            }

            default: {
                LOG.trace("-> UNKNOWN OP {}: {}", opCode, contentAll);
                // undocumented / unused
                break;
            }
        }
    }

    private void identify() {
        sequence = 0;
        int maxDaveProtocolVersion = daveSession.getMaxProtocolVersion();
        if (maxDaveProtocolVersion == 0) {
            LOG.warn("Maximum Dave Protocol Version is 0. "
                    + "This means your connection does not properly support encryption. "
                    + "This will fail to work in the future.");
        }

        DataObject connectObj = DataObject.empty()
                .put("server_id", guild.getId())
                .put("user_id", getJDA().getSelfUser().getId())
                .put("session_id", sessionId)
                .put("token", token)
                .put("max_dave_protocol_version", maxDaveProtocolVersion);
        send(VoiceCode.IDENTIFY, connectObj);
    }

    private void resume() {
        LOG.debug("Sending resume payload...");
        DataObject resumeObj = DataObject.empty()
                .put("server_id", guild.getId())
                .put("session_id", sessionId)
                .put("token", token)
                .put("seq_ack", sequence);
        send(VoiceCode.RESUME, resumeObj);
    }

    private JDAImpl getJDA() {
        return audioConnection.getJDA();
    }

    private void locked(Consumer<AudioManagerImpl> consumer) {
        AudioManagerImpl manager = (AudioManagerImpl) guild.getAudioManager();
        MiscUtil.locked(manager.CONNECTION_LOCK, () -> consumer.accept(manager));
    }

    private void reconnect() {
        if (shutdown) {
            return;
        }
        locked((unused) -> {
            if (shutdown) {
                return;
            }
            ready = false;
            reconnecting = true;
            changeStatus(ConnectionStatus.ERROR_LOST_CONNECTION);
            startConnection();
        });
    }

    private InetSocketAddress handleUdpDiscovery(InetSocketAddress address, int ssrc) {
        // We will now send a packet to discord to punch a port hole in the NAT wall.
        // This is called UDP hole punching.
        try {
            // First close existing socket from possible previous attempts
            if (audioConnection.udpSocket != null) {
                audioConnection.udpSocket.close();
            }
            // Create new UDP socket for communication
            audioConnection.udpSocket = new DatagramSocket();

            // Create a byte array of length 74 containing our ssrc.
            ByteBuffer buffer = ByteBuffer.allocate(74); // 74 taken from documentation
            buffer.putShort((short) 1); // 1 = send (receive will be 2)
            buffer.putShort((short) 70); // length = 70 bytes (required)
            // Put the ssrc that we were given into the packet to send back to discord.
            // rest of the bytes are used only in the response (address/port)
            buffer.putInt(ssrc);

            // Construct our packet to be sent loaded with the byte buffer we store the ssrc in.
            DatagramPacket discoveryPacket = new DatagramPacket(buffer.array(), buffer.array().length, address);
            audioConnection.udpSocket.send(discoveryPacket);

            // Discord responds to our packet, returning a packet containing our external ip and the
            // port we connected through.
            // Give a buffer the same size as the one we sent.
            DatagramPacket receivedPacket = new DatagramPacket(new byte[74], 74);
            audioConnection.udpSocket.setSoTimeout(1000);
            audioConnection.udpSocket.receive(receivedPacket);

            // The byte array returned by discord containing our external ip and the port
            // that we used to connect to discord with.
            byte[] received = receivedPacket.getData();

            // Example string:"   121.83.253.66 ��"
            // You'll notice that there are 4 leading nulls and a large amount of nulls
            // between the the ip and the last 2 bytes.
            // Not sure why these exist.
            // The last 2 bytes are the port. More info below.

            // Take bytes between SSRC and PORT and put them into a string
            // null bytes at the beginning are skipped
            // and the rest are appended to the end of the string
            String ourIP = new String(received, 8, received.length - 10);
            // Removes the extra nulls attached to the end of the IP string
            ourIP = ourIP.trim();

            // The port exists as the last 2 bytes in the packet data,
            // and is encoded as an UNSIGNED short.
            // Furthermore, it is stored in Little Endian instead of normal Big Endian.
            // We will first need to convert the byte order from Little Endian to Big Endian
            // (reverse the order)
            // Then we will need to deal with the fact that the bytes represent an unsigned short.
            // Java cannot deal with unsigned types, so we will have to promote the short to a
            // higher type.

            // Get our port which is stored as little endian at the end of the packet
            // We AND it with 0xFFFF to ensure that it isn't sign extended
            int ourPort = (int) IOUtil.getShortBigEndian(received, received.length - 2) & 0xFFFF;
            this.address = address;
            return new InetSocketAddress(ourIP, ourPort);
        } catch (IOException e) {
            // We either timed out or the socket could not be created (firewall?)
            return null;
        }
    }

    private void stopKeepAlive() {
        if (keepAliveHandle != null) {
            keepAliveHandle.cancel(true);
        }
        keepAliveHandle = null;
    }

    private void setupKeepAlive(int keepAliveInterval) {
        if (keepAliveHandle != null) {
            LOG.error("Setting up a KeepAlive runnable while the previous one seems to still be active!!");
        }

        try {
            if (socket != null) {
                Socket rawSocket = this.socket.getSocket();
                if (rawSocket != null) {
                    rawSocket.setSoTimeout(keepAliveInterval + 10000);
                }
            }
        } catch (SocketException ex) {
            LOG.warn("Failed to setup timeout for socket", ex);
        }

        Runnable keepAliveRunnable = () -> {
            getJDA().setContext();
            if (socket != null && socket.isOpen()) // TCP keep-alive
            {
                DataObject packet = DataObject.empty().put("t", System.currentTimeMillis());
                if (sequence > 0) {
                    packet.put("seq_ack", sequence);
                }
                send(VoiceCode.HEARTBEAT, packet);
            }
            if (audioConnection.udpSocket != null && !audioConnection.udpSocket.isClosed()) // UDP keep-alive
            {
                try {
                    DatagramPacket keepAlivePacket = new DatagramPacket(UDP_KEEP_ALIVE, UDP_KEEP_ALIVE.length, address);
                    audioConnection.udpSocket.send(keepAlivePacket);
                } catch (NoRouteToHostException e) {
                    LOG.warn("Closing AudioConnection due to inability to ping audio packets.");
                    LOG.warn("Cannot send audio packet because JDA navigate the route to Discord.\n"
                            + "Are you sure you have internet connection? It is likely that you've lost connection.");
                    this.close(ConnectionStatus.ERROR_LOST_CONNECTION);
                } catch (IOException e) {
                    LOG.error("There was some error sending an audio keepalive packet", e);
                }
            }
        };

        try {
            keepAliveHandle =
                    keepAlivePool.scheduleAtFixedRate(keepAliveRunnable, 0, keepAliveInterval, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ignored) {
        } // ignored because this is probably caused due to a race condition
        // related to the threadpool shutdown.
    }

    private User getUser(long userId) {
        return getJDA().getUserById(userId);
    }

    @Override
    @SuppressWarnings("deprecation") /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */
    protected void finalize() {
        if (!shutdown) {
            LOG.error("Finalization hook of AudioWebSocket was triggered without properly shutting down");
            close(ConnectionStatus.NOT_CONNECTED);
        }
    }
}
