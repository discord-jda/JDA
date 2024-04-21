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
package net.dv8tion.jda.api.interactions.components.text

import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.internal.interactions.component.TextInputImpl
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Represents a Discord Text input component
 *
 *
 * Must be used in [Modals][Modal]!
 */
interface TextInput : ActionComponent {
    @get:Nonnull
    val style: TextInputStyle?

    /**
     * The custom id of this TextInput component.
     *
     *
     * This is used to uniquely identify the TextInput. Similar to [Buttons][net.dv8tion.jda.api.interactions.components.buttons.Button].
     *
     * @return The custom id of this component.
     */
    @Nonnull
    override fun getId(): String?

    @get:Nonnull
    val label: String?

    /**
     * The minimum amount of characters that must be written to submit the Modal.
     *
     *
     * **This is -1 if no length has been set!**
     *
     * @return The minimum length of this TextInput component or -1
     */
    val minLength: Int

    /**
     * The maximum amount of characters that can be written to submit the Modal.
     *
     *
     * **This is -1 if no length has been set!**
     *
     * @return The maximum length of this TextInput component or -1
     */
    val maxLength: Int

    /**
     * Whether this TextInput is required to be non-empty
     *
     * @return True if this TextInput is required to be used.
     */
    val isRequired: Boolean

    /**
     * The pre-defined value of this TextInput component.
     * <br></br>If this is not null, sending a Modal with this component will pre-populate the field with this String.
     *
     *
     * **This is null if no pre-defined value has been set!**
     *
     * @return The value of this TextInput component or null.
     */
    val value: String?

    /**
     * The placeholder of this TextInput component.
     * <br></br>This is a short hint that describes the expected value of the TextInput field.
     *
     *
     * **This is null if no placeholder has been set!**
     *
     * @return The placeholder of this TextInput component or null.
     */
    val placeHolder: String?
    override fun isDisabled(): Boolean {
        return false
    }

    @Nonnull
    override fun withDisabled(disabled: Boolean): ActionComponent {
        throw UnsupportedOperationException("TextInputs cannot be disabled!")
    }

    @Nonnull
    override fun getType(): Component.Type {
        return Component.Type.TEXT_INPUT
    }

    /**
     * Builder for [TextInputs][TextInput]
     */
    class Builder(id: String?, label: String?, style: TextInputStyle) {
        /**
         * The custom id
         *
         * @return Custom id
         */
        @get:Nonnull
        var id: String? = null
            private set

        /**
         * The label shown above this text input box
         *
         * @return Label for the input
         */
        @get:Nonnull
        var label: String? = null
            private set

        /**
         * The String value of this TextInput
         *
         * @return Value
         */
        var value: String? = null
            private set

        /**
         * The placeholder of this TextInput
         * <br></br>This is the short hint that describes the expected value of the TextInput field.
         *
         * @return Placeholder
         */
        var placeholder: String? = null
            private set

        /**
         * The minimum length. This is -1 if none has been set.
         *
         * @return Minimum length or -1
         */
        var minLength = -1
            private set

        /**
         * The maximum length. This is -1 if none has been set.
         *
         * @return Maximum length or -1
         */
        var maxLength = -1
            private set

        /**
         * The [TextInputStyle]
         *
         * @return The TextInputStyle
         */
        @get:Nonnull
        var style: TextInputStyle? = null
            private set

        /**
         * Whether this TextInput is required.
         * <br></br>If this is True, the user must populate this TextInput field before they can submit the Modal.
         *
         * @return True if this TextInput is required
         *
         * @see TextInput.isRequired
         */
        var isRequired = true
            private set

        init {
            setId(id)
            setLabel(label)
            setStyle(style)
        }

        /**
         * Sets the id for this TextInput
         * <br></br>This is used to uniquely identify it.
         *
         * @param  id
         * The id to set
         *
         * @throws IllegalArgumentException
         *
         *  * If id is null or blank
         *  * If id is longer than {@value #MAX_ID_LENGTH} characters
         *
         *
         * @return The same Builder for chaining convenience.
         */
        @Nonnull
        fun setId(@Nonnull id: String?): Builder {
            Checks.notBlank(id, "ID")
            Checks.notLonger(id, MAX_ID_LENGTH, "ID")
            this.id = id
            return this
        }

        /**
         * Sets the label for this TextInput
         *
         * @param  label
         * The label to set
         *
         * @throws IllegalArgumentException
         *
         *  * If label is null or blank
         *  * If label is longer than {@value #MAX_LABEL_LENGTH} characters
         *
         *
         * @return The same Builder for chaining convenience.
         */
        @Nonnull
        fun setLabel(@Nonnull label: String?): Builder {
            Checks.notBlank(label, "Label")
            Checks.notLonger(label, MAX_LABEL_LENGTH, "Label")
            this.label = label
            return this
        }

        /**
         * Sets the style for this TextInput
         * <br></br>Possible values are:
         *
         *  * [TextInputStyle.SHORT]
         *  * [TextInputStyle.PARAGRAPH]
         *
         *
         * @param  style
         * The style to set
         *
         * @throws IllegalArgumentException
         * If style is null or [UNKNOWN][TextInputStyle.UNKNOWN]
         *
         * @return The same Builder for chaining convenience.
         */
        @Nonnull
        fun setStyle(style: TextInputStyle): Builder {
            Checks.notNull(style, "Style")
            Checks.check(style != TextInputStyle.UNKNOWN, "TextInputStyle cannot be UNKNOWN!")
            this.style = style
            return this
        }

