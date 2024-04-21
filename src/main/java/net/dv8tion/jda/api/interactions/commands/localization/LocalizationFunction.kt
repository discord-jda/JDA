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
import javax.annotation.Nonnull

/**
 * Functional interface accepting a localization key (complete path used to get the appropriate translations)
 * and returning a map of discord locales to their localized strings
 *
 *
 *
 * **Implementation note:**
 * The localization key is composed of the command/option/choice tree being walked, where each command/option/choice's name is separated by a dot
 * <br></br>Additionally, there is an "options" key between the command path and the name of the option, as well as a "choices" key between the options and the choices
 * <br></br>Note: the final key is lowercase and spaces replaced by underscores
 * <br></br>
 * <br></br>A few examples of localization keys:
 *
 *  * The name of a command named "ban": `ban.name`
 *  * The name of a message context named "Get content raw": `get_content_raw.name`
 *  * The description of a command named "ban": `ban.description`
 *  * The name of a subcommand "perm" in a command named "ban": `ban.perm.name`
 *  * The description of an option "duration" in a subcommand "perm" in a command named "ban": `ban.perm.options.duration.description`
 *  * The name of a choice (here, "1 day") in an option "duration" in a subcommand "perm" in a command named "ban": `ban.perm.options.duration.choices.1_day.name`
 *
 *
 * <br></br>Extremely naive implementation of LocalizationFunction
 * <pre>`
 * public class MyFunction implements LocalizationFunction {
 * &#64;Override
 * public Map<DiscordLocale, String> apply(String localizationKey) {
 * Map<DiscordLocale, String> map = new HashMap<>();
 * switch (localizationKey) {
 * case "ban.name":
 * map.put(DiscordLocale.SPANISH, "prohibición");
 * map.put(DiscordLocale.FRENCH, "bannir");
 * break;
 * case "ban.description"
 * map.put(DiscordLocale.SPANISH, "Prohibir a un usuario del servidor");
 * map.put(DiscordLocale.FRENCH, "Bannir un utilisateur du serveur");
 * break;
 * //etc etc
 * }
 *
 * return map;
 * }
 * }
`</pre> *
 *
 * Also, since this is a functional interface, the following is also possible
 * <pre>`
 * LocalizationFunction myfunc = s -> {
 * Map<DiscordLocale, String> map = new HashMap<>();
 * switch (localizationKey) {
 * case "ban.name":
 * map.put(DiscordLocale.SPANISH, "prohibición");
 * map.put(DiscordLocale.FRENCH, "bannir");
 * break;
 * case "ban.description"
 * map.put(DiscordLocale.SPANISH, "Prohibir a un usuario del servidor");
 * map.put(DiscordLocale.FRENCH, "Bannir un utilisateur du serveur");
 * break;
 * //etc etc
 * }
 *
 * return map;
 * }
`</pre> *
 *
 *
 *
 * You can look at a complete localization example [here](https://github.com/discord-jda/JDA/blob/master/src/examples/java/LocalizationExample.java)
 *
 * @see ResourceBundleLocalizationFunction
 *
 * @see net.dv8tion.jda.api.interactions.commands.build.CommandData.setLocalizationFunction
 */
interface LocalizationFunction {
    /**
     * Retrieves the localization mappings of the specified localization key
     *
     * @param  localizationKey
     * The localization key to get the translations from
     *
     * @return Never-null map of discord locales to their localized strings
     */
    @Nonnull
    fun apply(@Nonnull localizationKey: String?): Map<DiscordLocale, String>
}
