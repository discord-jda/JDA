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
package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.ApplicationCommandUpdatePrivilegesEvent;
import net.dv8tion.jda.api.events.interaction.command.ApplicationUpdatePrivilegesEvent;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationCommandPermissionsUpdateHandler extends SocketHandler
{
    public ApplicationCommandPermissionsUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        Guild guild;
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getUnsignedLong("guild_id");
            guild = getJDA().getGuildById(guildId);
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;
            else if (guild == null)
            {
                WebSocketClient.LOG.debug("Received APPLICATION_COMMAND_PERMISSIONS_UPDATE for a guild that is not cached: GuildID: {}", guildId);
                return null;
            }
        }
        else
        {
            return null;
        }

        long id = content.getUnsignedLong("id");
        long applicationId = content.getUnsignedLong("application_id");

        List<IntegrationPrivilege> privileges = content.getArray("permissions")
                .stream(DataArray::getObject)
                .map(obj -> new IntegrationPrivilege(guild, IntegrationPrivilege.Type.fromKey(obj.getInt("type")),
                        obj.getBoolean("permission"), obj.getUnsignedLong("id")))
                .collect(Collectors.toList());

        if (id != applicationId)
            api.handleEvent(new ApplicationCommandUpdatePrivilegesEvent(api, responseNumber, guild, id, applicationId, privileges));
        else
            api.handleEvent(new ApplicationUpdatePrivilegesEvent(api, responseNumber, guild, applicationId, privileges));
        return null;
    }
}
