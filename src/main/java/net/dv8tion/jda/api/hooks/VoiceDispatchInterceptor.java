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

package net.dv8tion.jda.api.hooks;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interceptor used to handle critical voice dispatches.
 * <br>This will make it impossible to connect to voice channels with
 * the built-in {@link net.dv8tion.jda.api.managers.AudioManager AudioManager}.
 * It is expected that the user has some other means of establishing voice connections when this is used.
 *
 * @since  4.0.0
 */
public interface VoiceDispatchInterceptor
{
    /**
     * Handles the <b>VOICE_SERVER_UPDATE</b>.
     *
     * @param update
     *        The {@link VoiceServerUpdate} to handle
     */
    void onVoiceServerUpdate(@Nonnull VoiceServerUpdate update);

    /**
     * Handles the <b>VOICE_STATE_UPDATE</b>.
     * <br>This indicates the user might have moved to a new voice channel.
     *
     * @param  update
     *         The {@link VoiceStateUpdate} to handle
     *
     * @return True, if a connection was previously established
     */
    boolean onVoiceStateUpdate(@Nonnull VoiceStateUpdate update);

    /**
     * Abstraction for all relevant voice updates
     *
     * @see VoiceServerUpdate
     * @see VoiceStateUpdate
     */
    interface VoiceUpdate extends SerializableData
    {
        /**
         * The {@link Guild} for this update
         *
         * @return The guild
         */
        @Nonnull
        Guild getGuild();

        /**
         * The raw JSON object that was parsed from this update
         *
         * @return The raw JSON object
         */
        @Nonnull
        @Override
        DataObject toData();

        /**
         * Shortcut to access the audio controller of this JDA instance
         *
         * @return The {@link DirectAudioController} for this JDA instance
         */
        @Nonnull
        default DirectAudioController getAudioController()
        {
            return getJDA().getDirectAudioController();
        }

        /**
         * Shortcut to access the guild id
         *
         * @return The guild id
         */
        default long getGuildIdLong()
        {
            return getGuild().getIdLong();
        }

        /**
         * Shortcut to access the guild id
         *
         * @return The guild id
         */
        @Nonnull
        default String getGuildId()
        {
            return Long.toUnsignedString(getGuildIdLong());
        }

        /**
         * Shortcut to access the JDA instance
         *
         * @return The JDA instance
         */
        @Nonnull
        default JDA getJDA()
        {
            return getGuild().getJDA();
        }

        /**
         * Shortcut to access the shard info for this JDA instance
         *
         * @return The shard information, or null if this was not for a sharded client
         */
        @Nullable
        default JDA.ShardInfo getShardInfo()
        {
            return getJDA().getShardInfo();
        }
    }

    /**
     * Wrapper for a <a href="https://discord.com/developers/docs/topics/gateway#voice-server-update" target="_blank">Voice Server Update</a>
     */
    class VoiceServerUpdate implements VoiceUpdate
    {
        private final Guild guild;
        private final String endpoint;
        private final String token;
        private final String sessionId;
        private final DataObject json;

        public VoiceServerUpdate(Guild guild, String endpoint, String token, String sessionId, DataObject json)
        {
            this.guild = guild;
            this.endpoint = endpoint;
            this.token = token;
            this.sessionId = sessionId;
            this.json = json;
        }

        @Nonnull
        @Override
        public Guild getGuild()
        {
            return guild;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            return json;
        }

        /**
         * The voice server endpoint
         *
         * @return The endpoint
         */
        @Nonnull
        public String getEndpoint()
        {
            return endpoint;
        }

        /**
         * The access token for the voice server connection
         *
         * @return The access token
         */
        @Nonnull
        public String getToken()
        {
            return token;
        }

        /**
         * The session id for the voice server session
         *
         * @return The session id
         */
        @Nonnull
        public String getSessionId()
        {
            return sessionId;
        }
    }

    /**
     * Wrapper for a <a href="https://discord.com/developers/docs/topics/gateway#voice-state-update" target="_blank">Voice State Update</a>
     */
    class VoiceStateUpdate implements VoiceUpdate
    {
        private final AudioChannel channel;
        private final GuildVoiceState voiceState;
        private final DataObject json;

        public VoiceStateUpdate(AudioChannel channel, GuildVoiceState voiceState, DataObject json)
        {
            this.channel = channel;
            this.voiceState = voiceState;
            this.json = json;
        }

        @Nonnull
        @Override
        public Guild getGuild()
        {
            return voiceState.getGuild();
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            return json;
        }

        /**
         * The update voice channel
         *
         * @return The updated voice channel, or null to signal disconnect
         */
        @Nullable
        public AudioChannel getChannel()
        {
            return channel;
        }

        /**
         * The voice state for the guild
         *
         * @return The voice state
         */
        @Nonnull
        public GuildVoiceState getVoiceState()
        {
            return voiceState;
        }
    }
}
