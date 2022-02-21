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

import net.dv8tion.jda.api.managers.channel.attribute.IPositionableChannelManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a {@link GuildChannel GuildChannel} that has a position.
 *
 * These channels can be re-ordered using a position value.
 *
 * In the case of identical position values, the natural order of the channel snowflakes is used.
 */
public interface IPositionableChannel extends GuildChannel
{
    //TODO-v5: Docs
    @Override
    @Nonnull
    IPositionableChannelManager<?, ?> getManager();

    //TODO-v5: We should probably reconsider how getPosition is calculated as it isn't particularly useful anymore...
    //TODO-v5: We should probably introduce getPositionAbsolute that returns the index of the channel in Guild#getChannels
    //TODO-v5: We should probably introduce getPositionInCategory (name pending) that returns index in Category#getChannels or -1
    /**
     * The position this GuildChannel is displayed at.
     * <br>Higher values mean they are displayed lower in the Client. Position 0 is the top most GuildChannel
     * Channels of a {@link net.dv8tion.jda.api.entities.Guild Guild} do not have to have continuous positions
     *
     * @throws IllegalStateException
     *         If this channel is not in the guild cache
     *
     * @return Zero-based int of position of the GuildChannel.
     */
    default int getPosition()
    {
        int sortBucket = getType().getSortBucket();
        List<GuildChannel> channels = getGuild().getChannels().stream()
            .filter(chan -> chan.getType().getSortBucket() == sortBucket)
            .sorted()
            .collect(Collectors.toList());

        for (int i = 0; i < channels.size(); i++)
        {
            if (equals(channels.get(i)))
                return i;
        }
        throw new IllegalStateException("Somehow when determining position we never found the " + getType().name() + " in the Guild's channels? wtf?");
    }

    /**
     * The actual position of the {@link GuildChannel GuildChannel} as stored and given by Discord.
     * Channel positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more channels share the same position then they are sorted based on their creation date.
     * The more recent a channel was created, the lower it is in the hierarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * channel then this method will give you that value.
     *
     * @return The true, Discord stored, position of the {@link GuildChannel GuildChannel}.
     */
    int getPositionRaw();
}
