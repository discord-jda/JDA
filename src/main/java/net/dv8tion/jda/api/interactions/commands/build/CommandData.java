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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Builder for Application Commands.
 * <br>Use the factory methods provided by {@link Commands} to create instances of this interface.
 *
 * @see Commands
 */
public interface CommandData extends SerializableData
{
    /**
     * The maximum length the name of a command can be.
     */
    int MAX_NAME_LENGTH = 32;

    /**
     * The maximum length the description of a command can be.
     */
    int MAX_DESCRIPTION_LENGTH = 100;

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
    CommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);

    /**
     * Configure the command name.
     *
     * @param  name
     *         The name, 1-32 characters (lowercase and alphanumeric for {@link Command.Type#SLASH})
     *
     * @throws IllegalArgumentException
     *         If the name is not between 1-32 characters long, or not lowercase and alphanumeric for slash commands
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setName(@Nonnull String name);

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
    CommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name);

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
    CommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map);

    /**
     * Whether this command is available to everyone by default.
     * <br>If this is disabled, you need to explicitly whitelist users and roles per guild.
     *
     * <p>You can use {@link CommandPrivilege} to enable or disable this command per guild for roles and members of the guild.
     * See {@link Command#updatePrivileges(Guild, CommandPrivilege...)} and {@link Guild#updateCommandPrivileges(Map)}.
     *
     * @param  enabled
     *         True, if this command is enabled by default for everyone. (Default: true)
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setDefaultEnabled(boolean enabled);

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

    /**
     * Whether this command is available to everyone by default.
     *
     * @return True, if this command is enabled to everyone by default
     *
     * @see    #setDefaultEnabled(boolean)
     * @see    CommandPrivilege
     */
    boolean isDefaultEnabled();

    /**
     * The {@link Command.Type}
     *
     * @return The {@link Command.Type}
     */
    @Nonnull
    Command.Type getType();

    /**
     * Converts the provided {@link Command} into a CommandData instance.
     *
     * @param  command
     *         The command to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the command has illegal configuration
     *
     * @return An instance of CommandData
     *
     * @see    SlashCommandData#fromCommand(Command)
     */
    @Nonnull
    static CommandData fromCommand(@Nonnull Command command)
    {
        Checks.notNull(command, "Command");
        if (command.getType() != Command.Type.SLASH)
        {
            final CommandDataImpl data = new CommandDataImpl(command.getType(), command.getName());
            return data.setDefaultEnabled(command.isDefaultEnabled())
                    .setNameLocalizations(command.getNameLocalizations().toMap())
                    .setDescriptionLocalizations(command.getDescriptionLocalizations().toMap());
        }

        return SlashCommandData.fromCommand(command);
    }

    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br>This is the reverse function for {@link CommandData#toData()}.
     *
     * @param  object
     *         The serialized {@link DataObject} representing the command
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed CommandData instance, which can be further configured through setters
     *
     * @see    SlashCommandData#fromData(DataObject)
     * @see    Commands#fromList(Collection)
     */
    @Nonnull
    static CommandData fromData(@Nonnull DataObject object)
    {
        Checks.notNull(object, "DataObject");
        String name = object.getString("name");
        Command.Type commandType = Command.Type.fromId(object.getInt("type", 1));
        if (commandType != Command.Type.SLASH)
        {
            final CommandDataImpl data = new CommandDataImpl(commandType, name);
            data.setNameLocalizations(LocalizationUtils.mapFromProperty(object, "name_localizations"));
            data.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(object, "description_localizations"));

            return data;
        }

        return SlashCommandData.fromData(object);
    }
}
