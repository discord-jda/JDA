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

package net.dv8tion.jda.internal.components.section;

import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.section.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class SectionImpl
        extends AbstractComponentImpl
        implements Section, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final int uniqueId;
    private final List<SectionContentComponentUnion> components;
    private final SectionAccessoryComponentUnion accessory;

    public SectionImpl(Collection<SectionContentComponentUnion> components, SectionAccessoryComponentUnion accessory)
    {
        this(-1, components, accessory);
    }

    private SectionImpl(int uniqueId, Collection<SectionContentComponentUnion> components, SectionAccessoryComponentUnion accessory)
    {
        this.uniqueId = uniqueId;
        this.components = Helpers.copyAsUnmodifiableList(components);
        this.accessory = accessory;
    }

    public static Section of(SectionAccessoryComponent _accessory, Collection<? extends SectionContentComponent> _components)
    {
        Checks.notNull(_accessory, "Accessory");
        Checks.notEmpty(_components, "Components");
        Checks.noneNull(_components, "Components");
        Checks.check(_components.size() <= MAX_COMPONENTS, "A section can only contain %d components, provided: %d", MAX_COMPONENTS, _components.size());

        final Collection<SectionContentComponentUnion> components = ComponentsUtil.membersToUnion(_components, SectionContentComponentUnion.class);
        final SectionAccessoryComponentUnion accessory = ComponentsUtil.safeUnionCast("accessory", _accessory, SectionAccessoryComponentUnion.class);

        return new SectionImpl(components, accessory);
    }

    public static SectionImpl fromData(DataObject data)
    {
        Checks.notNull(data, "Data");
        if (data.getInt("type", 0) != Type.SECTION.getKey())
            throw new IllegalArgumentException("Data has incorrect type. Expected: " + Type.SECTION.getKey() + " Found: " + data.getInt("type"));

        final int uniqueId = data.getInt("id");
        final List<SectionContentComponentUnion> components = SectionContentComponentUnion.fromData(data.getArray("components"));
        final SectionAccessoryComponentUnion accessory = SectionAccessoryComponentUnion.fromData(data.getObject("accessory"));

        return new SectionImpl(uniqueId, components, accessory);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.SECTION;
    }

    @Nonnull
    @Override
    public Section withUniqueId(int uniqueId)
    {
        Checks.positive(uniqueId, "Unique ID");
        return new SectionImpl(uniqueId, components, accessory);
    }

    @Nonnull
    @Override
    public Section replace(@Nonnull ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");

        final List<SectionContentComponentUnion> newContent = ComponentsUtil.doReplace(
                SectionContentComponent.class,
                getContentComponents(),
                replacer,
                Function.identity()
        );

        final SectionAccessoryComponentUnion newAccessory = accessory != null ? ComponentsUtil.doReplace(
                SectionAccessoryComponent.class,
                Collections.singletonList(accessory),
                replacer,
                newAccessories -> newAccessories.get(0)
        ) : null;

        return new SectionImpl(newContent, newAccessory);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    @Unmodifiable
    public List<SectionContentComponentUnion> getContentComponents()
    {
        return components;
    }

    @Nonnull
    @Override
    public SectionAccessoryComponentUnion getAccessory()
    {
        return accessory;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty();
        json.put("type", getType().getKey());
        json.put("components", DataArray.fromCollection(getContentComponents()));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (accessory != null)
            json.put("accessory", accessory);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof SectionImpl)) return false;
        SectionImpl that = (SectionImpl) o;
        return uniqueId == that.uniqueId && Objects.equals(components, that.components) && Objects.equals(accessory, that.accessory);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, components, accessory);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("components", components)
                .toString();
    }
}
