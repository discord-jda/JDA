/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import javax.annotation.Nonnull;

/**
 * Indicates that the {@link net.dv8tion.jda.api.entities.Guild#getMaxMembers() maximum member limit} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when the maximum member limit changes and retrieve the old one
 *
 * <p>Identifier: {@code max_members}
 */
public class GuildUpdateMaxMembersEvent extends GenericGuildUpdateEvent<Integer>
{
    public static final String IDENTIFIER = "max_members";

    public GuildUpdateMaxMembersEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, int previous)
    {
        super(api, responseNumber, guild, previous, guild.getMaxMembers(), IDENTIFIER);
    }

    /**
     * The old max members for this guild
     *
     * @return The old max members for this guild
     */
    public int getOldMaxMembers()
    {
        return getOldValue();
    }

    /**
     * The new max members for this guild
     *
     * @return The new max members for this guild
     */
    public int getNewMaxMembers()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public Integer getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Integer getNewValue()
    {
        return super.getNewValue();
    }
}
