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

package net.dv8tion.jda.api.interactions.components.text;

import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.internal.interactions.component.TextInputImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Discord Text input component
 *
 * Must be used in {@link Modal Modals}!
 */
public interface TextInput extends ActionComponent
{
    /**
     * The maximum amount of characters a TextInput's minimum length can have
     */
    int TEXT_INPUT_MIN_LENGTH_MAXIMUM = 4000;

    /**
     * The minimum amount of characters a TextInput's maximum length can have
     */
    int TEXT_INPUT_MAX_LENGTH_MINIMUM = 1;

    /**
     * The maximum amount of characters a TextInput's maximum length can have
     */
    int TEXT_INPUT_MAX_LENGTH_MAXIMUM = 4000;

    /**
     * The {@link TextInputStyle TextInputStyle} of this TextInput component.
     *
     * <b>This is null if the TextInput was received from a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent}!</b>
     *
     * @return The style of this TextInput component or null.
     */
    @Nullable
    TextInputStyle getStyle();

    /**
     * The custom id of this TextInput component.
     *
     * This is used to uniquely identify the modal. Similar to {@link net.dv8tion.jda.api.interactions.components.buttons.Button Buttons}.
     *
     * @return The custom id of this component.
     */
    @NotNull
    String getId();

    /**
     * The label of this TextInput component.
     *
     * <b>This is null if the TextInput was received from a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent}!</b>
     *
     * @return The label of this TextInput component or null.
     */
    @Nullable
    String getLabel();

    /**
     * The minimum amount of characters that must be written to submit the Modal.
     *
     * <b>This is -1 if no length has been set or this TextInput was received from a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent}</b>
     *
     * @return The minimum length of this TextInput component or -1
     */
    int getMinLength();

    /**
     * The maximum amount of characters that can be written to submit the Modal.
     *
     * <b>This is -1 if no length has been set or this TextInput was received from a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent}</b>
     *
     * @return The maximum length of this TextInput component or -1
     */
    int getMaxLength();

    /**
     * Whether this TextInput is required to be written to submit the Modal.
     *
     * <b>This returns false if this TextInput was received from a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent}</b>
     *
     * @return True if this TextInput is required to be used.
     */
    boolean isRequired();

    /**
     * The value of this TextInput component.
     * This contains the content of this input field when you received it from a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent}
     *
     * <b>This is null if no pre-defined value has been set</b>
     *
     * @return The value of this TextInput component or null.
     */
    @Nullable
    String getValue();

    /**
     * The placeholder of this TextInput component.
     *
     * <b>This is null if the TextInput was received from a {@link net.dv8tion.jda.api.events.interaction.ModalSubmitInteractionEvent ModalSubmitInteractionEvent}!</b>
     *
     * @return The placeholder of this TextInput component or null.
     */
    @Nullable
    String getPlaceHolder();

    @Override
    default boolean isDisabled()
    {
        return false;
    }

    @NotNull
    @Override
    default ActionComponent withDisabled(boolean disabled)
    {
        throw new UnsupportedOperationException("Text Inputs cannot be disabled!");
    }

    /**
     * Creates a new TextInput Builder.
     *
     * @param id The custom id
     * @param label The label
     * @param style The {@link TextInputStyle TextInputStyle}
     *
     * @return a new TextInput Builder.
     */
    static TextInput.Builder create(@NotNull String id, @NotNull String label, @NotNull TextInputStyle style)
    {
        Checks.notNull(id, "Custom ID");
        Checks.notNull(label, "Label");
        Checks.notNull(style, "Style");
        return new Builder(id, label, style);
    }

    /**
     * Builder for {@link TextInput TextInputs}
     */
    class Builder
    {
        private final String id;
        private final TextInputStyle style;
        private final String label;

        private int minLength = -1;
        private int maxLength = -1;
        private String value;
        private String placeholder;
        private boolean required;

        protected Builder(String id, String label, TextInputStyle style)
        {
            this.id = id;
            this.label = label;
            this.style = style;
        }

