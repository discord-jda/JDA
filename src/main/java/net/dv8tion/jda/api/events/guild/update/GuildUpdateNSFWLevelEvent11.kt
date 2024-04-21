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

import Guild.NSFWLevel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that the [NSFWLevel][net.dv8tion.jda.api.entities.Guild.NSFWLevel] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when a NSFWLevel changes and retrieve the old one
 *
 *
 * Identifier: `nsfw_level`
 */
class GuildUpdateNSFWLevelEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull guild: Guild,
    @Nonnull oldNSFWLevel: NSFWLevel?
) : GenericGuildUpdateEvent<NSFWLevel?>(api, responseNumber, guild, oldNSFWLevel, guild.nSFWLevel, IDENTIFIER) {
    @get:Nonnull
    val oldNSFWLevel: NSFWLevel?
        /**
         * The old [NSFWLevel][Guild.NSFWLevel]
         *
         * @return The old NSFWLevel
         */
        get() = oldValue

    @get:Nonnull
    val newNSFWLevel: NSFWLevel?
        /**
         * The new [NSFWLevel][Guild.NSFWLevel]
         *
         * @return The new NSFWLevel
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "nsfw_level"
    }
}
