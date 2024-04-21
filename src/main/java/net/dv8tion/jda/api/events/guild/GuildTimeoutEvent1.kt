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
import net.dv8tion.jda.api.events.*
import javax.annotation.Nonnull

/**
 * Indicates that a guild failed to ready up and timed out.
 * <br></br>Usually this event will be fired right before a [ReadyEvent].
 *
 *
 * This will mark the guild as **unavailable** and it will not be usable when JDA becomes ready.
 * You can check all unavailable guilds with [ReadyEvent.getGuildUnavailableCount] and [JDA.getUnavailableGuilds].
 *
 *
 * **Developer Note**<br></br>
 *
 *
 * Discord may also explicitly mark guilds as unavailable during the setup, in which case this event will not fire.
 * It is recommended to check for unavailable guilds in the ready event explicitly to avoid any ambiguity.
 */
class GuildTimeoutEvent(
    @Nonnull api: JDA,
    /**
     * The guild id for the timed out guild
     *
     * @return The guild id
     */
    val guildIdLong: Long
) : Event(api) {

    /**
     * The guild id for the timed out guild
     *
     * @return The guild id
     */
    @Nonnull
    fun getGuildId(): String {
        return java.lang.Long.toUnsignedString(guildIdLong)
    }
}
