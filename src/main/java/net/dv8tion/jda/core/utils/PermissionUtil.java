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
package net.dv8tion.jda.core.utils;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.*;

import java.util.List;
import java.util.Map;

public class PermissionUtil
{
    /**
     * Checks if one given Member can interact with a 2nd given Member - in a permission sense (kick/ban/modify perms).
     * This only checks the Role-Position and does not check the actual permission (kick/ban/manage_role/...)
     *
     * @param issuer
     *      The member that tries to interact with 2nd member
     * @param target
     *      The member that is the target of the interaction
     * @return
     *      True, if issuer can interact with target in guild
     */
    public static boolean canInteract(Member issuer, Member target)
    {
        Guild guild = issuer.getGuild();
        if (!guild.equals(target.getGuild()))
            throw new IllegalArgumentException("Provided members must both be Member objects of the same Guild!");
        if(guild.getOwner().equals(issuer))
            return true;
        if(guild.getOwner().equals(target))
            return false;
        List<Role> issuerRoles = issuer.getRoles();
        List<Role> targetRoles = target.getRoles();
        return !issuerRoles.isEmpty() && (targetRoles.isEmpty() || canInteract(issuerRoles.get(0), targetRoles.get(0)));
    }

    /**
     * Checks if a given Member can interact with a given Role - in a permission sense (kick/ban/modify perms).
     * This only checks the Role-Position and does not check the actual permission (kick/ban/manage_role/...)
     *
     * @param issuer
     *      The member that tries to interact with the role
     * @param target
     *      The role that is the target of the interaction
     * @return
     *      True, if issuer can interact with target
     */
    public static boolean canInteract(Member issuer, Role target)
    {
        Guild guild = issuer.getGuild();
        if (!guild.equals(target.getGuild()))
            throw new IllegalArgumentException("Provided Member issuer and Role target must be from the same Guild!");
        if(guild.getOwner().equals(issuer))
            return true;
        List<Role> issuerRoles = issuer.getRoles();
        return !issuerRoles.isEmpty() && canInteract(issuerRoles.get(0), target);
    }
    
    /**
     * Checks if one given Role can interact with a 2nd given Role - in a permission sense (kick/ban/modify perms).
     * This only checks the Role-Position and does not check the actual permission (kick/ban/manage_role/...)
     *
     * @param issuer
     *      The role that tries to interact with 2nd role
     * @param target
     *      The role that is the target of the interaction
     * @return
     *      True, if issuer can interact with target
     */
    public static boolean canInteract(Role issuer, Role target)
    {
        if(!issuer.getGuild().equals(target.getGuild()))
            throw new IllegalArgumentException("The 2 Roles are not from same Guild!");
        return target.getPosition() < issuer.getPosition();
    }

    public static PermissionOverride getFullPermOverride()
    {
        PermissionOverrideImpl override = new PermissionOverrideImpl(null, null, null);
        int allow = 0, deny = 0;
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
     * Checks to see if the {@link net.dv8tion.jda.core.entities.Member Member} has the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * in the specified {@link net.dv8tion.jda.core.entities.Guild Guild}. This method properly deals with Owner status.
     * <p>
     * <b>Note:</b> this is based on effective permissions, not literal permissions. If a member has permissions that would
     * enable them to do something without the literal permission to do it, this will still return true.<br>
     * Example: If a member has the {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR} permission, they will be able to
     * {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER} as well, even without the literal permissions.
     *
     * @param guild
     *          The {@link net.dv8tion.jda.core.entities.Guild Guild} being checked.
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} whose permissions are being checked.
     * @param permissions
     *          The {@link net.dv8tion.jda.core.Permission Permissions} being checked for.
     * @return
     *      True - if the {@link net.dv8tion.jda.core.entities.Member Member} effectively has the specified {@link net.dv8tion.jda.core.Permission Permissions}.
     */
    public static boolean checkPermission(Guild guild, Member member, Permission... permissions)
    {
        if (!guild.equals(member.getGuild()))
            throw new IllegalArgumentException("Provided member is not in the provided guild");

        List<Role> roles = member.getRoles();
        if (guild.getOwner().equals(member) // Admin or owner? If yes: no need to iterate
                || guild.getPublicRole().hasPermission(Permission.ADMINISTRATOR)
                || roles.stream().anyMatch(role -> role.hasPermission(Permission.ADMINISTRATOR)))
            return true;
        for (Permission perm : permissions)
        {
            if (!guild.getPublicRole().hasPermission(perm)
                    && !roles.parallelStream().anyMatch(role -> role.hasPermission(perm)))
                return false;
        }
        return true;
    }

