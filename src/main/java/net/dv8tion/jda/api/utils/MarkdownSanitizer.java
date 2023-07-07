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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * Implements an algorithm that can strip or replace markdown in any supplied string.
 *
 * @see #sanitize(String, net.dv8tion.jda.api.utils.MarkdownSanitizer.SanitizationStrategy)
 *
 * @since  4.0.0
 */
public class MarkdownSanitizer
{
    /** Normal characters that are not special for markdown, ignoring this has no effect */
    public static final int NORMAL      = 0;
    /** Bold region such as "**Hello**" */
    public static final int BOLD        = 1 << 0;
    /** Italics region for underline such as "_Hello_" */
    public static final int ITALICS_U   = 1 << 1;
    /** Italics region for asterisks such as "*Hello*" */
    public static final int ITALICS_A   = 1 << 2;
    /** Monospace region such as "`Hello`" */
    public static final int MONO        = 1 << 3;
    /** Monospace region such as "``Hello``" */
    public static final int MONO_TWO    = 1 << 4;
    /** Codeblock region such as "```Hello```" */
    public static final int BLOCK       = 1 << 5;
    /** Spoiler region such as "||Hello||" */
    public static final int SPOILER     = 1 << 6;
    /** Underline region such as "__Hello__" */
    public static final int UNDERLINE   = 1 << 7;
    /** Strikethrough region such as "~~Hello~~" */
    public static final int STRIKE      = 1 << 8;
    /** Quote region such as {@code "> text here"} */
    public static final int QUOTE       = 1 << 9;
    /** Quote block region such as {@code ">>> text here"} */
    public static final int QUOTE_BLOCK = 1 << 10;

    private static final int ESCAPED_BOLD        = Integer.MIN_VALUE | BOLD;
    private static final int ESCAPED_ITALICS_U   = Integer.MIN_VALUE | ITALICS_U;
    private static final int ESCAPED_ITALICS_A   = Integer.MIN_VALUE | ITALICS_A;
    private static final int ESCAPED_MONO        = Integer.MIN_VALUE | MONO;
    private static final int ESCAPED_MONO_TWO    = Integer.MIN_VALUE | MONO_TWO;
    private static final int ESCAPED_BLOCK       = Integer.MIN_VALUE | BLOCK;
    private static final int ESCAPED_SPOILER     = Integer.MIN_VALUE | SPOILER;
    private static final int ESCAPED_UNDERLINE   = Integer.MIN_VALUE | UNDERLINE;
    private static final int ESCAPED_STRIKE      = Integer.MIN_VALUE | STRIKE;
    private static final int ESCAPED_QUOTE       = Integer.MIN_VALUE | QUOTE;
    private static final int ESCAPED_QUOTE_BLOCK = Integer.MIN_VALUE | QUOTE_BLOCK;

