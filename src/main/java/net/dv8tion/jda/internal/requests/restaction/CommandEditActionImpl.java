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

public class CommandEditActionImpl extends RestActionImpl<Command> implements CommandEditAction
{
    private static final String UNDEFINED = "undefined";
    private static final int NAME_SET        = 1 << 0;
    private static final int DESCRIPTION_SET = 1 << 1;
    private static final int OPTIONS_SET     = 1 << 2;
    private final Guild guild;
    private int mask = 0;
    private CommandData data;

    public CommandEditActionImpl(JDA api, String id, CommandType type)
    {
        super(api, Route.Interactions.EDIT_COMMAND.compile(api.getSelfUser().getApplicationId(), id));
        if(type == CommandType.SLASH)
            data = new CommandData(UNDEFINED, UNDEFINED);
        else
            data = new CommandData(type, UNDEFINED);

        this.guild = null;
    }

    public CommandEditActionImpl(Guild guild, String id, CommandType type)
    {
        super(guild.getJDA(), Route.Interactions.EDIT_GUILD_COMMAND.compile(guild.getJDA().getSelfUser().getApplicationId(), guild.getId(), id));
        if(type == CommandType.SLASH)
            data = new CommandData(UNDEFINED, UNDEFINED);
        else
            data = new CommandData(type, UNDEFINED);
        this.guild = guild;
    }

    @Nonnull
    @Override
    public CommandEditAction setCheck(BooleanSupplier checks)
    {
        return (CommandEditAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public CommandEditAction deadline(long timestamp)
    {
        return (CommandEditAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public CommandEditAction apply(@Nonnull CommandData commandData)
    {
        Checks.notNull(commandData, "Command Data");
        this.mask = NAME_SET | DESCRIPTION_SET | OPTIONS_SET;
        this.data = commandData;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction setDefaultEnabled(boolean enabled)
    {
        data.setDefaultEnabled(enabled);
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction addCheck(@Nonnull BooleanSupplier checks)
    {
        return (CommandEditAction) super.addCheck(checks);
    }

    @Nonnull
    @Override
    public CommandEditAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CommandEditAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public CommandEditAction setName(@Nullable String name)
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
    public CommandEditAction setDescription(@Nullable String description)
    {
        Checks.check(data.getCommandType() == CommandType.SLASH, "You can only set the description of slash commands");
        if (description == null)
        {
            mask &= ~DESCRIPTION_SET;
            return this;
        }
        data.setDescription(description);
        mask |= DESCRIPTION_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction clearOptions()
    {
        data = new CommandData(data.getName(), data.getDescription());
        mask &= ~OPTIONS_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction addOptions(@Nonnull OptionData... options)
    {
        Checks.check(data.getCommandType() == CommandType.SLASH, "You can only add options to slash commands");
        data.addOptions(options);
        mask |= OPTIONS_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction addSubcommands(@Nonnull SubcommandData... subcommands)
    {
        Checks.check(data.getCommandType() == CommandType.SLASH, "You can only add subcommands to slash commands");
        data.addSubcommands(subcommands);
        mask |= OPTIONS_SET;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction addSubcommandGroups(@Nonnull SubcommandGroupData... groups)
    {
        Checks.check(data.getCommandType() == CommandType.SLASH, "You can only add subcommand groups to slash commands");
        data.addSubcommandGroups(groups);
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
        data = new CommandData(UNDEFINED, UNDEFINED);
        return getRequestBody(json);
    }

    @Override
    protected void handleSuccess(Response response, Request<Command> request)
    {
        DataObject json = response.getObject();
        switch (CommandType.fromKey(json.getInt("type"))) {
        case SLASH:
            request.onSuccess(new SlashCommand(api, guild, json));
        case USER_CONTEXT:
            request.onSuccess(new UserCommand(api, guild, json));
        case MESSAGE_CONTEXT:
            request.onSuccess(new MessageCommand(api, guild, json));
        default:
            request.onSuccess(new Command(api, guild, json));
        }
    }
}
