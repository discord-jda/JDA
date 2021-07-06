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
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * One row of interactive message components.
 *
 * @see Component
 */
public class ActionRow implements ComponentLayout, Iterable<Component>
{
    private final List<Component> components = new ArrayList<>();

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
                case SELECTION_MENU:
                    return new SelectionMenuImpl(obj);
                default:
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .forEach(row.components::add);
        return row;
    }

    /**
     * Create one row of interactive message {@link Component components}.
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
    public static ActionRow of(@Nonnull Collection<? extends Component> components)
    {
        Checks.noneNull(components, "Components");
        return of(components.toArray(new Component[0]));
    }

    /**
     * Create one row of interactive message {@link Component components}.
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
    public static ActionRow of(@Nonnull Component... components)
    {
        Checks.noneNull(components, "Components");
        Checks.check(components.length > 0, "Cannot have empty row!");
        ActionRow row = new ActionRow();
        Collections.addAll(row.components, components);
        if (!row.isValid())
        {
            Map<Component.Type, List<Component>> grouped = Arrays.stream(components).collect(Collectors.groupingBy(Component::getType));
            String provided = grouped.entrySet()
                .stream()
                .map(entry -> entry.getValue().size() + "/" + entry.getKey().getMaxPerRow() + " of " + entry.getKey())
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Cannot create action row with invalid component combinations. Provided: " + provided);
        }
        return row;
    }

    /**
     * Mutable list of components in this ActionRow.
     * <br>ActionRows should not be empty and are limited to 5 buttons.
     *
     * @return The list of components
     */
    @Nonnull
    @Override
    public List<Component> getComponents()
    {
        return components;
    }

    /**
     * Immutable list of buttons in this ActionRow.
     *
     * @return Immutable list of buttons
     */
    @Nonnull
    @Override
    public List<Button> getButtons()
    {
        return Collections.unmodifiableList(
            getComponents().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.ACTION_ROW;
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
    public Iterator<Component> iterator()
    {
        return components.iterator();
    }
}
