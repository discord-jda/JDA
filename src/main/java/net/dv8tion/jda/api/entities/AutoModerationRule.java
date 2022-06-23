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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.TriggerMetadata;
import net.dv8tion.jda.api.entities.automod.TriggerType;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents a Discord auto moderation rule.
 * <br>
 * Auto Moderation is a feature which allows each guild to set up rules that trigger based on some criteria.
 * For example, a rule can trigger whenever a message contains a specific keyword.
 */
public interface AutoModerationRule extends ISnowflake, Comparable<AutoModerationRule>
{
    /**
     * Returns the Guild that the rule belongs to.
     *
     * @return {@link Guild Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * Returns the name of the rule.
     *
     * @return {@link String String}
     */
    @Nonnull
    String getName();

    /**
     * Returns the user who created the rule.
     *
     * @return {@link User User}
     */
    @Nonnull
    User getUser();

    /**
     * Returns the type of event that can potentially trigger this rule.
     *
     * @return {@link EventType EventType}
     */
    @Nonnull
    EventType getEventType();

    /**
     * Returns the type of trigger that can cause this rule to be executed.
     *
     * @return {@link TriggerType TriggerTypes}
     */
    @Nonnull
    TriggerType getTriggerType();

    /**
     * Returns additional metadata for the trigger of this rule, used whenever this rule is executed.
     *
     * @return {@link TriggerMetadata TriggerMetadata}
     */
    @Nonnull
    TriggerMetadata getTriggerMetadata();

    /**
     * Returns the actions that will be performed when this rule is executed.
     *
     * @return A {@link List List} of {@link AutoModerationAction Actions}
     */
    @Nonnull
    List<AutoModerationAction> getActions();

    /**
     * Returns whether this rule is enabled or not.
     *
     * @return True, if this is enabled
     */
    boolean isEnabled();

    /**
     * Returns the roles that are exempt from this rule.
     *
     * @return A {@link List List} of {@link Role Roles} which are exempt from the rules.
     */
    List<Role> getExemptRoles();

    /**
     * Returns the channels that are exempt from this rule.
     *
     * @return A {@link List List} of {@link GuildChannel Channels} which are exempt from the rules.
     */
    List<GuildChannel> getExemptChannels();
}
