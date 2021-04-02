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

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.function.LongFunction;

@ForRemoval
@Deprecated
public class SnowflakeReference<T extends ISnowflake> implements ISnowflake
{
    private final LongFunction<T> fallbackProvider;
    private final long id;

    //We intentionally use a WeakReference rather than a SoftReference:
    // The reasoning is that we want to replace an old reference as soon as possible with a more up-to-date instance.
    // A soft reference would not be released until the user stops using it (ideally) so that is the wrong reference to use.
    private WeakReference<T> reference;

    public SnowflakeReference(T referent, LongFunction<T> fallback)
    {
        this.fallbackProvider = fallback;
        this.reference = new WeakReference<>(referent);
        this.id = referent.getIdLong();
    }

    @Nonnull
    public T resolve()
    {
        T referent = reference.get();
        if (referent == null)
        {
            referent = fallbackProvider.apply(id);
            if (referent == null)
                throw new IllegalStateException("Cannot get reference as it has already been Garbage Collected");
            reference = new WeakReference<>(referent);
        }
        return referent;
    }

    @Override
    public int hashCode()
    {
        return resolve().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return resolve().equals(obj);
    }

    @Override
    public String toString()
    {
        return resolve().toString();
    }

    @Override
    public long getIdLong()
    {
        return id;
    }
}
