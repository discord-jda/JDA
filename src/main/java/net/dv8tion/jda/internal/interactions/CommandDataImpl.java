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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.interactions.command.localization.LocalizationMapper;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandDataImpl implements SlashCommandData
{
    protected final List<SerializableData> options = new ArrayList<>(MAX_OPTIONS);

    protected String name, description = "";
    private LocalizationMapper localizationMapper;
    private final LocalizationMap nameLocalizations = new LocalizationMap(this::checkName);
    private final LocalizationMap descriptionLocalizations = new LocalizationMap(this::checkDescription);

    private boolean allowSubcommands = true;
    private boolean allowOption = true;
    private boolean allowRequired = true;
    private EnumSet<InteractionContextType> contexts = EnumSet.of(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    private EnumSet<IntegrationType> integrationTypes = EnumSet.of(IntegrationType.GUILD_INSTALL);
    private boolean nsfw = false;
    private DefaultMemberPermissions defaultMemberPermissions = DefaultMemberPermissions.ENABLED;

    private final Command.Type type;

    public CommandDataImpl(@Nonnull String name, @Nonnull String description)
    {
        this.type = Command.Type.SLASH;
        setName(name);
        setDescription(description);
    }

    public CommandDataImpl(@Nonnull Command.Type type, @Nonnull String name)
    {
        this.type = type;
        Checks.notNull(type, "Command Type");
        Checks.check(type != Command.Type.SLASH, "Cannot create slash command without description. Use `new CommandDataImpl(name, description)` instead.");
        setName(name);
    }

    protected void checkType(Command.Type required, String action)
    {
        if (required != type)
            throw new IllegalStateException("Cannot " + action + " for commands of type " + type);
    }

    public void checkName(@Nonnull String name)
    {
        Checks.inRange(name, 1, MAX_NAME_LENGTH, "Name");
        if (type == Command.Type.SLASH)
        {
            Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
            Checks.isLowercase(name, "Name");
        }
    }

    public void checkDescription(@Nonnull String description)
    {
        checkType(Command.Type.SLASH, "set description");
        Checks.inRange(description, 1, MAX_DESCRIPTION_LENGTH, "Description");
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataArray options = DataArray.fromCollection(this.options);

        if (localizationMapper != null) localizationMapper.localizeCommand(this, options);

        DataObject json = DataObject.empty()
                .put("type", type.getId())
                .put("name", name)
                .put("nsfw", nsfw)
                .put("options", options)
                .put("contexts", contexts.stream().map(InteractionContextType::getType).collect(Collectors.toList()))
                .put("integration_types", integrationTypes.stream().map(IntegrationType::getType).collect(Collectors.toList()))
                .put("default_member_permissions", defaultMemberPermissions == DefaultMemberPermissions.ENABLED
                        ? null
                        : Long.toUnsignedString(defaultMemberPermissions.getPermissionsRaw()))
                .put("name_localizations", nameLocalizations);

        if (type == Command.Type.SLASH)
        {
            json.put("description", description)
                .put("description_localizations", descriptionLocalizations);
        }
        return json;
    }

    @Nonnull
    @Override
    public Command.Type getType()
    {
        return type;
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
        return Collections.unmodifiableSet(contexts);
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

    @Nonnull
    @Override
    public List<OptionData> getOptions()
    {
        return options.stream()
                .filter(OptionData.class::isInstance)
                .map(OptionData.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public List<SubcommandData> getSubcommands()
    {
        return options.stream()
                .filter(SubcommandData.class::isInstance)
                .map(SubcommandData.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public List<SubcommandGroupData> getSubcommandGroups()
    {
        return options.stream()
                .filter(SubcommandGroupData.class::isInstance)
                .map(SubcommandGroupData.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public CommandDataImpl setDefaultPermissions(@Nonnull DefaultMemberPermissions permissions)
    {
        Checks.notNull(permissions, "Permissions");
        this.defaultMemberPermissions = permissions;
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setGuildOnly(boolean guildOnly)
    {
        setContexts(guildOnly
                ? EnumSet.of(InteractionContextType.GUILD)
                : EnumSet.of(InteractionContextType.GUILD, InteractionContextType.BOT_DM));
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setContexts(@Nonnull Collection<InteractionContextType> contexts)
    {
        this.contexts = EnumSet.copyOf(contexts);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setIntegrationTypes(@Nonnull Collection<IntegrationType> integrationTypes)
    {
        this.integrationTypes = EnumSet.copyOf(integrationTypes);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setNSFW(boolean nsfw)
    {
        this.nsfw = nsfw;
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl addOptions(@Nonnull OptionData... options)
    {
        Checks.noneNull(options, "Option");
        if (options.length == 0)
            return this;
        checkType(Command.Type.SLASH, "add options");
        Checks.check(options.length + this.options.size() <= CommandData.MAX_OPTIONS, "Cannot have more than %d options for a command!", CommandData.MAX_OPTIONS);
        Checks.check(allowOption, "You cannot mix options with subcommands/groups.");
        boolean allowRequired = this.allowRequired;
        for (OptionData option : options)
        {
            Checks.check(option.getType() != OptionType.SUB_COMMAND, "Cannot add a subcommand with addOptions(...). Use addSubcommands(...) instead!");
            Checks.check(option.getType() != OptionType.SUB_COMMAND_GROUP, "Cannot add a subcommand group with addOptions(...). Use addSubcommandGroups(...) instead!");
            Checks.check(allowRequired || !option.isRequired(), "Cannot add required options after non-required options!");
            allowRequired = option.isRequired(); // prevent adding required options after non-required options
        }

        Checks.checkUnique(
            Stream.concat(getOptions().stream(), Arrays.stream(options)).map(OptionData::getName),
            "Cannot have multiple options with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count }
        );

        allowSubcommands = false;
        this.allowRequired = allowRequired;
        Collections.addAll(this.options, options);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl addSubcommands(@Nonnull SubcommandData... subcommands)
    {
        Checks.noneNull(subcommands, "Subcommands");
        if (subcommands.length == 0)
            return this;
        checkType(Command.Type.SLASH, "add subcommands");
        if (!allowSubcommands)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        Checks.check(subcommands.length + this.options.size() <= CommandData.MAX_OPTIONS, "Cannot have more than %d subcommands for a command!", CommandData.MAX_OPTIONS);
        Checks.checkUnique(
            Stream.concat(getSubcommands().stream(), Arrays.stream(subcommands)).map(SubcommandData::getName),
            "Cannot have multiple subcommands with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count }
        );

        allowOption = false;
        Collections.addAll(this.options, subcommands);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl addSubcommandGroups(@Nonnull SubcommandGroupData... groups)
    {
        Checks.noneNull(groups, "SubcommandGroups");
        if (groups.length == 0)
            return this;
        checkType(Command.Type.SLASH, "add subcommand groups");
        if (!allowSubcommands)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        Checks.check(groups.length + this.options.size() <= CommandData.MAX_OPTIONS, "Cannot have more than %d subcommand groups for a command!", CommandData.MAX_OPTIONS);
        Checks.checkUnique(
            Stream.concat(getSubcommandGroups().stream(), Arrays.stream(groups)).map(SubcommandGroupData::getName),
            "Cannot have multiple subcommand groups with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count }
        );

        allowOption = false;
        Collections.addAll(this.options, groups);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction) {
        Checks.notNull(localizationFunction, "Localization function");

        this.localizationMapper = LocalizationMapper.fromFunction(localizationFunction);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setName(@Nonnull String name)
    {
        checkName(name);
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
    {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslation(locale, name);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        nameLocalizations.setTranslations(map);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setDescription(@Nonnull String description)
    {
        checkDescription(description);
        this.description = description;
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description)
    {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslation(locale, description);
        return this;
    }

    @Nonnull
    @Override
    public CommandDataImpl setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        descriptionLocalizations.setTranslations(map);
        return this;
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

    @Override
    public boolean removeOptions(@Nonnull Predicate<? super OptionData> condition)
    {
        Checks.notNull(condition, "Condition");
        boolean modified = options.removeIf((o) -> o instanceof OptionData && condition.test((OptionData) o));
        if (modified)
            updateAllowedOptions();
        return modified;
    }

    @Override
    public boolean removeSubcommands(@Nonnull Predicate<? super SubcommandData> condition)
    {
        Checks.notNull(condition, "Condition");
        boolean modified = options.removeIf((o) -> o instanceof SubcommandData && condition.test((SubcommandData) o));
        if (modified)
            updateAllowedOptions();
        return modified;
    }

    @Override
    public boolean removeSubcommandGroups(@Nonnull Predicate<? super SubcommandGroupData> condition)
    {
        Checks.notNull(condition, "Condition");
        boolean modified = options.removeIf((o) -> o instanceof SubcommandGroupData && condition.test((SubcommandGroupData) o));
        if (modified)
            updateAllowedOptions();
        return modified;
    }

    // Update allowed conditions after removing options
    private void updateAllowedOptions()
    {
        if (options.isEmpty())
        {
            allowRequired = allowOption = allowSubcommands = true;
            return;
        }

        SerializableData last = options.get(options.size() - 1);
        allowOption = last instanceof OptionData;
        allowRequired = allowOption && ((OptionData) last).isRequired();
        allowSubcommands = !allowOption;
    }
}
