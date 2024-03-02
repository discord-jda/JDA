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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Enum representing the locales that Discord supports.
 *
 * <p>Based off <a href="https://discord.com/developers/docs/reference#locales" target="_blank">Discord's locale list</a>
 */
public enum DiscordLocale
{
    BULGARIAN           ("bg",      "Bulgarian",             "български"),
    CHINESE_CHINA       ("zh-CN",   "Chinese, China",        "中文"),
    CHINESE_TAIWAN      ("zh-TW",   "Chinese, Taiwan",       "繁體中文"),
    CROATIAN            ("hr",      "Croatian",              "Hrvatski"),
    CZECH               ("cs",      "Czech",                 "Čeština"),
    DANISH              ("da",      "Danish",                "Dansk"),
    DUTCH               ("nl",      "Dutch",                 "Nederlands"),
    ENGLISH_UK          ("en-GB",   "English, UK",           "English, UK"),
    ENGLISH_US          ("en-US",   "English, US",           "English, US"),
    FINNISH             ("fi",      "Finnish",               "Suomi"),
    FRENCH              ("fr",      "French",                "Français"),
    GERMAN              ("de",      "German",                "Deutsch"),
    GREEK               ("el",      "Greek",                 "Ελληνικά"),
    HINDI               ("hi",      "Hindi",                 "हिन्दी"),
    HUNGARIAN           ("hu",      "Hungarian",             "Magyar"),
    INDONESIAN          ("id",      "Indonesian",            "Bahasa Indonesia"),
    ITALIAN             ("it",      "Italian",               "Italiano"),
    JAPANESE            ("ja",      "Japanese",              "日本語"),
    KOREAN              ("ko",      "Korean",                "한국어"),
    LITHUANIAN          ("lt",      "Lithuanian",            "Lietuviškai"),
    NORWEGIAN           ("no",      "Norwegian",             "Norsk"),
    POLISH              ("pl",      "Polish",                "Polski"),
    PORTUGUESE_BRAZILIAN("pt-BR",   "Portuguese, Brazilian", "Português do Brasil"),
    ROMANIAN_ROMANIA    ("ro",      "Romanian, Romania",     "Română"),
    RUSSIAN             ("ru",      "Russian",               "Pусский"),
    SPANISH             ("es-ES",   "Spanish",               "Español"),
    SPANISH_LATAM       ("es-419",  "Spanish, LATAM",        "Español, LATAM"),
    SWEDISH             ("sv-SE",   "Swedish",               "Svenska"),
    THAI                ("th",      "Thai",                  "ไทย"),
    TURKISH             ("tr",      "Turkish",               "Türkçe"),
    UKRAINIAN           ("uk",      "Ukrainian",             "Українська"),
    VIETNAMESE          ("vi",      "Vietnamese",            "Tiếng Việt"),

    UNKNOWN             ("unknown", "Unknown",               "Unknown");

    private final String locale;
    private final String languageName;
    private final String nativeName;

    DiscordLocale(String locale, String languageName, String nativeName)
    {
        this.locale = locale;
        this.languageName = languageName;
        this.nativeName = nativeName;
    }

    /**
     * The locale tag, could be parsed by {@link Locale#forLanguageTag(String)}
     *
     * @return The locale tag
     */
    @Nonnull
    public String getLocale()
    {
        return locale;
    }

    /**
     * Creates a {@link Locale} from this DiscordLocale.
     * <br>This is a simple shortcut to {@link Locale#forLanguageTag(String) Locale.forLanguageTag(getLocale())}.
     *
     * <p><b>Note:</b> The returned {@link Locale} might not be the same locale
     * as the one passed in {@link #from(Locale) DiscordLocale#from(Locale)}.
     * See {@link Locale#forLanguageTag(String)} for more details.
     *
     * @return the {@link Locale} from this DiscordLocale
     *
     * @see    Locale#forLanguageTag(String)
     */
    @Nonnull
    public Locale toLocale()
    {
        return Locale.forLanguageTag(getLocale());
    }

    /**
     * The language's human-readable name, in English.
     *
     * @return The English language name
     */
    @Nonnull
    public String getLanguageName()
    {
        return languageName;
    }

    /**
     * The language's human-readable name, translated to the current language.
     *
     * @return The native language name
     */
    @Nonnull
    public String getNativeName()
    {
        return nativeName;
    }

    /**
     * Converts the provided locale tag (such as {@code en-GB} or {@code fr}) to the enum constant
     *
     * @param  localeTag
     *         The locale tag
     *
     * @throws IllegalArgumentException
     *         If the locale tag is null
     *
     * @return The DiscordLocale constant or {@link #UNKNOWN}
     */
    @Nonnull
    public static DiscordLocale from(@Nonnull String localeTag)
    {
        Checks.notNull(localeTag, "Locale tag");

        for (DiscordLocale discordLocale : values())
        {
            if (discordLocale.locale.equals(localeTag))
            {
                return discordLocale;
            }
        }

        return UNKNOWN;
    }

    /**
     * Converts the provided {@link Locale} to the enum constant
     *
     * @param  locale
     *         The locale
     *
     * @throws IllegalArgumentException
     *         If the locale is null
     *
     * @return The DiscordLocale constant or {@link #UNKNOWN}
     */
    @Nonnull
    public static DiscordLocale from(@Nonnull Locale locale)
    {
        Checks.notNull(locale, "Locale");

        return from(locale.toLanguageTag());
    }
}
