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

package net.dv8tion.jda.api.components.textinput;

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.components.textinput.TextInputImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Discord Text input component
 *
 * <p>Must be used in {@link Modal Modals}!
 */
public interface TextInput extends ActionComponent, ActionRowChildComponent
{
    /**
     * The maximum length a TextInput value can have. ({@value})
     */
    int MAX_VALUE_LENGTH = 4000;

    /**
     * The maximum length a TextInput custom id can have. ({@value})
     */
    int MAX_ID_LENGTH = 100;

    /**
     * The maximum length a TextInput placeholder can have. ({@value})
     */
    int MAX_PLACEHOLDER_LENGTH = 100;

    /**
     * The maximum length a TextInput label can have. ({@value})
     */
    int MAX_LABEL_LENGTH = 45;

    /**
     * The {@link TextInputStyle} of this TextInput component.
     *
     * @return The style of this TextInput component.
     */
    @Nonnull
    TextInputStyle getStyle();

    /**
     * The custom id of this TextInput component.
     *
     * <p>This is used to uniquely identify the TextInput. Similar to {@link net.dv8tion.jda.api.components.buttons.Button Buttons}.
     *
     * @return The custom id of this component.
     */
    @Nonnull
    @Override
    String getCustomId();

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
     * <p><b>This is -1 if no length has been set!</b>
     *
     * @return The minimum length of this TextInput component or -1
     */
    int getMinLength();

    /**
     * The maximum amount of characters that can be written to submit the Modal.
     *
     * <p><b>This is -1 if no length has been set!</b>
     *
     * @return The maximum length of this TextInput component or -1
     */
    int getMaxLength();

    /**
     * Whether this TextInput is required to be non-empty
     *
     * @return True if this TextInput is required to be used.
     */
    boolean isRequired();

    /**
     * The pre-defined value of this TextInput component.
     * <br>If this is not null, sending a Modal with this component will pre-populate the field with this String.
     *
     * <p><b>This is null if no pre-defined value has been set!</b>
     *
     * @return The value of this TextInput component or null.
     */
    @Nullable
    String getValue();

    /**
     * The placeholder of this TextInput component.
     * <br>This is a short hint that describes the expected value of the TextInput field.
     *
     * <p><b>This is null if no placeholder has been set!</b>
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
        throw new UnsupportedOperationException("TextInputs cannot be disabled!");
    }

    @Nonnull
    @Override
    TextInput withUniqueId(int uniqueId);

    @Nonnull
    @Override
    default Type getType()
    {
        return Type.TEXT_INPUT;
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
     *             <li>If style is null or {@link TextInputStyle#UNKNOWN UNKNOWN}</li>
     *             <li>If id is longer than {@value #MAX_ID_LENGTH} characters</li>
     *             <li>If label is longer than {@value #MAX_LABEL_LENGTH} characters</li>
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
        private int uniqueId = -1;
        private String label;
        private String value;
        private String placeholder;
        private int minLength = -1;
        private int maxLength = -1;
        private TextInputStyle style;
        private boolean required = true;

        protected Builder(String id, String label, TextInputStyle style)
        {
            setId(id);
            setLabel(label);
            setStyle(style);
        }

        /**
         * Sets the id for this TextInput
         * <br>This can be used to uniquely identify it, or pass data to other handlers.
         *
         * @param  id
         *         The id to set
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If id is null or blank</li>
         *             <li>If id is longer than {@value #MAX_ID_LENGTH} characters</li>
         *         </ul>
         *
         * @return The same Builder for chaining convenience.
         */
        @Nonnull
        public Builder setId(@Nonnull String id)
        {
            Checks.notBlank(id, "ID");
            Checks.notLonger(id, MAX_ID_LENGTH, "ID");
            this.id = id;
            return this;
        }

        /**
         * Sets the unique ID for this TextInput
         * <br>This is used to uniquely identify it.
         *
         * @param  uniqueId
         *         The unique id to set
         *
         * @throws IllegalArgumentException
         *         If the id is negative
         *
         * @return The same Builder for chaining convenience.
         */
        @Nonnull
        public Builder setUniqueId(int uniqueId)
        {
            Checks.positive(uniqueId, "Unique ID");
            this.uniqueId = uniqueId;
            return this;
        }

        /**
         * Sets the label for this TextInput
         *
         * @param  label
         *         The label to set
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If label is null or blank</li>
         *             <li>If label is longer than {@value #MAX_LABEL_LENGTH} characters</li>
         *         </ul>
         *
         * @return The same Builder for chaining convenience.
         */
        @Nonnull
        public Builder setLabel(@Nonnull String label)
        {
            Checks.notBlank(label, "Label");
            Checks.notLonger(label, MAX_LABEL_LENGTH, "Label");
            this.label = label;
            return this;
        }

        /**
         * Sets the style for this TextInput
         * <br>Possible values are:
         * <ul>
         *     <li>{@link TextInputStyle#SHORT}</li>
         *     <li>{@link TextInputStyle#PARAGRAPH}</li>
         * </ul>
         *
         * @param  style
         *         The style to set
         *
         * @throws IllegalArgumentException
         *         If style is null or {@link TextInputStyle#UNKNOWN UNKNOWN}
         *
         * @return The same Builder for chaining convenience.
         */
        @Nonnull
        public Builder setStyle(TextInputStyle style)
        {
            Checks.notNull(style, "Style");
            Checks.check(style != TextInputStyle.UNKNOWN, "TextInputStyle cannot be UNKNOWN!");
            this.style = style;
            return this;
        }

