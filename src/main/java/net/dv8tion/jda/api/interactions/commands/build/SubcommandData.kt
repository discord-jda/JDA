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
package net.dv8tion.jda.api.interactions.commands.build

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.Command.Subcommand
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream
import javax.annotation.Nonnull

/**
 * Builder for a Slash-Command subcommand.
 */
class SubcommandData(@Nonnull name: String?, @Nonnull description: String?) : SerializableData {
    protected val options: MutableList<OptionData?> = ArrayList<OptionData?>(CommandData.Companion.MAX_OPTIONS)

    /**
     * The configured name
     *
     * @return The name
     */
    @get:Nonnull
    var name: String? = null
        protected set

    /**
     * The configured description
     *
     * @return The description
     */
    @get:Nonnull
    var description: String? = null
        protected set

    /**
     * The localizations of this subcommand's name for [various languages][DiscordLocale].
     *
     * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized name
     */
    @get:Nonnull
    val nameLocalizations = LocalizationMap { name: String? -> checkName(name) }

    /**
     * The localizations of this subcommand's description for [various languages][DiscordLocale].
     *
     * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized description
     */
    @get:Nonnull
    val descriptionLocalizations = LocalizationMap { description: String? -> checkDescription(description) }
    private var allowRequired = true

    /**
     * Create a subcommand builder.
     *
     * @param  name
     * The subcommand name, 1-32 lowercase alphanumeric characters
     * @param  description
     * The subcommand description, 1-100 characters
     *
     * @throws IllegalArgumentException
     * If any of the following requirements are not met
     *
     *  * The name must be lowercase alphanumeric (with dash), 1-32 characters long
     *  * The description must be 1-100 characters long
     *
     */
    init {
        setName(name)
        setDescription(description)
    }

    protected fun checkName(@Nonnull name: String?) {
        Checks.inRange(name, 1, 32, "Name")
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name")
        Checks.isLowercase(name, "Name")
    }

    protected fun checkDescription(@Nonnull description: String?) {
        Checks.notEmpty(description, "Description")
        Checks.notLonger(description, 100, "Description")
    }

    /**
     * Configure the name
     *
     * @param  name
     * The lowercase alphanumeric (with dash) name, 1-32 characters
     *
     * @throws IllegalArgumentException
     * If the name is null, not alphanumeric, or not between 1-32 characters
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    fun setName(@Nonnull name: String?): SubcommandData {
        checkName(name)
        this.name = name
        return this
    }

    /**
     * Sets a [language-specific][DiscordLocale] localization of this subcommand's name.
     *
     * @param  locale
     * The locale to associate the translated name with
     *
     * @param  name
     * The translated name to put
     *
     * @throws IllegalArgumentException
     *
     *  * If the locale is null
     *  * If the name is null
     *  * If the locale is [DiscordLocale.UNKNOWN]
     *  * If the name does not pass the corresponding [name check][.setName]
     *
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    fun setNameLocalization(@Nonnull locale: DiscordLocale?, @Nonnull name: String?): SubcommandData {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslation(locale!!, name!!)
        return this
    }

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this subcommand's name.
     *
     * @param  map
     * The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *
     *  * If the map is null
     *  * If the map contains an [DiscordLocale.UNKNOWN] key
     *  * If the map contains a name which does not pass the corresponding [name check][.setName]
     *
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    fun setNameLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): SubcommandData {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslations(map!!)
        return this
    }

    /**
     * Configure the description
     *
     * @param  description
     * The description, 1-100 characters
     *
     * @throws IllegalArgumentException
     * If the name is null or not between 1-100 characters
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    fun setDescription(@Nonnull description: String?): SubcommandData {
        checkDescription(description)
        this.description = description
        return this
    }

    /**
     * Sets a [language-specific][DiscordLocale] localization of this subcommand's description.
     *
     * @param  locale
     * The locale to associate the translated description with
     *
     * @param  description
     * The translated description to put
     *
     * @throws IllegalArgumentException
     *
     *  * If the locale is null
     *  * If the description is null
     *  * If the locale is [DiscordLocale.UNKNOWN]
     *  * If the description does not pass the corresponding [description check][.setDescription]
     *
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    fun setDescriptionLocalization(@Nonnull locale: DiscordLocale?, @Nonnull description: String?): SubcommandData {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslation(locale!!, description!!)
        return this
    }

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this subcommand's description.
     *
     * @param  map
     * The map from which to transfer the translated descriptions
     *
     * @throws IllegalArgumentException
     *
     *  * If the map is null
     *  * If the map contains an [DiscordLocale.UNKNOWN] key
     *  * If the map contains a description which does not pass the corresponding [description check][.setDescription]
     *
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    fun setDescriptionLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): SubcommandData {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslations(map!!)
        return this
    }

    /**
     * Removes all options that evaluate to `true` under the provided `condition`.
     *
     *
     * **Example: Remove all options**
     * <pre>`command.removeOptions(option -> true);
    `</pre> *
     *
     * **Example: Remove all options that are required**
     * <pre>`command.removeOptions(option -> option.isRequired());
    `</pre> *
     *
     * @param  condition
     * The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     * If the condition is null
     *
     * @return True, if any options were removed
     */
    fun removeOptions(@Nonnull condition: Predicate<in OptionData?>?): Boolean {
        Checks.notNull(condition, "Condition")
        return options.removeIf(condition!!)
    }

