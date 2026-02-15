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

/**
 * Indicates the {@linkplain ScheduledEvent#getCoverImageId() cover image ID} of a {@link ScheduledEvent} has changed.
 *
 * <p>Can be used to detect when the {@link ScheduledEvent} cover image had changed.
 *
 * <p>Identifier: {@code image}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Discord does not specifically tell us about the updates, but merely tells us the
 * {@link ScheduledEvent} was updated and gives us the updated {@link ScheduledEvent} object.
 * In order to fire a specific event like this we need to have the old {@link ScheduledEvent} cached to compare against.
 */
public class ScheduledEventUpdateCoverImageEvent extends GenericScheduledEventUpdateEvent<String> {
    public static final String IDENTIFIER = "image";

    public ScheduledEventUpdateCoverImageEvent(
            @Nonnull JDA api, long responseNumber, @Nonnull ScheduledEvent scheduledEvent, @Nullable String previous) {
        super(api, responseNumber, scheduledEvent, previous, scheduledEvent.getCoverImageId(), IDENTIFIER);
    }

    /**
     * The old {@linkplain ScheduledEvent#getCoverImageId() cover image ID}.
     *
     * @return The old cover image's ID
     */
    @Nullable
    public String getOldImageUrl() {
        return getOldValue();
    }

    /**
     * The new {@linkplain ScheduledEvent#getCoverImageId() cover image ID}.
     *
     * @return The new cover image's ID
     */
    @Nullable
    public String getNewImageUrl() {
        return getNewValue();
    }
}
