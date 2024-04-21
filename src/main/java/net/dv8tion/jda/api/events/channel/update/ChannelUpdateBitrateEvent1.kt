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
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import javax.annotation.Nonnull

/**
 * Indicates that a [Channel&#39;s][Channel] bitrate has been updated.
 *
 *
 * Limited to [Audio Channels][AudioChannel].
 *
 * @see AudioChannel.getBitrate
 * @see ChannelField.BITRATE
 */
class ChannelUpdateBitrateEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    channel: Channel,
    oldValue: Int?,
    newValue: Int?
) : GenericChannelUpdateEvent<Int?>(api, responseNumber, channel, FIELD, oldValue, newValue) {
    companion object {
        val FIELD = ChannelField.BITRATE
        val IDENTIFIER = FIELD.fieldName
    }
}
