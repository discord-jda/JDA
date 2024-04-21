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
package net.dv8tion.jda.api.entities.channel

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.*
import java.util.*
import javax.annotation.Nonnull

/**
 * Enum used to differentiate between the different types of Discord channels.
 */
enum class ChannelType @JvmOverloads constructor(
    /**
     * The interface this channel type corresponds to.
     *
     * @return This channel type's interface
     */
    @get:Nonnull val `interface`: Class<out Channel>,
    /**
     * The Discord id key used to represent the channel type.
     *
     * @return The id key used by discord for this channel type.
     */
    @JvmField val id: Int,
    /**
     * The sorting bucket for this channel type.
     *
     * @return The sorting bucket
     */
    @JvmField val sortBucket: Int,
    /**
     * Whether this ChannelType is present for a [GuildChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildChannel]
     *
     * @return Whether or not this a GuildChannel
     */
    @JvmField val isGuild: Boolean = false
) {
    /**
     * A [TextChannel], Guild-Only.
     */
    TEXT(TextChannel::class.java, 0, 0, true),

    /**
     * A [PrivateChannel].
     */
    PRIVATE(PrivateChannel::class.java, 1, -1),

    /**
     * A [VoiceChannel], Guild-Only.
     */
    VOICE(VoiceChannel::class.java, 2, 1, true),

    /**
     * A Group. (unused)
     */
    GROUP(GroupChannel::class.java, 3, -1),

    /**
     * A [Category], Guild-Only.
     */
    CATEGORY(Category::class.java, 4, 2, true),

    /**
     * A [NewsChannel], Guild-Only.
     */
    NEWS(NewsChannel::class.java, 5, 0, true),

    /**
     * A [StageChannel], Guild-Only.
     */
    STAGE(StageChannel::class.java, 13, 1, true),
    GUILD_NEWS_THREAD(ThreadChannel::class.java, 10, -1, true),
    GUILD_PUBLIC_THREAD(ThreadChannel::class.java, 11, -1, true),
    GUILD_PRIVATE_THREAD(ThreadChannel::class.java, 12, -1, true),

    /**
     * A [ForumChannel][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel], Guild-Only.
     */
    FORUM(ForumChannel::class.java, 15, 0, true),

    /**
     * A [MediaChannel], Guild-Only.
     */
    MEDIA(MediaChannel::class.java, 16, 0, true),

    /**
     * Unknown Discord channel type.
     *
     *
     * This might be used in the case when a channel is not available in cache, like when sending webhook messages.
     */
    UNKNOWN(Channel::class.java, -1, -2);

    val isAudio: Boolean
        /**
         * Whether channels of this type support audio connections.
         *
         * @return True, if channels of this type support audio
         */
        get() = when (this) {
            VOICE, STAGE -> true
            else -> false
        }
    val isMessage: Boolean
        /**
         * Whether channels of this type support message sending.
         *
         * @return True, if channels of this type support messages
         */
        get() = when (this) {
            TEXT, VOICE, STAGE, NEWS, PRIVATE, GROUP -> true
            else -> isThread
        }
    val isThread: Boolean
        /**
         * Whether channels of this type are [ThreadChannels][ThreadChannel].
         * This mostly exists to make handling threads simpler than having to check 3 separate ChannelTypes every time.
         *
         * @return True, if channels of this type are [ThreadChannel]
         */
        get() = when (this) {
            GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD -> true
            else -> false
        }

    companion object {
        /**
         * All the channel types for a [Guild][net.dv8tion.jda.api.entities.Guild].
         *
         * @return [EnumSet] of [ChannelType]
         */
        @Nonnull
        fun guildTypes(): EnumSet<ChannelType> {
            return EnumSet.complementOf(EnumSet.of(PRIVATE, GROUP, UNKNOWN))
        }

        /**
         * Static accessor for retrieving a channel type based on its Discord id key.
         *
         * @param  id
         * The id key of the requested channel type.
         *
         * @return The ChannelType that is referred to by the provided key. If the id key is unknown, [.UNKNOWN] is returned.
         */
        @JvmStatic
        @Nonnull
        fun fromId(id: Int): ChannelType {
            for (type in entries) {
                if (type.id == id) return type
            }
            return UNKNOWN
        }

        /**
         * An [java.util.EnumSet] populated with all channel types using the provided sorting bucket.
         *
         * @param  bucket
         * The sorting bucket
         *
         * @return Possibly-empty [java.util.EnumSet] for the bucket
         */
        @JvmStatic
        @Nonnull
        fun fromSortBucket(bucket: Int): EnumSet<ChannelType> {
            val types = EnumSet.noneOf(ChannelType::class.java)
            for (type in entries) {
                if (type.sortBucket == bucket) types.add(type)
            }
            return types
        }
    }
}
