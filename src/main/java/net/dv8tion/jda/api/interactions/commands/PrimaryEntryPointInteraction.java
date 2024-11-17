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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.commands.build.EntryPointCommandData;
import net.dv8tion.jda.api.requests.restaction.GlobalCommandListUpdateAction;

import javax.annotation.Nonnull;

/**
 * Interaction to launch your app's activity.
 *
 * <p><b>Note:</b> This interaction requires <a href="https://discord.com/developers/docs/activities/overview" target="_blank">activities</a> to be enabled,
 * and an {@link GlobalCommandListUpdateAction#setEntryPointCommand(EntryPointCommandData) entry point}
 * with its {@link EntryPointCommandData#setHandler(EntryPointCommandData.Handler) handler}
 * set to {@link EntryPointCommandData.Handler#APP_HANDLER APP_HANDLER} to be configured.
 */
public interface PrimaryEntryPointInteraction extends CommandInteraction
{
    /**
     * The respective {@link MessageChannelUnion} for this interaction.
     *
     * @return The {@link MessageChannelUnion}
     */
    @Nonnull
    @Override
    MessageChannelUnion getChannel();

    @Nonnull
    @Override
    default GuildMessageChannelUnion getGuildChannel()
    {
        return (GuildMessageChannelUnion) CommandInteraction.super.getGuildChannel();
    }
}
