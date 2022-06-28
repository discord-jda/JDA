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
 * Indicates that the {@link net.dv8tion.jda.api.entities.Guild#getBoostTier() boost tier} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when the boost tier changes and retrieve the old one
 *
 * <p>Identifier: {@code boost_tier}
 */
public class GuildUpdateBoostTierEvent extends GenericGuildUpdateEvent<Guild.BoostTier>
{
    public static final String IDENTIFIER = "boost_tier";

    public GuildUpdateBoostTierEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull Guild.BoostTier previous)
    {
        super(api, responseNumber, guild, previous, guild.getBoostTier(), IDENTIFIER);
    }

    /**
     * The old {@link net.dv8tion.jda.api.entities.Guild.BoostTier}
     *
     * @return The old BoostTier
     */
    @Nonnull
    public Guild.BoostTier getOldBoostTier()
    {
        return getOldValue();
    }

    /**
     * The new {@link net.dv8tion.jda.api.entities.Guild.BoostTier}
     *
     * @return The new BoostTier
     */
    @Nonnull
    public Guild.BoostTier getNewBoostTier()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public Guild.BoostTier getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Guild.BoostTier getNewValue()
    {
        return super.getNewValue();
    }
}
