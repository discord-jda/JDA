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

import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class GuildDeleteHandler extends SocketHandler
{
    public GuildDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long id = content.getLong("id");
        GuildSetupController setupController = getJDA().getGuildSetupController();
        boolean wasInit = setupController.onDelete(id, content);
        if (wasInit || setupController.isUnavailable(id))
            return null;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(id);
        boolean unavailable = content.getBoolean("unavailable");
        if (guild == null)
        {
            //getJDA().getEventCache().cache(EventCache.Type.GUILD, id, () -> handle(responseNumber, allContent));
            WebSocketClient.LOG.debug("Received GUILD_DELETE for a Guild that is not currently cached. ID: {} unavailable: {}", id, unavailable);
            return null;
        }

        //If the event is attempting to mark the guild as unavailable, but it is already unavailable,
        // ignore the event
        if (setupController.isUnavailable(id) && unavailable)
            return null;

        // Detach the guild cache from the global cache (also removes users if necessary)
        guild.invalidate();

        if (unavailable)
        {
            setupController.onUnavailable(id);
            getJDA().handleEvent(
                new GuildUnavailableEvent(
                    getJDA(), responseNumber,
                    guild));
        }
        else
        {
            getJDA().handleEvent(
                new GuildLeaveEvent(
                    getJDA(), responseNumber,
                    guild));
        }
        getJDA().getEventCache().clear(EventCache.Type.GUILD, id);
        return null;
    }
}
