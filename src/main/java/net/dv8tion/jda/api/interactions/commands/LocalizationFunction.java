package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import javax.annotation.Nonnull;
import java.util.Map;

public interface LocalizationFunction
{
    @Nonnull
    Map<DiscordLocale, String> apply(@Nonnull String localizationKey);
}
