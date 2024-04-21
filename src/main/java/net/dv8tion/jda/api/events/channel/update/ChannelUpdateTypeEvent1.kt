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
import net.dv8tion.jda.api.entities.channel.ChannelType
import javax.annotation.Nonnull

/**
 * Indicates that a [Channel&#39;s][Channel] [ChannelType] was updated.
 *
 *
 * Can be used to retrieve the old [ChannelType] and the new one.
 *
 *
 * This event will most likely be fired when a [Channel]:
 *
 *  * of type [ChannelType.TEXT] is converted to type [ChannelType.NEWS]
 *  * of type [ChannelType.NEWS] is converted to type [ChannelType.TEXT]
 *  * of type [ChannelType.FORUM] is converted to type [ChannelType.MEDIA]
 *
 *
 * @see Channel.getType
 * @see ChannelField.TYPE
 */
class ChannelUpdateTypeEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    channel: Channel,
    oldValue: ChannelType?,
    newValue: ChannelType?
) : GenericChannelUpdateEvent<ChannelType?>(api, responseNumber, channel, FIELD, oldValue, newValue) {
    companion object {
        val FIELD = ChannelField.TYPE
        val IDENTIFIER = FIELD.fieldName
    }
}
