/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.*;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import org.json.JSONObject;

import java.util.LinkedList;

public class GuildMemberAddHandler extends SocketHandler
{

    public GuildMemberAddHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        User user = new EntityBuilder(api).createUser(content.getJSONObject("user"));
        if (api.getOffline_pms().containsKey(user.getId()))
        {
            PrivateChannel pc = new PrivateChannelImpl(api.getOffline_pms().get(user.getId()), user, api);
            ((UserImpl) user).setPrivateChannel(pc);
            api.getOffline_pms().remove(user.getId());
        }
        guild.getUserRoles().put(user, new LinkedList<>());
        VoiceStatusImpl voiceStatus = new VoiceStatusImpl(user, guild);
        ((GuildImpl) guild).getVoiceStatusMap().put(user, voiceStatus);
        api.getEventManager().handle(
                new GuildMemberJoinEvent(
                        api, responseNumber,
                        guild, user));
    }
}
