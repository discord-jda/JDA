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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals("ABCDEF", markdown.compute("**A_B||C~~D__E`F`__~~||_**"));
    }

    @Test
    public void testTrivial()
    {
        Assertions.assertEquals("", markdown.compute(""));
        Assertions.assertEquals("Hello World ~~~~", markdown.compute("Hello World ~~~~"));
        Assertions.assertEquals("Hello World ~", markdown.compute("Hello World ~~~~~"));
    }

    @Test
    public void testBold()
    {
        Assertions.assertEquals("Hello", markdown.compute("**Hello**"));
        Assertions.assertEquals("**Hello", markdown.compute("**Hello"));
        Assertions.assertEquals("\\**Hello**", markdown.compute("\\**Hello**"));
    }

    @Test
    public void testItalics()
    {
        Assertions.assertEquals("Hello", markdown.compute("*Hello*"));
        Assertions.assertEquals("Hello", markdown.compute("_Hello_"));

        Assertions.assertEquals("*Hello", markdown.compute("*Hello"));
        Assertions.assertEquals("_Hello", markdown.compute("_Hello"));

        Assertions.assertEquals("\\*Hello*", markdown.compute("\\*Hello*"));
        Assertions.assertEquals("\\_Hello_", markdown.compute("\\_Hello_"));
    }

    @Test
    public void testBoldItalics()
    {
        Assertions.assertEquals("Hello", markdown.compute("***Hello***"));
        Assertions.assertEquals("***Hello", markdown.compute("***Hello"));
        Assertions.assertEquals("\\***Hello***", markdown.compute("\\***Hello***"));
    }

    @Test
    public void testUnderline()
    {
        Assertions.assertEquals("Hello", markdown.compute("__Hello__"));
        Assertions.assertEquals("__Hello", markdown.compute("__Hello"));
        Assertions.assertEquals("\\__Hello__", markdown.compute("\\__Hello__"));
    }

    @Test
    public void testStrike()
    {
        Assertions.assertEquals("Hello", markdown.compute("~~Hello~~"));
        Assertions.assertEquals("~~Hello", markdown.compute("~~Hello"));
        Assertions.assertEquals("\\~~Hello~~", markdown.compute("\\~~Hello~~"));
    }

    @Test
    public void testSpoiler()
    {
        Assertions.assertEquals("Hello", markdown.compute("||Hello||"));
        Assertions.assertEquals("||Hello", markdown.compute("||Hello"));
        Assertions.assertEquals("\\||Hello||", markdown.compute("\\||Hello||"));
    }

    @Test
    public void testMono()
    {
        Assertions.assertEquals("Hello", markdown.compute("`Hello`"));
        Assertions.assertEquals("`Hello", markdown.compute("`Hello"));
        Assertions.assertEquals("\\`Hello`", markdown.compute("\\`Hello`"));

        Assertions.assertEquals("Hello **World**", markdown.compute("`Hello **World**`"));
        Assertions.assertEquals("`Hello World", markdown.compute("`Hello **World**"));
        Assertions.assertEquals("\\`Hello World`", markdown.compute("\\`Hello **World**`"));
    }

    @Test
    public void testMonoTwo()
    {
        Assertions.assertEquals("Hello", markdown.compute("``Hello``"));
        Assertions.assertEquals("``Hello", markdown.compute("``Hello"));
        Assertions.assertEquals("\\``Hello``", markdown.compute("\\``Hello``"));

        Assertions.assertEquals("Hello **World**", markdown.compute("``Hello **World**``"));
        Assertions.assertEquals("``Hello World", markdown.compute("``Hello **World**"));
        Assertions.assertEquals("\\``Hello World``", markdown.compute("\\``Hello **World**``"));

        Assertions.assertEquals("Hello `to` World", markdown.compute("``Hello `to` World``"));
        Assertions.assertEquals("``Hello to World", markdown.compute("``Hello `to` World"));
        Assertions.assertEquals("\\``Hello to World``", markdown.compute("\\``Hello `to` World``"));
    }

    @Test
    public void testBlock()
    {
        Assertions.assertEquals("Hello", markdown.compute("```Hello```"));
        Assertions.assertEquals("```Hello", markdown.compute("```Hello"));
        Assertions.assertEquals("\\```Hello```", markdown.compute("\\```Hello```"));

        Assertions.assertEquals("Hello **World**", markdown.compute("```Hello **World**```"));
        Assertions.assertEquals("```Hello World", markdown.compute("```Hello **World**"));
        Assertions.assertEquals("\\```Hello World```", markdown.compute("\\```Hello **World**```"));

        Assertions.assertEquals("Hello `to` World", markdown.compute("```Hello `to` World```"));
        Assertions.assertEquals("```Hello to World", markdown.compute("```Hello `to` World"));
        Assertions.assertEquals("\\```Hello to World```", markdown.compute("\\```Hello `to` World```"));

        Assertions.assertEquals("Test", markdown.compute("```java\nTest```"));
    }

    @Test
    public void testQuote()
    {
        Assertions.assertEquals("Hello > World", markdown.compute("> Hello > World"));
        Assertions.assertEquals("Hello\nWorld", markdown.compute("> Hello\n> World"));
        Assertions.assertEquals("Hello\nWorld", markdown.compute(">>> Hello\nWorld"));
        Assertions.assertEquals("Hello\nWorld", markdown.compute(">>>\nHello\nWorld"));
        Assertions.assertEquals("Hello > World", markdown.compute(">>>\nHello > World"));
        Assertions.assertEquals("Hello\n World", markdown.compute("Hello\n > World"));
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
        Assertions.assertEquals("**A_B||C~~D__E`F`__~~||_**", markdown.compute("**A_B||C~~D__E`F`__~~||_**"));
    }

    @Test
    public void testBold()
    {
        Assertions.assertEquals("**Hello**", markdown.compute("**Hello**"));
        Assertions.assertEquals("**Hello", markdown.compute("**Hello"));
    }

    @Test
    public void testItalics()
    {
        Assertions.assertEquals("*Hello*", markdown.compute("*Hello*"));
        Assertions.assertEquals("_Hello_", markdown.compute("_Hello_"));

        Assertions.assertEquals("*Hello", markdown.compute("*Hello"));
        Assertions.assertEquals("_Hello", markdown.compute("_Hello"));
    }

    @Test
    public void testBoldItalics()
    {
        Assertions.assertEquals("***Hello***", markdown.compute("***Hello***"));
        Assertions.assertEquals("***Hello", markdown.compute("***Hello"));
        Assertions.assertEquals("\\***Hello***", markdown.compute("\\***Hello***"));
    }

    @Test
    public void testUnderline()
    {
        Assertions.assertEquals("__Hello__", markdown.compute("__Hello__"));
        Assertions.assertEquals("__Hello", markdown.compute("__Hello"));
    }

    @Test
    public void testStrike()
    {
        Assertions.assertEquals("~~Hello~~", markdown.compute("~~Hello~~"));
        Assertions.assertEquals("~~Hello", markdown.compute("~~Hello"));
    }

    @Test
    public void testSpoiler()
    {
        Assertions.assertEquals("||Hello||", markdown.compute("||Hello||"));
        Assertions.assertEquals("||Hello", markdown.compute("||Hello"));
    }

    @Test
    public void testMono()
    {
        Assertions.assertEquals("`Hello`", markdown.compute("`Hello`"));
        Assertions.assertEquals("`Hello", markdown.compute("`Hello"));

        Assertions.assertEquals("`Hello **World**`", markdown.compute("`Hello **World**`"));
        Assertions.assertEquals("`Hello **World**", markdown.compute("`Hello **World**"));
    }

    @Test
    public void testMonoTwo()
    {
        Assertions.assertEquals("``Hello``", markdown.compute("``Hello``"));
        Assertions.assertEquals("``Hello", markdown.compute("``Hello"));

        Assertions.assertEquals("``Hello **World**``", markdown.compute("``Hello **World**``"));
        Assertions.assertEquals("``Hello **World**", markdown.compute("``Hello **World**"));

        Assertions.assertEquals("``Hello `to` World``", markdown.compute("``Hello `to` World``"));
        Assertions.assertEquals("``Hello `to` World", markdown.compute("``Hello `to` World"));
    }

    @Test
    public void testBlock()
    {
        Assertions.assertEquals("```Hello```", markdown.compute("```Hello```"));
        Assertions.assertEquals("```Hello", markdown.compute("```Hello"));

        Assertions.assertEquals("```Hello **World**```", markdown.compute("```Hello **World**```"));
        Assertions.assertEquals("```Hello **World**", markdown.compute("```Hello **World**"));

        Assertions.assertEquals("```Hello `to` World```", markdown.compute("```Hello `to` World```"));
        Assertions.assertEquals("```Hello `to` World", markdown.compute("```Hello `to` World"));

        Assertions.assertEquals("```java\nTest```", markdown.compute("```java\nTest```"));
    }

    @Test
    public void testQuote()
    {
        Assertions.assertEquals("> Hello > World", markdown.compute("> Hello > World"));
        Assertions.assertEquals("> Hello\n> World", markdown.compute("> Hello\n> World"));
        Assertions.assertEquals(">>> Hello\nWorld", markdown.compute(">>> Hello\nWorld"));
        Assertions.assertEquals(">>>\nHello\nWorld", markdown.compute(">>>\nHello\nWorld"));
        Assertions.assertEquals(">>>\nHello > World", markdown.compute(">>>\nHello > World"));
        Assertions.assertEquals("Hello\n > World", markdown.compute("Hello\n > World"));
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
        Assertions.assertEquals("\\*\\*A\\_B\\||C\\~~D\\_\\_E\\`F\\`\\_\\_\\~~\\||\\_\\*\\*", markdown.compute("**A_B||C~~D__E`F`__~~||_**"));
    }

    @Test
    public void testBold()
    {
        Assertions.assertEquals("\\*\\*Hello\\*\\*", markdown.compute("**Hello**"));
        Assertions.assertEquals("**Hello", markdown.compute("**Hello"));
        Assertions.assertEquals("\\**Hello**", markdown.compute("\\**Hello**"));
    }

    @Test
    public void testItalics()
    {
        Assertions.assertEquals("\\*Hello\\*", markdown.compute("*Hello*"));
        Assertions.assertEquals("\\_Hello\\_", markdown.compute("_Hello_"));

        Assertions.assertEquals("*Hello", markdown.compute("*Hello"));
        Assertions.assertEquals("_Hello", markdown.compute("_Hello"));

        Assertions.assertEquals("\\*Hello*", markdown.compute("\\*Hello*"));
        Assertions.assertEquals("\\_Hello_", markdown.compute("\\_Hello_"));
    }

    @Test
    public void testBoldItalics()
    {
        Assertions.assertEquals("\\*\\*\\*Hello\\*\\*\\*", markdown.compute("***Hello***"));
        Assertions.assertEquals("***Hello", markdown.compute("***Hello"));
        Assertions.assertEquals("\\***Hello***", markdown.compute("\\***Hello***"));
    }

    @Test
    public void testUnderline()
    {
        Assertions.assertEquals("\\_\\_Hello\\_\\_", markdown.compute("__Hello__"));
        Assertions.assertEquals("__Hello", markdown.compute("__Hello"));
        Assertions.assertEquals("\\__Hello__", markdown.compute("\\__Hello__"));
    }

    @Test
    public void testStrike()
    {
        Assertions.assertEquals("\\~~Hello\\~~", markdown.compute("~~Hello~~"));
        Assertions.assertEquals("~~Hello", markdown.compute("~~Hello"));
        Assertions.assertEquals("\\~~Hello~~", markdown.compute("\\~~Hello~~"));
    }

    @Test
    public void testSpoiler()
    {
        Assertions.assertEquals("\\||Hello\\||", markdown.compute("||Hello||"));
        Assertions.assertEquals("||Hello", markdown.compute("||Hello"));
        Assertions.assertEquals("\\||Hello||", markdown.compute("\\||Hello||"));
    }

    @Test
    public void testMono()
    {
        Assertions.assertEquals("\\`Hello\\`", markdown.compute("`Hello`"));
        Assertions.assertEquals("`Hello", markdown.compute("`Hello"));
        Assertions.assertEquals("\\`Hello`", markdown.compute("\\`Hello`"));

        Assertions.assertEquals("\\`Hello **World**\\`", markdown.compute("`Hello **World**`"));
        Assertions.assertEquals("`Hello \\*\\*World\\*\\*", markdown.compute("`Hello **World**"));
        Assertions.assertEquals("\\`Hello \\*\\*World\\*\\*`", markdown.compute("\\`Hello **World**`"));

    }

    @Test
    public void testMonoTwo()
    {
        Assertions.assertEquals("\\``Hello\\``", markdown.compute("``Hello``"));
        Assertions.assertEquals("``Hello", markdown.compute("``Hello"));
        Assertions.assertEquals("\\``Hello``", markdown.compute("\\``Hello``"));

        Assertions.assertEquals("\\``Hello **World**\\``", markdown.compute("``Hello **World**``"));
        Assertions.assertEquals("``Hello \\*\\*World\\*\\*", markdown.compute("``Hello **World**"));
        Assertions.assertEquals("\\``Hello \\*\\*World\\*\\*``", markdown.compute("\\``Hello **World**``"));

        Assertions.assertEquals("\\``Hello `to` World\\``", markdown.compute("``Hello `to` World``"));
        Assertions.assertEquals("``Hello \\`to\\` World", markdown.compute("``Hello `to` World"));
        Assertions.assertEquals("\\``Hello \\`to\\` World", markdown.compute("\\``Hello `to` World"));
    }

    @Test
    public void testBlock()
    {
        Assertions.assertEquals("\\```Hello\\```", markdown.compute("```Hello```"));
        Assertions.assertEquals("```Hello", markdown.compute("```Hello"));
        Assertions.assertEquals("\\```Hello", markdown.compute("\\```Hello"));

        Assertions.assertEquals("\\```Hello **World**\\```", markdown.compute("```Hello **World**```"));
        Assertions.assertEquals("```Hello \\*\\*World\\*\\*", markdown.compute("```Hello **World**"));
        Assertions.assertEquals("\\```Hello \\*\\*World\\*\\*", markdown.compute("\\```Hello **World**"));

        Assertions.assertEquals("\\```Hello `to` World\\```", markdown.compute("```Hello `to` World```"));
        Assertions.assertEquals("```Hello \\`to\\` World", markdown.compute("```Hello `to` World"));
        Assertions.assertEquals("\\```Hello \\`to\\` World", markdown.compute("\\```Hello `to` World"));

        Assertions.assertEquals("\\```java\nTest\\```", markdown.compute("```java\nTest```"));
    }

    @Test
    public void testQuote()
    {
        Assertions.assertEquals("\\> Hello > World", markdown.compute("> Hello > World"));
        Assertions.assertEquals("\\> Hello\n\\> World", markdown.compute("> Hello\n> World"));
        Assertions.assertEquals("\\>>> Hello\nWorld", markdown.compute(">>> Hello\nWorld"));
        Assertions.assertEquals("\\>>>\nHello\nWorld", markdown.compute(">>>\nHello\nWorld"));
        Assertions.assertEquals("\\>>>\nHello > World", markdown.compute(">>>\nHello > World"));
        Assertions.assertEquals("\\> \\_Hello \n\\> World\\_", markdown.compute("> _Hello \n> World_"));
        Assertions.assertEquals("Hello\n \\> World", markdown.compute("Hello\n > World"));
    }
}

