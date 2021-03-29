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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Command implements ISnowflake
{
    private final JDAImpl api;
    private final Guild guild;
    private final String name, description;
    private final List<Option> options;
    private final long id, guildId;

    public Command(JDAImpl api, Guild guild, DataObject json)
    {
        this.api = api;
        this.guild = guild;
        this.name = json.getString("name");
        this.description = json.getString("description");
        this.id = json.getUnsignedLong("id");
        this.guildId = guild != null ? guild.getIdLong() : 0L;
        this.options = parseOptions(json);
    }

    protected static List<Option> parseOptions(DataObject json)
    {
        return json.optArray("options").map(arr ->
            arr.stream(DataArray::getObject)
               .map(Option::new)
               .collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    @Nonnull
    @CheckReturnValue
    public RestAction<Void> delete()
    {
        Route.CompiledRoute route;
        if (guildId != 0L)
            route = Route.Interactions.DELETE_GUILD_COMMAND.compile(Long.toUnsignedString(guildId), getId());
        else
            route = Route.Interactions.DELETE_COMMAND.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @CheckReturnValue
    public CommandEditAction editCommand()
    {
        return guild == null ? new CommandEditActionImpl(api, getId()) : new CommandEditActionImpl(guild, getId());
    }

    @Nonnull
    public JDA getJDA()
    {
        return api;
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
    public List<Option> getOptions()
    {
        return options;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "C:" + getName() + "(" + getId() + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof Command))
            return false;
        return id == ((Command) obj).id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    public enum OptionType
    {
        UNKNOWN(-1), SUB_COMMAND(1), SUB_COMMAND_GROUP(2), STRING(3, true), INTEGER(4, true), BOOLEAN(5), USER(6), CHANNEL(7), ROLE(8);
        private final int raw;
        private final boolean supportsChoices;

        OptionType(int raw)
        {
            this(raw, false);
        }

        OptionType(int raw, boolean supportsChoices)
        {
            this.raw = raw;
            this.supportsChoices = supportsChoices;
        }

        public int getKey()
        {
            return raw;
        }

        public boolean canSupportChoices()
        {
            return supportsChoices;
        }

        @Nonnull
        public static OptionType fromKey(int key)
        {
            for (OptionType type : values())
            {
                if (type.raw == key)
                    return type;
            }
            return UNKNOWN;
        }
    }

    public static class Choice
    {
        private final String name;
        private final long intValue;
        private final String stringValue;

        public Choice(DataObject json)
        {
            this.name = json.getString("name");
            if (json.isType("value", DataType.INT))
            {
                this.intValue = json.getLong("value");
                this.stringValue = Long.toString(intValue); // does this make sense?
            }
            else
            {
                this.intValue = 0;
                this.stringValue = json.getString("value");
            }
        }

        @Nonnull
        public String getName()
        {
            return name;
        }

        public long getAsLong()
        {
            return intValue;
        }

        @Nonnull
        public String getAsString()
        {
            return stringValue;
        }
    }

    public static class Option
    {
        private final String name, description;
        private final int type;
        private final List<Option> options;
        private final List<Choice> choices;

        public Option(DataObject json)
        {
            this.name = json.getString("name");
            this.description = json.getString("description");
            this.type = json.getInt("type");

            this.options = parseOptions(json);
            this.choices = json.optArray("choices")
                .map(it -> it.stream(DataArray::getObject).map(Choice::new).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
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

        public int getTypeRaw()
        {
            return type;
        }

        @Nonnull
        public List<Choice> getChoices()
        {
            return choices;
        }

        @Nonnull
        public List<Option> getOptions()
        {
            return options;
        }
    }
}
