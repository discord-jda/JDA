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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.managers.ScheduledEventManager
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.pagination.ScheduledEventMembersPaginationAction
import net.dv8tion.jda.api.utils.ImageProxy
import java.time.OffsetDateTime
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * A class representing a [ScheduledEvent] (The events that show up under the events tab in the Official Discord Client).
 * These events should not be confused with [Gateway Events][net.dv8tion.jda.api.events],
 * which are fired by Discord whenever something interesting happens
 * (ie., a [MessageDeleteEvent][net.dv8tion.jda.api.events.message.MessageDeleteEvent] gets fired whenever a message gets deleted).
 */
interface ScheduledEvent : ISnowflake, Comparable<ScheduledEvent?> {
    @JvmField
    @get:Nonnull
    val name: String?

    /**
     * The description of the event.
     *
     * @return The description, or `null` if none is specified
     */
    @JvmField
    val description: String?

    /**
     * The cover image url of the event.
     *
     * Links to a potentially heavily compressed image. You can append a size parameter to the URL if needed. Example: `?size=4096`
     *
     * @return The image url, or `null` if none is specified
     */
    @JvmField
    val imageUrl: String?
    val image: ImageProxy?
        /**
         * Returns an [ImageProxy] for this events cover image.
         *
         * @return The [ImageProxy] for this events cover image or null if no image is defined
         *
         * @see .getImageUrl
         */
        get() {
            val imageUrl = imageUrl
            return imageUrl?.let { ImageProxy(it) }
        }

    /**
     * The user who originally created the event.
     *
     *  May return `null` if user has deleted their account, the [User] object is not cached
     * or the event was created before Discord started keeping track of event creators on October 21st, 2021.
     *
     * @return [User] object representing the event's creator or `null`.
     *
     * @see .getCreatorId
     * @see .getCreatorIdLong
     */
    val creator: User?

    /**
     * The ID of the user who originally created this event.
     *
     *  This method may return 0 if the event was created before Discord started keeping track of event creators on October 21st, 2021.
     *
     * @return The ID of the user who created this event, or 0 if no user is associated with creating this event.
     *
     * @see .getCreatorId
     * @see .getCreator
     */
    val creatorIdLong: Long
    val creatorId: String?
        /**
         * The ID of the user who originally created this event.
         * <br></br>This method may return `null` if the event was created before Discord started keeping track of event creators on October 21st, 2021.
         *
         * @return The Id of the user who created this event, or `null` if no user is associated with creating this event.
         *
         * @see .getCreatorIdLong
         * @see .getCreator
         */
        get() = if (creatorIdLong == 0L) null else java.lang.Long.toUnsignedString(creatorIdLong)

    @JvmField
    @get:Nonnull
    val status: Status?

    @JvmField
    @get:Nonnull
    val type: Type?

    @JvmField
    @get:Nonnull
    val startTime: OffsetDateTime?

    /**
     * The time the event is set to end at.
     * <br></br>The end time is only required for external events,
     * which are events that are not associated with a stage or voice channel.
     *
     * @return The time the event is set to end at. This can't be `null` for events of
     * [Type.EXTERNAL], but can be null for other types.
     *
     * @see .getType
     * @see .getStartTime
     */
    @JvmField
    val endTime: OffsetDateTime?

    /**
     * The guild channel the event is set to take place in.
     * <br></br>Note that this method is only applicable to events which are not of [Type.STAGE_INSTANCE] or [Type.VOICE].
     *
     * @return The guild channel, or `null` if the guild channel was deleted
     * or if the event is of [Type.EXTERNAL]
     *
     * @see .getType
     * @see .getLocation
     */
    val channel: GuildChannelUnion?

    @JvmField
    @get:Nonnull
    val location: String?

    /**
     * Deletes this Scheduled Event.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_SCHEDULED_EVENT][net.dv8tion.jda.api.requests.ErrorResponse.SCHEDULED_EVENT]
     * <br></br>If the the event was already deleted.
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The send request was attempted after the account lost
     * [Permission.MANAGE_EVENTS][net.dv8tion.jda.api.Permission.MANAGE_EVENTS] in the guild.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the Guild
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If we don't have the permission to [MANAGE_EVENTS][net.dv8tion.jda.api.Permission.MANAGE_EVENTS]
     *
     * @return [AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): AuditableRestAction<Void?>?

    /**
     * A [PaginationAction] implementation
     * that allows to [iterate][Iterable] over all [Members][net.dv8tion.jda.api.entities.Member] interested in this Event.
     *
     * <br></br>This iterates in ascending order by member id.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.SCHEDULED_EVENT]
     * <br></br>If the the event was already deleted.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>If we were removed from the Guild or can't view the events channel (Location)
     *
     *
     * @return [ScheduledEventMembersPaginationAction]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveInterestedMembers(): ScheduledEventMembersPaginationAction?

    /**
     * The amount of users who are interested in attending the event.
     *
     * This method only returns the cached count, and may not be consistent with the live count. Discord may additionally not
     * provide an interested user count for some [ScheduledEvent] objects returned from the Guild's or JDA's
     * cache, and this method may return -1 as a result. However, event's retrieved using [Guild.retrieveScheduledEventById]
     * will always contain an interested user count.
     *
     * @return The amount of users who are interested in attending the event
     *
     * @see Guild.retrieveScheduledEventById
     * @see Guild.retrieveScheduledEventById
     */
    val interestedUserCount: Int

