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

package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a top-level layout used for {@link ActionComponent ActionComponents} such as {@link Button Buttons}.
 *
 * <p>Components must always be contained within such a layout.
 *
 * @see ActionRow
 */
public interface LayoutComponent extends SerializableData, Iterable<ActionComponent>, Component
{
    /**
     * List representation of this component layout.
     * <br>This list is modifiable. Note that empty action rows are not supported.
     *
     * @return {@link List} of components in this action row
     */
    @Nonnull
    List<ActionComponent> getComponents();

    /**
     * List of buttons in this component layout.
     *
     * @return Immutable {@link List} of {@link Button Buttons}
     */
    @Nonnull
    List<Button> getButtons();

    /**
     * Whether all components in this layout are {@link ActionComponent#isDisabled() disabled}.
     * <br>Note that this is a universal quantifier, which means false <b>does not</b> imply {@link #isEnabled()}!
     *
     * @return True, if all components are disabled
     */
    default boolean isDisabled()
    {
        return getComponents().stream().allMatch(ActionComponent::isDisabled);
    }

    /**
     * Whether all components in this layout are {@link ActionComponent#isDisabled() enabled}.
     * <br>Note that this is a universal quantifier, which means false <b>does not</b> imply {@link #isDisabled()}!
     *
     * @return True, if all components are enabled
     */
    default boolean isEnabled()
    {
        return getComponents().stream().noneMatch(ActionComponent::isDisabled);
    }

    /**
     * Returns a new instance of this LayoutComponent with all components set to disabled/enabled.
     * <br>This does not modify the layout this was called on. To do this in-place, you can use {@link #getComponents()}.
     *
     * @param  disabled
     *         True if the components should be set to disabled, false if they should be enabled
     *
     * @return The new layout component with all components updated
     *
     * @see    ActionComponent#withDisabled(boolean)
     */
    @Nonnull
    @CheckReturnValue
    LayoutComponent withDisabled(boolean disabled);

    /**
     * Returns a new instance of this LayoutComponent with all components set to disabled.
     * <br>This does not modify the layout this was called on. To do this in-place, you can use {@link #getComponents()}.
     *
     * @return The new layout component with all components updated
     *
     * @see    ActionComponent#asDisabled()
     */
    @Nonnull
    @CheckReturnValue
    LayoutComponent asDisabled();

    /**
     * Returns a new instance of this LayoutComponent with all components set to enabled.
     * <br>This does not modify the layout this was called on. To do this in-place, you can use {@link #getComponents()}.
     *
     * @return The new layout component with all components updated
     *
     * @see    ActionComponent#asEnabled()
     */
    @Nonnull
    @CheckReturnValue
    LayoutComponent asEnabled();

    /**
     * Check whether this layout is empty.
     * <br>Identical to {@code getComponents().isEmpty()}
     *
     * @return True, if this layout has no components
     */
    default boolean isEmpty()
    {
        return getComponents().isEmpty();
    }

    /**
     * Check whether this is a valid layout configuration.
     * <br>This checks that there is at least one component in this layout and it does not violate {@link ActionComponent#getMaxPerRow()}.
     *
     * @return True, if this layout is valid
     */
    default boolean isValid()
    {
        if (isEmpty())
            return false;
        List<ActionComponent> components = getComponents();
        Map<Component.Type, List<ActionComponent>> groups = components.stream().collect(Collectors.groupingBy(Component::getType));
        if (groups.size() > 1) // TODO: You can't mix components right now but maybe in the future, we need to check back on this when that happens
            return false;

        for (Map.Entry<Component.Type, List<ActionComponent>> entry : groups.entrySet())
        {
            Component.Type type = entry.getKey();
            List<ActionComponent> list = entry.getValue();
            if (list.size() > type.getMaxPerRow())
                return false;
        }

        return true;
    }

    /**
     * Find and replace a component in this layout.
     * <br>This will locate and replace the existing component with the specified ID. If you provide null it will be removed instead.
     *
     * @param  id
     *         The custom id of this component, can also be a URL for a {@link Button} with {@link ButtonStyle#LINK}
     * @param  newComponent
     *         The new component or null to remove it
     *
     * @throws IllegalArgumentException
     *         If the provided id is null
     *
     * @return The old {@link ActionComponent} that was replaced or removed
     */
    @Nullable
    default ActionComponent updateComponent(@Nonnull String id, @Nullable ActionComponent newComponent)
    {
        Checks.notNull(id, "ID");
        List<ActionComponent> list = getComponents();
        for (ListIterator<ActionComponent> it = list.listIterator(); it.hasNext();)
        {
            ActionComponent component = it.next();
            if (id.equals(component.getId()) || (component instanceof Button && id.equals(((Button) component).getUrl())))
            {
                if (newComponent == null)
                    it.remove();
                else
                    it.set(newComponent);
                return component;
            }
        }
        return null;
    }

    /**
     * Find and replace a component in this list of layouts.
     * <br>This will locate and replace the existing component with the specified ID. If you provide null it will be removed instead.
     *
     * <p>If one of the layouts is empty after removing the component, it will be removed from the list.
     * This is an inplace operation and modifies the provided list directly.
     *
     * @param  layouts
     *         The layouts to modify
     * @param  id
     *         The custom id of this component, can also be a URL for a {@link Button} with {@link ButtonStyle#LINK}
     * @param  newComponent
     *         The new component or null to remove it
     *
     * @throws UnsupportedOperationException
     *         If the list cannot be modified
     * @throws IllegalArgumentException
     *         If the provided id or list is null or the replace operation results in an {@link #isValid() invalid} layout
     *
     * @return True, if any of the layouts was modified
     */
    static boolean updateComponent(@Nonnull List<? extends LayoutComponent> layouts, @Nonnull String id, @Nullable ActionComponent newComponent)
    {
        Checks.notNull(layouts, "LayoutComponent");
        Checks.notEmpty(id, "ID or URL");
        for (Iterator<? extends LayoutComponent> it = layouts.iterator(); it.hasNext();)
        {
            LayoutComponent components = it.next();
            ActionComponent oldComponent = components.updateComponent(id, newComponent);
            if (oldComponent != null)
            {
                if (components.getComponents().isEmpty())
                    it.remove();
                else if (!components.isValid() && newComponent != null)
                    throw new IllegalArgumentException("Cannot replace " + oldComponent.getType() + " with " + newComponent.getType() + " due to a violation of the layout maximum. The resulting LayoutComponent is invalid!");
                return !Objects.equals(oldComponent, newComponent);
            }
        }
        return false;
    }
}
