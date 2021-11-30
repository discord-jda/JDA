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
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.audio.AudioConnection;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class VoiceServerUpdateHandler extends SocketHandler
{
    public VoiceServerUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long guildId = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;
        Guild guild = getJDA().getGuildById(guildId);
        if (guild == null)
            throw new IllegalArgumentException("Attempted to start audio connection with Guild that doesn't exist!");

        getJDA().getDirectAudioController().update(guild, guild.getSelfMember().getVoiceState().getChannel());

        if (content.isNull("endpoint"))
        {
            //Discord did not provide an endpoint yet, we are to wait until discord has resources to provide
            // an endpoint, which will result in them sending another VOICE_SERVER_UPDATE which we will handle
            // to actually connect to the audio server.
            return null;
        }

        //Strip the port from the endpoint.
        String endpoint = content.getString("endpoint").replace(":80", "");
        String token = content.getString("token");
        String sessionId = guild.getSelfMember().getVoiceState().getSessionId();
        if (sessionId == null)
            throw new IllegalArgumentException("Attempted to create audio connection without having a session ID. Did VOICE_STATE_UPDATED fail?");

        VoiceDispatchInterceptor voiceInterceptor = getJDA().getVoiceInterceptor();
        if (voiceInterceptor != null)
        {
            voiceInterceptor.onVoiceServerUpdate(new VoiceDispatchInterceptor.VoiceServerUpdate(guild, endpoint, token, sessionId, allContent));
            return null;
        }

        AudioManagerImpl audioManager = (AudioManagerImpl) getJDA().getAudioManagersView().get(guildId);
        if (audioManager == null)
        {
            WebSocketClient.LOG.debug(
                "Received a VOICE_SERVER_UPDATE but JDA is not currently connected nor attempted to connect " +
                "to a VoiceChannel. Assuming that this is caused by another client running on this account. " +
                "Ignoring the event.");
            return null;
        }

        MiscUtil.locked(audioManager.CONNECTION_LOCK, () ->
        {
            //Synchronized to prevent attempts to close while setting up initial objects.
            AudioChannel target = guild.getSelfMember().getVoiceState().getChannel();
            if (target == null)
            {
                WebSocketClient.LOG.warn("Ignoring VOICE_SERVER_UPDATE for unknown channel");
                return;
            }

            AudioConnection connection = new AudioConnection(audioManager, endpoint, sessionId, token, target);
            audioManager.setAudioConnection(connection);
            connection.startConnection();
        });
        return null;
    }
}
