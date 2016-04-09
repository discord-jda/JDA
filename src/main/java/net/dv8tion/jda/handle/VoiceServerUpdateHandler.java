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

import net.dv8tion.jda.audio.AudioConnection;
import net.dv8tion.jda.audio.AudioWebSocket;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

public class VoiceServerUpdateHandler extends SocketHandler
{
    public VoiceServerUpdateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        String endpoint = content.getString("endpoint");
        String token = content.getString("token");
        Guild guild = api.getGuildMap().get(content.getString("guild_id"));
        if (guild == null)
            throw new IllegalArgumentException("Attempted to start audio connection with Guild that doesn't exist! JSON: " + content);
        String sessionId = guild.getVoiceStatusOfUser(api.getSelfInfo()).getSessionId();
        if (sessionId == null)
            throw new IllegalArgumentException("Attempted to create audio connection without having a session ID. Did VOICE_STATE_UPDATED fail?");

        //Strip the port from the endpoint.
        endpoint = endpoint.replace(":80", "");

        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected())
            throw new IllegalStateException("Received VOICE_SERVER_UPDATE event while already connected to a VoiceChannel.\n" +
                    "Did Discord allow multi-guild / multi-channel audio while we weren't looking? O.o");
        if (!audioManager.isAttemptingToConnect())
            throw new IllegalStateException("Attempted to create an AudioConnection when we weren't expecting to create one.\n" +
                    "Did you attempt to start an audio connection...?");

        AudioWebSocket socket = new AudioWebSocket(endpoint, api, guild, sessionId, token);
        AudioConnection connection = new AudioConnection(socket, audioManager.getQueuedAudioConnection());
        audioManager.setAudioConnection(connection);
        return null;
    }
}