        /**
         * Sets whether the user is required to write in this TextInput.
         *
         * @param required If this TextInput should be required
         *
         * @return Builder for chaining convenience
         */
        public Builder setRequired(boolean required)
        {
            this.required = required;
            return this;
        }

        /**
         * Sets the minimum length of this input field.
         *
         * <b>This has to be between 0 and 4000</b>
         *
         * @param minLength The minimum amount of characters that need to be written
         *
         * @throws IllegalArgumentException
         *         If minLength is negative or greater than 4000
         *
         * @return Builder for chaining convenience
         */
        public Builder setMinLength(int minLength)
        {
            Checks.notNegative(minLength, "Minimum length");
            Checks.check(minLength <= TEXT_INPUT_MIN_LENGTH_MAXIMUM, "Minimum length cannot be longer than 4000 characters!");
            this.minLength = minLength;
            return this;
        }

        /**
         * Sets the maximum length of this input field.
         *
         * <b>This has to be between 1 and 4000</b>
         *
         * @param maxLength The maximum amount of characters that need to be written
         *
         * @throws IllegalArgumentException
         *         If maxLength is smaller than 1 or greater than 4000
         *
         * @return Builder for chaining convenience
         */
        public Builder setMaxLength(int maxLength)
        {
            Checks.check(maxLength >= TEXT_INPUT_MAX_LENGTH_MINIMUM, "Maximum length cannot be smaller than 1");
            Checks.check(maxLength <= TEXT_INPUT_MAX_LENGTH_MAXIMUM, "Maximum length cannot be longer than 4000 characters!");
            this.maxLength = maxLength;
            return this;
        }

        /**
         * Sets the required range of characters on this TextInput component
         *
         * @param min minimum
         * @param max maximum
         * @throws IllegalArgumentException
         *         If min is negative or greater than {@link #TEXT_INPUT_MIN_LENGTH_MAXIMUM}
         *         If max is smaller than 1 or greater than {@link #TEXT_INPUT_MAX_LENGTH_MAXIMUM}
         * @return Builder for chaining convenience
         */
        public Builder setRequiredRange(int min, int max)
        {
            setMinLength(min);
            setMaxLength(max);
            return this;
        }

        /**
         * Sets a pre-populated text for this TextInput field.
         *
         * @param value Pre-Populated text
         *
         * @return Builder for chaining convenience
         */
        public Builder setValue(String value)
        {
            this.value = value;
            return this;
        }

        /**
         * Sets a placeholder for this TextInput field.
         *
         * @param placeholder The placeholder
         *
         * @return Builder for chaining convenience
         */
        public Builder setPlaceholder(String placeholder)
        {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * The minimum length
         *
         * @return Minimum length
         */
        public int getMinLength()
        {
            return minLength;
        }

        /**
         * The maximum length
         *
         * @return Maximum length
         */
        public int getMaxLength()
        {
            return maxLength;
        }

        /**
         * The custom id
         *
         * @return Custom id
         */
        public String getId()
        {
            return id;
        }

        /**
         * The label
         * @return Label
         */
        public String getLabel()
        {
            return label;
        }

        /**
         * The {@link TextInputStyle TextInputStyle}
         *
         * @return The TextInputStyle
         */
        public TextInputStyle getStyle()
        {
            return style;
        }

        /**
         * The placeholder
         * @return Placeholder
         */
        public String getPlaceholder()
        {
            return placeholder;
        }

        /**
         * The value
         * @return Value
         */
        public String getValue()
        {
            return value;
        }

        /**
         * Whether this TextInput is required
         *
         * @see TextInput#isRequired()
         *
         * @return True if this TextInput is required
         */
        public boolean isRequired()
        {
            return required;
        }

        /**
         * Builds a new TextInput from this Builder
         *
         * @return the TextInput instance
         */
        public TextInput build()
        {
            return new TextInputImpl(id, style, label, minLength, maxLength, required, value, placeholder);
        }
    }
}
