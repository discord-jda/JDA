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
 * Indicates that the splash of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when a guild splash changes and retrieve the old one
 *
 *
 * Identifier: `splash`
 */
class GuildUpdateSplashEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild, oldSplashId: String?) :
    GenericGuildUpdateEvent<String?>(api, responseNumber, guild, oldSplashId, guild.splashId, IDENTIFIER) {
    val oldSplashId: String?
        /**
         * The old splash id
         *
         * @return The old splash id, or null
         */
        get() = oldValue
    val oldSplashUrl: String?
        /**
         * The url of the old splash
         *
         * @return The url of the old splash, or null
         */
        get() = if (previous == null) null else java.lang.String.format(Guild.SPLASH_URL, guild!!.id, previous)
    val oldSplash: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's old splash image.
         *
         *
         * **Note:** the old splash may not always be downloadable as it might have been removed from Discord.
         *
         * @return Possibly-null [ImageProxy] of this guild's old splash image
         *
         * @see .getOldSplashUrl
         */
        get() {
            val oldSplashUrl = oldSplashUrl
            return oldSplashUrl?.let { ImageProxy(it) }
        }
    val newSplashId: String?
        /**
         * The new splash id
         *
         * @return The new splash id, or null
         */
        get() = newValue
    val newSplashUrl: String?
        /**
         * The url of the new splash
         *
         * @return The url of the new splash, or null
         */
        get() = if (next == null) null else java.lang.String.format(Guild.SPLASH_URL, guild!!.id, next)
    val newSplash: ImageProxy?
        /**
         * Returns an [ImageProxy] for this guild's new splash image.
         *
         * @return Possibly-null [ImageProxy] of this guild's new splash image
         *
         * @see .getNewSplashUrl
         */
        get() {
            val newSplashUrl = newSplashUrl
            return newSplashUrl?.let { ImageProxy(it) }
        }

    companion object {
        const val IDENTIFIER = "splash"
    }
}
