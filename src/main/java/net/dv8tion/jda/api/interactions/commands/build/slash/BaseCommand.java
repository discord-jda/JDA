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

package net.dv8tion.jda.api.interactions.commands.build.slash;

import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseCommand<T extends BaseCommand<T>> extends CommandData
{
    protected final DataArray options = DataArray.empty();
    protected String description;

    public BaseCommand(@Nonnull String name, @Nonnull String description)
    {
        super(name);
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        Checks.isLowercase(name, "Name");
        this.name = name;
        this.description = description;
    }

    /**
     * Configure the name
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters
     *
     * @throws IllegalArgumentException
     *         If the name is null, not alphanumeric, or not between 1-32 characters
     *
     * @return The builder, for chaining
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public T setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        Checks.isLowercase(name, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        this.name = name;
        return (T) this;
    }

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
    @SuppressWarnings("unchecked")
    public T setDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
        this.description = description;
        return (T) this;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * The configured description
     *
     * @return The description
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * The options for this command.
     *
     * @return Immutable list of {@link OptionData}
     */
    @Nonnull
    public List<OptionData> getOptions()
    {
        return options.stream(DataArray::getObject)
                .map(OptionData::fromData)
                .filter(it -> it.getType().getKey() > OptionType.SUB_COMMAND_GROUP.getKey())
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return super.toData()
                .put("description", description)
                .put("options", options);
    }

    @NotNull
    @Override
    public CommandType getCommandType()
    {
        return CommandType.CHAT_INPUT;
    }
}
