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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.PermissionUtil;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Manager providing functionality to update one or more fields for a {@link net.dv8tion.jda.core.entities.Role Role}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("Administrator")
 *        .setColor(null)
 *        .queue();
 * manager.reset(RoleManager.PERMISSION | RoleManager.NAME)
 *        .setName("Traitor")
 *        .setColor(Color.RED)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.core.entities.Role#getManager()
 */
public class RoleManager extends ManagerBase
{
    /** Used to reset the name field */
    public static final long NAME        = 0x1;
    /** Used to reset the color field */
    public static final long COLOR       = 0x2;
    /** Used to reset the permission field */
    public static final long PERMISSION  = 0x4;
    /** Used to reset the hoisted field */
    public static final long HOIST       = 0x8;
    /** Used to reset the mentionable field */
    public static final long MENTIONABLE = 0x10;

    protected final Role role;

    protected String name;
    protected int color;
    protected long permissions;
    protected boolean hoist;
    protected boolean mentionable;

    /**
     * Creates a new RoleManager instance
     *
     * @param role
     *        {@link net.dv8tion.jda.core.entities.Role Role} that should be modified
     */
    public RoleManager(Role role)
    {
        super(role.getJDA(), Route.Roles.MODIFY_ROLE.compile(role.getGuild().getId(), role.getId()));
        this.role = role;
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Role Role} is in.
     * <br>This is logically the same as calling {@code getRole().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return role.getGuild();
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.Role Role} for this
     * manager
     *
     * @return The target Role
     */
    public Role getRole()
    {
        return role;
    }

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
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return RoleManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public RoleManager reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & COLOR) == COLOR)
            this.color = Role.DEFAULT_COLOR_RAW;
        return this;
    }

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(RoleManager.COLOR, RoleManager.NAME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #COLOR}</li>
     *     <li>{@link #PERMISSION}</li>
     *     <li>{@link #HOIST}</li>
     *     <li>{@link #MENTIONABLE}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return RoleManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public RoleManager reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    /**
     * Resets all fields for this manager.
     *
     * @return RoleManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public RoleManager reset()
    {
        super.reset();
        this.name = null;
        this.color = Role.DEFAULT_COLOR_RAW;
        return this;
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>A role name <b>must not</b> be {@code null} nor less than 1 characters or more than 32 characters long!
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-32 characters long
     *
     * @return RoleManager for chaining convenience
     */
    @CheckReturnValue
    public RoleManager setName(String name)
    {
        Checks.notBlank(name, "Name");
        Checks.check(name.length() <= 32, "Name must be within 32 characters in length");
        this.name = name;
        set |= NAME;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Permission Permissions} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The new raw permission value for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public RoleManager setPermissions(long perms)
    {
        long selfPermissions = PermissionUtil.getEffectivePermission(getGuild().getSelfMember());
        setupPermissions();
        long missingPerms = perms;         // include permissions we want to set to
        missingPerms &= ~selfPermissions;  // exclude permissions we have
        missingPerms &= ~this.permissions; // exclude permissions the role has
        // if any permissions remain, we have an issue
        if (missingPerms != 0 && isPermissionChecksEnabled())
        {
            List<Permission> permissionList = Permission.getPermissions(missingPerms);
            if (!permissionList.isEmpty())
                throw new InsufficientPermissionException(permissionList.get(0));
        }
        this.permissions = perms;
        set |= PERMISSION;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Permission Permissions} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     *         The new permission for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(long)
     * @see    net.dv8tion.jda.core.Permission#getRaw(net.dv8tion.jda.core.Permission...) Permission.getRaw(Permission...)
     */
    @CheckReturnValue
    public RoleManager setPermissions(Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        return setPermissions(Arrays.asList(permissions));
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Permission Permissions} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     *         The new permission for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Permission...)
     * @see    #setPermissions(long)
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @CheckReturnValue
    public RoleManager setPermissions(Collection<Permission> permissions)
    {
        Checks.noneNull(permissions, "Permissions");
        return setPermissions(Permission.getRaw(permissions));
    }

    /**
     * Sets the {@link java.awt.Color Color} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * @param  color
     *         The new color for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @return RoleManager for chaining convenience
     */
    @CheckReturnValue
    public RoleManager setColor(Color color)
    {
        this.color = color == null ? Role.DEFAULT_COLOR_RAW : color.getRGB();
        set |= COLOR;
        return this;
    }

    /**
     * Sets the rgb color of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * @param  rgb
     *         The new color for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @return RoleManager for chaining convenience
     *
     * @see    Role#DEFAULT_COLOR_RAW Role.DEFAULT_COLOR_RAW
     */
    @CheckReturnValue
    public RoleManager setColor(int rgb)
    {
        this.color = rgb;
        set |= COLOR;
        return this;
    }

    /**
     * Sets the <b><u>hoist state</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * @param  hoisted
     *         Whether the selected {@link net.dv8tion.jda.core.entities.Role Role} should be hoisted
     *
     * @return RoleManager for chaining convenience
     */
    @CheckReturnValue
    public RoleManager setHoisted(boolean hoisted)
    {
        this.hoist = hoisted;
        set |= HOIST;
        return this;
    }

    /**
     * Sets the <b><u>mentionable state</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * @param  mentionable
     *         Whether the selected {@link net.dv8tion.jda.core.entities.Role Role} should be mentionable
     *
     * @return RoleManager for chaining convenience
     */
    @CheckReturnValue
    public RoleManager setMentionable(boolean mentionable)
    {
        this.mentionable = mentionable;
        set |= MENTIONABLE;
        return this;
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.core.Permission Permissions} to the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     * @see    net.dv8tion.jda.core.Permission#getRaw(net.dv8tion.jda.core.Permission...) Permission.getRaw(Permission...)
     */
    @CheckReturnValue
    public RoleManager givePermissions(Permission... perms)
    {
        Checks.notNull(perms, "Permissions");
        return givePermissions(Arrays.asList(perms));
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.core.Permission Permissions} to the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to apply one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @CheckReturnValue
    public RoleManager givePermissions(Collection<Permission> perms)
    {
        Checks.noneNull(perms, "Permissions");
        setupPermissions();
        return setPermissions(this.permissions | Permission.getRaw(perms));
    }

    /**
     * Revokes the specified {@link net.dv8tion.jda.core.Permission Permissions} from the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to revoke one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     * @see    net.dv8tion.jda.core.Permission#getRaw(net.dv8tion.jda.core.Permission...) Permission.getRaw(Permission...)
     */
    @CheckReturnValue
    public RoleManager revokePermissions(Permission... perms)
    {
        Checks.notNull(perms, "Permissions");
        return revokePermissions(Arrays.asList(perms));
    }

    /**
     * Revokes the specified {@link net.dv8tion.jda.core.Permission Permissions} from the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have permission to revoke one of the specified permissions
     *
     * @return RoleManager for chaining convenience
     *
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @CheckReturnValue
    public RoleManager revokePermissions(Collection<Permission> perms)
    {
        Checks.noneNull(perms, "Permissions");
        setupPermissions();
        return setPermissions(this.permissions & ~Permission.getRaw(perms));
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject object = new JSONObject().put("name", role.getName());
        if (shouldUpdate(NAME))
            object.put("name", name);
        if (shouldUpdate(PERMISSION))
            object.put("permissions", permissions);
        if (shouldUpdate(HOIST))
            object.put("hoist", hoist);
        if (shouldUpdate(MENTIONABLE))
            object.put("mentionable", mentionable);
        if (shouldUpdate(COLOR))
            object.put("color", color == Role.DEFAULT_COLOR_RAW ? 0 : color & 0xFFFFFF);
        reset();
        return getRequestBody(object);
    }

    @Override
    protected boolean checkPermissions()
    {
        Member selfMember = getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.MANAGE_ROLES))
            throw new InsufficientPermissionException(Permission.MANAGE_ROLES);
        if (!selfMember.canInteract(role))
            throw new HierarchyException("Cannot modify a role that is higher or equal in hierarchy");
        return super.checkPermissions();
        /*
        //we can't reliably check the permissions of the role here
        long missingRaw = permissions;
        missingRaw &= ~selfPermissions; // exclude own perms
        missingRaw &= ~role.getPermissionsRaw(); // exclude role perms
        if (missingRaw != 0)
        {
            List<Permission> missingPermissions = Permission.getPermissions(missingRaw);
            if (!missingPermissions.isEmpty())
                throw new InsufficientPermissionException(missingPermissions.get(0));
        }
        */
    }

    private void setupPermissions()
    {
        if (!shouldUpdate(PERMISSION))
            this.permissions = role.getPermissionsRaw();
    }
}
