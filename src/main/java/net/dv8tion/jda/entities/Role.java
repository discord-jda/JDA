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
package net.dv8tion.jda.entities;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.managers.RoleManager;

import java.util.List;

public interface Role extends Comparable<Role>
{
    /**
     * The hierarchical position of this {@link net.dv8tion.jda.entities.Role Role} in the {@link net.dv8tion.jda.entities.Guild Guild} hierarchy.<br>
     * (higher value means higher role).<br>
     * The @everyone {@link net.dv8tion.jda.entities.Role Role} always return -1.
     *
     * @return
     *      The position of this {@link net.dv8tion.jda.entities.Role Role} as integer.
     */
    int getPosition();

    /**
     * The actual position of the {@link net.dv8tion.jda.entities.Role Role} as stored and given by Discord.
     * Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
     * The more recent a role was created, the lower it is in the heirarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * Role then this method will give you that value.
     *
     * @return
     *      The true, Discord stored, position of the {@link net.dv8tion.jda.entities.Role Role}.
     */
    int getPositionRaw();

    /**
     * The Name of the {@link net.dv8tion.jda.entities.Role Role}.
     *
     * @return
     *      Never-null String containing the name of this {@link net.dv8tion.jda.entities.Role Role}.
     */
    String getName();

    /**
     * Is this {@link net.dv8tion.jda.entities.Role Role} managed?<br>
     * (Via plugins like Twitch).
     *
     * @return
     *      If this {@link net.dv8tion.jda.entities.Role Role} is managed.
     */
    boolean isManaged();

    /**
     * Is this {@link net.dv8tion.jda.entities.Role Role} grouped?<br>
     * (Users with this Role are grouped in the online-list)
     *
     * @return
     *      If this {@link net.dv8tion.jda.entities.Role Role} is grouped.
     */
    boolean isGrouped();

    /**
     * Returns wheter or not this Role is mentionable
     *
     * @return
     *      True if Role is mentionable.
     */
    boolean isMentionable();

    /**
     * Returns the String needed to mention this Role in a {@link net.dv8tion.jda.entities.Message Message}.
     *
     * @return
     *      The String needed to mention this Role
     */
    String getAsMention();

    /**
     * The ID of this {@link net.dv8tion.jda.entities.Role Role}.
     *
     * @return
     *      Never-null String containing the id of this {@link net.dv8tion.jda.entities.Role Role}.
     */
    String getId();

    /**
     * The <code>int</code> representation of the literal permissions that this {@link net.dv8tion.jda.entities.Role Role} has.<br>
     * <b>NOTE:</b> these do not necessarily represent the permissions this role will have in a {@link net.dv8tion.jda.entities.Channel Channel}.
     *
     * @return
     *      Never-negative int containing offset permissions of this role.
     */
    int getPermissionsRaw();

    /**
     * A list of the literal {@link net.dv8tion.jda.Permission Permissions} that this {@link net.dv8tion.jda.entities.Role Role} has.<br>
     * <b>NOTE:</b> these do not necessarily represent the permissions this role will have in a {@link net.dv8tion.jda.entities.Channel Channel}.
     *
     * @return
     *      Possibly-empty list containing the literal permissions of this role.
     */
    List<Permission> getPermissions();

    /**
     * The color this {@link net.dv8tion.jda.entities.Role Role} is displayed in.
     *
     * @return
     *      Integer value of Role-color
     */
    int getColor();

    /**
     * Checks if this {@link net.dv8tion.jda.entities.Role Role} has a
     * {@link net.dv8tion.jda.entities.Guild Guild} level {@link net.dv8tion.jda.Permission Permission}.<br>
     * This does not check the Channel-specific override {@link net.dv8tion.jda.Permission Permission}.
     *
     * @param perm
     *          The {@link net.dv8tion.jda.Permission Permission} to check for
     * @return
     *      If the given {@link net.dv8tion.jda.Permission Permission} is available to this {@link net.dv8tion.jda.entities.Role Role}
     */
    boolean hasPermission(Permission perm);

    /**
     * Returns the {@link net.dv8tion.jda.entities.Guild Guild} this Role exists in
     * @return
     *      the Guild containing this Role
     */
    Guild getGuild();

    /**
     * Returns the {@link net.dv8tion.jda.managers.RoleManager RoleManager} for this Role.
     * In the RoleManager, you can modify all its values.
     *
     * @return
     *      The RoleManager of this Role
     */
    RoleManager getManager();

    /**
     * Returns the {@link net.dv8tion.jda.JDA JDA} instance of this Role
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();
}
