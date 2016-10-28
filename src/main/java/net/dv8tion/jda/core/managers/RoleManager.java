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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.RestAction;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

public class RoleManager
{
    protected final RoleManagerUpdatable updatable;

    public RoleManager(Role role)
    {
        this.updatable = new RoleManagerUpdatable(role);
    }

    public JDA getJDA()
    {
        return updatable.getJDA();
    }

    public Guild getGuild()
    {
        return updatable.getGuild();
    }

    public Role getRole()
    {
        return updatable.getRole();
    }

    public RestAction<Void> setName(String name)
    {
        return updatable.getNameField().setValue(name).update();
    }

    public RestAction<Void> setPermissions(Long perms)
    {
        return updatable.getPermissionField().setValue(perms).update();
    }

    public RestAction<Void> setPermissions(Permission... permissions)
    {
        return setPermissions(Arrays.asList(permissions));
    }

    public RestAction<Void> setPermissions(Collection<Permission> permissions)
    {
        return updatable.getPermissionField().setPermissions(permissions).update();
    }

    public RestAction<Void> setColor(Color color)
    {
        return updatable.getColorField().setValue(color).update();
    }

    public RestAction<Void> setHoisted(Boolean hoisted)
    {
        return updatable.getHoistedField().setValue(hoisted).update();
    }

    public RestAction<Void> setMentionable(Boolean mentionable)
    {
        return updatable.getMentionableField().setValue(mentionable).update();
    }

    public RestAction<Void> givePermissions(Permission... perms)
    {
        return givePermissions(Arrays.asList(perms));
    }

    public RestAction<Void> givePermissions(Collection<Permission> perms)
    {
        return updatable.getPermissionField().givePermissions(perms).update();
    }

    public RestAction<Void> revokePermissions(Permission... perms)
    {
        return revokePermissions(Arrays.asList(perms));
    }

    public RestAction<Void> revokePermissions(Collection<Permission> perms)
    {
        return updatable.getPermissionField().revokePermissions(perms).update();
    }
}
