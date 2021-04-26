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

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OptionData implements SerializableData
{
    private final OptionType type;
    private final String name, description;
    private boolean isRequired;
    private Map<String, Object> choices;

    public OptionData(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        Checks.notNull(type, "Type");
        Checks.notEmpty(name, "Name");
        Checks.notEmpty(description, "Description");
        Checks.notLonger(name, 32, "Name");
        Checks.notLonger(description, 100, "Description");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        this.type = type;
        this.name = name;
        this.description = description;
        if (type.canSupportChoices())
            choices = new LinkedHashMap<>();
    }

    @Nonnull
    public OptionType getType()
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
                .map(entry ->
                {
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
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        if (type != OptionType.INTEGER)
            throw new IllegalArgumentException("Cannot add int choice for OptionType." + type);
        choices.put(name, value);
        return this;
    }

    @Nonnull
    public OptionData addChoice(@Nonnull String name, @Nonnull String value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notEmpty(value, "Value");
        Checks.notLonger(name, 100, "Name");
        if (type != OptionType.STRING)
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
        if (type != OptionType.SUB_COMMAND && type != OptionType.SUB_COMMAND_GROUP)
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
        OptionType type = OptionType.fromKey(json.getInt("type"));
        OptionData option = new OptionData(type, name, description);
        option.setRequired(json.getBoolean("required"));
        json.optArray("choices").ifPresent(choices1 ->
                choices1.stream(DataArray::getObject).forEach(o ->
                {
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
