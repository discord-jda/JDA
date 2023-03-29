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

import javax.annotation.Nonnull;

/**
 * The type which defines what triggers an {@link AutoModRule}.
 */
public enum AutoModTriggerType
{
    /**
     * The rule is triggered by user content containing specific keywords or phrases.
     */
    KEYWORD(1, 6),
    /**
     * The rule is triggered by user content containing classified spam content.
     */
    SPAM(3, 1),
    /**
     * The rule is triggered by user content containing keywords from a predefined list (such as {@link AutoModRule.KeywordPreset#SLURS slurs}).
     */
    KEYWORD_PRESET(4, 1),
    /**
     * The rule is triggered by user content containing more than the allowed number of mentions.
     */
    MENTION_SPAM(5, 1),
    /**
     * Placeholder for unknown trigger types that haven't been added yet.
     */
    UNKNOWN(-1, 1),
    ;

    private final int key;
    private final int maxPerGuild;

    AutoModTriggerType(int key, int maxPerGuild)
    {
        this.key = key;
        this.maxPerGuild = maxPerGuild;
    }

    /**
     * The raw API key used to indicate this type.
     *
     * @return The int key
     */
    public int getKey()
    {
        return key;
    }

    /**
     * The maximum number of rules that can use this trigger type in a guild.
     *
     * @return The maximum number of rules
     */
    public int getMaxPerGuild()
    {
        return maxPerGuild;
    }

    /**
     * The {@link AutoModTriggerType} that matches the provided key.
     *
     * @param  key
     *         The key to match
     *
     * @return The matching {@link AutoModTriggerType} or {@link #UNKNOWN}
     */
    @Nonnull
    public static AutoModTriggerType fromKey(int key)
    {
        for (AutoModTriggerType trigger : values())
        {
            if (trigger.key == key)
                return trigger;
        }
        return UNKNOWN;
    }
}
