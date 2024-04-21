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
package net.dv8tion.jda.api.interactions.components.buttons

import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.requests.RestAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Interaction on a [Button] component.
 *
 * @see ButtonInteractionEvent
 */
interface ButtonInteraction : ComponentInteraction {
    @Nonnull
    override fun getComponent(): Button {
        return button
    }

    @get:Nonnull
    val button: Button

    /**
     * Update the button with a new button instance.
     *
     *
     * If this interaction is already acknowledged this will use [.getHook]
     * and otherwise [.editComponents] directly to acknowledge the interaction.
     *
     * @param  newButton
     * The new button to use, or null to remove this button from the message entirely
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editButton(newButton: Button?): RestAction<Void?>? {
        val message = message
        val components: List<ActionRow?> = ArrayList(message.actionRows)
        LayoutComponent.updateComponent(components, componentId, newButton)
        return if (isAcknowledged) hook!!.editMessageComponentsById(message.id, components)!!
            .map { it -> null } else editComponents(components)!!.map { it: InteractionHook? -> null }
    }
}
