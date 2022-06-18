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

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Enum representing the locales that Discord supports.
 */
public enum DiscordLocale
{
    DANISH("da","Danish","Dansk"),
    GERMAN("de","German","Deutsch"),
    UK("en-GB","English, UK","English, UK"),
    US("en-US","English, US","English, US"),
    SPANISH("es-ES","Spanish","Español"),
    FRENCH("fr","French","Français"),
    CROATIAN("hr","Croatian","Hrvatski"),
    ITALIAN("it","Italian","Italiano"),
    LITHUANIAN("lt","Lithuanian","Lietuviškai"),
    HUNGARIAN("hu","Hungarian","Magyar"),
    DUTCH("nl","Dutch","Nederlands"),
    NORWEGIAN("no","Norwegian","Norsk"),
    POLISH("pl","Polish","Polski"),
    BRAZILIAN("pt-BR","Portuguese, Brazilian","Português do Brasil"),
    ROMANIA("ro","Romanian, Romania","Română"),
    FINNISH("fi","Finnish","Suomi"),
    SWEDISH("sv-SE","Swedish","Svenska"),
    VIETNAMESE("vi","Vietnamese","Tiếng Việt"),
    TURKISH("tr","Turkish","Türkçe"),
    CZECH("cs","Czech","Čeština"),
    GREEK("el","Greek","Ελληνικά"),
    BULGARIAN("bg","Bulgarian","български"),
    RUSSIAN("ru","Russian","Pусский"),
    UKRAINIAN("uk","Ukrainian","Українська"),
    HINDI("hi","Hindi","हिन्दी"),
    THAI("th","Thai","ไทย"),
    CHINA("zh-CN","Chinese, China","中文"),
    JAPANESE("ja","Japanese","日本語"),
    TAIWAN("zh-TW","Chinese, Taiwan","繁體中文"),
    KOREAN("ko","Korean","한국어"),
    UNKNOWN("unknown", "Unknown", "Unknown");

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
    public String getLocale()
    {
        return locale;
    }

    /**
     * The language's human-readable name, in English.
     *
     * @return The English language name
     */
    public String getLanguageName()
    {
        return languageName;
    }

    /**
     * The language's human-readable name, translated to the current language.
     *
     * @return The native language name
     */
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
     * @return The DiscordLocale constant or {@link #UNKNOWN}
     */
    @Nonnull
    public static DiscordLocale from(String localeTag) {
        for (DiscordLocale discordLocale : values())
        {
            if (discordLocale.locale.equals(localeTag)) {
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
     * @return The DiscordLocale constant or {@link #UNKNOWN}
     */
    @Nonnull
    public static DiscordLocale from(Locale locale) {
        return from(locale.toLanguageTag());
    }
}
