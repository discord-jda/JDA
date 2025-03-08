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
import org.apache.commons.collections4.iterators.SingletonIterator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ComponentPathIterator implements Iterator<ComponentPathIterator.ComponentWithPath>
{
    private final Stack<Iterator<ComponentWithPath>> stack = new Stack<>();

    protected ComponentPathIterator(String rootName, Collection<? extends Component> components) {
        stack.push(new CollectionAttributeIterator(rootName, "components", components));
    }

    @Nonnull
    public static ComponentPathIterator create(@Nonnull String rootName, @Nonnull Collection<? extends Component> components) {
        return new ComponentPathIterator(rootName, components);
    }

    @Nonnull
    public static Stream<ComponentWithPath> createStream(@Nonnull String rootName, @Nonnull Collection<? extends Component> components) {
        Spliterator<ComponentWithPath> spliterator = Spliterators.spliteratorUnknownSize(create(rootName, components), Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public boolean hasNext() {
        ensureNestedIteratorHasNext();
        return !stack.isEmpty();
    }

    @Nonnull
    @Override
    public ComponentWithPath next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Iterator<ComponentWithPath> iterator = stack.peek();
        ComponentWithPath componentWithPath = iterator.next();
        Component component = componentWithPath.component;

        if (component instanceof Container) {
            Container container = (Container) component;
            stack.push(new CollectionAttributeIterator(componentWithPath.path, "components", container.getComponents()));
        } else if (component instanceof ActionRow) {
            ActionRow actionRow = (ActionRow) component;
            stack.push(new CollectionAttributeIterator(componentWithPath.path, "components", actionRow.getComponents()));
        } else if (component instanceof Section) {
            Section section = (Section) component;

            stack.push(new CollectionAttributeIterator(componentWithPath.path, "components", section.getContentComponents()));
            stack.push(singleAttributeIterator(componentWithPath.path, "accessory", section.getAccessory()));
        }

        return componentWithPath;
    }

    private void ensureNestedIteratorHasNext() {
        while (!stack.isEmpty() && !stack.peek().hasNext()) {
            stack.pop();
        }
    }

    public static class ComponentWithPath {
        public final Component component;
        public final String path;

        ComponentWithPath(Component component, String path) {
            this.component = component;
            this.path = path;
        }
    }

    public static class CollectionAttributeIterator implements Iterator<ComponentWithPath> {
        final Iterator<? extends Component> nestedIterator;
        final String parentPath;
        final String listName;
        int index = 0;

        CollectionAttributeIterator(String parentPath, String listName, Collection<? extends Component> collection) {
            this.nestedIterator = collection.iterator();
            this.parentPath = parentPath;
            this.listName = listName;
        }

        @Override
        public boolean hasNext()
        {
            return this.nestedIterator.hasNext();
        }

        @Nonnull
        @Override
        public ComponentWithPath next()
        {
            Component component = this.nestedIterator.next();
            String listIndexPath = this.listName + "[" + index++ + "]";

            return makeComponentWithPath(this.parentPath, listIndexPath, component);
        }
    }

    private static SingletonIterator<ComponentWithPath> singleAttributeIterator(String parentPath, String attributePath, Component component) {
        return new SingletonIterator<>(makeComponentWithPath(parentPath, attributePath, component));
    }

    private static ComponentWithPath makeComponentWithPath(String parentPath, String attributePath, Component component) {
        String path = String.format("%s.%s<%s>", parentPath, attributePath, component.getType());
        return new ComponentWithPath(component, path);
    }
}
