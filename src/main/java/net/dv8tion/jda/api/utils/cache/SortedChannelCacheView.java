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

package net.dv8tion.jda.api.utils.cache;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import javax.annotation.Nonnull;

/**
 * Specialized {@link ChannelCacheView} type used for handling sorted lists of channels.
 * <br>Sorting is done with respect to the positioning in the official Discord client, by comparing positions and category information.
 *
 * <p>Internally, this cache view makes a distinction between the varying {@link ChannelType ChannelTypes} and provides convenient methods to access a filtered subset.
 *
 * @param <T>
 *        The channel type
 *
 * @see   Guild#getChannels()
 */
public interface SortedChannelCacheView<T extends Channel & Comparable<? super T>> extends ChannelCacheView<T>, SortedSnowflakeCacheView<T>
{
    @Nonnull
    <C extends T> SortedChannelCacheView<C> ofType(@Nonnull Class<C> type);
}
