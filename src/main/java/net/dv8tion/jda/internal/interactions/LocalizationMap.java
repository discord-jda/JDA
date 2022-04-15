package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationMap implements SerializableData
{
    private final DataObject object = DataObject.empty();

    @NotNull
    @Override
    public DataObject toData()
    {
        return object;
    }

    public void tryAddTranslation(@Nonnull ResourceBundle resourceBundle, Locale locale, @Nonnull String... keyComponents) {
        final String key = String.join(".", keyComponents);

        if (resourceBundle.containsKey(key))
        {
            object.put(locale.toLanguageTag(), resourceBundle.getString(key));
        }
    }
}
