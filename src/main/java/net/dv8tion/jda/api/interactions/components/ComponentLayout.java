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

import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a top-level layout used for {@link Component Components} such as {@link Button Buttons}.
 *
 * <p>Components must always be contained within such a layout.
 *
 * @see ActionRow
 */
public interface ComponentLayout extends SerializableData, Iterable<Component>
{
    /**
     * List representation of this component layout.
     * <br>This list is modifiable. Note that empty action rows are not supported.
     *
     * @return {@link List} of components in this action row
     */
    @Nonnull
    List<Component> getComponents();

    /**
     * List of buttons in this component layout.
     *
     * @return Immutable {@link List} of {@link Button Buttons}
     */
    @Nonnull
    List<Button> getButtons();

    /**
     * The {@link Type} of layout
     *
     * @return The layout {@link Type} or {@link Type#UNKNOWN}
     */
    @Nonnull
    Type getType();

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
     * <br>This checks that there is at least one component in this layout and it does not violate {@link Component#getMaxPerRow()}.
     *
     * @return True, if this layout is valid
     */
    default boolean isValid()
    {
        if (isEmpty())
            return false;
        List<Component> components = getComponents();
        Map<Component.Type, List<Component>> groups = components.stream().collect(Collectors.groupingBy(Component::getType));
        if (groups.size() > 1) // TODO: You can't mix components right now but maybe in the future, we need to check back on this when that happens
            return false;

        for (Map.Entry<Component.Type, List<Component>> entry : groups.entrySet())
        {
            Component.Type type = entry.getKey();
            List<Component> list = entry.getValue();
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
     * @return The old {@link Component} that was replaced or removed
     */
    @Nullable
    default Component updateComponent(@Nonnull String id, @Nullable Component newComponent)
    {
        Checks.notNull(id, "ID");
        List<Component> list = getComponents();
        for (ListIterator<Component> it = list.listIterator(); it.hasNext();)
        {
            Component component = it.next();
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
    static boolean updateComponent(@Nonnull List<? extends ComponentLayout> layouts, @Nonnull String id, @Nullable Component newComponent)
    {
        Checks.notNull(layouts, "ComponentLayout");
        Checks.notEmpty(id, "ID or URL");
        for (Iterator<? extends ComponentLayout> it = layouts.iterator(); it.hasNext();)
        {
            ComponentLayout components = it.next();
            Component oldComponent = components.updateComponent(id, newComponent);
            if (oldComponent != null)
            {
                if (components.getComponents().isEmpty())
                    it.remove();
                else if (!components.isValid() && newComponent != null)
                    throw new IllegalArgumentException("Cannot replace " + oldComponent.getType() + " with " + newComponent.getType() + " due to a violation of the layout maximum. The resulting ComponentLayout is invalid!");
                return !Objects.equals(oldComponent, newComponent);
            }
        }
        return false;
    }

    /**
     * The layout types
     */
    enum Type
    {
        UNKNOWN(-1),
        ACTION_ROW(1)
        ;
        private final int key;

        Type(int key)
        {
            this.key = key;
        }

        /**
         * The raw type value
         *
         * @return The raw type value
         */
        public int getKey()
        {
            return key;
        }
    }
}
