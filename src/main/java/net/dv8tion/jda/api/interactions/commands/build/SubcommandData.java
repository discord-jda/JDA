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

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
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
 * Builder for a Slash-Command subcommand.
 */
public class SubcommandData implements SerializableData
{
    protected final DataArray options = DataArray.empty();
    protected String name, description;
    private boolean allowRequired = true;

    /**
     * Create a subcommand builder.
     *
     * @param  name
     *         The subcommand name, 1-32 lowercase alphanumeric characters
     * @param  description
     *         The subcommand description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         If any of the following requirements are not met
     *         <ul>
     *             <li>The name must be lowercase alphanumeric (with dash), 1-32 characters long</li>
     *             <li>The description must be 1-100 characters long</li>
     *         </ul>
     */
    public SubcommandData(@Nonnull String name, @Nonnull String description)
    {
        setName(name);
        setDescription(description);
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
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData setName(@Nonnull String name)
    {
        Checks.inRange(name, 1, 32, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        Checks.isLowercase(name, "Name");
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
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData setDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
        this.description = description;
        return this;
    }

    /**
     * Adds up to 25 options to this subcommand.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOptions(@Nonnull OptionData... options)
    {
        Checks.noneNull(options, "Option");
        Checks.check(options.length + this.options.length() <= 25, "Cannot have more than 25 options for a subcommand!");
        boolean allowRequired = this.allowRequired;
        for (OptionData option : options)
        {
            Checks.check(option.getType() != OptionType.SUB_COMMAND, "Cannot add a subcommand to a subcommand!");
            Checks.check(option.getType() != OptionType.SUB_COMMAND_GROUP, "Cannot add a subcommand group to a subcommand!");
            Checks.check(allowRequired || !option.isRequired(), "Cannot add required options after non-required options!");
            allowRequired = option.isRequired(); // prevent adding required options after non-required options
        }

        Checks.checkUnique(Stream.concat(getOptions().stream(), Arrays.stream(options)).map(OptionData::getName),
            "Cannot have multiple options with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count });

        this.allowRequired = allowRequired;
        for (OptionData option : options)
            this.options.add(option);

        return this;
    }

    /**
     * Adds up to 25 options to this subcommand.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  options
     *         The {@link OptionData options} to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOptions(@Nonnull Collection<? extends OptionData> options)
    {
        Checks.noneNull(options, "Options");
        return addOptions(options.toArray(new OptionData[0]));
    }

    /**
     * Adds an option to this subcommand.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     * @param  autoComplete
     *         Whether this option supports auto-complete via {@link CommandAutoCompleteInteractionEvent},
     *         only supported for option types which {@link OptionType#canSupportChoices() support choices}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If the provided option type does not support auto-complete</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required, boolean autoComplete)
    {
        return addOptions(new OptionData(type, name, description)
                .setRequired(required)
                .setAutoComplete(autoComplete));
    }

    /**
     * Adds an option to this subcommand.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     * @param  required
     *         Whether this option is required (See {@link OptionData#setRequired(boolean)})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required)
    {
        return addOption(type, name, description, required, false);
    }

    /**
     * Adds an option to this subcommand.
     * <br>The option is set to be non-required! You can use {@link #addOption(OptionType, String, String, boolean)} to add a required option instead.
     *
     * <p>Required options must be added before non-required options!
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The lowercase option name, 1-32 characters
     * @param  description
     *         The option description, 1-100 characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If this option is required and you already added a non-required option.</li>
     *             <li>If more than 25 options are provided.</li>
     *             <li>If the option name is not unique</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return The SubcommandData instance, for chaining
     */
    @Nonnull
    public SubcommandData addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        return addOption(type, name, description, false);
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
        return description;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("type", OptionType.SUB_COMMAND.getKey())
                .put("name", name)
                .put("description", description)
                .put("options", options);
    }

    /**
     * Parses the provided serialization back into an SubcommandData instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  json
     *         The serialized {@link DataObject} representing the subcommand
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed SubcommandData instance, which can be further configured through setters
     */
    @Nonnull
    public static SubcommandData fromData(@Nonnull DataObject json)
    {
        String name = json.getString("name");
        String description = json.getString("description");
        SubcommandData sub = new SubcommandData(name, description);
        json.optArray("options").ifPresent(arr ->
                arr.stream(DataArray::getObject)
                        .map(OptionData::fromData)
                        .forEach(sub::addOptions)
        );
        return sub;
    }

    /**
     * Converts the provided {@link Command.Subcommand} into a SubCommandData instance.
     *
     * @param  subcommand
     *         The subcommand to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the subcommand has illegal configuration
     *
     * @return An instance of SubCommandData
     */
    @Nonnull
    public static SubcommandData fromSubcommand(@Nonnull Command.Subcommand subcommand)
    {
        Checks.notNull(subcommand, "Subcommand");
        SubcommandData data = new SubcommandData(subcommand.getName(), subcommand.getDescription());
        subcommand.getOptions()
                .stream()
                .map(OptionData::fromOption)
                .forEach(data::addOptions);
        return data;
    }
}