class EscapeMarkdownAllTest
{
    @Test
    public void testAsterisk()
    {
        Assertions.assertEquals("Hello\\*World", MarkdownSanitizer.escape("Hello*World", true));
        Assertions.assertEquals("Hello\\*\\*World", MarkdownSanitizer.escape("Hello**World", true));
        Assertions.assertEquals("Hello\\*\\*\\*World", MarkdownSanitizer.escape("Hello***World", true));

        Assertions.assertEquals("Hello\\*World", MarkdownSanitizer.escape("Hello\\*World", true));
        Assertions.assertEquals("Hello\\*\\*World", MarkdownSanitizer.escape("Hello\\*\\*World", true));
        Assertions.assertEquals("Hello\\*\\*\\*World", MarkdownSanitizer.escape("Hello\\*\\*\\*World", true));
    }

    @Test
    public void testUnderscore()
    {
        Assertions.assertEquals("Hello\\_World", MarkdownSanitizer.escape("Hello_World", true));
        Assertions.assertEquals("Hello\\_\\_World", MarkdownSanitizer.escape("Hello__World", true));
        Assertions.assertEquals("Hello\\_\\_\\_World", MarkdownSanitizer.escape("Hello___World", true));

        Assertions.assertEquals("Hello\\_World", MarkdownSanitizer.escape("Hello\\_World", true));
        Assertions.assertEquals("Hello\\_\\_World", MarkdownSanitizer.escape("Hello\\_\\_World", true));
        Assertions.assertEquals("Hello\\_\\_\\_World", MarkdownSanitizer.escape("Hello\\_\\_\\_World", true));
    }

