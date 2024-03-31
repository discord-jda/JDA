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

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(markdown.compute("**A_B||C~~D__E`F`__~~||_**")).isEqualTo("ABCDEF");
    }

    @Test
    public void testTrivial()
    {
        assertThat(markdown.compute("")).isEqualTo("");
        assertThat(markdown.compute("Hello World ~~~~")).isEqualTo("Hello World ~~~~");
        assertThat(markdown.compute("Hello World ~~~~~")).isEqualTo("Hello World ~");
    }

    @Test
    public void testBold()
    {
        assertThat(markdown.compute("**Hello**")).isEqualTo("Hello");
        assertThat(markdown.compute("**Hello")).isEqualTo("**Hello");
        assertThat(markdown.compute("\\**Hello**")).isEqualTo("\\**Hello**");
    }

    @Test
    public void testItalics()
    {
        assertThat(markdown.compute("*Hello*")).isEqualTo("Hello");
        assertThat(markdown.compute("_Hello_")).isEqualTo("Hello");

        assertThat(markdown.compute("*Hello")).isEqualTo("*Hello");
        assertThat(markdown.compute("_Hello")).isEqualTo("_Hello");

        assertThat(markdown.compute("\\*Hello*")).isEqualTo("\\*Hello*");
        assertThat(markdown.compute("\\_Hello_")).isEqualTo("\\_Hello_");
    }

    @Test
    public void testBoldItalics()
    {
        assertThat(markdown.compute("***Hello***")).isEqualTo("Hello");
        assertThat(markdown.compute("***Hello")).isEqualTo("***Hello");
        assertThat(markdown.compute("\\***Hello***")).isEqualTo("\\***Hello***");
    }

    @Test
    public void testUnderline()
    {
        assertThat(markdown.compute("__Hello__")).isEqualTo("Hello");
        assertThat(markdown.compute("__Hello")).isEqualTo("__Hello");
        assertThat(markdown.compute("\\__Hello__")).isEqualTo("\\__Hello__");
    }

    @Test
    public void testStrike()
    {
        assertThat(markdown.compute("~~Hello~~")).isEqualTo("Hello");
        assertThat(markdown.compute("~~Hello")).isEqualTo("~~Hello");
        assertThat(markdown.compute("\\~~Hello~~")).isEqualTo("\\~~Hello~~");
    }

    @Test
    public void testSpoiler()
    {
        assertThat(markdown.compute("||Hello||")).isEqualTo("Hello");
        assertThat(markdown.compute("||Hello")).isEqualTo("||Hello");
        assertThat(markdown.compute("\\||Hello||")).isEqualTo("\\||Hello||");
    }

    @Test
    public void testMono()
    {
        assertThat(markdown.compute("`Hello`")).isEqualTo("Hello");
        assertThat(markdown.compute("`Hello")).isEqualTo("`Hello");
        assertThat(markdown.compute("\\`Hello`")).isEqualTo("\\`Hello`");

        assertThat(markdown.compute("`Hello **World**`")).isEqualTo("Hello **World**");
        assertThat(markdown.compute("`Hello **World**")).isEqualTo("`Hello World");
        assertThat(markdown.compute("\\`Hello **World**`")).isEqualTo("\\`Hello World`");
    }

    @Test
    public void testMonoTwo()
    {
        assertThat(markdown.compute("``Hello``")).isEqualTo("Hello");
        assertThat(markdown.compute("``Hello")).isEqualTo("``Hello");
        assertThat(markdown.compute("\\``Hello``")).isEqualTo("\\``Hello``");

        assertThat(markdown.compute("``Hello **World**``")).isEqualTo("Hello **World**");
        assertThat(markdown.compute("``Hello **World**")).isEqualTo("``Hello World");
        assertThat(markdown.compute("\\``Hello **World**``")).isEqualTo("\\``Hello World``");

        assertThat(markdown.compute("``Hello `to` World``")).isEqualTo("Hello `to` World");
        assertThat(markdown.compute("``Hello `to` World")).isEqualTo("``Hello to World");
        assertThat(markdown.compute("\\``Hello `to` World``")).isEqualTo("\\``Hello to World``");
    }

    @Test
    public void testBlock()
    {
        assertThat(markdown.compute("```Hello```")).isEqualTo("Hello");
        assertThat(markdown.compute("```Hello")).isEqualTo("```Hello");
        assertThat(markdown.compute("\\```Hello```")).isEqualTo("\\```Hello```");

        assertThat(markdown.compute("```Hello **World**```")).isEqualTo("Hello **World**");
        assertThat(markdown.compute("```Hello **World**")).isEqualTo("```Hello World");
        assertThat(markdown.compute("\\```Hello **World**```")).isEqualTo("\\```Hello World```");

        assertThat(markdown.compute("```Hello `to` World```")).isEqualTo("Hello `to` World");
        assertThat(markdown.compute("```Hello `to` World")).isEqualTo("```Hello to World");
        assertThat(markdown.compute("\\```Hello `to` World```")).isEqualTo("\\```Hello to World```");

        assertThat(markdown.compute("```java\nTest```")).isEqualTo("Test");
    }

    @Test
    public void testQuote()
    {
        assertThat(markdown.compute("> Hello > World")).isEqualTo("Hello > World");
        assertThat(markdown.compute("> Hello\n> World")).isEqualTo("Hello\nWorld");
        assertThat(markdown.compute(">>> Hello\nWorld")).isEqualTo("Hello\nWorld");
        assertThat(markdown.compute(">>>\nHello\nWorld")).isEqualTo("Hello\nWorld");
        assertThat(markdown.compute(">>>\nHello > World")).isEqualTo("Hello > World");
        assertThat(markdown.compute("Hello\n > World")).isEqualTo("Hello\n World");
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
        assertThat(markdown.compute("**A_B||C~~D__E`F`__~~||_**")).isEqualTo("**A_B||C~~D__E`F`__~~||_**");
    }

    @Test
    public void testBold()
    {
        assertThat(markdown.compute("**Hello**")).isEqualTo("**Hello**");
        assertThat(markdown.compute("**Hello")).isEqualTo("**Hello");
    }

    @Test
    public void testItalics()
    {
        assertThat(markdown.compute("*Hello*")).isEqualTo("*Hello*");
        assertThat(markdown.compute("_Hello_")).isEqualTo("_Hello_");

        assertThat(markdown.compute("*Hello")).isEqualTo("*Hello");
        assertThat(markdown.compute("_Hello")).isEqualTo("_Hello");
    }

    @Test
    public void testBoldItalics()
    {
        assertThat(markdown.compute("***Hello***")).isEqualTo("***Hello***");
        assertThat(markdown.compute("***Hello")).isEqualTo("***Hello");
        assertThat(markdown.compute("\\***Hello***")).isEqualTo("\\***Hello***");
    }

    @Test
    public void testUnderline()
    {
        assertThat(markdown.compute("__Hello__")).isEqualTo("__Hello__");
        assertThat(markdown.compute("__Hello")).isEqualTo("__Hello");
    }

    @Test
    public void testStrike()
    {
        assertThat(markdown.compute("~~Hello~~")).isEqualTo("~~Hello~~");
        assertThat(markdown.compute("~~Hello")).isEqualTo("~~Hello");
    }

    @Test
    public void testSpoiler()
    {
        assertThat(markdown.compute("||Hello||")).isEqualTo("||Hello||");
        assertThat(markdown.compute("||Hello")).isEqualTo("||Hello");
    }

    @Test
    public void testMono()
    {
        assertThat(markdown.compute("`Hello`")).isEqualTo("`Hello`");
        assertThat(markdown.compute("`Hello")).isEqualTo("`Hello");

        assertThat(markdown.compute("`Hello **World**`")).isEqualTo("`Hello **World**`");
        assertThat(markdown.compute("`Hello **World**")).isEqualTo("`Hello **World**");
    }

    @Test
    public void testMonoTwo()
    {
        assertThat(markdown.compute("``Hello``")).isEqualTo("``Hello``");
        assertThat(markdown.compute("``Hello")).isEqualTo("``Hello");

        assertThat(markdown.compute("``Hello **World**``")).isEqualTo("``Hello **World**``");
        assertThat(markdown.compute("``Hello **World**")).isEqualTo("``Hello **World**");

        assertThat(markdown.compute("``Hello `to` World``")).isEqualTo("``Hello `to` World``");
        assertThat(markdown.compute("``Hello `to` World")).isEqualTo("``Hello `to` World");
    }

    @Test
    public void testBlock()
    {
        assertThat(markdown.compute("```Hello```")).isEqualTo("```Hello```");
        assertThat(markdown.compute("```Hello")).isEqualTo("```Hello");

        assertThat(markdown.compute("```Hello **World**```")).isEqualTo("```Hello **World**```");
        assertThat(markdown.compute("```Hello **World**")).isEqualTo("```Hello **World**");

        assertThat(markdown.compute("```Hello `to` World```")).isEqualTo("```Hello `to` World```");
        assertThat(markdown.compute("```Hello `to` World")).isEqualTo("```Hello `to` World");

        assertThat(markdown.compute("```java\nTest```")).isEqualTo("```java\nTest```");
    }

    @Test
    public void testQuote()
    {
        assertThat(markdown.compute("> Hello > World")).isEqualTo("> Hello > World");
        assertThat(markdown.compute("> Hello\n> World")).isEqualTo("> Hello\n> World");
        assertThat(markdown.compute(">>> Hello\nWorld")).isEqualTo(">>> Hello\nWorld");
        assertThat(markdown.compute(">>>\nHello\nWorld")).isEqualTo(">>>\nHello\nWorld");
        assertThat(markdown.compute(">>>\nHello > World")).isEqualTo(">>>\nHello > World");
        assertThat(markdown.compute("Hello\n > World")).isEqualTo("Hello\n > World");
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
        assertThat(markdown.compute("**A_B||C~~D__E`F`__~~||_**")).isEqualTo("\\*\\*A\\_B\\||C\\~~D\\_\\_E\\`F\\`\\_\\_\\~~\\||\\_\\*\\*");
    }

    @Test
    public void testBold()
    {
        assertThat(markdown.compute("**Hello**")).isEqualTo("\\*\\*Hello\\*\\*");
        assertThat(markdown.compute("**Hello")).isEqualTo("**Hello");
        assertThat(markdown.compute("\\**Hello**")).isEqualTo("\\**Hello**");
    }

    @Test
    public void testItalics()
    {
        assertThat(markdown.compute("*Hello*")).isEqualTo("\\*Hello\\*");
        assertThat(markdown.compute("_Hello_")).isEqualTo("\\_Hello\\_");

        assertThat(markdown.compute("*Hello")).isEqualTo("*Hello");
        assertThat(markdown.compute("_Hello")).isEqualTo("_Hello");

        assertThat(markdown.compute("\\*Hello*")).isEqualTo("\\*Hello*");
        assertThat(markdown.compute("\\_Hello_")).isEqualTo("\\_Hello_");
    }

    @Test
    public void testBoldItalics()
    {
        assertThat(markdown.compute("***Hello***")).isEqualTo("\\*\\*\\*Hello\\*\\*\\*");
        assertThat(markdown.compute("***Hello")).isEqualTo("***Hello");
        assertThat(markdown.compute("\\***Hello***")).isEqualTo("\\***Hello***");
    }

    @Test
    public void testUnderline()
    {
        assertThat(markdown.compute("__Hello__")).isEqualTo("\\_\\_Hello\\_\\_");
        assertThat(markdown.compute("__Hello")).isEqualTo("__Hello");
        assertThat(markdown.compute("\\__Hello__")).isEqualTo("\\__Hello__");
    }

    @Test
    public void testStrike()
    {
        assertThat(markdown.compute("~~Hello~~")).isEqualTo("\\~~Hello\\~~");
        assertThat(markdown.compute("~~Hello")).isEqualTo("~~Hello");
        assertThat(markdown.compute("\\~~Hello~~")).isEqualTo("\\~~Hello~~");
    }

    @Test
    public void testSpoiler()
    {
        assertThat(markdown.compute("||Hello||")).isEqualTo("\\||Hello\\||");
        assertThat(markdown.compute("||Hello")).isEqualTo("||Hello");
        assertThat(markdown.compute("\\||Hello||")).isEqualTo("\\||Hello||");
    }

    @Test
    public void testMono()
    {
        assertThat(markdown.compute("`Hello`")).isEqualTo("\\`Hello\\`");
        assertThat(markdown.compute("`Hello")).isEqualTo("`Hello");
        assertThat(markdown.compute("\\`Hello`")).isEqualTo("\\`Hello`");

        assertThat(markdown.compute("`Hello **World**`")).isEqualTo("\\`Hello **World**\\`");
        assertThat(markdown.compute("`Hello **World**")).isEqualTo("`Hello \\*\\*World\\*\\*");
        assertThat(markdown.compute("\\`Hello **World**`")).isEqualTo("\\`Hello \\*\\*World\\*\\*`");

    }

    @Test
    public void testMonoTwo()
    {
        assertThat(markdown.compute("``Hello``")).isEqualTo("\\``Hello\\``");
        assertThat(markdown.compute("``Hello")).isEqualTo("``Hello");
        assertThat(markdown.compute("\\``Hello``")).isEqualTo("\\``Hello``");

        assertThat(markdown.compute("``Hello **World**``")).isEqualTo("\\``Hello **World**\\``");
        assertThat(markdown.compute("``Hello **World**")).isEqualTo("``Hello \\*\\*World\\*\\*");
        assertThat(markdown.compute("\\``Hello **World**``")).isEqualTo("\\``Hello \\*\\*World\\*\\*``");

        assertThat(markdown.compute("``Hello `to` World``")).isEqualTo("\\``Hello `to` World\\``");
        assertThat(markdown.compute("``Hello `to` World")).isEqualTo("``Hello \\`to\\` World");
        assertThat(markdown.compute("\\``Hello `to` World")).isEqualTo("\\``Hello \\`to\\` World");
    }

    @Test
    public void testBlock()
    {
        assertThat(markdown.compute("```Hello```")).isEqualTo("\\```Hello\\```");
        assertThat(markdown.compute("```Hello")).isEqualTo("```Hello");
        assertThat(markdown.compute("\\```Hello")).isEqualTo("\\```Hello");

        assertThat(markdown.compute("```Hello **World**```")).isEqualTo("\\```Hello **World**\\```");
        assertThat(markdown.compute("```Hello **World**")).isEqualTo("```Hello \\*\\*World\\*\\*");
        assertThat(markdown.compute("\\```Hello **World**")).isEqualTo("\\```Hello \\*\\*World\\*\\*");

        assertThat(markdown.compute("```Hello `to` World```")).isEqualTo("\\```Hello `to` World\\```");
        assertThat(markdown.compute("```Hello `to` World")).isEqualTo("```Hello \\`to\\` World");
        assertThat(markdown.compute("\\```Hello `to` World")).isEqualTo("\\```Hello \\`to\\` World");

        assertThat(markdown.compute("```java\nTest```")).isEqualTo("\\```java\nTest\\```");
    }

    @Test
    public void testQuote()
    {
        assertThat(markdown.compute("> Hello > World")).isEqualTo("\\> Hello > World");
        assertThat(markdown.compute("> Hello\n> World")).isEqualTo("\\> Hello\n\\> World");
        assertThat(markdown.compute(">>> Hello\nWorld")).isEqualTo("\\>>> Hello\nWorld");
        assertThat(markdown.compute(">>>\nHello\nWorld")).isEqualTo("\\>>>\nHello\nWorld");
        assertThat(markdown.compute(">>>\nHello > World")).isEqualTo("\\>>>\nHello > World");
        assertThat(markdown.compute("> _Hello \n> World_")).isEqualTo("\\> \\_Hello \n\\> World\\_");
        assertThat(markdown.compute("Hello\n > World")).isEqualTo("Hello\n \\> World");
    }
}

