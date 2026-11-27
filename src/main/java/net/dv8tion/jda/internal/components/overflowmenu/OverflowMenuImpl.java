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

import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.components.overflowmenu.OverflowMenu;
import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuGroup;
import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuGroupChildComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponentUnion;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class OverflowMenuImpl extends AbstractComponentImpl
        implements OverflowMenu, ActionRowChildComponentUnion, SectionAccessoryComponentUnion {
    private final int uniqueId;
    private final List<OverflowMenuGroup> groups;

    public OverflowMenuImpl(ComponentDeserializer deserializer, DataObject object) {
        this(
                object.getInt("id", -1),
                object.getArray("groups").stream(DataArray::getObject)
                        .map(o -> new OverflowMenuGroupImpl(deserializer, o))
                        .collect(Collectors.toList()));
    }

    public OverflowMenuImpl(Collection<? extends OverflowMenuGroup> groups) {
        this(-1, groups);
    }

    private OverflowMenuImpl(int uniqueId, Collection<? extends OverflowMenuGroup> groups) {
        this.uniqueId = uniqueId;
        this.groups = Helpers.copyAsUnmodifiableList(groups);
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.OVERFLOW_MENU;
    }

    @Nonnull
    @Override
    public OverflowMenu replace(@Nonnull ComponentReplacer replacer) {
        List<OverflowMenuGroup> newGroups = new ArrayList<>(groups.size());
        for (OverflowMenuGroup group : groups) {
            newGroups.add(OverflowMenuGroup.of(
                    group.getLabel(),
                    ComponentsUtil.doReplace(
                            OverflowMenuGroupChildComponent.class,
                            group.getComponents(),
                            replacer,
                            Function.identity())));
        }

        return new OverflowMenuImpl(uniqueId, newGroups);
    }

    @Nonnull
    @Override
    public OverflowMenuImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new OverflowMenuImpl(uniqueId, groups);
    }

    @Nonnull
    @Override
    public OverflowMenuImpl withGroups(@Nonnull Collection<? extends OverflowMenuGroup> groups) {
        Checks.notEmpty(groups, "Groups");
        Checks.noneNull(groups, "Groups");
        Checks.checkMaxSize(groups, "Overflow menus can contain at most %d groups", MAX_GROUPS);
        return new OverflowMenuImpl(uniqueId, groups);
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Nonnull
    @Override
    public List<OverflowMenuGroup> getGroups() {
        return groups;
    }

    @Nonnull
    @Override
    public DataObject toData() {
        DataObject json =
                DataObject.empty().put("type", getType().getKey()).put("groups", DataArray.fromCollection(groups));
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
        if (!(o instanceof OverflowMenuImpl)) {
            return false;
        }
        OverflowMenuImpl that = (OverflowMenuImpl) o;
        return uniqueId == that.uniqueId && Objects.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, groups);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("groups", groups)
                .toString();
    }
}
