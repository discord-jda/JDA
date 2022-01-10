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
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.interactions.command.CommandImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandListUpdateActionImpl extends RestActionImpl<List<Command>> implements CommandListUpdateAction
{
    private final List<CommandData> commands = new ArrayList<>();
    private final GuildImpl guild;
    private int slash, user, message;

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
    public CommandListUpdateAction addCommands(@Nonnull Collection<? extends CommandData> commands)
    {
        Checks.noneNull(commands, "Command");
        int newSlash = 0, newUser = 0, newMessage = 0;
        for (CommandData command : commands)
        {
            switch (command.getType())
            {
            case SLASH:
                newSlash++;
                break;
            case MESSAGE:
                newMessage++;
                break;
            case USER:
                newUser++;
                break;
            }
        }

        Checks.check(slash + newSlash <= Commands.MAX_SLASH_COMMANDS,
                "Cannot have more than %d slash commands! Try using subcommands instead.", Commands.MAX_SLASH_COMMANDS);
        Checks.check(user + newUser <= Commands.MAX_USER_COMMANDS,
                "Cannot have more than %d user context commands!", Commands.MAX_USER_COMMANDS);
        Checks.check(message + newMessage <= Commands.MAX_MESSAGE_COMMANDS,
                "Cannot have more than %d message context commands!", Commands.MAX_MESSAGE_COMMANDS);

        Checks.checkUnique(
            Stream.concat(commands.stream(), this.commands.stream()).map(c -> c.getType() + " " + c.getName()),
            "Cannot have multiple commands of the same type with identical names. " +
            "Name: \"%s\" with type %s appeared %d times!",
            (count, value) -> {
                String[] tuple = value.split(" ", 2);
                return new Object[] { tuple[1], tuple[0], count };
            }
        );

        slash += newSlash;
        user += newUser;
        message += newMessage;

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
        List<Command> commands = response.getArray().stream(DataArray::getObject)
                .map(obj -> new CommandImpl(api, guild, obj))
                .collect(Collectors.toList());
        request.onSuccess(commands);
    }
}
