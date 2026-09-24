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

package net.dv8tion.jda.internal.utils

import net.dv8tion.jda.api.entities.ISnowflake
import java.util.StringJoiner
import javax.annotation.Nonnull
import javax.annotation.Nullable

class EntityString(
    private val entity: Any,
) {
    private var type: Any? = null
    private var name: String? = null
    private var metadata: MutableList<String>? = null

    fun setType(
        @Nonnull type: Enum<*>,
    ): EntityString {
        this.type = type.name
        return this
    }

    fun setType(
        @Nonnull type: Any,
    ): EntityString {
        this.type = type
        return this
    }

    fun setName(
        @Nonnull name: String,
    ): EntityString {
        this.name = name
        return this
    }

    fun addMetadata(
        @Nullable key: String?,
        @Nullable value: Any?,
    ): EntityString {
        if (metadata == null) {
            metadata = ArrayList()
        }

        metadata!!.add(if (key == null) value.toString() else "$key=$value")

        return this
    }

    @Nonnull
    override fun toString(): String {
        val entityName =
            when (entity) {
                is String -> entity
                is Class<*> -> getCleanedClassName(entity)
                else -> getCleanedClassName(entity.javaClass)
            }

        val sb = StringBuilder(entityName)
        if (type != null) {
            sb.append('[').append(type).append(']')
        }
        if (name != null) {
            sb.append(':').append(name)
        }

        val isSnowflake = entity is ISnowflake
        if (isSnowflake || metadata != null) {
            val metadataJoiner = StringJoiner(", ", "(", ")")
            if (isSnowflake) {
                metadataJoiner.add("id=" + entity.getId())
            }
            if (metadata != null) {
                for (metadataItem in metadata!!) {
                    metadataJoiner.add(metadataItem)
                }
            }

            sb.append(metadataJoiner)
        }

        return sb.toString()
    }

    companion object {
        @Nonnull
        private fun getCleanedClassName(
            @Nonnull clazz: Class<*>,
        ): String {
            val packageName = clazz.`package`.name
            val fullName = clazz.name
            val simpleName = fullName.substring(packageName.length + 1)

            return simpleName
                .replace("$", ".") // Clean up nested classes
                .replace("Impl", "") // Don't expose Impl
        }
    }
}
