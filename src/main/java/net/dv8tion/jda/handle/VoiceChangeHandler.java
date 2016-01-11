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

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.VoiceChannelImpl;
import net.dv8tion.jda.entities.impl.VoiceStatusImpl;
import net.dv8tion.jda.events.voice.*;
import org.json.JSONObject;

public class VoiceChangeHandler extends SocketHandler
{
    public VoiceChangeHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        User user = api.getUserMap().get(content.getString("user_id"));
        if (user == null)
        {
            if (!content.isNull("channel_id"))
                throw new IllegalArgumentException("Received a VOICE_STATE_UPDATE for an unknown User! JSON: " + content);
            else
                return; //This is most likely a VOICE_STATE_UPDATE telling us that a user that left/was kicked/was banned
                        // has been disconnected from the VoiceChannel they were in.
                        //The VoiceLeaveEvent has already been handled by GuildMemberRemoveHandler
        }

        Guild guild = api.getGuildMap().get(content.getString("guild_id"));
        if (guild == null)
            throw new IllegalArgumentException("Received a VOICE_STATE_UPDATE for an unknown Guild! JSON: " + content);

        VoiceStatusImpl status = (VoiceStatusImpl) guild.getVoiceStatusOfUser(user);
        if (content.isNull("channel_id"))
        {
            if (status.getChannel() != null)
            {
                VoiceChannel oldChannel = status.getChannel();
                if (oldChannel != null)
                {
                    status.setChannel(null);
                    ((VoiceChannelImpl) oldChannel).getUsersModifiable().remove(user);
                    api.getEventManager().handle(new VoiceLeaveEvent(api, responseNumber, status, oldChannel));
                }
            }
        }
        else
        {
            if (status.getChannel() == null ||
                    !content.getString("channel_id").equals(status.getChannel().getId()))
            {
                VoiceChannel oldChannel = status.getChannel();
                VoiceChannel newChannel = api.getVoiceChannelMap().get(content.getString("channel_id"));
                status.setChannel(newChannel);
                if (oldChannel != null)
                {
                    ((VoiceChannelImpl) oldChannel).getUsersModifiable().remove(user);
                    api.getEventManager().handle(new VoiceLeaveEvent(api, responseNumber, status, oldChannel));
                }
                ((VoiceChannelImpl) newChannel).getUsersModifiable().add(user);
                api.getEventManager().handle(new VoiceJoinEvent(api, responseNumber, status));
            }
        }

        //TODO: Implement event for changing of session id? Might be important...
        if (!content.isNull("session_id"))
            status.setSessionId(content.getString("session_id"));
        else
            status.setSessionId(null);

        //TODO: Implement event for changing of suppressed value? Only occurs when entering an AFK room. Maybe important...
        status.setSuppressed(content.getBoolean("suppress"));

        boolean isSelfMute = !content.isNull("self_mute") && content.getBoolean("self_mute");
        if (isSelfMute != status.isMuted())
        {
            status.setMute(!status.isMuted());
            api.getEventManager().handle(new VoiceSelfMuteEvent(api, responseNumber, status));
        }
        boolean isSelfDeaf = !content.isNull("self_deaf") && content.getBoolean("self_deaf");
        if (isSelfDeaf != status.isDeaf())
        {
            status.setDeaf(!status.isDeaf());
            api.getEventManager().handle(new VoiceSelfDeafEvent(api, responseNumber, status));
        }
        if (content.getBoolean("mute") != status.isServerMuted())
        {
            status.setServerMute(!status.isServerMuted());
            api.getEventManager().handle(new VoiceServerMuteEvent(api, responseNumber, status));
        }
        if (content.getBoolean("deaf") != status.isServerDeaf())
        {
            status.setServerDeaf(!status.isServerDeaf());
            api.getEventManager().handle(new VoiceServerDeafEvent(api, responseNumber, status));
        }
    }
}
