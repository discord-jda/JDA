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

package net.dv8tion.jda.api.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class to escape markdown characters.
 */
public final class MarkdownUtil
{
    private MarkdownUtil() {}

    /**
     * Escapes already existing bold regions in the input
     * and applies bold formatting to the entire string.
     * <br>The resulting string will be {@code "**" + escaped(input) + "**"}.
     *
     * @param  input
     *         The input to bold
     *
     * @return The resulting output
     */
    @Nonnull
    public static String bold(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.BOLD);
        return "**" + sanitized + "**";
    }

    /**
     * Escapes already existing italics (with underscore) regions in the input
     * and applies italics formatting to the entire string.
     * <br>The resulting string will be {@code "_" + escaped(input) + "_"}.
     *
     * @param  input
     *         The input to italics
     *
     * @return The resulting output
     */
    @Nonnull
    public static String italics(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.ITALICS_U);
        return "_" + sanitized + "_";
    }

    /**
     * Escapes already existing underline regions in the input
     * and applies underline formatting to the entire string.
     * <br>The resulting string will be {@code "__" + escaped(input) + "__"}.
     *
     * @param  input
     *         The input to underline
     *
     * @return The resulting output
     */
    @Nonnull
    public static String underline(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.UNDERLINE);
        return "__" + sanitized + "__";
    }

    /**
     * Escapes already existing monospace (single backtick) regions in the input
     * and applies monospace formatting to the entire string.
     * <br>The resulting string will be {@code "`" + escaped(input) + "`"}.
     *
     * @param  input
     *         The input to monospace
     *
     * @return The resulting output
     */
    @Nonnull
    public static String monospace(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.MONO);
        return "`" + sanitized + "`";
    }

    /**
     * Escapes already existing codeblock regions in the input
     * and applies codeblock formatting to the entire string.
     * <br>The resulting string will be {@code "```" + escaped(input) + "```"}.
     *
     * @param  input
     *         The input to codeblock
     *
     * @return The resulting output
     */
    @Nonnull
    public static String codeblock(@Nonnull String input)
    {
        return codeblock(null, input);
    }

    /**
     * Escapes already existing codeblock regions in the input
     * and applies codeblock formatting to the entire string.
     * <br>The resulting string will be {@code "```" + language + "\n" + escaped(input) + "```"}.
     *
     * @param  language
     *         The language to use for syntax highlighting (null to use no language)
     * @param  input
     *         The input to codeblock
     *
     * @return The resulting output
     */
    @Nonnull
    public static String codeblock(@Nullable String language, @Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.BLOCK);
        if (language != null)
            return "```" + language.trim() + "\n" + sanitized + "```";
        return "```" + sanitized + "```";
    }

    /**
     * Escapes already existing spoiler regions in the input
     * and applies spoiler formatting to the entire string.
     * <br>The resulting string will be {@code "||" + escaped(input) + "||"}.
     *
     * @param  input
     *         The input to spoiler
     *
     * @return The resulting output
     */
    @Nonnull
    public static String spoiler(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.SPOILER);
        return "||" + sanitized + "||";
    }

    /**
     * Escapes already existing strike regions in the input
     * and applies strike formatting to the entire string.
     * <br>The resulting string will be {@code "~~" + escaped(input) + "~~"}.
     *
     * @param  input
     *         The input to strike
     *
     * @return The resulting output
     */
    @Nonnull
    public static String strike(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.STRIKE);
        return "~~" + sanitized + "~~";
    }

    /**
     * Escapes already existing quote regions in the input
     * and applies quote formatting to the entire string.
     * <br>The resulting string will be {@code "> " + escaped(input).replace("\n", "\n> ")}.
     *
     * @param  input
     *         The input to quote
     *
     * @return The resulting output
     */
    @Nonnull
    public static String quote(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.QUOTE);
        return "> " + sanitized.replace("\n", "\n> ");
    }

    /**
     * Applies quote block formatting to the entire string.
     * <br>The resulting string will be {@code ">>> " + input}.
     *
     * @param  input
     *         The input to quote block
     *
     * @return The resulting output
     */
    @Nonnull
    public static String quoteBlock(@Nonnull String input)
    {
        return ">>> " + input;
    }

    /**
     * Creates a masked link with the provided url as target.
     * <br>This will replace any closing parentheses (in the url) with the url encoded equivalent
     * and replace closing square brackets with their escaped equivalent.
     *
     * @param  text
     *         The text to display
     * @param  url
     *         The target url
     *
     * @return The resulting output
     */
    @Nonnull
    public static String maskedLink(@Nonnull String text, @Nonnull String url)
    {
        return "[" + text.replace("]", "\\]") + "](" + url.replace(")", "%29") + ")";
    }
}
