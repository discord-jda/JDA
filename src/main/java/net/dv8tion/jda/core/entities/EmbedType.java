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

/**
 * Represents the embedded resource type.
 * <br>These are typically either Images, Videos or Links.
 */
public enum EmbedType
{
    IMAGE("image"),
    VIDEO("video"),
    LINK("link"),
    RICH("rich"),
    UNKNOWN("");

    private final String key;
    EmbedType(String key)
    {
        this.key = key;
    }

    /**
     * Attempts to find the EmbedType from the provided key.
     * <br>If the provided key doesn't match any known {@link net.dv8tion.jda.core.entities.EmbedType EmbedType},
     * this will return {@link net.dv8tion.jda.core.entities.EmbedType#UNKNOWN UNKNOWN}.
     *
     * @param  key
     *         The key related to the {@link net.dv8tion.jda.core.entities.EmbedType EmbedType}.
     *
     * @return The {@link net.dv8tion.jda.core.entities.EmbedType EmbedType} matching the provided key,
     *         or {@link net.dv8tion.jda.core.entities.EmbedType#UNKNOWN UNKNOWN}.
     */
    public static EmbedType fromKey(String key)
    {
        for (EmbedType type : values())
        {
            if (type.key.equals(key))
                return type;
        }
        return UNKNOWN;
    }
}
