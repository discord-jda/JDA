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
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.internal.utils.*
import java.awt.Color
import java.util.concurrent.*
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Extension of [RestAction][net.dv8tion.jda.api.requests.RestAction] specifically
 * designed to create a [Role][net.dv8tion.jda.api.entities.Role].
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 *
 * @see net.dv8tion.jda.api.entities.Guild
 *
 * @see net.dv8tion.jda.api.entities.Guild.createRole
 * @see Role.createCopy
 * @see Role.createCopy
 */
interface RoleAction : AuditableRestAction<Role?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): RoleAction?
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): RoleAction?
    @Nonnull
    override fun deadline(timestamp: Long): RoleAction?

    @get:Nonnull
    val guild: Guild?

    /**
     * Sets the name for new role (optional)
     *
     * @param  name
     * The name for the new role, null to use default name
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided name is longer than 100 characters
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(name: String?): RoleAction?

    /**
     * Sets whether or not the new role should be hoisted
     *
     * @param  hoisted
     * Whether the new role should be hoisted (grouped). Default is `false`
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setHoisted(hoisted: Boolean?): RoleAction?

    /**
     * Sets whether the new role should be mentionable by members of
     * the parent [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  mentionable
     * Whether the new role should be mentionable. Default is `false`
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setMentionable(mentionable: Boolean?): RoleAction?

    /**
     * Sets the color which the new role should be displayed with.
     *
     * @param  color
     * An [Color][java.awt.Color] for the new role, null to use default white/black
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setColor(color: Color?): RoleAction? {
        return this.setColor(color?.rgb)
    }

    /**
     * Sets the Color for the new role.
     * This accepts colors from the range `0x000` to `0xFFFFFF`.
     * The provided value will be ranged using `rbg & 0xFFFFFF`
     *
     * @param  rgb
     * The color for the new role in integer form, `null` to use default white/black
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setColor(rgb: Int?): RoleAction?

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or [admin][net.dv8tion.jda.api.Permission.ADMINISTRATOR] of the parent [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  permissions
     * The varargs [Permissions][net.dv8tion.jda.api.Permission] for the new role
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not hold one of the specified permissions
     * @throws IllegalArgumentException
     * If any of the provided permissions is `null`
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun setPermissions(vararg permissions: Permission?): RoleAction? {
        if (permissions != null) Checks.noneNull(permissions, "Permissions")
        return setPermissions(if (permissions == null) null else Permission.getRaw(*permissions))
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or [admin][net.dv8tion.jda.api.Permission.ADMINISTRATOR] of the parent [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  permissions
     * A [Collection][java.util.Collection] of [Permissions][net.dv8tion.jda.api.Permission] for the new role
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not hold one of the specified permissions
     * @throws IllegalArgumentException
     * If any of the provided permissions is `null`
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see net.dv8tion.jda.api.Permission.getRaw
     * @see java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    fun setPermissions(permissions: Collection<Permission?>?): RoleAction? {
        if (permissions != null) Checks.noneNull(permissions, "Permissions")
        return setPermissions(if (permissions == null) null else Permission.getRaw(permissions))
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or [admin][net.dv8tion.jda.api.Permission.ADMINISTRATOR] of the parent [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     * @param  permissions
     * The raw [Permissions][net.dv8tion.jda.api.Permission] value for the new role.
     * To retrieve this use [net.dv8tion.jda.api.Permission.getRawValue]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not hold one of the specified permissions
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see net.dv8tion.jda.api.Permission.getRawValue
     * @see net.dv8tion.jda.api.Permission.getRaw
     * @see net.dv8tion.jda.api.Permission.getRaw
     */
    @Nonnull
    @CheckReturnValue
    fun setPermissions(permissions: Long?): RoleAction?

    /**
     * Sets the [Icon][net.dv8tion.jda.api.entities.Icon] of this [Role][net.dv8tion.jda.api.entities.Role].
     * This icon will be displayed next to the role's name in the members tab and in chat.
     *
     * @param  icon
     * The new icon for this [Role][net.dv8tion.jda.api.entities.Role]
     * or `null` to reset
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setIcon(icon: Icon?): RoleAction?

    /**
     * Sets the Unicode Emoji of this [Role][net.dv8tion.jda.api.entities.Role] instead of a custom image.
     * This emoji will be displayed next to the role's name in the members tab and in chat.
     *
     * @param  emoji
     * The new Unicode emoji for this [Role][net.dv8tion.jda.api.entities.Role]
     * or `null` to reset
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setIcon(emoji: String?): RoleAction?

    /**
     * Sets the Unicode Emoji of this [Role][net.dv8tion.jda.api.entities.Role] instead of a custom image.
     * This emoji will be displayed next to the role's name in the members tab and in chat.
     *
     * @param  emoji
     * The new Unicode emoji for this [Role][net.dv8tion.jda.api.entities.Role]
     * or `null` to reset
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode
     * @see UnicodeEmoji
     */
    @Nonnull
    @CheckReturnValue
    fun setIcon(emoji: UnicodeEmoji?): RoleAction? {
        return setIcon(emoji?.formatted)
    }
}
