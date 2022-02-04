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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a {@link GuildChannel} that uses {@link PermissionOverride Permission Overrides}.
 *
 * Channels that implement this interface can override permissions for specific users or roles.
 *
 * @see PermissionOverride
 */
public interface IPermissionContainer extends GuildChannel
{
    //TODO-v5: Docs
    @Override
    @Nonnull
    IPermissionContainerManager<?, ?> getManager();

    /**
     * The {@link PermissionOverride} relating to the specified {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role}.
     * If there is no {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} for this {@link GuildChannel GuildChannel}
     * relating to the provided Member or Role, then this returns {@code null}.
     *
     * @param  permissionHolder
     *         The {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role} whose
     *         {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} is requested.
     *
     * @throws IllegalArgumentException
     *         If the provided permission holder is null, or from a different guild
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     *         relating to the provided Member or Role.
     */
    @Nullable
    PermissionOverride getPermissionOverride(@Nonnull IPermissionHolder permissionHolder);

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} that are part
     * of this {@link GuildChannel GuildChannel}.
     * <br>This combines {@link net.dv8tion.jda.api.entities.Member Member} and {@link net.dv8tion.jda.api.entities.Role Role} overrides.
     * If you would like only {@link net.dv8tion.jda.api.entities.Member Member} overrides or only {@link net.dv8tion.jda.api.entities.Role Role}
     * overrides, use {@link #getMemberPermissionOverrides()} or {@link #getRolePermissionOverrides()} respectively.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#MEMBER_OVERRIDES CacheFlag.MEMBER_OVERRIDES} to be enabled!
     * Without that CacheFlag, this list will only contain overrides for the currently logged in account and roles.
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    List<PermissionOverride> getPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.Member Member} {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link GuildChannel GuildChannel}.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#MEMBER_OVERRIDES CacheFlag.MEMBER_OVERRIDES} to be enabled!
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for {@link net.dv8tion.jda.api.entities.Member Member}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    default List<PermissionOverride> getMemberPermissionOverrides()
    {
        return Collections.unmodifiableList(getPermissionOverrides().stream()
                .filter(PermissionOverride::isMemberOverride)
                .collect(Collectors.toList()));
    }

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.Role Role} {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link GuildChannel GuildChannel}.
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for {@link net.dv8tion.jda.api.entities.Role Roles}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    default List<PermissionOverride> getRolePermissionOverrides()
    {
        return Collections.unmodifiableList(getPermissionOverrides().stream()
                .filter(PermissionOverride::isRoleOverride)
                .collect(Collectors.toList()));
    }

    /**
     * Creates a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for the specified {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role} in this GuildChannel.
     * You can use {@link #putPermissionOverride(IPermissionHolder)} to replace existing overrides.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @param  permissionHolder
     *         The Member or Role to create an override for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         if the specified permission holder is null or is not from {@link #getGuild()}
     * @throws java.lang.IllegalStateException
     *         If the specified permission holder already has a PermissionOverride. Use {@link #getPermissionOverride(IPermissionHolder)} to retrieve it.
     *         You can use {@link #putPermissionOverride(IPermissionHolder)} to replace existing overrides.
     *
     * @return {@link PermissionOverrideAction PermissionOverrideAction}
     *         Provides the newly created PermissionOverride for the specified permission holder
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction createPermissionOverride(@Nonnull IPermissionHolder permissionHolder)
    {
        Checks.notNull(permissionHolder, "PermissionHolder");
        if (getPermissionOverride(permissionHolder) != null)
            throw new IllegalStateException("Provided member already has a PermissionOverride in this channel!");

        return putPermissionOverride(permissionHolder);
    }

    /**
     * Creates a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for the specified {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role} in this GuildChannel.
     * <br>If the permission holder already has an existing override it will be replaced.
     *
     * @param  permissionHolder
     *         The Member or Role to create the override for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is null or from a different guild
     *
     * @return {@link PermissionOverrideAction PermissionOverrideAction}
     *         Provides the newly created PermissionOverride for the specified permission holder
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction putPermissionOverride(@Nonnull IPermissionHolder permissionHolder);

    /**
     * Creates a new override or updates an existing one.
     * <br>This is similar to calling {@link PermissionOverride#getManager()} if an override exists.
     *
     * @param  permissionHolder
     *         The Member/Role for the override
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If we don't have the permission to {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is null or not from this guild
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction}
     *         <br>With the current settings of an existing override or a fresh override with no permissions set
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction upsertPermissionOverride(@Nonnull IPermissionHolder permissionHolder)
    {
        PermissionOverride override = getPermissionOverride(permissionHolder);
        if (override != null)
            return override.getManager();
        return putPermissionOverride(permissionHolder);
    }
}
