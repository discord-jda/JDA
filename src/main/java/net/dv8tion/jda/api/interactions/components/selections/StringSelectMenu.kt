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
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull
import kotlin.math.min

/**
 * Represents a select menu in a message.
 * <br></br>This is an interactive component and usually located within an [ActionRow][net.dv8tion.jda.api.interactions.components.ActionRow].
 * One select menu fills up an entire action row by itself. You cannot have an action row with other components if a select menu is present in the same row.
 *
 *
 * The selections a user makes are only visible within their current client session.
 * Other users cannot see the choices selected, and they will disappear when the client restarts or the message is reloaded.
 *
 *
 * **Examples**<br></br>
 * <pre>`public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 * if (!event.getName().equals("class")) return;
 *
 * StringSelectMenu menu = StringSelectMenu.create("menu:class")
 * .setPlaceholder("Choose your class") // shows the placeholder indicating what this menu is for
 * .setRequiredRange(1, 1) // exactly one must be selected
 * .addOption("Arcane Mage", "mage-arcane")
 * .addOption("Fire Mage", "mage-fire")
 * .addOption("Frost Mage", "mage-frost")
 * .setDefaultValues("mage-fire") // default to fire mage
 * .build();
 *
 * event.reply("Please pick your class below")
 * .setEphemeral(true)
 * .addActionRow(menu)
 * .queue();
 * }
`</pre> *
 *
 * @see StringSelectInteraction
 *
 * @see EntitySelectMenu
 */
interface StringSelectMenu : SelectMenu {
    @Nonnull
    override fun asDisabled(): StringSelectMenu {
        return withDisabled(true)
    }

    @Nonnull
    override fun asEnabled(): StringSelectMenu {
        return withDisabled(false)
    }

    @Nonnull
    override fun withDisabled(disabled: Boolean): StringSelectMenu {
        return createCopy().setDisabled(disabled)!!.build()
    }

    @JvmField
    @get:Nonnull
    val options: List<SelectOption?>

    /**
     * Creates a new preconfigured [Builder] with the same settings used for this select menu.
     * <br></br>This can be useful to create an updated version of this menu without needing to rebuild it from scratch.
     *
     * @return The [Builder] used to create the select menu
     */
    @Nonnull
    @CheckReturnValue
    fun createCopy(): Builder {
        val builder = create(id)
        builder.setRequiredRange(getMinValues(), getMaxValues())
        builder.setPlaceholder(getPlaceholder())
        builder.addOptions(options)
        builder.setDisabled(isDisabled)
        return builder
    }

    /**
     * A preconfigured builder for the creation of string select menus.
     */
    class Builder(@Nonnull customId: String?) : SelectMenu.Builder<StringSelectMenu, Builder?>(customId) {
        /**
         * Modifiable list of options currently configured in this builder.
         *
         * @return The list of [SelectOptions][SelectOption]
         */
        @get:Nonnull
        val options: MutableList<SelectOption?> = ArrayList()

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  options
         * The [SelectOptions][SelectOption] to add
         *
         * @throws IllegalArgumentException
         * If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT} or null is provided
         *
         * @return The same builder instance for chaining
         *
         * @see SelectOption.of
         */
        @Nonnull
        fun addOptions(@Nonnull vararg options: SelectOption?): Builder {
            Checks.noneNull(options, "Options")
            Checks.check(
                this.options.size + options.size <= SelectMenu.Companion.OPTIONS_MAX_AMOUNT,
                "Cannot have more than %d options for a select menu!",
                SelectMenu.Companion.OPTIONS_MAX_AMOUNT
            )
            Collections.addAll(this.options, *options)
            return this
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  options
         * The [SelectOptions][SelectOption] to add
         *
         * @throws IllegalArgumentException
         * If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT} or null is provided
         *
         * @return The same builder instance for chaining
         *
         * @see SelectOption.of
         */
        @Nonnull
        fun addOptions(@Nonnull options: Collection<SelectOption?>): Builder {
            Checks.noneNull(options, "Options")
            Checks.check(
                this.options.size + options.size <= SelectMenu.Companion.OPTIONS_MAX_AMOUNT,
                "Cannot have more than %d options for a select menu!",
                SelectMenu.Companion.OPTIONS_MAX_AMOUNT
            )
            this.options.addAll(options)
            return this
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         * The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         * The value for the option used to indicate which option was selected with [SelectMenuInteraction.getValues],
         * up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         *
         * @throws IllegalArgumentException
         * If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT}, invalid null is provided,
         * or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun addOption(@Nonnull label: String, @Nonnull value: String): Builder {
            return addOptions(SelectOption(label, value))
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         * The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         * The value for the option used to indicate which option was selected with [SelectMenuInteraction.getValues],
         * up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  emoji
         * The [Emoji] shown next to this option, or null
         *
         * @throws IllegalArgumentException
         * If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT}, invalid null is provided, or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun addOption(@Nonnull label: String, @Nonnull value: String, @Nonnull emoji: Emoji?): Builder {
            return addOption(label, value, null, emoji)
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         * The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         * The value for the option used to indicate which option was selected with [SelectMenuInteraction.getValues],
         * up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  description
         * The description explaining the meaning of this option in more detail, up to 50 characters
         *
         * @throws IllegalArgumentException
         * If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT}, invalid null is provided,
         * or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun addOption(@Nonnull label: String, @Nonnull value: String, @Nonnull description: String?): Builder {
            return addOption(label, value, description, null)
        }

