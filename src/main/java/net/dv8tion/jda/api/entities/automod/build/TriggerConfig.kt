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
import net.dv8tion.jda.api.entities.automod.AutoModRule.KeywordPreset
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.utils.data.SerializableData
import javax.annotation.Nonnull

/**
 * Configuration for [AutoModRule], which defines under what conditions the rule should be triggered.
 *
 *
 * Each rule is limited to a single trigger type. You can use the various factory methods on this interface to create a config.
 *
 *
 * Supported factories:
 *
 *  * [.mentionSpam] - Trigger on mention thresholds in messages
 *  * [.antiSpam] - Trigger on spam content in messages (classified by Discord magic)
 *  * [.keywordFilter]/[.patternFilter] - Trigger on messages containing certain keywords or regex patterns
 *  * [.presetKeywordFilter] - Trigger on messages containing words from predefined lists
 *
 *
 *
 * **Example**<br></br>
 * <pre>`AutoModRuleData rule = AutoModRule.onMessage("Invite Links",
 * TriggerConfig.keywordFilter("discord.gg/ *") // trigger on all invite links
 * .setAllowList("discord.gg/discord-api")   // except certain whitelisted ones
 * );
`</pre> *
 *
 * @see AutoModRule
 */
interface TriggerConfig : SerializableData {
    @JvmField
    @get:Nonnull
    val type: AutoModTriggerType

    companion object {
        /**
         * Trigger on mention thresholds in messages.
         *
         * @param  mentionLimit
         * The maximum number of unique mentions allowed in a message (1-{@value AutoModRule#MAX_MENTION_LIMIT})
         *
         * @throws IllegalArgumentException
         * If the provided mention limit is not between 1 and {@value AutoModRule#MAX_MENTION_LIMIT}
         *
         * @return [MentionSpamTriggerConfig]
         */
        @JvmStatic
        @Nonnull
        fun mentionSpam(mentionLimit: Int): MentionSpamTriggerConfig? {
            return MentionSpamTriggerConfig(mentionLimit)
        }

        /**
         * Trigger on spam content in messages (classified by Discord magic).
         *
         * @return [AntiSpamTriggerConfig]
         */
        @Nonnull
        fun antiSpam(): AntiSpamTriggerConfig? {
            return AntiSpamTriggerConfig()
        }

        /**
         * Trigger on messages containing certain keywords or regex patterns.
         * <br></br>Keywords are matched case-insensitively, and may also contain whitespace.
         *
         *
         * You can use wildcards at the keyword boundaries to extend the matches:
         * <br></br>`"foo*"` can match `"foo"`, `"foobar"`, `"foo-bar"`, etc.
         * <br></br>`"*foo*"` can match `"foo"`, `"foobar"`, `"barfoo"`, etc.
         * <br></br>`"*foo"` can match `"foo"`, `"barfoo"`, `"bar-foo"`, etc.
         *
         *
         * You can also use regex patterns using [.patternFilter] or [CustomKeywordTriggerConfig.addPatterns].
         *
         * @param  keywords
         * The keywords to match (case-insensitive)
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the keywords are empty, blank, or null
         *  * If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added
         *  * If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters
         *
         *
         * @return [CustomKeywordTriggerConfig]
         */
        @Nonnull
        fun keywordFilter(@Nonnull keywords: Collection<String?>): CustomKeywordTriggerConfig? {
            return CustomKeywordTriggerConfig().addKeywords(keywords)
        }

        /**
         * Trigger on messages containing certain keywords or regex patterns.
         * <br></br>Keywords are matched case-insensitively, and may also contain whitespace.
         *
         *
         * You can use wildcards at the keyword boundaries to extend the matches:
         * <br></br>`"foo*"` can match `"foo"`, `"foobar"`, `"foo-bar"`, etc.
         * <br></br>`"*foo*"` can match `"foo"`, `"foobar"`, `"barfoo"`, etc.
         * <br></br>`"*foo"` can match `"foo"`, `"barfoo"`, `"bar-foo"`, etc.
         *
         *
         * You can also use regex patterns using [.patternFilter] or [CustomKeywordTriggerConfig.addPatterns].
         *
         * @param  keywords
         * The keywords to match (case-insensitive)
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the keywords are empty, blank, or null
         *  * If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added
         *  * If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters
         *
         *
         * @return [CustomKeywordTriggerConfig]
         */
        @JvmStatic
        @Nonnull
        fun keywordFilter(@Nonnull vararg keywords: String?): CustomKeywordTriggerConfig? {
            return CustomKeywordTriggerConfig().addKeywords(*keywords)
        }

        /**
         * Trigger on messages containing certain keywords regex patterns.
         * <br></br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
         *
         *
         * Patterns may use anything supported by the rust regex crate.
         * You can use a validator such as [Rustexp](https://rustexp.lpil.uk/) to validate your pattern.
         *
         *
         * You can also use simple substring keywords using [.keywordFilter] or [CustomKeywordTriggerConfig.addKeywords].
         *
         * @param  patterns
         * The keyword patterns to match
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the patterns are empty, blank, or null
         *  * If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added
         *  * If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters
         *
         *
         * @return [CustomKeywordTriggerConfig]
         */
        @Nonnull
        fun patternFilter(@Nonnull patterns: Collection<String?>): CustomKeywordTriggerConfig? {
            return CustomKeywordTriggerConfig().addPatterns(patterns)
        }

        /**
         * Trigger on messages containing certain keywords regex patterns.
         * <br></br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
         *
         *
         * Patterns may use anything supported by the rust regex crate.
         * You can use a validator such as [Rustexp](https://rustexp.lpil.uk/) to validate your pattern.
         *
         *
         * You can also use simple substring keywords using [.keywordFilter] or [CustomKeywordTriggerConfig.addKeywords].
         *
         * @param  patterns
         * The keyword patterns to match
         *
         * @throws IllegalArgumentException
         *
         *  * If any of the patterns are empty, blank, or null
         *  * If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added
         *  * If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters
         *
         *
         * @return [CustomKeywordTriggerConfig]
         */
        @JvmStatic
        @Nonnull
        fun patternFilter(@Nonnull vararg patterns: String?): CustomKeywordTriggerConfig? {
            return CustomKeywordTriggerConfig().addPatterns(*patterns)
        }

        /**
         * Trigger on keywords from predefined lists.
         *
         * @param  presets
         * The presets to enable
         *
         * @throws IllegalArgumentException
         * If null or [KeywordPreset.UNKNOWN] is provided
         *
         * @return [PresetKeywordTriggerConfig]
         */
        @Nonnull
        fun presetKeywordFilter(@Nonnull presets: Collection<KeywordPreset?>): PresetKeywordTriggerConfig? {
            return PresetKeywordTriggerConfig().enablePresets(presets)
        }

        /**
         * Trigger on keywords from predefined lists.
         *
         * @param  presets
         * The presets to enable
         *
         * @throws IllegalArgumentException
         * If null or [KeywordPreset.UNKNOWN] is provided
         *
         * @return [PresetKeywordTriggerConfig]
         */
        @JvmStatic
        @Nonnull
        fun presetKeywordFilter(@Nonnull vararg presets: KeywordPreset?): PresetKeywordTriggerConfig? {
            return PresetKeywordTriggerConfig().enablePresets(*presets)
        }
    }
}
