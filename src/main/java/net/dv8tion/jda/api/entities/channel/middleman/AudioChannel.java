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

package net.dv8tion.jda.api.entities.channel.middleman;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.middleman.AudioChannelManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Guild Channel that is capable of handling audio.
 * <br>This is a {@link StandardGuildChannel} that contains additional methods present for audio channels
 *
 * @see VoiceChannel
 * @see StageChannel
 *
 * @see Guild#getVoiceChannelCache()
 * @see Guild#getVoiceChannels()
 * @see Guild#getVoiceChannelsByName(String, boolean)
 * @see Guild#getVoiceChannelById(long)
 *
 * @see Guild#getStageChannelCache()
 * @see Guild#getStageChannels()
 * @see Guild#getStageChannelsByName(String, boolean)
 * @see Guild#getStageChannelById(long)
 *
 * @see JDA#getVoiceChannelById(long)
 * @see JDA#getStageChannelById(long)
 */
public interface AudioChannel extends StandardGuildChannel
{
    @Override
    @Nonnull
    AudioChannelManager<?, ?> getManager();

    /**
     * The audio bitrate of the voice audio that is transmitted in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     * <br>Default and recommended value is 64000
     *
     * @return The audio bitrate of this audio channel.
     */
    int getBitrate();

    /**
     * The maximum amount of {@link net.dv8tion.jda.api.entities.Member Members} that be in an audio connection within this channel concurrently.
     * <br>Returns 0 if there is no limit.
     *
     * <p>Moderators with the {@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS VOICE_MOVE_OTHERS} permission can bypass this limit.
     *
     * @return The maximum connections allowed in this channel concurrently
     */
    int getUserLimit();

    /**
     * The {@link Region} of this channel.
     * <br>This will return {@link Region#AUTOMATIC} if the region of this channel is set to Automatic.
     *
     * @return the {@link Region} of this channel.
     */
    @Nonnull
    default Region getRegion()
    {
        return getRegionRaw() == null ? Region.AUTOMATIC : Region.fromKey(getRegionRaw());
    }

    /**
     * The raw region name for this channel.
     * <br>This will return null if the region is set to Automatic.
     *
     * @return Raw region name, or {@code null} if the region is set to automatic.
     */
    @Nullable
    String getRegionRaw();
}
