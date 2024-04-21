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

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataType
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils
import java.util.*
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Builder for a Slash-Command option.
 */
class OptionData @JvmOverloads constructor(
    @Nonnull type: OptionType,
    @Nonnull name: String?,
    @Nonnull description: String?,
    isRequired: Boolean = false,
    isAutoComplete: Boolean = false
) : SerializableData {
    /**
     * The [OptionType] for this option
     *
     * @return The [OptionType]
     */
    @JvmField
    @get:Nonnull
    val type: OptionType

    /**
     * The name for this option
     *
     * @return The name
     */
    @get:Nonnull
    var name: String? = null
        private set

    /**
     * The description for this option
     *
     * @return The description
     */
    @get:Nonnull
    var description: String? = null
        private set

    /**
     * The localizations of this option's name for [various languages][DiscordLocale].
     *
     * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized name
     */
    @get:Nonnull
    val nameLocalizations = LocalizationMap { name: String? -> checkName(name) }

    /**
     * The localizations of this option's description for [various languages][DiscordLocale].
     *
     * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized description
     */
    @get:Nonnull
    val descriptionLocalizations = LocalizationMap { description: String? -> checkDescription(description) }

    /**
     * Whether this option is required.
     * <br></br>This can be configured with [.setRequired].
     *
     *
     * Required options must always be set by the command invocation.
     *
     * @return True, if this option is required
     */
    var isRequired = false
        private set

    /**
     * Whether this option supports auto-complete interactions
     * via [CommandAutoCompleteInteractionEvent].
     *
     * @return True, if this option supports auto-complete
     */
    var isAutoComplete = false
        private set

    /**
     * The [ChannelTypes][ChannelType] this option is restricted to.
     * <br></br>This is empty if the option is not of type [CHANNEL][OptionType.CHANNEL] or not restricted to specific types.
     *
     * @return [EnumSet] of [ChannelType]
     */
    @get:Nonnull
    val channelTypes = EnumSet.noneOf(ChannelType::class.java)

    /**
     * The minimum value which can be provided for this option.
     * <br></br>This returns `null` if the value is not set or if the option
     * is not of type [INTEGER][OptionType.INTEGER] or [NUMBER][OptionType.NUMBER].
     *
     * @return The minimum value for this option
     */
    var minValue: Number? = null
        private set

    /**
     * The maximum value which can be provided for this option.
     * <br></br>This returns `null` if the value is not set or if the option
     * is not of type [INTEGER][OptionType.INTEGER] or [NUMBER][OptionType.NUMBER].
     *
     * @return The maximum value for this option
     */
    var maxValue: Number? = null
        private set

    /**
     * The minimum length for strings which can be provided for this option.
     * <br></br>This returns `null` if the value is not set or if the option
     * is not of type [STRING][OptionType.STRING].
     *
     * @return The minimum length for strings for this option or `null`
     */
    var minLength: Int? = null
        private set

    /**
     * The maximum length for strings which can be provided for this option.
     * <br></br>This returns `null` if the value is not set or if the option
     * is not of type [STRING][OptionType.STRING].
     *
     * @return The maximum length for strings for this option or `null`
     */
    var maxLength: Int? = null
        private set
    private var choices: MutableList<Command.Choice?>? = null
    /**
     * Create an option builder.
     *
     * @param  type
     * The [OptionType]
     * @param  name
     * The option name, up to {@value #MAX_NAME_LENGTH} alphanumeric (with dash) lowercase characters, as
     * defined by [.MAX_NAME_LENGTH]
     * @param  description
     * The option description, up to {@value #MAX_DESCRIPTION_LENGTH} characters, as defined by [.MAX_DESCRIPTION_LENGTH]
     * @param  isRequired
     * `True`, if this option is required
     * @param  isAutoComplete
     * True, if auto-complete should be supported (requires [OptionType.canSupportChoices])
     *
     * @throws IllegalArgumentException
     *
     *  * If `type` is null
     *  * If `type` is [UNKNOWN][OptionType.UNKNOWN], [SUB_COMMAND][OptionType.SUB_COMMAND], or [SUB_COMMAND_GROUP][OptionType.SUB_COMMAND_GROUP]
     *  * If `name` is not alphanumeric (with dash), lowercase and between 1 and {@value #MAX_NAME_LENGTH} characters long
     *  * If `description` is not between 1 and {@value #MAX_DESCRIPTION_LENGTH} characters long
     *  * If [OptionType.canSupportChoices] is false and `isAutoComplete` is true
     *
     */
    /**
     * Create an option builder.
     *
     * @param  type
     * The [OptionType]
     * @param  name
     * The option name, up to {@value #MAX_NAME_LENGTH} alphanumeric (with dash) lowercase characters, as
     * defined by [.MAX_NAME_LENGTH]
     * @param  description
     * The option description, up to {@value #MAX_DESCRIPTION_LENGTH} characters, as defined by [.MAX_DESCRIPTION_LENGTH]
     * @param  isRequired
     * `True`, if this option is required
     *
     * @throws IllegalArgumentException
     *
     *  * If `type` is null
     *  * If `type` is [UNKNOWN][OptionType.UNKNOWN]
     *  * If `name` is not alphanumeric (with dash), lowercase and between 1 and {@value #MAX_NAME_LENGTH} characters long
     *  * If `description` is not between 1 and {@value #MAX_DESCRIPTION_LENGTH} characters long
     *
     */
    /**
     * Create an option builder.
     * <br></br>This option is not [required][.isRequired] by default.
     *
     * @param  type
     * The [OptionType]
     * @param  name
     * The option name, up to {@value #MAX_NAME_LENGTH} alphanumeric (with dash) lowercase characters, as
     * defined by [.MAX_NAME_LENGTH]
     * @param  description
     * The option description, up to {@value #MAX_DESCRIPTION_LENGTH} characters, as defined by [.MAX_DESCRIPTION_LENGTH]
     *
     * @throws IllegalArgumentException
     *
     *  * If `type` is null
     *  * If `type` is [UNKNOWN][OptionType.UNKNOWN]
     *  * If `name` is not alphanumeric (with dash), lowercase and between 1 and {@value #MAX_NAME_LENGTH} characters long
     *  * If `description` is not between 1 and {@value #MAX_DESCRIPTION_LENGTH} characters long
     *
     */
    init {
        Checks.notNull(type, "Type")
        Checks.check(type != OptionType.UNKNOWN, "Cannot make option of unknown type!")
        Checks.check(
            type != OptionType.SUB_COMMAND,
            "Cannot make a subcommand group with OptionData. Use addSubcommands(...) instead!"
        )
        Checks.check(
            type != OptionType.SUB_COMMAND_GROUP,
            "Cannot make a subcommand group with OptionData. Use addSubcommandGroups(...) instead!"
        )
        this.type = type
        setName(name)
        setDescription(description)
        setRequired(isRequired)
        if (type.canSupportChoices()) choices = ArrayList()
        setAutoComplete(isAutoComplete)
    }

    protected fun checkName(@Nonnull name: String?) {
        Checks.notEmpty(name, "Name")
        Checks.notLonger(name, MAX_NAME_LENGTH, "Name")
        Checks.isLowercase(name, "Name")
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name")
    }

    protected fun checkDescription(@Nonnull description: String?) {
        Checks.notEmpty(description, "Description")
        Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description")
    }

    /**
     * The choices for this option.
     * <br></br>This is empty by default and can only be configured for specific option types.
     *
     * @return Immutable list of [Choices][Command.Choice]
     *
     * @see .addChoice
     * @see .addChoice
     */
    @Nonnull
    fun getChoices(): List<Command.Choice?> {
        return if (choices == null || choices.isEmpty()) emptyList<Command.Choice>() else Collections.unmodifiableList(
            choices
        )
    }

    /**
     * Configure the name
     *
     * @param  name
     * The lowercase alphanumeric (with dash) name, [1-32 characters long][.MAX_NAME_LENGTH]
     *
     * @throws IllegalArgumentException
     * If the name is null, empty, not alphanumeric, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
     * as defined by [.MAX_NAME_LENGTH]
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setName(@Nonnull name: String?): OptionData {
        checkName(name)
        this.name = name
        return this
    }

    /**
     * Sets a [language-specific][DiscordLocale] localization of this option's name.
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
    fun setNameLocalization(@Nonnull locale: DiscordLocale?, @Nonnull name: String?): OptionData {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslation(locale!!, name!!)
        return this
    }

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this option's name.
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
    fun setNameLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): OptionData {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslations(map!!)
        return this
    }

    /**
     * Configure the description
     *
     * @param  description
     * The description, 1-{@value #MAX_DESCRIPTION_LENGTH} characters, as defined by [.MAX_DESCRIPTION_LENGTH]
     *
     * @throws IllegalArgumentException
     * If the name is null, empty, or longer than {@value #MAX_DESCRIPTION_LENGTH}, as defined by [.MAX_DESCRIPTION_LENGTH]
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setDescription(@Nonnull description: String?): OptionData {
        checkDescription(description)
        this.description = description
        return this
    }

    /**
     * Sets a [language-specific][DiscordLocale] localization of this option's description.
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
    fun setDescriptionLocalization(@Nonnull locale: DiscordLocale?, @Nonnull description: String?): OptionData {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslation(locale!!, description!!)
        return this
    }

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this option's description.
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
    fun setDescriptionLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): OptionData {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslations(map!!)
        return this
    }

    /**
     * Configure whether the user must set this option.
     * <br></br>Required options must always be filled out when using the command.
     *
     * @param  required
     * True, if this option is required
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setRequired(required: Boolean): OptionData {
        isRequired = required
        return this
    }

    /**
     * Configure whether this option should support auto-complete interactions
     * via [CommandAutoCompleteInteractionEvent].
     *
     *
     * This is only supported for options which support choices. See [OptionType.canSupportChoices].
     *
     * @param  autoComplete
     * True, if auto-complete should be supported
     *
     * @throws IllegalStateException
     * If this option is already configured to use choices or the option type does not support auto-complete
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setAutoComplete(autoComplete: Boolean): OptionData {
        if (autoComplete) {
            check(!(choices == null || !type.canSupportChoices())) { "Cannot enable auto-complete for options of type $type" }
            check(choices.isEmpty()) { "Cannot enable auto-complete for options with choices" }
        }
        isAutoComplete = autoComplete
        return this
    }

    /**
     * Configure the [ChannelTypes][ChannelType] to restrict this option to.
     * **This only applies to options of type [CHANNEL][OptionType.CHANNEL].**
     *
     * @param  channelTypes
     * The [ChannelTypes][ChannelType] to restrict this option to
     * or empty array to accept all [ChannelTypes][ChannelType]
     *
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [CHANNEL][OptionType.CHANNEL]
     *  * If `channelTypes` contain `null`
     *  * If `channelTypes` contains non-guild channels
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setChannelTypes(@Nonnull vararg channelTypes: ChannelType?): OptionData {
        Checks.noneNull(channelTypes, "ChannelTypes")
        return setChannelTypes(Arrays.asList(*channelTypes))
    }

    /**
     * Configure the [ChannelTypes][ChannelType] to restrict this option to.
     * **This only applies to options of type [CHANNEL][OptionType.CHANNEL].**
     *
     * @param  channelTypes
     * The [ChannelTypes][ChannelType] to restrict this option to
     * or empty collection to accept all [ChannelTypes][ChannelType]
     *
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [CHANNEL][OptionType.CHANNEL]
     *  * If `channelTypes` is null
     *  * If `channelTypes` contain `null`
     *  * If `channelTypes` contains non-guild channels
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setChannelTypes(@Nonnull channelTypes: Collection<ChannelType?>): OptionData {
        require(type == OptionType.CHANNEL) { "Can only apply channel type restriction to options of type CHANNEL" }
        Checks.notNull(channelTypes, "ChannelType collection")
        Checks.noneNull(channelTypes, "ChannelType")
        for (channelType in channelTypes) {
            require(channelType!!.isGuild) { "Provided channel type is not a guild channel type. Provided: $channelType" }
        }
        this.channelTypes.clear()
        this.channelTypes.addAll(channelTypes)
        return this
    }

    /**
     * Configure the minimal value which can be provided for this option.
     *
     * @param  value
     * The minimal value which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [INTEGER][OptionType.INTEGER] or [NUMBER][OptionType.NUMBER]
     *  * If `value` is less than [MIN_NEGATIVE_NUMBER][OptionData.MIN_NEGATIVE_NUMBER]
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setMinValue(value: Long): OptionData {
        require(!(type != OptionType.INTEGER && type != OptionType.NUMBER)) { "Can only set min and max long value for options of type INTEGER or NUMBER" }
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Long value may not be less than %f", MIN_NEGATIVE_NUMBER)
        minValue = value
        return this
    }

    /**
     * Configure the minimal value which can be provided for this option.
     *
     * @param  value
     * The minimal value which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [NUMBER][OptionType.NUMBER]
     *  * If `value` is less than [MIN_NEGATIVE_NUMBER][OptionData.MIN_NEGATIVE_NUMBER]
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setMinValue(value: Double): OptionData {
        require(type == OptionType.NUMBER) { "Can only set min double value for options of type NUMBER" }
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Double value may not be less than %f", MIN_NEGATIVE_NUMBER)
        minValue = value
        return this
    }

    /**
     * Configure the maximal value which can be provided for this option.
     *
     * @param  value
     * The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [INTEGER][OptionType.INTEGER] or [NUMBER][OptionType.NUMBER]
     *  * If `value` is greater than [MAX_POSITIVE_NUMBER][OptionData.MAX_POSITIVE_NUMBER]
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setMaxValue(value: Long): OptionData {
        require(!(type != OptionType.INTEGER && type != OptionType.NUMBER)) { "Can only set min and max long value for options of type INTEGER or NUMBER" }
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Long value may not be greater than %f", MAX_POSITIVE_NUMBER)
        maxValue = value
        return this
    }

    /**
     * Configure the maximal value which can be provided for this option.
     *
     * @param  value
     * The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [NUMBER][OptionType.NUMBER]
     *  * If `value` is greater than [MAX_POSITIVE_NUMBER][OptionData.MAX_POSITIVE_NUMBER]
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setMaxValue(value: Double): OptionData {
        require(type == OptionType.NUMBER) { "Can only set max double value for options of type NUMBER" }
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Double value may not be greater than %f", MAX_POSITIVE_NUMBER)
        maxValue = value
        return this
    }

    /**
     * Configure the minimal and maximal value which can be provided for this option.
     *
     * @param  minValue
     * The minimal value which can be provided for this option.
     * @param  maxValue
     * The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [INTEGER][OptionType.INTEGER] or [NUMBER][OptionType.NUMBER]
     *  * If `minValue` is less than or not equal to [MIN_NEGATIVE_NUMBER][OptionData.MIN_NEGATIVE_NUMBER]
     *  * If `maxValue` is greater than [MAX_POSITIVE_NUMBER][OptionData.MAX_POSITIVE_NUMBER]
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setRequiredRange(minValue: Long, maxValue: Long): OptionData {
        require(!(type != OptionType.INTEGER && type != OptionType.NUMBER)) { "Can only set min and max long value for options of type INTEGER or NUMBER" }
        Checks.check(minValue >= MIN_NEGATIVE_NUMBER, "Long value may not be less than %f", MIN_NEGATIVE_NUMBER)
        Checks.check(maxValue <= MAX_POSITIVE_NUMBER, "Long value may not be greater than %f", MAX_POSITIVE_NUMBER)
        this.minValue = minValue
        this.maxValue = maxValue
        return this
    }

    /**
     * Configure the minimal and maximal value which can be provided for this option.
     *
     * @param  minValue
     * The minimal value which can be provided for this option.
     * @param  maxValue
     * The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [NUMBER][OptionType.NUMBER]
     *  * If `minValue` is less than or not equal to [MIN_NEGATIVE_NUMBER][OptionData.MIN_NEGATIVE_NUMBER]
     *  * If `maxValue` is greater than [MAX_POSITIVE_NUMBER][OptionData.MAX_POSITIVE_NUMBER]
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setRequiredRange(minValue: Double, maxValue: Double): OptionData {
        require(type == OptionType.NUMBER) { "Can only set min and max double value for options of type NUMBER" }
        Checks.check(minValue >= MIN_NEGATIVE_NUMBER, "Double value may not be less than %f", MIN_NEGATIVE_NUMBER)
        Checks.check(maxValue <= MAX_POSITIVE_NUMBER, "Double value may not be greater than %f", MAX_POSITIVE_NUMBER)
        this.minValue = minValue
        this.maxValue = maxValue
        return this
    }

    /**
     * Configure the minimum length for strings which can be provided for this option.
     *
     * @param  minLength
     * The minimum length for strings which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [STRING][OptionType.STRING]
     *  * If `minLength` is not positive
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setMinLength(minLength: Int): OptionData {
        require(type == OptionType.STRING) { "Can only set min length for options of type STRING" }
        Checks.positive(minLength, "Min length")
        this.minLength = minLength
        return this
    }

    /**
     * Configure the maximum length for strings which can be provided for this option.
     *
     * @param  maxLength
     * The maximum length for strings which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [STRING][OptionType.STRING]
     *  * If `maxLength` is not positive or greater than {@value MAX_STRING_OPTION_LENGTH}
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setMaxLength(maxLength: Int): OptionData {
        require(type == OptionType.STRING) { "Can only set max length for options of type STRING" }
        Checks.positive(maxLength, "Max length")
        Checks.check(
            maxLength <= MAX_STRING_OPTION_LENGTH,
            "Max length must not be greater than %d. Provided: %d",
            MAX_STRING_OPTION_LENGTH,
            maxLength
        )
        this.maxLength = maxLength
        return this
    }

    /**
     * Configure the minimum and maximum length for strings which can be provided for this option.
     *
     * @param  minLength
     * The minimum length for strings which can be provided for this option.
     * @param  maxLength
     * The maximum length for strings which can be provided for this option.
     * @throws IllegalArgumentException
     *
     *  * If [type of this option][OptionType] is not [STRING][OptionType.STRING]
     *  * If `minLength` is greater than `maxLength`
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun setRequiredLength(minLength: Int, maxLength: Int): OptionData {
        require(type == OptionType.STRING) { "Can only set min and max length for options of type STRING" }
        Checks.check(
            minLength <= maxLength,
            "Min length must not be greater than max length. Provided: %d > %d",
            minLength,
            maxLength
        )
        setMinLength(minLength)
        setMaxLength(maxLength)
        return this
    }

    /**
     * Add a predefined choice for this option.
     * <br></br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     * The name used in the client, up to {@value #MAX_CHOICE_NAME_LENGTH} characters long, as defined by
     * [.MAX_CHOICE_NAME_LENGTH]
     * @param  value
     * The value received in [OptionMapping][net.dv8tion.jda.api.interactions.commands.OptionMapping]
     *
     * @throws IllegalArgumentException
     *
     *  * If `name` is null, empty, or greater than {@value #MAX_CHOICE_NAME_LENGTH} characters long
     *  * If `value` is less than [.MIN_NEGATIVE_NUMBER] or greater than [.MAX_POSITIVE_NUMBER]
     *  * If adding this choice would exceed {@value #MAX_CHOICES} choices, as defined by [.MAX_CHOICES]
     *  * If the [OptionType] is not [OptionType.NUMBER]
     *  * If the option is auto-complete enabled
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun addChoice(@Nonnull name: String?, value: Double): OptionData {
        Checks.notEmpty(name, "Name")
        Checks.notLonger(name, MAX_CHOICE_NAME_LENGTH, "Name")
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Double value may not be less than %f", MIN_NEGATIVE_NUMBER)
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Double value may not be greater than %f", MAX_POSITIVE_NUMBER)
        check(!isAutoComplete) { "Cannot add choices to auto-complete options" }
        require(type == OptionType.NUMBER) { "Cannot add double choice for OptionType.$type" }
        Checks.check(choices!!.size < MAX_CHOICES, "Cannot have more than 25 choices for an option!")
        choices!!.add(Command.Choice(name!!, value))
        return this
    }

    /**
     * Add a predefined choice for this option.
     * <br></br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     * The name used in the client
     * @param  value
     * The value received in [OptionMapping][net.dv8tion.jda.api.interactions.commands.OptionMapping]
     *
     * @throws IllegalArgumentException
     *
     *  * If `name` is null, empty, or greater than {@value #MAX_CHOICE_NAME_LENGTH} characters long
     *  * If `value` is less than [.MIN_NEGATIVE_NUMBER] or greater than [.MAX_POSITIVE_NUMBER]
     *  * If adding this choice would exceed {@value #MAX_CHOICES} choices, as defined by [.MAX_CHOICES]
     *  * If the [OptionType] is not [OptionType.INTEGER]
     *  * If the option is auto-complete enabled
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun addChoice(@Nonnull name: String?, value: Long): OptionData {
        Checks.notEmpty(name, "Name")
        Checks.notLonger(name, MAX_CHOICE_NAME_LENGTH, "Name")
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Long value may not be less than %f", MIN_NEGATIVE_NUMBER)
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Long value may not be greater than %f", MAX_POSITIVE_NUMBER)
        check(!isAutoComplete) { "Cannot add choices to auto-complete options" }
        require(type == OptionType.INTEGER) { "Cannot add long choice for OptionType.$type" }
        Checks.check(choices!!.size < MAX_CHOICES, "Cannot have more than 25 choices for an option!")
        choices!!.add(Command.Choice(name!!, value))
        return this
    }

    /**
     * Add a predefined choice for this option.
     * <br></br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     * The name used in the client
     * @param  value
     * The value received in [OptionMapping][net.dv8tion.jda.api.interactions.commands.OptionMapping]
     *
     * @throws IllegalArgumentException
     *
     *  * If `name` is null, empty, or greater than {@value #MAX_CHOICE_NAME_LENGTH} characters long
     *  * If `value` is less than [.MIN_NEGATIVE_NUMBER] or greater than [.MAX_POSITIVE_NUMBER]
     *  * If adding this choice would exceed {@value #MAX_CHOICES} choices, as defined by [.MAX_CHOICES]
     *  * If the [OptionType] is not [OptionType.STRING]
     *  * If the option is auto-complete enabled
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun addChoice(@Nonnull name: String?, @Nonnull value: String?): OptionData {
        Checks.notEmpty(name, "Name")
        Checks.notEmpty(value, "Value")
        Checks.notLonger(name, MAX_CHOICE_NAME_LENGTH, "Name")
        Checks.notLonger(value, MAX_CHOICE_VALUE_LENGTH, "Value")
        check(!isAutoComplete) { "Cannot add choices to auto-complete options" }
        require(type == OptionType.STRING) { "Cannot add string choice for OptionType.$type" }
        Checks.check(choices!!.size < MAX_CHOICES, "Cannot have more than 25 choices for an option!")
        choices!!.add(Command.Choice(name!!, value!!))
        return this
    }

    /**
     * Adds up to 25 predefined choices for this option.
     * <br></br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  choices
     * The choices to add
     *
     * @throws IllegalArgumentException
     *
     *  * If the [OptionType] does not [support choices][OptionType.canSupportChoices]
     *  * If the provided `choices` are null
     *  * If the amount of `choices` provided, when combined with the already set choices, would be greater than {@value #MAX_CHOICES}, as defined by [.MAX_CHOICES]
     *  * If the [OptionType] of the choices is not either [OptionType.INTEGER], [OptionType.STRING] or [OptionType.NUMBER]
     *  * If the option is auto-complete enabled
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun addChoices(@Nonnull vararg choices: Command.Choice?): OptionData {
        Checks.noneNull(choices, "Choices")
        return addChoices(Arrays.asList(*choices))
    }

    /**
     * Adds up to 25 predefined choices for this option.
     * <br></br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  choices
     * The choices to add
     *
     * @throws IllegalArgumentException
     *
     *  * If the [OptionType] does not [support choices][OptionType.canSupportChoices]
     *  * If the provided `choices` collection is null
     *  * If the provided `choices` are null
     *  * If the amount of `choices` provided, when combined with the already set choices, would be greater than {@value #MAX_CHOICES}, as defined by [.MAX_CHOICES]
     *  * If the [OptionType] of the choices is not either [OptionType.INTEGER], [OptionType.STRING] or [OptionType.NUMBER]
     *  * If the option is auto-complete enabled
     *
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    fun addChoices(@Nonnull choices: Collection<Command.Choice?>): OptionData {
        Checks.notNull(choices, "Choices")
        if (choices.size == 0) return this
        check(!(this.choices == null || !type.canSupportChoices())) { "Cannot add choices for an option of type $type" }
        Checks.noneNull(choices, "Choices")
        check(!isAutoComplete) { "Cannot add choices to auto-complete options" }
        Checks.check(
            choices.size + this.choices.size <= MAX_CHOICES,
            "Cannot have more than 25 choices for one option!"
        )
        this.choices.addAll(choices)
        return this
    }

    @Nonnull
    override fun toData(): DataObject {
        val json = DataObject.empty()
            .put("type", type.key)
            .put("name", name)
            .put("name_localizations", nameLocalizations)
            .put("description", description)
            .put("description_localizations", descriptionLocalizations)
        if (type != OptionType.SUB_COMMAND && type != OptionType.SUB_COMMAND_GROUP) {
            json.put("required", isRequired)
            json.put("autocomplete", isAutoComplete)
        }
        if (choices != null && !choices.isEmpty()) {
            json.put("choices", DataArray.fromCollection(
                choices.stream()
                    .map { choice: Command.Choice? -> choice!!.toData(type) }
                    .collect(Collectors.toList())
            ))
        }
        if (type == OptionType.CHANNEL && !channelTypes.isEmpty()) json.put(
            "channel_types", channelTypes.stream().map<Any>(ChannelType::getId).collect(
                Collectors.toList<Any>()
            )
        )
        if (type == OptionType.INTEGER || type == OptionType.NUMBER) {
            if (minValue != null) json.put("min_value", minValue)
            if (maxValue != null) json.put("max_value", maxValue)
        }
        if (type == OptionType.STRING) {
            if (minLength != null) json.put("min_length", minLength)
            if (maxLength != null) json.put("max_length", maxLength)
        }
        return json
    }

    companion object {
        /**
         * The highest positive amount Discord allows the [NUMBER][OptionType.NUMBER] type to be.
         */
        const val MAX_POSITIVE_NUMBER = ((1L shl 53) - 1 // 1L << 53 is non-inclusive for Discord
                ).toDouble()

        /**
         * The largest negative amount Discord allows the [NUMBER][OptionType.NUMBER] type to be.
         */
        const val MIN_NEGATIVE_NUMBER = (-(1L shl 53) + 1 // 1L << 53 is non-inclusive for Discord
                ).toDouble()

        /**
         * The maximum length the name of an option can be.
         */
        const val MAX_NAME_LENGTH = 32

        /**
         * The maximum length of the name of Command Option Choice names
         */
        const val MAX_CHOICE_NAME_LENGTH = 100

        /**
         * The maximum length the description of an option can be.
         */
        const val MAX_DESCRIPTION_LENGTH = 100

        /**
         * The maximum length a [String value][OptionType.STRING] for a choice can be.
         */
        const val MAX_CHOICE_VALUE_LENGTH = 100

        /**
         * The total amount of [choices][.getChoices] you can set.
         */
        const val MAX_CHOICES = 25

        /**
         * The maximum length for a [String option][OptionType.STRING].
         */
        const val MAX_STRING_OPTION_LENGTH = 6000

        /**
         * Parses the provided serialization back into an OptionData instance.
         * <br></br>This is the reverse function for [.toData].
         *
         * @param  json
         * The serialized [DataObject] representing the option
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the serialized object is missing required fields
         * @throws IllegalArgumentException
         * If any of the values are failing the respective checks such as length
         *
         * @return The parsed OptionData instance, which can be further configured through setters
         */
        @Nonnull
        fun fromData(@Nonnull json: DataObject): OptionData {
            val name = json.getString("name")
            val description = json.getString("description")
            val type = OptionType.fromKey(json.getInt("type"))
            val option = OptionData(type, name, description)
            option.setRequired(json.getBoolean("required"))
            option.setAutoComplete(json.getBoolean("autocomplete"))
            if (type == OptionType.INTEGER || type == OptionType.NUMBER) {
                if (!json.isNull("min_value")) {
                    if (json.isType(
                            "min_value",
                            DataType.INT
                        )
                    ) option.setMinValue(json.getLong("min_value")) else if (json.isType(
                            "min_value",
                            DataType.FLOAT
                        )
                    ) option.setMinValue(json.getDouble("min_value"))
                }
                if (!json.isNull("max_value")) {
                    if (json.isType(
                            "max_value",
                            DataType.INT
                        )
                    ) option.setMaxValue(json.getLong("max_value")) else if (json.isType(
                            "max_value",
                            DataType.FLOAT
                        )
                    ) option.setMaxValue(json.getDouble("max_value"))
                }
            }
            if (type == OptionType.CHANNEL) {
                option.setChannelTypes(json.optArray("channel_types")
                    .map { it: DataArray ->
                        it.stream { obj: DataArray, index: Int? ->
                            obj.getInt(
                                index!!
                            )
                        }.map { obj: Int? -> ChannelType.fromId() }.collect(Collectors.toSet())
                    }
                    .orElse(emptySet<ChannelType>()))
            }
            if (type == OptionType.STRING) {
                if (!json.isNull("min_length")) option.setMinLength(json.getInt("min_length"))
                if (!json.isNull("max_length")) option.setMaxLength(json.getInt("max_length"))
            }
            json.optArray("choices").ifPresent { choices1: DataArray ->
                option.addChoices(choices1.stream { obj: DataArray, index: Int? ->
                    obj.getObject(
                        index!!
                    )
                }
                    .map { json: DataObject? ->
                        Command.Choice(
                            json!!
                        )
                    }
                    .collect(Collectors.toList())
                )
            }
            option.setNameLocalizations(LocalizationUtils.mapFromProperty(json, "name_localizations"))
            option.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(json, "description_localizations"))
            return option
        }

        /**
         * Converts the provided [Command.Option] into a OptionData instance.
         *
         * @param  option
         * The option to convert
         *
         * @throws IllegalArgumentException
         * If null is provided or the option has illegal configuration
         *
         * @return An instance of OptionData
         */
        @Nonnull
        fun fromOption(@Nonnull option: Command.Option): OptionData {
            Checks.notNull(option, "Option")
            val data = OptionData(option.type, option.name, option.description)
            data.setRequired(option.isRequired)
            data.setAutoComplete(option.isAutoComplete)
            data.addChoices(option.choices)
            data.setNameLocalizations(option.nameLocalizations.toMap())
            data.setDescriptionLocalizations(option.descriptionLocalizations.toMap())
            val min = option.minValue
            val max = option.maxValue
            val minLength = option.minLength
            val maxLength = option.maxLength
            when (option.type) {
                OptionType.CHANNEL -> data.setChannelTypes(option.channelTypes)
                OptionType.NUMBER -> {
                    if (min != null) data.setMinValue(min.toDouble())
                    if (max != null) data.setMaxValue(max.toDouble())
                }

                OptionType.INTEGER -> {
                    if (min != null) data.setMinValue(min.toLong())
                    if (max != null) data.setMaxValue(max.toLong())
                }

                OptionType.STRING -> {
                    if (minLength != null) data.setMinLength(minLength)
                    if (maxLength != null) data.setMaxLength(maxLength)
                }
            }
            return data
        }
    }
}
