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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.managers.RoleManager;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public interface Role extends ISnowflake, IMentionable, Comparable<Role>
{
    /**
     * The hierarchical position of this {@link net.dv8tion.jda.core.entities.Role Role} in the {@link net.dv8tion.jda.core.entities.Guild Guild} hierarchy.<br>
     * (higher value means higher role).<br>
     * The @everyone {@link net.dv8tion.jda.core.entities.Role Role} always return -1.
     *
     * @return
     *      The position of this {@link net.dv8tion.jda.core.entities.Role Role} as integer.
     */
    int getPosition();

    /**
     * The actual position of the {@link net.dv8tion.jda.core.entities.Role Role} as stored and given by Discord.
     * Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
     * The more recent a role was created, the lower it is in the heirarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * Role then this method will give you that value.
     *
     * @return
     *      The true, Discord stored, position of the {@link net.dv8tion.jda.core.entities.Role Role}.
     */
    int getPositionRaw();

    /**
     * The Name of the {@link net.dv8tion.jda.core.entities.Role Role}.
     *
     * @return
     *      Never-null String containing the name of this {@link net.dv8tion.jda.core.entities.Role Role}.
     */
    String getName();

    /**
     * Is this {@link net.dv8tion.jda.core.entities.Role Role} managed?<br>
     * (Via plugins like Twitch).
     *
     * @return
     *      If this {@link net.dv8tion.jda.core.entities.Role Role} is managed.
     */
    boolean isManaged();

    /**
     * Is this {@link net.dv8tion.jda.core.entities.Role Role} hoisted?<br>
     * Members in a hoisted role are displayed in their own grouping on the user-list
     *
     * @return
     *      If this {@link net.dv8tion.jda.core.entities.Role Role} is hoisted.
     */
    boolean isHoisted();

    /**
     * Returns wheter or not this Role is mentionable
     *
     * @return
     *      True if Role is mentionable.
     */
    boolean isMentionable();

    /**
     * The <code>long</code> representation of the literal permissions that this {@link net.dv8tion.jda.core.entities.Role Role} has.<br>
     * <b>NOTE:</b> these do not necessarily represent the permissions this role will have in a {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @return
     *      Never-negative long containing offset permissions of this role.
     */
    long getPermissionsRaw();

    /**
     * A list of the literal {@link net.dv8tion.jda.core.Permission Permissions} that this {@link net.dv8tion.jda.core.entities.Role Role} has.<br>
     * <b>NOTE:</b> these do not necessarily represent the permissions this role will have in a {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @return
     *      Possibly-empty list containing the literal permissions of this role.
     */
    List<Permission> getPermissions();

    /**
     * The color this {@link net.dv8tion.jda.core.entities.Role Role} is displayed in.
     *
     * @return
     *      Color value of Role-color
     */
    Color getColor();

    /**
     * Checks if this {@link net.dv8tion.jda.core.entities.Role Role} has access to the provided {@link net.dv8tion.jda.core.Permission Permissions}.
     * This does not check the Channel-specific override {@link net.dv8tion.jda.core.Permission Permissions}.
     * <p>
     * <b>NOTE:</b> this is not the same as {@link net.dv8tion.jda.core.entities.Role#getPermissions()}{@link Collection#contains(Object) .contains(Permission)}
     * as it does effective permission calculations. The correct usage of this method is to determine if a Role has
     * the ability to do something.
     *
     * @param permissions
     *          The {@link net.dv8tion.jda.core.Permission Permissions} to check for
     * @return
     *      If the given {@link net.dv8tion.jda.core.Permission Permissions} are available to this {@link net.dv8tion.jda.core.entities.Role Role}
     */
    boolean hasPermission(Permission... permissions);

    /**
     * Checks if this {@link net.dv8tion.jda.core.entities.Role Role} has access to the {@link net.dv8tion.jda.core.Permission Permissions}
     * in the provided Collection&lt;Permission&gt;<br>
     * This does not check the Channel-specific override {@link net.dv8tion.jda.core.Permission Permissions}.
     * <p>
     * <b>NOTE:</b> this is not the same as {@link net.dv8tion.jda.core.entities.Role#getPermissions()}{@link Collection#contains(Object) .contains(Permission)}
     * as it does effective permission calculations. The correct usage of this method is to determine if a Role has
     * the ability to do something.
     *
     * @param permissions
     *          The {@link net.dv8tion.jda.core.Permission Permissions} to check for
     * @return
     *      If the given {@link net.dv8tion.jda.core.Permission Permissions} are available to this {@link net.dv8tion.jda.core.entities.Role Role}
     */
    boolean hasPermission(Collection<Permission> permissions);

    /**
     * Checks if this {@link net.dv8tion.jda.core.entities.Role Role} has access to the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * in the specified {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @param permissions
     *          The {@link net.dv8tion.jda.core.Permission Permissions} to check for
     * @return
     *      If the given {@link net.dv8tion.jda.core.Permission Permissions} are available to this {@link net.dv8tion.jda.core.entities.Role Role} in this Channel
     */
    boolean hasPermission(Channel chanel, Permission... permissions);

    /**
     * Checks if this {@link net.dv8tion.jda.core.entities.Role Role} has access to the {@link net.dv8tion.jda.core.Permission Permissions}
     * in the provided Collection&lt;Permission&gt; in the specified {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @param permissions
     *          The {@link net.dv8tion.jda.core.Permission Permissions} to check for
     * @return
     *      If the given {@link net.dv8tion.jda.core.Permission Permissions} are available to this {@link net.dv8tion.jda.core.entities.Role Role} in this Channel
     */
    boolean hasPermission(Channel channel, Collection<Permission> permissions);

    boolean canInteract(Role role);

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} this Role exists in
     * @return
     *      the Guild containing this Role
     */
    Guild getGuild();

    /**
     * Returns the {@link net.dv8tion.jda.core.managers.RoleManager RoleManager} for this Role.
     * In the RoleManager, you can modify all its values.
     *
     * @return
     *      The RoleManager of this Role
     */
    RoleManager getManager();

    RoleManagerUpdatable getManagerUpdatable();

    RestAction<Void> delete();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this Role
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();
}