    @Test
    public void testCodeBlock()
    {
        Assertions.assertEquals("Hello\\`World", MarkdownSanitizer.escape("Hello`World", true));
        Assertions.assertEquals("Hello\\`\\`World", MarkdownSanitizer.escape("Hello``World", true));
        Assertions.assertEquals("Hello\\`\\`\\`World", MarkdownSanitizer.escape("Hello```World", true));

        Assertions.assertEquals("Hello\\`World", MarkdownSanitizer.escape("Hello\\`World", true));
        Assertions.assertEquals("Hello\\`\\`World", MarkdownSanitizer.escape("Hello\\`\\`World", true));
        Assertions.assertEquals("Hello\\`\\`\\`World", MarkdownSanitizer.escape("Hello\\`\\`\\`World", true));
    }

    @Test
    public void testSpoiler()
    {
        Assertions.assertEquals("Hello\\|\\|World", MarkdownSanitizer.escape("Hello||World", true));
        Assertions.assertEquals("Hello|World", MarkdownSanitizer.escape("Hello|World", true));

        Assertions.assertEquals("Hello\\|\\|World", MarkdownSanitizer.escape("Hello\\|\\|World", true));
        Assertions.assertEquals("Hello\\|World", MarkdownSanitizer.escape("Hello\\|World", true));
    }

