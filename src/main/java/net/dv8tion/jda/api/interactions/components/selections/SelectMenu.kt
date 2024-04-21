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
package net.dv8tion.jda.api.interactions.components.selections

import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Represents a select menu in a message.
 * <br></br>This is an interactive component and usually located within an [ActionRow][net.dv8tion.jda.api.interactions.components.ActionRow].
 * One select menu fills up an entire action row by itself. You cannot have an action row with other components if a select menu is present in the same row.
 *
 *
 * The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected, and they will disappear when the client restarts or the message is reloaded.
 *
 *
 * This is a generic interface for all types of select menus.
 * <br></br>You can use [EntitySelectMenu.create] to create a select menu of Discord entities such as [users][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.USER].
 * <br></br>Alternatively, you can use [StringSelectMenu.create] to create a select menu of up to {@value #OPTIONS_MAX_AMOUNT} pre-defined strings to pick from.
 *
 * @see StringSelectMenu
 *
 * @see EntitySelectMenu
 *
 * @see SelectMenuInteraction
 */
interface SelectMenu : ActionComponent {
    /**
     * Placeholder which is displayed when no selections have been made yet.
     *
     * @return The placeholder or null
     */
    @JvmField
    val placeholder: String?

    /**
     * The minimum amount of values a user has to select.
     *
     * @return The min values
     */
    @JvmField
    val minValues: Int

    /**
     * The maximum amount of values a user can select at once.
     *
     * @return The max values
     */
    @JvmField
    val maxValues: Int

    /**
     * A preconfigured builder for the creation of select menus.
     *
     * @param <T>
     * The output type
     * @param <B>
     * The builder type (used for fluent interface)
    </B></T> */
    abstract class Builder<T : SelectMenu?, B : Builder<T, B>?> protected constructor(@Nonnull customId: String?) {
        /**
         * The custom id used to identify the select menu.
         *
         * @return The custom id
         */
        @get:Nonnull
        var id: String? = null
            protected set

        /**
         * Placeholder which is displayed when no selections have been made yet.
         *
         * @return The placeholder or null
         */
        var placeholder: String? = null
            protected set

        /**
         * The minimum amount of values a user has to select.
         *
         * @return The min values
         */
        var minValues = 1
            protected set

        /**
         * The maximum amount of values a user can select at once.
         *
         * @return The max values
         */
        var maxValues = 1
            protected set

        /**
         * Whether the menu is disabled
         *
         * @return True if this menu is disabled
         */
        var isDisabled = false
            protected set

        init {
            setId(customId)
        }

        /**
         * Change the custom id used to identify the select menu.
         *
         * @param  customId
         * The new custom id to use
         *
         * @throws IllegalArgumentException
         * If the provided id is null, empty, or longer than {@value #ID_MAX_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setId(@Nonnull customId: String?): B {
            Checks.notEmpty(customId, "Component ID")
            Checks.notLonger(customId, ID_MAX_LENGTH, "Component ID")
            id = customId
            return this as B
        }

        /**
         * Configure the placeholder which is displayed when no selections have been made yet.
         *
         * @param  placeholder
         * The placeholder or null
         *
         * @throws IllegalArgumentException
         * If the provided placeholder is empty or longer than {@value #PLACEHOLDER_MAX_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setPlaceholder(placeholder: String?): B {
            if (placeholder != null) {
                Checks.notEmpty(placeholder, "Placeholder")
                Checks.notLonger(placeholder, PLACEHOLDER_MAX_LENGTH, "Placeholder")
            }
            this.placeholder = placeholder
            return this as B
        }

        /**
         * The minimum amount of values a user has to select.
         * <br></br>Default: `1`
         *
         *
         * The minimum must not exceed the amount of available options.
         *
         * @param  minValues
         * The min values
         *
         * @throws IllegalArgumentException
         * If the provided amount is negative or greater than {@value #OPTIONS_MAX_AMOUNT}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setMinValues(minValues: Int): B {
            Checks.notNegative(minValues, "Min Values")
            Checks.check(
                minValues <= OPTIONS_MAX_AMOUNT,
                "Min Values may not be greater than %d! Provided: %d",
                OPTIONS_MAX_AMOUNT,
                minValues
            )
            this.minValues = minValues
            return this as B
        }

        /**
         * The maximum amount of values a user can select.
         * <br></br>Default: `1`
         *
         *
         * The maximum must not exceed the amount of available options.
         *
         * @param  maxValues
         * The max values
         *
         * @throws IllegalArgumentException
         * If the provided amount is less than 1 or greater than {@value #OPTIONS_MAX_AMOUNT}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setMaxValues(maxValues: Int): B {
            Checks.positive(maxValues, "Max Values")
            Checks.check(
                maxValues <= OPTIONS_MAX_AMOUNT,
                "Max Values may not be greater than %d! Provided: %d",
                OPTIONS_MAX_AMOUNT,
                maxValues
            )
            this.maxValues = maxValues
            return this as B
        }

        /**
         * The minimum and maximum amount of values a user can select.
         * <br></br>Default: `1` for both
         *
         *
         * The minimum or maximum must not exceed the amount of available options.
         *
         * @param  min
         * The min values
         * @param  max
         * The max values
         *
         * @throws IllegalArgumentException
         * If the provided amount is not a valid range (`0 <= min <= max`)
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setRequiredRange(min: Int, max: Int): B {
            Checks.check(
                min <= max,
                "Min Values should be less than or equal to Max Values! Provided: [%d, %d]",
                min,
                max
            )
            return setMinValues(min)!!.setMaxValues(max)
        }

        /**
         * Configure whether this select menu should be disabled.
         * <br></br>Default: `false`
         *
         * @param  disabled
         * Whether this menu is disabled
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setDisabled(disabled: Boolean): B {
            isDisabled = disabled
            return this as B
        }

        /**
         * Creates a new [SelectMenu] instance if all requirements are satisfied.
         *
         * @throws IllegalArgumentException
         * Throws if [.getMinValues] is greater than [.getMaxValues]
         *
         * @return The new [SelectMenu] instance
         */
        @Nonnull
        abstract fun build(): T
    }

    companion object {
        /**
         * The maximum length a select menu id can have
         */
        const val ID_MAX_LENGTH = 100

        /**
         * The maximum length a select menu placeholder can have
         */
        const val PLACEHOLDER_MAX_LENGTH = 100

        /**
         * The maximum amount of options a select menu can have
         */
        const val OPTIONS_MAX_AMOUNT = 25
    }
}
