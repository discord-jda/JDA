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
import net.dv8tion.jda.api.managers.GuildScheduledEventManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} specifically
 * designed to create a {@link GuildScheduledEvent GuildScheduledEvent}.
 * This extension allows setting properties such as the name or description of an event before it is
 * created.
 *
 * <h2>Requirements</h2>
 * Events that are created are required to have a name, a location, and a start time. Depending on the
 * type of location provided, an event will be of one of three different {@link GuildScheduledEvent.Type Types}:
 * <ol>
 *     <li>
 *         {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
 *         <br>These events are set to take place inside of a {@link net.dv8tion.jda.api.entities.StageChannel StageChannel}. The
 *         following permissions are required in the specified stage channel in order to create an event there:
 *          <ul>
 *              <li>{@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}</li>
 *              <li>{@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}</li>
 *              <li>{@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}</li>
 *              <li>{@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS Permission.VOICE_MOVE_OTHERS}}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@link GuildScheduledEvent.Type#VOICE Type.VOICE}
 *         <br>These events are set to take place inside of a {@link net.dv8tion.jda.api.entities.VoiceChannel}. The
 *         following permissions are required in the specified voice channel in order to create an event there:
 *         <ul>
 *             <li>{@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}</li>
 *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
 *             <li>{@link net.dv8tion.jda.api.Permission#VOICE_CONNECT Permission.VOICE_CONNECT}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL}
 *         <br>These events are set to take place at a custom location. {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}
 *         is required on the guild level in order to create this type of event. Additionally, an end time <i>must</i>
 *         also be specified.
 *     </li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * guildScheduledEventAction.setName("Cactus Beauty Contest")
 *    .setDescription("Come and have your cacti judged! _Must be spikey to enter_")
 *    .setStartTime(OffsetDateTime.of(LocalDate.of(2022, 12, 31), LocalTime.of(11, 30), ZoneOffset.ofHours(7)))
 *    .setEndTime(OffsetDateTime.of(LocalDate.of(2025, 12, 31), LocalTime.of(15, 30), ZoneOffset.ofHours(7)))
 *    .setLocation("Mike's Backyard")
 *    .queue();
 * }</pre>
 *
 * @see    net.dv8tion.jda.api.entities.Guild
 * @see    Guild#createScheduledEvent()
 */
public interface GuildScheduledEventAction extends AuditableRestAction<GuildScheduledEvent>
{
    @Nonnull
    @Override
    GuildScheduledEventAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    GuildScheduledEventAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    GuildScheduledEventAction deadline(long timestamp);

