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
 * Indicates that the {@link net.dv8tion.jda.api.entities.Guild.NSFWLevel NSFWLevel} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a NSFWLevel changes and retrieve the old one
 *
 * <p>Identifier: {@code nsfw_level}
 */
public class GuildUpdateNSFWLevelEvent extends GenericGuildUpdateEvent<Guild.NSFWLevel>
{
    public static final String IDENTIFIER = "nsfw_level";

    public GuildUpdateNSFWLevelEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull Guild.NSFWLevel oldNSFWLevel)
    {
        super(api, responseNumber, guild, oldNSFWLevel, guild.getNSFWLevel(), IDENTIFIER);
    }

    /**
     * The old {@link Guild.NSFWLevel NSFWLevel}
     *
     * @return The old NSFWLevel
     */
    @Nonnull
    public Guild.NSFWLevel getOldNSFWLevel()
    {
        return getOldValue();
    }

    /**
     * The new {@link Guild.NSFWLevel NSFWLevel}
     *
     * @return The new NSFWLevel
     */
    @Nonnull
    public Guild.NSFWLevel getNewNSFWLevel()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public Guild.NSFWLevel getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Guild.NSFWLevel getNewValue()
    {
        return super.getNewValue();
    }
}
