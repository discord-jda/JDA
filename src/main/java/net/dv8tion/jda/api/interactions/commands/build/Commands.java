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

package net.dv8tion.jda.api.interactions.commands.build;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides various static factory methods for building commands of different types.
 * This also includes context menu items.
 *
 * @see #slash(String, String)
 * @see #context(Command.Type, String)
 */
public class Commands
{
    /**
     * Maximum amount of global or guild slash commands
     */
    public static final int MAX_SLASH_COMMANDS = 100;

    /**
     * Maximum amount of global or guild user context commands
     */
    public static final int MAX_USER_COMMANDS = 5;

    /**
     * Maximum amount of global or guild message context commands
     */
    public static final int MAX_MESSAGE_COMMANDS = 5;

    /**
     * Create a slash command builder.
     *
     * @param name
     *        The command name, 1-32 lowercase alphanumeric characters
     * @param description
     *        The command description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If any of the following requirements are not met
     *         <ul>
     *             <li>The name must be lowercase alphanumeric (with dash), 1-32 characters long</li>
     *             <li>The description must be 1-100 characters long</li>
     *         </ul>
     *
     * @return {@link SlashCommandData} builder for slash commands
     */
    @Nonnull
    public static SlashCommandData slash(@Nonnull String name, @Nonnull String description)
    {
        return new CommandDataImpl(name, description);
    }

    /**
     * Create a message context menu command builder.
     *
     * @param  name
     *         The command name, 1-32 characters
     *
     * @throws IllegalArgumentException
     *         If the name is not between 1-32 characters long
     *
     * @return {@link CommandData}
     */
    @Nonnull
    public static CommandData message(@Nonnull String name)
    {
        return new CommandDataImpl(Command.Type.MESSAGE, name);
    }

    /**
     * Create a user context menu command builder.
     *
     * @param  name
     *         The command name, 1-32 characters
     *
     * @throws IllegalArgumentException
     *         If the name is not between 1-32 characters long
     *
     * @return {@link CommandData}
     */
    @Nonnull
    public static CommandData user(@Nonnull String name)
    {
        return new CommandDataImpl(Command.Type.USER, name);
    }

    /**
     * Create a context menu command builder for the specified command type.
     * <br>This cannot be used for slash commands, because they require a description.
     * Use {@link #slash(String, String)} to create a slash command.
     *
     * @param  type
     *         The command type, must not be {@link Command.Type#SLASH SLASH}
     * @param  name
     *         The command name, 1-32 characters
     *
     * @throws IllegalArgumentException
     *         If the name is not between 1-32 characters long, the type is {@link Command.Type#SLASH}, or the type is null.
     *
     * @return {@link CommandData}
     */
    @Nonnull
    public static CommandData context(@Nonnull Command.Type type, @Nonnull String name)
    {
        return new CommandDataImpl(type, name);
    }


    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br>This is the reverse function for {@link CommandData#toData()}.
     *
     * @param  array
     *         Array of serialized {@link DataObject} representing the commands
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed CommandData instances, which can be further configured through setters
     *
     * @see    CommandData#fromData(DataObject)
     * @see    SlashCommandData#fromData(DataObject)
     */
    @Nonnull
    public static List<CommandData> fromList(@Nonnull DataArray array)
    {
        Checks.notNull(array, "DataArray");
        return array.stream(DataArray::getObject)
                .map(CommandData::fromData)
                .collect(Collectors.toList());
    }

    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br>This is the reverse function for {@link CommandData#toData()}.
     *
     * @param  collection
     *         Collection of serialized {@link DataObject} representing the commands
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed CommandData instances, which can be further configured through setters
     *
     * @see    CommandData#fromData(DataObject)
     * @see    SlashCommandData#fromData(DataObject)
     */
    @Nonnull
    public static List<CommandData> fromList(@Nonnull Collection<? extends DataObject> collection)
    {
        Checks.noneNull(collection, "CommandData");
        return fromList(DataArray.fromCollection(collection));
    }
}
