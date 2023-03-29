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
import net.dv8tion.jda.api.entities.automod.build.AutoModRuleData;
import net.dv8tion.jda.api.entities.automod.build.TriggerConfig;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.AutoModRuleManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import javax.annotation.Nonnull;
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
     * The maximum length of a keyword in {@link TriggerConfig#keywordFilter(String...)}. ({@value})
     */
    int MAX_KEYWORD_LENGTH = 60;
    /**
     * The maximum amount of keywords in {@link TriggerConfig#keywordFilter(String...)}. ({@value})
     */
    int MAX_KEYWORD_AMOUNT = 1000;
    /**
     * The maximum amount of whitelisted keywords in {@link TriggerConfig#keywordFilter(String...)}. ({@value})
     */
    int MAX_ALLOWLIST_CUSTOM_AMOUNT = 100;
    /**
     * The maximum amount of whitelisted keywords in {@link TriggerConfig#presetKeywordFilter(KeywordPreset...)}. ({@value})
     */
    int MAX_ALLOWLIST_PRESET_AMOUNT = 1000;
    /**
     * The maximum length of a regex pattern in {@link TriggerConfig#patternFilter(String...)}. ({@value})
     */
    int MAX_PATTERN_LENGTH = 260;
    /**
     * The maximum amount of regex patterns in {@link TriggerConfig#patternFilter(String...)}. ({@value})
     */
    int MAX_PATTERN_AMOUNT = 10;
    /**
     * The maximum limit of mentions in {@link TriggerConfig#mentionSpam(int)}. ({@value})
     */
    int MAX_MENTION_LIMIT = 50;

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

    @Nonnull
    default AutoModRuleManager getManager()
    {
        return getGuild().modifyAutoModRuleById(getId());
    }

    @Nonnull
    default AuditableRestAction<Void> delete()
    {
        return getGuild().deleteAutoModRuleById(getId());
    }

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
