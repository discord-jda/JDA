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

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

/**
 * Flags for specific channel settings.
 */
enum class ChannelFlag(
    /**
     * The raw bitset value of this flag.
     *
     * @return The raw value
     */
    val raw: Int
) {
    /**
     * This is a forum post [ThreadChannel] which is pinned in the [ForumChannel].
     */
    PINNED(1 shl 1),

    /**
     * This is a [ForumChannel] which requires all new post threads to have at least one applied tag.
     */
    REQUIRE_TAG(1 shl 4),

    /**
     * This is a [MediaChannel][net.dv8tion.jda.api.entities.channel.concrete.MediaChannel] which hides the copy embed option.
     */
    HIDE_MEDIA_DOWNLOAD_OPTIONS(1 shl 15);

    companion object {
        /**
         * Parses the provided bitset to the corresponding enum constants.
         *
         * @param  bitset
         * The bitset of channel flags
         *
         * @return The enum constants of the provided bitset
         */
        @JvmStatic
        @Nonnull
        fun fromRaw(bitset: Int): EnumSet<ChannelFlag> {
            val set = EnumSet.noneOf(ChannelFlag::class.java)
            if (bitset == 0) return set
            for (flag in entries) {
                if (flag.raw == bitset) set.add(flag)
            }
            return set
        }

        /**
         * The raw bitset value for the provided flags.
         *
         * @return The raw value
         */
        fun getRaw(@Nonnull flags: Collection<ChannelFlag>): Int {
            Checks.notNull(flags, "Flags")
            var raw = 0
            for (flag in flags) raw = raw or flag.raw
            return raw
        }
    }
}