        /**
         * Adds up to {@value #OPTIONS_MAX_AMOUNT} possible options to this select menu.
         *
         * @param  label
         * The label for the option, up to {@value SelectOption#LABEL_MAX_LENGTH} characters
         * @param  value
         * The value for the option used to indicate which option was selected with [SelectMenuInteraction.getValues],
         * up to {@value SelectOption#VALUE_MAX_LENGTH} characters
         * @param  description
         * The description explaining the meaning of this option in more detail, up to 50 characters
         * @param  emoji
         * The [Emoji] shown next to this option, or null
         *
         * @throws IllegalArgumentException
         * If the total amount of options is greater than {@value #OPTIONS_MAX_AMOUNT}, invalid null is provided,
         * or any of the individual parameter requirements are violated.
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun addOption(@Nonnull label: String, @Nonnull value: String, description: String?, emoji: Emoji?): Builder {
            return addOptions(SelectOption(label, value, description, false, emoji))
        }

        /**
         * Configures which of the currently applied [options][.getOptions] should be selected by default.
         *
         * @param  values
         * The [option values][SelectOption.getValue]
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setDefaultValues(@Nonnull values: Collection<String?>?): Builder {
            Checks.noneNull(values, "Values")
            val set: Set<String?> = HashSet(values)
            val it = options.listIterator()
            while (it.hasNext()) {
                val option = it.next()
                it.set(option!!.withDefault(set.contains(option.value)))
            }
            return this
        }

        /**
         * Configures which of the currently applied [options][.getOptions] should be selected by default.
         *
         * @param  values
         * The [option values][SelectOption.getValue]
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setDefaultValues(@Nonnull vararg values: String?): Builder {
            Checks.noneNull(values, "Values")
            return setDefaultValues(Arrays.asList(*values))
        }

        /**
         * Configures which of the currently applied [options][.getOptions] should be selected by default.
         *
         * @param  values
         * The [SelectOptions][SelectOption]
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setDefaultOptions(@Nonnull values: Collection<SelectOption?>): Builder {
            Checks.noneNull(values, "Values")
            return setDefaultValues(values.stream().map { obj: SelectOption? -> obj.getValue() }
                .collect(Collectors.toSet()))
        }

        /**
         * Configures which of the currently applied [options][.getOptions] should be selected by default.
         *
         * @param  values
         * The [SelectOptions][SelectOption]
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return The same builder instance for chaining
         */
        @Nonnull
        fun setDefaultOptions(@Nonnull vararg values: SelectOption?): Builder {
            Checks.noneNull(values, "Values")
            return setDefaultOptions(Arrays.asList(*values))
        }

        /**
         * Creates a new [StringSelectMenu] instance if all requirements are satisfied.
         * <br></br>A select menu may not have more than {@value #OPTIONS_MAX_AMOUNT} options at once.
         *
         *
         * The values for [.setMinValues] and [.setMaxValues] are bounded by the length of [.getOptions].
         * This means they will automatically be adjusted to not be greater than `getOptions().size()`.
         * You can use this to your advantage to easily make a select menu with unlimited options by setting it to [.OPTIONS_MAX_AMOUNT].
         *
         * @throws IllegalArgumentException
         *
         *  * If [.getMinValues] is greater than [.getMaxValues]
         *  * If no options are provided
         *  * If more than {@value #OPTIONS_MAX_AMOUNT} options are provided
         *
         *
         * @return The new [StringSelectMenu] instance
         */
        @Nonnull
        override fun build(): StringSelectMenu {
            Checks.check(minValues <= maxValues, "Min values cannot be greater than max values!")
            Checks.check(!options.isEmpty(), "Cannot build a select menu without options. Add at least one option!")
            Checks.check(
                options.size <= SelectMenu.Companion.OPTIONS_MAX_AMOUNT,
                "Cannot build a select menu with more than %d options.",
                SelectMenu.Companion.OPTIONS_MAX_AMOUNT
            )
            val min = min(minValues.toDouble(), options.size.toDouble()).toInt()
            val max = min(maxValues.toDouble(), options.size.toDouble()).toInt()
            return StringSelectMenuImpl(customId, placeholder, min, max, disabled, options)
        }
    }

    companion object {
        /**
         * Creates a new [Builder] for a select menu with the provided custom id.
         *
         * @param  customId
         * The id used to identify this menu with [ActionComponent.getId] for component interactions
         *
         * @throws IllegalArgumentException
         * If the provided id is null, empty, or longer than {@value #ID_MAX_LENGTH} characters
         *
         * @return The [Builder] used to create the select menu
         */
        @Nonnull
        @CheckReturnValue
        fun create(@Nonnull customId: String?): Builder {
            return Builder(customId)
        }
    }
}
