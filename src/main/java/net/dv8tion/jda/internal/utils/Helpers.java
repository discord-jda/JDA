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

package net.dv8tion.jda.internal.utils;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nullable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class has major inspiration from <a href="https://commons.apache.org/proper/commons-lang/" target="_blank">Lang 3</a>
 *
 * <p>Specifically StringUtils.java and ExceptionUtils.java
 */
public final class Helpers
{
    private static final ZoneOffset OFFSET = ZoneOffset.of("+00:00");
    @SuppressWarnings("rawtypes")
    private static final Consumer EMPTY_CONSUMER = (v) -> {};

    @SuppressWarnings("unchecked")
    public static <T> Consumer<T> emptyConsumer()
    {
        return (Consumer<T>) EMPTY_CONSUMER;
    }

    public static OffsetDateTime toOffset(long instant)
    {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(instant), OFFSET);
    }

    public static long toTimestamp(String iso8601String)
    {
        TemporalAccessor joinedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(iso8601String);
        return Instant.from(joinedAt).toEpochMilli();
    }

    public static OffsetDateTime toOffsetDateTime(@Nullable TemporalAccessor temporal)
    {
        if (temporal == null)
        {
            return null;
        }
        else if (temporal instanceof OffsetDateTime)
        {
            return (OffsetDateTime) temporal;
        }
        else
        {
            ZoneOffset offset;
            try
            {
                offset = ZoneOffset.from(temporal);
            }
            catch (DateTimeException ignore)
            {
                offset = ZoneOffset.UTC;
            }
            try
            {
                LocalDateTime ldt = LocalDateTime.from(temporal);
                return OffsetDateTime.of(ldt, offset);
            }
            catch (DateTimeException ignore)
            {
                try
                {
                    Instant instant = Instant.from(temporal);
                    return OffsetDateTime.ofInstant(instant, offset);
                }
                catch (DateTimeException ex)
                {
                    throw new DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: " +
                            temporal + " of type " + temporal.getClass().getName(), ex);
                }
            }
        }
    }

    // locale-safe String#format

    public static String format(String format, Object... args)
    {
        return String.format(Locale.ROOT, format, args);
    }

    // ## StringUtils ##

    public static boolean isEmpty(final CharSequence seq)
    {
        return seq == null || seq.length() == 0;
    }

    public static boolean containsWhitespace(final CharSequence seq)
    {
        if (isEmpty(seq))
            return false;
        for (int i = 0; i < seq.length(); i++)
        {
            if (Character.isWhitespace(seq.charAt(i)))
                return true;
        }
        return false;
    }

    public static boolean isBlank(final CharSequence seq)
    {
        if (isEmpty(seq))
            return true;
        for (int i = 0; i < seq.length(); i++)
        {
            if (!Character.isWhitespace(seq.charAt(i)))
                return false;
        }
        return true;
    }

    public static int countMatches(final CharSequence seq, final char c)
    {
        if (isEmpty(seq))
            return 0;
        int count = 0;
        for (int i = 0; i < seq.length(); i++)
        {
            if (seq.charAt(i) == c)
                count++;
        }
        return count;
    }

    public static String truncate(final String input, final int maxWidth)
    {
        if (input == null)
            return null;
        Checks.notNegative(maxWidth, "maxWidth");
        if (input.length() <= maxWidth)
            return input;
        if (maxWidth == 0)
            return "";
        return input.substring(0, maxWidth);
    }

    public static String rightPad(final String input, final int size)
    {
        int pads = size - input.length();
        if (pads <= 0)
            return input;
        StringBuilder out = new StringBuilder(input);
        for (int i = pads; i > 0; i--)
            out.append(' ');
        return out.toString();
    }

    public static String leftPad(final String input, final int size)
    {
        int pads = size - input.length();
        if (pads <= 0)
            return input;
        StringBuilder out = new StringBuilder();
        for (int i = pads; i > 0; i--)
            out.append(' ');
        return out.append(input).toString();
    }

    public static boolean isNumeric(final String input)
    {
        if (isEmpty(input))
            return false;
        for (char c : input.toCharArray())
        {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    public static int codePointLength(final CharSequence string)
    {
        return (int) string.codePoints().count();
    }

    public static String[] split(String input, String match)
    {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < input.length())
        {
            int j = input.indexOf(match, i);
            if (j == -1)
            {
                out.add(input.substring(i));
                break;
            }

            out.add(input.substring(i, j));
            i = j + match.length();
        }

        return out.toArray(new String[0]);
    }

    public static boolean equals(String a, String b, boolean ignoreCase)
    {
        return ignoreCase ? a == b || (a != null && b != null && a.equalsIgnoreCase(b)) : Objects.equals(a, b);
    }

    // ## CollectionUtils ##

    public static boolean deepEquals(Collection<?> first, Collection<?> second)
    {
        if (first == second)
            return true;
        if (first == null || second == null || first.size() != second.size())
            return false;
        for (Iterator<?> itFirst = first.iterator(), itSecond = second.iterator(); itFirst.hasNext(); )
        {
            Object elementFirst = itFirst.next();
            Object elementSecond = itSecond.next();
            if (!Objects.equals(elementFirst, elementSecond))
                return false;
        }
        return true;
    }

    public static boolean deepEqualsUnordered(Collection<?> first, Collection<?> second)
    {
        if (first == second) return true;
        if (first == null || second == null) return false;
        return first.size() == second.size() && second.containsAll(first);
    }

    public static <E extends Enum<E>> EnumSet<E> copyEnumSet(Class<E> clazz, Collection<E> col)
    {
        return col == null || col.isEmpty() ? EnumSet.noneOf(clazz) : EnumSet.copyOf(col);
    }

    @SafeVarargs
    public static <T> Set<T> setOf(T... elements)
    {
        Set<T> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... elements)
    {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    public static TLongObjectMap<DataObject> convertToMap(ToLongFunction<DataObject> getId, DataArray array)
    {
        TLongObjectMap<DataObject> map = new TLongObjectHashMap<>();
        for (int i = 0; i < array.length(); i++)
        {
            DataObject obj = array.getObject(i);
            long objId = getId.applyAsLong(obj);
            map.put(objId, obj);
        }
        return map;
    }

    // ## ExceptionUtils ##

    public static <T extends Throwable> T appendCause(T throwable, Throwable cause)
    {
        Throwable t = throwable;
        while (t.getCause() != null)
            t = t.getCause();
        t.initCause(cause);
        return throwable;
    }

    public static boolean hasCause(Throwable throwable, Class<? extends Throwable> cause)
    {
        Throwable cursor = throwable;
        while (cursor != null)
        {
            if (cause.isInstance(cursor))
                return true;
            cursor = cursor.getCause();
        }
        return false;
    }

    public static <T> Collector<T, ?, List<T>> toUnmodifiableList()
    {
        return Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList);
    }

    public static <T> Collector<T, ?, DataArray> toDataArray()
    {
        return Collector.of(DataArray::empty, DataArray::add, DataArray::addAll);
    }
}
