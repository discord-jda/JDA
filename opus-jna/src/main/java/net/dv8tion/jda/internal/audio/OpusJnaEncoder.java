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

package net.dv8tion.jda.internal.audio;

import com.sun.jna.ptr.PointerByReference;
import net.dv8tion.jda.api.audio.OpusPacket;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;
import tomp2p.opuswrapper.Opus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class OpusJnaEncoder implements OpusEncoder
{
    public static final Logger LOG = JDALogger.getLog(OpusJnaEncoder.class);

    private PointerByReference opusEncoder;

    protected OpusJnaEncoder(PointerByReference opusEncoder)
    {
        this.opusEncoder = opusEncoder;
    }

    @Nullable
    @Override
    public ByteBuffer encode(@Nonnull ByteBuffer rawAudio)
    {
        int result;
        ByteBuffer encoded = ByteBuffer.allocate(4096);
        synchronized (this)
        {
            if (opusEncoder == null)
                throw new IllegalStateException("Encoder is closed.");

            ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(rawAudio.remaining() / 2);
            for (int i = rawAudio.position(); i < rawAudio.limit(); i += 2)
            {
                int firstByte =  (0x000000FF & rawAudio.get(i));      //Promotes to int and handles the fact that it was unsigned.
                int secondByte = (0x000000FF & rawAudio.get(i + 1));

                //Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
                short toShort = (short) ((firstByte << 8) | secondByte);

                nonEncodedBuffer.put(toShort);
            }
            ((Buffer) nonEncodedBuffer).flip();

            result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, OpusPacket.OPUS_FRAME_SIZE, encoded, encoded.capacity());
        }

        if (result <= 0)
        {
            LOG.error("Received error code from opus_encode(...): {}", result);
            return null;
        }

        ((Buffer) encoded).position(0).limit(result);
        return encoded;
    }

    @Override
    public void close()
    {
        if (opusEncoder != null)
        {
            Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
            opusEncoder = null;
        }
    }
}
