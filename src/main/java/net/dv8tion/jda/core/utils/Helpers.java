/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * This class has major inspiration from <a href="https://commons.apache.org/proper/commons-lang/" target="_blank">Lang 3</a>
 *
 * <p>Specifically StringUtils.java and ExceptionUtils.java
 */
public final class Helpers
{

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

    public static boolean equalsIgnoreCase(final String seq0, final String seq1)
    {
        return Objects.equals(seq0, seq1) || (seq0 != null && seq0.equalsIgnoreCase(seq1));
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
        if (input.isEmpty())
            return false;
        for (char c : input.toCharArray())
        {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    // ## ExceptionUtils ##

    //Copied from ogr.apache.commons:commons-lang3:3.5 ExceptionsUtils.java
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    // ## CollectionUtils ##

    public static boolean deepEquals(Collection<?> first, Collection<?> second)
    {
        if (first != null)
        {
            if (second == null)
                return false;
            if (first.size() != second.size())
                return false;
            for (Iterator<?> itFirst = first.iterator(), itSecond = second.iterator(); itFirst.hasNext(); )
            {
                Object elementFirst = itFirst.next();
                Object elementSecond = itSecond.next();
                if (!Objects.equals(elementFirst, elementSecond))
                    return false;
            }
        }
        else if (second != null)
        {
            return false;
        }
        return true;
    }

    // ## JSONObject ##

    public static boolean optBoolean(JSONObject object, String key)
    {
        return !object.isNull(key) && object.getBoolean(key);
    }

    public static int optInt(JSONObject object, String key, int defaultValue)
    {
        return object.isNull(key) ? defaultValue : object.getInt(key);
    }

    public static long optLong(JSONObject object, String key, long defaultValue)
    {
        return object.isNull(key) ? defaultValue : object.getLong(key);
    }
}
