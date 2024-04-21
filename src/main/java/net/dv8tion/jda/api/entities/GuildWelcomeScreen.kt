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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.managers.GuildWelcomeScreenManager
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.entities.GuildWelcomeScreenImpl
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * The welcome screen of a [Guild].
 * This welcome screen will be shown to all members after joining the Guild.
 *
 * @see Guild.retrieveWelcomeScreen
 * @see Invite.Guild.getWelcomeScreen
 */
interface GuildWelcomeScreen {
    /**
     * The [Guild], or `null` if this welcome screen came from an [Invite]
     *
     * @return The Guild, or `null`
     */
    val guild: Guild?

    @get:Nonnull
    val manager: GuildWelcomeScreenManager?

    /**
     * The server description shown in the welcome screen.
     * <br></br>This will be `null` if the welcome screen has no description.
     *
     * @return The server description shown in the welcome screen or `null`
     */
    val description: String?

    @JvmField
    @get:Nonnull
    val channels: List<Channel?>?

    /**
     * POJO for the recommended channels information provided by a welcome screen.
     * <br></br>Recommended channels are shown in the welcome screen after joining a server.
     *
     * @see GuildWelcomeScreen.getChannels
     */
    interface Channel : ISnowflake, SerializableData {
        /**
         * The [Guild], or `null` if this welcome channel came from an [Invite]
         *
         * @return The Guild, or `null`
         */
        val guild: Guild?

        /**
         * The id of this recommended channel.
         *
         * @return The id of this recommended channel
         */
        abstract override val idLong: Long

        /**
         * Returns the [GuildChannel] that is linked to this recommended channel.
         * <br></br>This will be `null` if the linked channel was deleted, or if the welcome screen comes from an [invite guild][Invite.Guild].
         *
         * @return The [GuildChannel] that is linked to this recommended channel or `null`
         */
        val channel: GuildChannel?

        @get:Nonnull
        val description: String?

        /**
         * The emoji that is used for this recommended channel.
         * <br></br>This will return `null` if no emoji was set
         *
         *
         * The emoji will always be from this guild, if not a unicode emoji
         * <br></br>**[CustomEmoji.isAnimated] will always return `false` if:**
         *
         *  * This welcome screen came from an [invite&#39;s guild][Invite.Guild]
         *  * [CacheFlag.EMOJI][net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI] is disabled
         *
         *
         * @return The emoji that is used for this recommended channel or `null`
         */
        val emoji: EmojiUnion?

        companion object {
            /**
             * Constructs a new welcome channel.
             *
             * @param  channel
             * The Discord channel to be presented to the user
             * @param  description
             * The description of the channel, must not be longer than {@value #MAX_DESCRIPTION_LENGTH}
             *
             * @throws IllegalArgumentException
             *
             *  * If the channel is null
             *  * If the description is null, blank, or longer than {@value #MAX_DESCRIPTION_LENGTH}
             *
             *
             * @return The new welcome channel
             */
            @Nonnull
            fun of(@Nonnull channel: StandardGuildChannel, @Nonnull description: String?): Channel? {
                return of(channel, description, null)
            }

            /**
             * Constructs a new welcome channel.
             *
             * @param  channel
             * The Discord channel to be presented the user
             * @param  description
             * The description of the channel, must not be longer than {@value #MAX_DESCRIPTION_LENGTH}
             * @param  emoji
             * The emoji to show beside the channel
             *
             * @throws IllegalArgumentException
             *
             *  * If the channel is null
             *  * If the description is null, blank, or longer than {@value #MAX_DESCRIPTION_LENGTH}
             *
             *
             * @return The new welcome channel
             */
            @Nonnull
            fun of(@Nonnull channel: StandardGuildChannel, @Nonnull description: String?, emoji: Emoji?): Channel? {
                Checks.notNull(channel, "Channel")
                Checks.notBlank(description, "Description")
                Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description")
                return GuildWelcomeScreenImpl.ChannelImpl(
                    channel.guild,
                    channel.getIdLong(),
                    description!!,
                    emoji as EmojiUnion?
                )
            }

            /** Maximum length of a channel description ({@value})  */
            const val MAX_DESCRIPTION_LENGTH = 42
        }
    }

    companion object {
        /** The maximum length of a welcome screen description ({@value})  */
        const val MAX_DESCRIPTION_LENGTH = 140

        /** The maximum amount of welcome channel a welcome screen can show ({@value})  */
        const val MAX_WELCOME_CHANNELS = 5
    }
}
