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
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class CommandCreateActionImpl extends RestActionImpl<Command> implements CommandCreateAction
{
    private final Guild guild;
    private CommandData data;

    public CommandCreateActionImpl(JDAImpl api, CommandData command)
    {
        super(api, Route.Interactions.CREATE_COMMAND.compile(api.getSelfUser().getApplicationId()));
        this.guild = null;
        this.data = command;
    }

    public CommandCreateActionImpl(Guild guild, CommandData command)
    {
        super(guild.getJDA(), Route.Interactions.CREATE_GUILD_COMMAND.compile(guild.getJDA().getSelfUser().getApplicationId(), guild.getId()));
        this.guild = guild;
        this.data = command;
    }

    @NotNull
    @Override
    public CommandCreateAction addCheck(@NotNull BooleanSupplier checks)
    {
        return (CommandCreateAction) super.addCheck(checks);
    }

    @NotNull
    @Override
    public CommandCreateAction setCheck(BooleanSupplier checks)
    {
        return (CommandCreateAction) super.setCheck(checks);
    }

    @NotNull
    @Override
    public CommandCreateAction deadline(long timestamp)
    {
        return (CommandCreateAction) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public CommandCreateAction setDefaultEnabled(boolean enabled)
    {
        data.setDefaultEnabled(enabled);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction timeout(long timeout, @NotNull TimeUnit unit)
    {
        return (CommandCreateAction) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public CommandCreateAction setName(@NotNull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        data.setName(name);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction setDescription(@NotNull String description)
    {
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
        data.setDescription(description);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction addOptions(@NotNull OptionData... options)
    {
        data.addOptions(options);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction addSubcommands(@NotNull SubcommandData subcommand)
    {
        data.addSubcommands(subcommand);
        return this;
    }

    @NotNull
    @Override
    public CommandCreateAction addSubcommandGroups(@NotNull SubcommandGroupData group)
    {
        data.addSubcommandGroups(group);
        return this;
    }

    @Override
    public RequestBody finalizeData()
    {
        return getRequestBody(data.toData());
    }

    @Override
    protected void handleSuccess(Response response, Request<Command> request)
    {
        DataObject json = response.getObject();
        request.onSuccess(new Command(api, guild, json));
    }
}
