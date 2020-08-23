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

import net.dv8tion.jda.internal.utils.UnlockHook;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ReadWriteLockCache<T>
{
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected WeakReference<List<T>> cachedList;
    protected WeakReference<Set<T>>  cachedSet;

    public UnlockHook writeLock()
    {
        if (lock.getReadHoldCount() > 0)
            throw new IllegalStateException("Unable to acquire write-lock while holding read-lock!");
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        try
        {
            if (!writeLock.tryLock() && !writeLock.tryLock(10, TimeUnit.SECONDS))
                throw new IllegalStateException("Could not acquire write-lock in a reasonable timeframe! (10 seconds)");
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Unable to acquire write-lock while thread is interrupted!");
        }
        onAcquireWriteLock();
        clearCachedLists();
        return new UnlockHook(writeLock);
    }

    public UnlockHook readLock()
    {
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        try
        {
            if (!readLock.tryLock() && !readLock.tryLock(10, TimeUnit.SECONDS))
                throw new IllegalStateException("Could not acquire read-lock in a reasonable timeframe! (10 seconds)");
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Unable to acquire read-lock while thread is interrupted!");
        }
        onAcquireReadLock();
        return new UnlockHook(readLock);
    }

    public void clearCachedLists()
    {
        cachedList = null;
        cachedSet = null;
    }

    protected void onAcquireWriteLock() {}
    protected void onAcquireReadLock() {}

    protected List<T> getCachedList()
    {
        return cachedList == null ? null : cachedList.get();
    }

    protected Set<T> getCachedSet()
    {
        return cachedSet == null ? null : cachedSet.get();
    }

    protected List<T> cache(List<T> list)
    {
        list = Collections.unmodifiableList(list);
        cachedList = new WeakReference<>(list);
        return list;
    }

    protected Set<T> cache(Set<T> set)
    {
        set = Collections.unmodifiableSet(set);
        cachedSet = new WeakReference<>(set);
        return set;
    }

    protected NavigableSet<T> cache(NavigableSet<T> set)
    {
        set = Collections.unmodifiableNavigableSet(set);
        cachedSet = new WeakReference<>(set);
        return set;
    }
}
