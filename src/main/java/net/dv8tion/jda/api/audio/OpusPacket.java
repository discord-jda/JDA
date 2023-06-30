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

package net.dv8tion.jda.api.audio;

import net.dv8tion.jda.internal.audio.AudioPacket;
import net.dv8tion.jda.internal.audio.Decoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A raw OPUS packet received from Discord that can be used for lazy decoding.
 *
 * @since  4.0.0
 *
 * @see AudioReceiveHandler#canReceiveEncoded()
 * @see AudioReceiveHandler#handleEncodedAudio(OpusPacket)
 */
public final class OpusPacket implements Comparable<OpusPacket>
{
    /** (Hz) We want to use the highest of qualities! All the bandwidth! */
    public static final int OPUS_SAMPLE_RATE = 48000;
    /** An opus frame size of 960 at 48000hz represents 20 milliseconds of audio. */
    public static final int OPUS_FRAME_SIZE = 960;
    /** This is 20 milliseconds. We are only dealing with 20ms opus packets. */
    public static final int OPUS_FRAME_TIME_AMOUNT = 20;
    /** We want to use stereo. If the audio given is mono, the encoder promotes it to Left and Right mono (stereo that is the same on both sides) */
    public static final int OPUS_CHANNEL_COUNT = 2;

    private final long userId;
    private final byte[] opusAudio;
    private final Decoder decoder;
    private final AudioPacket rawPacket;

    private short[] decoded;
    private boolean triedDecode;

    public OpusPacket(@Nonnull AudioPacket packet, long userId, @Nullable Decoder decoder)
    {
        this.rawPacket = packet;
        this.userId = userId;
        this.decoder = decoder;
        this.opusAudio = packet.getEncodedAudio().array();
    }

    /**
     * The sequence number of this packet. This is used as ordering key for {@link #compareTo(OpusPacket)}.
     * <br>A char represents an unsigned short value in this case.
     *
     * <p>Note that packet sequence is important for decoding. If a packet is out of sequence the decode
     * step will fail.
     *
     * @return The sequence number of this packet
     *
     * @see    <a href="http://www.rfcreader.com/#rfc3550_line548" target="_blank">RTP Header</a>
     */
    public char getSequence()
    {
        return rawPacket.getSequence();
    }

    /**
     * The timestamp for this packet. As specified by the RTP header.
     *
     * @return The timestamp
     *
     * @see    <a href="http://www.rfcreader.com/#rfc3550_line548" target="_blank">RTP Header</a>
     */
    public int getTimestamp()
    {
        return rawPacket.getTimestamp();
    }

    /**
     * The synchronization source identifier (SSRC) for the user that sent this audio packet.
     *
     * @return The SSRC
     *
     * @see    <a href="http://www.rfcreader.com/#rfc3550_line548" target="_blank">RTP Header</a>
     */
    public int getSSRC()
    {
        return rawPacket.getSSRC();
    }

    /**
     * The ID of the responsible {@link net.dv8tion.jda.api.entities.User}.
     *
     * @return The user id
     */
    public long getUserId()
    {
        return userId;
    }

    /**
     * Whether {@link #decode()} is possible.
     *
     * @return True, if decode is possible.
     */
    public boolean canDecode()
    {
        return decoder != null && decoder.isInOrder(getSequence());
    }

    /**
     * The raw opus audio, copied to a new array.
     *
     * @return The raw opus audio
     */
    @Nonnull
    public byte[] getOpusAudio()
    {
        //prevent write access to backing array
        return Arrays.copyOf(opusAudio, opusAudio.length);
    }

    /**
     * Attempts to decode the opus packet.
     * <br>This method is idempotent and will provide the same result on multiple calls
     * without decoding again.
     *
     * <p>For most use-cases {@link #getAudioData(double)} should be used instead.
     *
     * @throws java.lang.IllegalStateException
     *         If {@link #canDecode()} is false
     *
     * @return The decoded audio or {@code null} if decoding failed for some reason.
     *
     * @see    #canDecode()
     * @see    #getAudioData(double)
     */
    @Nullable
    public synchronized short[] decode()
    {
        if (triedDecode)
            return decoded;
        if (decoder == null)
            throw new IllegalStateException("No decoder available");
        if (!decoder.isInOrder(getSequence()))
            throw new IllegalStateException("Packet is not in order");
        triedDecode = true;
        return decoded = decoder.decodeFromOpus(rawPacket); // null if failed to decode
    }

    /**
     * Decodes and adjusts the opus audio for the specified volume.
     * <br>The provided volume should be a double precision floating point in the interval from 0 to 1.
     * In this case 0.5 would represent 50% volume for instance.
     *
     * @param  volume
     *         The volume
     *
     * @throws java.lang.IllegalArgumentException
     *         If {@link #decode()} returns null
     *
     * @return The stereo PCM audio data as specified by {@link net.dv8tion.jda.api.audio.AudioReceiveHandler#OUTPUT_FORMAT}.
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions") // the null case is handled with an exception
    public byte[] getAudioData(double volume)
    {
        return getAudioData(decode(), volume); // throws IllegalArgument if decode failed
    }

    /**
     * Decodes and adjusts the opus audio for the specified volume.
     * <br>The provided volume should be a double precision floating point in the interval from 0 to 1.
     * In this case 0.5 would represent 50% volume for instance.
     *
     * @param  decoded
     *         The decoded audio data
     * @param  volume
     *         The volume
     *
     * @throws java.lang.IllegalArgumentException
     *         If {@code decoded} is null
     *
     * @return The stereo PCM audio data as specified by {@link net.dv8tion.jda.api.audio.AudioReceiveHandler#OUTPUT_FORMAT}.
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions") // the null case is handled with an exception
    public static byte[] getAudioData(@Nonnull short[] decoded, double volume)
    {
        if (decoded == null)
            throw new IllegalArgumentException("Cannot get audio data from null");
        int byteIndex = 0;
        byte[] audio = new byte[decoded.length * 2];
        for (short s : decoded)
        {
            if (volume != 1.0)
                s = (short) (s * volume);

            byte leftByte  = (byte) ((s >>> 8) & 0xFF);
            byte rightByte = (byte)  (s        & 0xFF);
            audio[byteIndex] = leftByte;
            audio[byteIndex + 1] = rightByte;
            byteIndex += 2;
        }
        return audio;
    }

    @Override
    public int compareTo(@Nonnull OpusPacket o)
    {
        return getSequence() - o.getSequence();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getSequence(), getTimestamp(), getOpusAudio());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof OpusPacket))
            return false;
        OpusPacket other = (OpusPacket) obj;
        return getSequence() == other.getSequence()
            && getTimestamp() == other.getTimestamp()
            && getSSRC() == other.getSSRC();
    }
}
