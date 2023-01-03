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
     * Returns the name of the slash command.
     * <br>If used on a subcommand, it returns the subcommand name.
     * And when used on a subcommand group, it returns the group name.
     * <br>You can use {@link #getFullCommandName()} to get the full name including base command and group.
     *
     * @return the name of the slash command
     */
    @Nonnull
    String getName();

    /**
     * Returns the full command name, including possible subcommand name and subcommand group name.
     * <br>This is the name shown on messages or when writing the command in the text input.
     *
     * <p>Examples:
     * <ul>
     *     <li>When used on a normal base command,  the full name is the name itself, as in the name {@code "ban"} is equal to the full name {@code "ban"}.</li>
     *     <li>When used on a subcommand {@code "ban"}, of the base command {@code "mod"}, the full name resolves to {@code "mod ban"}</li>
     *     <li>When the subcommand is part of a subcommand group, {@code "action"} it resolves to {@code "mod action ban"}</li>
     * </ul>
     *
     * @return the full command name
     */
    @Nonnull
    String getFullCommandName();

    /**
     * {@inheritDoc}
     *
     * <p><b>This will only work on slash commands!</b>
     *
     * @throws IllegalStateException
     *         If the command is not a slash command (i.e. not of type {@link Command.Type#SLASH})
     */
    @Nonnull
    @Override
    default String getAsMention()
    {
        return "</" + getFullCommandName() + ":" + getIdLong() + ">";
    }
}
