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
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a Discord slash-command.
 * <br>This can be used to edit or delete the command.
 *
 * @see Guild#retrieveCommandById(String)
 * @see Guild#retrieveCommands()
 */
public class Command implements ISnowflake
{
    private static final EnumSet<OptionType> OPTIONS = EnumSet.complementOf(EnumSet.of(OptionType.SUB_COMMAND, OptionType.SUB_COMMAND_GROUP));
    private static final Predicate<DataObject> OPTION_TEST = it -> OPTIONS.contains(OptionType.fromKey(it.getInt("type")));
    private static final Predicate<DataObject> SUBCOMMAND_TEST = it -> OptionType.fromKey(it.getInt("type")) == OptionType.SUB_COMMAND;
    private static final Predicate<DataObject> GROUP_TEST = it -> OptionType.fromKey(it.getInt("type")) == OptionType.SUB_COMMAND_GROUP;

    private final JDAImpl api;
    private final Guild guild;
    private final String name, description;
    private final List<Option> options;
    private final List<SubcommandGroup> groups;
    private final List<Subcommand> subcommands;
    private final long id, guildId, applicationId;
    private final boolean defaultEnabled;

    public Command(JDAImpl api, Guild guild, DataObject json)
    {
        this.api = api;
        this.guild = guild;
        this.name = json.getString("name");
        this.description = json.getString("description");
        this.id = json.getUnsignedLong("id");
        this.defaultEnabled = json.getBoolean("default_permission");
        this.guildId = guild != null ? guild.getIdLong() : 0L;
        this.applicationId = json.getUnsignedLong("application_id", api.getSelfUser().getApplicationIdLong());
        this.options = parseOptions(json, OPTION_TEST, Option::new);
        this.groups = parseOptions(json, GROUP_TEST, SubcommandGroup::new);
        this.subcommands = parseOptions(json, SUBCOMMAND_TEST, Subcommand::new);
    }

    protected static <T> List<T> parseOptions(DataObject json, Predicate<DataObject> test, Function<DataObject, T> transform)
    {
        return json.optArray("options").map(arr ->
            arr.stream(DataArray::getObject)
               .filter(test)
               .map(transform)
               .collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

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
    public RestAction<Void> delete()
    {
        if (applicationId != api.getSelfUser().getApplicationIdLong())
            throw new IllegalStateException("Cannot delete a command from another bot!");
        Route.CompiledRoute route;
        String appId = getJDA().getSelfUser().getApplicationId();
        if (guildId != 0L)
            route = Route.Interactions.DELETE_GUILD_COMMAND.compile(appId, Long.toUnsignedString(guildId), getId());
        else
            route = Route.Interactions.DELETE_COMMAND.compile(appId, getId());
        return new RestActionImpl<>(api, route);
    }

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
    public CommandEditAction editCommand()
    {
        if (applicationId != api.getSelfUser().getApplicationIdLong())
            throw new IllegalStateException("Cannot edit a command from another bot!");
        return guild == null ? new CommandEditActionImpl(api, getId()) : new CommandEditActionImpl(guild, getId());
    }

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
    public RestAction<List<CommandPrivilege>> retrievePrivileges(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        return guild.retrieveCommandPrivilegesById(id);
    }

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for this command.
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
    public RestAction<List<CommandPrivilege>> updatePrivileges(@Nonnull Guild guild, @Nonnull Collection<? extends CommandPrivilege> privileges)
    {
        if (applicationId != api.getSelfUser().getApplicationIdLong())
            throw new IllegalStateException("Cannot update privileges for a command from another bot!");
        Checks.notNull(guild, "Guild");
        return guild.updateCommandPrivilegesById(id, privileges);
    }

    /**
     * Updates the list of {@link CommandPrivilege CommandPrivileges} for this command.
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
    public RestAction<List<CommandPrivilege>> updatePrivileges(@Nonnull Guild guild, @Nonnull CommandPrivilege... privileges)
    {
        Checks.noneNull(privileges, "CommandPrivileges");
        return updatePrivileges(guild, Arrays.asList(privileges));
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this Command
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    public JDA getJDA()
    {
        return api;
    }

    /**
     * The name of this command.
     *
     * @return The name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The description of this command.
     *
     * @return The description
     */
    @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * Whether this command is enabled for everyone by default.
     *
     * @return True, if everyone can use this command by default.
     */
    public boolean isDefaultEnabled()
    {
        return defaultEnabled;
    }

    /**
     * The {@link Option Options} of this command.
     *
     * @return Immutable list of command options
     */
    @Nonnull
    public List<Option> getOptions()
    {
        return options;
    }

    /**
     * The {@link Subcommand Subcommands} of this command.
     *
     * @return Immutable list of subcommands
     */
    @Nonnull
    public List<Subcommand> getSubcommands()
    {
        return subcommands;
    }

    /**
     * The {@link SubcommandGroup SubcommandGroups} of this command.
     *
     * @return Immutable list of subcommand groups
     */
    @Nonnull
    public List<SubcommandGroup> getSubcommandGroups()
    {
        return groups;
    }

    /**
     * The id of the application this command belongs to.
     *
     * @return The application id
     */
    public long getApplicationIdLong()
    {
        return applicationId;
    }

    /**
     * The id of the application this command belongs to.
     *
     * @return The application id
     */
    @Nonnull
    public String getApplicationId()
    {
        return Long.toUnsignedString(applicationId);
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "C:" + getName() + "(" + getId() + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof Command))
            return false;
        return id == ((Command) obj).id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    /**
     * Predefined choice used for options.
     * 
     * @see net.dv8tion.jda.api.interactions.commands.build.OptionData#addChoices(Command.Choice...)
     * @see net.dv8tion.jda.api.interactions.commands.build.OptionData#addChoices(Collection)
     */
    public static class Choice
    {
        private final String name;
        private final long intValue;
        private final String stringValue;

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
            this.intValue = value;
            this.stringValue = Long.toString(value);
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
            this.intValue = 0;
            this.stringValue = value;
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
                this.intValue = json.getLong("value");
                this.stringValue = Long.toString(intValue); // does this make sense?
            }
            else
            {
                this.intValue = 0;
                this.stringValue = json.getString("value");
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
    }

    /**
     * An Option for a command.
     */
    public static class Option
    {
        private final String name, description;
        private final int type;
        private final boolean required;
        private final List<Choice> choices;

        public Option(@Nonnull DataObject json)
        {
            this.name = json.getString("name");
            this.description = json.getString("description");
            this.type = json.getInt("type");
            this.required = json.getBoolean("required");
            this.choices = json.optArray("choices")
                .map(it -> it.stream(DataArray::getObject).map(Choice::new).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
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
            return Objects.hash(name, description, type, choices);
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
    public static class Subcommand
    {
        private final String name, description;
        private final List<Option> options;

        public Subcommand(DataObject json)
        {
            this.name = json.getString("name");
            this.description = json.getString("description");
            this.options = parseOptions(json, OPTION_TEST, Option::new);
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
         * The options for this subcommand, or the subcommands whtin this group.
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
    public static class SubcommandGroup
    {
        private final String name, description;
        private final List<Subcommand> subcommands;

        public SubcommandGroup(DataObject json)
        {
            this.name = json.getString("name");
            this.description = json.getString("description");
            this.subcommands = parseOptions(json, SUBCOMMAND_TEST, Subcommand::new);
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
