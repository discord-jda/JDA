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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A PrivilegeConfig is the collection of moderator defined {@link IntegrationPrivilege privileges} set on a specific application and its commands
 * that define what channels the application can be used in and what users/roles are allowed to use it.
 * These privileges are set by moderators in the guild who have access to the guild's integrations page and permissions to edit them.
 *
 * @see Guild#retrieveCommandPrivileges()
 */
public class PrivilegeConfig
{
    private final Guild guild;
    private final Map<String, List<IntegrationPrivilege>> privileges;

    public PrivilegeConfig(@Nonnull Guild guild, @Nonnull Map<String, List<IntegrationPrivilege>> privileges)
    {
        this.guild = guild;
        this.privileges = Collections.unmodifiableMap(privileges);
    }

    /**
     * The guild in which this PrivilegeConfig is applied in.
     *
     * @return Guild in which this PrivilegeConfig is applied in.
     */
    @Nonnull
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * The JDA-instance.
     *
     * @return The JDA-instance.
     */
    @Nonnull
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    /**
     * The {@link IntegrationPrivilege IntegrationPrivileges} that have been applied to this application in this guild.
     *
     * <br><b>If the privileges are "Synced" (No custom config applied), this will return null.</b>
     *
     * <p>This does not include privileges applied to a command itself. Use {@link #getCommandPrivileges(String)} for that.
     *
     * @return Immutable List containing all IntegrationPrivileges that have been applied to this application in this guild.
     */
    @Nullable
    public List<IntegrationPrivilege> getApplicationPrivileges()
    {
        return getCommandPrivileges(getJDA().getSelfUser().getApplicationId());
    }

    /**
     * The {@link IntegrationPrivilege IntegrationPrivileges} that have been applied to the command with the given id in this guild.
     *
     * <br><b>If the privileges are "Synced" (No custom config applied), or a command with this id doesn't exist, this will return null.</b>
     *
     * <p>This does not include privileges applied to the application directly. Use {@link #getApplicationPrivileges()} for that.
     *
     * @param  id
     *         The id of the command
     *
     * @throws IllegalArgumentException
     *         If the provided id is null
     *
     * @return Immutable List containing all IntegrationPrivileges that have been applied to the command with the given id in this guild.
     */
    @Nullable
    public List<IntegrationPrivilege> getCommandPrivileges(@Nonnull String id)
    {
        Checks.notNull(id, "Id");
        return privileges.get(id);
    }

    /**
     * The {@link IntegrationPrivilege IntegrationPrivileges} that have been applied to the supplied {@link Command}.
     *
     * <br><b>If the privileges are "Synced" (No custom config applied), or this command no longer exists, this will return null.</b>
     *
     * <p>This does not include privileges applied to the application directly. Use {@link #getApplicationPrivileges()} for that.
     *
     * @param  command
     *         The {@link Command} to get the privileges from
     *
     * @throws IllegalArgumentException
     *         If the provided command is null
     *
     * @return Immutable List containing all IntegrationPrivileges that have been applied to the command in this guild.
     */
    @Nullable
    public List<IntegrationPrivilege> getCommandPrivileges(@Nonnull Command command)
    {
        Checks.notNull(command, "Command");
        return privileges.get(command.getId());
    }

    /**
     * Map containing all privileges, with the command-id as the Key, and a List of {@link IntegrationPrivilege} as Value.
     *
     * <br><b>If {@link #getApplicationPrivileges()} is not null, this will also contain the privileges applied directly
     * on this application with {@link SelfUser#getApplicationId()} as the Key.</b>
     *
     * @return Unmodifiable Map containing all privileges on this guild.
     */
    @Nonnull
    public Map<String, List<IntegrationPrivilege>> getAsMap()
    {
        return privileges;
    }
}
