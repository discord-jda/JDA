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
import net.dv8tion.jda.api.interactions.commands.Command.SubcommandGroup
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils
import java.util.function.Function
import java.util.function.Predicate
import javax.annotation.Nonnull

/**
 * Extension of [CommandData] which allows setting slash-command specific settings such as options and subcommands.
 */
interface SlashCommandData : CommandData {
    @Nonnull
    override fun setLocalizationFunction(@Nonnull localizationFunction: LocalizationFunction?): SlashCommandData?
    @Nonnull
    override fun setName(@Nonnull name: String?): SlashCommandData?
    @Nonnull
    override fun setNameLocalization(@Nonnull locale: DiscordLocale?, @Nonnull name: String?): SlashCommandData?
    @Nonnull
    override fun setNameLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): SlashCommandData?
    @Nonnull
    override fun setDefaultPermissions(@Nonnull permission: DefaultMemberPermissions?): SlashCommandData?
    @Nonnull
    override fun setGuildOnly(guildOnly: Boolean): SlashCommandData?
    @Nonnull
    override fun setNSFW(nsfw: Boolean): SlashCommandData?

    /**
     * Configure the description
     *
     * @param  description
     * The description, 1-{@value #MAX_DESCRIPTION_LENGTH} characters
     *
     * @throws IllegalArgumentException
     * If the name is null or not between 1-{@value #MAX_DESCRIPTION_LENGTH} characters
     *
     * @return The builder, for chaining
     */
    @Nonnull
    fun setDescription(@Nonnull description: String?): SlashCommandData?

    /**
     * Sets a [language-specific][DiscordLocale] localizations of this command's description.
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
    fun setDescriptionLocalization(@Nonnull locale: DiscordLocale?, @Nonnull description: String?): SlashCommandData?

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this command's description.
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
    fun setDescriptionLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): SlashCommandData?

    @get:Nonnull
    val description: String?

    @JvmField
    @get:Nonnull
    val descriptionLocalizations: LocalizationMap?

    /**
     * Removes all options that evaluate to `true` under the provided `condition`.
     * <br></br>This will not affect options within subcommands.
     * Use [SubcommandData.removeOptions] instead.
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
    fun removeOptions(@Nonnull condition: Predicate<in OptionData>?): Boolean

    /**
     * Removes options by the provided name.
     * <br></br>This will not affect options within subcommands.
     * Use [SubcommandData.removeOptionByName] instead.
     *
     * @param  name
     * The **case-sensitive** option name
     *
     * @return True, if any options were removed
     */
    fun removeOptionByName(@Nonnull name: String): Boolean {
        return removeOptions { option: OptionData -> option.name == name }
    }

    /**
     * Removes all subcommands that evaluate to `true` under the provided `condition`.
     * <br></br>This will not apply to subcommands within subcommand groups.
     * Use [SubcommandGroupData.removeSubcommand] instead.
     *
     *
     * **Example: Remove all subcommands**
     * <pre>`command.removeSubcommands(subcommand -> true);
    `</pre> *
     *
     * @param  condition
     * The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     * If the condition is null
     *
     * @return True, if any subcommands were removed
     */
    fun removeSubcommands(@Nonnull condition: Predicate<in SubcommandData>?): Boolean

    /**
     * Removes subcommands by the provided name.
     * <br></br>This will not apply to subcommands within subcommand groups.
     * Use [SubcommandGroupData.removeSubcommandByName] instead.
     *
     * @param  name
     * The **case-sensitive** subcommand name
     *
     * @return True, if any subcommands were removed
     */
    fun removeSubcommandByName(@Nonnull name: String): Boolean {
        return removeSubcommands { subcommand: SubcommandData -> subcommand.getName() == name }
    }

    /**
     * Removes all subcommand groups that evaluate to `true` under the provided `condition`.
     *
     *
     * **Example: Remove all subcommand groups**
     * <pre>`command.removeSubcommandGroups(group -> true);
    `</pre> *
     *
     * @param  condition
     * The removal condition (must not throw)
     *
     * @throws IllegalArgumentException
     * If the condition is null
     *
     * @return True, if any subcommand groups were removed
     */
    fun removeSubcommandGroups(@Nonnull condition: Predicate<in SubcommandGroupData>?): Boolean

    /**
     * Removes subcommand groups by the provided name.
     *
     * @param  name
     * The **case-sensitive** subcommand group name
     *
     * @return True, if any subcommand groups were removed
     */
    fun removeSubcommandGroupByName(@Nonnull name: String): Boolean {
        return removeSubcommandGroups { group: SubcommandGroupData -> group.name == name }
    }

    @get:Nonnull
    val subcommands: List<SubcommandData?>?

    @get:Nonnull
    val subcommandGroups: List<SubcommandGroupData?>?

    @get:Nonnull
    val options: List<OptionData?>?

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this command.
     *
     *
     * Required options must be added before non-required options!
     *
     * @param  options
     * The [Options][OptionData] to add
     *
     * @throws IllegalArgumentException
     *
     *  * If there already is a subcommand or subcommand group on this command (See [.addSubcommands] for details).
     *  * If the option type is [OptionType.SUB_COMMAND] or [OptionType.SUB_COMMAND_GROUP].
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addOptions(@Nonnull vararg options: OptionData?): SlashCommandData?

    /**
     * Adds up to {@value CommandData#MAX_OPTIONS} options to this command.
     *
     *
     * Required options must be added before non-required options!
     *
     * @param  options
     * The [Options][OptionData] to add
     *
     * @throws IllegalArgumentException
     *
     *  * If there already is a subcommand or subcommand group on this command (See [.addSubcommands] for details).
     *  * If the option type is [OptionType.SUB_COMMAND] or [OptionType.SUB_COMMAND_GROUP].
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addOptions(@Nonnull options: Collection<OptionData?>): SlashCommandData? {
        Checks.noneNull(options, "Option")
        return addOptions(*options.toTypedArray<OptionData?>())
    }

    /**
     * Adds an option to this command.
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
     *  * If there already is a subcommand or subcommand group on this command (See [.addSubcommands] for details).
     *  * If the option type is [UNKNOWN][OptionType.UNKNOWN].
     *  * If the option type is [OptionType.SUB_COMMAND] or [OptionType.SUB_COMMAND_GROUP].
     *  * If the provided option type does not support auto-complete
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addOption(
        @Nonnull type: OptionType,
        @Nonnull name: String?,
        @Nonnull description: String?,
        required: Boolean,
        autoComplete: Boolean
    ): SlashCommandData? {
        return addOptions(
            OptionData(type, name, description)
                .setRequired(required)
                .setAutoComplete(autoComplete)
        )
    }

    /**
     * Adds an option to this command.
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
     *  * If there already is a subcommand or subcommand group on this command (See [.addSubcommands] for details).
     *  * If the option type is [UNKNOWN][OptionType.UNKNOWN].
     *  * If the option type is [OptionType.SUB_COMMAND] or [OptionType.SUB_COMMAND_GROUP].
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addOption(
        @Nonnull type: OptionType,
        @Nonnull name: String?,
        @Nonnull description: String?,
        required: Boolean
    ): SlashCommandData? {
        return addOption(type, name, description, required, false)
    }

    /**
     * Adds an option to this command.
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
     *  * If there already is a subcommand or subcommand group on this command (See [.addSubcommands] for details).
     *  * If the option type is [UNKNOWN][OptionType.UNKNOWN].
     *  * If the option type is [OptionType.SUB_COMMAND] or [OptionType.SUB_COMMAND_GROUP].
     *  * If this option is required and you already added a non-required option.
     *  * If more than {@value CommandData#MAX_OPTIONS} options are provided.
     *  * If the option name is not unique
     *  * If null is provided
     *
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addOption(@Nonnull type: OptionType, @Nonnull name: String?, @Nonnull description: String?): SlashCommandData? {
        return addOption(type, name, description, false)
    }

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} [Subcommands][SubcommandData] to this command.
     * <br></br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using [.addOptions] and [.addSubcommands] / [.addSubcommandGroups]
     * for the same command, is not supported.
     *
     *
     * Valid command layouts are as follows:
     * <pre>`command
     * |-- subcommand
     * |__ subcommand group
     * |__ subcommand
     *
     * command
     * |__ subcommand group
     * |__ subcommand
     *
     * command
     * |-- option
     * |__ option
    `</pre> *
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  subcommands
     * The subcommands to add
     *
     * @throws IllegalArgumentException
     * If null, more than {@value CommandData#MAX_OPTIONS} subcommands, or duplicate subcommand names are provided.
     * Also throws if you try adding subcommands when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addSubcommands(@Nonnull vararg subcommands: SubcommandData?): SlashCommandData?

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} [Subcommands][SubcommandData] to this command.
     * <br></br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using [.addOptions] and [.addSubcommands] / [.addSubcommandGroups]
     * for the same command, is not supported.
     *
     *
     * Valid command layouts are as follows:
     * <pre>`command
     * |-- subcommand
     * |__ subcommand group
     * |__ subcommand
     *
     * command
     * |__ subcommand group
     * |__ subcommand
     *
     * command
     * |-- option
     * |__ option
    `</pre> *
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  subcommands
     * The subcommands to add
     *
     * @throws IllegalArgumentException
     * If null, more than {@value CommandData#MAX_OPTIONS} subcommands, or duplicate subcommand names are provided.
     * Also throws if you try adding subcommands when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addSubcommands(@Nonnull subcommands: Collection<SubcommandData?>): SlashCommandData? {
        Checks.noneNull(subcommands, "Subcommands")
        return addSubcommands(*subcommands.toTypedArray<SubcommandData?>())
    }

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} [Subcommand-Groups][SubcommandGroupData] to this command.
     * <br></br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using [.addOptions] and [.addSubcommands] / [.addSubcommandGroups]
     * for the same command, is not supported.
     *
     *
     * Valid command layouts are as follows:
     * <pre>`command
     * |-- subcommand
     * |__ subcommand group
     * |__ subcommand
     *
     * command
     * |__ subcommand group
     * |__ subcommand
     *
     * command
     * |-- option
     * |__ option
    `</pre> *
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  groups
     * The subcommand groups to add
     *
     * @throws IllegalArgumentException
     * If null, more than {@value CommandData#MAX_OPTIONS} subcommand groups, or duplicate group names are provided.
     * Also throws if you try adding subcommand groups when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addSubcommandGroups(@Nonnull vararg groups: SubcommandGroupData?): SlashCommandData?

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} [Subcommand-Groups][SubcommandGroupData] to this command.
     * <br></br>When a subcommand or subcommand group is added, the base command itself cannot be used.
     * Thus using [.addOptions] and [.addSubcommands] / [.addSubcommandGroups]
     * for the same command, is not supported.
     *
     *
     * Valid command layouts are as follows:
     * <pre>`command
     * |-- subcommand
     * |__ subcommand group
     * |__ subcommand
     *
     * command
     * |__ subcommand group
     * |__ subcommand
     *
     * command
     * |-- option
     * |__ option
    `</pre> *
     *
     * Having an option and subcommand simultaneously is not allowed.
     *
     * @param  groups
     * The subcommand groups to add
     *
     * @throws IllegalArgumentException
     * If null, more than {@value CommandData#MAX_OPTIONS} subcommand groups, or duplicate group names are provided.
     * Also throws if you try adding subcommand groups when options are already present.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    fun addSubcommandGroups(@Nonnull groups: Collection<SubcommandGroupData?>): SlashCommandData? {
        Checks.noneNull(groups, "SubcommandGroups")
        return addSubcommandGroups(*groups.toTypedArray<SubcommandGroupData?>())
    }

    companion object {
        /**
         * Converts the provided [Command] into a SlashCommandData instance.
         *
         * @param  command
         * The command to convert
         *
         * @throws IllegalArgumentException
         * If null is provided or the command has illegal configuration
         *
         * @return An instance of SlashCommandData
         */
        @Nonnull
        fun fromCommand(@Nonnull command: Command): SlashCommandData? {
            Checks.notNull(command, "Command")
            require(command.type == Command.Type.SLASH) { "Cannot convert command of type " + command.type + " to SlashCommandData!" }
            val data = CommandDataImpl(command.name, command.description)
            data.setGuildOnly(command.isGuildOnly)
            data.setNSFW(command.isNSFW)
            data.setDefaultPermissions(command.defaultPermissions)
            //Command localizations are unmodifiable, make a copy
            data.setNameLocalizations(command.nameLocalizations.toMap())
            data.setDescriptionLocalizations(command.descriptionLocalizations.toMap())
            command.options
                .stream()
                .map<OptionData>(Function<Command.Option, OptionData> { option: Command.Option ->
                    OptionData.Companion.fromOption(
                        option
                    )
                })
                .forEach { options: OptionData? -> data.addOptions(options) }
            command.subcommands
                .stream()
                .map<SubcommandData>(Function<Subcommand, SubcommandData> { subcommand: Subcommand ->
                    SubcommandData.Companion.fromSubcommand(
                        subcommand
                    )
                })
                .forEach { subcommands: SubcommandData? -> data.addSubcommands(subcommands) }
            command.subcommandGroups
                .stream()
                .map<SubcommandGroupData>(Function<SubcommandGroup, SubcommandGroupData> { group: SubcommandGroup ->
                    SubcommandGroupData.Companion.fromGroup(
                        group
                    )
                })
                .forEach { groups: SubcommandGroupData? -> data.addSubcommandGroups(groups) }
            return data
        }

        /**
         * Parses the provided serialization back into a SlashCommandData instance.
         * <br></br>This is the reverse function for [SlashCommandData.toData].
         *
         * @param  object
         * The serialized [DataObject] representing the command
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the serialized object is missing required fields
         * @throws IllegalArgumentException
         * If any of the values are failing the respective checks such as length
         *
         * @return The parsed SlashCommandData instance, which can be further configured through setters
         *
         * @see CommandData.fromData
         * @see Commands.fromList
         */
        @Nonnull
        fun fromData(@Nonnull `object`: DataObject): SlashCommandData? {
            Checks.notNull(`object`, "DataObject")
            val name = `object`.getString("name")
            val commandType = Command.Type.fromId(`object`.getInt("type", 1))
            require(commandType == Command.Type.SLASH) { "Cannot convert command of type $commandType to SlashCommandData!" }
            val description = `object`.getString("description")
            val options = `object`.optArray("options").orElseGet { DataArray.empty() }
            val command = CommandDataImpl(name, description)
            command.setGuildOnly(!`object`.getBoolean("dm_permission", true))
            command.setNSFW(`object`.getBoolean("nsfw"))
            command.setDefaultPermissions(
                if (`object`.isNull("default_member_permissions")) DefaultMemberPermissions.ENABLED else DefaultMemberPermissions.enabledFor(
                    `object`.getLong("default_member_permissions")
                )
            )
            command.setNameLocalizations(LocalizationUtils.mapFromProperty(`object`, "name_localizations"))
            command.setDescriptionLocalizations(
                LocalizationUtils.mapFromProperty(
                    `object`,
                    "description_localizations"
                )
            )
            options.stream<DataObject> { obj: DataArray, index: Int? ->
                obj.getObject(
                    index!!
                )
            }.forEach { opt: DataObject ->
                val type = OptionType.fromKey(opt.getInt("type"))
                when (type) {
                    OptionType.SUB_COMMAND -> command.addSubcommands(SubcommandData.Companion.fromData(opt))
                    OptionType.SUB_COMMAND_GROUP -> command.addSubcommandGroups(
                        SubcommandGroupData.Companion.fromData(
                            opt
                        )
                    )

                    else -> command.addOptions(OptionData.Companion.fromData(opt))
                }
            }
            return command
        }
    }
}
