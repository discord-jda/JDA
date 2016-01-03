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
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.*;

import java.util.List;
import java.util.Map;

public class PermissionUtil
{
    public static boolean checkPermission(TextChannel channel, User user, Permission perm)
    {
        return checkPermission(user, perm, ((GuildImpl) channel.getGuild()),
                ((TextChannelImpl) channel).getRolePermissionOverrides(), ((TextChannelImpl) channel).getUserPermissionOverrides());
    }

    public static boolean checkPermission(VoiceChannel channel, User user, Permission perm)
    {
        return checkPermission(user, perm, ((GuildImpl) channel.getGuild()),
                ((VoiceChannelImpl) channel).getRolePermissionOverrides(), ((VoiceChannelImpl) channel).getUserPermissionOverrides());
    }

    private static boolean checkPermission(User user, Permission perm, GuildImpl guild, Map<Role, PermissionOverride> roleOverrides, Map<User, PermissionOverride> userOverrides)
    {
        //Do we have all permissions possible? (Owner or user has MANAGE_ROLES permission)
        //If we have all permissions possible, then we will be able to see this room.
        if (guild.getOwnerId().equals(user.getId())
                || guild.getPublicRole().hasPermission(Permission.MANAGE_ROLES)
                || guild.getRolesForUser(user).stream().anyMatch(role -> role.hasPermission(Permission.MANAGE_ROLES)))
        {
            return true;
        }

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
            permission = roleOverrides.get(guild.getPublicRole()).apply(permission);
        }

        //handle role-overrides of this user in this channel (allow > disallow)
        override = null;
        for (Role role : rolesOfUser)
        {
            PermissionOverride po = roleOverrides.get(role);
            override = (po == null) ? override : ((override == null) ? po : po.after(override));
        }
        if (override != null)
        {
            permission = override.apply(permission);
        }

        //handle user-specific overrides
        PermissionOverride useroverride = userOverrides.get(user);
        if (useroverride != null)
        {
            permission = useroverride.apply(permission);
        }
        return (permission & (1 << perm.getOffset())) > 0;
    }
}
