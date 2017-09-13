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
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface SnowflakeCacheView<T extends ISnowflake> extends Iterable<T>
{
    List<T> asList();

    Set<T> asSet();

    long size();

    boolean isEmpty();

    default List<T> getElementsByName(String name)
    {
        return getElementsByName(name, false);
    }

    List<T> getElementsByName(String name, boolean ignoreCase);

    T getElementById(long id);

    default T getElementById(String id)
    {
        return getElementById(MiscUtil.parseSnowflake(id));
    }

    default Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    default Stream<T> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    @Override
    default CacheIterator<T> iterator()
    {
        return new CacheIterator<>(this);
    }

    class CacheIterator<V extends ISnowflake> implements Iterator<V>
    {
        protected final List<V> list;
        protected int index = 0;

        public CacheIterator(SnowflakeCacheView<V> view)
        {
            this.list = view.asList();
        }

        @Override
        public boolean hasNext()
        {
            return index < list.size();
        }

        @Override
        public V next()
        {
            if (!hasNext())
                throw new NoSuchElementException("Reached end of iteration.");
            return list.get(index++);
        }
    }
}
