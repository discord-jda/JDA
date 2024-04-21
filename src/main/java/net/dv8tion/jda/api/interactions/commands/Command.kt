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
package net.dv8tion.jda.api.interactions.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CommandEditAction
import net.dv8tion.jda.api.utils.TimeUtil
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataType
import net.dv8tion.jda.internal.interactions.command.CommandImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils
import java.time.OffsetDateTime
import java.util.*
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a Discord slash-command.
 * <br></br>This can be used to edit or delete the command.
 *
 * @see Guild.retrieveCommandById
 * @see Guild.retrieveCommands
 */
interface Command : ISnowflake, ICommandReference {
    /**
     * Delete this command.
     *
     * @throws IllegalStateException
     * If this command is not owned by this bot
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): RestAction<Void?>?

    /**
     * Edit this command.
     * <br></br>This can be used to change the command attributes such as name or description.
     *
     * @throws IllegalStateException
     * If this command is not owned by this bot
     *
     * @return [CommandEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editCommand(): CommandEditAction?

    /**
     * Retrieves the [IntegrationPrivileges][IntegrationPrivilege] for this command.
     * <br></br>This is a shortcut for [Guild.retrieveIntegrationPrivilegesById].
     *
     *
     * Moderators of a guild can modify these privileges through the Integrations Menu
     *
     *
     * If there is no command with the provided ID,
     * this RestAction fails with [ErrorResponse.UNKNOWN_COMMAND][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_COMMAND]
     *
     * @param  guild
     * The target guild from which to retrieve the privileges
     *
     * @throws IllegalArgumentException
     * If the guild is null
     *
     * @return [RestAction] - Type: [List] of [IntegrationPrivilege]
     */
    @Nonnull
    @CheckReturnValue
    fun retrievePrivileges(@Nonnull guild: Guild?): RestAction<List<IntegrationPrivilege?>?>?

    @get:Nonnull
    val jDA: JDA?

    @get:Nonnull
    val type: Type?

    @get:Nonnull
    abstract override val name: String

    @get:Nonnull
    val nameLocalizations: LocalizationMap?

    @get:Nonnull
    val description: String?

    @get:Nonnull
    val descriptionLocalizations: LocalizationMap?

    @get:Nonnull
    val options: List<Option?>?

    @get:Nonnull
    val subcommands: List<Subcommand?>?

    @get:Nonnull
    val subcommandGroups: List<SubcommandGroup?>?

    /**
     * The id of the application this command belongs to.
     *
     * @return The application id
     */
    val applicationIdLong: Long

    @get:Nonnull
    val applicationId: String?
        /**
         * The id of the application this command belongs to.
         *
         * @return The application id
         */
        get() = java.lang.Long.toUnsignedString(applicationIdLong)

    /**
     * The version of this command.
     * <br></br>This changes when a command is updated through [upsertCommand][JDA.upsertCommand], [updateCommands][JDA.updateCommands], or [editCommandById][JDA.editCommandById]
     * <br></br>Useful for checking if command cache is outdated
     *
     * @return The version of the command as a snowflake id.
     *
     * @see .getTimeModified
     */
    val version: Long

    @get:Nonnull
    val timeModified: OffsetDateTime?
        /**
         * The time this command was updated last.
         *
         * @return Time this command was updated last.
         *
         * @see .getVersion
         */
        get() = TimeUtil.getTimeCreated(version)

    @get:Nonnull
    val defaultPermissions: DefaultMemberPermissions?

    /**
     * Whether the command can only be used inside a guild.
     * <br></br>Always true for guild commands.
     *
     * @return True, if this command is restricted to guilds.
     */
    val isGuildOnly: Boolean

    /**
     * Whether this command is restricted to NSFW (age-restricted) channels.
     *
     * @return True, if this command is NSFW
     *
     * @see [Age-Restricted Commands FAQ](https://support.discord.com/hc/en-us/articles/10123937946007)
     */
    val isNSFW: Boolean

