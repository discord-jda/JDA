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

package net.dv8tion.jda.api.interactions.components.action_row;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.concrete.ActionRowImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * One row of action components.
 *
 * @see ActionRowChildComponent
 * @see LayoutComponent
 */
public interface ActionRow extends LayoutComponent<ActionRowChildComponentUnion>, IdentifiableComponent, MessageTopLevelComponent, ModalTopLevelComponent, ContainerChildComponent
{
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
    static ActionRow fromData(@Nonnull DataObject data)
    {
        return ActionRowImpl.fromData(data);
    }

    /**
     * Create one row of {@link ActionRowChildComponent components}.
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
    static ActionRow of(@Nonnull Collection<? extends ActionRowChildComponent> components)
    {
        return ActionRowImpl.of(components);
    }

    /**
     * Create one row of {@link ActionRowChildComponent components}.
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
    static ActionRow of(@Nonnull ActionRowChildComponent... components)
    {
        Checks.notNull(components, "Components");
        return of(Arrays.asList(components));
    }

    /**
     * Partitions the provided {@link ActionRowChildComponent components} into a list of ActionRow instances.
     * <br>This will split the provided components by {@link #getMaxAllowed(Type)} ()} and create homogeneously typed rows,
     * meaning they will not have mixed component types.
     *
     * <p><b>Example</b>
     * <pre>{@code
     * List<ActionRowChildComponent> components = Arrays.asList(
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
    static List<ActionRow> partitionOf(@Nonnull Collection<? extends ActionRowChildComponent> components)
    {
        return ActionRowImpl.partitionOf(components);
    }

    /**
     * Partitions the provided {@link ActionRowChildComponent components} into a list of ActionRow instances.
     * <br>This will split the provided components by {@link #getMaxAllowed(Type)} and create homogeneously typed rows,
     * meaning they will not have mixed component types.
     *
     * <p><b>Example</b>
     * <pre>{@code
     * List<ActionRowChildComponent> components = Arrays.asList(
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
    static List<ActionRow> partitionOf(@Nonnull ActionRowChildComponent... components)
    {
        Checks.notNull(components, "Components");
        return partitionOf(Arrays.asList(components));
    }

    /**
     * How many of components of the provided type can be added to a single {@link ActionRow}.
     *
     * @return The maximum amount an action row can contain
     */
    static int getMaxAllowed(Component.Type type)
    {
        switch (type) {
            case BUTTON:
                return 5;
            case STRING_SELECT:
            case USER_SELECT:
            case ROLE_SELECT:
            case MENTIONABLE_SELECT:
            case CHANNEL_SELECT:
                return 1;
            default:
                return 0;
        }
    }

    @Nonnull
    @Override
    @CheckReturnValue
    ActionRow withUniqueId(int uniqueId);

    /**
     * List representation of this component layout.
     * <br>This list is modifiable. Note that empty layouts are not supported.
     *
     * @return {@link List} of components in this layout
     */
    @Nonnull
    List<ActionRowChildComponentUnion> getComponents();

    /**
     * Check whether this layout is empty.
     * <br>Identical to {@code getComponents().isEmpty()}
     *
     * @return True, if this layout has no components
     */
    default boolean isEmpty()
    {
        return getComponents().isEmpty();
    }

    @Nonnull
    @Override
    ActionRow createCopy();

    @Override
    default boolean isMessageCompatible()
    {
        if (!getType().isMessageCompatible())
            return false;

        return getComponents().stream().allMatch(Component::isMessageCompatible);
    }

    @Override
    default boolean isModalCompatible()
    {
        if (!getType().isModalCompatible())
            return false;

        return getComponents().stream().allMatch(Component::isModalCompatible);
    }

    @Nonnull
    @Override
    ActionRow asDisabled();

    @Nonnull
    @Override
    ActionRow asEnabled();

    @Nonnull
    @Override
    ActionRow withDisabled(boolean disabled);

    @Override
    default boolean isValid()
    {
        if (isEmpty())
            return false;

        List<ActionRowChildComponentUnion> components = getComponents();
        Map<Component.Type, List<ActionRowChildComponentUnion>> groups = components.stream().collect(Collectors.groupingBy(Component::getType));
        if (groups.size() > 1) // TODO: You can't mix components right now but maybe in the future, we need to check back on this when that happens
            return false;

        for (Map.Entry<Component.Type, List<ActionRowChildComponentUnion>> entry : groups.entrySet())
        {
            Component.Type type = entry.getKey();
            List<ActionRowChildComponentUnion> list = entry.getValue();
            if (list.size() > getMaxAllowed(type))
                return false;
        }

        return true;
    }
}
