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
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.handle.SocketHandler
import net.dv8tion.jda.internal.utils.EntityString
import javax.annotation.Nonnull

/**
 * Top-level event type
 * <br></br>All events JDA fires are derived from this class.
 *
 *
 * Can be used to check if an Object is a JDA event in [EventListener][net.dv8tion.jda.api.hooks.EventListener] implementations to distinguish what event is being fired.
 * <br></br>Adapter implementation: [ListenerAdapter][net.dv8tion.jda.api.hooks.ListenerAdapter]
 */
abstract class Event @JvmOverloads constructor(
    @get:Nonnull
    @param:Nonnull override val jDA: JDA, override val responseNumber: Long = jDA.getResponseTotal()
) : GenericEvent {
    protected override val rawData: DataObject?
    /**
     * Creates a new Event from the given JDA instance
     *
     * @param jDA
     * Current JDA instance
     * @param responseNumber
     * The sequence number for this event
     *
     * @see .Event
     */
    /**
     * Creates a new Event from the given JDA instance
     * <br></br>Uses the current [net.dv8tion.jda.api.JDA.getResponseTotal] as sequence
     *
     * @param api
     * Current JDA instance
     */
    init {
        rawData = if (jDA is JDAImpl && (jDA as JDAImpl).isEventPassthrough) SocketHandler.CURRENT_EVENT.get() else null
    }

    override fun getRawData(): DataObject? {
        if (jDA is JDAImpl) {
            check((jDA as JDAImpl).isEventPassthrough) { "Event passthrough is not enabled, see JDABuilder#setEventPassthrough(boolean)" }
        }
        return rawData
    }

    override fun toString(): String {
        return if (this is UpdateEvent<*, *>) {
            val event = this as UpdateEvent<*, *>
            EntityString(this)
                .setType(event.getPropertyIdentifier())
                .addMetadata(null, event.getOldValue().toString() + " -> " + event.getNewValue())
                .toString()
        } else {
            EntityString(this).toString()
        }
    }
}
