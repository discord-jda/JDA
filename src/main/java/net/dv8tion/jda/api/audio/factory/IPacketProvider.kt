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

import net.dv8tion.jda.api.audio.hooks.ConnectionStatus
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import javax.annotation.Nonnull
import javax.annotation.concurrent.NotThreadSafe

/**
 * Represents the connection between a [IAudioSendSystem][net.dv8tion.jda.api.audio.factory.IAudioSendSystem] and
 * JDA's internal audio system, providing access to audio packets built from data provided from
 * [AudioSendHandlers][net.dv8tion.jda.api.audio.AudioSendHandler].
 *
 *
 * **Note that this provider is not thread-safe!**
 */
@NotThreadSafe
interface IPacketProvider {
    @get:Nonnull
    val identifier: String?

    @get:Nonnull
    val connectedChannel: AudioChannel?

    @get:Nonnull
    val udpSocket: DatagramSocket?

    @get:Nonnull
    val socketAddress: InetSocketAddress?

    /**
     * Used to retrieve an audio packet to send to Discord. The packet provided is already converted to Opus and
     * encrypted, and as such is completely ready to be sent to Discord.
     *
     *
     * The [java.nio.ByteBuffer.position] will be positioned on the start of the packet to send
     * and the [java.nio.ByteBuffer.limit] at the end of it. Use [java.nio.ByteBuffer.remaining]
     * to check the length of the packet.
     *
     *
     * **Note:** When the AudioSendHandler cannot or does not provide a new packet to send, this method will return null.
     *
     *
     * <u>The buffer used here may be used again on the next call to this getter, if you plan on storing the data, copy it.
     * The buffer was created using [ByteBuffer.allocate] and is not direct.</u>
     *
     * @return Possibly-null [ByteBuffer] containing an encoded and encrypted packet
     * of audio data ready to be sent to discord.
     */
    fun getNextPacketRaw(unused: Boolean): ByteBuffer?

    /**
     * Used to retrieve an audio packet to send to Discord. The packet provided is already converted to Opus and
     * encrypted, and as such is completely ready to be sent to Discord.
     *
     *
     * **Note:** When the AudioSendHandler cannot or does not provide a new packet to send, this method will return null.
     *
     * @return Possibly-null [DatagramPacket][java.net.DatagramPacket] containing an encoded and encrypted packet
     * of audio data ready to be sent to discord.
     */
    fun getNextPacket(unused: Boolean): DatagramPacket?

    /**
     * This method is used to indicate a connection error to JDA so that the connection can be properly shutdown.
     * <br></br>This is useful if, during setup or operation, an unrecoverable error is encountered.
     *
     * @param  status
     * The [ConnectionStatus][net.dv8tion.jda.api.audio.hooks.ConnectionStatus] being reported to JDA
     * indicating an error with connection.
     */
    fun onConnectionError(@Nonnull status: ConnectionStatus?)

    /**
     * This method is used to indicate to JDA that the UDP connection has been lost, whether that be due internet loss
     * or some other unknown reason. This is similar to
     * [.onConnectionError] as it provides a default error
     * reason of [net.dv8tion.jda.api.audio.hooks.ConnectionStatus.ERROR_LOST_CONNECTION].
     */
    fun onConnectionLost()
}
