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
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

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

    protected final JDAImpl api;
    protected final Guild guild;
    protected final String name;
    protected final long id, guildId, applicationId;
    protected final boolean defaultEnabled;

    public Command(JDAImpl api, Guild guild, DataObject json)
    {
        this.api = api;
        this.guild = guild;
        this.name = json.getString("name");
        this.id = json.getUnsignedLong("id");
        this.defaultEnabled = json.getBoolean("default_permission");
        this.guildId = guild != null ? guild.getIdLong() : 0L;
        this.applicationId = json.getUnsignedLong("application_id", api.getSelfUser().getApplicationIdLong());
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
        return guild == null ?
                new CommandEditActionImpl(api, getId(), CommandType.SLASH_COMMAND) :
                new CommandEditActionImpl(guild, getId(), CommandType.SLASH_COMMAND);
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
     * Whether this command is enabled for everyone by default.
     *
     * @return True, if everyone can use this command by default.
     */
    public boolean isDefaultEnabled()
    {
        return defaultEnabled;
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
}
