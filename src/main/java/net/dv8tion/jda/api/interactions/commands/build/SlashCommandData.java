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

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Extension of {@link CommandData} which allows setting slash-command specific settings such as options and subcommands.
 */
public interface SlashCommandData extends CommandData
{
    @Nonnull
    @Override
    SlashCommandData setName(@Nonnull String name);

    @Nonnull
    @Override
    SlashCommandData setDefaultEnabled(boolean enabled);

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If the name is null or not between 1-100 characters
     *
     * @return The builder, for chaining
     */
    @Nonnull
    SlashCommandData setDescription(@Nonnull String description);

    /**
     * The configured description
     *
     * @return The description
     */
    @Nonnull
    String getDescription();

    /**
     * The {@link SubcommandData Subcommands} in this command.
     * <br>These subcommand instances are <b>reconstructed</b>,
     * which means that any modifications will not be reflected in the backing state.
     *
     * @return Immutable list of {@link SubcommandData}
     */
    @Nonnull
    List<SubcommandData> getSubcommands();

    /**
     * The {@link SubcommandGroupData Subcommand Groups} in this command.
     * <br>These subcommand group instances are <b>reconstructed</b>,
     * which means that any modifications will not be reflected in the backing state.
     *
     * @return Immutable list of {@link SubcommandGroupData}
     */
    @Nonnull
    List<SubcommandGroupData> getSubcommandGroups();

    /**
     * The options for this command.
     *
     * @return Immutable list of {@link OptionData}
     */
    @Nonnull
    List<OptionData> getOptions();

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
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    SlashCommandData addOptions(@Nonnull OptionData... options);

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
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addOptions(@Nonnull Collection<? extends OptionData> options)
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
     * @param  autoComplete
     *         Whether this option supports auto-complete via {@link CommandAutoCompleteInteractionEvent},
     *         only supported for option types which {@link OptionType#canSupportChoices() support choices}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If you try to mix subcommands/options/groups in one command.</li>
     *             <li>If the option type is {@link OptionType#SUB_COMMAND} or {@link OptionType#SUB_COMMAND_GROUP}.</li>
     *             <li>If the provided option type does not support auto-complete</li>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required, boolean autoComplete)
    {
        return addOptions(new OptionData(type, name, description)
                .setRequired(required)
                .setAutoComplete(autoComplete));
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
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOption(type, name, description, required, false);
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
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
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
     *         If null, more than 25 subcommands, or duplicate subcommand names are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    SlashCommandData addSubcommands(@Nonnull SubcommandData... subcommands);

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this command.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than 25 subcommands, or duplicate subcommand names are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands)
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
     *         If null, more than 25 subcommand groups, or duplicate group names are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    SlashCommandData addSubcommandGroups(@Nonnull SubcommandGroupData... groups);

    /**
     * Add up to 25 {@link SubcommandGroupData Subcommand-Groups} to this command.
     *
     * @param  groups
     *         The subcommand groups to add
     *
     * @throws IllegalArgumentException
     *         If null, more than 25 subcommand groups, or duplicate group names are provided.
     *         Also throws if you try to mix subcommands/options/groups in one command.
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    default SlashCommandData addSubcommandGroups(@Nonnull Collection<? extends SubcommandGroupData> groups)
    {
        Checks.noneNull(groups, "SubcommandGroups");
        return addSubcommandGroups(groups.toArray(new SubcommandGroupData[0]));
    }

    /**
     * Converts the provided {@link Command} into a SlashCommandData instance.
     *
     * @param  command
     *         The command to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the command has illegal configuration
     *
     * @return An instance of SlashCommandData
     */
    @Nonnull
    static SlashCommandData fromCommand(@Nonnull Command command)
    {
        Checks.notNull(command, "Command");
        if (command.getType() != Command.Type.SLASH)
            throw new IllegalArgumentException("Cannot convert command of type " + command.getType() + " to SlashCommandData!");

        CommandDataImpl data = new CommandDataImpl(command.getName(), command.getDescription());
        data.setDefaultEnabled(command.isDefaultEnabled());
        command.getOptions()
                .stream()
                .map(OptionData::fromOption)
                .forEach(data::addOptions);
        command.getSubcommands()
                .stream()
                .map(SubcommandData::fromSubcommand)
                .forEach(data::addSubcommands);
        command.getSubcommandGroups()
                .stream()
                .map(SubcommandGroupData::fromGroup)
                .forEach(data::addSubcommandGroups);
        return data;
    }

    /**
     * Parses the provided serialization back into a SlashCommandData instance.
     * <br>This is the reverse function for {@link SlashCommandData#toData()}.
     *
     * @param  object
     *         The serialized {@link DataObject} representing the command
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed SlashCommandData instance, which can be further configured through setters
     *
     * @see    CommandData#fromData(DataObject)
     * @see    Commands#fromList(Collection)
     */
    @Nonnull
    static SlashCommandData fromData(@Nonnull DataObject object)
    {
        Checks.notNull(object, "DataObject");
        String name = object.getString("name");
        Command.Type commandType = Command.Type.fromId(object.getInt("type", 1));
        if (commandType != Command.Type.SLASH)
            throw new IllegalArgumentException("Cannot convert command of type " + commandType + " to SlashCommandData!");

        String description = object.getString("description");
        DataArray options = object.optArray("options").orElseGet(DataArray::empty);
        CommandDataImpl command = new CommandDataImpl(name, description);
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
}
