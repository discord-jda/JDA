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

import javax.annotation.Nonnull;
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
    public CommandData(@Nonnull String name, @Nonnull String description)
    {
        super(name, description);
    }

    /**
     * The {@link SubcommandData Subcommands} in this command.
     *
     * @return Immutable list of {@link SubcommandData}
     */
    @Nonnull
    public List<SubcommandData> getSubcommands()
    {
        return options.stream(DataArray::getObject)
                .filter(obj ->
                {
                    OptionType type = OptionType.fromKey(obj.getInt("type"));
                    return type == OptionType.SUB_COMMAND;
                })
                .map(SubcommandData::load)
                .collect(Collectors.toList());
    }

    /**
     * The {@link SubcommandGroupData Subcommand Groups} in this command.
     *
     * @return Immutable list of {@link SubcommandGroupData}
     */
    @Nonnull
    public List<SubcommandGroupData> getSubcommandGroups()
    {
        return options.stream(DataArray::getObject)
                .filter(obj ->
                {
                    OptionType type = OptionType.fromKey(obj.getInt("type"));
                    return type == OptionType.SUB_COMMAND_GROUP;
                })
                .map(SubcommandGroupData::load)
                .collect(Collectors.toList());
    }

    /**
     * Adds an option to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  data
     *         The {@link OptionData}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandData instance, for chaining
     */
    @Nonnull
    public CommandData addOption(@Nonnull OptionData data)
    {
        Checks.notNull(data, "Option");
        Checks.check(options.length() < 25, "Cannot have more than 25 options for a command!");
        switch (data.getType())
        {
        case SUB_COMMAND:
            if (!allowSubcommands)
                throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
            allowOption = allowGroups = false;
            break;
        case SUB_COMMAND_GROUP:
            if (!allowGroups)
                throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
            allowOption = allowSubcommands = false;
            break;
        default:
            if (!allowOption)
                throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
            allowSubcommands = allowGroups = false;
        }
        options.add(data);
        return this;
    }

    /**
     * Adds an option to this command.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionData}
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
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandData instance, for chaining
     */
    @Nonnull
    public CommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOption(new OptionData(type, name, description).setRequired(required));
    }

    /**
     * Adds an option to this command.
     * <br>The option is set to be non-required! You can use {@link #addOption(OptionType, String, String, boolean)} to add a required option instead.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionData}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The CommandData instance, for chaining
     */
    @Nonnull
    public CommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return addOption(type, name, description, false);
    }

    /**
     * Add a {@link SubcommandData} to this command.
     *
     * @param  data
     *         The subcommand to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandData instance, for chaining
     */
    @Nonnull
    public CommandData addSubcommand(@Nonnull SubcommandData data)
    {
        Checks.notNull(data, "Subcommand");
        if (!allowSubcommands)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        allowOption = allowGroups = false;
        Checks.check(options.length() < 25, "Cannot have more than 25 subcommands for a command!");
        options.add(data);
        return this;
    }

    /**
     * Add a {@link SubcommandGroupData} to this command.
     *
     * @param  data
     *         The subcommand group to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommand groups are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The CommandData instance, for chaining
     */
    @Nonnull
    public CommandData addSubcommandGroup(@Nonnull SubcommandGroupData data)
    {
        Checks.notNull(data, "SubcommandGroup");
        if (!allowGroups)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        allowSubcommands = allowOption = false;
        Checks.check(options.length() < 25, "Cannot have more than 25 subcommand groups for a command!");
        options.add(data);
        return this;
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
    @Nonnull
    public static CommandData load(@Nonnull DataObject object)
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
                command.addSubcommand(SubcommandData.load(opt));
                break;
            case SUB_COMMAND_GROUP:
                command.addSubcommandGroup(SubcommandGroupData.load(opt));
                break;
            default:
                command.addOption(OptionData.load(opt));
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
    @Nonnull
    public static List<CommandData> loadAll(@Nonnull DataArray array)
    {
        Checks.notNull(array, "DataArray");
        return array.stream(DataArray::getObject)
                .map(CommandData::load)
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
    @Nonnull
    public static List<CommandData> loadAll(@Nonnull Collection<? extends DataObject> collection)
    {
        Checks.noneNull(collection, "CommandData");
        return loadAll(DataArray.fromCollection(collection));
    }
}
