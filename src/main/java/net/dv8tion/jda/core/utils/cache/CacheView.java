/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.utils.cache;

import net.dv8tion.jda.bot.utils.cache.ShardCacheView;
import net.dv8tion.jda.bot.utils.cache.impl.ShardCacheViewImpl;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.cache.impl.AbstractCacheView;
import net.dv8tion.jda.core.utils.cache.impl.UnifiedCacheViewImpl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Read-only view on internal JDA cache of items.
 * <br>This can be useful to check information such as size without creating
 * an immutable snapshot first.
 *
 * @param  <T>
 *         The cache type
 */
public interface CacheView<T> extends Iterable<T>
{
    /**
     * Creates an immutable snapshot of the current cache state.
     * <br>This will copy all elements contained in this cache into a list.
     * <br>This will be sorted for a {@link net.dv8tion.jda.core.utils.cache.impl.SortedSnowflakeCacheView SortedSnowflakeCacheView}.
     *
     * @return Immutable list of cached elements
     */
    List<T> asList();

    /**
     * Creates an immutable snapshot of the current cache state.
     * <br>This will copy all elements contained in this cache into a set.
     *
     * @return Immutable set of cached elements
     */
    Set<T> asSet();

    /**
     * The current size of this cache
     * <br>This is a {@code long} as it may be a projected view of multiple caches
     * (See {@link net.dv8tion.jda.core.utils.cache.CacheView#all(java.util.function.Supplier)})
     *
     * <p>This is more efficient than creating a list or set snapshot first as it checks the size
     * of the internal cache directly.
     *
     * @return The current size of this cache
     */
    long size();

    /**
     * Whether the cache is empty
     *
     * <p>This is more efficient than creating a list or set snapshot first as it checks the size
     * of the internal cache directly.
     * <br>On a projected cache view this will simply look through all projected views and return false
     * the moment it finds one that is not empty.
     *
     * @return True, if this cache is currently empty
     */
    boolean isEmpty();

    /**
     * Creates an immutable list of all elements matching the given name.
     * <br>For a {@link net.dv8tion.jda.core.utils.cache.MemberCacheView MemberCacheView} this will
     * check the {@link net.dv8tion.jda.core.entities.Member#getEffectiveName() Effective Name} of the cached members.
     *
     * @param  name
     *         The name to check
     * @param  ignoreCase
     *         Whether to ignore case when comparing names
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is {@code null}
     *
     * @return Immutable list of elements with the given name
     */
    List<T> getElementsByName(String name, boolean ignoreCase);

    /**
     * Creates an immutable list of all elements matching the given name.
     * <br>For a {@link net.dv8tion.jda.core.utils.cache.MemberCacheView MemberCacheView} this will
     * check the {@link net.dv8tion.jda.core.entities.Member#getEffectiveName() Effective Name} of the cached members.
     *
     * @param  name
     *         The name to check
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is {@code null}
     *
     * @return Immutable list of elements with the given name
     */
    default List<T> getElementsByName(String name)
    {
        return getElementsByName(name, false);
    }

    /**
     * Creates a {@link java.util.stream.Stream Stream} of all cached elements.
     * <br>This will be sorted for a {@link net.dv8tion.jda.core.utils.cache.impl.SortedSnowflakeCacheView SortedSnowflakeCacheView}.
     *
     * @return Stream of elements
     */
    Stream<T> stream();

    /**
     * Creates a parallel {@link java.util.stream.Stream Stream} of all cached elements.
     * <br>This will be sorted for a {@link net.dv8tion.jda.core.utils.cache.impl.SortedSnowflakeCacheView SortedSnowflakeCacheView}.
     *
     * @return Parallel Stream of elements
     */
    Stream<T> parallelStream();

    /**
     * Collects all cached entities into a single Collection using the provided
     * {@link java.util.stream.Collector Collector}.
     * Shortcut for {@code stream().collect(collector)}.
     *
     * @param  collector
     *         The collector used to collect the elements
     *
     * @param  <R>
     *         The output type
     * @param  <A>
     *         The accumulator type
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided collector is {@code null}
     *
     * @return Resulting collections
     */
    default <R, A> R collect(Collector<? super T, A, R> collector)
    {
        return stream().collect(collector);
    }

    /**
     * Creates a combined {@link net.dv8tion.jda.core.utils.cache.CacheView CacheView}
     * for all provided CacheView implementations. This allows to combine cache of multiple
     * JDA sessions or Guilds.
     *
     * @param  cacheViews
     *         Collection of {@link net.dv8tion.jda.core.utils.cache.CacheView CacheView} implementations
     *
     * @param  <E>
     *         The target type of the projection
     *
     * @return Combined CacheView spanning over all provided implementation instances
     */
    static <E> CacheView<E> all(Collection<? extends CacheView<E>> cacheViews)
    {
        Checks.noneNull(cacheViews, "Collection");
        return new UnifiedCacheViewImpl<>(cacheViews::stream);
    }

