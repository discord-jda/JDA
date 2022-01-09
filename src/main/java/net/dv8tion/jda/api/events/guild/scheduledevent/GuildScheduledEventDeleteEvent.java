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
import net.dv8tion.jda.api.entities.GuildScheduledEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link GuildScheduledEvent} object has been deleted.
 *
 * Can be used to detect when a {@link GuildScheduledEvent} was deleted and retrieve the deleted scheduled event.
 */
public class GuildScheduledEventDeleteEvent extends GenericGuildScheduledEventGatewayEvent
{
    public GuildScheduledEventDeleteEvent(@Nonnull JDA api, long responseNumber, @Nonnull GuildScheduledEvent guildScheduledEvent)
    {
        super(api, responseNumber, guildScheduledEvent);
    }
}
