/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.*;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;

public class GuildMemberRemoveHandler extends SocketHandler
{

    public GuildMemberRemoveHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long id = content.getLong("guild_id");
        boolean setup = getJDA().getGuildSetupController().onRemoveMember(id, content);
        if (setup)
            return null;

        GuildImpl guild = (GuildImpl) getJDA().getGuildsView().get(id);
        if (guild == null)
        {
            //We probably just left the guild and this event is trying to remove us from the guild, therefore ignore
            return null;
        }

        final long userId = content.getObject("user").getUnsignedLong("id");
        if (userId == getJDA().getSelfUser().getIdLong())
        {
            //We probably just left the guild and this event is trying to remove us from the guild, therefore ignore
            return null;
        }
        MemberImpl member = (MemberImpl) guild.getMembersView().remove(userId);

        if (member == null)
        {
            WebSocketClient.LOG.debug("Received GUILD_MEMBER_REMOVE for a Member that does not exist in the specified Guild. UserId: {} GuildId: {}", userId, id);
            return null;
        }

        // Update the memberCount
        guild.onMemberRemove();

        GuildVoiceStateImpl voiceState = (GuildVoiceStateImpl) member.getVoiceState();
        if (voiceState != null && voiceState.inVoiceChannel())//If this user was in a VoiceChannel, fire VoiceLeaveEvent.
        {
            VoiceChannel channel = voiceState.getChannel();
            voiceState.setConnectedChannel(null);
            ((VoiceChannelImpl) channel).getConnectedMembersMap().remove(member.getUser().getIdLong());
            getJDA().handleEvent(
                    new GuildVoiceLeaveEvent(
                            getJDA(), responseNumber,
                            member, channel));
        }

        //The user is not in a different guild that we share
        SnowflakeCacheViewImpl<User> userView = getJDA().getUsersView();
        try (UnlockHook hook = userView.writeLock())
        {
            if (userId != getJDA().getSelfUser().getIdLong() // don't remove selfUser from cache
                    && getJDA().getGuildsView().stream()
                               .map(GuildImpl.class::cast)
                               .noneMatch(g -> g.getMembersView().get(userId) != null))
            {
                UserImpl user = (UserImpl) userView.getMap().remove(userId);
                if (user.hasPrivateChannel())
                {
                    PrivateChannelImpl priv = (PrivateChannelImpl) user.getPrivateChannel();
                    user.setFake(true);
                    priv.setFake(true);
                    getJDA().getFakeUserMap().put(user.getIdLong(), user);
                    getJDA().getFakePrivateChannelMap().put(priv.getIdLong(), priv);
                }
                getJDA().getEventCache().clear(EventCache.Type.USER, userId);
            }
        }
        getJDA().handleEvent(
            new GuildMemberLeaveEvent(
                getJDA(), responseNumber,
                member));
        return null;
    }
}
