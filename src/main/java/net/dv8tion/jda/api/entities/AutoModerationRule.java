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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Auto Moderation is a feature which allows each guild to set up rules that trigger based on some criteria. For example, a rule can trigger whenever a message contains a specific keyword.
 *  <br> <br>
 * Rules can be configured to automatically execute actions whenever they trigger. For example, if a user tries to send a message which contains a certain keyword, a rule can trigger and block the message before it is sent.
 * <br><br>
 * This is the main object for Auto Moderation.
 */
public interface AutoModerationRule extends ISnowflake {
    /**
     * The Guild in which the role belongs to.
     *
     * @return {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * The name of the rule.
     *
     * @return {@link java.lang.String String}
     */
    @Nonnull
    String getName();

    /**
     * The user who created the rule.
     *
     * @return {@link net.dv8tion.jda.api.entities.User User}
     */
    @Nonnull
    User getUser();

    /**
     * The event type which triggers the rule.
     *
     * @return {@link net.dv8tion.jda.api.entities.EventType EventType}
     */
    @Nonnull
    EventType getEventType();

    /**
     * The trigger of the rule.
     *
     * @return {@link net.dv8tion.jda.api.entities.TriggerType TriggerTypes}
     */
    @Nonnull
    TriggerType getTriggerType();

    /**
     * The metadata of the trigger.
     *
     * @return {@link net.dv8tion.jda.api.entities.TriggerMetadata TriggerMetadata}
     */
    @Nonnull
    TriggerMetadata getTriggerMetadata();

    /**
     * The actions which will be performed when the rule is triggered.
     *
     * @return A {@link java.util.List List} of {@link net.dv8tion.jda.api.entities.AutoModerationAction ActionTypes}
     */
    @Nonnull
    List<AutoModerationAction> getActions();

    /**
     * Weather the rule is enabled or not.
     *
     * @return {@link java.lang.Boolean Boolean}
     */
    boolean isEnabled();

    /**
     * The roles which are exempt from the rules.
     *
     * @return A {@link java.util.List List} of {@link net.dv8tion.jda.api.entities.Role Roles} which are exempt from the rules.
     */
    List<Role> getExemptRoles();

    /**
     * The channels which are exempt from the rules.
     *
     * @return A {@link java.util.List List} of {@link net.dv8tion.jda.api.entities.Channel Channels} which are exempt from the rules.
     */
    List<Channel> getExemptChannels();
}
