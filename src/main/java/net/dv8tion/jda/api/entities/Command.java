/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Command
{
    private final JDAImpl api;
    private final String name, description;
    private final List<Option> options;
    private final long id;

    public Command(JDAImpl api, DataObject json)
    {
        this.api = api;
        this.name = json.getString("name");
        this.description = json.getString("description");
        this.id = json.getUnsignedLong("id");
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

    public JDA getJDA()
    {
        return api;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public List<Option> getOptions()
    {
        return options;
    }

    public long getIdLong()
    {
        return id;
    }

    public String getId()
    {
        return Long.toUnsignedString(id);
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
        UNKNOWN(-1), SUB_COMMAND(1), SUB_COMMAND_GROUP(2), STRING(3), INTEGER(4), BOOLEAN(5), USER(6), CHANNEL(7), ROLE(8);
        private final int raw;

        OptionType(int raw)
        {
            this.raw = raw;
        }

        public int getKey()
        {
            return raw;
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

        public String getName()
        {
            return name;
        }

        public long getAsLong()
        {
            return intValue;
        }

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

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public int getTypeRaw()
        {
            return type;
        }

        public List<Choice> getChoices()
        {
            return choices;
        }

        public List<Option> getOptions()
        {
            return options;
        }
    }
}
