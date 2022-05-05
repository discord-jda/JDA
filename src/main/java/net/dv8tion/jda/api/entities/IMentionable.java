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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.Nonnull;
import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;

/**
 * Marks a mentionable entity.
 *
 * <h2>Formattable</h2>
 * This interface extends {@link java.util.Formattable Formattable} and can be used with a {@link java.util.Formatter Formatter}
 * such as used by {@link String#format(String, Object...) String.format(String, Object...)}
 * or {@link java.io.PrintStream#printf(String, Object...) PrintStream.printf(String, Object...)}.
 *
 * <p>This will use {@link #getAsMention()} rather than {@link Object#toString()}!
 * <br>Supported Features:
 * <ul>
 *     <li><b>Width/Left-Justification</b>
 *     <br>   - Ensures the size of a format
 *              (Example: {@code %20s} - uses at minimum 20 chars;
 *              {@code %-10s} - uses left-justified padding)</li>
 *
 *     <li><b>Precision</b>
 *     <br>   - Cuts the content to the specified size
 *              (Example: {@code %.20s})</li>
 * </ul>
 *
 * <p>More information on formatting syntax can be found in the {@link java.util.Formatter format syntax documentation}!
 * <br><b>Note</b>: Some implementations also support the <b>alternative</b> flag.
 *
 * @since 3.0
 */
public interface IMentionable extends Formattable, ISnowflake
{
    /**
     * Retrieve a Mention for this Entity.
     * For the public {@link net.dv8tion.jda.api.entities.Role Role} (@everyone), this will return the literal string {@code "@everyone"}.
     *
     * @return A resolvable mention.
     */
    @Nonnull
    String getAsMention();

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        String out = upper ? getAsMention().toUpperCase(formatter.locale()) : getAsMention();

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}
