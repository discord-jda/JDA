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

import com.iwebpp.crypto.TweetNaclFast;
import com.neovisionaries.ws.client.WebSocket;
import com.sun.jna.ptr.PointerByReference;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.dv8tion.jda.api.audio.*;
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.api.audio.factory.IAudioSendSystem;
import net.dv8tion.jda.api.audio.factory.IPacketProvider;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;
import tomp2p.opuswrapper.Opus;

import javax.annotation.Nonnull;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioConnection
{
    public static final Logger LOG = JDALogger.getLog(AudioConnection.class);

    public static final long MAX_UINT_32 = 4294967295L;

    private static final int NOT_SPEAKING = 0;
    private static final ByteBuffer silenceBytes = ByteBuffer.wrap(new byte[] {(byte)0xF8, (byte)0xFF, (byte)0xFE});
    private static boolean printedError = false;

    protected volatile DatagramSocket udpSocket;

    private final TIntLongMap ssrcMap = new TIntLongHashMap();
    private final TIntObjectMap<Decoder> opusDecoders = new TIntObjectHashMap<>();
    private final HashMap<User, Queue<AudioData>> combinedQueue = new HashMap<>();
    private final String threadIdentifier;
    private final AudioWebSocket webSocket;
    private final JDAImpl api;

    private AudioChannel channel;
    private PointerByReference opusEncoder;
    private ScheduledExecutorService combinedAudioExecutor;
    private IAudioSendSystem sendSystem;
    private Thread receiveThread;
    private long queueTimeout;
    private boolean sentSilenceOnConnect = false;
    private int speakingDelay = 10;

    private volatile AudioSendHandler sendHandler = null;
    private volatile AudioReceiveHandler receiveHandler = null;

    private volatile boolean couldReceive = false;
    private volatile boolean speaking = false;      //Also acts as "couldProvide"
    private volatile int speakingMode = SpeakingMode.VOICE.getRaw();
    private volatile int silenceCounter = 0;

    public AudioConnection(AudioManagerImpl manager, String endpoint, String sessionId, String token, AudioChannel channel)
    {
        this.api = (JDAImpl) channel.getJDA();
        this.channel = channel;
        final JDAImpl api = (JDAImpl) channel.getJDA();
        this.threadIdentifier = api.getIdentifierString() + " AudioConnection Guild: " + channel.getGuild().getId();
        this.webSocket = new AudioWebSocket(this, manager.getListenerProxy(), endpoint, channel.getGuild(), sessionId, token, manager.isAutoReconnect());
    }

    /* Used by AudioManagerImpl */

    public void startConnection()
    {
        webSocket.startConnection();
    }

    public ConnectionStatus getConnectionStatus()
    {
        return webSocket.getConnectionStatus();
    }

    public void setAutoReconnect(boolean shouldReconnect)
    {
        webSocket.setAutoReconnect(shouldReconnect);
    }

    public void setSpeakingDelay(int millis)
    {
        speakingDelay = Math.max(millis / 20, 10); // max { millis / frame-length, 200 millis }
    }

    public void setSendingHandler(AudioSendHandler handler)
    {
        this.sendHandler = handler;
        if (webSocket.isReady())
            setupSendSystem();
    }

    public void setReceivingHandler(AudioReceiveHandler handler)
    {
        this.receiveHandler = handler;
        if (webSocket.isReady())
            setupReceiveSystem();
    }

    public void setSpeakingMode(EnumSet<SpeakingMode> mode)
    {
        int raw = SpeakingMode.getRaw(mode);
        if (raw != this.speakingMode && speaking)
            setSpeaking(raw);
        this.speakingMode = raw;
    }

    public void setQueueTimeout(long queueTimeout)
    {
        this.queueTimeout = queueTimeout;
    }

    public AudioChannel getChannel()
    {
        return channel;
    }

    public void setChannel(AudioChannel channel)
    {
        this.channel = channel;
    }

    public JDAImpl getJDA()
    {
        return api;
    }

    public Guild getGuild()
    {
        return getChannel().getGuild();
    }

    public void close(ConnectionStatus closeStatus)
    {
        shutdown();
        webSocket.close(closeStatus);
    }

    public synchronized void shutdown()
    {
        if (sendSystem != null)
        {
            sendSystem.shutdown();
            sendSystem = null;
        }
        if (receiveThread != null)
        {
            receiveThread.interrupt();
            receiveThread = null;
        }
        if (combinedAudioExecutor != null)
        {
            combinedAudioExecutor.shutdownNow();
            combinedAudioExecutor = null;
        }
        if (opusEncoder != null)
        {
            Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
            opusEncoder = null;
        }

        opusDecoders.valueCollection().forEach(Decoder::close);
        opusDecoders.clear();
    }

    public WebSocket getWebSocket()
    {
        return webSocket.socket;
    }

    /* Used by AudioWebSocket */

    protected void prepareReady()
    {
        Thread readyThread = new Thread(() ->
        {
            getJDA().setContext();
            final long timeout = getGuild().getAudioManager().getConnectTimeout();

            final long started = System.currentTimeMillis();
            while (!webSocket.isReady())
            {
                if (timeout > 0 && System.currentTimeMillis() - started > timeout)
                    break;

                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    LOG.error("AudioConnection ready thread got interrupted while sleeping", e);
                    Thread.currentThread().interrupt();
                }
            }
            if (webSocket.isReady())
            {
                setupSendSystem();
                setupReceiveSystem();
            }
            else
            {
                webSocket.close(ConnectionStatus.ERROR_CONNECTION_TIMEOUT);
            }
        });
        readyThread.setUncaughtExceptionHandler((thread, throwable) ->
        {
            LOG.error("Uncaught exception in Audio ready-thread", throwable);
            JDAImpl api = getJDA();
            api.handleEvent(new ExceptionEvent(api, throwable, true));
        });
        readyThread.setDaemon(true);
        readyThread.setName(threadIdentifier + " Ready Thread");
        readyThread.start();
    }

    protected void removeUserSSRC(long userId)
    {
        final AtomicInteger ssrcRef = new AtomicInteger(0);
        final boolean modified = ssrcMap.retainEntries((ssrc, id) ->
        {
            final boolean isEntry = id == userId;
            if (isEntry)
                ssrcRef.set(ssrc);
            // if isEntry == true we don't want to retain it
            return !isEntry;
        });
        if (!modified)
            return;
        final Decoder decoder = opusDecoders.remove(ssrcRef.get());
        if (decoder != null) // cleanup decoder
            decoder.close();
    }

    protected void updateUserSSRC(int ssrc, long userId)
    {
        if (ssrcMap.containsKey(ssrc))
        {
            long previousId = ssrcMap.get(ssrc);
            if (previousId != userId)
            {
                //Different User already existed with this ssrc. What should we do? Just replace? Probably should nuke the old opusDecoder.
                //Log for now and see if any user report the error.
                LOG.error("Yeah.. So.. JDA received a UserSSRC update for an ssrc that already had a User set. Inform DV8FromTheWorld.\nChannelId: {} SSRC: {} oldId: {} newId: {}",
                      channel.getId(), ssrc, previousId, userId);
            }
        }
        else
        {
            ssrcMap.put(ssrc, userId);

            //Only create a decoder if we are actively handling received audio.
            if (receiveThread != null && AudioNatives.ensureOpus())
                opusDecoders.put(ssrc, new Decoder(ssrc));
        }
    }

    /* Internals */

    private synchronized void setupSendSystem()
    {
        if (udpSocket != null && !udpSocket.isClosed() && sendHandler != null && sendSystem == null)
        {
            IAudioSendFactory factory = getJDA().getAudioSendFactory();
            sendSystem = factory.createSendSystem(new PacketProvider(new TweetNaclFast.SecretBox(webSocket.getSecretKey())));
            sendSystem.setContextMap(getJDA().getContextMap());
            sendSystem.start();
        }
        else if (sendHandler == null && sendSystem != null)
        {
            sendSystem.shutdown();
            sendSystem = null;

            if (opusEncoder != null)
            {
                Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
                opusEncoder = null;
            }
        }
    }

    private synchronized void setupReceiveSystem()
    {
        if (udpSocket != null && !udpSocket.isClosed() && receiveHandler != null && receiveThread == null)
        {
            setupReceiveThread();
        }
        else if (receiveHandler == null && receiveThread != null)
        {
            receiveThread.interrupt();
            receiveThread = null;

            if (combinedAudioExecutor != null)
            {
                combinedAudioExecutor.shutdownNow();
                combinedAudioExecutor = null;
            }

            opusDecoders.valueCollection().forEach(Decoder::close);
            opusDecoders.clear();
        }
        else if (receiveHandler != null && !receiveHandler.canReceiveCombined() && combinedAudioExecutor != null)
        {
            combinedAudioExecutor.shutdownNow();
            combinedAudioExecutor = null;
        }
    }

    private synchronized void setupReceiveThread()
    {
        if (receiveThread == null)
        {
            receiveThread = new Thread(() ->
            {
                getJDA().setContext();
                try
                {
                    udpSocket.setSoTimeout(1000);
                }
                catch (SocketException e)
                {
                    LOG.error("Couldn't set SO_TIMEOUT for UDP socket", e);
                }
                while (!udpSocket.isClosed() && !Thread.currentThread().isInterrupted())
                {
                    DatagramPacket receivedPacket = new DatagramPacket(new byte[1920], 1920);
                    try
                    {
                        udpSocket.receive(receivedPacket);

                        boolean shouldDecode = receiveHandler != null && (receiveHandler.canReceiveUser() || receiveHandler.canReceiveCombined());
                        boolean canReceive = receiveHandler != null && (receiveHandler.canReceiveUser() || receiveHandler.canReceiveCombined() || receiveHandler.canReceiveEncoded());
                        if (canReceive && webSocket.getSecretKey() != null)
                        {
                            if (!couldReceive)
                            {
                                couldReceive = true;
                                sendSilentPackets();
                            }
                            AudioPacket decryptedPacket = AudioPacket.decryptAudioPacket(webSocket.encryption, receivedPacket, webSocket.getSecretKey());
                            if (decryptedPacket == null)
                                continue;

                            int ssrc = decryptedPacket.getSSRC();
                            final long userId = ssrcMap.get(ssrc);
                            Decoder decoder = opusDecoders.get(ssrc);
                            if (userId == ssrcMap.getNoEntryValue())
                            {
                                ByteBuffer audio = decryptedPacket.getEncodedAudio();

                                //If the bytes are silence, then this was caused by a User joining the voice channel,
                                // and as such, we haven't yet received information to pair the SSRC with the UserId.
                                if (!audio.equals(silenceBytes))
                                    LOG.debug("Received audio data with an unknown SSRC id. Ignoring");

                                continue;
                            }
                            if (decoder == null)
                            {
                                if (AudioNatives.ensureOpus())
                                {
                                    opusDecoders.put(ssrc, decoder = new Decoder(ssrc));
                                }
                                else if (!receiveHandler.canReceiveEncoded())
                                {
                                    LOG.error("Unable to decode audio due to missing opus binaries!");
                                    break;
                                }
                            }
                            OpusPacket opusPacket = new OpusPacket(decryptedPacket, userId, decoder);
                            if (receiveHandler.canReceiveEncoded())
                                receiveHandler.handleEncodedAudio(opusPacket);
                            if (!shouldDecode || !opusPacket.canDecode())
                                continue;

                            User user = getJDA().getUserById(userId);
                            if (user == null)
                            {
                                LOG.warn("Received audio data with a known SSRC, but the userId associate with the SSRC is unknown to JDA!");
                                continue;
                            }
                            short[] decodedAudio = opusPacket.decode();
                            //If decodedAudio is null, then the Opus decode failed, so throw away the packet.
                            if (decodedAudio == null)
                            {
                                //decoder error logged in method
                                continue;
                            }
                            if (receiveHandler.canReceiveUser())
                            {
                                receiveHandler.handleUserAudio(new UserAudio(user, decodedAudio));
                            }
                            if (receiveHandler.canReceiveCombined() && receiveHandler.includeUserInCombinedAudio(user))
                            {
                                Queue<AudioData> queue = combinedQueue.get(user);
                                if (queue == null)
                                {
                                    queue = new ConcurrentLinkedQueue<>();
                                    combinedQueue.put(user, queue);
                                }
                                queue.add(new AudioData(decodedAudio));
                            }
                        }
                        else if (couldReceive)
                        {
                            couldReceive = false;
                            sendSilentPackets();
                        }
                    }
                    catch (SocketTimeoutException e)
                    {
                        //Ignore. We set a low timeout so that we wont block forever so we can properly shutdown the loop.
                    }
                    catch (SocketException e)
                    {
                        //The socket was closed while we were listening for the next packet.
                        //This is expected. Ignore the exception. The thread will exit during the next while
                        // iteration because the udpSocket.isClosed() will return true.
                    }
                    catch (Exception e)
                    {
                        LOG.error("There was some random exception while waiting for udp packets", e);
                    }
                }
            });
            receiveThread.setUncaughtExceptionHandler((thread, throwable) ->
            {
                LOG.error("There was some uncaught exception in the audio receive thread", throwable);
                JDAImpl api = getJDA();
                api.handleEvent(new ExceptionEvent(api, throwable, true));
            });
            receiveThread.setDaemon(true);
            receiveThread.setName(threadIdentifier + " Receiving Thread");
            receiveThread.start();
        }

        if (receiveHandler.canReceiveCombined())
        {
            setupCombinedExecutor();
        }
    }

    private synchronized void setupCombinedExecutor()
    {
        if (combinedAudioExecutor == null)
        {
            combinedAudioExecutor = Executors.newSingleThreadScheduledExecutor((task) ->
            {
                final Thread t = new Thread(task, threadIdentifier + " Combined Thread");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler((thread, throwable) ->
                {
                    LOG.error("I have no idea how, but there was an uncaught exception in the combinedAudioExecutor", throwable);
                    JDAImpl api = getJDA();
                    api.handleEvent(new ExceptionEvent(api, throwable, true));
                });
                return t;
            });
            combinedAudioExecutor.scheduleAtFixedRate(() ->
            {
                getJDA().setContext();
                try
                {
                    List<User> users = new LinkedList<>();
                    List<short[]> audioParts = new LinkedList<>();
                    if (receiveHandler != null && receiveHandler.canReceiveCombined())
                    {
                        long currentTime = System.currentTimeMillis();
                        for (Map.Entry<User, Queue<AudioData>> entry : combinedQueue.entrySet())
                        {
                            User user = entry.getKey();
                            Queue<AudioData> queue = entry.getValue();

                            if (queue.isEmpty())
                                continue;

                            AudioData audioData = queue.poll();
                            //Make sure the audio packet is younger than 100ms
                            while (audioData != null && currentTime - audioData.time > queueTimeout)
                            {
                                audioData = queue.poll();
                            }

                            //If none of the audio packets were younger than 100ms, then there is nothing to add.
                            if (audioData == null)
                            {
                                continue;
                            }
                            users.add(user);
                            audioParts.add(audioData.data);
                        }

                        if (!audioParts.isEmpty())
                        {
                            int audioLength = audioParts.stream().mapToInt(it -> it.length).max().getAsInt();
                            short[] mix = new short[1920];  //960 PCM samples for each channel
                            int sample;
                            for (int i = 0; i < audioLength; i++)
                            {
                                sample = 0;
                                for (Iterator<short[]> iterator = audioParts.iterator(); iterator.hasNext(); )
                                {
                                    short[] audio = iterator.next();
                                    if (i < audio.length)
                                        sample += audio[i];
                                    else
                                        iterator.remove();
                                }
                                if (sample > Short.MAX_VALUE)
                                    mix[i] = Short.MAX_VALUE;
                                else if (sample < Short.MIN_VALUE)
                                    mix[i] = Short.MIN_VALUE;
                                else
                                    mix[i] = (short) sample;
                            }
                            receiveHandler.handleCombinedAudio(new CombinedAudio(users, mix));
                        }
                        else
                        {
                            //No audio to mix, provide 20 MS of silence. (960 PCM samples for each channel)
                            receiveHandler.handleCombinedAudio(new CombinedAudio(Collections.emptyList(), new short[1920]));
                        }
                    }
                }
                catch (Exception e)
                {
                    LOG.error("There was some unexpected exception in the combinedAudioExecutor!", e);
                }
            }, 0, 20, TimeUnit.MILLISECONDS);
        }
    }

    private ByteBuffer encodeToOpus(ByteBuffer rawAudio)
    {
        ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(rawAudio.remaining() / 2);
        ByteBuffer encoded = ByteBuffer.allocate(4096);
        for (int i = rawAudio.position(); i < rawAudio.limit(); i += 2)
        {
            int firstByte =  (0x000000FF & rawAudio.get(i));      //Promotes to int and handles the fact that it was unsigned.
            int secondByte = (0x000000FF & rawAudio.get(i + 1));

            //Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
            short toShort = (short) ((firstByte << 8) | secondByte);

            nonEncodedBuffer.put(toShort);
        }
        ((Buffer) nonEncodedBuffer).flip();

        int result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, OpusPacket.OPUS_FRAME_SIZE, encoded, encoded.capacity());
        if (result <= 0)
        {
            LOG.error("Received error code from opus_encode(...): {}", result);
            return null;
        }

        ((Buffer) encoded).position(0).limit(result);
        return encoded;
    }

    private void setSpeaking(int raw)
    {
        this.speaking = raw != 0;
        DataObject obj = DataObject.empty()
                .put("speaking", raw)
                .put("ssrc", webSocket.getSSRC())
                .put("delay", 0);
        webSocket.send(VoiceCode.USER_SPEAKING_UPDATE, obj);
    }

    private void sendSilentPackets()
    {
        silenceCounter = 0;
    }

    @Override
    @SuppressWarnings("deprecation") /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */
    protected void finalize()
    {
        shutdown();
    }

    private class PacketProvider implements IPacketProvider
    {
        private char seq = 0;           //Sequence of audio packets. Used to determine the order of the packets.
        private int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.
        private TweetNaclFast.SecretBox boxer;
        private long nonce = 0;
        private ByteBuffer buffer = ByteBuffer.allocate(512);
        private ByteBuffer encryptionBuffer = ByteBuffer.allocate(512);
        private final byte[] nonceBuffer = new byte[TweetNaclFast.SecretBox.nonceLength];

        public PacketProvider(TweetNaclFast.SecretBox boxer)
        {
            this.boxer = boxer;
        }

        @Nonnull
        @Override
        public String getIdentifier()
        {
            return threadIdentifier;
        }

        @Nonnull
        @Override
        public AudioChannel getConnectedChannel()
        {
            return getChannel();
        }

        @Nonnull
        @Override
        public DatagramSocket getUdpSocket()
        {
            return udpSocket;
        }

        @Nonnull
        @Override
        public InetSocketAddress getSocketAddress()
        {
            return webSocket.getAddress();
        }

        @Override
        public DatagramPacket getNextPacket(boolean changeTalking)
        {
            ByteBuffer buffer = getNextPacketRaw(changeTalking);
            return buffer == null ? null : getDatagramPacket(buffer);
        }

        @Override
        public ByteBuffer getNextPacketRaw(boolean changeTalking)
        {
            ByteBuffer nextPacket = null;
            try
            {
                cond: if (sentSilenceOnConnect && sendHandler != null && sendHandler.canProvide())
                {
                    silenceCounter = -1;
                    ByteBuffer rawAudio = sendHandler.provide20MsAudio();
                    if (rawAudio != null && !rawAudio.hasArray())
                    {
                        // we can't use the boxer without an array so encryption would not work
                        LOG.error("AudioSendHandler provided ByteBuffer without a backing array! This is unsupported.");
                    }
                    if (rawAudio == null || !rawAudio.hasRemaining() || !rawAudio.hasArray())
                    {
                        if (speaking && changeTalking)
                            sendSilentPackets();
                    }
                    else
                    {
                        if (!sendHandler.isOpus())
                        {
                            rawAudio = encodeAudio(rawAudio);
                            if (rawAudio == null)
                                break cond;
                        }

                        nextPacket = getPacketData(rawAudio);
                        if (!speaking)
                            setSpeaking(speakingMode);

                        if (seq + 1 > Character.MAX_VALUE)
                            seq = 0;
                        else
                            seq++;
                    }
                }
                else if (silenceCounter > -1)
                {
                    nextPacket = getPacketData(silenceBytes);
                    if (seq + 1 > Character.MAX_VALUE)
                        seq = 0;
                    else
                        seq++;

                    silenceCounter++;
                    //If we have sent our 10 silent packets on initial connect, or if we have sent enough silent packets
                    // to satisfy the speaking delay, stop transmitting silence.
                    if ((!sentSilenceOnConnect && silenceCounter > 10) || silenceCounter > speakingDelay)
                    {
                        if (sentSilenceOnConnect)
                            setSpeaking(NOT_SPEAKING);
                        silenceCounter = -1;
                        sentSilenceOnConnect = true;
                    }
                }
                else if (speaking && changeTalking)
                {
                    sendSilentPackets();
                }
            }
            catch (Exception e)
            {
                LOG.error("There was an error while getting next audio packet", e);
            }

            if (nextPacket != null)
                timestamp += OpusPacket.OPUS_FRAME_SIZE;

            return nextPacket;
        }

        private ByteBuffer encodeAudio(ByteBuffer rawAudio)
        {
            if (opusEncoder == null)
            {
                if (!AudioNatives.ensureOpus())
                {
                    if (!printedError)
                        LOG.error("Unable to process PCM audio without opus binaries!");
                    printedError = true;
                    return null;
                }
                IntBuffer error = IntBuffer.allocate(1);
                opusEncoder = Opus.INSTANCE.opus_encoder_create(OpusPacket.OPUS_SAMPLE_RATE, OpusPacket.OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);
                if (error.get() != Opus.OPUS_OK && opusEncoder == null)
                {
                    LOG.error("Received error status from opus_encoder_create(...): {}", error.get());
                    return null;
                }
            }
            return encodeToOpus(rawAudio);
        }

        private DatagramPacket getDatagramPacket(ByteBuffer b)
        {
            byte[] data = b.array();
            int offset = b.arrayOffset() + b.position();
            int length = b.remaining();
            return new DatagramPacket(data, offset, length, webSocket.getAddress());
        }

        private ByteBuffer getPacketData(ByteBuffer rawAudio)
        {
            ensureEncryptionBuffer(rawAudio);
            AudioPacket packet = new AudioPacket(encryptionBuffer, seq, timestamp, webSocket.getSSRC(), rawAudio);
            int nlen;
            switch (webSocket.encryption)
            {
                case XSALSA20_POLY1305:
                    nlen = 0;
                    break;
                case XSALSA20_POLY1305_LITE:
                    if (nonce >= MAX_UINT_32)
                        loadNextNonce(nonce = 0);
                    else
                        loadNextNonce(++nonce);
                    nlen = 4;
                    break;
                case XSALSA20_POLY1305_SUFFIX:
                    ThreadLocalRandom.current().nextBytes(nonceBuffer);
                    nlen = TweetNaclFast.SecretBox.nonceLength;
                    break;
                default:
                    throw new IllegalStateException("Encryption mode [" + webSocket.encryption + "] is not supported!");
            }
            return buffer = packet.asEncryptedPacket(boxer, buffer, nonceBuffer, nlen);
        }

        private void ensureEncryptionBuffer(ByteBuffer data)
        {
            ((Buffer) encryptionBuffer).clear();
            int currentCapacity = encryptionBuffer.remaining();
            int requiredCapacity = AudioPacket.RTP_HEADER_BYTE_LENGTH + data.remaining();
            if (currentCapacity < requiredCapacity)
                encryptionBuffer = ByteBuffer.allocate(requiredCapacity);
        }

        private void loadNextNonce(long nonce)
        {
            IOUtil.setIntBigEndian(nonceBuffer, 0, (int) nonce);
        }

        @Override
        public void onConnectionError(@Nonnull ConnectionStatus status)
        {
            LOG.warn("IAudioSendSystem reported a connection error of: {}", status);
            LOG.warn("Shutting down AudioConnection.");
            webSocket.close(status);
        }

        @Override
        public void onConnectionLost()
        {
            LOG.warn("Closing AudioConnection due to inability to send audio packets.");
            LOG.warn("Cannot send audio packet because JDA cannot navigate the route to Discord.\n" +
                "Are you sure you have internet connection? It is likely that you've lost connection.");
            webSocket.close(ConnectionStatus.ERROR_LOST_CONNECTION);
        }
    }

    private static class AudioData
    {
        private final long time;
        private final short[] data;

        public AudioData(short[] data)
        {
            this.time = System.currentTimeMillis();
            this.data = data;
        }
    }
}
