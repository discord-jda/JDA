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

package net.dv8tion.jda.api.components.radiogroup;

import net.dv8tion.jda.api.components.attribute.ICustomId;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.internal.components.radiogroup.RadioGroupImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A component displaying a group of up to {@value #OPTIONS_MAX_AMOUNT} radio buttons, in which only one can be chosen.
 *
 * <p>Must be used inside {@link net.dv8tion.jda.api.components.label.Label Labels} only!
 *
 * @see #create(String)
 */
public interface RadioGroup extends ICustomId, LabelChildComponent {
    /** The maximum length of a radio group's custom ID. ({@value}) */
    int CUSTOM_ID_MAX_LENGTH = 100;

    /** The maximum number of options a radio group can have. ({@value}) */
    int OPTIONS_MAX_AMOUNT = 10;

    @Nonnull
    @Override
    RadioGroup withUniqueId(int uniqueId);

    @Nonnull
    @Override
    String getCustomId();

    /**
     * Returns an immutable list of the options available for this radio group.
     *
     * @return Immutable list of this radio group's options
     */
    @Nonnull
    @Unmodifiable
    List<RadioGroupOption> getOptions();

    /**
     * Whether this radio group requires an option to be selected.
     *
     * @return {@code true} if an option must be selected by the user
     */
    boolean isRequired();

    /**
     * Creates a new radio group builder with the provided custom ID.
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
     * Creates a new preconfigured {@link RadioGroup.Builder} with the same settings used for this radio group.
     * <br>This can be useful to create an updated version of this radio group without needing to rebuild it from scratch.
     *
     * @return The {@link RadioGroup.Builder} used to create the radio group
     */
    @Nonnull
    default Builder createCopy() {
        return create(getCustomId())
                .setUniqueId(getUniqueId())
                .addOptions(getOptions())
                .setRequired(isRequired());
    }

    /**
     * Builder of {@link RadioGroup}
     *
     * @see RadioGroup#create(String)
     */
    class Builder {
        protected int uniqueId = -1;
        protected String customId;
        protected final List<RadioGroupOption> options = new ArrayList<>();
        protected boolean required = true;

        protected Builder(@Nonnull String customId) {
            this.customId = customId;
        }

        /**
         * The unique ID of this radio group, or {@code -1} if it isn't set.
         *
         * @return The unique ID, or {@code -1}
         */
        public int getUniqueId() {
            return uniqueId;
        }

        /**
         * The custom ID of this radio group.
         *
         * @return The custom ID
         */
        @Nonnull
        public String getCustomId() {
            return customId;
        }

        /**
         * Whether this radio group requires an option to be selected.
         *
         * @return {@code true} if an option must be selected by the user
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Sets the unique ID of this radio group.
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
         * Sets the custom ID of this radio group.
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
         * Adds the provided options to this radio group.
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
        public Builder addOptions(@Nonnull RadioGroupOption... options) {
            Checks.noneNull(options, "Options");
            return addOptions(Arrays.asList(options));
        }

        /**
         * Adds the provided options to this radio group.
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
        public Builder addOptions(@Nonnull Collection<? extends RadioGroupOption> options) {
            Checks.noneNull(options, "Options");
            this.options.addAll(options);
            return this;
        }

        /**
         * Adds an option to this radio group.
         *
         * @param  label
         *         The label to be displayed next to the radio button,
         *         up to {@value RadioGroupOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value associated to the option, this is what your bot will receive,
         *         up to {@value RadioGroupOption#VALUE_MAX_LENGTH} characters
         *
         * @throws IllegalArgumentException
         *         If the label or value is {@code null} or blank,
         *         or one of them is longer than allowed
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value) {
            return addOptions(RadioGroupOption.of(label, value));
        }

        /**
         * Adds an option to this radio group.
         *
         * @param  label
         *         The label to be displayed next to the radio button,
         *         up to {@value RadioGroupOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value associated to the option, this is what your bot will receive,
         *         up to {@value RadioGroupOption#VALUE_MAX_LENGTH} characters
         * @param  description
         *         The description of this radio button,
         *         up to {@value RadioGroupOption#DESCRIPTION_MAX_LENGTH} characters
         *
         * @throws IllegalArgumentException
         *         If the label or value is {@code null} or blank, or the description is blank,
         *         or one of them is longer than allowed
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nullable String description) {
            return addOptions(RadioGroupOption.of(label, value, description));
        }

        /**
         * Adds an option to this radio group.
         *
         * @param  label
         *         The label to be displayed next to the radio button,
         *         up to {@value RadioGroupOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value associated to the option, this is what your bot will receive,
         *         up to {@value RadioGroupOption#VALUE_MAX_LENGTH} characters
         * @param  description
         *         The description of this radio button,
         *         up to {@value RadioGroupOption#DESCRIPTION_MAX_LENGTH} characters
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
            return addOptions(RadioGroupOption.of(label, value, description, isDefault));
        }

        /**
         * Returns a modifiable list of radio group options.
         *
         * @return The modifiable list of options
         */
        @Nonnull
        public List<RadioGroupOption> getOptions() {
            return options;
        }

        /**
         * Sets whether this radio group must have an option selected.
         *
         * <p>Radio groups are required by default.
         *
         * @param  required
         *         {@code true} if a value must be selected before submitting
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
         *         The {@linkplain RadioGroupOption#getValue() option values} to select
         *
         * @throws IllegalArgumentException
         *         If {@code null} is provided
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder setSelectedValues(@Nonnull Collection<String> values) {
            Checks.noneNull(values, "Values");
            for (ListIterator<RadioGroupOption> iterator = options.listIterator(); iterator.hasNext(); ) {
                RadioGroupOption option = iterator.next();
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
         *         The {@linkplain RadioGroupOption#getValue() option values} to select
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
        public Builder setSelectedOptions(@Nonnull RadioGroupOption... options) {
            Checks.noneNull(options, "Options");
            return setSelectedValues(
                    Arrays.stream(options).map(RadioGroupOption::getValue).collect(Collectors.toList()));
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
        public Builder setSelectedOptions(@Nonnull Collection<RadioGroupOption> options) {
            Checks.noneNull(options, "Options");
            return setSelectedValues(
                    options.stream().map(RadioGroupOption::getValue).collect(Collectors.toList()));
        }

        /**
         * Builds a new {@link RadioGroup} from the current configuration.
         *
         * @throws IllegalStateException
         *         <ul>
         *             <li>If less than 2 options have been set</li>
         *             <li>If there is more than {@value #OPTIONS_MAX_AMOUNT} options</li>
         *             <li>If there is more than one option selected by default</li>
         *         </ul>
         *
         * @return The new {@link RadioGroup}
         */
        @Nonnull
        public RadioGroup build() {
            if (options.size() < 2) {
                throw new IllegalStateException("Cannot build a radio group with less than 2 options");
            }
            if (options.size() > OPTIONS_MAX_AMOUNT) {
                throw new IllegalStateException(
                        "Cannot build a radio group with more than " + OPTIONS_MAX_AMOUNT + " options");
            }
            checkUniqueDefault();

            return new RadioGroupImpl(uniqueId, customId, options, required);
        }

        private void checkUniqueDefault() {
            List<String> selectedValues = new ArrayList<>();
            for (RadioGroupOption option : options) {
                if (option.isDefault()) {
                    selectedValues.add(option.getValue());
                }
            }

            if (selectedValues.size() > 1) {
                throw new IllegalStateException(
                        "Cannot build a radio group with more than one option selected by default: " + selectedValues);
            }
        }
    }
}
