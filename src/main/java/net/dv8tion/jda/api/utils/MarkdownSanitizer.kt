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

import gnu.trove.map.TIntObjectMap
import gnu.trove.map.hash.TIntObjectHashMap
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.dv8tion.jda.internal.utils.Checks
import java.util.regex.Pattern
import javax.annotation.Nonnull

/**
 * Implements an algorithm that can strip or replace markdown in any supplied string.
 *
 * @see .sanitize
 * @since  4.0.0
 */
class MarkdownSanitizer {
    private var ignored: Int
    private var strategy: SanitizationStrategy

    constructor() {
        ignored = NORMAL
        strategy = SanitizationStrategy.REMOVE
    }

    constructor(ignored: Int, strategy: SanitizationStrategy?) {
        this.ignored = ignored
        this.strategy = strategy ?: SanitizationStrategy.REMOVE
    }

    /**
     * Switches the used [net.dv8tion.jda.api.utils.MarkdownSanitizer.SanitizationStrategy].
     *
     * @param  strategy
     * The new strategy
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with null
     *
     * @return The current sanitizer instance with the new strategy
     */
    @Nonnull
    fun withStrategy(@Nonnull strategy: SanitizationStrategy): MarkdownSanitizer {
        Checks.notNull(strategy, "Strategy")
        this.strategy = strategy
        return this
    }

    /**
     * Specific regions to ignore.
     * <br></br>Example: `new MarkdownSanitizer().withIgnored(MarkdownSanitizer.BOLD | MarkdownSanitizer.UNDERLINE).compute("Hello __world__!")`
     *
     * @param  ignored
     * The regions to ignore
     *
     * @return The current sanitizer instance with the new ignored regions
     */
    @Nonnull
    fun withIgnored(ignored: Int): MarkdownSanitizer {
        this.ignored = this.ignored or ignored
        return this
    }

    private fun getRegion(index: Int, @Nonnull sequence: String): Int {
        if (sequence.length - index >= 3) {
            val threeChars = sequence.substring(index, index + 3)
            when (threeChars) {
                "```" -> return if (doesEscape(index, sequence)) ESCAPED_BLOCK else BLOCK
                "***" -> return if (doesEscape(index, sequence)) ESCAPED_BOLD or ITALICS_A else BOLD or ITALICS_A
            }
        }
        if (sequence.length - index >= 2) {
            val twoChars = sequence.substring(index, index + 2)
            when (twoChars) {
                "**" -> return if (doesEscape(index, sequence)) ESCAPED_BOLD else BOLD
                "__" -> return if (doesEscape(index, sequence)) ESCAPED_UNDERLINE else UNDERLINE
                "~~" -> return if (doesEscape(index, sequence)) ESCAPED_STRIKE else STRIKE
                "``" -> return if (doesEscape(index, sequence)) ESCAPED_MONO_TWO else MONO_TWO
                "||" -> return if (doesEscape(index, sequence)) ESCAPED_SPOILER else SPOILER
            }
        }
        val current = sequence[index]
        when (current) {
            '*' -> return if (doesEscape(index, sequence)) ESCAPED_ITALICS_A else ITALICS_A
            '_' -> return if (doesEscape(index, sequence)) ESCAPED_ITALICS_U else ITALICS_U
            '`' -> return if (doesEscape(index, sequence)) ESCAPED_MONO else MONO
        }
        return NORMAL
    }

    private fun hasCollision(index: Int, @Nonnull sequence: String, c: Char): Boolean {
        return if (index < 0) false else index < sequence.length - 1 && sequence[index + 1] == c
    }

