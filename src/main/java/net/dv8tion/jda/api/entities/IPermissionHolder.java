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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.internal.utils.Checks;

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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a member and the bot {@link Guild#isDetached() isn't in the guild}.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a role and the bot {@link Guild#isDetached() isn't in the guild}.
     * @throws net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException
     *         if this is a member, the bot {@link Guild#isDetached() isn't in the guild},
     *         and the combination of Member and GuildChannel doesn't have permission data,
     *         see {@link net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException MissingEntityInteractionPermissionsException}.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a member and the bot {@link Guild#isDetached() isn't in the guild}.
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
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a role and the bot {@link Guild#isDetached() isn't in the guild}.
     * @throws net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException
     *         if this is a member, the bot {@link Guild#isDetached() isn't in the guild},
     *         and the combination of Member and GuildChannel doesn't have permission data,
     *         see {@link net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException MissingEntityInteractionPermissionsException}.
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
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a member and the bot {@link Guild#isDetached() isn't in the guild}.
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
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a member and the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder.
     *
     * @see    java.util.EnumSet EnumSet
     */
    default boolean hasPermission(@Nonnull Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");
        return hasPermission(permissions.toArray(Permission.EMPTY_PERMISSIONS));
    }

    /**
     * Checks whether or not this PermissionHolder has the given {@link net.dv8tion.jda.api.Permission Permissions} in the specified GuildChannel.
     *
     * @param  channel
     *         The {@link GuildChannel GuildChannel} in which to check.
     * @param  permissions
     *         Permissions to check for.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a role and the bot {@link Guild#isDetached() isn't in the guild}.
     * @throws net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException
     *         if this is a member, the bot {@link Guild#isDetached() isn't in the guild},
     *         and the combination of Member and GuildChannel doesn't have permission data,
     *         see {@link net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException MissingEntityInteractionPermissionsException}.
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
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a role and the bot {@link Guild#isDetached() isn't in the guild}.
     * @throws net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException
     *         if this is a member, the bot {@link Guild#isDetached() isn't in the guild},
     *         and the combination of Member and GuildChannel doesn't have permission data,
     *         see {@link net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException MissingEntityInteractionPermissionsException}.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder in the provided GuildChannel.
     */
    default boolean hasPermission(@Nonnull GuildChannel channel, @Nonnull Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");
        return hasPermission(channel, permissions.toArray(Permission.EMPTY_PERMISSIONS));
    }

    /**
     * Checks whether or not this PermissionHolder has {@link Permission#VIEW_CHANNEL VIEW_CHANNEL}
     * and {@link Permission#VOICE_CONNECT VOICE_CONNECT} permissions in the {@link GuildChannel}.
     *
     * @param  channel
     *         The channel to check access for
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if this is a role and the bot {@link Guild#isDetached() isn't in the guild}.
     * @throws net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException
     *         if this is a member, the bot {@link Guild#isDetached() isn't in the guild},
     *         and the combination of Member and GuildChannel doesn't have permission data,
     *         see {@link net.dv8tion.jda.api.exceptions.MissingEntityInteractionPermissionsException MissingEntityInteractionPermissionsException}.
     *
     * @return True, if the PermissionHolder has access
     */
    default boolean hasAccess(@Nonnull GuildChannel channel)
    {
        Checks.notNull(channel, "Channel");
        return channel.getType().isAudio()
                ? hasPermission(channel, Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL)
                : hasPermission(channel, Permission.VIEW_CHANNEL);
    }

    /**
     * Whether the permissions of this PermissionHolder are good enough to sync the target channel with the sync source.
     * <br>This checks what permissions would be changed by the overrides of the sync source and whether the permission holder is able to set them on the target channel.
     *
     * <p>If the permission holder had {@link Permission#MANAGE_PERMISSIONS} in an override on the target channel or {@link Permission#ADMINISTRATOR} on one of its roles, then it can set any permission on the target channel.
     * Otherwise, the permission holder can only set permissions it also has in the channel.
     *
     * @param  targetChannel
     *         The target channel to check
     * @param  syncSource
     *         The sync source, for example the parent category (see {@link net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel#getParentCategory()})
     *
     * @throws IllegalArgumentException
     *         If either of the channels is null or not from the same guild as this permission holder
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return True, if the channels can be synced
     */
    boolean canSync(@Nonnull IPermissionContainer targetChannel, @Nonnull IPermissionContainer syncSource);

    /**
     * Whether the permissions of this PermissionHolder are good enough to sync the target channel with any other channel.
     * <br>This checks whether the permission holder has <em>local administrator</em>.
     *
     * <p>If the permission holder had {@link Permission#MANAGE_PERMISSIONS} in an override on the target channel or {@link Permission#ADMINISTRATOR} on one of its roles, then it can set any permission on the target channel.
     *
     * @param  channel
     *         The target channel to check
     *
     * @throws IllegalArgumentException
     *         If the channel is null or not from the same guild as this permission holder
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return True, if the channel can be synced
     */
    boolean canSync(@Nonnull IPermissionContainer channel);
}
