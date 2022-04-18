package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class which contains a mapping from {@link Locale} to a translated String, similar to a {@code Map<Locale, String>}.
 * <br>This is used for command localization.
 */
public class LocalizationMap implements SerializableData
{
    private final Map<Locale, String> map = new HashMap<>();

    public LocalizationMap() {}

    private LocalizationMap(@Nonnull DataObject data)
    {
        for (String key : data.keys())
            setTranslation(data.getString(key), Locale.forLanguageTag(key));
    }

    /**
     * Parses the provided serialization back into an LocalizationMap instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  data
     *         The serialized {@link DataObject} representing the localization map
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     *
     * @return The parsed LocalizationMap instance, which can be further configured through setters
     */
    @Nonnull
    public static LocalizationMap fromData(@Nonnull DataObject data)
    {
        return new LocalizationMap(data);
    }

    /**
     * Parses the provided localization property, in the serialization, back into an LocalizationMap instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  json
     *         The serialized {@link DataObject} containing the localization map
     * @param  localizationProperty
     *         The name of the property which represents the localization map
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     *
     * @return The parsed LocalizationMap instance, which can be further configured through setters
     */
    @Nonnull
    public static LocalizationMap fromProperty(@Nonnull DataObject json, @Nonnull String localizationProperty) {
        return json.optObject(localizationProperty)
                .map(LocalizationMap::fromData)
                .orElse(new LocalizationMap());
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject data = DataObject.empty();
        map.forEach((locale, localizedString) -> data.put(locale.toLanguageTag(), localizedString));
        return data;
    }

    /**
     * Sets the given localized string to be used for the specified locales
     *
     * @param  localizedString
     *         The localized string to use
     * @param  locales
     *         The locales on which to apply the localized strings
     */
    public void setTranslations(@Nonnull String localizedString, @Nonnull Locale... locales)
    {
        for (Locale locale : locales)
            map.put(locale, localizedString);
    }

    public void putTranslations(@Nonnull Map<Locale, String> map)
    {
        this.map.putAll(map);
    }

    public void setTranslation(@Nonnull String localizedString, @Nonnull Locale locale) {
        map.put(locale, localizedString);
    }

    @Nullable
    public String get(@Nonnull Locale locale)
    {
        return map.get(locale);
    }

    @Nonnull
    public Map<Locale, String> toMap()
    {
        return Collections.unmodifiableMap(map);
    }
}
