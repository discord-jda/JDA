/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.entities;

import java.util.Objects;

public class AuditLogChange<T>
{

    protected final T oldValue;
    protected final T newValue;
    protected final String key;

    public AuditLogChange(T oldValue, T newValue, String key)
    {
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.key = key;
    }

    public T getOldValue()
    {
        return oldValue;
    }

    public T getNewValue()
    {
        return newValue;
    }

    public String getKey()
    {
        return key;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AuditLogChange<?>))
            return false;
        AuditLogChange<?> other = (AuditLogChange<?>) obj;
        return other.key.equals(key)
                && Objects.equals(other.oldValue, oldValue)
                && Objects.equals(other.newValue, newValue);
    }

    @Override
    public String toString()
    {
        return String.format("ALC:%s(%s -> %s)", key, oldValue, newValue);
    }
}
