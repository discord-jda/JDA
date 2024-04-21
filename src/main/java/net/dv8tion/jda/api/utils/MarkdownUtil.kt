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
package net.dv8tion.jda.api.utils

import javax.annotation.Nonnull

/**
 * Utility class to escape markdown characters.
 */
object MarkdownUtil {
    /**
     * Escapes already existing bold regions in the input
     * and applies bold formatting to the entire string.
     * <br></br>The resulting string will be `"**" + escaped(input) + "**"`.
     *
     * @param  input
     * The input to bold
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun bold(@Nonnull input: String): String {
        val sanitized: String = MarkdownSanitizer.Companion.escape(input, MarkdownSanitizer.Companion.BOLD.inv())
        return "**$sanitized**"
    }

    /**
     * Escapes already existing italics (with underscore) regions in the input
     * and applies italics formatting to the entire string.
     * <br></br>The resulting string will be `"_" + escaped(input) + "_"`.
     *
     * @param  input
     * The input to italics
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun italics(@Nonnull input: String): String {
        val sanitized: String = MarkdownSanitizer.Companion.escape(input, MarkdownSanitizer.Companion.ITALICS_U.inv())
        return "_" + sanitized + "_"
    }

    /**
     * Escapes already existing underline regions in the input
     * and applies underline formatting to the entire string.
     * <br></br>The resulting string will be `"__" + escaped(input) + "__"`.
     *
     * @param  input
     * The input to underline
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun underline(@Nonnull input: String): String {
        val sanitized: String = MarkdownSanitizer.Companion.escape(input, MarkdownSanitizer.Companion.UNDERLINE.inv())
        return "__" + sanitized + "__"
    }

    /**
     * Escapes already existing monospace (single backtick) regions in the input
     * and applies monospace formatting to the entire string.
     * <br></br>The resulting string will be `"`" + escaped(input) + "`"`.
     *
     * @param  input
     * The input to monospace
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun monospace(@Nonnull input: String): String {
        val sanitized: String = MarkdownSanitizer.Companion.escape(input, MarkdownSanitizer.Companion.MONO.inv())
        return "`$sanitized`"
    }

    /**
     * Escapes already existing codeblock regions in the input
     * and applies codeblock formatting to the entire string.
     * <br></br>The resulting string will be `"```" + escaped(input) + "```"`.
     *
     * @param  input
     * The input to codeblock
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun codeblock(@Nonnull input: String): String {
        return codeblock(null, input)
    }

    /**
     * Escapes already existing codeblock regions in the input
     * and applies codeblock formatting to the entire string.
     * <br></br>The resulting string will be `"```" + language + "\n" + escaped(input) + "```"`.
     *
     * @param  language
     * The language to use for syntax highlighting (null to use no language)
     * @param  input
     * The input to codeblock
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun codeblock(language: String?, @Nonnull input: String): String {
        val sanitized: String = MarkdownSanitizer.Companion.escape(input, MarkdownSanitizer.Companion.BLOCK.inv())
        return if (language != null) """
     ```${language.trim { it <= ' ' }}
     $sanitized```
     """.trimIndent() else "```$sanitized```"
    }

    /**
     * Escapes already existing spoiler regions in the input
     * and applies spoiler formatting to the entire string.
     * <br></br>The resulting string will be `"||" + escaped(input) + "||"`.
     *
     * @param  input
     * The input to spoiler
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun spoiler(@Nonnull input: String): String {
        val sanitized: String = MarkdownSanitizer.Companion.escape(input, MarkdownSanitizer.Companion.SPOILER.inv())
        return "||$sanitized||"
    }

    /**
     * Escapes already existing strike regions in the input
     * and applies strike formatting to the entire string.
     * <br></br>The resulting string will be `"~~" + escaped(input) + "~~"`.
     *
     * @param  input
     * The input to strike
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun strike(@Nonnull input: String): String {
        val sanitized: String = MarkdownSanitizer.Companion.escape(input, MarkdownSanitizer.Companion.STRIKE.inv())
        return "~~$sanitized~~"
    }

    /**
     * Escapes already existing quote regions in the input
     * and applies quote formatting to the entire string.
     * <br></br>The resulting string will be `"> " + escaped(input).replace("\n", "\n> ")`.
     *
     * @param  input
     * The input to quote
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun quote(@Nonnull input: String): String {
        val sanitized: String = MarkdownSanitizer.Companion.escape(input, MarkdownSanitizer.Companion.QUOTE.inv())
        return "> " + sanitized.replace("\n", "\n> ")
    }

    /**
     * Applies quote block formatting to the entire string.
     * <br></br>The resulting string will be `">>> " + input`.
     *
     * @param  input
     * The input to quote block
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun quoteBlock(@Nonnull input: String): String {
        return ">>> $input"
    }

    /**
     * Creates a masked link with the provided url as target.
     * <br></br>This will replace any closing parentheses (in the url) with the url encoded equivalent
     * and replace closing square brackets with their escaped equivalent.
     *
     * @param  text
     * The text to display
     * @param  url
     * The target url
     *
     * @return The resulting output
     */
    @JvmStatic
    @Nonnull
    fun maskedLink(@Nonnull text: String, @Nonnull url: String): String {
        return "[" + text.replace("]", "\\]") + "](" + url.replace(")", "%29") + ")"
    }
}
