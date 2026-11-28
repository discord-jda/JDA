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

package net.dv8tion.jda.api.interactions.components.overflowmenu;

import net.dv8tion.jda.api.components.menu.MenuActionItem;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.MenuActionItemInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Collection;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interaction on a {@link MenuActionItem} component.
 *
 * @see MenuActionItemInteractionEvent
 */
public interface MenuActionItemInteraction extends ComponentInteraction {
    @Nonnull
    @Override
    default MenuActionItem getComponent() {
        return getItem();
    }

    /**
     * The {@link MenuActionItem} this interaction belongs to.
     *
     * @return The {@link MenuActionItem}
     *
     * @see    #getComponentId()
     */
    @Nonnull
    MenuActionItem getItem();

    /**
     * Update the overflow menu item with a new instance.
     *
     * <p>If this interaction is already acknowledged this will use {@link #getHook()}
     * and otherwise {@link #editComponents(Collection)} directly to acknowledge the interaction.
     *
     * @param  newItem
     *         The new item to use, or null to remove this item from the message entirely
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> editItem(@Nullable MenuActionItem newItem) {
        Message message = getMessage();
        MessageComponentTree newTree =
                message.getComponentTree().replace(ComponentReplacer.byUniqueId(getItem(), newItem));

        if (isAcknowledged()) {
            return getHook()
                    .editMessageComponentsById(message.getId(), newTree.getComponents())
                    .useComponentsV2(message.isUsingComponentsV2())
                    .map(it -> null);
        } else {
            return editComponents(newTree.getComponents())
                    .useComponentsV2(message.isUsingComponentsV2())
                    .map(it -> null);
        }
    }
}
