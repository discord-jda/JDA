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

package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;
import net.dv8tion.jda.api.interactions.components.UnknownComponent;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.replacer.IReplaceable;
import net.dv8tion.jda.api.interactions.components.utils.ComponentIterator;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComponentsUtil
{
    /**
     * Checks the component has the target union type, and isn't an {@link UnknownComponent},
     * throws {@link IllegalArgumentException} otherwise.
     */
    public static <T extends ComponentUnion> T safeUnionCast(String componentCategory, Component component, Class<T> toUnionClass)
    {
        if (toUnionClass.isInstance(component))
        {
            final T union = toUnionClass.cast(component);
            Checks.check(union.isUnknownComponent(), "Cannot provide UnknownComponent");
            return union;
        }

        String cleanedClassName = component.getClass().getSimpleName().replace("Impl", "");
        throw new IllegalArgumentException(Helpers.format("Cannot convert " + componentCategory + " of type %s to %s!", cleanedClassName, toUnionClass.getSimpleName()));
    }

    /**
     * Checks all the components has the target union type, and isn't an {@link UnknownComponent},
     * throws {@link IllegalArgumentException} otherwise.
     */
    public static <TMember extends Component, TUnion extends ComponentUnion> List<TUnion> membersToUnion(Collection<? extends TMember> members, Class<TUnion> clazz) {
        return members
                .stream()
                .map(c -> safeUnionCast("component", c, clazz))
                .collect(Collectors.toList());
    }

    /** Checks whether the provided component has the {@code identifier} as its custom id, url or SKU id */
    public static boolean isSameIdentifier(@Nonnull ActionComponent component, @Nonnull String identifier)
    {
        if (identifier.equals(component.getId()))
            return true;

        if (component instanceof Button)
        {
            final Button button = (Button) component;
            if (identifier.equals(button.getUrl()))
                return true;
            if (button.getSku() != null)
                return identifier.equals(button.getSku().getId());
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static <R, E extends ComponentUnion> R doReplace(
            // This isn't '? extends E' as users are not required to return unions
            Class<? extends Component> expectedChildrenType,
            Iterable<E> children,
            ComponentReplacer replacer,
            Function<List<E>, R> finisher
    )
    {
        List<E> newComponents = new ArrayList<>();
        for (E component : children)
        {
            Component newComponent = replacer.apply(component);
            // If it returned a different component, then use it and don't try to recurse
            if (newComponent != component)
            {
                Checks.checkComponentType(expectedChildrenType, component, newComponent);
            }
            else if (component instanceof IReplaceable)
            {
                newComponent = ((IReplaceable) component).replace(replacer);
                Checks.checkComponentType(expectedChildrenType, component, newComponent);
            }
            newComponents.add((E) newComponent);
        }

        return finisher.apply(newComponents);
    }

    public static long getComponentTreeSize(@Nonnull Collection<? extends Component> tree)
    {
        return ComponentIterator.createStream(tree).count();
    }

    @Nonnull
    public static List<? extends Component> getIllegalV1Components(@Nonnull Collection<? extends Component> components)
    {
        return components.stream().filter(c -> !(c instanceof ActionRow)).collect(Collectors.toList());
    }

    public static boolean hasIllegalV1Components(@Nonnull Collection<? extends Component> components)
    {
        return !getIllegalV1Components(components).isEmpty();
    }
}
