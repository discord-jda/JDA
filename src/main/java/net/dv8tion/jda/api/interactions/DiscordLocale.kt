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
package net.dv8tion.jda.api.interactions

import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

/**
 * Enum representing the locales that Discord supports.
 *
 *
 * Based off [Discord's locale list](https://discord.com/developers/docs/reference#locales)
 */
enum class DiscordLocale(
    /**
     * The locale tag, could be parsed by [Locale.forLanguageTag]
     *
     * @return The locale tag
     */
    @JvmField @get:Nonnull val locale: String,
    /**
     * The language's human-readable name, in English.
     *
     * @return The English language name
     */
    @get:Nonnull val languageName: String,
    /**
     * The language's human-readable name, translated to the current language.
     *
     * @return The native language name
     */
    @get:Nonnull val nativeName: String
) {
    BULGARIAN("bg", "Bulgarian", "български"),
    CHINESE_CHINA("zh-CN", "Chinese, China", "中文"),
    CHINESE_TAIWAN("zh-TW", "Chinese, Taiwan", "繁體中文"),
    CROATIAN("hr", "Croatian", "Hrvatski"),
    CZECH("cs", "Czech", "Čeština"),
    DANISH("da", "Danish", "Dansk"),
    DUTCH("nl", "Dutch", "Nederlands"),
    ENGLISH_UK("en-GB", "English, UK", "English, UK"),
    ENGLISH_US("en-US", "English, US", "English, US"),
    FINNISH("fi", "Finnish", "Suomi"),
    FRENCH("fr", "French", "Français"),
    GERMAN("de", "German", "Deutsch"),
    GREEK("el", "Greek", "Ελληνικά"),
    HINDI("hi", "Hindi", "हिन्दी"),
    HUNGARIAN("hu", "Hungarian", "Magyar"),
    INDONESIAN("id", "Indonesian", "Bahasa Indonesia"),
    ITALIAN("it", "Italian", "Italiano"),
    JAPANESE("ja", "Japanese", "日本語"),
    KOREAN("ko", "Korean", "한국어"),
    LITHUANIAN("lt", "Lithuanian", "Lietuviškai"),
    NORWEGIAN("no", "Norwegian", "Norsk"),
    POLISH("pl", "Polish", "Polski"),
    PORTUGUESE_BRAZILIAN("pt-BR", "Portuguese, Brazilian", "Português do Brasil"),
    ROMANIAN_ROMANIA("ro", "Romanian, Romania", "Română"),
    RUSSIAN("ru", "Russian", "Pусский"),
    SPANISH("es-ES", "Spanish", "Español"),
    SPANISH_LATAM("es-419", "Spanish, LATAM", "Español, LATAM"),
    SWEDISH("sv-SE", "Swedish", "Svenska"),
    THAI("th", "Thai", "ไทย"),
    TURKISH("tr", "Turkish", "Türkçe"),
    UKRAINIAN("uk", "Ukrainian", "Українська"),
    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt"),
    UNKNOWN("unknown", "Unknown", "Unknown");

    /**
     * Creates a [Locale] from this DiscordLocale.
     * <br></br>This is a simple shortcut to [Locale.forLanguageTag(getLocale())][Locale.forLanguageTag].
     *
     *
     * **Note:** The returned [Locale] might not be the same locale
     * as the one passed in [DiscordLocale#from(Locale)][.from].
     * See [Locale.forLanguageTag] for more details.
     *
     * @return the [Locale] from this DiscordLocale
     *
     * @see Locale.forLanguageTag
     */
    @Nonnull
    fun toLocale(): Locale {
        return Locale.forLanguageTag(locale)
    }

    companion object {
        /**
         * Converts the provided locale tag (such as `en-GB` or `fr`) to the enum constant
         *
         * @param  localeTag
         * The locale tag
         *
         * @throws IllegalArgumentException
         * If the locale tag is null
         *
         * @return The DiscordLocale constant or [.UNKNOWN]
         */
        @JvmStatic
        @Nonnull
        fun from(@Nonnull localeTag: String): DiscordLocale {
            Checks.notNull(localeTag, "Locale tag")
            for (discordLocale in entries) {
                if (discordLocale.locale == localeTag) {
                    return discordLocale
                }
            }
            return UNKNOWN
        }

        /**
         * Converts the provided [Locale] to the enum constant
         *
         * @param  locale
         * The locale
         *
         * @throws IllegalArgumentException
         * If the locale is null
         *
         * @return The DiscordLocale constant or [.UNKNOWN]
         */
        @Nonnull
        fun from(@Nonnull locale: Locale): DiscordLocale {
            Checks.notNull(locale, "Locale")
            return from(locale.toLanguageTag())
        }
    }
}
