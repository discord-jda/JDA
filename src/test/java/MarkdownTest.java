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

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkdownTest
{
    private MarkdownSanitizer markdown;

    @BeforeEach
    public void setup()
    {
        markdown = new MarkdownSanitizer().withStrategy(MarkdownSanitizer.SanitizationStrategy.REMOVE);
    }

    @Test
    public void testComplex()
    {
        assertEquals("ABCDEF", markdown.compute("**A_B||C~~D__E`F`__~~||_**"));
    }

    @Test
    public void testTrivial()
    {
        assertEquals("", markdown.compute(""));
        assertEquals("Hello World ~~~~", markdown.compute("Hello World ~~~~"));
        assertEquals("Hello World ~", markdown.compute("Hello World ~~~~~"));
    }

    @Test
    public void testBold()
    {
        assertEquals("Hello", markdown.compute("**Hello**"));
        assertEquals("**Hello", markdown.compute("**Hello"));
        assertEquals("\\**Hello**", markdown.compute("\\**Hello**"));
    }

    @Test
    public void testItalics()
    {
        assertEquals("Hello", markdown.compute("*Hello*"));
        assertEquals("Hello", markdown.compute("_Hello_"));

        assertEquals("*Hello", markdown.compute("*Hello"));
        assertEquals("_Hello", markdown.compute("_Hello"));

        assertEquals("\\*Hello*", markdown.compute("\\*Hello*"));
        assertEquals("\\_Hello_", markdown.compute("\\_Hello_"));
    }

    @Test
    public void testBoldItalics()
    {
        assertEquals("Hello", markdown.compute("***Hello***"));
        assertEquals("***Hello", markdown.compute("***Hello"));
        assertEquals("\\***Hello***", markdown.compute("\\***Hello***"));
    }

    @Test
    public void testUnderline()
    {
        assertEquals("Hello", markdown.compute("__Hello__"));
        assertEquals("__Hello", markdown.compute("__Hello"));
        assertEquals("\\__Hello__", markdown.compute("\\__Hello__"));
    }

    @Test
    public void testStrike()
    {
        assertEquals("Hello", markdown.compute("~~Hello~~"));
        assertEquals("~~Hello", markdown.compute("~~Hello"));
        assertEquals("\\~~Hello~~", markdown.compute("\\~~Hello~~"));
    }

    @Test
    public void testSpoiler()
    {
        assertEquals("Hello", markdown.compute("||Hello||"));
        assertEquals("||Hello", markdown.compute("||Hello"));
        assertEquals("\\||Hello||", markdown.compute("\\||Hello||"));
    }

    @Test
    public void testMono()
    {
        assertEquals("Hello", markdown.compute("`Hello`"));
        assertEquals("`Hello", markdown.compute("`Hello"));
        assertEquals("\\`Hello`", markdown.compute("\\`Hello`"));

        assertEquals("Hello **World**", markdown.compute("`Hello **World**`"));
        assertEquals("`Hello World", markdown.compute("`Hello **World**"));
        assertEquals("\\`Hello World`", markdown.compute("\\`Hello **World**`"));
    }

    @Test
    public void testMonoTwo()
    {
        assertEquals("Hello", markdown.compute("``Hello``"));
        assertEquals("``Hello", markdown.compute("``Hello"));
        assertEquals("\\``Hello``", markdown.compute("\\``Hello``"));

        assertEquals("Hello **World**", markdown.compute("``Hello **World**``"));
        assertEquals("``Hello World", markdown.compute("``Hello **World**"));
        assertEquals("\\``Hello World``", markdown.compute("\\``Hello **World**``"));

        assertEquals("Hello `to` World", markdown.compute("``Hello `to` World``"));
        assertEquals("``Hello to World", markdown.compute("``Hello `to` World"));
        assertEquals("\\``Hello to World``", markdown.compute("\\``Hello `to` World``"));
    }

    @Test
    public void testBlock()
    {
        assertEquals("Hello", markdown.compute("```Hello```"));
        assertEquals("```Hello", markdown.compute("```Hello"));
        assertEquals("\\```Hello```", markdown.compute("\\```Hello```"));

        assertEquals("Hello **World**", markdown.compute("```Hello **World**```"));
        assertEquals("```Hello World", markdown.compute("```Hello **World**"));
        assertEquals("\\```Hello World```", markdown.compute("\\```Hello **World**```"));

        assertEquals("Hello `to` World", markdown.compute("```Hello `to` World```"));
        assertEquals("```Hello to World", markdown.compute("```Hello `to` World"));
        assertEquals("\\```Hello to World```", markdown.compute("\\```Hello `to` World```"));

        assertEquals("Test", markdown.compute("```java\nTest```"));
    }

    @Test
    public void testQuote()
    {
        assertEquals("Hello > World", markdown.compute("> Hello > World"));
        assertEquals("Hello\nWorld", markdown.compute("> Hello\n> World"));
        assertEquals("Hello\nWorld", markdown.compute(">>> Hello\nWorld"));
        assertEquals("Hello\nWorld", markdown.compute(">>>\nHello\nWorld"));
        assertEquals("Hello > World", markdown.compute(">>>\nHello > World"));
        assertEquals("Hello\n World", markdown.compute("Hello\n > World"));
    }
}

