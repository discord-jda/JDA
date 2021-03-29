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


    class BaseCommand<T extends BaseCommand<T>> implements SerializableData
    {
        protected final DataArray options = DataArray.empty();
        protected String name, description;

        public BaseCommand(@Nonnull String name, @Nonnull String description)
        {
            Checks.notEmpty(name, "Name");
            Checks.notEmpty(description, "Description");
            this.name = name;
            this.description = description;
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        public T setName(@Nonnull String name)
        {
            Checks.notEmpty(name, "Name");
            this.name = name;
            return (T) this;
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        public T setDescription(@Nonnull String description)
        {
            Checks.notEmpty(description, "Description");
            this.description = description;
            return (T) this;
        }

        @Nonnull
        public String getName()
        {
            return name;
        }

        @Nonnull
        public String getDescription()
        {
            return description;
        }

        @Nonnull
        public List<OptionData> getOptions()
        {
            return options.stream(DataArray::getObject)
                    .map(OptionData::load)
                    .filter(it -> it.getType().getKey() > Command.OptionType.SUB_COMMAND_GROUP.getKey())
                    .collect(Collectors.toList());
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
    }

    class CommandData extends BaseCommand<CommandData> implements SerializableData
    {
        public CommandData(@Nonnull String name, @Nonnull String description)
        {
            super(name, description);
        }

        @Nonnull
        public List<SubcommandData> getSubcommands()
        {
            return options.stream(DataArray::getObject)
                    .filter(obj -> {
                        Command.OptionType type = Command.OptionType.fromKey(obj.getInt("type"));
                        return type == Command.OptionType.SUB_COMMAND;
                    })
                    .map(SubcommandData::load)
                    .collect(Collectors.toList());
        }

        @Nonnull
        public List<SubcommandGroupData> getSubcommandGroups()
        {
            return options.stream(DataArray::getObject)
                    .filter(obj -> {
                        Command.OptionType type = Command.OptionType.fromKey(obj.getInt("type"));
                        return type == Command.OptionType.SUB_COMMAND_GROUP;
                    })
                    .map(SubcommandGroupData::load)
                    .collect(Collectors.toList());
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
        public static CommandData load(@Nonnull DataObject object)
        {
            Checks.notNull(object, "DataObject");
            String name = object.getString("name");
            String description = object.getString("description");
            DataArray options = object.optArray("options").orElseGet(DataArray::empty);
            CommandData command = new CommandData(name, description);
            options.stream(DataArray::getObject).forEach(opt -> {
                Command.OptionType type = Command.OptionType.fromKey(opt.getInt("type"));
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
        public Command.OptionType getType()
        {
            return type;
        }

        @Nonnull
        public String getName()
        {
            return name;
        }

        @Nonnull
        public String getDescription()
        {
            return description;
        }

        public boolean isRequired()
        {
            return isRequired;
        }

        @Nonnull
        public List<Command.Choice> getChoices()
        {
            if (choices == null || choices.isEmpty())
                return Collections.emptyList();
            return choices.entrySet().stream()
                    .map(entry -> {
                        if (entry.getValue() instanceof String)
                            return new Command.Choice(entry.getKey(), entry.getValue().toString());
                        return new Command.Choice(entry.getKey(), ((Number) entry.getValue()).longValue());
                    })
                    .collect(Collectors.toList());
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
            {
                json.put("choices", DataArray.fromCollection(choices.entrySet()
                    .stream()
                    .map(entry -> DataObject.empty().put("name", entry.getKey()).put("value", entry.getValue()))
                    .collect(Collectors.toList())));
            }
            return json;
        }

        @Nonnull
        public static OptionData load(@Nonnull DataObject json)
        {
            String name = json.getString("name");
            String description = json.getString("description");
            Command.OptionType type = Command.OptionType.fromKey(json.getInt("type"));
            OptionData option = new OptionData(type, name, description);
            option.setRequired(json.getBoolean("required"));
            json.optArray("choices").ifPresent(choices1 ->
                choices1.stream(DataArray::getObject).forEach(o -> {
                    Object value = o.get("value");
                    if (value instanceof Number)
                        option.addChoice(o.getString("name"), ((Number) value).intValue());
                    else
                        option.addChoice(o.getString("name"), value.toString());
                })
            );
            return option;
        }
    }

    class SubcommandData extends BaseCommand<CommandData> implements SerializableData
    {
        public SubcommandData(@Nonnull String name, @Nonnull String description)
        {
            super(name, description);
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
            return super.toData().put("type", Command.OptionType.SUB_COMMAND.getKey());
        }

        @Nonnull
        public static SubcommandData load(@Nonnull DataObject json)
        {
            String name = json.getString("name");
            String description = json.getString("description");
            SubcommandData sub = new SubcommandData(name, description);
            json.optArray("options").ifPresent(arr ->
                arr.stream(DataArray::getObject)
                   .map(OptionData::load)
                   .forEach(sub::addOption)
            );
            return sub;
        }
    }

    class SubcommandGroupData extends OptionData implements SerializableData
    {
        private final DataArray options = DataArray.empty();

        public SubcommandGroupData(String name, String description)
        {
            super(Command.OptionType.SUB_COMMAND_GROUP, name, description);
        }

        @Nonnull
        public List<SubcommandData> getSubcommands()
        {
            return options.stream(DataArray::getObject)
                    .map(SubcommandData::load)
                    .collect(Collectors.toList());
        }

        @Nonnull
        public SubcommandGroupData addSubcommand(@Nonnull SubcommandData data)
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

        @Nonnull
        public static SubcommandGroupData load(@Nonnull DataObject json)
        {
            String name = json.getString("name");
            String description = json.getString("description");
            SubcommandGroupData group = new SubcommandGroupData(name, description);
            json.optArray("options").ifPresent(arr ->
                arr.stream(DataArray::getObject)
                   .map(SubcommandData::load)
                   .forEach(group::addSubcommand)
            );
            return group;
        }
    }
}
