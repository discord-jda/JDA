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

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder for a Slash-Command.
 */
public class CommandData extends BaseCommand<CommandData> implements SerializableData
{
    private boolean allowSubcommands = true;
    private boolean allowGroups = true;
    private boolean allowOption = true;
    private boolean defaultPermissions = true; // whether the command uses default_permissions (blacklist/whitelist)
    private boolean allowRequired = true;

    /**
     * Create an command builder.
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
     */
    public CommandData(@NotNull String name, @NotNull String description)
    {
        super(name, description);
    }

    @NotNull
    @Override
    public DataObject toData()
    {
        return super.toData().put("default_permission", defaultPermissions);
    }

    /**
     * The {@link SubcommandData Subcommands} in this command.
     *
     * @return Immutable list of {@link SubcommandData}
     */
    @NotNull
    public List<SubcommandData> getSubcommands()
    {
        return options.stream(DataArray::getObject)
                .filter(obj ->
                {
                    OptionType type = OptionType.fromKey(obj.getInt("type"));
                    return type == OptionType.SUB_COMMAND;
                })
                .map(SubcommandData::fromData)
                .collect(Collectors.toList());
    }

    /**
     * The {@link SubcommandGroupData Subcommand Groups} in this command.
     *
     * @return Immutable list of {@link SubcommandGroupData}
     */
    @NotNull
    public List<SubcommandGroupData> getSubcommandGroups()
    {
        return options.stream(DataArray::getObject)
                .filter(obj ->
                {
                    OptionType type = OptionType.fromKey(obj.getInt("type"));
                    return type == OptionType.SUB_COMMAND_GROUP;
                })
                .map(SubcommandGroupData::fromData)
                .collect(Collectors.toList());
    }

    /**
     * Whether this command is available to everyone by default.
     * <br>If this is disabled, you need to explicitly whitelist users and roles per guild.
     *
     * @param  enabled
     *         True, if this command is enabled by default for everyone. (Default: true)
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData setDefaultEnabled(boolean enabled)
    {
        this.defaultPermissions = enabled;
        return this;
    }

    /**
     * Adds up to 25 options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *          The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData addOptions(@NotNull OptionData... options)
    {
        Checks.noneNull(options, "Option");
        Checks.check(options.length + this.options.length() <= 25, "Cannot have more than 25 options for a command!");
        Checks.check(allowOption, "You cannot mix options with subcommands/groups.");
        allowSubcommands = allowGroups = false;
        for (OptionData option : options)
        {
            Checks.check(option.getType() != OptionType.SUB_COMMAND, "Cannot add a subcommand with addOptions(...). Use addSubcommands(...) instead!");
            Checks.check(option.getType() != OptionType.SUB_COMMAND_GROUP, "Cannot add a subcommand group with addOptions(...). Use addSubcommandGroups(...) instead!");
            Checks.check(allowRequired || !option.isRequired(), "Cannot add required options after non-required options!");
            allowRequired = option.isRequired(); // prevent adding required options after non-required options
            this.options.add(option);
        }
        return this;
    }

    /**
     * Adds up to 25 options to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData Options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData addOptions(@NotNull Collection<? extends OptionData> options)
    {
        Checks.noneNull(options, "Option");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Adds an option to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description, boolean required)
    {
        return addOptions(new OptionData(type, name, description).setRequired(required));
    }

    /**
     * Adds an option to this command.
     * <br>The option is set to be non-required! You can use {@link #addOption(OptionType, String, String, boolean)} to add a required option instead.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData addOption(@NotNull OptionType type, @NotNull String name, @NotNull String description)
    {
        return addOption(type, name, description, false);
    }

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData addSubcommands(@NotNull SubcommandData... subcommands)
    {
        Checks.noneNull(subcommands, "Subcommands");
        if (!allowSubcommands)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        allowOption = false;
        Checks.check(subcommands.length + options.length() <= 25, "Cannot have more than 25 subcommands for a command!");
        for (SubcommandData data : subcommands)
            options.add(data);
        return this;
    }

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData addSubcommands(@NotNull Collection<? extends SubcommandData> subcommands)
    {
        Checks.noneNull(subcommands, "Subcommands");
        return addSubcommands(subcommands.toArray(new SubcommandData[0]));
    }

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData addSubcommandGroups(@NotNull SubcommandGroupData... groups)
    {
        Checks.noneNull(groups, "SubcommandGroups");
        if (!allowGroups)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        allowOption = false;
        Checks.check(groups.length + options.length() <= 25, "Cannot have more than 25 subcommand groups for a command!");
        for (SubcommandGroupData data : groups)
            options.add(data);
        return this;
    }

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandData instance, for chaining
     */
    @NotNull
    public CommandData addSubcommandGroups(@NotNull Collection<? extends SubcommandGroupData> groups)
    {
        Checks.noneNull(groups, "SubcommandGroups");
        return addSubcommandGroups(groups.toArray(new SubcommandGroupData[0]));
    }

    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br>This is the reverse function for {@link #toData()}.
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
     */
    @NotNull
    public static CommandData fromData(@NotNull DataObject object)
    {
        Checks.notNull(object, "DataObject");
        String name = object.getString("name");
        String description = object.getString("description");
        DataArray options = object.optArray("options").orElseGet(DataArray::empty);
        CommandData command = new CommandData(name, description);
        options.stream(DataArray::getObject).forEach(opt ->
        {
            OptionType type = OptionType.fromKey(opt.getInt("type"));
            switch (type)
            {
            case SUB_COMMAND:
                command.addSubcommands(SubcommandData.fromData(opt));
                break;
            case SUB_COMMAND_GROUP:
                command.addSubcommandGroups(SubcommandGroupData.fromData(opt));
                break;
            default:
                command.addOptions(OptionData.fromData(opt));
            }
        });
        return command;
    }

    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br>This is the reverse function for {@link #toData()}.
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
     */
    @NotNull
    public static List<CommandData> fromList(@NotNull DataArray array)
    {
        Checks.notNull(array, "DataArray");
        return array.stream(DataArray::getObject)
                .map(CommandData::fromData)
                .collect(Collectors.toList());
    }

    /**
     * Parses the provided serialization back into an CommandData instance.
     * <br>This is the reverse function for {@link #toData()}.
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
     */
    @NotNull
    public static List<CommandData> fromList(@NotNull Collection<? extends DataObject> collection)
    {
        Checks.noneNull(collection, "CommandData");
        return fromList(DataArray.fromCollection(collection));
    }
}
