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

package net.dv8tion.jda.api.components.selects;

import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selects.SelectMenuInteraction;
import net.dv8tion.jda.api.interactions.components.selects.StringSelectInteraction;
import net.dv8tion.jda.internal.components.selects.StringSelectMenuImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a select menu in a message.
 * <br>This is an interactive component and usually located within an {@link ActionRow ActionRow}.
 * One select menu fills up an entire action row by itself. You cannot have an action row with other components if a select menu is present in the same row.
 *
 * <p>The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected, and they will disappear when the client restarts or the message is reloaded.
 *
 * <p><b>Examples</b><br>
 * <pre>{@code
 * public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 *   if (!event.getName().equals("class")) return;
 *
 *   StringSelectMenu menu = StringSelectMenu.create("menu:class")
 *     .setPlaceholder("Choose your class") // shows the placeholder indicating what this menu is for
 *     .setRequiredRange(1, 1) // exactly one must be selected
 *     .addOption("Arcane Mage", "mage-arcane")
 *     .addOption("Fire Mage", "mage-fire")
 *     .addOption("Frost Mage", "mage-frost")
 *     .setDefaultValues("mage-fire") // default to fire mage
 *     .build();
 *
 *   event.reply("Please pick your class below")
 *     .setEphemeral(true)
 *     .addActionRow(menu)
 *     .queue();
 * }
 * }</pre>
 *
 * @see StringSelectInteraction
 * @see EntitySelectMenu
 */
public interface StringSelectMenu extends SelectMenu
{
    @Nonnull
    @Override
    default StringSelectMenu asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @Override
    default StringSelectMenu asEnabled()
    {
        return withDisabled(false);
    }

    @Nonnull
    @Override
    default StringSelectMenu withDisabled(boolean disabled)
    {
        return createCopy().setDisabled(disabled).build();
    }

    @Nonnull
    @Override
    default StringSelectMenu withUniqueId(int uniqueId)
    {
        return createCopy().setUniqueId(uniqueId).build();
    }

    /**
     * An <b>unmodifiable</b> list of up to {@value #OPTIONS_MAX_AMOUNT} available options to choose from.
     *
     * @return The {@link SelectOption SelectOptions} this menu provides
     *
     * @see    Builder#getOptions()
     */
    @Nonnull
    List<SelectOption> getOptions();

    /**
     * Creates a new preconfigured {@link Builder} with the same settings used for this select menu.
     * <br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    @Override
    default Builder createCopy()
    {
        //noinspection ConstantConditions
        Builder builder = create(getId());
        builder.setRequiredRange(getMinValues(), getMaxValues());
        builder.setPlaceholder(getPlaceholder());
        builder.addOptions(getOptions());
        builder.setDisabled(isDisabled());
        return builder;
    }

    /**
     * Creates a new {@link Builder} for a select menu with the provided custom id.
     *
     * @param  customId
     *         The id used to identify this menu with {@link ActionComponent#getId()} for component interactions
     *
     * @throws IllegalArgumentException
     *         If the provided id is null, empty, or longer than {@value #ID_MAX_LENGTH} characters
     *
     * @return The {@link Builder} used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    static Builder create(@Nonnull String customId)
    {
        return new Builder(customId);
    }

    /**
     * A preconfigured builder for the creation of string select menus.
     */
    class Builder extends SelectMenu.Builder<StringSelectMenu, StringSelectMenu.Builder>
    {
        private final List<SelectOption> options = new ArrayList<>();

