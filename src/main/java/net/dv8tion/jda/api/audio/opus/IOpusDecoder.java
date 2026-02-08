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

import net.dv8tion.jda.internal.audio.AudioPacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Decoder from Opus-encoded audio to raw audio.
 *
 * @see IOpusCodecFactory#createDecoder()
 */
public interface IOpusDecoder {
    /**
     * Closes the decoder, closing more than once is a no-op.
     */
    void close();

    /**
     * Checks if the provided sequence number is more recent than the most recent decoded audio packet.
     *
     * @param  sequence
     *         The new sequence number to test against
     *
     * @return {@code true} if the provided sequence number is in order
     */
    boolean isInOrder(char sequence);

    /**
     * Decodes the provided 20ms of Opus audio into an array
     * of raw audio samples interleaved for {@linkplain net.dv8tion.jda.api.audio.OpusPacket#OPUS_CHANNEL_COUNT 2 channels}.
     *
     * <p>This method <b>must</b> return {@code null} if decoding fails for any reason,
     * this method is responsible for logging the issue.
     *
     * @param  rawPacket
     *         The Opus packet to decode
     *
     * @throws IllegalStateException If the decoder is closed
     *
     * @return The decoded audio samples, interleaved for 2 audio channels, or {@code null}
     *
     * @see net.dv8tion.jda.api.audio.OpusPacket#OPUS_FRAME_SIZE OpusPacket.OPUS_FRAME_SIZE
     */
    @Nullable
    short[] decode(@Nonnull AudioPacket rawPacket);
}
