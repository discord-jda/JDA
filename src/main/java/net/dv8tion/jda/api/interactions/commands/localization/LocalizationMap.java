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

package net.dv8tion.jda.api.interactions.commands.localization;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Class which contains a mapping from {@link DiscordLocale} to a translated String, similar to a {@code Map<DiscordLocale, String>}.
 * <br>This is used for command, option, and choice localization.
 */
public class LocalizationMap implements SerializableData
{
    public static final Logger LOG = JDALogger.getLog(LocalizationMap.class);
    public static final Consumer<String> UNMODIFIABLE_CHECK = s ->
    {
        throw new IllegalStateException("This LocalizationMap is unmodifiable.");
    };

    private final Map<DiscordLocale, String> map = new HashMap<>();
    private final Consumer<String> checkConsumer;

    public LocalizationMap(@Nonnull Consumer<String> checkConsumer)
    {
        this.checkConsumer = checkConsumer;
    }

    //Comes from Discord API
    private LocalizationMap(@Nonnull Consumer<String> checkConsumer, @Nonnull DataObject data)
    {
        this(checkConsumer);
        for (String key : data.keys())
        {
            final DiscordLocale locale = DiscordLocale.from(key);
            if (locale == DiscordLocale.UNKNOWN)
            {
                LOG.debug("Discord provided an unknown locale, locale tag: {}", key);
                continue;
            }

            map.put(locale, data.getString(key));
        }
    }

    //Comes from user deserialization
    private LocalizationMap(Consumer<String> checkConsumer, LocalizationMap map)
    {
        this(checkConsumer);
        this.map.putAll(map.map); //This is safe as the LocalizationMap being given is already validated
    }

    private void putTranslation(DiscordLocale locale, String translation)
    {
        Checks.check(locale != DiscordLocale.UNKNOWN, "Cannot put an 'UNKNOWN' DiscordLocale");
        this.map.put(locale, translation);
    }

    /**
     * Copies the provided LocalizationMap into a new one.
     * <br>This might be useful if you want to make a LocalizationMap modifiable again, with a valid check.
     * <br>This is mostly used internally.
     *
     * @param  checkConsumer
     *         The check to run on every localization entry insertion
     * @param  map
     *         The map from which to get the localization entries from
     *
     * @return The copied LocalizationMap instance, which can be further configured through setters
     */
    @Nonnull
    public static LocalizationMap fromMap(@Nonnull Consumer<String> checkConsumer, @Nonnull LocalizationMap map)
    {
        Checks.notNull(checkConsumer, "Check consumer");
        Checks.notNull(map, "Localization map");

        return new LocalizationMap(checkConsumer, map);
    }

    /**
     * Parses the provided serialization back into an LocalizationMap instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  checkConsumer
     *         The check to run on every localization entry insertion
     * @param  data
     *         The serialized {@link DataObject} representing the localization map
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     *
     * @return The parsed LocalizationMap instance, which can be further configured through setters
     */
    @Nonnull
    public static LocalizationMap fromData(@Nonnull Consumer<String> checkConsumer, @Nonnull DataObject data)
    {
        Checks.notNull(checkConsumer, "Check consumer");
        Checks.notNull(data, "Data object");

        return new LocalizationMap(checkConsumer, data);
    }

    /**
     * Parses the provided localization property, in the serialization, back into an LocalizationMap instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  json
     *         The serialized {@link DataObject} containing the localization map
     * @param  localizationProperty
     *         The name of the property which represents the localization map
     * @param  checkConsumer
     *         The check to run on every localization entry insertion
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     *
     * @return The parsed LocalizationMap instance, which can be further configured through setters
     */
    @Nonnull
    public static LocalizationMap fromProperty(@Nonnull DataObject json, @Nonnull String localizationProperty, @Nonnull Consumer<String> checkConsumer)
    {
        Checks.notNull(json, "Data object");
        Checks.notNull(localizationProperty, "Localization property");
        Checks.notNull(checkConsumer, "Check consumer");

        return json.optObject(localizationProperty)
                .map(data -> fromData(checkConsumer, data))
                .orElse(new LocalizationMap(checkConsumer));
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject data = DataObject.empty();
        map.forEach((locale, localizedString) -> data.put(locale.getLocale(), localizedString));
        return data;
    }

    /**
     * Sets the given localized string to be used for the specified locales.
     *
     * @param  locale
     *         The locale on which to apply the localized string
     *
     * @param  localizedString
     *         The localized string to use
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the localized string is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the localized string does not pass the corresponding attribute check</li>
     *         </ul>
     */
    public void setTranslation(@Nonnull DiscordLocale locale, @Nonnull String localizedString)
    {
        Checks.notNull(locale, "Locale");
        Checks.notNull(localizedString, "Localized string");

        checkConsumer.accept(localizedString);
        putTranslation(locale, localizedString);
    }

    /**
     * Adds all the translations from the supplied map into this LocalizationMap.
     *
     * @param  map
     *         The map containing the localized strings
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *         </ul>
     */
    public void setTranslations(@Nonnull Map<DiscordLocale, String> map)
    {
        Checks.notNull(map, "Map");

        map.forEach((discordLocale, localizedString) ->
        {
            checkConsumer.accept(localizedString);
            putTranslation(discordLocale, localizedString);
        });
    }

    /**
     * Gets the localized string for the specified {@link DiscordLocale}.
     *
     * @param  locale
     *         The locale from which to get the localized string
     *
     * @return Possibly-null localized string
     */
    @Nullable
    public String get(@Nonnull DiscordLocale locale)
    {
        Checks.notNull(locale, "Locale");

        return map.get(locale);
    }

    /**
     * Gets the <b>unmodifiable</b> map representing this LocalizationMap.
     * <br>The changes on this LocalizationMap will be reflected on the returned map.
     *
     * @return The unmodifiable map of this LocalizationMap
     */
    @Nonnull
    public Map<DiscordLocale, String> toMap()
    {
        return Collections.unmodifiableMap(map);
    }
}
