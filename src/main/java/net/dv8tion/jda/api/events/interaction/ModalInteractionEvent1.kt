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
package net.dv8tion.jda.api.events.interaction

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.modals.ModalInteraction
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import javax.annotation.Nonnull

/**
 * Indicates that a [Modal] was submitted.
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 *
 * @see ModalInteraction
 */
class ModalInteractionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @param:Nonnull private val interaction: ModalInteraction
) : GenericInteractionCreateEvent(api, responseNumber, interaction), ModalInteraction {
    @Nonnull
    override fun getInteraction(): ModalInteraction? {
        return interaction
    }

    @Nonnull
    override fun getModalId(): String {
        return interaction.modalId
    }

    @Nonnull
    override fun getValues(): List<ModalMapping> {
        return interaction.values
    }

    override fun getMessage(): Message? {
        return interaction.message
    }

    @Nonnull
    override fun deferReply(): ReplyCallbackAction {
        return interaction.deferReply()
    }

    @Nonnull
    override fun getHook(): InteractionHook {
        return interaction.hook
    }

    @Nonnull
    override fun deferEdit(): MessageEditCallbackAction {
        return interaction.deferEdit()
    }

    @Nonnull
    override fun getChannel(): MessageChannelUnion {
        return interaction.getChannel()
    }
}
