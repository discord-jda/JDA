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

package net.dv8tion.jda.api.interactions.components.selections;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a select menu in a message.
 * <br>This is an interactive component and usually located within an {@link net.dv8tion.jda.api.interactions.components.ActionRow ActionRow}.
 * One select menu fills up an entire action row by itself. You cannot have an action row with other components if a select menu is present in the same row.
 *
 * <p>The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected, and they will disappear when the client restarts or the message is reloaded.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 *   if (!event.getName().equals("class")) return;
 *
 *   SelectMenu menu = SelectMenu.create("menu:class")
 *     .setPlaceholder("Choose your class") // shows the placeholder indicating what this menu is for
 *     .setRequireRange(1, 1) // only one can be selected
 *     .addOption("Arcane Mage", "mage-arcane")
 *     .addOption("Fire Mage", "mage-fire")
 *     .addOption("Frost Mage", "mage-frost")
 *     .build();
 *
 *   event.reply("Please pick your class below")
 *     .setEphemeral(true)
 *     .addActionRow(menu)
 *     .queue();
 * }
 * }</pre>
 *
 * @see SelectMenuInteraction
 */
public interface SelectMenu extends ActionComponent
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
     * An <b>unmodifiable</b> list of up to {@value OPTIONS_MAX_AMOUNT} available options to choose from.
     *
     * @return The {@link SelectOption SelectOptions} this menu provides
     *
     * @see    Builder#getOptions()
     */
    @Nonnull
    List<SelectOption> getOptions();

    @Nonnull
    @Override
    @CheckReturnValue
    default SelectMenu asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default SelectMenu asEnabled()
    {
        return withDisabled(false);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    default SelectMenu withDisabled(boolean disabled)
    {
        return createCopy().setDisabled(disabled).build();
    }

    /**
     * Creates a new preconfigured {@link Builder} with the same settings used for this select menu.
     * <br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    default Builder createCopy()
    {
        //noinspection ConstantConditions
        Builder builder = create(getId());
        builder.setRequiredRange(getMinValues(), getMaxValues());
        builder.setPlaceholder(getPlaceholder());
        builder.addOptions(getOptions());
        builder.setDisabled(isDisabled());
        return builder;
    }

    /**
     * Creates a new {@link Builder} for a select menu with the provided custom id.
     *
     * @param  customId
     *         The id used to identify this menu with {@link ActionComponent#getId()} for component interactions
     *
     * @throws IllegalArgumentException
     *         If the provided id is null, empty, or longer than {@value ID_MAX_LENGTH} characters
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    static Builder create(@Nonnull String customId)
    {
        return new Builder(customId);
    }

    /**
     * Inverse function for {@link #toData()} which parses the serialized select menu data.
     * <br>Returns a {@link Builder} which allows for further configuration.
     *
     * @param  data
     *         The serialized select menu data
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the data representation is invalid
     * @throws IllegalArgumentException
     *         If some part of the data has an invalid length or null is provided
     *
     * @return The parsed SelectMenu Builder instance
     */
    @Nonnull
    @CheckReturnValue
    static Builder fromData(@Nonnull DataObject data)
    {
        return new SelectMenuImpl(data).createCopy();
    }

    /**
     * A preconfigured builder for the creation of select menus.
     */
    class Builder
    {
        private String customId;
        private String placeholder;
        private int minValues = 1, maxValues = 1;
        private boolean disabled = false;
        private final List<SelectOption> options = new ArrayList<>();

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
         *         If the provided id is null, empty, or longer than {@value ID_MAX_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setId(@Nonnull String customId)
        {
            Checks.notEmpty(customId, "Component ID");
            Checks.notLonger(customId, ID_MAX_LENGTH, "Component ID");
            this.customId = customId;
            return this;
        }

        /**
         * Configure the placeholder which is displayed when no selections have been made yet.
         *
         * @param  placeholder
         *         The placeholder or null
         *
         * @throws IllegalArgumentException
         *         If the provided placeholder is empty or longer than {@value PLACEHOLDER_MAX_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setPlaceholder(@Nullable String placeholder)
        {
            if (placeholder != null)
            {
                Checks.notEmpty(placeholder, "Placeholder");
                Checks.notLonger(placeholder, PLACEHOLDER_MAX_LENGTH, "Placeholder");
            }
            this.placeholder = placeholder;
            return this;
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
         *         If the provided amount is negative or greater than {@value OPTIONS_MAX_AMOUNT}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMinValues(int minValues)
        {
            Checks.notNegative(minValues, "Min Values");
            Checks.check(minValues <= OPTIONS_MAX_AMOUNT, "Min Values may not be greater than %d! Provided: %d", OPTIONS_MAX_AMOUNT, minValues);
            this.minValues = minValues;
            return this;
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
         *         If the provided amount is less than 1 or greater than {@value OPTIONS_MAX_AMOUNT}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMaxValues(int maxValues)
        {
            Checks.positive(maxValues, "Max Values");
            Checks.check(maxValues <= OPTIONS_MAX_AMOUNT, "Min Values may not be greater than %d! Provided: %d", OPTIONS_MAX_AMOUNT, maxValues);
            this.maxValues = maxValues;
            return this;
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
        public Builder setRequiredRange(int min, int max)
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
        public Builder setDisabled(boolean disabled)
        {
            this.disabled = disabled;
            return this;
        }

        /**
         * Adds up to {@value OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  options
         *         The {@link SelectOption SelectOptions} to add
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value OPTIONS_MAX_AMOUNT} or null is provided
         *
         * @return The same builder instance for chaining
         *
         * @see    SelectOption#of(String, String)
         */
        @Nonnull
        public Builder addOptions(@Nonnull SelectOption... options)
        {
            Checks.noneNull(options, "Options");
            Checks.check(this.options.size() + options.length <= OPTIONS_MAX_AMOUNT, "Cannot have more than %d options for a select menu!", OPTIONS_MAX_AMOUNT);
            Collections.addAll(this.options, options);
            return this;
        }

        /**
         * Adds up to {@value OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  options
         *         The {@link SelectOption SelectOptions} to add
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value OPTIONS_MAX_AMOUNT} or null is provided
         *
         * @return The same builder instance for chaining
         *
         * @see    SelectOption#of(String, String)
         */
        @Nonnull
        public Builder addOptions(@Nonnull Collection<? extends SelectOption> options)
        {
            Checks.noneNull(options, "Options");
            Checks.check(this.options.size() + options.size() <= OPTIONS_MAX_AMOUNT, "Cannot have more than %d options for a select menu!", OPTIONS_MAX_AMOUNT);
            this.options.addAll(options);
            return this;
        }

        /**
         * Adds up to {@value OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         *         The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
         *         up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value OPTIONS_MAX_AMOUNT}, invalid null is provided,
         *         or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value)
        {
            return addOptions(new SelectOption(label, value));
        }

        /**
         * Adds up to {@value OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         *         The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
         *         up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  emoji
         *         The {@link Emoji} shown next to this option, or null
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value OPTIONS_MAX_AMOUNT}, invalid null is provided, or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nonnull Emoji emoji)
        {
            return addOption(label, value, null, emoji);
        }

        /**
         * Adds up to {@value OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         *         The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
         *         up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  description
         *         The description explaining the meaning of this option in more detail, up to 50 characters
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value OPTIONS_MAX_AMOUNT}, invalid null is provided,
         *         or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nonnull String description)
        {
            return addOption(label, value, description, null);
        }

        /**
         * Adds up to {@value OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         *         The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
         *         up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  description
         *         The description explaining the meaning of this option in more detail, up to 50 characters
         * @param  emoji
         *         The {@link Emoji} shown next to this option, or null
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value OPTIONS_MAX_AMOUNT}, invalid null is provided,
         *         or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nullable String description, @Nullable Emoji emoji)
        {
            return addOptions(new SelectOption(label, value, description, false, emoji));
        }

        /**
         * Modifiable list of options currently configured in this builder.
         *
         * @return The list of {@link SelectOption SelectOptions}
         */
        @Nonnull
        public List<SelectOption> getOptions()
        {
            return options;
        }

        /**
         * Configures which of the currently applied {@link #getOptions() options} should be selected by default.
         *
         * @param  values
         *         The {@link SelectOption#getValue() option values}
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setDefaultValues(@Nonnull Collection<String> values)
        {
            Checks.noneNull(values, "Values");
            Set<String> set = new HashSet<>(values);
            for (ListIterator<SelectOption> it = getOptions ().listIterator(); it.hasNext();)
            {
                SelectOption option = it.next();
                it.set(option.withDefault(set.contains(option.getValue())));
            }
            return this;
        }


        /**
         * Configures which of the currently applied {@link #getOptions() options} should be selected by default.
         *
         * @param  values
         *         The {@link SelectOption SelectOptions}
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setDefaultOptions(@Nonnull Collection<? extends SelectOption> values)
        {
            Checks.noneNull(values, "Values");
            return setDefaultValues(values.stream().map(SelectOption::getValue).collect(Collectors.toSet()));
        }

        /**
         * The custom id used to identify the select menu.
         *
         * @return The custom id
         */
        @Nonnull
        public String getId()
        {
            return customId;
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
         * <br>A select menu may not have more than {@value OPTIONS_MAX_AMOUNT} options at once.
         *
         * <p>The values for {@link #setMinValues(int)} and {@link #setMaxValues(int)} are bounded by the length of {@link #getOptions()}.
         * This means they will automatically be adjusted to not be greater than {@code getOptions().size()}.
         *
         * @throws IllegalArgumentException
         *         Throws if {@link #getMinValues()} is greater than {@link #getMaxValues()} or more than {@value OPTIONS_MAX_AMOUNT} options are provided
         *
         * @return The new {@link SelectMenu} instance
         */
        @Nonnull
        public SelectMenu build()
        {
            Checks.check(minValues <= maxValues, "Min values cannot be greater than max values!");
            Checks.check(options.size() <= OPTIONS_MAX_AMOUNT, "Cannot build a select menu with more than %d options.", OPTIONS_MAX_AMOUNT);
            int min = Math.min(minValues, options.size());
            int max = Math.min(maxValues, options.size());
            return new SelectMenuImpl(customId, placeholder, min, max, disabled, options);
        }
    }
}
