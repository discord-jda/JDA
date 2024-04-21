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
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.internal.utils.Checks
import java.time.Duration
import java.time.Instant
import javax.annotation.Nonnull

/**
 * Utility class representing Discord Markdown timestamps.
 * <br></br>This class implements [.toString] such that it can be directly included in message content.
 *
 *
 * These timestamps are rendered by the individual receiving Discord client in a local timezone and language format.
 * Each timestamp can be displayed with different [TimeFormats][TimeFormat].
 */
class Timestamp(format: TimeFormat, timestamp: Long) {
    /**
     * The [TimeFormat] used to display this timestamp.
     *
     * @return The [TimeFormat]
     */
    @get:Nonnull
    val format: TimeFormat

    /**
     * The unix epoch timestamp for this markdown timestamp.
     * <br></br>This is similar to [System.currentTimeMillis] and provided in millisecond precision for easier compatibility.
     * Discord uses seconds precision instead.
     *
     * @return The millisecond unix epoch timestamp
     */
    val timestamp: Long

    init {
        Checks.notNull(format, "TimeFormat")
        this.format = format
        this.timestamp = timestamp
    }

    /**
     * Shortcut for `Instant.ofEpochMilli(getTimestamp())`.
     *
     * @return The [Instant] of this timestamp
     */
    @Nonnull
    fun toInstant(): Instant {
        return Instant.ofEpochMilli(timestamp)
    }

    /**
     * Creates a new timestamp instance with the provided offset into the future relative to the current timestamp.
     *
     * @param  millis
     * The millisecond offset for the new timestamp
     *
     * @return Copy of this timestamp with the relative offset
     *
     * @see .plus
     */
    @Nonnull
    operator fun plus(millis: Long): Timestamp {
        return Timestamp(format, timestamp + millis)
    }

    /**
     * Creates a new timestamp instance with the provided offset into the future relative to the current timestamp.
     *
     * @param  duration
     * The offset for the new timestamp
     *
     * @throws IllegalArgumentException
     * If the provided duration is null
     *
     * @return Copy of this timestamp with the relative offset
     *
     * @see .plus
     */
    @Nonnull
    operator fun plus(@Nonnull duration: Duration): Timestamp {
        Checks.notNull(duration, "Duration")
        return plus(duration.toMillis())
    }

    /**
     * Creates a new timestamp instance with the provided offset into the past relative to the current timestamp.
     *
     * @param  millis
     * The millisecond offset for the new timestamp
     *
     * @return Copy of this timestamp with the relative offset
     *
     * @see .minus
     */
    @Nonnull
    operator fun minus(millis: Long): Timestamp {
        return Timestamp(format, timestamp - millis)
    }

    /**
     * Creates a new timestamp instance with the provided offset into the past relative to the current timestamp.
     *
     * @param  duration
     * The offset for the new timestamp
     *
     * @throws IllegalArgumentException
     * If the provided duration is null
     *
     * @return Copy of this timestamp with the relative offset
     *
     * @see .minus
     */
    @Nonnull
    operator fun minus(@Nonnull duration: Duration): Timestamp {
        Checks.notNull(duration, "Duration")
        return minus(duration.toMillis())
    }

    override fun toString(): String {
        return "<t:" + timestamp / 1000 + ":" + format.style + ">"
    }
}
