/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.Permission;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Marker for entities that hold Permissions within JDA
 *
 * @since 3.0
 *
 * @see Role
 * @see Member
 */
public interface IPermissionHolder extends ISnowflake
{
    /**
     * The Guild to which this PermissionHolder is related
     * 
     * @return A never-null Guild to which this PermissionHolder is linked
     */
    @Nonnull
    Guild getGuild();

    /**
     * The Guild-Wide Permissions this PermissionHolder holds.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return An EnumSet of Permissions granted to this PermissionHolder.
     */
    @Nonnull
    EnumSet<Permission> getPermissions();

    /**
     * The Permissions this PermissionHolder holds in the specified {@link GuildChannel GuildChannel}.
     * <br>Permissions returned by this may be different from {@link #getPermissions()}
     * due to the GuildChannel's {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @param  channel
     *         The {@link GuildChannel GuildChannel} of which to get Permissions for
     *
     * @throws java.lang.IllegalArgumentException
     *         If the channel is null
     *
     * @return Set of Permissions granted to this Permission Holder in the specified channel.
     */
    @Nonnull
    EnumSet<Permission> getPermissions(@Nonnull GuildChannel channel);

    /**
     * The explicitly granted permissions for this permission holder in the guild.
     * <br>This disregards owner and administrator privileges.
     * For a role this is identical to {@link #getPermissions()} and members have all their roles taken into consideration.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return EnumSet of the explicitly granted permissions
     */
    @Nonnull
    EnumSet<Permission> getPermissionsExplicit();

    /**
     * The explicitly granted permissions for this permission holder in the guild.
     * <br>This disregards owner and administrator privileges.
     * <br>Permissions returned by this may be different from {@link #getPermissionsExplicit()}
     * due to the GuildChannel's {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @param  channel
     *         The {@link GuildChannel GuildChannel} of which to get Permissions for
     *
     * @throws java.lang.IllegalArgumentException
     *         If the channel is null
     *
     * @return EnumSet of the explicitly granted permissions in the specified channel
     */
    @Nonnull
    EnumSet<Permission> getPermissionsExplicit(@Nonnull GuildChannel channel);

    /**
     * Checks whether or not this PermissionHolder has the given {@link net.dv8tion.jda.api.Permission Permissions} in the Guild.
     *
     * @param  permissions
     *         Permissions to check for.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder.
     */
    boolean hasPermission(@Nonnull Permission... permissions);

    /**
     * Checks whether or not this PermissionHolder has the {@link net.dv8tion.jda.api.Permission Permissions} in the provided
     * {@code Collection<Permission>} in the Guild.
     *
     * @param  permissions
     *         Permissions to check for.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder.
     *
     * @see    java.util.EnumSet EnumSet
     */
    boolean hasPermission(@Nonnull Collection<Permission> permissions);

    /**
     * Checks whether or not this PermissionHolder has the given {@link net.dv8tion.jda.api.Permission Permissions} in the specified GuildChannel.
     *
     * @param  channel
     *         The {@link GuildChannel GuildChannel} in which to check.
     * @param  permissions
     *         Permissions to check for.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder in the provided GuildChannel.
     *
     * @see    java.util.EnumSet EnumSet
     */
    boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Permission... permissions);

    /**
     * Checks whether or not this PermissionHolder has the {@link net.dv8tion.jda.api.Permission Permissions} in the provided
     * {@code Collection<Permission>} in the specified GuildChannel.
     *
     * @param  channel
     *         The {@link GuildChannel GuildChannel} in which to check.
     * @param  permissions
     *         Permissions to check for.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder in the provided GuildChannel.
     */
    boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Collection<Permission> permissions);
}