class IgnoreMarkdownTest
{
    private MarkdownSanitizer markdown;

    @BeforeEach
    public void setup()
    {
        markdown = new MarkdownSanitizer().withIgnored(0xFFFFFFFF);
    }

    @Test
    public void testComplex()
    {
        assertEquals("**A_B||C~~D__E`F`__~~||_**", markdown.compute("**A_B||C~~D__E`F`__~~||_**"));
    }

    @Test
    public void testBold()
    {
        assertEquals("**Hello**", markdown.compute("**Hello**"));
        assertEquals("**Hello", markdown.compute("**Hello"));
    }

    @Test
    public void testItalics()
    {
        assertEquals("*Hello*", markdown.compute("*Hello*"));
        assertEquals("_Hello_", markdown.compute("_Hello_"));

        assertEquals("*Hello", markdown.compute("*Hello"));
        assertEquals("_Hello", markdown.compute("_Hello"));
    }

    @Test
    public void testBoldItalics()
    {
        assertEquals("***Hello***", markdown.compute("***Hello***"));
        assertEquals("***Hello", markdown.compute("***Hello"));
        assertEquals("\\***Hello***", markdown.compute("\\***Hello***"));
    }

    @Test
    public void testUnderline()
    {
        assertEquals("__Hello__", markdown.compute("__Hello__"));
        assertEquals("__Hello", markdown.compute("__Hello"));
    }

    @Test
    public void testStrike()
    {
        assertEquals("~~Hello~~", markdown.compute("~~Hello~~"));
        assertEquals("~~Hello", markdown.compute("~~Hello"));
    }

    @Test
    public void testSpoiler()
    {
        assertEquals("||Hello||", markdown.compute("||Hello||"));
        assertEquals("||Hello", markdown.compute("||Hello"));
    }

    @Test
    public void testMono()
    {
        assertEquals("`Hello`", markdown.compute("`Hello`"));
        assertEquals("`Hello", markdown.compute("`Hello"));

        assertEquals("`Hello **World**`", markdown.compute("`Hello **World**`"));
        assertEquals("`Hello **World**", markdown.compute("`Hello **World**"));
    }

    @Test
    public void testMonoTwo()
    {
        assertEquals("``Hello``", markdown.compute("``Hello``"));
        assertEquals("``Hello", markdown.compute("``Hello"));

        assertEquals("``Hello **World**``", markdown.compute("``Hello **World**``"));
        assertEquals("``Hello **World**", markdown.compute("``Hello **World**"));

        assertEquals("``Hello `to` World``", markdown.compute("``Hello `to` World``"));
        assertEquals("``Hello `to` World", markdown.compute("``Hello `to` World"));
    }

    @Test
    public void testBlock()
    {
        assertEquals("```Hello```", markdown.compute("```Hello```"));
        assertEquals("```Hello", markdown.compute("```Hello"));

        assertEquals("```Hello **World**```", markdown.compute("```Hello **World**```"));
        assertEquals("```Hello **World**", markdown.compute("```Hello **World**"));

        assertEquals("```Hello `to` World```", markdown.compute("```Hello `to` World```"));
        assertEquals("```Hello `to` World", markdown.compute("```Hello `to` World"));

        assertEquals("```java\nTest```", markdown.compute("```java\nTest```"));
    }

    @Test
    public void testQuote()
    {
        assertEquals("> Hello > World", markdown.compute("> Hello > World"));
        assertEquals("> Hello\n> World", markdown.compute("> Hello\n> World"));
        assertEquals(">>> Hello\nWorld", markdown.compute(">>> Hello\nWorld"));
        assertEquals(">>>\nHello\nWorld", markdown.compute(">>>\nHello\nWorld"));
        assertEquals(">>>\nHello > World", markdown.compute(">>>\nHello > World"));
        assertEquals("Hello\n > World", markdown.compute("Hello\n > World"));
    }
}

class EscapeMarkdownTest
{
    private MarkdownSanitizer markdown;

    @BeforeEach
    public void setup()
    {
        markdown = new MarkdownSanitizer().withStrategy(MarkdownSanitizer.SanitizationStrategy.ESCAPE);
    }

