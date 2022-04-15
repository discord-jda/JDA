package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import java.util.Locale;

public class LocalizationMap implements SerializableData
{
    private final DataObject object = DataObject.empty();

    @Nonnull
    @Override
    public DataObject toData()
    {
        return object;
    }

    public void setTranslations(@Nonnull String string, @Nonnull Locale... locales) {
        for (Locale locale : locales)
        {
            object.put(locale.toLanguageTag(), string);
        }
    }
}
