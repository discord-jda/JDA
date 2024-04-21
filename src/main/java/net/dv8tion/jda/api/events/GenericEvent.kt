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
package net.dv8tion.jda.api.events

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.data.DataObject
import javax.annotation.Nonnull

/**
 * Interface for events supported by [EventManagers][net.dv8tion.jda.api.hooks.IEventManager].
 *
 * @see net.dv8tion.jda.api.hooks.EventListener.onEvent
 */
interface GenericEvent {
    val jDA: JDA
        /**
         * The current JDA instance corresponding to this Event
         *
         * @return The corresponding JDA instance
         */
        @Nonnull get

    /**
     * The current sequence for this event.
     * <br></br>This can be used to keep events in order when making sequencing system.
     *
     * @return The current sequence number for this event
     */
    val responseNumber: Long

    /**
     * The passthrough data that this event was serialized from. This data might be null in rare situations, for example, if the event came from a rest action.
     * <br></br>This provides the full gateway message payload, including sequence, event name and dispatch type.
     * For details, read the official [Discord Documentation](https://discord.dev/topics/gateway).
     *
     * @throws IllegalStateException
     * If event passthrough was not enabled, see [JDABuilder#setEventPassthrough(boolean)][net.dv8tion.jda.api.JDABuilder.setEventPassthrough]
     *
     * @return The corresponding [DataObject]
     */
    @JvmField
    val rawData: DataObject?
}
