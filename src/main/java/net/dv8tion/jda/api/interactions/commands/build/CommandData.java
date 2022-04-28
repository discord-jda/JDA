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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
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
     * Sets the default {@link Permission Permissions} a user must have in order to see and use this command.
     * <br>By default, everyone can use this command.
     *
     * @param permissions Collection of {@link Permission Permissions} a user must have to execute this command.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setDefaultPermissions(@Nonnull Collection<Permission> permissions);

    /**
     * Sets the default {@link Permission Permissions} a user must have in order to see and use this command.
     * <br>By default, everyone can use this command.
     *
     * @param permissions Vararg of {@link Permission Permissions} a user must have to execute this command.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default CommandData setDefaultPermissions(@Nonnull Permission... permissions)
    {
        Checks.noneNull(permissions, "Permissions");
        return setDefaultPermissions(Arrays.asList(permissions));
    }

    /**
     * Sets the default raw permission bitfield representing the permissions a user must have in order to see and use this command.
     * <br>By default, everyone can use this command.
     *
     * @param  raw Raw permission bitfield representing the permissions a user must have to execute this command.
     *
     * @throws IllegalArgumentException
     *         If raw is smaller than 0
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default CommandData setDefaultPermissions(long raw)
    {
        Checks.check(raw >= 0, "Raw permissions cannot be smaller than 0!");
        return setDefaultPermissions(Permission.getPermissions(raw));
    }

    /**
     * Sets whether this command can be used in DMs.
     * <br>This only has an effect if this command is registered globally.
     *
     * @param enabledInDMs Whether to enable this command in DMs
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setCommandEnabledInDMs(boolean enabledInDMs);

    /**
     * The current command name
     *
     * @return The command name
     */
    @Nonnull
    String getName();

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
     * The default {@link Permission Permissions} of this command.
     * <br>A user will not be able to execute this command if he does not have any of these permissions.
     *
     * @return {@link EnumSet} of containing the default Permissions of this command.
     */
    @Nonnull
    EnumSet<Permission> getDefaultPermissions();

    /**
     * The raw permission bitfield representing the default Permissions of this command.
     * <br>This is 0 if no permissions have been set
     *
     * @return raw permission bitfield representing the default Permissions of this command.
     */
    long getDefaultPermissionsRaw();

    /**
     * Whether the command can be accessed via Direct Messages.
     * <br>If this is a guild-command, this has no effect.
     *
     * @return False, if the command cannot be used in DMs
     */
    boolean isCommandEnabledInDMs();

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
                    .setDefaultEnabled(command.isDefaultEnabled());

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
            return new CommandDataImpl(commandType, name);

        return SlashCommandData.fromData(object);
    }
}
