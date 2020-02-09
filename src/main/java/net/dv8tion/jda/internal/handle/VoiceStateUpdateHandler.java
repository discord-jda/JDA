/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.*;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.MemberCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;

import java.util.Objects;
import java.util.Optional;

public class VoiceStateUpdateHandler extends SocketHandler
{
    public VoiceStateUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final Long guildId = content.isNull("guild_id") ? null : content.getLong("guild_id");
        if (guildId == null)
            return null; //unhandled for calls
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;
        handleGuildVoiceState(content);
        return null;
    }

    private void handleGuildVoiceState(DataObject content)
    {
        final long userId = content.getLong("user_id");
        final long guildId = content.getLong("guild_id");
        final Long channelId = !content.isNull("channel_id") ? content.getLong("channel_id") : null;
        final String sessionId = !content.isNull("session_id") ? content.getString("session_id") : null;
        boolean selfMuted = content.getBoolean("self_mute");
        boolean selfDeafened = content.getBoolean("self_deaf");
        boolean guildMuted = content.getBoolean("mute");
        boolean guildDeafened = content.getBoolean("deaf");
        boolean suppressed = content.getBoolean("suppress");

        Guild guild = getJDA().getGuildById(guildId);
        if (guild == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a VOICE_STATE_UPDATE for a Guild that has yet to be cached. JSON: {}", content);
            return;
        }

        VoiceChannelImpl channel = channelId != null ? (VoiceChannelImpl) guild.getVoiceChannelById(channelId) : null;
        if (channel == null && channelId != null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received VOICE_STATE_UPDATE for a VoiceChannel that has yet to be cached. JSON: {}", content);
            return;
        }

        MemberImpl member = getLazyMember(content, userId, (GuildImpl) guild, channelId != null);
        if (member == null) return;

        GuildVoiceStateImpl vState = (GuildVoiceStateImpl) member.getVoiceState();
        if (vState == null)
            return;
        vState.setSessionId(sessionId); //Cant really see a reason for an event for this
        VoiceDispatchInterceptor voiceInterceptor = getJDA().getVoiceInterceptor();
        boolean isSelf = guild.getSelfMember().equals(member);

        boolean wasMute = vState.isMuted();
        boolean wasDeaf = vState.isDeafened();

        if (selfMuted != vState.isSelfMuted())
        {
            vState.setSelfMuted(selfMuted);
            getJDA().handleEvent(new GuildVoiceSelfMuteEvent(getJDA(), responseNumber, member));
        }
        if (selfDeafened != vState.isSelfDeafened())
        {
            vState.setSelfDeafened(selfDeafened);
            getJDA().handleEvent(new GuildVoiceSelfDeafenEvent(getJDA(), responseNumber, member));
        }
        if (guildMuted != vState.isGuildMuted())
        {
            vState.setGuildMuted(guildMuted);
            getJDA().handleEvent(new GuildVoiceGuildMuteEvent(getJDA(), responseNumber, member));
        }
        if (guildDeafened != vState.isGuildDeafened())
        {
            vState.setGuildDeafened(guildDeafened);
            getJDA().handleEvent(new GuildVoiceGuildDeafenEvent(getJDA(), responseNumber, member));
        }
        if (suppressed != vState.isSuppressed())
        {
            vState.setSuppressed(suppressed);
            getJDA().handleEvent(new GuildVoiceSuppressEvent(getJDA(), responseNumber, member));
        }
        if (wasMute != vState.isMuted())
            getJDA().handleEvent(new GuildVoiceMuteEvent(getJDA(), responseNumber, member));
        if (wasDeaf != vState.isDeafened())
            getJDA().handleEvent(new GuildVoiceDeafenEvent(getJDA(), responseNumber, member));
            
        if (!Objects.equals(channel, vState.getChannel()))
        {
            VoiceChannelImpl oldChannel = (VoiceChannelImpl) vState.getChannel();
            vState.setConnectedChannel(channel);

            if (oldChannel == null)
            {
                channel.getConnectedMembersMap().put(userId, member);
                getJDA().handleEvent(
                        new GuildVoiceJoinEvent(
                                getJDA(), responseNumber,
                                member));
            }
            else if (channel == null)
            {
                oldChannel.getConnectedMembersMap().remove(userId);
                if (isSelf)
                    getJDA().getDirectAudioController().update(guild, null);
                getJDA().handleEvent(
                        new GuildVoiceLeaveEvent(
                                getJDA(), responseNumber,
                                member, oldChannel));
            }
            else
            {
                AudioManagerImpl mng = (AudioManagerImpl) getJDA().getAudioManagersView().get(guildId);
                //If the currently connected account is the one that is being moved
                if (isSelf && mng != null && voiceInterceptor == null)
                {
                    //And this instance of JDA is connected or attempting to connect,
                    // then change the channel we expect to be connected to.
                    if (mng.isConnected() || mng.isAttemptingToConnect())
                        mng.setConnectedChannel(channel);

                    //If we have connected (VOICE_SERVER_UPDATE received and AudioConnection created (actual connection might still be setting up)),
                    // then we need to stop sending audioOpen/Move requests through the MainWS if the channel
                    // we have just joined / moved to is the same as the currently queued audioRequest
                    // (handled by updateAudioConnection)
                    if (mng.isConnected())
                        getJDA().getDirectAudioController().update(guild, channel);
                    //If we are not already connected this will be removed by VOICE_SERVER_UPDATE
                }

                channel.getConnectedMembersMap().put(userId, member);
                oldChannel.getConnectedMembersMap().remove(userId);
                getJDA().handleEvent(
                        new GuildVoiceMoveEvent(
                                getJDA(), responseNumber,
                                member, oldChannel));
            }
        }
        if (isSelf && voiceInterceptor != null)
        {
            if (voiceInterceptor.onVoiceStateUpdate(new VoiceDispatchInterceptor.VoiceStateUpdate(channel, vState, allContent)))
                getJDA().getDirectAudioController().update(guild, channel);
        }
    }

    private MemberImpl getLazyMember(DataObject content, long userId, GuildImpl guild, boolean connected)
    {
        // Check for existing member
        Optional<DataObject> memberJson = content.optObject("member");
        MemberImpl member = (MemberImpl) guild.getMemberById(userId);
        if (!memberJson.isPresent() || userId == getJDA().getSelfUser().getIdLong())
            return member;

        // Handle cache changes
        boolean subscriptions = getJDA().isGuildSubscriptions();
        if (member == null)
        {
            if (connected && (subscriptions || getJDA().isCacheFlagSet(CacheFlag.VOICE_STATE)))
            {
                // the member just connected to a voice channel, otherwise we would know about it already!
                member = loadMember(userId, guild, memberJson.get(), "Initializing");
            }
        }
        else
        {
            if (subscriptions && member.isIncomplete())
            {
                // the member can be updated with new information that was missing before
                member = loadMember(userId, guild, memberJson.get(), "Updating");
            }
            else if (!subscriptions && !connected)
            {
                EntityBuilder.LOG.debug("Unloading member who just left a voice channel {}", memberJson);
                // the member just disconnected from the voice channel - remove it from cache
                unloadMember(userId, member);
                return null;
            }
        }
        return member;
    }

    @SuppressWarnings("ConstantConditions")
    private void unloadMember(long userId, MemberImpl member)
    {
        MemberCacheViewImpl membersView = member.getGuild().getMembersView();
        VoiceChannelImpl channelLeft = (VoiceChannelImpl) member.getVoiceState().getChannel();
        ((GuildVoiceStateImpl) member.getVoiceState()).setConnectedChannel(null);
        if (channelLeft != null)
            channelLeft.getConnectedMembersMap().remove(userId);
        getJDA().handleEvent(
            new GuildVoiceLeaveEvent(
                getJDA(), responseNumber,
                member, channelLeft));
        membersView.remove(userId);
        User user = member.getUser();
        boolean dropUser = getJDA().getGuildsView().applyStream(stream -> stream.noneMatch(it -> it.isMember(user)));
        if (dropUser)
            getJDA().getUsersView().remove(userId);
    }

    private MemberImpl loadMember(long userId, GuildImpl guild, DataObject memberJson, String comment)
    {
        EntityBuilder entityBuilder = getJDA().getEntityBuilder();
        MemberCacheViewImpl membersView = guild.getMembersView();
        SnowflakeCacheViewImpl<User> usersView = getJDA().getUsersView();
        MemberImpl member;
        EntityBuilder.LOG.debug("{} member from VOICE_STATE_UPDATE {}", comment, memberJson);
        member = entityBuilder.createMember(guild, memberJson);
        try (UnlockHook h1 = membersView.writeLock();
             UnlockHook h2 = usersView.writeLock())
        {
            membersView.getMap().put(userId, member);
            usersView.getMap().put(userId, member.getUser());
        }
        return member;
    }
}
