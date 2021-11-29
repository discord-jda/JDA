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

package net.dv8tion.jda.api.managers.channel.attribute;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IPositionableChannel;
import net.dv8tion.jda.api.managers.channel.ChannelManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface IPositionableChannelManager<T extends IPositionableChannel, M extends IPositionableChannelManager<T, M>> extends ChannelManager<T, M>
{
    /**
     * Sets the <b><u>position</u></b> of the selected {@link GuildChannel GuildChannel}.
     *
     * <p><b>To modify multiple channels you should use
     * <code>Guild.{@link Guild#modifyTextChannelPositions() modifyTextChannelPositions()}</code>
     * instead! This is not the same as looping through channels and using this to update positions!</b>
     *
     * @param  position
     *         The new position for the selected {@link GuildChannel GuildChannel}
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    M setPosition(int position);
}
