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
import net.dv8tion.jda.api.utils.ClosableIterator
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView
import net.dv8tion.jda.internal.utils.cache.ShardCacheViewImpl.UnifiedShardCacheViewImpl
import net.dv8tion.jda.internal.utils.cache.UnifiedCacheViewImpl
import net.dv8tion.jda.internal.utils.cache.UnifiedCacheViewImpl.UnifiedMemberCacheViewImpl
import net.dv8tion.jda.internal.utils.cache.UnifiedCacheViewImpl.UnifiedSnowflakeCacheView
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Stream
import java.util.stream.StreamSupport
import javax.annotation.Nonnull

/**
 * Read-only view on internal JDA cache of items.
 * <br></br>This can be useful to check information such as size without creating
 * an immutable snapshot first.
 *
 *
 * **Memory Efficient Usage**<br></br>
 * The [.forEach] method can be used to avoid creating a snapshot
 * of the backing data store, it is implemented by first acquiring a read-lock and then iterating the code.
 * The enhanced-for-loop uses the [.iterator] which has to first create a snapshot to avoid
 * concurrent modifications. Alternatively the [.lockedIterator] can be used to acquire an iterator
 * which holds a read-lock on the data store and thus prohibits concurrent modifications, for more details
 * read the documentation of [ClosableIterator]. Streams from [.stream]/[.parallelStream]
 * both use [.iterator] with a snapshot of the backing data store to avoid concurrent modifications.
 * <br></br>Using [.getElementsByName] is more efficient than [.asList] as it uses [.forEach]
 * for pattern matching and thus does not need to create a snapshot of the entire data store like [.asList] does.
 * <br></br>Both [.size] and [.isEmpty] are atomic operations.
 *
 *
 * Note that making a copy is a requirement if a specific order is desired. If using [.lockedIterator]
 * the order is not guaranteed as it directly iterates the backing cache.
 * Using [.forEach] on a [SortedSnowflakeCacheView] will copy the cache in order to sort
 * it, use [SortedSnowflakeCacheView.forEachUnordered] to avoid this overhead.
 * The backing cache is stored using an un-ordered hash map.
 *
 * @param  <T>
 * The cache type
</T> */
interface CacheView<T> : Iterable<T> {
    /**
     * Creates an immutable snapshot of the current cache state.
     * <br></br>This will copy all elements contained in this cache into a list.
     * <br></br>This will be sorted for a [SortedSnowflakeCacheView][SortedSnowflakeCacheViewImpl].
     *
     * @return Immutable list of cached elements
     */
    @Nonnull
    fun asList(): List<T>?

    /**
     * Creates an immutable snapshot of the current cache state.
     * <br></br>This will copy all elements contained in this cache into a set.
     *
     * @return Immutable set of cached elements
     */
    @Nonnull
    fun asSet(): Set<T>?

    /**
     * Returns an iterator with direct access to the underlying data store.
     * This iterator does not support removing elements.
     * <br></br>After usage this iterator should be closed to allow modifications by the library internals.
     *
     *
     * **Note: Order is not preserved in this iterator to be more efficient,
     * if order is desired use [.iterator] instead!**
     *
     * @return [ClosableIterator] holding a read-lock on the data structure.
     *
     * @since  4.0.0
     */
    @Nonnull
    fun lockedIterator(): ClosableIterator<T>

    /**
     * Behavior similar to [.forEach] but does not preserve order.
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
    fun forEachUnordered(@Nonnull action: Consumer<in T>?) {
        forEach(action)
    }

    /**
     * Creates an unordered sequenced stream of the elements in this cache.
     * <br></br>This does not copy the backing cache prior to consumption unlike [.stream].
     *
     *
     * The stream will be closed once this method returns and cannot be used anymore.
     *
     *
     * **Example**<br></br>
     * `
     * CacheView<User> view = jda.getUserCache();<br></br>
     * long shortNames = view.applyStream(stream -> stream.filter(it -> it.getName().length() < 4).count());<br></br>
     * System.out.println(shortNames + " users with less than 4 characters in their name");
    ` *
     *
     * @param  action
     * The action to perform on the stream
     * @param  <R>
     * The return type after performing the specified action
     *
     * @throws IllegalArgumentException
     * If the action is null
     *
     * @return The resulting value after the action was performed
     *
     * @since  4.0.0
     *
     * @see .acceptStream
    </R> */
    fun <R> applyStream(@Nonnull action: Function<in Stream<T>?, out R>): R {
        Checks.notNull(action, "Action")
        lockedIterator().use { it ->
            val spliterator = Spliterators.spliterator(it, size(), Spliterator.IMMUTABLE or Spliterator.NONNULL)
            val stream = StreamSupport.stream(spliterator, false)
            return action.apply(stream)
        }
    }

