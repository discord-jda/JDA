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

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.client.entities.CallableChannel;
import net.dv8tion.jda.client.entities.impl.CallImpl;
import net.dv8tion.jda.client.entities.impl.CallVoiceStateImpl;
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.client.events.call.voice.CallVoiceJoinEvent;
import net.dv8tion.jda.client.events.call.voice.CallVoiceLeaveEvent;
import net.dv8tion.jda.client.events.call.voice.CallVoiceSelfDeafenEvent;
import net.dv8tion.jda.client.events.call.voice.CallVoiceSelfMuteEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.GuildVoiceStateImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MemberImpl;
import net.dv8tion.jda.core.entities.impl.VoiceChannelImpl;
import net.dv8tion.jda.core.events.guild.voice.*;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.GuildLock;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

import java.util.Objects;

public class VoiceStateUpdateHandler extends SocketHandler
{
    public VoiceStateUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String guildId = content.has("guild_id") ? content.getString("guild_id") : null;
        if (guildId != null && GuildLock.get(api).isLocked(guildId))
        {
            return guildId;
        }

        if (guildId != null)
        {
            handleGuildVoiceState(content);
        }
        else
        {
            handleCallVoiceState(content);
        }
        return null;
    }

    private void handleGuildVoiceState(JSONObject content)
    {
        String userId = content.getString("user_id");
        String guildId = content.getString("guild_id");
        String channelId = !content.isNull("channel_id") ? content.getString("channel_id") : null;
        String sessionId = !content.isNull("session_id") ? content.getString("session_id") : null;
        boolean selfMuted = content.getBoolean("self_mute");
        boolean selfDeafened = content.getBoolean("self_deaf");
        boolean guildMuted = content.getBoolean("mute");
        boolean guildDeafened = content.getBoolean("deaf");
        boolean suppressed = content.getBoolean("suppress");

        Guild guild = api.getGuildById(guildId);
        if (guild == null)
        {
            EventCache.get(api).cache(EventCache.Type.GUILD, content.getString("guild_id"), () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received a VOICE_STATE_UPDATE for a Guild that has yet to be cached. JSON: " + content);
            return;
        }

        VoiceChannelImpl channel = (VoiceChannelImpl) guild.getVoiceChannelById(channelId);
        if (channel == null && channelId != null)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received VOICE_STATE_UPDATE for a VoiceChannel that has yet to be cached. JSON: " + content);
            return;
        }

        MemberImpl member = (MemberImpl) guild.getMemberById(userId);
        if (member == null)
        {
            //Caching of this might not be valid. It is possible that we received this
            // update due to this Member leaving the guild while still connected to a voice channel.
            // In that case, we should not cache this because it could cause problems if they rejoined.
            //However, we can't just ignore it completely because it could be a user that joined off of
            // an invite to a VoiceChannel, so the GUILD_MEMBER_ADD and the VOICE_STATE_UPDATE may have
            // come out of order. Not quite sure what to do. Going to cache for now however.
            //At the worst, this will just cause a few events to fire with bad data if the member rejoins the guild if
            // in fact the issue was that the VOICE_STATE_UPDATE was sent after they had left, however, by caching
            // it we will preserve the integrity of the cache in the event that it was actually a mis-ordering of
            // GUILD_MEMBER_ADD and VOICE_STATE_UPDATE. I'll take some bad-data events over an invalid cache.
            EventCache.get(api).cache(EventCache.Type.USER, userId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received VOICE_STATE_UPDATE for a Member that has yet to be cached. JSON: " + content);
            return;
        }

        GuildVoiceStateImpl vState = (GuildVoiceStateImpl) member.getVoiceState();
        vState.setSessionId(sessionId); //Cant really see a reason for an event for this

        if (!Objects.equals(channel, vState.getChannel()))
        {
            VoiceChannelImpl oldChannel = (VoiceChannelImpl) vState.getChannel();
            vState.setConnectedChannel(channel);

            if (oldChannel == null)
            {
                channel.getConnectedMembersMap().put(userId, member);
                api.getEventManager().handle(
                        new GuildVoiceJoinEvent(
                                api, responseNumber,
                                member));
            }
            else if (channel == null)
            {
                oldChannel.getConnectedMembersMap().remove(userId);
                api.getEventManager().handle(
                        new GuildVoiceLeaveEvent(
                                api, responseNumber,
                                member, oldChannel));
            }
            else
            {
                //If the connect account is the one that is being moved, and this instance of JDA
                // is connected or attempting to connect, them change the channel we expect to be connected to.
                if (guild.getSelfMember().equals(member))
                {
                    AudioManagerImpl mng = (AudioManagerImpl) api.getAudioManagerMap().get(guildId);
                    if (mng != null && (mng.isConnected() || mng.isAttemptingToConnect()))
                        mng.setConnectedChannel(channel);
                }

                channel.getConnectedMembersMap().put(userId, member);
                oldChannel.getConnectedMembersMap().remove(userId);
                api.getEventManager().handle(
                        new GuildVoiceMoveEvent(
                                api, responseNumber,
                                member, oldChannel));
            }
        }

        boolean wasMute = vState.isMuted();
        boolean wasDeaf = vState.isDeafened();

        if (selfMuted != vState.isSelfMuted())
        {
            vState.setSelfMuted(selfMuted);
            api.getEventManager().handle(new GuildVoiceSelfMuteEvent(api, responseNumber, member));
        }
        if (selfDeafened != vState.isSelfDeafened())
        {
            vState.setSelfDeafened(selfDeafened);
            api.getEventManager().handle(new GuildVoiceSelfDeafenEvent(api, responseNumber, member));
        }
        if (guildMuted != vState.isGuildMuted())
        {
            vState.setGuildMuted(guildMuted);
            api.getEventManager().handle(new GuildVoiceGuildMuteEvent(api, responseNumber, member));
        }
        if (guildDeafened != vState.isGuildDeafened())
        {
            vState.setGuildDeafened(guildDeafened);
            api.getEventManager().handle(new GuildVoiceGuildDeafenEvent(api, responseNumber, member));
        }
        if (suppressed != vState.isSuppressed())
        {
            vState.setSuppressed(suppressed);
            api.getEventManager().handle(new GuildVoiceSuppressEvent(api, responseNumber, member));
        }
        if (wasMute != vState.isMuted())
            api.getEventManager().handle(new GuildVoiceMuteEvent(api, responseNumber, member));
        if (wasDeaf != vState.isDeafened())
            api.getEventManager().handle(new GuildVoiceDeafenEvent(api, responseNumber, member));
    }

    private void handleCallVoiceState(JSONObject content)
    {
        String userId = content.getString("user_id");
        String channelId = !content.isNull("channel_id") ? content.getString("channel_id") : null;
        String sessionId = !content.isNull("session_id") ? content.getString("session_id") : null;
        boolean selfMuted = content.getBoolean("self_mute");
        boolean selfDeafened = content.getBoolean("self_deaf");

        //Joining a call
        CallableChannel channel;
        CallVoiceStateImpl vState;
        if (channelId != null)
        {
            channel = api.asClient().getGroupById(channelId);
            if (channel == null)
                channel = api.getPrivateChannelMap().get(channelId);

            if (channel == null)
            {
                EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                EventCache.LOG.debug("Received a VOICE_STATE_UPDATE for a Group/PrivateChannel that was not yet cached! JSON: " + content);
                return;
            }

            CallImpl call = (CallImpl) channel.getCurrentCall();
            if (call == null)
            {
                EventCache.get(api).cache(EventCache.Type.CALL, channelId, () -> handle(responseNumber, allContent));
                EventCache.LOG.debug("Received a VOICE_STATE_UPDATE for a Call that is not yet cached. JSON: " + content);
                return;
            }

            CallUser cUser = ((JDAClientImpl) api.asClient()).getCallUserMap().get(userId);
            if (cUser != null && !channelId.equals(cUser.getCall().getCallableChannel().getId()))
            {
                WebSocketClient.LOG.fatal("Received a VOICE_STATE_UPDATE for a user joining a call, but the user was already in a different call! Big error! JSON: " + content);
                ((CallVoiceStateImpl) cUser.getVoiceState()).setInCall(false);
            }

            cUser = call.getCallUserMap().get(userId);
            if (cUser == null)
            {
                EventCache.get(api).cache(EventCache.Type.USER, userId, () -> handle(responseNumber, allContent));
                EventCache.LOG.debug("Received a VOICE_STATE_UPDATE for a user that is not yet a a cached CallUser for the call. (groups only). JSON: " + content);
                return;
            }

            ((JDAClientImpl) api.asClient()).getCallUserMap().put(userId, cUser);
            vState = (CallVoiceStateImpl) cUser.getVoiceState();
            vState.setSessionId(sessionId);
            vState.setInCall(true);

            api.getEventManager().handle(
                    new CallVoiceJoinEvent(
                            api, responseNumber,
                            cUser));
        }
        else //Leaving a call
        {
            CallUser cUser = ((JDAClientImpl) api.asClient()).getCallUserMap().remove(userId);
            if (cUser == null)
            {
                EventCache.get(api).cache(EventCache.Type.USER, userId, () -> handle(responseNumber, allContent));
                EventCache.LOG.debug("Received a VOICE_STATE_UPDATE for a User leaving a Call, but the Call was not yet cached! JSON: " + content);
                return;
            }

            Call call = cUser.getCall();
            channel = call.getCallableChannel();
            vState = (CallVoiceStateImpl) cUser.getVoiceState();
            vState.setSessionId(sessionId);
            vState.setInCall(false);

            api.getEventManager().handle(
                    new CallVoiceLeaveEvent(
                            api, responseNumber,
                            cUser));
        }

        //Now that we're done dealing with the joins and leaves, we can deal with the mute/deaf changes.
        if (selfMuted != vState.isSelfMuted())
        {
            vState.setSelfMuted(selfMuted);
            api.getEventManager().handle(new CallVoiceSelfMuteEvent(api, responseNumber, vState.getCallUser()));
        }
        if (selfDeafened != vState.isSelfDeafened())
        {
            vState.setSelfDeafened(selfDeafened);
            api.getEventManager().handle(new CallVoiceSelfDeafenEvent(api, responseNumber, vState.getCallUser()));
        }
    }
}
