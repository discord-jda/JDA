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

public class CommandData extends BaseCommand<CommandData> implements SerializableData
{
    private boolean allowSubcommands = true;
    private boolean allowGroups = true;
    private boolean allowOption = true;

    public CommandData(@Nonnull String name, @Nonnull String description)
    {
        super(name, description);
    }

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

    @Nonnull
    public CommandData addOption(@Nonnull OptionData data)
    {
        Checks.notNull(data, "Option");
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

    @Nonnull
    public CommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOption(new OptionData(type, name, description).setRequired(required));
    }

    @Nonnull
    public CommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return addOption(type, name, description, false);
    }

    @Nonnull
    public CommandData addSubcommand(@Nonnull SubcommandData data)
    {
        Checks.notNull(data, "Subcommand");
        if (!allowSubcommands)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        allowOption = allowGroups = false;
        options.add(data);
        return this;
    }

    @Nonnull
    public CommandData addSubcommandGroup(@Nonnull SubcommandGroupData data)
    {
        Checks.notNull(data, "SubcommandGroup");
        if (!allowGroups)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        allowSubcommands = allowOption = false;
        options.add(data);
        return this;
    }

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

    @Nonnull
    public static List<CommandData> loadAll(@Nonnull DataArray array)
    {
        Checks.notNull(array, "DataArray");
        return array.stream(DataArray::getObject)
                .map(CommandData::load)
                .collect(Collectors.toList());
    }

    @Nonnull
    public static List<CommandData> loadAll(@Nonnull Collection<? extends DataObject> collection)
    {
        Checks.noneNull(collection, "CommandData");
        return loadAll(DataArray.fromCollection(collection));
    }
}
