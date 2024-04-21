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
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import javax.annotation.Nonnull

/**
 * Indicates that a gateway event relating to a [ScheduledEvent] has been fired.
 *
 *
 *  It should be noted that a [ScheduledEvent] is not an
 * actual gateway event found in the [net.dv8tion.jda.api.events] package, but are rather entities similar to
 * [User][net.dv8tion.jda.api.entities.User] or [TextChannel][net.dv8tion.jda.api.entities.channel.concrete.TextChannel] objects
 * representing a [scheduled event](https://support.discord.com/hc/en-us/articles/4409494125719-Scheduled-Events).
 *
 *
 * **Requirements**<br></br>
 *
 *
 * These events require the [SCHEDULED_EVENTS][net.dv8tion.jda.api.requests.GatewayIntent.SCHEDULED_EVENTS] intent and [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * This class may be used to check if a gateway event is related to a [ScheduledEvent]
 * as all gateway events in the [net.dv8tion.jda.api.events.guild.scheduledevent] package extend this class.
 */
abstract class GenericScheduledEventGatewayEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The [ScheduledEvent]
     *
     * @return The Scheduled Event
     */
    @get:Nonnull
    @param:Nonnull val scheduledEvent: ScheduledEvent
) : GenericGuildEvent(api, responseNumber, scheduledEvent.guild)
