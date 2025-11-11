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

import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.internal.components.actionrow.ActionRowImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * One row of action components.
 *
 * @see ActionRowChildComponent
 */
public interface ActionRow
        extends MessageTopLevelComponent, ContainerChildComponent, IReplaceable, IDisableable {
    /**
     * Create one row of {@link ActionRowChildComponent components}.
     * <br>You cannot currently mix different types of components and each type has its own maximum defined by {@link #getMaxAllowed(Type)}.
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
    static ActionRow of(@Nonnull Collection<? extends ActionRowChildComponent> components) {
        return ActionRowImpl.validated(components);
    }

    /**
     * Create one row of {@link ActionRowChildComponent components}.
     * <br>You cannot currently mix different types of components and each type has its own maximum defined by {@link #getMaxAllowed(Type)}.
     *
     * @param  component
     *         The first component for this action row
     * @param  components
     *         Additional components for this action row
     *
     * @throws IllegalArgumentException
     *         If anything is null, empty, or an invalid number of components are provided
     *
     * @return The action row
     */
    @Nonnull
    static ActionRow of(
            @Nonnull ActionRowChildComponent component,
            @Nonnull ActionRowChildComponent... components) {
        Checks.notNull(component, "Component");
        Checks.notNull(components, "Components");
        return of(Helpers.mergeVararg(component, components));
    }

    /**
     * Partitions the provided {@link ActionRowChildComponent components} into a list of ActionRow instances.
     * <br>This will split the provided components by {@link #getMaxAllowed(Type)} and create homogeneously typed rows,
     * meaning they will not have mixed component types.
     *
     * <p><b>Example</b>
     * {@snippet lang="java":
     * List<ActionRowChildComponent> components = Arrays.asList(
     *   Button.primary("id1", "Hello"),
     *   Button.secondary("id2", "World"),
     *   SelectMenu.create("menu:id").build()
     * );
     *
     * List<ActionRow> partitioned = ActionRow.partition(components);
     * // partitioned[0] = ActionRow(button, button)
     * // partitioned[1] = ActionRow(selectMenu)
     * }
     *
     * @param  components
     *         The components to partition
     *
     * @throws IllegalArgumentException
     *         If null is provided or there is no components
     *
     * @return {@link List} of {@link ActionRow}
     */
    @Nonnull
    static List<ActionRow> partitionOf(
            @Nonnull Collection<? extends ActionRowChildComponent> components) {
        return ActionRowImpl.partitionOf(components);
    }

    /**
     * Partitions the provided {@link ActionRowChildComponent components} into a list of ActionRow instances.
     * <br>This will split the provided components by {@link #getMaxAllowed(Type)} and create homogeneously typed rows,
     * meaning they will not have mixed component types.
     *
     * <p><b>Example</b>
     * {@snippet lang="java":
     * List<ActionRowChildComponent> components = Arrays.asList(
     *   Button.primary("id1", "Hello"),
     *   Button.secondary("id2", "World"),
     *   SelectMenu.create("menu:id").build()
     * );
     *
     * List<ActionRow> partitioned = ActionRow.partition(components);
     * // partitioned[0] = ActionRow(button, button)
     * // partitioned[1] = ActionRow(selectMenu)
     * }
     *
     * @param  component
     *         The first component to partition
     * @param  components
     *         Additional components to partition
     *
     * @throws IllegalArgumentException
     *         If null is provided or there is no components
     *
     * @return {@link List} of {@link ActionRow}
     */
    @Nonnull
    static List<ActionRow> partitionOf(
            @Nonnull ActionRowChildComponent component,
            @Nonnull ActionRowChildComponent... components) {
        Checks.notNull(component, "Component");
        Checks.notNull(components, "Components");
        return partitionOf(Helpers.mergeVararg(component, components));
    }

    /**
     * How many of components of the provided type can be added to a single {@link ActionRow}.
     *
     * @return The maximum amount an action row can contain
     */
    static int getMaxAllowed(@Nonnull Component.Type type) {
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
    default List<ActionComponent> getActionComponents() {
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
    default List<Button> getButtons() {
        return getComponents().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    @Override
    default boolean isMessageCompatible() {
        if (!getType().isMessageCompatible()) {
            return false;
        }

        return getComponents().stream().allMatch(Component::isMessageCompatible);
    }

    @Override
    default boolean isModalCompatible() {
        if (!getType().isModalCompatible()) {
            return false;
        }

        return getComponents().stream().allMatch(Component::isModalCompatible);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    ActionRow replace(@Nonnull ComponentReplacer replacer);

    @Override
    default boolean isDisabled() {
        return getActionComponents().stream().allMatch(ActionComponent::isDisabled);
    }

    @Override
    default boolean isEnabled() {
        return getActionComponents().stream().noneMatch(ActionComponent::isDisabled);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default ActionRow withDisabled(boolean disabled) {
        return replace(
                ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)));
    }

    @Nonnull
    @Override
    default ActionRow asDisabled() {
        return (ActionRow) IDisableable.super.asDisabled();
    }

    @Nonnull
    @Override
    default ActionRow asEnabled() {
        return (ActionRow) IDisableable.super.asEnabled();
    }

    /**
     * Creates a new {@link ActionRow} with the specified components.
     *
     * @param  components
     *         The new components
     *
     * @throws IllegalArgumentException
     *         If the provided components are {@code null} or contains {@code null}
     *
     * @return The new {@link ActionRow}
     */
    @Nonnull
    @CheckReturnValue
    ActionRow withComponents(@Nonnull Collection<? extends ActionRowChildComponent> components);

    /**
     * Creates a new {@link ActionRow} with the specified components.
     *
     * @param  component
     *         The first new component
     * @param  components
     *         Additional new components
     *
     * @throws IllegalArgumentException
     *         If the provided components are {@code null} or contains {@code null}
     *
     * @return The new {@link ActionRow}
     */
    @Nonnull
    @CheckReturnValue
    default ActionRow withComponents(
            @Nonnull ActionRowChildComponent component,
            @Nonnull ActionRowChildComponent... components) {
        Checks.notNull(component, "Component");
        Checks.notNull(components, "Components");
        return withComponents(Helpers.mergeVararg(component, components));
    }
}
