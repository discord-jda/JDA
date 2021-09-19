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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction} specifically
 * designed to create a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
 * for a {@link GuildChannel GuildChannel}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 *
 * @see    net.dv8tion.jda.api.entities.PermissionOverride#getManager()
 * @see    IPermissionContainer#upsertPermissionOverride(IPermissionHolder)
 * @see    IPermissionContainer#createPermissionOverride(IPermissionHolder)
 * @see    IPermissionContainer#putPermissionOverride(IPermissionHolder)
 */
public interface PermissionOverrideAction extends AuditableRestAction<PermissionOverride>
{
    @Nonnull
    @Override
    PermissionOverrideAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    PermissionOverrideAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    PermissionOverrideAction deadline(long timestamp);

    /**
     * Shortcut for {@code resetAllow().resetDeny()}.
     * <br>The permission override will be empty after this operation
     *
     * @return The current PermissionOverrideAction for chaining convenience
     */
    @Nonnull
    default PermissionOverrideAction reset()
    {
        return resetAllow().resetDeny();
    }

    /**
     * Resets the allowed permissions to the current original value.
     * <br>For a new override this will just be 0.
     *
     * @return The current PermissionOverrideAction for chaining convenience
     */
    @Nonnull
    PermissionOverrideAction resetAllow();

    /**
     * Resets the denied permissions to the current original value.
     * <br>For a new override this will just be 0.
     *
     * @return The current PermissionOverrideAction for chaining convenience
     */
    @Nonnull
    PermissionOverrideAction resetDeny();

    //TODO-v5: Should probably be IPermissionContainer?
    /**
     * The {@link GuildChannel} this will be created in
     *
     * @return The channel
     */
    @Nonnull
    GuildChannel getChannel();

    /**
     * The {@link Role} for this override
     *
     * @return The role, or null if this is a member override
     */
    @Nullable
    Role getRole();

    /**
     * The {@link Member} for this override
     *
     * @return The member, or null if this is a role override
     */
    @Nullable
    Member getMember();

    /**
     * The {@link Guild} for this override
     *
     * @return The guild
     */
    @Nonnull
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
    @Nonnull
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
    @Nonnull
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
    @Nonnull
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
     * <p>All newly granted permissions will be removed from the currently set denied permissions.
     * <br>{@code allow = allowBits; deny = deny & ~allowBits;}
     *
     * @param  allowBits
     *         The <b>positive</b> bits representing the granted
     *         permissions for the new PermissionOverride
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setAllow(java.util.Collection) setAllow(Collection)
     * @see    #setAllow(net.dv8tion.jda.api.Permission...) setAllow(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction setAllow(long allowBits);

