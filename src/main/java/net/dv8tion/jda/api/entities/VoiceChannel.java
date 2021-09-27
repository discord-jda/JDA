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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Discord Voice GuildChannel.
 * <br>Adds additional information specific to voice channels in Discord.
 *
 * @see GuildChannel
 * @see TextChannel
 * @see Category
 *
 * @see   Guild#getVoiceChannelCache()
 * @see   Guild#getVoiceChannels()
 * @see   Guild#getVoiceChannelsByName(String, boolean)
 * @see   Guild#getVoiceChannelById(long)
 *
 * @see   JDA#getVoiceChannelCache()
 * @see   JDA#getVoiceChannels()
 * @see   JDA#getVoiceChannelsByName(String, boolean)
 * @see   JDA#getVoiceChannelById(long)
 */
public interface VoiceChannel extends GuildChannel
{
    /**
     * The maximum amount of {@link net.dv8tion.jda.api.entities.Member Members} that can be in this
     * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} at once.
     * <br>0 - No limit
     *
     * <p>This is meaningless for {@link StageChannel StageChannels}.
     *
     * @return The maximum amount of members allowed in this channel at once.
     */
    int getUserLimit();

    /**
     * The audio bitrate of the voice audio that is transmitted in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     * <br>Default and recommended value is 64000
     *
     * @return The audio bitrate of this voice channel.
     */
    int getBitrate();

    /**
     * The {@link net.dv8tion.jda.api.Region Region} of this {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     * <br>This will return {@link Region#AUTOMATIC} if the region of this channel is set to Automatic.
     *
     * @return the {@link net.dv8tion.jda.api.Region Region} of this channel.
     */
    @Nonnull
    Region getRegion();

    /**
     * The raw region name for this {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     * This will return null if the region is set to Automatic.
     *
     * @return Raw region name
     */
    @Nullable
    String getRegionRaw();

    @Nonnull
    @Override
    ChannelAction<VoiceChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    ChannelAction<VoiceChannel> createCopy();
}
