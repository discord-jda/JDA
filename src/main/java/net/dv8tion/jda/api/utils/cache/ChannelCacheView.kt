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
package net.dv8tion.jda.api.utils.cache

import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.utils.MiscUtil
import javax.annotation.Nonnull

/**
 * Specialized [SnowflakeCacheView] type used for handling channels.
 * <br></br>This type caches all relevant channel types, including threads.
 *
 *
 * Internally, this cache view makes a distinction between the varying [ChannelTypes][ChannelType] and provides convenient methods to access a filtered subset.
 *
 * @param <T>
 * The channel type
</T> */
interface ChannelCacheView<T : Channel?> : SnowflakeCacheView<T> {
    /**
     * Creates a decorator around this cache, filtered to only provide access to the given type.
     *
     * @param type
     * The type class (Like `TextChannel.class`)
     * @param <C>
     * The type parameter
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The filtered cache view
    </C> */
    @Nonnull
    fun <C : T?> ofType(@Nonnull type: Class<C>?): ChannelCacheView<C>?

    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  type
     * The expected [ChannelType]
     * @param  id
     * The ID of the entity
     *
     * @return Possibly-null entity for the specified ID, null if the expected type is different from the actual type
     */
    fun getElementById(@Nonnull type: ChannelType?, id: Long): T?

    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  type
     * The expected [ChannelType]
     * @param  id
     * The ID of the entity
     *
     * @throws java.lang.NumberFormatException
     * If the provided String is `null` or
     * cannot be resolved to an unsigned long id
     *
     * @return Possibly-null entity for the specified ID, null if the expected type is different from the actual type
     */
    fun getElementById(@Nonnull type: ChannelType?, @Nonnull id: String?): T? {
        return getElementById(type, MiscUtil.parseSnowflake(id))
    }
}
