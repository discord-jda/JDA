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

package net.dv8tion.jda.api.exceptions;

import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;

/**
 * Exception indicating that an error occurred while localizing an application command.
 *
 * <p>They are usually caused by invalid strings,
 * or exceptions in a {@link net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction LocalizationFunction}.
 *
 * @see net.dv8tion.jda.api.interactions.commands.build.CommandData#setLocalizationFunction(LocalizationFunction) CommandData.setLocalizationFunction(LocalizationFunction)
 */
public class LocalizationException extends RuntimeException
{
    public LocalizationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
