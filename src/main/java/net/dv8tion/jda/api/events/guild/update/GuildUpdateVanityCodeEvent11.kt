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
 * Indicates that the [vanity url][net.dv8tion.jda.api.entities.Guild.getVanityUrl] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when the vanity url changes and retrieve the old one
 *
 *
 * Identifier: `vanity_code`
 */
class GuildUpdateVanityCodeEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild, previous: String?) :
    GenericGuildUpdateEvent<String?>(api, responseNumber, guild, previous, guild.vanityCode, IDENTIFIER) {
    val oldVanityCode: String?
        /**
         * The old vanity code
         *
         * @return The old vanity code
         */
        get() = oldValue
    val oldVanityUrl: String?
        /**
         * The old vanity url
         *
         * @return The old vanity url
         */
        get() = if (oldVanityCode == null) null else "https://discord.gg/" + oldVanityCode
    val newVanityCode: String?
        /**
         * The new vanity code
         *
         * @return The new vanity code
         */
        get() = newValue
    val newVanityUrl: String?
        /**
         * The new vanity url
         *
         * @return The new vanity url
         */
        get() = if (newVanityCode == null) null else "https://discord.gg/" + newVanityCode

    companion object {
        const val IDENTIFIER = "vanity_code"
    }
}
