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

package net.dv8tion.jda.api.utils.data.etf;

/**
 * Tags used for encoding and decoding for external terms.
 * <br>This list in incomplete as not all tags are used by this library.
 */
public class ExTermTag
{
    // 8 IEEE float
    public static final byte NEW_FLOAT = 70;
    // 4 Uncompressed Size | N ZLIB Compressed Data
    public static final byte COMPRESSED = 80;
    // 1 byte unsigned int value
    public static final byte SMALL_INT = 97;
    // 4 byte signed int value
    public static final byte INT = 98;
    // 31 bytes string of float value sprintf("%.20e")
    public static final byte FLOAT = 99; // deprecated
    // 2 byte length | N bytes latin-1 string
    public static final byte ATOM = 100; // deprecated
    // 0 bytes empty list
    public static final byte NIL = 106;
    // 2 byte length | N bytes
    public static final byte STRING = 107;
    // 4 byte length | N tags | 1 byte NIL tag
    public static final byte LIST = 108;
    // 4 byte length | N bytes UTF-8 string
    public static final byte BINARY = 109;
    // 1 byte length | 1 byte sign | N bytes unsigned SE int
    public static final byte SMALL_BIGINT = 110;
    // 1 byte length | N bytes latin-1 string
    public static final byte SMALL_ATOM = 115; // deprecated
    // 4 byte length | N pairs of KEY,VALUE terms
    public static final byte MAP = 116;
    // 2 byte length | N bytes of UTF-8 string
    public static final byte ATOM_UTF8 = 118;
    // 1 byte length | N bytes of UTF-8 string
    public static final byte SMALL_ATOM_UTF8 = 119;
}
