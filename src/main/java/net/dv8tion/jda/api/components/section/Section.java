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

package net.dv8tion.jda.api.components.section;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.api.components.utils.ComponentIterator;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.section.SectionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

// TODO-components-v2 docs
public interface Section extends MessageTopLevelComponent, ContainerChildComponent, IReplaceable, IDisableable
{
    // TODO-components-v2 docs
    int MAX_COMPONENTS = 3;

    // TODO-components-v2 docs
    @Nonnull
    static Section of(@Nonnull SectionAccessoryComponent accessory, @Nonnull Collection<? extends SectionContentComponent> components)
    {
        Checks.notNull(accessory, "Accessory");
        Checks.noneNull(components, "Components");
        Checks.notEmpty(components, "Components");
        return SectionImpl.of(accessory, components);
    }

    // TODO-components-v2 docs
    @Nonnull
    static Section of(@Nonnull SectionAccessoryComponent accessory, @Nonnull SectionContentComponent component, @Nonnull SectionContentComponent... components)
    {
        Checks.notNull(accessory, "Accessory");
        Checks.notNull(component, "Component");
        Checks.noneNull(components, "Components");
        return of(accessory, Helpers.mergeVararg(component, components));
    }

    // TODO-components-v2 docs
    @Nonnull
    static Section fromData(@Nonnull DataObject data)
    {
        return SectionImpl.fromData(data);
    }

    @Override
    default boolean isMessageCompatible()
    {
        if (!getType().isMessageCompatible())
            return false;

        return getContentComponents().stream().allMatch(Component::isMessageCompatible)
                && getAccessory().isMessageCompatible();
    }

    @Override
    default boolean isModalCompatible()
    {
        if (!getType().isModalCompatible())
            return false;

        return getContentComponents().stream().allMatch(Component::isModalCompatible)
                && getAccessory().isModalCompatible();
    }

    @Nonnull
    @Override
    Section replace(@Nonnull ComponentReplacer replacer);

    @Nonnull
    @Override
    @CheckReturnValue
    Section withUniqueId(int uniqueId);

    @Nonnull
    @Override
    @CheckReturnValue
    default Section withDisabled(boolean disabled)
    {
        return replace(ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default Section asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default Section asEnabled()
    {
        return withDisabled(false);
    }

    /**
     * Returns an immutable list with the components contained by this section.
     *
     * @return {@link List} of {@link SectionContentComponentUnion} in this section
     */
    @Nonnull
    @Unmodifiable
    List<SectionContentComponentUnion> getContentComponents();

    // TODO-components-v2 docs
    @Nonnull
    SectionAccessoryComponentUnion getAccessory();

    @Override
    default boolean isDisabled()
    {
        final SectionAccessoryComponentUnion accessory = getAccessory();
        if (accessory instanceof IDisableable && ((IDisableable) accessory).isEnabled())
            return false;

        return ComponentIterator.createStream(getContentComponents())
                .filter(IDisableable.class::isInstance)
                .map(IDisableable.class::cast)
                .allMatch(IDisableable::isDisabled);
    }
}
