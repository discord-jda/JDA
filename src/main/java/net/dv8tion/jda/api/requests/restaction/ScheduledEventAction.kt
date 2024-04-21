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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.entities.*
import java.time.temporal.TemporalAccessor
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Extension of [RestAction][net.dv8tion.jda.api.requests.RestAction] specifically
 * designed to create a [ScheduledEvent].
 * This extension allows setting properties such as the name or description of an event before it is
 * created.
 *
 *
 * **Requirements**<br></br>
 * Events that are created are required to have a name, a location, and a start time. Depending on the
 * type of location provided, an event will be of one of three different [Types][ScheduledEvent.Type]:
 *
 *  1.
 * [Type.STAGE_INSTANCE][ScheduledEvent.Type.STAGE_INSTANCE]
 * <br></br>These events are set to take place inside of a [StageChannel][net.dv8tion.jda.api.entities.channel.concrete.StageChannel]. The
 * following permissions are required in the specified stage channel in order to create an event there:
 *
 *  * [Permission.MANAGE_EVENTS][net.dv8tion.jda.api.Permission.MANAGE_EVENTS]
 *  * [Permission.MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL]
 *  * [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS]
 *  * [Permission.VOICE_MOVE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MOVE_OTHERS]
 *
 *
 *  1.
 * [Type.VOICE][ScheduledEvent.Type.VOICE]
 * <br></br>These events are set to take place inside of a [net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel]. The
 * following permissions are required in the specified voice channel in order to create an event there:
 *
 *  * [Permission.MANAGE_EVENTS][net.dv8tion.jda.api.Permission.MANAGE_EVENTS]
 *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
 *  * [Permission.VOICE_CONNECT][net.dv8tion.jda.api.Permission.VOICE_CONNECT]
 *
 *
 *  1.
 * [Type.EXTERNAL][ScheduledEvent.Type.EXTERNAL]
 * <br></br>These events are set to take place at an external location. [Permission.MANAGE_EVENTS][net.dv8tion.jda.api.Permission.MANAGE_EVENTS]
 * is required on the guild level in order to create this type of event. Additionally, an end time *must*
 * also be specified.
 *
 *
 *
 * @see net.dv8tion.jda.api.entities.Guild
 *
 * @see Guild.createScheduledEvent
 * @see Guild.createScheduledEvent
 */
interface ScheduledEventAction : FluentAuditableRestAction<ScheduledEvent?, ScheduledEventAction?> {
    @get:Nonnull
    val guild: Guild?

    /**
     * Sets the name for the new [ScheduledEvent].
     *
     * @param  name
     * The name for the new [ScheduledEvent]
     *
     * @throws java.lang.IllegalArgumentException
     * If the new name is blank, empty, `null`, or contains more than {@value ScheduledEvent#MAX_NAME_LENGTH}
     * characters
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    fun setName(@Nonnull name: String?): ScheduledEventAction?

    /**
     * Sets the description for the new [ScheduledEvent].
     * This field may include markdown.
     *
     * @param  description
     * The description for the new [ScheduledEvent],
     * or `null` for no description
     *
     * @throws java.lang.IllegalArgumentException
     * If the new description is longer than {@value ScheduledEvent#MAX_DESCRIPTION_LENGTH} characters
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDescription(description: String?): ScheduledEventAction?

    /**
     *
     * Sets the time that the new [ScheduledEvent] will start at.
     * Events of [Type.EXTERNAL][ScheduledEvent.Type.EXTERNAL] will automatically
     * start at this time, but events of [Type.STAGE_INSTANCE][ScheduledEvent.Type.STAGE_INSTANCE]
     * and [Type.VOICE][ScheduledEvent.Type.VOICE] will need to be manually started,
     * and will automatically be cancelled a few hours after the start time if not.
     *
     * @param  startTime
     * The time that the new [ScheduledEvent] should start at
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided start time is `null`, or takes place after the end time
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    fun setStartTime(@Nonnull startTime: TemporalAccessor?): ScheduledEventAction?

    /**
     * Sets the time that the new [ScheduledEvent] will end at.
     * Events of [Type.EXTERNAL][ScheduledEvent.Type.EXTERNAL] will automatically
     * end at this time, and events of [Type.STAGE_INSTANCE][ScheduledEvent.Type.STAGE_INSTANCE]
     * and [Type.VOICE][ScheduledEvent.Type.VOICE] will end a few minutes after the last
     * user has left the channel.
     *
     * **Note:** Setting an end time is only possible for events of [Type.EXTERNAL][ScheduledEvent.Type.EXTERNAL].
     *
     * @param  endTime
     * The time that the new [ScheduledEvent] is set to end at
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided end time is chronologically set before the start time
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    fun setEndTime(endTime: TemporalAccessor?): ScheduledEventAction?

    /**
     * Sets the cover image for the new [ScheduledEvent].
     *
     * @param  icon
     * The cover image for the new [ScheduledEvent],
     * or `null` for no cover image
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setImage(icon: Icon?): ScheduledEventAction?
}