    /**
     * Removes options by the provided name.
     *
     * @param  name
     * The **case-sensitive** option name
     *
     * @return True, if any options were removed
     */
    fun removeOptionByName(@Nonnull name: String): Boolean {
        return removeOptions { option: OptionData? -> option.getName() == name }
    }

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this subcommand.
     *
     *
     * Required options must be added before non-required options!
     *
     * @param  options
     * The [options][OptionData] to add
     *
     * @throws IllegalArgumentException
     *
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    fun addOptions(@Nonnull vararg options: OptionData?): SubcommandData {
        Checks.noneNull(options, "Option")
        Checks.check(
            options.size + this.options.size <= CommandData.Companion.MAX_OPTIONS,
            "Cannot have more than %d options for a subcommand!",
            CommandData.Companion.MAX_OPTIONS
        )
        var allowRequired = allowRequired
        for (option in options) {
            Checks.check(option.getType() != OptionType.SUB_COMMAND, "Cannot add a subcommand to a subcommand!")
            Checks.check(
                option.getType() != OptionType.SUB_COMMAND_GROUP,
                "Cannot add a subcommand group to a subcommand!"
            )
            Checks.check(
                allowRequired || !option!!.isRequired,
                "Cannot add required options after non-required options!"
            )
            allowRequired = option!!.isRequired // prevent adding required options after non-required options
        }
        Checks.checkUnique(
            Stream.concat(getOptions().stream(), Arrays.stream(options)).map { obj: OptionData? -> obj.getName() },
            "Cannot have multiple options with the same name. Name: \"%s\" appeared %d times!"
        ) { count: Long?, value: String? -> arrayOf<Any?>(value, count) }
        this.allowRequired = allowRequired
        this.options.addAll(Arrays.asList(*options))
        return this
    }

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this subcommand.
     *
     *
     * Required options must be added before non-required options!
     *
     * @param  options
     * The [options][OptionData] to add
     *
     * @throws IllegalArgumentException
     *
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    fun addOptions(@Nonnull options: Collection<OptionData?>): SubcommandData {
        Checks.noneNull(options, "Options")
        return addOptions(*options.toTypedArray<OptionData?>())
    }

