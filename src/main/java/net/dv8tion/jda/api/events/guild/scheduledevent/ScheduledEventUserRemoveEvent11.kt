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
package net.dv8tion.jda.api.events.guild.scheduledevent

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ScheduledEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [User][net.dv8tion.jda.api.entities.User] has unsubscribed from a [ScheduledEvent].
 *
 *
 * Can be used to detect when someone unsubscribed to an event and also retrieve their
 * [User][net.dv8tion.jda.api.entities.User] object as well as the [ScheduledEvent].
 *
 *
 * **Requirements**<br></br>
 *
 *
 * This event requires the [SCHEDULED_EVENTS][net.dv8tion.jda.api.requests.GatewayIntent.SCHEDULED_EVENTS] intent and [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 */
class ScheduledEventUserRemoveEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull scheduledEvent: ScheduledEvent,
    userId: Long
) : GenericScheduledEventUserEvent(api, responseNumber, scheduledEvent, userId)
