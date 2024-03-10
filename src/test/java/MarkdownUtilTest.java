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

import org.junit.jupiter.api.Test;

import static net.dv8tion.jda.api.utils.MarkdownUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarkdownUtilTest
{
    @Test
    public void testBold()
    {
        assertEquals("**Hello World**", bold("Hello World"));
        assertEquals("**Hello \\*\\*Test\\*\\* World**", bold("Hello **Test** World"));
        assertEquals("**Hello *Test* World**", bold("Hello *Test* World"));
    }

    @Test
    public void testItalics()
    {
        assertEquals("_Hello World_", italics("Hello World"));
        assertEquals("_Hello \\_Test\\_ World_", italics("Hello _Test_ World"));
        assertEquals("_Hello __Test__ World_", italics("Hello __Test__ World"));
    }

    @Test
    public void testUnderline()
    {
        assertEquals("__Hello World__", underline("Hello World"));
        assertEquals("__Hello \\_\\_Test\\_\\_ World__", underline("Hello __Test__ World"));
        assertEquals("__Hello _Test_ World__", underline("Hello _Test_ World"));
    }

    @Test
    public void testMonospace()
    {
        assertEquals("`Hello World`", monospace("Hello World"));
        assertEquals("`Hello \\`Test\\` World`", monospace("Hello `Test` World"));
        assertEquals("`Hello ``Test`` World`", monospace("Hello ``Test`` World"));
    }

    @Test
    public void testCodeblock()
    {
        assertEquals("```java\nHello World```", codeblock("java", "Hello World"));
        assertEquals("```java\nHello \\```java\nTest\\``` World```", codeblock("java", "Hello ```java\nTest``` World"));
        assertEquals("```java\nHello `Test` World```", codeblock("java", "Hello `Test` World"));

        assertEquals("```Hello World```", codeblock("Hello World"));
        assertEquals("```Hello \\```java\nTest\\``` World```", codeblock("Hello ```java\nTest``` World"));
        assertEquals("```Hello `Test` World```", codeblock("Hello `Test` World"));
    }

    @Test
    public void testSpoiler()
    {
        assertEquals("||Hello World||", spoiler("Hello World"));
        assertEquals("||Hello \\||Test\\|| World||", spoiler("Hello ||Test|| World"));
        assertEquals("||Hello |Test| World||", spoiler("Hello |Test| World"));
    }

    @Test
    public void testStrike()
    {
        assertEquals("~~Hello World~~", strike("Hello World"));
        assertEquals("~~Hello \\~~Test\\~~ World~~", strike("Hello ~~Test~~ World"));
        assertEquals("~~Hello ~Test~ World~~", strike("Hello ~Test~ World"));
    }

    @Test
    public void testQuote()
    {
        assertEquals("> Hello World", quote("Hello World"));
        assertEquals("> Hello \n> \\> Test World", quote("Hello \n> Test World"));
        assertEquals("> Hello > Test World", quote("Hello > Test World"));
    }

    @Test
    public void testQuoteBlock()
    {
        assertEquals(">>> Hello World", quoteBlock("Hello World"));
        assertEquals(">>> Hello \n>>> Test World", quoteBlock("Hello \n>>> Test World"));
    }

    @Test
    public void testMaskedLink()
    {
        assertEquals("[Hello](World)", maskedLink("Hello", "World"));
        assertEquals("[Hello](World%29)", maskedLink("Hello", "World)"));
        assertEquals("[Hello\\]](World%29)", maskedLink("Hello]", "World)"));
    }
}
