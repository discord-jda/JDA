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

package net.dv8tion.jda.api.entities.channel.middleman;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.attribute.*;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.middleman.StandardGuildChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Represents a standard {@link GuildChannel} which are the "<i>normal</i>" channels that are present in the channel sidebar.
 * <br>They include functionality "expected" of normal Discord channels like
 * {@link IPermissionContainer permissions},
 * {@link IInviteContainer invite support},
 * {@link IPositionableChannel positioning},
 * the ability {@link ICategorizableChannel to be categorized},
 * and more.
 *
 * @see GuildMessageChannel
 * @see TextChannel
 * @see NewsChannel
 * @see StageChannel
 * @see VoiceChannel
 */
public interface StandardGuildChannel extends GuildChannel, IPermissionContainer, IPositionableChannel, ICopyableChannel, IMemberContainer, IInviteContainer, ICategorizableChannel
{
    @Nonnull
    @Override
    StandardGuildChannelManager<?, ?> getManager();

    @Override
    @Nonnull
    @CheckReturnValue
    ChannelAction<? extends StandardGuildChannel> createCopy(@Nonnull Guild guild);

    @Override
    @Nonnull
    @CheckReturnValue
    ChannelAction<? extends StandardGuildChannel> createCopy();
}
