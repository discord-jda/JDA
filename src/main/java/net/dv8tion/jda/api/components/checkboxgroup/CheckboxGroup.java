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

package net.dv8tion.jda.api.components.checkboxgroup;

import net.dv8tion.jda.api.components.attribute.ICustomId;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.internal.components.checkboxgroup.CheckboxGroupImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A component displaying a group of up to {@value #OPTIONS_MAX_AMOUNT} checkboxes which can be checked independently.
 *
 * <p>Must be used inside {@link net.dv8tion.jda.api.components.label.Label Labels} only!
 *
 * @see #create(String)
 */
public interface CheckboxGroup extends ICustomId, LabelChildComponent {
    /** The maximum length of a checkbox group's custom ID. ({@value}) */
    int CUSTOM_ID_MAX_LENGTH = 100;

    /** The maximum number of options a checkbox group can have. ({@value}) */
    int OPTIONS_MAX_AMOUNT = 10;

    @Nonnull
    @Override
    CheckboxGroup withUniqueId(int uniqueId);

    @Nonnull
    @Override
    String getCustomId();

    /**
     * Returns an immutable list of the options available for this checkbox group.
     *
     * @return Immutable list of this checkbox group's options
     */
    @Nonnull
    @Unmodifiable
    List<CheckboxGroupOption> getOptions();

    /**
     * The minimum number of values the user has to select.
     *
     * @return Minimum number of values the user has to select
     */
    int getMinValues();

    /**
     * The maximum number of values the user can select.
     *
     * @return Maximum number of values the user can select
     */
    int getMaxValues();

    /**
     * Whether this checkbox group should, if at least one option is selected,
     * enforce the value range before sending.
     *
     * @return {@code true} if the value range should be enforced
     */
    boolean isRequired();

    /**
     * Creates a new checkbox group builder with the provided custom ID.
     *
     * @param  customId
     *         The custom ID, can be used to pass data to handlers
     *
     * @throws IllegalArgumentException
     *         If the ID is {@code null} or blank
     *
     * @return The new builder
     */
    @Nonnull
    static Builder create(@Nonnull String customId) {
        return new Builder(customId);
    }

    /**
     * Creates a new preconfigured {@link CheckboxGroup.Builder} with the same settings used for this checkbox group.
     * <br>This can be useful to create an updated version of this checkbox group without needing to rebuild it from scratch.
     *
     * @return The {@link CheckboxGroup.Builder} used to create the checkbox group
     */
    @Nonnull
    default Builder createCopy() {
        return create(getCustomId())
                .setUniqueId(getUniqueId())
                .setMinValues(getMinValues())
                .setMaxValues(getMaxValues())
                .addOptions(getOptions())
                .setRequired(isRequired());
    }

    /**
     * Builder of {@link CheckboxGroup}
     *
     * @see CheckboxGroup#create(String)
     */
    class Builder {
        protected int uniqueId = -1;
        protected String customId;
        protected final List<CheckboxGroupOption> options = new ArrayList<>();
        protected int minValues = -1, maxValues = -1;
        protected boolean required = true;

        protected Builder(@Nonnull String customId) {
            this.customId = customId;
        }

        /**
         * The unique ID of this checkbox group, or {@code -1} if it isn't set.
         *
         * @return The unique ID, or {@code -1}
         */
        public int getUniqueId() {
            return uniqueId;
        }

        /**
         * The custom ID of this checkbox group.
         *
         * @return The custom ID
         */
        @Nonnull
        public String getCustomId() {
            return customId;
        }

        /**
         * The minimum number of values the user has to select.
         *
         * @return Minimum number of values the user has to select
         */
        public int getMinValues() {
            return minValues;
        }

        /**
         * The maximum number of values the user can select.
         *
         * @return Maximum number of values the user can select
         */
        public int getMaxValues() {
            return maxValues;
        }

        /**
         * Whether this checkbox group should, if at least one option is selected,
         * enforce the value range before sending.
         *
         * @return {@code true} if the value range should be enforced
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Sets the unique ID of this checkbox group.
         * <br>This is used to identify components in the tree.
         *
         * @param  uniqueId
         *         The new unique ID, must be positive
         *
         * @throws IllegalArgumentException
         *         If the ID is negative or zero
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setUniqueId(int uniqueId) {
            Checks.positive(uniqueId, "Unique ID");
            this.uniqueId = uniqueId;
            return this;
        }

        /**
         * Sets the custom ID of this checkbox group.
         * <br>This is typically used to carry data between the modal creator and the modal handler.
         *
         * @param  customId
         *         The new custom ID
         *
         * @throws IllegalArgumentException
         *         If the ID is {@code null}, blank, or longer than {@value #CUSTOM_ID_MAX_LENGTH} characters
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setCustomId(@Nonnull String customId) {
            Checks.notBlank(customId, "Custom ID");
            Checks.notLonger(customId, CUSTOM_ID_MAX_LENGTH, "Custom ID");
            this.customId = customId;
            return this;
        }

        /**
         * Adds the provided options to this checkbox group.
         *
         * @param  options
         *         The options to add
         *
         * @throws IllegalArgumentException
         *         If the array or an element is {@code null}
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder addOptions(@Nonnull CheckboxGroupOption... options) {
            Checks.noneNull(options, "Options");
            return addOptions(Arrays.asList(options));
        }

        /**
         * Adds the provided options to this checkbox group.
         *
         * @param  options
         *         The options to add
         *
         * @throws IllegalArgumentException
         *         If the collection or an element is {@code null}
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder addOptions(@Nonnull Collection<? extends CheckboxGroupOption> options) {
            Checks.noneNull(options, "Options");
            this.options.addAll(options);
            return this;
        }

        /**
         * Adds an option to this checkbox group.
         *
         * @param  label
         *         The label to be displayed next to the checkbox,
         *         up to {@value CheckboxGroupOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value associated to the option, this is what your bot will receive,
         *         up to {@value CheckboxGroupOption#VALUE_MAX_LENGTH} characters
         *
         * @throws IllegalArgumentException
         *         If the label or value is {@code null} or blank,
         *         or one of them is longer than allowed
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value) {
            return addOptions(CheckboxGroupOption.of(label, value));
        }

        /**
         * Adds an option to this checkbox group.
         *
         * @param  label
         *         The label to be displayed next to the checkbox,
         *         up to {@value CheckboxGroupOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value associated to the option, this is what your bot will receive,
         *         up to {@value CheckboxGroupOption#VALUE_MAX_LENGTH} characters
         * @param  description
         *         The description of this checkbox,
         *         up to {@value CheckboxGroupOption#DESCRIPTION_MAX_LENGTH} characters
         *
         * @throws IllegalArgumentException
         *         If the label or value is {@code null} or blank, the description is blank,
         *         or one of them is longer than allowed
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nullable String description) {
            return addOptions(CheckboxGroupOption.of(label, value, description));
        }

        /**
         * Adds an option to this checkbox group.
         *
         * @param  label
         *         The label to be displayed next to the checkbox,
         *         up to {@value CheckboxGroupOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value associated to the option, this is what your bot will receive,
         *         up to {@value CheckboxGroupOption#VALUE_MAX_LENGTH} characters
         * @param  description
         *         The description of this checkbox,
         *         up to {@value CheckboxGroupOption#DESCRIPTION_MAX_LENGTH} characters
         * @param  isDefault
         *         Whether this option will be selected by default
         *
         * @throws IllegalArgumentException
         *         If the label or value is {@code null} or blank, or the description is blank,
         *         or one of them is longer than allowed
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder addOption(
                @Nonnull String label, @Nonnull String value, @Nullable String description, boolean isDefault) {
            return addOptions(CheckboxGroupOption.of(label, value, description, isDefault));
        }

        /**
         * Returns a modifiable list of checkbox group options.
         *
         * @return The modifiable list of options
         */
        @Nonnull
        public List<CheckboxGroupOption> getOptions() {
            return options;
        }

        /**
         * Sets the minimum number of values the user has to select.
         *
         * <p>If you set this to zero, you must set this checkbox group as {@linkplain #setRequired(boolean) optional}.
         *
         * @param  minValues
         *         Minimum number of values to select
         *
         * @throws IllegalArgumentException
         *         If the value is negative or greater than {@value #OPTIONS_MAX_AMOUNT}
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setMinValues(int minValues) {
            Checks.notNegative(minValues, "Min values");
            Checks.check(minValues <= OPTIONS_MAX_AMOUNT, "Min values cannot be greater than " + OPTIONS_MAX_AMOUNT);
            this.minValues = minValues;
            return this;
        }

        /**
         * Sets the maximum number of values the user can select.
         *
         * @param  maxValues
         *         Maximum number of selectable values
         *
         * @throws IllegalArgumentException
         *         If the value is zero, negative, or, greater than {@value #OPTIONS_MAX_AMOUNT}
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setMaxValues(int maxValues) {
            Checks.positive(maxValues, "Max values");
            Checks.check(maxValues <= OPTIONS_MAX_AMOUNT, "Max values cannot be greater than " + OPTIONS_MAX_AMOUNT);
            this.maxValues = maxValues;
            return this;
        }

        /**
         * Sets the minimum and maximum number of values the user has to select.
         *
         * <p>If you set the minimum to zero, you must set this checkbox group as {@linkplain #setRequired(boolean) optional}.
         *
         * @param  minValues
         *         Minimum number of values to select
         * @param  maxValues
         *         Maximum number of selectable values
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the min value is negative or greater than {@value #OPTIONS_MAX_AMOUNT}</li>
         *             <li>If the max value is zero, negative, or, greater than {@value #OPTIONS_MAX_AMOUNT}</li>
         *         </ul>
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setRequiredRange(int minValues, int maxValues) {
            Checks.check(
                    minValues <= maxValues,
                    "Min values (%d) cannot be greater than max values (%d)",
                    minValues,
                    maxValues);
            return setMinValues(minValues).setMinValues(maxValues);
        }

        /**
         * Sets whether this checkbox group must have at least {@linkplain #setMinValues(int) the minimum amount of options} be selected.
         *
         * <p>This attribute is completely separate from the value range,
         * for example, you can have an optional checkbox group with the range set to {@code [2 ; 5]},
         * meaning you accept either 0 options, or, at least 2 but at most 5.
         *
         * <p>Checkbox groups are required by default.
         *
         * @param  required
         *         {@code true} if the value range must be enforced when at least one value is selected
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Configures which of the currently applied {@linkplain #getOptions() options} should be selected by default.
         *
         * @param  values
         *         The {@linkplain CheckboxGroupOption#getValue() option values} to select
         *
         * @throws IllegalArgumentException
         *         If {@code null} is provided
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setSelectedValues(@Nonnull Collection<String> values) {
            Checks.noneNull(values, "Values");
            for (ListIterator<CheckboxGroupOption> iterator = options.listIterator(); iterator.hasNext(); ) {
                CheckboxGroupOption option = iterator.next();
                if (values.contains(option.getValue())) {
                    iterator.set(option.withDefault(true));
                }
            }
            return this;
        }

        /**
         * Configures which of the currently applied {@linkplain #getOptions() options} should be selected by default.
         *
         * @param  values
         *         The {@linkplain CheckboxGroupOption#getValue() option values} to select
         *
         * @throws IllegalArgumentException
         *         If {@code null} is provided
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setSelectedValues(@Nonnull String... values) {
            Checks.noneNull(values, "Values");
            return setSelectedValues(Arrays.asList(values));
        }

        /**
         * Configures which of the currently applied options should be selected by default.
         *
         * @param  options
         *         The options to select
         *
         * @throws IllegalArgumentException
         *         If {@code null} is provided
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setSelectedOptions(@Nonnull CheckboxGroupOption... options) {
            Checks.noneNull(options, "Options");
            return setSelectedValues(
                    Arrays.stream(options).map(CheckboxGroupOption::getValue).collect(Collectors.toList()));
        }

        /**
         * Configures which of the currently applied options should be selected by default.
         *
         * @param  options
         *         The options to select
         *
         * @throws IllegalArgumentException
         *         If {@code null} is provided
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setSelectedOptions(@Nonnull Collection<CheckboxGroupOption> options) {
            Checks.noneNull(options, "Options");
            return setSelectedValues(
                    options.stream().map(CheckboxGroupOption::getValue).collect(Collectors.toList()));
        }

        /**
         * Builds a new {@link CheckboxGroup} from the current configuration.
         *
         * @throws IllegalStateException
         *         <ul>
         *             <li>If no options have been set</li>
         *             <li>If there is more than {@value #OPTIONS_MAX_AMOUNT} options</li>
         *             <li>If the minimum value range is greater than the maximum value range</li>
         *             <li>If the minimum value range is 0 and is also required</li>
         *         </ul>
         *
         * @return The new {@link CheckboxGroup}
         */
        @Nonnull
        public CheckboxGroup build() {
            if (options.isEmpty()) {
                throw new IllegalStateException("Cannot build a checkbox group with no options");
            }
            if (options.size() > OPTIONS_MAX_AMOUNT) {
                throw new IllegalStateException(
                        "Cannot build a checkbox group with more than " + OPTIONS_MAX_AMOUNT + " options");
            }
            if ((minValues != -1 && maxValues != -1) && minValues > maxValues) {
                throw new IllegalStateException(
                        String.format("Min values (%d) cannot be greater than max values (%d)", minValues, maxValues));
            }
            if (minValues == 0 && required) {
                throw new IllegalStateException("A checkbox group cannot have min values set to 0 and be required at the same time");
            }

            int min = Math.min(minValues, options.size());
            int max = Math.min(maxValues, options.size());
            return new CheckboxGroupImpl(uniqueId, customId, options, min, max, required);
        }
    }
}
