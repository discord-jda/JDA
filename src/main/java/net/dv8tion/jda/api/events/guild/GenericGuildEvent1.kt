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
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.Event
import javax.annotation.Nonnull

/**
 * Indicates that a [Guild][net.dv8tion.jda.api.entities.Guild] event is fired.
 * <br></br>Every GuildEvent is an instance of this event and can be casted.
 *
 *
 * Can be used to detect any GuildEvent.
 */
abstract class GenericGuildEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The [Guild][net.dv8tion.jda.api.entities.Guild]
     *
     * @return The Guild
     */
    @get:Nonnull
    @param:Nonnull val guild: Guild
) : Event(api, responseNumber)
