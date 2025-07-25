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

package net.dv8tion.jda.api.components.utils;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class to recursively iterate on a list of components.
 */
public class ComponentIterator implements Iterator<Component>
{
    private final Deque<Iterator<? extends Component>> stack = new ArrayDeque<>();

    protected ComponentIterator(Collection<? extends Component> components)
    {
        stack.push(components.iterator());
    }

    /**
     * Creates a {@link ComponentIterator} to recursively iterate on the provided components.
     *
     * @param  components
     *         The components to iterate on
     *
     * @return A new {@link ComponentIterator}
     */
    @Nonnull
    public static ComponentIterator create(@Nonnull Collection<? extends Component> components)
    {
        return new ComponentIterator(components);
    }

    /**
     * Creates a {@link Stream} of {@link Component} which recursively iterates on the provided components.
     *
     * @param  components
     *         The components to iterate on
     *
     * @return A new, ordered {@link Stream} of {@link Component}
     */
    @Nonnull
    public static Stream<Component> createStream(@Nonnull Collection<? extends Component> components)
    {
        Spliterator<Component> spliterator = Spliterators.spliteratorUnknownSize(create(components), Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public boolean hasNext()
    {
        ensureNestedIteratorHasNext();
        return !stack.isEmpty();
    }

    @Nonnull
    @Override
    public Component next()
    {
        if (!hasNext())
            throw new NoSuchElementException();
        Iterator<? extends Component> iterator = stack.peek();
        Component component = iterator.next();

        Iterator<? extends Component> childrenIterator = getIteratorForComponent(component);
        if (childrenIterator != null)
            stack.push(childrenIterator);

        return component;
    }

    private void ensureNestedIteratorHasNext()
    {
        while (!stack.isEmpty() && !stack.peek().hasNext())
            stack.pop();
    }

    @Nullable
    private static Iterator<? extends Component> getIteratorForComponent(Component component)
    {
        if (component instanceof Container)
        {
            Container container = (Container) component;
            return container.getComponents().iterator();
        }
        else if (component instanceof ActionRow)
        {
            ActionRow actionRow = (ActionRow) component;
            return actionRow.getComponents().iterator();
        }
        else if (component instanceof Section)
        {
            Section section = (Section) component;

            List<Component> sectionComponents = new ArrayList<>(section.getContentComponents());
            sectionComponents.add(section.getAccessory());

            return sectionComponents.iterator();
        }

        return null;
    }
}
