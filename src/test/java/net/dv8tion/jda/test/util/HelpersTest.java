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

package net.dv8tion.jda.test.util;

import net.dv8tion.jda.internal.utils.Helpers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HelpersTest
{
    @Test
    void testIsEmpty()
    {
        assertThat(Helpers.isEmpty(null)).isTrue();
        assertThat(Helpers.isEmpty("")).isTrue();
        assertThat(Helpers.isEmpty("null")).isFalse();
        assertThat(Helpers.isEmpty("testing with spaces")).isFalse();
    }

    @Test
    void testContainsWhitespace()
    {
        assertThat(Helpers.containsWhitespace(" ")).isTrue();
        assertThat(Helpers.containsWhitespace("testing with spaces")).isTrue();
        assertThat(Helpers.containsWhitespace(null)).isFalse();
        assertThat(Helpers.containsWhitespace("")).isFalse();
        assertThat(Helpers.containsWhitespace("null")).isFalse();
    }

    @Test
    void testIsBlank()
    {
        assertThat(Helpers.isBlank(" ")).isTrue();
        assertThat(Helpers.isBlank(null)).isTrue();
        assertThat(Helpers.isBlank("")).isTrue();
        assertThat(Helpers.isBlank("testing with spaces")).isFalse();
        assertThat(Helpers.isBlank("null")).isFalse();
    }

    @Test
    void testCountMatches()
    {
        assertThat(Helpers.countMatches("Hello World", 'l')).isEqualTo(3);
        assertThat(Helpers.countMatches("Hello World", ' ')).isEqualTo(1);
        assertThat(Helpers.countMatches("Hello World", '_')).isEqualTo(0);
        assertThat(Helpers.countMatches("", '!')).isEqualTo(0);
        assertThat(Helpers.countMatches(null, '?')).isEqualTo(0);
    }

    @Test
    void testTruncate()
    {
        assertThat(Helpers.truncate("Hello World", 5)).isEqualTo("Hello");
        assertThat(Helpers.truncate("Hello", 5)).isEqualTo("Hello");
        assertThat(Helpers.truncate("Hello", 10)).isEqualTo("Hello");
        assertThat(Helpers.truncate("", 10)).isEqualTo("");
        assertThat(Helpers.truncate("Test", 0)).isEqualTo("");
        assertThat(Helpers.truncate(null, 10)).isNull();
    }

    @Test
    void testRightPad()
    {
        assertThat(Helpers.rightPad("Hello", 9)).isEqualTo("Hello    ");
        assertThat(Helpers.rightPad("Hello World", 9)).isEqualTo("Hello World");
        assertThat(Helpers.rightPad("Hello", 5)).isEqualTo("Hello");
    }

    @Test
    void testLeftPad()
    {
        assertThat(Helpers.leftPad("Hello", 9)).isEqualTo("    Hello");
        assertThat(Helpers.leftPad("Hello World", 9)).isEqualTo("Hello World");
        assertThat(Helpers.leftPad("Hello", 5)).isEqualTo("Hello");
    }

    @Test
    void testIsNumeric()
    {
        assertThat(Helpers.isNumeric("10")).isTrue();
        assertThat(Helpers.isNumeric("1")).isTrue();
        assertThat(Helpers.isNumeric("0")).isTrue();
        assertThat(Helpers.isNumeric(String.valueOf(Long.MAX_VALUE))).isTrue();
        assertThat(Helpers.isNumeric(null)).isFalse();
        assertThat(Helpers.isNumeric("")).isFalse();
        assertThat(Helpers.isNumeric("Test")).isFalse();
        assertThat(Helpers.isNumeric("1.0")).isFalse();
        assertThat(Helpers.isNumeric("1e10")).isFalse();
    }

    @Test
    void testDeepEquals()
    {
        List<String> a = Arrays.asList("A", "B", "C");
        List<String> b = Arrays.asList("B", "A", "C");
        List<String> c = Arrays.asList("A", "B");
        List<String> d = Arrays.asList("A", "B", "C");

        assertThat(Helpers.deepEquals(a, a)).isTrue();
        assertThat(Helpers.deepEquals(a, d)).isTrue();
        assertThat(Helpers.deepEqualsUnordered(a, b)).isTrue();
        assertThat(Helpers.deepEquals(a, b)).isFalse();
        assertThat(Helpers.deepEquals(a, c)).isFalse();
        assertThat(Helpers.deepEqualsUnordered(b, c)).isFalse();
    }
}
