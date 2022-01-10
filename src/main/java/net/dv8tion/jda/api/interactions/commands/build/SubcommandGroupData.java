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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builder for a Slash-Command group.
 */
public class SubcommandGroupData implements SerializableData
{
    private final DataArray options = DataArray.empty();
    private String name, description;

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
        Checks.notEmpty(name, "Name");
        Checks.notEmpty(description, "Description");
        Checks.notLonger(name, 32, "Name");
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
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        Checks.isLowercase(name, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        this.name = name;
        return this;
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
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData setDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
        this.description = description;
        return this;
    }

    /**
     * The name for this subcommand group
     *
     * @return The name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The description for this  subcommand group
     *
     * @return The description
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * The {@link SubcommandData Subcommands} in this group.
     * <br>These subcommand instances are <b>reconstructed</b>,
     * which means that any modifications will not be reflected in the backing state.
     *
     * @return Immutable list of {@link SubcommandData}
     */
    @Nonnull
    public List<SubcommandData> getSubcommands()
    {
        return options.stream(DataArray::getObject)
                .map(SubcommandData::fromData)
                .collect(Collectors.toList());
    }

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this group.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than 25 subcommands, or duplicate subcommand names are provided.
     *
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData addSubcommands(@Nonnull SubcommandData... subcommands)
    {
        Checks.noneNull(subcommands, "Subcommand");
        Checks.check(subcommands.length + options.length() <= 25, "Cannot have more than 25 subcommands in one group!");
        Checks.checkUnique(
            Stream.concat(getSubcommands().stream(), Arrays.stream(subcommands)).map(SubcommandData::getName),
            "Cannot have multiple subcommands with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count }
        );
        for (SubcommandData subcommand : subcommands)
            options.add(subcommand);
        return this;
    }

    /**
     * Add up to 25 {@link SubcommandData Subcommands} to this group.
     *
     * @param  subcommands
     *         The subcommands to add
     *
     * @throws IllegalArgumentException
     *         If null, more than 25 subcommands, or duplicate subcommand names are provided.
     *
     * @return The SubcommandGroupData instance, for chaining
     */
    @Nonnull
    public SubcommandGroupData addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands)
    {
        Checks.noneNull(subcommands, "Subcommands");
        return addSubcommands(subcommands.toArray(new SubcommandData[0]));
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("type", OptionType.SUB_COMMAND_GROUP.getKey())
                .put("name", name)
                .put("description", description)
                .put("options", options);
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
    public static SubcommandGroupData fromData(@Nonnull DataObject json)
    {
        String name = json.getString("name");
        String description = json.getString("description");
        SubcommandGroupData group = new SubcommandGroupData(name, description);
        json.optArray("options").ifPresent(arr ->
                arr.stream(DataArray::getObject)
                        .map(SubcommandData::fromData)
                        .forEach(group::addSubcommands)
        );
        return group;
    }

    /**
     * Converts the provided {@link Command.SubcommandGroup} into a SubcommandGroupData instance.
     *
     * @param  group
     *         The subcommand group to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the subcommand group has illegal configuration
     *
     * @return An instance of SubcommandGroupData
     */
    @Nonnull
    public static SubcommandGroupData fromGroup(@Nonnull Command.SubcommandGroup group)
    {
        Checks.notNull(group, "Subcommand Group");
        SubcommandGroupData data = new SubcommandGroupData(group.getName(), group.getDescription());
        group.getSubcommands()
                .stream()
                .map(SubcommandData::fromSubcommand)
                .forEach(data::addSubcommands);
        return data;
    }
}
