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

package net.dv8tion.jda.api.entities.automod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.build.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * Rule used for auto-moderation in a {@link Guild}.
 *
 * @see Guild#retrieveAutoModRules()
 * @see Guild#createAutoModRule(AutoModRuleData)
 */
public interface AutoModRule extends ISnowflake
{
    /**
     * The maximum length of a rule name. ({@value})
     */
    int MAX_RULE_NAME_LENGTH = 100;
    /**
     * The maximum length of a keyword in {@link #createCustomKeywordRule(String, String...)}. ({@value})
     */
    int MAX_KEYWORD_LENGTH = 60;
    /**
     * The maximum amount of keywords in {@link #createCustomKeywordRule(String, String...)}. ({@value})
     */
    int MAX_KEYWORD_AMOUNT = 1000;
    /**
     * The maximum amount of whitelisted keywords in {@link #createCustomKeywordRule(String, String...)}. ({@value})
     */
    int MAX_ALLOWLIST_CUSTOM_AMOUNT = 100;
    /**
     * The maximum amount of whitelisted keywords in {@link #createPresetKeywordRule(String, KeywordPreset...)}. ({@value})
     */
    int MAX_ALLOWLIST_PRESET_AMOUNT = 1000;
    /**
     * The maximum length of a regex pattern in {@link #createCustomKeywordRule(String, String...)}. ({@value})
     */
    int MAX_PATTERN_LENGTH = 260;
    /**
     * The maximum amount of regex patterns in {@link #createCustomKeywordRule(String, String...)}. ({@value})
     */
    int MAX_PATTERN_AMOUNT = 10;
    /**
     * The maximum limit of mentions in {@link #createMentionSpamRule(String, int)}. ({@value})
     */
    int MAX_MENTION_LIMIT = 50;

    /**
     * Creates a {@link AutoModTriggerType#MENTION_SPAM MENTION_SPAM} rule.
     *
     * <p>Every automod rule must have at least one {@link AutoModResponse} configured.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * AutoModRule.createMentionSpamRule("Mention Spam", 10)
     *     .putResponse(AutoModResponse.blockMessage("Don't spam mentions!"))
     *     .addExemptRoles(modRole)
     *     .build()
     * }</pre>
     *
     * @param  name
     *         The name of the rule (max {@value #MAX_RULE_NAME_LENGTH} characters)
     * @param  limit
     *         The maximum amount of mentions allowed (up to {@value #MAX_MENTION_LIMIT})
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the name is null, empty, or longer than {@value #MAX_RULE_NAME_LENGTH} characters.</li>
     *             <li>If the limit is not between 1 and {@value #MAX_MENTION_LIMIT}</li>
     *         </ul>
     *
     * @return {@link MentionSpamRuleBuilder} instance to build the rule
     */
    @Nonnull
    static MentionSpamRuleBuilder createMentionSpamRule(@Nonnull String name, int limit)
    {
        return new MentionSpamRuleBuilder(name, limit);
    }

    /**
     * Creates a {@link AutoModTriggerType#KEYWORD KEYWORD} rule.
     * <br>Keywords may also use wildcards at the beginning and end of the keyword (for example {@code "foo*"} would match {@code "foobar"}).
     * Keywords can also contain whitespace to block phrases like {@code "foo bar"}. Additionally, keywords are case-insensitive.
     *
     * <p>Every automod rule must have at least one {@link AutoModResponse} configured.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * AutoModRule.createCustomKeywordRule("No morbius memes", "morb*", "*morb")
     *     .putResponse(AutoModResponse.blockMessage("This is not a funny meme."))
     *     .build()
     * }</pre>
     *
     * @param  name
     *         The name of the rule (max {@value #MAX_RULE_NAME_LENGTH} characters)
     * @param  keywords
     *         The blocked keywords (max {@value #MAX_KEYWORD_AMOUNT} keywords, max {@value #MAX_KEYWORD_LENGTH} characters per keyword)
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the name is null, empty, or longer than {@value #MAX_RULE_NAME_LENGTH} characters.</li>
     *             <li>If any keyword is empty or longer than {@value #MAX_KEYWORD_LENGTH}</li>
     *             <li>If more than {@value #MAX_KEYWORD_AMOUNT} keywords are provided</li>
     *         </ul>
     *
     * @return {@link CustomKeywordRuleBuilder} instance to build the rule
     */
    @Nonnull
    static CustomKeywordRuleBuilder createCustomKeywordRule(@Nonnull String name, @Nonnull String... keywords)
    {
        return new CustomKeywordRuleBuilder(name).addKeywords(keywords);
    }

