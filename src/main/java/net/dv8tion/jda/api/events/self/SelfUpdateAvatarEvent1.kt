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
package net.dv8tion.jda.api.events.self

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.ImageProxy
import javax.annotation.Nonnull

/**
 * Indicates that the avatar of the current user changed.
 *
 *
 * Can be used to retrieve the old avatar.
 *
 *
 * Identifier: `avatar`
 */
class SelfUpdateAvatarEvent(@Nonnull api: JDA, responseNumber: Long, oldAvatarId: String?) :
    GenericSelfUpdateEvent<String?>(api, responseNumber, oldAvatarId, api.getSelfUser().getAvatarId(), IDENTIFIER) {
    val oldAvatarId: String?
        /**
         * The old avatar id
         *
         * @return The old avatar id
         */
        get() = oldValue
    val oldAvatarUrl: String?
        /**
         * The old avatar url
         *
         * @return  The old avatar url
         */
        get() = if (previous == null) null else String.format(
            AVATAR_URL,
            selfUser.id,
            previous,
            if (previous.startsWith("a_")) ".gif" else ".png"
        )
    val oldAvatar: ImageProxy?
        /**
         * Returns an [ImageProxy] for this bot's new avatar image.
         *
         *
         * **Note:** the old avatar may not always be downloadable as it might have been removed from Discord.
         *
         * @return Possibly-null [ImageProxy] of this bot's new avatar image
         *
         * @see .getOldAvatarUrl
         */
        get() {
            val oldAvatarUrl = oldAvatarUrl
            return oldAvatarUrl?.let { ImageProxy(it) }
        }
    val newAvatarId: String?
        /**
         * The new avatar id
         *
         * @return The new avatar id
         */
        get() = newValue
    val newAvatarUrl: String?
        /**
         * The new avatar url
         *
         * @return  The new avatar url
         */
        get() = if (next == null) null else String.format(
            AVATAR_URL,
            selfUser.id,
            next,
            if (next.startsWith("a_")) ".gif" else ".png"
        )
    val newAvatar: ImageProxy?
        /**
         * Returns an [ImageProxy] for this bot's new avatar image.
         *
         * @return Possibly-null [ImageProxy] of this bot's new avatar image
         *
         * @see .getNewAvatarUrl
         */
        get() {
            val newAvatarUrl = newAvatarUrl
            return newAvatarUrl?.let { ImageProxy(it) }
        }

    companion object {
        const val IDENTIFIER = "avatar"
        private const val AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s%s"
    }
}