    /**
     * Creates an unordered sequenced stream of the elements in this cache.
     * <br></br>This does not copy the backing cache prior to consumption unlike [.stream].
     *
     *
     * The stream will be closed once this method returns and cannot be used anymore.
     *
     *
     * **Example**<br></br>
     * `
     * CacheView<TextChannel> view = guild.getTextChannelCache();<br></br>
     * view.acceptStream(stream -> stream.filter(it -> it.isNSFW()).forEach(it -> it.sendMessage("lewd").queue()));
    ` *
     *
     * @param  action
     * The action to perform on the stream
     *
     * @throws IllegalArgumentException
     * If the action is null
     *
     * @since  4.0.0
     *
     * @see .applyStream
     */
    fun acceptStream(@Nonnull action: Consumer<in Stream<T>?>) {
        Checks.notNull(action, "Action")
        lockedIterator().use { it ->
            val spliterator = Spliterators.spliterator(it, size(), Spliterator.IMMUTABLE or Spliterator.NONNULL)
            val stream = StreamSupport.stream(spliterator, false)
            action.accept(stream)
        }
    }

    /**
     * The current size of this cache
     * <br></br>This is a `long` as it may be a projected view of multiple caches
     * (See [net.dv8tion.jda.api.utils.cache.CacheView.all])
     *
     *
     * This is more efficient than creating a list or set snapshot first as it checks the size
     * of the internal cache directly.
     *
     * @return The current size of this cache
     */
    fun size(): Long

    /**
     * Whether the cache is empty
     *
     *
     * This is more efficient than creating a list or set snapshot first as it checks the size
     * of the internal cache directly.
     * <br></br>On a projected cache view this will simply look through all projected views and return false
     * the moment it finds one that is not empty.
     *
     * @return True, if this cache is currently empty
     */
    @JvmField
    val isEmpty: Boolean

    /**
     * Creates an immutable list of all elements matching the given name.
     * <br></br>For a [MemberCacheView][net.dv8tion.jda.api.utils.cache.MemberCacheView] this will
     * check the [Effective Name][net.dv8tion.jda.api.entities.Member.getEffectiveName] of the cached members.
     *
     * @param  name
     * The name to check
     * @param  ignoreCase
     * Whether to ignore case when comparing names
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided name is `null`
     *
     * @return Immutable list of elements with the given name
     */
    @Nonnull
    fun getElementsByName(@Nonnull name: String?, ignoreCase: Boolean): List<T>?

    /**
     * Creates an immutable list of all elements matching the given name.
     * <br></br>For a [MemberCacheView][net.dv8tion.jda.api.utils.cache.MemberCacheView] this will
     * check the [Effective Name][net.dv8tion.jda.api.entities.Member.getEffectiveName] of the cached members.
     *
     * @param  name
     * The name to check
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided name is `null`
     *
     * @return Immutable list of elements with the given name
     */
    @Nonnull
    fun getElementsByName(@Nonnull name: String?): List<T>? {
        return getElementsByName(name, false)
    }

    /**
     * Creates a [Stream][java.util.stream.Stream] of all cached elements.
     * <br></br>This will be sorted for a [SortedSnowflakeCacheView][SortedSnowflakeCacheViewImpl].
     *
     * @return Stream of elements
     */
    @Nonnull
    fun stream(): Stream<T>

    /**
     * Creates a parallel [Stream][java.util.stream.Stream] of all cached elements.
     * <br></br>This will be sorted for a [SortedSnowflakeCacheView][SortedSnowflakeCacheViewImpl].
     *
     * @return Parallel Stream of elements
     */
    @Nonnull
    fun parallelStream(): Stream<T>?

    /**
     * Collects all cached entities into a single Collection using the provided
     * [Collector][java.util.stream.Collector].
     * Shortcut for `stream().collect(collector)`.
     *
     * @param  collector
     * The collector used to collect the elements
     *
     * @param  <R>
     * The output type
     * @param  <A>
     * The accumulator type
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided collector is `null`
     *
     * @return Resulting collections
    </A></R> */
    @Nonnull
    fun <R, A> collect(@Nonnull collector: Collector<in T, A, R>?): R {
        return stream().collect(collector)
    }

    /**
     * Basic implementation of [CacheView][net.dv8tion.jda.api.utils.cache.CacheView] interface.
     * <br></br>Using [TLongObjectMap][gnu.trove.map.TLongObjectMap] to cache entities!
     *
     * @param <T>
     * The type this should cache
    </T> */
    class SimpleCacheView<T>(@Nonnull type: Class<T>?, nameMapper: Function<T, String?>?) :
        AbstractCacheView<T>(type, nameMapper)

