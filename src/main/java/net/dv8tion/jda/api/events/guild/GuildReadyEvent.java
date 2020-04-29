/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Guild Guild} finished setting up
 * <br>This event is fired if a guild finished setting up during login phase.
 * After this event is fired, JDA will start dispatching events related to this guild.
 * This indicates a guild was created and added to the cache. It will be fired for both the initial
 * setup and full reconnects (indicated by {@link net.dv8tion.jda.api.events.ReconnectedEvent ReconnectedEvent}).
 *
 * <p>Can be used to initialize any services that depend on this guild.
 */
public class GuildReadyEvent extends GenericGuildEvent
{
    public GuildReadyEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild)
    {
        super(api, responseNumber, guild);
    }
}
