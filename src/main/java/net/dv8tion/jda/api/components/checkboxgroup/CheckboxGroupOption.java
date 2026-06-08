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

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An option of a {@link CheckboxGroup}.
 */
public final class CheckboxGroupOption implements SerializableData {
    /**
     * The maximum length a checkbox group option label can have.
     */
    public static final int LABEL_MAX_LENGTH = 100;

    /**
     * The maximum length a checkbox group option value can have.
     */
    public static final int VALUE_MAX_LENGTH = 100;

    /**
     * The maximum length a checkbox group option description can have.
     */
    public static final int DESCRIPTION_MAX_LENGTH = 100;

    private final String label;
    private final String value;
    private final String description;
    private final boolean isDefault;

    private CheckboxGroupOption(String label, String value, String description, boolean isDefault) {
        Checks.notBlank(label, "Label");
        Checks.notBlank(value, "Value");
        if (description != null) {
            Checks.notBlank(description, "Description");
        }

        this.label = label;
        this.value = value;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * Creates an unchecked checkbox group option.
     *
     * @param  label
     *         The label to be displayed next to the checkbox,
     *         up to {@value #LABEL_MAX_LENGTH} characters
     * @param  value
     *         The value associated to the option, this is what your bot will receive,
     *         up to {@value #VALUE_MAX_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If the label or value is {@code null} or blank,
     *         or one of them is longer than allowed
     *
     * @return The new option
     */
    @Nonnull
    public static CheckboxGroupOption of(@Nonnull String label, @Nonnull String value) {
        return new CheckboxGroupOption(label, value, null, false);
    }

    /**
     * Creates an unchecked checkbox group option.
     *
     * @param  label
     *         The label to be displayed next to the checkbox,
     *         up to {@value #LABEL_MAX_LENGTH} characters
     * @param  value
     *         The value associated to the option, this is what your bot will receive
     *         up to {@value #VALUE_MAX_LENGTH} characters
     * @param  description
     *         The description of this checkbox,
     *         up to {@value #DESCRIPTION_MAX_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If the label or value is {@code null} or blank, or the description is blank,
     *         or one of them is longer than allowed
     *
     * @return The new option
     */
    @Nonnull
    public static CheckboxGroupOption of(@Nonnull String label, @Nonnull String value, @Nullable String description) {
        return new CheckboxGroupOption(label, value, description, false);
    }

    /**
     * Creates a checkbox group option.
     *
     * @param  label
     *         The label to be displayed next to the checkbox,
     *         up to {@value #LABEL_MAX_LENGTH} characters
     * @param  value
     *         The value associated to the option, this is what your bot will receive,
     *         up to {@value #VALUE_MAX_LENGTH} characters
     * @param  description
     *         The description of this checkbox,
     *         up to {@value #DESCRIPTION_MAX_LENGTH} characters
     * @param  isDefault
     *         Whether this option will be selected by default
     *
     * @throws IllegalArgumentException
     *         If the label or value is {@code null} or blank, or the description is blank,
     *         or one of them is longer than allowed
     *
     * @return The new option
     */
    @Nonnull
    public static CheckboxGroupOption of(
            @Nonnull String label, @Nonnull String value, @Nullable String description, boolean isDefault) {
        return new CheckboxGroupOption(label, value, description, isDefault);
    }

    /**
     * Returns a copy of this checkbox group option with the changed label.
     *
     * @param  label
     *         The label for the option, up to {@value #LABEL_MAX_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If the label is null, empty, or longer than {@value #LABEL_MAX_LENGTH} characters
     *
     * @return The new checkbox group option instance
     */
    @Nonnull
    @CheckReturnValue
    public CheckboxGroupOption withLabel(@Nonnull String label) {
        return new CheckboxGroupOption(label, value, description, isDefault);
    }

    /**
     * Returns a copy of this checkbox group option with the changed value.
     *
     * @param  value
     *         The value associated to the option, this is what your bot will receive,
     *         up to {@value #VALUE_MAX_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If the label is null, empty, or longer than {@value #VALUE_MAX_LENGTH} characters
     *
     * @return The new checkbox group option instance
     */
    @Nonnull
    @CheckReturnValue
    public CheckboxGroupOption withValue(@Nonnull String value) {
        return new CheckboxGroupOption(label, value, description, isDefault);
    }

    /**
     * Returns a copy of this checkbox group option with the changed description of this option.
     *
     * @param  description
     *         The new description or {@code null} to have no description,
     *         up to {@value #DESCRIPTION_MAX_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If the provided description is longer than {@value #DESCRIPTION_MAX_LENGTH} characters
     *
     * @return The new checkbox group option instance
     */
    @Nonnull
    @CheckReturnValue
    public CheckboxGroupOption withDescription(@Nullable String description) {
        return new CheckboxGroupOption(label, value, description, isDefault);
    }

    /**
     * Returns a copy of this checkbox group option with the changed default.
     *
     * @param  isDefault
     *         Whether this option is selected by default
     *
     * @return The new checkbox group option instance
     */
    @Nonnull
    @CheckReturnValue
    public CheckboxGroupOption withDefault(boolean isDefault) {
        return new CheckboxGroupOption(label, value, description, isDefault);
    }

    /**
     * Returns the label displayed next to the checkbox.
     *
     * @return The label
     */
    @Nonnull
    public String getLabel() {
        return label;
    }

    /**
     * Returns the value associated to this checkbox group option.
     *
     * @return The associated value
     */
    @Nonnull
    public String getValue() {
        return value;
    }

    /**
     * Returns the description of this option, or {@code null} if unset.
     *
     * @return The description, or {@code null}
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Whether this option is selected by default.
     *
     * @return {@code true} if this option is selected by default
     */
    public boolean isDefault() {
        return isDefault;
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
     * @return The parsed CheckboxGroupOption instance
     */
    @Nonnull
    @CheckReturnValue
    public static CheckboxGroupOption fromData(@Nonnull DataObject data) {
        Checks.notNull(data, "DataObject");
        return new CheckboxGroupOption(
                data.getString("label"),
                data.getString("value"),
                data.getString("description", null),
                data.getBoolean("default", false));
    }

    @Nonnull
    @Override
    public DataObject toData() {
        DataObject object = DataObject.empty().put("label", label).put("value", value);
        if (description != null) {
            object.put("description", description);
        }
        if (isDefault) {
            object.put("default", true);
        }

        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CheckboxGroupOption)) {
            return false;
        }
        CheckboxGroupOption that = (CheckboxGroupOption) o;
        return label.equals(that.label)
                && value.equals(that.value)
                && Objects.equals(description, that.description)
                && isDefault == that.isDefault;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, value, description, isDefault);
    }

    @Override
    public String toString() {
        return new EntityString(this).addMetadata("value", value).toString();
    }
}
