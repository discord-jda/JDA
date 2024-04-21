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
package net.dv8tion.jda.api.hooks

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDA.ShardInfo
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.managers.DirectAudioController
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import javax.annotation.Nonnull

/**
 * Interceptor used to handle critical voice dispatches.
 * <br></br>This will make it impossible to connect to voice channels with
 * the built-in [AudioManager][net.dv8tion.jda.api.managers.AudioManager].
 * It is expected that the user has some other means of establishing voice connections when this is used.
 *
 * @since  4.0.0
 */
interface VoiceDispatchInterceptor {
    /**
     * Handles the **VOICE_SERVER_UPDATE**.
     *
     * @param update
     * The [VoiceServerUpdate] to handle
     */
    fun onVoiceServerUpdate(@Nonnull update: VoiceServerUpdate?)

    /**
     * Handles the **VOICE_STATE_UPDATE**.
     * <br></br>This indicates the user might have moved to a new voice channel.
     *
     * @param  update
     * The [VoiceStateUpdate] to handle
     *
     * @return True, if a connection was previously established
     */
    fun onVoiceStateUpdate(@Nonnull update: VoiceStateUpdate?): Boolean

    /**
     * Abstraction for all relevant voice updates
     *
     * @see VoiceServerUpdate
     *
     * @see VoiceStateUpdate
     */
    interface VoiceUpdate : SerializableData {
        @get:Nonnull
        val guild: Guild?

        /**
         * The raw JSON object that was parsed from this update
         *
         * @return The raw JSON object
         */
        @Nonnull
        override fun toData(): DataObject

        @get:Nonnull
        val audioController: DirectAudioController?
            /**
             * Shortcut to access the audio controller of this JDA instance
             *
             * @return The [DirectAudioController] for this JDA instance
             */
            get() = jDA.directAudioController
        val guildIdLong: Long
            /**
             * Shortcut to access the guild id
             *
             * @return The guild id
             */
            get() = guild!!.idLong

        @get:Nonnull
        val guildId: String?
            /**
             * Shortcut to access the guild id
             *
             * @return The guild id
             */
            get() = java.lang.Long.toUnsignedString(guildIdLong)

        @get:Nonnull
        val jDA: JDA
            /**
             * Shortcut to access the JDA instance
             *
             * @return The JDA instance
             */
            get() = guild.getJDA()
        val shardInfo: ShardInfo?
            /**
             * Shortcut to access the shard info for this JDA instance
             *
             * @return The shard information, or null if this was not for a sharded client
             */
            get() = jDA.getShardInfo()
    }

    /**
     * Wrapper for a [Voice Server Update](https://discord.com/developers/docs/topics/gateway#voice-server-update)
     */
    class VoiceServerUpdate(
        private override val guild: Guild,
        /**
         * The voice server endpoint
         *
         * @return The endpoint
         */
        @get:Nonnull val endpoint: String,
        /**
         * The access token for the voice server connection
         *
         * @return The access token
         */
        @get:Nonnull val token: String,
        /**
         * The session id for the voice server session
         *
         * @return The session id
         */
        @get:Nonnull val sessionId: String, private val json: DataObject
    ) : VoiceUpdate {

        @Nonnull
        override fun getGuild(): Guild? {
            return guild
        }

        @Nonnull
        override fun toData(): DataObject {
            return json
        }
    }

    /**
     * Wrapper for a [Voice State Update](https://discord.com/developers/docs/topics/gateway#voice-state-update)
     */
    class VoiceStateUpdate(
        private val channel: AudioChannel,
        /**
         * The voice state for the guild
         *
         * @return The voice state
         */
        @get:Nonnull val voiceState: GuildVoiceState, private val json: DataObject
    ) : VoiceUpdate {
        @get:Nonnull
        override val guild: Guild?
            get() = voiceState.guild

        @Nonnull
        override fun toData(): DataObject {
            return json
        }

        /**
         * The update voice channel
         *
         * @return The updated voice channel, or null to signal disconnect
         */
        fun getChannel(): AudioChannel? {
            return channel
        }
    }
}
