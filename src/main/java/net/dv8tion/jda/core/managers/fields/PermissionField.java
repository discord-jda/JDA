/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.managers.fields;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.*;
import java.util.function.Supplier;

public class PermissionField extends RoleField<Long>
{
    Set<Permission> permsGiven = new HashSet<>();
    Set<Permission> permsRevoked = new HashSet<>();

    public PermissionField(RoleManagerUpdatable manager, Supplier<Long> originalValue)
    {
        super(manager, originalValue);
    }

    @Override
    public RoleManagerUpdatable setValue(Long value)
    {
        checkValue(value);

        this.value = value;
        this.set = true;
        this.permsGiven.clear();
        this.permsRevoked.clear();

        return manager;
    }

    public RoleManagerUpdatable setPermissions(Permission... permissions)
    {
        return setPermissions(Arrays.asList(permissions));
    }

    public RoleManagerUpdatable setPermissions(Collection<Permission> permissions)
    {
        checkNull(permissions, "permissions Collection");
        permissions.forEach(p ->
        {
            checkNull(p, "Permission in the Collection");
        });

        return setValue(Permission.getRaw(permissions));
    }

    @Override
    public void checkValue(Long value)
    {
        checkNull(value, "permission value");
        Permission.getPermissions(value).forEach(p ->
        {
            checkPermission(p);
        });
    }

    public RoleManagerUpdatable givePermissions(Permission... permissions)
    {
        return givePermissions(Arrays.asList(permissions));
    }

    public RoleManagerUpdatable givePermissions(Collection<Permission> permissions)
    {
        checkNull(permissions, "Permission Collection");
        permissions.forEach(p ->
        {
            checkNull(p, "Permission in the Collection");
            checkPermission(p);
        });

        permsGiven.addAll(permissions);
        permsRevoked.removeAll(permissions);

        set = true;

        return manager;
    }

    public RoleManagerUpdatable revokePermissions(Permission... permissions)
    {
        return revokePermissions(Arrays.asList(permissions));
    }

    public RoleManagerUpdatable revokePermissions(Collection<Permission> permissions)
    {
        checkNull(permissions, "Permission Collection");
        permissions.forEach(p ->
        {
            checkNull(p, "Permission in the Collection");
            checkPermission(p);
        });

        permsRevoked.addAll(permissions);
        permsGiven.remove(permissions);

        set = true;

        return manager;
    }

    @Override
    public Long getValue()
    {
        if (!isSet())
            return null;

        long perms;
        if (value != null)  //If we have a set based value, use that
            perms = value;
        else
            perms = getOriginalValue(); //Otherwise, assume we are adding and removing from the original value;

        long given = Permission.getRaw(permsGiven);
        long removed = Permission.getRaw(permsRevoked);

        perms = perms | given;      //Apply all of the bits that were given
        perms = perms & ~removed;   //Removed all the removed perm bits

        return perms;
    }

    @Override
    public RoleManagerUpdatable reset()
    {
        super.reset();
        this.permsGiven.clear();
        this.permsRevoked.clear();

        return manager;
    }

    public List<Permission> getPermissions()
    {
        Long perms = getValue();
        return perms != null ? Permission.getPermissions(perms) : null;
    }

    public List<Permission> getOriginalPermissions()
    {
        return Permission.getPermissions(getOriginalValue());
    }

    protected void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(manager.getGuild(), manager.getGuild().getSelfMember(), perm))
            throw new PermissionException(perm, "Cannot give / revoke the permission because the logged in account does not have access to it! Permission: " + perm);
    }
}
