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
 * Indicates that a [Role][net.dv8tion.jda.api.entities.Role] updated its name.
 *
 *
 * Can be used to retrieve the old name.
 *
 *
 * Identifier: `name`
 */
class RoleUpdateNameEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull role: Role, @Nonnull oldName: String?) :
    GenericRoleUpdateEvent<String?>(api, responseNumber, role, oldName, role.name, IDENTIFIER) {
    @get:Nonnull
    val oldName: String?
        /**
         * The old name
         *
         * @return The old name
         */
        get() = oldValue

    @get:Nonnull
    val newName: String?
        /**
         * The new name
         *
         * @return The new name
         */
        get() = newValue

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "name"
    }
}
