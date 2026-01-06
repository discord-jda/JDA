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

package net.dv8tion.jda.api.audio.opus;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Encodes raw audio using Opus.
 *
 * @see IOpusCodecFactory#createEncoder()
 */
public interface IOpusEncoder {
    /**
     * Closes the encoder, closing more than once is a no-op.
     */
    void close();

    /**
     * Encodes the audio provided by {@link net.dv8tion.jda.api.audio.AudioSendHandler#provide20MsAudio() AudioSendHandler.provide20MsAudio()} using Opus.
     *
     * <p>This method <b>must</b> return {@code null} if decoding fails for any reason,
     * this method is responsible for logging the issue.
     *
     * @param  rawAudio
     *         The audio to Opus encode
     *
     * @throws IllegalStateException If the encoder is closed
     *
     * @return A {@link ByteBuffer} of the Opus-encoded audio, or {@code null} on failure
     *
     * @see net.dv8tion.jda.api.audio.OpusPacket#OPUS_FRAME_SIZE OpusPacket.OPUS_FRAME_SIZE
     */
    @Nullable
    ByteBuffer encode(@Nonnull ByteBuffer rawAudio);
}
