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

import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Meta-data for the team of an application.
 *
 * @see ApplicationInfo.getTeam
 */
interface ApplicationTeam : ISnowflake {
    val owner: TeamMember?
        /**
         * Searches for the [TeamMember][net.dv8tion.jda.api.entities.TeamMember]
         * in [.getMembers] that has the same user id as [.getOwnerIdLong].
         * <br></br>Its possible although unlikely that the owner of the team is not a member, in that case this will be null.
         *
         * @return Possibly-null [TeamMember][net.dv8tion.jda.api.entities.TeamMember] who owns the team
         */
        get() = getMemberById(ownerIdLong)

    @get:Nonnull
    val ownerId: String?
        /**
         * The id for the user who owns this team.
         *
         * @return The owner id
         */
        get() = java.lang.Long.toUnsignedString(ownerIdLong)

    /**
     * The id for the user who owns this team.
     *
     * @return The owner id
     */
    val ownerIdLong: Long

    /**
     * The id hash for the icon of this team.
     *
     * @return The icon id, or null if no icon is applied
     *
     * @see .getIconUrl
     */
    val iconId: String?
    val iconUrl: String?
        /**
         * The url for the icon of this team.
         *
         * @return The icon url, or null if no icon is applied
         */
        get() {
            val iconId = iconId
            return if (iconId == null) null else String.format(ICON_URL, id, iconId)
        }
    val icon: ImageProxy?
        /**
         * Returns an [ImageProxy] for this application team's icon.
         *
         * @return The [ImageProxy] of this application team's icon, or null if no icon is applied
         *
         * @see .getIconUrl
         */
        get() {
            val iconUrl = iconUrl
            return iconUrl?.let { ImageProxy(it) }
        }

    @get:Nonnull
    val members: List<TeamMember>

    /**
     * Check whether [.getMember] returns null for the provided user.
     *
     * @param  user
     * The user to check
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with null
     *
     * @return True, if the provided user is a member of this team
     */
    fun isMember(@Nonnull user: User): Boolean {
        return getMember(user) != null
    }

    /**
     * Retrieves the [TeamMember][net.dv8tion.jda.api.entities.TeamMember] instance
     * for the provided user. If the user is not a member of this team, null is returned.
     *
     * @param  user
     * The user for the team member
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with null
     *
     * @return The [TeamMember][net.dv8tion.jda.api.entities.TeamMember] for the user or null
     */
    fun getMember(@Nonnull user: User): TeamMember? {
        Checks.notNull(user, "User")
        return getMemberById(user.getIdLong())
    }

    /**
     * Retrieves the [TeamMember][net.dv8tion.jda.api.entities.TeamMember] instance
     * for the provided user id. If the user is not a member of this team, null is returned.
     *
     * @param  userId
     * The user id for the team member
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with null
     *
     * @return The [TeamMember][net.dv8tion.jda.api.entities.TeamMember] for the user or null
     */
    fun getMemberById(@Nonnull userId: String?): TeamMember? {
        return getMemberById(MiscUtil.parseSnowflake(userId))
    }

    /**
     * Retrieves the [TeamMember][net.dv8tion.jda.api.entities.TeamMember] instance
     * for the provided user id. If the user is not a member of this team, null is returned.
     *
     * @param  userId
     * The user id for the team member
     *
     * @return The [TeamMember][net.dv8tion.jda.api.entities.TeamMember] for the user or null
     */
    fun getMemberById(userId: Long): TeamMember? {
        for (member in members) {
            if (member.getUser().getIdLong() == userId) return member
        }
        return null
    }

    companion object {
        /** Template for [.getIconUrl]  */
        const val ICON_URL = "https://cdn.discordapp.com/team-icons/%s/%s.png"
    }
}
