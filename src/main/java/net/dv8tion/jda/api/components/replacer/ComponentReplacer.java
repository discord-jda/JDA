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

package net.dv8tion.jda.api.components.replacer;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.internal.components.replacer.TypedComponentReplacerImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Functional interface similar to a {@link Function},
 * which takes a {@link Component} as an input and can change/remove the component based on what is returned.
 * <br>Component replacers can be used by {@link ComponentTree} and {@link IReplaceable#replace(ComponentReplacer)}.
 *
 * <p>This interface also provides static factories to help you use the most common replacers.
 */
@FunctionalInterface
public interface ComponentReplacer
{
    /**
     * Attempts to replace or remove the given component.
     *
     * <p>If this method returns the same component and contains children,
     * then this replacer will be applied recursively,
     * otherwise, the component is not replaced.
     *
     * <p>The returned component must be compatible with the source (a {@link ActionRow} or a {@link Container} for example) it originated from.
     *
     * @param  oldComponent
     *         The component which is attempted to be replaced
     *
     * @return A new, compatible component, the same component, or {@code null} to remove the component.
     */
    @Nullable
    Component apply(@Nonnull Component oldComponent);

    /**
     * Creates a new {@link ComponentReplacer} combining the provided replacers.
     *
     * <p>Each replacer will run one after the other,
     * if one returns a new components, the next replacer will still run against it.
     * <br>However, if a replacer returns {@code null}, thus removing the component, it will stop.
     *
     * @param  replacers
     *         The replacers to combine
     *
     * @throws IllegalArgumentException
     *         If {@code null} is passed
     *
     * @return A {@link ComponentReplacer} running all the provided replacers
     */
    @Nonnull
    static ComponentReplacer all(Collection<? extends ComponentReplacer> replacers)
    {
        Checks.notEmpty(replacers, "ComponentReplacers");
        Checks.noneNull(replacers, "ComponentReplacers");
        return oldComponent ->
        {
            final Iterator<? extends ComponentReplacer> iterator = replacers.iterator();
            Component newComponent = oldComponent;
            do
            {
                newComponent = iterator.next().apply(newComponent);
            } while (iterator.hasNext() && newComponent != null);
            return newComponent;
        };
    }

    /**
     * Creates a new {@link ComponentReplacer} combining the provided replacers.
     *
     * <p>Each replacer will run one after the other,
     * if one returns a new components, the next replacer will still run against it.
     * <br>However, if a replacer returns {@code null}, thus removing the component, it will stop.
     *
     * @param  first
     *         The first replacer
     * @param  others
     *         Additional replacers
     *
     * @throws IllegalArgumentException
     *         If {@code null} is passed
     *
     * @return A {@link ComponentReplacer} running all the provided replacers
     */
    @Nonnull
    static ComponentReplacer all(ComponentReplacer first, ComponentReplacer... others)
    {
        Checks.notNull(first, "ComponentReplacer");
        Checks.noneNull(others, "ComponentReplacers");
        return all(Helpers.mergeVararg(first, others));
    }

    /**
     * Creates a {@link ComponentReplacer} which recursively iterates on components of the given type,
     * while running the {@code update} function that satisfy the provided filter.
     *
     * <p>The provided {@code update} function can return {@code null} to remove the component.
     *
     * @param  type
     *         The type of components which should be attempted to be replaced
     * @param  filter
     *         The filter to match against
     * @param  update
     *         The replacement function, can return {@code null}
     *
     * @return A {@link ComponentReplacer} with the provided functions
     */
    @Nonnull
    static <T extends Component> ComponentReplacer of(@Nonnull Class<? super T> type, @Nonnull Predicate<? super T> filter, @Nonnull Function<? super T, Component> update)
    {
        Checks.notNull(type, "Component type");
        Checks.notNull(filter, "Component filter");
        Checks.notNull(update, "Component updater");
        return new TypedComponentReplacerImpl<T>(type, filter, update);
    }

    /**
     * Creates a {@link ComponentReplacer} which replaces a given component with another,
     * based on their {@linkplain Component#getUniqueId() numeric ID}.
     *
     * @param  oldComponent
     *         The component to replace
     * @param  newComponent
     *         The component to replace with
     *
     * @return A {@link ComponentReplacer} replacing the old component with the new one
     */
    @Nonnull
    static ComponentReplacer byId(@Nonnull Component oldComponent, @Nullable Component newComponent)
    {
        Checks.notNull(oldComponent, "Old component");
        return byId(oldComponent.getUniqueId(), newComponent);
    }

    /**
     * Creates a {@link ComponentReplacer} which replaces a given component with another,
     * based on their {@linkplain Component#getUniqueId() numeric ID}.
     *
     * @param  id
     *         The ID of the component to replace
     * @param  newComponent
     *         The component to replace with
     *
     * @return A {@link ComponentReplacer} replacing the old component with the new one
     */
    @Nonnull
    static ComponentReplacer byId(int id, @Nullable Component newComponent)
    {
        Checks.notNull(newComponent, "New component");
        return of(Component.class,
                component -> component.getUniqueId() == id,
                component -> newComponent);
    }
}
