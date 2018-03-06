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
package net.dv8tion.jda.core.events.channel.voice.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.VoiceChannel;

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

    private final int oldBitrate;
    private final int newBitrate;

    public VoiceChannelUpdateBitrateEvent(JDA api, long responseNumber, VoiceChannel channel, int oldBitrate)
    {
        super(api, responseNumber, channel);
        this.oldBitrate = oldBitrate;
        this.newBitrate = channel.getBitrate();
    }

    /**
     * The old bitrate
     *
     * @return The old bitrate
     */
    public int getOldBitrate()
    {
        return oldBitrate;
    }

    /**
     * The new bitrate
     *
     * @return The new bitrate
     */
    public int getNewBitrate()
    {
        return newBitrate;
    }

    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public Integer getOldValue()
    {
        return oldBitrate;
    }

    @Override
    public Integer getNewValue()
    {
        return newBitrate;
    }
}
