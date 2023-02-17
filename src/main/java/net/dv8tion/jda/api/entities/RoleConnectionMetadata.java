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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A metadata record used for role connections.
 *
 * @see <a href="https://discord.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles" target="_blank">Configuring App Metadata for Linked Roles</a>
 * @see Role.RoleTags#isLinkedRole()
 */
public class RoleConnectionMetadata implements SerializableData
{
    /** The maximum length a name can be ({@value}) */
    public static final int MAX_NAME_LENGTH = 100;
    /** The maximum length a description can be ({@value}) */
    public static final int MAX_DESCRIPTION_LENGTH = 200;
    /** The maximum length a key can be ({@value}) */
    public static final int MAX_KEY_LENGTH = 50;
    /** The maximum number of records that can be configured ({@value}) */
    public static final int MAX_RECORDS = 5;

    private final MetadataType type;
    private final String key;
    private final String name;
    private final String description;
    private final LocalizationMap nameLocalization = new LocalizationMap(RoleConnectionMetadata::checkName);
    private final LocalizationMap descriptionLocalization = new LocalizationMap(RoleConnectionMetadata::checkDescription);

    /**
     * Creates a new RoleConnectionMetadata instance.
     *
     * @param type
     *        The {@link MetadataType}
     * @param name
     *        The display name of the metadata
     * @param key
     *        The key of the metadata (to update the value later)
     * @param description
     *        The description of the metadata
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If null is provided</li>
     *             <li>If the provided name is empty or more than {@value MAX_NAME_LENGTH} characters long</li>
     *             <li>If the provided key is empty or more than {@value MAX_KEY_LENGTH} characters long</li>
     *             <li>If the provided description is empty or more than {@value MAX_DESCRIPTION_LENGTH} characters long</li>
     *             <li>If the provided type is {@link MetadataType#UNKNOWN}</li>
     *             <li>If the provided key contains any characters other than {@code a-z}, {@code 0-9}, or {@code _}</li>
     *         </ul>
     */
    public RoleConnectionMetadata(@Nonnull MetadataType type, @Nonnull String name, @Nonnull String key, @Nonnull String description)
    {
        Checks.check(type != MetadataType.UNKNOWN, "Type must not be UNKNOWN");
        Checks.notNull(type, "Type");
        Checks.notNull(key, "Key");
        Checks.inRange(key, 1, MAX_KEY_LENGTH, "Key");
        Checks.matches(key, Checks.LOWERCASE_ASCII_ALPHANUMERIC, "Key");
        checkName(name);
        checkDescription(description);

        this.type = type;
        this.name = name;
        this.key = key;
        this.description = description;
    }

    private static void checkName(String name)
    {
        Checks.notNull(name, "Name");
        Checks.inRange(name, 1, MAX_NAME_LENGTH, "Name");
    }

    private static void checkDescription(String description)
    {
        Checks.notNull(description, "Description");
        Checks.inRange(description, 1, MAX_DESCRIPTION_LENGTH, "Description");
    }

    /**
     * The type of the metadata.
     *
     * @return The type, or {@link MetadataType#UNKNOWN} if unknown
     */
    @Nonnull
    public MetadataType getType()
    {
        return type;
    }

    /**
     * The display name of the metadata.
     *
     * @return The display name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The key of the metadata.
     *
     * @return The key
     */
    @Nonnull
    public String getKey()
    {
        return key;
    }

    /**
     * The description of the metadata.
     *
     * @return The description
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * The localizations of this record's name for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
     */
    @Nonnull
    public LocalizationMap getNameLocalizations()
    {
        return nameLocalization;
    }

