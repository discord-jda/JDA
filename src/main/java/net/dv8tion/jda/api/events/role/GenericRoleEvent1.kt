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
package net.dv8tion.jda.api.events.role

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.Event
import javax.annotation.Nonnull

/**
 * Indicates that a [Role][net.dv8tion.jda.api.entities.Role] was created/deleted/changed.
 * <br></br>Every RoleEvent is derived from this event and can be casted.
 *
 *
 * Can be used to detect any RoleEvent.
 */
abstract class GenericRoleEvent(
    @Nonnull api: JDA, responseNumber: Long,
    /**
     * The role for this event
     *
     * @return The role for this event
     */
    @get:Nonnull
    @param:Nonnull val role: Role
) : Event(api, responseNumber) {

    @get:Nonnull
    val guild: Guild
        /**
         * The guild of the role
         *
         * @return The guild of the role
         */
        get() = role.guild
}
