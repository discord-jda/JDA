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

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.ApplicationCommandPermission;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;

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
     *
     * @deprecated This has been deprecated in favor of {@link #setDefaultPermissions(ApplicationCommandPermission)}
     */
    @Deprecated
    @ForRemoval
    @DeprecatedSince("5.0.0")
    @Nonnull
    CommandData setDefaultEnabled(boolean enabled);

    /**
     * Sets the default {@link ApplicationCommandPermission} for this command.
     * <br>By default, everyone can use this command.
     * <p>Passing {@link ApplicationCommandPermission#DISABLED} will only show this command to admins.
     *
     * @param permission {@link ApplicationCommandPermission} representing the default permissions of this command.
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions are null or {@link Permission#UNKNOWN UNKNOWN}.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    CommandData setDefaultPermissions(@Nonnull ApplicationCommandPermission permission);

    /**
     * Sets whether this command is only usable in a guild.
     * <br>This only has an effect if this command is registered globally.
     *
     * @param guildOnly Whether to restrict this command to guilds
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
     * Whether this command is available to everyone by default.
     *
     * @return True, if this command is enabled to everyone by default
     *
     * @see    #setDefaultEnabled(boolean)
     * @see    CommandPrivilege
     *
     * @deprecated This has been deprecated in favor of {@link #getDefaultPermissions()}.
     */
    @Deprecated
    @ForRemoval
    @DeprecatedSince("5.0.0")
    boolean isDefaultEnabled();

    /**
     * The {@link Command.Type}
     *
     * @return The {@link Command.Type}
     */
    @Nonnull
    Command.Type getType();

    /**
     * Gets the {@link ApplicationCommandPermission} of this command.
     * <br>If no permissions have been set, this returns {@link ApplicationCommandPermission#ENABLED}.
     *
     * @return ApplicationCommandPermission of this command.
     */
    @Nonnull
    ApplicationCommandPermission getDefaultPermissions();

    /**
     * Whether the command can only be used inside a guild.
     * <br>If this is a guild-command, this has no effect.
     *
     * @return False, if the command is not restricted to guilds.
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
            return new CommandDataImpl(command.getType(), command.getName());

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