    @Test
    public void testComplex()
    {
        assertEquals("\\*\\*A\\_B\\||C\\~~D\\_\\_E\\`F\\`\\_\\_\\~~\\||\\_\\*\\*", markdown.compute("**A_B||C~~D__E`F`__~~||_**"));
    }

    @Test
    public void testBold()
    {
        assertEquals("\\*\\*Hello\\*\\*", markdown.compute("**Hello**"));
        assertEquals("**Hello", markdown.compute("**Hello"));
        assertEquals("\\**Hello**", markdown.compute("\\**Hello**"));
    }

    @Test
    public void testItalics()
    {
        assertEquals("\\*Hello\\*", markdown.compute("*Hello*"));
        assertEquals("\\_Hello\\_", markdown.compute("_Hello_"));

        assertEquals("*Hello", markdown.compute("*Hello"));
        assertEquals("_Hello", markdown.compute("_Hello"));

        assertEquals("\\*Hello*", markdown.compute("\\*Hello*"));
        assertEquals("\\_Hello_", markdown.compute("\\_Hello_"));
    }

    @Test
    public void testBoldItalics()
    {
        assertEquals("\\*\\*\\*Hello\\*\\*\\*", markdown.compute("***Hello***"));
        assertEquals("***Hello", markdown.compute("***Hello"));
        assertEquals("\\***Hello***", markdown.compute("\\***Hello***"));
    }

    @Test
    public void testUnderline()
    {
        assertEquals("\\_\\_Hello\\_\\_", markdown.compute("__Hello__"));
        assertEquals("__Hello", markdown.compute("__Hello"));
        assertEquals("\\__Hello__", markdown.compute("\\__Hello__"));
    }

    @Test
    public void testStrike()
    {
        assertEquals("\\~~Hello\\~~", markdown.compute("~~Hello~~"));
        assertEquals("~~Hello", markdown.compute("~~Hello"));
        assertEquals("\\~~Hello~~", markdown.compute("\\~~Hello~~"));
    }

    @Test
    public void testSpoiler()
    {
        assertEquals("\\||Hello\\||", markdown.compute("||Hello||"));
        assertEquals("||Hello", markdown.compute("||Hello"));
        assertEquals("\\||Hello||", markdown.compute("\\||Hello||"));
    }

    @Test
    public void testMono()
    {
        assertEquals("\\`Hello\\`", markdown.compute("`Hello`"));
        assertEquals("`Hello", markdown.compute("`Hello"));
        assertEquals("\\`Hello`", markdown.compute("\\`Hello`"));

        assertEquals("\\`Hello **World**\\`", markdown.compute("`Hello **World**`"));
        assertEquals("`Hello \\*\\*World\\*\\*", markdown.compute("`Hello **World**"));
        assertEquals("\\`Hello \\*\\*World\\*\\*`", markdown.compute("\\`Hello **World**`"));

    }

    @Test
    public void testMonoTwo()
    {
        assertEquals("\\``Hello\\``", markdown.compute("``Hello``"));
        assertEquals("``Hello", markdown.compute("``Hello"));
        assertEquals("\\``Hello``", markdown.compute("\\``Hello``"));

        assertEquals("\\``Hello **World**\\``", markdown.compute("``Hello **World**``"));
        assertEquals("``Hello \\*\\*World\\*\\*", markdown.compute("``Hello **World**"));
        assertEquals("\\``Hello \\*\\*World\\*\\*``", markdown.compute("\\``Hello **World**``"));

        assertEquals("\\``Hello `to` World\\``", markdown.compute("``Hello `to` World``"));
        assertEquals("``Hello \\`to\\` World", markdown.compute("``Hello `to` World"));
        assertEquals("\\``Hello \\`to\\` World", markdown.compute("\\``Hello `to` World"));
    }

    @Test
    public void testBlock()
    {
        assertEquals("\\```Hello\\```", markdown.compute("```Hello```"));
        assertEquals("```Hello", markdown.compute("```Hello"));
        assertEquals("\\```Hello", markdown.compute("\\```Hello"));

        assertEquals("\\```Hello **World**\\```", markdown.compute("```Hello **World**```"));
        assertEquals("```Hello \\*\\*World\\*\\*", markdown.compute("```Hello **World**"));
        assertEquals("\\```Hello \\*\\*World\\*\\*", markdown.compute("\\```Hello **World**"));

        assertEquals("\\```Hello `to` World\\```", markdown.compute("```Hello `to` World```"));
        assertEquals("```Hello \\`to\\` World", markdown.compute("```Hello `to` World"));
        assertEquals("\\```Hello \\`to\\` World", markdown.compute("\\```Hello `to` World"));

        assertEquals("\\```java\nTest\\```", markdown.compute("```java\nTest```"));
    }

