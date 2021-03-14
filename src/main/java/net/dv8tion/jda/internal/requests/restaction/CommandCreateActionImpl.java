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

import net.dv8tion.jda.api.entities.Command;
import net.dv8tion.jda.api.entities.Command.OptionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandCreateActionImpl extends RestActionImpl<Command> implements CommandCreateAction
{
    private final Guild guild;
    private String name;
    private String description;
    private final List<Option> options = new ArrayList<>();

    public CommandCreateActionImpl(JDAImpl api)
    {
        super(api, Route.Interactions.CREATE_COMMAND.compile(api.getSelfUser().getApplicationId()));
        this.guild = null;
    }

    public CommandCreateActionImpl(Guild guild)
    {
        super(guild.getJDA(), Route.Interactions.CREATE_GUILD_COMMAND.compile(guild.getJDA().getSelfUser().getApplicationId(), guild.getId()));
        this.guild = guild;
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
    public CommandCreateAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CommandCreateAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public CommandCreateAction setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setDescription(@Nonnull String description)
    {
        Checks.notEmpty(description, "Description");
        this.description = description;
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction addOption(@Nonnull String name, @Nonnull String description, @Nonnull OptionType type, @Nonnull Consumer<? super OptionBuilder> builder)
    {
        Checks.notEmpty(name, "Name");
        Checks.notEmpty(description, "Description");
        Checks.notNull(type, "Type");
        Checks.notNull(builder, "Builder");
        Option option = new Option(type, name, description);
        builder.accept(option);
        this.options.add(option);
        return this;
    }

    @Override
    public RequestBody finalizeData()
    {
        DataObject json = DataObject.empty();
        json.put("name", name);
        json.put("description", description);
        if (!options.isEmpty())
            json.put("options", DataArray.fromCollection(options));
        return getRequestBody(json);
    }

    @Override
    protected void handleSuccess(Response response, Request<Command> request)
    {
        DataObject json = response.getObject();
        request.onSuccess(new Command(api, guild, json));
    }

    protected static class Option implements OptionBuilder, SerializableData
    {
        private final OptionType type;
        private final String name;
        private final String description;
        private final Map<String, Object> choices = new HashMap<>();
        private final List<Option> options = new ArrayList<>();
        private boolean required, isDefault; // TODO: we can do some validation here, maybe?

        protected Option(OptionType type, String name, String description)
        {
            this.type = type;
            this.name = name;
            this.description = description;
        }

        @Override
        public Option setRequired(boolean required)
        {
            this.required = required;
            return this;
        }

        @Override
        public Option setDefault(boolean isDefault)
        {
            this.isDefault = isDefault;
            return this;
        }

        @Override
        public OptionBuilder addChoice(String name, String value)
        {
            this.choices.put(name, value);
            return this;
        }

        @Override
        public OptionBuilder addChoice(String name, int value)
        {
            this.choices.put(name, value);
            return this;
        }

        @Override
        public Option addOption(String name, String description, OptionType type, Consumer<? super OptionBuilder> builder)
        {
            Checks.notEmpty(name, "Name");
            Checks.notEmpty(description, "Description");
            Checks.notNull(type, "Type");
            Checks.notNull(builder, "Builder");
            Option option = new Option(type, name, description);
            builder.accept(option);
            this.options.add(option);
            return this;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            DataObject json = DataObject.empty();
            json.put("name", name);
            json.put("description", description);
            json.put("type", type.getKey());
            json.put("required", required);
            json.put("default", isDefault);
            if (!choices.isEmpty())
            {
                json.put("choices", DataArray.fromCollection(choices.entrySet()
                    .stream()
                    .map(entry ->
                        DataObject.empty()
                            .put("name", entry.getKey())
                            .put("value", entry.getValue()))
                    .collect(Collectors.toList())
                ));
            }
            if (!options.isEmpty())
                json.put("options", DataArray.fromCollection(options));
            return json;
        }
    }
}
