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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public interface CommandUpdateAction extends RestAction<Void>
{
    @Nonnull
    @Override
    default CommandUpdateAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CommandUpdateAction) RestAction.super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    default CommandUpdateAction deadline(long timestamp)
    {
        return (CommandUpdateAction) RestAction.super.deadline(timestamp);
    }

    @Nonnull
    @Override
    CommandUpdateAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    default CommandUpdateAction addCheck(@Nonnull BooleanSupplier checks)
    {
        return (CommandUpdateAction) RestAction.super.addCheck(checks);
    }

    @Nonnull
    @CheckReturnValue
    CommandUpdateAction addCommands(@Nonnull Collection<? extends CommandData> commands);

    @Nonnull
    @CheckReturnValue
    default CommandUpdateAction addCommands(@Nonnull CommandData... commands)
    {
        Checks.noneNull(commands, "Command");
        return addCommands(Arrays.asList(commands));
    }

    class CommandData implements SerializableData
    {
        private final DataArray options = DataArray.empty();
        private final String name, description;

        public CommandData(@Nonnull String name, @Nonnull String description)
        {
            Checks.notNull(name, "Name");
            Checks.notNull(description, "Description");
            this.name = name;
            this.description = description;
        }

        @Nonnull
        public CommandData addOption(@Nonnull OptionData data)
        {
            Checks.notNull(data, "Option");
            options.add(data);
            return this;
        }

        @Nonnull
        public CommandData addSubcommand(@Nonnull SubcommandData data)
        {
            Checks.notNull(data, "Subcommand");
            options.add(data);
            return this;
        }

        @Nonnull
        public CommandData addSubcommandGroup(@Nonnull SubcommandGroupData data)
        {
            Checks.notNull(data, "SubcommandGroup");
            options.add(data);
            return this;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            return DataObject.empty()
                    .put("name", name)
                    .put("description", description)
                    .put("options", options);
        }

        @Nonnull
        public static CommandData load(@Nonnull DataObject object)
        {
            Checks.notNull(object, "DataObject");
            String name = object.getString("name");
            String description = object.getString("description");
            DataArray options = object.optArray("options").orElseGet(DataArray::empty);
            CommandData command = new CommandData(name, description);
            options.stream(DataArray::getObject).forEach(opt -> {
                Command.OptionType type = Command.OptionType.fromKey(opt.getInt("type"));
                String optionName = opt.getString("name");
                String optionDescription = opt.getString("description");
                switch (type)
                {
                case SUB_COMMAND:
                    SubcommandData sub = loadSubcommand(opt, optionName, optionDescription);
                    command.addSubcommand(sub);
                    break;
                case SUB_COMMAND_GROUP:
                    SubcommandGroupData group = new SubcommandGroupData(optionName, optionDescription);
                    opt.optArray("options").ifPresent(arr ->
                        arr.stream(DataArray::getObject).forEach(o ->
                            group.addSubcommand(loadSubcommand(o, o.getString("name"), o.getString("description")
                    ))));
                    command.addSubcommandGroup(group);
                    break;
                default:
                    OptionData option = loadOption(opt, type, optionName, optionDescription);
                    command.addOption(option);
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

        @Nonnull
        private static SubcommandData loadSubcommand(DataObject opt, String optionName, String optionDescription)
        {
            SubcommandData sub = new SubcommandData(optionName, optionDescription);
            opt.optArray("options").ifPresent(arr -> {
                arr.stream(DataArray::getObject).forEach(o -> {
                    sub.addOption(loadOption(o,
                        Command.OptionType.fromKey(o.getInt("type")),
                        o.getString("name"),
                        o.getString("description")));
                });
            });
            return sub;
        }

        @Nonnull
        private static OptionData loadOption(DataObject opt, Command.OptionType type, String optionName, String optionDescription)
        {
            OptionData option = new OptionData(type, optionName, optionDescription);
            option.setRequired(opt.getBoolean("required"));
            opt.optObject("choices").ifPresent(choices ->
                choices.keys().forEach(choiceName -> {
                    Object value = choices.get(choiceName);
                    if (value instanceof Number)
                        option.addChoice(choiceName, ((Number) value).intValue());
                    else
                        option.addChoice(choiceName, value.toString());
                })
            );
            return option;
        }
    }

    class OptionData implements SerializableData
    {
        private final Command.OptionType type;
        private final String name, description;
        private boolean isRequired;
        private Map<String, Object> choices;

        public OptionData(@Nonnull Command.OptionType type, @Nonnull String name, @Nonnull String description)
        {
            Checks.notNull(type, "Type");
            Checks.notNull(name, "Name");
            Checks.notNull(description, "Description");
            this.type = type;
            this.name = name;
            this.description = description;
            if (type.canSupportChoices())
                choices = new HashMap<>();
        }

        @Nonnull
        public OptionData setRequired(boolean required)
        {
            this.isRequired = required;
            return this;
        }

        @Nonnull
        public OptionData addChoice(@Nonnull String name, int value)
        {
            Checks.notNull(name, "Name");
            if (type != Command.OptionType.INTEGER)
                throw new IllegalArgumentException("Cannot add int choice for OptionType." + type);
            choices.put(name, value);
            return this;
        }

        @Nonnull
        public OptionData addChoice(@Nonnull String name, @Nonnull String value)
        {
            Checks.notNull(name, "Name");
            Checks.notNull(value, "Value");
            if (type != Command.OptionType.STRING)
                throw new IllegalArgumentException("Cannot add string choice for OptionType." + type);
            choices.put(name, value);
            return this;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            DataObject json = DataObject.empty()
                .put("type", type.getKey())
                .put("name", name)
                .put("description", description);
            if (type != Command.OptionType.SUB_COMMAND && type != Command.OptionType.SUB_COMMAND_GROUP)
                json.put("required", isRequired);
            if (choices != null && !choices.isEmpty())
                json.put("choices", choices);
            return json;
        }
    }

    class SubcommandData extends OptionData implements SerializableData
    {
        private final DataArray options = DataArray.empty();

        public SubcommandData(@Nonnull String name, @Nonnull String description)
        {
            super(Command.OptionType.SUB_COMMAND, name, description);
        }

        @Nonnull
        public SubcommandData addOption(@Nonnull OptionData data)
        {
            Checks.notNull(data, "Option");
            options.add(data);
            return this;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            return super.toData().put("options", options);
        }
    }

    class SubcommandGroupData extends OptionData implements SerializableData
    {
        private final DataArray options = DataArray.empty();

        public SubcommandGroupData(String name, String description)
        {
            super(Command.OptionType.SUB_COMMAND_GROUP, name, description);
        }

        public SubcommandGroupData addSubcommand(SubcommandData data)
        {
            Checks.notNull(data, "Subcommand");
            options.add(data);
            return this;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            return super.toData().put("options", options);
        }
    }
}
