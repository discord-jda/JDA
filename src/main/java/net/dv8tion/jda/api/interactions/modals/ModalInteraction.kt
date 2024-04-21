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
package net.dv8tion.jda.api.interactions.modals

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Interaction on a [Modal]
 *
 *
 * If the modal of this interaction was a reply to a [ComponentInteraction][net.dv8tion.jda.api.interactions.components.ComponentInteraction],
 * you can also use [.deferEdit] to edit the original message that contained the component instead of replying.
 *
 * @see net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
 */
interface ModalInteraction : IReplyCallback, IMessageEditCallback {
    @get:Nonnull
    val modalId: String?

    @get:Nonnull
    val values: List<ModalMapping?>

    /**
     * Convenience method to get a [ModalMapping][net.dv8tion.jda.api.interactions.modals.ModalMapping] by its id from the List of [ModalMappings][net.dv8tion.jda.api.interactions.modals.ModalMapping]
     *
     *
     * Returns null if no component with that id has been found
     *
     * @param  id
     * The custom id
     *
     * @throws IllegalArgumentException
     * If the provided id is null
     *
     * @return ModalMapping with this id, or null if not found
     *
     * @see .getValues
     */
    fun getValue(@Nonnull id: String): ModalMapping? {
        Checks.notNull(id, "ID")
        return values.stream()
            .filter { mapping: ModalMapping? -> mapping.getId() == id }
            .findFirst().orElse(null)
    }

    /**
     * Message this modal came from, if it was a reply to a [ComponentInteraction][net.dv8tion.jda.api.interactions.components.ComponentInteraction].
     *
     * @return The message the component is attached to, or `null`
     */
    val message: Message?
    @Nonnull
    override fun getChannel(): MessageChannelUnion?
    @Nonnull
    override fun getGuildChannel(): GuildMessageChannelUnion? {
        return super<IReplyCallback>.getGuildChannel() as GuildMessageChannelUnion
    }
}
