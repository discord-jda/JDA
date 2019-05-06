/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
 * Indicates that the {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a VerificationLevel changes and retrieve the old one
 *
 * <p>Identifier: {@code verification_level}
 */
public class GuildUpdateVerificationLevelEvent extends GenericGuildUpdateEvent<Guild.VerificationLevel>
{
    public static final String IDENTIFIER = "verification_level";

    public GuildUpdateVerificationLevelEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull Guild.VerificationLevel oldVerificationLevel)
    {
        super(api, responseNumber, guild, oldVerificationLevel, guild.getVerificationLevel(), IDENTIFIER);
    }

    /**
     * The old {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel}
     *
     * @return The old VerificationLevel
     */
    @Nonnull
    public Guild.VerificationLevel getOldVerificationLevel()
    {
        return getOldValue();
    }

    /**
     * The new {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel}
     *
     * @return The new VerificationLevel
     */
    @Nonnull
    public Guild.VerificationLevel getNewVerificationLevel()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public Guild.VerificationLevel getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Guild.VerificationLevel getNewValue()
    {
        return super.getNewValue();
    }
}
