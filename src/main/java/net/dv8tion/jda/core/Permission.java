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
package net.dv8tion.jda.core;

import org.apache.http.util.Args;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public enum Permission
{
    CREATE_INSTANT_INVITE(0, true, true),
    KICK_MEMBERS(1, true, false),
    BAN_MEMBERS(2, true, false),
    ADMINISTRATOR(3, true, false),
    MANAGE_CHANNEL(4, true, true),
    MANAGE_SERVER(5, true, false),
    MESSAGE_ADD_REACTION(6, true, true),

    MESSAGE_READ(10, true, true),
    MESSAGE_WRITE(11, true, true),
    MESSAGE_TTS(12, true, true),
    MESSAGE_MANAGE(13, true, true),
    MESSAGE_EMBED_LINKS(14, true, true),
    MESSAGE_ATTACH_FILES(15, true, true),
    MESSAGE_HISTORY(16, true, true),
    MESSAGE_MENTION_EVERYONE(17, true, true),
    MESSAGE_EXT_EMOJI(18, true, true),

    VOICE_CONNECT(20, true, true),
    VOICE_SPEAK(21, true, true),
    VOICE_MUTE_OTHERS(22, true, true),
    VOICE_DEAF_OTHERS(23, true, true),
    VOICE_MOVE_OTHERS(24, true, true),
    VOICE_USE_VAD(25, true, true),

    NICKNAME_CHANGE(26, true, false),
    NICKNAME_MANAGE(27, true, false),

    MANAGE_ROLES(28, true, false),
    MANAGE_PERMISSIONS(28, false, true),
    MANAGE_WEBHOOKS(29, true, true),
    MANAGE_EMOTES(30, true, false),

    UNKNOWN(-1, false, false);

    private final int offset;
    private final boolean isGuild, isChannel;

    Permission(int offset, boolean isGuild, boolean isChannel)
    {
        this.offset = offset;
        this.isGuild = isGuild;
        this.isChannel = isChannel;
    }

    /**
     * The binary offset of the permission.<br>
     * For more information about Discord's offset system refer to
     * <a href="https://discordapi.readthedocs.org/en/latest/reference/channels/permissions.html#permissions-number">Discord Permission Numbers</a>.
     *
     * @return
     *      The offset that represents this {@link net.dv8tion.jda.core.Permission Permission}.
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * The value of this permission when viewed as a raw value.<br>
     * This is equvalent to <code>1 &lt;&lt; {@link #getOffset()}</code>
     *
     * @return
     *      The raw value of this specific permission.
     */
    public long getRawValue()
    {
        return 1 << getOffset();
    }

    /**
     * Returns whether or not this Permission is present Guild-side (configurable via roles)
     *
     * @return
     *      True if this permission is present Guild-side
     */
    public boolean isGuild()
    {
        return isGuild;
    }

    /**
     * Returns whether or not this Permission is present Channel-side (configurable via chanel-overrides)
     *
     * @return
     *      True if this permission is present Channel-side
     */
    public boolean isChannel()
    {
        return isChannel;
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.Permission Permission} relating to the provided offset.<br>
     * If there is no {@link net.dv8tion.jda.core.Permission Permssions} that matches the provided
     * offset, {@link net.dv8tion.jda.core.Permission#UNKNOWN Permission.UNKNOWN} is returned.
     *
     * @param offset
     *          The offset to match a {@link net.dv8tion.jda.core.Permission Permission} to.
     * @return
     *      {@link net.dv8tion.jda.core.Permission Permission} relating to the provided offset.
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
     * A list of all {@link net.dv8tion.jda.core.Permission Permissions} that are specified by this raw int representation of
     * permissions. The is best used with the getRaw methods in {@link net.dv8tion.jda.core.entities.Role Role},
     * {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} or {@link net.dv8tion.jda.core.utils.PermissionUtil}.
     * <p>
     * Examples:<br>
     * {@link net.dv8tion.jda.core.utils.PermissionUtil#getEffectivePermission(net.dv8tion.jda.core.entities.Channel, net.dv8tion.jda.core.entities.Member) PermissionUtil.getEffectivePermission(channel, member)}<br>
     * {@link net.dv8tion.jda.core.entities.PermissionOverride#getAllowedRaw() PermissionOverride.getAllowedRaw()}<br>
     * {@link net.dv8tion.jda.core.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()}
     *
     * @param permissions
     *          The raw <code>int</code> representation of permissions.
     * @return
     *      Possibly-empty list of {@link net.dv8tion.jda.core.Permission Permissions}.
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

    public static long getRaw(Permission... permissions)
    {
        long raw = 0;
        for (Permission perm : permissions)
            raw |= (1 << perm.getOffset());

        return raw;
    }

    public static long getRaw(Collection<Permission> permissions)
    {
        Args.notNull(permissions, "Permission Collection");

        return getRaw(permissions.toArray(new Permission[permissions.size()]));
    }
}
