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

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builder for a Slash-Command option.
 */
public class OptionData implements SerializableData
{
    /**
     * The highest positive amount Discord allows the {@link OptionType#NUMBER NUMBER} type to be.
     */
    public static final double MAX_POSITIVE_NUMBER = (1L << 53) - 1; // 1L << 53 is non-inclusive for Discord

    /**
     * The largest negative amount Discord allows the {@link OptionType#NUMBER NUMBER} type to be.
     */
    public static final double MIN_NEGATIVE_NUMBER = -(1L << 53) + 1; // 1L << 53 is non-inclusive for Discord

    /**
     * The maximum length the name of an option can be.
     */
    public static final int MAX_NAME_LENGTH = 32;

    /**
     * The maximum length of the name of Command Option Choice names
     */
    public static final int MAX_CHOICE_NAME_LENGTH = 100;

    /**
     * The maximum length the description of an option can be.
     */
    public static final int MAX_DESCRIPTION_LENGTH = 100;

    /**
     * The maximum length a {@link OptionType#STRING String value} for a choice can be.
     */
    public static final int MAX_CHOICE_VALUE_LENGTH = 100;

    /**
     * The total amount of {@link #getChoices() choices} you can set.
     */
    public static final int MAX_CHOICES = 25;

    private final OptionType type;
    private String name, description;
    private boolean isRequired;
    private final EnumSet<ChannelType> channelTypes = EnumSet.noneOf(ChannelType.class);
    private Number minValue;
    private Number maxValue;
    private Map<String, Object> choices;

    /**
     * Create an option builder.
     * <br>This option is not {@link #isRequired() required} by default.
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The option name, up to {@value #MAX_NAME_LENGTH} alphanumeric (with dash) lowercase characters, as
     *         defined by {@link #MAX_NAME_LENGTH}
     * @param  description
     *         The option description, up to {@value #MAX_DESCRIPTION_LENGTH} characters, as defined by {@link #MAX_DESCRIPTION_LENGTH}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code type} is not null</li>
     *             <li>{@code name} is alphanumeric (with dash), lowercase and between 1 and {@value #MAX_NAME_LENGTH} characters long</li>
     *             <li>{@code description} is between 1 and {@value #MAX_DESCRIPTION_LENGTH} characters long</li>
     *         </ul>
     */
    public OptionData(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description)
    {
        this(type, name, description, false);
    }

    /**
     * Create an option builder.
     *
     * @param  type
     *         The {@link OptionType}
     * @param  name
     *         The option name, up to {@value #MAX_NAME_LENGTH} alphanumeric (with dash) lowercase characters, as
     *         defined by {@link #MAX_NAME_LENGTH}
     * @param  description
     *         The option description, up to {@value #MAX_DESCRIPTION_LENGTH} characters, as defined by {@link #MAX_DESCRIPTION_LENGTH}
     * @param  isRequired
     *         {@code True}, if this option is required
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code type} is not null</li>
     *             <li>{@code name} is alphanumeric (with dash), lowercase and between 1 and {@value #MAX_NAME_LENGTH} characters long</li>
     *             <li>{@code description} is between 1 and {@value #MAX_DESCRIPTION_LENGTH} characters long</li>
     *         </ul>
     */
    public OptionData(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean isRequired)
    {
        Checks.notNull(type, "Type");
        this.type = type;

        setName(name);
        setDescription(description);
        setRequired(isRequired);
        if (type.canSupportChoices())
            choices = new LinkedHashMap<>();
    }

    /**
     * The {@link OptionType} for this option
     *
     * @return The {@link OptionType}
     */
    @Nonnull
    public OptionType getType()
    {
        return type;
    }

