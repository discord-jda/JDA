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
import net.dv8tion.jda.api.utils.ImageProxy
import javax.annotation.Nonnull

/**
 * Indicates that the Icon of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when a guild icon changes and retrieve the old one
 *
 *
 * Identifier: `icon`
 */
class GuildUpdateIconEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild, oldIconId: String?) :
    GenericGuildUpdateEvent<String?>(api, responseNumber, guild, oldIconId, guild.iconId, IDENTIFIER) {
    val oldIconId: String?
        /**
         * The old icon id
         *
         * @return The old icon id, or null
         */
        get() = oldValue
    val oldIconUrl: String?
        /**
         * The url of the old icon
         *
         * @return The url of the old icon, or null
         */
        get() = if (previous == null) null else java.lang.String.format(
            Guild.ICON_URL,
            guild!!.id,
            previous,
            if (previous.startsWith("a_")) "gif" else "png"
        )
    val oldIcon: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's old icon.
         *
         *
         * **Note:** the old icon may not always be downloadable as it might have been removed from Discord.
         *
         * @return Possibly-null [ImageProxy] of this guild's old icon
         *
         * @see .getOldIconUrl
         */
        get() {
            val oldIconUrl = oldIconUrl
            return oldIconUrl?.let { ImageProxy(it) }
        }
    val newIconId: String?
        /**
         * The old icon id
         *
         * @return The old icon id, or null
         */
        get() = newValue
    val newIconUrl: String?
        /**
         * The url of the new icon
         *
         * @return The url of the new icon, or null
         */
        get() = if (next == null) null else java.lang.String.format(
            Guild.ICON_URL,
            guild!!.id,
            next,
            if (next.startsWith("a_")) "gif" else "png"
        )
    val newIcon: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's new icon.
         *
         * @return Possibly-null [ImageProxy] of this guild's new icon
         *
         * @see .getNewIconUrl
         */
        get() {
            val newIconUrl = newIconUrl
            return newIconUrl?.let { ImageProxy(it) }
        }

    companion object {
        const val IDENTIFIER = "icon"
    }
}
