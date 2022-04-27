package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Class which contains a mapping from {@link Locale} to a translated String, similar to a {@code Map<Locale, String>}.
 * <br>This is used for command localization.
 */
public class LocalizationMap implements SerializableData
{
    public static final Consumer<String> UNMODIFIABLE_CHECK = s -> { throw new IllegalStateException("This LocalizationMap is unmodifiable."); };

    private final Map<Locale, String> map = new HashMap<>();
    private final Consumer<String> checkConsumer;

    public LocalizationMap(@Nonnull Consumer<String> checkConsumer) {
        this.checkConsumer = checkConsumer;
    }

    private LocalizationMap(@Nonnull Consumer<String> checkConsumer, @Nonnull DataObject data)
    {
        this(checkConsumer);
        for (String key : data.keys())
            map.put(Locale.forLanguageTag(key), data.getString(key));
    }

    private LocalizationMap(Consumer<String> checkConsumer, LocalizationMap map)
    {
        this(checkConsumer);
        this.map.putAll(map.map); //This is safe as the LocalizationMap being given is already validated
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
    public static LocalizationMap fromProperty(@Nonnull DataObject json, @Nonnull String localizationProperty, @Nonnull Consumer<String> checkConsumer) {
        return json.optObject(localizationProperty)
                .map(data -> fromData(checkConsumer, data))
                .orElse(new LocalizationMap(checkConsumer));
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
     * Sets the given localized string to be used for the specified locales.
     *
     * @param  localizedString
     *         The localized string to use
     * @param  locales
     *         The locales on which to apply the localized string
     */
    public void setTranslations(@Nonnull String localizedString, @Nonnull Locale... locales)
    {
        checkConsumer.accept(localizedString);
        for (Locale locale : locales)
            map.put(locale, localizedString);
    }

    /**
     * Adds all the translations from the supplied map into this LocalizationMap.
     *
     * @param  map
     *         The map containing the localized strings
     */
    public void putTranslations(@Nonnull Map<Locale, String> map)
    {
        for (String localizedString : map.values())
            checkConsumer.accept(localizedString);
        this.map.putAll(map);
    }

    /**
     * Sets the given localized string to be used for the specified locale.
     *
     * @param  localizedString
     *         The localized string to use
     * @param  locale
     *         The locale on which to apply the localized string
     */
    public void setTranslation(@Nonnull String localizedString, @Nonnull Locale locale) {
        checkConsumer.accept(localizedString);
        map.put(locale, localizedString);
    }

    /**
     * Gets the localized string for the specified {@link Locale}.
     *
     * @param  locale
     *         The locale from which to get the localized string
     *
     * @return Possibly-null localized string
     */
    @Nullable
    public String get(@Nonnull Locale locale)
    {
        return map.get(locale);
    }

    /**
     * Gets the <b>unmodifiable</b> map representing this LocalizationMap.
     * <br>The changes on this LocalizationMap will be reflected on the returned map.
     *
     * @return The unmodifiable map of this LocalizationMap
     */
    @Nonnull
    public Map<Locale, String> toMap()
    {
        return Collections.unmodifiableMap(map);
    }
}
