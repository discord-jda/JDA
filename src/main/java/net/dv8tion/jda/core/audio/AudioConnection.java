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
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import tomp2p.opuswrapper.Opus;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioConnection
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDAAudioConn");
    public static final int OPUS_SAMPLE_RATE = 48000;   //(Hz) We want to use the highest of qualities! All the bandwidth!
    public static final int OPUS_FRAME_SIZE = 960;      //An opus frame size of 960 at 48000hz represents 20 milliseconds of audio.
    public static final int OPUS_FRAME_TIME_AMOUNT = 20;//This is 20 milliseconds. We are only dealing with 20ms opus packets.
    public static final int OPUS_CHANNEL_COUNT = 2;     //We want to use stereo. If the audio given is mono, the encoder promotes it
                                                        // to Left and Right mono (stereo that is the same on both sides)
    private final TIntLongMap ssrcMap = new TIntLongHashMap();
    private final TIntObjectMap<Decoder> opusDecoders = new TIntObjectHashMap<>();
    private final HashMap<User, Queue<Pair<Long, short[]>>> combinedQueue = new HashMap<>();

    private final String threadIdentifier;
    private final AudioWebSocket webSocket;
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
    boolean sentSilenceOnConnect = false;
    private final byte[] silenceBytes = new byte[] {(byte)0xF8, (byte)0xFF, (byte)0xFE};

    public AudioConnection(AudioWebSocket webSocket, VoiceChannel channel)
    {
        this.channel = channel;
        this.webSocket = webSocket;
        this.webSocket.audioConnection = this;

        final JDAImpl api = (JDAImpl) channel.getJDA();
        this.threadIdentifier = api.getIdentifierString() + " AudioConnection Guild: " + channel.getGuild().getId();
    }

    public void ready()
    {
        Thread readyThread = new Thread(threadIdentifier + " Ready Thread")
        {
            @Override
            public void run()
            {
                final long timeout = getGuild().getAudioManager().getConnectTimeout();

                JDAImpl api = (JDAImpl) getJDA();
                long started = System.currentTimeMillis();
                boolean connectionTimeout = false;
                while (!webSocket.isReady() && !connectionTimeout)
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
                        LOG.log(e);
                        Thread.currentThread().interrupt();
                    }
                }
                if (!connectionTimeout)
                {
                    AudioConnection.this.udpSocket = webSocket.getUdpSocket();

                    setupSendSystem();
                    setupReceiveSystem();
                }
                else
                {
                    webSocket.close(ConnectionStatus.ERROR_CONNECTION_TIMEOUT);
                }
            }
        };
        readyThread.setDaemon(true);
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

    public void updateUserSSRC(int ssrc, long userId, boolean talking)
    {
        if (ssrcMap.containsKey(ssrc))
        {
            long previousId = ssrcMap.get(ssrc);
            if (previousId != userId)
            {
                //Different User already existed with this ssrc. What should we do? Just replace? Probably should nuke the old opusDecoder.
                //Log for now and see if any user report the error.
                LOG.fatal("Yeah.. So.. JDA received a UserSSRC update for an ssrc that already had a User set. Inform DV8FromTheWorld.\n" +
                        "ChannelId: " + channel.getId() + " SSRC: " + ssrc + " oldId: " + previousId + " newId: " + userId);
            }
        }
        else
        {
            ssrcMap.put(ssrc, userId);

            //Only create a decoder if we are actively handling received audio.
            if (receiveThread != null)
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
            IntBuffer error = IntBuffer.allocate(4);
            opusEncoder = Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);

            IAudioSendFactory factory = ((JDAImpl) channel.getJDA()).getAudioSendFactory();
            sendSystem = factory.createSendSystem(new PacketProvider());
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
            receiveThread = new Thread(AudioManagerImpl.AUDIO_THREADS, threadIdentifier + " Receiving Thread")
            {
                @Override
                public void run()
                {
                    try
                    {
                        udpSocket.setSoTimeout(1000);
                    }
                    catch (SocketException e)
                    {
                        LOG.log(e);
                    }
                    while (!udpSocket.isClosed() && !this.isInterrupted())
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
                                AudioPacket decryptedPacket = AudioPacket.decryptAudioPacket(receivedPacket, webSocket.getSecretKey());

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
                                    decoder = new Decoder(ssrc);
                                    opusDecoders.put(ssrc, decoder);
                                }
                                if (!decoder.isInOrder(decryptedPacket.getSequence()))
                                {
                                    LOG.trace("Got out-of-order audio packet. Ignoring.");
                                    continue;
                                }

                                User user = getJDA().getUserById(userId);
                                if (user == null)
                                    LOG.warn("Received audio data with a known SSRC, but the userId associate with the SSRC is unknown to JDA!");
                                else
                                {
//                                    if (decoder.wasPacketLost(decryptedPacket.getSequence()))
//                                    {
//                                        LOG.debug("Packet(s) missed. Using Opus packetloss-compensation.");
//                                        short[] decodedAudio = decoder.decodeFromOpus(null);
//                                        receiveHandler.handleUserAudio(new UserAudio(user, decodedAudio));
//                                    }
                                    short[] decodedAudio = decoder.decodeFromOpus(decryptedPacket);

                                    //If decodedAudio is null, then the Opus decode failed, so throw away the packet.
                                    if (decodedAudio == null)
                                    {
                                        LOG.trace("Received audio data but Opus failed to properly decode, instead it returned an error");
                                    }
                                    else
                                    {
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
                                            queue.add(Pair.<Long, short[]>of(System.currentTimeMillis(), decodedAudio));
                                        }
                                    }
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
                            LOG.log(e);
                        }
                    }
                }
            };
            receiveThread.setDaemon(true);
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
            combinedAudioExecutor = Executors.newSingleThreadScheduledExecutor( r ->
                    new Thread(AudioManagerImpl.AUDIO_THREADS, r, threadIdentifier + " Combined Thread"));
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
                    LOG.log(e);
                }
            }, 0, 20, TimeUnit.MILLISECONDS);
        }
    }

    private class PacketProvider implements IPacketProvider
    {
        char seq = 0;           //Sequence of audio packets. Used to determine the order of the packets.
        int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.

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
        public DatagramPacket getNextPacket(boolean changeTalking)
        {
            DatagramPacket nextPacket = null;

            try
            {
                if (sentSilenceOnConnect && sendHandler != null && sendHandler.canProvide())
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
                            rawAudio = encodeToOpus(rawAudio);
                        }
                        AudioPacket packet = new AudioPacket(seq, timestamp, webSocket.getSSRC(), rawAudio);
                        if (!speaking)
                            setSpeaking(true);

                        nextPacket = packet.asEncryptedUdpPacket(webSocket.getAddress(), webSocket.getSecretKey());

                        if (seq + 1 > Character.MAX_VALUE)
                            seq = 0;
                        else
                            seq++;
                    }
                }
                else if (silenceCounter > -1)
                {
                    AudioPacket packet = new AudioPacket(seq, timestamp, webSocket.getSSRC(), silenceBytes);

                    nextPacket = packet.asEncryptedUdpPacket(webSocket.getAddress(), webSocket.getSecretKey());

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
                    setSpeaking(false);
            }
            catch (Exception e)
            {
                LOG.log(e);
            }

            if (nextPacket != null)
                timestamp += OPUS_FRAME_SIZE;

            return nextPacket;
        }

        @Override
        public void onConnectionError(ConnectionStatus status)
        {
            LOG.warn("IAudioSendSystem reported a connection error of: " + status);
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
        nonEncodedBuffer.flip();

        //TODO: check for 0 / negative value for error.
        int result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, OPUS_FRAME_SIZE, encoded, encoded.capacity());

        //ENCODING STOPS HERE

        byte[] audio = new byte[result];
        encoded.get(audio);
        return audio;
    }

    private void setSpeaking(boolean isSpeaking)
    {
        this.speaking = isSpeaking;
        JSONObject obj = new JSONObject()
                .put("op", 5)
                .put("d", new JSONObject()
                        .put("speaking", isSpeaking)
                        .put("delay", 0)
                );
        webSocket.send(obj.toString());
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
    protected void finalize() throws Throwable
    {
        super.finalize();
        shutdown();
    }
}
