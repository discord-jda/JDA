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

import com.sun.jna.ptr.PointerByReference;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.audio.AudioConnectEvent;
import net.dv8tion.jda.events.audio.AudioTimeoutEvent;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONObject;
import tomp2p.opuswrapper.Opus;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class AudioConnection
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDAAudioConn");
    public static final int OPUS_SAMPLE_RATE = 48000;   //(Hz) We want to use the highest of qualities! All the bandwidth!
    public static final int OPUS_FRAME_SIZE = 960;      //An opus frame size of 960 at 48000hz represents 20 milliseconds of audio.
    public static final int OPUS_FRAME_TIME_AMOUNT = 20;//This is 20 milliseconds. We are only dealing with 20ms opus packets.
    public static final int OPUS_CHANNEL_COUNT = 2;     //We want to use stereo. If the audio given is mono, the encoder promotes it
                                                        // to Left and Right mono (stereo that is the same on both sides)
    private final AudioWebSocket webSocket;
    private DatagramSocket udpSocket;
    private VoiceChannel channel;
    private AudioSendHandler sendHandler = null;
    private AudioReceiveHandler receiveHandler = null;

    private PointerByReference opusEncoder;
    private PointerByReference opusDecoder;

    private Thread sendThread;
    private Thread receiveThread;

    private boolean speaking = false;

    public AudioConnection(AudioWebSocket webSocket, VoiceChannel channel)
    {
        this.channel = channel;
        this.webSocket = webSocket;

        IntBuffer error = IntBuffer.allocate(4);
        opusEncoder =
                Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);
    }

    public void ready(long timeout)
    {
        Thread readyThread = new Thread()
        {
            @Override
            public void run()
            {
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
                    }
                }
                if (!connectionTimeout)
                {
                    AudioConnection.this.udpSocket = webSocket.getUdpSocket();
                    setupSendThread();
                    setupReceiveThread();
                    api.getEventManager().handle(new AudioConnectEvent(api, AudioConnection.this.channel));
                }
                else
                {
                    webSocket.close(false, AudioWebSocket.CONNECTION_SETUP_TIMEOUT);
                    api.getEventManager().handle(new AudioTimeoutEvent(api, AudioConnection.this.channel, timeout));
                }
            }
        };
        readyThread.setDaemon(true);
        readyThread.start();
    }

    public void setSendingHandler(AudioSendHandler handler)
    {
        this.sendHandler = handler;
    }

    public void setReceivingHandler(AudioReceiveHandler handler)
    {
        this.receiveHandler = handler;
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

    public void close(boolean regionChange)
    {
//        setSpeaking(false);
        sendThread.interrupt();
        receiveThread.interrupt();
        webSocket.close(regionChange, -1);
    }

    private void setupSendThread()
    {
        sendThread = new Thread()
        {
            @Override
            public void run()
            {
                char seq = 0;           //Sequence of audio packets. Used to determine the order of the packets.
                int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.
                long lastFrameSent = System.currentTimeMillis();
                while (!udpSocket.isClosed() && !this.isInterrupted())
                {
                    try
                    {
                        //WE NEED TO CONSIDER BUFFERING STUFF BECAUSE REASONS.
                        //Consider storing 40-60ms of encoded audio as a buffer.
                        if (sendHandler != null && sendHandler.canProvide())
                        {
                            byte[] rawAudio = sendHandler.provide20MsAudio();
                            if (rawAudio == null || rawAudio.length == 0)
                            {
                                if (speaking && (System.currentTimeMillis() - lastFrameSent) > OPUS_FRAME_TIME_AMOUNT)
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
                                udpSocket.send(packet.asEncryptedUdpPacket(webSocket.getAddress(), webSocket.getSecretKey()));

                                if (seq + 1 > Character.MAX_VALUE)
                                    seq = 0;
                                else
                                    seq++;
							}
                        }
                        else if (speaking && (System.currentTimeMillis() - lastFrameSent) > OPUS_FRAME_TIME_AMOUNT)
                            setSpeaking(false);
                    }
                    catch (NoRouteToHostException e)
                    {
                        LOG.warn("Closing AudioConnection due to inability to send audio packets.");
                        LOG.warn("Cannot send audio packet because JDA navigate the route to Discord.\n" +
                                "Are you sure you have internet connection? It is likely that you've lost connection.");
                        webSocket.close(true, -1);
                    }
                    catch (SocketException e)
                    {
                        //Most likely the socket has been closed due to the audio connection be closed. Next iteration will kill loop.
                    }
                    catch (Exception e)
                    {
                        LOG.log(e);
                    }
                    finally
                    {
                        timestamp += OPUS_FRAME_SIZE;
                        long sleepTime = (OPUS_FRAME_TIME_AMOUNT) - (System.currentTimeMillis() - lastFrameSent);
                        if (sleepTime > 0)
                        {
                            try
                            {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e)
                            {
                                //We've been asked to stop. The next iteration will kill the loop. 
                            }
                        }
                        lastFrameSent += OPUS_FRAME_TIME_AMOUNT;
                    }
                }
            }
        };
        sendThread.setPriority((Thread.NORM_PRIORITY + Thread.MAX_PRIORITY) / 2);
        sendThread.setDaemon(true);
        sendThread.start();
    }

    private void setupReceiveThread()
    {
        receiveThread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    udpSocket.setSoTimeout(100);
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

                        if (receiveHandler != null && receiveHandler.canReceive() && webSocket.getSecretKey() != null)
                        {
                            //Currently just gives the raw packet with STILL ENCODED DATA
                            //This needs to be changed to ->
                                //1) possibly buffer by 40-60ms (configurable)
                                //2) decode from Opus -> raw PCM or another format as defined by the receiveHandler.
                            AudioPacket decryptedPacket = AudioPacket.decryptAudioPacket(receivedPacket, webSocket.getSecretKey());

                            receiveHandler.handleReceivedAudio(decryptedPacket);
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

    private byte[] decodeFromOpus(byte[] encodedAudio)
    {
        return null;
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
    }
}
