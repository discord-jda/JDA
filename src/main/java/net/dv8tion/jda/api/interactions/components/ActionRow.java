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

package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * One row of components.
 *
 * @see ItemComponent
 * @see LayoutComponent
 */
public class ActionRow implements LayoutComponent
{
    private final List<ItemComponent> components = new ArrayList<>();

    private ActionRow() {}

    /**
     * Load ActionRow from serialized representation.
     * <br>Inverse of {@link #toData()}.
     *
     * @param  data
     *         Serialized version of an action row
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided data is not a valid action row
     * @throws IllegalArgumentException
     *         If the data is null or the type is not 1
     *
     * @return ActionRow instance
     */
    @Nonnull
    public static ActionRow fromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "Data");
        ActionRow row = new ActionRow();
        if (data.getInt("type", 0) != 1)
            throw new IllegalArgumentException("Data has incorrect type. Expected: 1 Found: " + data.getInt("type"));
        data.getArray("components")
            .stream(DataArray::getObject)
            .map(obj -> {
                switch (Component.Type.fromKey(obj.getInt("type")))
                {
                case BUTTON:
                    return new ButtonImpl(obj);
                case SELECT_MENU:
                    return new SelectMenuImpl(obj);
                default:
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .forEach(row.components::add);
        return row;
    }

    /**
     * Create one row of {@link ItemComponent components}.
     * <br>You cannot currently mix different types of components and each type has its own maximum defined by {@link Component.Type#getMaxPerRow()}.
     *
     * @param  components
     *         The components for this action row
     *
     * @throws IllegalArgumentException
     *         If anything is null, empty, or an invalid number of components are provided
     *
     * @return The action row
     */
    @Nonnull
    public static ActionRow of(@Nonnull Collection<? extends ItemComponent> components)
    {
        Checks.noneNull(components, "Components");
        return of(components.toArray(new ItemComponent[0]));
    }

    /**
     * Create one row of {@link ItemComponent components}.
     * <br>You cannot currently mix different types of components and each type has its own maximum defined by {@link Component.Type#getMaxPerRow()}.
     *
     * @param  components
     *         The components for this action row
     *
     * @throws IllegalArgumentException
     *         If anything is null, empty, or an invalid number of components are provided
     *
     * @return The action row
     */
    @Nonnull
    public static ActionRow of(@Nonnull ItemComponent... components)
    {
        Checks.noneNull(components, "Components");
        Checks.check(components.length > 0, "Cannot have empty row!");
        ActionRow row = new ActionRow();
        Collections.addAll(row.components, components);
        if (!row.isValid())
        {
            Map<Component.Type, List<ItemComponent>> grouped = Arrays.stream(components).collect(Collectors.groupingBy(Component::getType));
            String provided = grouped.entrySet()
                .stream()
                .map(entry -> entry.getValue().size() + "/" + entry.getKey().getMaxPerRow() + " of " + entry.getKey())
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Cannot create action row with invalid component combinations. Provided: " + provided);
        }
        return row;
    }

    /**
     * Partitions the provided {@link ItemComponent components} into a list of ActionRow instances.
     * <br>This will split the provided components by {@link Type#getMaxPerRow()} and create homogeneously typed rows,
     * meaning they will not have mixed component types.
     *
     * <p><b>Example</b>
     * <pre>{@code
     * List<ItemComponent> components = Arrays.asList(
     *   Button.primary("id1", "Hello"),
     *   Button.secondary("id2", "World"),
     *   SelectMenu.create("menu:id").build()
     * );
     *
     * List<ActionRow> partitioned = ActionRow.partition(components);
     * // partitioned[0] = ActionRow(button, button)
     * // partitioned[1] = ActionRow(selectMenu)
     * }</pre>
     *
     * @param  components
     *         The components to partition
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link List} of {@link ActionRow}
     */
    @Nonnull
    public static List<ActionRow> partitionOf(@Nonnull Collection<? extends ItemComponent> components)
    {
        Checks.noneNull(components, "Components");

        List<ActionRow> rows = new ArrayList<>();
        // The current action row we are building
        List<ItemComponent> currentRow = null;
        // The component types contained in that row (for now it can't have mixed types)
        Component.Type type = null;

        for (ItemComponent current : components)
        {
            if (type != current.getType() || currentRow.size() == type.getMaxPerRow())
            {
                type = current.getType();
                ActionRow row = ActionRow.of(current);
                currentRow = row.components;
                rows.add(row);
            }
            else
            {
                currentRow.add(current);
            }
        }

        return rows;
    }

    /**
     * Partitions the provided {@link ItemComponent components} into a list of ActionRow instances.
     * <br>This will split the provided components by {@link Type#getMaxPerRow()} and create homogeneously typed rows,
     * meaning they will not have mixed component types.
     *
     * <p><b>Example</b>
     * <pre>{@code
     * List<ItemComponent> components = Arrays.asList(
     *   Button.primary("id1", "Hello"),
     *   Button.secondary("id2", "World"),
     *   SelectMenu.create("menu:id").build()
     * );
     *
     * List<ActionRow> partitioned = ActionRow.partition(components);
     * // partitioned[0] = ActionRow(button, button)
     * // partitioned[1] = ActionRow(selectMenu)
     * }</pre>
     *
     * @param  components
     *         The components to partition
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link List} of {@link ActionRow}
     */
    @Nonnull
    public static List<ActionRow> partitionOf(@Nonnull ItemComponent... components)
    {
        Checks.notNull(components, "Components");
        return partitionOf(Arrays.asList(components));
    }

    /**
     * Mutable list of components in this ActionRow.
     * <br>ActionRows should not be empty and are limited to 5 buttons.
     *
     * @return The list of components
     */
    @Nonnull
    @Override
    public List<ItemComponent> getComponents()
    {
        return components;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ActionRow withDisabled(boolean disabled)
    {
        return ActionRow.of(components.stream()
                .map(c -> {
                    if (c instanceof ActionComponent)
                        return ((ActionComponent) c).withDisabled(disabled);
                    return c;
                })
                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ActionRow asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ActionRow asEnabled()
    {
        return withDisabled(false);
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
        return DataObject.empty()
                .put("type", 1)
                .put("components", DataArray.fromCollection(components));
    }

    @Nonnull
    @Override
    public Iterator<ItemComponent> iterator()
    {
        return components.iterator();
    }

    @Override
    public String toString()
    {
        return "ActionRow(" + components + ")";
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
        if (!(obj instanceof ActionRow))
            return false;
        return components.equals(((ActionRow) obj).components);
    }
}
