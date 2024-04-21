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

import net.dv8tion.jda.api.entities.ISnowflake
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream
import javax.annotation.Nonnull

/**
 * Specialized [CacheView] for entities that occur in a specified order.
 * <br></br>In this specialization [.forEach] will copy the underlying data store
 * in order to preserve order on iterations, use [.forEachUnordered] to avoid this overhead.
 *
 * @param <T>
 * The entity type
 *
 * @see CacheView CacheView for details on Efficient Memory Usage
</T> */
interface SortedSnowflakeCacheView<T> : SnowflakeCacheView<T> where T : Comparable<T>?, T : ISnowflake? {
    /**
     * Behavior similar to [CacheView.forEach] but does not preserve order.
     * <br></br>This will not copy the data store as sorting is not needed.
     *
     * @param  action
     * The action to perform
     *
     * @throws NullPointerException
     * If provided with null
     *
     * @since  4.0.0
     */
    override fun forEachUnordered(@Nonnull action: Consumer<in T>?)
    @Nonnull
    override fun asSet(): NavigableSet<T>?

    /**
     * Behavior similar to [CacheView.stream] which does not preserve order.
     *
     * @return Stream of the contained elements
     *
     * @since  4.0.0
     */
    @Nonnull
    fun streamUnordered(): Stream<T>?

    /**
     * Behavior similar to [CacheView.parallelStream] which does not preserve order.
     *
     * @return (Parallel) Stream of contained elements
     *
     * @since  4.0.0
     */
    @Nonnull
    fun parallelStreamUnordered(): Stream<T>?
}
