/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.managers.RoleManager;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.RoleAction;

import javax.annotation.CheckReturnValue;
import java.awt.Color;

/**
 * Represents a {@link net.dv8tion.jda.core.entities.Guild Guild}'s Role. Used to control permissions for Members.
 */
public interface Role extends ISnowflake, IMentionable, IPermissionHolder, Comparable<Role>
{
    /** Used to keep consistency between color values used in the API */
    int DEFAULT_COLOR_RAW = 0x1FFFFFFF; // java.awt.Color fills the MSB with FF, we just use 1F to provide better consistency

    /**
     * The hierarchical position of this {@link net.dv8tion.jda.core.entities.Role Role}
     * in the {@link net.dv8tion.jda.core.entities.Guild Guild} hierarchy. (higher value means higher role).
     * <br>The {@link net.dv8tion.jda.core.entities.Guild#getPublicRole()}'s getPosition() always return -1.
     *
     * @return The position of this {@link net.dv8tion.jda.core.entities.Role Role} as integer.
     */
    int getPosition();

    /**
     * The actual position of the {@link net.dv8tion.jda.core.entities.Role Role} as stored and given by Discord.
     * <br>Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
     * <br>The more recent a role was created, the lower it is in the hierarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * Role then this method will give you that value.
     *
     * @return The true, Discord stored, position of the {@link net.dv8tion.jda.core.entities.Role Role}.
     */
    int getPositionRaw();

    /**
     * The Name of this {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * @return Never-null String containing the name of this {@link net.dv8tion.jda.core.entities.Role Role}.
     */
    String getName();

    /**
     * Whether this {@link net.dv8tion.jda.core.entities.Role Role} is managed by an integration
     *
     * @return True, if this {@link net.dv8tion.jda.core.entities.Role Role} is managed.
     */
    boolean isManaged();

    /**
     * Whether this {@link net.dv8tion.jda.core.entities.Role Role} is hoisted
     * <br>Members in a hoisted role are displayed in their own grouping on the user-list
     *
     * @return True, if this {@link net.dv8tion.jda.core.entities.Role Role} is hoisted.
     */
    boolean isHoisted();

    /**
     * Whether or not this Role is mentionable
     *
     * @return True, if Role is mentionable.
     */
    boolean isMentionable();

    /**
     * The {@code long} representation of the literal permissions that this {@link net.dv8tion.jda.core.entities.Role Role} has.
     * <br><b>NOTE:</b> these do not necessarily represent the permissions this role will have in a {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @return Never-negative long containing offset permissions of this role.
     */
    long getPermissionsRaw();

    /**
     * The color this {@link net.dv8tion.jda.core.entities.Role Role} is displayed in.
     *
     * @return Color value of Role-color
     *
     * @see    #getColorRaw()
     */
    Color getColor();

    /**
     * The raw color RGB value used for this role
     * <br>Defaults to {@link #DEFAULT_COLOR_RAW} if this role has no set color
     *
     * @return The raw RGB color value or default
     */
    int getColorRaw();

    /**
     * Whether this role is the @everyone role for its {@link net.dv8tion.jda.core.entities.Guild Guild},
     * which is assigned to everyone who joins the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return True, if and only if this {@link net.dv8tion.jda.core.entities.Role Role} is the public role
     * for the {@link net.dv8tion.jda.core.entities.Guild Guild} provided by {@link #getGuild()}.
     *
     * @see net.dv8tion.jda.core.entities.Guild#getPublicRole()
     */
    boolean isPublicRole();

    /**
     * Whether this Role can interact with the specified Role.
     * (move/manage/etc.)
     *
     * @param  role
     *         The not-null role to compare to
     *
     * @throws IllegalArgumentException
     *         if the provided Role is null or not from the same {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return True, if this role can interact with the specified role
     *
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Role, Role)
     */
    boolean canInteract(Role role);

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} this Role exists in
     *
     * @return the Guild containing this Role
     */
    Guild getGuild();

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Role Role} in the specified {@link net.dv8tion.jda.core.entities.Guild Guild}
     * with the same settings as the given {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>The position of the specified Role does not matter in this case!
     *
     * <p>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     * and all {@link net.dv8tion.jda.core.Permission Permissions} the given {@link net.dv8tion.jda.core.entities.Role Role} has.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @param  guild
     *         The {@link net.dv8tion.jda.core.entities.Role Role} that should be copied
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES} Permission and every Permission the provided Role has
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws java.lang.IllegalArgumentException
     *         If the specified guild is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.RoleAction RoleAction}
     *         <br>RoleAction with already copied values from the specified {@link net.dv8tion.jda.core.entities.Role Role}
     */
    @CheckReturnValue
    RoleAction createCopy(Guild guild);

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Role Role} in this {@link net.dv8tion.jda.core.entities.Guild Guild}
     * with the same settings as the given {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>The position of the specified Role does not matter in this case!
     *
     * <p>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     * and all {@link net.dv8tion.jda.core.Permission Permissions} the given {@link net.dv8tion.jda.core.entities.Role Role} has.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES} Permission and every Permission the provided Role has
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.RoleAction RoleAction}
     *         <br>RoleAction with already copied values from the specified {@link net.dv8tion.jda.core.entities.Role Role}
     */
    @CheckReturnValue
    default RoleAction createCopy()
    {
        return createCopy(getGuild());
    }

    /**
     * The {@link net.dv8tion.jda.core.managers.RoleManager RoleManager} for this Role.
     * In the RoleManager, you can modify all its values.
     * <br>You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not have the required position to modify this role
     *
     * @return The RoleManager of this Role
     */
    RoleManager getManager();

    /**
     * Deletes this Role.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_ROLE}
     *     <br>If the the role was already deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES} in the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If we don't have the permission to {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the role is too high in the role hierarchy to be deleted
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this Role
     *
     * @return the corresponding JDA instance
     */
    JDA getJDA();
}
