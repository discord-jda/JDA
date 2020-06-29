/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.utils.TimeUtil;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * Marks a snowflake entity. Snowflake entities are ones that have an id that uniquely identifies them.
 *
 * @since 3.0
 */
public interface ISnowflake
{
    /**
     * The Snowflake id of this entity. This is unique to every entity and will never change.
     *
     * @return Never-null String containing the Id.
     */
    @Nonnull
    default String getId()
    {
        return Long.toUnsignedString(getIdLong());
    }

    /**
     * The Snowflake id of this entity. This is unique to every entity and will never change.
     *
     * @return Long containing the Id.
     */
    long getIdLong();

    /**
     * The time this entity was created. Calculated through the Snowflake in {@link #getIdLong}.
     *
     * @return OffsetDateTime - Time this entity was created at.
     *
     * @see    TimeUtil#getTimeCreated(long)
     */
    @Nonnull
    default OffsetDateTime getTimeCreated()
    {
        return TimeUtil.getTimeCreated(getIdLong());
    }
}
