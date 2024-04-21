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
 * Types of webhooks.
 */
enum class WebhookType(
    /**
     * The raw api key for this type
     *
     * @return The api key, or -1 for [.UNKNOWN]
     */
    val key: Int
) {
    /** Placeholder for unsupported types  */
    UNKNOWN(-1),

    /** Normal webhooks that can be used for sending messages  */
    INCOMING(1),

    /** Webhook responsible for re-posting messages from another channel  */
    FOLLOWER(2);

    companion object {
        /**
         * Resolves the provided raw api key to the corresponding webhook type.
         *
         * @param  key
         * The key
         *
         * @return The WebhookType or [.UNKNOWN]
         */
        @JvmStatic
        @Nonnull
        fun fromKey(key: Int): WebhookType {
            for (type in entries) {
                if (type.key == key) return type
            }
            return UNKNOWN
        }
    }
}
