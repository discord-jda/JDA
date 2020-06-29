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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.utils.LockIterator;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.UnlockHook;
import org.apache.commons.collections4.iterators.ObjectArrayIterator;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractCacheView<T> extends ReadWriteLockCache<T> implements CacheView<T>
{
    protected final TLongObjectMap<T> elements = new TLongObjectHashMap<>();
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
        try (UnlockHook hook = writeLock())
        {
            elements.clear();
        }
    }

    public TLongObjectMap<T> getMap()
    {
        if (!lock.writeLock().isHeldByCurrentThread())
            throw new IllegalStateException("Cannot access map directly without holding write lock!");
        return elements;
    }

    public T get(long id)
    {
        try (UnlockHook hook = readLock())
        {
            return elements.get(id);
        }
    }

    public T remove(long id)
    {
        try (UnlockHook hook = writeLock())
        {
            return elements.remove(id);
        }
    }

    public TLongSet keySet()
    {
        try (UnlockHook hook = readLock())
        {
            return new TLongHashSet(elements.keySet());
        }
    }

    @Override
    public void forEach(Consumer<? super T> action)
    {
        Objects.requireNonNull(action);
        try (UnlockHook hook = readLock())
        {
            for (T elem : elements.valueCollection())
            {
                action.accept(elem);
            }
        }
    }

    @Nonnull
    @Override
    public LockIterator<T> lockedIterator()
    {
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.lock();
        try
        {
            Iterator<T> directIterator = elements.valueCollection().iterator();
            return new LockIterator<>(directIterator, readLock);
        }
        catch (Throwable t)
        {
            readLock.unlock();
            throw t;
        }
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
            return cache(list);
        }
    }

    @Nonnull
    @Override
    public Set<T> asSet()
    {
        if (isEmpty())
            return Collections.emptySet();
        try (UnlockHook hook = readLock())
        {
            Set<T> set = getCachedSet();
            if (set != null)
                return set;
            set = new HashSet<>(elements.size());
            elements.forEachValue(set::add);
            return cache(set);
        }
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

    @Nonnull
    @Override
    public List<T> getElementsByName(@Nonnull String name, boolean ignoreCase)
    {
        Checks.notEmpty(name, "Name");
        if (elements.isEmpty())
            return Collections.emptyList();
        if (nameMapper == null) // no getName method available
            throw new UnsupportedOperationException("The contained elements are not assigned with names.");
        if (isEmpty())
            return Collections.emptyList();
        List<T> list = new ArrayList<>();
        forEach(elem ->
        {
            String elementName = nameMapper.apply(elem);
            if (elementName != null && equals(ignoreCase, elementName, name))
                list.add(elem);
        });
        return list; // must be modifiable because of SortedSnowflakeCacheView
    }

    @Override
    public Spliterator<T> spliterator()
    {
        try (UnlockHook hook = readLock())
        {
            return Spliterators.spliterator(elements.values(), Spliterator.IMMUTABLE);
        }
    }

    @Nonnull
    @Override
    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    @Nonnull
    @Override
    public Stream<T> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator()
    {
        try (UnlockHook hook = readLock())
        {
            return new ObjectArrayIterator<>(elements.values(emptyArray));
        }
    }

    @Override
    public String toString()
    {
        return asList().toString();
    }

    @Override
    public int hashCode()
    {
        try (UnlockHook hook = readLock())
        {
            return elements.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof AbstractCacheView))
            return false;
        AbstractCacheView view = (AbstractCacheView) obj;
        try (UnlockHook hook = readLock(); UnlockHook otherHook = view.readLock())
        {
            return this.elements.equals(view.elements);
        }
    }

    protected boolean equals(boolean ignoreCase, String first, String second)
    {
        return ignoreCase ? first.equalsIgnoreCase(second) : first.equals(second);
    }
}
