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
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.TextChannelImpl;
import net.dv8tion.jda.entities.impl.UserImpl;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuildLeaveHandler extends SocketHandler
{

    public GuildLeaveHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (GuildLock.get(api).isLocked(content.getString("id")))
        {
            return content.getString("id");
        }

        Guild guild = api.getGuildMap().get(content.getString("id"));
        AudioManager manager = api.getAudioManagersMap().get(guild);
        if (manager != null)
            manager.closeAudioConnection();
        if (content.has("unavailable") && content.getBoolean("unavailable"))
        {
            ((GuildImpl) guild).setAvailable(false);
            api.getEventManager().handle(
                    new GuildUnavailableEvent(api, responseNumber, guild)
            );
            return null;
        }

        if (manager != null)
            api.getAudioManagersMap().remove(guild);

        //cleaning up all users that we do not share a guild with anymore
        List<User> users = guild.getUsers();
        Set<User> usersInOtherGuilds = new HashSet<>();
        for (Guild g : api.getGuilds())
        {
            if (g == guild)
                continue;
            usersInOtherGuilds.addAll(g.getUsers());
        }
        for (User user : users)
        {
            if (!usersInOtherGuilds.contains(user))
            {
                //clean up this user
                if (((UserImpl) user).hasPrivateChannel())
                {
                    api.getOffline_pms().put(user.getId(), user.getPrivateChannel().getId());
                }
                api.getUserMap().remove(user.getId());
            }
        }

        api.getGuildMap().remove(guild.getId());
        guild.getTextChannels().forEach(chan -> api.getChannelMap().remove(chan.getId()));
        guild.getVoiceChannels().forEach(chan -> api.getVoiceChannelMap().remove(chan.getId()));
        TextChannelImpl.AsyncMessageSender.stop(api, guild.getId());
        api.getEventManager().handle(
                new GuildLeaveEvent(
                        api, responseNumber,
                        guild));
        return null;
    }
}
