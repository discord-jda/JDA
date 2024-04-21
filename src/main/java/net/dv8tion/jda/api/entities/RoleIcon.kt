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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.managers.RoleManager
import net.dv8tion.jda.api.utils.ImageProxy
import java.util.*

/**
 * An object representing a Role's icon.
 *
 * @see Role.getIcon
 */
class RoleIcon(
    private val iconId: String,
    /**
     * The Unicode Emoji of this [Role][net.dv8tion.jda.api.entities.Role] that is used instead of a custom image.
     * If no emoji has been set, this returns `null`.
     *
     * The Role emoji can be modified using [RoleManager.setIcon].
     *
     * @return Possibly-null String containing the Role's Unicode Emoji.
     *
     * @since  4.3.1
     */
    @JvmField val emoji: String?, private val roleId: Long
) {
    /**
     * The Discord hash-id of the [Role][net.dv8tion.jda.api.entities.Role] icon image.
     * If no icon has been set or an emoji is used in its place, this returns `null`.
     *
     * The Role icon can be modified using [RoleManager.setIcon].
     *
     * @return Possibly-null String containing the Role's icon hash-id.
     *
     * @since  4.3.1
     */
    fun getIconId(): String? {
        return iconId
    }

    val iconUrl: String?
        /**
         * The URL of the [Role][net.dv8tion.jda.api.entities.Role] icon image.
         * If no icon has been set or an emoji is used in its place, this returns `null`.
         *
         * The Role icon can be modified using [RoleManager.setIcon].
         *
         * @return Possibly-null String containing the Role's icon URL.
         *
         * @since  4.3.1
         */
        get() {
            val iconId = getIconId()
            return if (iconId == null) null else String.format(ICON_URL, roleId, iconId)
        }
    val icon: ImageProxy?
        /**
         * Returns an [ImageProxy] for this role's icon.
         *
         * @return Possibly-null [ImageProxy] of this role's icon
         *
         * @see .getIconUrl
         */
        get() {
            val iconUrl = iconUrl
            return iconUrl?.let { ImageProxy(it) }
        }

    /**
     * Whether this [RoleIcon] is an emoji instead of a custom image.
     *
     * @return True, if this [RoleIcon] is an emoji
     */
    fun isEmoji(): Boolean {
        return emoji != null
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is RoleIcon) return false
        val icon = obj
        return icon.iconId == iconId && icon.emoji == emoji
    }

    override fun hashCode(): Int {
        return Objects.hash(iconId, emoji)
    }

    companion object {
        /** Template for [.getIconUrl].  */
        const val ICON_URL = "https://cdn.discordapp.com/role-icons/%s/%s.png"
    }
}
