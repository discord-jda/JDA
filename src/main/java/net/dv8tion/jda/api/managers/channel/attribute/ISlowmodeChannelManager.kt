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

import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager which supports setting slowmode of a channel.
 *
 * @param <T>
 * The concrete [ISlowmodeChannel] type
 * @param <M>
 * The concrete manager type
</M></T> */
interface ISlowmodeChannelManager<T : ISlowmodeChannel?, M : ISlowmodeChannelManager<T, M>?> : ChannelManager<T, M> {
    /**
     * Sets the **<u>slowmode</u>** of the selected channel.
     * <br></br>Provide `0` to disable slowmode.
     *
     *
     * A channel slowmode **must not** be negative nor greater than [ISlowmodeChannel.MAX_SLOWMODE]!
     *
     *
     * Note: Bots are unaffected by this.
     * <br></br>Having [MESSAGE_MANAGE][Permission.MESSAGE_MANAGE] or
     * [MANAGE_CHANNEL][Permission.MANAGE_CHANNEL] permission also
     * grants immunity to slowmode.
     *
     *
     * **Special case**<br></br>
     * [ForumChannels][net.dv8tion.jda.api.entities.channel.concrete.ForumChannel] use this to limit how many posts a user can create.
     * The client refers to this as the post slowmode.
     *
     * @param  slowmode
     * The new slowmode
     *
     * @throws IllegalArgumentException
     * If the provided slowmode is negative or greater than {@value ISlowmodeChannel#MAX_SLOWMODE}
     *
     * @return ChannelManager for chaining convenience
     *
     * @see net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel.getSlowmode
     */
    @Nonnull
    @CheckReturnValue
    fun setSlowmode(slowmode: Int): M
}
