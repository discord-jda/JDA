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

import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public enum AudioEncryption {
    // ordered by priority descending
    AEAD_AES256_GCM_RTPSIZE,
    AEAD_XCHACHA20_POLY1305_RTPSIZE;

    private final String key;

    AudioEncryption() {
        this.key = name().toLowerCase(Locale.ROOT);
    }

    public String getKey() {
        return key;
    }

    public static AudioEncryption getPreferredMode(DataArray array) {
        AudioEncryption encryption = null;
        for (Object o : array) {
            try {
                String name = String.valueOf(o).toUpperCase(Locale.ROOT);
                AudioEncryption e = valueOf(name);
                if (encryption == null || e.ordinal() < encryption.ordinal()) {
                    encryption = e;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return encryption;
    }

    public static EnumSet<AudioEncryption> fromArray(DataArray modes) {
        return modes.stream(DataArray::getString)
                .map(mode -> mode.toLowerCase(Locale.ROOT))
                .map(AudioEncryption::forMode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AudioEncryption.class)));
    }

    public static AudioEncryption forMode(String mode) {
        switch (mode) {
            case "aead_aes256_gcm_rtpsize":
                return AEAD_AES256_GCM_RTPSIZE;
            case "aead_xchacha20_poly1305_rtpsize":
                return AEAD_XCHACHA20_POLY1305_RTPSIZE;
            default:
                return null;
        }
    }
}
