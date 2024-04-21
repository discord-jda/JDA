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

import java.util.*
import javax.annotation.Nonnull

/**
 * Enum representing the flags in a [RichPresence][net.dv8tion.jda.api.entities.RichPresence]
 */
enum class ActivityFlag(
    /**
     * The offset for this flag: `1 << offset`
     *
     * @return The offset
     */
    val offset: Int
) {
    INSTANCE(0),
    JOIN(1),
    SPECTATE(2),
    JOIN_REQUEST(3),
    SYNC(4),
    PLAY(5);

    /**
     * The raw bitmask for this flag
     *
     * @return The raw bitmask
     */
    val raw: Int

    init {
        raw = 1 shl offset
    }

    companion object {
        /**
         * Maps the ActivityFlags based on the provided bitmask.
         *
         * @param  raw
         * The bitmask
         *
         * @return EnumSet containing the set activity flags
         *
         * @see RichPresence.getFlags
         * @see EnumSet EnumSet
         */
        @JvmStatic
        @Nonnull
        fun getFlags(raw: Int): EnumSet<ActivityFlag> {
            val set = EnumSet.noneOf(ActivityFlag::class.java)
            if (raw == 0) return set
            for (flag in entries) {
                if (flag.raw and raw == flag.raw) set.add(flag)
            }
            return set
        }
    }
}
