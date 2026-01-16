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

import com.neovisionaries.ws.client.WebSocket;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.dv8tion.jda.api.audio.*;
import net.dv8tion.jda.api.audio.dave.DaveSession;
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.api.audio.factory.IAudioSendSystem;
import net.dv8tion.jda.api.audio.factory.IPacketProvider;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.audio.opus.IOpusCodecFactory;
import net.dv8tion.jda.api.audio.opus.IOpusDecoder;
import net.dv8tion.jda.api.audio.opus.IOpusEncoder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.ResizingByteBuffer;
import org.slf4j.Logger;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

public class AudioConnection {
    public static final Logger LOG = JDALogger.getLog(AudioConnection.class);

    public static final long MAX_UINT_32 = 4294967295L;

    static final ByteBuffer silenceBytes = ByteBuffer.wrap(new byte[] {(byte) 0xF8, (byte) 0xFF, (byte) 0xFE});
    private static boolean printedError = false;

    protected volatile DatagramSocket udpSocket;

    private final TIntLongMap ssrcMap = new TIntLongHashMap();
    private final TIntObjectMap<IOpusDecoder> opusDecoders = new TIntObjectHashMap<>();
    private final HashMap<User, Queue<AudioData>> combinedQueue = new HashMap<>();
    private final String threadIdentifier;
    private final AudioWebSocket webSocket;
    private final JDAImpl api;

    protected final ReentrantLock readyLock = new ReentrantLock();
    protected final Condition readyCondvar = readyLock.newCondition();

    private AudioChannel channel;
    private final IOpusCodecFactory opusCodecFactory;
    private IOpusEncoder opusEncoder;
    private ScheduledExecutorService combinedAudioExecutor;
    private IAudioSendSystem sendSystem;
    private Thread receiveThread;
    private long queueTimeout;
    private boolean shutdown = false;

    private volatile AudioSendHandler sendHandler = null;
    private volatile AudioReceiveHandler receiveHandler = null;

    private volatile boolean couldReceive = false;
    private volatile int speakingMode = SpeakingMode.VOICE.getRaw();

    public AudioConnection(
            AudioManagerImpl manager, String endpoint, String sessionId, String token, AudioChannel channel) {
        this.api = (JDAImpl) channel.getJDA();
        this.channel = channel;
        JDAImpl api = (JDAImpl) channel.getJDA();
        this.threadIdentifier = api.getIdentifierString() + " AudioConnection Guild: "
                + channel.getGuild().getId();

        this.webSocket = new AudioWebSocket(
                this,
                manager.getListenerProxy(),
                endpoint,
                channel.getGuild(),
                sessionId,
                token,
                manager.isAutoReconnect());

        DaveSession daveSession = manager.getJDA()
                .getAudioModuleConfig()
                .getDaveSessionFactory()
                .createDaveSession(webSocket, manager.getJDA().getSelfUser().getIdLong(), channel.getIdLong());
        webSocket.setDaveSession(daveSession);

        this.opusCodecFactory = getJDA().getAudioModuleConfig().getOpusCodecFactory();
    }

    /* Used by AudioManagerImpl */

    public void startConnection() {
        webSocket.startConnection();
    }

    public ConnectionStatus getConnectionStatus() {
        return webSocket.getConnectionStatus();
    }

    public void setAutoReconnect(boolean shouldReconnect) {
        webSocket.setAutoReconnect(shouldReconnect);
    }

    public void setSendingHandler(AudioSendHandler handler) {
        this.sendHandler = handler;
        if (webSocket.isReady()) {
            setupSendSystem();
        }
    }

    public void setReceivingHandler(AudioReceiveHandler handler) {
        this.receiveHandler = handler;
        if (webSocket.isReady()) {
            setupReceiveSystem();
        }
    }

    public void setSpeakingMode(EnumSet<SpeakingMode> mode) {
        int raw = SpeakingMode.getRaw(mode);
        if (raw != this.speakingMode && webSocket.isReady()) {
            setSpeaking(raw);
        }
        this.speakingMode = raw;
    }

