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
import net.dv8tion.jda.api.entities.automod.build.AntiSpamRuleBuilder;
import net.dv8tion.jda.api.entities.automod.build.CustomKeywordRuleBuilder;
import net.dv8tion.jda.api.entities.automod.build.MentionSpamRuleBuilder;
import net.dv8tion.jda.api.entities.automod.build.PresetKeywordRuleBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;

public interface AutoModRule extends ISnowflake
{
    int MAX_KEYWORD_LENGTH = 60;
    int MAX_KEYWORD_TOTAL_LENGTH = 1000;
    int MAX_ALLOWLIST_TOTAL_LENGTH = 100;
    int MAX_ALLOWLIST_PRESET_LENGTH = 1000;

    @Nonnull
    static MentionSpamRuleBuilder createMentionSpamRule(@Nonnull String name, int limit)
    {
        return new MentionSpamRuleBuilder(name, limit);
    }

    @Nonnull
    static CustomKeywordRuleBuilder createCustomKeywordRule(@Nonnull String name, @Nonnull String... keywords)
    {
        return new CustomKeywordRuleBuilder(name).addKeywords(keywords);
    }

    @Nonnull
    static PresetKeywordRuleBuilder createPresetKeywordRule(@Nonnull String name, @Nonnull KeywordPreset... presets)
    {
        return new PresetKeywordRuleBuilder(name).enablePresets(presets);
    }

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
