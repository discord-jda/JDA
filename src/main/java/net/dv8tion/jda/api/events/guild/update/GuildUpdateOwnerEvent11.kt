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
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import javax.annotation.Nonnull

/**
 * Indicates that the owner of a [Guild][net.dv8tion.jda.api.entities.Guild] changed.
 *
 *
 * Can be used to detect when an owner of a guild changes and retrieve the old one
 *
 *
 * Identifier: `owner`
 */
class GuildUpdateOwnerEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild, oldOwner: Member?,
    /**
     * The previous owner user id
     *
     * @return The previous owner id
     */
    val oldOwnerIdLong: Long,
    /**
     * The new owner user id
     *
     * @return The new owner id
     */
    val newOwnerIdLong: Long
) : GenericGuildUpdateEvent<Member?>(api, responseNumber, guild, oldOwner, guild.owner, IDENTIFIER) {

    @get:Nonnull
    val newOwnerId: String
        /**
         * The new owner user id
         *
         * @return The new owner id
         */
        get() = java.lang.Long.toUnsignedString(newOwnerIdLong)

    @get:Nonnull
    val oldOwnerId: String
        /**
         * The previous owner user id
         *
         * @return The previous owner id
         */
        get() = java.lang.Long.toUnsignedString(oldOwnerIdLong)
    val oldOwner: Member?
        /**
         * The old owner
         *
         * @return The old owner
         */
        get() = oldValue
    val newOwner: Member?
        /**
         * The new owner
         *
         * @return The new owner
         */
        get() = newValue

    companion object {
        const val IDENTIFIER = "owner"
    }
}
