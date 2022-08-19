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
package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.temporal.TemporalAccessor;


/**
 * The Manager is providing functionality to update one or more fields of a {@link GuildScheduledEvent}.
 * <br>The manager may also be used to start, cancel or end events.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setLocation("at the beach")
 *     .setStartTime(OffsetDateTime.now().plusHours(1))
 *     .setEndTime(OffsetDateTime.now().plusHours(3))
 *     .setName("Discussing Turtle Shells")
 *     .queue();
 * }</pre>
 *
 * @see    GuildScheduledEvent#getManager()
 */
public interface GuildScheduledEventManager extends Manager<GuildScheduledEventManager>
{
    /** Used to reset the name field */
    long NAME         = 1;
    /** Used to reset the description field */
    long DESCRIPTION  = 1 << 1;
    /** Used to reset the location field */
    long LOCATION     = 1 << 2;
    /** Used to reset the start time field */
    long START_TIME   = 1 << 3;
    /** Used to reset the end time field */
    long END_TIME     = 1 << 4;
    /** Used to reset the image field */
    long IMAGE        = 1 << 5;
    /** Used to reset the status field */
    long STATUS       = 1 << 6;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(GuildScheduledEventManager.DESCRIPTION | GuildScheduledEventManager.END_TIME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #LOCATION}</li>
     *     <li>{@link #START_TIME}</li>
     *     <li>{@link #END_TIME}</li>
     *     <li>{@link #IMAGE}</li>
     *     <li>{@link #STATUS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return GuildScheduledEventManager for chaining convenience
     */
    @Nonnull
    @Override
    GuildScheduledEventManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(GuildScheduledEventManager.DESCRIPTION, GuildScheduledEventManager.END_TIME);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #LOCATION}</li>
     *     <li>{@link #START_TIME}</li>
     *     <li>{@link #END_TIME}</li>
     *     <li>{@link #IMAGE}</li>
     *     <li>{@link #STATUS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return GuildScheduledEventManager for chaining convenience
     */
    @Nonnull
    @Override
    GuildScheduledEventManager reset(long... fields);

    /**
     * The target {@link GuildScheduledEvent} for this manager
     */
    @Nonnull
    GuildScheduledEvent getGuildScheduledEvent();

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} this
     * {@link GuildScheduledEvent GuildScheduledEvent} is in.
     * <br>This is logically the same as calling {@code getGuildScheduledEvent().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    default Guild getGuild()
    {
        return getGuildScheduledEvent().getGuild();
    }

    /**
     * Sets the name of the selected {@link GuildScheduledEvent GuildScheduledEvent}
     *
     * @param  name
     *         The new name for the selected {@link GuildScheduledEvent GuildScheduledEvent}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the new name is blank, empty, {@code null}, or longer than {@value GuildScheduledEvent#MAX_NAME_LENGTH}
     *         characters
     *
     * @return GuildScheduledEventManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventManager setName(@Nonnull String name);

    /**
     * Sets the description of the selected {@link GuildScheduledEvent GuildScheduledEvent}.
     * This field may include markdown.
     *
     * @param  description
     *         The new description for the selected {@link GuildScheduledEvent GuildScheduledEvent},
     *         or {@code null} to reset the description
     *
     * @throws java.lang.IllegalArgumentException
     *         If the new description is longer than {@value GuildScheduledEvent#MAX_DESCRIPTION_LENGTH} characters
     *
     * @return GuildScheduledEventManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventManager setDescription(@Nullable String description);

    /**
     * Sets the cover image for the new {@link GuildScheduledEvent GuildScheduledEvent}.
     *
     * @param  icon
     *         The cover image for the new {@link GuildScheduledEvent GuildScheduledEvent},
     *         or {@code null} for no cover image.
     *
     * @return GuildScheduledEventManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventManager setImage(@Nullable Icon icon);

    /**
     * Sets the location of the selected {@link GuildScheduledEvent} to take place in the specified {@link GuildChannel}.
     * <p>This will change the event's type to {@link GuildScheduledEvent.Type#STAGE_INSTANCE} or {@link GuildScheduledEvent.Type#VOICE},
     * which are the only supported channel types for the location of scheduled events currently.
     *
     * @param  channel
     *         The {@link GuildChannel} that the selected {@link GuildScheduledEvent} is set to take place in.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@link GuildChannel} is {@code null}</li>
     *             <li>If the provided {@link GuildChannel} is not from the same guild</li>
     *             <li>If the provided {@link GuildChannel} is not a {@link StageChannel} or {@link VoiceChannel}</li>
     *         </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS},
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL},
     *         {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS},
     *         or {@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS Permission.VOICE_MOVE_OTHERS}, in the provided
     *         channel.
     *
     * @return GuildScheduledEventManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventManager setLocation(@Nonnull GuildChannel channel);

    /**
     * Sets the location of the selected {@link GuildScheduledEvent} to take place externally,
     * or not in a specific {@link GuildChannel}. <u>Please note that an event is required to have an end time set if
     * the location is external.</u>
     * <p>This will change the event's type to {@link GuildScheduledEvent.Type#EXTERNAL}
     *
     * @param  location
     *         The location that the selected {@link GuildScheduledEvent} is set to take place at.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided location is blank, empty, {@code null}, or longer than
     *         {@value GuildScheduledEvent#MAX_LOCATION_LENGTH}
     * @throws java.lang.IllegalStateException
     *         If the selected {@link GuildScheduledEvent} does not have an end time
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}
     *
     * @return GuildScheduledEventManager for chaining convenience
     *
     * @see    #setEndTime(TemporalAccessor)
     * @see    #setLocation(GuildChannel)
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventManager setLocation(@Nonnull String location);

    /**
     * Sets the time that the selected {@link GuildScheduledEvent} should start at.
     * Events of {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL} will automatically
     * start at this time. Events of {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
     * and {@link GuildScheduledEvent.Type#VOICE Type.VOICE} need to be manually started.
     * If the {@link GuildScheduledEvent} has not begun after its scheduled start time, it will be automatically cancelled after a few hours.
     *
     * @param  startTime
     *         The time that the selected {@link GuildScheduledEvent} is set to start at.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided start time is {@code null}</li>
     *             <li>If the provided start time is before the end time</li>
     *             <li>If the provided start time is before the current time</li>
     *         </ul>
     * @return GuildScheduledEventManager for chaining convenience
     *
     * @see    #setEndTime(TemporalAccessor)
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventManager setStartTime(@Nonnull TemporalAccessor startTime);

    /**
     * Sets the time that the selected {@link GuildScheduledEvent} should end at.
     * Events of {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL} will automatically
     * end at this time, and events of {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
     * and {@link GuildScheduledEvent.Type#VOICE Type.VOICE} will end a few minutes after the last
     * person has left the channel.
     *
     * @param  endTime
     *         The time that the selected {@link GuildScheduledEvent} is set to end at.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided end time is before the start time</li>
     *             <li>If the provided end time is {@code null}</li>
     *         </ul>
     * @throws java.lang.IllegalStateException
     *         If an end time is provided and the event is not set to take place at an external location
     *
     * @return GuildScheduledEventManager for chaining convenience
     *
     * @see    #setStartTime(TemporalAccessor)
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventManager setEndTime(@Nonnull TemporalAccessor endTime);

    /**
     * Sets the status of the event. This method may be used to start, end or cancel an event but can only be used to
     * complete one of the following transitions:
     * <ol>
     *     <li>{@link GuildScheduledEvent.Status#SCHEDULED Status.SCHEDULED} to {@link GuildScheduledEvent.Status#ACTIVE Status.ACTIVE}</li>
     *     <li>{@link GuildScheduledEvent.Status#SCHEDULED Status.SCHEDULED} to {@link GuildScheduledEvent.Status#CANCELED Status.CANCELED}</li>
     *     <li>{@link GuildScheduledEvent.Status#ACTIVE Status.ACTIVE} to {@link GuildScheduledEvent.Status#COMPLETED Status.COMPLETED}</li>
     * </ol>
     *
     * @param  status
     *         The new status
     *
     * @throws java.lang.IllegalStateException
     *         If the transition between statuses does not follow one of the three documented above.
     * @throws IllegalArgumentException
     *         If the provided status is {@code null}
     *
     * @return GuildScheduledEventManager for chaining convenience
     *
     * @see    #getGuildScheduledEvent()
     * @see    GuildScheduledEvent#getStatus()
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventManager setStatus(@Nonnull GuildScheduledEvent.Status status);
}
