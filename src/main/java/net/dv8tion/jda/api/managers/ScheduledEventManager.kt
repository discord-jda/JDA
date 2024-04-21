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
package net.dv8tion.jda.api.managers

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import java.time.temporal.TemporalAccessor
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * The Manager is providing functionality to update one or more fields of a [ScheduledEvent].
 * <br></br>The manager may also be used to start, cancel or end events.
 *
 *
 * **Example**
 * <pre>`manager.setLocation("at the beach")
 * .setStartTime(OffsetDateTime.now().plusHours(1))
 * .setEndTime(OffsetDateTime.now().plusHours(3))
 * .setName("Discussing Turtle Shells")
 * .queue();
`</pre> *
 *
 * @see ScheduledEvent.getManager
 */
interface ScheduledEventManager : Manager<ScheduledEventManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(ScheduledEventManager.DESCRIPTION | ScheduledEventManager.END_TIME);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.DESCRIPTION]
     *  * [.LOCATION]
     *  * [.START_TIME]
     *  * [.END_TIME]
     *  * [.IMAGE]
     *  * [.STATUS]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return ScheduledEventManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): ScheduledEventManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(ScheduledEventManager.DESCRIPTION, ScheduledEventManager.END_TIME);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.DESCRIPTION]
     *  * [.LOCATION]
     *  * [.START_TIME]
     *  * [.END_TIME]
     *  * [.IMAGE]
     *  * [.STATUS]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return ScheduledEventManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): ScheduledEventManager?

    @get:Nonnull
    val scheduledEvent: ScheduledEvent

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] this
         * [ScheduledEvent] is in.
         * <br></br>This is logically the same as calling `getScheduledEvent().getGuild()`
         *
         * @return The parent [Guild][net.dv8tion.jda.api.entities.Guild]
         */
        get() = scheduledEvent.guild

    /**
     * Sets the name of the selected [ScheduledEvent]
     *
     * @param  name
     * The new name for the selected [ScheduledEvent]
     *
     * @throws java.lang.IllegalArgumentException
     * If the new name is blank, empty, `null`, or longer than {@value ScheduledEvent#MAX_NAME_LENGTH}
     * characters
     *
     * @return ScheduledEventManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): ScheduledEventManager?

    /**
     * Sets the description of the selected [ScheduledEvent].
     * This field may include markdown.
     *
     * @param  description
     * The new description for the selected [ScheduledEvent],
     * or `null` to reset the description
     *
     * @throws java.lang.IllegalArgumentException
     * If the new description is longer than {@value ScheduledEvent#MAX_DESCRIPTION_LENGTH} characters
     *
     * @return ScheduledEventManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setDescription(description: String?): ScheduledEventManager?

    /**
     * Sets the cover image for the new [ScheduledEvent].
     *
     * @param  icon
     * The cover image for the new [ScheduledEvent],
     * or `null` for no cover image.
     *
     * @return ScheduledEventManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setImage(icon: Icon?): ScheduledEventManager?

    /**
     * Sets the location of the selected [ScheduledEvent] to take place in the specified [GuildChannel].
     *
     * This will change the event's type to [ScheduledEvent.Type.STAGE_INSTANCE] or [ScheduledEvent.Type.VOICE],
     * which are the only supported channel types for the location of scheduled events currently.
     *
     * @param  channel
     * The [GuildChannel] that the selected [ScheduledEvent] is set to take place in.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the provided [GuildChannel] is `null`
     *  * If the provided [GuildChannel] is not from the same guild
     *  * If the provided [GuildChannel] is not a [net.dv8tion.jda.api.entities.channel.concrete.StageChannel] or [net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.MANAGE_EVENTS][net.dv8tion.jda.api.Permission.MANAGE_EVENTS],
     * [Permission.MANAGE_CHANNEL][net.dv8tion.jda.api.Permission.MANAGE_CHANNEL],
     * [Permission.VOICE_MUTE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MUTE_OTHERS],
     * or [Permission.VOICE_MOVE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MOVE_OTHERS], in the provided
     * channel.
     *
     * @return ScheduledEventManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setLocation(@Nonnull channel: GuildChannel?): ScheduledEventManager?

    /**
     * Sets the location of the selected [ScheduledEvent] to take place externally,
     * or not in a specific [GuildChannel]. <u>Please note that an event is required to have an end time set if
     * the location is external.</u>
     *
     * This will change the event's type to [ScheduledEvent.Type.EXTERNAL]
     *
     * @param  location
     * The location that the selected [ScheduledEvent] is set to take place at.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided location is blank, empty, `null`, or longer than
     * {@value ScheduledEvent#MAX_LOCATION_LENGTH}
     * @throws java.lang.IllegalStateException
     * If the selected [ScheduledEvent] does not have an end time
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.MANAGE_EVENTS][net.dv8tion.jda.api.Permission.MANAGE_EVENTS]
     *
     * @return ScheduledEventManager for chaining convenience
     *
     * @see .setEndTime
     * @see .setLocation
     */
    @Nonnull
    @CheckReturnValue
    fun setLocation(@Nonnull location: String?): ScheduledEventManager?

    /**
     * Sets the time that the selected [ScheduledEvent] should start at.
     * Events of [Type.EXTERNAL][ScheduledEvent.Type.EXTERNAL] will automatically
     * start at this time. Events of [Type.STAGE_INSTANCE][ScheduledEvent.Type.STAGE_INSTANCE]
     * and [Type.VOICE][ScheduledEvent.Type.VOICE] need to be manually started.
     * If the [ScheduledEvent] has not begun after its scheduled start time, it will be automatically cancelled after a few hours.
     *
     * @param  startTime
     * The time that the selected [ScheduledEvent] is set to start at.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the provided start time is `null`
     *  * If the provided start time is before the end time
     *  * If the provided start time is before the current time
     *
     * @return ScheduledEventManager for chaining convenience
     *
     * @see .setEndTime
     */
    @Nonnull
    @CheckReturnValue
    fun setStartTime(@Nonnull startTime: TemporalAccessor?): ScheduledEventManager?

    /**
     * Sets the time that the selected [ScheduledEvent] should end at.
     * Events of [Type.EXTERNAL][ScheduledEvent.Type.EXTERNAL] will automatically
     * end at this time, and events of [Type.STAGE_INSTANCE][ScheduledEvent.Type.STAGE_INSTANCE]
     * and [Type.VOICE][ScheduledEvent.Type.VOICE] will end a few minutes after the last
     * person has left the channel.
     *
     * @param  endTime
     * The time that the selected [ScheduledEvent] is set to end at.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the provided end time is before the start time
     *  * If the provided end time is `null`
     *
     *
     * @return ScheduledEventManager for chaining convenience
     *
     * @see .setStartTime
     */
    @Nonnull
    @CheckReturnValue
    fun setEndTime(@Nonnull endTime: TemporalAccessor?): ScheduledEventManager?

    /**
     * Sets the status of the event. This method may be used to start, end or cancel an event but can only be used to
     * complete one of the following transitions:
     *
     *  1. [Status.SCHEDULED][ScheduledEvent.Status.SCHEDULED] to [Status.ACTIVE][ScheduledEvent.Status.ACTIVE]
     *  1. [Status.SCHEDULED][ScheduledEvent.Status.SCHEDULED] to [Status.CANCELED][ScheduledEvent.Status.CANCELED]
     *  1. [Status.ACTIVE][ScheduledEvent.Status.ACTIVE] to [Status.COMPLETED][ScheduledEvent.Status.COMPLETED]
     *
     *
     * @param  status
     * The new status
     *
     * @throws java.lang.IllegalStateException
     * If the transition between statuses does not follow one of the three documented above.
     * @throws IllegalArgumentException
     * If the provided status is `null`
     *
     * @return ScheduledEventManager for chaining convenience
     *
     * @see .getScheduledEvent
     * @see ScheduledEvent.getStatus
     */
    @Nonnull
    @CheckReturnValue
    fun setStatus(@Nonnull status: ScheduledEvent.Status?): ScheduledEventManager?

    companion object {
        /** Used to reset the name field  */
        const val NAME: Long = 1

        /** Used to reset the description field  */
        const val DESCRIPTION = (1 shl 1).toLong()

        /** Used to reset the location field  */
        const val LOCATION = (1 shl 2).toLong()

        /** Used to reset the start time field  */
        const val START_TIME = (1 shl 3).toLong()

        /** Used to reset the end time field  */
        const val END_TIME = (1 shl 4).toLong()

        /** Used to reset the image field  */
        const val IMAGE = (1 shl 5).toLong()

        /** Used to reset the status field  */
        const val STATUS = (1 shl 6).toLong()
    }
}