    public void setQueueTimeout(long queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    public AudioChannel getChannel() {
        return channel;
    }

    public void setChannel(AudioChannel channel) {
        this.channel = channel;
    }

    public JDAImpl getJDA() {
        return api;
    }

    public Guild getGuild() {
        return getChannel().getGuild();
    }

    public void close(ConnectionStatus closeStatus) {
        shutdown();
        webSocket.close(closeStatus);
    }

    public synchronized void shutdown() {
        shutdown = true;
        if (sendSystem != null) {
            sendSystem.shutdown();
            sendSystem = null;
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
            receiveThread = null;
        }
        if (combinedAudioExecutor != null) {
            combinedAudioExecutor.shutdownNow();
            combinedAudioExecutor = null;
        }
        if (opusEncoder != null) {
            opusEncoder.close();
            opusEncoder = null;
        }

        opusDecoders.valueCollection().forEach(IOpusDecoder::close);
        opusDecoders.clear();

        MiscUtil.locked(readyLock, readyCondvar::signalAll);
    }

    public WebSocket getWebSocket() {
        return webSocket.socket;
    }

    /* Used by AudioWebSocket */

    protected void prepareReady() {
        Thread readyThread = new Thread(() -> {
            getJDA().setContext();

            boolean ready = MiscUtil.locked(readyLock, () -> {
                long timeout = getGuild().getAudioManager().getConnectTimeout();
                while (!webSocket.isReady()) {
                    try {
                        boolean activated = readyCondvar.await(timeout, TimeUnit.MILLISECONDS);
                        if (!activated) {
                            webSocket.close(ConnectionStatus.ERROR_CONNECTION_TIMEOUT);
                            shutdown = true;
                        }
                        if (shutdown) {
                            return false;
                        }
                    } catch (InterruptedException e) {
                        LOG.error("AudioConnection ready thread got interrupted while sleeping", e);
                        return false;
                    }
                }

                return true;
            });

            if (ready) {
                setupSendSystem();
                setupReceiveSystem();
            }
        });
        readyThread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOG.error("Uncaught exception in Audio ready-thread", throwable);
            JDAImpl api = getJDA();
            api.handleEvent(new ExceptionEvent(api, throwable, true));
        });
        readyThread.setDaemon(true);
        readyThread.setName(threadIdentifier + " Ready Thread");
        readyThread.start();
    }

    protected void removeUserSSRC(long userId) {
        AtomicInteger ssrcRef = new AtomicInteger(0);
        boolean modified = ssrcMap.retainEntries((ssrc, id) -> {
            boolean isEntry = id == userId;
            if (isEntry) {
                ssrcRef.set(ssrc);
            }
            // if isEntry == true we don't want to retain it
            return !isEntry;
        });
        if (!modified) {
            return;
        }
        IOpusDecoder decoder = opusDecoders.remove(ssrcRef.get());
        if (decoder != null) { // cleanup decoder
            decoder.close();
        }
    }

    protected void updateUserSSRC(int ssrc, long userId) {
        if (ssrcMap.containsKey(ssrc)) {
            long previousId = ssrcMap.get(ssrc);
            if (previousId != userId) {
                // Different User already existed with this ssrc. What should we do? Just replace?
                // Probably should nuke the old opusDecoder.
                // Log for now and see if any user report the error.
                LOG.error(
                        "Yeah.. So.. JDA received a UserSSRC update for an ssrc that already had a User set. Inform"
                                + " devs.\n"
                                + "ChannelId: {} SSRC: {} oldId: {} newId: {}",
                        channel.getId(),
                        ssrc,
                        previousId,
                        userId);
            }
        } else {
            ssrcMap.put(ssrc, userId);

            // Only create a decoder if we are actively handling received audio.
            if (receiveThread != null && opusCodecFactory != null) {
                opusDecoders.put(ssrc, opusCodecFactory.createDecoder());
            }
        }
    }

    /* Internals */

    private synchronized void setupSendSystem() {
        if (udpSocket != null && !udpSocket.isClosed() && sendHandler != null && sendSystem == null) {
            setSpeaking(speakingMode);
            IAudioSendFactory factory = getJDA().getAudioSendFactory();
            sendSystem = factory.createSendSystem(new PacketProvider());
            sendSystem.setContextMap(getJDA().getContextMap());
            sendSystem.start();
        } else if (sendHandler == null && sendSystem != null) {
            sendSystem.shutdown();
            sendSystem = null;

            if (opusEncoder != null) {
                opusEncoder.close();
                opusEncoder = null;
            }
        }
    }

    private synchronized void setupReceiveSystem() {
        if (udpSocket != null && !udpSocket.isClosed() && receiveHandler != null && receiveThread == null) {
            setupReceiveThread();
        } else if (receiveHandler == null && receiveThread != null) {
            receiveThread.interrupt();
            receiveThread = null;

            if (combinedAudioExecutor != null) {
                combinedAudioExecutor.shutdownNow();
                combinedAudioExecutor = null;
            }

            opusDecoders.valueCollection().forEach(IOpusDecoder::close);
            opusDecoders.clear();
        } else if (receiveHandler != null && !receiveHandler.canReceiveCombined() && combinedAudioExecutor != null) {
            combinedAudioExecutor.shutdownNow();
            combinedAudioExecutor = null;
        }
    }

    private synchronized void setupReceiveThread() {
        if (receiveThread == null) {
            receiveThread = new Thread(() -> {
                getJDA().setContext();
                try {
                    udpSocket.setSoTimeout(1000);
                } catch (SocketException e) {
                    LOG.error("Couldn't set SO_TIMEOUT for UDP socket", e);
                }

                byte[] buffer = new byte[4096];
                ResizingByteBuffer decryptBuffer = new ResizingByteBuffer(ByteBuffer.allocateDirect(1024));
                while (!udpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                    try {
                        udpSocket.receive(receivedPacket);

                        boolean shouldDecode = receiveHandler != null
                                && (receiveHandler.canReceiveUser() || receiveHandler.canReceiveCombined());
                        boolean canReceive = receiveHandler != null
                                && (receiveHandler.canReceiveUser()
                                        || receiveHandler.canReceiveCombined()
                                        || receiveHandler.canReceiveEncoded());
                        if (canReceive && webSocket.getSecretKey() != null) {
                            couldReceive = true;

                            AudioPacket audioPacket = new AudioPacket(receivedPacket);
                            int ssrc = audioPacket.getSSRC();
                            long userId = ssrcMap.containsKey(ssrc) ? ssrcMap.get(ssrc) : 0L;
                            if (userId == 0L) {
                                continue;
                            }

                            AudioPacket decryptedPacket =
                                    audioPacket.asDecryptAudioPacket(webSocket.crypto, userId, decryptBuffer);
                            if (decryptedPacket == null) {
                                continue;
                            }

                            IOpusDecoder decoder = opusDecoders.get(ssrc);
                            if (decoder == null) {
                                if (opusCodecFactory != null) {
                                    opusDecoders.put(ssrc, decoder = opusCodecFactory.createDecoder());
                                } else if (!receiveHandler.canReceiveEncoded()) {
                                    LOG.error("Unable to decode audio due to missing Opus support!");
                                    break;
                                }
                            }
                            OpusPacket opusPacket = new OpusPacket(decryptedPacket, userId, decoder);
                            if (receiveHandler.canReceiveEncoded()) {
                                receiveHandler.handleEncodedAudio(opusPacket);
                            }
                            if (!shouldDecode || !opusPacket.canDecode()) {
                                continue;
                            }

                            User user = getJDA().getUserById(userId);
                            if (user == null) {
                                LOG.warn("Received audio data with a known SSRC, but the userId associate with the SSRC"
                                        + " is unknown to JDA!");
                                continue;
                            }
                            short[] decodedAudio = opusPacket.decode();
                            // If decodedAudio is null, then the Opus decode failed,
                            // so throw away the packet.
                            if (decodedAudio == null) {
                                // decoder error logged in method
                                continue;
                            }
                            if (receiveHandler.canReceiveUser()) {
                                receiveHandler.handleUserAudio(new UserAudio(user, decodedAudio));
                            }
                            if (receiveHandler.canReceiveCombined()
                                    && receiveHandler.includeUserInCombinedAudio(user)) {
                                Queue<AudioData> queue = combinedQueue.get(user);
                                if (queue == null) {
                                    queue = new ConcurrentLinkedQueue<>();
                                    combinedQueue.put(user, queue);
                                }
                                queue.add(new AudioData(decodedAudio));
                            }
                        } else {
                            couldReceive = false;
                        }
                    } catch (SocketTimeoutException e) {
                        // Ignore. We set a low timeout so that we wont block forever so we can
                        // properly shutdown the loop.
                    } catch (SocketException e) {
                        // The socket was closed while we were listening for the next packet.
                        // This is expected. Ignore the exception.
                        // The thread will exit during the next while
                        // iteration because the udpSocket.isClosed() will return true.
                    } catch (Exception e) {
                        LOG.error("There was some random exception while waiting for udp packets", e);
                    }
                }
            });
            receiveThread.setUncaughtExceptionHandler((thread, throwable) -> {
                LOG.error("There was some uncaught exception in the audio receive thread", throwable);
                JDAImpl api = getJDA();
                api.handleEvent(new ExceptionEvent(api, throwable, true));
            });
            receiveThread.setDaemon(true);
            receiveThread.setName(threadIdentifier + " Receiving Thread");
            receiveThread.start();
        }

        if (receiveHandler.canReceiveCombined()) {
            setupCombinedExecutor();
        }
    }

    private synchronized void setupCombinedExecutor() {
        if (combinedAudioExecutor == null) {
            combinedAudioExecutor = Executors.newSingleThreadScheduledExecutor((task) -> {
                Thread t = new Thread(task, threadIdentifier + " Combined Thread");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler((thread, throwable) -> {
                    LOG.error(
                            "I have no idea how, but there was an uncaught exception in the combinedAudioExecutor",
                            throwable);
                    JDAImpl api = getJDA();
                    api.handleEvent(new ExceptionEvent(api, throwable, true));
                });
                return t;
            });
            combinedAudioExecutor.scheduleAtFixedRate(
                    () -> {
                        getJDA().setContext();
                        try {
                            List<User> users = new LinkedList<>();
                            List<short[]> audioParts = new LinkedList<>();
                            if (receiveHandler != null && receiveHandler.canReceiveCombined()) {
                                long currentTime = System.currentTimeMillis();
                                for (Map.Entry<User, Queue<AudioData>> entry : combinedQueue.entrySet()) {
                                    User user = entry.getKey();
                                    Queue<AudioData> queue = entry.getValue();

                                    if (queue.isEmpty()) {
                                        continue;
                                    }

                                    AudioData audioData = queue.poll();
                                    // Make sure the audio packet is younger than 100ms
                                    while (audioData != null && currentTime - audioData.time > queueTimeout) {
                                        audioData = queue.poll();
                                    }

                                    // If none of the audio packets were younger than 100ms, then
                                    // there is nothing to add.
                                    if (audioData == null) {
                                        continue;
                                    }
                                    users.add(user);
                                    audioParts.add(audioData.data);
                                }

                                if (!audioParts.isEmpty()) {
                                    int audioLength = audioParts.stream()
                                            .mapToInt(it -> it.length)
                                            .max()
                                            .getAsInt();
                                    short[] mix = new short[1920]; // 960 PCM samples for each channel
                                    int sample;
                                    for (int i = 0; i < audioLength; i++) {
                                        sample = 0;
                                        for (Iterator<short[]> iterator = audioParts.iterator(); iterator.hasNext(); ) {
                                            short[] audio = iterator.next();
                                            if (i < audio.length) {
                                                sample += audio[i];
                                            } else {
                                                iterator.remove();
                                            }
                                        }
                                        if (sample > Short.MAX_VALUE) {
                                            mix[i] = Short.MAX_VALUE;
                                        } else if (sample < Short.MIN_VALUE) {
                                            mix[i] = Short.MIN_VALUE;
                                        } else {
                                            mix[i] = (short) sample;
                                        }
                                    }
                                    receiveHandler.handleCombinedAudio(new CombinedAudio(users, mix));
                                } else {
                                    // No audio to mix, provide 20 MS of silence.
                                    // (960 PCM samples for each channel)
                                    receiveHandler.handleCombinedAudio(
                                            new CombinedAudio(Collections.emptyList(), new short[1920]));
                                }
                            }
                        } catch (Exception e) {
                            LOG.error("There was some unexpected exception in the combinedAudioExecutor!", e);
                        }
                    },
                    0,
                    20,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void setSpeaking(int raw) {
        DataObject obj = DataObject.empty()
                .put("speaking", raw)
                .put("ssrc", webSocket.getSSRC())
                .put("delay", 0);
        webSocket.send(VoiceCode.USER_SPEAKING_UPDATE, obj);
    }

    @Override
    @SuppressWarnings("deprecation") /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */
    protected void finalize() {
        shutdown();
    }

    private class PacketProvider implements IPacketProvider {
        private char seq = 0; // Sequence of audio packets. Used to determine the order of the packets.
        private int timestamp = 0; // Used to sync up our packets within the same timeframe of other people talking.
        private ResizingByteBuffer buffer = new ResizingByteBuffer(ByteBuffer.allocateDirect(2048));
        private ByteBuffer temporaryDirectBuffer = null;
        private ByteBuffer datagramBuffer = null;

        @Nonnull
        @Override
        public String getIdentifier() {
            return threadIdentifier;
        }

        @Nonnull
        @Override
        public AudioChannel getConnectedChannel() {
            return getChannel();
        }

        @Nonnull
        @Override
        public DatagramSocket getUdpSocket() {
            return udpSocket;
        }

        @Nonnull
        @Override
        public InetSocketAddress getSocketAddress() {
            return webSocket.getAddress();
        }

        @Override
        public DatagramPacket getNextPacket(boolean unused) {
            ByteBuffer buffer = getNextPacketRaw(unused);
            return buffer == null ? null : getDatagramPacket(buffer);
        }

        @Override
        public ByteBuffer getNextPacketRaw(boolean unused) {
            try {
                if (sendHandler != null && sendHandler.canProvide()) {
                    ByteBuffer rawAudio = sendHandler.provide20MsAudio();
                    if (rawAudio != null && rawAudio.hasRemaining()) {
                        if (!sendHandler.isOpus()) {
                            rawAudio = encodeAudio(rawAudio);
                            if (rawAudio == null) {
                                return null;
                            }
                        }

                        loadEncryptedPacketData(ensureDirect(rawAudio));

                        if (seq + 1 > Character.MAX_VALUE) {
                            seq = 0;
                        } else {
                            seq++;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("There was an error while getting next audio packet", e);
            }

            timestamp += OpusPacket.OPUS_FRAME_SIZE;
            return buffer.buffer();
        }

        private ByteBuffer ensureDirect(ByteBuffer buffer) {
            if (buffer.isDirect()) {
                return buffer;
            }

            if (temporaryDirectBuffer == null) {
                temporaryDirectBuffer = ByteBuffer.allocateDirect(buffer.remaining());
            }

            temporaryDirectBuffer = IOUtil.replace(temporaryDirectBuffer, buffer);
            return temporaryDirectBuffer;
        }

        private ByteBuffer encodeAudio(ByteBuffer rawAudio) {
            if (opusEncoder == null) {
                if (opusCodecFactory == null) {
                    if (!printedError) {
                        LOG.error("Unable to process PCM audio without Opus support!");
                    }
                    printedError = true;
                    return null;
                }

                try {
                    opusEncoder = opusCodecFactory.createEncoder();
                } catch (Exception e) {
                    LOG.error("Received error from Opus support", e);
                    return null;
                }
            }
            return opusEncoder.encode(rawAudio);
        }

        private DatagramPacket getDatagramPacket(ByteBuffer b) {
            if (datagramBuffer == null) {
                datagramBuffer = ByteBuffer.allocate(b.remaining());
            }

            datagramBuffer = IOUtil.replace(datagramBuffer, b);

            byte[] data = datagramBuffer.array();
            int offset = datagramBuffer.arrayOffset() + datagramBuffer.position();
            int length = datagramBuffer.remaining();
            return new DatagramPacket(data, offset, length, webSocket.getAddress());
        }

        private void loadEncryptedPacketData(ByteBuffer rawAudio) {
            AudioPacket packet = new AudioPacket(seq, timestamp, webSocket.getSSRC(), rawAudio);
            packet.asEncryptedPacket(webSocket.crypto, buffer);
        }

        @Override
        public void onConnectionError(@Nonnull ConnectionStatus status) {
            LOG.warn("IAudioSendSystem reported a connection error of: {}", status);
            LOG.warn("Shutting down AudioConnection.");
            webSocket.close(status);
        }

        @Override
        public void onConnectionLost() {
            LOG.warn("Closing AudioConnection due to inability to send audio packets.");
            LOG.warn("Cannot send audio packet because JDA cannot navigate the route to Discord.\n"
                    + "Are you sure you have internet connection? It is likely that you've lost connection.");
            webSocket.close(ConnectionStatus.ERROR_LOST_CONNECTION);
        }
    }

    private static class AudioData {
        private final long time;
        private final short[] data;

        public AudioData(short[] data) {
            this.time = System.currentTimeMillis();
            this.data = data;
        }
    }
}
