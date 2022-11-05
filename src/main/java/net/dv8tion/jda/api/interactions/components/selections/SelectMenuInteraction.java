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

package net.dv8tion.jda.api.interactions.components.selections;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Component Interaction for a {@link SelectMenu}.
 *
 * @param <T>
 *        The select menu value type
 * @param <S>
 *        The type of select menu
 *
 * @see GenericSelectMenuInteractionEvent
 * @see EntitySelectInteraction
 * @see StringSelectInteraction
 */
public interface SelectMenuInteraction<T, S extends SelectMenu> extends ComponentInteraction
{
    @Nonnull
    @Override
    S getComponent();

    /**
     * The {@link SelectMenu} this interaction belongs to.
     *
     * @return The {@link SelectMenu}
     *
     * @see    #getComponentId()
     */
    @Nonnull
    default S getSelectMenu()
    {
        return getComponent();
    }

    /**
     * The provided selection.
     *
     * @return {@link List} of {@link T}
     */
    @Nonnull
    List<T> getValues();

    /**
     * Update the select menu with a new select menu instance.
     *
     * <p>If this interaction is already acknowledged this will use {@link #getHook()}
     * and otherwise {@link #editComponents(Collection)} directly to acknowledge the interaction.
     *
     * @param  newMenu
     *         The new select menu to use, or null to remove this menu from the message entirely
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> editSelectMenu(@Nullable SelectMenu newMenu)
    {
        Message message = getMessage();
        List<ActionRow> components = new ArrayList<>(message.getActionRows());
        LayoutComponent.updateComponent(components, getComponentId(), newMenu);

        if (isAcknowledged())
            return getHook().editMessageComponentsById(message.getId(), components).map(it -> null);
        else
            return editComponents(components).map(it -> null);
    }
}
