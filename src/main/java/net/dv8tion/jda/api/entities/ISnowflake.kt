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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.utils.TimeUtil
import java.time.OffsetDateTime
import javax.annotation.Nonnull

/**
 * Marks a snowflake entity. Snowflake entities are ones that have an id that uniquely identifies them.
 *
 * @since 3.0
 */
interface ISnowflake {
    @get:Nonnull
    val id: String?
        /**
         * The Snowflake id of this entity. This is unique to every entity and will never change.
         *
         * @return Never-null String containing the Id.
         */
        get() = java.lang.Long.toUnsignedString(idLong)

    /**
     * The Snowflake id of this entity. This is unique to every entity and will never change.
     *
     * @return Long containing the Id.
     */
    @JvmField
    val idLong: Long

    @get:Nonnull
    val timeCreated: OffsetDateTime?
        /**
         * The time this entity was created. Calculated through the Snowflake in [.getIdLong].
         *
         * @return OffsetDateTime - Time this entity was created at.
         *
         * @see TimeUtil.getTimeCreated
         */
        get() = TimeUtil.getTimeCreated(idLong)
}
