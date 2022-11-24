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

package net.dv8tion.jda.api.events.channel.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelField;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * Indicates that a {@link Channel Channel's} archival timestamp was updated.
 *
 * <p>This timestamp will be updated when any of the following happens:
 * <ul>
 *     <li>The channel is archived</li>
 *     <li>The channel is unarchived</li>
 *     <li>The AUTO_ARCHIVE_DURATION is changed</li>
 * </ul>
 *
 * Limited to {@link ThreadChannel Thread Channels}.
 *
 * @see ThreadChannel#getTimeArchiveInfoLastModified()
 * @see ChannelField#ARCHIVED_TIMESTAMP
 */
public class ChannelUpdateArchiveTimestampEvent extends GenericChannelUpdateEvent<OffsetDateTime>
{
    public static final ChannelField FIELD = ChannelField.ARCHIVED_TIMESTAMP;
    public static final String IDENTIFIER = FIELD.getFieldName();

    private final long oldTimestamp;
    private final long newTimestamp;

    public ChannelUpdateArchiveTimestampEvent(@Nonnull JDA api, long responseNumber, Channel channel, long oldValue, long newValue)
    {
        //Explicitly providing null for new and old value here as we will override the methods providing them.
        //We are doing this so that we only construct the OffsetDateTime objects if they are specifically requested
        super(api, responseNumber, channel, FIELD, null, null);

        this.oldTimestamp = oldValue;
        this.newTimestamp = newValue;
    }

    @Override
    public OffsetDateTime getOldValue()
    {
        return Helpers.toOffset(oldTimestamp);
    }

    @Override
    public OffsetDateTime getNewValue()
    {
        return Helpers.toOffset(newTimestamp);
    }
}
