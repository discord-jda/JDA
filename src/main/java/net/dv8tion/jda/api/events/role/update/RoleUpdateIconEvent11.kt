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
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.RoleIcon
import javax.annotation.Nonnull

/**
 * Indicates that the Icon of a [Role][net.dv8tion.jda.api.entities.Role] changed.
 *
 *
 * Can be used to detect when a role's icon or emoji changes and retrieve the old one
 *
 *
 * Identifier: `icon`
 */
class RoleUpdateIconEvent(@Nonnull api: JDA, responseNumber: Long, @Nonnull role: Role, oldIcon: RoleIcon?) :
    GenericRoleUpdateEvent<RoleIcon?>(api, responseNumber, role, oldIcon, role.icon, IDENTIFIER) {
    val oldIcon: RoleIcon?
        /**
         * The old icon
         *
         * @return The old icon
         */
        get() = oldValue
    val newIcon: RoleIcon?
        /**
         * The new icon
         *
         * @return The new icon
         */
        get() = newValue

    companion object {
        const val IDENTIFIER = "icon"
    }
}
