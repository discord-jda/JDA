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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.unions.IPermissionContainerUnion
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents the specific [Member][net.dv8tion.jda.api.entities.Member] or [Role][net.dv8tion.jda.api.entities.Role]
 * permission overrides that can be set for channels.
 *
 * @see net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer.upsertPermissionOverride
 * @see net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer.getPermissionOverrides
 * @see net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer.getPermissionOverride
 * @see net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer.getMemberPermissionOverrides
 * @see net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer.getRolePermissionOverrides
 */
interface PermissionOverride : ISnowflake {
    /**
     * This is the raw binary representation (as a base 10 long) of the permissions **allowed** by this override.
     * <br></br>The long relates to the offsets used by each [Permission][net.dv8tion.jda.api.Permission].
     *
     * @return Never-negative long containing the binary representation of the allowed permissions of this override.
     */
    @JvmField
    val allowedRaw: Long

    /**
     * This is the raw binary representation (as a base 10 long) of the permissions **not affected** by this override.
     * <br></br>The long relates to the offsets used by each [Permission][net.dv8tion.jda.api.Permission].
     *
     * @return Never-negative long containing the binary representation of the unaffected permissions of this override.
     */
    val inheritRaw: Long

    /**
     * This is the raw binary representation (as a base 10 long) of the permissions **denied** by this override.
     * <br></br>The long relates to the offsets used by each [Permission][net.dv8tion.jda.api.Permission].
     *
     * @return Never-negative long containing the binary representation of the denied permissions of this override.
     */
    @JvmField
    val deniedRaw: Long

    @get:Nonnull
    val allowed: EnumSet<Permission?>?

    @get:Nonnull
    val inherit: EnumSet<Permission?>?

    @get:Nonnull
    val denied: EnumSet<Permission?>?

    @JvmField
    @get:Nonnull
    val jDA: JDA?

    /**
     * This method will return the [PermissionHolder][net.dv8tion.jda.api.entities.IPermissionHolder] of this PermissionOverride.
     * It can be used to get the general permissions of that PermissionHolder, no matter if it is a [Member][net.dv8tion.jda.api.entities.Member] or a [Role][net.dv8tion.jda.api.entities.Role].
     * <br></br>Similar to [.getMember] this will return `null` if the member is not cached.
     *
     *
     * To get the concrete Member or Role, use [PermissionOverride.getMember] or [PermissionOverride.getRole]!
     *
     * @return Possibly-null [IPermissionHolder][net.dv8tion.jda.api.entities.IPermissionHolder] of this PermissionOverride.
     *
     * @see PermissionOverride.getRole
     * @see PermissionOverride.getMember
     */
    @JvmField
    val permissionHolder: IPermissionHolder?

    /**
     * If this PermissionOverride is an override dealing with a [Member][net.dv8tion.jda.api.entities.Member], then
     * this method will return the related [Member][net.dv8tion.jda.api.entities.Member] if the member is currently cached.
     * <br></br>Otherwise, this method returns `null`.
     * <br></br>Basically: if [PermissionOverride.isMemberOverride] returns `false` or the member is not cached, this returns `null`.
     *
     * @return Possibly-null related [Member][net.dv8tion.jda.api.entities.Member].
     */
    @JvmField
    val member: Member?

    /**
     * If this PermissionOverride is an override dealing with a [Role][net.dv8tion.jda.api.entities.Role], then
     * this method will return the related [Role][net.dv8tion.jda.api.entities.Role].
     * <br></br>Otherwise, this method returns `null`.
     * <br></br>Basically: if [PermissionOverride.isRoleOverride]
     * returns `false`, this returns `null`.
     *
     * @return Possibly-null related [net.dv8tion.jda.api.entities.Role].
     */
    @JvmField
    val role: Role?

    @JvmField
    @get:Nonnull
    val channel: IPermissionContainerUnion?

    @JvmField
    @get:Nonnull
    val guild: Guild?

    /**
     * Used to determine if this PermissionOverride relates to
     * a specific [Member][net.dv8tion.jda.api.entities.Member].
     *
     * @return True if this override is a user override.
     */
    @JvmField
    val isMemberOverride: Boolean

    /**
     * Used to determine if this PermissionOverride relates to
     * a specific [Role][net.dv8tion.jda.api.entities.Role].
     *
     * @return True if this override is a role override.
     */
    @JvmField
    val isRoleOverride: Boolean

    @JvmField
    @get:Nonnull
    val manager: PermissionOverrideAction?

    /**
     * Deletes this PermissionOverride.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_OVERRIDE]
     * <br></br>If the the override was already deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the channel this override was a part of was already deleted
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the Guild
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * if we don't have the permission to [MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL]
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): AuditableRestAction<Void?>?
}
