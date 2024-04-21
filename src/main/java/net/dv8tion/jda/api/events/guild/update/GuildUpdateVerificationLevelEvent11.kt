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

import Guild.VerificationLevel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that the [VerificationLevel][net.dv8tion.jda.api.entities.Guild.VerificationLevel] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when a VerificationLevel changes and retrieve the old one
 *
 *
 * Identifier: `verification_level`
 */
class GuildUpdateVerificationLevelEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild,
    @Nonnull oldVerificationLevel: VerificationLevel?
) : GenericGuildUpdateEvent<VerificationLevel?>(
    api,
    responseNumber,
    guild,
    oldVerificationLevel,
    guild.getVerificationLevel(),
    IDENTIFIER
) {
    @get:Nonnull
    val oldVerificationLevel: VerificationLevel?
        /**
         * The old [VerificationLevel][net.dv8tion.jda.api.entities.Guild.VerificationLevel]
         *
         * @return The old VerificationLevel
         */
        get() = oldValue

    @get:Nonnull
    val newVerificationLevel: VerificationLevel?
        /**
         * The new [VerificationLevel][net.dv8tion.jda.api.entities.Guild.VerificationLevel]
         *
         * @return The new VerificationLevel
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "verification_level"
    }
}
