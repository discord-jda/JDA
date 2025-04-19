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
import net.dv8tion.jda.api.interactions.commands.build.PrimaryEntryPointCommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public class PrimaryEntryPointCommandDataImpl
        extends CommandDataImpl
        implements PrimaryEntryPointCommandData
{
    private Handler handler;

    public PrimaryEntryPointCommandDataImpl(@Nonnull String name, @Nonnull String description)
    {
        super(Command.Type.PRIMARY_ENTRY_POINT, name, description);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return super.toData()
                .put("handler", handler.getValue());
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setDefaultPermissions(@Nonnull DefaultMemberPermissions permissions)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setDefaultPermissions(permissions);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setGuildOnly(boolean guildOnly)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setGuildOnly(guildOnly);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setContexts(@Nonnull InteractionContextType... contexts)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setContexts(contexts);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setContexts(@Nonnull Collection<InteractionContextType> contexts)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setContexts(contexts);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setIntegrationTypes(@Nonnull IntegrationType... integrationTypes)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setIntegrationTypes(integrationTypes);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setIntegrationTypes(@Nonnull Collection<IntegrationType> integrationTypes)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setIntegrationTypes(integrationTypes);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setNSFW(boolean nsfw)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setNSFW(nsfw);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setLocalizationFunction(@Nonnull LocalizationFunction localizationFunction) {
        return (PrimaryEntryPointCommandDataImpl) super.setLocalizationFunction(localizationFunction);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setName(@Nonnull String name)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setName(name);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setNameLocalization(@Nonnull DiscordLocale locale, @Nonnull String name)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setNameLocalization(locale, name);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setNameLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setNameLocalizations(map);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setDescription(@Nonnull String description)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setDescription(description);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setDescriptionLocalization(@Nonnull DiscordLocale locale, @Nonnull String description)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setDescriptionLocalization(locale, description);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setDescriptionLocalizations(@Nonnull Map<DiscordLocale, String> map)
    {
        return (PrimaryEntryPointCommandDataImpl) super.setDescriptionLocalizations(map);
    }

    @Nonnull
    @Override
    public PrimaryEntryPointCommandDataImpl setHandler(@Nonnull Handler handler)
    {
        this.handler = handler;
        return this;
    }
}
