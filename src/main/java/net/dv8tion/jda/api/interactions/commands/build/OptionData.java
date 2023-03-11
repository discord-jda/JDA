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

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils;

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

    /**
     * The maximum length for a {@link OptionType#STRING String option}.
     */
    public static final int MAX_STRING_OPTION_LENGTH = 6000;

    private final OptionType type;
    private String name, description;
    private final LocalizationMap nameLocalizations = new LocalizationMap(this::checkName);
    private final LocalizationMap descriptionLocalizations = new LocalizationMap(this::checkDescription);
    private boolean isRequired, isAutoComplete;
    private final EnumSet<ChannelType> channelTypes = EnumSet.noneOf(ChannelType.class);
    private Number minValue;
    private Number maxValue;
    private Integer minLength, maxLength;
    private List<Command.Choice> choices;

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
     *         <ul>
     *             <li>If {@code type} is null</li>
     *             <li>If {@code type} is {@link OptionType#UNKNOWN UNKNOWN}</li>
     *             <li>If {@code name} is not alphanumeric (with dash), lowercase and between 1 and {@value #MAX_NAME_LENGTH} characters long</li>
     *             <li>If {@code description} is not between 1 and {@value #MAX_DESCRIPTION_LENGTH} characters long</li>
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
     *         <ul>
     *             <li>If {@code type} is null</li>
     *             <li>If {@code type} is {@link OptionType#UNKNOWN UNKNOWN}</li>
     *             <li>If {@code name} is not alphanumeric (with dash), lowercase and between 1 and {@value #MAX_NAME_LENGTH} characters long</li>
     *             <li>If {@code description} is not between 1 and {@value #MAX_DESCRIPTION_LENGTH} characters long</li>
     *         </ul>
     */
    public OptionData(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean isRequired)
    {
        this(type, name, description, isRequired, false);
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
     * @param  isAutoComplete
     *         True, if auto-complete should be supported (requires {@link OptionType#canSupportChoices()})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code type} is null</li>
     *             <li>If {@code type} is {@link OptionType#UNKNOWN UNKNOWN}, {@link OptionType#SUB_COMMAND SUB_COMMAND}, or {@link OptionType#SUB_COMMAND_GROUP SUB_COMMAND_GROUP}</li>
     *             <li>If {@code name} is not alphanumeric (with dash), lowercase and between 1 and {@value #MAX_NAME_LENGTH} characters long</li>
     *             <li>If {@code description} is not between 1 and {@value #MAX_DESCRIPTION_LENGTH} characters long</li>
     *             <li>If {@link OptionType#canSupportChoices()} is false and {@code isAutoComplete} is true</li>
     *         </ul>
     */
    public OptionData(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean isRequired, boolean isAutoComplete)
    {
        Checks.notNull(type, "Type");
        Checks.check(type != OptionType.UNKNOWN, "Cannot make option of unknown type!");
        Checks.check(type != OptionType.SUB_COMMAND, "Cannot make a subcommand group with OptionData. Use addSubcommands(...) instead!");
        Checks.check(type != OptionType.SUB_COMMAND_GROUP, "Cannot make a subcommand group with OptionData. Use addSubcommandGroups(...) instead!");
        this.type = type;

        setName(name);
        setDescription(description);
        setRequired(isRequired);
        if (type.canSupportChoices())
            choices = new ArrayList<>();
        setAutoComplete(isAutoComplete);
    }

    protected void checkName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, MAX_NAME_LENGTH, "Name");
        Checks.isLowercase(name, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
    }

    protected void checkDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");
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
     * The localizations of this option's name for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
     */
    @Nonnull
    public LocalizationMap getNameLocalizations()
    {
        return nameLocalizations;
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
     * The localizations of this option's description for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
     */
    @Nonnull
    public LocalizationMap getDescriptionLocalizations()
    {
        return descriptionLocalizations;
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
     * Whether this option supports auto-complete interactions
     * via {@link CommandAutoCompleteInteractionEvent}.
     *
     * @return True, if this option supports auto-complete
     */
    public boolean isAutoComplete()
    {
        return isAutoComplete;
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
     * The minimum length for strings which can be provided for this option.
     * <br>This returns {@code null} if the value is not set or if the option
     * is not of type {@link OptionType#STRING STRING}.
     *
     * @return The minimum length for strings for this option or {@code null}
     */
    @Nullable
    public Integer getMinLength()
    {
        return minLength;
    }

    /**
     * The maximum length for strings which can be provided for this option.
     * <br>This returns {@code null} if the value is not set or if the option
     * is not of type {@link OptionType#STRING STRING}.
     *
     * @return The maximum length for strings for this option or {@code null}
     */
    @Nullable
    public Integer getMaxLength()
    {
        return maxLength;
    }

    /**
     * The choices for this option.
     * <br>This is empty by default and can only be configured for specific option types.
     *
     * @return Immutable list of {@link Command.Choice Choices}
     *
     * @see #addChoice(String, long)
     * @see #addChoice(String, String)
     */
    @Nonnull
    public List<Command.Choice> getChoices()
    {
        if (choices == null || choices.isEmpty())
            return Collections.emptyList();
        return Collections.unmodifiableList(choices);
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
        checkName(name);
        this.name = name;
        return this;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this option's name.
     *
     * @param  locale
     *         The locale to associate the translated name with
     *
     * @param  name
     *         The translated name to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the name is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the name does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    public OptionData setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
    {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslation(locale, name);
        return this;
    }

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this option's name.
     *
     * @param  map
     *         The map from which to transfer the translated names
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a name which does not pass the corresponding {@link #setName(String) name check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    public OptionData setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslations(map);
        return this;
    }

    /**
     * Configure the description
     *
     * @param  description
     *         The description, 1-{@value #MAX_DESCRIPTION_LENGTH} characters, as defined by {@link #MAX_DESCRIPTION_LENGTH}
     *
     * @throws IllegalArgumentException
     *         If the name is null, empty, or longer than {@value #MAX_DESCRIPTION_LENGTH}, as defined by {@link #MAX_DESCRIPTION_LENGTH}
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setDescription(@Nonnull String description)
    {
        checkDescription(description);
        this.description = description;
        return this;
    }

    /**
     * Sets a {@link DiscordLocale language-specific} localization of this option's description.
     *
     * @param  locale
     *         The locale to associate the translated description with
     *
     * @param  description
     *         The translated description to put
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the description is null</li>
     *             <li>If the locale is {@link DiscordLocale#UNKNOWN}</li>
     *             <li>If the description does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    public OptionData setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description)
    {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslation(locale, description);
        return this;
    }

    /**
     * Sets multiple {@link DiscordLocale language-specific} localizations of this option's description.
     *
     * @param  map
     *         The map from which to transfer the translated descriptions
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link DiscordLocale#UNKNOWN} key</li>
     *             <li>If the map contains a description which does not pass the corresponding {@link #setDescription(String) description check}</li>
     *         </ul>
     *
     * @return This builder instance, for chaining
     */
    @Nonnull
    public OptionData setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslations(map);
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
     * Configure whether this option should support auto-complete interactions
     * via {@link CommandAutoCompleteInteractionEvent}.
     *
     * <p>This is only supported for options which support choices. See {@link OptionType#canSupportChoices()}.
     *
     * @param  autoComplete
     *         True, if auto-complete should be supported
     *
     * @throws IllegalStateException
     *         If this option is already configured to use choices or the option type does not support auto-complete
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setAutoComplete(boolean autoComplete)
    {
        if (autoComplete)
        {
            if (choices == null || !type.canSupportChoices())
                throw new IllegalStateException("Cannot enable auto-complete for options of type " + type);
            if (!choices.isEmpty())
                throw new IllegalStateException("Cannot enable auto-complete for options with choices");
        }

        isAutoComplete = autoComplete;
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
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#CHANNEL CHANNEL}</li>
     *             <li>If {@code channelTypes} contain {@code null}</li>
     *             <li>If {@code channelTypes} contains non-guild channels</li>
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
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#CHANNEL CHANNEL}</li>
     *             <li>If {@code channelTypes} is null</li>
     *             <li>If {@code channelTypes} contain {@code null}</li>
     *             <li>If {@code channelTypes} contains non-guild channels</li>
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
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}</li>
     *             <li>If {@code value} is less than {@link OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMinValue(long value)
    {
        if (type != OptionType.INTEGER && type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min and max long value for options of type INTEGER or NUMBER");
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Long value may not be less than %f", MIN_NEGATIVE_NUMBER);
        this.minValue = value;
        return this;
    }

    /**
     * Configure the minimal value which can be provided for this option.
     *
     * @param  value
     *         The minimal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#NUMBER NUMBER}</li>
     *             <li>If {@code value} is less than {@link OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMinValue(double value)
    {
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min double value for options of type NUMBER");
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Double value may not be less than %f", MIN_NEGATIVE_NUMBER);
        this.minValue = value;
        return this;
    }

    /**
     * Configure the maximal value which can be provided for this option.
     *
     * @param  value
     *         The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}</li>
     *             <li>If {@code value} is greater than {@link OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMaxValue(long value)
    {
        if (type != OptionType.INTEGER && type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min and max long value for options of type INTEGER or NUMBER");
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Long value may not be greater than %f", MAX_POSITIVE_NUMBER);
        this.maxValue = value;
        return this;
    }

    /**
     * Configure the maximal value which can be provided for this option.
     *
     * @param  value
     *         The maximal value which can be provided for this option.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#NUMBER NUMBER}</li>
     *             <li>If {@code value} is greater than {@link OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMaxValue(double value)
    {
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set max double value for options of type NUMBER");
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Double value may not be greater than %f", MAX_POSITIVE_NUMBER);
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
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}</li>
     *             <li>If {@code minValue} is less than or not equal to {@link OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}</li>
     *             <li>If {@code maxValue} is greater than {@link OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setRequiredRange(long minValue, long maxValue)
    {
        if (type != OptionType.INTEGER && type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min and max long value for options of type INTEGER or NUMBER");
        Checks.check(minValue >= MIN_NEGATIVE_NUMBER, "Long value may not be less than %f", MIN_NEGATIVE_NUMBER);
        Checks.check(maxValue <= MAX_POSITIVE_NUMBER, "Long value may not be greater than %f", MAX_POSITIVE_NUMBER);
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
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#NUMBER NUMBER}</li>
     *             <li>If {@code minValue} is less than or not equal to {@link OptionData#MIN_NEGATIVE_NUMBER MIN_NEGATIVE_NUMBER}</li>
     *             <li>If {@code maxValue} is greater than {@link OptionData#MAX_POSITIVE_NUMBER MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setRequiredRange(double minValue, double maxValue)
    {
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Can only set min and max double value for options of type NUMBER");
        Checks.check(minValue >= MIN_NEGATIVE_NUMBER, "Double value may not be less than %f", MIN_NEGATIVE_NUMBER);
        Checks.check(maxValue <= MAX_POSITIVE_NUMBER, "Double value may not be greater than %f", MAX_POSITIVE_NUMBER);
        this.minValue = minValue;
        this.maxValue = maxValue;
        return this;
    }

    /**
     * Configure the minimum length for strings which can be provided for this option.
     *
     * @param  minLength
     *         The minimum length for strings which can be provided for this option.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#STRING STRING}</li>
     *             <li>If {@code minLength} is not positive</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMinLength(int minLength)
    {
        if (type != OptionType.STRING)
            throw new IllegalArgumentException("Can only set min length for options of type STRING");
        Checks.positive(minLength, "Min length");
        this.minLength = minLength;
        return this;
    }

    /**
     * Configure the maximum length for strings which can be provided for this option.
     *
     * @param  maxLength
     *         The maximum length for strings which can be provided for this option.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#STRING STRING}</li>
     *             <li>If {@code maxLength} is not positive or greater than {@value MAX_STRING_OPTION_LENGTH}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setMaxLength(int maxLength)
    {
        if (type != OptionType.STRING)
            throw new IllegalArgumentException("Can only set max length for options of type STRING");
        Checks.positive(maxLength, "Max length");
        Checks.check(maxLength <= MAX_STRING_OPTION_LENGTH, "Max length must not be greater than %d. Provided: %d", MAX_STRING_OPTION_LENGTH, maxLength);
        this.maxLength = maxLength;
        return this;
    }

    /**
     * Configure the minimum and maximum length for strings which can be provided for this option.
     *
     * @param  minLength
     *         The minimum length for strings which can be provided for this option.
     * @param  maxLength
     *         The maximum length for strings which can be provided for this option.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@link OptionType type of this option} is not {@link OptionType#STRING STRING}</li>
     *             <li>If {@code minLength} is greater than {@code maxLength}</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData setRequiredLength(int minLength, int maxLength)
    {
        if (type != OptionType.STRING)
            throw new IllegalArgumentException("Can only set min and max length for options of type STRING");
        Checks.check(minLength <= maxLength, "Min length must not be greater than max length. Provided: %d > %d", minLength, maxLength);
        this.setMinLength(minLength);
        this.setMaxLength(maxLength);
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
     *         <ul>
     *             <li>If {@code name} is null, empty, or greater than {@value #MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>If {@code value} is less than {@link #MIN_NEGATIVE_NUMBER} or greater than {@link #MAX_POSITIVE_NUMBER}</li>
     *             <li>If adding this choice would exceed {@value #MAX_CHOICES} choices, as defined by {@link #MAX_CHOICES}</li>
     *             <li>If the {@link OptionType} is not {@link OptionType#NUMBER}</li>
     *             <li>If the option is auto-complete enabled</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoice(@Nonnull String name, double value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, MAX_CHOICE_NAME_LENGTH, "Name");
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Double value may not be less than %f", MIN_NEGATIVE_NUMBER);
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Double value may not be greater than %f", MAX_POSITIVE_NUMBER);
        if (isAutoComplete)
            throw new IllegalStateException("Cannot add choices to auto-complete options");
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Cannot add double choice for OptionType." + type);
        Checks.check(choices.size() < MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        choices.add(new Command.Choice(name, value));
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
     *         <ul>
     *             <li>If {@code name} is null, empty, or greater than {@value #MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>If {@code value} is less than {@link #MIN_NEGATIVE_NUMBER} or greater than {@link #MAX_POSITIVE_NUMBER}</li>
     *             <li>If adding this choice would exceed {@value #MAX_CHOICES} choices, as defined by {@link #MAX_CHOICES}</li>
     *             <li>If the {@link OptionType} is not {@link OptionType#INTEGER}</li>
     *             <li>If the option is auto-complete enabled</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoice(@Nonnull String name, long value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, MAX_CHOICE_NAME_LENGTH, "Name");
        Checks.check(value >= MIN_NEGATIVE_NUMBER, "Long value may not be less than %f", MIN_NEGATIVE_NUMBER);
        Checks.check(value <= MAX_POSITIVE_NUMBER, "Long value may not be greater than %f", MAX_POSITIVE_NUMBER);
        if (isAutoComplete)
            throw new IllegalStateException("Cannot add choices to auto-complete options");
        if (type != OptionType.INTEGER)
            throw new IllegalArgumentException("Cannot add long choice for OptionType." + type);
        Checks.check(choices.size() < MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        choices.add(new Command.Choice(name, value));
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
     *         <ul>
     *             <li>If {@code name} is null, empty, or greater than {@value #MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>If {@code value} is less than {@link #MIN_NEGATIVE_NUMBER} or greater than {@link #MAX_POSITIVE_NUMBER}</li>
     *             <li>If adding this choice would exceed {@value #MAX_CHOICES} choices, as defined by {@link #MAX_CHOICES}</li>
     *             <li>If the {@link OptionType} is not {@link OptionType#STRING}</li>
     *             <li>If the option is auto-complete enabled</li>
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
        if (isAutoComplete)
            throw new IllegalStateException("Cannot add choices to auto-complete options");
        if (type != OptionType.STRING)
            throw new IllegalArgumentException("Cannot add string choice for OptionType." + type);
        Checks.check(choices.size() < MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        choices.add(new Command.Choice(name, value));
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
     *         <ul>
     *             <li>If the {@link OptionType} does not {@link OptionType#canSupportChoices() support choices}</li>
     *             <li>If the provided {@code choices} are null</li>
     *             <li>If the amount of {@code choices} provided, when combined with the already set choices, would be greater than {@value #MAX_CHOICES}, as defined by {@link #MAX_CHOICES}</li>
     *             <li>If the {@link OptionType} of the choices is not either {@link OptionType#INTEGER}, {@link OptionType#STRING} or {@link OptionType#NUMBER}</li>
     *             <li>If the option is auto-complete enabled</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoices(@Nonnull Command.Choice... choices)
    {
        Checks.noneNull(choices, "Choices");
        return addChoices(Arrays.asList(choices));
    }

    /**
     * Adds up to 25 predefined choices for this option.
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  choices
     *         The choices to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the {@link OptionType} does not {@link OptionType#canSupportChoices() support choices}</li>
     *             <li>If the provided {@code choices} collection is null</li>
     *             <li>If the provided {@code choices} are null</li>
     *             <li>If the amount of {@code choices} provided, when combined with the already set choices, would be greater than {@value #MAX_CHOICES}, as defined by {@link #MAX_CHOICES}</li>
     *             <li>If the {@link OptionType} of the choices is not either {@link OptionType#INTEGER}, {@link OptionType#STRING} or {@link OptionType#NUMBER}</li>
     *             <li>If the option is auto-complete enabled</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    public OptionData addChoices(@Nonnull Collection<? extends Command.Choice> choices)
    {
        Checks.notNull(choices, "Choices");
        if (choices.size() == 0)
            return this;
        if (this.choices == null || !type.canSupportChoices())
            throw new IllegalStateException("Cannot add choices for an option of type " + type);
        Checks.noneNull(choices, "Choices");
        if (isAutoComplete)
            throw new IllegalStateException("Cannot add choices to auto-complete options");
        Checks.check(choices.size() + this.choices.size() <= MAX_CHOICES, "Cannot have more than 25 choices for one option!");
        this.choices.addAll(choices);
        return this;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject json = DataObject.empty()
                .put("type", type.getKey())
                .put("name", name)
                .put("name_localizations", nameLocalizations)
                .put("description", description)
                .put("description_localizations", descriptionLocalizations);
        if (type != OptionType.SUB_COMMAND && type != OptionType.SUB_COMMAND_GROUP)
        {
            json.put("required", isRequired);
            json.put("autocomplete", isAutoComplete);
        }
        if (choices != null && !choices.isEmpty())
        {
            json.put("choices", DataArray.fromCollection(
                    choices.stream()
                            .map(choice -> choice.toData(type))
                            .collect(Collectors.toList())
            ));
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
        if (type == OptionType.STRING)
        {
            if (minLength != null)
                json.put("min_length", minLength);
            if (maxLength != null)
                json.put("max_length", maxLength);
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
        option.setAutoComplete(json.getBoolean("autocomplete"));
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
        if (type == OptionType.STRING)
        {
            if (!json.isNull("min_length"))
                option.setMinLength(json.getInt("min_length"));
            if (!json.isNull("max_length"))
                option.setMaxLength(json.getInt("max_length"));
        }
        json.optArray("choices").ifPresent(choices1 ->
                option.addChoices(choices1.stream(DataArray::getObject)
                        .map(Command.Choice::new)
                        .collect(Collectors.toList())
                )
        );
        option.setNameLocalizations(LocalizationUtils.mapFromProperty(json, "name_localizations"));
        option.setDescriptionLocalizations(LocalizationUtils.mapFromProperty(json, "description_localizations"));
        return option;
    }

    /**
     * Converts the provided {@link Command.Option} into a OptionData instance.
     *
     * @param  option
     *         The option to convert
     *
     * @throws IllegalArgumentException
     *         If null is provided or the option has illegal configuration
     *
     * @return An instance of OptionData
     */
    @Nonnull
    public static OptionData fromOption(@Nonnull Command.Option option)
    {
        Checks.notNull(option, "Option");
        OptionData data = new OptionData(option.getType(), option.getName(), option.getDescription());
        data.setRequired(option.isRequired());
        data.setAutoComplete(option.isAutoComplete());
        data.addChoices(option.getChoices());
        data.setNameLocalizations(option.getNameLocalizations().toMap());
        data.setDescriptionLocalizations(option.getDescriptionLocalizations().toMap());
        Number min = option.getMinValue(), max = option.getMaxValue();
        Integer minLength = option.getMinLength(), maxLength = option.getMaxLength();
        switch (option.getType())
        {
        case CHANNEL:
            data.setChannelTypes(option.getChannelTypes());
            break;
        case NUMBER:
            if (min != null)
                data.setMinValue(min.doubleValue());
            if (max != null)
                data.setMaxValue(max.doubleValue());
            break;
        case INTEGER:
            if (min != null)
                data.setMinValue(min.longValue());
            if (max != null)
                data.setMaxValue(max.longValue());
            break;
        case STRING:
            if (minLength != null)
                data.setMinLength(minLength);
            if (maxLength != null)
                data.setMaxLength(maxLength);
            break;
        }
        return data;
    }
}
