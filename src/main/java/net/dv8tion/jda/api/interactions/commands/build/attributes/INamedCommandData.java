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

package net.dv8tion.jda.api.interactions.commands.build.attributes;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Builder for named Application Commands.
 *
 * @see net.dv8tion.jda.api.interactions.commands.build.CommandData
 * @see net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
 * @see net.dv8tion.jda.api.interactions.commands.build.EntryPointCommandData
 */
public interface INamedCommandData
{
    /**
     * The maximum length the name of a command can be. ({@value})
     */
    int MAX_NAME_LENGTH = 32;

    /**
     * The {@link Command.Type}
     *
     * @return The {@link Command.Type}
     */
    @Nonnull
    Command.Type getType();

    /**
     * Sets the {@link LocalizationFunction} for this command
     * <br>This enables you to have the entirety of this command to be localized.
     *
     * @param  localizationFunction
     *         The localization function
     *
     * @throws IllegalArgumentException
     *         If the localization function is null
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    INamedCommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);

    /**
     * Configure the command name.
     *
     * @param  name
     *         The name, 1-{@value #MAX_NAME_LENGTH} characters (lowercase and alphanumeric for {@link Command.Type#SLASH})
     *
     * @throws IllegalArgumentException
     *         If the name is not between 1-{@value #MAX_NAME_LENGTH} characters long, or not lowercase and alphanumeric for slash commands
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    INamedCommandData setName(@Nonnull String name);

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this command's name.
     *
     * @param  locale
     *         The locale to associate the translated name with
     *
     * @param  name
     *         The translated name to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the name is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the name does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    INamedCommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name);

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this command's name.
     *
     * @param  map
     *         The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a name which does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    INamedCommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map);

    /**
     * The current command name
     *
     * @return The command name
     */
    @Nonnull
    String getName();

    /**
     * The localizations of this command's name for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
     */
    @Nonnull
    LocalizationMap getNameLocalizations();
}
