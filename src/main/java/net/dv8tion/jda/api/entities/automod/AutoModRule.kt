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

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.managers.AutoModRuleManager
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import java.util.*
import javax.annotation.Nonnull

/**
 * Rule used for auto-moderation in a [Guild].
 *
 * @see Guild.retrieveAutoModRules
 * @see Guild.createAutoModRule
 */
interface AutoModRule : ISnowflake {
    @get:Nonnull
    val guild: Guild

    /**
     * The user id of the creator of this rule.
     *
     * @return The owner id
     */
    val creatorIdLong: Long

    @get:Nonnull
    val creatorId: String?
        /**
         * The user id of the creator of this rule.
         *
         * @return The owner id
         */
        get() = java.lang.Long.toUnsignedString(creatorIdLong)

    @get:Nonnull
    val name: String?

    @get:Nonnull
    val eventType: AutoModEventType?

    @get:Nonnull
    val triggerType: AutoModTriggerType?

    /**
     * Whether this rule is enabled.
     *
     * @return True, if enabled
     */
    val isEnabled: Boolean

    @get:Nonnull
    val exemptRoles: List<Role?>?

    @get:Nonnull
    val exemptChannels: List<GuildChannel?>?

    @get:Nonnull
    val actions: List<AutoModResponse?>?

    @get:Nonnull
    val filteredKeywords: List<String?>?

    @get:Nonnull
    val filteredRegex: List<String?>?

    @get:Nonnull
    val filteredPresets: EnumSet<KeywordPreset?>?

    @get:Nonnull
    val allowlist: List<String?>?

    /**
     * The maximum amount of mentions that are allowed in a message.
     *
     * Only applies to [AutoModTriggerType.MENTION_SPAM].
     *
     * @return The mention limit, or 0 if this is not using [AutoModTriggerType.MENTION_SPAM]
     */
    val mentionLimit: Int

    /**
     * Whether this rule is using the raid protection feature.
     *
     * Only applies to [AutoModTriggerType.MENTION_SPAM].
     *
     * @return True, if mention raid protection is enabled
     */
    val isMentionRaidProtectionEnabled: Boolean

    @get:Nonnull
    val manager: AutoModRuleManager?
        /**
         * Returns an [AutoModRuleManager], which can be used to modify this rule.
         *
         * The manager allows modifying multiple fields in a single request.
         * <br></br>You modify multiple fields in one request by chaining setters before calling [RestAction.queue()][net.dv8tion.jda.api.requests.RestAction.queue].
         *
         * @throws InsufficientPermissionException
         * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission.
         *
         * @return The manager instance
         */
        get() = guild.modifyAutoModRuleById(id)

    /**
     * Deletes this rule.
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have the [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] permission.
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [Void]
     */
    @Nonnull
    fun delete(): AuditableRestAction<Void?>? {
        return guild.deleteAutoModRuleById(id)
    }

    /**
     * Keyword presets that can be used in [AutoModRule.getFilteredPresets].
     */
    enum class KeywordPreset(
        /**
         * The raw value used by Discord to represent this preset.
         *
         * @return The raw value
         */
        val key: Int
    ) {
        /**
         * Words that can be considered as swearing or cursing.
         */
        PROFANITY(1),

        /**
         * Words that can be considered as sexual in nature.
         */
        SEXUAL_CONTENT(2),

        /**
         * Words that can be considered as slurs or insults.
         */
        SLURS(3),

        /**
         * Placeholder for unknown values.
         */
        UNKNOWN(-1);

        companion object {
            /**
             * The [KeywordPreset] represented by the provided key.
             *
             * @param  key
             * The raw key
             *
             * @return The [KeywordPreset] or [.UNKNOWN]
             */
            @JvmStatic
            @Nonnull
            fun fromKey(key: Int): KeywordPreset {
                for (preset in entries) {
                    if (preset.key == key) return preset
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        /**
         * The maximum length of a rule name. ({@value})
         */
        const val MAX_RULE_NAME_LENGTH = 100

        /**
         * The maximum length of a keyword in [TriggerConfig.keywordFilter]. ({@value})
         */
        const val MAX_KEYWORD_LENGTH = 60

        /**
         * The maximum amount of keywords in [TriggerConfig.keywordFilter]. ({@value})
         */
        const val MAX_KEYWORD_AMOUNT = 1000

        /**
         * The maximum amount of whitelisted keywords in [TriggerConfig.keywordFilter]. ({@value})
         */
        const val MAX_ALLOWLIST_CUSTOM_AMOUNT = 100

        /**
         * The maximum amount of whitelisted keywords in [TriggerConfig.presetKeywordFilter]. ({@value})
         */
        const val MAX_ALLOWLIST_PRESET_AMOUNT = 1000

        /**
         * The maximum length of a regex pattern in [TriggerConfig.patternFilter]. ({@value})
         */
        const val MAX_PATTERN_LENGTH = 260

        /**
         * The maximum amount of regex patterns in [TriggerConfig.patternFilter]. ({@value})
         */
        const val MAX_PATTERN_AMOUNT = 10

        /**
         * The maximum limit of mentions in [TriggerConfig.mentionSpam]. ({@value})
         */
        const val MAX_MENTION_LIMIT = 50

        /**
         * The maximum amount of roles that can be added to [AutoModRule.getExemptRoles]. ({@value})
         */
        const val MAX_EXEMPT_ROLES = 20

        /**
         * The maximum amount of channels that can be added to [AutoModRule.getExemptChannels]. ({@value})
         */
        const val MAX_EXEMPT_CHANNELS = 50
    }
}
