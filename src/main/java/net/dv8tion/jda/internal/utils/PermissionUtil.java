/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
        Checks.notNull(issuer, "Issuer Member");
        Checks.notNull(target, "Target Member");

        Guild guild = issuer.getGuild();
        if (!guild.equals(target.getGuild()))
            throw new IllegalArgumentException("Provided members must both be Member objects of the same Guild!");
        if(issuer.isOwner())
            return true;
        if(target.isOwner())
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
        Checks.notNull(issuer, "Issuer Member");
        Checks.notNull(target, "Target Role");

        Guild guild = issuer.getGuild();
        if (!guild.equals(target.getGuild()))
            throw new IllegalArgumentException("Provided Member issuer and Role target must be from the same Guild!");
        if(issuer.isOwner())
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
        Checks.notNull(issuer, "Issuer Role");
        Checks.notNull(target, "Target Role");

        if(!issuer.getGuild().equals(target.getGuild()))
            throw new IllegalArgumentException("The 2 Roles are not from same Guild!");
        return target.compareTo(issuer) < 0;
    }

    /**
     * Check whether the provided {@link net.dv8tion.jda.api.entities.Member Member} can use the specified {@link RichCustomEmoji Emoji}.
     *
     * <p>If the specified Member is not in the emoji's guild or the emoji provided is from a message this will return false.
     * Otherwise, it will check if the emoji is restricted to any roles and if that is the case if the Member has one of these.
     *
     * <br><b>Note</b>: This is not checking if the issuer owns the Guild or not.
     *
     * @param  issuer
     *         The member that tries to interact with the Emoji
     * @param  emoji
     *         The emoji that is the target interaction
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True, if the issuer can interact with the emoji
     */
    public static boolean canInteract(Member issuer, RichCustomEmoji emoji)
    {
        Checks.notNull(issuer, "Issuer Member");
        Checks.notNull(emoji,  "Target Emoji");

        if (!issuer.getGuild().equals(emoji.getGuild()))
            throw new IllegalArgumentException("The issuer and target are not in the same Guild");

        return emoji.getRoles().isEmpty() // emoji restricted to roles -> check if the issuer has them
            || CollectionUtils.containsAny(issuer.getRoles(), emoji.getRoles());
    }

    /**
     * Checks whether the specified {@link RichCustomEmoji Emoji} can be used by the provided
     * {@link net.dv8tion.jda.api.entities.User User} in the {@link MessageChannel MessageChannel}.
     *
     * @param  issuer
     *         The user that tries to interact with the emoji
     * @param  emoji
     *         The emoji that is the target interaction
     * @param  channel
     *         The MessageChannel this emoji should be interacted within
     * @param  botOverride
     *         Whether bots can use non-managed emojis in other guilds
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True, if the issuer can interact with the emoji within the specified MessageChannel
     */
    public static boolean canInteract(User issuer, RichCustomEmoji emoji, MessageChannel channel, boolean botOverride)
    {
        Checks.notNull(issuer,  "Issuer Member");
        Checks.notNull(emoji,   "Target Emoji");
        Checks.notNull(channel, "Target Channel");

        if (emoji.getGuild() == null || !emoji.getGuild().isMember(issuer))
            return false; // cannot use an emoji if you're not in its guild
        Member member = emoji.getGuild().getMemberById(issuer.getIdLong());
        if (!canInteract(member, emoji))
            return false;
        // external means it is available outside of its own guild - works for bots or if its managed
        // currently we cannot check whether other users have nitro, we assume no here
        final boolean external = emoji.isManaged() || (issuer.isBot() && botOverride);
        switch (channel.getType())
        {
            case TEXT:
                TextChannel text = (TextChannel) channel;
                member = text.getGuild().getMemberById(issuer.getIdLong());
                return emoji.getGuild().equals(text.getGuild()) // within the same guild
                    || (external && member != null && member.hasPermission(text, Permission.MESSAGE_EXT_EMOJI)); // in different guild
            default:
                return external; // In Group or Private it only needs to be external
        }
    }

    /**
     * Checks whether the specified {@link RichCustomEmoji} can be used by the provided
     * {@link net.dv8tion.jda.api.entities.User User} in the {@link MessageChannel MessageChannel}.
     *
     * @param  issuer
     *         The user that tries to interact with the emoji
     * @param  emoji
     *         The emoji that is the target interaction
     * @param  channel
     *         The MessageChannel this emoji should be interacted within
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True, if the issuer can interact with the emoji within the specified MessageChannel
     */
    public static boolean canInteract(User issuer, RichCustomEmoji emoji, MessageChannel channel)
    {
        return canInteract(issuer, emoji, channel, true);
    }

    /**
     * Checks to see if the {@link net.dv8tion.jda.api.entities.Member Member} has the specified {@link net.dv8tion.jda.api.Permission Permissions}
     * in the specified {@link net.dv8tion.jda.api.entities.Guild Guild}. This method properly deals with Owner status.
     *
     * <p><b>Note:</b> this is based on effective permissions, not literal permissions. If a member has permissions that would
     * enable them to do something without the literal permission to do it, this will still return true.
     * <br>Example: If a member has the {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR} permission, they will be able to
     * {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER} as well, even without the literal permissions.
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} whose permissions are being checked.
     * @param  permissions
     *         The {@link net.dv8tion.jda.api.Permission Permissions} being checked for.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is null
     *
     * @return True -
     *         if the {@link net.dv8tion.jda.api.entities.Member Member} effectively has the specified {@link net.dv8tion.jda.api.Permission Permissions}.
     */
    public static boolean checkPermission(Member member, Permission... permissions)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(permissions, "Permissions");

        long effectivePerms = getEffectivePermission(member);
        return isApplied(effectivePerms, Permission.ADMINISTRATOR.getRawValue())
                || isApplied(effectivePerms, Permission.getRaw(permissions));
    }

    /**
     * Checks to see if the {@link net.dv8tion.jda.api.entities.Member Member} has the specified {@link net.dv8tion.jda.api.Permission Permissions}
     * in the specified {@link IPermissionContainer GuildChannel}. This method properly deals with
     * {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} and Owner status.
     *
     * <p><b>Note:</b> this is based on effective permissions, not literal permissions. If a member has permissions that would
     * enable them to do something without the literal permission to do it, this will still return true.
     * <br>Example: If a member has the {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR} permission, they will be able to
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND} in every channel.
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} whose permissions are being checked.
     * @param  channel
     *         The {@link IPermissionContainer GuildChannel} being checked.
     * @param  permissions
     *         The {@link net.dv8tion.jda.api.Permission Permissions} being checked for.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return True -
     *         if the {@link net.dv8tion.jda.api.entities.Member Member} effectively has the specified {@link net.dv8tion.jda.api.Permission Permissions}.
     */
    public static boolean checkPermission(IPermissionContainer channel, Member member, Permission... permissions)
    {
        Checks.notNull(channel, "Channel");
        Checks.notNull(member, "Member");
        Checks.notNull(permissions, "Permissions");

        checkGuild(channel.getGuild(), member.getGuild(), "Member");

        long effectivePerms = getEffectivePermission(channel, member);
        return isApplied(effectivePerms, Permission.getRaw(permissions));
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link net.dv8tion.jda.api.entities.Member Member}
     * in this {@link net.dv8tion.jda.api.entities.Guild Guild}. This can be used in conjunction with
     * {@link net.dv8tion.jda.api.Permission#getPermissions(long) Permission.getPermissions(int)} to easily get a list of all
     * {@link net.dv8tion.jda.api.Permission Permissions} that this member has in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>This only returns the Guild-level permissions!</b>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} whose permissions are being checked.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return The {@code long} representation of the literal permissions that
     *         this {@link net.dv8tion.jda.api.entities.Member Member} has in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     */
    public static long getEffectivePermission(Member member)
    {
        Checks.notNull(member, "Member");

        if (member.isDetached())
            throw new IllegalStateException("Cannot get the effective permissions of a detached member without a channel. " +
                    "Instead, please use the Member methods while supplying a GuildChannel");

        if (member.isOwner())
            return Permission.ALL_PERMISSIONS;
        //Default to binary OR of all global permissions in this guild
        long permission = member.getGuild().getPublicRole().getPermissionsRaw();
        for (Role role : member.getRoles())
        {
            permission |= role.getPermissionsRaw();
            if (isApplied(permission, Permission.ADMINISTRATOR.getRawValue()))
                return Permission.ALL_PERMISSIONS;
        }
        // See https://discord.com/developers/docs/topics/permissions#permissions-for-timed-out-members
        if (member.isTimedOut())
            permission &= Permission.VIEW_CHANNEL.getRawValue() | Permission.MESSAGE_HISTORY.getRawValue();
        return permission;
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link net.dv8tion.jda.api.entities.Member Member}
     * in this {@link IPermissionContainer GuildChannel}. This can be used in conjunction with
     * {@link net.dv8tion.jda.api.Permission#getPermissions(long) Permission.getPermissions(long)} to easily get a list of all
     * {@link net.dv8tion.jda.api.Permission Permissions} that this member can use in this {@link IPermissionContainer GuildChannel}.
     * <br>This functions very similarly to how {@link net.dv8tion.jda.api.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()}.
     *
     * @param  channel
     *         The {@link IPermissionContainer GuildChannel} being checked.
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} whose permissions are being checked.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return The {@code long} representation of the effective permissions that this {@link net.dv8tion.jda.api.entities.Member Member}
     *         has in this {@link IPermissionContainer GuildChannel}.
     */
    public static long getEffectivePermission(GuildChannel channel, Member member)
    {
        Checks.notNull(channel, "Channel");
        Checks.notNull(member, "Member");

        Checks.check(channel.getGuild().equals(member.getGuild()), "Provided channel and provided member are not of the same guild!");

        if (member.isDetached())
            throw new IllegalStateException("Cannot get the effective permissions of a detached member. " +
                    "Instead, please use the Member methods while supplying a GuildChannel");

        if (member.isOwner())
        {
            // Owner effectively has all permissions
            return Permission.ALL_PERMISSIONS;
        }

        long permission = getEffectivePermission(member);
        final long admin = Permission.ADMINISTRATOR.getRawValue();
        if (isApplied(permission, admin))
            return Permission.ALL_PERMISSIONS;

        // MANAGE_CHANNEL allows to delete channels within a category (this is undocumented behavior)
        if (channel instanceof ICategorizableChannel) {
            ICategorizableChannel categorizableChannel = (ICategorizableChannel) channel;
            if (categorizableChannel.getParentCategory() != null && checkPermission(categorizableChannel.getParentCategory(), member, Permission.MANAGE_CHANNEL))
                permission |= Permission.MANAGE_CHANNEL.getRawValue();
        }

        AtomicLong allow = new AtomicLong(0);
        AtomicLong deny = new AtomicLong(0);
        getExplicitOverrides(channel, member, allow, deny);
        permission = apply(permission, allow.get(), deny.get());
        final long viewChannel = Permission.VIEW_CHANNEL.getRawValue();
        final long connectChannel = Permission.VOICE_CONNECT.getRawValue();

        //When the permission to view the channel or to connect to the channel is not applied it is not granted
        // This means that we have no access to this channel at all
        // See https://github.com/discord/discord-api-docs/issues/1522
        final boolean hasConnect = !channel.getType().isAudio() || isApplied(permission, connectChannel);
        final boolean hasAccess = isApplied(permission, viewChannel) && hasConnect;

        // See https://discord.com/developers/docs/topics/permissions#permissions-for-timed-out-members
        if (member.isTimedOut())
            permission &= viewChannel | Permission.MESSAGE_HISTORY.getRawValue();

        return hasAccess ? permission : 0;
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link net.dv8tion.jda.api.entities.Role Role}
     * in this {@link IPermissionContainer GuildChannel}. This can be used in conjunction with
     * {@link net.dv8tion.jda.api.Permission#getPermissions(long) Permission.getPermissions(long)} to easily get a list of all
     * {@link net.dv8tion.jda.api.Permission Permissions} that this role can use in this {@link IPermissionContainer GuildChannel}.
     *
     * @param  channel
     *         The {@link IPermissionContainer GuildChannel} in which permissions are being checked.
     * @param  role
     *         The {@link net.dv8tion.jda.api.entities.Role Role} whose permissions are being checked.
     *
     * @throws IllegalArgumentException
     *         if any of the provided parameters is {@code null}
     *         or the provided entities are not from the same guild
     *
     * @return The {@code long} representation of the effective permissions that this {@link net.dv8tion.jda.api.entities.Role Role}
     *         has in this {@link IPermissionContainer GuildChannel}
     */
    public static long getEffectivePermission(GuildChannel channel, Role role)
    {
        Checks.notNull(channel, "Channel");
        Checks.notNull(role, "Role");

        if (!channel.getGuild().equals(role.getGuild()))
            throw new IllegalArgumentException("Provided channel and role are not of the same guild!");

        long permissions = getExplicitPermission(channel, role);
        if (isApplied(permissions, Permission.ADMINISTRATOR.getRawValue()))
            return Permission.ALL_CHANNEL_PERMISSIONS;
        else if (!isApplied(permissions, Permission.VIEW_CHANNEL.getRawValue()))
            return 0;
        return permissions;
    }

    /**
     * Retrieves the explicit permissions of the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * in its hosting {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>This method does not calculate the owner in.
     *
     * <p>All permissions returned are explicitly granted to this Member via its {@link net.dv8tion.jda.api.entities.Role Roles}.
     * <br>Permissions like {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     *
     * @param  member
     *         The non-null {@link net.dv8tion.jda.api.entities.Member Member} for which to get implicit permissions
     *
     * @throws IllegalArgumentException
     *         If the specified member is {@code null}
     *
     * @return Primitive (unsigned) long value with the implicit permissions of the specified member
     *
     * @since  3.1
     */
    public static long getExplicitPermission(Member member)
    {
        Checks.notNull(member, "Member");

        if (member.isDetached())
            throw new IllegalStateException("Cannot get the explicit permissions of a detached member without a channel. " +
                    "Instead, please use the Member methods while supplying a GuildChannel");

        final Guild guild = member.getGuild();
        long permission = guild.getPublicRole().getPermissionsRaw();

        for (Role role : member.getRoles())
            permission |= role.getPermissionsRaw();

        return permission;
    }

    /**
     * Retrieves the explicit permissions of the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * in its hosting {@link net.dv8tion.jda.api.entities.Guild Guild} and specific {@link IPermissionContainer GuildChannel}.
     * <br>This method does not calculate the owner in.
     * <b>Allowed permissions override denied permissions of {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}!</b>
     *
     * <p>All permissions returned are explicitly granted to this Member via its {@link net.dv8tion.jda.api.entities.Role Roles}.
     * <br>Permissions like {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     * <p>This factor in all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} that affect this member
     * and only grants the ones that are explicitly given.
     *
     * @param  channel
     *         The target channel of which to check {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * @param  member
     *         The non-null {@link net.dv8tion.jda.api.entities.Member Member} for which to get implicit permissions
     *
     * @throws IllegalArgumentException
     *         If any of the arguments is {@code null}
     *         or the specified entities are not from the same {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @return Primitive (unsigned) long value with the implicit permissions of the specified member in the specified channel
     *
     * @since  3.1
     */
    public static long getExplicitPermission(GuildChannel channel, Member member)
    {
        return getExplicitPermission(channel, member, true);
    }

    /**
     * Retrieves the explicit permissions of the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * in its hosting {@link net.dv8tion.jda.api.entities.Guild Guild} and specific {@link IPermissionContainer GuildChannel}.
     * <br>This method does not calculate the owner in.
     * <b>Allowed permissions override denied permissions of {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}!</b>
     *
     * <p>All permissions returned are explicitly granted to this Member via its {@link net.dv8tion.jda.api.entities.Role Roles}.
     * <br>Permissions like {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     * <p>This factor in all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} that affect this member
     * and only grants the ones that are explicitly given.
     *
     * @param  channel
     *         The target channel of which to check {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * @param  member
     *         The non-null {@link net.dv8tion.jda.api.entities.Member Member} for which to get implicit permissions
     * @param  includeRoles
     *         Whether the base role permissions should be included
     *
     * @throws IllegalArgumentException
     *         If any of the arguments is {@code null}
     *         or the specified entities are not from the same {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @return Primitive (unsigned) long value with the implicit permissions of the specified member in the specified channel
     *
     * @since  3.1
     */
    public static long getExplicitPermission(GuildChannel channel, Member member, boolean includeRoles)
    {
        Checks.notNull(channel, "Channel");
        Checks.notNull(member, "Member");

        checkGuild(channel.getGuild(), member.getGuild(), "Member");

        if (member.isDetached())
            throw new IllegalStateException("Cannot get the explicit permissions of a detached member. " +
                    "Instead, please use the Member methods while supplying a GuildChannel");

        long permission = includeRoles ? getExplicitPermission(member) : 0L;

        AtomicLong allow = new AtomicLong(0);
        AtomicLong deny = new AtomicLong(0);

        // populates allow/deny
        getExplicitOverrides(channel, member, allow, deny);

        return apply(permission, allow.get(), deny.get());
    }

    /**
     * Retrieves the explicit permissions of the specified {@link net.dv8tion.jda.api.entities.Role Role}
     * in its hosting {@link net.dv8tion.jda.api.entities.Guild Guild} and specific {@link IPermissionContainer GuildChannel}.
     * <br><b>Allowed permissions override denied permissions of {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}!</b>
     *
     * <p>All permissions returned are explicitly granted to this Role.
     * <br>Permissions like {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     * <p>This factor in existing {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} if possible.
     *
     * @param  channel
     *         The target channel of which to check {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * @param  role
     *         The non-null {@link net.dv8tion.jda.api.entities.Role Role} for which to get implicit permissions
     *
     * @throws IllegalArgumentException
     *         If any of the arguments is {@code null}
     *         or the specified entities are not from the same {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @return Primitive (unsigned) long value with the implicit permissions of the specified role in the specified channel
     *
     * @since  3.1
     */
    public static long getExplicitPermission(GuildChannel channel, Role role)
    {
        return getExplicitPermission(channel, role, true);
    }

    /**
     * Retrieves the explicit permissions of the specified {@link net.dv8tion.jda.api.entities.Role Role}
     * in its hosting {@link net.dv8tion.jda.api.entities.Guild Guild} and specific {@link IPermissionContainer GuildChannel}.
     * <br><b>Allowed permissions override denied permissions of {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}!</b>
     *
     * <p>All permissions returned are explicitly granted to this Role.
     * <br>Permissions like {@link net.dv8tion.jda.api.Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     * <p>This factor in existing {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} if possible.
     *
     * @param  channel
     *         The target channel of which to check {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * @param  role
     *         The non-null {@link net.dv8tion.jda.api.entities.Role Role} for which to get implicit permissions
     * @param  includeRoles
     *         Whether the base role permissions should be included
     *
     * @throws IllegalArgumentException
     *         If any of the arguments is {@code null}
     *         or the specified entities are not from the same {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @return Primitive (unsigned) long value with the implicit permissions of the specified role in the specified channel
     *
     * @since  3.1
     */
    public static long getExplicitPermission(GuildChannel channel, Role role, boolean includeRoles)
    {
        Checks.notNull(channel, "Channel");
        Checks.notNull(role, "Role");

        // Can't know exactly what the role's permissions in that channel are, since we don't have the overrides.
        if (role.isDetached())
            throw new IllegalStateException("Cannot get the explicit permissions of a detached role");

        IPermissionContainer permsChannel = channel.getPermissionContainer();

        final Guild guild = role.getGuild();
        checkGuild(channel.getGuild(), guild, "Role");

        long permission = includeRoles ? role.getPermissionsRaw() | guild.getPublicRole().getPermissionsRaw() : 0;
        PermissionOverride override = permsChannel.getPermissionOverride(guild.getPublicRole());
        if (override != null)
            permission = apply(permission, override.getAllowedRaw(), override.getDeniedRaw());
        if (role.isPublicRole())
            return permission;

        override = permsChannel.getPermissionOverride(role);

        return override == null
            ? permission
            : apply(permission, override.getAllowedRaw(), override.getDeniedRaw());
    }

    private static void getExplicitOverrides(GuildChannel channel, Member member, AtomicLong allow, AtomicLong deny)
    {
        IPermissionContainer permsChannel = channel.getPermissionContainer();
        PermissionOverride override = permsChannel.getPermissionOverride(member.getGuild().getPublicRole());
        long allowRaw = 0;
        long denyRaw = 0;
        if (override != null)
        {
            denyRaw = override.getDeniedRaw();
            allowRaw = override.getAllowedRaw();
        }

        long allowRole = 0;
        long denyRole = 0;
        // create temporary bit containers for role cascade
        for (Role role : member.getRoles())
        {
            override = permsChannel.getPermissionOverride(role);
            if (override != null)
            {
                // important to update role cascade not others
                denyRole |= override.getDeniedRaw();
                allowRole |= override.getAllowedRaw();
            }
        }
        // Override the raw values of public role then apply role cascade
        allowRaw = (allowRaw & ~denyRole) | allowRole;
        denyRaw = (denyRaw & ~allowRole) | denyRole;

        override = permsChannel.getPermissionOverride(member);
        if (override != null)
        {
            // finally override the role cascade with member overrides
            final long oDeny = override.getDeniedRaw();
            final long oAllow = override.getAllowedRaw();
            allowRaw = (allowRaw & ~oDeny) | oAllow;
            denyRaw = (denyRaw & ~oAllow) | oDeny;
            // this time we need to exclude new allowed bits from old denied ones and OR the new denied bits as final overrides
        }
        // set as resulting values
        allow.set(allowRaw);
        deny.set(denyRaw);
    }

    /*
     * Check whether the specified permission is applied in the bits
     */
    private static boolean isApplied(long permissions, long perms)
    {
        return (permissions & perms) == perms;
    }

    private static long apply(long permission, long allow, long deny)
    {
        permission &= ~deny;  //Deny everything that the cascade of roles denied.
        permission |= allow;  //Allow all the things that the cascade of roles allowed
                              // The allowed bits override the denied ones!
        return permission;
    }

    private static void checkGuild(Guild o1, Guild o2, String name)
    {
        Checks.check(o1.equals(o2),
            "Specified %s is not in the same guild! (%s / %s)", name, o1, o2);
    }
}
