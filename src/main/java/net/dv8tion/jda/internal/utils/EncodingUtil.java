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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.stream.Collectors;

public class EncodingUtil
{
    public static String encodeUTF8(String chars)
    {
        try
        {
            return URLEncoder.encode(chars, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AssertionError(e); // thanks JDK 1.4
        }
    }

    public static String encodeCodepointsUTF8(String input)
    {
        if (!input.startsWith("U+"))
            throw new IllegalArgumentException("Invalid format");
        String[] codePoints = input.substring(2).split("\\s*U\\+\\s*");
        StringBuilder encoded = new StringBuilder();
        for (String part : codePoints)
        {
            String utf16 = decodeCodepoint(part, 16);
            String urlEncoded = encodeUTF8(utf16);
            encoded.append(urlEncoded);
        }
        return encoded.toString();
    }

    public static String decodeCodepoint(String codepoint)
    {
        if (!codepoint.startsWith("U+"))
            throw new IllegalArgumentException("Invalid format");
        return decodeCodepoint(codepoint.substring(2), 16);
    }

    public static String encodeCodepoints(String unicode)
    {
        return unicode.codePoints()
               .mapToObj(code -> "U+" + Integer.toHexString(code))
               .collect(Collectors.joining());
    }

    private static String decodeCodepoint(String hex, int radix)
    {
        int codePoint = Integer.parseUnsignedInt(hex, radix);
        return String.valueOf(Character.toChars(codePoint));
    }

    /**
     * Encodes a unicode correctly based on being in codepoint notation or not.
     * @param  unicode Provided unicode in the form of <code>\​uXXXX</code> or <code>U+XXXX</code>
     * @return Never-null String containing the encoded unicode
     */
    public static String encodeReaction(String unicode)
    {
        if (unicode.startsWith("U+") || unicode.startsWith("u+"))
            return encodeCodepointsUTF8(unicode);
        else
            return encodeUTF8(unicode);
    }
}