    /**
     * The name for this option
     *
     * @return The name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The description for this option
     *
     * @return The description
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * Whether this option is required.
     * <br>This can be configured with {@link #setRequired(boolean)}.
     *
     * <p>Required options must always be set by the command invocation.
     *
     * @return True, if this option is required
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /**
     * The {@link ChannelType ChannelTypes} this option is restricted to.
     * <br>This is empty if the option is not of type {@link OptionType#CHANNEL CHANNEL} or not restricted to specific types.
     *
     * @return {@link EnumSet} of {@link ChannelType}
     */
    @Nonnull
    public EnumSet<ChannelType> getChannelTypes()
    {
        return channelTypes;
    }

    /**
     * The minimum value which can be provided for this option.
     * <br>This returns {@code null} if the value is not set or if the option
     * is not of type {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}.
     *
     * @return The minimum value for this option
     */
    @Nullable
    public Number getMinValue()
    {
        return minValue;
    }

    /**
     * The maximum value which can be provided for this option.
     * <br>This returns {@code null} if the value is not set or if the option
     * is not of type {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}.
     *
     * @return The maximum value for this option
     */
    @Nullable
    public Number getMaxValue()
    {
        return maxValue;
    }

    /**
     * The choices for this option.
     * <br>This is empty by default and can only be configured for specific option types.
     *
     * @return Immutable list of {@link net.dv8tion.jda.api.interactions.commands.Command.Choice Choices}
     *
     * @see #addChoice(String, long)
     * @see #addChoice(String, String)
     */
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
                    else if (entry.getValue() instanceof Double)
                        return new Command.Choice(entry.getKey(), ((Number) entry.getValue()).doubleValue());
                    return new Command.Choice(entry.getKey(), ((Number) entry.getValue()).longValue());
                })
                .collect(Collectors.toList());
    }

    /**
     * Configure the name
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, {@link #MAX_NAME_LENGTH 1-32 characters long}
     *
     * @throws IllegalArgumentException
     *         If the name is null, empty, not alphanumeric, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
     *         as defined by {@link #MAX_NAME_LENGTH}
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, MAX_NAME_LENGTH, "Name");
        Checks.isLowercase(name, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        this.name = name;
        return this;
    }

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-{@value #MAX_DESCRIPTION_LENGTH} characters, as defined by {@link #MAX_DESCRIPTION_LENGTH}
     *
     * @throws IllegalArgumentException
     *         If the name is null or larger than {@link #MAX_DESCRIPTION_LENGTH}
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");
        this.description = description;
        return this;
    }

    /**
     * Configure whether the user must set this option.
     * <br>Required options must always be filled out when using the command.
     *
     * @param  required
     *         True, if this option is required
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setRequired(boolean required)
    {
        this.isRequired = required;
        return this;
    }

    /**
     * Configure the {@link ChannelType ChannelTypes} to restrict this option to.
     * <b>This only applies to options of type {@link OptionType#CHANNEL CHANNEL}.</b>
     *
     * @param  channelTypes
     *         The {@link ChannelType ChannelTypes} to restrict this option to
     *         or empty array to accept all {@link ChannelType ChannelTypes}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@link OptionType type of this option} is {@link OptionType#CHANNEL CHANNEL}</li>
     *             <li>{@code channelTypes} doesn't contain {@code null}</li>
     *             <li>{@code channelTypes} only contains guild channels</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setChannelTypes(@Nonnull ChannelType... channelTypes)
    {
        Checks.noneNull(channelTypes, "ChannelTypes");
        return setChannelTypes(Arrays.asList(channelTypes));
    }

    /**
     * Configure the {@link ChannelType ChannelTypes} to restrict this option to.
     * <b>This only applies to options of type {@link OptionType#CHANNEL CHANNEL}.</b>
     *
     * @param  channelTypes
     *         The {@link ChannelType ChannelTypes} to restrict this option to
     *         or empty collection to accept all {@link ChannelType ChannelTypes}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@link OptionType type of this option} is {@link OptionType#CHANNEL CHANNEL}</li>
     *             <li>{@code channelTypes} is not null</li>
     *             <li>{@code channelTypes} doesn't contain {@code null}</li>
     *             <li>{@code channelTypes} only contains guild channels</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setChannelTypes(@Nonnull Collection<ChannelType> channelTypes)
    {
        if (type != OptionType.CHANNEL)
            throw new IllegalArgumentException("Can only apply channel type restriction to options of type CHANNEL");
        Checks.notNull(channelTypes, "ChannelType collection");
        Checks.noneNull(channelTypes, "ChannelType");

        for (ChannelType channelType : channelTypes)
        {
            if (!channelType.isGuild())
                throw new IllegalArgumentException("Provided channel type is not a guild channel type. Provided: " + channelType);
        }
        this.channelTypes.clear();
        this.channelTypes.addAll(channelTypes);
        return this;
    }

    /**
     * Configure the minimal value which can be provided for this option.
     *
     * @param  value
     *         The minimal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@link OptionType type of this option} is {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}</li>
     *             <li>{@code value} is larger than or equal to {@link OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMinValue(long value)
    {
        if (type != OptionType.INTEGER && type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min and max long value for options of type INTEGER or NUMBER");
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Long value may not be lower than %f", MIN_NEGATIVE_NUMBER);
        this.minValue = value;
        return this;
    }

    /**
     * Configure the minimal value which can be provided for this option.
     *
     * @param  value
     *         The minimal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@link OptionType type of this option} is {@link OptionType#NUMBER NUMBER}</li>
     *             <li>{@code value} is larger than or equal to {@link OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMinValue(double value)
    {
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min double value for options of type NUMBER");
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Double value may not be lower than %f", MIN_NEGATIVE_NUMBER);
        this.minValue = value;
        return this;
    }

    /**
     * Configure the maximal value which can be provided for this option.
     *
     * @param  value
     *         The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@link OptionType type of this option} is {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}</li>
     *             <li>{@code value} is lower than or equal to {@link OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMaxValue(long value)
    {
        if (type != OptionType.INTEGER && type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min and max long value for options of type INTEGER or NUMBER");
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Long value may not be larger than %f", MAX_POSITIVE_NUMBER);
        this.maxValue = value;
        return this;
    }

    /**
     * Configure the maximal value which can be provided for this option.
     *
     * @param  value
     *         The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@link OptionType type of this option} is {@link OptionType#NUMBER NUMBER}</li>
     *             <li>{@code value} is lower than or equal to {@link OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMaxValue(double value)
    {
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set max double value for options of type NUMBER");
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Double value may not be larger than %f", MAX_POSITIVE_NUMBER);
        this.maxValue = value;
        return this;
    }

    /**
     * Configure the minimal and maximal value which can be provided for this option.
     *
     * @param  minValue
     *         The minimal value which can be provided for this option.
     * @param  maxValue
     *         The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@link OptionType type of this option} is {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}</li>
     *             <li>{@code minValue} is larger than or equal to {@link OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}</li>
     *             <li>{@code maxValue} is lower than or equal to {@link OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setRequiredRange(long minValue, long maxValue)
    {
        if (type != OptionType.INTEGER && type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min and max long value for options of type INTEGER or NUMBER");
        Checks.check(minValue >= MIN_NEGATIVE_NUMBER, "Long value may not be lower than %f", MIN_NEGATIVE_NUMBER);
        Checks.check(maxValue <= MAX_POSITIVE_NUMBER, "Long value may not be larger than %f", MAX_POSITIVE_NUMBER);
        this.minValue = minValue;
        this.maxValue = maxValue;
        return this;
    }

    /**
     * Configure the minimal and maximal value which can be provided for this option.
     *
     * @param  minValue
     *         The minimal value which can be provided for this option.
     * @param  maxValue
     *         The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@link OptionType type of this option} is {@link OptionType#NUMBER NUMBER}</li>
     *             <li>{@code minValue} is larger than or equal to {@link OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}</li>
     *             <li>{@code maxValue} is lower than or equal to {@link OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setRequiredRange(double minValue, double maxValue)
    {
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min and max double value for options of type NUMBER");
        Checks.check(minValue >= MIN_NEGATIVE_NUMBER, "Double value may not be lower than %f", MIN_NEGATIVE_NUMBER);
        Checks.check(maxValue <= MAX_POSITIVE_NUMBER, "Double value may not be larger than %f", MAX_POSITIVE_NUMBER);
        this.minValue = minValue;
        this.maxValue = maxValue;
        return this;
    }

    /**
     * Add a predefined choice for this option.
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     *         The name used in the client, up to {@value #MAX_CHOICE_NAME_LENGTH} characters long, as defined by
     *         {@link #MAX_CHOICE_NAME_LENGTH}
     * @param  value
     *         The value received in {@link net.dv8tion.jda.api.interactions.commands.OptionMapping OptionMapping}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value #MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not less than {@link #MIN_NEGATIVE_NUMBER} and not larger than {@link #MAX_POSITIVE_NUMBER}</li>
     *             <li>The amount of already set choices is less than {@link #MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is {@link OptionType#NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoice(@Nonnull String name, double value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, MAX_CHOICE_NAME_LENGTH, "Name");
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Double value may not be lower than %f", MIN_NEGATIVE_NUMBER);
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Double value may not be larger than %f", MAX_POSITIVE_NUMBER);
        Checks.check(choices.size() < MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Cannot add double choice for OptionType." + type);
        choices.put(name, value);
        return this;
    }

    /**
     * Add a predefined choice for this option.
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     *         The name used in the client
     * @param  value
     *         The value received in {@link net.dv8tion.jda.api.interactions.commands.OptionMapping OptionMapping}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value #MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not less than {@link #MIN_NEGATIVE_NUMBER} and not larger than {@link #MAX_POSITIVE_NUMBER}</li>
     *             <li>The amount of already set choices is less than {@link #MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is {@link OptionType#INTEGER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoice(@Nonnull String name, long value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, MAX_CHOICE_NAME_LENGTH, "Name");
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Long value may not be lower than %f", MIN_NEGATIVE_NUMBER);
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Long value may not be larger than %f", MAX_POSITIVE_NUMBER);
        Checks.check(choices.size() < MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        if (type != OptionType.INTEGER)
            throw new IllegalArgumentException("Cannot add long choice for OptionType." + type);
        choices.put(name, value);
        return this;
    }

    /**
     * Add a predefined choice for this option.
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     *         The name used in the client
     * @param  value
     *         The value received in {@link net.dv8tion.jda.api.interactions.commands.OptionMapping OptionMapping}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value #MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not null, empty and less or equal to {@value #MAX_CHOICE_VALUE_LENGTH} characters long</li>
     *             <li>The amount of already set choices is less than {@link #MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is {@link OptionType#STRING}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoice(@Nonnull String name, @Nonnull String value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notEmpty(value, "Value");
        Checks.notLonger(name, MAX_CHOICE_NAME_LENGTH, "Name");
        Checks.notLonger(value, MAX_CHOICE_VALUE_LENGTH, "Value");
        Checks.check(choices.size() < MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        if (type != OptionType.STRING)
            throw new IllegalArgumentException("Cannot add string choice for OptionType." + type);
        choices.put(name, value);
        return this;
    }

    /**
     * Adds up to 25 predefined choices for this option.
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  choices
     *         The choices to add
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>The {@link OptionType} does {@link OptionType#canSupportChoices() support choices}</li>
     *             <li>The provided {@code choices} are not null</li>
     *             <li>The amount of {@code choices} provided is smaller than {@link #MAX_CHOICES} when combined with already set choices</li>
     *             <li>The {@link OptionType} of the choices is either {@link OptionType#INTEGER}, {@link OptionType#STRING} or {@link OptionType#NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoices(@Nonnull Command.Choice... choices)
    {
        if (this.choices == null)
            throw new IllegalStateException("Cannot add choices for an option of type " + type);
        Checks.noneNull(choices, "Choices");
        Checks.check(choices.length + this.choices.size() <= MAX_CHOICES, "Cannot have more than 25 choices for one option!");
        for (Command.Choice choice : choices)
        {
            if (type == OptionType.INTEGER)
                addChoice(choice.getName(), choice.getAsLong());
            else if (type == OptionType.STRING)
                addChoice(choice.getName(), choice.getAsString());
            else if (type == OptionType.NUMBER)
                addChoice(choice.getName(), choice.getAsDouble());
            else
                throw new IllegalArgumentException("Cannot add choice for type " + type);
        }
        return this;
    }

    /**
     * Adds up to 25 predefined choices for this option.
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  choices
     *         The choices to add
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>The {@link OptionType} does {@link OptionType#canSupportChoices() support choices}</li>
     *             <li>The provided {@code choices} are not null</li>
     *             <li>The amount of {@code choices} provided is smaller than {@link #MAX_CHOICES} when combined with already set choices</li>
     *             <li>The {@link OptionType} of the choices is either {@link OptionType#INTEGER}, {@link OptionType#STRING} or {@link OptionType#NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoices(@Nonnull Collection<? extends Command.Choice> choices)
    {
        Checks.noneNull(choices, "Choices");
        return addChoices(choices.toArray(new Command.Choice[0]));
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
        if (type == OptionType.CHANNEL && !channelTypes.isEmpty())
            json.put("channel_types", channelTypes.stream().map(ChannelType::getId).collect(Collectors.toList()));
        if (type == OptionType.INTEGER || type == OptionType.NUMBER)
        {
            if (minValue != null)
                json.put("min_value", minValue);
            if (maxValue != null)
                json.put("max_value", maxValue);
        }
        return json;
    }

    /**
     * Parses the provided serialization back into an OptionData instance.
     * <br>This is the reverse function for {@link #toData()}.
     *
     * @param  json
     *         The serialized {@link DataObject} representing the option
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the serialized object is missing required fields
     * @throws IllegalArgumentException
     *         If any of the values are failing the respective checks such as length
     *
     * @return The parsed OptionData instance, which can be further configured through setters
     */
    @Nonnull
    public static OptionData fromData(@Nonnull DataObject json)
    {
        String name = json.getString("name");
        String description = json.getString("description");
        OptionType type = OptionType.fromKey(json.getInt("type"));
        OptionData option = new OptionData(type, name, description);
        option.setRequired(json.getBoolean("required"));
        if (type == OptionType.INTEGER || type == OptionType.NUMBER)
        {
            if (!json.isNull("min_value"))
            {
                if (json.isType("min_value", DataType.INT))
                    option.setMinValue(json.getLong("min_value"));
                else if (json.isType("min_value", DataType.FLOAT))
                    option.setMinValue(json.getDouble("min_value"));
            }
            if (!json.isNull("max_value"))
            {
                if (json.isType("max_value", DataType.INT))
                    option.setMaxValue(json.getLong("max_value"));
                else if (json.isType("max_value", DataType.FLOAT))
                    option.setMaxValue(json.getDouble("max_value"));
            }
        }
        if (type == OptionType.CHANNEL)
        {
            option.setChannelTypes(json.optArray("channel_types")
                    .map(it -> it.stream(DataArray::getInt).map(ChannelType::fromId).collect(Collectors.toSet()))
                    .orElse(Collections.emptySet()));
        }
        json.optArray("choices").ifPresent(choices1 ->
                choices1.stream(DataArray::getObject).forEach(o ->
                {
                    if (o.isType("value", DataType.FLOAT))
                        option.addChoice(o.getString("name"), o.getDouble("value"));
                    else if (o.isType("value", DataType.INT))
                        option.addChoice(o.getString("name"), o.getLong("value"));
                    else
                        option.addChoice(o.getString("name"), o.get("value").toString());
                })
        );
        return option;
    }
}
