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

import javax.annotation.Nonnull

/**
 * Represents the embedded resource type.
 * <br></br>These are typically either Images, Videos or Links.
 */
enum class EmbedType(private val key: String) {
    IMAGE("image"),
    VIDEO("video"),
    LINK("link"),
    RICH("rich"),
    AUTO_MODERATION("auto_moderation_message"),
    UNKNOWN("");

    companion object {
        /**
         * Attempts to find the EmbedType from the provided key.
         * <br></br>If the provided key doesn't match any known [EmbedType][net.dv8tion.jda.api.entities.EmbedType],
         * this will return [UNKNOWN][net.dv8tion.jda.api.entities.EmbedType.UNKNOWN].
         *
         * @param  key
         * The key related to the [EmbedType][net.dv8tion.jda.api.entities.EmbedType].
         *
         * @return The [EmbedType][net.dv8tion.jda.api.entities.EmbedType] matching the provided key,
         * or [UNKNOWN][net.dv8tion.jda.api.entities.EmbedType.UNKNOWN].
         */
        @JvmStatic
        @Nonnull
        fun fromKey(key: String): EmbedType {
            for (type in entries) {
                if (type.key == key) return type
            }
            return UNKNOWN
        }
    }
}
