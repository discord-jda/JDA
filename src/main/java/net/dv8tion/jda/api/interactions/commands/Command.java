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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.interactions.command.CommandImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a Discord slash-command.
 * <br>This can be used to edit or delete the command.
 *
 * @see Guild#retrieveCommandById(String)
 * @see Guild#retrieveCommands()
 */
public interface Command extends ISnowflake, ICommandReference
{
    /**
     * Delete this command.
     *
     * @throws IllegalStateException
     *         If this command is not owned by this bot
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> delete();

    /**
     * Edit this command.
     * <br>This can be used to change the command attributes such as name or description.
     *
     * @throws IllegalStateException
     *         If this command is not owned by this bot
     *
     * @return {@link CommandEditAction}
     */
    @Nonnull
    @CheckReturnValue
    CommandEditAction editCommand();

    /**
     * Retrieves the {@link IntegrationPrivilege IntegrationPrivileges} for this command.
     * <br>This is a shortcut for {@link Guild#retrieveIntegrationPrivilegesById(String)}.
     *
     * <p>Moderators of a guild can modify these privileges through the Integrations Menu
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  guild
     *         The target guild from which to retrieve the privileges
     *
     * @throws IllegalArgumentException
     *         If the guild is null
     *
     * @return {@link RestAction} - Type: {@link List} of {@link IntegrationPrivilege}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<IntegrationPrivilege>> retrievePrivileges(@Nonnull Guild guild);

    /**
     * Returns the {@link JDA JDA} instance of this Command
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * The {@link Type} of command
     *
     * @return The command type
     */
    @Nonnull
    Type getType();

    /**
     * The name of this command.
     *
     * @return The name
     */
    @Nonnull
    String getName();

    /**
     * The localizations of this command's name for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
     */
    @Nonnull
    LocalizationMap getNameLocalizations();

    /**
     * The description of this command.
     *
     * @return The description, empty for context menu commands
     */
    @Nonnull
    String getDescription();

    /**
     * The localizations of this command's description for {@link DiscordLocale various languages}.
     *
     * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
     */
    @Nonnull
    LocalizationMap getDescriptionLocalizations();

    /**
     * The {@link Option Options} of this command.
     *
     * @return Immutable list of command options
     */
    @Nonnull
    @Unmodifiable
    List<Option> getOptions();

    /**
     * The {@link Subcommand Subcommands} of this command.
     *
     * @return Immutable list of subcommands
     */
    @Nonnull
    @Unmodifiable
    List<Subcommand> getSubcommands();

    /**
     * The {@link SubcommandGroup SubcommandGroups} of this command.
     *
     * @return Immutable list of subcommand groups
     */
    @Nonnull
    @Unmodifiable
    List<SubcommandGroup> getSubcommandGroups();

    /**
     * The id of the application this command belongs to.
     *
     * @return The application id
     */
    long getApplicationIdLong();

    /**
     * The id of the application this command belongs to.
     *
     * @return The application id
     */
    @Nonnull
    default String getApplicationId()
    {
        return Long.toUnsignedString(getApplicationIdLong());
    }

    /**
     * The version of this command.
     * <br>This changes when a command is updated through {@link JDA#upsertCommand(CommandData) upsertCommand}, {@link JDA#updateCommands() updateCommands}, or {@link JDA#editCommandById(String) editCommandById}
     * <br>Useful for checking if command cache is outdated
     *
     * @return The version of the command as a snowflake id.
     *
     * @see #getTimeModified()
     */
    long getVersion();

    /**
     * The time this command was updated last.
     *
     * @return Time this command was updated last.
     *
     * @see #getVersion()
     */
    @Nonnull
    default OffsetDateTime getTimeModified()
    {
        return TimeUtil.getTimeCreated(getVersion());
    }

    /**
     * The {@link DefaultMemberPermissions} of this command.
     * <br>If this command has no default permission set, this returns {@link DefaultMemberPermissions#ENABLED}.
     *
     * @return The DefaultMemberPermissions of this command.
     */
    @Nonnull
    DefaultMemberPermissions getDefaultPermissions();

