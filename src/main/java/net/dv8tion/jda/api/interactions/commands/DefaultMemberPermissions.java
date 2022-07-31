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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents the default permissions for a Discord Application-Command. These permissions define the type of users that can use this command if no explicit command-specific
 * privileges are set by moderators to control who can and can't use the command within a Guild.
 * <p>For example, given a command defined with {@link net.dv8tion.jda.api.interactions.commands.build.CommandData#setDefaultPermissions CommandData#setDefaultPermissions} as <code>command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))</code>
 * any user with the {@link Permission#BAN_MEMBERS} permission would be able to use the command by default.
 */
public class DefaultMemberPermissions
{
    /**
     * Default permissions of a command with no restrictions applied. (Everyone can see and access this command by default)
     */
    public static final DefaultMemberPermissions ENABLED  = new DefaultMemberPermissions(null);

    /**
     * "Empty" permissions of a command.
     * <br>Only members with the {@link Permission#ADMINISTRATOR ADMINISTRATOR} permission can see and access this command by default.
     */
    public static final DefaultMemberPermissions DISABLED = new DefaultMemberPermissions(0L);

    private final Long permissions;

    private DefaultMemberPermissions(@Nullable Long permissions)
    {
        this.permissions = permissions;
    }

    /**
     * Raw permission integer representing the default permissions of a command.
     * <br>This returns null if it is of type {@link DefaultMemberPermissions#ENABLED ENABLED}
     * <br>If the default member permissions are {@link DefaultMemberPermissions#DISABLED DISABLED}, this returns 0
     *
     * @return Raw permission integer representing the default member permissions of a command
     */
    @Nullable
    public Long getPermissionsRaw()
    {
        return permissions;
    }

    /**
     * Returns a DefaultMemberPermissions instance with the predefined permissions a member must have to see and access a command.
     *
     * <br><b>If the passed Collection is empty, this returns {@link DefaultMemberPermissions#ENABLED ENABLED}</b>
     *
     * @param  permissions
     *         Collection of {@link Permission Permissions}
     *
     * @throws IllegalArgumentException
     *         If any of the passed Permission is null
     *
     * @return DefaultMemberPermissions instance with the predefined permissions
     */
    @Nonnull
    public static DefaultMemberPermissions enabledFor(@Nonnull Collection<Permission> permissions)
    {
        Checks.noneNull(permissions, "Permissions");
        if (permissions.isEmpty())
            return ENABLED;

        return enabledFor(Permission.getRaw(permissions));
    }

    /**
     * Returns a DefaultMemberPermissions instance with the predefined permissions a member must have to see and access a command.
     *
     * <br><b>If the passed Array is empty, this returns {@link DefaultMemberPermissions#ENABLED ENABLED}</b>
     *
     * @param  permissions
     *         Vararg of {@link Permission Permissions}
     *
     * @throws IllegalArgumentException
     *         If any of the passed Permission is null
     *
     * @return DefaultMemberPermissions instance with the predefined permissions
     */
    @Nonnull
    public static DefaultMemberPermissions enabledFor(@Nonnull Permission... permissions)
    {
        return enabledFor(Arrays.asList(permissions));
    }

    /**
     * Returns a DefaultMemberPermissions instance with the predefined permissions a member must have to see and access a command.
     *
     * <br><b>If the passed permission offset is 0, this returns {@link DefaultMemberPermissions#ENABLED ENABLED}</b>
     *
     * @param  permissions
     *         Raw permission bitset
     *
     * @return DefaultMemberPermissions instance with the predefined permissions
     */
    @Nonnull
    public static DefaultMemberPermissions enabledFor(long permissions)
    {
        return new DefaultMemberPermissions(permissions);
    }
}
