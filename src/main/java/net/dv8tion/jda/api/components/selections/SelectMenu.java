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

package net.dv8tion.jda.api.components.selections;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Represents a select menu in a message.
 * <br>This is an interactive component and usually located within an {@link ActionRow ActionRow}.
 * One select menu fills up an entire action row by itself. You cannot have an action row with other components if a select menu is present in the same row.
 *
 * <p>The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected, and they will disappear when the client restarts or the message is reloaded.
 *
 * <p>This is a generic interface for all types of select menus.
 * <br>You can use {@link EntitySelectMenu#create(String, Collection)} to create a select menu of Discord entities such as {@link net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget#USER users}.
 * <br>Alternatively, you can use {@link StringSelectMenu#create(String)} to create a select menu of up to {@value #OPTIONS_MAX_AMOUNT} pre-defined strings to pick from.
 *
 * @see StringSelectMenu
 * @see EntitySelectMenu
 * @see SelectMenuInteraction
 */
public interface SelectMenu extends ActionComponent, ActionRowChildComponent
{
    /**
     * The maximum length a select menu id can have
     */
    int ID_MAX_LENGTH = 100;

    /**
     * The maximum length a select menu placeholder can have
     */
    int PLACEHOLDER_MAX_LENGTH = 100;

    /**
     * The maximum amount of options a select menu can have
     */
    int OPTIONS_MAX_AMOUNT = 25;

    @Nonnull
    @Override
    SelectMenu withDisabled(boolean disabled);

    @Nonnull
    @Override
    SelectMenu withUniqueId(int uniqueId);

    @Nonnull
    @Override
    String getCustomId();

    /**
     * Placeholder which is displayed when no selections have been made yet.
     *
     * @return The placeholder or null
     */
    @Nullable
    String getPlaceholder();

    /**
     * The minimum amount of values a user has to select.
     *
     * @return The min values
     */
    int getMinValues();

    /**
     * The maximum amount of values a user can select at once.
     *
     * @return The max values
     */
    int getMaxValues();

    /**
     * Creates a new preconfigured {@link SelectMenu.Builder} with the same settings used for this select menu.
     * <br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The {@link SelectMenu.Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    Builder<? extends SelectMenu, ? extends Builder<?, ?>> createCopy();

    /**
     * A preconfigured builder for the creation of select menus.
     *
     * @param <T>
     *        The output type
     * @param <B>
     *        The builder type (used for fluent interface)
     */
    @SuppressWarnings("unchecked")
    abstract class Builder<T extends SelectMenu, B extends Builder<T, B>>
    {
        protected String customId;
        protected int uniqueId = -1;
        protected String placeholder;
        protected int minValues = 1, maxValues = 1;
        protected boolean disabled = false;

        protected Builder(@Nonnull String customId)
        {
            setId(customId);
        }

        /**
         * Change the custom id used to identify the select menu.
         *
         * @param  customId
         *         The new custom id to use
         *
         * @throws IllegalArgumentException
         *         If the provided id is null, empty, or longer than {@value #ID_MAX_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public B setId(@Nonnull String customId)
        {
            Checks.notEmpty(customId, "Component ID");
            Checks.notLonger(customId, ID_MAX_LENGTH, "Component ID");
            this.customId = customId;
            return (B) this;
        }

        /**
         * Changes the numeric ID used to identify the select menu.
         *
         * @param  uniqueId
         *         The new ID, must not be negative
         *
         * @throws IllegalArgumentException
         *         If the ID is negative
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public B setUniqueId(int uniqueId)
        {
            Checks.positive(uniqueId, "Unique ID");
            this.uniqueId = uniqueId;
            return (B) this;
        }

        /**
         * Configure the placeholder which is displayed when no selections have been made yet.
         *
         * @param  placeholder
         *         The placeholder or null
         *
         * @throws IllegalArgumentException
         *         If the provided placeholder is empty or longer than {@value #PLACEHOLDER_MAX_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public B setPlaceholder(@Nullable String placeholder)
        {
            if (placeholder != null)
            {
                Checks.notEmpty(placeholder, "Placeholder");
                Checks.notLonger(placeholder, PLACEHOLDER_MAX_LENGTH, "Placeholder");
            }
            this.placeholder = placeholder;
            return (B) this;
        }

        /**
         * The minimum amount of values a user has to select.
         * <br>Default: {@code 1}
         *
         * <p>The minimum must not exceed the amount of available options.
         *
         * @param  minValues
         *         The min values
         *
         * @throws IllegalArgumentException
         *         If the provided amount is negative or greater than {@value #OPTIONS_MAX_AMOUNT}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public B setMinValues(int minValues)
        {
            Checks.notNegative(minValues, "Min Values");
            Checks.check(minValues <= OPTIONS_MAX_AMOUNT, "Min Values may not be greater than %d! Provided: %d", OPTIONS_MAX_AMOUNT, minValues);
            this.minValues = minValues;
            return (B) this;
        }

        /**
         * The maximum amount of values a user can select.
         * <br>Default: {@code 1}
         *
         * <p>The maximum must not exceed the amount of available options.
         *
         * @param  maxValues
         *         The max values
         *
         * @throws IllegalArgumentException
         *         If the provided amount is less than 1 or greater than {@value #OPTIONS_MAX_AMOUNT}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public B setMaxValues(int maxValues)
        {
            Checks.positive(maxValues, "Max Values");
            Checks.check(maxValues <= OPTIONS_MAX_AMOUNT, "Max Values may not be greater than %d! Provided: %d", OPTIONS_MAX_AMOUNT, maxValues);
            this.maxValues = maxValues;
            return (B) this;
        }

        /**
         * The minimum and maximum amount of values a user can select.
         * <br>Default: {@code 1} for both
         *
         * <p>The minimum or maximum must not exceed the amount of available options.
         *
         * @param  min
         *         The min values
         * @param  max
         *         The max values
         *
         * @throws IllegalArgumentException
         *         If the provided amount is not a valid range ({@code 0 <= min <= max})
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public B setRequiredRange(int min, int max)
        {
            Checks.check(min <= max, "Min Values should be less than or equal to Max Values! Provided: [%d, %d]", min, max);
            return setMinValues(min).setMaxValues(max);
        }

        /**
         * Configure whether this select menu should be disabled.
         * <br>Default: {@code false}
         *
         * @param  disabled
         *         Whether this menu is disabled
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public B setDisabled(boolean disabled)
        {
            this.disabled = disabled;
            return (B) this;
        }

        /**
         * The custom id used to identify the select menu.
         *
         * @return The custom id
         *
         * @deprecated
         *         Replaced with {@link #getCustomId()}
         */
        @Nonnull
        @Deprecated
        @ForRemoval
        @ReplaceWith("getCustomId()")
        public String getId()
        {
            return customId;
        }

        /**
         * The custom id used to identify the select menu.
         *
         * @return The custom id
         */
        @Nonnull
        public String getCustomId()
        {
            return customId;
        }

        /**
         * The numeric id used to identify the select menu.
         *
         * @return The numeric id
         */
        public int getUniqueId()
        {
            return uniqueId;
        }

        /**
         * Placeholder which is displayed when no selections have been made yet.
         *
         * @return The placeholder or null
         */
        @Nullable
        public String getPlaceholder()
        {
            return placeholder;
        }

        /**
         * The minimum amount of values a user has to select.
         *
         * @return The min values
         */
        public int getMinValues()
        {
            return minValues;
        }

        /**
         * The maximum amount of values a user can select at once.
         *
         * @return The max values
         */
        public int getMaxValues()
        {
            return maxValues;
        }

        /**
         * Whether the menu is disabled
         *
         * @return True if this menu is disabled
         */
        public boolean isDisabled()
        {
            return disabled;
        }

        /**
         * Creates a new {@link SelectMenu} instance if all requirements are satisfied.
         *
         * @throws IllegalArgumentException
         *         Throws if {@link #getMinValues()} is greater than {@link #getMaxValues()}
         *
         * @return The new {@link SelectMenu} instance
         */
        @Nonnull
        public abstract T build();
    }
}
