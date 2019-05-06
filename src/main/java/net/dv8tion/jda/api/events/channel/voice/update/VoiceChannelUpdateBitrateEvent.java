/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.events.channel.voice.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link VoiceChannel VoiceChannel}'s bitrate changed.
 *
 * <p>Can be sued to get affected VoiceChannel, affected Guild and previous bitrate.
 *
 * <p>Identifier: {@code bitrate}
 */
public class VoiceChannelUpdateBitrateEvent extends GenericVoiceChannelUpdateEvent<Integer>
{
    public static final String IDENTIFIER = "bitrate";

    public VoiceChannelUpdateBitrateEvent(@Nonnull JDA api, long responseNumber, @Nonnull VoiceChannel channel, int oldBitrate)
    {
        super(api, responseNumber, channel, oldBitrate, channel.getBitrate(), IDENTIFIER);
    }

    /**
     * The old bitrate
     *
     * @return The old bitrate
     */
    public int getOldBitrate()
    {
        return getOldValue();
    }

    /**
     * The new bitrate
     *
     * @return The new bitrate
     */
    public int getNewBitrate()
    {
        return getNewValue();
    }
}
