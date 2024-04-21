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
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer
import net.dv8tion.jda.api.managers.channel.ChannelManager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager abstraction to configure settings related to thread channel containers, such as [ForumChannel].
 *
 * @param <T> The channel type
 * @param <M> The manager type
</M></T> */
interface IThreadContainerManager<T : IThreadContainer?, M : IThreadContainerManager<T, M>?> : ChannelManager<T, M> {
    /**
     * Sets the **<u>default thread slowmode</u>** of the selected channel.
     * This is applied to newly created threads by default.
     * <br></br>Provide `0` to disable slowmode.
     *
     *
     * A channel default thread slowmode **must not** be negative nor greater than [ISlowmodeChannel.MAX_SLOWMODE]!
     *
     *
     * Note: Bots are unaffected by this.
     * <br></br>Having [MESSAGE_MANAGE][Permission.MESSAGE_MANAGE] or
     * [MANAGE_CHANNEL][Permission.MANAGE_CHANNEL] permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     * The new default thread slowmode (in seconds)
     *
     * @throws IllegalArgumentException
     * If the provided slowmode is negative or greater than {@value ISlowmodeChannel#MAX_SLOWMODE}
     *
     * @return ChannelManager for chaining convenience
     *
     * @see IThreadContainer.getDefaultThreadSlowmode
     */
    @Nonnull
    @CheckReturnValue
    fun setDefaultThreadSlowmode(slowmode: Int): M
}
