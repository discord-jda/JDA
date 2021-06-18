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

public class Timestamp
{
    private final TimeFormat format;
    private final long timestamp;

    public Timestamp(TimeFormat format, long timestamp)
    {
        Checks.notNull(format, "TimeFormat");
        this.format = format;
        this.timestamp = timestamp;
    }

    @Nonnull
    public TimeFormat getFormat()
    {
        return format;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    @Nonnull
    public Instant toInstant()
    {
        return Instant.ofEpochMilli(timestamp);
    }

    @Nonnull
    public Timestamp plus(long millis)
    {
        return new Timestamp(format, timestamp + millis);
    }

    @Nonnull
    public Timestamp plus(@Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        return plus(duration.toMillis());
    }

    @Nonnull
    public Timestamp minus(long millis)
    {
        return new Timestamp(format, timestamp - millis);
    }

    @Nonnull
    public Timestamp minus(@Nonnull Duration duration)
    {
        Checks.notNull(duration, "Duration");
        return plus(duration.toMillis());
    }

    @Override
    public String toString()
    {
        return "<t:" + timestamp / 1000 + ":" + format.getFlag() + ">";
    }
}
