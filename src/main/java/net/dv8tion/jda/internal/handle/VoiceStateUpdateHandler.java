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

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.GuildVoiceStateImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.AudioChannelMixin;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

import java.time.OffsetDateTime;
import java.util.Objects;

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

        // TODO: Handle these voice states properly
        if (content.isNull("member"))
        {
            WebSocketClient.LOG.debug("Discarding VOICE_STATE_UPDATE with missing member. JSON: {}", content);
            return null;
        }

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
        boolean stream = content.getBoolean("self_stream");
        boolean video = content.getBoolean("self_video", false);
        String requestToSpeak = content.getString("request_to_speak_timestamp", null);
        OffsetDateTime requestToSpeakTime = null;
        long requestToSpeakTimestamp = 0L;
        if (requestToSpeak != null)
        {
            requestToSpeakTime = OffsetDateTime.parse(requestToSpeak);
            requestToSpeakTimestamp = requestToSpeakTime.toInstant().toEpochMilli();
        }

        Guild guild = getJDA().getGuildById(guildId);
        if (guild == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a VOICE_STATE_UPDATE for a Guild that has yet to be cached. JSON: {}", content);
            return;
        }

        AudioChannel channel = null;
        if (channelId != null) {
            channel = (AudioChannel) guild.getGuildChannelById(channelId);
        }

        if (channel == null && (channelId != null))
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received VOICE_STATE_UPDATE for an AudioChannel that has yet to be cached. JSON: {}", content);
            return;
        }


        DataObject memberJson = content.getObject("member");
        MemberImpl member = getJDA().getEntityBuilder().createMember((GuildImpl) guild, memberJson);
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
            getJDA().getEntityBuilder().updateMemberCache(member);
            getJDA().handleEvent(new GuildVoiceSelfMuteEvent(getJDA(), responseNumber, member));
        }
        if (selfDeafened != vState.isSelfDeafened())
        {
            vState.setSelfDeafened(selfDeafened);
            getJDA().getEntityBuilder().updateMemberCache(member);
            getJDA().handleEvent(new GuildVoiceSelfDeafenEvent(getJDA(), responseNumber, member));
        }
        if (guildMuted != vState.isGuildMuted())
        {
            vState.setGuildMuted(guildMuted);
            getJDA().getEntityBuilder().updateMemberCache(member);
            getJDA().handleEvent(new GuildVoiceGuildMuteEvent(getJDA(), responseNumber, member));
        }
        if (guildDeafened != vState.isGuildDeafened())
        {
            vState.setGuildDeafened(guildDeafened);
            getJDA().getEntityBuilder().updateMemberCache(member);
            getJDA().handleEvent(new GuildVoiceGuildDeafenEvent(getJDA(), responseNumber, member));
        }
        if (suppressed != vState.isSuppressed())
        {
            vState.setSuppressed(suppressed);
            getJDA().getEntityBuilder().updateMemberCache(member);
            getJDA().handleEvent(new GuildVoiceSuppressEvent(getJDA(), responseNumber, member));
        }
        if (stream != vState.isStream())
        {
            vState.setStream(stream);
            getJDA().getEntityBuilder().updateMemberCache(member);
            getJDA().handleEvent(new GuildVoiceStreamEvent(getJDA(), responseNumber, member, stream));
        }
        if (video != vState.isSendingVideo())
        {
            vState.setVideo(video);
            getJDA().getEntityBuilder().updateMemberCache(member);
            getJDA().handleEvent(new GuildVoiceVideoEvent(getJDA(), responseNumber, member, video));
        }
        if (wasMute != vState.isMuted())
            getJDA().handleEvent(new GuildVoiceMuteEvent(getJDA(), responseNumber, member));
        if (wasDeaf != vState.isDeafened())
            getJDA().handleEvent(new GuildVoiceDeafenEvent(getJDA(), responseNumber, member));
        if (requestToSpeakTimestamp != vState.getRequestToSpeak())
        {
            OffsetDateTime oldRequestToSpeak = vState.getRequestToSpeakTimestamp();
            vState.setRequestToSpeak(requestToSpeakTime);
            getJDA().handleEvent(new GuildVoiceRequestToSpeakEvent(getJDA(), responseNumber, member, oldRequestToSpeak, requestToSpeakTime));
        }

        if (!Objects.equals(channel, vState.getChannel()))
        {
            AudioChannel oldChannel = vState.getChannel();
            vState.setConnectedChannel(channel);

            if (oldChannel == null)
            {
                ((AudioChannelMixin<?>) channel).getConnectedMembersMap().put(userId, member);
                getJDA().getEntityBuilder().updateMemberCache(member);

                getJDA().handleEvent(
                    new GuildVoiceJoinEvent(
                        getJDA(), responseNumber,
                        member));
            }
            else if (channel == null)
            {
                ((AudioChannelMixin<?>) oldChannel).getConnectedMembersMap().remove(userId);
                if (isSelf)
                    getJDA().getDirectAudioController().update(guild, null);
                getJDA().getEntityBuilder().updateMemberCache(member, memberJson.isNull("joined_at"));

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
                    if (mng.isConnected())
                        mng.setConnectedChannel(channel);

                    //If we have connected (VOICE_SERVER_UPDATE received and AudioConnection created (actual connection might still be setting up)),
                    // then we need to stop sending audioOpen/Move requests through the MainWS if the channel
                    // we have just joined / moved to is the same as the currently queued audioRequest
                    // (handled by updateAudioConnection)
                    if (mng.isConnected())
                        getJDA().getDirectAudioController().update(guild, channel);
                    //If we are not already connected this will be removed by VOICE_SERVER_UPDATE
                }

                ((AudioChannelMixin<?>) channel).getConnectedMembersMap().put(userId, member);
                ((AudioChannelMixin<?>) oldChannel).getConnectedMembersMap().remove(userId);
                getJDA().getEntityBuilder().updateMemberCache(member);

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

        ((GuildImpl) guild).updateRequestToSpeak();
    }
}
