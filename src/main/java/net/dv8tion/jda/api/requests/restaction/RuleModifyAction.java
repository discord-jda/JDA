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
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Specialized {@link RestAction} used to modify an existing auto moderation rule.
 *
 * @see net.dv8tion.jda.api.entities.Guild#modifyAutoModerationRule(AutoModerationRule, AutoModerationRule)
 */
public interface RuleModifyAction extends RestAction<AutoModerationRule>
{
    @Nonnull
    @Override
    RuleModifyAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    RuleModifyAction addCheck(@Nonnull BooleanSupplier checks);

    @Nonnull
    @Override
    RuleModifyAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    RuleModifyAction deadline(long timestamp);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction apply(@Nonnull AutoModerationRule ruleData);

    /**
     * Used to modify the name of the rule.
     *
     * @param  name
     *         The new name of the rule. Must be between 1 and 100 characters.
     *
     * @return The {@link RuleModifyAction}
     */
    @Nonnull
    @CheckReturnValue
    RuleModifyAction setName(@Nullable String name);

    /**
     * Used to modify the event that will cause the auto moderation system to check for the specified trigger.
     *
     * @param  eventType
     *         The event type.
     *
     * @return The {@link RuleModifyAction}.
     */
    @Nonnull
    @CheckReturnValue
    RuleModifyAction setEventType(@Nullable EventType eventType);

    /**
     * Used to modify the trigger that will cause the auto moderation system to be executed.
     *
     * @param  triggerType
     *         The trigger type.
     *
     * @return The {@link RuleModifyAction}.
     */
    @Nonnull
    @CheckReturnValue
    RuleModifyAction setTriggerType(@Nullable TriggerType triggerType);

    /**
     * Used to modify the actions that will be executed when the trigger is met.
     *
     * @param  actions
     *         The actions that will be carried out.
     *
     * @return The {@link RuleModifyAction}.
     */
    @Nonnull
    @CheckReturnValue
    RuleModifyAction setActions(@Nonnull List<AutoModerationAction> actions);

    /**
     * Used to enable or disable the rule
     *
     * @param  enabled
     *         Whether the rule is enabled or not.
     *
     * @return The {@link RuleModifyAction}.
     */
    @Nonnull
    @CheckReturnValue
    RuleModifyAction setEnabled(@Nullable Boolean enabled);

    /**
     * Used to modify additional data that can he used to determine whether a rule should be executed or not.
     *
     * @param  triggerMetaData
     *         Additional data.
     *
     * @return The {@link RuleModifyAction}.
     */
    @Nonnull
    @CheckReturnValue
    RuleModifyAction setTriggerMetadata(@Nullable TriggerMetadata triggerMetaData);

    /**
     * Used to modify the roles that will not be affected by the rule.
     *
     * @param  exemptRoles
     *         The roles that will not be affected.
     *
     * @return The {@link RuleModifyAction}.
     */
    @Nonnull
    @CheckReturnValue
    RuleModifyAction setExemptRoles(@Nonnull List<Role> exemptRoles);


    /**
     * Used to modify the channel that will not be affected by the rule.
     *
     * @param  exemptChannels
     *         The channels that will not be affected.
     *
     * @return The {@link RuleModifyAction}.
     */
    @Nonnull
    @CheckReturnValue
    RuleModifyAction setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);
}
