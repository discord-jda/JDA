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

package net.dv8tion.jda.api.components.actionrow;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.actionrow.ActionRowImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * One row of action components.
 *
 * @see ActionRowChildComponent
 */
public interface ActionRow extends LayoutComponent<ActionRowChildComponentUnion>, MessageTopLevelComponent, ModalTopLevelComponent, ContainerChildComponent, IReplaceable, IDisableable
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
        switch (type)
        {
        case BUTTON:
            return 5;
        case STRING_SELECT:
        case USER_SELECT:
        case ROLE_SELECT:
        case MENTIONABLE_SELECT:
        case CHANNEL_SELECT:
        case TEXT_INPUT:
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
     * Returns an unmodifiable list of the components contained in this action row.
     *
     * @return Unmodifiable {@link List} of {@link ActionRowChildComponentUnion} contained in this action row
     */
    @Nonnull
    @Unmodifiable
    List<ActionRowChildComponentUnion> getComponents();

    /**
     * Returns an immutable list of {@link ActionComponent ActionComponents} in this row.
     *
     * @return Immutable {@link List} copy of {@link ActionComponent ActionComponents} in this row
     */
    @Nonnull
    @Unmodifiable
    default List<ActionComponent> getActionComponents()
    {
        return getComponents().stream()
                .filter(ActionComponent.class::isInstance)
                .map(ActionComponent.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Returns an immutable list of {@link Button Buttons} in this row.
     *
     * @return Immutable {@link List} of {@link Button Buttons}
     */
    @Nonnull
    @Unmodifiable
    default List<Button> getButtons()
    {
        return getComponents().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    @Deprecated
    @ForRemoval
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
    @CheckReturnValue
    ActionRow replace(@Nonnull ComponentReplacer replacer);

    default boolean isDisabled()
    {
        return getActionComponents().stream().allMatch(ActionComponent::isDisabled);
    }

    default boolean isEnabled()
    {
        return getActionComponents().stream().noneMatch(ActionComponent::isDisabled);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default ActionRow withDisabled(boolean disabled)
    {
        return replace(ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)));
    }

    @Nonnull
    @Override
    default ActionRow asDisabled()
    {
        return (ActionRow) LayoutComponent.super.asDisabled();
    }

    @Nonnull
    @Override
    default ActionRow asEnabled()
    {
        return (ActionRow) LayoutComponent.super.asEnabled();
    }

    /**
     * Check whether this row has a valid configuration.
     *
     * <p>This primarily checks the number of components in a single row.
     *
     * @return True, if this action row is valid
     */
    boolean isValid();
}
