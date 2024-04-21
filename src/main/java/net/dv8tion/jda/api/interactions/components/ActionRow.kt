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

import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.interactions.component.ButtonImpl
import net.dv8tion.jda.internal.interactions.component.EntitySelectMenuImpl
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl
import net.dv8tion.jda.internal.interactions.component.TextInputImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * One row of components.
 *
 * @see ItemComponent
 *
 * @see LayoutComponent
 */
class ActionRow private constructor() : LayoutComponent {
    /**
     * Mutable list of components in this ActionRow.
     * <br></br>ActionRows should not be empty and are limited to 5 buttons.
     *
     * @return The list of components
     */
    @get:Nonnull
    override val components: MutableList<ItemComponent> = ArrayList()
    @Nonnull
    @CheckReturnValue
    override fun withDisabled(disabled: Boolean): ActionRow {
        return of(components.stream()
            .map { c: ItemComponent ->
                if (c is ActionComponent) return@map c.withDisabled(disabled)
                c
            }
            .collect(Collectors.toList()))
    }

    @Nonnull
    @CheckReturnValue
    override fun asDisabled(): ActionRow {
        return withDisabled(true)
    }

    @Nonnull
    @CheckReturnValue
    override fun asEnabled(): ActionRow {
        return withDisabled(false)
    }

    @Nonnull
    override fun createCopy(): ActionRow {
        return of(components)
    }

    @get:Nonnull
    override val type: Component.Type?
        get() = Component.Type.ACTION_ROW

    @Nonnull
    override fun toData(): DataObject {
        return DataObject.empty()
            .put("type", 1)
            .put("components", DataArray.fromCollection(components))
    }

    @Nonnull
    override fun iterator(): MutableIterator<ItemComponent> {
        return components.iterator()
    }

    override fun toString(): String {
        return EntityString(this)
            .addMetadata("components", components)
            .toString()
    }

    override fun hashCode(): Int {
        return components.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        return if (obj !is ActionRow) false else components == obj.components
    }

