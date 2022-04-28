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
package net.dv8tion.jda.api.events.interaction.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.interactions.commands.CommandPermission;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Indicates that the Permissions of an interaction-command changed
 *
 * <p>Can be used to get affected Guild and {@link List} of new {@link CommandPermission CommandPermissions}
 */
public class ApplicationCommandUpdatePermissionsEvent extends GenericGuildEvent
{
    private final long commandId;
    private final long applicationId;
    private final List<CommandPermission> permissions;

    public ApplicationCommandUpdatePermissionsEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull DataObject json)
    {
        super(api, responseNumber, guild);
        this.commandId = Long.parseLong(json.getString("id"));
        this.applicationId = Long.parseLong(json.getString("application_id"));
        this.permissions = json.getArray("permissions")
                .stream(DataArray::getObject)
                .map(obj -> new CommandPermission(api, guild, obj.getBoolean("permission"), obj.getLong("id"), obj.getInt("type")))
                .collect(Collectors.toList());
    }

    /**
     * The new {@link CommandPermission CommandPermissions} of this command.
     *
     * @return Unmodifiable list containing the new CommandPermissions of the affected command.
     */
    @Nonnull
    public List<CommandPermission> getPermissions()
    {
        return Collections.unmodifiableList(permissions);
    }

    /**
     * The id of the command in question.
     *
     * @return id of the command in question.
     */
    public long getCommandId()
    {
        return commandId;
    }

    /**
     * The id of the application whose command has changed.
     *
     * @return id of the application in question.
     */
    public long getApplicationId()
    {
        return applicationId;
    }
}
