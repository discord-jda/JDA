/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.events.guild.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

/**
 * Indicates that the {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel VerificationLevel} of a {@link net.dv8tion.jda.core.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a VerificationLevel changes and retrieve the old one
 *
 * <p>Identifier: {@code verification_level}
 */
public class GuildUpdateVerificationLevelEvent extends GenericGuildUpdateEvent<Guild.VerificationLevel>
{
    public static final String IDENTIFIER = "verification_level";

    public GuildUpdateVerificationLevelEvent(JDA api, long responseNumber, Guild guild, Guild.VerificationLevel oldVerificationLevel)
    {
        super(api, responseNumber, guild, oldVerificationLevel, guild.getVerificationLevel(), IDENTIFIER);
    }

    /**
     * The old {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel VerificationLevel}
     *
     * @return The old VerificationLevel
     */
    public Guild.VerificationLevel getOldVerificationLevel()
    {
        return getOldValue();
    }

    /**
     * The new {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel VerificationLevel}
     *
     * @return The new VerificationLevel
     */
    public Guild.VerificationLevel getNewVerificationLevel()
    {
        return getNewValue();
    }
}
