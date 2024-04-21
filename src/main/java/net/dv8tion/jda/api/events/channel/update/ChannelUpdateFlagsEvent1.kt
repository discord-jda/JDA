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
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import java.util.*
import javax.annotation.Nonnull

/**
 * Indicates that the [flags][Channel.getFlags] of a [Channel] changed.
 *
 *
 * Can be used to retrieve the old flags and the new ones.
 *
 * @see ChannelField.FLAGS
 */
class ChannelUpdateFlagsEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull channel: Channel,
    @Nonnull oldValue: EnumSet<ChannelFlag?>?,
    @Nonnull newValue: EnumSet<ChannelFlag?>?
) : GenericChannelUpdateEvent<EnumSet<ChannelFlag?>?>(api, responseNumber, channel, FIELD, oldValue, newValue) {
    @Nonnull
    override fun getOldValue(): EnumSet<ChannelFlag?>? {
        return super.getOldValue()
    }

    @Nonnull
    override fun getNewValue(): EnumSet<ChannelFlag?>? {
        return super.getNewValue()
    }

    companion object {
        val FIELD = ChannelField.FLAGS
        val IDENTIFIER = FIELD.fieldName
    }
}
