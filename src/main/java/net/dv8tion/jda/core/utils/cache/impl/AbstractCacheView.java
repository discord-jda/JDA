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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.cache.CacheView;
import org.apache.commons.collections4.iterators.ObjectArrayIterator;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractCacheView<T> implements CacheView<T>
{
    protected final TLongObjectMap<T> elements = MiscUtil.newLongMap();
    protected final T[] emptyArray;
    protected final Function<T, String> nameMapper;
    protected final Class<T> type;

    @SuppressWarnings("unchecked")
    protected AbstractCacheView(Class<T> type, Function<T, String> nameMapper)
    {
        this.nameMapper = nameMapper;
        this.type = type;
        this.emptyArray = (T[]) Array.newInstance(type, 0);
    }

    public void clear()
    {
        elements.clear();
    }

    public TLongObjectMap<T> getMap()
    {
        return elements;
    }

    @Override
    public List<T> asList()
    {
        ArrayList<T> list = new ArrayList<>(elements.size());
        elements.forEachValue(list::add);
        return Collections.unmodifiableList(list);
    }

    @Override
    public Set<T> asSet()
    {
        HashSet<T> set = new HashSet<>(elements.size());
        elements.forEachValue(set::add);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public long size()
    {
        return elements.size();
    }

    @Override
    public boolean isEmpty()
    {
        return elements.isEmpty();
    }

    @Override
    public List<T> getElementsByName(String name, boolean ignoreCase)
    {
        Checks.notEmpty(name, "Name");
        if (elements.isEmpty())
            return Collections.emptyList();
        if (nameMapper == null) // no getName method available
            throw new UnsupportedOperationException("The contained elements are not assigned with names.");

        List<T> list = new ArrayList<>();
        for (T elem : this)
        {
            String elementName = nameMapper.apply(elem);
            if (elementName != null && equals(ignoreCase, elementName, name))
                list.add(elem);
        }

        return list;
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return Spliterators.spliterator(elements.values(), Spliterator.IMMUTABLE);
    }

    @Override
    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Stream<T> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator()
    {
        return new ObjectArrayIterator<>(elements.values(emptyArray));
    }

    @SuppressWarnings("StringEquality")
    protected boolean equals(boolean ignoreCase, String first, String second)
    {
        return first == second || ignoreCase ? first.equalsIgnoreCase(second) : first.equals(second);
    }
}
