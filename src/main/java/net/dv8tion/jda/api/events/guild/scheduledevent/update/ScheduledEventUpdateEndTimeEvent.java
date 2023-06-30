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
package net.dv8tion.jda.api.events.guild.scheduledevent.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Indicates the {@link ScheduledEvent#getEndTime() end time} of a {@link ScheduledEvent} has changed.
 *
 * <p>Can be used to detect when the {@link ScheduledEvent} end time has changed.
 *
 * <p>Identifier: {@code end_time}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Discord does not specifically tell us about the updates, but merely tells us the
 * {@link ScheduledEvent ScheduledEvent} was updated and gives us the updated {@link ScheduledEvent ScheduledEvent} object.
 * In order to fire a specific event like this we need to have the old {@link ScheduledEvent ScheduledEvent} cached to compare against.
 */
public class ScheduledEventUpdateEndTimeEvent extends GenericScheduledEventUpdateEvent<OffsetDateTime>
{
    public static final String IDENTIFIER = "end_time";

    public ScheduledEventUpdateEndTimeEvent(@Nonnull JDA api, long responseNumber, @Nonnull ScheduledEvent scheduledEvent, @Nullable OffsetDateTime previous)
    {
        super(api, responseNumber, scheduledEvent, previous, scheduledEvent.getEndTime(), IDENTIFIER);
    }

    /**
     * The old {@link ScheduledEvent#getEndTime() end time}.
     *
     * @return The old end time, or {@code null} if no end time was previously set.
     */
    @Nullable
    public OffsetDateTime getOldEndTime()
    {
        return getOldValue();
    }

    /**
     * The new {@link ScheduledEvent#getEndTime() end time}.
     *
     * @return The new start time, or {@code null} if the end time has been removed.
     */
    @Nullable
    public OffsetDateTime getNewEndTime()
    {
        return getNewValue();
    }
}
