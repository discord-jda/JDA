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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class HelpersTest
{
    @Test
    public void testIsEmpty()
    {
        Assertions.assertTrue(Helpers.isEmpty(null));
        Assertions.assertTrue(Helpers.isEmpty(""));
        Assertions.assertFalse(Helpers.isEmpty("null"));
        Assertions.assertFalse(Helpers.isEmpty("testing with spaces"));
    }

    @Test
    public void testContainsWhitespace()
    {
        Assertions.assertTrue(Helpers.containsWhitespace(" "));
        Assertions.assertTrue(Helpers.containsWhitespace("testing with spaces"));
        Assertions.assertFalse(Helpers.containsWhitespace(null));
        Assertions.assertFalse(Helpers.containsWhitespace(""));
        Assertions.assertFalse(Helpers.containsWhitespace("null"));
    }

    @Test
    public void testIsBlank()
    {
        Assertions.assertTrue(Helpers.isBlank(" "));
        Assertions.assertTrue(Helpers.isBlank(null));
        Assertions.assertTrue(Helpers.isBlank(""));
        Assertions.assertFalse(Helpers.isBlank("testing with spaces"));
        Assertions.assertFalse(Helpers.isBlank("null"));
    }

    @Test
    public void testCountMatches()
    {
        Assertions.assertEquals(3, Helpers.countMatches("Hello World", 'l'));
        Assertions.assertEquals(1, Helpers.countMatches("Hello World", ' '));
        Assertions.assertEquals(0, Helpers.countMatches("Hello World", '_'));
        Assertions.assertEquals(0, Helpers.countMatches("", '!'));
        Assertions.assertEquals(0, Helpers.countMatches(null, '?'));
    }

    @Test
    public void testTruncate()
    {
        Assertions.assertEquals("Hello", Helpers.truncate("Hello World", 5));
        Assertions.assertEquals("Hello", Helpers.truncate("Hello", 5));
        Assertions.assertEquals("Hello", Helpers.truncate("Hello", 10));
        Assertions.assertEquals("", Helpers.truncate("", 10));
        Assertions.assertEquals("", Helpers.truncate("Test", 0));
        Assertions.assertNull(Helpers.truncate(null, 10));
    }

    @Test
    public void testRightPad()
    {
        Assertions.assertEquals("Hello    ", Helpers.rightPad("Hello", 9));
        Assertions.assertEquals("Hello World", Helpers.rightPad("Hello World", 9));
        Assertions.assertEquals("Hello", Helpers.rightPad("Hello", 5));
    }

    @Test
    public void testLeftPad()
    {
        Assertions.assertEquals("    Hello", Helpers.leftPad("Hello", 9));
        Assertions.assertEquals("Hello World", Helpers.leftPad("Hello World", 9));
        Assertions.assertEquals("Hello", Helpers.leftPad("Hello", 5));
    }

    @Test
    public void testIsNumeric()
    {
        Assertions.assertTrue(Helpers.isNumeric("10"));
        Assertions.assertTrue(Helpers.isNumeric("1"));
        Assertions.assertTrue(Helpers.isNumeric("0"));
        Assertions.assertTrue(Helpers.isNumeric(String.valueOf(Long.MAX_VALUE)));
        Assertions.assertFalse(Helpers.isNumeric(null));
        Assertions.assertFalse(Helpers.isNumeric(""));
        Assertions.assertFalse(Helpers.isNumeric("Test"));
        Assertions.assertFalse(Helpers.isNumeric("1.0"));
        Assertions.assertFalse(Helpers.isNumeric("1e10"));
    }

    @Test
    public void testDeepEquals()
    {
        List<String> a = Arrays.asList("A", "B", "C");
        List<String> b = Arrays.asList("B", "A", "C");
        List<String> c = Arrays.asList("A", "B");
        List<String> d = Arrays.asList("A", "B", "C");

        Assertions.assertTrue(Helpers.deepEquals(a, a));
        Assertions.assertTrue(Helpers.deepEquals(a, d));
        Assertions.assertTrue(Helpers.deepEqualsUnordered(a, b));
        Assertions.assertFalse(Helpers.deepEquals(a, b));
        Assertions.assertFalse(Helpers.deepEquals(a, c));
        Assertions.assertFalse(Helpers.deepEqualsUnordered(b, c));
    }
}
