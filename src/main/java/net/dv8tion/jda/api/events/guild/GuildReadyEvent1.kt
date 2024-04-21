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
package net.dv8tion.jda.api.events.guild

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that a [Guild] finished setting up
 * <br></br>This event is fired if a guild finished setting up during login phase.
 * After this event is fired, JDA will start dispatching events related to this guild.
 * This indicates a guild was created and added to the cache. It will be fired for both the initial
 * setup and full reconnects (indicated by [SessionRecreateEvent]).
 *
 *
 * Can be used to initialize any services that depend on this guild.
 *
 *
 * When a guild fails to ready up due to Discord outages you will not receive this event.
 * Guilds that fail to ready up will either timeout or get marked as unavailable.
 * <br></br>You can use [ReadyEvent.getGuildUnavailableCount] and [JDA.getUnavailableGuilds] to check for unavailable guilds.
 * [GuildTimeoutEvent] will be fired for guilds that don't ready up and also don't get marked as unavailable by Discord.
 * Guilds that timeout will be marked as unavailable by the timeout event,
 * they will **not** fire a [GuildUnavailableEvent] as that event is only indicating that a guild becomes unavailable **after** ready happened.
 */
class GuildReadyEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild?) :
    GenericGuildEvent(api, responseNumber, guild)
