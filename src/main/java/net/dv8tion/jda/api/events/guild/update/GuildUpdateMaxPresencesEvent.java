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

import javax.annotation.Nonnull;

/**
 * Indicates that the {@link net.dv8tion.jda.api.entities.Guild#getMaxPresences() maximum presences limit} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when the maximum presences limit changes and retrieve the old one
 *
 * <p>Identifier: {@code max_presences}
 */
public class GuildUpdateMaxPresencesEvent extends GenericGuildUpdateEvent<Integer>
{
    public static final String IDENTIFIER = "max_presences";

    public GuildUpdateMaxPresencesEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, int previous)
    {
        super(api, responseNumber, guild, previous, guild.getMaxPresences(), IDENTIFIER);
    }

    /**
     * The old max presences
     *
     * @return The old max presences
     */
    public int getOldMaxPresences()
    {
        return getOldValue();
    }

    /**
     * The new max presences
     *
     * @return The new max presences
     */
    public int getNewMaxPresences()
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
