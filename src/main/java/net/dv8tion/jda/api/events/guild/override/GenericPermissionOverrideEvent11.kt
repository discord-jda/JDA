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
package net.dv8tion.jda.api.events.guild.override

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.PermissionOverride
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.unions.IPermissionContainerUnion
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [PermissionOverride] for a [GuildChannel] was created, deleted, or updated.
 * <br></br>Every guild channel override event is a subclass of this event and can be casted
 *
 *
 * Can be used to detect that any guild channel override event was fired
 */
open class GenericPermissionOverrideEvent(
    @Nonnull api: JDA, responseNumber: Long, @param:Nonnull protected val channel: IPermissionContainer,
    /**
     * The affected [PermissionOverride] that was updated.
     *
     * @return The override
     */
    @get:Nonnull
    @param:Nonnull val permissionOverride: PermissionOverride
) : GenericGuildEvent(api, responseNumber, channel.guild) {

    @get:Nonnull
    val channelType: ChannelType?
        /**
         * The [ChannelType] of the [GuildChannel][.getChannel] this override belongs to.
         *
         * @return The [ChannelType]
         */
        get() = channel.type

    /**
     * The [guild channel][IPermissionContainer] this override belongs to.
     *
     * @return The [channel][IPermissionContainer]
     */
    @Nonnull
    fun getChannel(): IPermissionContainerUnion {
        return channel as IPermissionContainerUnion
    }

    val isRoleOverride: Boolean
        /**
         * Whether this override was for a role.
         * <br></br>This means [.getRole] is likely not null.
         *
         * @return True, if this override is for a role
         */
        get() = permissionOverride.isRoleOverride
    val isMemberOverride: Boolean
        /**
         * Whether this override was for a member.
         * <br></br>Note that [.getMember] might still be null if the member isn't cached or there is a discord inconsistency.
         *
         * @return True, if this override is for a member
         */
        get() = permissionOverride.isMemberOverride
    val permissionHolder: IPermissionHolder?
        /**
         * The [IPermissionHolder] for the override.
         * <br></br>This can be a [Member] or [Role]. If the role or member are not cached then this will be null.
         *
         * @return Possibly-null permission holder
         */
        get() = if (isMemberOverride) permissionOverride.member else permissionOverride.role
    val member: Member?
        /**
         * The [Member] for the override.
         * <br></br>This can be null if the member is not cached or there is a discord inconsistency.
         *
         * @return Possibly-null member
         */
        get() = permissionOverride.member
    val role: Role?
        /**
         * The [Role] for the override.
         *
         * @return Possibly-null role
         */
        get() = permissionOverride.role
}
