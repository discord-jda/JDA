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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
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
import java.util.stream.Collectors;

/**
 * Component Interaction for a {@link RoleSelectMenu}.
 *
 * @see SelectMenuInteractionEvent
 */
public interface RoleSelectMenuInteraction extends ComponentInteraction
{
    @Nonnull
    @Override
    RoleSelectMenu getComponent();

    /**
     * The {@link RoleSelectMenu} this interaction belongs to.
     *
     * @return The {@link RoleSelectMenu}
     *
     * @see    #getComponentId()
     */
    @Nonnull
    default RoleSelectMenu getSelectMenu()
    {
        return getComponent();
    }

    /**
     * If available, this will resolve the selected {@link #getValues() values} to the representative {@link SelectOption SelectOption} instances.
     * <br>This is null if the message is ephemeral.
     *
     * @return {@link List} of the selected options or null if this message is ephemeral
     */
    @Nonnull
    default List<Role> getSelectedOptions()
    {
        List<Role> values = getValues();
        return getGuild().getRoles()
                .stream()
                .filter(values::contains)
                .collect(Collectors.toList());
    }

    /**
     * The selected values. These are defined in the individual {@link SelectOption SelectOptions}.
     *
     * @return {@link List} of {@link SelectOption#getValue()}
     */
    @Nonnull
    List<Role> getValues();

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
     *
     * @see    SelectMenu#createCopy()
     * @see    SelectMenu#create(String)
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> editSelectMenu(@Nullable RoleSelectMenu newMenu)
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
