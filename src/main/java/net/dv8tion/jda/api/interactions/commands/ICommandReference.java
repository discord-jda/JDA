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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.IMentionable;

import javax.annotation.Nonnull;

/**
 * Represents a mentionable slash command.
 */
public interface ICommandReference extends IMentionable
{
    /**
     * Returns the name of the slash command
     *
     * @return the name of the slash command
     */
    @Nonnull
    String getName();

    /**
     * {@inheritDoc}
     *
     * <p><b>This will only work on slash commands!</b>
     *
     * @throws IllegalStateException
     *         If the command is not a slash command (i.e. not of type {@link Command.Type#SLASH}
     */
    @Nonnull
    @Override
    default String getAsMention()
    {
        return "</" + getName() + ":" + getIdLong() + ">";
    }
}
