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
package net.dv8tion.jda.api.audio

import net.dv8tion.jda.internal.audio.AudioPacket
import net.dv8tion.jda.internal.audio.Decoder
import java.util.*
import javax.annotation.Nonnull

/**
 * A raw OPUS packet received from Discord that can be used for lazy decoding.
 *
 * @since  4.0.0
 *
 * @see AudioReceiveHandler.canReceiveEncoded
 * @see AudioReceiveHandler.handleEncodedAudio
 */
class OpusPacket(
    @param:Nonnull private val rawPacket: AudioPacket,
    /**
     * The ID of the responsible [net.dv8tion.jda.api.entities.User].
     *
     * @return The user id
     */
    val userId: Long, private val decoder: Decoder?
) : Comparable<OpusPacket> {
    private val opusAudio: ByteArray
    private var decoded: ShortArray
    private var triedDecode = false

    init {
        opusAudio = rawPacket.encodedAudio.array()
    }

    val sequence: Char
        /**
         * The sequence number of this packet. This is used as ordering key for [.compareTo].
         * <br></br>A char represents an unsigned short value in this case.
         *
         *
         * Note that packet sequence is important for decoding. If a packet is out of sequence the decode
         * step will fail.
         *
         * @return The sequence number of this packet
         *
         * @see [RTP Header](http://www.rfcreader.com/.rfc3550_line548)
         */
        get() = rawPacket.sequence
    val timestamp: Int
        /**
         * The timestamp for this packet. As specified by the RTP header.
         *
         * @return The timestamp
         *
         * @see [RTP Header](http://www.rfcreader.com/.rfc3550_line548)
         */
        get() = rawPacket.timestamp
    val sSRC: Int
        /**
         * The synchronization source identifier (SSRC) for the user that sent this audio packet.
         *
         * @return The SSRC
         *
         * @see [RTP Header](http://www.rfcreader.com/.rfc3550_line548)
         */
        get() = rawPacket.ssrc

    /**
     * Whether [.decode] is possible.
     *
     * @return True, if decode is possible.
     */
    fun canDecode(): Boolean {
        return decoder != null && decoder.isInOrder(sequence)
    }

    /**
     * The raw opus audio, copied to a new array.
     *
     * @return The raw opus audio
     */
    @Nonnull
    fun getOpusAudio(): ByteArray {
        //prevent write access to backing array
        return opusAudio.copyOf(opusAudio.size)
    }

    /**
     * Attempts to decode the opus packet.
     * <br></br>This method is idempotent and will provide the same result on multiple calls
     * without decoding again.
     *
     *
     * For most use-cases [.getAudioData] should be used instead.
     *
     * @throws java.lang.IllegalStateException
     * If [.canDecode] is false
     *
     * @return The decoded audio or `null` if decoding failed for some reason.
     *
     * @see .canDecode
     * @see .getAudioData
     */
    @Synchronized
    fun decode(): ShortArray? {
        if (triedDecode) return decoded
        checkNotNull(decoder) { "No decoder available" }
        check(decoder.isInOrder(sequence)) { "Packet is not in order" }
        triedDecode = true
        return decoder.decodeFromOpus(rawPacket).also {
            decoded = it // null if failed to decode
        }
    }

    /**
     * Decodes and adjusts the opus audio for the specified volume.
     * <br></br>The provided volume should be a double precision floating point in the interval from 0 to 1.
     * In this case 0.5 would represent 50% volume for instance.
     *
     * @param  volume
     * The volume
     *
     * @throws java.lang.IllegalArgumentException
     * If [.decode] returns null
     *
     * @return The stereo PCM audio data as specified by [net.dv8tion.jda.api.audio.AudioReceiveHandler.OUTPUT_FORMAT].
     */
    @Nonnull  // the null case is handled with an exception
    fun getAudioData(volume: Double): ByteArray {
        return getAudioData(decode(), volume) // throws IllegalArgument if decode failed
    }

    override fun compareTo(@Nonnull o: OpusPacket): Int {
        return sequence.code - o.sequence.code
    }

    override fun hashCode(): Int {
        return Objects.hash(sequence, timestamp, getOpusAudio())
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is OpusPacket) return false
        val other = obj
        return sequence == other.sequence && timestamp == other.timestamp && sSRC == other.sSRC
    }

    companion object {
        /** (Hz) We want to use the highest of qualities! All the bandwidth!  */
        const val OPUS_SAMPLE_RATE = 48000

        /** An opus frame size of 960 at 48000hz represents 20 milliseconds of audio.  */
        const val OPUS_FRAME_SIZE = 960

        /** This is 20 milliseconds. We are only dealing with 20ms opus packets.  */
        const val OPUS_FRAME_TIME_AMOUNT = 20

        /** We want to use stereo. If the audio given is mono, the encoder promotes it to Left and Right mono (stereo that is the same on both sides)  */
        const val OPUS_CHANNEL_COUNT = 2

        /**
         * Decodes and adjusts the opus audio for the specified volume.
         * <br></br>The provided volume should be a double precision floating point in the interval from 0 to 1.
         * In this case 0.5 would represent 50% volume for instance.
         *
         * @param  decoded
         * The decoded audio data
         * @param  volume
         * The volume
         *
         * @throws java.lang.IllegalArgumentException
         * If `decoded` is null
         *
         * @return The stereo PCM audio data as specified by [net.dv8tion.jda.api.audio.AudioReceiveHandler.OUTPUT_FORMAT].
         */
        @Nonnull  // the null case is handled with an exception
        fun getAudioData(@Nonnull decoded: ShortArray?, volume: Double): ByteArray {
            requireNotNull(decoded) { "Cannot get audio data from null" }
            var byteIndex = 0
            val audio = ByteArray(decoded.size * 2)
            for (s in decoded) {
                if (volume != 1.0) s = (s * volume).toInt().toShort()
                val leftByte = (s.toInt() ushr 8 and 0xFF).toByte()
                val rightByte = (s.toInt() and 0xFF).toByte()
                audio[byteIndex] = leftByte
                audio[byteIndex + 1] = rightByte
                byteIndex += 2
            }
            return audio
        }
    }
}
