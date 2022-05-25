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
 * Represents the default permissions for a Discord Interaction-Command.
 * <br>Moderators can manually configure or overwrite these permissions in each guild.
 */
public class CommandPermissions
{
    /**
     * Default permissions of a command. (Everyone can see and access this command by default)
     */
    public static final CommandPermissions ENABLED  = new CommandPermissions(null);

    /**
     * "Empty" permissions of a command.
     * <br>Only members with the {@link Permission#ADMINISTRATOR ADMINISTRATOR} permission can see and access this command by default.
     */
    public static final CommandPermissions DISABLED = new CommandPermissions(0L);

    private final Long permissions;

    private CommandPermissions(@Nullable Long permissions)
    {
        this.permissions = permissions;
    }

    /**
     * Raw permission integer representing the default permissions of a command.
     * <br>This returns null if it is of type {@link CommandPermissions#ENABLED ENABLED}
     * <br>If the CommandPermissions is {@link CommandPermissions#DISABLED DISABLED}, this returns 0
     *
     * @return Raw permission integer representing the default permissions of a command
     */
    @Nullable
    public Long getPermissionsRaw()
    {
        return permissions;
    }

    /**
     * Returns a CommandPermissions instance with the predefined permissions a member must have to see and access a command.
     *
     * <br><b>If the passed Collection is empty, this returns {@link CommandPermissions#ENABLED ENABLED}</b>
     *
     * @param  permissions
     *         Collection of {@link Permission Permissions}
     *
     * @throws IllegalArgumentException
     *         If any of the passed Permission is null
     *
     * @return CommandPermissions instance with the predefined permissions
     */
    @Nonnull
    public static CommandPermissions enabledFor(@Nonnull Collection<Permission> permissions)
    {
        Checks.noneNull(permissions, "Permissions");
        if (permissions.isEmpty())
            return ENABLED;

        return enabledFor(Permission.getRaw(permissions));
    }

    /**
     * Returns a CommandPermissions instance with the predefined permissions a member must have to see and access a command.
     *
     * <br><b>If the passed Array is empty, this returns {@link CommandPermissions#ENABLED ENABLED}</b>
     *
     * @param  permissions
     *         Vararg of {@link Permission Permissions}
     *
     * @throws IllegalArgumentException
     *         If any of the passed Permission is null
     *
     * @return CommandPermissions instance with the predefined permissions
     */
    @Nonnull
    public static CommandPermissions enabledFor(@Nonnull Permission... permissions)
    {
        return enabledFor(Arrays.asList(permissions));
    }

    /**
     * Returns a CommandPermissions instance with the predefined permissions a member must have to see and access a command.
     *
     * <br><b>If the passed permission offset is 0, this returns {@link CommandPermissions#ENABLED ENABLED}</b>
     *
     * @param  permissions
     *         Raw permission bitset
     *
     * @return CommandPermissions instance with the predefined permissions
     */
    @Nonnull
    public static CommandPermissions enabledFor(long permissions)
    {
        return enabledFor(Permission.getPermissions(permissions));
    }
}
