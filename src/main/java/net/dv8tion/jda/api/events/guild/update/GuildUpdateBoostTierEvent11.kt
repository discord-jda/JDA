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
package net.dv8tion.jda.api.events.guild.update

import Guild.BoostTier
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that the [boost tier][net.dv8tion.jda.api.entities.Guild.getBoostTier] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when the boost tier changes and retrieve the old one
 *
 *
 * Identifier: `boost_tier`
 */
class GuildUpdateBoostTierEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild,
    @Nonnull previous: BoostTier?
) : GenericGuildUpdateEvent<BoostTier?>(api, responseNumber, guild, previous, guild.boostTier, IDENTIFIER) {
    @get:Nonnull
    val oldBoostTier: BoostTier?
        /**
         * The old [net.dv8tion.jda.api.entities.Guild.BoostTier]
         *
         * @return The old BoostTier
         */
        get() = oldValue

    @get:Nonnull
    val newBoostTier: BoostTier?
        /**
         * The new [net.dv8tion.jda.api.entities.Guild.BoostTier]
         *
         * @return The new BoostTier
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "boost_tier"
    }
}
