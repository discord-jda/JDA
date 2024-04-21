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
package net.dv8tion.jda.api.entities.channel.attribute

import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.PermissionOverride
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction
import net.dv8tion.jda.internal.utils.Helpers
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a [GuildChannel] that uses [Permission Overrides][net.dv8tion.jda.api.entities.PermissionOverride].
 *
 *
 * Channels that implement this interface can override permissions for specific users or roles.
 *
 * @see net.dv8tion.jda.api.entities.PermissionOverride
 */
interface IPermissionContainer : GuildChannel {
    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?

    /**
     * The [net.dv8tion.jda.api.entities.PermissionOverride] relating to the specified [Member][net.dv8tion.jda.api.entities.Member] or [Role][net.dv8tion.jda.api.entities.Role].
     * If there is no [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride] for this [GuildChannel]
     * relating to the provided Member or Role, then this returns `null`.
     *
     * @param  permissionHolder
     * The [Member][net.dv8tion.jda.api.entities.Member] or [Role][net.dv8tion.jda.api.entities.Role] whose
     * [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride] is requested.
     *
     * @throws IllegalArgumentException
     * If the provided permission holder is null, or from a different guild
     *
     * @return Possibly-null [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     * relating to the provided Member or Role.
     */
    fun getPermissionOverride(@Nonnull permissionHolder: IPermissionHolder?): PermissionOverride?

    @JvmField
    @get:Nonnull
    val permissionOverrides: List<PermissionOverride>

    @get:Nonnull
    val memberPermissionOverrides: List<PermissionOverride>?
        /**
         * Gets all of the [Member][net.dv8tion.jda.api.entities.Member] [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride]
         * that are part of this [GuildChannel].
         *
         *
         * This requires [CacheFlag.MEMBER_OVERRIDES][net.dv8tion.jda.api.utils.cache.CacheFlag.MEMBER_OVERRIDES] to be enabled!
         *
         * @return Possibly-empty immutable list of all [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride]
         * for [Member][net.dv8tion.jda.api.entities.Member]
         * for this [GuildChannel].
         */
        get() = permissionOverrides.stream()
            .filter { obj: PermissionOverride -> obj.isMemberOverride }
            .collect(Helpers.toUnmodifiableList())

    @get:Nonnull
    val rolePermissionOverrides: List<PermissionOverride>?
        /**
         * Gets all of the [Role][net.dv8tion.jda.api.entities.Role] [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride]
         * that are part of this [GuildChannel].
         *
         * @return Possibly-empty immutable list of all [PermissionOverrides][net.dv8tion.jda.api.entities.PermissionOverride]
         * for [Roles][net.dv8tion.jda.api.entities.Role]
         * for this [GuildChannel].
         */
        get() = permissionOverrides.stream()
            .filter { obj: PermissionOverride -> obj.isRoleOverride }
            .collect(Helpers.toUnmodifiableList())

    /**
     * Creates a new override or updates an existing one.
     * <br></br>This is similar to calling [PermissionOverride.getManager] if an override exists.
     *
     * @param  permissionHolder
     * The Member/Role for the override
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If we don't have the permission to [MANAGE_PERMISSIONS][net.dv8tion.jda.api.Permission.MANAGE_PERMISSIONS]
     * @throws java.lang.IllegalArgumentException
     * If the provided permission holder is null or not from this guild
     *
     * @return [net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction]
     * <br></br>With the current settings of an existing override or a fresh override with no permissions set
     *
     * @see PermissionOverrideAction.clear
     * @see PermissionOverrideAction.grant
     * @see PermissionOverrideAction.deny
     */
    @Nonnull
    @CheckReturnValue
    fun upsertPermissionOverride(@Nonnull permissionHolder: IPermissionHolder?): PermissionOverrideAction?
}
