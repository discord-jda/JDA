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

    public static PermissionOverride getFullPermOverride()
    {
        PermissionOverrideImpl override = new PermissionOverrideImpl(null, null, null);
        int allow = 0, deny=0;
        for (Permission permission : Permission.values())
        {
            if(permission != Permission.UNKNOWN)
            {
                allow = allow | (1 << permission.getOffset());
            }
        }
        return override.setAllow(allow).setDeny(deny);
    }

    /**
     * Checks to see if the {@link net.dv8tion.jda.entities.User User} has the specified {@link net.dv8tion.jda.Permission Permission}
     * in the specified {@link net.dv8tion.jda.entities.Channel Channel}. This method properly deals with
     * {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverrides} and Owner status.
     * <p>
     * <b>Note:</b> this is based on effective permissions, not literal permissions. If a user has permissions that would
     * enable them to do something without the literal permission to do it, this will still return true.<br>
     * Example: If a user has the {@link net.dv8tion.jda.Permission#MANAGE_ROLES} permission, they will be able to
     * {@link net.dv8tion.jda.Permission#MESSAGE_WRITE} in every channel.
     *
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} whose permissions are being checked.
     * @param perm
     *          The {@link net.dv8tion.jda.Permission Permission} being checked for.
     * @param channel
     *          The {@link net.dv8tion.jda.entities.Channel Channel} being checked.
     * @return
     *      True - if the {@link net.dv8tion.jda.entities.User User} effectively has the specified {@link net.dv8tion.jda.Permission Permission}.
     */
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

    /**
     * Checks to see if the {@link net.dv8tion.jda.entities.User User} has the specified {@link net.dv8tion.jda.Permission Permission}
     * in the specified {@link net.dv8tion.jda.entities.Guild Guild}. This method properly deals with Owner status.
     * <p>
     * <b>Note:</b> this is based on effective permissions, not literal permissions. If a user has permissions that would
     * enable them to do something without the literal permission to do it, this will still return true.<br>
     * Example: If a user has the {@link net.dv8tion.jda.Permission#MANAGE_ROLES} permission, they will be able to
     * {@link net.dv8tion.jda.Permission#MANAGE_SERVER} as well, even without the literal permission.
     *
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} whose permissions are being checked.
     * @param perm
     *          The {@link net.dv8tion.jda.Permission Permission} being checked for.
     * @param guild
     *          The {@link net.dv8tion.jda.entities.Guild Guild} being checked.
     * @return
     *      True - if the {@link net.dv8tion.jda.entities.User User} effectively has the specified {@link net.dv8tion.jda.Permission Permission}.
     */
    public static boolean checkPermission(User user, Permission perm, Guild guild)
    {
        return guild.getOwnerId().equals(user.getId())
                || guild.getPublicRole().hasPermission(Permission.MANAGE_ROLES)
                || guild.getRolesForUser(user).stream().anyMatch(role ->
                            role.hasPermission(Permission.MANAGE_ROLES)
                            || role.hasPermission(perm));
    }

    /**
     * Gets the <code>int</code> representation of the effective permissions allowed for this {@link net.dv8tion.jda.entities.User User}
     * in this {@link net.dv8tion.jda.entities.Channel Channel}. This can be used in conjunction with
     * {@link net.dv8tion.jda.Permission#getPermissions(int) Permission.getPermissions(int)} to easily get a list of all
     * {@link net.dv8tion.jda.Permission Permissions} that this user can use in this {@link net.dv8tion.jda.entities.Channel Channel}.<br>
     * This functions very similarly to how {@link net.dv8tion.jda.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()}.
     *
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} whose permissions are being checked.
     * @param channel
     *          The {@link net.dv8tion.jda.entities.Channel Channel} being checked.
     * @return
     *      The <code>int</code> representation of the literal permissions that this {@link net.dv8tion.jda.entities.User User} has in this {@link net.dv8tion.jda.entities.Channel Channel}.
     */
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
        int permission = ((RoleImpl) guild.getPublicRole()).getPermissionsRaw();
        List<Role> rolesOfUser = guild.getRolesForUser(user);
        for (Role role : rolesOfUser)
        {
            permission = permission | ((RoleImpl) role).getPermissionsRaw();
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

                    deny = (po.getDeniedRaw() | deny) & (~allow);  //Deny everything that this role denies.
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
