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
package net.dv8tion.jda.api.interactions.components

import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.util.function.Function
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a top-level layout used for [ItemComponents][ItemComponent] such as [Buttons][Button].
 *
 *
 * Components must always be contained within such a layout.
 *
 * @see ActionRow
 */
interface LayoutComponent : SerializableData, Iterable<ItemComponent?>, Component {
    @JvmField
    @get:Nonnull
    val components: MutableList<ItemComponent>
    override val isMessageCompatible: Boolean
        get() = if (!getType().isMessageCompatible) false else components.stream()
            .allMatch { obj: ItemComponent -> obj.isMessageCompatible }
    override val isModalCompatible: Boolean
        get() = if (!getType().isModalCompatible) false else components.stream()
            .allMatch { obj: ItemComponent -> obj.isModalCompatible }

    @get:Nonnull
    val actionComponents: List<ActionComponent>
        /**
         * Immutable filtered copy of [.getComponents] elements which are [ActionComponents][ActionComponent].
         *
         * @return Immutable [List] copy of [ActionComponents][ActionComponent] in this layout
         */
        get() = components.stream()
            .filter { obj: ItemComponent? -> ActionComponent::class.java.isInstance(obj) }
            .map { obj: ItemComponent? -> ActionComponent::class.java.cast(obj) }
            .collect(Helpers.toUnmodifiableList())

    @get:Nonnull
    val buttons: List<Button>?
        /**
         * List of buttons in this component layout.
         *
         * @return Immutable [List] of [Buttons][Button]
         */
        get() = components.stream()
            .filter { obj: ItemComponent? -> Button::class.java.isInstance(obj) }
            .map { obj: ItemComponent? -> Button::class.java.cast(obj) }
            .collect(Helpers.toUnmodifiableList())
    val isDisabled: Boolean
        /**
         * Whether all components in this layout are [disabled][ActionComponent.isDisabled].
         * <br></br>Note that this is a universal quantifier, which means false **does not** imply [.isEnabled]!
         *
         * @return True, if all components are disabled
         */
        get() = actionComponents.stream().allMatch { obj: ActionComponent -> obj.isDisabled() }
    val isEnabled: Boolean
        /**
         * Whether all components in this layout are [enabled][ActionComponent.isDisabled].
         * <br></br>Note that this is a universal quantifier, which means false **does not** imply [.isDisabled]!
         *
         * @return True, if all components are enabled
         */
        get() = actionComponents.stream().noneMatch { obj: ActionComponent -> obj.isDisabled() }

    /**
     * Returns a new instance of this LayoutComponent with all components set to disabled/enabled.
     * <br></br>This does not modify the layout this was called on. To do this in-place, you can use [.getComponents].
     *
     * @param  disabled
     * True if the components should be set to disabled, false if they should be enabled
     *
     * @return The new layout component with all components updated
     *
     * @see ActionComponent.withDisabled
     */
    @Nonnull
    @CheckReturnValue
    fun withDisabled(disabled: Boolean): LayoutComponent

    /**
     * Returns a new instance of this LayoutComponent with all components set to disabled.
     * <br></br>This does not modify the layout this was called on. To do this in-place, you can use [.getComponents].
     *
     * @return The new layout component with all components updated
     *
     * @see ActionComponent.asDisabled
     */
    @Nonnull
    @CheckReturnValue
    fun asDisabled(): LayoutComponent

    /**
     * Returns a new instance of this LayoutComponent with all components set to enabled.
     * <br></br>This does not modify the layout this was called on. To do this in-place, you can use [.getComponents].
     *
     * @return The new layout component with all components updated
     *
     * @see ActionComponent.asEnabled
     */
    @Nonnull
    @CheckReturnValue
    fun asEnabled(): LayoutComponent
    val isEmpty: Boolean
        /**
         * Check whether this layout is empty.
         * <br></br>Identical to `getComponents().isEmpty()`
         *
         * @return True, if this layout has no components
         */
        get() = components.isEmpty()
    val isValid: Boolean
        /**
         * Check whether this is a valid layout configuration.
         * <br></br>This checks that there is at least one component in this layout and it does not violate [ItemComponent.getMaxPerRow].
         *
         * @return True, if this layout is valid
         */
        get() {
            if (isEmpty) return false
            val components: List<ItemComponent> = components
            val groups = components.stream().collect(
                Collectors.groupingBy(
                    Function { obj: ItemComponent -> obj.getType() })
            )
            if (groups.size > 1) // TODO: You can't mix components right now but maybe in the future, we need to check back on this when that happens
                return false
            for ((type, list) in groups) {
                if (list.size > type.getMaxPerRow()) return false
            }
            return true
        }

    /**
     * Creates a copy of this [LayoutComponent].
     * <br></br>This does not create copies of the contained components.
     *
     * @return A copy of this [LayoutComponent]
     */
    @Nonnull
    fun createCopy(): LayoutComponent

