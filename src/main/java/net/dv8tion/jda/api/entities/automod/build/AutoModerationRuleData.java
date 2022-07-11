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

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.TriggerMetadata;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Used to construct a new {@link net.dv8tion.jda.api.entities.automod.AutoModerationRule}.
 *
 * <p>
 *     The following methods are required to be called in order to create a new {@link net.dv8tion.jda.api.entities.automod.AutoModerationRule}.
 *     If any of these methods are not called, an {@link IllegalStateException} will be thrown.
 *     <ul>
 *         <li>{@link #setName(String)}</li>
 *         <li>{@link #setEventType(EventType)}</li>
 *         <li>{@link #setTriggerType(TriggerType)}</li>
 *         <li>{@link #setEnabled(boolean)}</li>
 *         <li>{@link #setActions(List)}</li>
 *     </ul>
 * </p>
 */
public interface AutoModerationRuleData extends SerializableData
{

    /**
     * Used to set the name of the new rule.
     *
     * @param  name
     *         The name of the rule. Must be between 1 and 100 characters.
     *
     * @return The {@link AutoModerationRuleData}.
     */
    @Nonnull
    AutoModerationRuleData setName(@Nonnull String name);

    /**
     * The event that will cause the auto moderation system to check for the specified trigger.
     *
     * @param  eventType
     *         The event type.
     *
     * @return The {@link AutoModerationRuleData}.
     */
    @Nonnull
    AutoModerationRuleData setEventType(@Nonnull EventType eventType);

    /**
     * The trigger that will cause the auto moderation system to be executed.
     *
     * @param  triggerType
     *         The trigger type.
     *
     * @return The {@link AutoModerationRuleData}.
     */
    @Nonnull
    AutoModerationRuleData setTriggerType(@Nonnull TriggerType triggerType);

    /**
     * Whether the rule is enabled or not.
     *
     * @param  enabled
     *         Whether the rule is enabled or not.
     *
     * @return The {@link AutoModerationRuleData}.
     */
    @Nonnull
    AutoModerationRuleData setEnabled(boolean enabled);

    /**
     * The actions that will be executed when the trigger is met.
     *
     * @param  actions
     *         The actions that will be carried out.
     *
     * @return The {@link AutoModerationRuleData}.
     */
    @Nonnull
    AutoModerationRuleData setActions(@Nonnull List<AutoModerationAction> actions);

    /**
     * The roles that will not be affected by the rule.
     *
     * @param  exemptRoles
     *         The roles that will not be affected.
     *
     * @return The {@link AutoModerationRuleData}.
     */
    @Nullable
    AutoModerationRuleData setExemptRoles(@Nonnull List<Role> exemptRoles);

    /**
     * The channel that will not be affected by the rule.
     * @param  exemptChannels
     *         The channels that will not be affected.
     *
     * @return The {@link AutoModerationRuleData}.
     */
    @Nullable
    AutoModerationRuleData setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);

    /**
     * Additional data that can he used to determine whether a rule should be executed or not.
     *
     * @param  triggerMetaData
     *         Additional data.
     *
     * @return The {@link AutoModerationRuleData}.
     */
    @Nullable
    AutoModerationRuleData setTriggerMetadata(@Nonnull TriggerMetadata triggerMetaData);

    /**
     * Used to retrieve the name of the rule.
     *
     * @return The {@link String name} of the rule
     */
    @Nonnull
    String getName();

    /**
     * Used to retrieve the event type of the rule.
     *
     * @return The {@link EventType} of the rule.
     */
    @Nonnull
    EventType getEventType();

    /**
     * Used to retrieve the trigger type of the rule.
     *
     * @return The {@link TriggerType} of the rule.
     */
    @Nonnull
    TriggerType getTriggerType();

    /**
     * Used to retrieve whether the rule is enabled or not.
     *
     * @return True, if the rule is enabled. False, otherwise.
     */
    boolean isEnabled();

    /**
     * Used to retrieve any additional data that can be used to determine whether a rule should be executed or not.
     *
     * @return The {@link TriggerMetadata}
     */
    @Nullable
    TriggerMetadata getTriggerMetadata();

    /**
     * Used to retrieve the actions that will be executed when the trigger is met.
     *
     * @return The {@link List} of {@link AutoModerationAction}s.
     */
    @Nonnull
    List<AutoModerationAction> getActions();

    /**
     * Used to retrieve the roles that will not be affected by the rule.
     *
     * @return The {@link List} of {@link Role}s.
     */
    @Nullable
    List<Role> getExemptRoles();

    /**
     * Used to retrieve the channels that will not be affected by the rule.
     *
     * @return The {@link List} of {@link GuildChannel}s.
     */
    @Nullable
    List<GuildChannel> getExemptChannels();
}
