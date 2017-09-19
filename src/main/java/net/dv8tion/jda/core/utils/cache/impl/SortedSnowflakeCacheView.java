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

import net.dv8tion.jda.core.entities.ISnowflake;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class SortedSnowflakeCacheView<T extends ISnowflake & Comparable<T>> extends SnowflakeCacheViewImpl<T>
{
    protected final Comparator<T> comparator;

    public SortedSnowflakeCacheView(Comparator<T> comparator)
    {
        this(null, comparator);
    }

    public SortedSnowflakeCacheView(Function<T, String> nameMapper, Comparator<T> comparator)
    {
        super(nameMapper);
        this.comparator = comparator;
    }

    @Override
    public List<T> asList()
    {
        List<T> list = new ArrayList<>(elements.valueCollection());
        list.sort(comparator);
        return Collections.unmodifiableList(list);
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
    public Iterator<T> iterator()
    {
        return asList().iterator();
    }
}
