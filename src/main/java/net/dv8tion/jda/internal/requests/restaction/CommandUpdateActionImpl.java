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
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import net.dv8tion.jda.api.utils.data.DataArray;
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

public class CommandUpdateActionImpl extends RestActionImpl<Void> implements CommandUpdateAction
{
    private final List<CommandUpdateAction.CommandData> commands = new ArrayList<>();

    public CommandUpdateActionImpl(JDA api, Route.CompiledRoute route)
    {
        super(api, route);
    }

    @Nonnull
    @Override
    public CommandUpdateAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return CommandUpdateAction.super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public CommandUpdateAction addCheck(@Nonnull BooleanSupplier checks)
    {
        return CommandUpdateAction.super.addCheck(checks);
    }

    @Nonnull
    @Override
    public CommandUpdateAction setCheck(BooleanSupplier checks)
    {
        return (CommandUpdateAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public CommandUpdateAction deadline(long timestamp)
    {
        return (CommandUpdateAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public CommandUpdateAction addCommands(@Nonnull Collection<? extends CommandData> commands)
    {
        Checks.noneNull(commands, "Command");
        Checks.check(this.commands.size() + commands.size() <= 100, "Cannot have more than 100 commands! Try using subcommands instead.");
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
}