    /**
     * The guild to create the {@link GuildScheduledEvent} in
     *
     * @return The guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * Sets the name for the new {@link GuildScheduledEvent GuildScheduledEvent}.
     * <br><b>Note:</b> A name is required to be set before creating an event.
     *
     * @param  name
     *         The name for the new {@link GuildScheduledEvent GuildScheduledEvent}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the new name is empty, {@code null}, or contains more than {@value GuildScheduledEvent#MAX_NAME_LENGTH}
     *         characters
     *
     * @return The current GuildScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction setName(@Nonnull String name);

    /**
     * Sets the description for the new {@link GuildScheduledEvent GuildScheduledEvent}.
     * This field may include markdown.
     *
     * @param  description
     *         The description for the new {@link GuildScheduledEvent GuildScheduledEvent},
     *         or {@code null} for no description.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the new description contains more than {@value GuildScheduledEvent#MAX_DESCRIPTION_LENGTH}
     *         characters.
     *
     * @return The current GuildScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction setDescription(@Nonnull String description);

    /**
     * Sets the cover image for the new {@link GuildScheduledEvent GuildScheduledEvent}.
     *
     * @param  icon
     *         The cover image for the new {@link GuildScheduledEvent GuildScheduledEvent},
     *         or {@code null} for no cover image.
     *
     * @return The current GuildScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction setImage(@Nonnull Icon icon);

    /**
     * Sets the location for the new {@link GuildScheduledEvent} to take place in a specified stage channel.
     * This will also change the event's type to {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type#STAGE_INSTANCE}, and any previously set external location or
     * voice channel info will be lost!
     * <p><b>Note:</b> A location is required to be set by either this method, {@link #setLocation(VoiceChannel)} or
     * {@link #setLocation(String)} before the event is created.
     *
     * @param  stageChannel
     *         The Stage Channel that the new {@link GuildScheduledEvent} is set to take place in.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Stage Channel is {@code null}, or is not from the same guild
     *         that the event is being created in.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.ManageEvents},
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL},
     *         {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS},
     *         or {@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS Permission.VOICE_MOVE_OTHERS}, in the provided
     *         stage channel.
     *
     * @return The current GuildScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction setLocation(@Nonnull StageChannel stageChannel);

    /**
     * Sets the location for the new {@link GuildScheduledEvent} to take place in a specified voice channel. This will
     * also change the event's type to {@link GuildScheduledEvent.Type#VOICE}, and any previously set external location
     * or stage channel info will be lost!
     * <p><b>Note:</b> A location is required to be set by either this method, {@link #setLocation(StageChannel)} or {@link #setLocation(String)} before the
     * event is created.
     *
     * @param  voiceChannel
     *         The Voice Channel that the selected {@link GuildScheduledEvent} is set to take place at.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided Voice Channel is {@code null}, or is not from the same guild
     *         that the event is being created in.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.ManageEvents},
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}, or
     *         {@link net.dv8tion.jda.api.Permission#VOICE_CONNECT Permission.VOICE_CONNECT} in the provided
     *         Voice Channel.
     *
     * @return The current GuildScheduledEventAction, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction setLocation(@Nonnull VoiceChannel voiceChannel);

    /**
     * Sets the location for the new {@link GuildScheduledEvent} to take place "externally",
     * or at a custom location opposed to a specific Voice or Stage Channel. <u>Please note that it is required to set an end time using
     * {@link #setEndTime(OffsetDateTime)} before this method is called.</u>
     * <p>This will also change the event's type to {@link GuildScheduledEvent.Type#EXTERNAL}, and any previously set voice or
     * stage channel info will be lost!
     * <p><b>Note:</b> A location is required to be sent by either this method, {@link #setLocation(StageChannel)} or {@link #setLocation(VoiceChannel)} before the
     * event is created.
     *
     * @param  externalLocation
     *         The location that the new {@link GuildScheduledEvent} is set to take place at.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided location is {@code null}
     *
     * @throws java.lang.IllegalStateException
     *         If the selected {@link GuildScheduledEvent} does not have an end time associated with it
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}
     *
     * @return The current GuildScheduledEventAction, for chaining convenience
     *
     * @see    #setEndTime(OffsetDateTime)
     * @see    #setLocation(StageChannel)
     * @see    #setLocation(VoiceChannel)
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction setLocation(@Nonnull String externalLocation);

    /**
     * <p>Sets the time that the new {@link GuildScheduledEvent} should start at.
     * Events of {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL} will automatically
     * start at this time, but events of {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
     * and {@link GuildScheduledEvent.Type#VOICE Type.VOICE} will need to be manually started,
     * and will automatically be cancelled a few hours after the start time if not.
     * <p><b>Note:</b> A start time is required to be set before the event is created.
     *
     * @param  startTime
     *         The time that the new {@link GuildScheduledEvent} should start at
     *
     * @throws java.lang.IllegalArgumentException If the provided start time is {@code null}, or takes place after the
     * end time.
     *
     * @return The current GuildScheduledEventAction, for chaining convenience
     *
     * @see    #setEndTime(OffsetDateTime)
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction setStartTime(@Nonnull OffsetDateTime startTime);

    /**
     * Sets the time that the new {@link GuildScheduledEvent} should end at.
     * Events of {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL} will automatically
     * end at this time, and events of {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
     * and {@link GuildScheduledEvent.Type#VOICE Type.VOICE} will end a few minutes after the last
     * person has left the channel.
     * <p><b>Note:</b> The end time is required to be set if the event is taking place at a custom/external location, and
     * this method must be called before {@link #setLocation(String)} if so. Otherwise, setting the end time is optional
     * for events of {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE} and {@link GuildScheduledEvent.Type#VOICE Type.VOICE}.
     *
     * @param  endTime
     *         The time that the new {@link GuildScheduledEvent} is set to end at,
     *         or {@code null} for no end time to be set.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided end time is before the start time
     *
     * @throws java.lang.IllegalStateException
     *         If the provided end time is {@code null} when the event is set to take place at an external location
     *
     * @return The current GuildScheduledEventAction, for chaining convenience
     *
     * @see    #setStartTime(OffsetDateTime)
     */
    @Nonnull
    @CheckReturnValue
    GuildScheduledEventAction setEndTime(@Nullable OffsetDateTime endTime);
}
