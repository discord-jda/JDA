/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.utils;

import gnu.trove.TCollections;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.ISnowflake;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Formatter;
import java.util.TimeZone;

public class MiscUtil
{
    public static final long DISCORD_EPOCH = 1420070400000L;
    public static final long TIMESTAMP_OFFSET = 22;
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;

    /**
     * Gets the creation-time of a JDA-entity by doing the reverse snowflake algorithm on its id.
     * This returns the creation-time of the actual entity on Discords side, not inside JDA.
     *
     * @param  entityId
     *         The id of the JDA entity where the creation-time should be determined for
     *
     * @return The creation time of the JDA entity as OffsetDateTime
     */
    public static OffsetDateTime getCreationTime(long entityId)
    {
        long timestamp = ((entityId >>> TIMESTAMP_OFFSET) + DISCORD_EPOCH);
        Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmt.setTimeInMillis(timestamp);
        return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());
    }

    /**
     * Gets the creation-time of a JDA-entity by doing the reverse snowflake algorithm on its id.
     * This returns the creation-time of the actual entity on Discords side, not inside JDA.
     *
     * @param  entity
     *         The JDA entity where the creation-time should be determined for
     *
     * @throws IllegalArgumentException
     *         If the provided entity is {@code null}
     *
     * @return The creation time of the JDA entity as OffsetDateTime
     */
    public static OffsetDateTime getCreationTime(ISnowflake entity)
    {
        Checks.notNull(entity, "Entity");
        return getCreationTime(entity.getIdLong());
    }

    /**
     * Returns a prettier String-representation of a OffsetDateTime object
     *
     * @param  time
     *         The OffsetDateTime object to format
     *
     * @return The String of the formatted OffsetDateTime
     */
    public static String getDateTimeString(OffsetDateTime time)
    {
        return time.format(dtFormatter);
    }

    /**
     * Generates a new thread-safe {@link gnu.trove.map.TLongObjectMap TLongObjectMap}
     * @param  <T>
     *         The Object type
     *
     * @return a new thread-safe {@link gnu.trove.map.TLongObjectMap TLongObjectMap}
     */
    public static <T> TLongObjectMap<T> newLongMap()
    {
        return TCollections.synchronizedMap(new TLongObjectHashMap<T>());
    }

    /**
     * URL-Encodes the given String to UTF-8 after
     * form-data specifications (space {@literal ->} +)
     *
     * @param  chars
     *         The characters to encode
     *
     * @throws java.lang.RuntimeException
     *         If somehow the encoding fails
     *
     * @return The encoded String
     */
    public static String encodeUTF8(String chars)
    {
        try
        {
            return URLEncoder.encode(chars, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e); // thanks JDK 1.4
        }
    }

    public static long parseSnowflake(String input)
    {
        Checks.notEmpty(input, "ID");
        try
        {
            if (!input.startsWith("-")) // if not negative -> parse unsigned
                return Long.parseUnsignedLong(input);
            else // if negative -> parse normal
                return Long.parseLong(input);
        }
        catch (NumberFormatException ex)
        {
            throw new NumberFormatException(
                String.format("The specified ID is not a valid snowflake (%s). Expecting a valid long value!", input));
        }
    }

    /**
     * Can be used to append a String to a formatter.
     *
     * @param formatter
     *        The {@link java.util.Formatter Formatter}
     * @param width
     *        Minimum width to meet, filled with space if needed
     * @param precision
     *        Maximum amount of characters to append
     * @param leftJustified
     *        Whether or not to left-justify the value
     * @param out
     *        The String to append
     */
    public static void appendTo(Formatter formatter, int width, int precision, boolean leftJustified, String out)
    {
        try
        {
            Appendable appendable = formatter.out();
            if (precision > -1 && out.length() > precision)
            {
                appendable.append(StringUtils.truncate(out, precision));
                return;
            }

            if (leftJustified)
                appendable.append(StringUtils.rightPad(out, width));
            else
                appendable.append(StringUtils.leftPad(out, width));
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }
}
