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

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Builder for Application Commands.
 * <br>Use the factory methods provided by {@link Commands} to create instances of this interface.
 *
 * @see Commands
 */
public interface CommandData extends SerializableData
{
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
     * Sets the default {@link DefaultMemberPermissions} for this command.
     * <br>By default, everyone can use this command ({@link DefaultMemberPermissions#ENABLED}). This can be configured or overwritten by moderators in each guild.
     *
     * <p>Passing {@link DefaultMemberPermissions#DISABLED} will only show this command to admins.
     *
     * @param  permission
     *         {@link DefaultMemberPermissions} representing the default permissions of this command.
     *
     * @return The builder instance, for chaining
     *
     * @see DefaultMemberPermissions#ENABLED
     * @see DefaultMemberPermissions#DISABLED
     */
    @Nonnull
    CommandData setDefaultPermissions(@Nonnull DefaultMemberPermissions permission);

    /**
     * Sets whether this command is only usable in a guild (Default: false).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  guildOnly
     *         Whether to restrict this command to guilds
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setGuildOnly(boolean guildOnly);

    /**
     * The current command name
     *
     * @return The command name
     */
    @Nonnull
    String getName();

    /**
     * The {@link Command.Type}
     *
     * @return The {@link Command.Type}
     */
    @Nonnull
    Command.Type getType();

    /**
     * Gets the {@link DefaultMemberPermissions} of this command.
     * <br>If no permissions have been set, this returns {@link DefaultMemberPermissions#ENABLED}.
     *
     * @return DefaultMemberPermissions of this command.
     *
     * @see    DefaultMemberPermissions#ENABLED
     * @see    DefaultMemberPermissions#DISABLED
     */
    @Nonnull
    DefaultMemberPermissions getDefaultPermissions();

    /**
     * Whether the command can only be used inside a guild.
     * <br>Always true for guild commands.
     *
     * @return True, if this command is restricted to guilds.
     */
    boolean isGuildOnly();

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
            return new CommandDataImpl(command.getType(), command.getName())
                    .setDefaultPermissions(command.getDefaultPermissions())
                    .setGuildOnly(command.isGuildOnly());

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
            CommandData data = new CommandDataImpl(commandType, name);
            if (!object.isNull("default_member_permissions"))
            {
                long defaultPermissions = object.getLong("default_member_permissions");
                data.setDefaultPermissions(defaultPermissions == 0 ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.enabledFor(defaultPermissions));
            }

            data.setGuildOnly(!object.getBoolean("dm_permission", true));
            return data;
        }

        return SlashCommandData.fromData(object);
    }
}
