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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class CommandEditActionImpl<T extends Command> extends RestActionImpl<T> implements CommandEditAction<T>
{
    private static final String UNDEFINED = "undefined";
    private static final int NAME_SET = 1 << 0;
    private static final int DESCRIPTION_SET = 1 << 1;
    private static final int OPTIONS_SET = 1 << 2;

    private final Guild guild;
    private int mask = 0;
    private CommandData<? extends CommandData<?>> data = new CommandData<>(UNDEFINED, UNDEFINED, CommandType.UNKNOWN);

    public CommandEditActionImpl(JDA api, String id)
    {
        super(api, Route.Interactions.EDIT_COMMAND.compile(api.getSelfUser().getApplicationId(), id));
        this.guild = null;
    }

    public CommandEditActionImpl(Guild guild, String id)
    {
        super(guild.getJDA(), Route.Interactions.EDIT_GUILD_COMMAND.compile(guild.getJDA().getSelfUser().getApplicationId(), guild.getId(), id));
        this.guild = guild;
    }

    @Nonnull
    @Override
    public CommandEditAction<T> setCheck(BooleanSupplier checks)
    {
        return (CommandEditAction<T>) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public CommandEditAction<T> deadline(long timestamp)
    {
        return (CommandEditAction<T>) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public CommandEditAction<T> apply(@Nonnull CommandData<? extends CommandData<?>> commandData)
    {
        Checks.notNull(commandData, "Command Data");
        this.mask = NAME_SET | DESCRIPTION_SET | OPTIONS_SET;
        this.data = commandData;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction<T> setDefaultEnabled(boolean enabled)
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
    public CommandEditAction<T> addCheck(@Nonnull BooleanSupplier checks)
    {
        return (CommandEditAction<T>) super.addCheck(checks);
    }

    @Nonnull
    @Override
    public CommandEditAction<T> timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CommandEditAction<T>) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public CommandEditAction<T> setName(@Nullable String name)
    {
        if (name == null)
        {
            mask &= ~NAME_SET;
            return this;
        }
        data.setName(name);
        mask |= NAME_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction<T> setDescription(@Nullable String description)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only set the description for a SlashCommand!");
        }
        if (description == null)
        {
            mask &= ~DESCRIPTION_SET;
            return this;
        }
        ((CommandData.SlashCommand) data).setDescription(description);
        mask |= DESCRIPTION_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction<T> clearOptions()
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only clear options for a SlashCommand!");
        }
        data = new CommandData.SlashCommand(data.getName(), data.getDescription());
        mask &= ~OPTIONS_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction<T> addOptions(@Nonnull OptionData... options)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only add options to a SlashCommand!");
        }
        ((CommandData.SlashCommand) data).addOptions(options);
        mask |= OPTIONS_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction<T> addSubcommands(@Nonnull SubcommandData... subcommands)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only add subcommands to a SlashCommand!");
        }
        ((CommandData.SlashCommand) data).addSubcommands(subcommands);
        mask |= OPTIONS_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction<T> addSubcommandGroups(@Nonnull SubcommandGroupData... groups)
    {
        if (data.getType() != CommandType.SLASH)
        {
            throw new UnsupportedOperationException("Can only add subcommand groups to a SlashCommand!");
        }
        ((CommandData.SlashCommand) data).addSubcommandGroups(groups);
        mask |= OPTIONS_SET;
        return this;
    }

    private boolean isUnchanged(int flag)
    {
        return (mask & flag) != flag;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject json = data.toData();
        if (isUnchanged(NAME_SET))
            json.remove("name");
        if (isUnchanged(DESCRIPTION_SET))
            json.remove("description");
        if (isUnchanged(OPTIONS_SET))
            json.remove("options");
        mask = 0;
        data = new CommandData<>(UNDEFINED, UNDEFINED, CommandType.UNKNOWN);
        return getRequestBody(json);
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
            request.onFailure(new IllegalStateException("Edited command of unknown type!"));
            return;
        }
        request.onSuccess(command);
    }
}
