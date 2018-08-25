/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.core.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.core.utils.Helpers;
import org.json.JSONObject;

public class GuildCreateHandler extends SocketHandler
{

    public GuildCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.getLong("id");
        GuildImpl guild = (GuildImpl) getJDA().getGuildMap().get(id);
        if (guild == null)
        {
            getJDA().getGuildSetupController().onCreate(id, content);
            return null;
        }

        boolean unavailable = Helpers.optBoolean(content, "unavailable");
        if (guild.isAvailable() && unavailable)
        {
            guild.setAvailable(false);
            getJDA().getEventManager().handle(
                new GuildUnavailableEvent(
                    getJDA(), responseNumber,
                    guild));
        }
        else if (!guild.isAvailable() && !unavailable)
        {
            guild.setAvailable(true);
            getJDA().getEventManager().handle(
                new GuildAvailableEvent(
                    getJDA(), responseNumber,
                    guild));
            // I'm not sure if this is actually needed, but if discord sends us an updated field here
            //  we can just use the same logic we use for GUILD_UPDATE in order to update it and fire events
            getJDA().getClient().<GuildUpdateHandler>getHandler("GUILD_UPDATE").handle(responseNumber, allContent);
        }
        return null;
    }
}
