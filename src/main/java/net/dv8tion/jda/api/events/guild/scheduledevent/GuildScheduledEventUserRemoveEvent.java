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
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.User User} has shown that they are no longer intertested/have unsubscribed
 * to a {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent}.
 *
 * Can be used to detect when someone has indicated that they are no longer interested in an event and also retrieve their
 * {@link {@link net.dv8tion.jda.api.entities.User User} object as well as the {@link GuildScheduledEvent}.
 */
public class GuildScheduledEventUserRemoveEvent extends GenericGuildScheduledEventGatewayEvent
{
    private final User user;

    public GuildScheduledEventUserRemoveEvent(@Nonnull JDA api, long responseNumber, @Nonnull GuildScheduledEvent guildScheduledEvent, @Nonnull User user)
    {
        super(api, responseNumber, guildScheduledEvent);
        this.user = user;
    }

    /**
     * The user that indicated that they are no longer interested in the event.
     *
     * @return The user
     */
    @Nonnull
    public User getUser()
    {
        return user;
    }
}
