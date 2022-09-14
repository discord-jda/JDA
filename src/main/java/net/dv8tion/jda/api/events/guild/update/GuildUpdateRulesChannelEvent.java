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
 * Indicates that the rules channel of a {@link Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild rule channel changes and retrieve the old one
 *
 * <p>Identifier: {@code rules_channel}
 */
public class GuildUpdateRulesChannelEvent extends GenericGuildUpdateEvent<TextChannel>
{
    public static final String IDENTIFIER = "rules_channel";

    public GuildUpdateRulesChannelEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable TextChannel oldRulesChannel)
    {
        super(api, responseNumber, guild, oldRulesChannel, guild.getRulesChannel(), IDENTIFIER);
    }

    /**
     * The previous rules channel.
     * 
     * @return The previous rules channel
     */
    @Nullable
    public TextChannel getOldRulesChannel()
    {
        return getOldValue();
    }

    /**
     * The new rules channel.
     *
     * @return The new rules channel
     */
    @Nullable
    public TextChannel getNewRulesChannel()
    {
        return getNewValue();
    }
}
