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

package net.dv8tion.jda.api.components.checkbox;

import net.dv8tion.jda.api.components.attribute.ICustomId;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.internal.components.checkbox.CheckboxImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A component displaying a box which can be checked. Useful for simple yes/no questions.
 *
 * <p>Must be used inside {@link net.dv8tion.jda.api.components.label.Label Labels} only!
 *
 * @see #create(String)
 * @see #create(String, boolean)
 */
public interface Checkbox extends ICustomId, LabelChildComponent {
    /**
     * The maximum length a checkbox's custom ID. ({@value})
     */
    int CUSTOM_ID_MAX_LENGTH = 100;

    @Nonnull
    @Override
    @CheckReturnValue
    Checkbox withUniqueId(int uniqueId);

    /**
     * Creates a new instance with the provided custom ID.
     * <br>This is typically used to carry data between the modal creator and the modal handler.
     *
     * @param  customId
     *         The custom ID to use
     *
     * @throws IllegalArgumentException
     *         If the provided custom ID is {@code null}, blank, or longer than {@value #CUSTOM_ID_MAX_LENGTH} characters
     *
     * @return The new, updated instance
     */
    @Nonnull
    @CheckReturnValue
    Checkbox withCustomId(@Nonnull String customId);

    /**
     * Creates a new instance with the provided default selection state.
     *
     * <p>Checkboxes are unchecked by default.
     *
     * @param  isDefault
     *         The new default selection state
     *
     * @return The new, updated instance
     */
    @Nonnull
    @CheckReturnValue
    Checkbox withDefault(boolean isDefault);

    @Nonnull
    @Override
    String getCustomId();

    /**
     * Whether this checkbox is selected by default.
     *
     * @return {@code true} if this is checked by default
     */
    boolean isDefault();

    /**
     * Creates a new checkbox with the provided custom ID, unselected by default.
     *
     * @param customId
     *        The custom ID
     *
     * @throws IllegalArgumentException
     *         If the provided custom ID is {@code null}, blank or longer than {@value #CUSTOM_ID_MAX_LENGTH} characters
     *
     * @return The new instance
     */
    static Checkbox create(@Nonnull String customId) {
        Checks.notBlank(customId, "Custom ID");
        Checks.notLonger(customId, CUSTOM_ID_MAX_LENGTH, "Custom ID");
        return new CheckboxImpl(-1, customId, false);
    }

    /**
     * Creates a new checkbox with the provided custom ID and selected state.
     *
     * @param  customId
     *         The custom ID
     * @param  isDefault
     *         Whether this checkbox will be selected by default
     *
     * @throws IllegalArgumentException
     *         If the provided custom ID is {@code null}, blank or longer than {@value #CUSTOM_ID_MAX_LENGTH} characters
     *
     * @return The new instance
     */
    static Checkbox create(@Nonnull String customId, boolean isDefault) {
        Checks.notBlank(customId, "Custom ID");
        Checks.notLonger(customId, CUSTOM_ID_MAX_LENGTH, "Custom ID");
        return new CheckboxImpl(-1, customId, isDefault);
    }
}
