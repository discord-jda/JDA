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
 * Indicates that the {@link GuildScheduledEvent#getStatus() status} of a {@link GuildScheduledEvent} has changed. The status
 * of a {@link GuildScheduledEvent} represents if it is currently active, canceled, or completed.
 *
 * <p>Can be used to detect when the {@link GuildScheduledEvent} status has changed.
 *
 * <p>Identifier: {@code status}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_SCHEDULED_EVENTS GUILD_SCHEDULED_EVENTS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * Discord does not specifically tell us about the updates, but merely tells us the
 * {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} was updated and gives us the updated {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} object.
 * In order to fire a specific event like this we need to have the old {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} cached to compare against.
 */
public class GuildScheduledEventUpdateStatusEvent extends GenericGuildScheduledEventUpdateEvent<GuildScheduledEvent.Status>
{
    public static final String IDENTIFIER = "status";

    public GuildScheduledEventUpdateStatusEvent(@Nonnull JDA api, long responseNumber, @Nonnull GuildScheduledEvent guildScheduledEvent, @Nonnull GuildScheduledEvent.Status previous)
    {
        super(api, responseNumber, guildScheduledEvent, previous, guildScheduledEvent.getStatus(), IDENTIFIER);
    }

    /**
     * The old {@link GuildScheduledEvent#getStatus() status}.
     *
     * @return The old status
     */
    @Nonnull
    public GuildScheduledEvent.Status getOldStatus()
    {
        return getOldValue();
    }

    /**
     * The new {@link GuildScheduledEvent#getStatus() status}.
     *
     * @return The new status
     */
    @Nonnull
    public GuildScheduledEvent.Status getNewStatus()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public GuildScheduledEvent.Status getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public GuildScheduledEvent.Status getNewValue()
    {
        return super.getNewValue();
    }
}
