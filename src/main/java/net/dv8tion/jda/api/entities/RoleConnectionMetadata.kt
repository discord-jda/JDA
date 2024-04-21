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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.RoleConnectionMetadata.MetadataType
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils
import java.util.*
import javax.annotation.Nonnull

/**
 * A metadata record used for role connections.
 *
 * @see [Configuring App Metadata for Linked Roles](https://discord.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles)
 *
 * @see Role.RoleTags.isLinkedRole
 */
class RoleConnectionMetadata(
    @Nonnull type: MetadataType,
    @Nonnull name: String,
    @Nonnull key: String,
    @Nonnull description: String
) : SerializableData {
    /**
     * The type of the metadata.
     *
     * @return The type, or [MetadataType.UNKNOWN] if unknown
     */
    @get:Nonnull
    val type: MetadataType

    /**
     * The key of the metadata.
     *
     * @return The key
     */
    @get:Nonnull
    val key: String

    /**
     * The display name of the metadata.
     *
     * @return The display name
     */
    @get:Nonnull
    val name: String

    /**
     * The description of the metadata.
     *
     * @return The description
     */
    @get:Nonnull
    val description: String

    /**
     * The localizations of this record's name for [various languages][DiscordLocale].
     *
     * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized name
     */
    @get:Nonnull
    val nameLocalizations = LocalizationMap { name: String -> checkName(name) }

    /**
     * The localizations of this record's description for [various languages][DiscordLocale].
     *
     * @return The [LocalizationMap] containing the mapping from [DiscordLocale] to the localized description
     */
    @get:Nonnull
    val descriptionLocalizations = LocalizationMap { description: String -> checkDescription(description) }

    /**
     * Creates a new RoleConnectionMetadata instance.
     *
     * @param type
     * The [MetadataType]
     * @param name
     * The display name of the metadata
     * @param key
     * The key of the metadata (to update the value later)
     * @param description
     * The description of the metadata
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If null is provided
     *  * If the provided name is empty or more than {@value MAX_NAME_LENGTH} characters long
     *  * If the provided key is empty or more than {@value MAX_KEY_LENGTH} characters long
     *  * If the provided description is empty or more than {@value MAX_DESCRIPTION_LENGTH} characters long
     *  * If the provided type is [MetadataType.UNKNOWN]
     *  * If the provided key contains any characters other than `a-z`, `0-9`, or `_`
     *
     */
    init {
        Checks.check(type != MetadataType.UNKNOWN, "Type must not be UNKNOWN")
        Checks.notNull(type, "Type")
        Checks.notNull(key, "Key")
        Checks.inRange(key, 1, MAX_KEY_LENGTH, "Key")
        Checks.matches(key, Checks.LOWERCASE_ASCII_ALPHANUMERIC, "Key")
        checkName(name)
        checkDescription(description)
        this.type = type
        this.name = name
        this.key = key
        this.description = description
    }

    /**
     * Sets a [language-specific][DiscordLocale] localization of this record's name.
     *
     *
     * This change will not take effect in Discord until you update the role connection metadata using [JDA.updateRoleConnectionMetadata].
     *
     * @param  locale
     * The locale to associate the translated name with
     * @param  name
     * The translated name to put
     *
     * @throws IllegalArgumentException
     *
     *  * If the locale is null
     *  * If the name is null
     *  * If the locale is [DiscordLocale.UNKNOWN]
     *  * If the provided name is empty or more than {@value MAX_NAME_LENGTH} characters long
     *
     *
     * @return This updated record instance
     */
    @Nonnull
    fun setNameLocalization(@Nonnull locale: DiscordLocale?, @Nonnull name: String?): RoleConnectionMetadata {
        nameLocalizations.setTranslation(locale!!, name!!)
        return this
    }

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this record's name.
     *
     *
     * This change will not take effect in Discord until you update the role connection metadata using [JDA.updateRoleConnectionMetadata].
     *
     * @param  map
     * The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *
     *  * If the map is null
     *  * If the map contains an [DiscordLocale.UNKNOWN] key
     *  * If the map contains a name which is empty or more than {@value MAX_NAME_LENGTH} characters long
     *
     *
     * @return This updated record instance
     */
    @Nonnull
    fun setNameLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): RoleConnectionMetadata {
        nameLocalizations.setTranslations(map!!)
        return this
    }

    /**
     * Sets a [language-specific][DiscordLocale] localization of this record's description.
     *
     *
     * This change will not take effect in Discord until you update the role connection metadata using [JDA.updateRoleConnectionMetadata].
     *
     * @param  locale
     * The locale to associate the translated description with
     * @param  description
     * The translated description to put
     *
     * @throws IllegalArgumentException
     *
     *  * If the locale is null
     *  * If the description is null
     *  * If the locale is [DiscordLocale.UNKNOWN]
     *  * If the provided description is empty or more than {@value MAX_DESCRIPTION_LENGTH} characters long
     *
     *
     * @return This updated record instance
     */
    @Nonnull
    fun setDescriptionLocalization(
        @Nonnull locale: DiscordLocale?,
        @Nonnull description: String?
    ): RoleConnectionMetadata {
        descriptionLocalizations.setTranslation(locale!!, description!!)
        return this
    }

    /**
     * Sets multiple [language-specific][DiscordLocale] localizations of this record's description.
     *
     *
     * This change will not take effect in Discord until you update the role connection metadata using [JDA.updateRoleConnectionMetadata].
     *
     * @param  map
     * The map from which to transfer the translated descriptions
     *
     * @throws IllegalArgumentException
     *
     *  * If the map is null
     *  * If the map contains an [DiscordLocale.UNKNOWN] key
     *  * If the map contains a description which is empty or more than {@value MAX_DESCRIPTION_LENGTH} characters long
     *
     *
     * @return This updated record instance
     */
    @Nonnull
    fun setDescriptionLocalizations(@Nonnull map: Map<DiscordLocale?, String?>?): RoleConnectionMetadata {
        descriptionLocalizations.setTranslations(map!!)
        return this
    }

    override fun toString(): String {
        return EntityString(this)
            .setType(type)
            .setName(name)
            .addMetadata("key", key)
            .toString()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is RoleConnectionMetadata) return false
        if (this === obj) return true
        val o = obj
        return type == o.type && key == o.key && name == o.name && description == o.description
    }

    override fun hashCode(): Int {
        return Objects.hash(type, key, name, description)
    }

    @Nonnull
    override fun toData(): DataObject {
        return DataObject.empty()
            .put("type", type.value)
            .put("name", name)
            .put("key", key)
            .put("description", description)
            .put("name_localizations", nameLocalizations)
            .put("description_localizations", descriptionLocalizations)
    }

    /**
     * The type of metadata.
     * <br></br>Each metadata type offers a comparison operation that allows guilds to configure role requirements based on metadata values stored by the bot.
     * Bots specify a **metadata value** for each user and guilds specify the required **guild's configured value** within the guild role settings.
     *
     *
     * For example, you could use [.INTEGER_GREATER_THAN_OR_EQUAL] on a connection to require a certain metadata value to be at least the desired minimum value.
     */
    enum class MetadataType(
        /**
         * The raw value used by Discord.
         *
         * @return The raw value
         */
        val value: Int
    ) {
        INTEGER_LESS_THAN_OR_EQUAL(1),
        INTEGER_GREATER_THAN_OR_EQUAL(2),
        INTEGER_EQUALS(3),
        INTEGER_NOT_EQUALS(4),
        DATETIME_LESS_THAN_OR_EQUAL(5),
        DATETIME_GREATER_THAN_OR_EQUAL(6),
        BOOLEAN_EQUAL(7),
        BOOLEAN_NOT_EQUAL(8),
        UNKNOWN(-1);

        companion object {
            /**
             * The MetadataType for the provided raw value.
             *
             * @param  value
             * The raw value
             *
             * @return The MetadataType for the provided raw value, or [.UNKNOWN] if none is found
             */
            @Nonnull
            fun fromValue(value: Int): MetadataType {
                for (type in entries) {
                    if (type.value == value) return type
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /** The maximum length a name can be ({@value})  */
        const val MAX_NAME_LENGTH = 100

        /** The maximum length a description can be ({@value})  */
        const val MAX_DESCRIPTION_LENGTH = 200

        /** The maximum length a key can be ({@value})  */
        const val MAX_KEY_LENGTH = 50

        /** The maximum number of records that can be configured ({@value})  */
        const val MAX_RECORDS = 5
        private fun checkName(name: String) {
            Checks.notNull(name, "Name")
            Checks.inRange(name, 1, MAX_NAME_LENGTH, "Name")
        }

        private fun checkDescription(description: String) {
            Checks.notNull(description, "Description")
            Checks.inRange(description, 1, MAX_DESCRIPTION_LENGTH, "Description")
        }

        /**
         * Parses a [RoleConnectionMetadata] from a [DataObject].
         * <br></br>This is the reverse of [.toData].
         *
         * @param  data
         * The data object to parse values from#
         *
         * @throws IllegalArgumentException
         * If the provided data object is null
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided data does not have a valid int type value
         *
         * @return The parsed metadata instance
         */
        @JvmStatic
        @Nonnull
        fun fromData(@Nonnull data: DataObject): RoleConnectionMetadata {
            Checks.notNull(data, "Data")
            val metadata = RoleConnectionMetadata(
                MetadataType.fromValue(data.getInt("type")),
                data.getString("name", null),
                data.getString("key", null),
                data.getString("description", null)
            )
            return metadata.setNameLocalizations(LocalizationUtils.mapFromProperty(data, "name_localizations"))
                .setDescriptionLocalizations(LocalizationUtils.mapFromProperty(data, "description_localizations"))
        }
    }
}
