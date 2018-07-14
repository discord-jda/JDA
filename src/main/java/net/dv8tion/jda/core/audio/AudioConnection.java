/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import com.iwebpp.crypto.TweetNaclFast;
import com.sun.jna.ptr.PointerByReference;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.core.audio.factory.IAudioSendSystem;
import net.dv8tion.jda.core.audio.factory.IPacketProvider;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ExceptionEvent;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.MDC;
import tomp2p.opuswrapper.Opus;

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
    public static final int OPUS_SAMPLE_RATE = 48000;   //(Hz) We want to use the highest of qualities! All the bandwidth!
    public static final int OPUS_FRAME_SIZE = 960;      //An opus frame size of 960 at 48000hz represents 20 milliseconds of audio.
    public static final int OPUS_FRAME_TIME_AMOUNT = 20;//This is 20 milliseconds. We are only dealing with 20ms opus packets.
    public static final int OPUS_CHANNEL_COUNT = 2;     //We want to use stereo. If the audio given is mono, the encoder promotes it
                                                        // to Left and Right mono (stereo that is the same on both sides)
    public static final long MAX_UINT_32 = 4294967295L;

    private final TIntLongMap ssrcMap = new TIntLongHashMap();
    private final TIntObjectMap<Decoder> opusDecoders = new TIntObjectHashMap<>();
    private final HashMap<User, Queue<Pair<Long, short[]>>> combinedQueue = new HashMap<>();

    private final String threadIdentifier;
    private final AudioWebSocket webSocket;
    private final ConcurrentMap<String, String> contextMap;
    private DatagramSocket udpSocket;
    private VoiceChannel channel;
    private volatile AudioSendHandler sendHandler = null;
    private volatile AudioReceiveHandler receiveHandler = null;
    private PointerByReference opusEncoder;
    private ScheduledExecutorService combinedAudioExecutor;

    private IAudioSendSystem sendSystem;
    private Thread receiveThread;
    private long queueTimeout;

    private volatile boolean couldReceive = false;
    private volatile boolean speaking = false;      //Also acts as "couldProvide"

    private volatile int silenceCounter = 0;
    private boolean sentSilenceOnConnect = false;
    private final byte[] silenceBytes = new byte[] {(byte)0xF8, (byte)0xFF, (byte)0xFE};
    private static boolean printedError = false;

    public AudioConnection(AudioWebSocket webSocket, VoiceChannel channel)
    {
        this.channel = channel;
        this.webSocket = webSocket;
        this.webSocket.audioConnection = this;

        final JDAImpl api = (JDAImpl) channel.getJDA();
        this.threadIdentifier = api.getIdentifierString() + " AudioConnection Guild: " + channel.getGuild().getId();
        this.contextMap = api.getContextMap();
    }

    public void ready()
    {
        Thread readyThread = new Thread(AudioManagerImpl.AUDIO_THREADS, () ->
        {
            if (contextMap != null)
                MDC.setContextMap(contextMap);
            final long timeout = getGuild().getAudioManager().getConnectTimeout();

            final long started = System.currentTimeMillis();
            boolean connectionTimeout = false;
            while (!webSocket.isReady())
            {
                if (timeout > 0 && System.currentTimeMillis() - started > timeout)
                {
                    connectionTimeout = true;
                    break;
                }

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
            if (!connectionTimeout)
            {
                this.udpSocket = webSocket.getUdpSocket();

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
            JDAImpl api = (JDAImpl) getJDA();
            api.getEventManager().handle(new ExceptionEvent(api, throwable, true));
        });
        readyThread.setDaemon(true);
        readyThread.setName(threadIdentifier + " Ready Thread");
        readyThread.start();
    }

    public void setSendingHandler(AudioSendHandler handler)
    {
        this.sendHandler = handler;
        setupSendSystem();
    }

    public void setReceivingHandler(AudioReceiveHandler handler)
    {
        this.receiveHandler = handler;
        setupReceiveSystem();
    }

    public void setQueueTimeout(long queueTimeout)
    {
        this.queueTimeout = queueTimeout;
    }

    public VoiceChannel getChannel()
    {
        return channel;
    }

    public void setChannel(VoiceChannel channel)
    {
        this.channel = channel;
    }

    public JDA getJDA()
    {
        return channel.getJDA();
    }

    public Guild getGuild()
    {
        return channel.getGuild();
    }

    public void removeUserSSRC(long userId)
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

    public void close(ConnectionStatus closeStatus)
    {
        shutdown();
        webSocket.close(closeStatus);
    }

    public synchronized void shutdown()
    {
//        setSpeaking(false);
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

    private synchronized void setupSendSystem()
    {
        if (udpSocket != null && !udpSocket.isClosed() && sendHandler != null && sendSystem == null)
        {
            IAudioSendFactory factory = ((JDAImpl) channel.getJDA()).getAudioSendFactory();
            sendSystem = factory.createSendSystem(new PacketProvider());
            sendSystem.setContextMap(contextMap);
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
            receiveThread = new Thread(AudioManagerImpl.AUDIO_THREADS, () ->
            {
                if (contextMap != null)
                    MDC.setContextMap(contextMap);
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

                        if (receiveHandler != null && (receiveHandler.canReceiveUser() || receiveHandler.canReceiveCombined()) && webSocket.getSecretKey() != null)
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
                                byte[] audio = decryptedPacket.getEncodedAudio();

                                //If the bytes are silence, then this was caused by a User joining the voice channel,
                                // and as such, we haven't yet received information to pair the SSRC with the UserId.
                                if (!Arrays.equals(audio, silenceBytes))
                                    LOG.debug("Received audio data with an unknown SSRC id. Ignoring");

                                continue;
                            }
                            if (decoder == null)
                            {
                                if (AudioNatives.ensureOpus())
                                {
                                    opusDecoders.put(ssrc, decoder = new Decoder(ssrc));
                                }
                                else
                                {
                                    LOG.error("Unable to decode audio due to missing opus binaries!");
                                    break;
                                }
                            }
                            if (!decoder.isInOrder(decryptedPacket.getSequence()))
                            {
                                LOG.trace("Got out-of-order audio packet. Ignoring.");
                                continue;
                            }

                            User user = getJDA().getUserById(userId);
                            if (user == null)
                            {
                                LOG.warn("Received audio data with a known SSRC, but the userId associate with the SSRC is unknown to JDA!");
                                continue;
                            }
                            short[] decodedAudio = decoder.decodeFromOpus(decryptedPacket);

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
                            if (receiveHandler.canReceiveCombined())
                            {
                                Queue<Pair<Long, short[]>> queue = combinedQueue.get(user);
                                if (queue == null)
                                {
                                    queue = new ConcurrentLinkedQueue<>();
                                    combinedQueue.put(user, queue);
                                }
                                queue.add(Pair.of(System.currentTimeMillis(), decodedAudio));
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
                JDAImpl api = (JDAImpl) getJDA();
                api.getEventManager().handle(new ExceptionEvent(api, throwable, true));
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
                Runnable r = () ->
                {
                    if (contextMap != null)
                        MDC.setContextMap(contextMap);
                    task.run();
                };
                final Thread t = new Thread(AudioManagerImpl.AUDIO_THREADS, r, threadIdentifier + " Combined Thread");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler((thread, throwable) ->
                {
                    LOG.error("I have no idea how, but there was an uncaught exception in the combinedAudioExecutor", throwable);
                    JDAImpl api = (JDAImpl) getJDA();
                    api.getEventManager().handle(new ExceptionEvent(api, throwable, true));
                });
                return t;
            });
            combinedAudioExecutor.scheduleAtFixedRate(() ->
            {
                try
                {
                    List<User> users = new LinkedList<>();
                    List<short[]> audioParts = new LinkedList<>();
                    if (receiveHandler != null && receiveHandler.canReceiveCombined())
                    {
                        long currentTime = System.currentTimeMillis();
                        for (Map.Entry<User, Queue<Pair<Long, short[]>>> entry : combinedQueue.entrySet())
                        {
                            User user = entry.getKey();
                            Queue<Pair<Long, short[]>> queue = entry.getValue();

                            if (queue.isEmpty())
                                continue;

                            Pair<Long, short[]> audioData = queue.poll();
                            //Make sure the audio packet is younger than 100ms
                            while (audioData != null && currentTime - audioData.getLeft() > queueTimeout)
                            {
                                audioData = queue.poll();
                            }

                            //If none of the audio packets were younger than 100ms, then there is nothing to add.
                            if (audioData == null)
                            {
                                continue;
                            }
                            users.add(user);
                            audioParts.add(audioData.getRight());
                        }

                        if (!audioParts.isEmpty())
                        {
                            int audioLength = audioParts.get(0).length;
                            short[] mix = new short[1920];  //960 PCM samples for each channel
                            int sample;
                            for (int i = 0; i < audioLength; i++)
                            {
                                sample = 0;
                                for (short[] audio : audioParts)
                                {
                                    sample += audio[i];
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

    private byte[] encodeToOpus(byte[] rawAudio)
    {
        ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(rawAudio.length / 2);
        ByteBuffer encoded = ByteBuffer.allocate(4096);
        for (int i = 0; i < rawAudio.length; i += 2)
        {
            int firstByte =  (0x000000FF & rawAudio[i]);      //Promotes to int and handles the fact that it was unsigned.
            int secondByte = (0x000000FF & rawAudio[i + 1]);  //

            //Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
            short toShort = (short) ((firstByte << 8) | secondByte);

            nonEncodedBuffer.put(toShort);
        }
        ((Buffer) nonEncodedBuffer).flip();

        int result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, OPUS_FRAME_SIZE, encoded, encoded.capacity());
        if (result <= 0)
        {
            LOG.error("Received error code from opus_encode(...): {}", result);
            return null;
        }

        //ENCODING STOPS HERE

        byte[] audio = new byte[result];
        encoded.get(audio);
        return audio;
    }

    private void setSpeaking(boolean isSpeaking)
    {
        this.speaking = isSpeaking;
        JSONObject obj = new JSONObject()
                .put("speaking", isSpeaking ? 1 : 0)
                .put("delay", 0);
        webSocket.send(VoiceCode.USER_SPEAKING_UPDATE, obj);
        if (!isSpeaking)
            sendSilentPackets();
    }

    //Actual logic for this is in the Sending Thread.

    private void sendSilentPackets()
    {
        silenceCounter = 0;
    }
    public AudioWebSocket getWebSocket()
    {
        return webSocket;
    }

    @Override
    @Deprecated
    protected void finalize()
    {
        shutdown();
    }

    private class PacketProvider implements IPacketProvider
    {
        char seq = 0;           //Sequence of audio packets. Used to determine the order of the packets.
        int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.
        private long nonce = 0;
        private ByteBuffer buffer = ByteBuffer.allocate(512);
        private final byte[] nonceBuffer = new byte[TweetNaclFast.SecretBox.nonceLength];

        @Override
        public String getIdentifier()
        {
            return threadIdentifier;
        }

        @Override
        public VoiceChannel getConnectedChannel()
        {
            return AudioConnection.this.channel;
        }

        @Override
        public DatagramSocket getUdpSocket()
        {
            return AudioConnection.this.udpSocket;
        }

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
                    byte[] rawAudio = sendHandler.provide20MsAudio();
                    if (rawAudio == null || rawAudio.length == 0)
                    {
                        if (speaking && changeTalking)
                            setSpeaking(false);
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
                        if (!speaking && changeTalking)
                            setSpeaking(true);

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

                    if (++silenceCounter > 10)
                    {
                        silenceCounter = -1;
                        sentSilenceOnConnect = true;
                    }
                }
                else if (speaking && changeTalking)
                {
                    setSpeaking(false);
                }
            }
            catch (Exception e)
            {
                LOG.error("There was an error while getting next audio packet", e);
            }

            if (nextPacket != null)
                timestamp += OPUS_FRAME_SIZE;

            return nextPacket;
        }

        private byte[] encodeAudio(byte[] rawAudio)
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
                opusEncoder = Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);
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
            int offset = b.arrayOffset();
            int position = b.position();
            return new DatagramPacket(data, offset, position - offset, webSocket.getAddress());
        }

        private ByteBuffer getPacketData(byte[] rawAudio)
        {
            AudioPacket packet = new AudioPacket(seq, timestamp, webSocket.getSSRC(), rawAudio);
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
            return buffer = packet.asEncryptedPacket(buffer, webSocket.getSecretKey(), nonceBuffer, nlen);
        }

        private void loadNextNonce(long nonce)
        {
            nonceBuffer[0] = (byte) ((nonce >>> 24) & 0xFF);
            nonceBuffer[1] = (byte) ((nonce >>> 16) & 0xFF);
            nonceBuffer[2] = (byte) ((nonce >>>  8) & 0xFF);
            nonceBuffer[3] = (byte) ( nonce         & 0xFF);
        }

        @Override
        public void onConnectionError(ConnectionStatus status)
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
}
