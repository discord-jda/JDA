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
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.replacer.IReplaceable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ComponentsUtil
{
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
}
