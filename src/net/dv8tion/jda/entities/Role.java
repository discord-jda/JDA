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

public interface Role
{
    /**
     * The position of this {@link net.dv8tion.jda.entities.Role Role} in the {@link net.dv8tion.jda.entities.Guild Guild} hierarchy.<br>
     * (higher value means higher role).<br>
     * The @everyone {@link net.dv8tion.jda.entities.Role Role} always return -1
     *
     * @return
     *      The position of this {@link net.dv8tion.jda.entities.Role Role} as integer.
     */
    int getPosition();

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
     * The ID of this {@link net.dv8tion.jda.entities.Role Role}.
     *
     * @return
     *      Never-null String containing the id of this {@link net.dv8tion.jda.entities.Role Role}.
     */
    String getId();

    /**
     * The color this {@link net.dv8tion.jda.entities.Role Role} is displayed in.
     *
     * @return
     *      Integer value of Role-color
     */
    int getColor();

    /**
     * Checks if this {@link net.dv8tion.jda.entities.Role Role} a
     * {@link net.dv8tion.jda.entities.Guild Guild} level {@link net.dv8tion.jda.Permission Permission}.<br>
     * This does not check the Channel-specific override {@link net.dv8tion.jda.Permission Permission}.
     *
     * @param perm
     *          The {@link net.dv8tion.jda.Permission Permission} to check for
     * @return
     *      If the given {@link net.dv8tion.jda.Permission Permission} is available to this {@link net.dv8tion.jda.entities.Role Role}
     */
    boolean hasPermission(Permission perm);
}
