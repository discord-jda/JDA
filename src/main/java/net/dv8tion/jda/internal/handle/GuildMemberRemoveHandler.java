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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.GuildVoiceStateImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.MemberPresenceImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.VoiceChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.AudioChannelMixin;
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

        // Update the memberCount
        guild.onMemberRemove();
        CacheView.SimpleCacheView<MemberPresenceImpl> presences = guild.getPresenceView();
        if (presences != null)
            presences.remove(userId);

        User user = api.getEntityBuilder().createUser(content.getObject("user"));
        MemberImpl member = (MemberImpl) guild.getMembersView().remove(userId);

        if (member == null)
        {
//            WebSocketClient.LOG.debug("Received GUILD_MEMBER_REMOVE for a Member that does not exist in the specified Guild. UserId: {} GuildId: {}", userId, id);
            // Remove user from voice channel if applicable
            guild.getVoiceChannelCache().forEachUnordered((channel) -> {
                VoiceChannelImpl impl = (VoiceChannelImpl) channel;
                Member connected = impl.getConnectedMembersMap().remove(userId);
                if (connected != null) // user left channel!
                {
                    getJDA().handleEvent(
                        new GuildVoiceUpdateEvent(
                            getJDA(), responseNumber,
                            connected, channel));
                }
            });

            // Fire cache independent event, we can still inform the library user about the member removal
            getJDA().handleEvent(
                new GuildMemberRemoveEvent(
                    getJDA(), responseNumber,
                    guild, user, null));
            return null;
        }

        GuildVoiceStateImpl voiceState = (GuildVoiceStateImpl) member.getVoiceState();
        if (voiceState != null && voiceState.inAudioChannel()) //If this user was in an AudioChannel, fire VoiceLeaveEvent.
        {
            AudioChannel channel = voiceState.getChannel();
            voiceState.setConnectedChannel(null);
            ((AudioChannelMixin<?>) channel).getConnectedMembersMap().remove(userId);

            getJDA().handleEvent(
                new GuildVoiceUpdateEvent(
                    getJDA(), responseNumber,
                    member, channel));
        }

        //The user is not in a different guild that we share
        SnowflakeCacheViewImpl<User> userView = getJDA().getUsersView();
        try (UnlockHook hook = userView.writeLock())
        {
            if (userId != getJDA().getSelfUser().getIdLong() // don't remove selfUser from cache
                    && getJDA().getGuildsView().stream()
                               .noneMatch(g -> g.getMemberById(userId) != null))
            {
                userView.remove(userId);
                getJDA().getEventCache().clear(EventCache.Type.USER, userId);
            }
        }
        // Cache independent event
        getJDA().handleEvent(
            new GuildMemberRemoveEvent(
                getJDA(), responseNumber,
                guild, user, member));
        return null;
    }
}
