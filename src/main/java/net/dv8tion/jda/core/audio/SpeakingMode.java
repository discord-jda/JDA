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

package net.dv8tion.jda.core.audio;

import java.util.EnumSet;

/**
 * Flags representing the speaking modes used by discord users.
 */
public enum SpeakingMode
{
    VOICE(1), SOUNDSHARE(2);

    private final int raw;

    SpeakingMode(int raw)
    {
        this.raw = raw;
    }

    /**
     * The raw bitmask for this mode
     *
     * @return bitmask
     */
    public int getRaw()
    {
        return raw;
    }

    /**
     * Parses the active modes represented by the provided bitmask
     *
     * @param  mask
     *         The bitmask containing the active speaking modes
     *
     * @return {@link EnumSet EnumSet} containing the speaking modes
     */
    public static EnumSet<SpeakingMode> getModes(int mask)
    {
        final EnumSet<SpeakingMode> modes = EnumSet.noneOf(SpeakingMode.class);
        if (mask == 0)
            return modes;
        final SpeakingMode[] values = SpeakingMode.values();
        for (SpeakingMode mode : values)
        {
            if ((mode.raw & mask) == mode.raw)
                modes.add(mode);
        }
        return modes;
    }

    /**
     * Converts the given speaking modes into raw its bitmast.
     * This is only useful for sending speaking updates.
     *
     * @param  modes
     *         The modes
     *
     * @return The bitmask for the provided speaking modes
     */
    public static int getRaw(SpeakingMode... modes)
    {
        if (modes == null || modes.length == 0)
            return 0;
        int mask = 0;
        for (SpeakingMode m : modes)
            mask |= m.raw;
        return mask;
    }
}
