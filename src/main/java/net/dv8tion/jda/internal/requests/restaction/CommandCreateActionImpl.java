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
package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.CommandDataBase;
import net.dv8tion.jda.api.interactions.commands.build.slash.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.slash.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.slash.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.slash.SubcommandGroupData;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class CommandCreateActionImpl extends RestActionImpl<Command> implements CommandCreateAction
{
    private final Guild guild;
    private final CommandDataBase<?> data;

    public CommandCreateActionImpl(JDAImpl api, CommandDataBase<?> command)
    {
        super(api, Route.Interactions.CREATE_COMMAND.compile(api.getSelfUser().getApplicationId()));
        this.guild = null;
        this.data = command;
    }

    public CommandCreateActionImpl(Guild guild, CommandDataBase<?> command)
    {
        super(guild.getJDA(), Route.Interactions.CREATE_GUILD_COMMAND.compile(guild.getJDA().getSelfUser().getApplicationId(), guild.getId()));
        this.guild = guild;
        this.data = command;
    }

    @Nonnull
    @Override
    public CommandCreateAction addCheck(@Nonnull BooleanSupplier checks)
    {
        return (CommandCreateAction) super.addCheck(checks);
    }

    @Nonnull
    @Override
    public CommandCreateAction setCheck(BooleanSupplier checks)
    {
        return (CommandCreateAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public CommandCreateAction deadline(long timestamp)
    {
        return (CommandCreateAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public CommandCreateAction setDefaultEnabled(boolean enabled)
    {
        data.setDefaultEnabled(enabled);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CommandCreateAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public CommandCreateAction setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        data.setName(name);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
        if(data instanceof SlashCommandData)
            ((SlashCommandData) data).setDescription(description);
        else
            throw new IllegalArgumentException("Command is not a slash-command");
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction addOptions(@Nonnull OptionData... options)
    {
        if(data instanceof SlashCommandData)
            ((SlashCommandData) data).addOptions(options);
        else
            throw new IllegalArgumentException("Command is not a slash-command");
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction addSubcommands(@Nonnull SubcommandData subcommand)
    {
        if(data instanceof SlashCommandData)
            ((SlashCommandData) data).addSubcommands(subcommand);
        else
            throw new IllegalArgumentException("Command is not a slash-command");
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction addSubcommandGroups(@Nonnull SubcommandGroupData group)
    {
        if(data instanceof SlashCommandData)
            ((SlashCommandData) data).addSubcommandGroups(group);
        else
            throw new IllegalArgumentException("Command is not a slash-command");
        return this;
    }

    @Override
    public RequestBody finalizeData()
    {
        return getRequestBody(((CommandData) data).toData());
    }

    @Override
    protected void handleSuccess(Response response, Request<Command> request)
    {
        DataObject json = response.getObject();
        request.onSuccess(CommandType.fromKey(json.getInt("type", 1)).create(api, guild, json));
    }
}
