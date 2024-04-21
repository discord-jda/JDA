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
package net.dv8tion.jda.api.entities.automod

import net.dv8tion.jda.annotations.Incubating
import javax.annotation.Nonnull

/**
 * The type of event an [AutoModRule] is triggered by.
 *
 * @see AutoModRule.getEventType
 * @see AutoModRuleData.onMessage
 */
enum class AutoModEventType(
    /**
     * The raw value used by Discord to represent this type.
     *
     * @return The raw value
     */
    val key: Int
) {
    /**
     * The rule is triggered by a message being sent in a guild channel.
     */
    MESSAGE_SEND(1),

    /**
     * The rule is triggered when a member updates their profile.
     *
     * @incubating This has not been officially released yet
     */
    @Incubating
    MEMBER_UPDATE(2),

    /**
     * Placeholder for unknown types which haven't been added yet.
     */
    UNKNOWN(-1);

    companion object {
        /**
         * The [AutoModEventType] represented by the provided key.
         *
         * @param  key
         * The raw key
         *
         * @return The [AutoModEventType] or [.UNKNOWN]
         */
        @JvmStatic
        @Nonnull
        fun fromKey(key: Int): AutoModEventType {
            for (type in entries) {
                if (type.key == key) return type
            }
            return UNKNOWN
        }
    }
}
