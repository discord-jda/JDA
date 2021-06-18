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
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TimeFormat
{
    /** Formats time as {@code 18:49} or {@code 6:49 PM} */
    TIME_SHORT("t"),
    /** Formats time as {@code 18:49:26} or {@code 6:49:26 PM} */
    TIME_LONG("T"),
    /** Formats date as {@code 16/06/2021} or {@code 06/16/2021} */
    DATE_SHORT("d"),
    /** Formats date as {@code 16 June 2021} */
    DATE_LONG("D"),
    /** Formats date and time as {@code 16 June 2021 18:49} or {@code 16 June 2021 6:49 PM} */
    DATE_TIME_SHORT("f"),
    /** Formats date and time as {@code Wednesday, 16 June 2021 18:49} or {@code Wednesday, 16 June 2021 6:49 PM} */
    DATE_TIME_LONG("F"),
    /** Formats date and time as relative {@code 18 minutes ago} or {@code 2 days ago} */
    RELATIVE("R"),
    ;

    public static final TimeFormat DEFAULT = DATE_TIME_SHORT;
    public static final Pattern MARKDOWN = Pattern.compile("<t:(?<time>-?\\d{1,17})(?::(?<format>[tTdDfFR]))?>");

    private final String flag;

    TimeFormat(String flag)
    {
        this.flag = flag;
    }

    @Nonnull
    public String getFlag()
    {
        return flag;
    }

    @Nonnull
    public static TimeFormat fromKey(@Nonnull String key)
    {
        for (TimeFormat format : values())
        {
            if (format.flag.equals(key))
                return format;
        }
        return DEFAULT;
    }

    @Nonnull
    public static Timestamp parse(@Nonnull String markdown)
    {
        Checks.notNull(markdown, "Markdown");
        Matcher matcher = MARKDOWN.matcher(markdown);
        if (!matcher.find())
            throw new IllegalArgumentException("Invalid markdown format! Provided: " + markdown);
        String format = matcher.group("format");
        return new Timestamp(format == null ? DEFAULT : fromKey(format), Long.parseLong(matcher.group("time")) * 1000);
    }

    @Nonnull
    public String format(@Nonnull TemporalAccessor temporal)
    {
        Checks.notNull(temporal, "Temporal");
        long timestamp = Instant.from(temporal).toEpochMilli();
        return format(timestamp);
    }

    @Nonnull
    public String format(long timestamp)
    {
        return "<t:" + timestamp / 1000 + ":" + flag + ">";
    }

    @Nonnull
    public Timestamp atInstant(@Nonnull Instant instant)
    {
        Checks.notNull(instant, "Instant");
        return new Timestamp(this, instant.toEpochMilli());
    }

    @Nonnull
    public Timestamp atTimestamp(long timestamp)
    {
        return new Timestamp(this, timestamp);
    }

    @Nonnull
    public Timestamp now()
    {
        return new Timestamp(this, System.currentTimeMillis());
    }

    @Nonnull
    public Timestamp after(@Nonnull Duration duration)
    {
        return now().plus(duration);
    }

    @Nonnull
    public Timestamp after(long millis)
    {
        return now().plus(millis);
    }

    @Nonnull
    public Timestamp before(@Nonnull Duration duration)
    {
        return now().minus(duration);
    }

    @Nonnull
    public Timestamp before(long millis)
    {
        return now().minus(millis);
    }
}
