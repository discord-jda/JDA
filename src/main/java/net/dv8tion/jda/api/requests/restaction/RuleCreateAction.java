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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.*;
import net.dv8tion.jda.api.entities.automod.build.AutoModerationRuleData;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Specialized {@link RestAction} used to create or update an auto moderation rule.
 * 
 * @see net.dv8tion.jda.api.entities.Guild#createAutoModerationRule(AutoModerationRuleData) 
 */
public interface RuleCreateAction extends RestAction<AutoModerationRule>, AutoModerationRuleData
{
    @Nonnull
    @Override
    RuleCreateAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    RuleCreateAction addCheck(@Nonnull BooleanSupplier checks);

    @Nonnull
    @Override
    RuleCreateAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    RuleCreateAction deadline(long timestamp);

    /**
     * Used to set the name of the new rule.
     *
     * @param  name
     *         The name of the rule. Must be between 1 and 100 characters.
     *
     * @return The {@link RuleCreateAction}.
     */
    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setName(@Nonnull String name);

    /**
     * Used to set the event that will cause the auto moderation system to check for the specified trigger.
     *
     * @param  eventType
     *         The event type.
     *
     * @return The {@link RuleCreateAction}.
     */
    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setEventType(@Nonnull EventType eventType);

    /**
     * Used to set the trigger that will cause the auto moderation system to be executed.
     *
     * @param  triggerType
     *         The trigger type.
     *
     * @return The {@link RuleCreateAction}.
     */
    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setTriggerType(@Nonnull TriggerType triggerType);

    /**
     * Used to set the actions that will be executed when the trigger is met.
     *
     * @param  actions
     *         The actions that will be carried out.
     *
     * @return The {@link RuleCreateAction}.
     */
    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setActions(@Nonnull List<AutoModerationAction> actions);

    /**
     * Used to enable or disable the rule.
     *
     * @param  enabled
     *         Whether the rule is enabled or not.
     *
     * @return The {@link RuleCreateAction}.
     */
    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setEnabled(boolean enabled);

    /**
     * Used to set additional data that can he used to determine whether a rule should be executed or not.
     *
     * @param  triggerMetaData
     *         Additional data.
     *
     * @return The {@link RuleCreateAction}.
     */
    @Nullable
    @Override
    @CheckReturnValue
    RuleCreateAction setTriggerMetadata(@Nonnull TriggerMetadata triggerMetaData);

    /**
     * Used to set the roles that will not be affected by the rule.
     *
     * @param  exemptRoles
     *         The roles that will not be affected.
     *
     * @return The {@link RuleCreateAction}.
     */
    @Nullable
    @Override
    @CheckReturnValue
    RuleCreateAction setExemptRoles(@Nonnull List<Role> exemptRoles);

    /**
     * Used to set the channel that will not be affected by the rule.
     *
     * @param  exemptChannels
     *         The channels that will not be affected.
     *
     * @return The {@link RuleCreateAction}.
     */
    @Nullable
    @Override
    @CheckReturnValue
    RuleCreateAction setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);
}
