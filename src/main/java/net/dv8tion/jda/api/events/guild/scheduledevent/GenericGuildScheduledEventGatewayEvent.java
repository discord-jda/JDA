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
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that a gateway event relating to a {@link GuildScheduledEvent} has been fired.
 *
 * <p> It should be noted that a {@link GuildScheduledEvent} is not an
 * actual gateway event found in the {@link net.dv8tion.jda.api.events} package, but are rather entities similar to
 * {@link net.dv8tion.jda.api.entities.User User} or {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} objects
 * representing a <a href="https://support.discord.com/hc/en-us/articles/4409494125719-Scheduled-Events">guild's scheduled events</a>.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_SCHEDULED_EVENTS GUILD_SCHEDULED_EVENTS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p> This class may be used to check if a gateway event is related to a {@link GuildScheduledEvent}
 * as all gateway events in the {@link net.dv8tion.jda.api.events.guild.scheduledevent} package extend this class.
 */
public abstract class GenericGuildScheduledEventGatewayEvent extends GenericGuildEvent
{
    protected final GuildScheduledEvent guildScheduledEvent;

    public GenericGuildScheduledEventGatewayEvent(@Nonnull JDA api, long responseNumber, @Nonnull GuildScheduledEvent guildScheduledEvent)
    {
        super(api, responseNumber, guildScheduledEvent.getGuild());
        this.guildScheduledEvent = guildScheduledEvent;
    }

    /**
     * The {@link GuildScheduledEvent}
     *
     * @return The GuildScheduledEvent
     */
    @Nonnull
    public GuildScheduledEvent getGuildScheduledEvent()
    {
        return guildScheduledEvent;
    }
}
