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

package net.dv8tion.jda.internal.interactions.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.CommandEditActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.localization.LocalizationUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandImpl implements Command
{
    public static final EnumSet<OptionType> OPTIONS = EnumSet.complementOf(EnumSet.of(OptionType.SUB_COMMAND, OptionType.SUB_COMMAND_GROUP));
    public static final Predicate<DataObject> OPTION_TEST = it -> OPTIONS.contains(OptionType.fromKey(it.getInt("type")));
    public static final Predicate<DataObject> SUBCOMMAND_TEST = it -> OptionType.fromKey(it.getInt("type")) == OptionType.SUB_COMMAND;
    public static final Predicate<DataObject> GROUP_TEST = it -> OptionType.fromKey(it.getInt("type")) == OptionType.SUB_COMMAND_GROUP;

    private final JDAImpl api;
    private final Guild guild;
    private final String name, description;
    private final LocalizationMap nameLocalizations;
    private final LocalizationMap descriptionLocalizations;
    private final List<Command.Option> options;
    private final List<Command.SubcommandGroup> groups;
    private final List<Command.Subcommand> subcommands;
    private final long id, guildId, applicationId, version;
    private final boolean nsfw;
    private final Set<InteractionContextType> contexts;
    private final Set<IntegrationType> integrationTypes;
    private final Command.Type type;
    private final DefaultMemberPermissions defaultMemberPermissions;

    public CommandImpl(JDAImpl api, Guild guild, DataObject json)
    {
        this.api = api;
        this.guild = guild;
        this.name = json.getString("name");
        this.nameLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "name_localizations");
        this.description = json.getString("description", "");
        this.descriptionLocalizations = LocalizationUtils.unmodifiableFromProperty(json, "description_localizations");
        this.type = Command.Type.fromId(json.getInt("type", 1));
        this.id = json.getUnsignedLong("id");
        this.guildId = guild != null ? guild.getIdLong() : 0L;
        this.applicationId = json.getUnsignedLong("application_id", api.getSelfUser().getApplicationIdLong());
        this.options = parseOptions(json, OPTION_TEST, Command.Option::new);
        this.groups = parseOptions(json, GROUP_TEST, (DataObject o) -> new SubcommandGroup(this, o));
        this.subcommands = parseOptions(json, SUBCOMMAND_TEST, (DataObject o) -> new Subcommand(this, o));
        this.version = json.getUnsignedLong("version", id);

        this.defaultMemberPermissions = json.isNull("default_member_permissions")
                ? DefaultMemberPermissions.ENABLED
                : DefaultMemberPermissions.enabledFor(json.getLong("default_member_permissions"));

        this.contexts = json.optArray("contexts")
                .map(d -> d.stream(DataArray::getString)
                        .map(InteractionContextType::fromKey)
                        .collect(Helpers.toUnmodifiableEnumSet(InteractionContextType.class))
                )
                // If the command is in a guild, it can only be guild, otherwise up to the dm_permission flag
                .orElseGet(() ->
                {
                    if (guildId != 0L) return Helpers.unmodifiableEnumSet(InteractionContextType.GUILD);

                    final boolean dmPermission = json.getBoolean("dm_permission", true);
                    return dmPermission
                            ? Helpers.unmodifiableEnumSet(InteractionContextType.GUILD, InteractionContextType.BOT_DM)
                            : Helpers.unmodifiableEnumSet(InteractionContextType.GUILD);
                });
        this.integrationTypes = json.optArray("integration_types")
                .map(d -> d.stream(DataArray::getString)
                        .map(IntegrationType::fromKey)
                        .collect(Helpers.toUnmodifiableEnumSet(IntegrationType.class))
                )
                .orElse(Helpers.unmodifiableEnumSet(IntegrationType.GUILD_INSTALL));
        this.nsfw = json.getBoolean("nsfw");
    }

    public static <T> List<T> parseOptions(DataObject json, Predicate<DataObject> test, Function<DataObject, T> transform)
    {
        return json.optArray("options").map(arr ->
            arr.stream(DataArray::getObject)
               .filter(test)
               .map(transform)
               .collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        checkSelfUser("Cannot delete a command from another bot!");
        Route.CompiledRoute route;
        String appId = getJDA().getSelfUser().getApplicationId();
        if (guildId != 0L)
            route = Route.Interactions.DELETE_GUILD_COMMAND.compile(appId, Long.toUnsignedString(guildId), getId());
        else
            route = Route.Interactions.DELETE_COMMAND.compile(appId, getId());
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public CommandEditAction editCommand()
    {
        checkSelfUser("Cannot edit a command from another bot!");
        return guild == null ? new CommandEditActionImpl(api, getId()) : new CommandEditActionImpl(guild, getId());
    }

    @Nonnull
    @Override
    public RestAction<List<IntegrationPrivilege>> retrievePrivileges(@Nonnull Guild guild)
    {
        checkSelfUser("Cannot retrieve privileges for a command from another bot!");
        Checks.notNull(guild, "Guild");
        return guild.retrieveIntegrationPrivilegesById(id);
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public Command.Type getType()
    {
        return type;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public LocalizationMap getNameLocalizations()
    {
        return nameLocalizations;
    }

    @Nonnull
    @Override
    public String getFullCommandName()
    {
        return name;
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return description;
    }

    @Nonnull
    @Override
    public LocalizationMap getDescriptionLocalizations()
    {
        return descriptionLocalizations;
    }

    @Nonnull
    @Override
    public List<Command.Option> getOptions()
    {
        return options;
    }

    @Nonnull
    @Override
    public List<Command.Subcommand> getSubcommands()
    {
        return subcommands;
    }

    @Nonnull
    @Override
    public List<Command.SubcommandGroup> getSubcommandGroups()
    {
        return groups;
    }

    @Override
    public long getApplicationIdLong()
    {
        return applicationId;
    }

    @Override
    public long getVersion()
    {
        return version;
    }

    @Nonnull
    @Override
    public DefaultMemberPermissions getDefaultPermissions()
    {
        return defaultMemberPermissions;
    }

    @Override
    public boolean isGuildOnly()
    {
        return contexts.size() == 1 && contexts.contains(InteractionContextType.GUILD);
    }

    @Nonnull
    @Override
    public Set<InteractionContextType> getContexts()
    {
        return contexts;
    }

    @Nonnull
    @Override
    public Set<IntegrationType> getIntegrationTypes()
    {
        return integrationTypes;
    }

    @Override
    public boolean isNSFW()
    {
        return nsfw;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public String getAsMention()
    {
        if (getType() != Type.SLASH)
            throw new IllegalStateException("Only slash commands can be mentioned");
        return Command.super.getAsMention();
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(getType())
                .setName(getName())
                .toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof Command))
            return false;
        return id == ((Command) obj).getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    private void checkSelfUser(String s)
    {
        if (applicationId != api.getSelfUser().getApplicationIdLong())
            throw new IllegalStateException(s);
    }
}
