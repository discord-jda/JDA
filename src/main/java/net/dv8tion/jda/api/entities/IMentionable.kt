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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.utils.MiscUtil
import java.util.*
import javax.annotation.Nonnull

/**
 * Marks a mentionable entity.
 *
 *
 * **Formattable**<br></br>
 * This interface extends [Formattable][java.util.Formattable] and can be used with a [Formatter][java.util.Formatter]
 * such as used by [String.format(String, Object...)][String.format]
 * or [PrintStream.printf(String, Object...)][java.io.PrintStream.printf].
 *
 *
 * This will use [.getAsMention] rather than [Object.toString]!
 * <br></br>Supported Features:
 *
 *  * **Width/Left-Justification**
 * <br></br>   - Ensures the size of a format
 * (Example: `%20s` - uses at minimum 20 chars;
 * `%-10s` - uses left-justified padding)
 *
 *  * **Precision**
 * <br></br>   - Cuts the content to the specified size
 * (Example: `%.20s`)
 *
 *
 *
 * More information on formatting syntax can be found in the [format syntax documentation][java.util.Formatter]!
 * <br></br>**Note**: Some implementations also support the **alternative** flag.
 *
 * @since 3.0
 */
interface IMentionable : Formattable, ISnowflake {
    @JvmField
    @get:Nonnull
    val asMention: String
    override fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int) {
        val leftJustified = flags and FormattableFlags.LEFT_JUSTIFY == FormattableFlags.LEFT_JUSTIFY
        val upper = flags and FormattableFlags.UPPERCASE == FormattableFlags.UPPERCASE
        val out = if (upper) asMention.uppercase(formatter.locale()) else asMention
        MiscUtil.appendTo(formatter, width, precision, leftJustified, out)
    }
}
