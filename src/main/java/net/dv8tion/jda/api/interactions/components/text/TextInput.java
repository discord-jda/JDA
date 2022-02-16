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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Discord Text input component
 *
 * Must be used in {@link Modal Modals}!
 */
public interface TextInput extends ActionComponent
{
    /**
     * The minimum amount of characters a TextInput's value can have
     */
    int TEXT_INPUT_MIN_LENGTH = 1;

    /**
     * The maximum amount of characters a TextInput's value can have
     */
    int TEXT_INPUT_MAX_LENGTH = 4000;

    /**
     * The {@link TextInputStyle TextInputStyle} of this TextInput component.
     *
     * @return The style of this TextInput component.
     */
    @Nonnull
    TextInputStyle getStyle();

    /**
     * The custom id of this TextInput component.
     *
     * <p>This is used to uniquely identify the modal. Similar to {@link net.dv8tion.jda.api.interactions.components.buttons.Button Buttons}.
     *
     * @return The custom id of this component.
     */
    @Nonnull
    String getId();

    /**
     * The label of this TextInput component.
     *
     * @return The label of this TextInput component.
     */
    @Nonnull
    String getLabel();

    /**
     * The minimum amount of characters that must be written to submit the Modal.
     *
     * <b>This is -1 if no length has been set!</b>
     *
     * @return The minimum length of this TextInput component or -1
     */
    int getMinLength();

    /**
     * The maximum amount of characters that can be written to submit the Modal.
     *
     * <b>This is -1 if no length has been set!</b>
     *
     * @return The maximum length of this TextInput component or -1
     */
    int getMaxLength();

    /**
     * Whether this TextInput is required to be written to submit the Modal.
     *
     * @return True if this TextInput is required to be used.
     */
    boolean isRequired();

    /**
     * The pre-defined value of this TextInput component.
     *
     * <b>This is null if no pre-defined value has been set!</b>
     *
     * @return The value of this TextInput component or null.
     */
    @Nullable
    String getValue();

    /**
     * The placeholder of this TextInput component.
     *
     * <b>This is null if no placeholder has been set!</b>
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

    @Nonnull
    @Override
    default ActionComponent withDisabled(boolean disabled)
    {
        throw new UnsupportedOperationException("Text Inputs cannot be disabled!");
    }

    /**
     * Creates a new TextInput Builder.
     *
     * @param  id
     *         The custom id
     * @param  label
     *         The label
     * @param  style
     *         The {@link TextInputStyle TextInputStyle}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If either id or label are null or blank</li>
     *             <li>If style is null or UNKNOWN</li>
     *         </ul>
     *
     * @return a new TextInput Builder.
     */
    @Nonnull
    static TextInput.Builder create(@Nonnull String id, @Nonnull String label, @Nonnull TextInputStyle style)
    {
        return new Builder(id, label, style);
    }

    /**
     * Builder for {@link TextInput TextInputs}
     */
    class Builder
    {
        private String id;
        private String label;
        private String value;
        private String placeholder;
        private int minLength = -1;
        private int maxLength = -1;
        private TextInputStyle style;
        private boolean required;

        protected Builder(String id, String label, TextInputStyle style)
        {
            Checks.notBlank(id, "ID");
            Checks.notBlank(label, "Label");
            Checks.notNull(style, "Style");

            Checks.check(style != TextInputStyle.UNKNOWN, "TextInputStyle cannot be UNKNOWN!");

            this.id = id;
            this.label = label;
            this.style = style;
        }

        /**
         * Sets the id for this TextInput
         * <br>This is used to uniquely identify it.
         *
         * @param  id
         *         The id to set
         *
         * @throws IllegalArgumentException
         *         If id is null or blank
         *
         * @return The same Builder for chaining convenience.
         */
        public Builder setId(@Nonnull String id)
        {
            Checks.notBlank(id, "ID");
            this.id = id;
            return this;
        }

        /**
         * Sets the label for this TextInput
         *
         * @param  label
         *         The label to set
         *
         * @throws IllegalArgumentException
         *         If label is null or blank
         *
         * @return The same Builder for chaining convenience.
         */
        public Builder setLabel(@Nonnull String label)
        {
            Checks.notBlank(label, "Label");
            this.label = label;
            return this;
        }

