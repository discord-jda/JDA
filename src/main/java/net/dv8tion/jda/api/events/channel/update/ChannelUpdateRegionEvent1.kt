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
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelField
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import javax.annotation.Nonnull

/**
 * Indicates that a [Channel&#39;s][Channel] region has been updated.
 *
 *
 * Can be used to retrieve the old region and the new one.
 *
 *
 * Limited to [Audio Channels][AudioChannel].
 *
 * @see AudioChannel.getRegion
 * @see net.dv8tion.jda.api.Region
 *
 * @see ChannelField.REGION
 */
class ChannelUpdateRegionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    channel: Channel,
    oldValue: Region?,
    newValue: Region?
) : GenericChannelUpdateEvent<Region?>(api, responseNumber, channel, FIELD, oldValue, newValue) {
    companion object {
        val FIELD = ChannelField.REGION
        val IDENTIFIER = FIELD.fieldName
    }
}
