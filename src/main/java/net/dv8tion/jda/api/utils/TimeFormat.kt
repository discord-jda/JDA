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
import java.time.temporal.TemporalAccessor
import java.util.regex.Pattern
import javax.annotation.Nonnull

/**
 * Utility enum used to provide different markdown styles for timestamps.
 * <br></br>These can be used to represent a unix epoch timestamp in different formats.
 *
 *
 * These timestamps are rendered by the individual receiving discord client in a local timezone and language format.
 * Each timestamp can be displayed with different [TimeFormats][TimeFormat].
 *
 *
 * **Example**<br></br>
 * <pre>`channel.sendMessage("Current Time: " + TimeFormat.RELATIVE.now()).queue();
 * channel.sendMessage("Uptime: " + TimeFormat.RELATIVE.format(getStartTime())).queue();
`</pre> *
 */
enum class TimeFormat(
    /**
     * The display style flag used for the markdown representation.
     * <br></br>This is encoded into the markdown to provide the client with rendering context.
     *
     * @return The style flag
     */
    @get:Nonnull val style: String
) {
    /** Formats time as `18:49` or `6:49 PM`  */
    TIME_SHORT("t"),

    /** Formats time as `18:49:26` or `6:49:26 PM`  */
    TIME_LONG("T"),

    /** Formats date as `16/06/2021` or `06/16/2021`  */
    DATE_SHORT("d"),

    /** Formats date as `16 June 2021`  */
    DATE_LONG("D"),

    /** Formats date and time as `16 June 2021 18:49` or `June 16, 2021 6:49 PM`  */
    DATE_TIME_SHORT("f"),

    /** Formats date and time as `Wednesday, 16 June 2021 18:49` or `Wednesday, June 16, 2021 6:49 PM`  */
    DATE_TIME_LONG("F"),

    /** Formats date and time as relative `18 minutes ago` or `2 days ago`  */
    RELATIVE("R");

    /**
     * Formats the provided [TemporalAccessor] instance into a timestamp markdown.
     *
     * @param  temporal
     * The [TemporalAccessor]
     *
     * @throws IllegalArgumentException
     * If the provided temporal instance is null
     * @throws java.time.DateTimeException
     * If the temporal accessor cannot be converted to an instant
     *
     * @return The markdown string with this encoded style
     *
     * @see Instant.from
     */
    @Nonnull
    fun format(@Nonnull temporal: TemporalAccessor?): String {
        Checks.notNull(temporal, "Temporal")
        val timestamp = Instant.from(temporal).toEpochMilli()
        return format(timestamp)
    }

    /**
     * Formats the provided unix epoch timestamp into a timestamp markdown.
     * <br></br>Compatible with millisecond precision timestamps such as the ones provided by [System.currentTimeMillis].
     *
     * @param  timestamp
     * The millisecond epoch
     *
     * @return The markdown string with this encoded style
     */
    @Nonnull
    fun format(timestamp: Long): String {
        return "<t:" + timestamp / 1000 + ":" + style + ">"
    }

    /**
     * Converts the provided [Instant] into a [Timestamp] with this style.
     *
     * @param  instant
     * The [Instant] for the timestamp
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The [Timestamp] instance
     *
     * @see .now
     * @see .atTimestamp
     * @see Instant.from
     * @see Instant.toEpochMilli
     */
    @Nonnull
    fun atInstant(@Nonnull instant: Instant): Timestamp {
        Checks.notNull(instant, "Instant")
        return Timestamp(this, instant.toEpochMilli())
    }

    /**
     * Converts the provided unix epoch timestamp into a [Timestamp] with this style.
     * <br></br>Compatible with millisecond precision timestamps such as the ones provided by [System.currentTimeMillis].
     *
     * @param  timestamp
     * The millisecond epoch
     *
     * @return The [Timestamp] instance
     *
     * @see .now
     */
    @Nonnull
    fun atTimestamp(timestamp: Long): Timestamp {
        return Timestamp(this, timestamp)
    }

    /**
     * Shortcut for `style.atTimestamp(System.currentTimeMillis())`.
     *
     * @return [Timestamp] instance for the current time
     *
     * @see Timestamp.plus
     * @see Timestamp.minus
     */
    @Nonnull
    fun now(): Timestamp {
        return Timestamp(this, System.currentTimeMillis())
    }

    /**
     * Shortcut for `style.now().plus(duration)`.
     *
     * @param  duration
     * The [Duration] offset into the future
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [Timestamp] instance for the offset relative to the current time
     *
     * @see .now
     * @see Timestamp.plus
     */
    @Nonnull
    fun after(@Nonnull duration: Duration): Timestamp? {
        return now().plus(duration)
    }

    /**
     * Shortcut for `style.now().plus(millis)`.
     *
     * @param  millis
     * The millisecond offset into the future
     *
     * @return [Timestamp] instance for the offset relative to the current time
     *
     * @see .now
     * @see Timestamp.plus
     */
    @Nonnull
    fun after(millis: Long): Timestamp? {
        return now().plus(millis)
    }

    /**
     * Shortcut for `style.now().minus(duration)`.
     *
     * @param  duration
     * The [Duration] offset into the past
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [Timestamp] instance for the offset relative to the current time
     *
     * @see .now
     * @see Timestamp.minus
     */
    @Nonnull
    fun before(@Nonnull duration: Duration): Timestamp? {
        return now().minus(duration)
    }

    /**
     * Shortcut for `style.now().minus(millis)`.
     *
     * @param  millis
     * The millisecond offset into the past
     *
     * @return [Timestamp] instance for the offset relative to the current time
     *
     * @see .now
     * @see Timestamp.minus
     */
    @Nonnull
    fun before(millis: Long): Timestamp? {
        return now().minus(millis)
    }

    companion object {
        /**
         * The default time format used when no style is provided.
         */
        val DEFAULT = DATE_TIME_SHORT

        /**
         * [Pattern] used for [.parse].
         *
         *
         * **Groups**<br></br>
         * <table>
         * <caption style="display: none">Javadoc is stupid, this is not a required tag</caption>
         * <tr>
         * <th>Index</th>
         * <th>Name</th>
         * <th>Description</th>
        </tr> *
         * <tr>
         * <td>0</td>
         * <td>N/A</td>
         * <td>The entire timestamp markdown</td>
        </tr> *
         * <tr>
         * <td>1</td>
         * <td>time</td>
         * <td>The timestamp value as a unix epoch in second precision</td>
        </tr> *
         * <tr>
         * <td>2</td>
         * <td>style</td>
         * <td>The style used for displaying the timestamp (single letter flag)</td>
        </tr> *
        </table> *
         *
         * @see .parse
         */
        val MARKDOWN = Pattern.compile("<t:(?<time>-?\\d{1,17})(?::(?<style>[tTdDfFR]))?>")

        /**
         * Returns the time format for the provided style flag.
         *
         * @param  style
         * The style flag
         *
         * @throws IllegalArgumentException
         * If the provided style string is not exactly one character long
         *
         * @return The representative TimeFormat or [.DEFAULT] if none could be identified
         */
        @Nonnull
        fun fromStyle(@Nonnull style: String): TimeFormat {
            Checks.notEmpty(style, "Style")
            Checks.notLonger(style, 1, "Style")
            for (format in entries) {
                if (format.style == style) return format
            }
            return DEFAULT
        }

        /**
         * Parses the provided markdown into a [Timestamp] instance.
         * <br></br>This is the reverse operation for the [Timestamp.toString()][Timestamp.toString] representation.
         *
         * @param  markdown
         * The markdown for the timestamp value
         *
         * @throws IllegalArgumentException
         * If the provided markdown is null or does not match the [.MARKDOWN] pattern
         *
         * @return [Timestamp] instance for the provided markdown
         */
        @Nonnull
        fun parse(@Nonnull markdown: String): Timestamp {
            Checks.notNull(markdown, "Markdown")
            val matcher = MARKDOWN.matcher(markdown.trim { it <= ' ' })
            require(matcher.find()) { "Invalid markdown format! Provided: $markdown" }
            val format = matcher.group("style")
            return Timestamp(if (format == null) DEFAULT else fromStyle(format), matcher.group("time").toLong() * 1000)
        }
    }
}