    /**
     * Checks to see if the {@link net.dv8tion.jda.core.entities.Member Member} has the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * in the specified {@link net.dv8tion.jda.core.entities.Channel Channel}. This method properly deals with
     * {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} and Owner status.
     * <p>
     * <b>Note:</b> this is based on effective permissions, not literal permissions. If a member has permissions that would
     * enable them to do something without the literal permission to do it, this will still return true.<br>
     * Example: If a member has the {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR} permission, they will be able to
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE} in every channel.
     *
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} whose permissions are being checked.
     * @param channel
     *          The {@link net.dv8tion.jda.core.entities.Channel Channel} being checked.
     * @param permissions
     *          The {@link net.dv8tion.jda.core.Permission Permissions} being checked for.
     * @return
     *      True - if the {@link net.dv8tion.jda.core.entities.Member Member} effectively has the specified {@link net.dv8tion.jda.core.Permission Permissions}.
     */
    public static boolean checkPermission(Channel channel, Member member, Permission... permissions)
    {
        GuildImpl guild = (GuildImpl) channel.getGuild();
        if (!guild.equals(member.getGuild()))
            throw new IllegalArgumentException("Provided channel and member are not from the same guild!");

        if (guild.getOwner().equals(member) // Admin or owner? If yes: no need to iterate
                || guild.getPublicRole().hasPermission(Permission.ADMINISTRATOR)
                || member.getRoles().stream().anyMatch(role -> role.hasPermission(Permission.ADMINISTRATOR)))
            return true;

        if (channel instanceof TextChannel)
        {
            for (Permission perm : permissions)
            {
                if (!checkPermission(member, perm, ((GuildImpl) channel.getGuild()),
                        ((TextChannelImpl) channel).getRoleOverrideMap(), ((TextChannelImpl) channel).getMemberOverrideMap()))
                    return false;
            }
        }
        else
        {
            for (Permission perm : permissions)
            {
                if (!checkPermission(member, perm, ((GuildImpl) channel.getGuild()),
                        ((VoiceChannelImpl) channel).getRoleOverrideMap(), ((VoiceChannelImpl) channel).getMemberOverrideMap()))
                    return false;
            }
        }
        return true;
    }

    /**
     * Check whether the provided {@link net.dv8tion.jda.core.entities.Member Member} can use the specified {@link net.dv8tion.jda.core.entities.Emote Emote}.<p>
     * If the specified Member is not in the emote's guild or the emote provided is fake this will return false.
     * Otherwise it will check if the emote is restricted to any roles and if that is the case if the Member has one of these.
     * <br><b>Note</b>: This is not checking if the issuer owns the Guild or not.
     *
     * @param issuer
     *      The member that tries to interact with the Emote
     * @param emote
     *      The emote that is the target interaction
     * @return
     *      True, if the issuer can interact with the emote
     */
    public static boolean canInteract(Member issuer, Emote emote)
    {
        return !emote.isFake() // Fake emote -> can't use
                && !(!emote.getRoles().isEmpty() // Emote restricted to roles -> check if the issuer has them
                && !issuer.getRoles().parallelStream().anyMatch(r -> emote.getRoles().contains(r)));
    }

    /**
     * Checks whether the specified {@link net.dv8tion.jda.core.entities.Emote Emote} can be used by the provided
     * {@link net.dv8tion.jda.core.entities.Member Member} in the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.<p>
     *
     * @param issuer
     *      The member that tries to interact with the Emote
     * @param emote
     *      The emote that is the target interaction
     * @param channel
     *      The MessageChannel this emote should be interacted within
     * @return
     *      True, if the issuer can interact with the emote within the specified MessageChannel
     */
    public static boolean canInteract(Member issuer, Emote emote, MessageChannel channel)
    {
        return canInteract(issuer, emote) &&
                (channel instanceof TextChannel
                        ? emote.getGuild().equals(((TextChannel) channel).getGuild())
                        || (emote.isManaged() && checkPermission((TextChannel) channel, issuer, Permission.MESSAGE_EXT_EMOJI))
                        : emote.isManaged());
    }