class EscapeMarkdownAllTest
{
    @Test
    public void testAsterisk()
    {
        assertThat(MarkdownSanitizer.escape("Hello*World", true)).isEqualTo("Hello\\*World");
        assertThat(MarkdownSanitizer.escape("Hello**World", true)).isEqualTo("Hello\\*\\*World");
        assertThat(MarkdownSanitizer.escape("Hello***World", true)).isEqualTo("Hello\\*\\*\\*World");

        assertThat(MarkdownSanitizer.escape("Hello\\*World", true)).isEqualTo("Hello\\*World");
        assertThat(MarkdownSanitizer.escape("Hello\\*\\*World", true)).isEqualTo("Hello\\*\\*World");
        assertThat(MarkdownSanitizer.escape("Hello\\*\\*\\*World", true)).isEqualTo("Hello\\*\\*\\*World");
    }

    @Test
    public void testUnderscore()
    {
        assertThat(MarkdownSanitizer.escape("Hello_World", true)).isEqualTo("Hello\\_World");
        assertThat(MarkdownSanitizer.escape("Hello__World", true)).isEqualTo("Hello\\_\\_World");
        assertThat(MarkdownSanitizer.escape("Hello___World", true)).isEqualTo("Hello\\_\\_\\_World");

        assertThat(MarkdownSanitizer.escape("Hello\\_World", true)).isEqualTo("Hello\\_World");
        assertThat(MarkdownSanitizer.escape("Hello\\_\\_World", true)).isEqualTo("Hello\\_\\_World");
        assertThat(MarkdownSanitizer.escape("Hello\\_\\_\\_World", true)).isEqualTo("Hello\\_\\_\\_World");
    }

