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
package net.dv8tion.jda.api.events.self

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.events.*
import javax.annotation.Nonnull

/**
 * Indicates that a [SelfUser][net.dv8tion.jda.api.entities.SelfUser] changed or started an activity.
 * <br></br>Every SelfUserEvent is derived from this event and can be casted.
 *
 *
 * Can be used to detect any SelfUserEvent.
 */
abstract class GenericSelfUpdateEvent<T>(
    @Nonnull api: JDA, responseNumber: Long,
    previous: T?, next: T?, @Nonnull identifier: String
) : Event(api, responseNumber), UpdateEvent<SelfUser, T?> {
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
    val selfUser: SelfUser
        /**
         * The [SelfUser][net.dv8tion.jda.api.entities.SelfUser]
         *
         * @return The [SelfUser][net.dv8tion.jda.api.entities.SelfUser]
         */
        get() = api.getSelfUser()

    @get:Nonnull
    override val entity: E
        get() = selfUser
    override val oldValue: T?
        get() = previous
    override val newValue: T?
        get() = next
}
