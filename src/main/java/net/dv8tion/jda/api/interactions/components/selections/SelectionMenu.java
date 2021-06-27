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
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a selection menu in a message.
 * <br>This is an interactive component and usually located within an {@link net.dv8tion.jda.api.interactions.components.ActionRow ActionRow}.
 * One selection menu fills up an entire action row by itself. You cannot have an action row with other components if a selection menu is present in the same row.
 *
 * <p>The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected and they will disappear when the client restarts or the message is reloaded.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * public void onSlashCommand(SlashCommandEvent event) {
 *   if (!event.getName().equals("class")) return;
 *
 *   SelectionMenu menu = SelectionMenu.create("menu:class")
 *     .setPlaceholder("Choose your class") // shows the placeholder indicating what this menu is for
 *     .setRequireRange(1, 1) // only one can be selected
 *     .addOption("mage-arcane", "Arcane Mage")
 *     .addOption("mage-fire", "Fire Mage")
 *     .addOption("mage-frost", "Frost Mage")
 *     .build();
 *
 *   event.reply("Please pick your class below")
 *     .setEphemeral(true)
 *     .addActionRow(menu)
 *     .queue();
 * }
 * }</pre>
 */
public interface SelectionMenu extends Component
{
    /**
     * Placeholder which is displayed when no selections have been made yet.
     *
     * @return The placeholder or null
     */
    @Nullable
    String getPlaceholder();

    /**
     * The minimum amount of values a user has to select.
     *
     * @return The min values
     */
    int getMinValues();

    /**
     * The maximum amount of values a user can select at once.
     *
     * @return The max values
     */
    int getMaxValues();

    /**
     * Up to 25 available options to choose from.
     *
     * @return The {@link SelectOption SelectOptions} this menu provides
     */
    @Nonnull
    List<SelectOption> getOptions();

    /**
     * Creates a new preconfigured {@link Builder} with the same settings used for this selection menu.
     * <br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The {@link Builder} used to create the selection menu
     */
    @Nonnull
    @CheckReturnValue
    default Builder createCopy()
    {
        //noinspection ConstantConditions
        Builder builder = create(getId());
        builder.setRequiredRange(getMinValues(), getMaxValues());
        builder.setPlaceholder(getPlaceholder());
        builder.addOptions(getOptions());
        return builder;
    }

    /**
     * Creates a new {@link Builder} for a selection menu with the provided custom id.
     *
     * @param  customId
     *         The id used to identify this menu with {@link Component#getId()} for component interactions
     *
     * @throws IllegalArgumentException
     *         If the provided id is null, empty, or longer than 100 characters
     *
     * @return The {@link Builder} used to create the selection menu
     */
    @Nonnull
    @CheckReturnValue
    static Builder create(@Nonnull String customId)
    {
        return new Builder(customId);
    }

    /**
     * A preconfigured builder for the creation of selection menus.
     */
    class Builder
    {
        private String customId;
        private String placeholder;
        private int minValues = 1, maxValues = 1;
        private final List<SelectOption> options = new ArrayList<>();

        private Builder(@Nonnull String customId)
        {
            setId(customId);
        }