    @Test
    public void testCodeBlock()
    {
        assertThat(MarkdownSanitizer.escape("Hello`World", true)).isEqualTo("Hello\\`World");
        assertThat(MarkdownSanitizer.escape("Hello``World", true)).isEqualTo("Hello\\`\\`World");
        assertThat(MarkdownSanitizer.escape("Hello```World", true)).isEqualTo("Hello\\`\\`\\`World");

        assertThat(MarkdownSanitizer.escape("Hello\\`World", true)).isEqualTo("Hello\\`World");
        assertThat(MarkdownSanitizer.escape("Hello\\`\\`World", true)).isEqualTo("Hello\\`\\`World");
        assertThat(MarkdownSanitizer.escape("Hello\\`\\`\\`World", true)).isEqualTo("Hello\\`\\`\\`World");
    }

    @Test
    public void testSpoiler()
    {
        assertThat(MarkdownSanitizer.escape("Hello||World", true)).isEqualTo("Hello\\|\\|World");
        assertThat(MarkdownSanitizer.escape("Hello|World", true)).isEqualTo("Hello|World");

        assertThat(MarkdownSanitizer.escape("Hello\\|\\|World", true)).isEqualTo("Hello\\|\\|World");
        assertThat(MarkdownSanitizer.escape("Hello\\|World", true)).isEqualTo("Hello\\|World");
    }

