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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleColors;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.internal.utils.Checks;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manager providing functionality to update one or more fields for a {@link Role}.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * manager.setName("Administrator")
 *        .setColor(null)
 *        .queue();
 * manager.reset(RoleManager.PERMISSION | RoleManager.NAME)
 *        .setName("Traitor")
 *        .setColor(Color.RED)
 *        .queue();
 * }
 *
 * @see net.dv8tion.jda.api.entities.Role#getManager()
 */
public interface RoleManager extends Manager<RoleManager> {
    /** Used to reset the name field */
    long NAME = 1;
    /** Used to reset the color field */
    long COLOR = 1 << 1;
    /** Used to reset the permission field */
    long PERMISSION = 1 << 2;
    /** Used to reset the hoisted field */
    long HOIST = 1 << 3;
    /** Used to reset the mentionable field */
    long MENTIONABLE = 1 << 4;
    /** Used to reset the icon field */
    long ICON = 1 << 5;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(RoleManager.COLOR | RoleManager.NAME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #COLOR}</li>
     *     <li>{@link #PERMISSION}</li>
     *     <li>{@link #HOIST}</li>
     *     <li>{@link #MENTIONABLE}</li>
     *     <li>{@link #ICON}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    RoleManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(RoleManager.COLOR, RoleManager.NAME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #COLOR}</li>
     *     <li>{@link #PERMISSION}</li>
     *     <li>{@link #HOIST}</li>
     *     <li>{@link #MENTIONABLE}</li>
     *     <li>{@link #ICON}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @Override
    @CheckReturnValue
    RoleManager reset(@Nonnull long... fields);

    /**
     * The target {@link Role} for this
     * manager
     *
     * @return The target Role
     */
    @Nonnull
    Role getRole();

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} this Manager's
     * {@link Role} is in.
     * <br>This is logically the same as calling {@code getRole().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    default Guild getGuild() {
        return getRole().getGuild();
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link Role}.
     *
     * <p>A role name <b>must not</b> be {@code null} nor less than 1 character or more than 100 characters long!
     *
     * @param  name
     *         The new name for the selected {@link Role}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-100 characters long
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setName(@Nonnull String name);

    /**
     * Sets the {@link net.dv8tion.jda.api.Permission Permissions} of the selected {@link Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The new raw permission value for the selected {@link Role}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setPermissions(long perms);

    /**
     * Sets the {@link net.dv8tion.jda.api.Permission Permissions} of the selected {@link Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     *         The new permission for the selected {@link Role}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(long)
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    default RoleManager setPermissions(@Nonnull Permission... permissions) {
        Checks.notNull(permissions, "Permissions");
        return setPermissions(Arrays.asList(permissions));
    }

    /**
     * Sets the {@link net.dv8tion.jda.api.Permission Permissions} of the selected {@link Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     *         The new permission for the selected {@link Role}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Permission...)
     * @see    #setPermissions(long)
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default RoleManager setPermissions(@Nonnull Collection<Permission> permissions) {
        Checks.noneNull(permissions, "Permissions");
        return setPermissions(Permission.getRaw(permissions));
    }

    /**
     * Sets the {@link java.awt.Color Color} of the selected {@link Role}.
     *
     * @param  color
     *         The new color for the selected {@link Role}
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default RoleManager setColor(@Nullable Color color) {
        return setColor(color == null ? Role.DEFAULT_COLOR_RAW : color.getRGB());
    }

    /**
     * Sets the rgb color of the selected {@link Role}.
     *
     * <p>This accepts colors from the range {@code 0x000} to {@code 0xFFFFFF}.
     * The provided value will be ranged using {@code rgb & 0xFFFFFF}.
     *
     * @param  rgb
     *         The new color for the selected {@link Role}
     *
     * @return RoleManager for chaining convenience
     *
     * @see    Role#DEFAULT_COLOR_RAW
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setColor(int rgb);

    /**
     * Sets the three color components of this role.
     *
     * <p>It is recommended to use {@link #setColor(int)}, {@link #setGradientColors(int, int)}, or {@link #useHolographicStyle()} for setting colors instead,
     * this method is primarily intended for copying colors from an existing role object with {@link Role#getColors()}.
     *
     * @param  colors
     *         The role colors or {@code null} to use the default white/black
     *
     * @return RoleManager for chaining convenience
     *
     * @see    Role#getColors()
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setColors(@Nullable RoleColors colors);

    /**
     * Sets the primary and secondary color for the new role color gradient.
     *
     * <p>Use {@link #setColor(Color)} or {@link #useHolographicStyle()} to use a single color or holographic style instead.
     *
     * @param  primary
     *         The primary color for gradient
     * @param  secondary
     *         The secondary color for gradient
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default RoleManager setGradientColors(@Nonnull Color primary, @Nonnull Color secondary) {
        Checks.notNull(primary, "Primary");
        Checks.notNull(secondary, "Secondary");
        return setGradientColors(primary.getRGB(), secondary.getRGB());
    }

    /**
     * Sets the primary and secondary color for the new role color gradient.
     *
     * <p>This accepts colors from the range {@code 0x000} to {@code 0xFFFFFF}.
     * The provided value will be ranged using {@code rgb & 0xFFFFFF}.
     *
     * <p>Use {@link #setColor(int)} or {@link #useHolographicStyle()} to use a single color or holographic style instead.
     *
     * @param  primaryRgb
     *         The primary color for gradient
     * @param  secondaryRgb
     *         The secondary color for gradient
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setGradientColors(int primaryRgb, int secondaryRgb);

    /**
     * Sets the colors of this role to {@link RoleColors#DEFAULT_HOLOGRAPHIC}.
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleManager useHolographicStyle();

    /**
     * Sets the <b><u>hoist state</u></b> of the selected {@link Role}.
     *
     * @param  hoisted
     *         Whether the selected {@link Role} should be hoisted
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setHoisted(boolean hoisted);

    /**
     * Sets the <b><u>mentionable state</u></b> of the selected {@link Role}.
     *
     * @param  mentionable
     *         Whether the selected {@link Role} should be mentionable
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setMentionable(boolean mentionable);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Icon Icon} of this {@link Role}.
     *
     * @param  icon
     *         The new icon for this {@link Role}
     *         or {@code null} to reset
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setIcon(@Nullable Icon icon);

    /**
     * Sets the Unicode Emoji of this {@link Role} instead of a custom image.
     *
     * @param  emoji
     *         The new Unicode Emoji for this {@link Role}
     *         or {@code null} to reset
     *
     * @return RoleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    RoleManager setIcon(@Nullable String emoji);

    /**
     * Sets the Unicode Emoji of this {@link Role} instead of a custom image.
     *
     * @param  emoji
     *         The new Unicode Emoji for this {@link Role}
     *         or {@code null} to reset
     *
     * @return RoleManager for chaining convenience
     *
     * @see    net.dv8tion.jda.api.entities.emoji.Emoji#fromUnicode(String) Emoji.fromUnicode(String)
     * @see    UnicodeEmoji
     */
    @Nonnull
    @CheckReturnValue
    default RoleManager setIcon(@Nullable UnicodeEmoji emoji) {
        return setIcon(emoji == null ? null : emoji.getFormatted());
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.api.Permission Permissions} to the selected {@link Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link Role}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    default RoleManager givePermissions(@Nonnull Permission... perms) {
        Checks.notNull(perms, "Permissions");
        return givePermissions(Arrays.asList(perms));
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.api.Permission Permissions} to the selected {@link Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link Role}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @Nonnull
    @CheckReturnValue
    RoleManager givePermissions(@Nonnull Collection<Permission> perms);

    /**
     * Revokes the specified {@link net.dv8tion.jda.api.Permission Permissions} from the selected {@link Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link Role}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to revoke one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    default RoleManager revokePermissions(@Nonnull Permission... perms) {
        Checks.notNull(perms, "Permissions");
        return revokePermissions(Arrays.asList(perms));
    }

    /**
     * Revokes the specified {@link net.dv8tion.jda.api.Permission Permissions} from the selected {@link Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link Role}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to revoke one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @Nonnull
    @CheckReturnValue
    RoleManager revokePermissions(@Nonnull Collection<Permission> perms);
}
