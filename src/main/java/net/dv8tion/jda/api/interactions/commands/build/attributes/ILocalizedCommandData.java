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

import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;

import javax.annotation.Nonnull;

// This attribute exists so a [[IDescribedCommandData]] instance can be automatically localized,
// sharing the docs for both this attribute and also [[CommandData]].
/**
 * Builder for named Application Commands.
 *
 * @see net.dv8tion.jda.api.interactions.commands.build.CommandData
 * @see net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
 * @see net.dv8tion.jda.api.interactions.commands.build.PrimaryEntryPointCommandData
 */
public interface ILocalizedCommandData
{
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
    ILocalizedCommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);
}
