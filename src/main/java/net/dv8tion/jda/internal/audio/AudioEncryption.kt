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

package net.dv8tion.jda.internal.audio;

import net.dv8tion.jda.api.utils.data.DataArray;

public enum AudioEncryption
{
    // these are ordered by priority, lite > suffix > normal
    // we prefer lite because it uses only 4 bytes for its nonce while the others use 24 bytes
    XSALSA20_POLY1305_LITE,
    XSALSA20_POLY1305_SUFFIX,
    XSALSA20_POLY1305;

    private final String key;

    AudioEncryption()
    {
        this.key = name().toLowerCase();
    }

    public String getKey()
    {
        return key;
    }

    public static AudioEncryption getPreferredMode(DataArray array)
    {
        AudioEncryption encryption = null;
        for (Object o : array)
        {
            try
            {
                String name = String.valueOf(o).toUpperCase();
                AudioEncryption e = valueOf(name);
                if (encryption == null || e.ordinal() < encryption.ordinal())
                    encryption = e;
            }
            catch (IllegalArgumentException ignored) {}
        }
        return encryption;
    }
}
