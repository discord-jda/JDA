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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a {@link GuildChannel} that uses {@link net.dv8tion.jda.api.entities.PermissionOverride Permission Overrides}.
 *
 * <p>Channels that implement this interface can override permissions for specific users or roles.
 *
 * @see net.dv8tion.jda.api.entities.PermissionOverride
 */
public interface IPermissionContainer extends GuildChannel
{
    @Override
    @Nonnull
    IPermissionContainerManager<?, ?> getManager();

    /**
     * The {@link net.dv8tion.jda.api.entities.PermissionOverride} relating to the specified {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role}.
     * If there is no {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} for this {@link GuildChannel GuildChannel}
     * relating to the provided Member or Role, then this returns {@code null}.
     *
     * @param  permissionHolder
     *         The {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role} whose
     *         {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} is requested.
     *
     * @throws IllegalArgumentException
     *         If the provided permission holder is null, or from a different guild
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    @Unmodifiable
    List<PermissionOverride> getPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.Member Member} {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link GuildChannel GuildChannel}.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#MEMBER_OVERRIDES CacheFlag.MEMBER_OVERRIDES} to be enabled!
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for {@link net.dv8tion.jda.api.entities.Member Member}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    @Unmodifiable
    default List<PermissionOverride> getMemberPermissionOverrides()
    {
        return getPermissionOverrides().stream()
                .filter(PermissionOverride::isMemberOverride)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.Role Role} {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link GuildChannel GuildChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for {@link net.dv8tion.jda.api.entities.Role Roles}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    @Unmodifiable
    default List<PermissionOverride> getRolePermissionOverrides()
    {
        return getPermissionOverrides().stream()
                .filter(PermissionOverride::isRoleOverride)
                .collect(Helpers.toUnmodifiableList());
    }

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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction}
     *         <br>With the current settings of an existing override or a fresh override with no permissions set
     *
     * @see    PermissionOverrideAction#clear(long)
     * @see    PermissionOverrideAction#grant(long)
     * @see    PermissionOverrideAction#deny(long)
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction upsertPermissionOverride(@Nonnull IPermissionHolder permissionHolder);
}
