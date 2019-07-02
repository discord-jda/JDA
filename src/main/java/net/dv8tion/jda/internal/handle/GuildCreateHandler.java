/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.WebSocketCode;

public class GuildCreateHandler extends SocketHandler
{

    public GuildCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long id = content.getLong("id");
        GuildImpl guild = (GuildImpl) getJDA().getGuildById(id);
        if (guild == null)
        {
            getJDA().getGuildSetupController().onCreate(id, content);
            return null;
        }

        boolean unavailable = content.getBoolean("unavailable");
        if (guild.isAvailable() && unavailable)
        {
            guild.setAvailable(false);
            getJDA().handleEvent(
                new GuildUnavailableEvent(
                    getJDA(), responseNumber,
                    guild));
        }
        else if (!guild.isAvailable() && !unavailable)
        {
            guild.setAvailable(true);
            getJDA().handleEvent(
                new GuildAvailableEvent(
                    getJDA(), responseNumber,
                    guild));
            // I'm not sure if this is actually needed, but if discord sends us an updated field here
            //  we can just use the same logic we use for GUILD_UPDATE in order to update it and fire events
            getJDA().getClient().<GuildUpdateHandler>getHandler("GUILD_UPDATE")
                .handle(responseNumber, DataObject.empty()
                    .put("comment", "This was previously a GUILD_CREATE with unavailable set to false")
                    .put("t", "GUILD_UPDATE")
                    .put("s", responseNumber)
                    .put("op", WebSocketCode.DISPATCH)
                    .put("d", content));
        }
        return null;
    }
}