    private static final Pattern codeLanguage = Pattern.compile("^\\w+\n.*", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern quote = Pattern.compile("> +.*", Pattern.DOTALL | Pattern.MULTILINE);
    private static final Pattern quoteBlock = Pattern.compile(">>>\\s+\\S.*", Pattern.DOTALL | Pattern.MULTILINE);

    private static final TIntObjectMap<String> tokens;

    static
    {
        tokens = new TIntObjectHashMap<>();
        tokens.put(NORMAL, "");
        tokens.put(BOLD, "**");
        tokens.put(ITALICS_U, "_");
        tokens.put(ITALICS_A, "*");
        tokens.put(BOLD | ITALICS_A, "***");
        tokens.put(MONO, "`");
        tokens.put(MONO_TWO, "``");
        tokens.put(BLOCK, "```");
        tokens.put(SPOILER, "||");
        tokens.put(UNDERLINE, "__");
        tokens.put(STRIKE, "~~");
    }

    private int ignored;
    private SanitizationStrategy strategy;

    public MarkdownSanitizer()
    {
        this.ignored = NORMAL;
        this.strategy = SanitizationStrategy.REMOVE;
    }

    public MarkdownSanitizer(int ignored, @Nullable SanitizationStrategy strategy)
    {
        this.ignored = ignored;
        this.strategy = strategy == null ? SanitizationStrategy.REMOVE : strategy;
    }

    /**
     * Sanitize string with default settings.
     * <br>Same as {@code sanitize(sequence, SanitizationStrategy.REMOVE)}
     *
     * @param  sequence
     *         The string to sanitize
     *
     * @return The sanitized string
     */
    @Nonnull
    public static String sanitize(@Nonnull String sequence)
    {
        return sanitize(sequence, SanitizationStrategy.REMOVE);
    }

    /**
     * Sanitize string without ignoring anything.
     *
     * @param  sequence
     *         The string to sanitize
     * @param  strategy
     *         The {@link net.dv8tion.jda.api.utils.MarkdownSanitizer.SanitizationStrategy} to apply
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The sanitized string
     *
     * @see    MarkdownSanitizer#MarkdownSanitizer()
     * @see    #withIgnored(int)
     */
    @Nonnull
    public static String sanitize(@Nonnull String sequence, @Nonnull SanitizationStrategy strategy)
    {
        Checks.notNull(sequence, "String");
        Checks.notNull(strategy, "Strategy");
        return new MarkdownSanitizer().withStrategy(strategy).compute(sequence);
    }

    /**
     * Escapes every markdown formatting found in the provided string.
     *
     * @param  sequence
     *         The string to sanitize
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The string with escaped markdown
     *
     * @see    #escape(String, int)
     */
    @Nonnull
    public static String escape(@Nonnull String sequence)
    {
        return escape(sequence, NORMAL);
    }

    /**
     * Escapes every markdown formatting found in the provided string.
     * <br>Example: {@code escape("**Hello** ~~World~~!", MarkdownSanitizer.BOLD | MarkdownSanitizer.STRIKE)}
     *
     * @param  sequence
     *         The string to sanitize
     * @param  ignored
     *         Formats to ignore
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The string with escaped markdown
     */
    @Nonnull
    public static String escape(@Nonnull String sequence, int ignored)
    {
        return new MarkdownSanitizer()
                .withIgnored(ignored)
                .withStrategy(SanitizationStrategy.ESCAPE)
                .compute(sequence);
    }

    /**
     * Escapes every single markdown formatting token found in the provided string.
     * <br>Example: {@code escape("**Hello _World_", true)}
     *
     * @param sequence
     *        The string to sanitize
     * @param single
     *        Whether it should scape single tokens or not.
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null sequence
     *
     * @return The string with escaped markdown
     */
    @Nonnull
    public static String escape(@Nonnull String sequence, boolean single)
    {
        Checks.notNull(sequence, "Input");
        if(!single) return escape(sequence);

        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        boolean newline = true;
        for (int i = 0; i < sequence.length(); i++)
        {
            char current = sequence.charAt(i);
            if (newline)
            {
                newline = Character.isWhitespace(current); // might still be a quote if prefixed by whitespace
                if (current == '>')
                {
                    // Check for quote if line starts with angle bracket
                    if (i + 1 < sequence.length() && Character.isWhitespace(sequence.charAt(i+1)))
                    {
                        builder.append("\\>"); // simple quote
                    }
                    else if (i + 3 < sequence.length() && sequence.startsWith(">>>", i) && Character.isWhitespace(sequence.charAt(i+3)))
                    {
                        builder.append("\\>\\>\\>").append(sequence.charAt(i+3)); // block quote
                        i += 3; // since we include 3 angle brackets AND whitespace
                    }
                    else
                    {
                        builder.append(current); // just a normal angle bracket
                    }
                    continue;
                }
            }

            if (escaped)
            {
                builder.append(current);
                escaped = false;
                continue;
            }
            // Handle average case
            switch (current)
            {
            case '*': // simple markdown escapes for single characters
            case '_':
            case '`':
                builder.append('\\').append(current);
                break;
            case '|': // cases that require at least 2 characters in sequence
            case '~':
                if (i + 1 < sequence.length() && sequence.charAt(i+1) == current)
                {
                    builder.append('\\').append(current)
                            .append('\\').append(current);
                    i++;
                }
                else
                    builder.append(current);
                break;
            case '\\': // escape character
                builder.append(current);
                escaped = true;
                break;
            case '\n': // linefeed is a special case for quotes
                builder.append(current);
                newline = true;
                break;
            default:
                builder.append(current);
            }
        }
        return builder.toString();
    }

    /**
     * Switches the used {@link net.dv8tion.jda.api.utils.MarkdownSanitizer.SanitizationStrategy}.
     *
     * @param  strategy
     *         The new strategy
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with null
     *
     * @return The current sanitizer instance with the new strategy
     */
    @Nonnull
    public MarkdownSanitizer withStrategy(@Nonnull SanitizationStrategy strategy)
    {
        Checks.notNull(strategy, "Strategy");
        this.strategy = strategy;
        return this;
    }

    /**
     * Specific regions to ignore.
     * <br>Example: {@code new MarkdownSanitizer().withIgnored(MarkdownSanitizer.BOLD | MarkdownSanitizer.UNDERLINE).compute("Hello __world__!")}
     *
     * @param  ignored
     *         The regions to ignore
     *
     * @return The current sanitizer instance with the new ignored regions
     */
    @Nonnull
    public MarkdownSanitizer withIgnored(int ignored)
    {
        this.ignored |= ignored;
        return this;
    }

    private int getRegion(int index, @Nonnull String sequence)
    {
        if (sequence.length() - index >= 3)
        {
            String threeChars = sequence.substring(index, index + 3);
            switch (threeChars)
            {
                case "```":
                    return doesEscape(index, sequence) ? ESCAPED_BLOCK : BLOCK;
                case "***":
                    return doesEscape(index, sequence) ? ESCAPED_BOLD | ITALICS_A : BOLD | ITALICS_A;
            }
        }
        if (sequence.length() - index >= 2)
        {
            String twoChars = sequence.substring(index, index + 2);
            switch (twoChars)
            {
                case "**":
                    return doesEscape(index, sequence) ? ESCAPED_BOLD : BOLD;
                case "__":
                    return doesEscape(index, sequence) ? ESCAPED_UNDERLINE : UNDERLINE;
                case "~~":
                    return doesEscape(index, sequence) ? ESCAPED_STRIKE : STRIKE;
                case "``":
                    return doesEscape(index, sequence) ? ESCAPED_MONO_TWO : MONO_TWO;
                case "||":
                    return doesEscape(index, sequence) ? ESCAPED_SPOILER : SPOILER;
            }
        }
        char current = sequence.charAt(index);
        switch (current)
        {
            case '*':
                return doesEscape(index, sequence) ? ESCAPED_ITALICS_A : ITALICS_A;
            case '_':
                return doesEscape(index, sequence) ? ESCAPED_ITALICS_U : ITALICS_U;
            case '`':
                return doesEscape(index, sequence) ? ESCAPED_MONO : MONO;
        }
        return NORMAL;
    }

    private boolean hasCollision(int index, @Nonnull String sequence, char c)
    {
        if (index < 0)
            return false;
        return index < sequence.length() - 1 && sequence.charAt(index + 1) == c;
    }

    private int findEndIndex(int afterIndex, int region, @Nonnull String sequence)
    {
        if (isEscape(region))
            return -1;
        int lastMatch = afterIndex + getDelta(region) + 1;
        while (lastMatch != -1)
        {
            switch (region)
            {
                case BOLD | ITALICS_A:
                    lastMatch = sequence.indexOf("***", lastMatch);
                    break;
                case BOLD:
                    lastMatch = sequence.indexOf("**", lastMatch);
                    if (lastMatch != -1 && hasCollision(lastMatch + 1, sequence, '*')) // did we find a bold italics tag?
                    {
                        lastMatch += 3;
                        continue;
                    }
                    break;
                case ITALICS_A:
                    lastMatch = sequence.indexOf('*', lastMatch);
                    if (lastMatch != -1 && hasCollision(lastMatch, sequence, '*')) // did we find a bold tag?
                    {
                        if (hasCollision(lastMatch + 1, sequence, '*'))
                            lastMatch += 3;
                        else
                            lastMatch += 2;
                        continue;
                    }
                    break;
                case UNDERLINE:
                    lastMatch = sequence.indexOf("__", lastMatch);
                    break;
                case ITALICS_U:
                    lastMatch = sequence.indexOf('_', lastMatch);
                    if (lastMatch != -1 && hasCollision(lastMatch, sequence, '_')) // did we find an underline tag?
                    {
                        lastMatch += 2;
                        continue;
                    }
                    break;
                case SPOILER:
                    lastMatch = sequence.indexOf("||", lastMatch);
                    break;
                case BLOCK:
                    lastMatch = sequence.indexOf("```", lastMatch);
                    break;
                case MONO_TWO:
                    lastMatch = sequence.indexOf("``", lastMatch);
                    if (lastMatch != -1 && hasCollision(lastMatch + 1, sequence, '`')) // did we find a codeblock?
                    {
                        lastMatch += 3;
                        continue;
                    }
                    break;
                case MONO:
                    lastMatch = sequence.indexOf('`', lastMatch);
                    if (lastMatch != -1 && hasCollision(lastMatch, sequence, '`')) // did we find a codeblock?
                    {
                        if (hasCollision(lastMatch + 1, sequence, '`'))
                            lastMatch += 3;
                        else
                            lastMatch += 2;
                        continue;
                    }
                    break;
                case STRIKE:
                    lastMatch = sequence.indexOf("~~", lastMatch);
                    break;
                default:
                    return -1;
            }
            if (lastMatch == -1 || !doesEscape(lastMatch, sequence))
                return lastMatch;
            lastMatch++;
        }
        return -1;
    }

    @Nonnull
    private String handleRegion(int start, int end, @Nonnull String sequence, int region)
    {
        String resolved = sequence.substring(start, end);
        switch (region)
        {
            case BLOCK:
            case MONO:
            case MONO_TWO:
                return resolved;
            default:
                return new MarkdownSanitizer(ignored, strategy).compute(resolved);
        }
    }

    private int getDelta(int region)
    {
        switch (region)
        {
            case ESCAPED_BLOCK:
            case ESCAPED_BOLD | ITALICS_A:
            case BLOCK:
            case BOLD | ITALICS_A:
                return 3;
            case ESCAPED_MONO_TWO:
            case ESCAPED_BOLD:
            case ESCAPED_UNDERLINE:
            case ESCAPED_SPOILER:
            case ESCAPED_STRIKE:
            case MONO_TWO:
            case BOLD:
            case UNDERLINE:
            case SPOILER:
            case STRIKE:
                return 2;
            case ESCAPED_ITALICS_A:
            case ESCAPED_ITALICS_U:
            case ESCAPED_MONO:
            case ESCAPED_QUOTE:
            case ITALICS_A:
            case ITALICS_U:
            case MONO:
                return 1;
            default:
                return 0;
        }
    }

    private void applyStrategy(int region, @Nonnull String seq, @Nonnull StringBuilder builder)
    {
        if (strategy == SanitizationStrategy.REMOVE)
        {
            if (codeLanguage.matcher(seq).matches())
                builder.append(seq.substring(seq.indexOf("\n") + 1));
            else
                builder.append(seq);
            return;
        }
        String token = tokens.get(region);
        if (token == null)
            throw new IllegalStateException("Found illegal region for strategy ESCAPE '" + region + "' with no known format token!");
        if (region == UNDERLINE)
            token = "_\\_"; // UNDERLINE needs special handling because the client thinks its ITALICS_U if you only escape once
        else if (region == BOLD)
            token = "*\\*"; // BOLD needs special handling because the client thinks its ITALICS_A if you only escape once
        else if (region == (BOLD | ITALICS_A))
            token = "*\\*\\*"; // BOLD | ITALICS_A needs special handling because the client thinks its BOLD if you only escape once
        builder.append("\\").append(token)
               .append(seq)
               .append("\\").append(token);
    }

    private boolean doesEscape(int index, @Nonnull String seq)
    {
        int backslashes = 0;
        for (int i = index - 1; i > -1; i--)
        {
            if (seq.charAt(i) != '\\')
                break;
            backslashes++;
        }
        return backslashes % 2 != 0;
    }

    private boolean isEscape(int region)
    {
        return (Integer.MIN_VALUE & region) != 0;
    }

    private boolean isIgnored(int nextRegion)
    {
        return (nextRegion & ignored) == nextRegion;
    }

    /**
     * Computes the provided input.
     * <br>Uses the specified {@link net.dv8tion.jda.api.utils.MarkdownSanitizer.SanitizationStrategy} and
     * ignores any regions specified with {@link #withIgnored(int)}.
     *
     * @param  sequence
     *         The string to compute
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided string is null
     *
     * @return The resulting string after applying the computation
     */
    @Nonnull
    public String compute(@Nonnull String sequence)
    {
        Checks.notNull(sequence, "Input");
        StringBuilder builder = new StringBuilder();
        String end = handleQuote(sequence);
        if (end != null) return end;

        boolean onlySpacesSinceNewLine = true;
        for (int i = 0; i < sequence.length();)
        {
            int nextRegion = getRegion(i, sequence);
            char c = sequence.charAt(i);
            boolean isNewLine = c == '\n';
            boolean isSpace = c == ' ';
            onlySpacesSinceNewLine = isNewLine || (onlySpacesSinceNewLine && isSpace);

            if (nextRegion == NORMAL)
            {
                builder.append(sequence.charAt(i++));
                if ((isNewLine || (isSpace && onlySpacesSinceNewLine)) && i < sequence.length())
                {
                    String result = handleQuote(sequence.substring(i));
                    if (result != null)
                        return builder.append(result).toString();
                }
                continue;
            }

            int endRegion = findEndIndex(i, nextRegion, sequence);
            if (isIgnored(nextRegion) || endRegion == -1)
            {
                int delta = getDelta(nextRegion);
                for (int j = 0; j < delta; j++)
                    builder.append(sequence.charAt(i++));
                continue;
            }
            int delta = getDelta(nextRegion);
            applyStrategy(nextRegion, handleRegion(i + delta, endRegion, sequence, nextRegion), builder);
            i = endRegion + delta;
        }
        return builder.toString();
    }

    private String handleQuote(@Nonnull String sequence)
    {
        // Special handling for quote
        if (!isIgnored(QUOTE) && quote.matcher(sequence).matches())
        {
            int start = sequence.indexOf('>');
            if (start < 0)
                start = 0;
            StringBuilder builder = new StringBuilder(compute(sequence.substring(start + 2)));
            if (strategy == SanitizationStrategy.ESCAPE)
                builder.insert(0, "\\> ");
            return builder.toString();

        }
        else if (!isIgnored(QUOTE_BLOCK) && quoteBlock.matcher(sequence).matches())
        {
            if (strategy == SanitizationStrategy.ESCAPE)
                return compute("\\".concat(sequence));
            return compute(sequence.substring(4));
        }
        return null;
    }

    public enum SanitizationStrategy
    {
        /**
         * Remove any format tokens that are not escaped or within a special region.
         * <br>{@code "**Hello** World!" -> "Hello World!"}
         */
        REMOVE,

        /**
         * Escape any format tokens that are not escaped or within a special region.
         * <br>{@code "**Hello** World!" -> "\**Hello\** World!"}
         */
        ESCAPE,
    }
}
