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

import Guild.MFALevel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that the [MFALevel][net.dv8tion.jda.api.entities.Guild.MFALevel] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when a MFALevel changes and retrieve the old one
 *
 *
 * Identifier: `mfa_level`
 */
class GuildUpdateMFALevelEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild,
    @Nonnull oldMFALevel: MFALevel?
) : GenericGuildUpdateEvent<MFALevel?>(
    api,
    responseNumber,
    guild,
    oldMFALevel,
    guild.getRequiredMFALevel(),
    IDENTIFIER
) {
    @get:Nonnull
    val oldMFALevel: MFALevel?
        /**
         * The old [MFALevel][net.dv8tion.jda.api.entities.Guild.MFALevel]
         *
         * @return The old MFALevel
         */
        get() = oldValue

    @get:Nonnull
    val newMFALevel: MFALevel?
        /**
         * The new [MFALevel][net.dv8tion.jda.api.entities.Guild.MFALevel]
         *
         * @return The new MFALevel
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "mfa_level"
    }
}
