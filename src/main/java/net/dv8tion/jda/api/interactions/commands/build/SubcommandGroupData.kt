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
import net.dv8tion.jda.api.interactions.commands.Command.Subcommand
import net.dv8tion.jda.api.interactions.commands.Command.SubcommandGroup
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
 * Builder for a Slash-Command group.
 */
class SubcommandGroupData(@Nonnull name: String, @Nonnull description: String) : SerializableData {
    private val subcommands: MutableList<SubcommandData?> =
        ArrayList<SubcommandData?>(CommandData.Companion.MAX_OPTIONS)

    /**
     * The name for this subcommand group
     *
     * @return The name
     */
    @get:Nonnull
    var name: String
        private set

    /**
     * The description for this  subcommand group
     *
     * @return The description
     */
    @get:Nonnull
    var description: String
        private set

    /**
     * The localizations of this subcommand's name for [various languages][DiscordLocale] group.
     *
     * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized name
     */
    @get:Nonnull
    val nameLocalizations = LocalizationMap { name: String? -> checkName(name) }

    /**
     * The localizations of this subcommand's description for [various languages][DiscordLocale] group.
     *
     * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized description
     */
    @get:Nonnull
    val descriptionLocalizations = LocalizationMap { description: String? -> checkDescription(description) }

    /**
     * Create an group builder.
     *
     * @param name
     * The group name, 1-32 lowercase alphanumeric characters
     * @param description
     * The group description, 1-100 characters
     *
     * @throws IllegalArgumentException
     * If any of the following requirements are not met
     *
     *  * The name must be lowercase alphanumeric (with dash), 1-32 characters long
     *  * The description must be 1-100 characters long
     *
     */
    init {
        Checks.notEmpty(name, "Name")
        Checks.notEmpty(description, "Description")
        Checks.notLonger(name, 32, "Name")
        Checks.notLonger(description, 100, "Description")
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name")
        Checks.isLowercase(name, "Name")
        this.name = name
        this.description = description
    }

