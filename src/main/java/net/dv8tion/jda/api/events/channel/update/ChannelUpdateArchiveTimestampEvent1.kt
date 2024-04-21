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
package net.dv8tion.jda.api.events.channel.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelField
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.internal.utils.Helpers
import java.time.OffsetDateTime
import javax.annotation.Nonnull

/**
 * Indicates that a [Channel&#39;s][Channel] archival timestamp was updated.
 *
 *
 * This timestamp will be updated when any of the following happens:
 *
 *  * The channel is archived
 *  * The channel is unarchived
 *  * The AUTO_ARCHIVE_DURATION is changed
 *
 *
 * Limited to [Thread Channels][ThreadChannel].
 *
 * @see ThreadChannel.getTimeArchiveInfoLastModified
 * @see ChannelField.ARCHIVED_TIMESTAMP
 */
class ChannelUpdateArchiveTimestampEvent //Explicitly providing null for new and old value here as we will override the methods providing them.
//We are doing this so that we only construct the OffsetDateTime objects if they are specifically requested
    (
    @Nonnull api: JDA,
    responseNumber: Long,
    channel: Channel,
    private val oldTimestamp: Long,
    private val newTimestamp: Long
) : GenericChannelUpdateEvent<OffsetDateTime?>(api, responseNumber, channel, FIELD, null, null) {
    override fun getOldValue(): OffsetDateTime? {
        return Helpers.toOffset(oldTimestamp)
    }

    override fun getNewValue(): OffsetDateTime? {
        return Helpers.toOffset(newTimestamp)
    }

    companion object {
        val FIELD = ChannelField.ARCHIVED_TIMESTAMP
        val IDENTIFIER = FIELD.fieldName
    }
}
