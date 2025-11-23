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

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import net.dv8tion.jda.api.audio.OpusPacket;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;
import tomp2p.opuswrapper.Opus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.IntBuffer;

public class OpusJnaCodecFactory implements OpusCodecFactory {

    public static final Logger LOG = JDALogger.getLog(OpusJnaCodecFactory.class);

    private static boolean initialized = false;
    private static boolean audioSupported = false;

    @Override
    public synchronized boolean initialize() throws Exception
    {
        if (initialized)
            return audioSupported;
        initialized = true;

        if (OpusLibrary.isInitialized())
            return audioSupported = true;
        OpusLibrary.loadFromJar();
        return audioSupported = true;
    }

    @Nonnull
    @Override
    public OpusDecoder createDecoder(int ssrc)
    {
        if (!initialized)
            throw new IllegalStateException("Opus is not initialized");

        IntBuffer error = IntBuffer.allocate(1);
        PointerByReference opusDecoder = Opus.INSTANCE.opus_decoder_create(OpusPacket.OPUS_SAMPLE_RATE, OpusPacket.OPUS_CHANNEL_COUNT, error);
        if (error.get() != Opus.OPUS_OK && opusDecoder == null)
            throw new IllegalStateException("Received error code from opus_decoder_create(...): " + error.get());

        return new OpusJnaDecoder(ssrc, opusDecoder);
    }

    @Nullable
    @Override
    public OpusEncoder createEncoder()
    {
        if (!initialized)
            throw new IllegalStateException("Opus is not initialized");

        IntBuffer error = IntBuffer.allocate(1);
        PointerByReference opusEncoder = Opus.INSTANCE.opus_encoder_create(OpusPacket.OPUS_SAMPLE_RATE, OpusPacket.OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);
        if (error.get() != Opus.OPUS_OK && opusEncoder == null)
        {
            LOG.error("Received error status from opus_encoder_create(...): {}", error.get());
            return null;
        }

        return new OpusJnaEncoder(opusEncoder);
    }
}
