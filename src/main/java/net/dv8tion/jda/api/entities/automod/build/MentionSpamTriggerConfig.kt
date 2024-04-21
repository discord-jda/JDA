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
package net.dv8tion.jda.api.entities.automod.build

import net.dv8tion.jda.api.entities.automod.AutoModRule
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * Configuration for [MENTION_SPAM][AutoModTriggerType.MENTION_SPAM] trigger.
 */
class MentionSpamTriggerConfig(private var mentionLimit: Int) :
    AbstractTriggerConfig<MentionSpamTriggerConfig?>(AutoModTriggerType.MENTION_SPAM), TriggerConfig {
    private var isMentionRaidProtectionEnabled = false

    /**
     * Configure the maximum number of unique mentions allowed in a message.
     *
     * @param  mentionLimit
     * The maximum number of unique mentions allowed in a message (1-{@value AutoModRule#MAX_MENTION_LIMIT})
     *
     * @throws IllegalArgumentException
     * If the provided mention limit is not between 1 and {@value AutoModRule#MAX_MENTION_LIMIT}
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun setMentionLimit(mentionLimit: Int): MentionSpamTriggerConfig {
        Checks.positive(mentionLimit, "Mention Limit")
        Checks.check(
            mentionLimit <= AutoModRule.MAX_MENTION_LIMIT,
            "Mention Limit cannot be higher than %d. Provided: %d",
            AutoModRule.MAX_MENTION_LIMIT,
            mentionLimit
        )
        this.mentionLimit = mentionLimit
        return this
    }

    /**
     * Whether to enable mention raid protection.
     *
     * @param  enabled
     * True, if mention raid protection should be enabled
     *
     * @return The current config for chaining convenience
     */
    @Nonnull
    fun setMentionRaidProtectionEnabled(enabled: Boolean): MentionSpamTriggerConfig {
        isMentionRaidProtectionEnabled = enabled
        return this
    }

    @Nonnull
    override fun toData(): DataObject {
        val data = super.toData()
        data.put("mention_total_limit", mentionLimit)
        data.put("mention_raid_protection_enabled", isMentionRaidProtectionEnabled)
        return data
    }
}
