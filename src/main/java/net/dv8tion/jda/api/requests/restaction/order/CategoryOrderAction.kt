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

import net.dv8tion.jda.api.entities.channel.concrete.Category
import javax.annotation.Nonnull

/**
 * An extension of [ChannelOrderAction] with
 * similar functionality, but constrained to the bounds of a single [Category][net.dv8tion.jda.api.entities.channel.concrete.Category].
 * <br></br>To apply the changes you must finish the [RestAction][net.dv8tion.jda.api.requests.RestAction].
 *
 *
 * Before you can use any of the `move` methods
 * you must use either [selectPosition(GuildChannel)][.selectPosition] or [.selectPosition]!
 *
 * @author Kaidan Gustave
 *
 * @see Category.modifyTextChannelPositions
 * @see Category.modifyVoiceChannelPositions
 */
interface CategoryOrderAction : ChannelOrderAction {
    @get:Nonnull
    val category: Category?
}
