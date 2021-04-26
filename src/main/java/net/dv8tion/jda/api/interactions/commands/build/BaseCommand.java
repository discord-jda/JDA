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
import java.util.List;
import java.util.stream.Collectors;

public class BaseCommand<T extends BaseCommand<T>> implements SerializableData
{
    protected final DataArray options = DataArray.empty();
    protected String name, description;

    public BaseCommand(@Nonnull String name, @Nonnull String description)
    {
        Checks.notEmpty(name, "Name");
        Checks.notEmpty(description, "Description");
        Checks.notLonger(name, 32, "Name");
        Checks.notLonger(description, 100, "Description");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        this.name = name;
        this.description = description;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public T setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        this.name = name;
        return (T) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public T setDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
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
                .filter(it -> it.getType().getKey() > OptionType.SUB_COMMAND_GROUP.getKey())
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
