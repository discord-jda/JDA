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

package net.dv8tion.jda.api.components.tree;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.IComponentUnion;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.utils.ComponentIterator;
import net.dv8tion.jda.internal.components.tree.ComponentTreeImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a tree of components, in which you can find, replace or remove components recursively.
 *
 * <p>As with every component, component trees are immutable and will return a new instance on every mutating call.
 *
 * @param <E> Type of components contained by this tree
 */
public interface ComponentTree<E extends Component>
{
    /**
     * Creates a {@link ComponentTree} from the given components,
     * and checks their compatibility.
     *
     * @param  unionType
     *         The union type expected from the components
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided, or if one of the component is not of the provided union type
     *
     * @return A {@link ComponentTree} containing the given components
     */
    @Nonnull
    static <E extends Component, T extends IComponentUnion> ComponentTree<T> of(@Nonnull Class<T> unionType, @Nonnull Collection<E> components)
    {
        Checks.notNull(unionType, "Component union type");
        Checks.noneNull(components, "Components");
        // We don't care if there are unknown components, they will be unpacked and checked when sending
        return new ComponentTreeImpl<>(unionType, ComponentsUtil.membersToUnionWithUnknownType(components, unionType));
    }

    /**
     * Creates a {@link ComponentTree} from the given components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link ComponentTree} containing the given components
     */
    @Nonnull
    static ComponentTree<IComponentUnion> of(@Nonnull Collection<? extends Component> components)
    {
        Checks.noneNull(components, "Components");
        return new ComponentTreeImpl<>(IComponentUnion.class, ComponentsUtil.membersToUnionWithUnknownType(components, IComponentUnion.class));
    }

    /**
     * Creates a {@link MessageComponentTree} from the given top-level message components.
     * <br>This is a shortcut for {@code MessageComponentTree.of(components)}.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link MessageComponentTree} containing the given components
     */
    @Nonnull
    static MessageComponentTree forMessage(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        return MessageComponentTree.of(components);
    }

    /**
     * Creates a {@link ModalComponentTree} from the given top-level message components.
     * <br>This is a shortcut for {@code ModalComponentTree.of(components)}.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link ModalComponentTree} containing the given components
     */
    @Nonnull
    static ModalComponentTree forModal(@Nonnull Collection<? extends ModalTopLevelComponent> components)
    {
        return ModalComponentTree.of(components);
    }

    /**
     * Returns the type of this component tree.
     *
     * @return The type of this component tree
     */
    @Nonnull
    Type getType();

    /**
     * Unmodifiable list of components contained by this tree.
     *
     * @return An unmodifiable list of components in this tree
     */
    @Nonnull
    @Unmodifiable
    List<E> getComponents();

    /**
     * Finds all components with the given type, recursively.
     * <br>This is a shortcut for {@code findAll(type, _ -> true)}
     *
     * @param  type
     *         The type of components to search for
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A modifiable list of components with the specified type
     */
    @Nonnull
    default <T extends Component> List<T> findAll(@Nonnull Class<T> type)
    {
        return findAll(type, c -> true);
    }

    /**
     * Finds all components with the given type and satisfying the filter, recursively.
     *
     * @param  type
     *         The type of components to search for
     * @param  filter
     *         The component filter to apply
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A modifiable list of components satisfying the type and filter
     */
    @Nonnull
    default <T extends Component> List<T> findAll(@Nonnull Class<T> type, @Nonnull Predicate<? super T> filter)
    {
        Checks.notNull(type, "Component type");
        Checks.notNull(filter, "Component filter");

        return ComponentIterator.createStream(getComponents())
                .filter(type::isInstance)
                .map(type::cast)
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Finds the first component with the given type and satisfying the filter, recursively.
     *
     * @param  type
     *         The type of components to search for
     * @param  filter
     *         The component filter to apply
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return An {@link Optional} possibly containing a component satisfying the type and filter
     */
    @Nonnull
    default <T extends Component> Optional<T> find(@Nonnull Class<T> type, @Nonnull Predicate<? super T> filter)
    {
        Checks.notNull(type, "Component type");
        Checks.notNull(filter, "Component filter");

        return ComponentIterator.createStream(getComponents())
                .filter(type::isInstance)
                .map(type::cast)
                .filter(filter)
                .findFirst();
    }

    /**
     * Replaces and/or removes components using the provided {@link ComponentReplacer},
     * and construct a new tree from the result.
     *
     * @param  replacer
     *         The {@link ComponentReplacer} to apply
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A new tree with the new components
     *
     * @see ComponentReplacer
     */
    @Nonnull
    @CheckReturnValue
    ComponentTree<E> replace(@Nonnull ComponentReplacer replacer);

    /**
     * Enables or disables all components which {@linkplain net.dv8tion.jda.api.components.attribute.IDisableable can be enabled/disabled},
     * and constructs a new tree from the result.
     *
     * @return A new tree with all components enabled/disabled.
     */
    @Nonnull
    @CheckReturnValue
    ComponentTree<E> withDisabled(boolean disabled);

    /**
     * Disables all components which {@linkplain net.dv8tion.jda.api.components.attribute.IDisableable can be disabled},
     * and constructs a new tree from the result.
     *
     * @return A new tree with all components disabled.
     */
    @Nonnull
    @CheckReturnValue
    default ComponentTree<E> asDisabled()
    {
        return withDisabled(true);
    }

    /**
     * Enables all components which {@linkplain net.dv8tion.jda.api.components.attribute.IDisableable can be enabled},
     * and constructs a new tree from the result.
     *
     * @return A new tree with all components enabled.
     */
    @Nonnull
    @CheckReturnValue
    default ComponentTree<E> asEnabled()
    {
        return withDisabled(false);
    }

    /**
     * Represents the type of component tree.
     */
    enum Type
    {
        /**
         * A component tree of no specific type,
         * this includes ones made with {@link ComponentTree#of(Collection)} or {@link ComponentTree#of(Class, Collection)}.
         */
        ANY,

        /**
         * Represents a {@link MessageComponentTree}.
         */
        MESSAGE,

        /**
         * Represents a {@link ModalComponentTree}.
         */
        MODAL,
        ;
    }
}
