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

package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the community updates channel of a {@link Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild community updates channel changes and retrieve the old one
 *
 * <p>Identifier: {@code community_updates_channel}
 */
public class GuildUpdateCommunityUpdatesChannelEvent extends GenericGuildUpdateEvent<TextChannel>
{
    public static final String IDENTIFIER = "community_updates_channel";

    public GuildUpdateCommunityUpdatesChannelEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable TextChannel oldCommunityUpdatesChannel)
    {
        super(api, responseNumber, guild, oldCommunityUpdatesChannel, guild.getCommunityUpdatesChannel(), IDENTIFIER);
    }

    /**
     * The previous community updates channel.
     * 
     * @return The previous community updates channel
     */
    @Nullable
    public TextChannel getOldCommunityUpdatesChannel()
    {
        return getOldValue();
    }

    /**
     * The new community updates channel.
     *
     * @return The new community updates channel
     */
    @Nullable
    public TextChannel getNewCommunityUpdatesChannel()
    {
        return getNewValue();
    }
}
