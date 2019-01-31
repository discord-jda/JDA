/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import javax.annotation.Nonnull;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class UpstreamReference<T> extends WeakReference<T>
{
    public UpstreamReference(T referent)
    {
        super(referent);
    }

    public UpstreamReference(T referent, ReferenceQueue<? super T> q)
    {
        super(referent, q);
    }

    @Nonnull
    @Override
    public T get()
    {
        T tmp = super.get();
        if (tmp == null)
            throw new IllegalStateException("Cannot get reference as it has already been Garbage Collected");
        return tmp;
    }

    @Override
    public int hashCode()
    {
        return get().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return get().equals(obj);
    }

    @Override
    public String toString()
    {
        return get().toString();
    }
}
