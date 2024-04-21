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
package net.dv8tion.jda.api.requests.restaction.order

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import javax.annotation.Nonnull

/**
 * Implementation of [OrderAction]
 * designed to modify the order of [Roles][net.dv8tion.jda.api.entities.Role] of the
 * specified [Guild][net.dv8tion.jda.api.entities.Guild].
 * <br></br>To apply the changes you must finish the [RestAction][net.dv8tion.jda.api.requests.RestAction]
 *
 *
 * Before you can use any of the `move` methods
 * you must use either [selectPosition(Role)][.selectPosition] or [.selectPosition]!
 *
 *
 * **This uses descending order!**
 *
 * @since 3.0
 *
 * @see net.dv8tion.jda.api.entities.Guild.modifyRolePositions
 * @see net.dv8tion.jda.api.entities.Guild.modifyRolePositions
 */
interface RoleOrderAction : OrderAction<Role?, RoleOrderAction?> {
    @get:Nonnull
    val guild: Guild?
}
