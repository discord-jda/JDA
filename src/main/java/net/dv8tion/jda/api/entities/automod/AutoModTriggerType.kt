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
import net.dv8tion.jda.internal.utils.Helpers
import java.util.*
import javax.annotation.Nonnull

/**
 * The type which defines what triggers an [AutoModRule].
 */
enum class AutoModTriggerType(
    /**
     * The raw API key used to indicate this type.
     *
     * @return The int key
     */
    val key: Int,
    /**
     * The maximum number of rules that can use this trigger type in a guild.
     *
     * @return The maximum number of rules
     */
    @JvmField val maxPerGuild: Int, vararg supportedEvents: AutoModEventType
) {
    /**
     * The rule is triggered by user message content containing specific keywords or phrases.
     */
    KEYWORD(1, 6, AutoModEventType.MESSAGE_SEND, AutoModEventType.MEMBER_UPDATE),

    /**
     * The rule is triggered by user message content containing classified spam content.
     */
    SPAM(3, 1, AutoModEventType.MESSAGE_SEND),

    /**
     * The rule is triggered by user message content containing keywords from a predefined list (such as [slurs][AutoModRule.KeywordPreset.SLURS]).
     */
    KEYWORD_PRESET(4, 1, AutoModEventType.MESSAGE_SEND),

    /**
     * The rule is triggered by user message content containing more than the allowed number of mentions.
     */
    MENTION_SPAM(5, 1, AutoModEventType.MESSAGE_SEND),

    /**
     * The rule is triggered by a member profile containing specific keywords or phrases.
     *
     * @incubating This has not been officially released yet
     */
    @Incubating
    MEMBER_PROFILE_KEYWORD(6, 1, AutoModEventType.MEMBER_UPDATE),

    /**
     * Placeholder for unknown trigger types that haven't been added yet.
     */
    UNKNOWN(-1, 0);

    private var eventTypes: EnumSet<AutoModEventType>? = null

    init {
        if (supportedEvents.size > 0) eventTypes = EnumSet.of(supportedEvents[0], *supportedEvents) else eventTypes =
            EnumSet.noneOf(
                AutoModEventType::class.java
            )
    }

    @get:Nonnull
    val supportedEventTypes: EnumSet<AutoModEventType>
        /**
         * The [AutoModEventTypes][AutoModEventType] that support this trigger type.
         *
         * @return The supported event types
         */
        get() = Helpers.copyEnumSet(AutoModEventType::class.java, eventTypes)

    /**
     * Whether the provided [AutoModEventType] is supported by this trigger type.
     *
     * @param  type
     * The event type to check
     *
     * @return True, if the event type is supported
     */
    fun isEventTypeSupported(@Nonnull type: AutoModEventType?): Boolean {
        return type != null && eventTypes!!.contains(type)
    }

    companion object {
        /**
         * The [AutoModTriggerType] that matches the provided key.
         *
         * @param  key
         * The key to match
         *
         * @return The matching [AutoModTriggerType] or [.UNKNOWN]
         */
        @JvmStatic
        @Nonnull
        fun fromKey(key: Int): AutoModTriggerType {
            for (trigger in entries) {
                if (trigger.key == key) return trigger
            }
            return UNKNOWN
        }
    }
}
