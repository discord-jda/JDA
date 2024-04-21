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
package net.dv8tion.jda.api.events.interaction.component

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import javax.annotation.Nonnull

/**
 * Indicates that a [ComponentInteraction] was created in a channel.
 * <br></br>Every component interaction event is derived from this event.
 *
 *
 * **Requirements**<br></br>
 * To receive these events, you must unset the **Interactions Endpoint URL** in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the [Discord Developers Portal](https://discord.com/developers/applications).
 */
open class GenericComponentInteractionCreateEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @param:Nonnull private val interaction: ComponentInteraction
) : GenericInteractionCreateEvent(api, responseNumber, interaction), ComponentInteraction {
    @Nonnull
    override fun getInteraction(): ComponentInteraction? {
        return interaction
    }

    @Nonnull
    override fun getChannel(): MessageChannelUnion {
        return interaction.getChannel()
    }

    @Nonnull
    override fun getComponentId(): String {
        return interaction.componentId
    }

    @Nonnull
    override fun getComponent(): ActionComponent {
        return interaction.component
    }

    @Nonnull
    override fun getMessage(): Message {
        return interaction.message
    }

    override fun getMessageIdLong(): Long {
        return interaction.messageIdLong
    }

    @Nonnull
    override fun getComponentType(): Component.Type {
        return interaction.componentType
    }

    @Nonnull
    override fun deferEdit(): MessageEditCallbackAction {
        return interaction.deferEdit()
    }

    @Nonnull
    override fun getHook(): InteractionHook {
        return interaction.hook
    }

    @Nonnull
    override fun deferReply(): ReplyCallbackAction {
        return interaction.deferReply()
    }

    @Nonnull
    override fun replyModal(@Nonnull modal: Modal): ModalCallbackAction {
        return interaction.replyModal(modal)
    }

    @Nonnull
    override fun replyWithPremiumRequired(): PremiumRequiredCallbackAction {
        return interaction.replyWithPremiumRequired()
    }
}
