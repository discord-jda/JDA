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

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.attributes.IAgeRestrictedCommandData;
import net.dv8tion.jda.api.interactions.commands.build.attributes.INamedCommandData;
import net.dv8tion.jda.api.interactions.commands.build.attributes.IRestrictedCommandData;
import net.dv8tion.jda.api.interactions.commands.build.attributes.IScopedCommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
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
public interface CommandData
        extends INamedCommandData, IScopedCommandData, IRestrictedCommandData, IAgeRestrictedCommandData,
        SerializableData
{
    /**
     * The maximum amount of options/subcommands/groups that can be added to a command or subcommand. ({@value})
     */
    int MAX_OPTIONS = 25;

    @Nonnull
    @Override
    CommandData setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction);

    @Nonnull
    @Override
    CommandData setName(@Nonnull String name);

    @Nonnull
    @Override
    CommandData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name);

    @Nonnull
    @Override
    CommandData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map);

    @Nonnull
    @Override
    CommandData setDefaultPermissions(@Nonnull DefaultMemberPermissions permission);

    @Nonnull
    @Override
    CommandData setGuildOnly(boolean guildOnly);

    @Nonnull
    @Override
    CommandData setNSFW(boolean nsfw);

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
            return data.setDefaultPermissions(command.getDefaultPermissions())
                    .setGuildOnly(command.isGuildOnly())
                    .setNSFW(command.isNSFW())
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
            CommandDataImpl data = new CommandDataImpl(commandType, name);
            if (!object.isNull("default_member_permissions"))
            {
                long defaultPermissions = object.getLong("default_member_permissions");
                data.setDefaultPermissions(defaultPermissions == 0 ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.enabledFor(defaultPermissions));
            }

            data.setGuildOnly(!object.getBoolean("dm_permission", true));
            data.setNSFW(object.getBoolean("nsfw"));
            data.setNameLocalizations(LocalizationUtils.mapFromProperty(object, "name_localizations"));
            data.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(object, "description_localizations"));
            return data;
        }

        return SlashCommandData.fromData(object);
    }
}