    @Test
    public void testStrike()
    {
        Assertions.assertEquals("Hello\\~\\~World", MarkdownSanitizer.escape("Hello~~World", true));
        Assertions.assertEquals("Hello\\~\\~World", MarkdownSanitizer.escape("Hello\\~\\~World", true));
    }

    @Test
    public void testQuote()
    {
        Assertions.assertEquals("\\> Hello World", MarkdownSanitizer.escape("> Hello World", true));
        Assertions.assertEquals(">Hello World", MarkdownSanitizer.escape(">Hello World", true));
        Assertions.assertEquals("\\>\\>\\> Hello World", MarkdownSanitizer.escape(">>> Hello World", true));
        Assertions.assertEquals(">>>Hello World", MarkdownSanitizer.escape(">>>Hello World", true));
        Assertions.assertEquals("\\>\\>\\> Hello > World\n\\> Hello >>> World\n<@12345> > Hello\n \\> Hello world", MarkdownSanitizer.escape(">>> Hello > World\n> Hello >>> World\n<@12345> > Hello\n > Hello world", true));

        Assertions.assertEquals("\\> Hello World", MarkdownSanitizer.escape("\\> Hello World", true));
        Assertions.assertEquals("\\>\\>\\> Hello World", MarkdownSanitizer.escape("\\>\\>\\> Hello World", true));
        Assertions.assertEquals("Hello > World", MarkdownSanitizer.escape("Hello > World"));
        Assertions.assertEquals("Hello\n \\> World", MarkdownSanitizer.escape("Hello\n > World"));
        Assertions.assertEquals("Hello\n\\> World", MarkdownSanitizer.escape("Hello\n> World"));
    }
}
