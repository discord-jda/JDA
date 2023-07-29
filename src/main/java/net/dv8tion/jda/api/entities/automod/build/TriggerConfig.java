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

package net.dv8tion.jda.api.entities.automod.build;

import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModRule.KeywordPreset;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Configuration for {@link AutoModRule}, which defines under what conditions the rule should be triggered.
 *
 * <p>Each rule is limited to a single trigger type. You can use the various factory methods on this interface to create a config.
 *
 * <p>Supported factories:
 * <ul>
 *     <li>{@link #mentionSpam(int)} - Trigger on mention thresholds in messages</li>
 *     <li>{@link #antiSpam()} - Trigger on spam content in messages (classified by Discord magic)</li>
 *     <li>{@link #keywordFilter(Collection)}/{@link #patternFilter(Collection)} - Trigger on messages containing certain keywords or regex patterns</li>
 *     <li>{@link #presetKeywordFilter(AutoModRule.KeywordPreset...)} - Trigger on messages containing words from predefined lists</li>
 * </ul>
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * AutoModRuleData rule = AutoModRule.onMessage("Invite Links",
 *   TriggerConfig.keywordFilter("discord.gg/*") // trigger on all invite links
 *     .setAllowList("discord.gg/discord-api")   // except certain whitelisted ones
 * );
 * }</pre>
 *
 * @see AutoModRule
 */
public interface TriggerConfig extends SerializableData
{
    /**
     * The type of trigger for this config.
     *
     * @return {@link AutoModTriggerType}
     */
    @Nonnull
    AutoModTriggerType getType();

    /**
     * Trigger on mention thresholds in messages.
     *
     * @param  mentionLimit
     *         The maximum number of unique mentions allowed in a message (1-{@value AutoModRule#MAX_MENTION_LIMIT})
     *
     * @throws IllegalArgumentException
     *         If the provided mention limit is not between 1 and {@value AutoModRule#MAX_MENTION_LIMIT}
     *
     * @return {@link MentionSpamTriggerConfig}
     */
    @Nonnull
    static MentionSpamTriggerConfig mentionSpam(int mentionLimit)
    {
        return new MentionSpamTriggerConfig(mentionLimit);
    }

    /**
     * Trigger on spam content in messages (classified by Discord magic).
     *
     * @return {@link AntiSpamTriggerConfig}
     */
    @Nonnull
    static AntiSpamTriggerConfig antiSpam()
    {
        return new AntiSpamTriggerConfig();
    }

    /**
     * Trigger on messages containing certain keywords or regex patterns.
     * <br>Keywords are matched case-insensitively, and may also contain whitespace.
     *
     * <p>You can use wildcards at the keyword boundaries to extend the matches:
     * <br>{@code "foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "foo-bar"}, etc.
     * <br>{@code "*foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "barfoo"}, etc.
     * <br>{@code "*foo"} can match {@code "foo"}, {@code "barfoo"}, {@code "bar-foo"}, etc.
     *
     * <p>You can also use regex patterns using {@link #patternFilter(Collection)} or {@link CustomKeywordTriggerConfig#addPatterns(Collection)}.
     *
     * @param  keywords
     *         The keywords to match (case-insensitive)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the keywords are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added</li>
     *             <li>If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters</li>
     *         </ul>
     *
     * @return {@link CustomKeywordTriggerConfig}
     */
    @Nonnull
    static CustomKeywordTriggerConfig keywordFilter(@Nonnull Collection<String> keywords)
    {
        return new CustomKeywordTriggerConfig().addKeywords(keywords);
    }

    /**
     * Trigger on messages containing certain keywords or regex patterns.
     * <br>Keywords are matched case-insensitively, and may also contain whitespace.
     *
     * <p>You can use wildcards at the keyword boundaries to extend the matches:
     * <br>{@code "foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "foo-bar"}, etc.
     * <br>{@code "*foo*"} can match {@code "foo"}, {@code "foobar"}, {@code "barfoo"}, etc.
     * <br>{@code "*foo"} can match {@code "foo"}, {@code "barfoo"}, {@code "bar-foo"}, etc.
     *
     * <p>You can also use regex patterns using {@link #patternFilter(String...)} or {@link CustomKeywordTriggerConfig#addPatterns(String...)}.
     *
     * @param  keywords
     *         The keywords to match (case-insensitive)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the keywords are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_KEYWORD_AMOUNT} keywords are added</li>
     *             <li>If any of the keywords is longer than {@value AutoModRule#MAX_KEYWORD_LENGTH} characters</li>
     *         </ul>
     *
     * @return {@link CustomKeywordTriggerConfig}
     */
    @Nonnull
    static CustomKeywordTriggerConfig keywordFilter(@Nonnull String... keywords)
    {
        return new CustomKeywordTriggerConfig().addKeywords(keywords);
    }

    /**
     * Trigger on messages containing certain keywords regex patterns.
     * <br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
     *
     * <p>Patterns may use anything supported by the rust regex crate.
     * You can use a validator such as <a href="https://rustexp.lpil.uk/" target="_blank">Rustexp</a> to validate your pattern.
     *
     * <p>You can also use simple substring keywords using {@link #keywordFilter(String...)} or {@link CustomKeywordTriggerConfig#addKeywords(String...)}.
     *
     * @param  patterns
     *         The keyword patterns to match
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the patterns are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added</li>
     *             <li>If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters</li>
     *         </ul>
     *
     * @return {@link CustomKeywordTriggerConfig}
     */
    @Nonnull
    static CustomKeywordTriggerConfig patternFilter(@Nonnull Collection<String> patterns)
    {
        return new CustomKeywordTriggerConfig().addPatterns(patterns);
    }

    /**
     * Trigger on messages containing certain keywords regex patterns.
     * <br>Keyword patterns are matched case-insensitively, and may also contain whitespace.
     *
     * <p>Patterns may use anything supported by the rust regex crate.
     * You can use a validator such as <a href="https://rustexp.lpil.uk/" target="_blank">Rustexp</a> to validate your pattern.
     *
     * <p>You can also use simple substring keywords using {@link #keywordFilter(String...)} or {@link CustomKeywordTriggerConfig#addKeywords(String...)}.
     *
     * @param  patterns
     *         The keyword patterns to match
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the patterns are empty, blank, or null</li>
     *             <li>If more than {@value AutoModRule#MAX_PATTERN_AMOUNT} patterns are added</li>
     *             <li>If any of the patterns is longer than {@value AutoModRule#MAX_PATTERN_LENGTH} characters</li>
     *         </ul>
     *
     * @return {@link CustomKeywordTriggerConfig}
     */
    @Nonnull
    static CustomKeywordTriggerConfig patternFilter(@Nonnull String... patterns)
    {
        return new CustomKeywordTriggerConfig().addPatterns(patterns);
    }

    /**
     * Trigger on keywords from predefined lists.
     *
     * @param  presets
     *         The presets to enable
     *
     * @throws IllegalArgumentException
     *         If null or {@link KeywordPreset#UNKNOWN} is provided
     *
     * @return {@link PresetKeywordTriggerConfig}
     */
    @Nonnull
    static PresetKeywordTriggerConfig presetKeywordFilter(@Nonnull Collection<AutoModRule.KeywordPreset> presets)
    {
        return new PresetKeywordTriggerConfig().enablePresets(presets);
    }

    /**
     * Trigger on keywords from predefined lists.
     *
     * @param  presets
     *         The presets to enable
     *
     * @throws IllegalArgumentException
     *         If null or {@link KeywordPreset#UNKNOWN} is provided
     *
     * @return {@link PresetKeywordTriggerConfig}
     */
    @Nonnull
    static PresetKeywordTriggerConfig presetKeywordFilter(@Nonnull AutoModRule.KeywordPreset... presets)
    {
        return new PresetKeywordTriggerConfig().enablePresets(presets);
    }
}
