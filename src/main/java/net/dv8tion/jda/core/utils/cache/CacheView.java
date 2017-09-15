/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.utils.Checks;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface CacheView<T> extends Iterable<T>
{
    List<T> asList();

    Set<T> asSet();

    long size();

    boolean isEmpty();

    List<T> getElementsByName(String name, boolean ignoreCase);

    default List<T> getElementsByName(String name)
    {
        return getElementsByName(name, false);
    }

    Stream<T> stream();

    Stream<T> parallelStream();

    default <R, A> R collect(Collector<? super T, A, R> collector)
    {
        return stream().collect(collector);
    }


    static <E> CacheView<E> project(Collection<? extends CacheView<E>> cacheView)
    {
        Checks.noneNull(cacheView, "Collection");
        return new ProjectedCacheViewImpl<>(cacheView::stream);
    }

    static <E> CacheView<E> project(Supplier<Stream<CacheView<E>>> generator)
    {
        Checks.notNull(generator, "Generator");
        return new ProjectedCacheViewImpl<>(generator);
    }

    static <E extends ISnowflake> SnowflakeCacheView<E> projectSnowflake(Collection<SnowflakeCacheView<E>> cacheViews)
    {
        Checks.noneNull(cacheViews, "Collection");
        return new ProjectedCacheViewImpl.ProjectedSnowflakeCacheView<>(cacheViews::stream);
    }

    static <E extends ISnowflake> SnowflakeCacheView<E> projectSnowflake(Supplier<Stream<SnowflakeCacheView<E>>> generator)
    {
        Checks.notNull(generator, "Generator");
        return new ProjectedCacheViewImpl.ProjectedSnowflakeCacheView<>(generator);
    }

    static ProjectedMemberCacheView projectMember(Collection<MemberCacheView> cacheViews)
    {
        Checks.noneNull(cacheViews, "Collection");
        return new ProjectedCacheViewImpl.ProjectedMemberCacheViewImpl(cacheViews::stream);
    }

    static ProjectedMemberCacheView projectMember(Supplier<Stream<MemberCacheView>> generator)
    {
        Checks.notNull(generator, "Generator");
        return new ProjectedCacheViewImpl.ProjectedMemberCacheViewImpl(generator);
    }
}
