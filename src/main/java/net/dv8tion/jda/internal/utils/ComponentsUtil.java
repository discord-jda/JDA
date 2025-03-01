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
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nonnull;
import java.util.Collection;

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

    // These methods are for compatibility purposes only, can remove after removing the remaining components V1 stuff
    @Nonnull
    public static ActionRow ensureIsActionRow(@Nonnull LayoutComponent<?> component)
    {
        Checks.check(component instanceof ActionRow, "This deprecated compatibility method does not accept anything other than ActionRow, please use the replacement");
        return (ActionRow) component;
    }

    @Nonnull
    public static ActionRow[] ensureIsActionRow(@Nonnull LayoutComponent<?>... components)
    {
        for (LayoutComponent<?> component : components)
            ensureIsActionRow(component);
        return (ActionRow[]) components;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static Collection<ActionRow> ensureIsActionRow(@Nonnull Collection<? extends LayoutComponent<?>> components)
    {
        for (LayoutComponent<?> component : components)
            ensureIsActionRow(component);
        return (Collection<ActionRow>) components;
    }
}
