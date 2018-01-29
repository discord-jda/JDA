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
import net.dv8tion.jda.core.requests.Requester;
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
 * Facade for a {@link net.dv8tion.jda.core.managers.RoleManagerUpdatable RoleManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class RoleManager extends ManagerBase
{
    public static final int NAME        = 0x1;
    public static final int COLOR       = 0x2;
    public static final int PERMISSION  = 0x4;
    public static final int HOIST       = 0x8;
    public static final int MENTIONABLE = 0x10;

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

    @Override
    @CheckReturnValue
    public RoleManager reset(int fields)
    {
        super.reset(fields);
        if ((fields & PERMISSION) == PERMISSION)
            permissions = 0;
        return this;
    }

    @Override
    @CheckReturnValue
    public RoleManager reset(int... fields)
    {
        super.reset(fields);
        return this;
    }

    @Override
    @CheckReturnValue
    public RoleManager reset()
    {
        super.reset();
        permissions = 0;
        return this;
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link RoleManagerUpdatable#getNameField()}
     *
     * <p>A role name <b>must not</b> be {@code null} nor less than 1 characters or more than 32 characters long!
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-32 characters long
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getNameField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
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
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#setValue(Long) RoleManagerUpdatable#getPermissionField().setValue(Long)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The new raw permission value for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public RoleManager setPermissions(long perms)
    {
        long selfPermissions = PermissionUtil.getEffectivePermission(getGuild().getSelfMember());
        long missingPerms = ~selfPermissions & perms;
        if (missingPerms != 0)
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
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#setPermissions(Permission...) RoleManagerUpdatable#getPermissionField().setPermissions(Permission...)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     *         The new permission for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(long)
     */
    @CheckReturnValue
    public RoleManager setPermissions(Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        return setPermissions(Arrays.asList(permissions));
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Permission Permissions} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#setPermissions(Collection) RoleManagerUpdatable#getPermissionField().setPermissions(Collection)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     *         The new permission for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Permission...)
     * @see    #setPermissions(long)
     */
    @CheckReturnValue
    public RoleManager setPermissions(Collection<Permission> permissions)
    {
        Checks.noneNull(permissions, "Permissions");
        return setPermissions(Permission.getRaw(permissions));
    }

    /**
     * Sets the {@link java.awt.Color Color} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link RoleManagerUpdatable#getColorField()}
     *
     * @param  color
     *         The new color for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getColorField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     */
    @CheckReturnValue
    public RoleManager setColor(Color color)
    {
        this.color = color == null ? 0 : color.getRGB();
        set |= COLOR;
        return this;
    }

    /**
     * Sets the <b><u>hoist state</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link RoleManagerUpdatable#getHoistedField()}
     *
     * @param  hoisted
     *         Whether the selected {@link net.dv8tion.jda.core.entities.Role Role} should be hoisted
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getHoistedField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
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
     * <br>Wraps {@link RoleManagerUpdatable#getMentionableField()}
     *
     * @param  mentionable
     *         Whether the selected {@link net.dv8tion.jda.core.entities.Role Role} should be mentionable
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getMentionableField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
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
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#givePermissions(Permission...) RoleManagerUpdatable#getPermissionField().givePermissions(Permission...)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public RoleManager givePermissions(Permission... perms)
    {
        Checks.notNull(perms, "Permissions");
        return givePermissions(Arrays.asList(perms));
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.core.Permission Permissions} to the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#givePermissions(Collection) RoleManagerUpdatable#getPermissionField().givePermissions(Collection)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public RoleManager givePermissions(Collection<Permission> perms)
    {
        Checks.noneNull(perms, "Permissions");
        return setPermissions(this.permissions | Permission.getRaw(perms));
    }

    /**
     * Revokes the specified {@link net.dv8tion.jda.core.Permission Permissions} from the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#revokePermissions(Permission...) RoleManagerUpdatable#getPermissionField().revokePermissions(Permission...)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to revoke one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public RoleManager revokePermissions(Permission... perms)
    {
        Checks.notNull(perms, "Permissions");
        return revokePermissions(Arrays.asList(perms));
    }

    /**
     * Revokes the specified {@link net.dv8tion.jda.core.Permission Permissions} from the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#revokePermissions(Collection) RoleManagerUpdatable#getPermissionField().revokePermissions(Collection)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to revoke one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#getPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public RoleManager revokePermissions(Collection<Permission> perms)
    {
        Checks.noneNull(perms, "Permissions");
        return setPermissions(this.permissions & ~Permission.getRaw(perms));
    }

    @Override
    protected RequestBody finalizeData()
    {
        Member selfMember = getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.MANAGE_ROLES))
            throw new InsufficientPermissionException(Permission.MANAGE_ROLES);
        if (!selfMember.canInteract(role))
            throw new HierarchyException("Cannot modify a role that is higher or equal in hierarchy");
        long selfPermissions = PermissionUtil.getEffectivePermission(selfMember);
        long missingRaw = ~selfPermissions & permissions;
        if (missingRaw != 0)
        {
            List<Permission> missingPermissions = Permission.getPermissions(missingRaw);
            if (!missingPermissions.isEmpty())
                throw new InsufficientPermissionException(missingPermissions.get(0));
        }

        JSONObject object = new JSONObject().put("name", role.getName());
        if (shouldUpdate(NAME))
            object.put("name", name);
        if (shouldUpdate(PERMISSION))
            object.put("permission", permissions);
        if (shouldUpdate(HOIST))
            object.put("hoist", hoist);
        if (shouldUpdate(MENTIONABLE))
            object.put("mentionable", mentionable);
        if (shouldUpdate(COLOR))
            object.put("color", color & 0xFFFFFF);
        reset();
        return RequestBody.create(Requester.MEDIA_TYPE_JSON, object.toString());
    }
}
