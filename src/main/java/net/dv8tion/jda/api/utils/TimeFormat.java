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

/**
 * Utility enum used to provide different markdown styles for timestamps.
 * <br>These can be used to represent a unix epoch timestamp in different formats.
 *
 * <p>These timestamps are rendered by the individual receiving discord client in a local timezone and language format.
 * Each timestamp can be displayed with different {@link TimeFormat TimeFormats}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * channel.sendMessage("Current Time: " + TimeFormat.RELATIVE.now()).queue();
 * channel.sendMessage("Uptime: " + TimeFormat.RELATIVE.format(getStartTime())).queue();
 * }</pre>
 */
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
    /** Formats date and time as {@code 16 June 2021 18:49} or {@code June 16, 2021 6:49 PM} */
    DATE_TIME_SHORT("f"),
    /** Formats date and time as {@code Wednesday, 16 June 2021 18:49} or {@code Wednesday, June 16, 2021 6:49 PM} */
    DATE_TIME_LONG("F"),
    /** Formats date and time as relative {@code 18 minutes ago} or {@code 2 days ago} */
    RELATIVE("R"),
    ;

    /**
     * The default time format used when no style is provided.
     */
    public static final TimeFormat DEFAULT = DATE_TIME_SHORT;

    /**
     * {@link Pattern} used for {@link #parse(String)}.
     *
     * <h4>Groups</h4>
     * <table>
     *   <caption style="display: none">Javadoc is stupid, this is not a required tag</caption>
     *   <tr>
     *     <th>Index</th>
     *     <th>Name</th>
     *     <th>Description</th>
     *   </tr>
     *   <tr>
     *     <td>0</td>
     *     <td>N/A</td>
     *     <td>The entire timestamp markdown</td>
     *   </tr>
     *   <tr>
     *     <td>1</td>
     *     <td>time</td>
     *     <td>The timestamp value as a unix epoch in second precision</td>
     *   </tr>
     *   <tr>
     *     <td>2</td>
     *     <td>style</td>
     *     <td>The style used for displaying the timestamp (single letter flag)</td>
     *   </tr>
     * </table>
     *
     * @see #parse(String)
     */
    public static final Pattern MARKDOWN = Pattern.compile("<t:(?<time>-?\\d{1,17})(?::(?<style>[tTdDfFR]))?>");

    private final String style;

    TimeFormat(String style)
    {
        this.style = style;
    }

    /**
     * The display style flag used for the markdown representation.
     * <br>This is encoded into the markdown to provide the client with rendering context.
     *
     * @return The style flag
     */
    @Nonnull
    public String getStyle()
    {
        return style;
    }

    /**
     * Returns the time format for the provided style flag.
     *
     * @param  style
     *         The style flag
     *
     * @throws IllegalArgumentException
     *         If the provided style string is not exactly one character long
     *
     * @return The representative TimeFormat or {@link #DEFAULT} if none could be identified
     */
    @Nonnull
    public static TimeFormat fromStyle(@Nonnull String style)
    {
        Checks.notEmpty(style, "Style");
        Checks.notLonger(style, 1, "Style");
        for (TimeFormat format : values())
        {
            if (format.style.equals(style))
                return format;
        }
        return DEFAULT;
    }

    /**
     * Parses the provided markdown into a {@link Timestamp} instance.
     * <br>This is the reverse operation for the {@link Timestamp#toString() Timestamp.toString()} representation.
     *
     * @param  markdown
     *         The markdown for the timestamp value
     *
     * @throws IllegalArgumentException
     *         If the provided markdown is null or does not match the {@link #MARKDOWN} pattern
     *
     * @return {@link Timestamp} instance for the provided markdown
     */
    @Nonnull
    public static Timestamp parse(@Nonnull String markdown)
    {
        Checks.notNull(markdown, "Markdown");
        Matcher matcher = MARKDOWN.matcher(markdown.trim());
        if (!matcher.find())
            throw new IllegalArgumentException("Invalid markdown format! Provided: " + markdown);
        String format = matcher.group("style");
        return new Timestamp(format == null ? DEFAULT : fromStyle(format), Long.parseLong(matcher.group("time")) * 1000);
    }

    /**
     * Formats the provided {@link TemporalAccessor} instance into a timestamp markdown.
     *
     * @param  temporal
     *         The {@link TemporalAccessor}
     *
     * @throws IllegalArgumentException
     *         If the provided temporal instance is null
     * @throws java.time.DateTimeException
     *         If the temporal accessor cannot be converted to an instant
     *
     * @return The markdown string with this encoded style
     *
     * @see    Instant#from(TemporalAccessor)
     */
    @Nonnull
    public String format(@Nonnull TemporalAccessor temporal)
    {
        Checks.notNull(temporal, "Temporal");
        long timestamp = Instant.from(temporal).toEpochMilli();
        return format(timestamp);
    }

    /**
     * Formats the provided unix epoch timestamp into a timestamp markdown.
     * <br>Compatible with millisecond precision timestamps such as the ones provided by {@link System#currentTimeMillis()}.
     *
     * @param  timestamp
     *         The millisecond epoch
     *
     * @return The markdown string with this encoded style
     */
    @Nonnull
    public String format(long timestamp)
    {
        return "<t:" + timestamp / 1000 + ":" + style + ">";
    }

    /**
     * Converts the provided {@link Instant} into a {@link Timestamp} with this style.
     *
     * @param  instant
     *         The {@link Instant} for the timestamp
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The {@link Timestamp} instance
     *
     * @see    #now()
     * @see    #atTimestamp(long)
     * @see    Instant#from(TemporalAccessor)
     * @see    Instant#toEpochMilli()
     */
    @Nonnull
    public Timestamp atInstant(@Nonnull Instant instant)
    {
        Checks.notNull(instant, "Instant");
        return new Timestamp(this, instant.toEpochMilli());
    }

    /**
     * Converts the provided unix epoch timestamp into a {@link Timestamp} with this style.
     * <br>Compatible with millisecond precision timestamps such as the ones provided by {@link System#currentTimeMillis()}.
     *
     * @param  timestamp
     *         The millisecond epoch
     *
     * @return The {@link Timestamp} instance
     *
     * @see    #now()
     */
    @Nonnull
    public Timestamp atTimestamp(long timestamp)
    {
        return new Timestamp(this, timestamp);
    }

    /**
     * Shortcut for {@code style.atTimestamp(System.currentTimeMillis())}.
     *
     * @return {@link Timestamp} instance for the current time
     *
     * @see    Timestamp#plus(long)
     * @see    Timestamp#minus(long)
     */
    @Nonnull
    public Timestamp now()
    {
        return new Timestamp(this, System.currentTimeMillis());
    }

    /**
     * Shortcut for {@code style.now().plus(duration)}.
     *
     * @param  duration
     *         The {@link Duration} offset into the future
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link Timestamp} instance for the offset relative to the current time
     *
     * @see    #now()
     * @see    Timestamp#plus(Duration)
     */
    @Nonnull
    public Timestamp after(@Nonnull Duration duration)
    {
        return now().plus(duration);
    }

    /**
     * Shortcut for {@code style.now().plus(millis)}.
     *
     * @param  millis
     *         The millisecond offset into the future
     *
     * @return {@link Timestamp} instance for the offset relative to the current time
     *
     * @see    #now()
     * @see    Timestamp#plus(long)
     */
    @Nonnull
    public Timestamp after(long millis)
    {
        return now().plus(millis);
    }

    /**
     * Shortcut for {@code style.now().minus(duration)}.
     *
     * @param  duration
     *         The {@link Duration} offset into the past
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link Timestamp} instance for the offset relative to the current time
     *
     * @see    #now()
     * @see    Timestamp#minus(Duration)
     */
    @Nonnull
    public Timestamp before(@Nonnull Duration duration)
    {
        return now().minus(duration);
    }

    /**
     * Shortcut for {@code style.now().minus(millis)}.
     *
     * @param  millis
     *         The millisecond offset into the past
     *
     * @return {@link Timestamp} instance for the offset relative to the current time
     *
     * @see    #now()
     * @see    Timestamp#minus(long)
     */
    @Nonnull
    public Timestamp before(long millis)
    {
        return now().minus(millis);
    }
}
