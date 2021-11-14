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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.channel.concrete.StoreChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import javax.annotation.Nonnull;

/**
 * Represents a Discord Store GuildChannel.
 *
 * @since  4.0.0
 *
 * @see   Guild#getStoreChannelCache()
 * @see   Guild#getStoreChannels()
 * @see   Guild#getStoreChannelsByName(String, boolean)
 * @see   Guild#getStoreChannelById(long)
 *
 * @see   JDA#getStoreChannelCache()
 * @see   JDA#getStoreChannels()
 * @see   JDA#getStoreChannelsByName(String, boolean)
 * @see   JDA#getStoreChannelById(long)
 */
//TODO-v5: We're probably going to remove this entity as Discord has deprecated it.
public interface StoreChannel extends GuildChannel, ICategorizableChannel, IPositionableChannel, ICopyableChannel, IPermissionContainer, IMemberContainer
{

    @Nonnull
    @Override
    ChannelAction<StoreChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    default ChannelAction<StoreChannel> createCopy()
    {
        return createCopy(getGuild());
    }

    @Override
    @Nonnull
    StoreChannelManager getManager();
}
