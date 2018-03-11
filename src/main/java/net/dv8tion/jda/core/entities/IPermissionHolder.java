/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.Permission;

import java.util.Collection;
import java.util.List;

/**
 * Marker for entities that hold Permissions within JDA
 *
 * @since 3.0
 */
public interface IPermissionHolder
{

    /**
     * The Guild to which this PermissionHolder is related
     * 
     * @return A never-null Guild to which this PermissionHolder is linked
     */
    Guild getGuild();

    /**
     * The Guild-Wide Permissions this PermissionHolder holds.
     *
     * @return An immutable List of Permissions granted to this PermissionHolder.
     */
    List<Permission> getPermissions();

    /**
     * Checks whether or not this PermissionHolder has the given {@link net.dv8tion.jda.core.Permission Permissions} in the Guild.
     *
     * @param  permissions
     *         Permissions to check for.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder.
     */
    boolean hasPermission(Permission... permissions);

    /**
     * Checks whether or not this PermissionHolder has the {@link net.dv8tion.jda.core.Permission Permissions} in the provided
     * {@code Collection<Permission>} in the Guild.
     *
     * @param  permissions
     *         Permissions to check for.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder.
     *
     * @see    java.util.EnumSet EnumSet
     */
    boolean hasPermission(Collection<Permission> permissions);

    /**
     * Checks whether or not this PermissionHolder has the given {@link net.dv8tion.jda.core.Permission Permissions} in the specified Channel.
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.Channel Channel} in which to check.
     * @param  permissions
     *         Permissions to check for.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder in the provided Channel.
     *
     * @see    java.util.EnumSet EnumSet
     */
    boolean hasPermission(Channel channel, Permission... permissions);

    /**
     * Checks whether or not this PermissionHolder has the {@link net.dv8tion.jda.core.Permission Permissions} in the provided
     * {@code Collection<Permission>} in the specified Channel.
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.Channel Channel} in which to check.
     * @param  permissions
     *         Permissions to check for.
     *
     * @return True, if all of the specified Permissions are granted to this PermissionHolder in the provided Channel.
     */
    boolean hasPermission(Channel channel, Collection<Permission> permissions);
}
