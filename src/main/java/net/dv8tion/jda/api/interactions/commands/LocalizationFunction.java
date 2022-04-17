package net.dv8tion.jda.api.interactions.commands;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;

public interface LocalizationFunction
{
    @Nonnull
    Map<Locale, String> apply(@Nonnull String localizationKey);
}
