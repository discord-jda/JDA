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
import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CommandEditActionImpl extends RestActionImpl<Command> implements CommandEditAction
{
    private final Guild guild;
    private String name, description;
    private List<CommandCreateActionImpl.Option> options = null;

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
        if (name != null)
        {
            Checks.notEmpty(name, "Name");
            Checks.check(name.length() <= 32, "Name must not be longer than 32 characters.");
        }

        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction setDescription(@Nullable String description)
    {
        if (description != null)
        {
            Checks.notEmpty(description, "Description");
            Checks.check(description.length() <= 100, "Description must not be longer than 100 characters");
        }

        this.description = description;
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction clearOptions()
    {
        options = new ArrayList<>();
        return this;
    }

    @Nonnull
    @Override
    public CommandEditAction addOption(@Nonnull String name, @Nonnull String description, @Nonnull Command.OptionType type, @Nonnull Consumer<? super CommandCreateAction.OptionBuilder> builder)
    {
        Checks.notNull(name, "Name");
        Checks.notNull(description, "Description");
        Checks.notNull(type, "Type");
        Checks.notNull(builder, "Consumer");

        CommandCreateActionImpl.Option optionBuilder = new CommandCreateActionImpl.Option(type, name, description);
        builder.accept(optionBuilder);
        if (options == null)
            options = new ArrayList<>();
        options.add(optionBuilder);
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject json = DataObject.empty();
        if (name != null)
            json.put("name", name);
        if (description != null)
            json.put("description", description);
        if (options != null)
            json.put("options", DataArray.fromCollection(options));
        return getRequestBody(json);
    }

    @Override
    protected void handleSuccess(Response response, Request<Command> request)
    {
        DataObject json = response.getObject();
        request.onSuccess(new Command(api, guild, json));
    }
}