        protected Builder(@Nonnull String customId)
        {
            super(customId);
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  options
         *         The {@link SelectOption SelectOptions} to add
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT} or null is provided
         *
         * @return The same builder instance for chaining
         *
         * @see    SelectOption#of(String, String)
         */
        @Nonnull
        public Builder addOptions(@Nonnull SelectOption... options)
        {
            Checks.noneNull(options, "Options");
            Checks.check(this.options.size() + options.length <= OPTIONS_MAX_AMOUNT, "Cannot have more than %d options for a select menu!", OPTIONS_MAX_AMOUNT);
            Collections.addAll(this.options, options);
            return this;
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  options
         *         The {@link SelectOption SelectOptions} to add
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT} or null is provided
         *
         * @return The same builder instance for chaining
         *
         * @see    SelectOption#of(String, String)
         */
        @Nonnull
        public Builder addOptions(@Nonnull Collection<? extends SelectOption> options)
        {
            Checks.noneNull(options, "Options");
            Checks.check(this.options.size() + options.size() <= OPTIONS_MAX_AMOUNT, "Cannot have more than %d options for a select menu!", OPTIONS_MAX_AMOUNT);
            this.options.addAll(options);
            return this;
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         *         The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
         *         up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT}, invalid null is provided,
         *         or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value)
        {
            return addOptions(new SelectOption(label, value));
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         *         The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
         *         up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  emoji
         *         The {@link Emoji} shown next to this option, or null
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT}, invalid null is provided, or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nonnull Emoji emoji)
        {
            return addOption(label, value, null, emoji);
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         *         The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
         *         up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  description
         *         The description explaining the meaning of this option in more detail, up to 50 characters
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT}, invalid null is provided,
         *         or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nonnull String description)
        {
            return addOption(label, value, description, null);
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         *         The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         *         The value for the option used to indicate which option was selected with {@link SelectMenuInteraction#getValues()},
         *         up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  description
         *         The description explaining the meaning of this option in more detail, up to 50 characters
         * @param  emoji
         *         The {@link Emoji} shown next to this option, or null
         *
         * @throws IllegalArgumentException
         *         If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT}, invalid null is provided,
         *         or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder addOption(@Nonnull String label, @Nonnull String value, @Nullable String description, @Nullable Emoji emoji)
        {
            return addOptions(new SelectOption(label, value, description, false, emoji));
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
         * Configures which of the currently applied {@link #getOptions() options} should be selected by default.
         *
         * @param  values
         *         The {@link SelectOption#getValue() option values}
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setDefaultValues(@Nonnull Collection<String> values)
        {
            Checks.noneNull(values, "Values");
            Set<String> set = new HashSet<>(values);
            for (ListIterator<SelectOption> it = getOptions ().listIterator(); it.hasNext();)
            {
                SelectOption option = it.next();
                it.set(option.withDefault(set.contains(option.getValue())));
            }
            return this;
        }

        /**
         * Configures which of the currently applied {@link #getOptions() options} should be selected by default.
         *
         * @param  values
         *         The {@link SelectOption#getValue() option values}
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setDefaultValues(@Nonnull String... values)
        {
            Checks.noneNull(values, "Values");
            return setDefaultValues(Arrays.asList(values));
        }

        /**
         * Configures which of the currently applied {@link #getOptions() options} should be selected by default.
         *
         * @param  values
         *         The {@link SelectOption SelectOptions}
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setDefaultOptions(@Nonnull Collection<? extends SelectOption> values)
        {
            Checks.noneNull(values, "Values");
            return setDefaultValues(values.stream().map(SelectOption::getValue).collect(Collectors.toSet()));
        }

        /**
         * Configures which of the currently applied {@link #getOptions() options} should be selected by default.
         *
         * @param  values
         *         The {@link SelectOption SelectOptions}
         *
         * @throws IllegalArgumentException
         *         If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        public Builder setDefaultOptions(@Nonnull SelectOption... values)
        {
            Checks.noneNull(values, "Values");
            return setDefaultOptions(Arrays.asList(values));
        }

        /**
         * Creates a new {@link StringSelectMenu} instance if all requirements are satisfied.
         * <br>A select menu may not have more than {@value #OPTIONS_MAX_AMOUNT} options at once.
         *
         * <p>The values for {@link #setMinValues(int)} and {@link #setMaxValues(int)} are bounded by the length of {@link #getOptions()}.
         * This means they will automatically be adjusted to not be greater than {@code getOptions().size()}.
         * You can use this to your advantage to easily make a select menu with unlimited options by setting it to {@link #OPTIONS_MAX_AMOUNT}.
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If {@link #getMinValues()} is greater than {@link #getMaxValues()}</li>
         *             <li>If no options are provided</li>
         *             <li>If more than {@value #OPTIONS_MAX_AMOUNT} options are provided</li>
         *         </ul>
         *
         * @return The new {@link StringSelectMenu} instance
         */
        @Nonnull
        public StringSelectMenu build()
        {
            Checks.check(minValues <= maxValues, "Min values cannot be greater than max values!");
            Checks.check(!options.isEmpty(), "Cannot build a select menu without options. Add at least one option!");
            Checks.check(options.size() <= OPTIONS_MAX_AMOUNT, "Cannot build a select menu with more than %d options.", OPTIONS_MAX_AMOUNT);
            int min = Math.min(minValues, options.size());
            int max = Math.min(maxValues, options.size());
            return new StringSelectMenuImpl(customId, uniqueId, placeholder, min, max, disabled, options);
        }
    }
}
