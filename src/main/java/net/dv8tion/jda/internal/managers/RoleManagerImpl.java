/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;

public class RoleManagerImpl extends ManagerBase<RoleManager> implements RoleManager
{
    protected final SnowflakeReference<Role> role;

    protected String name;
    protected int color;
    protected long permissions;
    protected boolean hoist;
    protected boolean mentionable;

    /**
     * Creates a new RoleManager instance
     *
     * @param role
     *        {@link net.dv8tion.jda.api.entities.Role Role} that should be modified
     */
    public RoleManagerImpl(Role role)
    {
        super(role.getJDA(), Route.Roles.MODIFY_ROLE.compile(role.getGuild().getId(), role.getId()));
        JDA api = role.getJDA();
        this.role = new SnowflakeReference<>(role, api::getRoleById);
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    @Nonnull
    @Override
    public Role getRole()
    {
        return role.resolve();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & COLOR) == COLOR)
            this.color = Role.DEFAULT_COLOR_RAW;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl reset()
    {
        super.reset();
        this.name = null;
        this.color = Role.DEFAULT_COLOR_RAW;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        Checks.check(name.length() <= 100, "Name must be less or equal to 100 characters in length");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setPermissions(long perms)
    {
        long selfPermissions = PermissionUtil.getEffectivePermission(getGuild().getSelfMember());
        setupPermissions();
        long missingPerms = perms;         // include permissions we want to set to
        missingPerms &= ~selfPermissions;  // exclude permissions we have
        missingPerms &= ~this.permissions; // exclude permissions the role has
        // if any permissions remain, we have an issue
        if (missingPerms != 0 && isPermissionChecksEnabled())
        {
            EnumSet<Permission> permissionList = Permission.getPermissions(missingPerms);
            if (!permissionList.isEmpty())
                throw new InsufficientPermissionException(getGuild(), permissionList.iterator().next());
        }
        this.permissions = perms;
        set |= PERMISSION;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setColor(int rgb)
    {
        this.color = rgb;
        set |= COLOR;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setHoisted(boolean hoisted)
    {
        this.hoist = hoisted;
        set |= HOIST;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setMentionable(boolean mentionable)
    {
        this.mentionable = mentionable;
        set |= MENTIONABLE;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl givePermissions(@Nonnull Collection<Permission> perms)
    {
        Checks.noneNull(perms, "Permissions");
        setupPermissions();
        return setPermissions(this.permissions | Permission.getRaw(perms));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RoleManagerImpl revokePermissions(@Nonnull Collection<Permission> perms)
    {
        Checks.noneNull(perms, "Permissions");
        setupPermissions();
        return setPermissions(this.permissions & ~Permission.getRaw(perms));
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty().put("name", getRole().getName());
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
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_ROLES);
        if (!selfMember.canInteract(getRole()))
            throw new HierarchyException("Cannot modify a role that is higher or equal in hierarchy");
        return super.checkPermissions();
    }

    private void setupPermissions()
    {
        if (!shouldUpdate(PERMISSION))
            this.permissions = getRole().getPermissionsRaw();
    }
}
