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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.attribute.IPositionableChannelManager;

import javax.annotation.Nonnull;

/**
 * Represents a {@link GuildChannel GuildChannel} that has a position.
 *
 * <p>These channels can be re-ordered using a position value.
 *
 * <p>In the case of identical position values, the natural order of the channel snowflakes is used.
 */
public interface IPositionableChannel extends GuildChannel
{
    @Override
    @Nonnull
    IPositionableChannelManager<?, ?> getManager();

    /**
     * The position of this channel in the channel list of the guild.
     * <br>This does not account for thread channels, as they do not have positions.
     *
     * <p>This is functionally equivalent to {@code getGuild().getChannels().indexOf(channel)}.
     * To efficiently compare the position between channels, it is recommended to use {@link #compareTo(Object)} instead of the position.
     *
     * @throws IllegalStateException
     *         If this channel is not in the guild cache
     *
     * @return Zero-based int of position of the GuildChannel.
     */
    default int getPosition()
    {
        int position = getGuild().getChannels().indexOf(this);
        if (position > -1)
            return position;
        throw new IllegalStateException("Somehow when determining position we never found the " + getType().name() + " in the Guild's channels? wtf?");
    }

    /**
     * The actual position of the {@link GuildChannel GuildChannel} as stored and given by Discord.
     *
     * <p>Channel positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more channels share the same position then they are sorted based on their creation date.
     * The more recent a channel was created, the lower it is in the hierarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * channel then this method will give you that value.
     *
     * @return The true, Discord stored, position of the {@link GuildChannel GuildChannel}.
     */
    int getPositionRaw();
}