    @Test
    public void testQuote()
    {
        assertEquals("\\> Hello > World", markdown.compute("> Hello > World"));
        assertEquals("\\> Hello\n\\> World", markdown.compute("> Hello\n> World"));
        assertEquals("\\>>> Hello\nWorld", markdown.compute(">>> Hello\nWorld"));
        assertEquals("\\>>>\nHello\nWorld", markdown.compute(">>>\nHello\nWorld"));
        assertEquals("\\>>>\nHello > World", markdown.compute(">>>\nHello > World"));
        assertEquals("\\> \\_Hello \n\\> World\\_", markdown.compute("> _Hello \n> World_"));
        assertEquals("Hello\n \\> World", markdown.compute("Hello\n > World"));
    }
}

class EscapeMarkdownAllTest
{
    @Test
    public void testAsterisk()
    {
        assertEquals("Hello\\*World", MarkdownSanitizer.escape("Hello*World", true));
        assertEquals("Hello\\*\\*World", MarkdownSanitizer.escape("Hello**World", true));
        assertEquals("Hello\\*\\*\\*World", MarkdownSanitizer.escape("Hello***World", true));

        assertEquals("Hello\\*World", MarkdownSanitizer.escape("Hello\\*World", true));
        assertEquals("Hello\\*\\*World", MarkdownSanitizer.escape("Hello\\*\\*World", true));
        assertEquals("Hello\\*\\*\\*World", MarkdownSanitizer.escape("Hello\\*\\*\\*World", true));
    }

    @Test
    public void testUnderscore()
    {
        assertEquals("Hello\\_World", MarkdownSanitizer.escape("Hello_World", true));
        assertEquals("Hello\\_\\_World", MarkdownSanitizer.escape("Hello__World", true));
        assertEquals("Hello\\_\\_\\_World", MarkdownSanitizer.escape("Hello___World", true));

        assertEquals("Hello\\_World", MarkdownSanitizer.escape("Hello\\_World", true));
        assertEquals("Hello\\_\\_World", MarkdownSanitizer.escape("Hello\\_\\_World", true));
        assertEquals("Hello\\_\\_\\_World", MarkdownSanitizer.escape("Hello\\_\\_\\_World", true));
    }

    @Test
    public void testCodeBlock()
    {
        assertEquals("Hello\\`World", MarkdownSanitizer.escape("Hello`World", true));
        assertEquals("Hello\\`\\`World", MarkdownSanitizer.escape("Hello``World", true));
        assertEquals("Hello\\`\\`\\`World", MarkdownSanitizer.escape("Hello```World", true));

        assertEquals("Hello\\`World", MarkdownSanitizer.escape("Hello\\`World", true));
        assertEquals("Hello\\`\\`World", MarkdownSanitizer.escape("Hello\\`\\`World", true));
        assertEquals("Hello\\`\\`\\`World", MarkdownSanitizer.escape("Hello\\`\\`\\`World", true));
    }

    @Test
    public void testSpoiler()
    {
        assertEquals("Hello\\|\\|World", MarkdownSanitizer.escape("Hello||World", true));
        assertEquals("Hello|World", MarkdownSanitizer.escape("Hello|World", true));

        assertEquals("Hello\\|\\|World", MarkdownSanitizer.escape("Hello\\|\\|World", true));
        assertEquals("Hello\\|World", MarkdownSanitizer.escape("Hello\\|World", true));
    }

    @Test
    public void testStrike()
    {
        assertEquals("Hello\\~\\~World", MarkdownSanitizer.escape("Hello~~World", true));
        assertEquals("Hello\\~\\~World", MarkdownSanitizer.escape("Hello\\~\\~World", true));
    }

    @Test
    public void testQuote()
    {
        assertEquals("\\> Hello World", MarkdownSanitizer.escape("> Hello World", true));
        assertEquals(">Hello World", MarkdownSanitizer.escape(">Hello World", true));
        assertEquals("\\>\\>\\> Hello World", MarkdownSanitizer.escape(">>> Hello World", true));
        assertEquals(">>>Hello World", MarkdownSanitizer.escape(">>>Hello World", true));
        assertEquals("\\>\\>\\> Hello > World\n\\> Hello >>> World\n<@12345> > Hello\n \\> Hello world", MarkdownSanitizer.escape(">>> Hello > World\n> Hello >>> World\n<@12345> > Hello\n > Hello world", true));

        assertEquals("\\> Hello World", MarkdownSanitizer.escape("\\> Hello World", true));
        assertEquals("\\>\\>\\> Hello World", MarkdownSanitizer.escape("\\>\\>\\> Hello World", true));
        assertEquals("Hello > World", MarkdownSanitizer.escape("Hello > World"));
        assertEquals("Hello\n \\> World", MarkdownSanitizer.escape("Hello\n > World"));
        assertEquals("Hello\n\\> World", MarkdownSanitizer.escape("Hello\n> World"));
    }
}
