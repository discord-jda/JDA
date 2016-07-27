/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.UnavailableGuildJoinedEvent;
import org.json.JSONObject;

public class GuildJoinHandler extends SocketHandler
{

    public GuildJoinHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        Guild g = api.getGuildById(content.getString("id"));
        Boolean wasAvail = (g == null || g.getName() == null) ? null : g.isAvailable();
        new EntityBuilder(api).createGuildFirstPass(content, guild ->
        {
            if (guild.isAvailable())
            {
                if (!api.getClient().isReady())
                {
                    new ReadyHandler(api, responseNumber).onGuildInit(guild);
                }
                else
                {
                    if(wasAvail == null)                    //didn't exist
                    {
                        api.getEventManager().handle(
                                new GuildJoinEvent(
                                        api, responseNumber,
                                        guild));
                        EventCache.get(api).playbackCache(EventCache.Type.GUILD, guild.getId());
                    }
                    else if (!wasAvail)                     //was previously unavailable
                    {
                        api.getEventManager().handle(
                                new GuildAvailableEvent(api, responseNumber, guild)
                        );
                    }
                    else
                    {
                        throw new RuntimeException("Got a GuildCreateEvent for a guild that already existed! Json: " + content.toString());
                    }
                }
            }
            else
            {
                //Proper GuildJoinedEvent is fired when guild was populated
                api.getEventManager().handle(
                        new UnavailableGuildJoinedEvent(api, responseNumber, guild.getId())
                );
            }
        });
        return null;
    }
}
