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

import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseCommand<T extends BaseCommand<T>> implements SerializableData
{
    protected final DataArray options = DataArray.empty();
    protected String name, description;
    protected final CommandType commandType;

    public BaseCommand(CommandType commandType, @Nonnull String name, String description)
    {
        this.commandType = commandType;
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");

        if(commandType == CommandType.SLASH) {
            Checks.check(description.length() > 0, "Description");
            Checks.notLonger(description, 100, "Description");
            Checks.isLowercase(name, "Name");
            Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
            this.description = description;
        }
        this.name = name;
    }

    /**
     * Configure the name
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters.
     *         If the command is not of type {@link CommandType#SLASH}, the name may be non-alphanumeric and capitalized
     *
     * @throws IllegalArgumentException
     *         <p>If the command is of type {@link CommandType#SLASH}:
     *             <ul>
     *                 <li>The name is null</li>
     *                 <li>The name is not alphanumeric</li>
     *                 <li>The name is not between 1-32 characters</li>
     *             </ul>
     *         </p>
     *         <p>If the command is of type {@link CommandType#USER_CONTEXT} or {@link CommandType#MESSAGE_CONTEXT}:
     *             <ul>
     *                 <li>The name is null</li>
     *                 <li>The name is not between 1-32 characters</li>
     *             </ul>
     *         </p>
     *
     * @return The builder, for chaining
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public T setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        // If the description is null it means it's not a slash command
        if(commandType == CommandType.SLASH)
        {
            Checks.isLowercase(name, "Name");
            Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        }
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
     *         If the name is null or not between 1-100 characters.
     *         Also, if the command is not of type {@link CommandType#SLASH}
     *
     * @return The builder, for chaining
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public T setDescription(@Nonnull String description)
    {
        Checks.check(commandType == CommandType.SLASH, "You can only modify the descriptions of slash commands");
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
        this.description = description;
        return (T) this;
    }

    /**
     * The configured name
     *
     * @return The name
     */
    @Nonnull
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
        return description == null ? "" : description;
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
        return DataObject.empty()
                .put("name", name)
                .put("description", description == null ? "" : description)
                .put("options", options);
    }
}
