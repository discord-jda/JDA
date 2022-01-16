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

package net.dv8tion.jda.internal.interactions.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandImpl implements Command
{
    public static final EnumSet<OptionType> OPTIONS = EnumSet.complementOf(EnumSet.of(OptionType.SUB_COMMAND, OptionType.SUB_COMMAND_GROUP));
    public static final Predicate<DataObject> OPTION_TEST = it -> OPTIONS.contains(OptionType.fromKey(it.getInt("type")));
    public static final Predicate<DataObject> SUBCOMMAND_TEST = it -> OptionType.fromKey(it.getInt("type")) == OptionType.SUB_COMMAND;
    public static final Predicate<DataObject> GROUP_TEST = it -> OptionType.fromKey(it.getInt("type")) == OptionType.SUB_COMMAND_GROUP;

    private final JDAImpl api;
    private final Guild guild;
    private final String name, description;
    private final List<Command.Option> options;
    private final List<Command.SubcommandGroup> groups;
    private final List<Command.Subcommand> subcommands;
    private final long id, guildId, applicationId, version;
    private final boolean defaultEnabled;
    private final Command.Type type;

    public CommandImpl(JDAImpl api, Guild guild, DataObject json)
    {
        this.api = api;
        this.guild = guild;
        this.name = json.getString("name");
        this.description = json.getString("description", "");
        this.type = Command.Type.fromId(json.getInt("type", 1));
        this.id = json.getUnsignedLong("id");
        this.defaultEnabled = json.getBoolean("default_permission");
        this.guildId = guild != null ? guild.getIdLong() : 0L;
        this.applicationId = json.getUnsignedLong("application_id", api.getSelfUser().getApplicationIdLong());
        this.options = parseOptions(json, OPTION_TEST, Command.Option::new);
        this.groups = parseOptions(json, GROUP_TEST, Command.SubcommandGroup::new);
        this.subcommands = parseOptions(json, SUBCOMMAND_TEST, Command.Subcommand::new);
        this.version = json.getUnsignedLong("version", id);
    }

    public static <T> List<T> parseOptions(DataObject json, Predicate<DataObject> test, Function<DataObject, T> transform)
    {
        return json.optArray("options").map(arr ->
                arr.stream(DataArray::getObject)
                        .filter(test)
                        .map(transform)
                        .collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        checkSelfUser("Cannot delete a command from another bot!");
        Route.CompiledRoute route;
        String appId = getJDA().getSelfUser().getApplicationId();
        if (guildId != 0L)
            route = Route.Interactions.DELETE_GUILD_COMMAND.compile(appId, Long.toUnsignedString(guildId), getId());
        else
            route = Route.Interactions.DELETE_COMMAND.compile(appId, getId());
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public CommandEditAction editCommand()
    {
        checkSelfUser("Cannot edit a command from another bot!");
        return guild == null ? new CommandEditActionImpl(api, getId()) : new CommandEditActionImpl(guild, getId());
    }

    @Nonnull
    @Override
    public RestAction<List<CommandPrivilege>> retrievePrivileges(@Nonnull Guild guild)
    {
        checkSelfUser("Cannot retrieve privileges for a command from another bot!");
        Checks.notNull(guild, "Guild");
        return guild.retrieveCommandPrivilegesById(id);
    }

    @Nonnull
    @Override
    public RestAction<List<CommandPrivilege>> updatePrivileges(@Nonnull Guild guild, @Nonnull Collection<? extends CommandPrivilege> privileges)
    {
        checkSelfUser("Cannot update privileges for a command from another bot!");
        Checks.notNull(guild, "Guild");
        return guild.updateCommandPrivilegesById(id, privileges);
    }

    @Nonnull
    @Override
    public RestAction<List<CommandPrivilege>> updatePrivileges(@Nonnull Guild guild, @Nonnull CommandPrivilege... privileges)
    {
        Checks.noneNull(privileges, "CommandPrivileges");
        return updatePrivileges(guild, Arrays.asList(privileges));
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public Command.Type getType()
    {
        return type;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean isDefaultEnabled()
    {
        return defaultEnabled;
    }

    @Nonnull
    @Override
    public List<Command.Option> getOptions()
    {
        return options;
    }

    @Nonnull
    @Override
    public List<Command.Subcommand> getSubcommands()
    {
        return subcommands;
    }

    @Nonnull
    @Override
    public List<Command.SubcommandGroup> getSubcommandGroups()
    {
        return groups;
    }

    @Override
    public long getApplicationIdLong()
    {
        return applicationId;
    }

    @Override
    public long getVersion()
    {
        return version;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "Command[" + getType() + "](" + getId() + ":" + getName() + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof Command))
            return false;
        return id == ((Command) obj).getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    private void checkSelfUser(String s)
    {
        if (applicationId != api.getSelfUser().getApplicationIdLong())
            throw new IllegalStateException(s);
    }
}
