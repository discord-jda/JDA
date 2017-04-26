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
package net.dv8tion.jda.core;

import org.apache.http.util.Args;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the bit offsets used by Discord for Permissions.
 */
public enum Permission
{
    CREATE_INSTANT_INVITE(0, true, true, "Create Instant Invite"),
    KICK_MEMBERS(1, true, false, "Kick Members"),
    BAN_MEMBERS(2, true, false, "Ban Members"),
    ADMINISTRATOR(3, true, false, "Administrator"),
    MANAGE_CHANNEL(4, true, true, "Manage Channels"),
    MANAGE_SERVER(5, true, false, "Manage Server"),
    MESSAGE_ADD_REACTION(6, true, true, "Add Reactions"),

    MESSAGE_READ(10, true, true, "Read Messages"),
    MESSAGE_WRITE(11, true, true, "Send Messages"),
    MESSAGE_TTS(12, true, true, "Send TTS Messages"),
    MESSAGE_MANAGE(13, true, true, "Manage Messages"),
    MESSAGE_EMBED_LINKS(14, true, true, "Embed Links"),
    MESSAGE_ATTACH_FILES(15, true, true, "Attach Files"),
    MESSAGE_HISTORY(16, true, true, "Read History"),
    MESSAGE_MENTION_EVERYONE(17, true, true, "Mention Everyone"),
    MESSAGE_EXT_EMOJI(18, true, true, "Use External Emojis"),

    VOICE_CONNECT(20, true, true, "Connect"),
    VOICE_SPEAK(21, true, true, "Speak"),
    VOICE_MUTE_OTHERS(22, true, true, "Mute Members"),
    VOICE_DEAF_OTHERS(23, true, true, "Deafen Members"),
    VOICE_MOVE_OTHERS(24, true, true, "Move Members"),
    VOICE_USE_VAD(25, true, true, "Use Voice Activity"),

    NICKNAME_CHANGE(26, true, false, "Change Nickname"),
    NICKNAME_MANAGE(27, true, false, "Manage Nicknames"),

    MANAGE_ROLES(28, true, false, "Manage Roles"),
    MANAGE_PERMISSIONS(28, false, true, "Manage Permissions"),
    MANAGE_WEBHOOKS(29, true, true, "Manage Webhooks"),
    MANAGE_EMOTES(30, true, false, "Manage Emojis"),

    UNKNOWN(-1, false, false, "Unknown");

    /**
     * Represents a raw set of all permissions
     */
    public static final long ALL_PERMISSIONS = Permission.getRaw(Permission.values());

    private final int offset;
    private final boolean isGuild, isChannel;
    private final String name;

    Permission(int offset, boolean isGuild, boolean isChannel, String name)
    {
        this.offset = offset;
        this.isGuild = isGuild;
        this.isChannel = isChannel;
        this.name = name;
    }

    /**
     * The readable name as used in the Discord client.
     *
     * @return
     *      The readable name of this {@link net.dv8tion.jda.core.Permission Permission}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * The binary offset of the permission.
     * <br>For more information about Discord's offset system refer to
     * <a href="https://discordapi.readthedocs.org/en/latest/reference/channels/permissions.html#permissions-number">Discord Permission Numbers</a>.
     *
     * @return The offset that represents this {@link net.dv8tion.jda.core.Permission Permission}.
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * The value of this permission when viewed as a raw value.
     * <br>This is equivalent to: <code>1 {@literal <<} {@link #getOffset()}</code>
     *
     * @return The raw value of this specific permission.
     */
    public long getRawValue()
    {
        return 1 << getOffset();
    }

    /**
     * Returns whether or not this Permission is present at the Guild level
     * (configurable via {@link net.dv8tion.jda.core.entities.Role Roles})
     *
     * @return True if this permission is present at the Guild level.
     */
    public boolean isGuild()
    {
        return isGuild;
    }

    /**
     * Returns whether or not this Permission is present Channel level
     * (configurable via {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionsOverrides})
     *
     * @return True if this permission is present at the Channel level.
     */
    public boolean isChannel()
    {
        return isChannel;
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.Permission Permission} relating to the provided offset.
     * <br>If there is no {@link net.dv8tion.jda.core.Permission Permssions} that matches the provided
     * offset, {@link net.dv8tion.jda.core.Permission#UNKNOWN Permission.UNKNOWN} is returned.
     *
     * @param  offset The offset to match a {@link net.dv8tion.jda.core.Permission Permission} to.
     *
     * @return {@link net.dv8tion.jda.core.Permission Permission} relating to the provided offset.
     */
    public static Permission getFromOffset(int offset)
    {
        for (Permission perm : values())
        {
            if (perm.offset == offset)
                return perm;
        }
        return UNKNOWN;
    }

    /**
     * A list of all {@link net.dv8tion.jda.core.Permission Permissions} that are specified by this raw long representation of
     * permissions. The is best used with the getRaw methods in {@link net.dv8tion.jda.core.entities.Role Role},
     * {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} or {@link net.dv8tion.jda.core.utils.PermissionUtil}.
     *
     * <p>Examples:
     * <br>{@link net.dv8tion.jda.core.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()}
     * <br>{@link net.dv8tion.jda.core.utils.PermissionUtil#getEffectivePermission(net.dv8tion.jda.core.entities.Channel, net.dv8tion.jda.core.entities.Member)
     * PermissionUtil.getEffectivePermission(Channel, Member)}
     *
     * @param  permissions
     *         The raw {@code long} representation of permissions.
     *
     * @return Possibly-empty list of {@link net.dv8tion.jda.core.Permission Permissions}.
     */
    public static List<Permission> getPermissions(long permissions)
    {
        List<Permission> perms = new LinkedList<>();
        for (Permission perm : Permission.values())
        {
            if (perm.equals(Permission.UNKNOWN))
                continue;
            if(((permissions >> perm.getOffset()) & 1) == 1)
                perms.add(perm);
        }
        return perms;
    }

    /**
     * This is effectively the opposite of {@link #getPermissions(long)}, this takes 1 or more {@link net.dv8tion.jda.core.Permission Permissions}
     * and returns the raw offset {@code long} representation of the permissions.
     *
     * @param  permissions
     *         The array of permissions of which to form into the raw long representation.
     *
     * @return Unsigned long representing the provided permissions.
     */
    public static long getRaw(Permission... permissions)
    {
        long raw = 0;
        for (Permission perm : permissions)
        {
            if (perm != UNKNOWN)
                raw |= (1 << perm.getOffset());
        }

        return raw;
    }

    /**
     * This is effectively the opposite of {@link #getPermissions(long)}, this takes a Collection of {@link net.dv8tion.jda.core.Permission Permissions}
     * and returns the raw offset {@code long} representation of the permissions.
     *
     * @param  permissions
     *         The Collection of permissions of which to form into the raw long representation.
     *
     * @return Unsigned long representing the provided permissions.
     */
    public static long getRaw(Collection<Permission> permissions)
    {
        Args.notNull(permissions, "Permission Collection");

        return getRaw(permissions.toArray(new Permission[permissions.size()]));
    }
}
