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
package net.dv8tion.jda.api.events.guild.scheduledevent.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ScheduledEvent
import javax.annotation.Nonnull

/**
 * Indicates that the [status][ScheduledEvent.getStatus] of a [ScheduledEvent] has changed. The status
 * of a [ScheduledEvent] represents if it is currently active, canceled, or completed.
 *
 *
 * Can be used to detect when the [ScheduledEvent] status has changed.
 *
 *
 * Identifier: `status`
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [SCHEDULED_EVENTS][net.dv8tion.jda.api.requests.GatewayIntent.SCHEDULED_EVENTS] intent and [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * Discord does not specifically tell us about the updates, but merely tells us the
 * [ScheduledEvent] was updated and gives us the updated [ScheduledEvent] object.
 * In order to fire a specific event like this we need to have the old [ScheduledEvent] cached to compare against.
 */
class ScheduledEventUpdateStatusEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull scheduledEvent: ScheduledEvent,
    @Nonnull previous: ScheduledEvent.Status?
) : GenericScheduledEventUpdateEvent<ScheduledEvent.Status?>(
    api,
    responseNumber,
    scheduledEvent,
    previous,
    scheduledEvent.status,
    IDENTIFIER
) {
    @get:Nonnull
    val oldStatus: ScheduledEvent.Status?
        /**
         * The old [status][ScheduledEvent.getStatus].
         *
         * @return The old status
         */
        get() = oldValue

    @get:Nonnull
    val newStatus: ScheduledEvent.Status?
        /**
         * The new [status][ScheduledEvent.getStatus].
         *
         * @return The new status
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "status"
    }
}
