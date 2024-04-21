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

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.entities.EntityBuilder
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * One of the possible options provided in a [SelectMenu].
 */
class SelectOption @JvmOverloads constructor(
    @Nonnull label: String,
    @Nonnull value: String,
    description: String? = null,
    isDefault: Boolean = false,
    emoji: Emoji? = null
) : SerializableData {
    /**
     * The current option label which would be shown to the user in the client.
     *
     * @return The label
     */
    @get:Nonnull
    val label: String

    /**
     * The current option value which is used to identify the selected options in [SelectMenuInteraction.getValues].
     *
     * @return The option value
     */
    @get:Nonnull
    val value: String

    /**
     * The current description for this option.
     *
     * @return The description
     */
    val description: String?

    /**
     * Whether this option is selected by default
     *
     * @return True, if this option is selected by default
     */
    val isDefault: Boolean

    /**
     * The emoji attached to this option which is shown next to the option in the select menu
     *
     * @return The attached emoji
     */
    val emoji: EmojiUnion?
    /**
     * Creates a new SelectOption instance
     *
     * @param  label
     * The label for the option, up to {@value #LABEL_MAX_LENGTH} characters, as defined by [.LABEL_MAX_LENGTH]
     * @param  value
     * The value for the option used to indicate which option was selected with [SelectMenuInteraction.getValues],
     * up to {@value #VALUE_MAX_LENGTH} characters, as defined by [.VALUE_MAX_LENGTH]
     * @param  description
     * The description explaining the meaning of this option in more detail, up to {@value #DESCRIPTION_MAX_LENGTH} characters, as defined by [.DESCRIPTION_MAX_LENGTH]
     * @param  isDefault
     * Whether this option is selected by default
     * @param  emoji
     * The [Emoji] shown next to this option, or null
     *
     * @throws IllegalArgumentException
     * If an unexpected null is provided, or any of the individual parameter requirements are violated.
     */
    /**
     * Creates a new SelectOption instance
     *
     * @param  label
     * The label for the option, up to {@value #LABEL_MAX_LENGTH} characters, as defined by [.LABEL_MAX_LENGTH]
     * @param  value
     * The value for the option used to indicate which option was selected with [SelectMenuInteraction.getValues],
     * up to {@value #VALUE_MAX_LENGTH} characters, as defined by [.VALUE_MAX_LENGTH]
     *
     * @throws IllegalArgumentException
     * If the null is provided, or any of the individual parameter requirements are violated.
     */
    init {
        Checks.notEmpty(label, "Label")
        Checks.notEmpty(value, "Value")
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label")
        Checks.notLonger(value, VALUE_MAX_LENGTH, "Value")
        if (description != null) Checks.notLonger(description, DESCRIPTION_MAX_LENGTH, "Description")
        this.label = label
        this.value = value
        this.description = description
        this.isDefault = isDefault
        this.emoji = emoji as EmojiUnion?
    }

    /**
     * Returns a copy of this select option with the changed label.
     *
     * @param  label
     * The label for the option, up to {@value #LABEL_MAX_LENGTH} characters, as defined by [.LABEL_MAX_LENGTH]
     *
     * @throws IllegalArgumentException
     * If the label is null, empty, or longer than {@value #LABEL_MAX_LENGTH} characters
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    fun withLabel(@Nonnull label: String): SelectOption {
        return SelectOption(label, value, description, isDefault, emoji)
    }

    /**
     * Returns a copy of this select option with the changed value.
     *
     * @param  value
     * The value for the option used to indicate which option was selected with [SelectMenuInteraction.getValues],
     * up to {@value #VALUE_MAX_LENGTH} characters, as defined by [.VALUE_MAX_LENGTH]
     *
     * @throws IllegalArgumentException
     * If the label is null, empty, or longer than {@value #VALUE_MAX_LENGTH} characters
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    fun withValue(@Nonnull value: String): SelectOption {
        return SelectOption(label, value, description, isDefault, emoji)
    }

    /**
     * Returns a copy of this select option with the changed description of this option.
     * <br></br>Default: `null`
     *
     * @param  description
     * The new description or null to have no description,
     * up to {@value #DESCRIPTION_MAX_LENGTH} characters, as defined by [.DESCRIPTION_MAX_LENGTH]
     *
     * @throws IllegalArgumentException
     * If the provided description is longer than {@value #DESCRIPTION_MAX_LENGTH} characters
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    fun withDescription(description: String?): SelectOption {
        return SelectOption(label, value, description, isDefault, emoji)
    }

    /**
     * Returns a copy of this select option with the changed default.
     * <br></br>Default: `false`
     *
     * @param  isDefault
     * Whether this option is selected by default
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    fun withDefault(isDefault: Boolean): SelectOption {
        return SelectOption(label, value, description, isDefault, emoji)
    }

    /**
     * Returns a copy of this select option with the changed emoji.
     * <br></br>Default: `null`
     *
     * @param  emoji
     * The [Emoji] shown next to this option, or null
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    fun withEmoji(emoji: Emoji?): SelectOption {
        return SelectOption(label, value, description, isDefault, emoji)
    }

    @Nonnull
    override fun toData(): DataObject {
        val `object` = DataObject.empty()
        `object`.put("label", label)
        `object`.put("value", value)
        `object`.put("default", isDefault)
        if (emoji != null) `object`.put("emoji", emoji)
        if (description != null && !description.isEmpty()) `object`.put("description", description)
        return `object`
    }

    companion object {
        /**
         * The maximum length a select option label can have
         */
        const val LABEL_MAX_LENGTH = 100

        /**
         * The maximum length a select option value can have
         */
        const val VALUE_MAX_LENGTH = 100

        /**
         * The maximum length a select option description can have
         */
        const val DESCRIPTION_MAX_LENGTH = 100

        /**
         * Creates a new SelectOption instance.
         * <br></br>You can further configure this with the various setters that return new instances.
         *
         * @param  label
         * The label for the option, up to {@value #LABEL_MAX_LENGTH} characters, as defined by [.LABEL_MAX_LENGTH]
         * @param  value
         * The value for the option used to indicate which option was selected with [SelectMenuInteraction.getValues],
         * up to {@value #VALUE_MAX_LENGTH} characters, as defined by [.VALUE_MAX_LENGTH]
         *
         * @throws IllegalArgumentException
         * If null is provided, or any of the individual parameter requirements are violated.
         *
         * @return The new select option instance
         */
        @Nonnull
        @CheckReturnValue
        fun of(@Nonnull label: String, @Nonnull value: String): SelectOption {
            return SelectOption(label, value)
        }

        /**
         * Inverse function for [.toData] which parses the serialized option data
         *
         * @param  data
         * The serialized option data
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the data representation is invalid
         * @throws IllegalArgumentException
         * If some part of the data has an invalid length or null is provided
         *
         * @return The parsed SelectOption instance
         */
        @JvmStatic
        @Nonnull
        @CheckReturnValue
        fun fromData(@Nonnull data: DataObject): SelectOption {
            Checks.notNull(data, "DataObject")
            return SelectOption(
                data.getString("label"),
                data.getString("value"),
                data.getString("description", null),
                data.getBoolean("default", false),
                data.optObject("emoji").map { emoji: DataObject? -> EntityBuilder.createEmoji(emoji) }
                    .orElse(null)
            )
        }
    }
}