    private fun findEndIndex(afterIndex: Int, region: Int, @Nonnull sequence: String): Int {
        if (isEscape(region)) return -1
        var lastMatch = afterIndex + getDelta(region) + 1
        while (lastMatch != -1) {
            when (region) {
                BOLD or ITALICS_A -> lastMatch = sequence.indexOf("***", lastMatch)
                BOLD -> {
                    lastMatch = sequence.indexOf("**", lastMatch)
                    if (lastMatch != -1 && hasCollision(
                            lastMatch + 1,
                            sequence,
                            '*'
                        )
                    ) // did we find a bold italics tag?
                    {
                        lastMatch += 3
                        continue
                    }
                }

                ITALICS_A -> {
                    lastMatch = sequence.indexOf('*', lastMatch)
                    if (lastMatch != -1 && hasCollision(lastMatch, sequence, '*')) // did we find a bold tag?
                    {
                        lastMatch += if (hasCollision(lastMatch + 1, sequence, '*')) 3 else 2
                        continue
                    }
                }

                UNDERLINE -> lastMatch = sequence.indexOf("__", lastMatch)
                ITALICS_U -> {
                    lastMatch = sequence.indexOf('_', lastMatch)
                    if (lastMatch != -1 && hasCollision(lastMatch, sequence, '_')) // did we find an underline tag?
                    {
                        lastMatch += 2
                        continue
                    }
                }

                SPOILER -> lastMatch = sequence.indexOf("||", lastMatch)
                BLOCK -> lastMatch = sequence.indexOf("```", lastMatch)
                MONO_TWO -> {
                    lastMatch = sequence.indexOf("``", lastMatch)
                    if (lastMatch != -1 && hasCollision(lastMatch + 1, sequence, '`')) // did we find a codeblock?
                    {
                        lastMatch += 3
                        continue
                    }
                }

                MONO -> {
                    lastMatch = sequence.indexOf('`', lastMatch)
                    if (lastMatch != -1 && hasCollision(lastMatch, sequence, '`')) // did we find a codeblock?
                    {
                        lastMatch += if (hasCollision(lastMatch + 1, sequence, '`')) 3 else 2
                        continue
                    }
                }

                STRIKE -> lastMatch = sequence.indexOf("~~", lastMatch)
                else -> return -1
            }
            if (lastMatch == -1 || !doesEscape(lastMatch, sequence)) return lastMatch
            lastMatch++
        }
        return -1
    }

    @Nonnull
    private fun handleRegion(start: Int, end: Int, @Nonnull sequence: String, region: Int): String {
        val resolved = sequence.substring(start, end)
        return when (region) {
            BLOCK, MONO, MONO_TWO -> resolved
            else -> MarkdownSanitizer(ignored, strategy).compute(resolved)
        }
    }

    private fun getDelta(region: Int): Int {
        return when (region) {
            ESCAPED_BLOCK, ESCAPED_BOLD or ITALICS_A, BLOCK, BOLD or ITALICS_A -> 3
            ESCAPED_MONO_TWO, ESCAPED_BOLD, ESCAPED_UNDERLINE, ESCAPED_SPOILER, ESCAPED_STRIKE, MONO_TWO, BOLD, UNDERLINE, SPOILER, STRIKE -> 2
            ESCAPED_ITALICS_A, ESCAPED_ITALICS_U, ESCAPED_MONO, ESCAPED_QUOTE, ITALICS_A, ITALICS_U, MONO -> 1
            else -> 0
        }
    }

    private fun applyStrategy(region: Int, @Nonnull seq: String, @Nonnull builder: StringBuilder) {
        if (strategy == SanitizationStrategy.REMOVE) {
            if (codeLanguage.matcher(seq)
                    .matches()
            ) builder.append(seq.substring(seq.indexOf("\n") + 1)) else builder.append(seq)
            return
        }
        var token = tokens!![region]
            ?: throw IllegalStateException("Found illegal region for strategy ESCAPE '$region' with no known format token!")
        if (region == UNDERLINE) token =
            "_\\_" // UNDERLINE needs special handling because the client thinks its ITALICS_U if you only escape once
        else if (region == BOLD) token =
            "*\\*" // BOLD needs special handling because the client thinks its ITALICS_A if you only escape once
        else if (region == BOLD or ITALICS_A) token =
            "*\\*\\*" // BOLD | ITALICS_A needs special handling because the client thinks its BOLD if you only escape once
        builder.append("\\").append(token)
            .append(seq)
            .append("\\").append(token)
    }

    private fun doesEscape(index: Int, @Nonnull seq: String): Boolean {
        var backslashes = 0
        for (i in index - 1 downTo -1 + 1) {
            if (seq[i] != '\\') break
            backslashes++
        }
        return backslashes % 2 != 0
    }

    private fun isEscape(region: Int): Boolean {
        return Int.MIN_VALUE and region != 0
    }

    private fun isIgnored(nextRegion: Int): Boolean {
        return nextRegion and ignored == nextRegion
    }

    /**
     * Computes the provided input.
     * <br></br>Uses the specified [net.dv8tion.jda.api.utils.MarkdownSanitizer.SanitizationStrategy] and
     * ignores any regions specified with [.withIgnored].
     *
     * @param  sequence
     * The string to compute
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided string is null
     *
     * @return The resulting string after applying the computation
     */
    @Nonnull
    fun compute(@Nonnull sequence: String): String {
        Checks.notNull(sequence, "Input")
        val builder = StringBuilder()
        val end = handleQuote(sequence)
        if (end != null) return end
        var onlySpacesSinceNewLine = true
        var i = 0
        while (i < sequence.length) {
            val nextRegion = getRegion(i, sequence)
            val c = sequence[i]
            val isNewLine = c == '\n'
            val isSpace = c == ' '
            onlySpacesSinceNewLine = isNewLine || onlySpacesSinceNewLine && isSpace
            if (nextRegion == NORMAL) {
                builder.append(sequence[i++])
                if ((isNewLine || isSpace && onlySpacesSinceNewLine) && i < sequence.length) {
                    val result = handleQuote(sequence.substring(i))
                    if (result != null) return builder.append(result).toString()
                }
                continue
            }
            val endRegion = findEndIndex(i, nextRegion, sequence)
            if (isIgnored(nextRegion) || endRegion == -1) {
                val delta = getDelta(nextRegion)
                for (j in 0 until delta) builder.append(sequence[i++])
                continue
            }
            val delta = getDelta(nextRegion)
            applyStrategy(nextRegion, handleRegion(i + delta, endRegion, sequence, nextRegion), builder)
            i = endRegion + delta
        }
        return builder.toString()
    }

