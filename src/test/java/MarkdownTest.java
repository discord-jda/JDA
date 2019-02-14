/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
    public void testBold()
    {
        Assertions.assertEquals("Hello", markdown.compute("**Hello**"));
        Assertions.assertEquals("**Hello", markdown.compute("**Hello"));
    }

    @Test
    public void testItalics()
    {
        Assertions.assertEquals("Hello", markdown.compute("*Hello*"));
        Assertions.assertEquals("Hello", markdown.compute("_Hello_"));

        Assertions.assertEquals("*Hello", markdown.compute("*Hello"));
        Assertions.assertEquals("_Hello", markdown.compute("_Hello"));
    }

    @Test
    public void testUnderline()
    {
        Assertions.assertEquals("Hello", markdown.compute("__Hello__"));
        Assertions.assertEquals("__Hello", markdown.compute("__Hello"));
    }

    @Test
    public void testStrike()
    {
        Assertions.assertEquals("Hello", markdown.compute("~~Hello~~"));
        Assertions.assertEquals("~~Hello", markdown.compute("~~Hello"));
    }

    @Test
    public void testSpoiler()
    {
        Assertions.assertEquals("Hello", markdown.compute("||Hello||"));
        Assertions.assertEquals("||Hello", markdown.compute("||Hello"));
    }

    @Test
    public void testMono()
    {
        Assertions.assertEquals("Hello", markdown.compute("`Hello`"));
        Assertions.assertEquals("`Hello", markdown.compute("`Hello"));

        Assertions.assertEquals("Hello **World**", markdown.compute("`Hello **World**`"));
        Assertions.assertEquals("`Hello World", markdown.compute("`Hello **World**"));
    }

    @Test
    public void testMonoTwo()
    {
        Assertions.assertEquals("Hello", markdown.compute("``Hello``"));
        Assertions.assertEquals("``Hello", markdown.compute("``Hello"));

        Assertions.assertEquals("Hello **World**", markdown.compute("``Hello **World**``"));
        Assertions.assertEquals("``Hello World", markdown.compute("``Hello **World**"));

        Assertions.assertEquals("Hello `to` World", markdown.compute("``Hello `to` World``"));
        Assertions.assertEquals("``Hello to World", markdown.compute("``Hello `to` World"));
    }

    @Test
    public void testBlock()
    {
        Assertions.assertEquals("Hello", markdown.compute("```Hello```"));
        Assertions.assertEquals("```Hello", markdown.compute("```Hello"));

        Assertions.assertEquals("Hello **World**", markdown.compute("```Hello **World**```"));
        Assertions.assertEquals("```Hello World", markdown.compute("```Hello **World**"));

        Assertions.assertEquals("Hello `to` World", markdown.compute("```Hello `to` World```"));
        Assertions.assertEquals("```Hello to World", markdown.compute("```Hello `to` World"));
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
    public void testBold()
    {
        Assertions.assertEquals("\\**Hello\\**", markdown.compute("**Hello**"));
        Assertions.assertEquals("**Hello", markdown.compute("**Hello"));
    }

    @Test
    public void testItalics()
    {
        Assertions.assertEquals("\\*Hello\\*", markdown.compute("*Hello*"));
        Assertions.assertEquals("\\_Hello\\_", markdown.compute("_Hello_"));

        Assertions.assertEquals("*Hello", markdown.compute("*Hello"));
        Assertions.assertEquals("_Hello", markdown.compute("_Hello"));
    }

    @Test
    public void testUnderline()
    {
        Assertions.assertEquals("\\__Hello\\__", markdown.compute("__Hello__"));
        Assertions.assertEquals("__Hello", markdown.compute("__Hello"));
    }

    @Test
    public void testStrike()
    {
        Assertions.assertEquals("\\~~Hello\\~~", markdown.compute("~~Hello~~"));
        Assertions.assertEquals("~~Hello", markdown.compute("~~Hello"));
    }

    @Test
    public void testSpoiler()
    {
        Assertions.assertEquals("\\||Hello\\||", markdown.compute("||Hello||"));
        Assertions.assertEquals("||Hello", markdown.compute("||Hello"));
    }

    @Test
    public void testMono()
    {
        Assertions.assertEquals("\\`Hello\\`", markdown.compute("`Hello`"));
        Assertions.assertEquals("`Hello", markdown.compute("`Hello"));

        Assertions.assertEquals("\\`Hello **World**\\`", markdown.compute("`Hello **World**`"));
        Assertions.assertEquals("`Hello \\**World\\**", markdown.compute("`Hello **World**"));
    }

    @Test
    public void testMonoTwo()
    {
        Assertions.assertEquals("\\``Hello\\``", markdown.compute("``Hello``"));
        Assertions.assertEquals("``Hello", markdown.compute("``Hello"));

        Assertions.assertEquals("\\``Hello **World**\\``", markdown.compute("``Hello **World**``"));
        Assertions.assertEquals("``Hello \\**World\\**", markdown.compute("``Hello **World**"));

        Assertions.assertEquals("\\``Hello `to` World\\``", markdown.compute("``Hello `to` World``"));
        Assertions.assertEquals("``Hello \\`to\\` World", markdown.compute("``Hello `to` World"));
    }

    @Test
    public void testBlock()
    {
        Assertions.assertEquals("\\```Hello\\```", markdown.compute("```Hello```"));
        Assertions.assertEquals("```Hello", markdown.compute("```Hello"));

        Assertions.assertEquals("\\```Hello **World**\\```", markdown.compute("```Hello **World**```"));
        Assertions.assertEquals("```Hello \\**World\\**", markdown.compute("```Hello **World**"));

        Assertions.assertEquals("\\```Hello `to` World\\```", markdown.compute("```Hello `to` World```"));
        Assertions.assertEquals("```Hello \\`to\\` World", markdown.compute("```Hello `to` World"));
    }
}
