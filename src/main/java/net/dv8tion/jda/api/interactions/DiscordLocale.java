package net.dv8tion.jda.api.interactions;

import javax.annotation.Nullable;
import java.util.Locale;

//TODO docs
public enum DiscordLocale
{
    UK("en-GB","English, UK","English, UK"),
    US("en-US","English, US","English, US"),
    FRENCH("fr","French","Français");
    //TODO add langs

    private final String locale;
    private final String languageName;
    private final String nativeName;

    DiscordLocale(String locale, String languageName, String nativeName)
    {
        this.locale = locale;
        this.languageName = languageName;
        this.nativeName = nativeName;
    }

    //TODO docs
    public String getLocale()
    {
        return locale;
    }

    //TODO docs
    public String getLanguageName()
    {
        return languageName;
    }

    //TODO docs
    public String getNativeName()
    {
        return nativeName;
    }

    //TODO docs
    @Nullable
    public static DiscordLocale from(String localeTag) {
        for (DiscordLocale discordLocale : values())
        {
            if (discordLocale.locale.equals(localeTag)) {
                return discordLocale;
            }
        }

        return null;
    }

    //TODO docs
    @Nullable
    public static DiscordLocale from(Locale locale) {
        return from(locale.toLanguageTag());
    }
}