    /**
     * The localizations of this record's description for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
     */
    @Nonnull
    public LocalizationMap getDescriptionLocalizations()
    {
        return descriptionLocalization;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this record's name.
     *
     * <p>This change will not take effect in Discord until you update the role connection metadata using {@link JDA#updateRoleConnectionMetadata(Collection)}.
     *
     * @param  locale
     *         The locale to associate the translated name with
     * @param  name
     *         The translated name to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the name is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the provided name is empty or more than {@value MAX_NAME_LENGTH} characters long</li>
     *         </ul>
     *
     * @return This updated record instance
     */
    @Nonnull
    public RoleConnectionMetadata setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
    {
        this.nameLocalization.setTranslation(locale, name);
        return this;
    }

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this record's name.
     *
     * <p>This change will not take effect in Discord until you update the role connection metadata using {@link JDA#updateRoleConnectionMetadata(Collection)}.
     *
     * @param  map
     *         The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a name which is empty or more than {@value MAX_NAME_LENGTH} characters long</li>
     *         </ul>
     *
     * @return This updated record instance
     */
    @Nonnull
    public RoleConnectionMetadata setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        this.nameLocalization.setTranslations(map);
        return this;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this record's description.
     *
     * <p>This change will not take effect in Discord until you update the role connection metadata using {@link JDA#updateRoleConnectionMetadata(Collection)}.
     *
     * @param  locale
     *         The locale to associate the translated description with
     * @param  description
     *         The translated description to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the description is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the provided description is empty or more than {@value MAX_DESCRIPTION_LENGTH} characters long</li>
     *         </ul>
     *
     * @return This updated record instance
     */
    @Nonnull
    public RoleConnectionMetadata setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description)
    {
        this.descriptionLocalization.setTranslation(locale, description);
        return this;
    }

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this record's description.
     *
     * <p>This change will not take effect in Discord until you update the role connection metadata using {@link JDA#updateRoleConnectionMetadata(Collection)}.
     *
     * @param  map
     *         The map from which to transfer the translated descriptions
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a description which is empty or more than {@value MAX_DESCRIPTION_LENGTH} characters long</li>
     *         </ul>
     *
     * @return This updated record instance
     */
    @Nonnull
    public RoleConnectionMetadata setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        this.descriptionLocalization.setTranslations(map);
        return this;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
            .setType(type)
            .setName(name)
            .addMetadata("key", key)
            .toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RoleConnectionMetadata))
            return false;
        if (this == obj)
            return true;
        RoleConnectionMetadata o = (RoleConnectionMetadata) obj;
        return this.type == o.type
            && this.key.equals(o.key)
            && this.name.equals(o.name)
            && this.description.equals(o.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, key, name, description);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
            .put("type", type.value)
            .put("name", name)
            .put("key", key)
            .put("description", description)
            .put("name_localizations", nameLocalization)
            .put("description_localizations", descriptionLocalization);
    }

    /**
     * Parses a {@link RoleConnectionMetadata} from a {@link DataObject}.
     * <br>This is the reverse of {@link #toData()}.
     *
     * @param  data
     *         The data object to parse values from#
     *
     * @throws IllegalArgumentException
     *         If the provided data object is null
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided data does not have a valid int type value
     *
     * @return The parsed metadata instance
     */
    @Nonnull
    public static RoleConnectionMetadata fromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "Data");
        RoleConnectionMetadata metadata = new RoleConnectionMetadata(
            MetadataType.fromValue(data.getInt("type")),
            data.getString("name", null),
            data.getString("key", null),
            data.getString("description", null)
        );
        return metadata.setNameLocalizations(LocalizationUtils.mapFromProperty(data, "name_localizations"))
                       .setDescriptionLocalizations(LocalizationUtils.mapFromProperty(data, "description_localizations"));
    }

    /**
     * The type of metadata.
     * <br>Each metadata type offers a comparison operation that allows guilds to configure role requirements based on metadata values stored by the bot.
     * Bots specify a <b>metadata value</b> for each user and guilds specify the required <b>guild's configured value</b> within the guild role settings.
     *
     * <p>For example, you could use {@link #INTEGER_GREATER_THAN_OR_EQUAL} on a connection to require a certain metadata value to be at least the desired minimum value.
     */
    public enum MetadataType
    {
        INTEGER_LESS_THAN_OR_EQUAL(1),
        INTEGER_GREATER_THAN_OR_EQUAL(2),
        INTEGER_EQUALS(3),
        INTEGER_NOT_EQUALS(4),
        DATETIME_LESS_THAN_OR_EQUAL(5),
        DATETIME_GREATER_THAN_OR_EQUAL(6),
        BOOLEAN_EQUAL(7),
        BOOLEAN_NOT_EQUAL(8),
        UNKNOWN(-1);

        private final int value;

        MetadataType(int value)
        {
            this.value = value;
        }


        /**
         * The raw value used by Discord.
         *
         * @return The raw value
         */
        public int getValue()
        {
            return value;
        }

        /**
         * The MetadataType for the provided raw value.
         *
         * @param  value
         *         The raw value
         *
         * @return The MetadataType for the provided raw value, or {@link #UNKNOWN} if none is found
         */
        @Nonnull
        public static MetadataType fromValue(int value)
        {
            for (MetadataType type : values())
            {
                if (type.value == value)
                    return type;
            }
            return UNKNOWN;
        }
    }
}
