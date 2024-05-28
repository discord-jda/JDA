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

package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.EnumSet;

public class ChannelUtil
{
    public static final EnumSet<ChannelType> SLOWMODE_SUPPORTED = EnumSet.of(
        ChannelType.TEXT, ChannelType.FORUM, ChannelType.MEDIA,
        ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PRIVATE_THREAD,
        ChannelType.STAGE, ChannelType.VOICE
    );

    public static final EnumSet<ChannelType> NSFW_SUPPORTED = EnumSet.of(ChannelType.TEXT, ChannelType.VOICE, ChannelType.FORUM, ChannelType.MEDIA, ChannelType.NEWS, ChannelType.STAGE);

    public static final EnumSet<ChannelType> TOPIC_SUPPORTED = EnumSet.of(ChannelType.TEXT, ChannelType.FORUM, ChannelType.MEDIA, ChannelType.NEWS);

    public static final EnumSet<ChannelType> POST_CONTAINERS = EnumSet.of(ChannelType.FORUM, ChannelType.MEDIA);

    public static final EnumSet<ChannelType> THREAD_CONTAINERS = EnumSet.of(ChannelType.TEXT, ChannelType.NEWS, ChannelType.FORUM, ChannelType.MEDIA);

    public static <T extends Channel> T safeChannelCast(Object instance, Class<T> toObjectClass)
    {
        if (toObjectClass.isInstance(instance))
            return toObjectClass.cast(instance);

        String cleanedClassName = instance.getClass().getSimpleName().replace("Impl", "");
        throw new IllegalStateException(Helpers.format("Cannot convert channel of type %s to %s!", cleanedClassName, toObjectClass.getSimpleName()));
    }

    public static int compare(GuildChannel a, GuildChannel b)
    {
        Checks.notNull(b, "Channel");

        // Check thread positions
        ThreadChannel thisThread = a instanceof ThreadChannel ? (ThreadChannel) a : null;
        ThreadChannel otherThread = b instanceof ThreadChannel ? (ThreadChannel) b : null;

        if (thisThread != null && otherThread == null)
        {
            // Thread should be below its parent
            if (thisThread.getParentChannel().getIdLong() == b.getIdLong())
                return 1;
            // Otherwise compare parents
            return thisThread.getParentChannel().compareTo(b);
        }
        if (thisThread == null && otherThread != null)
        {
            // Thread should be below its parent
            if (otherThread.getParentChannel().getIdLong() == a.getIdLong())
                return -1;
            // Otherwise compare parents
            return a.compareTo(otherThread.getParentChannel());
        }
        if (thisThread != null)
        {
            // If they are threads on the same channel
            if (thisThread.getParentChannel().getIdLong() == otherThread.getParentChannel().getIdLong())
                return Long.compare(b.getIdLong(), a.getIdLong()); // threads are ordered ascending by age
            // If they are threads on different channels
            return thisThread.getParentChannel().compareTo(otherThread.getParentChannel());
        }

        // Check category positions
        Category thisParent = a instanceof ICategorizableChannel ? ((ICategorizableChannel) a).getParentCategory() : null;
        Category otherParent = b instanceof ICategorizableChannel ? ((ICategorizableChannel) b).getParentCategory() : null;

        if (thisParent != null && otherParent == null)
        {
            if (b instanceof Category)
            {
                // The other channel is the parent category of this channel
                if (b.getIdLong() == thisParent.getIdLong())
                    return 1;
                // The other channel is another category
                return thisParent.compareTo(b);
            }
            return 1;
        }
        if (thisParent == null && otherParent != null)
        {
            if (a instanceof Category)
            {
                // This channel is parent of other channel
                if (a.getIdLong() == otherParent.getIdLong())
                    return -1;
                // This channel is a category higher than the other channel's parent category
                return a.compareTo(otherParent); //safe use of recursion since no circular parents exist
            }
            return -1;
        }
        // Both channels are in different categories, compare the categories instead
        if (thisParent != null && !thisParent.equals(otherParent))
            return thisParent.compareTo(otherParent);

        // Check sort bucket (text/message is above audio)
        if (a.getType().getSortBucket() != b.getType().getSortBucket())
            return Integer.compare(a.getType().getSortBucket(), b.getType().getSortBucket());

        // Check actual position
        if (b instanceof IPositionableChannel && a instanceof IPositionableChannel)
        {
            IPositionableChannel oPositionableChannel = (IPositionableChannel) b;
            IPositionableChannel thisPositionableChannel = (IPositionableChannel) a;

            if (thisPositionableChannel.getPositionRaw() != oPositionableChannel.getPositionRaw())
                return Integer.compare(thisPositionableChannel.getPositionRaw(), oPositionableChannel.getPositionRaw());
        }

        // last resort by id
        return Long.compareUnsigned(a.getIdLong(), b.getIdLong());
    }
}
