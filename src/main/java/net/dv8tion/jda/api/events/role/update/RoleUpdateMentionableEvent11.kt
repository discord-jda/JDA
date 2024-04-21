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
 * Indicates that a [Role][net.dv8tion.jda.api.entities.Role] updated its mentionable state.
 *
 *
 * Can be used to retrieve the old mentionable state.
 *
 *
 * Identifier: `mentionable`
 */
class RoleUpdateMentionableEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull role: Role,
    wasMentionable: Boolean
) : GenericRoleUpdateEvent<Boolean?>(api, responseNumber, role, wasMentionable, !wasMentionable, IDENTIFIER) {
    /**
     * Whether the role was mentionable
     *
     * @return True, if this role was mentionable before this update
     */
    fun wasMentionable(): Boolean {
        return oldValue
    }

    @get:Nonnull
    override val oldValue: T?
        get() = super.getOldValue()

    @get:Nonnull
    override val newValue: T?
        get() = super.getNewValue()

    companion object {
        const val IDENTIFIER = "mentionable"
    }
}
