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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;

/**
 * Utility class representing Discord Markdown timestamps.
 * <br>This class implements {@link #toString()} such that it can be directly included in message content.
 *
 * <p>These timestamps are rendered by the individual receiving Discord client in a local timezone and language format.
 * Each timestamp can be displayed with different {@link TimeFormat TimeFormats}.
 */
public class Timestamp
{
    private final TimeFormat format;
    private final long timestamp;

    protected Timestamp(TimeFormat format, long timestamp)
    {
        Checks.notNull(format, "TimeFormat");
        this.format = format;
        this.timestamp = timestamp;
    }

    /**
     * The {@link TimeFormat} used to display this timestamp.
     *
     * @return The {@link TimeFormat}
     */
    @Nonnull
    public TimeFormat getFormat()
    {
        return format;
    }

    /**
     * The unix epoch timestamp for this markdown timestamp.
     * <br>This is similar to {@link System#currentTimeMillis()} and provided in millisecond precision for easier compatibility.
     * Discord uses seconds precision instead.
     *
     * @return The millisecond unix epoch timestamp
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    /**
     * Shortcut for {@code Instant.ofEpochMilli(getTimestamp())}.
     *
     * @return The {@link Instant} of this timestamp
     */
    @Nonnull
    public Instant toInstant()
    {
        return Instant.ofEpochMilli(timestamp);
    }

    /**
     * Creates a new timestamp instance with the provided offset into the future relative to the current timestamp.
     *
     * @param  millis
     *         The millisecond offset for the new timestamp
     *
     * @return Copy of this timestamp with the relative offset
     *
     * @see    #plus(Duration)
     */
    @Nonnull
    public Timestamp plus(long millis)
    {
        return new Timestamp(format, timestamp + millis);
    }

    /**
     * Creates a new timestamp instance with the provided offset into the future relative to the current timestamp.
     *
     * @param  duration
     *         The offset for the new timestamp
     *
     * @throws IllegalArgumentException
     *         If the provided duration is null
     *
     * @return Copy of this timestamp with the relative offset
     *
     * @see    #plus(long)
     */
    @Nonnull
    public Timestamp plus(@Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        return plus(duration.toMillis());
    }

    /**
     * Creates a new timestamp instance with the provided offset into the past relative to the current timestamp.
     *
     * @param  millis
     *         The millisecond offset for the new timestamp
     *
     * @return Copy of this timestamp with the relative offset
     *
     * @see    #minus(Duration)
     */
    @Nonnull
    public Timestamp minus(long millis)
    {
        return new Timestamp(format, timestamp - millis);
    }

    /**
     * Creates a new timestamp instance with the provided offset into the past relative to the current timestamp.
     *
     * @param  duration
     *         The offset for the new timestamp
     *
     * @throws IllegalArgumentException
     *         If the provided duration is null
     *
     * @return Copy of this timestamp with the relative offset
     *
     * @see    #minus(long)
     */
    @Nonnull
    public Timestamp minus(@Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        return minus(duration.toMillis());
    }

    @Override
    public String toString()
    {
        return "<t:" + timestamp / 1000 + ":" + format.getStyle() + ">";
    }
}
