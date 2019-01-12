/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.api.requests.restaction.order;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.managers.GuildController;

/**
 * Implementation of {@link OrderAction OrderAction}
 * to modify the order of {@link net.dv8tion.jda.api.entities.GuildChannel Channels} for a {@link net.dv8tion.jda.api.entities.Guild Guild}.
 * <br>To apply the changes you must finish the {@link net.dv8tion.jda.api.requests.RestAction RestAction}.
 *
 * <p>Before you can use any of the {@code move} methods
 * you must use either {@link #selectPosition(Object) selectPosition(GuildChannel)} or {@link #selectPosition(int)}!
 *
 * @param <T>
 *        The type of {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} defining
 *        which channels to order
 *
 * @since 3.0
 *
 * @see   GuildController
 * @see   GuildController#modifyTextChannelPositions()
 * @see   GuildController#modifyVoiceChannelPositions()
 * @see   GuildController#modifyCategoryPositions()
 * @see   CategoryOrderAction
 */
public interface ChannelOrderAction<T extends GuildChannel> extends OrderAction<T, ChannelOrderAction<T>>
{
    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} which holds
     * the channels from {@link #getCurrentOrder()}
     *
     * @return The corresponding {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    Guild getGuild();

    /**
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} of
     * all channels that are ordered by this ChannelOrderAction
     *
     * @return The corresponding {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
     */
    ChannelType getChannelType();
}
