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
 * Indicates that the [banner][net.dv8tion.jda.api.entities.Guild.getBannerId] of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when the banner changes and retrieve the old one
 *
 *
 * Identifier: `banner`
 */
class GuildUpdateBannerEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild, previous: String?) :
    GenericGuildUpdateEvent<String?>(api, responseNumber, guild, previous, guild.bannerId, IDENTIFIER) {
    val newBannerId: String?
        /**
         * The new banner id
         *
         * @return The new banner id, or null if the banner was removed
         */
        get() = newValue
    val newBannerUrl: String?
        /**
         * The new banner url
         *
         * @return The new banner url, or null if the banner was removed
         */
        get() = if (next == null) null else java.lang.String.format(
            Guild.BANNER_URL,
            guild!!.id,
            next,
            if (next.startsWith("a_")) "gif" else "png"
        )
    val newBanner: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's new banner.
         *
         * @return Possibly-null [ImageProxy] of this guild's new banner
         *
         * @see .getNewBannerUrl
         */
        get() {
            val newBannerUrl = newBannerUrl
            return newBannerUrl?.let { ImageProxy(it) }
        }
    val oldBannerId: String?
        /**
         * The old banner id
         *
         * @return The old banner id, or null if the banner didn't exist
         */
        get() = oldValue
    val oldBannerUrl: String?
        /**
         * The old banner url
         *
         * @return The old banner url, or null if the banner didn't exist
         */
        get() = if (previous == null) null else java.lang.String.format(
            Guild.BANNER_URL,
            guild!!.id,
            previous,
            if (previous.startsWith("a_")) "gif" else "png"
        )
    val oldBanner: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's old banner.
         *
         *
         * **Note:** the old banner may not always be downloadable as it might have been removed from Discord.
         *
         * @return Possibly-null [ImageProxy] of this guild's old banner
         *
         * @see .getOldBannerUrl
         */
        get() {
            val oldBannerUrl = oldBannerUrl
            return oldBannerUrl?.let { ImageProxy(it) }
        }

    companion object {
        const val IDENTIFIER = "banner"
    }
}
