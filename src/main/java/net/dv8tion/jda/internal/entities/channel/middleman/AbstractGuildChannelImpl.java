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

package net.dv8tion.jda.internal.entities.channel.middleman;

import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public abstract class AbstractGuildChannelImpl<T extends AbstractGuildChannelImpl<T>> extends AbstractChannelImpl<T> implements GuildChannelMixin<T>
{
    protected GuildImpl guild;

    public AbstractGuildChannelImpl(long id, GuildImpl guild)
    {
        super(id, guild.getJDA());
        this.guild = guild;
    }

    @Nonnull
    @Override
    public GuildImpl getGuild()
    {
        return guild;
    }

    @Override
    public int compareTo(@Nonnull GuildChannel o)
    {
        Checks.notNull(o, "Channel");

        // Check thread positions
        ThreadChannel thisThread = this instanceof ThreadChannel ? (ThreadChannel) this : null;
        ThreadChannel otherThread = o instanceof ThreadChannel ? (ThreadChannel) o : null;

        if (thisThread != null && otherThread == null)
            return thisThread.getParentChannel().compareTo(o);
        if (thisThread == null && otherThread != null)
            return this.compareTo(otherThread.getParentChannel());
        if (thisThread != null)
        {
            // If they are threads on the same channel
            if (thisThread.getParentChannel().equals(otherThread.getParentChannel()))
                return Long.compare(o.getIdLong(), id); // threads are ordered ascending by age
            // If they are threads on different channels
            return thisThread.getParentChannel().compareTo(otherThread.getParentChannel());
        }

        // Check category positions
        Category thisParent = this instanceof ICategorizableChannel ? ((ICategorizableChannel) this).getParentCategory() : null;
        Category otherParent = o instanceof ICategorizableChannel ? ((ICategorizableChannel) o).getParentCategory() : null;

        if (thisParent != null && otherParent == null)
        {
            if (o instanceof Category)
            {
                // The other channel is the parent category of this channel
                if (o.equals(thisParent))
                    return 1;
                // The other channel is another category
                return thisParent.compareTo(o);
            }
            return 1;
        }
        if (thisParent == null && otherParent != null)
        {
            if (this instanceof Category)
            {
                // This channel is parent of other channel
                if (this.equals(otherParent))
                    return -1;
                // This channel is a category higher than the other channel's parent category
                return this.compareTo(otherParent); //safe use of recursion since no circular parents exist
            }
            return -1;
        }
        // Both channels are in different categories, compare the categories instead
        if (thisParent != null && !thisParent.equals(otherParent))
            return thisParent.compareTo(otherParent);

        // Check sort bucket (text/message is above audio)
        if (getType().getSortBucket() != o.getType().getSortBucket())
            return Integer.compare(getType().getSortBucket(), o.getType().getSortBucket());

        // Check actual position
        if (o instanceof IPositionableChannel && this instanceof IPositionableChannel)
        {
            IPositionableChannel oPositionableChannel = (IPositionableChannel) o;
            IPositionableChannel thisPositionableChannel = (IPositionableChannel) this;

            if (thisPositionableChannel.getPositionRaw() != oPositionableChannel.getPositionRaw())
                return Integer.compare(thisPositionableChannel.getPositionRaw(), oPositionableChannel.getPositionRaw());
        }

        // last resort by id
        return Long.compareUnsigned(id, o.getIdLong());
    }
}
