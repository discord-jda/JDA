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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

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
    CommandUpdateAction addCommands(@Nonnull Collection<CommandData> commands);

    @Nonnull
    @CheckReturnValue
    default CommandUpdateAction addCommands(@Nonnull CommandData... commands)
    {
        Checks.noneNull(commands, "Command");
        return addCommands(Arrays.asList(commands));
    }

    //TODO: Checks! Checks! Checks!
    class CommandData implements SerializableData
    {
        private final DataArray options = DataArray.empty();
        private final String name, description;

        public CommandData(String name, String description)
        {
            this.name = name;
            this.description = description;
        }

        public CommandData addOption(OptionData data)
        {
            Checks.notNull(data, "Option");
            options.add(data);
            return this;
        }

        public CommandData addSubcommand(SubcommandData data)
        {
            Checks.notNull(data, "Subcommand");
            options.add(data);
            return this;
        }

        public CommandData addSubcommandGroup(SubcommandGroupData data)
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
    }

    class OptionData implements SerializableData
    {
        private final Command.OptionType type;
        private final String name, description;
        private boolean isRequired;
        private Map<String, Object> choices;

        public OptionData(Command.OptionType type, String name, String description)
        {
            this.type = type;
            this.name = name;
            this.description = description;
            if (type.canSupportChoices())
                choices = new HashMap<>();
        }

        public OptionData setRequired(boolean required)
        {
            this.isRequired = required;
            return this;
        }

        public OptionData addChoice(String name, int value)
        {
            if (type != Command.OptionType.INTEGER)
                throw new IllegalArgumentException("Cannot add int choice for OptionType." + type);
            choices.put(name, value);
            return this;
        }

        public OptionData addChoice(String name, String value)
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
                .put("description", description)
                .put("required", isRequired);
            if (choices != null && !choices.isEmpty())
                json.put("choices", choices);
            return json;
        }
    }

    class SubcommandData extends OptionData implements SerializableData
    {
        private final DataArray options = DataArray.empty();

        public SubcommandData(String name, String description)
        {
            super(Command.OptionType.SUB_COMMAND, name, description);
        }

        public SubcommandData addOption(OptionData data)
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
