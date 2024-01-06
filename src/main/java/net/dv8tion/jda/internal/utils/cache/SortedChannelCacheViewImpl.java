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

package net.dv8tion.jda.internal.utils.cache;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.utils.cache.SortedChannelCacheView;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.UnlockHook;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SortedChannelCacheViewImpl<T extends Channel & Comparable<? super T>> extends ChannelCacheViewImpl<T> implements SortedChannelCacheView<T>
{
    public SortedChannelCacheViewImpl(Class<T> type)
    {
        super(type);
    }

    @Nonnull
    @Override
    public <C extends T> SortedFilteredCacheView<C> ofType(@Nonnull Class<C> type)
    {
        return new SortedFilteredCacheView<>(type);
    }

    @Nonnull
    @Override
    public List<T> asList()
    {
        List<T> list = getCachedList();
        if (list == null)
            list = cache(new ArrayList<>(asSet()));
        return list;
    }

    @Nonnull
    @Override
    public NavigableSet<T> asSet()
    {
        NavigableSet<T> set = (NavigableSet<T>) getCachedSet();
        if (set == null)
            set = cache((NavigableSet<T>) applyStream(stream -> stream.collect(Collectors.toCollection(TreeSet::new))));
        return set;
    }

    @Override
    public void forEachUnordered(@Nonnull Consumer<? super T> action)
    {
        super.forEach(action);
    }

    @Override
    public void forEach(@Nonnull Consumer<? super T> action)
    {
        asSet().forEach(action);
    }

    @Nonnull
    @Override
    public List<T> getElementsByName(@Nonnull String name)
    {
        List<T> elements = super.getElementsByName(name);
        elements.sort(Comparator.naturalOrder());
        return elements;
    }

    @Nonnull
    @Override
    public Stream<T> streamUnordered()
    {
        try (UnlockHook hook = readLock())
        {
            return caches.values().stream().flatMap(cache -> cache.valueCollection().stream()).collect(Collectors.toList()).stream();
        }
    }

    @Nonnull
    @Override
    public Stream<T> parallelStreamUnordered()
    {
        return streamUnordered().parallel();
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return asSet().spliterator();
    }

    @Nonnull
    @Override
    public Iterator<T> iterator()
    {
        return asSet().iterator();
    }

    public class SortedFilteredCacheView<C extends T> extends FilteredCacheView<C> implements SortedChannelCacheView<C>
    {
        protected SortedFilteredCacheView(Class<C> type)
        {
            super(type);
        }

        @Nonnull
        @Override
        public List<C> asList()
        {
            return applyStream(stream ->
                stream
                    .sorted()
                    .collect(Helpers.toUnmodifiableList())
            );
        }

        @Nonnull
        @Override
        public NavigableSet<C> asSet()
        {
            return applyStream(stream ->
                stream.collect(
                    Collectors.collectingAndThen(
                        Collectors.toCollection(TreeSet::new),
                        Collections::unmodifiableNavigableSet))
            );
        }

        @Nonnull
        @Override
        public List<C> getElementsByName(@Nonnull String name, boolean ignoreCase)
        {
            Checks.notEmpty(name, "Name");
            return applyStream(stream ->
                stream
                    .filter(it -> Helpers.equals(name, it.getName(), ignoreCase))
                    .sorted()
                    .collect(Helpers.toUnmodifiableList())
            );
        }

        @Nonnull
        @Override
        public Stream<C> streamUnordered()
        {
            List<C> elements = applyStream(stream -> stream.filter(type::isInstance).collect(Collectors.toList()));
            return elements.stream();
        }

        @Nonnull
        @Override
        public Stream<C> parallelStreamUnordered()
        {
            return stream().parallel();
        }

        @Nonnull
        @Override
        public <C1 extends C> SortedChannelCacheView<C1> ofType(@Nonnull Class<C1> type)
        {
            return SortedChannelCacheViewImpl.this.ofType(type);
        }

        @Override
        public void forEachUnordered(@Nonnull Consumer<? super C> action)
        {
            super.forEach(action);
        }

        @Override
        public void forEach(Consumer<? super C> action)
        {
            stream().forEach(action);
        }
    }
}