    /**
     * Find and replace a component in this layout.
     * <br></br>This will locate and replace the existing component with the specified ID. If you provide null it will be removed instead.
     *
     * @param  id
     * The custom id of this component, can also be a URL for a [Button] with [ButtonStyle.LINK]
     * @param  newComponent
     * The new component or null to remove it
     *
     * @throws IllegalArgumentException
     * If the provided id is null
     *
     * @return The old [ItemComponent] that was replaced or removed
     */
    fun updateComponent(@Nonnull id: String, newComponent: ItemComponent?): ItemComponent? {
        Checks.notNull(id, "ID")
        val list = components
        val it = list.listIterator()
        while (it.hasNext()) {
            val component = it.next() as? ActionComponent ?: continue
            val action = component
            if (id == action.getId() || action is Button && id == action.url) {
                if (newComponent == null) it.remove() else it.set(newComponent)
                return component
            }
        }
        return null
    }

    /**
     * Find and replace a component in this layout.
     * <br></br>This will locate and replace the existing component by checking for [equality][Object.equals]. If you provide null it will be removed instead.
     *
     *
     * **Example**
     * <pre>`public void disableButton(ActionRow row, Button button) {
     * row.updateComponent(button, button.asDisabled());
     * }
    `</pre> *
     *
     * @param  component
     * The component that should be replaced
     * @param  newComponent
     * The new component or null to remove it
     *
     * @throws IllegalArgumentException
     * If the provided component is null
     *
     * @return The old [ItemComponent] that was replaced or removed
     */
    fun updateComponent(@Nonnull component: ItemComponent, newComponent: ItemComponent?): ItemComponent? {
        Checks.notNull(component, "Component to replace")
        val list = components
        val it = list.listIterator()
        while (it.hasNext()) {
            val item = it.next()
            if (component == item) {
                if (newComponent == null) it.remove() else it.set(newComponent)
                return component
            }
        }
        return null
    }

    companion object {
        /**
         * Find and replace a component in this list of layouts.
         * <br></br>This will locate and replace the existing component with the specified ID. If you provide null it will be removed instead.
         *
         *
         * If one of the layouts is empty after removing the component, it will be removed from the list.
         * This is an inplace operation and modifies the provided list directly.
         *
         * @param  layouts
         * The layouts to modify
         * @param  id
         * The custom id of this component, can also be a URL for a [Button] with [ButtonStyle.LINK]
         * @param  newComponent
         * The new component or null to remove it
         *
         * @throws UnsupportedOperationException
         * If the list cannot be modified
         * @throws IllegalArgumentException
         * If the provided id or list is null or the replace operation results in an [invalid][.isValid] layout
         *
         * @return True, if any of the layouts was modified
         */
        fun updateComponent(
            @Nonnull layouts: MutableList<out LayoutComponent>,
            @Nonnull id: String,
            newComponent: ItemComponent?
        ): Boolean {
            Checks.notNull(layouts, "LayoutComponent")
            Checks.notEmpty(id, "ID or URL")
            val it = layouts.iterator()
            while (it.hasNext()) {
                val components = it.next()
                val oldComponent = components.updateComponent(id, newComponent)
                if (oldComponent != null) {
                    if (components.components.isEmpty()) it.remove() else require(!(!components.isValid && newComponent != null)) { "Cannot replace " + oldComponent.getType() + " with " + newComponent.getType() + " due to a violation of the layout maximum. The resulting LayoutComponent is invalid!" }
                    return oldComponent != newComponent
                }
            }
            return false
        }

        /**
         * Find and replace a component in this list of layouts.
         * <br></br>This will locate and replace the existing component by checking for [equality][Object.equals]. If you provide null it will be removed instead.
         *
         *
         * If one of the layouts is empty after removing the component, it will be removed from the list.
         * This is an inplace operation and modifies the provided list directly.
         *
         * @param  layouts
         * The layouts to modify
         * @param  component
         * The component that should be replaced
         * @param  newComponent
         * The new component or null to remove it
         *
         * @throws UnsupportedOperationException
         * If the list cannot be modified
         * @throws IllegalArgumentException
         * If the provided component or list is null or the replace operation results in an [invalid][.isValid] layout
         *
         * @return True, if any of the layouts was modified
         */
        fun updateComponent(
            @Nonnull layouts: MutableList<out LayoutComponent>,
            @Nonnull component: ItemComponent,
            newComponent: ItemComponent?
        ): Boolean {
            Checks.notNull(layouts, "LayoutComponent")
            Checks.notNull(component, "Component to replace")
            val it = layouts.iterator()
            while (it.hasNext()) {
                val components = it.next()
                val oldComponent = components.updateComponent(component, newComponent)
                if (oldComponent != null) {
                    if (components.components.isEmpty()) it.remove() else require(!(!components.isValid && newComponent != null)) { "Cannot replace " + oldComponent.getType() + " with " + newComponent.getType() + " due to a violation of the layout maximum. The resulting LayoutComponent is invalid!" }
                    return oldComponent != newComponent
                }
            }
            return false
        }
    }
}
