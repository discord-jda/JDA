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

import net.dv8tion.jda.internal.utils.Helpers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HelpersTest
{
    @Test
    public void testIsEmpty()
    {
        assertTrue(Helpers.isEmpty(null));
        assertTrue(Helpers.isEmpty(""));
        assertFalse(Helpers.isEmpty("null"));
        assertFalse(Helpers.isEmpty("testing with spaces"));
    }

    @Test
    public void testContainsWhitespace()
    {
        assertTrue(Helpers.containsWhitespace(" "));
        assertTrue(Helpers.containsWhitespace("testing with spaces"));
        assertFalse(Helpers.containsWhitespace(null));
        assertFalse(Helpers.containsWhitespace(""));
        assertFalse(Helpers.containsWhitespace("null"));
    }

    @Test
    public void testIsBlank()
    {
        assertTrue(Helpers.isBlank(" "));
        assertTrue(Helpers.isBlank(null));
        assertTrue(Helpers.isBlank(""));
        assertFalse(Helpers.isBlank("testing with spaces"));
        assertFalse(Helpers.isBlank("null"));
    }

    @Test
    public void testCountMatches()
    {
        assertEquals(3, Helpers.countMatches("Hello World", 'l'));
        assertEquals(1, Helpers.countMatches("Hello World", ' '));
        assertEquals(0, Helpers.countMatches("Hello World", '_'));
        assertEquals(0, Helpers.countMatches("", '!'));
        assertEquals(0, Helpers.countMatches(null, '?'));
    }

    @Test
    public void testTruncate()
    {
        assertEquals("Hello", Helpers.truncate("Hello World", 5));
        assertEquals("Hello", Helpers.truncate("Hello", 5));
        assertEquals("Hello", Helpers.truncate("Hello", 10));
        assertEquals("", Helpers.truncate("", 10));
        assertEquals("", Helpers.truncate("Test", 0));
        assertNull(Helpers.truncate(null, 10));
    }

    @Test
    public void testRightPad()
    {
        assertEquals("Hello    ", Helpers.rightPad("Hello", 9));
        assertEquals("Hello World", Helpers.rightPad("Hello World", 9));
        assertEquals("Hello", Helpers.rightPad("Hello", 5));
    }

    @Test
    public void testLeftPad()
    {
        assertEquals("    Hello", Helpers.leftPad("Hello", 9));
        assertEquals("Hello World", Helpers.leftPad("Hello World", 9));
        assertEquals("Hello", Helpers.leftPad("Hello", 5));
    }

    @Test
    public void testIsNumeric()
    {
        assertTrue(Helpers.isNumeric("10"));
        assertTrue(Helpers.isNumeric("1"));
        assertTrue(Helpers.isNumeric("0"));
        assertTrue(Helpers.isNumeric(String.valueOf(Long.MAX_VALUE)));
        assertFalse(Helpers.isNumeric(null));
        assertFalse(Helpers.isNumeric(""));
        assertFalse(Helpers.isNumeric("Test"));
        assertFalse(Helpers.isNumeric("1.0"));
        assertFalse(Helpers.isNumeric("1e10"));
    }

    @Test
    public void testDeepEquals()
    {
        List<String> a = Arrays.asList("A", "B", "C");
        List<String> b = Arrays.asList("B", "A", "C");
        List<String> c = Arrays.asList("A", "B");
        List<String> d = Arrays.asList("A", "B", "C");

        assertTrue(Helpers.deepEquals(a, a));
        assertTrue(Helpers.deepEquals(a, d));
        assertTrue(Helpers.deepEqualsUnordered(a, b));
        assertFalse(Helpers.deepEquals(a, b));
        assertFalse(Helpers.deepEquals(a, c));
        assertFalse(Helpers.deepEqualsUnordered(b, c));
    }
}
