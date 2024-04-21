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
package net.dv8tion.jda.api.audio

import java.util.*
import javax.annotation.Nonnull

/**
 * Flags representing the speaking modes used by discord users.
 */
enum class SpeakingMode(
    /**
     * The raw bitmask for this mode
     *
     * @return bitmask
     */
    @JvmField val raw: Int
) {
    VOICE(1),
    SOUNDSHARE(2),
    PRIORITY(4);

    companion object {
        /**
         * Parses the active modes represented by the provided bitmask
         *
         * @param  mask
         * The bitmask containing the active speaking modes
         *
         * @return [EnumSet] containing the speaking modes
         */
        @JvmStatic
        @Nonnull
        fun getModes(mask: Int): EnumSet<SpeakingMode> {
            val modes = EnumSet.noneOf(SpeakingMode::class.java)
            if (mask == 0) return modes
            val values = entries.toTypedArray()
            for (mode in values) {
                if (mode.raw and mask == mode.raw) modes.add(mode)
            }
            return modes
        }

        /**
         * Converts the given speaking modes into raw its bitmask.
         * This is only useful for sending speaking updates.
         *
         * @param  modes
         * The modes
         *
         * @return The bitmask for the provided speaking modes
         */
        fun getRaw(vararg modes: SpeakingMode?): Int {
            if (modes == null || modes.size == 0) return 0
            var mask = 0
            for (m in modes) mask = mask or m!!.raw
            return mask
        }

        /**
         * Converts the given speaking modes into raw its bitmask.
         * This is only useful for sending speaking updates.
         *
         * @param  modes
         * The modes
         *
         * @return The bitmask for the provided speaking modes
         */
        fun getRaw(modes: Collection<SpeakingMode>?): Int {
            if (modes == null) return 0
            var raw = 0
            for (mode in modes) raw = raw or mode.raw
            return raw
        }
    }
}
