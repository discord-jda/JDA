/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.utils;

import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.entities.impl.*;

import java.util.List;
import java.util.Map;

public class PermissionUtil
{
    public static boolean checkPermission(User user, Permission perm, Channel channel)
    {
        if (channel instanceof TextChannel)
        {
            return checkPermission(user, perm, ((GuildImpl) channel.getGuild()),
                    ((TextChannelImpl) channel).getRolePermissionOverridesMap(), ((TextChannelImpl) channel).getUserPermissionOverridesMap());
        }
        else
        {
            return checkPermission(user, perm, ((GuildImpl) channel.getGuild()),
                    ((VoiceChannelImpl) channel).getRolePermissionOverridesMap(), ((VoiceChannelImpl) channel).getUserPermissionOverridesMap());
        }
    }

    public static boolean checkPermission(User user, Permission perm, Guild guild)
    {
        return guild.getOwnerId().equals(user.getId())
                || guild.getPublicRole().hasPermission(Permission.MANAGE_ROLES)
                || guild.getRolesForUser(user).stream().anyMatch(role ->
                            role.hasPermission(Permission.MANAGE_ROLES)
                            || role.hasPermission(perm));
    }

    public static int getEffectivePermission(User user, Channel channel)
    {
        if (channel instanceof TextChannel)
        {
            return getEffectivePermission(user, ((GuildImpl) channel.getGuild()),
                    ((TextChannelImpl) channel).getRolePermissionOverridesMap(), ((TextChannelImpl) channel).getUserPermissionOverridesMap());
        }
        else
        {
            return getEffectivePermission(user, ((GuildImpl) channel.getGuild()),
                    ((VoiceChannelImpl) channel).getRolePermissionOverridesMap(), ((VoiceChannelImpl) channel).getUserPermissionOverridesMap());
        }
    }

    private static boolean checkPermission(User user, Permission perm, GuildImpl guild, Map<Role, PermissionOverride> roleOverrides, Map<User, PermissionOverride> userOverrides)
    {
        //Do we have all permissions possible? (Owner or user has MANAGE_ROLES permission)
        //If we have all permissions possible, then we will be able to see this room.
        if (checkPermission(user, Permission.MANAGE_ROLES, guild))
            return true;

        int effectivePerms = getEffectivePermission(user, guild, roleOverrides, userOverrides);
        return (effectivePerms & (1 << perm.getOffset())) > 0;
    }

    private static int getEffectivePermission(User user, GuildImpl guild, Map<Role, PermissionOverride> roleOverrides, Map<User, PermissionOverride> userOverrides)
    {
        //Default to binary OR of all global permissions in this guild
        int permission = ((RoleImpl) guild.getPublicRole()).getPermissions();
        List<Role> rolesOfUser = guild.getRolesForUser(user);
        for (Role role : rolesOfUser)
        {
            permission = permission | ((RoleImpl) role).getPermissions();
        }

        //override with channel-specific overrides of @everyone
        PermissionOverride override = roleOverrides.get(guild.getPublicRole());
        if (override != null)
        {
            permission = apply(permission, override.getAllowedRaw(), override.getDeniedRaw());
        }

        //handle role-overrides of this user in this channel (allow > disallow)
        int allow = -1;
        int deny = -1;
        for (Role role : rolesOfUser)
        {
            PermissionOverride po = roleOverrides.get(role);
            if (po != null) //If an override exists for this role
            {
                if (allow == -1 || deny == -1)  //If this is the first role we've encountered.
                {
                    allow = po.getAllowedRaw(); //First role, take values from this role as the base for permission
                    deny = po.getDeniedRaw();   //
                }
                else
                {
                    allow = po.getAllowedRaw() | allow;     //Give all the stuff allowed by this Role's allow

                    deny = (po.getDeniedRaw() | deny) & (~po.getAllowedRaw());  //Deny everything that this role denies.
                    // This also rewrites the previous role's denies if this role allowed those permissions.
                }
            }
        }
        if (allow != -1 && deny != -1)  //If we found atleast 1 role with overrides.
        {
            permission = apply(permission, allow, deny);
        }

        //handle user-specific overrides
        PermissionOverride userOverride = userOverrides.get(user);
        if (userOverride != null)
        {
            permission = apply(permission, userOverride.getAllowedRaw(), userOverride.getDeniedRaw());
        }
        return permission;
    }

    private static int apply(int permission, int allow, int deny)
    {
        permission = permission | allow;    //Allow all the things that the cascade of roles allowed
        permission = permission & (~deny);  //Deny everything that the cascade of roles denied.
        return permission;
    }
}
