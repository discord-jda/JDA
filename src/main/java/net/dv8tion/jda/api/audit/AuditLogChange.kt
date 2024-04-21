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
package net.dv8tion.jda.api.audit

import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * Plain-Old-Java-Object (POJO) representing a single
 * change for an [AuditLogEntry][net.dv8tion.jda.api.audit.AuditLogEntry]!
 * <br></br>This object holds the [old-][.getOldValue] and [new value][.getNewValue] for the
 * updated field. The field is specified by the [key][.getKey].
 */
class AuditLogChange(
    protected val oldValue: Any, protected val newValue: Any,
    /**
     * The key which defines the field that was updated
     * by this change
     *
     * @return The key
     */
    @JvmField @get:Nonnull val key: String
) {

    /**
     * The previous value for the field specified by [.getKey].
     *
     * @param  <T>
     * The expected generic type for this value.
     * <br></br>This will be used to cast the value.
     *
     * @throws java.lang.ClassCastException
     * If the type cast to the generic type fails
     *
     * @return The old value
    </T> */
    fun <T> getOldValue(): T? {
        return oldValue as T
    }

    /**
     * The updated value for the field specified by [.getKey].
     *
     * @param  <T>
     * The expected generic type for this value.
     * <br></br>This will be used to cast the value.
     *
     * @throws java.lang.ClassCastException
     * If the type cast to the generic type fails
     *
     * @return The new value
    </T> */
    fun <T> getNewValue(): T? {
        return newValue as T
    }

    override fun hashCode(): Int {
        return Objects.hash(key, oldValue, newValue)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is AuditLogChange) return false
        val other = obj
        return other.key == key && other.oldValue == oldValue && other.newValue == newValue
    }

    override fun toString(): String {
        return EntityString(this)
            .setName(key)
            .addMetadata(null, "$oldValue -> $newValue")
            .toString()
    }
}
