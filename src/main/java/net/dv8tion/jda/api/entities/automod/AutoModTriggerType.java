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

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * The type which defines what triggers an {@link AutoModRule}.
 */
public enum AutoModTriggerType
{
    /**
     * The rule is triggered by user message content containing specific keywords or phrases.
     */
    KEYWORD(1, 6, AutoModEventType.MESSAGE_SEND, AutoModEventType.MEMBER_UPDATE),
    /**
     * The rule is triggered by user message content containing classified spam content.
     */
    SPAM(3, 1, AutoModEventType.MESSAGE_SEND),
    /**
     * The rule is triggered by user message content containing keywords from a predefined list (such as {@link AutoModRule.KeywordPreset#SLURS slurs}).
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
    UNKNOWN(-1, 0),
    ;

    private final int key;
    private final int maxPerGuild;
    private final EnumSet<AutoModEventType> eventTypes;

    AutoModTriggerType(int key, int maxPerGuild, AutoModEventType... supportedEvents)
    {
        this.key = key;
        this.maxPerGuild = maxPerGuild;
        if (supportedEvents.length > 0)
            this.eventTypes = EnumSet.of(supportedEvents[0], supportedEvents);
        else
            this.eventTypes = EnumSet.noneOf(AutoModEventType.class);
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
     * The {@link AutoModEventType AutoModEventTypes} that support this trigger type.
     *
     * @return The supported event types
     */
    @Nonnull
    public EnumSet<AutoModEventType> getSupportedEventTypes()
    {
        return Helpers.copyEnumSet(AutoModEventType.class, eventTypes);
    }

    /**
     * Whether the provided {@link AutoModEventType} is supported by this trigger type.
     *
     * @param  type
     *         The event type to check
     *
     * @return True, if the event type is supported
     */
    public boolean isEventTypeSupported(@Nonnull AutoModEventType type)
    {
        return type != null && eventTypes.contains(type);
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
