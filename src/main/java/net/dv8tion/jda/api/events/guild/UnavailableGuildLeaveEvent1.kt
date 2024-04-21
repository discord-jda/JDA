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
import net.dv8tion.jda.api.events.Event
import javax.annotation.Nonnull

/**
 * Indicates that you left a [Guild][net.dv8tion.jda.api.entities.Guild] that is not yet available.
 * **This does not extend [GenericGuildEvent][net.dv8tion.jda.api.events.guild.GenericGuildEvent]**
 *
 *
 * Can be used to retrieve id of the unavailable Guild.
 */
class UnavailableGuildLeaveEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The id for the guild we left.
     *
     * @return The id for the guild
     */
    val guildIdLong: Long
) : Event(api, responseNumber) {

    /**
     * The id for the guild we left.
     *
     * @return The id for the guild
     */
    @Nonnull
    fun getGuildId(): String {
        return java.lang.Long.toUnsignedString(guildIdLong)
    }
}