    /**
     * Gets the <code>int</code> representation of the effective permissions allowed for this {@link net.dv8tion.jda.core.entities.Member Member}
     * in this {@link net.dv8tion.jda.core.entities.Guild Guild}. This can be used in conjunction with
     * {@link net.dv8tion.jda.core.Permission#getPermissions(int) Permission.getPermissions(int)} to easily get a list of all
     * {@link net.dv8tion.jda.core.Permission Permissions} that this member has in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <p>
     * <b>This only returns the Guild-level permissions!</b>
     *
     * @param guild
     *          The {@link net.dv8tion.jda.core.entities.Guild Guild} being checked.
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} whose permissions are being checked.
     * @return
     *      The <code>int</code> representation of the literal permissions that this {@link net.dv8tion.jda.core.entities.Member Member} has in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     */
    public static int getEffectivePermission(Guild guild, Member member)
    {
        if (!member.getGuild().equals(guild))
            throw new IllegalArgumentException("Provided member is not in the provided guild!");
        //Default to binary OR of all global permissions in this guild
        int permission = guild.getPublicRole().getPermissionsRaw();
        for (Role role : member.getRoles())
        {
            permission = permission | role.getPermissionsRaw();
        }
        return permission;
    }

    /**
     * Gets the <code>int</code> representation of the effective permissions allowed for this {@link net.dv8tion.jda.core.entities.Member Member}
     * in this {@link net.dv8tion.jda.core.entities.Channel Channel}. This can be used in conjunction with
     * {@link net.dv8tion.jda.core.Permission#getPermissions(int) Permission.getPermissions(int)} to easily get a list of all
     * {@link net.dv8tion.jda.core.Permission Permissions} that this member can use in this {@link net.dv8tion.jda.core.entities.Channel Channel}.<br>
     * This functions very similarly to how {@link net.dv8tion.jda.core.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()}.
     *
     * @param channel
     *          The {@link net.dv8tion.jda.core.entities.Channel Channel} being checked.
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} whose permissions are being checked.
     * @return
     *      The <code>int</code> representation of the literal permissions that this {@link net.dv8tion.jda.core.entities.Member Member} has in this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    public static int getEffectivePermission(Channel channel, Member member)
    {
        if (!channel.getGuild().equals(member.getGuild()))
            throw new IllegalArgumentException("Provided channel and provided member are not of the same guild!");

        if (channel instanceof TextChannel)
        {
            return getEffectivePermission(member, ((GuildImpl) channel.getGuild()),
                    ((TextChannelImpl) channel).getRoleOverrideMap(), ((TextChannelImpl) channel).getMemberOverrideMap());
        }
        else
        {
            return getEffectivePermission(member, ((GuildImpl) channel.getGuild()),
                    ((VoiceChannelImpl) channel).getRoleOverrideMap(), ((VoiceChannelImpl) channel).getMemberOverrideMap());
        }
    }

    private static boolean checkPermission(Member member, Permission perm, GuildImpl guild, Map<Role, PermissionOverride> roleOverrides, Map<Member, PermissionOverride> memberOverrides)
    {
        //--Do we have all permissions possible? (Owner or member has ADMINISTRATOR permission)
        //--If we have all permissions possible, then we will be able to see this room.
        //WE DO NOT WANT TO CHECK THIS FOR CHANNELS, AS CHANNELS CAN OVERRIDE MANAGE_PERMISSIONS
//        if (checkPermission(member, Permission.ADMINISTRATOR, guild))
//            return true;

        //BUT: WE DO WANT TO CHECK IF HE IS OWNER
        if (guild.getOwner().equals(member))
            return true;

        int effectivePerms = getEffectivePermission(member, guild, roleOverrides, memberOverrides);
        return ((effectivePerms & (1 << Permission.ADMINISTRATOR.getOffset())) | (effectivePerms & (1 << perm.getOffset()))) > 0;
    }

    private static int getEffectivePermission(Member member, GuildImpl guild, Map<Role, PermissionOverride> roleOverrides, Map<Member, PermissionOverride> memberOverrides)
    {
        int permission = getEffectivePermission(guild, member);

        //override with channel-specific overrides of @everyone
        PermissionOverride override = roleOverrides.get(guild.getPublicRole());
        if (override != null)
        {
            permission = apply(permission, override.getAllowedRaw(), override.getDeniedRaw());
        }

        //handle role-overrides of this member in this channel (allow > disallow)
        int allow = -1;
        int deny = -1;
        for (Role role : member.getRoles())
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

        //handle member-specific overrides
        PermissionOverride memberOverride = memberOverrides.get(member);
        if (memberOverride != null)
        {
            permission = apply(permission, memberOverride.getAllowedRaw(), memberOverride.getDeniedRaw());
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
