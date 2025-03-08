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

package net.dv8tion.jda.api.components.container;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ComponentUnion;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.api.components.utils.ComponentIterator;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.components.container.ContainerImpl;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * Component which groups components vertically, you can specify an accent color, similar to embeds,
 * and mark the container as a spoiler.
 *
 * <p>This can contain up to {@value #MAX_COMPONENTS} {@link ContainerChildComponent}.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 *
 * @see ContainerChildComponent
 * @see ContainerChildComponentUnion
 */
public interface Container extends MessageTopLevelComponent, IReplaceable, IDisableable
{
    /**
     * How many {@link ContainerChildComponent} can be in this container.
     */
    int MAX_COMPONENTS = 10;

    /**
     * Constructs a new {@link Container} from the given components.
     *
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
     * @return The new {@link Container}
     */
    @Nonnull
    static Container of(@Nonnull Collection<? extends ContainerChildComponent> components)
    {
        return ContainerImpl.of(components);
    }

    /**
     * Constructs a new {@link Container} from the given components.
     *
     * @param  component
     *         The first component
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
     * @return The new {@link Container}
     */
    @Nonnull
    static Container of(@Nonnull ContainerChildComponent component, @Nonnull ContainerChildComponent... components)
    {
        return of(Helpers.mergeVararg(component, components));
    }

    /**
     * Constructs a new {@link Container} from the given {@link DataObject}.
     *
     * @param  data
     *         The data to construct the {@link Container} from
     *
     * @throws IllegalArgumentException
     *         If the data does not represent a {@link Container}
     *
     * @return The new {@link Container}
     */
    @Nonnull
    static Container fromData(@Nonnull DataObject data)
    {
        return ContainerImpl.fromData(data);
    }

    @Override
    default boolean isMessageCompatible()
    {
        if (!getType().isMessageCompatible())
            return false;

        return getComponents().stream().allMatch(Component::isMessageCompatible);
    }

    @Override
    default boolean isModalCompatible()
    {
        if (!getType().isModalCompatible())
            return false;

        return getComponents().stream().allMatch(Component::isModalCompatible);
    }

    @Nonnull
    @Override
    Container replace(@Nonnull ComponentReplacer replacer);

    @Nonnull
    @Override
    @CheckReturnValue
    Container withUniqueId(int uniqueId);

    /**
     * Creates a new {@link Container} with the specified accent color, which appears on the side.
     *
     * @param  accentColor
     *         The new accent color, or {@code null} to remove it
     *
     * @return The new {@link Container}
     */
    @Nonnull
    @CheckReturnValue
    Container withAccentColor(@Nullable Integer accentColor);

    /**
     * Creates a new {@link Container} with the specified accent color, which appears on the side.
     * <br>Note that the {@link Color#getAlpha() alpha component} will be removed, making the color opaque.
     *
     * @param  accentColor
     *         The new accent color, or {@code null} to remove it
     *
     * @return The new {@link Container}
     */
    @Nonnull
    @CheckReturnValue
    default Container withAccentColor(@Nullable Color accentColor)
    {
        return withAccentColor(accentColor == null ? null : accentColor.getRGB());
    }

    /**
     * Creates a new {@link Container} with the specified spoiler status.
     * <br>Spoilers are hidden until the user clicks on it.
     *
     * @param  spoiler
     *         The new spoiler status
     *
     * @return The new {@link Container}
     */
    @Nonnull
    @CheckReturnValue
    Container withSpoiler(boolean spoiler);

    @Nonnull
    @Override
    @CheckReturnValue
    default Container withDisabled(boolean disabled)
    {
        return replace(ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default Container asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default Container asEnabled()
    {
        return withDisabled(false);
    }

    /**
     * Returns an immutable list with the components contained by this container.
     *
     * @return {@link List} of {@link ContainerChildComponentUnion} in this container
     */
    @Nonnull
    @Unmodifiable
    List<ContainerChildComponentUnion> getComponents();

    @Override
    default boolean isDisabled()
    {
        return ComponentIterator.createStream(getComponents())
                .filter(IDisableable.class::isInstance)
                .map(IDisableable.class::cast)
                .allMatch(IDisableable::isDisabled);
    }

    /**
     * The color of the stripe/border on the side of the container.
     * <br>If no accent color has been set, this will return null.
     *
     * @return Possibly-null {@link Color}.
     */
    @Nullable
    default Color getAccentColor()
    {
        return getAccentColorRaw() != null ? new Color(getAccentColorRaw()) : null;
    }

    /**
     * The raw RGB color value for the stripe/border on the side of the container.
     * <br>If no accent color has been set, this will return null.
     *
     * @return The raw RGB color value or {@code null}.
     */
    @Nullable
    Integer getAccentColorRaw();

    /**
     * Whether this container is hidden until the user clicks on it.
     *
     * @return {@code true} if this is hidden by default, {@code false} otherwise
     */
    boolean isSpoiler();
}
