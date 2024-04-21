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
package net.dv8tion.jda.api.managers.channel.attribute

import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager abstraction to modify the position of a [positionable channel][IPositionableChannel].
 *
 * @param <T> The channel type
 * @param <M> The manager type
</M></T> */
interface IPositionableChannelManager<T : IPositionableChannel?, M : IPositionableChannelManager<T, M>?> :
    ChannelManager<T, M> {
    /**
     * Sets the **<u>position</u>** of the selected [GuildChannel].
     *
     *
     * **To modify multiple channels you should use
     * `Guild.[modifyTextChannelPositions()][Guild.modifyTextChannelPositions]`
     * instead! This is not the same as looping through channels and using this to update positions!**
     *
     * @param  position
     * The new position for the selected [GuildChannel]
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setPosition(position: Int): M
}
