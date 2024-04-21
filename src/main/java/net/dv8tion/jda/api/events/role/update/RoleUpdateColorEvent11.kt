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
import java.awt.Color
import javax.annotation.Nonnull

/**
 * Indicates that a [Role][net.dv8tion.jda.api.entities.Role] updated its color.
 *
 *
 * Can be used to retrieve the old color.
 *
 *
 * Identifier: `color`
 */
class RoleUpdateColorEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull role: Role, oldColor: Int) :
    GenericRoleUpdateEvent<Int?>(api, responseNumber, role, oldColor, role.colorRaw, IDENTIFIER) {
    val oldColor: Color?
        /**
         * The old color
         *
         * @return The old color, or null
         */
        get() = if (previous != Role.DEFAULT_COLOR_RAW) Color(previous!!) else null
    val oldColorRaw: Int
        /**
         * The raw rgb value of the old color
         *
         * @return The raw rgb value of the old color
         */
        get() = oldValue
    val newColor: Color?
        /**
         * The new color
         *
         * @return The new color, or null
         */
        get() = if (next != Role.DEFAULT_COLOR_RAW) Color(next!!) else null
    val newColorRaw: Int
        /**
         * The raw rgb value of the new color
         *
         * @return The raw rgb value of the new color
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "color"
    }
}
