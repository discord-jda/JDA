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

package net.dv8tion.jda.internal.components.actionrow;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.utils.ComponentDeserializer;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActionRowImpl
        extends AbstractComponentImpl
        implements ActionRow, MessageTopLevelComponentUnion, ContainerChildComponentUnion
{
    private final int uniqueId;
    private final List<ActionRowChildComponentUnion> components;

    public ActionRowImpl(ComponentDeserializer deserializer, DataObject data)
    {
        this(
            deserializer.deserializeAs(ActionRowChildComponentUnion.class, data.getArray("components")).collect(Collectors.toList()),
            data.getInt("id", -1)
        );
    }

    public ActionRowImpl(Collection<ActionRowChildComponentUnion> components, int uniqueId)
    {
        this.uniqueId = uniqueId;
        this.components = Helpers.copyAsUnmodifiableList(components);
    }

    @Nonnull
    public static ActionRow validated(@Nonnull Collection<? extends ActionRowChildComponent> components)
    {
        return validated(components, -1);
    }

    @Nonnull
    public static ActionRow validated(@Nonnull Collection<? extends ActionRowChildComponent> components, int uniqueId)
    {
        Checks.notEmpty(components, "Row");
        Checks.noneNull(components, "Components");
        checkIsValid(components);

        // Don't allow unknown components in user-called methods
        Collection<ActionRowChildComponentUnion> componentUnions = ComponentsUtil.membersToUnion(components, ActionRowChildComponentUnion.class);
        return new ActionRowImpl(componentUnions, uniqueId);
    }

    @Nonnull
    public static List<ActionRow> partitionOf(@Nonnull Collection<? extends ActionRowChildComponent> components)
    {
        Checks.noneNull(components, "Components");
        Checks.notEmpty(components, "Components");
        // Don't allow unknown components in user-called methods
        Collection<ActionRowChildComponentUnion> componentUnions = ComponentsUtil.membersToUnion(components, ActionRowChildComponentUnion.class);

        List<ActionRow> rows = new ArrayList<>();
        // The current action row we are building
        List<ActionRowChildComponentUnion> currentRow = new ArrayList<>();
        // The component types contained in that row (for now it can't have mixed types)
        Component.Type type = null;

        for (ActionRowChildComponentUnion current : componentUnions)
        {
            if ((type != null && type != current.getType()) || currentRow.size() == ActionRow.getMaxAllowed(current.getType()))
            {
                rows.add(ActionRow.of(currentRow));
                currentRow.clear();
            }

            type = current.getType();
            currentRow.add(current);
        }

        rows.add(ActionRow.of(currentRow));

        return rows;
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public List<ActionRowChildComponentUnion> getComponents()
    {
        return components;
    }

    @Nonnull
    @Override
    public ActionRow replace(@Nonnull ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");

        return ComponentsUtil.doReplace(
                ActionRowChildComponent.class,
                components,
                replacer,
                newComponents -> validated(newComponents, uniqueId)
        );
    }

    @Nonnull
    @Override
    public ActionRowImpl withUniqueId(int uniqueId)
    {
        Checks.positive(uniqueId, "Unique ID");
        return new ActionRowImpl(components, uniqueId);
    }

    @Nonnull
    @Override
    public ActionRow withComponents(@Nonnull Collection<? extends ActionRowChildComponent> components)
    {
        Checks.noneNull(components, "Components");
        return new ActionRowImpl(ComponentsUtil.membersToUnion(components, ActionRowChildComponentUnion.class), uniqueId);
    }

    @Nonnull
    @Override
    public Component.Type getType()
    {
        return Component.Type.ACTION_ROW;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", 1)
                .put("components", DataArray.fromCollection(components));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }

    private static void checkIsValid(Collection<? extends ActionRowChildComponent> components)
    {
        Map<Component.Type, List<ActionRowChildComponent>> groups = components.stream().collect(Collectors.groupingBy(Component::getType));
        // TODO: You can't mix components right now but maybe in the future, we need to check back on this when that happens
        if (groups.size() > 1)
            throw new IllegalArgumentException("Cannot create action row containing different component types! Provided: " + groups.keySet());

        for (Map.Entry<Component.Type, List<ActionRowChildComponent>> entry : groups.entrySet())
        {
            Component.Type type = entry.getKey();
            List<ActionRowChildComponent> list = entry.getValue();
            final int maxAllowed = ActionRow.getMaxAllowed(type);
            Checks.check(list.size() <= maxAllowed, "Cannot create an action row with more than %d %s! Provided: %d", maxAllowed, type.name(), list.size());
        }
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("components", components)
                .toString();
    }

    @Override
    public int hashCode()
    {
        return components.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof ActionRowImpl))
            return false;

        return components.equals(((ActionRowImpl) obj).components);
    }
}
