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

package net.dv8tion.jda.api.managers.channel.attribute;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionContainer;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.managers.channel.ChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface IPermissionContainerManager<T extends IPermissionContainer, M extends IPermissionContainerManager<T, M>> extends ChannelManager<T, M>
{

    /**
     * Clears the overrides added via {@link #putPermissionOverride(IPermissionHolder, Collection, Collection)}.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M clearOverridesAdded();

    /**
     * Clears the overrides removed via {@link #removePermissionOverride(IPermissionHolder)}.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M clearOverridesRemoved();

    /**
     * Adds an override for the specified {@link IPermissionHolder IPermissionHolder}
     * with the provided raw bitmasks as allowed and denied permissions. If the permission holder already
     * had an override on this channel it will be replaced instead.
     *
     * @param  permHolder
     *         The permission holder
     * @param  allow
     *         The bitmask to grant
     * @param  deny
     *         The bitmask to deny
     *
     * @throws IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putPermissionOverride(IPermissionHolder, Collection, Collection)
     * @see    Permission#getRaw(Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    M putPermissionOverride(@Nonnull IPermissionHolder permHolder, long allow, long deny);

    /**
     * Adds an override for the specified {@link IPermissionHolder IPermissionHolder}
     * with the provided permission sets as allowed and denied permissions. If the permission holder already
     * had an override on this channel it will be replaced instead.
     * <br>Example: {@code putPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL), null)}
     *
     * @param  permHolder
     *         The permission holder
     * @param  allow
     *         The permissions to grant, or null
     * @param  deny
     *         The permissions to deny, or null
     *
     * @throws IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putPermissionOverride(IPermissionHolder, long, long)
     * @see    java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    default M putPermissionOverride(@Nonnull IPermissionHolder permHolder, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny)
    {
        long allowRaw = allow == null ? 0 : Permission.getRaw(allow);
        long denyRaw  = deny  == null ? 0 : Permission.getRaw(deny);
        return putPermissionOverride(permHolder, allowRaw, denyRaw);
    }

    /**
     * Adds an override for the specified role with the provided raw bitmasks as allowed and denied permissions.
     * If the role already had an override on this channel it will be replaced instead.
     *
     * @param  roleId
     *         The ID of the role to set permissions for
     * @param  allow
     *         The bitmask to grant
     * @param  deny
     *         The bitmask to deny
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putRolePermissionOverride(long, Collection, Collection)
     * @see    Permission#getRaw(Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    M putRolePermissionOverride(long roleId, long allow, long deny);

    /**
     * Adds an override for the specified role with the provided permission sets as allowed and denied permissions.
     * If the role already had an override on this channel it will be replaced instead.
     *
     * @param  roleId
     *         The ID of the role to set permissions for
     * @param  allow
     *         The permissions to grant, or null
     * @param  deny
     *         The permissions to deny, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putRolePermissionOverride(long, long, long)
     * @see    java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    default M putRolePermissionOverride(long roleId, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny)
    {
        long allowRaw = allow == null ? 0 : Permission.getRaw(allow);
        long denyRaw  = deny  == null ? 0 : Permission.getRaw(deny);
        return putRolePermissionOverride(roleId, allowRaw, denyRaw);
    }

    /**
     * Adds an override for the specified member with the provided raw bitmasks as allowed and denied permissions.
     * If the member already had an override on this channel it will be replaced instead.
     *
     * @param  memberId
     *         The ID of the member to set permissions for
     * @param  allow
     *         The bitmask to grant
     * @param  deny
     *         The bitmask to deny
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putMemberPermissionOverride(long, Collection, Collection)
     * @see    Permission#getRaw(Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    M putMemberPermissionOverride(long memberId, long allow, long deny);

    /**
     * Adds an override for the specified member with the provided permission sets as allowed and denied permissions.
     * If the member already had an override on this channel it will be replaced instead.
     *
     * @param  memberId
     *         The ID of the member to set permissions for
     * @param  allow
     *         The permissions to grant, or null
     * @param  deny
     *         The permissions to deny, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putMemberPermissionOverride(long, long, long)
     * @see    java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    default M putMemberPermissionOverride(long memberId, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny)
    {
        long allowRaw = allow == null ? 0 : Permission.getRaw(allow);
        long denyRaw  = deny  == null ? 0 : Permission.getRaw(deny);
        return putMemberPermissionOverride(memberId, allowRaw, denyRaw);
    }

    /**
     * Removes the {@link PermissionOverride PermissionOverride} for the specified
     * {@link IPermissionHolder IPermissionHolder}. If no override existed for this member
     * or role, this does nothing.
     *
     * @param  permHolder
     *         The permission holder
     *
     * @throws IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M removePermissionOverride(@Nonnull IPermissionHolder permHolder);

    /**
     * Removes the {@link PermissionOverride PermissionOverride} for the specified
     * member or role ID. If no override existed for this member or role, this does nothing.
     *
     * @param  id
     *         The ID of the permission holder
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M removePermissionOverride(long id);
}