    /**
     * Whether the command can only be used inside a guild.
     * <br>Always true for guild commands.
     *
     * @return True, if this command is restricted to guilds.
     *
     * @deprecated Replaced with {@link #getContexts()}
     */
    @Deprecated
    boolean isGuildOnly();

    /**
     * Gets the contexts in which this command can be executed.
     *
     * @return The contexts in which this command can be executed
     */
    @Nonnull
    Set<InteractionContextType> getContexts();

    /**
     * Gets the integration types on which this command can be installed on.
     *
     * @return The integration types on which this command can be installed on
     */
    @Nonnull
    Set<IntegrationType> getIntegrationTypes();

    /**
     * Whether this command is restricted to NSFW (age-restricted) channels.
     *
     * @return True, if this command is NSFW
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    boolean isNSFW();

    /**
     * Possible command types
     */
    enum Type
    {
        UNKNOWN(-1),
        SLASH(1),
        USER(2),
        MESSAGE(3);

        private final int id;

        Type(int id)
        {
            this.id = id;
        }

        /**
         * Resolves the provided command type id to the enum constant
         *
         * @param  id
         *         The command type id
         *
         * @return The type or {@link #UNKNOWN}
         */
        @Nonnull
        public static Type fromId(int id)
        {
            for (Type type : values())
            {
                if (type.id == id)
                    return type;
            }
            return UNKNOWN;
        }

        /**
         * The raw command type id used in the API
         *
         * @return The command type id
         */
        public int getId()
        {
            return id;
        }
    }

    /**
     * Predefined choice used for options.
     *
     * @see net.dv8tion.jda.api.interactions.commands.build.OptionData#addChoices(Command.Choice...)
     * @see net.dv8tion.jda.api.interactions.commands.build.OptionData#addChoices(Collection)
     */
    class Choice
    {
        /**
         * The maximum length the name of a choice can be.
         */
        public static final int MAX_NAME_LENGTH = 100;

        /**
         * The maximum length the {@link OptionType#STRING STRING} value of a choice can be.
         */
        public static final int MAX_STRING_VALUE_LENGTH = 100;

        private String name;
        private final LocalizationMap nameLocalizations = new LocalizationMap(this::checkName);
        private long intValue = 0;
        private double doubleValue = Double.NaN;
        private String stringValue = null;
        private OptionType type;

        /**
         * Create a Choice tuple
         *
         * @param name
         *        The display name of this choice, must be less than 100 characters
         * @param value
         *        The integer value you receive in a command option
         *
         * @throws IllegalArgumentException
         *         If the name is null, empty, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
         *         as defined by {@link #MAX_NAME_LENGTH}
         */
        public Choice(@Nonnull String name, long value)
        {
            setName(name);
            setIntValue(value);
        }

        /**
         * Create a Choice tuple
         *
         * @param name
         *        The display name of this choice, must be less than 100 characters
         * @param value
         *        The double value you receive in a command option
         *
         * @throws IllegalArgumentException
         *         If the name is null, empty, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
         *         as defined by {@link #MAX_NAME_LENGTH}
         */
        public Choice(@Nonnull String name, double value)
        {
            setName(name);
            setDoubleValue(value);
        }

        /**
         * Create a Choice tuple
         *
         * @param name
         *        The display name of this choice, must be less than 100 characters
         * @param value
         *        The string value you receive in a command option
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the name is null, empty, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
         *                 as defined by {@link #MAX_NAME_LENGTH}</li>
         *             <li>If the value is null or longer than {@value #MAX_STRING_VALUE_LENGTH} characters long,
         *                 as defined by {@link #MAX_STRING_VALUE_LENGTH}</li>
         *         </ul>
         *
         */
        public Choice(@Nonnull String name, @Nonnull String value)
        {
            setName(name);
            setStringValue(value);
        }

        /**
         * Create a Choice tuple
         *
         * @param json
         *        The serialized choice instance with name and value mapping
         *
         * @throws IllegalArgumentException
         *         If null is provided
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         *         If the data is not formatted correctly or missing required parameters
         */
        public Choice(@Nonnull DataObject json)
        {
            Checks.notNull(json, "DataObject");
            this.name = json.getString("name");
            if (json.isType("value", DataType.INT))
            {
                setIntValue(json.getLong("value"));
            }
            else if (json.isType("value", DataType.FLOAT))
            {
                setDoubleValue(json.getDouble("value"));
            }
            else
            {
                setStringValue(json.getString("value"));
            }
            setNameLocalizations(LocalizationUtils.mapFromProperty(json, "name_localizations"));
        }