    protected fun checkName(@Nonnull name: String?) {
        Checks.notEmpty(name, "Name")
        Checks.notLonger(name, 32, "Name")
        Checks.isLowercase(name, "Name")
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name")
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
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    fun setName(@Nonnull name: String): SubcommandGroupData {
        checkName(name)
        this.name = name
        return this
    }

    /**
     * Sets a [language-specific][DiscordLocale] localization of this subcommand group's name.
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
    fun setNameLocalization(@Nonnull locale: DiscordLocale?, @Nonnull name: String?): SubcommandGroupData {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslation(locale!!, name!!)
        return this
    }

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this subcommand group's name.
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
    fun setNameLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): SubcommandGroupData {
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
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    fun setDescription(@Nonnull description: String): SubcommandGroupData {
        checkDescription(description)
        this.description = description
        return this
    }

    /**
     * Sets a [language-specific][DiscordLocale] localization of this subcommand group's description.
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
    fun setDescriptionLocalization(
        @Nonnull locale: DiscordLocale?,
        @Nonnull description: String?
    ): SubcommandGroupData {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslation(locale!!, description!!)
        return this
    }

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this subcommand group's description.
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
    fun setDescriptionLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): SubcommandGroupData {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslations(map!!)
        return this
    }

    /**
     * Removes all subcommands that evaluate to `true` under the provided `condition`.
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
    fun removeSubcommand(@Nonnull condition: Predicate<in SubcommandData?>?): Boolean {
        Checks.notNull(condition, "Condition")
        return subcommands.removeIf(condition!!)
    }

    /**
     * Removes subcommands by the provided name.
     *
     * @param  name
     * The **case-sensitive** subcommand name
     *
     * @return True, if any subcommands were removed
     */
    fun removeSubcommandByName(@Nonnull name: String): Boolean {
        return removeSubcommand { subcommand: SubcommandData? -> subcommand.getName() == name }
    }

    /**
     * The [Subcommands][SubcommandData] in this group.
     *
     * @return Immutable list of [SubcommandData]
     */
    @Nonnull
    fun getSubcommands(): List<SubcommandData?> {
        return Collections.unmodifiableList(subcommands)
    }

    /**
     * Add up to {@value CommandData#MAX_OPTIONS} [Subcommands][SubcommandData] to this group.
     *
     * @param  subcommands
     * The subcommands to add
     *
     * @throws IllegalArgumentException
     * If null, more than {@value CommandData#MAX_OPTIONS} subcommands, or duplicate subcommand names are provided.
     *
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    fun addSubcommands(@Nonnull vararg subcommands: SubcommandData?): SubcommandGroupData {
        Checks.noneNull(subcommands, "Subcommand")
        Checks.check(
            subcommands.size + this.subcommands.size <= CommandData.Companion.MAX_OPTIONS,
            "Cannot have more than %d subcommands in one group!",
            CommandData.Companion.MAX_OPTIONS
        )
        Checks.checkUnique(
            Stream.concat(getSubcommands().stream(), Arrays.stream(subcommands))
                .map { obj: SubcommandData? -> obj.getName() },
            "Cannot have multiple subcommands with the same name. Name: \"%s\" appeared %d times!"
        ) { count: Long?, value: String? -> arrayOf<Any?>(value, count) }
        this.subcommands.addAll(Arrays.asList(*subcommands))
        return this
    }

    /**
     * Add up to 25 [Subcommands][SubcommandData] to this group.
     *
     * @param  subcommands
     * The subcommands to add
     *
     * @throws IllegalArgumentException
     * If null, more than 25 subcommands, or duplicate subcommand names are provided.
     *
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    fun addSubcommands(@Nonnull subcommands: Collection<SubcommandData?>): SubcommandGroupData {
        Checks.noneNull(subcommands, "Subcommands")
        return addSubcommands(*subcommands.toTypedArray<SubcommandData?>())
    }

    @Nonnull
    override fun toData(): DataObject {
        return DataObject.empty()
            .put("type", OptionType.SUB_COMMAND_GROUP.key)
            .put("name", name)
            .put("name_localizations", nameLocalizations)
            .put("description", description)
            .put("description_localizations", descriptionLocalizations)
            .put("options", DataArray.fromCollection(subcommands))
    }

    companion object {
        /**
         * Parses the provided serialization back into an SubcommandGroupData instance.
         * <br></br>This is the reverse function for [.toData].
         *
         * @param  json
         * The serialized [DataObject] representing the group
         *
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the serialized object is missing required fields
         * @throws IllegalArgumentException
         * If any of the values are failing the respective checks such as length
         *
         * @return The parsed SubcommandGroupData instance, which can be further configured through setters
         */
        @Nonnull
        fun fromData(@Nonnull json: DataObject): SubcommandGroupData {
            val name = json.getString("name")
            val description = json.getString("description")
            val group = SubcommandGroupData(name, description)
            json.optArray("options").ifPresent { arr: DataArray ->
                arr.stream<DataObject> { obj: DataArray, index: Int? ->
                    obj.getObject(
                        index!!
                    )
                }
                    .map<SubcommandData>(Function<DataObject, SubcommandData> { json: DataObject ->
                        SubcommandData.Companion.fromData(
                            json
                        )
                    })
                    .forEach { subcommands: SubcommandData? -> group.addSubcommands(subcommands) }
            }
            group.setNameLocalizations(LocalizationUtils.mapFromProperty(json, "name_localizations"))
            group.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(json, "description_localizations"))
            return group
        }

        /**
         * Converts the provided [Command.SubcommandGroup] into a SubcommandGroupData instance.
         *
         * @param  group
         * The subcommand group to convert
         *
         * @throws IllegalArgumentException
         * If null is provided or the subcommand group has illegal configuration
         *
         * @return An instance of SubcommandGroupData
         */
        @Nonnull
        fun fromGroup(@Nonnull group: SubcommandGroup): SubcommandGroupData {
            Checks.notNull(group, "Subcommand Group")
            val data = SubcommandGroupData(group.name, group.description)
            data.setNameLocalizations(group.nameLocalizations.toMap())
            data.setDescriptionLocalizations(group.descriptionLocalizations.toMap())
            group.subcommands
                .stream()
                .map<SubcommandData>(Function<Subcommand, SubcommandData> { subcommand: Subcommand ->
                    SubcommandData.Companion.fromSubcommand(
                        subcommand
                    )
                })
                .forEach { subcommands: SubcommandData? -> data.addSubcommands(subcommands) }
            return data
        }
    }
}
