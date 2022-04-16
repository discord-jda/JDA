package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationMap implements SerializableData
{
    private final Map<Locale, String> map = new HashMap<>();

    public LocalizationMap() {}

    private LocalizationMap(@Nonnull DataObject data)
    {
        for (String key : data.keys())
            map.put(Locale.forLanguageTag(key), data.getString(key));
    }

    @Nonnull
    public static LocalizationMap fromData(@Nonnull DataObject data)
    {
        return new LocalizationMap(data);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject data = DataObject.empty();
        map.forEach((locale, value) -> data.put(locale.toLanguageTag(), value));
        return data;
    }

    public void setTranslations(@Nonnull String value, @Nonnull Locale... locales)
    {
        for (Locale locale : locales)
            map.put(locale, value);
    }

    public void setTranslation(@Nonnull String value, @Nonnull Locale locale) {
        map.put(locale, value);
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