    /**
     * Creates a combined {@link net.dv8tion.jda.core.utils.cache.CacheView CacheView}
     * for all provided CacheView implementations. This allows to combine cache of multiple
     * JDA sessions or Guilds.
     *
     * @param  generator
     *         Stream generator of {@link net.dv8tion.jda.core.utils.cache.CacheView CacheView} implementations
     *
     * @param  <E>
     *         The target type of the projection
     *
     * @return Combined CacheView spanning over all provided implementation instances
     */
    static <E> CacheView<E> all(Supplier<Stream<CacheView<E>>> generator)
    {
        Checks.notNull(generator, "Generator");
        return new UnifiedCacheViewImpl<>(generator);
    }

    /**
     * Creates a combined {@link net.dv8tion.jda.bot.utils.cache.ShardCacheView ShardCacheView}
     * for all provided ShardCacheView implementations.
     *
     * @param  cacheViews
     *         Collection of {@link net.dv8tion.jda.bot.utils.cache.ShardCacheView ShardCacheView} implementations
     *
     * @return Combined ShardCacheView spanning over all provided implementation instances
     */
    static ShardCacheView allShards(Collection<ShardCacheView> cacheViews)
    {
        Checks.noneNull(cacheViews, "Collection");
        return new ShardCacheViewImpl.UnifiedShardCacheViewImpl(cacheViews::stream);
    }

    /**
     * Creates a combined {@link net.dv8tion.jda.bot.utils.cache.ShardCacheView ShardCacheView}
     * for all provided ShardCacheView implementations.
     *
     * @param  generator
     *         Stream generator of {@link net.dv8tion.jda.bot.utils.cache.ShardCacheView ShardCacheView} implementations
     *
     * @return Combined ShardCacheView spanning over all provided implementation instances
     */
    static ShardCacheView allShards(Supplier<Stream<ShardCacheView>> generator)
    {
        Checks.notNull(generator, "Generator");
        return new ShardCacheViewImpl.UnifiedShardCacheViewImpl(generator);
    }

    /**
     * Creates a combined {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     * for all provided SnowflakeCacheView implementations.
     * <br>This allows to combine cache of multiple JDA sessions or Guilds.
     *
     * @param  cacheViews
     *         Collection of {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} implementations
     *
     * @param  <E>
     *         The target type of the chain
     *
     * @return Combined SnowflakeCacheView spanning over all provided implementation instances
     */
    static <E extends ISnowflake> SnowflakeCacheView<E> allSnowflakes(Collection<SnowflakeCacheView<E>> cacheViews)
    {
        Checks.noneNull(cacheViews, "Collection");
        return new UnifiedCacheViewImpl.UnifiedSnowflakeCacheView<>(cacheViews::stream);
    }

    /**
     * Creates a combined {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     * for all provided SnowflakeCacheView implementations.
     * <br>This allows to combine cache of multiple JDA sessions or Guilds.
     *
     * @param  generator
     *         Stream generator of {@link net.dv8tion.jda.core.utils.cache.SnowflakeCacheView SnowflakeCacheView} implementations
     *
     * @param  <E>
     *         The target type of the chain
     *
     * @return Combined SnowflakeCacheView spanning over all provided implementation instances
     */
    static <E extends ISnowflake> SnowflakeCacheView<E> allSnowflakes(Supplier<Stream<SnowflakeCacheView<E>>> generator)
    {
        Checks.notNull(generator, "Generator");
        return new UnifiedCacheViewImpl.UnifiedSnowflakeCacheView<>(generator);
    }

    /**
     * Creates a combined {@link UnifiedMemberCacheView UnifiedMemberCacheView}
     * for all provided MemberCacheView implementations.
     * <br>This allows to combine cache of multiple JDA sessions or Guilds.
     *
     * @param  cacheViews
     *         Collection of {@link net.dv8tion.jda.core.utils.cache.MemberCacheView MemberCacheView} instances
     *
     * @return Combined MemberCacheView spanning over all provided instances
     */
    static UnifiedMemberCacheView allMembers(Collection<MemberCacheView> cacheViews)
    {
        Checks.noneNull(cacheViews, "Collection");
        return new UnifiedCacheViewImpl.UnifiedMemberCacheViewImpl(cacheViews::stream);
    }

    /**
     * Creates a combined {@link UnifiedMemberCacheView UnifiedMemberCacheView}
     * for all provided MemberCacheView implementations.
     * <br>This allows to combine cache of multiple JDA sessions or Guilds.
     *
     * @param  generator
     *         Stream generator of {@link net.dv8tion.jda.core.utils.cache.MemberCacheView MemberCacheView} instances
     *
     * @return Combined MemberCacheView spanning over all provided instances
     */
    static UnifiedMemberCacheView allMembers(Supplier<Stream<MemberCacheView>> generator)
    {
        Checks.notNull(generator, "Generator");
        return new UnifiedCacheViewImpl.UnifiedMemberCacheViewImpl(generator);
    }

    /**
     * Basic implementation of {@link net.dv8tion.jda.core.utils.cache.CacheView CacheView} interface.
     * <br>Using {@link gnu.trove.map.TLongObjectMap TLongObjectMap} to cache entities!
     *
     * @param <T>
     *        The type this should cache
     */
    class SimpleCacheView<T> extends AbstractCacheView<T>
    {
        public SimpleCacheView(Class<T> type, Function<T, String> nameMapper)
        {
            super(type, nameMapper);
        }
    }
}
