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
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.command.localization.LocalizationMapper;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CommandDataImpl implements SlashCommandData
{
    protected final DataArray options = DataArray.empty();
    protected String name, description = "";
    private LocalizationMapper localizationMapper;
    private final LocalizationMap nameLocalizations = new LocalizationMap(this::checkName);
    private final LocalizationMap descriptionLocalizations = new LocalizationMap(this::checkDescription);

    private boolean allowSubcommands = true;
    private boolean allowGroups = true;
    private boolean allowOption = true;
    private boolean allowRequired = true;
    private boolean guildOnly = false;
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
        Checks.notEmpty(description, "Description");
        Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        if (localizationMapper != null) localizationMapper.localizeCommand(this, options);

        DataObject json = DataObject.empty()
                .put("type", type.getId())
                .put("name", name)
                .put("options", options)
                .put("dm_permission", !guildOnly)
                .put("default_member_permissions", defaultMemberPermissions == DefaultMemberPermissions.ENABLED
                        ? null
                        : Long.toUnsignedString(defaultMemberPermissions.getPermissionsRaw()))
                .put("name_localizations", nameLocalizations)
                .put("options", options);
        if (type == Command.Type.SLASH)
            json.put("description", description)
                .put("description_localizations", descriptionLocalizations);
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
        return guildOnly;
    }

    @Nonnull
    @Override
    public List<SubcommandData> getSubcommands()
    {
        return options.stream(DataArray::getObject)
                .filter(obj ->
                {
                    OptionType type = OptionType.fromKey(obj.getInt("type"));
                    return type == OptionType.SUB_COMMAND;
                })
                .map(SubcommandData::fromData)
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public List<SubcommandGroupData> getSubcommandGroups()
    {
        return options.stream(DataArray::getObject)
                .filter(obj ->
                {
                    OptionType type = OptionType.fromKey(obj.getInt("type"));
                    return type == OptionType.SUB_COMMAND_GROUP;
                })
                .map(SubcommandGroupData::fromData)
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
        this.guildOnly = guildOnly;
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
        Checks.check(options.length + this.options.length() <= 25, "Cannot have more than 25 options for a command!");
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

        allowSubcommands = allowGroups = false;
        this.allowRequired = allowRequired;
        for (OptionData option : options)
            this.options.add(option);
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
        Checks.check(subcommands.length + options.length() <= 25, "Cannot have more than 25 subcommands for a command!");
        Checks.checkUnique(
            Stream.concat(getSubcommands().stream(), Arrays.stream(subcommands)).map(SubcommandData::getName),
            "Cannot have multiple subcommands with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count }
        );

        allowOption = false;
        for (SubcommandData data : subcommands)
            options.add(data);
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
        if (!allowGroups)
            throw new IllegalArgumentException("You cannot mix options with subcommands/groups.");
        Checks.check(groups.length + options.length() <= 25, "Cannot have more than 25 subcommand groups for a command!");
        Checks.checkUnique(
            Stream.concat(getSubcommandGroups().stream(), Arrays.stream(groups)).map(SubcommandGroupData::getName),
            "Cannot have multiple subcommand groups with the same name. Name: \"%s\" appeared %d times!",
            (count, value) -> new Object[]{ value, count }
        );

        allowOption = false;
        for (SubcommandGroupData data : groups)
            options.add(data);
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

    @Nonnull
    @Override
    public List<OptionData> getOptions()
    {
        return options.stream(DataArray::getObject)
                .map(OptionData::fromData)
                .filter(it -> it.getType().getKey() > OptionType.SUB_COMMAND_GROUP.getKey())
                .collect(Helpers.toUnmodifiableList());
    }
}
