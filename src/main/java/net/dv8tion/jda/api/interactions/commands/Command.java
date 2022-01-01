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
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.interactions.command.CommandImpl;
import net.dv8tion.jda.internal.utils.Checks;

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
public interface Command extends ISnowflake
{
    /**
     * Delete this command.
     * <br>If this is a global command it may take up to 1 hour to vanish from all clients.
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
     * Retrieves the {@link CommandPrivilege CommandPrivileges} for this command.
     * <br>This is a shortcut for {@link Guild#retrieveCommandPrivilegesById(String)}.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
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
     * @return {@link RestAction} - Type: {@link List} of {@link CommandPrivilege}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<CommandPrivilege>> retrievePrivileges(@Nonnull Guild guild);

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for this command.
     * <br>Note that commands are enabled by default for all members of a guild, which means you can only <em>blacklist</em> roles and members using this method.
     * To change this behavior, use {@link CommandData#setDefaultEnabled(boolean)} on your command.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  guild
     *         The target guild from which to update the privileges
     * @param  privileges
     *         Complete list of {@link CommandPrivilege CommandPrivileges} for this command
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws IllegalStateException
     *         If this command is not owned by this bot
     *
     * @return {@link RestAction} - Type: {@link List} or {@link CommandPrivilege}
     *         The updated list of privileges for this command.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<CommandPrivilege>> updatePrivileges(@Nonnull Guild guild, @Nonnull Collection<? extends CommandPrivilege> privileges);

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for this command.
     * <br>Note that commands are enabled by default for all members of a guild, which means you can only <em>blacklist</em> roles and members using this method.
     * To change this behavior, use {@link CommandData#setDefaultEnabled(boolean)} on your command.
     *
     * <p>These privileges are used to restrict who can use commands through Role/User whitelists/blacklists.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param  guild
     *         The target guild from which to update the privileges
     * @param  privileges
     *         Complete list of {@link CommandPrivilege CommandPrivileges} for this command
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws IllegalStateException
     *         If this command is not owned by this bot
     *
     * @return {@link RestAction} - Type: {@link List} or {@link CommandPrivilege}
     *         The updated list of privileges for this command.
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<CommandPrivilege>> updatePrivileges(@Nonnull Guild guild, @Nonnull CommandPrivilege... privileges);

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
     * The description of this command.
     *
     * @return The description, empty for context menu commands
     */
    @Nonnull
    String getDescription();

    /**
     * Whether this command is enabled for everyone by default.
     *
     * @return True, if everyone can use this command by default.
     */
    boolean isDefaultEnabled();

    /**
     * The {@link Option Options} of this command.
     *
     * @return Immutable list of command options
     */
    @Nonnull
    List<Option> getOptions();

    /**
     * The {@link Subcommand Subcommands} of this command.
     *
     * @return Immutable list of subcommands
     */
    @Nonnull
    List<Subcommand> getSubcommands();

    /**
     * The {@link SubcommandGroup SubcommandGroups} of this command.
     *
     * @return Immutable list of subcommand groups
     */
    @Nonnull
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
        private final String name;
        private long intValue = 0;
        private double doubleValue = Double.NaN;
        private String stringValue = null;
        private OptionType type;

        /**
         * Create a Choice tuple
         *
         * @param name
         *        The display name of this choice
         * @param value
         *        The integer value you receive in a command option
         */
        public Choice(@Nonnull String name, long value)
        {
            this.name = name;
            setIntValue(value);
        }

        /**
         * Create a Choice tuple
         *
         * @param name
         *        The display name of this choice
         * @param value
         *        The double value you receive in a command option
         */
        public Choice(@Nonnull String name, double value)
        {
            this.name = name;
            setDoubleValue(value);
        }

        /**
         * Create a Choice tuple
         *
         * @param name
         *        The display name of this choice
         * @param value
         *        The string value you receive in a command option
         */
        public Choice(@Nonnull String name, @Nonnull String value)
        {
            this.name = name;
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
            return "Choice(" + name + "," + stringValue + ")";
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
            this.doubleValue = Double.NaN;
            this.intValue = 0;
            this.stringValue = value;
            this.type = OptionType.STRING;
        }
    }

    /**
     * An Option for a command.
     */
    class Option
    {
        private final String name, description;
        private final int type;
        private final boolean required, autoComplete;
        private final Set<ChannelType> channelTypes;
        private final List<Choice> choices;
        private Number minValue;
        private Number maxValue;

        public Option(@Nonnull DataObject json)
        {
            this.name = json.getString("name");
            this.description = json.getString("description");
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
         * The predefined choices available for this option.
         * <br>If no choices are defined, this returns an empty list.
         *
         * @return Immutable {@link List} of {@link Choice}
         */
        @Nonnull
        public List<Choice> getChoices()
        {
            return choices;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name, description, type, choices, channelTypes, minValue, maxValue);
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
                && other.type == type;
        }

        @Override
        public String toString()
        {
            return "Option[" + getType() + "](" + name + ")";
        }
    }

    /**
     * An Subcommand for a command.
     */
    class Subcommand
    {
        private final String name, description;
        private final List<Option> options;

        public Subcommand(DataObject json)
        {
            this.name = json.getString("name");
            this.description = json.getString("description");
            this.options = CommandImpl.parseOptions(json, CommandImpl.OPTION_TEST, Option::new);
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
         * The options for this subcommand, or the subcommands within this group.
         *
         * @return Immutable list of Options
         */
        @Nonnull
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
            return "Subcommand(" + name + ")";
        }
    }

    /**
     * An Subcommand Group for a command.
     */
    class SubcommandGroup
    {
        private final String name, description;
        private final List<Subcommand> subcommands;

        public SubcommandGroup(DataObject json)
        {
            this.name = json.getString("name");
            this.description = json.getString("description");
            this.subcommands = CommandImpl.parseOptions(json, CommandImpl.SUBCOMMAND_TEST, Subcommand::new);
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
         * The {@link Subcommand Subcommands} in this group
         *
         * @return Immutable {@link List} of {@link Subcommand}
         */
        @Nonnull
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
            return "SubcommandGroup(" + name + ")";
        }
    }
}
