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
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class SectionImpl extends AbstractComponentImpl
        implements Section, MessageTopLevelComponentUnion, ContainerChildComponentUnion {
    private final int uniqueId;
    private final List<SectionContentComponentUnion> components;
    private final SectionAccessoryComponentUnion accessory;

    public SectionImpl(ComponentDeserializer deserializer, DataObject data) {
        this(
                data.getInt("id", -1),
                deserializer
                        .deserializeAs(SectionContentComponentUnion.class, data.getArray("components"))
                        .collect(Collectors.toList()),
                deserializer.deserializeAs(SectionAccessoryComponentUnion.class, data.getObject("accessory")));
    }

    public SectionImpl(Collection<SectionContentComponentUnion> components, SectionAccessoryComponentUnion accessory) {
        this(-1, components, accessory);
    }

    public SectionImpl(
            int uniqueId,
            Collection<SectionContentComponentUnion> components,
            SectionAccessoryComponentUnion accessory) {
        Checks.notNull(accessory, "Accessory");
        this.uniqueId = uniqueId;
        this.components = Helpers.copyAsUnmodifiableList(components);
        this.accessory = accessory;
    }

    public static Section validated(
            SectionAccessoryComponent accessory, Collection<? extends SectionContentComponent> components) {
        return validated(accessory, components, -1);
    }

    public static Section validated(
            SectionAccessoryComponent accessory,
            Collection<? extends SectionContentComponent> components,
            int uniqueId) {
        Checks.notNull(accessory, "Accessory");
        Checks.noneNull(components, "Components");
        Checks.notEmpty(components, "Components");
        Checks.check(
                components.size() <= MAX_COMPONENTS,
                "A section can only contain %d components, provided: %d",
                MAX_COMPONENTS,
                components.size());

        // Don't allow unknown components in user-called methods
        Collection<SectionContentComponentUnion> componentUnions =
                ComponentsUtil.membersToUnion(components, SectionContentComponentUnion.class);
        SectionAccessoryComponentUnion accessoryUnion =
                ComponentsUtil.safeUnionCast("accessory", accessory, SectionAccessoryComponentUnion.class);

        return new SectionImpl(uniqueId, componentUnions, accessoryUnion);
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.SECTION;
    }

    @Nonnull
    @Override
    public SectionImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new SectionImpl(uniqueId, components, accessory);
    }

    @Nonnull
    @Override
    public Section withContentComponents(@Nonnull Collection<? extends SectionContentComponent> components) {
        Checks.noneNull(components, "Components");
        return new SectionImpl(
                uniqueId, ComponentsUtil.membersToUnion(components, SectionContentComponentUnion.class), accessory);
    }

    @Nonnull
    @Override
    public Section withAccessory(@Nonnull SectionAccessoryComponent accessory) {
        Checks.notNull(accessory, "Accessory");
        return new SectionImpl(
                uniqueId,
                components,
                ComponentsUtil.safeUnionCast("accessory", accessory, SectionAccessoryComponentUnion.class));
    }

    @Nonnull
    @Override
    public Section replace(@Nonnull ComponentReplacer replacer) {
        Checks.notNull(replacer, "ComponentReplacer");

        List<SectionContentComponentUnion> newContent = ComponentsUtil.doReplace(
                SectionContentComponent.class, getContentComponents(), replacer, Function.identity());

        SectionAccessoryComponentUnion newAccessory = ComponentsUtil.doReplace(
                SectionAccessoryComponent.class,
                Collections.singletonList(accessory),
                replacer,
                newAccessories -> newAccessories.isEmpty() ? null : newAccessories.get(0));

        return validated(newAccessory, newContent, uniqueId);
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Nonnull
    @Override
    @Unmodifiable
    public List<SectionContentComponentUnion> getContentComponents() {
        return components;
    }

    @Nonnull
    @Override
    public SectionAccessoryComponentUnion getAccessory() {
        return accessory;
    }

    @Nonnull
    @Override
    public DataObject toData() {
        DataObject json = DataObject.empty();
        json.put("type", getType().getKey());
        json.put("accessory", accessory);
        json.put("components", DataArray.fromCollection(getContentComponents()));
        if (uniqueId >= 0) {
            json.put("id", uniqueId);
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SectionImpl)) {
            return false;
        }
        SectionImpl that = (SectionImpl) o;
        return uniqueId == that.uniqueId
                && Objects.equals(components, that.components)
                && Objects.equals(accessory, that.accessory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, components, accessory);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("components", components)
                .toString();
    }
}