    /**
     * Possible command types
     */
    enum class Type(
        /**
         * The raw command type id used in the API
         *
         * @return The command type id
         */
        @JvmField val id: Int
    ) {
        UNKNOWN(-1),
        SLASH(1),
        USER(2),
        MESSAGE(3);

        companion object {
            /**
             * Resolves the provided command type id to the enum constant
             *
             * @param  id
             * The command type id
             *
             * @return The type or [.UNKNOWN]
             */
            @JvmStatic
            @Nonnull
            fun fromId(id: Int): Type {
                for (type in entries) {
                    if (type.id == id) return type
                }
                return UNKNOWN
            }
        }
    }

    /**
     * Predefined choice used for options.
     *
     * @see net.dv8tion.jda.api.interactions.commands.build.OptionData.addChoices
     * @see net.dv8tion.jda.api.interactions.commands.build.OptionData.addChoices
     */
    class Choice {
        /**
         * The readable name of this choice.
         * <br></br>This is shown to the user in the official client.
         *
         * @return The choice name
         */
        @get:Nonnull
        var name: String? = null
            private set

        /**
         * The localizations of this choice's name for [various languages][DiscordLocale].
         *
         * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized name
         */
        @get:Nonnull
        val nameLocalizations = LocalizationMap { name: String -> checkName(name) }

        /**
         * The value of this choice.
         *
         * @return The long value
         */
        var asLong: Long = 0
            private set

        /**
         * The value of this choice.
         *
         * @return The double value, or NaN if this is not a numeric choice value
         */
        var asDouble = Double.NaN
            private set

        /**
         * The value of this choice.
         *
         * @return The String value
         */
        @get:Nonnull
        var asString: String? = null
            private set

        /**
         * The [OptionType] this choice is for
         *
         * @return The option type of this choice
         */
        @get:Nonnull
        var type: OptionType? = null
            private set

        /**
         * Create a Choice tuple
         *
         * @param name
         * The display name of this choice, must be less than 100 characters
         * @param value
         * The integer value you receive in a command option
         *
         * @throws IllegalArgumentException
         * If the name is null, empty, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
         * as defined by [.MAX_NAME_LENGTH]
         */
        constructor(@Nonnull name: String, value: Long) {
            setName(name)
            setIntValue(value)
        }

        /**
         * Create a Choice tuple
         *
         * @param name
         * The display name of this choice, must be less than 100 characters
         * @param value
         * The double value you receive in a command option
         *
         * @throws IllegalArgumentException
         * If the name is null, empty, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
         * as defined by [.MAX_NAME_LENGTH]
         */
        constructor(@Nonnull name: String, value: Double) {
            setName(name)
            setDoubleValue(value)
        }

        /**
         * Create a Choice tuple
         *
         * @param name
         * The display name of this choice, must be less than 100 characters
         * @param value
         * The string value you receive in a command option
         *
         * @throws IllegalArgumentException
         *
         *  * If the name is null, empty, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
         * as defined by [.MAX_NAME_LENGTH]
         *  * If the value is null or longer than {@value #MAX_STRING_VALUE_LENGTH} characters long,
         * as defined by [.MAX_STRING_VALUE_LENGTH]
         *
         */
        constructor(@Nonnull name: String, @Nonnull value: String) {
            setName(name)
            setStringValue(value)
        }

        /**
         * Create a Choice tuple
         *
         * @param json
         * The serialized choice instance with name and value mapping
         *
         * @throws IllegalArgumentException
         * If null is provided
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the data is not formatted correctly or missing required parameters
         */
        constructor(@Nonnull json: DataObject) {
            Checks.notNull(json, "DataObject")
            name = json.getString("name")
            if (json.isType("value", DataType.INT)) {
                setIntValue(json.getLong("value"))
            } else if (json.isType("value", DataType.FLOAT)) {
                setDoubleValue(json.getDouble("value"))
            } else {
                setStringValue(json.getString("value"))
            }
            setNameLocalizations(LocalizationUtils.mapFromProperty(json, "name_localizations"))
        }

