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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDataImpl extends BaseCommand<CommandDataImpl> implements SlashCommandData
{
    private boolean allowSubcommands = true;
    private boolean allowGroups = true;
    private boolean allowOption = true;
    private boolean defaultPermissions = true; // whether the command uses default_permissions (blacklist/whitelist)
    private boolean allowRequired = true;

    private final Command.Type type;

    public CommandDataImpl(@Nonnull String name, @Nonnull String description)
    {
        super(name, description);
        this.type = Command.Type.SLASH;
        checkName(name);
        checkDescription(description);
    }

    public CommandDataImpl(@Nonnull Command.Type type, @Nonnull String name)
    {
        super(name, null);
        Checks.check(type != Command.Type.SLASH, "Cannot create slash command without description. Use `new CommandData(name, description)` instead.");
        this.type = type;
    }

    @Override
    protected void checkName(String name)
    {
        Checks.inRange(name, 1, 32, "Name");
        if (type == Command.Type.SLASH)
        {
            Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
            Checks.isLowercase(name, "Name");
        }
    }

    @Override
    protected void checkDescription(String description)
    {
        if (type != Command.Type.SLASH)
        {
            if (description == null)
                return;
            throw new IllegalArgumentException("Cannot set descriptions for commands of type " + type);
        }
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
    }

    protected void checkType(Command.Type required, String action)
    {
        if (required != type)
            throw new IllegalStateException("Cannot " + action + " for commands of type " + type);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return super.toData().put("default_permission", defaultPermissions).put("type", type.getId());
    }

    @Nonnull
    @Override
    public Command.Type getType()
    {
        return type;
    }

    @Nonnull
    public CommandDataImpl setName(@Nonnull String name)
    {
        checkName(name);
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
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

    @Nonnull
    @Override
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

    @Nonnull
    @Override
    public CommandDataImpl setDefaultEnabled(boolean enabled)
    {
        this.defaultPermissions = enabled;
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl addOptions(@Nonnull OptionData... options)
    {
        checkType(Command.Type.SLASH, "add options");
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

    @Nonnull
    @Override
    public CommandDataImpl addSubcommands(@Nonnull SubcommandData... subcommands)
    {
        checkType(Command.Type.SLASH, "add subcommands");
        Checks.noneNull(subcommands, "Subcommands");
        if (!allowSubcommands)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        allowOption = false;
        Checks.check(subcommands.length + options.length() <= 25, "Cannot have more than 25 subcommands for a command!");
        for (SubcommandData data : subcommands)
            options.add(data);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl addSubcommandGroups(@Nonnull SubcommandGroupData... groups)
    {
        checkType(Command.Type.SLASH, "add subcommand groups");
        Checks.noneNull(groups, "SubcommandGroups");
        if (!allowGroups)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        allowOption = false;
        Checks.check(groups.length + options.length() <= 25, "Cannot have more than 25 subcommand groups for a command!");
        for (SubcommandGroupData data : groups)
            options.add(data);
        return this;
    }
}