    @Test
    public void testStrike()
    {
        assertThat(MarkdownSanitizer.escape("Hello~~World", true)).isEqualTo("Hello\\~\\~World");
        assertThat(MarkdownSanitizer.escape("Hello\\~\\~World", true)).isEqualTo("Hello\\~\\~World");
    }

    @Test
    public void testQuote()
    {
        assertThat(MarkdownSanitizer.escape("> Hello World", true)).isEqualTo("\\> Hello World");
        assertThat(MarkdownSanitizer.escape(">Hello World", true)).isEqualTo(">Hello World");
        assertThat(MarkdownSanitizer.escape(">>> Hello World", true)).isEqualTo("\\>\\>\\> Hello World");
        assertThat(MarkdownSanitizer.escape(">>>Hello World", true)).isEqualTo(">>>Hello World");
        assertThat(MarkdownSanitizer.escape(">>> Hello > World\n> Hello >>> World\n<@12345> > Hello\n > Hello world", true)).isEqualTo("\\>\\>\\> Hello > World\n\\> Hello >>> World\n<@12345> > Hello\n \\> Hello world");

        assertThat(MarkdownSanitizer.escape("\\> Hello World", true)).isEqualTo("\\> Hello World");
        assertThat(MarkdownSanitizer.escape("\\>\\>\\> Hello World", true)).isEqualTo("\\>\\>\\> Hello World");
        assertThat(MarkdownSanitizer.escape("Hello > World")).isEqualTo("Hello > World");
        assertThat(MarkdownSanitizer.escape("Hello\n > World")).isEqualTo("Hello\n \\> World");
        assertThat(MarkdownSanitizer.escape("Hello\n> World")).isEqualTo("Hello\n\\> World");
    }
}