        /**
         * Configure the choice name
         *
         * @param  name
         * The choice name, [1-100 characters long][.MAX_NAME_LENGTH]
         *
         * @throws IllegalArgumentException
         * If the name is null, empty, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
         * as defined by [.MAX_NAME_LENGTH]
         *
         * @return The Choice instance, for chaining
         */
        fun setName(@Nonnull name: String): Choice {
            checkName(name)
            this.name = name
            return this
        }

        /**
         * Sets the name localizations of this choice.
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
        fun setNameLocalization(@Nonnull locale: DiscordLocale?, @Nonnull name: String?): Choice {
            nameLocalizations.setTranslation(locale!!, name!!)
            return this
        }

        /**
         * Sets the name localizations of this choice.
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
        fun setNameLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): Choice {
            //Checks are done in LocalizationMap
            nameLocalizations.setTranslations(map)
            return this
        }

        override fun hashCode(): Int {
            return Objects.hash(name, asString)
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) return true
            if (obj !is Choice) return false
            val other = obj
            return other.name == name && other.asString == asString
        }

        override fun toString(): String {
            return EntityString(this)
                .setName(name!!)
                .addMetadata("value", asString)
                .toString()
        }

        private fun setIntValue(value: Long) {
            asDouble = value.toDouble()
            asLong = value
            asString = value.toString()
            type = OptionType.INTEGER
        }

        private fun setDoubleValue(value: Double) {
            asDouble = value
            asLong = value.toLong()
            asString = value.toString()
            type = OptionType.NUMBER
        }

        private fun setStringValue(@Nonnull value: String) {
            Checks.notLonger(value, MAX_STRING_VALUE_LENGTH, "Choice string value")
            asDouble = Double.NaN
            asLong = 0
            asString = value
            type = OptionType.STRING
        }

        private fun checkName(@Nonnull name: String) {
            Checks.notEmpty(name, "Choice name")
            Checks.notLonger(name, MAX_NAME_LENGTH, "Choice name")
        }

        @Nonnull
        fun toData(optionType: OptionType): DataObject {
            val value: Any?
            value =
                if (optionType == OptionType.INTEGER) asLong else if (optionType == OptionType.STRING) asString else if (optionType == OptionType.NUMBER) asDouble else throw IllegalArgumentException(
                    "Cannot transform choice into data for type $optionType"
                )
            return DataObject.empty()
                .put("name", name)
                .put("value", value)
                .put("name_localizations", nameLocalizations)
        }

        companion object {
            /**
             * The maximum length the name of a choice can be.
             */
            const val MAX_NAME_LENGTH = 100

