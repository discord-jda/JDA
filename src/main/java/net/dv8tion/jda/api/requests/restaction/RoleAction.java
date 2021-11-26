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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} specifically
 * designed to create a {@link net.dv8tion.jda.api.entities.Role Role}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 *
 * @see    net.dv8tion.jda.api.entities.Guild
 * @see    net.dv8tion.jda.api.entities.Guild#createRole()
 * @see    Role#createCopy()
 * @see    Role#createCopy(Guild)
 */
public interface RoleAction extends AuditableRestAction<Role>
{
    @Nonnull
    @Override
    RoleAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    RoleAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    RoleAction deadline(long timestamp);

    /**
     * The guild to create the role in
     *
     * @return The guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * Sets the name for new role (optional)
     *
     * @param  name
     *         The name for the new role, null to use default name
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is longer than 100 characters
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleAction setName(@Nullable String name);

    /**
     * Sets whether or not the new role should be hoisted
     *
     * @param  hoisted
     *         Whether the new role should be hoisted (grouped). Default is {@code false}
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleAction setHoisted(@Nullable Boolean hoisted);

    /**
     * Sets whether the new role should be mentionable by members of
     * the parent {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  mentionable
     *         Whether the new role should be mentionable. Default is {@code false}
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleAction setMentionable(@Nullable Boolean mentionable);

    /**
     * Sets the color which the new role should be displayed with.
     *
     * @param  color
     *         An {@link java.awt.Color Color} for the new role, null to use default white/black
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default RoleAction setColor(@Nullable Color color)
    {
        return this.setColor(color != null ? color.getRGB() : null);
    }

    /**
     * Sets the Color for the new role.
     * This accepts colors from the range {@code 0x000} to {@code 0xFFFFFF}.
     * The provided value will be ranged using {@code rbg & 0xFFFFFF}
     *
     * @param  rgb
     *         The color for the new role in integer form, {@code null} to use default white/black
     *
     * @return The current RoleAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleAction setColor(@Nullable Integer rgb);

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR admin} of the parent {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  permissions
     *         The varargs {@link net.dv8tion.jda.api.Permission Permissions} for the new role
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    default RoleAction setPermissions(@Nullable Permission... permissions)
    {
        if (permissions != null)
            Checks.noneNull(permissions, "Permissions");

        return setPermissions(permissions == null ? null : Permission.getRaw(permissions));
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR admin} of the parent {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  permissions
     *         A {@link java.util.Collection Collection} of {@link net.dv8tion.jda.api.Permission Permissions} for the new role
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     * @see    java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    default RoleAction setPermissions(@Nullable Collection<Permission> permissions)
    {
        if (permissions != null)
            Checks.noneNull(permissions, "Permissions");

        return setPermissions(permissions == null ? null : Permission.getRaw(permissions));
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR admin} of the parent {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @param  permissions
     *         The raw {@link net.dv8tion.jda.api.Permission Permissions} value for the new role.
     *         To retrieve this use {@link net.dv8tion.jda.api.Permission#getRawValue()}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see    net.dv8tion.jda.api.Permission#getRawValue()
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection)
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...)
     */
    @Nonnull
    @CheckReturnValue
    RoleAction setPermissions(@Nullable Long permissions);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Icon Icon} of this {@link net.dv8tion.jda.api.entities.Role Role}.
     * This icon will be displayed next to the role's name in the members tab and in chat.
     *
     * @param  icon
     *         The new icon for this {@link net.dv8tion.jda.api.entities.Role Role}
     *         or {@code null} to reset
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleAction setIcon(@Nullable Icon icon);

    /**
     * Sets the Unicode Emoji of this {@link net.dv8tion.jda.api.entities.Role Role} instead of a custom image.
     * This emoji will be displayed next to the role's name in the members tab and in chat.
     *
     * @param  emoji
     *         The new Unicode emoji for this {@link net.dv8tion.jda.api.entities.Role Role}
     *         or {@code null} to reset
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleAction setIcon(@Nullable String emoji);
}
