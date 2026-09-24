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

import javax.annotation.Nonnull

object UnionUtil {
    @Nonnull
    @JvmStatic
    fun <T> safeUnionCast(
        @Nonnull classCategory: String,
        @Nonnull instance: Any,
        @Nonnull toObjectClass: Class<T>,
    ): T {
        if (toObjectClass.isInstance(instance)) {
            return toObjectClass.cast(instance)
        }

        val cleanedClassName = instance.javaClass.simpleName.replace("Impl", "")
        throw IllegalStateException(
            Helpers.format(
                "Cannot convert %s of type %s to %s!",
                classCategory,
                cleanedClassName,
                toObjectClass.simpleName,
            ),
        )
    }
}
