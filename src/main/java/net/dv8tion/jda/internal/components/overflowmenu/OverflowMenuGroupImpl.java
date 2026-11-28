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

package net.dv8tion.jda.internal.components.overflowmenu;

import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuGroup;
import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuGroupChildComponent;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OverflowMenuGroupImpl implements OverflowMenuGroup, SerializableData {
    private final String label;
    private final List<OverflowMenuGroupChildComponent> components;

    public OverflowMenuGroupImpl(ComponentDeserializer deserializer, DataObject object) {
        this(
                object.getString("label", null),
                object.getArray("components").stream(DataArray::getObject)
                        .map(o -> deserializer.deserializeAs(OverflowMenuGroupChildComponent.class, o))
                        .collect(Collectors.toList()));
    }

    public OverflowMenuGroupImpl(String label, Collection<? extends OverflowMenuGroupChildComponent> components) {
        this.label = label;
        this.components = Helpers.copyAsUnmodifiableList(components);
    }

    @Nonnull
    @Override
    public OverflowMenuGroup withLabel(@Nullable String label) {
        if (label != null) {
            Checks.notBlank(label, "Label");
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        }
        return new OverflowMenuGroupImpl(label, components);
    }

    @Nonnull
    @Override
    public OverflowMenuGroup withComponents(@Nonnull Collection<? extends OverflowMenuGroupChildComponent> components) {
        Checks.notEmpty(components, "Components");
        Checks.noneNull(components, "Components");
        Checks.checkMaxSize(components, "Overflow menu groups can contain at most %d components", MAX_COMPONENTS);
        return new OverflowMenuGroupImpl(label, components);
    }

    @Nullable
    @Override
    public String getLabel() {
        return label;
    }

    @Nonnull
    @Override
    public List<OverflowMenuGroupChildComponent> getComponents() {
        return components;
    }

    @Nonnull
    @Override
    public DataObject toData() {
        final DataObject json = DataObject.empty().put("components", DataArray.fromCollection(components));
        if (label != null) {
            json.put("label", label);
        }

        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OverflowMenuGroupImpl)) {
            return false;
        }
        OverflowMenuGroupImpl that = (OverflowMenuGroupImpl) o;
        return Objects.equals(label, that.label) && Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, components);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("label", label)
                .addMetadata("components", components)
                .toString();
    }
}
