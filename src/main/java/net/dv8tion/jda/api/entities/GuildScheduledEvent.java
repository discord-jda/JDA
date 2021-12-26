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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.GuildScheduledEventManager;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * A class representing a guild scheduled event (the ones that show up under the events tab in the Official Discord Client).
 * These events should not be confused with {@link net.dv8tion.jda.api.events Gateway Events},
 * which are fired by Discord whenever something interesting happens
 * (ie., a {@link net.dv8tion.jda.api.events.message.MessageDeleteEvent MessageDeleteEvent} gets fired whenever a message gets deleted).
 */
public interface GuildScheduledEvent extends ISnowflake
{
    /**
     * The maximum allowed length for an event's name.
     */
    int MAX_NAME_LENGTH = 100;

    /**
     * The maximum allowed length for an event's description.
     */
    int MAX_DESCRIPTION_LENGTH = 1000;

    /**
     * The name of the event.
     *
     * @return The name
     */
    @Nonnull
    String getName();

    /**
     * The description of the event.
     *
     * @return The description, or {@code null} if none is specified
     */
    @Nullable
    String getDescription();

    /**
     * The user who originally created the event.
     *
     * @return The user, or {@code null} if the event was created before Discord started keeping track of event
     *         creators on October 21st, 2021.
     */
    @Nullable
    User getCreator();

    /**
     * The status of the event (ie., if the event has ended or has not yet started).
     *
     * @return The status
     */
    @Nonnull
    Status getStatus();

    /**
     * Gets what type of event an event is, or where the event will be taking place at. Possible types include
     * {@link Type#STAGE_INSTANCE Type.STAGE_INSTANCE}, {@link Type#VOICE Type.VOICE} and {@link Type#EXTERNAL Type.EXTERNAL}
     * (which indicates that the events location is manually set to a custom location).
     *
     * @return The type, or {@link Type#UNKNOWN Type.UNKOWN} if the event type is unknown to JDA.
     */
    @Nonnull
    Type getType();

    /**
     * The time that the event is set to start at.
     *
     * @return The time the event is set to start at
     *
     * @see #getEndTime()
     */
    @Nonnull
    OffsetDateTime getStartTime();

    /**
     * The time that the event is set to end at.
     * <br>The end time is only required for external events,
     * which are events that are not associated with a stage or voice channel.
     *
     * @return The time that the event is set to end at. This will never be {@code null} for events of
     *         {@link Type#EXTERNAL Type.EXTERNAL}, but can be null for other types.
     *
     * @see #getType()
     * @see #getStartTime()
     */
    @Nullable
    OffsetDateTime getEndTime();

    /**
     * The stage channel that the event is set to take place at.
     * <br>Note that this method is only applicable to events which are of {@link Type#STAGE_INSTANCE Type.STAGE_INSTANCE}.
     *
     * @return The stage channel, or {@code null} if the stage channel was deleted
     *         or if the event is not of {@link Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
     *
     * @see #getType()
     * @see #getVoiceChannel()
     * @see #getExternalLocation()
     */
    @Nullable
    StageChannel getStageChannel();

    /**
     * The voice channel that the event is set to take place at.
     * <br>Note that this method is only applicable to events which are of {@link Type#VOICE Type.VOICE}.
     *
     * @return The voice channel, or {@code null} if the voice channel was deleted
     *         or if the event is not of {@link Type#STAGE_INSTANCE Type.VOICE}
     *
     * @see #getType()
     * @see #getStageChannel()
     * @see #getExternalLocation()
     */
    @Nullable
    VoiceChannel getVoiceChannel();

    /**
     * The external location that the event is set to take place at.
     * <br>Note that this method is only applicable to events which are of {@link Type#EXTERNAL Type.EXTERNAL}.
     *
     * @return The location, or {@code null} if the event is not of {@link Type#EXTERNAL Type.EXTERNAL}
     *
     * @see #getType()
     * @see #getStageChannel()
     * @see #getVoiceChannel()
     */
    @Nullable
    String getExternalLocation();

    /**
     * The amount of users who are interested in attending the event.
     * <br>This method only returns the cached count, and may not be consistent with the live count.
     *
     * @return  The amount of users who are interested in attending the event
     */
    int getInterestedUserCount();

    /**
     * Retrieves a list of users who are interested in attending the event.
     *
     * @param  max
     *         The maximum amount of users to retrieve
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided maximum amount of users to retrieve is not positive
     *
     * @return {@link RestAction} - Type: {@link List} or {@link User}
     */
    RestAction<List<User>> retrieveInterestedUsers(int max);

    /**
     * The {@link GuildScheduledEventManager} for this event.
     * <br>In the GuildScheduledEventManager, you can modify all its values, and can also start, end, or cancel events.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * <p>This is a lazy idempotent getter. The manager is retained after the first call.
     * This getter is not thread-safe and would require guards by the user.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}
     *
     * @return The GuildScheduledEventManager of this event
     */
    @Nonnull
    GuildScheduledEventManager getManager();

    /**
     * The guild that this event was created in
     *
     * @return The guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * The JDA instance associated with this event object
     *
     * @return The JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Represents the status of a scheduled guild event.
     * @see GuildScheduledEvent#getStatus
     */
    enum Status
    {
        UNKNOWN(-1),
        SCHEDULED(1),
        ACTIVE(2),
        COMPLETED(3),
        CANCELED(4);

        private final int key;

        Status(int key)
        {
            this.key = key;
        }

        /**
         * The Discord id key for this Status.
         *
         * @return The id key for this Status
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Used to retrieve a Status based on a Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested Status.
         *
         * @return The Status related to the provided key, or {@link #UNKNOWN Status.UNKNOWN} if the key is not recognized.
         */
        @Nonnull
        public static Status fromKey(int key)
        {
            for (Status status : Status.values())
            {
                if (status.getKey() == key)
                    return status;
            }

            return UNKNOWN;
        }
    }

    /**
     * Represents what type of event an event is, or where the event will be taking place at.
     */
    enum Type
    {
        UNKNOWN(-1),
        STAGE_INSTANCE(1),
        VOICE(2),
        EXTERNAL(3);

        private final int key;

        Type(int key)
        {
            this.key = key;
        }

        /**
         * The Discord id key for this Type.
         *
         * @return The id key for this Type
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Used to retrieve a Type based on a Discord id key.
         *
         * @param  key
         *         The Discord id key representing the requested Type.
         *
         * @return The Type related to the provided key, or {@link #UNKNOWN Type.UNKNOWN} if the key is not recognized.
         */
        @Nonnull
        public static Type fromKey(int key)
        {
            for (Type type : Type.values())
            {
                if (type.getKey() == key)
                    return type;
            }

            return UNKNOWN;
        }
    }
}
