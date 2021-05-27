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

/**
 * Builder for a Slash-Command group.
 */
public class SubcommandGroupData extends OptionData implements SerializableData
{
    private final DataArray options = DataArray.empty();

    /**
     * Create an group builder.
     *
     * @param name
     *        The group name, 1-32 lowercase alphanumeric characters
     * @param description
     *        The group description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If any of the following requirements are not met
     *         <ul>
     *             <li>The name must be lowercase alphanumeric (with dash), 1-32 characters long</li>
     *             <li>The description must be 1-100 characters long</li>
     *         </ul>
     */
    public SubcommandGroupData(@Nonnull String name, @Nonnull String description)
    {
        super(OptionType.SUB_COMMAND_GROUP, name, description);
    }

    /**
     * The {@link SubcommandData Subcommands} in this group.
     *
     * @return Immutable list of {@link SubcommandData}
     */
    @Nonnull
    public List<SubcommandData> getSubcommands()
    {
        return options.stream(DataArray::getObject)
                .map(SubcommandData::load)
                .collect(Collectors.toList());
    }

    /**
     * Add a {@link SubcommandData} to this group.
     *
     * @param  data
     *         The subcommand to add
     *
     * @throws IllegalArgumentException
     *         If null is provided, or more than 25 subcommands are provided
     *
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData addSubcommand(@Nonnull SubcommandData data)
    {
        Checks.notNull(data, "Subcommand");
        Checks.check(options.length() < 25, "Cannot have more than 25 subcommands in one group!");
        options.add(data);
        return this;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return super.toData().put("options", options);
    }

    /**
     * Parses the provided serialization back into an SubcommandGroupData instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  json
     *         The serialized {@link DataObject} representing the group
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed SubcommandGroupData instance, which can be further configured through setters
     */
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
