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

import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.managers.channel.middleman.AudioChannelManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//TODO-v5: Docs
public interface AudioChannel extends GuildChannel, IMemberContainer
{
    //TODO-v5: Docs
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
     * The {@link net.dv8tion.jda.api.Region Region} of this {@link AudioChannel AudioChannel}.
     * <br>This will return {@link Region#AUTOMATIC} if the region of this channel is set to Automatic.
     *
     * @return the {@link net.dv8tion.jda.api.Region Region} of this channel.
     */
    @Nonnull
    default Region getRegion()
    {
        return getRegionRaw() == null ? Region.AUTOMATIC : Region.fromKey(getRegionRaw());
    }

    /**
     * The raw region name for this {@link AudioChannel AudioChannel}.
     * This will return null if the region is set to Automatic.
     *
     * @return Raw region name
     */
    @Nullable
    String getRegionRaw();
}
