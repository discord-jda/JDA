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
package net.dv8tion.jda.api.managers.channel.attribute

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.managers.channel.ChannelManager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager abstraction to modify [PermissionOverrides][PermissionOverride] of a [permission containing channel][IPermissionContainer].
 *
 * @param <T> The channel type
 * @param <M> The manager type
</M></T> */
interface IPermissionContainerManager<T : IPermissionContainer?, M : IPermissionContainerManager<T, M>?> :
    ChannelManager<T, M> {
    /**
     * Clears the overrides added via [.putPermissionOverride].
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clearOverridesAdded(): M

    /**
     * Clears the overrides removed via [.removePermissionOverride].
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clearOverridesRemoved(): M

    /**
     * Adds an override for the specified [IPermissionHolder]
     * with the provided raw bitmasks as allowed and denied permissions. If the permission holder already
     * had an override on this channel it will be replaced instead.
     *
     * @param  permHolder
     * The permission holder
     * @param  allow
     * The bitmask to grant
     * @param  deny
     * The bitmask to deny
     *
     * @throws IllegalArgumentException
     * If the provided permission holder is `null`
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel, or tries to set permissions it does not have without having [Permission.MANAGE_PERMISSIONS] explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see .putPermissionOverride
     * @see Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun putPermissionOverride(@Nonnull permHolder: IPermissionHolder?, allow: Long, deny: Long): M

    /**
     * Adds an override for the specified [IPermissionHolder]
     * with the provided permission sets as allowed and denied permissions. If the permission holder already
     * had an override on this channel it will be replaced instead.
     * <br></br>Example: `putPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL), null)`
     *
     * @param  permHolder
     * The permission holder
     * @param  allow
     * The permissions to grant, or null
     * @param  deny
     * The permissions to deny, or null
     *
     * @throws IllegalArgumentException
     * If the provided permission holder is `null`
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel, or tries to set permissions it does not have without having [Permission.MANAGE_PERMISSIONS] explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see .putPermissionOverride
     * @see java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    fun putPermissionOverride(
        @Nonnull permHolder: IPermissionHolder?,
        allow: Collection<Permission?>?,
        deny: Collection<Permission?>?
    ): M {
        val allowRaw = if (allow == null) 0 else Permission.getRaw(allow)
        val denyRaw = if (deny == null) 0 else Permission.getRaw(deny)
        return putPermissionOverride(permHolder, allowRaw, denyRaw)
    }

    /**
     * Adds an override for the specified role with the provided raw bitmasks as allowed and denied permissions.
     * If the role already had an override on this channel it will be replaced instead.
     *
     * @param  roleId
     * The ID of the role to set permissions for
     * @param  allow
     * The bitmask to grant
     * @param  deny
     * The bitmask to deny
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel, or tries to set permissions it does not have without having [Permission.MANAGE_PERMISSIONS] explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see .putRolePermissionOverride
     * @see Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun putRolePermissionOverride(roleId: Long, allow: Long, deny: Long): M

    /**
     * Adds an override for the specified role with the provided permission sets as allowed and denied permissions.
     * If the role already had an override on this channel it will be replaced instead.
     *
     * @param  roleId
     * The ID of the role to set permissions for
     * @param  allow
     * The permissions to grant, or null
     * @param  deny
     * The permissions to deny, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel, or tries to set permissions it does not have without having [Permission.MANAGE_PERMISSIONS] explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see .putRolePermissionOverride
     * @see java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    fun putRolePermissionOverride(roleId: Long, allow: Collection<Permission?>?, deny: Collection<Permission?>?): M {
        val allowRaw = if (allow == null) 0 else Permission.getRaw(allow)
        val denyRaw = if (deny == null) 0 else Permission.getRaw(deny)
        return putRolePermissionOverride(roleId, allowRaw, denyRaw)
    }

    /**
     * Adds an override for the specified member with the provided raw bitmasks as allowed and denied permissions.
     * If the member already had an override on this channel it will be replaced instead.
     *
     * @param  memberId
     * The ID of the member to set permissions for
     * @param  allow
     * The bitmask to grant
     * @param  deny
     * The bitmask to deny
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel, or tries to set permissions it does not have without having [Permission.MANAGE_PERMISSIONS] explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see .putMemberPermissionOverride
     * @see Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun putMemberPermissionOverride(memberId: Long, allow: Long, deny: Long): M

    /**
     * Adds an override for the specified member with the provided permission sets as allowed and denied permissions.
     * If the member already had an override on this channel it will be replaced instead.
     *
     * @param  memberId
     * The ID of the member to set permissions for
     * @param  allow
     * The permissions to grant, or null
     * @param  deny
     * The permissions to deny, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel, or tries to set permissions it does not have without having [Permission.MANAGE_PERMISSIONS] explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see .putMemberPermissionOverride
     * @see java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    fun putMemberPermissionOverride(
        memberId: Long,
        allow: Collection<Permission?>?,
        deny: Collection<Permission?>?
    ): M {
        val allowRaw = if (allow == null) 0 else Permission.getRaw(allow)
        val denyRaw = if (deny == null) 0 else Permission.getRaw(deny)
        return putMemberPermissionOverride(memberId, allowRaw, denyRaw)
    }

    /**
     * Removes the [PermissionOverride] for the specified
     * [IPermissionHolder]. If no override existed for this member
     * or role, this does nothing.
     *
     * @param  permHolder
     * The permission holder
     *
     * @throws IllegalArgumentException
     * If the provided permission holder is `null`
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun removePermissionOverride(@Nonnull permHolder: IPermissionHolder?): M

    /**
     * Removes the [PermissionOverride] for the specified
     * member or role ID. If no override existed for this member or role, this does nothing.
     *
     * @param  id
     * The ID of the permission holder
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * in this channel
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun removePermissionOverride(id: Long): M
}
