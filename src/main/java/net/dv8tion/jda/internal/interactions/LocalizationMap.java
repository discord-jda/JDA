package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationMap implements SerializableData
{
    private final DataObject data;

    public LocalizationMap()
    {
        data = DataObject.empty();
    }

    private LocalizationMap(DataObject data)
    {
        this.data = data;
    }

    @Nonnull
    public static LocalizationMap fromData(DataObject data)
    {
        return new LocalizationMap(data);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return data;
    }

    public void setTranslations(@Nonnull String string, @Nonnull Locale... locales)
    {
        for (Locale locale : locales)
        {
            data.put(locale.toLanguageTag(), string);
        }
    }

    public String get(@Nonnull String languageTag)
    {
        return data.getString(languageTag);
    }

    public Map<String, String> toMap()
    {
        final Map<String, String> map = new HashMap<>();

        for (String key : data.keys())
        {
            map.put(key, data.getString(key));
        }

        return map;
    }
}
