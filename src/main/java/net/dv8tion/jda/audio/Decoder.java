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

        IntBuffer error = IntBuffer.allocate(4);
        opusDecoder = Opus.INSTANCE.opus_decoder_create(AudioConnection.OPUS_SAMPLE_RATE,
                AudioConnection.OPUS_CHANNEL_COUNT, error);
        //TODO: check `error` for an error flag.
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
            char seq = decryptedPacket.getSequence();
            this.lastSeq = seq;
            this.lastTimestamp = decryptedPacket.getTimestamp();

            byte[] encodedAudio = decryptedPacket.getEncodedAudio();

            result = Opus.INSTANCE.opus_decode(opusDecoder, encodedAudio, encodedAudio.length, decoded,
                    AudioConnection.OPUS_FRAME_SIZE, 0);
        }

        //If we get a result that is less than 0, then there was an error. Return null as a signifier.
        if (result < 0)
            return null;

        short[] audio = new short[result * 2];
        decoded.get(audio);
        return audio;
    }

    protected void close()
    {
        Opus.INSTANCE.opus_decoder_destroy(opusDecoder);
    }
}
