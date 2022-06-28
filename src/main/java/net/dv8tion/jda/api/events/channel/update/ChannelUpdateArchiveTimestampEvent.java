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
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelField;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

//TODO-v5: Docs
public class ChannelUpdateArchiveTimestampEvent extends GenericChannelUpdateEvent<OffsetDateTime>
{
    public static final ChannelField FIELD = ChannelField.ARCHIVED_TIMESTAMP;

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
