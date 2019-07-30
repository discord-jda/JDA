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

package net.dv8tion.jda.api.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MarkdownUtil
{
    @Nonnull
    public static String bold(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.BOLD);
        return "**" + sanitized + "**";
    }

    @Nonnull
    public static String italics(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.ITALICS_U);
        return "_" + sanitized + "_";
    }

    @Nonnull
    public static String underline(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.UNDERLINE);
        return "__" + sanitized + "__";
    }

    @Nonnull
    public static String monospace(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.MONO);
        return "`" + sanitized + "`";
    }

    @Nonnull
    public static String codeblock(@Nullable String language, @Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.BLOCK);
        if (language != null)
            return "```" + language.trim() + "\n" + sanitized + "```";
        return "```" + sanitized + "```";
    }

    @Nonnull
    public static String spoiler(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.SPOILER);
        return "||" + sanitized + "||";
    }

    @Nonnull
    public static String strike(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.STRIKE);
        return "~~" + sanitized + "~~";
    }

    @Nonnull
    public static String quote(@Nonnull String input)
    {
        String sanitized = MarkdownSanitizer.escape(input, ~MarkdownSanitizer.QUOTE);
        return "> " + sanitized.replace("\n", "\n> ");
    }

    @Nonnull
    public static String quoteBlock(@Nonnull String input)
    {
        return ">>> " + input;
    }

    @Nonnull
    public static String maskedLink(@Nonnull String text, @Nonnull String url)
    {
        return "[" + text + "](" + url.replace(")", "%29") + ")";
    }
}
