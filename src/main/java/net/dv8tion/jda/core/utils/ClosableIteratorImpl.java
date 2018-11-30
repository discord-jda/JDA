/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.utils;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;

public class ClosableIteratorImpl<T> implements ClosableIterator<T>
{
    private final static Logger log = JDALogger.getLog(ClosableIterator.class);
    private final Iterator<T> it;
    private Lock lock;

    public ClosableIteratorImpl(Iterator<T> it, Lock lock)
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
    public void remove()
    {
        throw new IllegalStateException("Cannot remove from this iterator!");
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

    @Override
    public T next()
    {
        if (lock == null)
            throw new NoSuchElementException();
        return it.next();
    }

    @Override
    @Deprecated
    protected void finalize()
    {
        if (lock != null)
        {
            log.error("Finalizing without closing, performing force close on lock");
            close();
        }
    }
}
