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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda;

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

    MESSAGE_READ(10, true, true),
    MESSAGE_WRITE(11, true, true),
    MESSAGE_TTS(12, true, true),
    MESSAGE_MANAGE(13, true, true),
    MESSAGE_EMBED_LINKS(14, true, true),
    MESSAGE_ATTACH_FILES(15, true, true),
    MESSAGE_HISTORY(16, true, true),
    MESSAGE_MENTION_EVERYONE(17, true, true),

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
     *      The offset that represents this {@link net.dv8tion.jda.Permission Permission}.
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * Gets the {@link net.dv8tion.jda.Permission Permission} relating to the provided offset.<br>
     * If there is no {@link net.dv8tion.jda.Permission Permssions} that matches the provided
     * offset, {@link net.dv8tion.jda.Permission#UNKNOWN Permission.UNKNOWN} is returned.
     *
     * @param offset
     *          The offset to match a {@link net.dv8tion.jda.Permission Permission} to.
     * @return
     *      {@link net.dv8tion.jda.Permission Permission} relating to the provided offset.
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
     * A list of all {@link net.dv8tion.jda.Permission Permissions} that are specified by this raw int representation of
     * permissions. The is best used with the getRaw methods in {@link net.dv8tion.jda.entities.Role Role},
     * {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} or {@link net.dv8tion.jda.utils.PermissionUtil}.
     * <p>
     * Examples:<br>
     * {@link net.dv8tion.jda.utils.PermissionUtil#getEffectivePermission(net.dv8tion.jda.entities.User, net.dv8tion.jda.entities.Channel) PermissionUtil.getEffectivePermission(user, channel)}<br>
     * {@link net.dv8tion.jda.entities.PermissionOverride#getAllowedRaw() PermissionOverride.getAllowedRaw()}<br>
     * {@link net.dv8tion.jda.entities.Role#getPermissionsRaw() Role.getPermissionsRaw()}
     *
     * @param permissions
     *          The raw <code>int</code> representation of permissions.
     * @return
     *      Possibly-empty list of {@link net.dv8tion.jda.Permission Permissions}.
     */
    public static List<Permission> getPermissions(int permissions)
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
}
