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

public enum SpeakingMode
{
    NONE(0), VOICE(1), SOUNDSHARE(2);

    private final int raw;

    SpeakingMode(int raw)
    {
        this.raw = raw;
    }

    public int getRaw()
    {
        return raw;
    }

    public static EnumSet<SpeakingMode> getModes(int mask)
    {
        if (mask == 0)
            return EnumSet.of(SpeakingMode.NONE);
        final EnumSet<SpeakingMode> modes = EnumSet.noneOf(SpeakingMode.class);
        final SpeakingMode[] values = SpeakingMode.values();
        for (int i = 1; i < values.length; i++)
        {
            SpeakingMode mode = values[i];
            if ((mode.raw & mask) == mode.raw)
                modes.add(mode);
        }
        return modes;
    }
}