        /**
         * The readable name of this choice.
         * <br>This is shown to the user in the official client.
         *
         * @return The choice name
         */
        @Nonnull
        public String getName()
        {
            return name;
        }

        /**
         * Configure the choice name
         *
         * @param  name
         *         The choice name, {@link #MAX_NAME_LENGTH 1-100 characters long}
         *
         * @throws IllegalArgumentException
         *         If the name is null, empty, or not between 1-{@value #MAX_NAME_LENGTH} characters long,
         *         as defined by {@link #MAX_NAME_LENGTH}
         *
         * @return The Choice instance, for chaining
         */
        public Choice setName(@Nonnull String name)
        {
            checkName(name);
            this.name = name;
            return this;
        }

        /**
         * The localizations of this choice's name for {@link DiscordLocale various languages}.
         *
         * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
         */
        @Nonnull
        public LocalizationMap getNameLocalizations()
        {
            return nameLocalizations;
        }

        /**
         * Sets the name localizations of this choice.
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
        public Choice setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
        {
            nameLocalizations.setTranslation(locale, name);
            return this;
        }

        /**
         * Sets the name localizations of this choice.
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
        public Choice setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
        {
            //Checks are done in LocalizationMap
            nameLocalizations.setTranslations(map);
            return this;
        }

        /**
         * The value of this choice.
         *
         * @return The double value, or NaN if this is not a numeric choice value
         */
        public double getAsDouble()
        {
            return doubleValue;
        }

        /**
         * The value of this choice.
         *
         * @return The long value
         */
        public long getAsLong()
        {
            return intValue;
        }

        /**
         * The value of this choice.
         *
         * @return The String value
         */
        @Nonnull
        public String getAsString()
        {
            return stringValue;
        }

        /**
         * The {@link OptionType} this choice is for
         *
         * @return The option type of this choice
         */
        @Nonnull
        public OptionType getType()
        {
            return type;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, stringValue);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (!(obj instanceof Choice)) return false;
            Choice other = (Choice) obj;
            return Objects.equals(other.name, name) && Objects.equals(other.stringValue, stringValue);
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setName(name)
                    .addMetadata("value", stringValue)
                    .toString();
        }

        private void setIntValue(long value)
        {
            this.doubleValue = value;
            this.intValue = value;
            this.stringValue = Long.toString(value);
            this.type = OptionType.INTEGER;
        }

        private void setDoubleValue(double value)
        {
            this.doubleValue = value;
            this.intValue = (long) value;
            this.stringValue = Double.toString(value);
            this.type = OptionType.NUMBER;
        }

        private void setStringValue(@Nonnull String value)
        {
            Checks.notLonger(value, MAX_STRING_VALUE_LENGTH, "Choice string value");
            this.doubleValue = Double.NaN;
            this.intValue = 0;
            this.stringValue = value;
            this.type = OptionType.STRING;
        }

        private void checkName(@Nonnull String name)
        {
            Checks.notEmpty(name, "Choice name");
            Checks.notLonger(name, MAX_NAME_LENGTH, "Choice name");
        }

