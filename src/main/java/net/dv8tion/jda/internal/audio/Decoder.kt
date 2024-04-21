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
package net.dv8tion.jda.internal.audio

import com.sun.jna.ptr.PointerByReference
import net.dv8tion.jda.api.audio.OpusPacket
import tomp2p.opuswrapper.Opus
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

/**
 * Class that wraps functionality around the Opus decoder.
 */
class Decoder(protected var ssrc: Int) {
    protected var lastSeq: Char
    protected var lastTimestamp: Int
    protected var opusDecoder: PointerByReference?

    init {
        lastSeq = -1.toChar()
        lastTimestamp = -1
        val error: IntBuffer = IntBuffer.allocate(1)
        opusDecoder =
            Opus.INSTANCE.opus_decoder_create(OpusPacket.OPUS_SAMPLE_RATE, OpusPacket.OPUS_CHANNEL_COUNT, error)
        if (error.get() != Opus.OPUS_OK && opusDecoder == null) throw IllegalStateException("Received error code from opus_decoder_create(...): " + error.get())
    }

    fun isInOrder(newSeq: Char): Boolean {
        return (lastSeq == -1.toChar()) || (newSeq > lastSeq) || (lastSeq.code - newSeq.code > 10)
    }

    fun wasPacketLost(newSeq: Char): Boolean {
        return newSeq.code > lastSeq.code + 1
    }

    fun decodeFromOpus(decryptedPacket: AudioPacket?): ShortArray? {
        val result: Int
        val decoded: ShortBuffer = ShortBuffer.allocate(4096)
        if (decryptedPacket == null) //Flag for packet-loss
        {
            result = Opus.INSTANCE.opus_decode(opusDecoder, null, 0, decoded, OpusPacket.OPUS_FRAME_SIZE, 0)
            lastSeq = -1.toChar()
            lastTimestamp = -1
        } else {
            lastSeq = decryptedPacket.getSequence()
            lastTimestamp = decryptedPacket.getTimestamp()
            val encodedAudio: ByteBuffer? = decryptedPacket.getEncodedAudio()
            val length: Int = encodedAudio!!.remaining()
            val offset: Int = encodedAudio.arrayOffset() + encodedAudio.position()
            val buf: ByteArray = ByteArray(length)
            val data: ByteArray = encodedAudio.array()
            System.arraycopy(data, offset, buf, 0, length)
            result = Opus.INSTANCE.opus_decode(opusDecoder, buf, buf.size, decoded, OpusPacket.OPUS_FRAME_SIZE, 0)
        }

        //If we get a result that is less than 0, then there was an error. Return null as a signifier.
        if (result < 0) {
            handleDecodeError(result)
            return null
        }
        val audio: ShortArray = ShortArray(result * 2)
        decoded.get(audio)
        return audio
    }

    private fun handleDecodeError(result: Int) {
        val b: StringBuilder = StringBuilder("Decoder failed to decode audio from user with code ")
        when (result) {
            Opus.OPUS_BAD_ARG -> b.append("OPUS_BAD_ARG")
            Opus.OPUS_BUFFER_TOO_SMALL -> b.append("OPUS_BUFFER_TOO_SMALL")
            Opus.OPUS_INTERNAL_ERROR -> b.append("OPUS_INTERNAL_ERROR")
            Opus.OPUS_INVALID_PACKET -> b.append("OPUS_INVALID_PACKET")
            Opus.OPUS_UNIMPLEMENTED -> b.append("OPUS_UNIMPLEMENTED")
            Opus.OPUS_INVALID_STATE -> b.append("OPUS_INVALID_STATE")
            Opus.OPUS_ALLOC_FAIL -> b.append("OPUS_ALLOC_FAIL")
            else -> b.append(result)
        }
        AudioConnection.Companion.LOG.debug("{}", b)
    }

    @Synchronized
    fun close() {
        if (opusDecoder != null) {
            Opus.INSTANCE.opus_decoder_destroy(opusDecoder)
            opusDecoder = null
        }
    }

    @Suppress("deprecation")
    /* If this was in JDK9 we would be using java.lang.ref.Cleaner instead! */
    @Throws(
        Throwable::class
    )
    protected fun finalize() {
        super.finalize()
        close()
    }
}