        /**
         * Sets whether the user is required to write in this TextInput. Default is true.
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
         * Sets the minimum length of this input field. Default is -1 (No minimum length).
         *
         * <p><b>This has to be between 0 and {@value #MAX_VALUE_LENGTH}, or -1 for no minimum length</b>
         *
         * @param  minLength
         *         The minimum amount of characters that need to be written, or -1
         *
         * @throws IllegalArgumentException
         *         If minLength is not -1 and is negative or greater than {@value #MAX_VALUE_LENGTH}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMinLength(int minLength)
        {
            if (minLength != -1)
            {
                Checks.notNegative(minLength, "Minimum length");
                Checks.check(minLength <= MAX_VALUE_LENGTH, "Minimum length cannot be longer than %d characters!", MAX_VALUE_LENGTH);
            }

            this.minLength = minLength;
            return this;
        }

        /**
         * Sets the maximum length of this input field. Default is -1 (No maximum length).
         *
         * <p><b>This has to be between 1 and {@value #MAX_VALUE_LENGTH}, or -1 for no maximum length</b>
         *
         * @param  maxLength 
         *         The maximum amount of characters that need to be written, or -1
         *
         * @throws IllegalArgumentException
         *         If maxLength is not -1 and is smaller than 1 or greater than {@value #MAX_VALUE_LENGTH}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMaxLength(int maxLength)
        {
            if (maxLength != -1)
            {
                Checks.check(maxLength >= 1, "Maximum length cannot be smaller than 1 character!");
                Checks.check(maxLength <= MAX_VALUE_LENGTH, "Maximum length cannot be longer than %d characters!", MAX_VALUE_LENGTH);
            }

            this.maxLength = maxLength;
            return this;
        }

        /**
         * Sets the minimum and maximum required length on this TextInput component
         *
         * @param  min 
         *         Minimum length of the text input, or -1 for none
         * @param  max 
         *         Maximum length of the text input, or -1 for none

         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If min is not -1 and is negative or greater than {@link #MAX_VALUE_LENGTH}</li>
         *             <li>If max is not -1 and is smaller than 1, smaller than min or greater than {@link #MAX_VALUE_LENGTH}</li>
         *         </ul>
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setRequiredRange(int min, int max)
        {
            if (min != -1 && max != -1 && min > max)
                throw new IllegalArgumentException("minimum cannot be greater than maximum!");

            setMinLength(min);
            setMaxLength(max);
            return this;
        }

        /**
         * Sets a pre-populated text for this TextInput field.
         * <br>If this is not null, sending a Modal with this component will pre-populate the TextInput field with the specified String.
         *
         * @param  value 
         *         Pre-Populated text
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setValue(@Nullable String value)
        {
            if (value != null)
            {
                Checks.notLonger(value, MAX_VALUE_LENGTH, "Value");
                Checks.notBlank(value, "Value");
            }

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
         * @throws IllegalArgumentException
         *         If the provided placeholder is longer than {@link #MAX_PLACEHOLDER_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setPlaceholder(@Nullable String placeholder)
        {
            if (placeholder != null)
            {
                Checks.notLonger(placeholder, MAX_PLACEHOLDER_LENGTH, "Placeholder");
                Checks.notBlank(placeholder, "Placeholder");
            }
            this.placeholder = placeholder;
            return this;
        }

        /**
         * The minimum length. This is -1 if none has been set.
         *
         * @return Minimum length or -1
         */
        public int getMinLength()
        {
            return minLength;
        }

        /**
         * The maximum length. This is -1 if none has been set.
         *
         * @return Maximum length or -1
         */
        public int getMaxLength()
        {
            return maxLength;
        }

        /**
         * The custom id
         *
         * @return Custom id
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
            return id;
        }

        /**
         * The custom id
         *
         * @return Custom id
         */
        @Nonnull
        public String getCustomId()
        {
            return id;
        }

        /**
         * The numeric unique ID
         *
         * @return Unique ID
         */
        public int getUniqueId()
        {
            return uniqueId;
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
         * <br>This is the short hint that describes the expected value of the TextInput field.
         *
         * @return Placeholder
         */
        @Nullable
        public String getPlaceholder()
        {
            return placeholder;
        }

        /**
         * The String value of this TextInput
         *
         * @return Value
         */
        @Nullable
        public String getValue()
        {
            return value;
        }

        /**
         * Whether this TextInput is required.
         * <br>If this is True, the user must populate this TextInput field before they can submit the Modal.
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
         * @throws IllegalStateException
         *         If maxLength is smaller than minLength
         *
         * @return the TextInput instance
         */
        @Nonnull
        public TextInput build()
        {
            if (maxLength < minLength && maxLength != -1)
                throw new IllegalStateException("maxLength cannot be smaller than minLength!");

            return new TextInputImpl(id, uniqueId, style, label, minLength, maxLength, required, value, placeholder);
        }
    }
}
