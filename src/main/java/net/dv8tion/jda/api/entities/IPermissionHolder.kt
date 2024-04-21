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

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

/**
 * Marker for entities that hold Permissions within JDA
 *
 * @since 3.0
 *
 * @see Role
 *
 * @see Member
 */
interface IPermissionHolder : ISnowflake {
    @JvmField
    @get:Nonnull
    val guild: Guild

    @JvmField
    @get:Nonnull
    val permissions: EnumSet<Permission?>?

    /**
     * The Permissions this PermissionHolder holds in the specified [GuildChannel].
     * <br></br>Permissions returned by this may be different from [.getPermissions]
     * due to the GuildChannel's [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride].
     * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @param  channel
     * The [GuildChannel] of which to get Permissions for
     *
     * @throws java.lang.IllegalArgumentException
     * If the channel is null
     *
     * @return Set of Permissions granted to this Permission Holder in the specified channel.
     */
    @Nonnull
    fun getPermissions(@Nonnull channel: GuildChannel?): EnumSet<Permission?>?

    @get:Nonnull
    val permissionsExplicit: EnumSet<Permission?>?

    /**
     * The explicitly granted permissions for this permission holder in the guild.
     * <br></br>This disregards owner and administrator privileges.
     * <br></br>Permissions returned by this may be different from [.getPermissionsExplicit]
     * due to the GuildChannel's [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride].
     * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @param  channel
     * The [GuildChannel] of which to get Permissions for
     *
     * @throws java.lang.IllegalArgumentException
     * If the channel is null
     *
     * @return EnumSet of the explicitly granted permissions in the specified channel
     */
    @Nonnull
    fun getPermissionsExplicit(@Nonnull channel: GuildChannel?): EnumSet<Permission?>?

    /**
     * Checks whether or not this PermissionHolder has the given [Permissions][net.dv8tion.jda.api.Permission] in the Guild.
     *
     * @param  permissions
     * Permissions to check for.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder.
     */
    fun hasPermission(@Nonnull vararg permissions: Permission?): Boolean

    /**
     * Checks whether or not this PermissionHolder has the [Permissions][net.dv8tion.jda.api.Permission] in the provided
     * `Collection<Permission>` in the Guild.
     *
     * @param  permissions
     * Permissions to check for.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder.
     *
     * @see java.util.EnumSet EnumSet
     */
    fun hasPermission(@Nonnull permissions: Collection<Permission?>?): Boolean

    /**
     * Checks whether or not this PermissionHolder has the given [Permissions][net.dv8tion.jda.api.Permission] in the specified GuildChannel.
     *
     * @param  channel
     * The [GuildChannel] in which to check.
     * @param  permissions
     * Permissions to check for.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder in the provided GuildChannel.
     *
     * @see java.util.EnumSet EnumSet
     */
    fun hasPermission(@Nonnull channel: GuildChannel?, @Nonnull vararg permissions: Permission?): Boolean

    /**
     * Checks whether or not this PermissionHolder has the [Permissions][net.dv8tion.jda.api.Permission] in the provided
     * `Collection<Permission>` in the specified GuildChannel.
     *
     * @param  channel
     * The [GuildChannel] in which to check.
     * @param  permissions
     * Permissions to check for.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder in the provided GuildChannel.
     */
    fun hasPermission(@Nonnull channel: GuildChannel?, @Nonnull permissions: Collection<Permission?>?): Boolean

    /**
     * Checks whether or not this PermissionHolder has [VIEW_CHANNEL][Permission.VIEW_CHANNEL]
     * and [VOICE_CONNECT][Permission.VOICE_CONNECT] permissions in the [GuildChannel].
     *
     * @param  channel
     * The channel to check access for
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return True, if the PermissionHolder has access
     */
    fun hasAccess(@Nonnull channel: GuildChannel): Boolean {
        Checks.notNull(channel, "Channel")
        return if (channel.type!!.isAudio()) hasPermission(
            channel,
            Permission.VOICE_CONNECT,
            Permission.VIEW_CHANNEL
        ) else hasPermission(channel, Permission.VIEW_CHANNEL)
    }

    /**
     * Whether the permissions of this PermissionHolder are good enough to sync the target channel with the sync source.
     * <br></br>This checks what permissions would be changed by the overrides of the sync source and whether the permission holder is able to set them on the target channel.
     *
     *
     * If the permission holder had [Permission.MANAGE_PERMISSIONS] in an override on the target channel or [Permission.ADMINISTRATOR] on one of its roles, then it can set any permission on the target channel.
     * Otherwise, the permission holder can only set permissions it also has in the channel.
     *
     * @param  targetChannel
     * The target channel to check
     * @param  syncSource
     * The sync source, for example the parent category (see [net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel.getParentCategory])
     *
     * @throws IllegalArgumentException
     * If either of the channels is null or not from the same guild as this permission holder
     *
     * @return True, if the channels can be synced
     */
    fun canSync(@Nonnull targetChannel: IPermissionContainer?, @Nonnull syncSource: IPermissionContainer?): Boolean

    /**
     * Whether the permissions of this PermissionHolder are good enough to sync the target channel with any other channel.
     * <br></br>This checks whether the permission holder has *local administrator*.
     *
     *
     * If the permission holder had [Permission.MANAGE_PERMISSIONS] in an override on the target channel or [Permission.ADMINISTRATOR] on one of its roles, then it can set any permission on the target channel.
     *
     * @param  channel
     * The target channel to check
     *
     * @throws IllegalArgumentException
     * If the channel is null or not from the same guild as this permission holder
     *
     * @return True, if the channel can be synced
     */
    fun canSync(@Nonnull channel: IPermissionContainer?): Boolean
}
