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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that the [description][net.dv8tion.jda.api.entities.Guild.getDescription] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when the description changes and retrieve the old one
 *
 *
 * Identifier: `description`
 */
class GuildUpdateDescriptionEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild, previous: String?) :
    GenericGuildUpdateEvent<String?>(api, responseNumber, guild, previous, guild.description, IDENTIFIER) {
    val oldDescription: String?
        /**
         * The old description for this guild
         *
         * @return The old description for this guild, or null if none was set
         */
        get() = oldValue
    val newDescription: String?
        /**
         * The new description for this guild
         *
         * @return The new description, or null if it was removed
         */
        get() = newValue

    companion object {
        const val IDENTIFIER = "description"
    }
}
