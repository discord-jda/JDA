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
package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} specifically
 * designed to create a {@link ScheduledEvent ScheduledEvent}.
 * This extension allows setting properties such as the name or description of an event before it is
 * created.
 *
 * <p><b>Requirements</b><br>
 * Events that are created are required to have a name, a location, and a start time. Depending on the
 * type of location provided, an event will be of one of three different {@link ScheduledEvent.Type Types}:
 * <ol>
 *     <li>
 *         {@link ScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
 *         <br>These events are set to take place inside of a {@link net.dv8tion.jda.api.entities.channel.concrete.StageChannel StageChannel}. The
 *         following permissions are required in the specified stage channel in order to create an event there:
 *          <ul>
 *              <li>{@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}</li>
 *              <li>{@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}</li>
 *              <li>{@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}</li>
 *              <li>{@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS Permission.VOICE_MOVE_OTHERS}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@link ScheduledEvent.Type#VOICE Type.VOICE}
 *         <br>These events are set to take place inside of a {@link net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel}. The
 *         following permissions are required in the specified voice channel in order to create an event there:
 *         <ul>
 *             <li>{@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}</li>
 *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
 *             <li>{@link net.dv8tion.jda.api.Permission#VOICE_CONNECT Permission.VOICE_CONNECT}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@link ScheduledEvent.Type#EXTERNAL Type.EXTERNAL}
 *         <br>These events are set to take place at an external location. {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}
 *         is required on the guild level in order to create this type of event. Additionally, an end time <em>must</em>
 *         also be specified.
 *     </li>
 * </ol>
 *
 * @see    net.dv8tion.jda.api.entities.Guild
 * @see    Guild#createScheduledEvent(String, String, OffsetDateTime, OffsetDateTime) 
 * @see    Guild#createScheduledEvent(String, net.dv8tion.jda.api.entities.channel.middleman.GuildChannel, OffsetDateTime)
 */
public interface ScheduledEventAction extends FluentAuditableRestAction<ScheduledEvent, ScheduledEventAction>
{
    /**
     * The guild to create the {@link ScheduledEvent} in
     *
     * @return The guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * Sets the name for the new {@link ScheduledEvent ScheduledEvent}.
     *
     * @param  name
     *         The name for the new {@link ScheduledEvent ScheduledEvent}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the new name is blank, empty, {@code null}, or contains more than {@value ScheduledEvent#MAX_NAME_LENGTH}
     *         characters
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    ScheduledEventAction setName(@Nonnull String name);

    /**
     * Sets the description for the new {@link ScheduledEvent ScheduledEvent}.
     * This field may include markdown.
     *
     * @param  description
     *         The description for the new {@link ScheduledEvent ScheduledEvent},
     *         or {@code null} for no description
     *
     * @throws java.lang.IllegalArgumentException
     *         If the new description is longer than {@value ScheduledEvent#MAX_DESCRIPTION_LENGTH} characters
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ScheduledEventAction setDescription(@Nullable String description);

    /**
     * <p>Sets the time that the new {@link ScheduledEvent} will start at.
     * Events of {@link ScheduledEvent.Type#EXTERNAL Type.EXTERNAL} will automatically
     * start at this time, but events of {@link ScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
     * and {@link ScheduledEvent.Type#VOICE Type.VOICE} will need to be manually started,
     * and will automatically be cancelled a few hours after the start time if not.
     *
     * @param  startTime
     *         The time that the new {@link ScheduledEvent} should start at
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided start time is {@code null}, or takes place after the end time
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    ScheduledEventAction setStartTime(@Nonnull TemporalAccessor startTime);

    /**
     * Sets the time that the new {@link ScheduledEvent} will end at.
     * Events of {@link ScheduledEvent.Type#EXTERNAL Type.EXTERNAL} will automatically
     * end at this time, and events of {@link ScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
     * and {@link ScheduledEvent.Type#VOICE Type.VOICE} will end a few minutes after the last
     * user has left the channel.
     * <p><b>Note:</b> Setting an end time is only possible for events of {@link ScheduledEvent.Type#EXTERNAL Type.EXTERNAL}.
     *
     * @param  endTime
     *         The time that the new {@link ScheduledEvent} is set to end at
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided end time is chronologically set before the start time
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    ScheduledEventAction setEndTime(@Nullable TemporalAccessor endTime);

    /**
     * Sets the cover image for the new {@link ScheduledEvent ScheduledEvent}.
     *
     * @param  icon
     *         The cover image for the new {@link ScheduledEvent ScheduledEvent},
     *         or {@code null} for no cover image
     *
     * @return The current ScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ScheduledEventAction setImage(@Nullable Icon icon);
}
