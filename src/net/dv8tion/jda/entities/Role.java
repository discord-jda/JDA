/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.Permission;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public interface Role
{
    /**
     * The Position of this Role on the server (higher value -> higher role)
     * The @everyone Role always return -1
     *
     * @return The position of this Role as integer
     */
    int getPosition();

    /**
     * The Name of the Role
     *
     * @return The name of the Role as String
     */
    String getName();

    /**
     * Is this role managed? (Via plugins like Twitch)
     *
     * @return If this Role is managed
     */
    boolean isManaged();

    /**
     * Is this Role hoist? (Users with this Role are grouped in the online-list)
     *
     * @return If this Role is hoist
     */
    boolean isHoist();

    /**
     * The ID of this Role
     *
     * @return The Id of this Role
     */
    String getId();

    /**
     * The color this Role is displayed in
     *
     * @return integer value of Role-color
     */
    int getColor();

    /**
     * Checks if this Role has a given Permission
     * This does not check the Channel-specific Permissions
     *
     * @param perm the Permission to check for
     * @return If the given Permission is available to this Role
     */
    boolean hasPermission(Permission perm);
}
