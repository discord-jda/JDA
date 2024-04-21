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
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import javax.annotation.Nonnull

/**
 * Indicates that the [default layout][ForumChannel.getDefaultLayout] of a [ForumChannel] changed.
 *
 *
 * Can be used to retrieve the old default layout and the new one.
 *
 * @see ChannelField.DEFAULT_FORUM_LAYOUT
 */
class ChannelUpdateDefaultLayoutEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull channel: Channel,
    @Nonnull oldValue: ForumChannel.Layout?,
    @Nonnull newValue: ForumChannel.Layout?
) : GenericChannelUpdateEvent<ForumChannel.Layout?>(
    api,
    responseNumber,
    channel,
    ChannelField.DEFAULT_FORUM_LAYOUT,
    oldValue,
    newValue
) {
    @Nonnull
    override fun getOldValue(): ForumChannel.Layout? {
        return super.getOldValue()
    }

    @Nonnull
    override fun getNewValue(): ForumChannel.Layout? {
        return super.getNewValue()
    }

    companion object {
        val FIELD = ChannelField.DEFAULT_FORUM_LAYOUT
        val IDENTIFIER = FIELD.fieldName
    }
}
