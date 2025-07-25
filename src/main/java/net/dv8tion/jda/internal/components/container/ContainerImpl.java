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

package net.dv8tion.jda.internal.components.container;

import net.dv8tion.jda.api.components.Components;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ContainerImpl
        extends AbstractComponentImpl
        implements Container, MessageTopLevelComponentUnion
{
    private final int uniqueId;
    private final List<ContainerChildComponentUnion> components;
    private final boolean spoiler;
    private final Integer accentColor;

    public ContainerImpl(DataObject data)
    {
        this(
                data.getInt("id"),
                // Allow unknown components in deserialization methods
                Components.parseComponents(ContainerChildComponentUnion.class, data.getArray("components")),
                data.getBoolean("spoiler", false),
                (Integer) data.opt("accent_color").orElse(null)
        );
    }

    private ContainerImpl(Collection<ContainerChildComponentUnion> components)
    {
        this(-1, components, false, null);
    }

    public ContainerImpl(int uniqueId, Collection<ContainerChildComponentUnion> components, boolean spoiler, Integer accentColor)
    {
        Checks.notEmpty(components, "Components");
        this.uniqueId = uniqueId;
        this.components = Helpers.copyAsUnmodifiableList(components);
        this.spoiler = spoiler;
        this.accentColor = accentColor;
    }

    public static Container of(Collection<? extends ContainerChildComponent> components)
    {
        Checks.noneNull(components, "Components");

        // Don't allow unknown components in user-called methods
        final Collection<ContainerChildComponentUnion> componentUnions = ComponentsUtil.membersToUnion(components, ContainerChildComponentUnion.class);
        return new ContainerImpl(componentUnions);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.CONTAINER;
    }

    @Nonnull
    @Override
    public ContainerImpl withUniqueId(int uniqueId)
    {
        Checks.positive(uniqueId, "Unique ID");
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Container withSpoiler(boolean spoiler)
    {
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Container withAccentColor(@Nullable Integer accentColor)
    {
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public Container replace(@Nonnull ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");

        return ComponentsUtil.doReplace(
                ContainerChildComponent.class,
                getComponents(),
                replacer,
                components -> new ContainerImpl(uniqueId, components, spoiler, accentColor)
        );
    }

    @Nonnull
    @Override
    public List<ContainerChildComponentUnion> getComponents()
    {
        return components;
    }

    @Nullable
    @Override
    public Integer getAccentColorRaw()
    {
        return accentColor;
    }

    @Override
    public boolean isSpoiler()
    {
        return spoiler;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("components", DataArray.fromCollection(getComponents()))
                .put("spoiler", spoiler);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (accentColor != null)
            json.put("accent_color", accentColor & 0xFFFFFF);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (!(o instanceof ContainerImpl)) return false;
        ContainerImpl that = (ContainerImpl) o;
        return uniqueId == that.uniqueId && spoiler == that.spoiler && Objects.equals(components, that.components) && Objects.equals(accentColor, that.accentColor);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, components, spoiler, accentColor);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("accentColor", accentColor)
                .addMetadata("spoiler", spoiler)
                .addMetadata("components", components)
                .toString();
    }
}
