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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;

/**
 * Simple implementation of a {@link ClosableIterator} that uses a lock.
 * <br>Close is an idempotent function and can be performed multiple times without effects beyond first invocation.
 * <br>This is deployed by {@link CacheView#lockedIterator()} to allow read-only access
 * to the underlying structure without the need to clone it, unlike the normal iterator.
 *
 * <p>This closes automatically when {@link #hasNext()} returns {@code false} but
 * its recommended to only be used within a {@code try-with-resources} block for safety.
 *
 * <h3>Example</h3>
 * This can handle any exceptions thrown while iterating and ensures the lock is released correctly.
 * <pre>{@code
 * try (ClosableIterator<T> it = cacheView.lockedIterator()) {
 *     while (it.hasNext()) {
 *         consume(it.next());
 *     }
 * }
 * }</pre>
 *
 * @param <T>
 *        The element type for this iterator
 *
 * @since  4.0.0
 */
public class LockIterator<T> implements ClosableIterator<T>
{
    private final static Logger log = JDALogger.getLog(ClosableIterator.class);
    private final Iterator<? extends T> it;
    private Lock lock;

    public LockIterator(@Nonnull Iterator<? extends T> it, Lock lock)
    {
        this.it = it;
        this.lock = lock;
    }

    @Override
    public void close()
    {
        if (lock != null)
            lock.unlock();
        lock = null;
    }

    @Override
    public boolean hasNext()
    {
        if (lock == null)
            return false;
        boolean hasNext = it.hasNext();
        if (!hasNext)
            close();
        return hasNext;
    }

    @Nonnull
    @Override
    public T next()
    {
        if (lock == null)
            throw new NoSuchElementException();
        return it.next();
    }

    @Override
    @Deprecated //Deprecated in Java 9 because the finalization system is being changed/removed
    protected void finalize()
    {
        if (lock != null)
        {
            log.error("Finalizing without closing, performing force close on lock");
            close();
        }
    }
}
