/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.core.utils;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.AbstractChannelImpl;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.PermissionOverrideImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.util.Args;

import java.util.List;

public class PermissionUtil
{
    /**
     * Checks if one given Member can interact with a 2nd given Member - in a permission sense (kick/ban/modify perms).
     * This only checks the Role-Position and does not check the actual permission (kick/ban/manage_role/...)
     *
     * @param  issuer
     *         The member that tries to interact with 2nd member
     * @param  target
     *         The member that is the target of the interaction
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True, if issuer can interact with target in guild
     */
    public static boolean canInteract(Member issuer, Member target)
    {
        Args.notNull(issuer, "Issuer Member");
        Args.notNull(target, "Target Member");

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
     * @param  issuer
     *         The member that tries to interact with the role
     * @param  target
     *         The role that is the target of the interaction
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True, if issuer can interact with target
     */
    public static boolean canInteract(Member issuer, Role target)
    {
        Args.notNull(issuer, "Issuer Member");
        Args.notNull(target, "Target Role");

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
     * @param  issuer
     *         The role that tries to interact with 2nd role
     * @param  target
     *         The role that is the target of the interaction
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True, if issuer can interact with target
     */
    public static boolean canInteract(Role issuer, Role target)
    {
        Args.notNull(issuer, "Issuer Role");
        Args.notNull(target, "Target Role");

        if(!issuer.getGuild().equals(target.getGuild()))
            throw new IllegalArgumentException("The 2 Roles are not from same Guild!");
        return target.getPosition() < issuer.getPosition();
    }

    /**
     * Check whether the provided {@link net.dv8tion.jda.core.entities.Member Member} can use the specified {@link net.dv8tion.jda.core.entities.Emote Emote}.
     *
     * <p>If the specified Member is not in the emote's guild or the emote provided is fake this will return false.
     * Otherwise it will check if the emote is restricted to any roles and if that is the case if the Member has one of these.
     * <br><b>Note</b>: This is not checking if the issuer owns the Guild or not.
     *
     * @param  issuer
     *         The member that tries to interact with the Emote
     * @param  emote
     *         The emote that is the target interaction
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True, if the issuer can interact with the emote
     */
    public static boolean canInteract(Member issuer, Emote emote)
    {
        Args.notNull(issuer, "Issuer Member");
        Args.notNull(emote,  "Target Emote");

        if (!issuer.getGuild().equals(emote.getGuild()))
            throw new IllegalArgumentException("The issuer and target are not in the same Guild");
        return !emote.isFake() // Fake emote -> can't use
                && (emote.getRoles().isEmpty() // Emote restricted to roles -> check if the issuer has them
                    || CollectionUtils.containsAny(issuer.getRoles(), emote.getRoles()));
    }

    /**
     * Checks whether the specified {@link net.dv8tion.jda.core.entities.Emote Emote} can be used by the provided
     * {@link net.dv8tion.jda.core.entities.User User} in the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     *
     * @param  issuer
     *         The user that tries to interact with the Emote
     * @param  emote
     *         The emote that is the target interaction
     * @param  channel
     *         The MessageChannel this emote should be interacted within
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True, if the issuer can interact with the emote within the specified MessageChannel
     */
    public static boolean canInteract(User issuer, Emote emote, MessageChannel channel)
    {
        Args.notNull(issuer,  "Issuer Member");
        Args.notNull(emote,   "Target Emote");
        Args.notNull(channel, "Target Channel");

        if (emote.isFake() || !emote.getGuild().isMember(issuer))
            return false; // cannot use an emote if you're not in its guild
        Member member = emote.getGuild().getMemberById(issuer.getIdLong());
        if (!canInteract(member, emote))
            return false;
        switch (channel.getType())
        {
            case TEXT:
                TextChannel text = (TextChannel) channel;
                member = text.getGuild().getMemberById(issuer.getIdLong());
                return emote.getGuild().equals(text.getGuild()) // within the same guild
                    || (emote.isManaged() && checkPermission(text, member, Permission.MESSAGE_EXT_EMOJI)); // in different guild
            default:
                return emote.isManaged(); // In Group or Private it only needs to be managed
        }
    }

    @Deprecated
    public static PermissionOverride getFullPermOverride()
    {
        PermissionOverrideImpl override = new PermissionOverrideImpl(null, 0);
        long allow = 0, deny = 0;
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
     * @deprecated Use {@link #checkPermission(net.dv8tion.jda.core.entities.Member, net.dv8tion.jda.core.Permission...)} instead
     */
    @Deprecated
    public static boolean checkPermission(Guild guild, Member member, Permission... permissions)
    {
        return checkPermission(member, permissions);
    }

    /**
     * Checks to see if the {@link net.dv8tion.jda.core.entities.Member Member} has the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * in the specified {@link net.dv8tion.jda.core.entities.Guild Guild}. This method properly deals with Owner status.
     *
     * <p><b>Note:</b> this is based on effective permissions, not literal permissions. If a member has permissions that would
     * enable them to do something without the literal permission to do it, this will still return true.
     * <br>Example: If a member has the {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR} permission, they will be able to
     * {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER} as well, even without the literal permissions.
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} whose permissions are being checked.
     * @param  permissions
     *         The {@link net.dv8tion.jda.core.Permission Permissions} being checked for.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is null
     *
     * @return True -
     *         if the {@link net.dv8tion.jda.core.entities.Member Member} effectively has the specified {@link net.dv8tion.jda.core.Permission Permissions}.
     */
    public static boolean checkPermission(Member member, Permission... permissions)
    {
        Args.notNull(member, "Member");
        Args.notNull(permissions, "Permissions");

        long effectivePerms = getEffectivePermission(member);
        if (isApplied(effectivePerms, Permission.ADMINISTRATOR)) return true;
        for (Permission p : permissions)
        {
            if (!isApplied(effectivePerms, p))
                return false;
        }

        return true;
    }

    /**
     * Checks to see if the {@link net.dv8tion.jda.core.entities.Member Member} has the specified {@link net.dv8tion.jda.core.Permission Permissions}
     * in the specified {@link net.dv8tion.jda.core.entities.Channel Channel}. This method properly deals with
     * {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} and Owner status.
     *
     * <p><b>Note:</b> this is based on effective permissions, not literal permissions. If a member has permissions that would
     * enable them to do something without the literal permission to do it, this will still return true.
     * <br>Example: If a member has the {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR} permission, they will be able to
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE} in every channel.
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} whose permissions are being checked.
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.Channel Channel} being checked.
     * @param  permissions
     *         The {@link net.dv8tion.jda.core.Permission Permissions} being checked for.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True -
     *         if the {@link net.dv8tion.jda.core.entities.Member Member} effectively has the specified {@link net.dv8tion.jda.core.Permission Permissions}.
     */
    public static boolean checkPermission(Channel channel, Member member, Permission... permissions)
    {
        Args.notNull(channel, "Channel");
        Args.notNull(member, "Member");
        Args.notNull(permissions, "Permissions");

        GuildImpl guild = (GuildImpl) channel.getGuild();
        if (!guild.equals(member.getGuild()))
            throw new IllegalArgumentException("Provided channel and member are not from the same guild!");

//        if (guild.getOwner().equals(member) // Admin or owner? If yes: no need to iterate
//                || guild.getPublicRole().hasPermission(Permission.ADMINISTRATOR)
//                || member.getRoles().stream().anyMatch(role -> role.hasPermission(Permission.ADMINISTRATOR)))
//            return true; // can be removed as getEffectivePermissions calculates these cases in

        long effectivePerms = getEffectivePermission(channel, member);
        if (isApplied(effectivePerms, Permission.ADMINISTRATOR)) return true;
        for (Permission perm : permissions)
        {
            if (!isApplied(effectivePerms, perm))
                return false;
        }

        return true;
    }

    /**
     * @deprecated Use {@link #getEffectivePermission(net.dv8tion.jda.core.entities.Member)} instead
     */
    @Deprecated
    public static long getEffectivePermission(Guild guild, Member member)
    {
        return getEffectivePermission(member);
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link net.dv8tion.jda.core.entities.Member Member}
     * in this {@link net.dv8tion.jda.core.entities.Guild Guild}. This can be used in conjunction with
     * {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(int)} to easily get a list of all
     * {@link net.dv8tion.jda.core.Permission Permissions} that this member has in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p><b>This only returns the Guild-level permissions!</b>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} whose permissions are being checked.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return The {@code long} representation of the literal permissions that
     *         this {@link net.dv8tion.jda.core.entities.Member Member} has in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     */
    public static long getEffectivePermission(Member member)
    {
        Args.notNull(member, "Member");

        if (member.isOwner())
            return Permission.ALL_PERMISSIONS;
        //Default to binary OR of all global permissions in this guild
        long permission = member.getGuild().getPublicRole().getPermissionsRaw();
        for (Role role : member.getRoles())
        {
            permission |= role.getPermissionsRaw();
            if (isApplied(permission, Permission.ADMINISTRATOR))
                return Permission.ALL_PERMISSIONS;
        }

        return permission;
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link net.dv8tion.jda.core.entities.Member Member}
     * in this {@link net.dv8tion.jda.core.entities.Channel Channel}. This can be used in conjunction with
     * {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(long)} to easily get a list of all
     * {@link net.dv8tion.jda.core.Permission Permissions} that this member can use in this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     * <br>This functions very similarly to how {@link net.dv8tion.jda.core.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()}.
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.Channel Channel} being checked.
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} whose permissions are being checked.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return The {@code long} representation of the effective permissions that this {@link net.dv8tion.jda.core.entities.Member Member}
     *         has in this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    public static long getEffectivePermission(Channel channel, Member member)
    {
        Args.notNull(channel, "Channel");
        Args.notNull(member, "Member");

        if (!channel.getGuild().equals(member.getGuild()))
            throw new IllegalArgumentException("Provided channel and provided member are not of the same guild!");

        if (member.isOwner())
            // Owner effectively has all permissions
            return Permission.ALL_PERMISSIONS;

        final AbstractChannelImpl<?> abstractChannel = (AbstractChannelImpl<?>) channel;
        final Guild guild = member.getGuild();
        long permission = getEffectivePermission(member);

        //override with channel-specific overrides of @everyone
        TLongObjectMap<PermissionOverride> overrides = abstractChannel.getOverrideMap();
        PermissionOverride override = overrides.get(guild.getPublicRole().getIdLong());

        if (override != null)
        {
            permission = apply(permission, override.getAllowedRaw(), override.getDeniedRaw());
            if (isApplied(permission, Permission.ADMINISTRATOR))
                // If the public role is marked as administrator we can return full permissions here
                return Permission.ALL_PERMISSIONS;
        }

        //handle role-overrides of this member in this channel (grant > deny)
        long allow = -1;
        long deny = -1;
        for (Role role : member.getRoles())
        {
            PermissionOverride po = overrides.get(role.getIdLong());
            if (po != null)
            // If an override exists for this role
            {
                if (allow == -1 || deny == -1)
                // If this is the first role we've encountered.
                {
                    // First role, take values from this role as the base for permission
                    allow = po.getAllowedRaw();
                    deny = po.getDeniedRaw();
                }
                else
                {
                    allow |= po.getAllowedRaw();
                    // Give all the stuff allowed by this Role's allow

                    deny = (po.getDeniedRaw() | deny) & (~allow);
                    // Deny everything that this role denies.
                    // This also rewrites the previous role's denies if this role allowed those permissions.
                }
            }
        }

        if (allow != -1 && deny != -1)
            //If we found at least 1 role with overrides.
            permission = apply(permission, allow, deny);

        //handle member-specific overrides
        PermissionOverride memberOverride = overrides.get(member.getUser().getIdLong());
        if (memberOverride != null)
            permission = apply(permission, memberOverride.getAllowedRaw(), memberOverride.getDeniedRaw());

        if (isApplied(permission, Permission.ADMINISTRATOR))
            // If the public role is marked as administrator we can return full permissions here
            return Permission.ALL_PERMISSIONS;

        if (isApplied(permission, Permission.MANAGE_PERMISSIONS) || isApplied(permission, Permission.MANAGE_CHANNEL))
            // In text channels MANAGE_CHANNEL and MANAGE_PERMISSIONS grant full text/voice permissions
            permission |= Permission.ALL_TEXT_PERMISSIONS | Permission.ALL_VOICE_PERMISSIONS;

        return permission;
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link net.dv8tion.jda.core.entities.Role Role}
     * in this {@link net.dv8tion.jda.core.entities.Channel Channel}. This can be used in conjunction with
     * {@link net.dv8tion.jda.core.Permission#getPermissions(long) Permission.getPermissions(long)} to easily get a list of all
     * {@link net.dv8tion.jda.core.Permission Permissions} that this role can use in this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.Channel Channel} in which permissions are being checked.
     * @param  role
     *         The {@link net.dv8tion.jda.core.entities.Role Role} whose permissions are being checked.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return The {@code long} representation of the effective permissions that this {@link net.dv8tion.jda.core.entities.Role Role}
     *         has in this {@link net.dv8tion.jda.core.entities.Channel Channel}
     */
    public static long getEffectivePermission(Channel channel, Role role)
    {
        Args.notNull(channel, "Channel");
        Args.notNull(role, "Role");

        Guild guild = channel.getGuild();
        if (!guild.equals(role.getGuild()))
            throw new IllegalArgumentException("Provided channel and role are not of the same guild!");

        long permissions = guild.getPublicRole().getPermissionsRaw() | role.getPermissionsRaw();

        PermissionOverride publicOverride = channel.getPermissionOverride(guild.getPublicRole());
        PermissionOverride roleOverride = channel.getPermissionOverride(role);

        if (publicOverride != null)
        {
            permissions &= ~publicOverride.getDeniedRaw();
            permissions |= publicOverride.getAllowedRaw();
        }

        if (roleOverride != null)
        {
            permissions &= ~roleOverride.getDeniedRaw();
            permissions |= roleOverride.getAllowedRaw();
        }

        return permissions;
    }

    /**
     * Retrieves the implicit permissions of the specified {@link net.dv8tion.jda.core.entities.Member Member}
     * in its hosting {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>This method does not calculate the owner in.
     *
     * <p>All permissions returned are explicitly granted to this Member via its {@link net.dv8tion.jda.core.entities.Role Roles}.
     * <br>Permissions like {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     *
     * @param  member
     *         The non-null {@link net.dv8tion.jda.core.entities.Member Member} for which to get implicit permissions
     *
     * @throws IllegalArgumentException
     *         If the specified member is {@code null}
     *
     * @return Primitive (unsigned) long value with the implicit permissions of the specified member
     *
     * @since  3.1
     */
    public static long getImplicitPermission(Member member)
    {
        Args.notNull(member, "Member");
        Guild guild = member.getGuild();

        long permission = guild.getPublicRole().getPermissionsRaw();

        for (Role role : member.getRoles())
            permission |= role.getPermissionsRaw();

        return permission;
    }

    /**
     * Retrieves the implicit permissions of the specified {@link net.dv8tion.jda.core.entities.Member Member}
     * in its hosting {@link net.dv8tion.jda.core.entities.Guild Guild} and specific {@link net.dv8tion.jda.core.entities.Channel Channel}.
     * <br>This method does not calculate the owner in.
     * <b>Allowed permissions override denied permissions of {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}!</b>
     *
     * <p>All permissions returned are explicitly granted to this Member via its {@link net.dv8tion.jda.core.entities.Role Roles}.
     * <br>Permissions like {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     * <p>This factor in all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} that affect this member
     * and only grants the ones that are explicitly given.
     *
     * @param  channel
     *         The target channel of which to check {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     * @param  member
     *         The non-null {@link net.dv8tion.jda.core.entities.Member Member} for which to get implicit permissions
     *
     * @throws IllegalArgumentException
     *         If any of the arguments is {@code null}
     *         or the specified entities are not from the same {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return Primitive (unsigned) long value with the implicit permissions of the specified member in the specified channel
     *
     * @since  3.1
     */
    public static long getImplicitPermission(Channel channel, Member member)
    {
        Args.notNull(channel, "Channel");
        Args.notNull(member, "Member");
        checkGuild(channel.getGuild(), member.getGuild(), "Member");

        long permission = 0;
        long allow = -1;
        long deny = -1;

        PermissionOverride override = channel.getPermissionOverride(member.getGuild().getPublicRole());
        if (override != null)
            permission = apply(permission, override.getAllowedRaw(), override.getDeniedRaw());

        for (Role role : member.getRoles())
        {
            override = channel.getPermissionOverride(role);
            if (override == null) continue;
            if (allow < 0 || deny < 0)
            {
                allow = override.getAllowedRaw();
                deny = override.getDeniedRaw();
            }
            else
            {
                allow |= override.getAllowedRaw();
                deny = (override.getDeniedRaw() | deny) & (~allow);
            }
        }

        if (allow >= 0 && deny >= 0)
            permission = apply(permission, allow, deny);

        override = channel.getPermissionOverride(member);
        if (override != null)
            permission = apply(permission, override.getAllowedRaw(), override.getDeniedRaw());

        return permission;
    }

    /**
     * Retrieves the implicit permissions of the specified {@link net.dv8tion.jda.core.entities.Role Role}
     * in its hosting {@link net.dv8tion.jda.core.entities.Guild Guild} and specific {@link net.dv8tion.jda.core.entities.Channel Channel}.
     * <br><b>Allowed permissions override denied permissions of {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}!</b>
     *
     * <p>All permissions returned are explicitly granted to this Role.
     * <br>Permissions like {@link net.dv8tion.jda.core.Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     * <p>This factor in existing {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} if possible.
     *
     * @param  channel
     *         The target channel of which to check {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     * @param  role
     *         The non-null {@link net.dv8tion.jda.core.entities.Role Role} for which to get implicit permissions
     *
     * @throws IllegalArgumentException
     *         If any of the arguments is {@code null}
     *         or the specified entities are not from the same {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return Primitive (unsigned) long value with the implicit permissions of the specified role in the specified channel
     *
     * @since  3.1
     */
    public static long getImplicitPermission(Channel channel, Role role)
    {
        Args.notNull(channel, "Channel");
        Args.notNull(role, "Role");
        checkGuild(channel.getGuild(), role.getGuild(), "Role");

        long permission = 0;
        PermissionOverride override = channel.getPermissionOverride(role.getGuild().getPublicRole());
        if (override != null)
            permission = apply(permission, override.getAllowedRaw(), override.getDeniedRaw());
        if (role.equals(role.getGuild().getPublicRole()))
            return permission;

        override = channel.getPermissionOverride(role);

        return override == null
            ? permission
            : apply(permission, override.getAllowedRaw(), override.getDeniedRaw());
    }

    /*
     * Check whether the specified permission is applied in the bits
     */
    private static boolean isApplied(long permissions, Permission permission)
    {
        return (permissions & permission.getRawValue()) > 0;
    }

    private static long apply(long permission, long allow, long deny)
    {
        deny &= ~allow;
        permission |= allow;  //Allow all the things that the cascade of roles allowed
        permission &= ~deny;  //Deny everything that the cascade of roles denied.
        return permission;
    }

    private static void checkGuild(Guild o1, Guild o2, String name)
    {
        Args.check(o1.equals(o2),
            "Specified %s is not in the same guild! (%s / %s)", name, o1.toString(), o2.toString());
    }
}
