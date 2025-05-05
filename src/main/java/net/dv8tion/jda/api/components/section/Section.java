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
import net.dv8tion.jda.api.components.ComponentUnion;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.api.components.utils.ComponentIterator;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.components.section.SectionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Component which contains the main content on the left and an accessory on the right.
 *
 * <p>This can contain up to {@value #MAX_COMPONENTS} {@link SectionContentComponent}.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 *
 * @see SectionContentComponent
 * @see SectionContentComponentUnion
 * @see SectionAccessoryComponent
 * @see SectionAccessoryComponentUnion
 */
public interface Section extends MessageTopLevelComponent, ContainerChildComponent, IReplaceable, IDisableable
{
    /**
     * How many {@link SectionContentComponent} can be in this section. ({@value})
     */
    int MAX_COMPONENTS = 3;

    /**
     * Constructs a new {@link Section} from the given accessory and components.
     *
     * @param  accessory
     *         The accessory of this section
     * @param  components
     *         The components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value #MAX_COMPONENTS} components are provided</li>
     *             <li>If one of the components is {@linkplain ComponentUnion#isUnknownComponent() unknown}</li>
     *         </ul>
     *
     * @return The new {@link Section}
     */
    @Nonnull
    static Section of(@Nonnull SectionAccessoryComponent accessory, @Nonnull Collection<? extends SectionContentComponent> components)
    {
        return SectionImpl.of(accessory, components);
    }

    /**
     * Constructs a new {@link Section} from the given accessory and components.
     *
     * @param  accessory
     *         The accessory of this section
     * @param  component
     *         The component to add
     * @param  components
     *         Additional components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value #MAX_COMPONENTS} components are provided</li>
     *             <li>If one of the components is {@linkplain ComponentUnion#isUnknownComponent() unknown}</li>
     *         </ul>
     *
     * @return The new {@link Section}
     */
    @Nonnull
    static Section of(@Nonnull SectionAccessoryComponent accessory, @Nonnull SectionContentComponent component, @Nonnull SectionContentComponent... components)
    {
        Checks.notNull(component, "Component");
        Checks.noneNull(components, "Components");
        return of(accessory, Helpers.mergeVararg(component, components));
    }

    /**
     * Constructs a new {@link Section} from the given {@link DataObject}.
     *
     * @param  data
     *         The data to construct the section from
     *
     * @throws IllegalArgumentException
     *         If the data does not represent a {@link Section}
     *
     * @return The new {@link Section}
     */
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
        return (Section) IDisableable.super.asDisabled();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default Section asEnabled()
    {
        return (Section) IDisableable.super.asEnabled();
    }

    /**
     * Returns an immutable list with the components contained by this section.
     *
     * @return {@link List} of {@link SectionContentComponentUnion} in this section
     */
    @Nonnull
    @Unmodifiable
    List<SectionContentComponentUnion> getContentComponents();

    /**
     * The accessory of this section.
     *
     * @return Accessory of this section
     */
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

    @Override
    default boolean isEnabled()
    {
        final SectionAccessoryComponentUnion accessory = getAccessory();
        if (accessory instanceof IDisableable && ((IDisableable) accessory).isDisabled())
            return false;

        return ComponentIterator.createStream(getContentComponents())
                .filter(IDisableable.class::isInstance)
                .map(IDisableable.class::cast)
                .allMatch(IDisableable::isEnabled);
    }
}
