/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction} specifically
 * designed to create a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
 * for a {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 *
 * @see    net.dv8tion.jda.api.entities.GuildChannel#createPermissionOverride(Role)
 * @see    net.dv8tion.jda.api.entities.GuildChannel#createPermissionOverride(Member)
 * @see    net.dv8tion.jda.api.entities.GuildChannel#putPermissionOverride(Role)
 * @see    net.dv8tion.jda.api.entities.GuildChannel#putPermissionOverride(Member)
 */
public interface PermissionOverrideAction extends AuditableRestAction<PermissionOverride>
{
    @Override
    PermissionOverrideAction setCheck(BooleanSupplier checks);

    /**
     * The {@link GuildChannel} this will be created in
     *
     * @return The channel
     */
    GuildChannel getChannel();

    /**
     * The {@link Role} for this override
     *
     * @return The role, or null if this is a member override
     */
    Role getRole();

    /**
     * The {@link Member} for this override
     *
     * @return The member, or null if this is a role override
     */
    Member getMember();

    /**
     * Whether this override is for a {@link Member}
     *
     * @return True, if this override is for a member
     */
    boolean isMemberOverride();

    /**
     * Whether this override is for a {@link Role}
     *
     * @return True, if this override is for a role
     */
    default boolean isRoleOverride()
    {
        return !isMemberOverride();
    }

    /**
     * The {@link Guild} for this override
     *
     * @return The guild
     */
    default Guild getGuild()
    {
        return getChannel().getGuild();
    }

    /**
     * The currently set of allowed permission bits.
     * <br>This value represents all <b>granted</b> permissions
     * in the raw bitwise representation.
     *
     * <p>Use {@link #getAllowedPermissions()} to retrieve a {@link java.util.List List}
     * with {@link net.dv8tion.jda.api.Permission Permissions} for this value
     *
     * @return long value of granted permissions
     */
    long getAllow();

    /**
     * Set of {@link net.dv8tion.jda.api.Permission Permissions}
     * that would be <b>granted</b> by the PermissionOverride that is created by this action.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return set of granted {@link net.dv8tion.jda.api.Permission Permissions}
     */
    default EnumSet<Permission> getAllowedPermissions()
    {
        return Permission.getPermissions(getAllow());
    }

    /**
     * The currently set of denied permission bits.
     * <br>This value represents all <b>denied</b> permissions
     * in the raw bitwise representation.
     *
     * <p>Use {@link #getDeniedPermissions()} to retrieve a {@link java.util.List List}
     * with {@link net.dv8tion.jda.api.Permission Permissions} for this value
     *
     * @return long value of denied permissions
     */
    long getDeny();

    /**
     * Set of {@link net.dv8tion.jda.api.Permission Permissions}
     * that would be <b>denied</b> by the PermissionOverride that is created by this action.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return set of denied {@link net.dv8tion.jda.api.Permission Permissions}
     */
    default EnumSet<Permission> getDeniedPermissions()
    {
        return Permission.getPermissions(getDeny());
    }

    /**
     * The currently set of inherited permission bits.
     * <br>This value represents all permissions that are not explicitly allowed or denied
     * in their raw bitwise representation.
     * <br>Inherited Permissions are permissions that are defined by other rules
     * from maybe other PermissionOverrides or a Role.
     *
     * <p>Use {@link #getInheritedPermissions()} to retrieve a {@link java.util.List List}
     * with {@link net.dv8tion.jda.api.Permission Permissions} for this value
     *
     * @return long value of inherited permissions
     */
    long getInherited();

    /**
     * Set of {@link net.dv8tion.jda.api.Permission Permissions}
     * that would be <b>inherited</b> from other permission holders.
     * <br>Permissions returned are not explicitly granted or denied!
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return set of inherited {@link net.dv8tion.jda.api.Permission Permissions}
     *
     * @see    #getInherited()
     */
    default EnumSet<Permission> getInheritedPermissions()
    {
        return Permission.getPermissions(getInherited());
    }

    /**
     * Whether this Action will
     * create a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for a {@link net.dv8tion.jda.api.entities.Member Member} or not
     *
     * @return True, if this is targeting a Member
     *         If this is {@code false} it is targeting a {@link net.dv8tion.jda.api.entities.Role Role}. ({@link #isRole()})
     */
    boolean isMember();

    /**
     * Whether this Action will
     * create a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for a {@link net.dv8tion.jda.api.entities.Role Role} or not
     *
     * @return True, if this is targeting a Role.
     *         If this is {@code false} it is targeting a {@link net.dv8tion.jda.api.entities.Member Member}. ({@link #isMember()})
     */
    boolean isRole();

    /**
     * Sets the value of explicitly granted permissions
     * using the bitwise representation of a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br>This value can be retrieved through {@link net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permissions.getRaw(Permission...)}!
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * @param  allowBits
     *         The <b>positive</b> bits representing the granted
     *         permissions for the new PermissionOverride
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided bits are negative
     *         or higher than {@link net.dv8tion.jda.api.Permission#ALL_PERMISSIONS Permission.ALL_PERMISSIONS}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setAllow(java.util.Collection) setAllow(Collection)
     * @see    #setAllow(net.dv8tion.jda.api.Permission...) setAllow(Permission...)
     */
    @CheckReturnValue
    PermissionOverrideAction setAllow(long allowBits);

    /**
     * Sets the value of explicitly granted permissions
     * using a Collection of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>Example: {@code setAllow(EnumSet.of(Permission.MESSAGE_READ))}</p>
     *
     * @param  permissions
     *         The Collection of Permissions representing the granted
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    #setAllow(net.dv8tion.jda.api.Permission...) setAllow(Permission...)
     */
    @CheckReturnValue
    default PermissionOverrideAction setAllow(Collection<Permission> permissions)
    {
        if (permissions == null || permissions.isEmpty())
            return setAllow(0);
        Checks.noneNull(permissions, "Permissions");
        return setAllow(Permission.getRaw(permissions));
    }

    /**
     * Sets the value of explicitly granted permissions
     * using a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * @param  permissions
     *         The Permissions representing the granted
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @CheckReturnValue
    default PermissionOverrideAction setAllow(Permission... permissions)
    {
        if (permissions == null || permissions.length == 0)
            return setAllow(0);
        Checks.noneNull(permissions, "Permissions");
        return setAllow(Permission.getRaw(permissions));
    }

    /**
     * Sets the value of explicitly denied permissions
     * using the bitwise representation of a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br>This value can be retrieved through {@link net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permissions.getRaw(Permission...)}!
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * @param  denyBits
     *         The <b>positive</b> bits representing the denied
     *         permissions for the new PermissionOverride
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided bits are negative
     *         or higher than {@link net.dv8tion.jda.api.Permission#ALL_PERMISSIONS Permission.ALL_PERMISSIONS}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setDeny(java.util.Collection) setDeny(Collection)
     * @see    #setDeny(net.dv8tion.jda.api.Permission...) setDeny(Permission...)
     */
    @CheckReturnValue
    PermissionOverrideAction setDeny(long denyBits);

    /**
     * Sets the value of explicitly denied permissions
     * using a Collection of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>Example: {@code setDeny(EnumSet.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_EXT_EMOJI))}</p>
     *
     * @param  permissions
     *         The Collection of Permissions representing the denied
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    #setDeny(net.dv8tion.jda.api.Permission...) setDeny(Permission...)
     */
    @CheckReturnValue
    default PermissionOverrideAction setDeny(Collection<Permission> permissions)
    {
        if (permissions == null || permissions.isEmpty())
            return setDeny(0);
        Checks.noneNull(permissions, "Permissions");
        return setDeny(Permission.getRaw(permissions));
    }

    /**
     * Sets the value of explicitly denied permissions
     * using a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * @param  permissions
     *         The Permissions representing the denied
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @CheckReturnValue
    default PermissionOverrideAction setDeny(Permission... permissions)
    {
        if (permissions == null || permissions.length == 0)
            return setDeny(0);
        Checks.noneNull(permissions, "Permissions");
        return setDeny(Permission.getRaw(permissions));
    }

    /**
     * Combination of {@link #setAllow(long)} and {@link #setDeny(long)}
     *
     * @param  allowBits
     *         A non-negative bitwise representation
     *         of granted Permissions
     * @param  denyBits
     *         A non-negative bitwise representation
     *         of denied Permissions
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided bits are negative
     *         or higher than {@link net.dv8tion.jda.api.Permission#ALL_PERMISSIONS Permission.ALL_PERMISSIONS}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setPermissions(java.util.Collection, java.util.Collection)
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permission.getRaw(Permission...)
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection)  Permission.getRaw(Collection)
     */
    @CheckReturnValue
    PermissionOverrideAction setPermissions(long allowBits, long denyBits);

    /**
     * Combination of {@link #setAllow(java.util.Collection)} and {@link #setDeny(java.util.Collection)}
     * <br>If a passed collection is {@code null} it resets the represented value to {@code 0} - no permission specifics.
     *
     * <p>Example: {@code setPermissions(EnumSet.of(Permission.MESSAGE_READ), EnumSet.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_EXT_EMOJI))}
     *
     * @param  grantPermissions
     *         A Collection of {@link net.dv8tion.jda.api.Permission Permissions}
     *         representing all explicitly granted Permissions for the PermissionOverride
     * @param  denyPermissions
     *         A Collection of {@link net.dv8tion.jda.api.Permission Permissions}
     *         representing all explicitly denied Permissions for the PermissionOverride
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @CheckReturnValue
    default PermissionOverrideAction setPermissions(Collection<Permission> grantPermissions, Collection<Permission> denyPermissions)
    {
        return setAllow(grantPermissions).setDeny(denyPermissions);
    }
}
