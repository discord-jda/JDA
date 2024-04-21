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
package net.dv8tion.jda.api.interactions.commands.build

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.function.Function
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Provides various static factory methods for building commands of different types.
 * This also includes context menu items.
 *
 * @see .slash
 * @see .context
 */
object Commands {
    /**
     * Maximum amount of global or guild slash commands
     */
    const val MAX_SLASH_COMMANDS = 100

    /**
     * Maximum amount of global or guild user context commands
     */
    const val MAX_USER_COMMANDS = 5

    /**
     * Maximum amount of global or guild message context commands
     */
    const val MAX_MESSAGE_COMMANDS = 5

    /**
     * Create a slash command builder.
     *
     * @param name
     * The command name, 1-32 lowercase alphanumeric characters
     * @param description
     * The command description, 1-100 characters
     *
     * @throws IllegalArgumentException
     * If any of the following requirements are not met
     *
     *  * The name must be lowercase alphanumeric (with dash), 1-32 characters long
     *  * The description must be 1-100 characters long
     *
     *
     * @return [SlashCommandData] builder for slash commands
     */
    @JvmStatic
    @Nonnull
    fun slash(@Nonnull name: String?, @Nonnull description: String?): SlashCommandData {
        return CommandDataImpl(name!!, description!!)
    }

    /**
     * Create a message context menu command builder.
     *
     * @param  name
     * The command name, 1-32 characters
     *
     * @throws IllegalArgumentException
     * If the name is not between 1-32 characters long
     *
     * @return [CommandData]
     */
    @JvmStatic
    @Nonnull
    fun message(@Nonnull name: String?): CommandData {
        return CommandDataImpl(Command.Type.MESSAGE, name!!)
    }

    /**
     * Create a user context menu command builder.
     *
     * @param  name
     * The command name, 1-32 characters
     *
     * @throws IllegalArgumentException
     * If the name is not between 1-32 characters long
     *
     * @return [CommandData]
     */
    @JvmStatic
    @Nonnull
    fun user(@Nonnull name: String?): CommandData {
        return CommandDataImpl(Command.Type.USER, name!!)
    }

    /**
     * Create a context menu command builder for the specified command type.
     * <br></br>This cannot be used for slash commands, because they require a description.
     * Use [.slash] to create a slash command.
     *
     * @param  type
     * The command type, must not be [SLASH][Command.Type.SLASH]
     * @param  name
     * The command name, 1-32 characters
     *
     * @throws IllegalArgumentException
     * If the name is not between 1-32 characters long, the type is [Command.Type.SLASH], or the type is null.
     *
     * @return [CommandData]
     */
    @Nonnull
    fun context(@Nonnull type: Command.Type?, @Nonnull name: String?): CommandData {
        return CommandDataImpl(type!!, name!!)
    }

    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br></br>This is the reverse function for [CommandData.toData].
     *
     * @param  array
     * Array of serialized [DataObject] representing the commands
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the serialized object is missing required fields
     * @throws IllegalArgumentException
     * If any of the values are failing the respective checks such as length
     *
     * @return The parsed CommandData instances, which can be further configured through setters
     *
     * @see CommandData.fromData
     * @see SlashCommandData.fromData
     */
    @Nonnull
    fun fromList(@Nonnull array: DataArray): List<CommandData?> {
        Checks.notNull(array, "DataArray")
        return array.stream<DataObject> { obj: DataArray, index: Int? ->
            obj.getObject(
                index!!
            )
        }
            .map<CommandData?>(Function<DataObject, CommandData?> { `object`: DataObject ->
                CommandData.Companion.fromData(
                    `object`
                )
            })
            .collect(Collectors.toList<CommandData?>())
    }

    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br></br>This is the reverse function for [CommandData.toData].
     *
     * @param  collection
     * Collection of serialized [DataObject] representing the commands
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     * If the serialized object is missing required fields
     * @throws IllegalArgumentException
     * If any of the values are failing the respective checks such as length
     *
     * @return The parsed CommandData instances, which can be further configured through setters
     *
     * @see CommandData.fromData
     * @see SlashCommandData.fromData
     */
    @Nonnull
    fun fromList(@Nonnull collection: Collection<DataObject?>?): List<CommandData?> {
        Checks.noneNull(collection, "CommandData")
        return fromList(DataArray.fromCollection(collection!!))
    }
}