    /**
     * Adds an option to this subcommand.
     *
     *
     * Required options must be added before non-required options!
     *
     * @param  type
     * The [OptionType]
     * @param  name
     * The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     * The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     * @param  required
     * Whether this option is required (See [OptionData.setRequired])
     * @param  autoComplete
     * Whether this option supports auto-complete via [CommandAutoCompleteInteractionEvent],
     * only supported for option types which [support choices][OptionType.canSupportChoices]
     *
     * @throws IllegalArgumentException
     *
     *  * If this option is required and you already added a non-required option.
     *  * If the provided option type does not support auto-complete
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    fun addOption(
        @Nonnull type: OptionType,
        @Nonnull name: String?,
        @Nonnull description: String?,
        required: Boolean,
        autoComplete: Boolean
    ): SubcommandData {
        return addOptions(
            OptionData(type, name, description)
                .setRequired(required)
                .setAutoComplete(autoComplete)
        )
    }

    /**
     * Adds an option to this subcommand.
     *
     *
     * Required options must be added before non-required options!
     *
     * @param  type
     * The [OptionType]
     * @param  name
     * The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     * The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     * @param  required
     * Whether this option is required (See [OptionData.setRequired])
     *
     * @throws IllegalArgumentException
     *
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    fun addOption(
        @Nonnull type: OptionType,
        @Nonnull name: String?,
        @Nonnull description: String?,
        required: Boolean
    ): SubcommandData {
        return addOption(type, name, description, required, false)
    }

    /**
     * Adds an option to this subcommand.
     * <br></br>The option is set to be non-required! You can use [.addOption] to add a required option instead.
     *
     *
     * Required options must be added before non-required options!
     *
     * @param  type
     * The [OptionType]
     * @param  name
     * The lowercase option name, 1-{@value OptionData#MAX_NAME_LENGTH} characters
     * @param  description
     * The option description, 1-{@value OptionData#MAX_DESCRIPTION_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    fun addOption(@Nonnull type: OptionType, @Nonnull name: String?, @Nonnull description: String?): SubcommandData {
        return addOption(type, name, description, false)
    }

    /**
     * The options for this command.
     *
     * @return Immutable list of [OptionData]
     */
    @Nonnull
    fun getOptions(): List<OptionData?> {
        return Collections.unmodifiableList(options)
    }

    @Nonnull
    override fun toData(): DataObject {
        return DataObject.empty()
            .put("type", OptionType.SUB_COMMAND.key)
            .put("name", name)
            .put("name_localizations", nameLocalizations)
            .put("description", description)
            .put("description_localizations", descriptionLocalizations)
            .put("options", DataArray.fromCollection(options))
    }

    companion object {
        /**
         * Parses the provided serialization back into an SubcommandData instance.
         * <br></br>This is the reverse function for [.toData].
         *
         * @param  json
         * The serialized [DataObject] representing the subcommand
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the serialized object is missing required fields
         * @throws IllegalArgumentException
         * If any of the values are failing the respective checks such as length
         *
         * @return The parsed SubcommandData instance, which can be further configured through setters
         */
        @Nonnull
        fun fromData(@Nonnull json: DataObject): SubcommandData {
            val name = json.getString("name")
            val description = json.getString("description")
            val sub = SubcommandData(name, description)
            json.optArray("options").ifPresent { arr: DataArray ->
                arr.stream<DataObject> { obj: DataArray, index: Int? ->
                    obj.getObject(
                        index!!
                    )
                }
                    .map<OptionData>(Function<DataObject, OptionData> { json: DataObject ->
                        OptionData.Companion.fromData(
                            json
                        )
                    })
                    .forEach { options: OptionData? -> sub.addOptions(options) }
            }
            sub.setNameLocalizations(LocalizationUtils.mapFromProperty(json, "name_localizations"))
            sub.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(json, "description_localizations"))
            return sub
        }

        /**
         * Converts the provided [Command.Subcommand] into a SubCommandData instance.
         *
         * @param  subcommand
         * The subcommand to convert
         *
         * @throws IllegalArgumentException
         * If null is provided or the subcommand has illegal configuration
         *
         * @return An instance of SubCommandData
         */
        @Nonnull
        fun fromSubcommand(@Nonnull subcommand: Subcommand): SubcommandData {
            Checks.notNull(subcommand, "Subcommand")
            val data = SubcommandData(subcommand.name, subcommand.description)
            data.setNameLocalizations(subcommand.nameLocalizations.toMap())
            data.setDescriptionLocalizations(subcommand.descriptionLocalizations.toMap())
            subcommand.options
                .stream()
                .map<OptionData>(Function<Command.Option, OptionData> { option: Command.Option ->
                    OptionData.Companion.fromOption(
                        option
                    )
                })
                .forEach { options: OptionData? -> data.addOptions(options) }
            return data
        }
    }
}
