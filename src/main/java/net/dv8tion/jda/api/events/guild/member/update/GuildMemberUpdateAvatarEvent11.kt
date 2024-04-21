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
package net.dv8tion.jda.api.events.guild.member.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.ImageProxy
import javax.annotation.Nonnull

/**
 * Indicates that a [Member][net.dv8tion.jda.api.entities.Member] updated their [Guild][net.dv8tion.jda.api.entities.Guild] avatar.
 *
 *
 * Can be used to retrieve members who change their per guild avatar, the triggering guild, the old avatar id and the new avatar id.
 *
 *
 * Identifier: `avatar`
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [GUILD_MEMBERS][net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS] intent to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * Additionally, this event requires the [MemberCachePolicy][net.dv8tion.jda.api.utils.MemberCachePolicy]
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
class GuildMemberUpdateAvatarEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull member: Member,
    oldAvatarId: String?
) : GenericGuildMemberUpdateEvent<String?>(api, responseNumber, member, oldAvatarId, member.avatarId, IDENTIFIER) {
    val oldAvatarId: String?
        /**
         * The old avatar id
         *
         * @return The old avatar id
         */
        get() = oldValue
    val oldAvatarUrl: String?
        /**
         * The previous avatar url
         *
         * @return The previous avatar url
         */
        get() = if (previous == null) null else String.format(
            Member.AVATAR_URL,
            member.guild.id,
            member.id,
            previous,
            if (previous.startsWith("a_")) "gif" else "png"
        )
    val oldAvatar: ImageProxy?
        /**
         * Returns an [ImageProxy] for this member's old avatar.
         *
         *
         * **Note:** the old avatar may not always be downloadable as it might have been removed from Discord.
         *
         * @return Possibly-null [ImageProxy] of this member's old avatar
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
         * The url of the new avatar
         *
         * @return The url of the new avatar
         */
        get() = if (next == null) null else String.format(
            Member.AVATAR_URL,
            member.guild.id,
            member.id,
            next,
            if (next.startsWith("a_")) "gif" else "png"
        )
    val newAvatar: ImageProxy?
        /**
         * Returns an [ImageProxy] for this member's new avatar.
         *
         * @return Possibly-null [ImageProxy] of this member's new avatar
         *
         * @see .getNewAvatarUrl
         */
        get() {
            val newAvatarUrl = newAvatarUrl
            return newAvatarUrl?.let { ImageProxy(it) }
        }

    companion object {
        const val IDENTIFIER = "avatar"
    }
}
