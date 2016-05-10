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

import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.*;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

public class GuildMemberRemoveHandler extends SocketHandler
{

    public GuildMemberRemoveHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
        if(guild == null)
        {
            //We probably just left the guild, therefore ignore
            return null;
        }
        UserImpl user = ((UserImpl) api.getUserMap().get(content.getJSONObject("user").getString("id")));
        if (guild.getVoiceStatusMap().get(user).inVoiceChannel())   //If this user was in a VoiceChannel, fire VoiceLeaveEvent.
        {
            VoiceStatusImpl status = (VoiceStatusImpl) guild.getVoiceStatusMap().get(user);
            VoiceChannel channel = status.getChannel();
            status.setChannel(null);
            ((VoiceChannelImpl) channel).getUsersModifiable().remove(user);
            api.getEventManager().handle(
                    new VoiceLeaveEvent(
                            api, responseNumber,
                            status, channel));
        }
        guild.getVoiceStatusMap().remove(user);
        guild.getUserRoles().remove(user);
        guild.getJoinedAtMap().remove(user);
        if (!api.getGuildMap().values().stream().anyMatch(g -> ((GuildImpl) g).getUserRoles().containsKey(user)))
        {
            if (user.hasPrivateChannel())
            {
                api.getOffline_pms().put(user.getId(), user.getPrivateChannel().getId());
            }
            api.getUserMap().remove(user.getId());
        }
        api.getEventManager().handle(
                new GuildMemberLeaveEvent(
                        api, responseNumber,
                        guild, user));
        return null;
    }
}
