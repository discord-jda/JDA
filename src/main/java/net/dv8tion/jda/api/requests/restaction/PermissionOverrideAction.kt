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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.internal.utils.*
import java.util.*
import java.util.concurrent.*
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Extension of [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction] specifically
 * designed to create a [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
 * for a [GuildChannel].
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 *
 * @see net.dv8tion.jda.api.entities.PermissionOverride.getManager
 * @see net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer.upsertPermissionOverride
 */
interface PermissionOverrideAction : AuditableRestAction<PermissionOverride?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): PermissionOverrideAction?
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): PermissionOverrideAction?
    @Nonnull
    override fun deadline(timestamp: Long): PermissionOverrideAction?

    /**
     * Shortcut for `resetAllow().resetDeny()`.
     * <br></br>The permission override will be empty after this operation
     *
     * @return The current PermissionOverrideAction for chaining convenience
     */
    @Nonnull
    fun reset(): PermissionOverrideAction? {
        return resetAllow().resetDeny()
    }

    /**
     * Resets the allowed permissions to the current original value.
     * <br></br>For a new override this will just be 0.
     *
     * @return The current PermissionOverrideAction for chaining convenience
     */
    @Nonnull
    fun resetAllow(): PermissionOverrideAction

    /**
     * Resets the denied permissions to the current original value.
     * <br></br>For a new override this will just be 0.
     *
     * @return The current PermissionOverrideAction for chaining convenience
     */
    @Nonnull
    fun resetDeny(): PermissionOverrideAction?

    @get:Nonnull
    val channel: IPermissionContainer

    /**
     * The [Role] for this override
     *
     * @return The role, or null if this is a member override
     */
    val role: Role?

    /**
     * The [Member] for this override
     *
     * @return The member, or null if this is a role override
     */
    val member: Member?

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild] for this override
         *
         * @return The guild
         */
        get() = channel.guild

    /**
     * The currently set of allowed permission bits.
     * <br></br>This value represents all **granted** permissions
     * in the raw bitwise representation.
     *
     *
     * Use [.getAllowedPermissions] to retrieve a [List][java.util.List]
     * with [Permissions][net.dv8tion.jda.api.Permission] for this value
     *
     * @return long value of granted permissions
     */
    val allowed: Long

    @get:Nonnull
    val allowedPermissions: EnumSet<Permission?>?
        /**
         * Set of [Permissions][net.dv8tion.jda.api.Permission]
         * that would be **granted** by the PermissionOverride that is created by this action.
         * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
         *
         * @return set of granted [Permissions][net.dv8tion.jda.api.Permission]
         */
        get() = Permission.getPermissions(allowed)

    /**
     * The currently set of denied permission bits.
     * <br></br>This value represents all **denied** permissions
     * in the raw bitwise representation.
     *
     *
     * Use [.getDeniedPermissions] to retrieve a [List][java.util.List]
     * with [Permissions][net.dv8tion.jda.api.Permission] for this value
     *
     * @return long value of denied permissions
     */
    val denied: Long

    @get:Nonnull
    val deniedPermissions: EnumSet<Permission?>?
        /**
         * Set of [Permissions][net.dv8tion.jda.api.Permission]
         * that would be **denied** by the PermissionOverride that is created by this action.
         * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
         *
         * @return set of denied [Permissions][net.dv8tion.jda.api.Permission]
         */
        get() = Permission.getPermissions(denied)

    /**
     * The currently set of inherited permission bits.
     * <br></br>This value represents all permissions that are not explicitly allowed or denied
     * in their raw bitwise representation.
     * <br></br>Inherited Permissions are permissions that are defined by other rules
     * from maybe other PermissionOverrides or a Role.
     *
     *
     * Use [.getInheritedPermissions] to retrieve a [List][java.util.List]
     * with [Permissions][net.dv8tion.jda.api.Permission] for this value
     *
     * @return long value of inherited permissions
     */
    val inherited: Long

    @get:Nonnull
    val inheritedPermissions: EnumSet<Permission?>?
        /**
         * Set of [Permissions][net.dv8tion.jda.api.Permission]
         * that would be **inherited** from other permission holders.
         * <br></br>Permissions returned are not explicitly granted or denied!
         * <br></br><u>Changes to the returned set do not affect this entity directly.</u>
         *
         * @return set of inherited [Permissions][net.dv8tion.jda.api.Permission]
         *
         * @see .getInherited
         */
        get() = Permission.getPermissions(inherited)

    /**
     * Whether this Action will
     * create a [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     * for a [Member][net.dv8tion.jda.api.entities.Member] or not
     *
     * @return True, if this is targeting a Member
     * If this is `false` it is targeting a [Role][net.dv8tion.jda.api.entities.Role]. ([.isRole])
     */
    fun isMember(): Boolean

    /**
     * Whether this Action will
     * create a [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     * for a [Role][net.dv8tion.jda.api.entities.Role] or not
     *
     * @return True, if this is targeting a Role.
     * If this is `false` it is targeting a [Member][net.dv8tion.jda.api.entities.Member]. ([.isMember])
     */
    fun isRole(): Boolean

    /**
     * Sets the value of explicitly granted permissions
     * using the bitwise representation of a set of [Permissions][net.dv8tion.jda.api.Permission].
     * <br></br>This value can be retrieved through [Permissions.getRaw(Permission...)][net.dv8tion.jda.api.Permission.getRaw]!
     * <br></br>**Note: Permissions not marked as [isChannel()][net.dv8tion.jda.api.Permission.isChannel] will have no affect!**
     *
     *
     * All newly granted permissions will be removed from the currently set denied permissions.
     * <br></br>`allow = allowBits; deny = deny & ~allowBits;`
     *
     * @param  allowBits
     * The **positive** bits representing the granted
     * permissions for the new PermissionOverride
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see .setAllowed
     * @see .setAllowed
     */
    @Nonnull
    @CheckReturnValue
    fun setAllowed(allowBits: Long): PermissionOverrideAction

    /**
     * Sets the value of explicitly granted permissions
     * using a Collection of [Permissions][net.dv8tion.jda.api.Permission].
     * <br></br>**Note: Permissions not marked as [isChannel()][net.dv8tion.jda.api.Permission.isChannel] will have no affect!**
     *
     *
     * Example: `setAllow(EnumSet.of(Permission.VIEW_CHANNEL))`
     *
     * @param  permissions
     * The Collection of Permissions representing the granted
     * permissions for the new PermissionOverride.
     * <br></br>If the provided value is `null` the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     * If the any of the specified Permissions is `null`
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see java.util.EnumSet EnumSet
     *
     * @see .setAllowed
     */
    @Nonnull
    @CheckReturnValue
    fun setAllowed(permissions: Collection<Permission?>?): PermissionOverrideAction {
        if (permissions == null || permissions.isEmpty()) return setAllowed(0)
        Checks.noneNull(permissions, "Permissions")
        return setAllowed(Permission.getRaw(permissions))
    }

    /**
     * Sets the value of explicitly granted permissions
     * using a set of [Permissions][net.dv8tion.jda.api.Permission].
     * <br></br>**Note: Permissions not marked as [isChannel()][net.dv8tion.jda.api.Permission.isChannel] will have no affect!**
     *
     * @param  permissions
     * The Permissions representing the granted
     * permissions for the new PermissionOverride.
     * <br></br>If the provided value is `null` the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     * If the any of the specified Permissions is `null`
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setAllowed(vararg permissions: Permission?): PermissionOverrideAction? {
        if (permissions == null || permissions.size == 0) return setAllowed(0)
        Checks.noneNull(permissions, "Permissions")
        return setAllowed(Permission.getRaw(*permissions))
    }

    /**
     * Grants the specified permissions.
     * <br></br>This does not override already granted permissions.
     *
     * @param  allowBits
     * The permissions to grant, in addition to already allowed permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun grant(allowBits: Long): PermissionOverrideAction?

    /**
     * Grants the specified permissions.
     * <br></br>This does not override already granted permissions.
     *
     * @param  permissions
     * The permissions to grant, in addition to already allowed permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     * If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun grant(@Nonnull permissions: Collection<Permission?>?): PermissionOverrideAction? {
        return grant(Permission.getRaw(permissions!!))
    }

    /**
     * Grants the specified permissions.
     * <br></br>This does not override already granted permissions.
     *
     * @param  permissions
     * The permissions to grant, in addition to already allowed permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     * If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun grant(@Nonnull vararg permissions: Permission?): PermissionOverrideAction? {
        return grant(Permission.getRaw(*permissions))
    }

    /**
     * Sets the value of explicitly denied permissions
     * using the bitwise representation of a set of [Permissions][net.dv8tion.jda.api.Permission].
     * <br></br>This value can be retrieved through [Permissions.getRaw(Permission...)][net.dv8tion.jda.api.Permission.getRaw]!
     * <br></br>**Note: Permissions not marked as [isChannel()][net.dv8tion.jda.api.Permission.isChannel] will have no affect!**
     *
     *
     * All newly denied permissions will be removed from the currently set allowed permissions.
     * <br></br>`deny = denyBits; allow = allow & ~denyBits;`
     *
     * @param  denyBits
     * The **positive** bits representing the denied
     * permissions for the new PermissionOverride
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see .setDenied
     * @see .setDenied
     */
    @Nonnull
    @CheckReturnValue
    fun setDenied(denyBits: Long): PermissionOverrideAction?

    /**
     * Sets the value of explicitly denied permissions
     * using a Collection of [Permissions][net.dv8tion.jda.api.Permission].
     * <br></br>**Note: Permissions not marked as [isChannel()][net.dv8tion.jda.api.Permission.isChannel] will have no affect!**
     *
     *
     * Example: `setDeny(EnumSet.of(Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI))`
     *
     * @param  permissions
     * The Collection of Permissions representing the denied
     * permissions for the new PermissionOverride.
     * <br></br>If the provided value is `null` the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     * If the any of the specified Permissions is `null`
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see java.util.EnumSet EnumSet
     *
     * @see .setDenied
     */
    @Nonnull
    @CheckReturnValue
    fun setDenied(permissions: Collection<Permission?>?): PermissionOverrideAction? {
        if (permissions == null || permissions.isEmpty()) return setDenied(0)
        Checks.noneNull(permissions, "Permissions")
        return setDenied(Permission.getRaw(permissions))
    }

    /**
     * Sets the value of explicitly denied permissions
     * using a set of [Permissions][net.dv8tion.jda.api.Permission].
     * <br></br>**Note: Permissions not marked as [isChannel()][net.dv8tion.jda.api.Permission.isChannel] will have no affect!**
     *
     * @param  permissions
     * The Permissions representing the denied
     * permissions for the new PermissionOverride.
     * <br></br>If the provided value is `null` the permissions are reset to the default of none
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     * @throws java.lang.IllegalArgumentException
     * If the any of the specified Permissions is `null`
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDenied(vararg permissions: Permission?): PermissionOverrideAction? {
        if (permissions == null || permissions.size == 0) return setDenied(0)
        Checks.noneNull(permissions, "Permissions")
        return setDenied(Permission.getRaw(*permissions))
    }

    /**
     * Denies the specified permissions.
     * <br></br>This does not override already denied permissions.
     *
     * @param  denyBits
     * The permissions to deny, in addition to already denied permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun deny(denyBits: Long): PermissionOverrideAction?

    /**
     * Denies the specified permissions.
     * <br></br>This does not override already denied permissions.
     *
     * @param  permissions
     * The permissions to deny, in addition to already denied permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     * If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun deny(@Nonnull permissions: Collection<Permission?>?): PermissionOverrideAction? {
        return deny(Permission.getRaw(permissions!!))
    }

    /**
     * Denies the specified permissions.
     * <br></br>This does not override already denied permissions.
     *
     * @param  permissions
     * The permissions to deny, in addition to already denied permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     * If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun deny(@Nonnull vararg permissions: Permission?): PermissionOverrideAction? {
        return deny(Permission.getRaw(*permissions))
    }

    /**
     * Clears the provided [Permissions][net.dv8tion.jda.api.Permission] bits
     * from the [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     * <br></br>This will cause the provided Permissions to be inherited from other overrides or roles.
     *
     * @param  inheritedBits
     * The permissions to clear from the [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clear(inheritedBits: Long): PermissionOverrideAction?

    /**
     * Clears the provided [Permissions][net.dv8tion.jda.api.Permission] bits
     * from the [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     * <br></br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     * The permissions to clear from the [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     * If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clear(@Nonnull permissions: Collection<Permission?>?): PermissionOverrideAction? {
        return clear(Permission.getRaw(permissions!!))
    }

    /**
     * Clears the provided [Permissions][net.dv8tion.jda.api.Permission] bits
     * from the [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride].
     * <br></br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     * The permissions to clear from the [PermissionOverride][net.dv8tion.jda.api.entities.PermissionOverride]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     * If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun clear(@Nonnull vararg permissions: Permission?): PermissionOverrideAction? {
        return clear(Permission.getRaw(*permissions))
    }

    /**
     * Combination of [.setAllowed] and [.setDenied]
     * <br></br>First sets the allow bits and then the deny bits.
     *
     * @param  allowBits
     * An unsigned bitwise representation
     * of granted Permissions
     * @param  denyBits
     * An unsigned bitwise representation
     * of denied Permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see .setPermissions
     * @see net.dv8tion.jda.api.Permission.getRaw
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun setPermissions(allowBits: Long, denyBits: Long): PermissionOverrideAction?

    /**
     * Combination of [.setAllowed] and [.setDenied]
     * <br></br>First sets the granted permissions and then the denied permissions.
     * <br></br>If a passed collection is `null` it resets the represented value to `0` - no permission specifics.
     *
     *
     * Example: `setPermissions(EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI))`
     *
     * @param  grantPermissions
     * A Collection of [Permissions][net.dv8tion.jda.api.Permission]
     * representing all explicitly granted Permissions for the PermissionOverride
     * @param  denyPermissions
     * A Collection of [Permissions][net.dv8tion.jda.api.Permission]
     * representing all explicitly denied Permissions for the PermissionOverride
     *
     * @throws java.lang.IllegalArgumentException
     * If the any of the specified Permissions is `null`
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_PERMISSIONS]
     * on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see java.util.EnumSet EnumSet
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun setPermissions(
        grantPermissions: Collection<Permission?>?,
        denyPermissions: Collection<Permission?>?
    ): PermissionOverrideAction? {
        return setAllowed(grantPermissions).setDenied(denyPermissions)
    }
}