        /**
         * Change the custom id used to identify the selection menu.
         *
         * @param  customId
         *         The new custom id to use
         *
         * @throws IllegalArgumentException
         *         If the provided id is null, empty, or longer than 100 characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setId(@Nonnull String customId)
        {
            Checks.notEmpty(customId, "Component ID");
            Checks.notLonger(customId, 100, "Component ID");
            this.customId = customId;
            return this;
        }

        /**
         * Configure the placeholder which is displayed when no selections have been made yet.
         *
         * @param  placeholder
         *         The placeholder or null
         *
         * @throws IllegalArgumentException
         *         If the provided placeholder is empty or longer than 100 characters
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setPlaceholder(@Nullable String placeholder)
        {
            if (placeholder != null)
            {
                Checks.notEmpty(placeholder, "Placeholder");
                Checks.notLonger(placeholder, 100, "Placeholder");
            }
            this.placeholder = placeholder;
            return this;
        }

        /**
         * The minimum amount of values a user has to select.
         * <br>Default: {@code 1}
         *
         * <p>The minimum must not exceed the amount of available options.
         *
         * @param  minValues
         *         The min values
         *
         * @throws IllegalArgumentException
         *         If the provided amount is negative
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMinValues(int minValues)
        {
            Checks.notNegative(minValues, "Min Values");
            this.minValues = minValues;
            return this;
        }

        /**
         * The maximum amount of values a user can select.
         * <br>Default: {@code 1}
         *
         * <p>The maximum must not exceed the amount of available options.
         *
         * @param  maxValues
         *         The max values
         *
         * @throws IllegalArgumentException
         *         If the provided amount is less than 1
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setMaxValues(int maxValues)
        {
            Checks.positive(maxValues, "Max Values");
            this.maxValues = maxValues;
            return this;
        }

        /**
         * The minimum and maximum amount of values a user can select.
         * <br>Default: {@code 1} for both
         *
         * <p>The minimum or maximum must not exceed the amount of available options.
         *
         * @param  min
         *         The min values
         * @param  max
         *         The max values
         *
         * @throws IllegalArgumentException
         *         If the provided amount is not a valid range ({@code 0 <= min <= max})
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setRequiredRange(int min, int max)
        {
            Checks.check(min <= max, "Min Values should be less than or equal to Max Values! Provided: [%d, %d]", min, max);
            Checks.notNegative(min, "Min Values");
            Checks.positive(max, "Max Values");
            this.minValues = min;
            this.maxValues = max;
            return this;
        }

        /**
         * Adds up to 25 possible options to this selection menu.
         *
         * @param  options
         *         The {@link SelectOption SelectOptions} to add
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than 25 or null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOptions(@Nonnull SelectOption... options)
        {
            Checks.noneNull(options, "Options");
            Checks.check(this.options.size() + options.length <= 25, "Cannot have more than 25 options for a selection menu!");
            Collections.addAll(this.options, options);
            return this;
        }

        /**
         * Adds up to 25 possible options to this selection menu.
         *
         * @param  options
         *         The {@link SelectOption SelectOptions} to add
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than 25 or null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOptions(@Nonnull Collection<? extends SelectOption> options)
        {
            Checks.noneNull(options, "Options");
            Checks.check(this.options.size() + options.size() <= 25, "Cannot have more than 25 options for a selection menu!");
            this.options.addAll(options);
            return this;
        }

        /**
         * Adds up to 25 possible options to this selection menu.
         *
         * @param  label
         *         The label for the option, up to 25 characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectionMenuInteraction#getValues()}, up to 100 characters
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than 25, invalid null is provided, or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value)
        {
            return addOptions(new SelectOption(label, value));
        }

        /**
         * Adds up to 25 possible options to this selection menu.
         *
         * @param  label
         *         The label for the option, up to 25 characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectionMenuInteraction#getValues()}, up to 100 characters
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than 25, invalid null is provided, or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nonnull Emoji emoji)
        {
            return addOptions(new SelectOption(label, value).setEmoji(emoji));
        }

        /**
         * Adds up to 25 possible options to this selection menu.
         *
         * @param  label
         *         The label for the option, up to 25 characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectionMenuInteraction#getValues()}, up to 100 characters
         * @param  description
         *         The description explaining the meaning of this option in more detail, up to 50 characters
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than 25, invalid null is provided, or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nonnull String description)
        {
            return addOptions(new SelectOption(label, value).setDescription(description));
        }

        /**
         * Adds up to 25 possible options to this selection menu.
         *
         * @param  label
         *         The label for the option, up to 25 characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectionMenuInteraction#getValues()}, up to 100 characters
         * @param  description
         *         The description explaining the meaning of this option in more detail, up to 50 characters
         * @param  emoji
         *         The {@link Emoji} shown next to this option, or null
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than 25, invalid null is provided, or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nullable String description, @Nullable Emoji emoji)
        {
            return addOptions(new SelectOption(label, value)
                .setDescription(description)
                .setEmoji(emoji));
        }

        /**
         * Modifiable list of options currently configured in this builder.
         *
         * @return The list of {@link SelectOption SelectOptions}
         */
        @Nonnull
        public List<SelectOption> getOptions()
        {
            return options;
        }

        /**
         * The custom id used to identify the selection menu.
         *
         * @return The custom id
         */
        @Nonnull
        public String getId()
        {
            return customId;
        }

        /**
         * Placeholder which is displayed when no selections have been made yet.
         *
         * @return The placeholder or null
         */
        @Nullable
        public String getPlaceholder()
        {
            return placeholder;
        }

        /**
         * The minimum amount of values a user has to select.
         *
         * @return The min values
         */
        public int getMinValues()
        {
            return minValues;
        }

        /**
         * The maximum amount of values a user can select at once.
         *
         * @return The max values
         */
        public int getMaxValues()
        {
            return maxValues;
        }

        /**
         * Creates a new {@link SelectionMenu} instance if all requirements are satisfied.
         * <br>A selection menu may not have more than 25 options at once.
         *
         * @throws IllegalArgumentException
         *         If the number of options is either greater than {@link #getMaxValues()} or greater than 25
         *
         * @return The new {@link SelectionMenu} instance
         */
        @Nonnull
        public SelectionMenu build()
        {
            Checks.check(maxValues <= options.size(), "The max values should be less than or equal to the amount of available options");
            Checks.check(options.size() <= 25, "Cannot build a selection menu with more than 25 options.");
            return new SelectionMenuImpl(customId, placeholder, minValues, maxValues, options);
        }
    }
}
