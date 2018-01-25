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

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.UnavailableGuildJoinedEvent;
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
        Guild g = api.getGuildById(id);
        Boolean wasAvail = (g == null || g.getName() == null) ? null : g.isAvailable();
        api.getEntityBuilder().createGuildFirstPass(content, guild ->
        {
            if (guild.isAvailable())
            {
                if (!api.getClient().isReady())
                {
                    getReadyHandler().guildSetupComplete(guild);
                }
                else
                {
                    if (wasAvail == null) //didn't exist
                    {
                        api.getEventManager().handle(
                            new GuildJoinEvent(
                                api, responseNumber,
                                guild));
                        api.getEventCache().playbackCache(EventCache.Type.GUILD, guild.getIdLong());
                    }
                    else if (!wasAvail) //was previously unavailable
                    {
                        api.getEventManager().handle(
                            new GuildAvailableEvent(
                                api, responseNumber,
                                guild));
                    }
                    else
                    {
                        throw new IllegalStateException("Got a GuildCreateEvent for a guild that already existed! ID: " + id);
                    }
                }
            }
            else
            {
                if (!api.getClient().isReady())
                {
                    getReadyHandler().acknowledgeGuild(guild, false, false, false);
                }
                else
                {
                    //Proper GuildJoinedEvent is fired when guild was populated
                    api.getEventManager().handle(
                        new UnavailableGuildJoinedEvent(
                            api, responseNumber,
                            guild.getIdLong()));
                }
            }
        });
        return null;
    }

    private ReadyHandler getReadyHandler()
    {
        return api.getClient().getHandler("READY");
    }
}
