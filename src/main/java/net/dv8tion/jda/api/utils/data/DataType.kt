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
package net.dv8tion.jda.api.utils.data

import javax.annotation.Nonnull

/**
 * Enum constants representing possible types for a [net.dv8tion.jda.api.utils.data.DataObject] value.
 */
enum class DataType {
    INT,
    FLOAT,
    STRING,
    OBJECT,
    ARRAY,
    BOOLEAN,
    NULL,
    UNKNOWN;

    /**
     * Tests whether the type for the provided value is
     * the one represented by this enum constant.
     *
     * @param  value
     * The value to check
     *
     * @return True, if the value is of this type
     */
    fun isType(value: Any?): Boolean {
        return when (this) {
            INT -> value is Int || value is Long || value is Short || value is Byte
            FLOAT -> value is Double || value is Float
            STRING -> value is String
            BOOLEAN -> value is Boolean
            ARRAY -> value is List<*>
            OBJECT -> value is Map<*, *>
            NULL -> value == null
            else -> false
        }
    }

    companion object {
        /**
         * Assumes the type of the provided value through instance checks.
         *
         * @param  value
         * The value to test
         *
         * @return The DataType constant or [.UNKNOWN]
         */
        @Nonnull
        fun getType(value: Any?): DataType {
            for (type in entries) {
                if (type.isType(value)) return type
            }
            return UNKNOWN
        }
    }
}