    @JvmField
    @get:Nonnull
    val guild: Guild

    @get:Nonnull
    val jDA: JDA?
        /**
         * The JDA instance associated with this event object
         *
         * @return The JDA instance
         */
        get() = guild.getJDA()

    @JvmField
    @get:Nonnull
    val manager: ScheduledEventManager?

    /**
     * Compares two [ScheduledEvent] objects based on their scheduled start times.
     * <br></br>If two events are set to start at the same time, the comparison will be made based on their snowflake ID.
     *
     * @param  scheduledEvent
     * The provided scheduled event
     *
     * @throws IllegalArgumentException
     * If the provided scheduled event is `null`, from a different [Guild], or is not a valid
     * scheduled event provided by JDA.
     *
     * @return A negative number if the original event (which is the event that the [compareTo][.compareTo]
     * method is called upon) starts sooner than the provided event, or positive if it will start later than
     * the provided event. If both events are set to start at the same time, then the result will be negative if the original
     * event's snowflake ID is less than the provided event's ID, positive if it is greater than, or 0 if they
     * are the same.
     *
     * @see Comparable.compareTo
     * @see .getStartTime
     * @see .getIdLong
     */
    override fun compareTo(@Nonnull scheduledEvent: ScheduledEvent?): Int

    /**
     * Represents the status of a scheduled event.
     *
     * @see ScheduledEvent.getStatus
     */
    enum class Status(
        /**
         * The Discord id key for this Status.
         *
         * @return The id key for this Status
         */
        @JvmField val key: Int
    ) {
        UNKNOWN(-1),
        SCHEDULED(1),
        ACTIVE(2),
        COMPLETED(3),
        CANCELED(4);

        companion object {
            /**
             * Used to retrieve a Status based on a Discord id key.
             *
             * @param  key
             * The Discord id key representing the requested Status.
             *
             * @return The Status related to the provided key, or [Status.UNKNOWN][.UNKNOWN] if the key is not recognized.
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): Status {
                for (status in entries) {
                    if (status.key == key) return status
                }
                return UNKNOWN
            }
        }
    }

    /**
     * Represents what type of event an event is, or where the event will be taking place at.
     */
    enum class Type(
        /**
         * The Discord id key used to represent the scheduled event type.
         *
         * @return The id key used by discord for this scheduled event type.
         */
        @JvmField val key: Int
    ) {
        /**
         * Unknown future types that may be added by Discord which aren't represented in JDA yet.
         */
        UNKNOWN(-1),

        /**
         * An event with it's own [StageInstance][net.dv8tion.jda.api.entities.StageInstance]
         */
        STAGE_INSTANCE(1),

        /**
         * An event inside a [VoiceChannel][net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel]
         */
        VOICE(2),

        /**
         * An event held externally.
         */
        EXTERNAL(3);

        val isChannel: Boolean
            /**
             * Whether the event is scheduled to be held in a [GuildChannel].
             *
             * @return True, if the event is scheduled to be held in a [GuildChannel]
             */
            get() = this == STAGE_INSTANCE || this == VOICE

        companion object {
            /**
             * Used to retrieve a Type based on a Discord id key.
             *
             * @param  key
             * The Discord id key representing the requested Type.
             *
             * @return The Type related to the provided key, or [Type.UNKNOWN][.UNKNOWN] if the key is not recognized.
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): Type {
                for (type in entries) {
                    if (type.key == key) return type
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /**
         * The maximum allowed length for an event's name.
         */
        const val MAX_NAME_LENGTH = 100

        /**
         * The maximum allowed length for an event's description.
         */
        const val MAX_DESCRIPTION_LENGTH = 1000

        /**
         * The maximum allowed length for an event's location.
         */
        const val MAX_LOCATION_LENGTH = 100

        /**
         * Template for [.getImageUrl]
         */
        const val IMAGE_URL = "https://cdn.discordapp.com/guild-events/%s/%s.%s"
    }
}