            /**
             * The maximum length the [STRING][OptionType.STRING] value of a choice can be.
             */
            const val MAX_STRING_VALUE_LENGTH = 100
        }
    }

    /**
     * An Option for a command.
     */
    class Option(@Nonnull json: DataObject) {
        /**
         * The name of this option, subcommand, or subcommand group.
         *
         * @return The name
         */
        @get:Nonnull
        val name: String

        /**
         * The description of this option, subcommand, or subcommand group.
         *
         * @return The description
         */
        @get:Nonnull
        val description: String

        /**
         * The localizations of this option's name for [various languages][DiscordLocale].
         *
         * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized name
         */
        @get:Nonnull
        val nameLocalizations: LocalizationMap

        /**
         * The localizations of this option's description for [various languages][DiscordLocale].
         *
         * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized description
         */
        @get:Nonnull
        val descriptionLocalizations: LocalizationMap

        /**
         * The raw option type.
         *
         * @return The type
         */
        val typeRaw: Int

        /**
         * Whether this option is required
         *
         * @return True if this option is required
         */
        val isRequired: Boolean

        /**
         * Whether this option supports auto-complete
         *
         * @return True if this option supports auto-complete
         */
        val isAutoComplete: Boolean

        /**
         * The [ChannelTypes][ChannelType] this option is restricted to.
         * <br></br>This is empty if the option is not of type [CHANNEL][OptionType.CHANNEL] or not restricted to specific types.
         *
         * @return Immutable [Set] of [ChannelType]
         */
        @get:Nonnull
        val channelTypes: Set<ChannelType>

        /**
         * The predefined choices available for this option.
         * <br></br>If no choices are defined, this returns an empty list.
         *
         * @return Immutable [List] of [Choice]
         */
        @get:Nonnull
        val choices: List<Choice>

        /**
         * The minimum value which can be provided for this option.
         * <br></br>This returns `null` if the value is not set or if the option
         * is not of type [INTEGER][OptionType.INTEGER] or [NUMBER][OptionType.NUMBER].
         *
         * @return The minimum value for this option or `null`
         */
        var minValue: Number? = null

        /**
         * The maximum value which can be provided for this option.
         * <br></br>This returns `null` if the value is not set or if the option
         * is not of type [INTEGER][OptionType.INTEGER] or [NUMBER][OptionType.NUMBER].
         *
         * @return The maximum value for this option or `null`
         */
        var maxValue: Number? = null

        /**
         * The minimum length for strings which can be provided for this option.
         * <br></br>This returns `null` if the value is not set or if the option
         * is not of type [STRING][OptionType.STRING].
         *
         * @return The minimum length for strings for this option or `null`
         */
        var minLength: Int? = null

        /**
         * The maximum length for strings which can be provided for this option.
         * <br></br>This returns `null` if the value is not set or if the option
         * is not of type [STRING][OptionType.STRING].
         *
         * @return The maximum length for strings for this option or `null`
         */
        var maxLength: Int? = null

        init {
            name = json.getString("name")
            nameLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "name_localizations")
            description = json.getString("description")
            descriptionLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "description_localizations")
            typeRaw = json.getInt("type")
            isRequired = json.getBoolean("required")
            isAutoComplete = json.getBoolean("autocomplete")
            channelTypes = Collections.unmodifiableSet(json.optArray("channel_types")
                .map { it: DataArray ->
                    it.stream { obj: DataArray, index: Int? ->
                        obj.getInt(
                            index!!
                        )
                    }.map { obj: Int? -> ChannelType.fromId() }.collect(Collectors.toSet())
                }
                .orElse(emptySet()))
            choices = json.optArray("choices")
                .map { it: DataArray ->
                    it.stream { obj: DataArray, index: Int? ->
                        obj.getObject(
                            index!!
                        )
                    }.map { json: DataObject -> Choice(json) }
                        .collect(Collectors.toList())
                }
                .orElse(emptyList())
            if (!json.isNull("min_value")) minValue = json.getDouble("min_value")
            if (!json.isNull("max_value")) maxValue = json.getDouble("max_value")
            if (!json.isNull("min_length")) minLength = json.getInt("min_length")
            if (!json.isNull("max_length")) maxLength = json.getInt("max_length")
        }

        /**
         * The [OptionType].
         *
         * @return The type
         */
        @Nonnull
        fun getType(): OptionType {
            return OptionType.Companion.fromKey(typeRaw)
        }

        override fun hashCode(): Int {
            return Objects.hash(
                name,
                description,
                typeRaw,
                choices,
                channelTypes,
                minValue,
                maxValue,
                minLength,
                maxLength,
                isRequired,
                isAutoComplete
            )
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) return true
            if (obj !is Option) return false
            val other = obj
            return other.name == name && other.description == description && other.choices == choices && other.channelTypes == channelTypes && other.minValue == minValue && other.maxValue == maxValue && other.minLength == minLength && other.maxLength == maxLength && other.isRequired == isRequired && other.isAutoComplete == isAutoComplete && other.typeRaw == typeRaw
        }

        override fun toString(): String {
            return EntityString(this)
                .setType(getType())
                .addMetadata("name", name)
                .toString()
        }
    }

    /**
     * An Subcommand for a command.
     */
    class Subcommand(//Could be Command or SubcommandGroup
        private val parentCommand: ICommandReference, json: DataObject
    ) : ICommandReference {
        /**
         * The name of this subcommand.
         *
         * @return The name
         */
        @get:Nonnull
        override val name: String

        /**
         * The description of this subcommand.
         *
         * @return The description
         */
        @get:Nonnull
        val description: String

        /**
         * The localizations of this subcommands's name for [various languages][DiscordLocale].
         *
         * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized name
         */
        @get:Nonnull
        val nameLocalizations: LocalizationMap

        /**
         * The localizations of this subcommand's description for [various languages][DiscordLocale].
         *
         * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized description
         */
        @get:Nonnull
        val descriptionLocalizations: LocalizationMap

        /**
         * The options for this subcommand, or the subcommands within this group.
         *
         * @return Immutable list of Options
         */
        @get:Nonnull
        val options: List<Option>

        init {
            name = json.getString("name")
            nameLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "name_localizations")
            description = json.getString("description")
            descriptionLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "description_localizations")
            options = CommandImpl.parseOptions(json, CommandImpl.OPTION_TEST) { json: DataObject -> Option(json) }
        }

        /**
         * {@inheritDoc}
         *
         *
         * **This will return the ID of the top level command**
         */
        fun getIdLong(): Long {
            return parentCommand.idLong
        }

        @Nonnull
        override fun getFullCommandName(): String {
            return parentCommand.getFullCommandName() + " " + name
        }

        override fun hashCode(): Int {
            return Objects.hash(name, description, options)
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) return true
            if (obj !is Subcommand) return false
            val other = obj
            return other.name == name && other.description == description && other.options == options
        }

        override fun toString(): String {
            return EntityString(this)
                .addMetadata("name", name)
                .toString()
        }
    }

    /**
     * An Subcommand Group for a command.
     */
    class SubcommandGroup(private val parentCommand: Command, json: DataObject) : ICommandReference {
        /**
         * The name of this subcommand group.
         *
         * @return The name
         */
        @get:Nonnull
        override val name: String

        /**
         * The description of this subcommand group.
         *
         * @return The description
         */
        @get:Nonnull
        val description: String

        /**
         * The localizations of this subcommand group's name for [various languages][DiscordLocale].
         *
         * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized name
         */
        @get:Nonnull
        val nameLocalizations: LocalizationMap

        /**
         * The localizations of this subcommand group's description for [various languages][DiscordLocale].
         *
         * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized description
         */
        @get:Nonnull
        val descriptionLocalizations: LocalizationMap

        /**
         * The [Subcommands][Subcommand] in this group
         *
         * @return Immutable [List] of [Subcommand]
         */
        @get:Nonnull
        val subcommands: List<Subcommand>

        init {
            name = json.getString("name")
            nameLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "name_localizations")
            description = json.getString("description")
            descriptionLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "description_localizations")
            subcommands =
                CommandImpl.parseOptions(json, CommandImpl.SUBCOMMAND_TEST) { o: DataObject -> Subcommand(this, o) }
        }

        /**
         * {@inheritDoc}
         *
         *
         * **This will return the ID of the top level command**
         */
        fun getIdLong(): Long {
            return parentCommand.idLong
        }

        @Nonnull
        override fun getFullCommandName(): String {
            return parentCommand.getFullCommandName() + " " + name
        }

        override fun hashCode(): Int {
            return Objects.hash(name, description, subcommands)
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) return true
            if (obj !is SubcommandGroup) return false
            val other = obj
            return other.name == name && other.description == description && other.subcommands == subcommands
        }

        override fun toString(): String {
            return EntityString(this)
                .addMetadata("name", name)
                .toString()
        }
    }
}
