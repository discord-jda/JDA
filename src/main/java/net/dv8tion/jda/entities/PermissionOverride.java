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
import net.dv8tion.jda.managers.PermissionOverrideManager;

import java.util.List;

/**
 * Represents the specific {@link net.dv8tion.jda.entities.User User } or {@link net.dv8tion.jda.entities.Role Role}
 * permission overrides that can be set for channels.
 */
public interface PermissionOverride
{
    /**
     * This is the raw binary representation (as a base 10 int) of the permissions allowed by this override.<br>
     * The integer relates to the offsets used by each {@link net.dv8tion.jda.Permission Permission}.
     *
     * @return
     *      Never-negative int containing the binary representation of the allowed permissions of this override.
     */
    int getAllowedRaw();

    /**
     * This is the raw binary representation (as a base 10 int) of the permissions not affected by this override.<br>
     * The integer relates to the offsets used by each {@link net.dv8tion.jda.Permission Permission}.
     *
     * @return
     *      Never-negative int containing the binary representation of the unaffected permissions of this override.
     */
    int getInheritRaw();

    /**
     * This is the raw binary representation (as a base 10 int) of the permissions denied by this override.<br>
     * The integer relates to the offsets used by each {@link net.dv8tion.jda.Permission Permission}.
     *
     * @return
     *      Never-negative int containing the binary representation of the denied permissions of this override.
     */
    int getDeniedRaw();

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.Permission Permissions} that are specifically allowed by this override.
     *
     * @return
     *      Possibly-empty unmodifiable list of allowed {@link net.dv8tion.jda.Permission Permissions}.
     */
    List<Permission> getAllowed();

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.Permission Permission} that are unaffected by this override.
     *
     * @return
     *      Possibly-empty unmodifiable list of unaffected {@link net.dv8tion.jda.Permission Permissions}.
     */
    List<Permission> getInherit();

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.Permission Permissions} that are denied by this override.
     *
     * @return
     *      Possibly-empty unmodifiable list of denied {@link net.dv8tion.jda.Permission Permissions}.
     */
    List<Permission> getDenied();

    /**
     * The {@link net.dv8tion.jda.JDA JDA} instance that this {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} is related to.
     *
     * @return
     *      Never-null {@link net.dv8tion.jda.JDA JDA} instance.
     */
    JDA getJDA();

    /**
     * If this {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} is an override dealing with
     * a {@link net.dv8tion.jda.entities.User User}, then this method will return the related {@link net.dv8tion.jda.entities.User User}.<br>
     * Otherwise, this method returns <code>null</code>.<br>
     * Basically: if {@link PermissionOverride#isUserOverride()} returns <code>false</code>, this returns <code>null</code>.
     *
     * @return
     *      Possibly-null related {@link net.dv8tion.jda.entities.User User}.
     */
    User getUser();

    /**
     * If this {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} is an override dealing with
     * a {@link net.dv8tion.jda.entities.Role Role}, then this method will return the related {@link net.dv8tion.jda.entities.Role Role}.<br>
     * Otherwise, this method returns <code>null</code>.<br>
     * Basically: if {@link PermissionOverride#isRoleOverride()} returns <code>false</code>, this returns <code>null</code>.
     *
     * @return
     *      Possibly-null related {@link net.dv8tion.jda.entities.Role}.
     */
    Role getRole();

    /**
     * The {@link net.dv8tion.jda.entities.Channel Channel} that this {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} affects.
     *
     * @return
     *      Never-null related {@link net.dv8tion.jda.entities.Channel Channel} that this override is part of.
     */
    Channel getChannel();

    /**
     * The {@link net.dv8tion.jda.entities.Guild Guild} that the {@link net.dv8tion.jda.entities.Channel Channel} returned
     * from {@link PermissionOverride#getChannel()} is a part of. By inference, this is the {@link net.dv8tion.jda.entities.Guild Guild}
     * that this {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} is part of.
     *
     * @return
     *      Never-null related {@link net.dv8tion.jda.entities.Guild Guild}.
     */
    Guild getGuild();

    /**
     * Used to determine if this {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} relates to
     * a specific {@link net.dv8tion.jda.entities.User User}.
     *
     * @return
     *      True if this override is a user override.
     */
    boolean isUserOverride();

    /**
     * Used to determine if this {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} relates to
     * a specific {@link net.dv8tion.jda.entities.Role Role}.
     *
     * @return
     *      True if this override is a role override.
     */
    boolean isRoleOverride();

    /**
     * Returns the {@link net.dv8tion.jda.managers.PermissionOverrideManager PermissionOverrideManager} for this PermissionOverride.
     * In the PermissionOverrideManager, you can modify its permissions.
     *
     * @return
     *      The PermissionOverrideManager of this PermissionOverride
     */
    PermissionOverrideManager getManager();
}