    companion object {
        /**
         * Creates a combined [CacheView][net.dv8tion.jda.api.utils.cache.CacheView]
         * for all provided CacheView implementations. This allows to combine cache of multiple
         * JDA sessions or Guilds.
         *
         * @param  cacheViews
         * Collection of [CacheView][net.dv8tion.jda.api.utils.cache.CacheView] implementations
         *
         * @param  <E>
         * The target type of the projection
         *
         * @return Combined CacheView spanning over all provided implementation instances
        </E> */
        @Nonnull
        fun <E> all(@Nonnull cacheViews: Collection<CacheView<E>?>): CacheView<E>? {
            Checks.noneNull(cacheViews, "Collection")
            return UnifiedCacheViewImpl { cacheViews.stream() }
        }

        /**
         * Creates a combined [CacheView][net.dv8tion.jda.api.utils.cache.CacheView]
         * for all provided CacheView implementations. This allows to combine cache of multiple
         * JDA sessions or Guilds.
         *
         * @param  generator
         * Stream generator of [CacheView][net.dv8tion.jda.api.utils.cache.CacheView] implementations
         *
         * @param  <E>
         * The target type of the projection
         *
         * @return Combined CacheView spanning over all provided implementation instances
        </E> */
        @Nonnull
        fun <E> all(@Nonnull generator: Supplier<out Stream<out CacheView<E>>?>?): CacheView<E>? {
            Checks.notNull(generator, "Generator")
            return UnifiedCacheViewImpl(generator)
        }

        /**
         * Creates a combined [ShardCacheView]
         * for all provided ShardCacheView implementations.
         *
         * @param  cacheViews
         * Collection of [ShardCacheView] implementations
         *
         * @return Combined ShardCacheView spanning over all provided implementation instances
         */
        @Nonnull
        fun allShards(@Nonnull cacheViews: Collection<ShardCacheView?>): ShardCacheView? {
            Checks.noneNull(cacheViews, "Collection")
            return UnifiedShardCacheViewImpl { cacheViews.stream() }
        }

        /**
         * Creates a combined [ShardCacheView]
         * for all provided ShardCacheView implementations.
         *
         * @param  generator
         * Stream generator of [ShardCacheView] implementations
         *
         * @return Combined ShardCacheView spanning over all provided implementation instances
         */
        @Nonnull
        fun allShards(@Nonnull generator: Supplier<out Stream<out ShardCacheView?>?>?): ShardCacheView? {
            Checks.notNull(generator, "Generator")
            return UnifiedShardCacheViewImpl(generator)
        }

        /**
         * Creates a combined [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
         * for all provided SnowflakeCacheView implementations.
         * <br></br>This allows to combine cache of multiple JDA sessions or Guilds.
         *
         * @param  cacheViews
         * Collection of [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] implementations
         *
         * @param  <E>
         * The target type of the chain
         *
         * @return Combined SnowflakeCacheView spanning over all provided implementation instances
        </E> */
        @Nonnull
        fun <E : ISnowflake?> allSnowflakes(@Nonnull cacheViews: Collection<SnowflakeCacheView<E>?>): SnowflakeCacheView<E>? {
            Checks.noneNull(cacheViews, "Collection")
            return UnifiedSnowflakeCacheView { cacheViews.stream() }
        }

        /**
         * Creates a combined [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView]
         * for all provided SnowflakeCacheView implementations.
         * <br></br>This allows to combine cache of multiple JDA sessions or Guilds.
         *
         * @param  generator
         * Stream generator of [SnowflakeCacheView][net.dv8tion.jda.api.utils.cache.SnowflakeCacheView] implementations
         *
         * @param  <E>
         * The target type of the chain
         *
         * @return Combined SnowflakeCacheView spanning over all provided implementation instances
        </E> */
        @JvmStatic
        @Nonnull
        fun <E : ISnowflake?> allSnowflakes(@Nonnull generator: Supplier<out Stream<out SnowflakeCacheView<E>?>?>?): SnowflakeCacheView<E>? {
            Checks.notNull(generator, "Generator")
            return UnifiedSnowflakeCacheView(generator)
        }

        /**
         * Creates a combined [UnifiedMemberCacheView]
         * for all provided MemberCacheView implementations.
         * <br></br>This allows to combine cache of multiple JDA sessions or Guilds.
         *
         * @param  cacheViews
         * Collection of [MemberCacheView][net.dv8tion.jda.api.utils.cache.MemberCacheView] instances
         *
         * @return Combined MemberCacheView spanning over all provided instances
         */
        @Nonnull
        fun allMembers(@Nonnull cacheViews: Collection<MemberCacheView?>): UnifiedMemberCacheView? {
            Checks.noneNull(cacheViews, "Collection")
            return UnifiedMemberCacheViewImpl { cacheViews.stream() }
        }

        /**
         * Creates a combined [UnifiedMemberCacheView]
         * for all provided MemberCacheView implementations.
         * <br></br>This allows to combine cache of multiple JDA sessions or Guilds.
         *
         * @param  generator
         * Stream generator of [MemberCacheView][net.dv8tion.jda.api.utils.cache.MemberCacheView] instances
         *
         * @return Combined MemberCacheView spanning over all provided instances
         */
        @Nonnull
        fun allMembers(@Nonnull generator: Supplier<out Stream<out MemberCacheView?>?>?): UnifiedMemberCacheView? {
            Checks.notNull(generator, "Generator")
            return UnifiedMemberCacheViewImpl(generator)
        }
    }
}
