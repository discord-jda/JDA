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
import net.dv8tion.jda.api.events.UpdateEvent
import net.dv8tion.jda.api.events.channel.GenericChannelEvent
import javax.annotation.Nonnull

/**
 * Top-level channel update event type indicating that a field of a [Channel] was updated.
 * <br></br>All channel update events JDA fires are derived from this class.
 *
 * @param <T>
 * The value type of the field that has been updated.
 * @see ChannelField
</T> */
open class GenericChannelUpdateEvent<T>(
    @Nonnull api: JDA,
    responseNumber: Long,
    channel: Channel,
    protected val channelField: ChannelField,
    protected override val oldValue: T,
    protected override val newValue: T
) : GenericChannelEvent(api, responseNumber, channel), UpdateEvent<Channel?, T?> {
    @get:Nonnull
    override val propertyIdentifier: String
        /**
         * The identifier of the [Channel&#39;s][Channel] field that has just been updated.
         *
         * @return The identifier of the [Channel&#39;s][Channel] field that has just been updated.
         */
        get() = channelField.fieldName

    @get:Nonnull
    override val entity: E
        /**
         * The [Channel] entity affected by this update event.
         * <br></br>Equivalent with `getChannel()`.
         *
         * @return The [Channel] entity affected by this update event.
         */
        get() = getChannel()

    /**
     * The [Channel&#39;s][Channel] old value of the just updated field.
     *
     * @return The old value of the just updated field.
     */
    override fun getOldValue(): T? {
        return oldValue
    }

    /**
     * The [Channel&#39;s][Channel] new value of the just updated field.
     *
     * @return The new value of the just updated field.
     */
    override fun getNewValue(): T? {
        return newValue
    }
}
