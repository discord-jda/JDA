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
import net.dv8tion.jda.api.components.button.Button;
import net.dv8tion.jda.api.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.components.selects.SelectMenu;
import net.dv8tion.jda.api.components.selects.StringSelectMenu;
import net.dv8tion.jda.internal.components.replacer.TypedComponentReplacerImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface ComponentReplacer
{
    @Nullable
    Component apply(@Nonnull Component oldComponent);

    // TODO-components-v2 - docs
    @Nonnull
    static ComponentReplacer all(ComponentReplacer first, ComponentReplacer... others)
    {
        return oldComponent ->
        {
            Component newComponent = first.apply(oldComponent);
            for (ComponentReplacer other : others)
            {
                if (newComponent == null)
                    return null;
                newComponent = other.apply(newComponent);
            }
            return newComponent;
        };
    }

    // TODO-components-v2 - docs
    //  update can return null
    @Nonnull
    static <T extends Component> ComponentReplacer of(@Nonnull Class<? super T> type, @Nonnull Predicate<? super T> filter, @Nonnull Function<? super T, Component> update)
    {
        return new TypedComponentReplacerImpl<T>(type, filter, update);
    }

    // TODO-components-v2 - docs
    @Nonnull
    static ComponentReplacer byId(@Nonnull Component oldComponent, @Nullable Component newComponent)
    {
        return byId(oldComponent.getUniqueId(), newComponent);
    }

    // TODO-components-v2 - docs
    @Nonnull
    static ComponentReplacer byId(int id, @Nullable Component newComponent)
    {
        return of(Component.class,
                component -> component.getUniqueId() == id,
                component -> newComponent);
    }

    // TODO-components-v2 - docs
    //  update can return null
    @Nonnull
    static ComponentReplacer button(@Nonnull Predicate<Button> filter, @Nonnull Function<Button, Button> update)
    {
        return new TypedComponentReplacerImpl<>(Button.class, filter, update);
    }

    // TODO-components-v2 - docs
    //  update can return null
    @Nonnull
    static ComponentReplacer selectMenu(@Nonnull Predicate<SelectMenu> filter, @Nonnull Function<SelectMenu, SelectMenu> update)
    {
        return new TypedComponentReplacerImpl<>(SelectMenu.class, filter, update);
    }

    // TODO-components-v2 - docs
    //  update can return null
    @Nonnull
    static ComponentReplacer stringSelectMenu(@Nonnull Predicate<StringSelectMenu> filter, @Nonnull Function<StringSelectMenu, StringSelectMenu> update)
    {
        return new TypedComponentReplacerImpl<>(StringSelectMenu.class, filter, update);
    }

    // TODO-components-v2 - docs
    //  update can return null
    @Nonnull
    static ComponentReplacer entitySelectMenu(@Nonnull Predicate<EntitySelectMenu> filter, @Nonnull Function<EntitySelectMenu, EntitySelectMenu> update)
    {
        return new TypedComponentReplacerImpl<>(EntitySelectMenu.class, filter, update);
    }
}
