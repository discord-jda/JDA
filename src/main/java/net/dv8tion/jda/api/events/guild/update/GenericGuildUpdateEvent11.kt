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
package net.dv8tion.jda.api.events.guild.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.UpdateEvent
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import javax.annotation.Nonnull

/**
 * Indicates that a [Guild][net.dv8tion.jda.api.entities.Guild] was updated.
 *
 *
 * Can be used to detect when a Guild is updated.
 */
abstract class GenericGuildUpdateEvent<T>(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull guild: Guild?,
    previous: T?, next: T?, @Nonnull identifier: String
) : GenericGuildEvent(api, responseNumber, guild), UpdateEvent<Guild?, T?> {
    protected val previous: T
    protected val next: T

    @get:Nonnull
    override val propertyIdentifier: String

    init {
        this.previous = previous
        this.next = next
        propertyIdentifier = identifier
    }

    @get:Nonnull
    override val entity: E
        get() = getGuild()
    override val oldValue: T?
        get() = previous
    override val newValue: T?
        get() = next
}
