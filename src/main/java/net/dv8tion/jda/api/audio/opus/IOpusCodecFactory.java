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

import javax.annotation.Nonnull;

/**
 * Factory of {@link IOpusEncoder} and {@link IOpusDecoder}.
 *
 * <p>This is an optional interface, you must provide an implementation of it when using {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandlers}
 * and {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandlers} that only support raw audio.
 *
 * <p>Third-party implementations include <a href="https://github.com/freya022/opus-jda" target="_blank">opus-jda</a>,
 * you are free to use anything else, or, to make your own.
 *
 * @see net.dv8tion.jda.api.audio.AudioModuleConfig#withOpusCodecFactory(IOpusCodecFactory) AudioModuleConfig.withOpusCodecFactory(IOpusCodecFactory)
 */
public interface IOpusCodecFactory {
    /**
     * Creates an Opus encoder for {@value net.dv8tion.jda.api.audio.OpusPacket#OPUS_SAMPLE_RATE 48} Hz audio
     * with {@value net.dv8tion.jda.api.audio.OpusPacket#OPUS_CHANNEL_COUNT} channels.
     *
     * <p>This is only called when an {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandler}
     * is set and provides <b>raw</b> audio.
     *
     * @return The new encoder
     *
     * @see net.dv8tion.jda.api.audio.OpusPacket#OPUS_SAMPLE_RATE OpusPacket.OPUS_SAMPLE_RATE
     * @see net.dv8tion.jda.api.audio.OpusPacket#OPUS_CHANNEL_COUNT OpusPacket.OPUS_CHANNEL_COUNT
     */
    @Nonnull
    IOpusEncoder createEncoder();

    /**
     * Creates an Opus decoder for {@value net.dv8tion.jda.api.audio.OpusPacket#OPUS_SAMPLE_RATE 48} Hz audio
     * with {@value net.dv8tion.jda.api.audio.OpusPacket#OPUS_CHANNEL_COUNT} channels.
     *
     * <p>A decoder is always created for each user.
     *
     * @return The new decoder
     *
     * @see net.dv8tion.jda.api.audio.OpusPacket#OPUS_SAMPLE_RATE OpusPacket.OPUS_SAMPLE_RATE
     * @see net.dv8tion.jda.api.audio.OpusPacket#OPUS_CHANNEL_COUNT OpusPacket.OPUS_CHANNEL_COUNT
     */
    @Nonnull
    IOpusDecoder createDecoder();
}
