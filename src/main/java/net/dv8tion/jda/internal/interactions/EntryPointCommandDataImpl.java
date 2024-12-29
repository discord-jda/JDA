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
import net.dv8tion.jda.api.interactions.commands.build.EntryPointCommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.command.localization.LocalizationMapper;
import net.dv8tion.jda.internal.interactions.mixin.attributes.IDescribedCommandDataMixin;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Map;

public class EntryPointCommandDataImpl implements EntryPointCommandData, IDescribedCommandDataMixin
{
    protected String name, description = "";
    private LocalizationMapper localizationMapper;
    private final LocalizationMap nameLocalizations = new LocalizationMap(this::checkName);
    private final LocalizationMap descriptionLocalizations = new LocalizationMap(this::checkDescription);

    private boolean guildOnly = false;
    private boolean nsfw = false;
    private DefaultMemberPermissions defaultMemberPermissions = DefaultMemberPermissions.ENABLED;
    private Handler handler;

    public EntryPointCommandDataImpl(@Nonnull String name, @Nonnull String description)
    {
        setName(name);
        setDescription(description);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        if (localizationMapper != null) localizationMapper.localizeCommand(this, DataArray.empty());

        return DataObject.empty()
                .put("type", getType().getId())
                .put("handler", handler.getValue())
                .put("name", name)
                .put("description", description)
                .put("description_localizations", descriptionLocalizations)
                .put("nsfw", nsfw)
                .put("dm_permission", !guildOnly)
                .put("default_member_permissions", defaultMemberPermissions == DefaultMemberPermissions.ENABLED
                        ? null
                        : Long.toUnsignedString(defaultMemberPermissions.getPermissionsRaw()))
                .put("name_localizations", nameLocalizations);
    }

    @Nonnull
    @Override
    public Command.Type getType()
    {
        return Command.Type.PRIMARY_ENTRY_POINT;
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

    @Override
    public boolean isNSFW()
    {
        return nsfw;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setDefaultPermissions(@Nonnull DefaultMemberPermissions permissions)
    {
        Checks.notNull(permissions, "Permissions");
        this.defaultMemberPermissions = permissions;
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setGuildOnly(boolean guildOnly)
    {
        this.guildOnly = guildOnly;
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setNSFW(boolean nsfw)
    {
        this.nsfw = nsfw;
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction) {
        Checks.notNull(localizationFunction, "Localization function");

        this.localizationMapper = LocalizationMapper.fromFunction(localizationFunction);
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setName(@Nonnull String name)
    {
        checkName(name);
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
    {
        //Checks are done in LocalizationMap
        nameLocalizations.setTranslation(locale, name);
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        nameLocalizations.setTranslations(map);
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setDescription(@Nonnull String description)
    {
        checkDescription(description);
        this.description = description;
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description)
    {
        //Checks are done in LocalizationMap
        descriptionLocalizations.setTranslation(locale, description);
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandDataImpl setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        descriptionLocalizations.setTranslations(map);
        return this;
    }

    @Nonnull
    @Override
    public EntryPointCommandData setHandler(@Nonnull Handler handler)
    {
        this.handler = handler;
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
}
