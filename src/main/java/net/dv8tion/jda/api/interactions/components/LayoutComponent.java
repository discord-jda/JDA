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

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.attribute.IDisableable;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a top-level layout used for {@link ItemComponent ItemComponents} such as {@link Button Buttons}.
 *
 * <p>Components must always be contained within such a layout.
 *
 * @see ActionRow
 *
 * @deprecated
 *      Will be removed in a future release, please use {@link ActionRow} instead.
 */
@Deprecated
@ForRemoval
public interface LayoutComponent<T extends Component> extends SerializableData, Iterable<T>, MessageTopLevelComponent, ModalTopLevelComponent, Component, IDisableable
{
    /**
     * @deprecated Replace it by iterating on a more precise component type, such as {@link ActionRow#getComponents() ActionRow}
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    default Stream<T> iterableStream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * @deprecated Replace it by iterating on a more precise component type, such as {@link ActionRow#getComponents() ActionRow}
     */
    @Nonnull
    @Override
    @Deprecated
    @ForRemoval
    Iterator<T> iterator();

    /**
     * @deprecated Replace it by iterating on a more precise component type, such as {@link ActionRow#getComponents() ActionRow}
     */
    @Override
    @Deprecated
    @ForRemoval
    default void forEach(@Nonnull Consumer<? super T> action)
    {
        Iterable.super.forEach(action);
    }

    /**
     * @deprecated Replace it by iterating on a more precise component type, such as {@link ActionRow#getComponents() ActionRow}
     */
    @Nonnull
    @Override
    @Deprecated
    @ForRemoval
    default Spliterator<T> spliterator()
    {
        return Iterable.super.spliterator();
    }

    /**
     * List representation of this component layout.
     * <br>This list may be unmodifiable. Note that empty layouts are not supported.
     *
     * @return {@link List} of components in this layout
     *
     * @deprecated
     *         Moved to the subclasses: {@link ActionRow#getComponents()},
     *         {@link Section#getContentComponents()},
     *         {@link Container#getComponents()}
     *         This will be removed as it is unclear what this method returns for some component types,
     *         like {@link Section}, which also contains an accessory, would it be included? or not? does it get included recursively?
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    List<? extends Component> getComponents();

    /**
     * Immutable filtered copy of {@link #iterableStream()} elements which are {@link ActionComponent ActionComponents}.
     *
     * @return Immutable {@link List} copy of {@link ActionComponent ActionComponents} in this layout
     *
     * @deprecated
     *         Moved to {@link ActionRow#getActionComponents()}
     */
    @Nonnull
    @Unmodifiable
    @Deprecated
    @ForRemoval
    default List<ActionComponent> getActionComponents()
    {
        return iterableStream()
                .filter(ActionComponent.class::isInstance)
                .map(ActionComponent.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * List of buttons in this component layout.
     *
     * @return Immutable {@link List} of {@link Button Buttons}
     *
     * @deprecated
     *         Moved to {@link ActionRow#getButtons()}
     */
    @Nonnull
    @Unmodifiable
    @Deprecated
    @ForRemoval
    default List<Button> getButtons()
    {
        return iterableStream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Whether all components in this layout are {@link ActionComponent#isDisabled() disabled}.
     * <br>Note that this is a universal quantifier, which means false <b>does not</b> imply {@link #isEnabled()}!
     *
     * @return True, if all components are disabled
     */
    default boolean isDisabled()
    {
        return getActionComponents().stream().allMatch(ActionComponent::isDisabled);
    }

    /**
     * Whether all components in this layout are {@link ActionComponent#isDisabled() enabled}.
     * <br>Note that this is a universal quantifier, which means false <b>does not</b> imply {@link #isDisabled()}!
     *
     * @return True, if all components are enabled
     */
    default boolean isEnabled()
    {
        return getActionComponents().stream().noneMatch(ActionComponent::isDisabled);
    }

    @Nonnull
    @CheckReturnValue
    LayoutComponent<T> withDisabled(boolean disabled);

    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unchecked")
    default LayoutComponent<T> asDisabled()
    {
        return (LayoutComponent<T>) IDisableable.super.asDisabled();
    }

    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unchecked")
    default LayoutComponent<T> asEnabled()
    {
        return (LayoutComponent<T>) IDisableable.super.asEnabled();
    }

    /**
     * Check whether this layout is empty.
     * <br>This is <b>not always</b> the same as {@code getComponents().isEmpty()}.
     *
     * @return True, if this layout has no components
     *
     * @deprecated
     *         Will be removed in a future release, as empty layout cannot be created.
     */
    @Deprecated
    @ForRemoval
    boolean isEmpty();

    /**
     * Check whether this is a valid layout configuration.
     * The definition of whether a LayoutComponent is valid is dependent on the specific component being validated.
     * Typically, this includes making sure that the layout isn't empty and that its maximum allowed components haven't
     * been exceeded.
     *
     * @return True, if this layout is valid
     *
     * @deprecated
     *         Moved to {@link ActionRow#isValid()}
     */
    @Deprecated
    @ForRemoval
    boolean isValid();

    /**
     * Creates a copy of this {@link LayoutComponent}.
     * <br>This does not create copies of the contained components.
     *
     * @return A copy of this {@link LayoutComponent}
     *
     * @deprecated
     *         For removal, all components will be immutable, so this method is not required
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    LayoutComponent<T> createCopy();
}
