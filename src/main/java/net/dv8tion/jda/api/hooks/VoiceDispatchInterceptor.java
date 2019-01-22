/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.api.hooks;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.json.JSONObject;
import org.json.JSONString;

import javax.annotation.Nonnull;

/**
 * Interceptor used to handle critical voice dispatches.
 * <br>This will make it impossible to connect to voice channels with
 * the built-in {@link net.dv8tion.jda.api.managers.AudioManager AudioManager}.
 * It is expected that the user has some other means of establishing voice connections when this is used.
 */
public interface VoiceDispatchInterceptor
{
    /**
     * Handles the <b>VOICE_SERVER_UPDATE</b>.
     *
     * @param update
     *        The {@link VoiceServerUpdate} to handle
     */
    void interceptServerUpdate(@Nonnull VoiceServerUpdate update);

    /**
     * Handles the <b>VOICE_STATE_UPDATE</b>.
     * <br>This indicates the user might have moved to a new voice channel.
     *
     * @param  update
     *         The {@link VoiceStateUpdate} to handle
     *
     * @return True, if a connection was previously established
     */
    boolean interceptStateUpdate(@Nonnull VoiceStateUpdate update);

    interface VoiceUpdate extends JSONString
    {
        Guild getGuild();
        JSONObject getJSON();

        @Override
        default String toJSONString()
        {
            return getJSON().toString();
        }

        default long getGuildIdLong()
        {
            return getGuild().getIdLong();
        }

        default String getGuildId()
        {
            return Long.toUnsignedString(getGuildIdLong());
        }

        default JDA getJDA()
        {
            return getGuild().getJDA();
        }

        default JDA.ShardInfo getShardInfo()
        {
            return getJDA().getShardInfo();
        }
    }

    class VoiceServerUpdate implements VoiceUpdate
    {
        private final Guild guild;
        private final String endpoint;
        private final String token;
        private final String sessionId;
        private final JSONObject json;

        public VoiceServerUpdate(Guild guild, String endpoint, String token, String sessionId, JSONObject json)
        {
            this.guild = guild;
            this.endpoint = endpoint;
            this.token = token;
            this.sessionId = sessionId;
            this.json = json;
        }

        public Guild getGuild()
        {
            return guild;
        }

        public JSONObject getJSON()
        {
            return json;
        }

        public String getEndpoint()
        {
            return endpoint;
        }

        public String getToken()
        {
            return token;
        }

        public String getSessionId()
        {
            return sessionId;
        }
    }

    class VoiceStateUpdate implements VoiceUpdate
    {
        private final Guild guild;
        private final VoiceChannel channel;
        private final GuildVoiceState voiceState;
        private final JSONObject json;

        public VoiceStateUpdate(Guild guild, VoiceChannel channel, GuildVoiceState voiceState, JSONObject json)
        {
            this.guild = guild;
            this.channel = channel;
            this.voiceState = voiceState;
            this.json = json;
        }

        public Guild getGuild()
        {
            return guild;
        }

        public JSONObject getJSON()
        {
            return json;
        }

        public VoiceChannel getChannel()
        {
            return channel;
        }

        public GuildVoiceState getVoiceState()
        {
            return voiceState;
        }
    }
}
