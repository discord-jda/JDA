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
package net.dv8tion.jda.api.events.guild.scheduledevent;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.events.annotations.RequiredCacheFlags;
import net.dv8tion.jda.api.events.annotations.RequiredIntents;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link ScheduledEvent} object has been deleted.
 *
 * <p>Can be used to detect when a {@link ScheduledEvent} was deleted and retrieve the deleted scheduled event.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables this by default!
 */
@RequiredIntents(always = GatewayIntent.SCHEDULED_EVENTS)
@RequiredCacheFlags(always = CacheFlag.SCHEDULED_EVENTS)
public class ScheduledEventDeleteEvent extends GenericScheduledEventGatewayEvent
{
    public ScheduledEventDeleteEvent(@Nonnull JDA api, long responseNumber, @Nonnull ScheduledEvent scheduledEvent)
    {
        super(api, responseNumber, scheduledEvent);
    }
}
