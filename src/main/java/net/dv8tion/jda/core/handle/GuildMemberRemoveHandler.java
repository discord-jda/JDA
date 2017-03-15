/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

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

        if (member == null)
        {
            WebSocketClient.LOG.debug("Received GUILD_MEMBER_REMOVE for a Member that does not exist in the specified Guild.");
            return null;
        }

        if (member.getVoiceState().inVoiceChannel())//If this user was in a VoiceChannel, fire VoiceLeaveEvent.
        {

            GuildVoiceStateImpl vState = (GuildVoiceStateImpl) member.getVoiceState();
            VoiceChannel channel = vState.getChannel();
            vState.setConnectedChannel(null);
            ((VoiceChannelImpl) channel).getConnectedMembersMap().remove(member.getUser().getId());
            api.getEventManager().handle(
                    new GuildVoiceLeaveEvent(
                            api, responseNumber,
                            member, channel));
        }

        //The user is not in a different guild that we share
        // The user also is not a friend of this account in the case that the logged in account is a client account.
        if (!api.getGuildMap().values().stream().anyMatch(g -> ((GuildImpl) g).getMembersMap().containsKey(userId))
                && !(api.getAccountType() == AccountType.CLIENT && api.asClient().getFriendById(userId) != null))
        {
            UserImpl user = (UserImpl) api.getUserMap().remove(userId);
            if (user.hasPrivateChannel())
            {
                PrivateChannelImpl priv = (PrivateChannelImpl) user.getPrivateChannel();
                user.setFake(true);
                priv.setFake(true);
                api.getFakeUserMap().put(user.getId(), user);
                api.getFakePrivateChannelMap().put(priv.getId(), priv);
            }
            else if (api.getAccountType() == AccountType.CLIENT)
            {
                //While the user might not have a private channel, if this is a client account then the user
                // could be in a Group, and if so we need to change the User object to be fake and
                // place it in the FakeUserMap
                for (Group grp : api.asClient().getGroups())
                {
                    if (grp.getNonFriendUsers().contains(user))
                    {
                        user.setFake(true);
                        api.getFakeUserMap().put(user.getId(), user);
                        break; //Breaks from groups loop
                    }
                }
            }
        }
        api.getEventManager().handle(
                new GuildMemberLeaveEvent(
                        api, responseNumber,
                        guild, member));
        return null;
    }
}
