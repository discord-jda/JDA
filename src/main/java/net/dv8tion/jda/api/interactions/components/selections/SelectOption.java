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
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * One of the possible options provided in a {@link SelectMenu}.
 */
public class SelectOption implements SerializableData
{
    /**
     * The maximum length a select option label can have
     */
    public static final int LABEL_MAX_LENGTH = 100;

    /**
     * The maximum length a select option value can have
     */
    public static final int VALUE_MAX_LENGTH = 100;

    /**
     * The maximum length a select option description can have
     */
    public static final int DESCRIPTION_MAX_LENGTH = 100;

    private final String label, value;
    private final String description;
    private final boolean isDefault;
    private final Emoji emoji;

    /**
     * Creates a new SelectOption instance
     *
     * @param  label
     *         The label for the option, up to {@value #LABEL_MAX_LENGTH} characters, as defined by {@link #LABEL_MAX_LENGTH}
     * @param  value
     *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
     *         up to {@value #VALUE_MAX_LENGTH} characters, as defined by {@link #VALUE_MAX_LENGTH}
     *
     * @throws IllegalArgumentException
     *         If the null is provided, or any of the individual parameter requirements are violated.
     */
    protected SelectOption(@Nonnull String label, @Nonnull String value)
    {
        this(label, value, null, false, null);
    }

    /**
     * Creates a new SelectOption instance
     *
     * @param  label
     *         The label for the option, up to {@value #LABEL_MAX_LENGTH} characters, as defined by {@link #LABEL_MAX_LENGTH}
     * @param  value
     *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
     *         up to {@value #VALUE_MAX_LENGTH} characters, as defined by {@link #VALUE_MAX_LENGTH}
     * @param  description
     *         The description explaining the meaning of this option in more detail, up to {@value #DESCRIPTION_MAX_LENGTH} characters, as defined by {@link #DESCRIPTION_MAX_LENGTH}
     * @param  isDefault
     *         Whether this option is selected by default
     * @param  emoji
     *         The {@link Emoji} shown next to this option, or null
     *
     * @throws IllegalArgumentException
     *         If the an invalid null is provided, or any of the individual parameter requirements are violated.
     */
    protected SelectOption(@Nonnull String label, @Nonnull String value, @Nullable String description, boolean isDefault, @Nullable Emoji emoji)
    {
        Checks.notEmpty(label, "Label");
        Checks.notEmpty(value, "Value");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        Checks.notLonger(value, VALUE_MAX_LENGTH, "Value");
        if (description != null)
            Checks.notLonger(description, DESCRIPTION_MAX_LENGTH, "Description");
        this.label = label;
        this.value = value;
        this.description = description;
        this.isDefault = isDefault;
        this.emoji = emoji;
    }

    /**
     * Creates a new SelectOption instance.
     * <br>You can further configure this with the various setters that return new instances.
     *
     * @param  label
     *         The label for the option, up to {@value #LABEL_MAX_LENGTH} characters, as defined by {@link #LABEL_MAX_LENGTH}
     * @param  value
     *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
     *         up to {@value #VALUE_MAX_LENGTH} characters, as defined by {@link #VALUE_MAX_LENGTH}
     *
     * @throws IllegalArgumentException
     *         If the null is provided, or any of the individual parameter requirements are violated.
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    public static SelectOption of(@Nonnull String label, @Nonnull String value)
    {
        return new SelectOption(label, value);
    }

    /**
     * Returns a copy of this select option with the changed label.
     *
     * @param  label
     *         The label for the option, up to {@value #LABEL_MAX_LENGTH} characters, as defined by {@link #LABEL_MAX_LENGTH}
     *
     * @throws IllegalArgumentException
     *         If the label is null, empty, or longer than {@value #LABEL_MAX_LENGTH} characters
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    public SelectOption withLabel(@Nonnull String label)
    {
        return new SelectOption(label, value, description, isDefault, emoji);
    }

    /**
     * Returns a copy of this select option with the changed value.
     *
     * @param  value
     *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
     *         up to {@value #VALUE_MAX_LENGTH} characters, as defined by {@link #VALUE_MAX_LENGTH}
     *
     * @throws IllegalArgumentException
     *         If the label is null, empty, or longer than {@value #VALUE_MAX_LENGTH} characters
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    public SelectOption withValue(@Nonnull String value)
    {
        return new SelectOption(label, value, description, isDefault, emoji);
    }

    /**
     * Returns a copy of this select option with the changed description of this option.
     * <br>Default: {@code null}
     *
     * @param  description
     *         The new description or null to have no description,
     *         up to {@value #DESCRIPTION_MAX_LENGTH} characters, as defined by {@link #DESCRIPTION_MAX_LENGTH}
     *
     * @throws IllegalArgumentException
     *         If the provided description is longer than {@value #DESCRIPTION_MAX_LENGTH} characters
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    public SelectOption withDescription(@Nullable String description)
    {
        return new SelectOption(label, value, description, isDefault, emoji);
    }

    /**
     * Returns a copy of this select option with the changed default.
     * <br>Default: {@code false}
     *
     * @param  isDefault
     *         Whether this option is selected by default
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    public SelectOption withDefault(boolean isDefault)
    {
        return new SelectOption(label, value, description, isDefault, emoji);
    }

    /**
     * Returns a copy of this select option with the changed emoji.
     * <br>Default: {@code null}
     *
     * @param  emoji
     *         The {@link Emoji} shown next to this option, or null
     *
     * @return The new select option instance
     */
    @Nonnull
    @CheckReturnValue
    public SelectOption withEmoji(@Nullable Emoji emoji)
    {
        return new SelectOption(label, value, description, isDefault, emoji);
    }

    /**
     * The current option label which would be shown to the user in the client.
     *
     * @return The label
     */
    @Nonnull
    public String getLabel()
    {
        return label;
    }

    /**
     * The current option value which is used to identify the selected options in {@link SelectMenuInteraction#getValues()}.
     *
     * @return The option value
     */
    @Nonnull
    public String getValue()
    {
        return value;
    }

    /**
     * The current description for this option.
     *
     * @return The description
     */
    @Nullable
    public String getDescription()
    {
        return description;
    }

    /**
     * Whether this option is selected by default
     *
     * @return True, if this option is selected by default
     */
    public boolean isDefault()
    {
        return isDefault;
    }

    /**
     * The emoji attached to this option which is shown next to the option in the select menu
     *
     * @return The attached emoji
     */
    @Nullable
    public Emoji getEmoji()
    {
        return emoji;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject object = DataObject.empty();
        object.put("label", label);
        object.put("value", value);
        object.put("default", isDefault);
        if (emoji != null)
            object.put("emoji", emoji);
        if (description != null && !description.isEmpty())
            object.put("description", description);
        return object;
    }

    /**
     * Inverse function for {@link #toData()} which parses the serialized option data
     *
     * @param  data
     *         The serialized option data
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the data representation is invalid
     * @throws IllegalArgumentException
     *         If some part of the data has an invalid length or null is provided
     *
     * @return The parsed SelectOption instance
     */
    @Nonnull
    @CheckReturnValue
    public static SelectOption fromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "DataObject");
        return new SelectOption(
            data.getString("label"),
            data.getString("value"),
            data.getString("description", null),
            data.getBoolean("default", false),
            data.optObject("emoji").map(Emoji::fromData).orElse(null)
        );
    }
}
