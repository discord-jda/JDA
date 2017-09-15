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

package net.dv8tion.jda.core.utils.cache.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.cache.CacheView;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractCacheView<T> implements CacheView<T>
{
    protected final TLongObjectMap<T> elements = MiscUtil.newLongMap();
    protected final Function<T, String> nameMapper;

    protected AbstractCacheView(Function<T, String> nameMapper)
    {
        this.nameMapper = nameMapper;
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
        return Collections.unmodifiableList(new ArrayList<>(elements.valueCollection()));
    }

    @Override
    public Set<T> asSet()
    {
        return Collections.unmodifiableSet(new HashSet<>(elements.valueCollection()));
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

        List<T> list = new LinkedList<>();
        for (T elem : elements.valueCollection())
        {
            String elementName = nameMapper.apply(elem);
            if (elementName != null)
            {
                if (ignoreCase)
                {
                    if (elementName.equalsIgnoreCase(name))
                        list.add(elem);
                }
                else
                {
                    if (elementName.equals(name))
                        list.add(elem);
                }
            }
        }

        return list;
    }

    @Override
    public Stream<T> stream()
    {
        return elements.valueCollection().stream();
    }

    @Override
    public Stream<T> parallelStream()
    {
        return elements.valueCollection().parallelStream();
    }

    @Nonnull
    @Override
    public Iterator<T> iterator()
    {
        return elements.valueCollection().iterator();
    }
}