        /**
         * Sets whether the user is required to write in this TextInput. Default is true.
         *
         * @param  required
         * If this TextInput should be required
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setRequired(required: Boolean): Builder {
            isRequired = required
            return this
        }

        /**
         * Sets the minimum length of this input field. Default is -1 (No minimum length).
         *
         *
         * **This has to be between 0 and {@value #MAX_VALUE_LENGTH}, or -1 for no minimum length**
         *
         * @param  minLength
         * The minimum amount of characters that need to be written, or -1
         *
         * @throws IllegalArgumentException
         * If minLength is not -1 and is negative or greater than {@value #MAX_VALUE_LENGTH}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setMinLength(minLength: Int): Builder {
            if (minLength != -1) {
                Checks.notNegative(minLength, "Minimum length")
                Checks.check(
                    minLength <= MAX_VALUE_LENGTH,
                    "Minimum length cannot be longer than %d characters!",
                    MAX_VALUE_LENGTH
                )
            }
            this.minLength = minLength
            return this
        }

        /**
         * Sets the maximum length of this input field. Default is -1 (No maximum length).
         *
         *
         * **This has to be between 1 and {@value #MAX_VALUE_LENGTH}, or -1 for no maximum length**
         *
         * @param  maxLength
         * The maximum amount of characters that need to be written, or -1
         *
         * @throws IllegalArgumentException
         * If maxLength is not -1 and is smaller than 1 or greater than {@value #MAX_VALUE_LENGTH}
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setMaxLength(maxLength: Int): Builder {
            if (maxLength != -1) {
                Checks.check(maxLength >= 1, "Maximum length cannot be smaller than 1 character!")
                Checks.check(
                    maxLength <= MAX_VALUE_LENGTH,
                    "Maximum length cannot be longer than %d characters!",
                    MAX_VALUE_LENGTH
                )
            }
            this.maxLength = maxLength
            return this
        }

        /**
         * Sets the minimum and maximum required length on this TextInput component
         *
         * @param  min
         * Minimum length of the text input, or -1 for none
         * @param  max
         * Maximum length of the text input, or -1 for none
         *
         * @throws IllegalArgumentException
         *
         *  * If min is not -1 and is negative or greater than [.MAX_VALUE_LENGTH]
         *  * If max is not -1 and is smaller than 1, smaller than min or greater than [.MAX_VALUE_LENGTH]
         *
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setRequiredRange(min: Int, max: Int): Builder {
            require(!(min != -1 && max != -1 && min > max)) { "minimum cannot be greater than maximum!" }
            setMinLength(min)
            setMaxLength(max)
            return this
        }

        /**
         * Sets a pre-populated text for this TextInput field.
         * <br></br>If this is not null, sending a Modal with this component will pre-populate the TextInput field with the specified String.
         *
         * @param  value
         * Pre-Populated text
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setValue(value: String?): Builder {
            if (value != null) {
                Checks.notLonger(value, MAX_VALUE_LENGTH, "Value")
                Checks.notBlank(value, "Value")
            }
            this.value = value
            return this
        }

        /**
         * Sets a placeholder for this TextInput field.
         * <br></br>This is a short hint that describes the expected value of the input field.
         *
         * @param  placeholder
         * The placeholder
         *
         * @throws IllegalArgumentException
         * If the provided placeholder is longer than [.MAX_PLACEHOLDER_LENGTH] characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setPlaceholder(placeholder: String?): Builder {
            if (placeholder != null) {
                Checks.notLonger(placeholder, MAX_PLACEHOLDER_LENGTH, "Placeholder")
                Checks.notBlank(placeholder, "Placeholder")
            }
            this.placeholder = placeholder
            return this
        }

        /**
         * Builds a new TextInput from this Builder
         *
         * @throws IllegalStateException
         * If maxLength is smaller than minLength
         *
         * @return the TextInput instance
         */
        @Nonnull
        fun build(): TextInput {
            check(!(maxLength < minLength && maxLength != -1)) { "maxLength cannot be smaller than minLength!" }
            return TextInputImpl(id, style, label, minLength, maxLength, isRequired, value, placeholder)
        }
    }

    companion object {
        /**
         * Creates a new TextInput Builder.
         *
         * @param  id
         * The custom id
         * @param  label
         * The label
         * @param  style
         * The [TextInputStyle]
         *
         * @throws IllegalArgumentException
         *
         *  * If either id or label are null or blank
         *  * If style is null or [UNKNOWN][TextInputStyle.UNKNOWN]
         *  * If id is longer than {@value #MAX_ID_LENGTH} characters
         *  * If label is longer than {@value #MAX_LABEL_LENGTH} characters
         *
         *
         * @return a new TextInput Builder.
         */
        @Nonnull
        fun create(@Nonnull id: String?, @Nonnull label: String?, @Nonnull style: TextInputStyle): Builder? {
            return Builder(id, label, style)
        }

        /**
         * The maximum length a TextInput value can have. ({@value})
         */
        const val MAX_VALUE_LENGTH = 4000

        /**
         * The maximum length a TextInput custom id can have. ({@value})
         */
        const val MAX_ID_LENGTH = 100

        /**
         * The maximum length a TextInput placeholder can have. ({@value})
         */
        const val MAX_PLACEHOLDER_LENGTH = 100

        /**
         * The maximum length a TextInput label can have. ({@value})
         */
        const val MAX_LABEL_LENGTH = 45
    }
}
