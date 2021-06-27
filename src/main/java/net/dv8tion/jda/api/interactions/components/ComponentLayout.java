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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

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

    default Component updateComponent(@Nonnull String id, @Nullable Component newComponent)
    {
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

    static boolean updateComponent(@Nonnull List<? extends ComponentLayout> layout, @Nonnull String id, @Nullable Component newComponent)
    {
        Checks.notNull(layout, "ComponentLayout");
        Checks.notEmpty(id, "ID or URL");
        for (Iterator<? extends ComponentLayout> it = layout.iterator(); it.hasNext();)
        {
            ComponentLayout components = it.next();
            Component oldComponent = components.updateComponent(id, newComponent);
            if (oldComponent != null)
            {
                if (components.getComponents().isEmpty())
                    it.remove();
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
