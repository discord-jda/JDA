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
package net.dv8tion.jda.api.events.role.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * Indicates that a [Role][net.dv8tion.jda.api.entities.Role] updated its position.
 *
 *
 * Can be used to retrieve the old position.
 *
 *
 * Identifier: `position`
 */
class RoleUpdatePositionEvent(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull role: Role, oldPosition: Int,
    /**
     * The old position
     *
     * @return The old position
     */
    val oldPositionRaw: Int
) : GenericRoleUpdateEvent<Int?>(api, responseNumber, role, oldPosition, role.position, IDENTIFIER) {

    /**
     * The new position
     *
     * @return The new position
     */
    val newPositionRaw: Int

    init {
        newPositionRaw = role.positionRaw
    }

    val oldPosition: Int
        /**
         * The old position
         *
         * @return The old position
         */
        get() = oldValue
    val newPosition: Int
        /**
         * The new position
         *
         * @return The new position
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "position"
    }
}
