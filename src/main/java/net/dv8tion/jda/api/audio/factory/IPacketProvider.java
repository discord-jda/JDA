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

package net.dv8tion.jda.api.audio.factory;

import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Represents the connection between a {@link net.dv8tion.jda.api.audio.factory.IAudioSendSystem IAudioSendSystem} and
 * JDA's internal audio system, providing access to audio packets built from data provided from
 * {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandlers}.
 *
 * <p><b>Note that this provider is not thread-safe!</b>
 */
@NotThreadSafe
public interface IPacketProvider
{
    /**
     * Provides a unique String identifier for the connection.
     * <br>Uses shard information and specific audio connection information to build string.
     *
     * @return Never-null String unique to this audio connection.
     */
    @Nonnull
    String getIdentifier();

    /**
     * Provides the current channel that this connection is transmitting to.
     *
     * @return The {@link AudioChannel} that this connection is sending to.
     */
    @Nonnull
    AudioChannel getConnectedChannel();

    /**
     * The UDP connection for this audio connection. The {@link net.dv8tion.jda.api.audio.factory.DefaultSendSystem DefaultSendSystem}
     * uses this socket to send audio packets to discord, and this is also the socket used to receive audio packets from discord.
     * <br>If you are implementing your own system, it is recommended that you used this connection as it is part of JDA's internal
     * system that JDA monitors for errors and closures. It should be noted however that using this is not required to
     * send audio packets if the developer wishes to open their own UDP socket to send from.
     *
     * @return The UDP socket connection used for audio sending.
     */
    @Nonnull
    DatagramSocket getUdpSocket();

    /**
     * The connected socket address for this audio connection. This can be useful for developers
     * to open their own socket for datagram sending and allows to avoid using {@link #getNextPacket(boolean)}.
     *
     * @return {@link InetSocketAddress} of the current UDP connection
     */
    @Nonnull
    InetSocketAddress getSocketAddress();

    /**
     * Used to retrieve an audio packet to send to Discord. The packet provided is already converted to Opus and
     * encrypted, and as such is completely ready to be sent to Discord.
     *
     * <p>The {@link java.nio.ByteBuffer#position()} will be positioned on the start of the packet to send
     * and the {@link java.nio.ByteBuffer#limit()} at the end of it. Use {@link java.nio.ByteBuffer#remaining()}
     * to check the length of the packet.
     *
     * <p><b>Note:</b> When the AudioSendHandler cannot or does not provide a new packet to send, this method will return null.
     *
     * <p><u>The buffer used here may be used again on the next call to this getter, if you plan on storing the data, copy it.
     * The buffer was created using {@link ByteBuffer#allocate(int)} and is not direct.</u>
     *
     * @return Possibly-null {@link ByteBuffer} containing an encoded and encrypted packet
     *         of audio data ready to be sent to discord.
     */
    @Nullable
    ByteBuffer getNextPacketRaw(boolean unused);

    /**
     * Used to retrieve an audio packet to send to Discord. The packet provided is already converted to Opus and
     * encrypted, and as such is completely ready to be sent to Discord.
     *
     * <p><b>Note:</b> When the AudioSendHandler cannot or does not provide a new packet to send, this method will return null.
     *
     * @return Possibly-null {@link java.net.DatagramPacket DatagramPacket} containing an encoded and encrypted packet
     *         of audio data ready to be sent to discord.
     */
    @Nullable
    DatagramPacket getNextPacket(boolean unused);

    /**
     * This method is used to indicate a connection error to JDA so that the connection can be properly shutdown.
     * <br>This is useful if, during setup or operation, an unrecoverable error is encountered.
     *
     * @param  status
     *         The {@link net.dv8tion.jda.api.audio.hooks.ConnectionStatus ConnectionStatus} being reported to JDA
     *         indicating an error with connection.
     */
    void onConnectionError(@Nonnull ConnectionStatus status);

    /**
     * This method is used to indicate to JDA that the UDP connection has been lost, whether that be due internet loss
     * or some other unknown reason. This is similar to
     * {@link #onConnectionError(net.dv8tion.jda.api.audio.hooks.ConnectionStatus)} as it provides a default error
     * reason of {@link net.dv8tion.jda.api.audio.hooks.ConnectionStatus#ERROR_LOST_CONNECTION}.
     */
    void onConnectionLost();
}
