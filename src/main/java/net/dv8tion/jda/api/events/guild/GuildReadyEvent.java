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

package net.dv8tion.jda.api.events.guild;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link Guild} finished setting up
 * <br>This event is fired if a guild finished setting up during login phase.
 * After this event is fired, JDA will start dispatching events related to this guild.
 * This indicates a guild was created and added to the cache. It will be fired for both the initial
 * setup and full reconnects (indicated by {@link SessionRecreateEvent}).
 *
 * <p>Can be used to initialize any services that depend on this guild.
 *
 * <p>When a guild fails to ready up due to Discord outages you will not receive this event.
 * Guilds that fail to ready up will either timeout or get marked as unavailable.
 * <br>You can use {@link ReadyEvent#getGuildUnavailableCount()} and {@link JDA#getUnavailableGuilds()} to check for unavailable guilds.
 * {@link GuildTimeoutEvent} will be fired for guilds that don't ready up and also don't get marked as unavailable by Discord.
 * Guilds that timeout will be marked as unavailable by the timeout event,
 * they will <b>not</b> fire a {@link GuildUnavailableEvent} as that event is only indicating that a guild becomes unavailable <b>after</b> ready happened.
 */
public class GuildReadyEvent extends GenericGuildEvent
{
    public GuildReadyEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild)
    {
        super(api, responseNumber, guild);
    }
}
