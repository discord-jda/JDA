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

import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuActionItem;
import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuGroup;
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
    private final List<OverflowMenuActionItem> items;

    public OverflowMenuGroupImpl(ComponentDeserializer deserializer, DataObject object) {
        this(
                object.getString("label", null),
                object.getArray("components").stream(DataArray::getObject)
                        .map(o -> deserializer.deserializeAs(OverflowMenuActionItem.class, o))
                        .collect(Collectors.toList()));
    }

    public OverflowMenuGroupImpl(String label, Collection<? extends OverflowMenuActionItem> items) {
        this.label = label;
        this.items = Helpers.copyAsUnmodifiableList(items);
    }

    @Nonnull
    @Override
    public OverflowMenuGroup withLabel(@Nullable String label) {
        if (label != null) {
            Checks.notEmpty(label, "Label");
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        }
        return new OverflowMenuGroupImpl(label, items);
    }

    @Nonnull
    @Override
    public OverflowMenuGroup withItems(@Nonnull Collection<? extends OverflowMenuActionItem> items) {
        Checks.notEmpty(items, "Items");
        Checks.noneNull(items, "Items");
        Checks.checkMaxSize(items, "Overflow menu groups can contain at most %d items", MAX_ITEMS);
        return new OverflowMenuGroupImpl(label, items);
    }

    @Nullable
    @Override
    public String getLabel() {
        return label;
    }

    @Nonnull
    @Override
    public List<OverflowMenuActionItem> getItems() {
        return items;
    }

    @Nonnull
    @Override
    public DataObject toData() {
        final DataObject json = DataObject.empty().put("components", DataArray.fromCollection(items));
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
        return Objects.equals(label, that.label) && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, items);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("label", label)
                .addMetadata("items", items)
                .toString();
    }
}
