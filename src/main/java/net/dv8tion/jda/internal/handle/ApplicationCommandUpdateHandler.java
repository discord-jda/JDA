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
import net.dv8tion.jda.api.events.application.ApplicationCommandUpdateEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class ApplicationCommandUpdateHandler extends SocketHandler
{
    public ApplicationCommandUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        /* {"op":0,"s":7,"t":"APPLICATION_COMMAND_UPDATE","d":{"name":"ping","guild_id":"163772719836430337","description":"Test command","id":"819569491162955796","version":"820389460008108083","application_id":"420321485757087746"}} */
        long guildId = content.getUnsignedLong("guild_id");
        if (api.getGuildSetupController().isLocked(guildId))
            return guildId;
        Guild guild = api.getGuildById(guildId);
        if (guildId != 0L && guild == null)
        {
            EventCache.LOG.debug("Received APPLICATION_COMMAND_UPDATE for Guild that isn't cache. GuildId: {}", guildId);
            api.getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        Command command = new Command(api, guild, content);
        api.handleEvent(
            new ApplicationCommandUpdateEvent(api, responseNumber,
                command, guild));
        return null;
    }
}
