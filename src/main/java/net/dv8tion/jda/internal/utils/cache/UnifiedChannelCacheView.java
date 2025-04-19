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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.utils.ClosableIterator;
import net.dv8tion.jda.api.utils.cache.ChannelCacheView;
import net.dv8tion.jda.internal.utils.ChainedClosableIterator;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnifiedChannelCacheView<C extends Channel> implements ChannelCacheView<C>
{
    private final Supplier<Stream<ChannelCacheView<C>>> supplier;

    public UnifiedChannelCacheView(Supplier<Stream<ChannelCacheView<C>>> supplier)
    {
        this.supplier = supplier;
    }

    @Override
    public void forEach(Consumer<? super C> action)
    {
        Objects.requireNonNull(action, "Consumer");
        try (ClosableIterator<C> iterator = lockedIterator())
        {
            while (iterator.hasNext())
                action.accept(iterator.next());
        }
    }

    @Nonnull
    @Override
    public List<C> asList()
    {
        return stream().collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Set<C> asSet()
    {
        return stream().collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    @Nonnull
    @Override
    public ClosableIterator<C> lockedIterator()
    {
        return new ChainedClosableIterator<>(supplier.get().iterator());
    }

    @Override
    public long size()
    {
        return supplier.get().mapToLong(ChannelCacheView::size).sum();
    }

    @Override
    public boolean isEmpty()
    {
        return supplier.get().allMatch(ChannelCacheView::isEmpty);
    }

    @Nonnull
    @Override
    public List<C> getElementsByName(@Nonnull String name, boolean ignoreCase)
    {
        return supplier.get()
                .flatMap(view -> view.getElementsByName(name, ignoreCase).stream())
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Stream<C> stream()
    {
        return supplier.get().flatMap(ChannelCacheView::stream);
    }

    @Nonnull
    @Override
    public Stream<C> parallelStream()
    {
        return supplier.get().parallel().flatMap(ChannelCacheView::parallelStream);
    }

    @Nonnull
    @Override
    public <T extends C> ChannelCacheView<T> ofType(@Nonnull Class<T> type)
    {
        Checks.notNull(type, "Type");
        return new UnifiedChannelCacheView<>(() -> supplier.get().map(view -> view.ofType(type)));
    }

    @Nullable
    @Override
    public C getElementById(@Nonnull ChannelType type, long id)
    {
        return supplier.get().map(view -> view.getElementById(type, id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    @Override
    public C getElementById(long id)
    {
        return supplier.get().map(view -> view.getElementById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Nonnull
    @Override
    public Iterator<C> iterator()
    {
        return stream().iterator();
    }
}
