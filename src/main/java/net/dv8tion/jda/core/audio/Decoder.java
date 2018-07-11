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

import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Class that wraps functionality around the Opus decoder.
 */
public class Decoder
{
    protected int ssrc;
    protected char lastSeq;
    protected int lastTimestamp;
    protected PointerByReference opusDecoder;

    protected Decoder(int ssrc)
    {
        this.ssrc = ssrc;
        this.lastSeq = (char) -1;
        this.lastTimestamp = -1;

        IntBuffer error = IntBuffer.allocate(1);
        opusDecoder = Opus.INSTANCE.opus_decoder_create(AudioConnection.OPUS_SAMPLE_RATE, AudioConnection.OPUS_CHANNEL_COUNT, error);
        if (error.get() != Opus.OPUS_OK && opusDecoder == null)
            throw new IllegalStateException("Received error code from opus_decoder_create(...): " + error.get());
    }

    protected boolean isInOrder(char newSeq)
    {
        return lastSeq == -1 || newSeq > lastSeq || lastSeq - newSeq > 10;
    }

    protected boolean wasPacketLost(char newSeq)
    {
        return newSeq > lastSeq + 1;
    }

    protected short[] decodeFromOpus(AudioPacket decryptedPacket)
    {
        int result;
        ShortBuffer decoded = ShortBuffer.allocate(4096);
        if (decryptedPacket == null)    //Flag for packet-loss
        {
            result = Opus.INSTANCE.opus_decode(opusDecoder, null, 0, decoded,
                    AudioConnection.OPUS_FRAME_SIZE, 0);
            lastSeq = (char) -1;
            lastTimestamp = -1;
        }
        else
        {
            this.lastSeq = decryptedPacket.getSequence();
            this.lastTimestamp = decryptedPacket.getTimestamp();

            byte[] encodedAudio = decryptedPacket.getEncodedAudio();

            result = Opus.INSTANCE.opus_decode(opusDecoder, encodedAudio, encodedAudio.length, decoded,
                    AudioConnection.OPUS_FRAME_SIZE, 0);
        }

        //If we get a result that is less than 0, then there was an error. Return null as a signifier.
        if (result < 0)
        {
            handleDecodeError(result);
            return null;
        }

        short[] audio = new short[result * 2];
        decoded.get(audio);
        return audio;
    }

    private void handleDecodeError(int result)
    {
        StringBuilder b = new StringBuilder("Decoder failed to decode audio from user with code ");
        switch (result)
        {
            case Opus.OPUS_BAD_ARG: //-1
                b.append("OPUS_BAD_ARG");
                break;
            case Opus.OPUS_BUFFER_TOO_SMALL: //-2
                b.append("OPUS_BUFFER_TOO_SMALL");
                break;
            case Opus.OPUS_INTERNAL_ERROR: //-3
                b.append("OPUS_INTERNAL_ERROR");
                break;
            case Opus.OPUS_INVALID_PACKET: //-4
                b.append("OPUS_INVALID_PACKET");
                break;
            case Opus.OPUS_UNIMPLEMENTED: //-5
                b.append("OPUS_UNIMPLEMENTED");
                break;
            case Opus.OPUS_INVALID_STATE: //-6
                b.append("OPUS_INVALID_STATE");
                break;
            case Opus.OPUS_ALLOC_FAIL: //-7
                b.append("OPUS_ALLOC_FAIL");
                break;
            default:
                b.append(result);
        }
        AudioConnection.LOG.debug("{}", b);
    }

    protected synchronized void close()
    {
        if (opusDecoder != null)
        {
            Opus.INSTANCE.opus_decoder_destroy(opusDecoder);
            opusDecoder = null;
        }
    }

    @Override
    @Deprecated
    protected void finalize() throws Throwable
    {
        super.finalize();
        close();
    }
}
