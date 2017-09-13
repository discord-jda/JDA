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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectedCacheViewImpl<T extends ISnowflake, E extends SnowflakeCacheView<T>> implements SnowflakeCacheView<T>
{
    protected final Supplier<Stream<E>> generator;

    public ProjectedCacheViewImpl(
        Supplier<Stream<E>> generator)
    {
        this.generator = generator;
    }

    @Override
    public List<T> asList()
    {
        List<T> list = new ArrayList<>();
        generator.get().forEach(view -> view.forEach(list::add));
        return Collections.unmodifiableList(list);
    }

    @Override
    public Set<T> asSet()
    {
        Set<T> set = new HashSet<>();
        generator.get().forEach(view -> view.forEach(set::add));
        return Collections.unmodifiableSet(set);
    }

    @Override
    public long size()
    {
        return generator.get().mapToLong(SnowflakeCacheView::size).sum();
    }

    @Override
    public boolean isEmpty()
    {
        return !generator.get().findAny().isPresent();
    }

    @Override
    public List<T> getElementsByName(String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(generator.get()
                .flatMap(view -> view.getElementsByName(name, ignoreCase).stream())
                .collect(Collectors.toList()));
    }

    @Override
    public T getElementById(long id)
    {
        return generator.get()
                        .map(view -> view.getElementById(id))
                        .filter(Objects::nonNull)
                        .findFirst().orElse(null);
    }
}