    /**
     * Creates a {@link AutoModTriggerType#KEYWORD_PRESET KEYWORD_PRESET} rule.
     * <br>In the {@link PresetKeywordRuleBuilder#setAllowList(Collection) allowlist},
     * keywords may also use wildcards at the beginning and end of the keyword (for example {@code "foo*"} would match {@code "foobar"}).
     * Keywords can also contain whitespace to block phrases like {@code "foo bar"}. Additionally, keywords are case-insensitive.
     *
     * <p>Every automod rule must have at least one {@link AutoModResponse} configured.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * AutoModRule.createPresetKeywordRule("No slurs", KeywordPreset.SLURS)
     *     .putResponse(AutoModResponse.blockMessage("Please refrain from using this kind of language."))
     *     .build()
     * }</pre>
     *
     * @param  name
     *         The name of the rule (max {@value #MAX_RULE_NAME_LENGTH} characters)
     * @param  presets
     *         Preset lists of keywords to block. (Should be at least 1 preset)
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the name is null, empty, or longer than {@value #MAX_RULE_NAME_LENGTH} characters.</li>
     *             <li>If null is provided</li>
     *         </ul>
     *
     * @return {@link PresetKeywordRuleBuilder} instance to build the rule
     */
    @Nonnull
    static PresetKeywordRuleBuilder createPresetKeywordRule(@Nonnull String name, @Nonnull KeywordPreset... presets)
    {
        return new PresetKeywordRuleBuilder(name).enablePresets(presets);
    }

    /**
     * Creates a {@link AutoModTriggerType#SPAM SPAM} rule.
     *
     * <p>Every automod rule must have at least one {@link AutoModResponse} configured.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * AutoModRule.createAntiSpamRule("Spam detected")
     *     .putResponse(AutoModResponse.timeoutMember(Duration.ofMinutes(1))
     *     .addExemptRoles(modRole)
     *     .build()
     * }</pre>
     *
     * @param  name
     *         The name of the rule (max {@value #MAX_RULE_NAME_LENGTH} characters)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the name is null, empty, or longer than {@value #MAX_RULE_NAME_LENGTH} characters.
     *
     * @return {@link AntiSpamRuleBuilder} instance to build the rule
     */
    @Nonnull
    static AntiSpamRuleBuilder createAntiSpamRule(@Nonnull String name)
    {
        return new AntiSpamRuleBuilder(name);
    }

    @Nonnull
    Guild getGuild();

    long getOwnerIdLong();

    @Nonnull
    default String getOwnerId()
    {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    @Nonnull
    String getName();

    @Nonnull
    AutoModEventType getEventType();

    @Nonnull
    AutoModTriggerType getTriggerType();

    boolean isEnabled();

    @Nonnull
    List<Role> getExemptRoles();

    @Nonnull
    List<GuildChannel> getExemptChannels();

    @Nonnull
    List<AutoModResponse> getActions();

    @Nonnull
    List<String> getFilteredKeywords();

    @Nonnull
    List<String> getFilteredRegex();

    @Nonnull
    EnumSet<KeywordPreset> getFilteredPresets();

    @Nonnull
    List<String> getAllowlist();

    int getMentionLimit();

    enum KeywordPreset
    {
        PROFANITY(1),
        SEXUAL_CONTENT(2),
        SLURS(3),
        UNKNOWN(-1);

        private final int key;

        KeywordPreset(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        @Nonnull
        public static KeywordPreset fromKey(int key)
        {
            for (KeywordPreset preset : values())
            {
                if (preset.key == key)
                    return preset;
            }
            return UNKNOWN;
        }
    }
}
