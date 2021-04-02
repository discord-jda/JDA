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
 * Indicates that the {@link net.dv8tion.jda.api.entities.Guild.MFALevel MFALevel} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a MFALevel changes and retrieve the old one
 *
 * <p>Identifier: {@code mfa_level}
 */
public class GuildUpdateMFALevelEvent extends GenericGuildUpdateEvent<Guild.MFALevel>
{
    public static final String IDENTIFIER = "mfa_level";

    public GuildUpdateMFALevelEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull Guild.MFALevel oldMFALevel)
    {
        super(api, responseNumber, guild, oldMFALevel, guild.getRequiredMFALevel(), IDENTIFIER);
    }

    /**
     * The old {@link net.dv8tion.jda.api.entities.Guild.MFALevel MFALevel}
     *
     * @return The old MFALevel
     */
    @Nonnull
    public Guild.MFALevel getOldMFALevel()
    {
        return getOldValue();
    }

    /**
     * The new {@link net.dv8tion.jda.api.entities.Guild.MFALevel MFALevel}
     *
     * @return The new MFALevel
     */
    @Nonnull
    public Guild.MFALevel getNewMFALevel()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public Guild.MFALevel getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Guild.MFALevel getNewValue()
    {
        return super.getNewValue();
    }
}
