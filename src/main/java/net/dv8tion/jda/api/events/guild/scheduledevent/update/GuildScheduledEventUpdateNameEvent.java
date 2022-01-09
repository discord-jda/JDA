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
import net.dv8tion.jda.api.entities.GuildScheduledEvent;

import javax.annotation.Nonnull;

/**
 * Indicates the {@link GuildScheduledEvent#getName() name} of a {@link GuildScheduledEvent} has changed.
 *
 * <p>Can be used to detect when the {@link GuildScheduledEvent} name had changed and retrieve the old one.
 *
 * <p>Identifier: {@code guild_scheduled_event_name}
 */
public class GuildScheduledEventUpdateNameEvent extends GenericGuildScheduledEventUpdateEvent<String>
{
    public static final String IDENTIFIER = "guild_scheduled_event_name";

    public GuildScheduledEventUpdateNameEvent(@Nonnull JDA api, long responseNumber, @Nonnull GuildScheduledEvent guildScheduledEvent, @Nonnull String previous)
    {
        super(api, responseNumber, guildScheduledEvent, previous, guildScheduledEvent.getDescription(), IDENTIFIER);
    }

    /**
     * The old {@link GuildScheduledEvent#getName() name}.
     *
     * @return The old name
     */
    @Nonnull
    public String getOldName()
    {
        return getOldValue();
    }

    /**
     * The new {@link GuildScheduledEvent#getName() name}.
     *
     * @return The new name
     */
    @Nonnull
    public String getNewName()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public String getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public String getNewValue()
    {
        return super.getNewValue();
    }
}
