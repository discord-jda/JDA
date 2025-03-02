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

package net.dv8tion.jda.api.interactions.components.container;

import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.components.container.ContainerImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.List;

// TODO-components-v2 docs
public interface Container extends LayoutComponent<ContainerChildComponentUnion>, IdentifiableComponent, MessageTopLevelComponent
{
    // TODO-components-v2 docs
    @Nonnull
    static Container of(@Nonnull Collection<? extends ContainerChildComponent> children)
    {
        return ContainerImpl.of(children);
    }

    // TODO-components-v2 docs
    @Nonnull
    static Container of(@Nonnull ContainerChildComponent component, @Nonnull ContainerChildComponent... components)
    {
        return of(Helpers.mergeVararg(component, components));
    }

    // TODO-components-v2 docs
    @Nonnull
    static Container fromData(@Nonnull DataObject data)
    {
        return ContainerImpl.fromData(data);
    }

    // TODO-components-v2 docs
    @Nonnull
    @Override
    @CheckReturnValue
    Container withUniqueId(int uniqueId);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Container withAccentColor(int accentColor);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    default Container withAccentColor(@Nonnull Color accentColor)
    {
        Checks.notNull(accentColor, "Accent color");
        return withAccentColor(accentColor.getRGB());
    }

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Container withSpoiler(boolean spoiler);

    @Nonnull
    @Override
    Container asEnabled();

    @Nonnull
    @Override
    Container asDisabled();

    @Nonnull
    @Override
    Container withDisabled(boolean disabled);

    // TODO-components-v2 docs
    @Nonnull
    @Unmodifiable
    List<ContainerChildComponentUnion> getComponents();

    /**
     * The color of the stripe/border on the side of the container.
     * <br>If the color is 0 (no color), this will return null.
     *
     * @return Possibly-null Color.
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
     * @return The raw RGB color value or null.
     */
    @Nullable
    Integer getAccentColorRaw();

    // TODO-components-v2 docs
    boolean isSpoiler();
}
