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

package net.dv8tion.jda.api.interactions.components.section;

import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.internal.interactions.components.section.SectionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

// TODO-components-v2 docs
public interface Section extends LayoutComponent<SectionContentComponentUnion>, IdentifiableComponent, MessageTopLevelComponent, ContainerChildComponent
{
    // TODO-components-v2 docs
    @Nonnull
    static Section of(@Nonnull Collection<? extends SectionContentComponent> components)
    {
        Checks.noneNull(components, "Components");
        return SectionImpl.of(components);
    }

    // TODO-components-v2 docs
    @Nonnull
    static Section of(@Nonnull SectionContentComponent component, @Nonnull SectionContentComponent... components)
    {
        Checks.notNull(component, "Components");
        Checks.noneNull(components, "Components");
        return of(Helpers.mergeVararg(component, components));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    Section withUniqueId(int uniqueId);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Section withAccessory(@Nullable SectionAccessoryComponent accessory);

    @Nonnull
    @Override
    Section withDisabled(boolean disabled);

    @Nonnull
    @Override
    Section asDisabled();

    @Nonnull
    @Override
    Section asEnabled();

    @Nonnull
    @Override
    Section createCopy();

    // TODO-components-v2 docs
    @Nonnull
    @Unmodifiable
    List<SectionContentComponentUnion> getComponents();

    // TODO-components-v2 docs
    @Nullable
    SectionAccessoryComponentUnion getAccessory();
}
