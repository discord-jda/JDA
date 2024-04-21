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

import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import net.dv8tion.jda.api.managers.channel.ChannelManager
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager abstraction to set whether a channel is NSFW or Age-Restricted.
 *
 * @param <T> The channel type
 * @param <M> The manager type
</M></T> */
interface IAgeRestrictedChannelManager<T : IAgeRestrictedChannel?, M : IAgeRestrictedChannelManager<T, M>?> :
    ChannelManager<T, M> {
    /**
     * Sets the **<u>nsfw flag</u>** (also known as Age Restriction) of the selected [channel][IAgeRestrictedChannel].
     *
     * @param  nsfw
     * The new nsfw flag for the selected [channel][IAgeRestrictedChannel].
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setNSFW(nsfw: Boolean): M
}
