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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class CommandListUpdateActionImpl extends RestActionImpl<List<Command>> implements CommandListUpdateAction
{
    private final List<CommandData<? extends CommandData<?>>> commands = new ArrayList<>();
    private final GuildImpl guild;

    public CommandListUpdateActionImpl(JDA api, GuildImpl guild, Route.CompiledRoute route)
    {
        super(api, route);
        this.guild = guild;
    }

    @Nonnull
    @Override
    public CommandListUpdateAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CommandListUpdateAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public CommandListUpdateAction addCheck(@Nonnull BooleanSupplier checks)
    {
        return (CommandListUpdateAction) super.addCheck(checks);
    }

    @Nonnull
    @Override
    public CommandListUpdateAction setCheck(BooleanSupplier checks)
    {
        return (CommandListUpdateAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public CommandListUpdateAction deadline(long timestamp)
    {
        return (CommandListUpdateAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public CommandListUpdateAction addCommands(@Nonnull Collection<? extends CommandData<?>> commands)
    {
        Checks.noneNull(commands, "Command");
        Checks.check(this.commands.stream()
                .filter(commandData -> commandData.getType() == CommandType.SLASH).count() + commands.stream()
                        .filter(commandData -> commandData.getType() == CommandType.SLASH).count() <= 100,
                "Cannot have more than 100 slash commands! Try using subcommands instead.");
        Checks.check(this.commands.stream()
                .filter(commandData -> commandData.getType() == CommandType.USER).count() + commands.stream()
                .filter(commandData -> commandData.getType() == CommandType.USER).count() <= 5,
                "Cannot have more than 5 user commands!");
        Checks.check(this.commands.stream()
                        .filter(commandData -> commandData.getType() == CommandType.MESSAGE).count() + commands.stream()
                        .filter(commandData -> commandData.getType() == CommandType.MESSAGE).count() <= 5,
                "Cannot have more than 5 message commands!");

        this.commands.addAll(commands);
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataArray json = DataArray.empty();
        json.addAll(commands);
        return getRequestBody(json);
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Command>> request)
    {
        List<Command> commands = new ArrayList<>();
        Iterator<DataObject> it = response.getArray().stream(DataArray::getObject).iterator();
        while (it.hasNext())
        {
            DataObject obj = it.next();
            switch (CommandType.fromId(obj.getInt("type")))
            {
            case SLASH:
                commands.add(new SlashCommand(api, guild, obj));
                break;
            case USER:
                commands.add(new UserCommand(api, guild, obj));
                break;
            case MESSAGE:
                commands.add(new MessageCommand(api, guild, obj));
                break;
            default:
                request.onFailure(new IllegalStateException("Created channel of unknown type!"));
                return;
            }
        }
        request.onSuccess(commands);
    }
}
