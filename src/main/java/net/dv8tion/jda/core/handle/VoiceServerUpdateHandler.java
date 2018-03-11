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

import net.dv8tion.jda.core.audio.AudioConnection;
import net.dv8tion.jda.core.audio.AudioWebSocket;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.WebSocketClient;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONObject;

public class VoiceServerUpdateHandler extends SocketHandler
{
    public VoiceServerUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long guildId = content.getLong("guild_id");
        Guild guild = api.getGuildMap().get(guildId);
        if (guild == null)
            throw new IllegalArgumentException("Attempted to start audio connection with Guild that doesn't exist! JSON: " + content);

        api.getClient().updateAudioConnection(guildId, guild.getSelfMember().getVoiceState().getChannel());

        if (api.getGuildLock().isLocked(guildId))
            return guildId;

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

        AudioManagerImpl audioManager = (AudioManagerImpl) guild.getAudioManager();
        MiscUtil.locked(audioManager.CONNECTION_LOCK, () ->
        {
            //Synchronized to prevent attempts to close while setting up initial objects.
            if (audioManager.isConnected())
                audioManager.prepareForRegionChange();
            if (!audioManager.isAttemptingToConnect())
            {
                WebSocketClient.LOG.debug(
                    "Received a VOICE_SERVER_UPDATE but JDA is not currently connected nor attempted to connect " +
                    "to a VoiceChannel. Assuming that this is caused by another client running on this account. " +
                    "Ignoring the event.");
                return;
            }

            AudioWebSocket socket = new AudioWebSocket(audioManager.getListenerProxy(), endpoint, api, guild, sessionId, token, audioManager.isAutoReconnect());
            AudioConnection connection = new AudioConnection(socket, audioManager.getQueuedAudioConnection());
            audioManager.setAudioConnection(connection);
            socket.startConnection();
        });
        return null;
    }
}
