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
package net.dv8tion.jda.api.managers

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.internal.utils.Checks
import java.awt.Color
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for a [Role][net.dv8tion.jda.api.entities.Role].
 *
 *
 * **Example**
 * <pre>`manager.setName("Administrator")
 * .setColor(null)
 * .queue();
 * manager.reset(RoleManager.PERMISSION | RoleManager.NAME)
 * .setName("Traitor")
 * .setColor(Color.RED)
 * .queue();
`</pre> *
 *
 * @see net.dv8tion.jda.api.entities.Role.getManager
 */
interface RoleManager : Manager<RoleManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(RoleManager.COLOR | RoleManager.NAME);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.COLOR]
     *  * [.PERMISSION]
     *  * [.HOIST]
     *  * [.MENTIONABLE]
     *  * [.ICON]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): RoleManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(RoleManager.COLOR, RoleManager.NAME);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.COLOR]
     *  * [.PERMISSION]
     *  * [.HOIST]
     *  * [.MENTIONABLE]
     *  * [.ICON]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): RoleManager?

    @get:Nonnull
    val role: Role

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] this Manager's
         * [Role][net.dv8tion.jda.api.entities.Role] is in.
         * <br></br>This is logically the same as calling `getRole().getGuild()`
         *
         * @return The parent [Guild][net.dv8tion.jda.api.entities.Guild]
         */
        get() = role.guild

    /**
     * Sets the **<u>name</u>** of the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * A role name **must not** be `null` nor less than 1 characters or more than 32 characters long!
     *
     * @param  name
     * The new name for the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @throws IllegalArgumentException
     * If the provided name is `null` or not between 1-100 characters long
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): RoleManager?

    /**
     * Sets the [Permissions][net.dv8tion.jda.api.Permission] of the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * Permissions may only include already present Permissions for the currently logged in account.
     * <br></br>You are unable to give permissions you don't have!
     *
     * @param  perms
     * The new raw permission value for the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see .setPermissions
     * @see .setPermissions
     */
    @Nonnull
    @CheckReturnValue
    fun setPermissions(perms: Long): RoleManager?

    /**
     * Sets the [Permissions][net.dv8tion.jda.api.Permission] of the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * Permissions may only include already present Permissions for the currently logged in account.
     * <br></br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     * The new permission for the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have permission to apply one of the specified permissions
     * @throws java.lang.IllegalArgumentException
     * If any of the provided values is `null`
     *
     * @return RoleManager for chaining convenience
     *
     * @see .setPermissions
     * @see .setPermissions
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun setPermissions(@Nonnull vararg permissions: Permission?): RoleManager? {
        Checks.notNull(permissions, "Permissions")
        return setPermissions(Arrays.asList(*permissions))
    }

    /**
     * Sets the [Permissions][net.dv8tion.jda.api.Permission] of the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * Permissions may only include already present Permissions for the currently logged in account.
     * <br></br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     * The new permission for the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have permission to apply one of the specified permissions
     * @throws java.lang.IllegalArgumentException
     * If any of the provided values is `null`
     *
     * @return RoleManager for chaining convenience
     *
     * @see .setPermissions
     * @see .setPermissions
     * @see java.util.EnumSet EnumSet
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun setPermissions(@Nonnull permissions: Collection<Permission?>?): RoleManager? {
        Checks.noneNull(permissions, "Permissions")
        return setPermissions(Permission.getRaw(permissions!!))
    }

    /**
     * Sets the [Color][java.awt.Color] of the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     * @param  color
     * The new color for the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setColor(color: Color?): RoleManager? {
        return setColor(color?.rgb ?: Role.DEFAULT_COLOR_RAW)
    }

    /**
     * Sets the rgb color of the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     * @param  rgb
     * The new color for the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @return RoleManager for chaining convenience
     *
     * @see Role.DEFAULT_COLOR_RAW Role.DEFAULT_COLOR_RAW
     */
    @Nonnull
    @CheckReturnValue
    fun setColor(rgb: Int): RoleManager?

    /**
     * Sets the **<u>hoist state</u>** of the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     * @param  hoisted
     * Whether the selected [Role][net.dv8tion.jda.api.entities.Role] should be hoisted
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setHoisted(hoisted: Boolean): RoleManager?

    /**
     * Sets the **<u>mentionable state</u>** of the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     * @param  mentionable
     * Whether the selected [Role][net.dv8tion.jda.api.entities.Role] should be mentionable
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setMentionable(mentionable: Boolean): RoleManager?

    /**
     * Sets the [Icon][net.dv8tion.jda.api.entities.Icon] of this [Role][net.dv8tion.jda.api.entities.Role].
     *
     * @param  icon
     * The new icon for this [Role][net.dv8tion.jda.api.entities.Role]
     * or `null` to reset
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setIcon(icon: Icon?): RoleManager?

    /**
     * Sets the Unicode Emoji of this [Role][net.dv8tion.jda.api.entities.Role] instead of a custom image.
     *
     * @param  emoji
     * The new Unicode Emoji for this [Role][net.dv8tion.jda.api.entities.Role]
     * or `null` to reset
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setIcon(emoji: String?): RoleManager?

    /**
     * Sets the Unicode Emoji of this [Role][net.dv8tion.jda.api.entities.Role] instead of a custom image.
     *
     * @param  emoji
     * The new Unicode Emoji for this [Role][net.dv8tion.jda.api.entities.Role]
     * or `null` to reset
     *
     * @return RoleManager for chaining convenience
     *
     * @see net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode
     * @see UnicodeEmoji
     */
    @Nonnull
    @CheckReturnValue
    fun setIcon(emoji: UnicodeEmoji?): RoleManager? {
        return setIcon(emoji?.formatted)
    }

    /**
     * Adds the specified [Permissions][net.dv8tion.jda.api.Permission] to the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * Permissions may only include already present Permissions for the currently logged in account.
     * <br></br>You are unable to give permissions you don't have!
     *
     * @param  perms
     * The permission to give to the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see .setPermissions
     * @see .setPermissions
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun givePermissions(@Nonnull vararg perms: Permission?): RoleManager? {
        Checks.notNull(perms, "Permissions")
        return givePermissions(Arrays.asList(*perms))
    }

    /**
     * Adds the specified [Permissions][net.dv8tion.jda.api.Permission] to the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * Permissions may only include already present Permissions for the currently logged in account.
     * <br></br>You are unable to give permissions you don't have!
     *
     * @param  perms
     * The permission to give to the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see .setPermissions
     * @see .setPermissions
     * @see java.util.EnumSet EnumSet
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun givePermissions(@Nonnull perms: Collection<Permission?>?): RoleManager?

    /**
     * Revokes the specified [Permissions][net.dv8tion.jda.api.Permission] from the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * Permissions may only include already present Permissions for the currently logged in account.
     * <br></br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     * The permission to give to the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have permission to revoke one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see .setPermissions
     * @see .setPermissions
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun revokePermissions(@Nonnull vararg perms: Permission?): RoleManager? {
        Checks.notNull(perms, "Permissions")
        return revokePermissions(Arrays.asList(*perms))
    }

    /**
     * Revokes the specified [Permissions][net.dv8tion.jda.api.Permission] from the selected [Role][net.dv8tion.jda.api.entities.Role].
     *
     *
     * Permissions may only include already present Permissions for the currently logged in account.
     * <br></br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     * The permission to give to the selected [Role][net.dv8tion.jda.api.entities.Role]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have permission to revoke one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see .setPermissions
     * @see .setPermissions
     * @see java.util.EnumSet EnumSet
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun revokePermissions(@Nonnull perms: Collection<Permission?>?): RoleManager?

    companion object {
        /** Used to reset the name field  */
        const val NAME: Long = 1

        /** Used to reset the color field  */
        const val COLOR = (1 shl 1).toLong()

        /** Used to reset the permission field  */
        const val PERMISSION = (1 shl 2).toLong()

        /** Used to reset the hoisted field  */
        const val HOIST = (1 shl 3).toLong()

        /** Used to reset the mentionable field  */
        const val MENTIONABLE = (1 shl 4).toLong()

        /** Used to reset the icon field  */
        const val ICON = (1 shl 5).toLong()
    }
}
