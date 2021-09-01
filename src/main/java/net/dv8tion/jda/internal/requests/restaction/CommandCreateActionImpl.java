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
import net.dv8tion.jda.api.interactions.commands.*;
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

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class CommandCreateActionImpl<T extends Command> extends RestActionImpl<T> implements CommandCreateAction<T>
{
    private final Guild guild;
    private final CommandData<? extends CommandData<?>> data;

    public CommandCreateActionImpl(JDAImpl api, CommandData<? extends CommandData<?>> command)
    {
        super(api, Route.Interactions.CREATE_COMMAND.compile(api.getSelfUser().getApplicationId()));
        this.guild = null;
        this.data = command;
    }

    public CommandCreateActionImpl(Guild guild, CommandData<? extends CommandData<?>> command)
    {
        super(guild.getJDA(), Route.Interactions.CREATE_GUILD_COMMAND.compile(guild.getJDA().getSelfUser().getApplicationId(), guild.getId()));
        this.guild = guild;
        this.data = command;
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> addCheck(@Nonnull BooleanSupplier checks)
    {
        return (CommandCreateAction<T>) super.addCheck(checks);
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> setCheck(BooleanSupplier checks)
    {
        return (CommandCreateAction<T>) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> deadline(long timestamp)
    {
        return (CommandCreateAction<T>) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> setDefaultEnabled(boolean enabled)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only set the default enabled for a SlashCommand!");
        }
        data.setDefaultEnabled(enabled);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CommandCreateAction<T>) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 32, "Name");
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
        data.setName(name);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> setDescription(@Nonnull String description)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only set the description for a SlashCommand!");
        }
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, 100, "Description");
        ((CommandData.SlashCommand) data).setDescription(description);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> addOptions(@Nonnull OptionData... options)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only add options to a SlashCommand!");
        }
        ((CommandData.SlashCommand) data).addOptions(options);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> addSubcommands(@Nonnull SubcommandData subcommand)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only add subcommands to a SlashCommand!");
        }
        ((CommandData.SlashCommand) data).addSubcommands(subcommand);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction<T> addSubcommandGroups(@Nonnull SubcommandGroupData group)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only add subcommand groups to a SlashCommand!");
        }
        ((CommandData.SlashCommand) data).addSubcommandGroups(group);
        return this;
    }

    @Override
    public RequestBody finalizeData()
    {
        return getRequestBody(data.toData());
    }

    @Override
    protected void handleSuccess(Response response, Request<T> request)
    {
        DataObject obj = response.getObject();
        T command;
        switch (data.getType())
        {
        case SLASH:
            command = (T) new SlashCommand(api, guild, obj);
            break;
        case USER:
            command = (T) new UserCommand(api, guild, obj);
            break;
        case MESSAGE:
            command = (T) new MessageCommand(api, guild, obj);
            break;
        default:
            request.onFailure(new IllegalStateException("Created command of unknown type!"));
            return;
        }
        request.onSuccess(command);
    }
}
