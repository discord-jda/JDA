/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.internal.utils.UnlockHook;
import org.apache.commons.collections4.iterators.ObjectArrayIterator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class SortedSnowflakeCacheViewImpl<T extends ISnowflake & Comparable<? super T>>
        extends SnowflakeCacheViewImpl<T> implements SortedSnowflakeCacheView<T>
{
    protected static final int SPLIT_CHARACTERISTICS = Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.NONNULL;

    protected final Comparator<T> comparator;

    public SortedSnowflakeCacheViewImpl(Class<T> type, Comparator<T> comparator)
    {
        this(type, null, comparator);
    }

    public SortedSnowflakeCacheViewImpl(Class<T> type, Function<T, String> nameMapper, Comparator<T> comparator)
    {
        super(type, nameMapper);
        this.comparator = comparator;
    }

    @Override
    public void forEach(@Nonnull Consumer<? super T> action)
    {
        try (UnlockHook hook = readLock())
        {
            iterator().forEachRemaining(action);
        }
    }

    @Override
    public void forEachUnordered(@Nonnull Consumer<? super T> action)
    {
        super.forEach(action);
    }

    @Nonnull
    @Override
    public List<T> asList()
    {
        if (isEmpty())
            return Collections.emptyList();
        try (UnlockHook hook = readLock())
        {
            List<T> list = getCachedList();
            if (list != null)
                return list;
            list = new ArrayList<>(elements.size());
            elements.forEachValue(list::add);
            list.sort(comparator);
            return cache(list);
        }
    }

    @Nonnull
    @Override
    public NavigableSet<T> asSet()
    {
        if (isEmpty())
            return Collections.emptyNavigableSet();
        try (UnlockHook hook = readLock())
        {
            NavigableSet<T> set = (NavigableSet<T>) getCachedSet();
            if (set != null)
                return set;
            set = new TreeSet<>(comparator);
            elements.forEachValue(set::add);
            return cache(set);
        }
    }

    @Nonnull
    @Override
    public List<T> getElementsByName(@Nonnull String name, boolean ignoreCase)
    {
        List<T> filtered = super.getElementsByName(name, ignoreCase);
        filtered.sort(comparator);
        return filtered;
    }

    @Override
    public Spliterator<T> spliterator()
    {
        try (UnlockHook hook = readLock())
        {
            return Spliterators.spliterator(iterator(), elements.size(), SPLIT_CHARACTERISTICS);
        }
    }

    @Nonnull
    @Override
    public Stream<T> streamUnordered()
    {
        return super.stream();
    }

    @Nonnull
    @Override
    public Stream<T> parallelStreamUnordered()
    {
        return super.parallelStream();
    }

    @Nonnull
    @Override
    public Stream<T> stream()
    {
        return super.stream().sorted(comparator);
    }

    @Nonnull
    @Override
    public Stream<T> parallelStream()
    {
        return super.parallelStream().sorted(comparator);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator()
    {
        try (UnlockHook hook = readLock())
        {
            T[] arr = elements.values(emptyArray);
            Arrays.sort(arr, comparator);
            return new ObjectArrayIterator<>(arr);
        }
    }
}
