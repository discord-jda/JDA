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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GuildDeleteHandler extends SocketHandler
{
    public GuildDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (GuildLock.get(api).isLocked(content.getString("id")))
        {
            return content.getString("id");
        }

        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("id"));
//        AudioManager manager = api.getAudioManagersMap().get(guild);
//        if (manager != null)
//            manager.closeAudioConnection();

        if (content.has("unavailable") && content.getBoolean("unavailable"))
        {
            ((GuildImpl) guild).setAvailable(false);
            api.getEventManager().handle(
                    new GuildUnavailableEvent(api, responseNumber, guild)
            );
            return null;
        }

//        if (manager != null)
//            api.getAudioManagersMap().remove(guild);

        //cleaning up all users that we do not share a guild with anymore
        Set<String> memberIds = guild.getMembersMap().keySet();
        for (Guild guildI : api.getGuilds())
        {
            GuildImpl g = (GuildImpl) guildI;
            if (g.equals(guild))
                continue;

            for (Iterator<String> it = memberIds.iterator(); it.hasNext();)
            {

                if (g.getMembersMap().containsKey(it.next()))
                    it.remove();
            }
        }

        //TODO: remove memberIds that have Relationships with the logged in account (thus saving them)

        for (String memberId : memberIds)
        {
            User user = api.getUserMap().remove(memberId);
            if (user.hasPrivateChannel())
            {
                PrivateChannelImpl chan = (PrivateChannelImpl) user.getPrivateChannel();
                ((UserImpl) user).setFake(true);
                chan.setFake(true);
                api.getFakeUserMap().put(user.getId(), user);
                api.getFakePrivateChannelMap().put(chan.getId(), chan);
            }

        }

        api.getGuildMap().remove(guild.getId());
        guild.getTextChannels().forEach(chan -> api.getTextChannelMap().remove(chan.getId()));
        guild.getVoiceChannels().forEach(chan -> api.getVoiceChannelMap().remove(chan.getId()));
        api.getEventManager().handle(
                new GuildLeaveEvent(
                        api, responseNumber,
                        guild));
        return null;
    }
}
