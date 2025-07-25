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

package net.dv8tion.jda.api.interactions.components.buttons;

import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Interaction on a {@link Button} component.
 *
 * @see ButtonInteractionEvent
 */
public interface ButtonInteraction extends ComponentInteraction
{
    @Nonnull
    @Override
    default Button getComponent()
    {
        return getButton();
    }

    /**
     * The {@link Button} this interaction belongs to.
     *
     * @return The {@link Button}
     *
     * @see    #getComponentId()
     */
    @Nonnull
    Button getButton();

    /**
     * Update the button with a new button instance.
     *
     * <p>If this interaction is already acknowledged this will use {@link #getHook()}
     * and otherwise {@link #editComponents(Collection)} directly to acknowledge the interaction.
     *
     * @param  newButton
     *         The new button to use, or null to remove this button from the message entirely
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> editButton(@Nullable Button newButton)
    {
        final Message message = getMessage();
        final MessageComponentTree newTree = message.getComponentTree().replace(ComponentReplacer.byUniqueId(getButton(), newButton));

        if (isAcknowledged())
            return getHook().editMessageComponentsById(message.getId(), newTree.getComponents())
                    .useComponentsV2(message.isUsingComponentsV2())
                    .map(it -> null);
        else
            return editComponents(newTree.getComponents())
                    .useComponentsV2(message.isUsingComponentsV2())
                    .map(it -> null);
    }
}
