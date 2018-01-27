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

package net.dv8tion.jda.core.audio.factory;

import net.dv8tion.jda.core.audio.AudioConnection;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import org.slf4j.MDC;

import javax.annotation.CheckForNull;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.util.concurrent.ConcurrentMap;

import static net.dv8tion.jda.core.audio.AudioConnection.OPUS_FRAME_TIME_AMOUNT;

/**
 * The default implementation of the {@link net.dv8tion.jda.core.audio.factory.IAudioSendSystem IAudioSendSystem}.
 * <br>This implementation uses a Java thread, named based on: {@link IPacketProvider#getIdentifier()} + " Sending Thread".
 */
public class DefaultSendSystem implements IAudioSendSystem
{
    private final IPacketProvider packetProvider;
    private Thread sendThread;
    private ConcurrentMap<String, String> contextMap;

    public DefaultSendSystem(IPacketProvider packetProvider)
    {
        this.packetProvider = packetProvider;
    }

    @Override
    public void setContextMap(@CheckForNull ConcurrentMap<String, String> contextMap)
    {
        this.contextMap = contextMap;
    }

    @Override
    public void start()
    {
        final DatagramSocket udpSocket = packetProvider.getUdpSocket();

        sendThread = new Thread(AudioManagerImpl.AUDIO_THREADS, () ->
        {
            if (contextMap != null)
                MDC.setContextMap(contextMap);
            long lastFrameSent = System.currentTimeMillis();
            while (!udpSocket.isClosed() && !sendThread.isInterrupted())
            {
                try
                {
                    boolean changeTalking = (System.currentTimeMillis() - lastFrameSent) > OPUS_FRAME_TIME_AMOUNT;
                    DatagramPacket packet = packetProvider.getNextPacket(changeTalking);

                    if (packet != null)
                        udpSocket.send(packet);
                }
                catch (NoRouteToHostException e)
                {
                    packetProvider.onConnectionLost();
                }
                catch (SocketException e)
                {
                    //Most likely the socket has been closed due to the audio connection be closed. Next iteration will kill loop.
                }
                catch (Exception e)
                {
                    AudioConnection.LOG.error("Error while sending udp audio data", e);
                }
                finally
                {
                    long sleepTime = (OPUS_FRAME_TIME_AMOUNT) - (System.currentTimeMillis() - lastFrameSent);
                    if (sleepTime > 0)
                    {
                        try
                        {
                            Thread.sleep(sleepTime);
                        }
                        catch (InterruptedException e)
                        {
                            //We've been asked to stop.
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (System.currentTimeMillis() < lastFrameSent + 60) // If the sending didn't took longer than 60ms (3 times the time frame)
                    {
                        lastFrameSent += OPUS_FRAME_TIME_AMOUNT; // increase lastFrameSent
                    }
                    else
                    {
                        lastFrameSent = System.currentTimeMillis(); // else reset lastFrameSent to current time
                    }
                }
            }
        });
        sendThread.setUncaughtExceptionHandler((thread, throwable) ->
        {
            JDALogger.getLog(DefaultSendSystem.class).error("Uncaught exception in audio send thread", throwable);
            start();
        });
        sendThread.setDaemon(true);
        sendThread.setName(packetProvider.getIdentifier() + " Sending Thread");
        sendThread.setPriority((Thread.NORM_PRIORITY + Thread.MAX_PRIORITY) / 2);
        sendThread.start();
    }

    @Override
    public void shutdown()
    {
        if (sendThread != null)
            sendThread.interrupt();
    }
}
