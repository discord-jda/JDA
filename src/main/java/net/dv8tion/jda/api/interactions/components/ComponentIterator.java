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

import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.interactions.components.container.Container;
import net.dv8tion.jda.api.interactions.components.section.Section;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ComponentIterator implements Iterator<Component>
{
    private final Stack<Iterator<? extends Component>> stack = new Stack<>();

    protected ComponentIterator(List<? extends Component> components) {
        stack.push(components.iterator());
    }

    public static ComponentIterator create(List<? extends Component> components) {
        return new ComponentIterator(components);
    }

    public static Stream<Component> createStream(List<? extends Component> components) {
        Spliterator<Component> spliterator = Spliterators.spliteratorUnknownSize(create(components), Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public boolean hasNext() {
        ensureNestedIteratorHasNext();
        return !stack.isEmpty();
    }

    @Override
    public Component next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Iterator<? extends Component> iterator = stack.peek();
        Component component = iterator.next();

        Iterator<? extends Component> childrenIterator = getIteratorForComponent(component);
        if (childrenIterator != null) {
            stack.push(childrenIterator);
        }

        return component;
    }

    private void ensureNestedIteratorHasNext() {
        while (!stack.isEmpty() && !stack.peek().hasNext()) {
            stack.pop();
        }
    }

    @Nullable
    private static Iterator<? extends Component> getIteratorForComponent(Component component) {
        if (component instanceof Container) {
            Container container = (Container) component;
            return container.getComponents().iterator();
        } else if (component instanceof ActionRow) {
            ActionRow actionRow = (ActionRow) component;
            return actionRow.getComponents().iterator();
        } else if (component instanceof Section) {
            Section section = (Section) component;

            List<Component> sectionComponents = new ArrayList<>(section.getComponents());
            sectionComponents.add(section.getAccessory());

            return sectionComponents.iterator();
        }

        return null;
    }
}