    /**
     * Sets the value of explicitly granted permissions
     * using a Collection of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>Example: {@code setAllow(EnumSet.of(Permission.VIEW_CHANNEL))}</p>
     *
     * @param  permissions
     *         The Collection of Permissions representing the granted
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    #setAllow(net.dv8tion.jda.api.Permission...) setAllow(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction setAllow(@Nullable Collection<Permission> permissions)
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
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction setAllow(@Nullable Permission... permissions)
    {
        if (permissions == null || permissions.length == 0)
            return setAllow(0);
        Checks.noneNull(permissions, "Permissions");
        return setAllow(Permission.getRaw(permissions));
    }

    /**
     * Grants the specified permissions.
     * <br>This does not override already granted permissions.
     *
     * @param  allowBits
     *         The permissions to grant, in addition to already allowed permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction grant(long allowBits);

    /**
     * Grants the specified permissions.
     * <br>This does not override already granted permissions.
     *
     * @param  permissions
     *         The permissions to grant, in addition to already allowed permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     *         If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction grant(@Nonnull Collection<Permission> permissions)
    {
        return grant(Permission.getRaw(permissions));
    }

    /**
     * Grants the specified permissions.
     * <br>This does not override already granted permissions.
     *
     * @param  permissions
     *         The permissions to grant, in addition to already allowed permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     *         If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction grant(@Nonnull Permission... permissions)
    {
        return grant(Permission.getRaw(permissions));
    }


    /**
     * Sets the value of explicitly denied permissions
     * using the bitwise representation of a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br>This value can be retrieved through {@link net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permissions.getRaw(Permission...)}!
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>All newly denied permissions will be removed from the currently set allowed permissions.
     * <br>{@code deny = denyBits; allow = allow & ~denyBits;}
     *
     * @param  denyBits
     *         The <b>positive</b> bits representing the denied
     *         permissions for the new PermissionOverride
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setDeny(java.util.Collection) setDeny(Collection)
     * @see    #setDeny(net.dv8tion.jda.api.Permission...) setDeny(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction setDeny(long denyBits);

    /**
     * Sets the value of explicitly denied permissions
     * using a Collection of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>Example: {@code setDeny(EnumSet.of(Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI))}</p>
     *
     * @param  permissions
     *         The Collection of Permissions representing the denied
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    #setDeny(net.dv8tion.jda.api.Permission...) setDeny(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction setDeny(@Nullable Collection<Permission> permissions)
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
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction setDeny(@Nullable Permission... permissions)
    {
        if (permissions == null || permissions.length == 0)
            return setDeny(0);
        Checks.noneNull(permissions, "Permissions");
        return setDeny(Permission.getRaw(permissions));
    }

    /**
     * Denies the specified permissions.
     * <br>This does not override already denied permissions.
     *
     * @param  denyBits
     *         The permissions to deny, in addition to already denied permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction deny(long denyBits);

    /**
     * Denies the specified permissions.
     * <br>This does not override already denied permissions.
     *
     * @param  permissions
     *         The permissions to deny, in addition to already denied permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     *         If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction deny(@Nonnull Collection<Permission> permissions)
    {
        return deny(Permission.getRaw(permissions));
    }

    /**
     * Denies the specified permissions.
     * <br>This does not override already denied permissions.
     *
     * @param  permissions
     *         The permissions to deny, in addition to already denied permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     *         If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction deny(@Nonnull Permission... permissions)
    {
        return deny(Permission.getRaw(permissions));
    }

    /**
     * Clears the provided {@link net.dv8tion.jda.api.Permission Permissions} bits
     * from the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited from other overrides or roles.
     *
     * @param  inheritedBits
     *         The permissions to clear from the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction clear(long inheritedBits);

    /**
     * Clears the provided {@link net.dv8tion.jda.api.Permission Permissions} bits
     * from the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     *         If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction clear(@Nonnull Collection<Permission> permissions)
    {
        return clear(Permission.getRaw(permissions));
    }

    /**
     * Clears the provided {@link net.dv8tion.jda.api.Permission Permissions} bits
     * from the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     * @throws IllegalArgumentException
     *         If any provided argument is null
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction clear(@Nonnull Permission... permissions)
    {
        return clear(Permission.getRaw(permissions));
    }


    /**
     * Combination of {@link #setAllow(long)} and {@link #setDeny(long)}
     * <br>First sets the allow bits and then the deny bits.
     *
     * @param  allowBits
     *         An unsigned bitwise representation
     *         of granted Permissions
     * @param  denyBits
     *         An unsigned bitwise representation
     *         of denied Permissions
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setPermissions(java.util.Collection, java.util.Collection)
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permission.getRaw(Permission...)
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection)  Permission.getRaw(Collection)
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction setPermissions(long allowBits, long denyBits);

    /**
     * Combination of {@link #setAllow(java.util.Collection)} and {@link #setDeny(java.util.Collection)}
     * <br>First sets the granted permissions and then the denied permissions.
     * <br>If a passed collection is {@code null} it resets the represented value to {@code 0} - no permission specifics.
     *
     * <p>Example: {@code setPermissions(EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND, Permission.MESSAGE_EXT_EMOJI))}
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
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         on the channel and tries to set permissions it does not have in the channel
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction setPermissions(@Nullable Collection<Permission> grantPermissions, @Nullable Collection<Permission> denyPermissions)
    {
        return setAllow(grantPermissions).setDeny(denyPermissions);
    }
}
