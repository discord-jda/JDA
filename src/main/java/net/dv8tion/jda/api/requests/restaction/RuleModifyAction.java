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
 * Specialized {@link RestAction} used to modify an existing auto moderation rule.
 */
//TODO: JavaDoc for methods
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
    RuleModifyAction apply(@Nonnull AutoModerationRuleData ruleData);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction setName(@Nullable String name);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction setEventType(@Nullable EventType eventType);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction setTriggerType(@Nullable TriggerType triggerType);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction setActions(@Nonnull List<AutoModerationAction> actions);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction setEnabled(@Nullable Boolean enabled);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction setTriggerMetadata(@Nullable TriggerMetadata triggerMetaData);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction setExemptRoles(@Nonnull List<Role> exemptRoles);

    @Nonnull
    @CheckReturnValue
    RuleModifyAction setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);
}
