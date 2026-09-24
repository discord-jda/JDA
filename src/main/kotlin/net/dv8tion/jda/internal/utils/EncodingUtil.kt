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

package net.dv8tion.jda.internal.utils

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.stream.Collectors

object EncodingUtil {
    private const val HEX_RADIX = 16

    @JvmStatic
    @Suppress("JdkObsolete", "AndroidJdkLibsChecker")
    fun encodeUTF8(chars: String): String =
        try {
            URLEncoder.encode(chars, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw AssertionError(e) // thanks JDK 1.4
        }

    @JvmStatic
    fun encodeCodepointsUTF8(input: String): String {
        require(input.startsWith("U+")) { "Invalid format" }
        val codePoints = input.substring(2).split("\\s*U\\+\\s*".toRegex())
        val encoded = StringBuilder()
        for (part in codePoints) {
            val utf16 = decodeCodepoint(part, HEX_RADIX)
            val urlEncoded = encodeUTF8(utf16)
            encoded.append(urlEncoded)
        }
        return encoded.toString()
    }

    @JvmStatic
    fun decodeCodepoint(codepoint: String): String {
        require(codepoint.startsWith("U+")) { "Invalid format" }
        return decodeCodepoint(codepoint.substring(2), HEX_RADIX)
    }

    @JvmStatic
    fun encodeCodepoints(unicode: String): String =
        unicode
            .codePoints()
            .mapToObj { code -> "U+" + Integer.toHexString(code) }
            .collect(Collectors.joining())

    private fun decodeCodepoint(
        hex: String,
        radix: Int,
    ): String {
        val codePoint = Integer.parseUnsignedInt(hex, radix)
        return String(Character.toChars(codePoint))
    }

    /**
     * Encodes a unicode correctly based on being in codepoint notation or not.
     *
     * @param unicode Provided unicode in the form of `\uXXXX` or `U+XXXX`
     *
     * @return Never-null String containing the encoded unicode
     */
    @JvmStatic
    fun encodeReaction(unicode: String): String =
        if (unicode.startsWith("U+") || unicode.startsWith("u+")) {
            encodeCodepointsUTF8(unicode)
        } else {
            encodeUTF8(unicode)
        }
}
