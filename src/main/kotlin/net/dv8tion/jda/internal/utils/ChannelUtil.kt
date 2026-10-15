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

package net.dv8tion.jda.internal.utils

import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import java.util.EnumSet

object ChannelUtil {
    @JvmField
    val SLOWMODE_SUPPORTED: EnumSet<ChannelType> =
        EnumSet.of(
            ChannelType.TEXT,
            ChannelType.FORUM,
            ChannelType.MEDIA,
            ChannelType.GUILD_PUBLIC_THREAD,
            ChannelType.GUILD_NEWS_THREAD,
            ChannelType.GUILD_PRIVATE_THREAD,
            ChannelType.STAGE,
            ChannelType.VOICE,
        )

    @JvmField
    val NSFW_SUPPORTED: EnumSet<ChannelType> =
        EnumSet.of(
            ChannelType.TEXT,
            ChannelType.VOICE,
            ChannelType.FORUM,
            ChannelType.MEDIA,
            ChannelType.NEWS,
            ChannelType.STAGE,
        )

    @JvmField
    val TOPIC_SUPPORTED: EnumSet<ChannelType> =
        EnumSet.of(ChannelType.TEXT, ChannelType.FORUM, ChannelType.MEDIA, ChannelType.NEWS)

    @JvmField
    val POST_CONTAINERS: EnumSet<ChannelType> = EnumSet.of(ChannelType.FORUM, ChannelType.MEDIA)

    @JvmField
    val THREAD_CONTAINERS: EnumSet<ChannelType> =
        EnumSet.of(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM, ChannelType.MEDIA)

    @JvmStatic
    fun <T : Channel> safeChannelCast(
        instance: Any,
        toObjectClass: Class<T>,
    ): T = UnionUtil.safeUnionCast("channel", instance, toObjectClass)

    @JvmStatic
    @Suppress("ReturnCount") // faithfully ported comparison chain
    fun compare(
        a: GuildChannel,
        b: GuildChannel?,
    ): Int {
        Checks.notNull(b, "Channel")

        // Check thread positions
        val thisThread = a as? ThreadChannel
        val otherThread = b as? ThreadChannel

        if (thisThread != null && otherThread == null) {
            // Thread should be below its parent
            if (thisThread.parentChannel.idLong == b.idLong) {
                return 1
            }
            // Otherwise compare parents
            return thisThread.parentChannel.compareTo(b)
        }
        if (thisThread == null && otherThread != null) {
            // Thread should be below its parent
            if (otherThread.parentChannel.idLong == a.idLong) {
                return -1
            }
            // Otherwise compare parents
            return a.compareTo(otherThread.parentChannel)
        }
        if (thisThread != null && otherThread != null) {
            // If they are threads on the same channel
            if (thisThread.parentChannel.idLong == otherThread.parentChannel.idLong) {
                return b.idLong.compareTo(a.idLong) // threads are ordered ascending by age
            }
            // If they are threads on different channels
            return thisThread.parentChannel.compareTo(otherThread.parentChannel)
        }

        // Check category positions
        val thisParent = (a as? ICategorizableChannel)?.parentCategory
        val otherParent = (b as? ICategorizableChannel)?.parentCategory

        if (thisParent != null && otherParent == null) {
            if (b is Category) {
                // The other channel is the parent category of this channel
                if (b.idLong == thisParent.idLong) {
                    return 1
                }
                // The other channel is another category
                return thisParent.compareTo(b)
            }
            return 1
        }
        if (thisParent == null && otherParent != null) {
            if (a is Category) {
                // This channel is parent of other channel
                if (a.idLong == otherParent.idLong) {
                    return -1
                }
                // This channel is a category higher than the other channel's parent category
                return a.compareTo(otherParent) // safe use of recursion since no circular parents exist
            }
            return -1
        }
        // Both channels are in different categories, compare the categories instead
        if (thisParent != null && thisParent != otherParent) {
            return thisParent.compareTo(otherParent)
        }

        // Check sort bucket (text/message is above audio)
        if (a.type.sortBucket != b.type.sortBucket) {
            return a.type.sortBucket.compareTo(b.type.sortBucket)
        }

        // Check actual position
        if (b is IPositionableChannel && a is IPositionableChannel && a.positionRaw != b.positionRaw) {
            return a.positionRaw.compareTo(b.positionRaw)
        }

        // last resort by id
        return java.lang.Long.compareUnsigned(a.idLong, b.idLong)
    }
}
