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

// TODO: Docs
public class CommandPermissions
{
    public static final CommandPermissions ENABLED  = new CommandPermissions(null);
    public static final CommandPermissions DISABLED = new CommandPermissions(0L);

    private final Long permissions;

    private CommandPermissions(@Nullable Long permissions)
    {
        this.permissions = permissions;
    }

    @Nullable
    public Long getPermissionsRaw()
    {
        return permissions;
    }

    @Nonnull
    public static CommandPermissions enabledFor(@Nonnull Collection<Permission> permissions)
    {
        Checks.noneNull(permissions, "Permissions");
        if (permissions.isEmpty())
            return ENABLED;

        return new CommandPermissions(Permission.getRaw(permissions));
    }

    @Nonnull
    public static CommandPermissions enabledFor(@Nonnull Permission... permissions)
    {
        return enabledFor(Arrays.asList(permissions));
    }

    @Nonnull
    public static CommandPermissions enabledFor(@Nullable Long permissions)
    {
        if (permissions == null)
            return ENABLED;
        if (permissions == 0)
            return DISABLED;

        return enabledFor(Permission.getPermissions(permissions));
    }
}