        @Nonnull
        public DataObject toData(OptionType optionType)
        {
            final Object value;
            if (optionType == OptionType.INTEGER)
                value = getAsLong();
            else if (optionType == OptionType.STRING)
                value = getAsString();
            else if (optionType == OptionType.NUMBER)
                value = getAsDouble();
            else
                throw new IllegalArgumentException("Cannot transform choice into data for type " + optionType);

            return DataObject.empty()
                    .put("name", name)
                    .put("value", value)
                    .put("name_localizations", nameLocalizations);
        }
    }

    /**
     * An Option for a command.
     */
    class Option
    {
        private final String name, description;
        private final LocalizationMap nameLocalizations;
        private final LocalizationMap descriptionLocalizations;
        private final int type;
        private final boolean required, autoComplete;
        private final Set<ChannelType> channelTypes;
        private final List<Choice> choices;
        private Number minValue;
        private Number maxValue;
        private Integer minLength, maxLength;

        public Option(@Nonnull DataObject json)
        {
            this.name = json.getString("name");
            this.nameLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "name_localizations");
            this.description = json.getString("description");
            this.descriptionLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "description_localizations");
            this.type = json.getInt("type");
            this.required = json.getBoolean("required");
            this.autoComplete = json.getBoolean("autocomplete");
            this.channelTypes = Collections.unmodifiableSet(json.optArray("channel_types")
                    .map(it -> it.stream(DataArray::getInt).map(ChannelType::fromId).collect(Collectors.toSet()))
                    .orElse(Collections.emptySet()));
            this.choices = json.optArray("choices")
                .map(it -> it.stream(DataArray::getObject).map(Choice::new).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
            if (!json.isNull("min_value"))
                this.minValue = json.getDouble("min_value");
            if (!json.isNull("max_value"))
                this.maxValue = json.getDouble("max_value");
            if (!json.isNull("min_length"))
                this.minLength = json.getInt("min_length");
            if (!json.isNull("max_length"))
                this.maxLength = json.getInt("max_length");
        }

        /**
         * The name of this option, subcommand, or subcommand group.
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
         * The description of this option, subcommand, or subcommand group.
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
         * The raw option type.
         *
         * @return The type
         */
        public int getTypeRaw()
        {
            return type;
        }

        /**
         * Whether this option is required
         *
         * @return True if this option is required
         */
        public boolean isRequired()
        {
            return required;
        }

        /**
         * Whether this option supports auto-complete
         *
         * @return True if this option supports auto-complete
         */
        public boolean isAutoComplete()
        {
            return autoComplete;
        }

        /**
         * The {@link OptionType}.
         *
         * @return The type
         */
        @Nonnull
        public OptionType getType()
        {
            return OptionType.fromKey(type);
        }

        /**
         * The {@link ChannelType ChannelTypes} this option is restricted to.
         * <br>This is empty if the option is not of type {@link OptionType#CHANNEL CHANNEL} or not restricted to specific types.
         *
         * @return Immutable {@link Set} of {@link ChannelType}
         */
        @Nonnull
        @Unmodifiable
        public Set<ChannelType> getChannelTypes()
        {
            return channelTypes;
        }

        /**
         * The minimum value which can be provided for this option.
         * <br>This returns {@code null} if the value is not set or if the option
         * is not of type {@link OptionType#INTEGER INTEGER} or {@link OptionType#NUMBER NUMBER}.
         *
         * @return The minimum value for this option or {@code null}
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
         * @return The maximum value for this option or {@code null}
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
         * The predefined choices available for this option.
         * <br>If no choices are defined, this returns an empty list.
         *
         * @return Immutable {@link List} of {@link Choice}
         */
        @Nonnull
        @Unmodifiable
        public List<Choice> getChoices()
        {
            return choices;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, description, type, choices, channelTypes, minValue, maxValue, minLength, maxLength, required, autoComplete);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (!(obj instanceof Option)) return false;
            Option other = (Option) obj;
            return Objects.equals(other.name, name)
                && Objects.equals(other.description, description)
                && Objects.equals(other.choices, choices)
                && Objects.equals(other.channelTypes, channelTypes)
                && Objects.equals(other.minValue, minValue)
                && Objects.equals(other.maxValue, maxValue)
                && Objects.equals(other.minLength, minLength)
                && Objects.equals(other.maxLength, maxLength)
                && other.required == required
                && other.autoComplete == autoComplete
                && other.type == type;
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setType(getType())
                    .addMetadata("name", name)
                    .toString();
        }
    }

    /**
     * An Subcommand for a command.
     */
    class Subcommand implements ICommandReference
    {
        private final ICommandReference parentCommand; //Could be Command or SubcommandGroup
        private final String name, description;
        private final LocalizationMap nameLocalizations;
        private final LocalizationMap descriptionLocalizations;
        private final List<Option> options;

        public Subcommand(ICommandReference parentCommand, DataObject json)
        {
            this.parentCommand = parentCommand;
            this.name = json.getString("name");
            this.nameLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "name_localizations");
            this.description = json.getString("description");
            this.descriptionLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "description_localizations");
            this.options = CommandImpl.parseOptions(json, CommandImpl.OPTION_TEST, Option::new);
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>This will return the ID of the top level command</b>
         */
        @Override
        public long getIdLong()
        {
            return parentCommand.getIdLong();
        }

        /**
         * The name of this subcommand.
         *
         * @return The name
         */
        @Nonnull
        public String getName()
        {
            return name;
        }

        /**
         * The localizations of this subcommands's name for {@link DiscordLocale various languages}.
         *
         * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
         */
        @Nonnull
        public LocalizationMap getNameLocalizations()
        {
            return nameLocalizations;
        }

        @Nonnull
        @Override
        public String getFullCommandName()
        {
            return parentCommand.getFullCommandName() + " " + getName();
        }

        /**
         * The description of this subcommand.
         *
         * @return The description
         */
        @Nonnull
        public String getDescription()
        {
            return description;
        }

        /**
         * The localizations of this subcommand's description for {@link DiscordLocale various languages}.
         *
         * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
         */
        @Nonnull
        public LocalizationMap getDescriptionLocalizations()
        {
            return descriptionLocalizations;
        }

        /**
         * The options for this subcommand, or the subcommands within this group.
         *
         * @return Immutable list of Options
         */
        @Nonnull
        @Unmodifiable
        public List<Option> getOptions()
        {
            return options;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, description, options);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (!(obj instanceof Subcommand)) return false;
            Subcommand other = (Subcommand) obj;
            return Objects.equals(other.name, name)
                && Objects.equals(other.description, description)
                && Objects.equals(other.options, options);
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .addMetadata("name", name)
                    .toString();
        }
    }

    /**
     * An Subcommand Group for a command.
     */
    class SubcommandGroup implements ICommandReference
    {
        private final Command parentCommand;
        private final String name, description;
        private final LocalizationMap nameLocalizations;
        private final LocalizationMap descriptionLocalizations;
        private final List<Subcommand> subcommands;

        public SubcommandGroup(Command parentCommand, DataObject json)
        {
            this.parentCommand = parentCommand;
            this.name = json.getString("name");
            this.nameLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "name_localizations");
            this.description = json.getString("description");
            this.descriptionLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "description_localizations");
            this.subcommands = CommandImpl.parseOptions(json, CommandImpl.SUBCOMMAND_TEST, (DataObject o) -> new Subcommand(this, o));
        }

        /**
         * {@inheritDoc}
         *
         * <p><b>This will return the ID of the top level command</b>
         */
        @Override
        public long getIdLong()
        {
            return parentCommand.getIdLong();
        }

        /**
         * The name of this subcommand group.
         *
         * @return The name
         */
        @Nonnull
        public String getName()
        {
            return name;
        }

        /**
         * The localizations of this subcommand group's name for {@link DiscordLocale various languages}.
         *
         * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized name
         */
        @Nonnull
        public LocalizationMap getNameLocalizations()
        {
            return nameLocalizations;
        }

        @Nonnull
        @Override
        public String getFullCommandName()
        {
            return parentCommand.getFullCommandName() + " " + getName();
        }

        /**
         * The description of this subcommand group.
         *
         * @return The description
         */
        @Nonnull
        public String getDescription()
        {
            return description;
        }

        /**
         * The localizations of this subcommand group's description for {@link DiscordLocale various languages}.
         *
         * @return The {@link LocalizationMap} containing the mapping from {@link DiscordLocale} to the localized description
         */
        @Nonnull
        public LocalizationMap getDescriptionLocalizations()
        {
            return descriptionLocalizations;
        }

        /**
         * The {@link Subcommand Subcommands} in this group
         *
         * @return Immutable {@link List} of {@link Subcommand}
         */
        @Nonnull
        @Unmodifiable
        public List<Subcommand> getSubcommands()
        {
            return subcommands;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, description, subcommands);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (!(obj instanceof SubcommandGroup)) return false;
            SubcommandGroup other = (SubcommandGroup) obj;
            return Objects.equals(other.name, name)
                    && Objects.equals(other.description, description)
                    && Objects.equals(other.subcommands, subcommands);
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .addMetadata("name", name)
                    .toString();
        }
    }
}
