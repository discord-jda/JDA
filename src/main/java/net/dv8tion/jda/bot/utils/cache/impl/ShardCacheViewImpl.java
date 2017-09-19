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
package net.dv8tion.jda.bot.utils.cache.impl;

import gnu.trove.impl.sync.TSynchronizedIntObjectMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.*;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.dv8tion.jda.bot.utils.cache.ShardCacheView;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.utils.Checks;

public class ShardCacheViewImpl implements ShardCacheView
{
    protected final TIntObjectMap<JDA> elements;

    public ShardCacheViewImpl()
    {
        this.elements = new TSynchronizedIntObjectMap<>(new TIntObjectHashMap<JDA>(), new Object());
    }

    public ShardCacheViewImpl(int initialCapacity)
    {
        this.elements = new TSynchronizedIntObjectMap<>(new TIntObjectHashMap<JDA>(initialCapacity), new Object());
    }

    public void clear()
    {
        elements.clear();
    }

    public TIntObjectMap<JDA> getMap()
    {
        return elements;
    }

    @Override
    public List<JDA> asList()
    {
        return Collections.unmodifiableList(new ArrayList<>(elements.valueCollection()));
    }

    @Override
    public Set<JDA> asSet()
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
    public List<JDA> getElementsByName(String name, boolean ignoreCase)
    {
        Checks.notEmpty(name, "Name");
        if (elements.isEmpty())
            return Collections.emptyList();

        List<JDA> list = new LinkedList<>();
        for (JDA elem : elements.valueCollection())
        {
            String elementName = elem.getShardInfo().getShardString();
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
    public Stream<JDA> stream()
    {
        return elements.valueCollection().stream();
    }

    @Override
    public Stream<JDA> parallelStream()
    {
        return elements.valueCollection().parallelStream();
    }

    @Nonnull
    @Override
    public Iterator<JDA> iterator()
    {
        return elements.valueCollection().iterator();
    }

    @Override
    public JDA getElementById(int id)
    {
        return this.elements.get(id);
    }

}