        /**
         * Sets the style for this TextInput
         * <br>Possible values are:
         * <br><ul>
         *     <li>{@link TextInputStyle#SHORT}</li>
         *     <li>{@link TextInputStyle#PARAGRAPH}</li>
         * </ul>
         *
         * @param  style
         *         The style to set
         *
         * @throws IllegalArgumentException
         *         If style is null or UNKNOWN
         *
         * @return The same Builder for chaining convenience.
         */
        public Builder setStyle(TextInputStyle style)
        {
            Checks.notNull(style, "Style");
            Checks.check(style != TextInputStyle.UNKNOWN, "TextInputStyle cannot be UNKNOWN!");
            this.style = style;
            return this;
        }

        /**
         * Sets whether the user is required to write in this TextInput.
         *
         * @param  required 
         *         If this TextInput should be required
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setRequired(boolean required)
        {
            this.required = required;
            return this;
        }

        /**
         * Sets the minimum length of this input field.
         *
         * <b>This has to be between 0 and {@value #TEXT_INPUT_MAX_LENGTH}</b>
         *
         * @param  minLength
         *         The minimum amount of characters that need to be written
         *
         * @throws IllegalArgumentException
         *         If minLength is negative or greater than {@value #TEXT_INPUT_MAX_LENGTH}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMinLength(int minLength)
        {
            Checks.notNegative(minLength, "Minimum length");
            Checks.check(minLength <= TEXT_INPUT_MAX_LENGTH, "Minimum length cannot be longer than %d characters!", TEXT_INPUT_MAX_LENGTH);
            this.minLength = minLength;
            return this;
        }

        /**
         * Sets the maximum length of this input field.
         *
         * <b>This has to be between {@value #TEXT_INPUT_MIN_LENGTH} and {@value #TEXT_INPUT_MAX_LENGTH}</b>
         *
         * @param maxLength The maximum amount of characters that need to be written
         *
         * @throws IllegalArgumentException
         *         If maxLength is smaller than {@value #TEXT_INPUT_MIN_LENGTH} or greater than {@value #TEXT_INPUT_MAX_LENGTH}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMaxLength(int maxLength)
        {
            Checks.check(maxLength >= TEXT_INPUT_MIN_LENGTH, "Maximum length cannot be smaller than " + TEXT_INPUT_MIN_LENGTH + " characters!");
            Checks.check(maxLength <= TEXT_INPUT_MAX_LENGTH, "Maximum length cannot be longer than " + TEXT_INPUT_MAX_LENGTH + " characters!");

            this.maxLength = maxLength;
            return this;
        }

        /**
         * Sets the minimum and maximum required length on this TextInput component
         *
         * @param  min 
         *         Minimum length of the text input
         * @param  max 
         *         Maximum length of the text input

         * @throws IllegalArgumentException
         * <ul>
         *  <li>If min is negative or greater than {@link #TEXT_INPUT_MAX_LENGTH}</li>
         *  <li>If max is smaller than 1, smaller than min or greater than {@link #TEXT_INPUT_MAX_LENGTH}</li>
         * </ul>
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setRequiredRange(int min, int max)
        {
            setMinLength(min);
            setMaxLength(max);
            return this;
        }

        /**
         * Sets a pre-populated text for this TextInput field.
         *
         * @param  value 
         *         Pre-Populated text
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setValue(@Nullable String value)
        {
            this.value = value;
            return this;
        }

        /**
         * Sets a placeholder for this TextInput field.
         * <br>This is a short hint that describes the expected value of the input field.
         *
         * @param  placeholder 
         *         The placeholder
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setPlaceholder(@Nullable String placeholder)
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
        @Nonnull
        public String getId()
        {
            return id;
        }

        /**
         * The label shown above this text input box
         *
         * @return Label for the input
         */
        @Nonnull
        public String getLabel()
        {
            return label;
        }

        /**
         * The {@link TextInputStyle TextInputStyle}
         *
         * @return The TextInputStyle
         */
        @Nonnull
        public TextInputStyle getStyle()
        {
            return style;
        }

        /**
         * The placeholder of this TextInput
         *
         * @return Placeholder
         */
        @Nullable
        public String getPlaceholder()
        {
            return placeholder;
        }

        /**
         * The String value of this Modal
         *
         * @return Value
         */
        @Nullable
        public String getValue()
        {
            return value;
        }

        /**
         * Whether this TextInput is required
         *
         * @return True if this TextInput is required
         * 
         * @see    TextInput#isRequired()
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
        @Nonnull
        public TextInput build()
        {
            if (maxLength < minLength)
                throw new IllegalArgumentException("maxLength cannot be smaller than minLength!");

            return new TextInputImpl(id, style, label, minLength, maxLength, required, value, placeholder);
        }
    }
}
