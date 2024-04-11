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
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.interactions.command.CommandImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class CommandCreateActionImpl extends RestActionImpl<Command> implements CommandCreateAction
{
    private final Guild guild;
    private final CommandDataImpl data;

    public CommandCreateActionImpl(JDAImpl api, CommandDataImpl command)
    {
        super(api, Route.Interactions.CREATE_COMMAND.compile(api.getSelfUser().getApplicationId()));
        this.guild = null;
        this.data = command;
    }

    public CommandCreateActionImpl(Guild guild, CommandDataImpl command)
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
    public CommandCreateAction setDefaultPermissions(@Nonnull DefaultMemberPermissions permission)
    {
        data.setDefaultPermissions(permission);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setGuildOnly(boolean guildOnly)
    {
        data.setGuildOnly(guildOnly);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setContexts(@Nonnull Collection<InteractionContextType> contexts)
    {
        data.setContexts(contexts);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setIntegrationTypes(@Nonnull Collection<IntegrationType> integrationTypes)
    {
        data.setIntegrationTypes(integrationTypes);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setNSFW(boolean nsfw)
    {
        data.setNSFW(nsfw);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction)
    {
        data.setLocalizationFunction(localizationFunction);
        return this;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return data.getName();
    }

    @Nonnull
    @Override
    public LocalizationMap getNameLocalizations()
    {
        return data.getNameLocalizations();
    }

    @Nonnull
    @Override
    public Command.Type getType()
    {
        return data.getType();
    }

    @Nonnull
    @Override
    public DefaultMemberPermissions getDefaultPermissions()
    {
        return data.getDefaultPermissions();
    }

    @Override
    public boolean isGuildOnly()
    {
        return data.isGuildOnly();
    }

    @Nonnull
    @Override
    public Set<InteractionContextType> getContexts()
    {
        return data.getContexts();
    }

    @Nonnull
    @Override
    public Set<IntegrationType> getIntegrationTypes()
    {
        return data.getIntegrationTypes();
    }

    @Override
    public boolean isNSFW()
    {
        return data.isNSFW();
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
        data.setName(name);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
    {
        data.setNameLocalization(locale, name);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        data.setNameLocalizations(map);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setDescription(@Nonnull String description)
    {
        data.setDescription(description);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description)
    {
        data.setDescriptionLocalization(locale, description);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        data.setDescriptionLocalizations(map);
        return this;
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return data.getDescription();
    }

    @Nonnull
    @Override
    public LocalizationMap getDescriptionLocalizations()
    {
        return data.getDescriptionLocalizations();
    }

    @Override
    public boolean removeOptions(@Nonnull Predicate<? super OptionData> condition)
    {
        return data.removeOptions(condition);
    }

    @Override
    public boolean removeSubcommands(@Nonnull Predicate<? super SubcommandData> condition)
    {
        return data.removeSubcommands(condition);
    }

    @Override
    public boolean removeSubcommandGroups(@Nonnull Predicate<? super SubcommandGroupData> condition)
    {
        return data.removeSubcommandGroups(condition);
    }

    @Nonnull
    @Override
    public List<SubcommandData> getSubcommands()
    {
        return data.getSubcommands();
    }

    @Nonnull
    @Override
    public List<SubcommandGroupData> getSubcommandGroups()
    {
        return data.getSubcommandGroups();
    }

    @Nonnull
    @Override
    public List<OptionData> getOptions()
    {
        return data.getOptions();
    }

    @Nonnull
    @Override
    public CommandCreateAction addOptions(@Nonnull OptionData... options)
    {
        data.addOptions(options);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction addSubcommands(@Nonnull SubcommandData... subcommand)
    {
        data.addSubcommands(subcommand);
        return this;
    }

    @Nonnull
    @Override
    public CommandCreateAction addSubcommandGroups(@Nonnull SubcommandGroupData... group)
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
        request.onSuccess(new CommandImpl(api, guild, json));
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return data.toData();
    }
}
