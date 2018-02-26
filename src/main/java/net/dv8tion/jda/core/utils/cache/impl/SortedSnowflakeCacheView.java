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

package net.dv8tion.jda.core.utils.cache.impl;

import net.dv8tion.jda.core.entities.ISnowflake;
import org.apache.commons.collections4.iterators.ObjectArrayIterator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class SortedSnowflakeCacheView<T extends ISnowflake & Comparable<T>> extends SnowflakeCacheViewImpl<T>
{
    protected static final int SPLIT_CHARACTERISTICS = Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.NONNULL;

    protected final Comparator<T> comparator;

    public SortedSnowflakeCacheView(Class<T> type, Comparator<T> comparator)
    {
        this(type, null, comparator);
    }

    public SortedSnowflakeCacheView(Class<T> type, Function<T, String> nameMapper, Comparator<T> comparator)
    {
        super(type, nameMapper);
        this.comparator = comparator;
    }

    @Override
    public List<T> asList()
    {
        List<T> list = new ArrayList<>(elements.size());
        elements.forEachValue(list::add);
        list.sort(comparator);
        return Collections.unmodifiableList(list);
    }

    @Override
    public SortedSet<T> asSet()
    {
        SortedSet<T> set = new TreeSet<>(comparator);
        elements.forEachValue(set::add);
        return Collections.unmodifiableSortedSet(set);
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return Spliterators.spliterator(iterator(), elements.size(), SPLIT_CHARACTERISTICS);
    }

    @Override
    public Stream<T> stream()
    {
        return super.stream().sorted(comparator);
    }

    @Override
    public Stream<T> parallelStream()
    {
        return super.parallelStream().sorted(comparator);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator()
    {
        T[] arr = elements.values(emptyArray);
        Arrays.sort(arr, comparator);
        return new ObjectArrayIterator<>(arr);
    }
}
