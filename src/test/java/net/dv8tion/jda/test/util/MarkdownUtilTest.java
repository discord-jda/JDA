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

import org.junit.jupiter.api.Test;

import static net.dv8tion.jda.api.utils.MarkdownUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class MarkdownUtilTest
{
    @Test
    void testBold()
    {
        assertThat(bold("Hello World")).isEqualTo("**Hello World**");
        assertThat(bold("Hello **Test** World")).isEqualTo("**Hello \\*\\*Test\\*\\* World**");
        assertThat(bold("Hello *Test* World")).isEqualTo("**Hello *Test* World**");
    }

    @Test
    void testItalics()
    {
        assertThat(italics("Hello World")).isEqualTo("_Hello World_");
        assertThat(italics("Hello _Test_ World")).isEqualTo("_Hello \\_Test\\_ World_");
        assertThat(italics("Hello __Test__ World")).isEqualTo("_Hello __Test__ World_");
    }

    @Test
    void testUnderline()
    {
        assertThat(underline("Hello World")).isEqualTo("__Hello World__");
        assertThat(underline("Hello __Test__ World")).isEqualTo("__Hello \\_\\_Test\\_\\_ World__");
        assertThat(underline("Hello _Test_ World")).isEqualTo("__Hello _Test_ World__");
    }

    @Test
    void testMonospace()
    {
        assertThat(monospace("Hello World")).isEqualTo("`Hello World`");
        assertThat(monospace("Hello `Test` World")).isEqualTo("`Hello \\`Test\\` World`");
        assertThat(monospace("Hello ``Test`` World")).isEqualTo("`Hello ``Test`` World`");
    }

    @Test
    void testCodeblock()
    {
        assertThat(codeblock("java", "Hello World")).isEqualTo("```java\nHello World```");
        assertThat(codeblock("java", "Hello ```java\nTest``` World")).isEqualTo("```java\nHello \\```java\nTest\\``` World```");
        assertThat(codeblock("java", "Hello `Test` World")).isEqualTo("```java\nHello `Test` World```");

        assertThat(codeblock("Hello World")).isEqualTo("```Hello World```");
        assertThat(codeblock("Hello ```java\nTest``` World")).isEqualTo("```Hello \\```java\nTest\\``` World```");
        assertThat(codeblock("Hello `Test` World")).isEqualTo("```Hello `Test` World```");
    }

    @Test
    void testSpoiler()
    {
        assertThat(spoiler("Hello World")).isEqualTo("||Hello World||");
        assertThat(spoiler("Hello ||Test|| World")).isEqualTo("||Hello \\||Test\\|| World||");
        assertThat(spoiler("Hello |Test| World")).isEqualTo("||Hello |Test| World||");
    }

    @Test
    void testStrike()
    {
        assertThat(strike("Hello World")).isEqualTo("~~Hello World~~");
        assertThat(strike("Hello ~~Test~~ World")).isEqualTo("~~Hello \\~~Test\\~~ World~~");
        assertThat(strike("Hello ~Test~ World")).isEqualTo("~~Hello ~Test~ World~~");
    }

    @Test
    void testQuote()
    {
        assertThat(quote("Hello World")).isEqualTo("> Hello World");
        assertThat(quote("Hello \n> Test World")).isEqualTo("> Hello \n> \\> Test World");
        assertThat(quote("Hello > Test World")).isEqualTo("> Hello > Test World");
    }

    @Test
    void testQuoteBlock()
    {
        assertThat(quoteBlock("Hello World")).isEqualTo(">>> Hello World");
        assertThat(quoteBlock("Hello \n>>> Test World")).isEqualTo(">>> Hello \n>>> Test World");
    }

    @Test
    void testMaskedLink()
    {
        assertThat(maskedLink("Hello", "World")).isEqualTo("[Hello](World)");
        assertThat(maskedLink("Hello", "World)")).isEqualTo("[Hello](World%29)");
        assertThat(maskedLink("Hello]", "World)")).isEqualTo("[Hello\\]](World%29)");
    }
}
