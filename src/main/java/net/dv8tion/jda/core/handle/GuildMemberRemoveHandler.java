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

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

public class GuildMemberRemoveHandler extends SocketHandler
{

    public GuildMemberRemoveHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.getLong("guild_id");
        boolean setup = getJDA().getGuildSetupController().onRemoveMember(id, content);
        if (setup)
            return null;

        GuildImpl guild = (GuildImpl) getJDA().getGuildMap().get(id);
        if (guild == null)
        {
            //We probably just left the guild and this event is trying to remove us from the guild, therefore ignore
            return null;
        }

        final long userId = content.getJSONObject("user").getLong("id");
        if (userId == getJDA().getSelfUser().getIdLong())
        {
            //We probably just left the guild and this event is trying to remove us from the guild, therefore ignore
            return null;
        }
        MemberImpl member = (MemberImpl) guild.getMembersMap().remove(userId);

        if (member == null)
        {
            WebSocketClient.LOG.debug("Received GUILD_MEMBER_REMOVE for a Member that does not exist in the specified Guild.");
            return null;
        }

        GuildVoiceStateImpl voiceState = (GuildVoiceStateImpl) member.getVoiceState();
        if (voiceState != null && voiceState.inVoiceChannel())//If this user was in a VoiceChannel, fire VoiceLeaveEvent.
        {
            VoiceChannel channel = voiceState.getChannel();
            voiceState.setConnectedChannel(null);
            ((VoiceChannelImpl) channel).getConnectedMembersMap().remove(member.getUser().getIdLong());
            getJDA().getEventManager().handle(
                    new GuildVoiceLeaveEvent(
                            getJDA(), responseNumber,
                            member, channel));
        }

        //The user is not in a different guild that we share
        // The user also is not a friend of this account in the case that the logged in account is a client account.
        if (userId != getJDA().getSelfUser().getIdLong() // don't remove selfUser from cache
            && getJDA().getGuildMap().valueCollection().stream().noneMatch(g -> ((GuildImpl) g).getMembersMap().containsKey(userId))
            && !(getJDA().getAccountType() == AccountType.CLIENT && getJDA().asClient().getFriendById(userId) != null))
        {
            UserImpl user = (UserImpl) getJDA().getUserMap().remove(userId);
            if (user.hasPrivateChannel())
            {
                PrivateChannelImpl priv = (PrivateChannelImpl) user.getPrivateChannel();
                user.setFake(true);
                priv.setFake(true);
                getJDA().getFakeUserMap().put(user.getIdLong(), user);
                getJDA().getFakePrivateChannelMap().put(priv.getIdLong(), priv);
            }
            else if (getJDA().getAccountType() == AccountType.CLIENT)
            {
                //While the user might not have a private channel, if this is a client account then the user
                // could be in a Group, and if so we need to change the User object to be fake and
                // place it in the FakeUserMap
                for (Group grp : getJDA().asClient().getGroups())
                {
                    if (grp.getNonFriendUsers().contains(user))
                    {
                        user.setFake(true);
                        getJDA().getFakeUserMap().put(user.getIdLong(), user);
                        break; //Breaks from groups loop
                    }
                }
            }
            getJDA().getEventCache().clear(EventCache.Type.USER, userId);
        }
        getJDA().getEventManager().handle(
                new GuildMemberLeaveEvent(
                        getJDA(), responseNumber,
                        member));
        return null;
    }
}
