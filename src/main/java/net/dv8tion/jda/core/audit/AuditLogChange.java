/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.audit;

import java.util.Objects;

/**
 * Plain-Old-Java-Object (POJO) representing a single
 * change for an {@link net.dv8tion.jda.core.audit.AuditLogEntry AuditLogEntry}!
 * <br>This object holds the {@link #getOldValue() old-} and {@link #getNewValue() new value} for the
 * updated field. The field is specified by the {@link #getKey() key}.
 */
public class AuditLogChange
{
    protected final Object oldValue;
    protected final Object newValue;
    protected final String key;

    public AuditLogChange(Object oldValue, Object newValue, String key)
    {
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.key = key;
    }

    /**
     * The previous value for the field specified by {@link #getKey()}.
     *
     * @param  <T>
     *         The expected generic type for this value.
     *         <br>This will be used to cast the value.
     *
     * @throws java.lang.ClassCastException
     *         If the type cast to the generic type fails
     *
     * @return The old value
     */
    @SuppressWarnings("unchecked")
    public <T> T getOldValue()
    {
        return (T) oldValue;
    }

    /**
     * The updated value for the field specified by {@link #getKey()}.
     *
     * @param  <T>
     *         The expected generic type for this value.
     *         <br>This will be used to cast the value.
     *
     * @throws java.lang.ClassCastException
     *         If the type cast to the generic type fails
     *
     * @return The new value
     */
    @SuppressWarnings("unchecked")
    public <T> T getNewValue()
    {
        return (T) newValue;
    }

    /**
     * The key which defines the field that was updated
     * by this change
     *
     * @return The key
     */
    public String getKey()
    {
        return key;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, oldValue, newValue);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AuditLogChange))
            return false;
        AuditLogChange other = (AuditLogChange) obj;
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
