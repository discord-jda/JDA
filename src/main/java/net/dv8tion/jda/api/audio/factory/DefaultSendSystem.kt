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
package net.dv8tion.jda.api.audio.factory

import net.dv8tion.jda.api.audio.OpusPacket
import net.dv8tion.jda.internal.audio.AudioConnection
import net.dv8tion.jda.internal.utils.JDALogger
import org.slf4j.MDC
import java.net.NoRouteToHostException
import java.net.SocketException
import java.util.concurrent.ConcurrentMap

/**
 * The default implementation of the [IAudioSendSystem][net.dv8tion.jda.api.audio.factory.IAudioSendSystem].
 * <br></br>This implementation uses a Java thread, named based on: [IPacketProvider.getIdentifier] + " Sending Thread".
 */
class DefaultSendSystem(private val packetProvider: IPacketProvider) : IAudioSendSystem {
    private var sendThread: Thread? = null
    private var contextMap: ConcurrentMap<String, String>? = null
    override fun setContextMap(contextMap: ConcurrentMap<String, String>?) {
        this.contextMap = contextMap
    }

    override fun start() {
        val udpSocket = packetProvider.udpSocket
        sendThread = Thread {
            if (contextMap != null) MDC.setContextMap(contextMap)
            var lastFrameSent = System.currentTimeMillis()
            var sentPacket = true
            while (!udpSocket.isClosed && !sendThread!!.isInterrupted) {
                try {
                    val changeTalking =
                        !sentPacket || System.currentTimeMillis() - lastFrameSent > OpusPacket.OPUS_FRAME_TIME_AMOUNT
                    val packet = packetProvider.getNextPacket(changeTalking)
                    sentPacket = packet != null
                    if (sentPacket) udpSocket.send(packet)
                } catch (e: NoRouteToHostException) {
                    packetProvider.onConnectionLost()
                } catch (e: SocketException) {
                    //Most likely the socket has been closed due to the audio connection be closed. Next iteration will kill loop.
                } catch (e: Exception) {
                    AudioConnection.LOG.error("Error while sending udp audio data", e)
                } finally {
                    val sleepTime = OpusPacket.OPUS_FRAME_TIME_AMOUNT - (System.currentTimeMillis() - lastFrameSent)
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime)
                        } catch (e: InterruptedException) {
                            //We've been asked to stop.
                            Thread.currentThread().interrupt()
                        }
                    }
                    if (System.currentTimeMillis() < lastFrameSent + 60) {
                        // If the sending didn't took longer than 60ms (3 times the time frame)
                        lastFrameSent += OpusPacket.OPUS_FRAME_TIME_AMOUNT.toLong()
                    } else {
                        // else reset lastFrameSent to current time
                        lastFrameSent = System.currentTimeMillis()
                    }
                }
            }
        }
        sendThread!!.setUncaughtExceptionHandler { thread: Thread?, throwable: Throwable? ->
            JDALogger.getLog(DefaultSendSystem::class.java).error("Uncaught exception in audio send thread", throwable)
            start()
        }
        sendThread!!.setDaemon(true)
        sendThread!!.setName(packetProvider.identifier + " Sending Thread")
        sendThread!!.setPriority((Thread.NORM_PRIORITY + Thread.MAX_PRIORITY) / 2)
        sendThread!!.start()
    }

    override fun shutdown() {
        if (sendThread != null) sendThread!!.interrupt()
    }
}
