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

package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.core.utils.ClosableIterator;
import net.dv8tion.jda.core.utils.cache.CacheView;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ChainedClosableIterator<T> implements ClosableIterator<T>
{
    private final static Logger log = JDALogger.getLog(ClosableIterator.class);
    private final Iterator<? extends CacheView<T>> generator;
    private ClosableIterator<T> currentIterator;

    public ChainedClosableIterator(Iterator<? extends CacheView<T>> generator)
    {
        this.generator = generator;
    }

    @Override
    public void close()
    {
        if (currentIterator != null)
            currentIterator.close();
        currentIterator = null;
    }

    @Override
    public boolean hasNext()
    {
        if (currentIterator != null && !currentIterator.hasNext())
        {
            currentIterator.close();
            currentIterator = null;
        }
        if (currentIterator == null)
        {
            CacheView<T> view = null;
            while (generator.hasNext())
            {
                view = generator.next();
                if (!view.isEmpty())
                    break;
                view = null;
            }
            if (view == null)
                return false;
            currentIterator = view.lockedIterator();
        }
        return true;
    }

    @Override
    public T next()
    {
        if (!hasNext())
            throw new NoSuchElementException();
        return currentIterator.next();
    }

    @Override
    @Deprecated
    protected void finalize()
    {
        if (currentIterator != null)
        {
            log.error("Finalizing without closing, performing force close on lock");
            close();
        }
    }
}
