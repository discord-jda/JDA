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

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

//import net.dv8tion.jda.core.events.voice.VoiceLeaveEvent;

public class GuildMemberRemoveHandler extends SocketHandler
{

    public GuildMemberRemoveHandler(JDAImpl api)
    {
        super(api);
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
            //We probably just left the guild and this event is trying to remove us from the guild, therefore ignore
            return null;
        }

        String userId = content.getJSONObject("user").getString("id");
        MemberImpl member = (MemberImpl) guild.getMembersMap().remove(userId);

        if (member.getVoiceState().inVoiceChannel())//If this user was in a VoiceChannel, fire VoiceLeaveEvent.
        {

            VoiceStateImpl vState = (VoiceStateImpl) member.getVoiceState();
            VoiceChannel channel = vState.getChannel();
            vState.setConnectedChannel(null);
            ((VoiceChannelImpl) channel).getConnectedMembersMap().remove(member);
            //TODO: Implement after deciding how to handle VoiceChannel vs GroupCall for VoiceState.
//            api.getEventManager().handle(
//                    new VoiceLeaveEvent(
//                            api, responseNumber,
//                            vState, channel));
        }

        if (!api.getGuildMap().values().stream().anyMatch(g -> ((GuildImpl) g).getMembersMap().containsKey(userId)))
        {
            UserImpl user = (UserImpl) member.getUser();
            if (user.hasPrivateChannel())
            {
                PrivateChannelImpl priv = (PrivateChannelImpl) user.getPrivateChannel();
                user.setFake(true);
                priv.setFake(true);
                api.getFakeUserMap().put(user.getId(), user);
                api.getFakePrivateChannelMap().put(priv.getId(), priv);
            }
            api.getUserMap().remove(user.getId());
        }
        api.getEventManager().handle(
                new GuildMemberLeaveEvent(
                        api, responseNumber,
                        guild, member));
        return null;
    }
}
