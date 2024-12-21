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
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Builder for describable Application Commands.
 *
 * @see net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
 * @see net.dv8tion.jda.api.interactions.commands.build.EntryPointCommandData
 */
public interface IDescribedCommandData extends INamedCommandData
{
    /**
     * The maximum length the description of a command can be. ({@value})
     */
    int MAX_DESCRIPTION_LENGTH = 100;

    @Nonnull
    @Override
    IDescribedCommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);

    @Nonnull
    @Override
    IDescribedCommandData setName(@Nonnull String name);

    @Nonnull
    @Override
    IDescribedCommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name);

    @Nonnull
    @Override
    IDescribedCommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map);

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-{@value #MAX_DESCRIPTION_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If the name is null or not between 1-{@value #MAX_DESCRIPTION_LENGTH} characters
     *
     * @return The builder, for chaining
     */
    @Nonnull
    IDescribedCommandData setDescription(@Nonnull String description);

    /**
     * Sets a {@link DiscordLocale language-specific} localizations of this command's description.
     *
     * @param  locale
     *         The locale to associate the translated description with
     *
     * @param  description
     *         The translated description to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the description is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the description does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    IDescribedCommandData setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description);

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this command's description.
     *
     * @param  map
     *         The map from which to transfer the translated descriptions
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a description which does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    IDescribedCommandData setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map);

    /**
     * The configured description
     *
     * @return The description
     */
    @Nonnull
    String getDescription();

    /**
     * The localizations of this command's description for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
     */
    @Nonnull
    LocalizationMap getDescriptionLocalizations();
}
