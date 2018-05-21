/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.entities;

import java.util.EnumSet;

/**
 * Enum representing the flags in a {@link net.dv8tion.jda.core.entities.RichPresence RichPresence}
 */
public enum ActivityFlag
{
    INSTANCE(0),
    JOIN(1),
    SPECTATE(2),
    JOIN_REQUEST(3),
    SYNC(4),
    PLAY(5);

    private final int offset;
    private final int raw;

    ActivityFlag(int offset)
    {
        this.offset = offset;
        this.raw = 1 << offset;
    }

    /**
     * The offset for this flag: {@code 1 << offset}
     *
     * @return The offset
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * The raw bitmask for this flag
     *
     * @return The raw bitmask
     */
    public int getRaw()
    {
        return raw;
    }

    /**
     * Maps the ActivityFlags based on the provided bitmask.
     *
     * @param  raw
     *         The bitmask
     *
     * @return EnumSet containing the set activity flags
     *
     * @see    RichPresence#getFlags()
     * @see    EnumSet EnumSet
     */
    public static EnumSet<ActivityFlag> getFlags(int raw)
    {
        EnumSet<ActivityFlag> set = EnumSet.noneOf(ActivityFlag.class);
        if (raw == 0)
            return set;
        for (ActivityFlag flag : values())
        {
            if ((flag.getRaw() & raw) == flag.getRaw())
                set.add(flag);
        }
        return set;
    }
}