    private fun handleQuote(@Nonnull sequence: String): String? {
        // Special handling for quote
        if (!isIgnored(QUOTE) && quote.matcher(sequence).matches()) {
            var start = sequence.indexOf('>')
            if (start < 0) start = 0
            val builder = StringBuilder(compute(sequence.substring(start + 2)))
            if (strategy == SanitizationStrategy.ESCAPE) builder.insert(0, "\\> ")
            return builder.toString()
        } else if (!isIgnored(QUOTE_BLOCK) && quoteBlock.matcher(sequence).matches()) {
            return if (strategy == SanitizationStrategy.ESCAPE) compute("\\" + sequence) else compute(
                sequence.substring(
                    4
                )
            )
        }
        return null
    }

    enum class SanitizationStrategy {
        /**
         * Remove any format tokens that are not escaped or within a special region.
         * <br></br>`"**Hello** World!" -> "Hello World!"`
         */
        REMOVE,

        /**
         * Escape any format tokens that are not escaped or within a special region.
         * <br></br>`"**Hello** World!" -> "\**Hello\** World!"`
         */
        ESCAPE
    }

    companion object {
        /** Normal characters that are not special for markdown, ignoring this has no effect  */
        const val NORMAL = 0

        /** Bold region such as "**Hello**"  */
        const val BOLD = 1 shl 0

        /** Italics region for underline such as "_Hello_"  */
        const val ITALICS_U = 1 shl 1

        /** Italics region for asterisks such as "*Hello*"  */
        const val ITALICS_A = 1 shl 2

        /** Monospace region such as "`Hello`"  */
        const val MONO = 1 shl 3

        /** Monospace region such as "``Hello``"  */
        const val MONO_TWO = 1 shl 4

        /** Codeblock region such as "```Hello```"  */
        const val BLOCK = 1 shl 5

        /** Spoiler region such as "||Hello||"  */
        const val SPOILER = 1 shl 6

        /** Underline region such as "__Hello__"  */
        const val UNDERLINE = 1 shl 7

        /** Strikethrough region such as "~~Hello~~"  */
        const val STRIKE = 1 shl 8

        /** Quote region such as `"> text here"`  */
        const val QUOTE = 1 shl 9

        /** Quote block region such as `">>> text here"`  */
        const val QUOTE_BLOCK = 1 shl 10
        private const val ESCAPED_BOLD = Int.MIN_VALUE or BOLD
        private const val ESCAPED_ITALICS_U = Int.MIN_VALUE or ITALICS_U
        private const val ESCAPED_ITALICS_A = Int.MIN_VALUE or ITALICS_A
        private const val ESCAPED_MONO = Int.MIN_VALUE or MONO
        private const val ESCAPED_MONO_TWO = Int.MIN_VALUE or MONO_TWO
        private const val ESCAPED_BLOCK = Int.MIN_VALUE or BLOCK
        private const val ESCAPED_SPOILER = Int.MIN_VALUE or SPOILER
        private const val ESCAPED_UNDERLINE = Int.MIN_VALUE or UNDERLINE
        private const val ESCAPED_STRIKE = Int.MIN_VALUE or STRIKE
        private const val ESCAPED_QUOTE = Int.MIN_VALUE or QUOTE
        private const val ESCAPED_QUOTE_BLOCK = Int.MIN_VALUE or QUOTE_BLOCK
        private val codeLanguage = Pattern.compile("^\\w+\n.*", Pattern.MULTILINE or Pattern.DOTALL)
        private val quote = Pattern.compile("> +.*", Pattern.DOTALL or Pattern.MULTILINE)
        private val quoteBlock = Pattern.compile(">>>\\s+\\S.*", Pattern.DOTALL or Pattern.MULTILINE)
        private val tokens: TIntObjectMap<String>? = null

        init {
            tokens = TIntObjectHashMap()
            tokens.put(NORMAL, "")
            tokens.put(BOLD, "**")
            tokens.put(ITALICS_U, "_")
            tokens.put(ITALICS_A, "*")
            tokens.put(BOLD or ITALICS_A, "***")
            tokens.put(MONO, "`")
            tokens.put(MONO_TWO, "``")
            tokens.put(BLOCK, "```")
            tokens.put(SPOILER, "||")
            tokens.put(UNDERLINE, "__")
            tokens.put(STRIKE, "~~")
        }

        /**
         * Sanitize string with default settings.
         * <br></br>Same as `sanitize(sequence, SanitizationStrategy.REMOVE)`
         *
         * @param  sequence
         * The string to sanitize
         *
         * @return The sanitized string
         */
        @JvmStatic
        @Nonnull
        fun sanitize(@Nonnull sequence: String): String {
            return sanitize(sequence, SanitizationStrategy.REMOVE)
        }

        /**
         * Sanitize string without ignoring anything.
         *
         * @param  sequence
         * The string to sanitize
         * @param  strategy
         * The [net.dv8tion.jda.api.utils.MarkdownSanitizer.SanitizationStrategy] to apply
         *
         * @throws java.lang.IllegalArgumentException
         * If provided with null
         *
         * @return The sanitized string
         *
         * @see MarkdownSanitizer.MarkdownSanitizer
         * @see .withIgnored
         */
        @Nonnull
        fun sanitize(@Nonnull sequence: String, @Nonnull strategy: SanitizationStrategy): String {
            Checks.notNull(sequence, "String")
            Checks.notNull(strategy, "Strategy")
            return MarkdownSanitizer().withStrategy(strategy).compute(sequence)
        }

        /**
         * Escapes every markdown formatting found in the provided string.
         *
         * @param  sequence
         * The string to sanitize
         *
         * @throws java.lang.IllegalArgumentException
         * If provided with null
         *
         * @return The string with escaped markdown
         *
         * @see .escape
         */
        @JvmStatic
        @Nonnull
        fun escape(@Nonnull sequence: String): String {
            return escape(sequence, NORMAL)
        }

        /**
         * Escapes every markdown formatting found in the provided string.
         * <br></br>Example: `escape("**Hello** ~~World~~!", MarkdownSanitizer.BOLD | MarkdownSanitizer.STRIKE)`
         *
         * @param  sequence
         * The string to sanitize
         * @param  ignored
         * Formats to ignore
         *
         * @throws java.lang.IllegalArgumentException
         * If provided with null
         *
         * @return The string with escaped markdown
         */
        @Nonnull
        fun escape(@Nonnull sequence: String, ignored: Int): String {
            return MarkdownSanitizer()
                .withIgnored(ignored)
                .withStrategy(SanitizationStrategy.ESCAPE)
                .compute(sequence)
        }

        /**
         * Escapes every single markdown formatting token found in the provided string.
         * <br></br>Example: `escape("**Hello _World_", true)`
         *
         * @param sequence
         * The string to sanitize
         * @param single
         * Whether it should scape single tokens or not.
         *
         * @throws java.lang.IllegalArgumentException
         * If provided with null sequence
         *
         * @return The string with escaped markdown
         */
        @JvmStatic
        @Nonnull
        fun escape(@Nonnull sequence: String, single: Boolean): String {
            Checks.notNull(sequence, "Input")
            if (!single) return escape(sequence)
            val builder = StringBuilder()
            var escaped = false
            var newline = true
            var i = 0
            while (i < sequence.length) {
                val current = sequence[i]
                if (newline) {
                    newline = Character.isWhitespace(current) // might still be a quote if prefixed by whitespace
                    if (current == '>') {
                        // Check for quote if line starts with angle bracket
                        if (i + 1 < sequence.length && Character.isWhitespace(sequence[i + 1])) {
                            builder.append("\\>") // simple quote
                        } else if (i + 3 < sequence.length && sequence.startsWith(">>>", i) && Character.isWhitespace(
                                sequence[i + 3]
                            )
                        ) {
                            builder.append("\\>\\>\\>").append(sequence[i + 3]) // block quote
                            i += 3 // since we include 3 angle brackets AND whitespace
                        } else {
                            builder.append(current) // just a normal angle bracket
                        }
                        i++
                        continue
                    }
                }
                if (escaped) {
                    builder.append(current)
                    escaped = false
                    i++
                    continue
                }
                when (current) {
                    '*', '_', '`' -> builder.append('\\').append(current)
                    '|', '~' -> if (i + 1 < sequence.length && sequence[i + 1] == current) {
                        builder.append('\\').append(current)
                            .append('\\').append(current)
                        i++
                    } else builder.append(current)

                    '\\' -> {
                        builder.append(current)
                        escaped = true
                    }

                    '\n' -> {
                        builder.append(current)
                        newline = true
                    }

                    else -> builder.append(current)
                }
                i++
            }
            return builder.toString()
        }
    }
}