    companion object {
        /**
         * Load ActionRow from serialized representation.
         * <br></br>Inverse of [.toData].
         *
         * @param  data
         * Serialized version of an action row
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided data is not a valid action row
         * @throws IllegalArgumentException
         * If the data is null or the type is not 1
         *
         * @return ActionRow instance
         */
        @JvmStatic
        @Nonnull
        fun fromData(@Nonnull data: DataObject): ActionRow {
            Checks.notNull(data, "Data")
            val row = ActionRow()
            require(
                data.getInt(
                    "type",
                    0
                ) == 1
            ) { "Data has incorrect type. Expected: 1 Found: " + data.getInt("type") }
            data.getArray("components")
                .stream<DataObject> { obj: DataArray, index: Int? ->
                    obj.getObject(
                        index!!
                    )
                }
                .map<ActionComponent> { obj: DataObject ->
                    when (Component.Type.Companion.fromKey(obj.getInt("type"))) {
                        Component.Type.BUTTON -> return@map ButtonImpl(obj)
                        Component.Type.STRING_SELECT -> return@map StringSelectMenuImpl(obj)
                        Component.Type.TEXT_INPUT -> return@map TextInputImpl(obj)
                        Component.Type.USER_SELECT, Component.Type.ROLE_SELECT, Component.Type.CHANNEL_SELECT, Component.Type.MENTIONABLE_SELECT -> return@map EntitySelectMenuImpl(
                            obj
                        )

                        else -> return@map null
                    }
                }
                .filter { obj: ActionComponent? -> Objects.nonNull(obj) }
                .forEach { e: ActionComponent -> row.components.add(e) }
            return row
        }

        /**
         * Create one row of [components][ItemComponent].
         * <br></br>You cannot currently mix different types of components and each type has its own maximum defined by [Component.Type.getMaxPerRow].
         *
         * @param  components
         * The components for this action row
         *
         * @throws IllegalArgumentException
         * If anything is null, empty, or an invalid number of components are provided
         *
         * @return The action row
         */
        @Nonnull
        fun of(@Nonnull components: Collection<ItemComponent>): ActionRow {
            Checks.noneNull(components, "Components")
            return of(*components.toTypedArray<ItemComponent>())
        }

        /**
         * Create one row of [components][ItemComponent].
         * <br></br>You cannot currently mix different types of components and each type has its own maximum defined by [Component.Type.getMaxPerRow].
         *
         * @param  components
         * The components for this action row
         *
         * @throws IllegalArgumentException
         * If anything is null, empty, or an invalid number of components are provided
         *
         * @return The action row
         */
        @JvmStatic
        @Nonnull
        fun of(@Nonnull vararg components: ItemComponent): ActionRow {
            Checks.noneNull(components, "Components")
            Checks.check(components.size > 0, "Cannot have empty row!")
            val row = ActionRow()
            Collections.addAll(row.components, *components)
            if (!row.isValid()) {
                val grouped = Arrays.stream(components).collect(
                    Collectors.groupingBy(
                        Function { obj: ItemComponent -> obj.getType() })
                )
                val provided = grouped.entries
                    .stream()
                    .map { (key, value): Map.Entry<Component.Type?, List<ItemComponent>> -> value.size.toString() + "/" + key.getMaxPerRow() + " of " + key }
                    .collect(Collectors.joining(", "))
                throw IllegalArgumentException("Cannot create action row with invalid component combinations. Provided: $provided")
            }
            return row
        }

        /**
         * Partitions the provided [components][ItemComponent] into a list of ActionRow instances.
         * <br></br>This will split the provided components by [Type.getMaxPerRow] and create homogeneously typed rows,
         * meaning they will not have mixed component types.
         *
         *
         * **Example**
         * <pre>`List<ItemComponent> components = Arrays.asList(
         * Button.primary("id1", "Hello"),
         * Button.secondary("id2", "World"),
         * SelectMenu.create("menu:id").build()
         * );
         *
         * List<ActionRow> partitioned = ActionRow.partition(components);
         * // partitioned[0] = ActionRow(button, button)
         * // partitioned[1] = ActionRow(selectMenu)
        `</pre> *
         *
         * @param  components
         * The components to partition
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return [List] of [ActionRow]
         */
        @Nonnull
        fun partitionOf(@Nonnull components: Collection<ItemComponent?>): List<ActionRow> {
            Checks.noneNull(components, "Components")
            val rows: MutableList<ActionRow> = ArrayList()
            // The current action row we are building
            var currentRow: MutableList<ItemComponent>? = null
            // The component types contained in that row (for now it can't have mixed types)
            var type: Component.Type? = null
            for (current in components) {
                if (type != current.getType() || currentRow!!.size == type.maxPerRow) {
                    type = current.getType()
                    val row = of(current)
                    currentRow = row.components
                    rows.add(row)
                } else {
                    currentRow.add(current)
                }
            }
            return rows
        }

        /**
         * Partitions the provided [components][ItemComponent] into a list of ActionRow instances.
         * <br></br>This will split the provided components by [Type.getMaxPerRow] and create homogeneously typed rows,
         * meaning they will not have mixed component types.
         *
         *
         * **Example**
         * <pre>`List<ItemComponent> components = Arrays.asList(
         * Button.primary("id1", "Hello"),
         * Button.secondary("id2", "World"),
         * SelectMenu.create("menu:id").build()
         * );
         *
         * List<ActionRow> partitioned = ActionRow.partition(components);
         * // partitioned[0] = ActionRow(button, button)
         * // partitioned[1] = ActionRow(selectMenu)
        `</pre> *
         *
         * @param  components
         * The components to partition
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return [List] of [ActionRow]
         */
        @Nonnull
        fun partitionOf(@Nonnull vararg components: ItemComponent?): List<ActionRow> {
            Checks.notNull(components, "Components")
            return partitionOf(Arrays.asList(*components))
        }
    }
}
