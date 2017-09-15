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
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectedCacheViewImpl<T, E extends CacheView<T>> implements CacheView<T>
{
    protected final Supplier<Stream<E>> generator;

    public ProjectedCacheViewImpl(Supplier<Stream<E>> generator)
    {
        this.generator = generator;
    }

    @Override
    public long size()
    {
        return generator.get().mapToLong(CacheView::size).sum();
    }

    @Override
    public boolean isEmpty()
    {
        return !generator.get().findAny().isPresent();
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
    public List<T> getElementsByName(String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(generator.get()
                .flatMap(view -> view.getElementsByName(name, ignoreCase).stream())
                .collect(Collectors.toList()));
    }

    @Override
    public Stream<T> stream()
    {
        return generator.get().flatMap(CacheView::stream);
    }

    @Override
    public Stream<T> parallelStream()
    {
        return generator.get().flatMap(CacheView::parallelStream);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator()
    {
        return asList().iterator();
    }

    public static class ProjectedSnowflakeCacheView<T extends ISnowflake>
        extends ProjectedCacheViewImpl<T, SnowflakeCacheView<T>> implements SnowflakeCacheView<T>
    {
        public ProjectedSnowflakeCacheView(Supplier<Stream<SnowflakeCacheView<T>>> generator)
        {
            super(generator);
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

    public static class ProjectedMemberCacheViewImpl
        extends ProjectedCacheViewImpl<Member, MemberCacheView> implements ProjectedMemberCacheView
    {

        public ProjectedMemberCacheViewImpl(Supplier<Stream<MemberCacheView>> generator)
        {
            super(generator);
        }

        @Override
        public List<Member> getElementsById(long id)
        {
            return Collections.unmodifiableList(generator.get()
                .map(view -> view.getElementById(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        }

        @Override
        public List<Member> getElementsByUsername(String name, boolean ignoreCase)
        {
            return Collections.unmodifiableList(generator.get()
                .flatMap(view -> view.getElementsByUsername(name, ignoreCase).stream())
                .collect(Collectors.toList()));
        }

        @Override
        public List<Member> getElementsByNickname(String name, boolean ignoreCase)
        {
            return Collections.unmodifiableList(generator.get()
                .flatMap(view -> view.getElementsByNickname(name, ignoreCase).stream())
                .collect(Collectors.toList()));
        }

        @Override
        public List<Member> getElementsWithRoles(Role... roles)
        {
            return Collections.unmodifiableList(generator.get()
                .flatMap(view -> view.getElementsWithRoles(roles).stream())
                .collect(Collectors.toList()));
        }

        @Override
        public List<Member> getElementsWithRoles(Collection<Role> roles)
        {
            return Collections.unmodifiableList(generator.get()
                .flatMap(view -> view.getElementsWithRoles(roles).stream())
                .collect(Collectors.toList()));
        }
    }
}
