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
package net.dv8tion.jda.api.interactions.commands.localization

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.JDALogger
import java.util.*
import java.util.function.Consumer
import javax.annotation.Nonnull

/**
 * Class which contains a mapping from [DiscordLocale] to a translated String, similar to a `Map<DiscordLocale, String>`.
 * <br></br>This is used for command, option, and choice localization.
 */
open class LocalizationMap(@param:Nonnull private val checkConsumer: Consumer<String>) : SerializableData {
    @JvmField
    protected val map: MutableMap<DiscordLocale, String> = HashMap()
    private fun putTranslation(locale: DiscordLocale, translation: String) {
        Checks.check(locale != DiscordLocale.UNKNOWN, "Cannot put an 'UNKNOWN' DiscordLocale")
        map[locale] = translation
    }

    @Nonnull
    override fun toData(): DataObject {
        val data = DataObject.empty()
        map.forEach { (locale: DiscordLocale, localizedString: String?) -> data.put(locale.locale, localizedString) }
        return data
    }

    /**
     * Sets the given localized string to be used for the specified locale.
     *
     * @param  locale
     * The locale on which to apply the localized string
     *
     * @param  localizedString
     * The localized string to use
     *
     * @throws IllegalArgumentException
     *
     *  * If the locale is null
     *  * If the localized string is null
     *  * If the locale is [DiscordLocale.UNKNOWN]
     *  * If the localized string does not pass the corresponding attribute check
     *
     */
    fun setTranslation(@Nonnull locale: DiscordLocale, @Nonnull localizedString: String) {
        Checks.notNull(locale, "Locale")
        Checks.notNull(localizedString, "Localized string")
        checkConsumer.accept(localizedString)
        putTranslation(locale, localizedString)
    }

    /**
     * Adds all the translations from the supplied map into this LocalizationMap.
     *
     * @param  map
     * The map containing the localized strings
     *
     * @throws IllegalArgumentException
     *
     *  * If the map is null
     *  * If the map contains an [DiscordLocale.UNKNOWN] key
     *  * If the map contains a localized string which does not pass the corresponding attribute check
     *
     */
    fun setTranslations(@Nonnull map: Map<DiscordLocale, String>) {
        Checks.notNull(map, "Map")
        map.forEach { (discordLocale: DiscordLocale, localizedString: String) ->
            checkConsumer.accept(localizedString)
            putTranslation(discordLocale, localizedString)
        }
    }

    /**
     * Gets the localized string for the specified [DiscordLocale].
     *
     * @param  locale
     * The locale from which to get the localized string
     *
     * @return Possibly-null localized string
     */
    operator fun get(@Nonnull locale: DiscordLocale): String? {
        Checks.notNull(locale, "Locale")
        return map[locale]
    }

    /**
     * Gets the **unmodifiable** map representing this LocalizationMap.
     * <br></br>The changes on this LocalizationMap will be reflected on the returned map.
     *
     * @return The unmodifiable map of this LocalizationMap
     */
    @Nonnull
    fun toMap(): Map<DiscordLocale, String> {
        return Collections.unmodifiableMap(map)
    }

    companion object {
        val LOG = JDALogger.getLog(LocalizationMap::class.java)
    }
}
