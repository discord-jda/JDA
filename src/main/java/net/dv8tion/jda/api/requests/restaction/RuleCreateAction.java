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
 */
//TODO: JavaDoc for methods
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

    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setName(@Nonnull String name);

    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setEventType(@Nonnull EventType eventType);

    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setTriggerType(@Nonnull TriggerType triggerType);

    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setActions(@Nonnull List<AutoModerationAction> actions);

    @Nonnull
    @Override
    @CheckReturnValue
    RuleCreateAction setEnabled(boolean enabled);

    @Nullable
    @Override
    @CheckReturnValue
    RuleCreateAction setTriggerMetadata(@Nonnull TriggerMetadata triggerMetaData);

    @Nullable
    @Override
    @CheckReturnValue
    RuleCreateAction setExemptRoles(@Nonnull List<Role> exemptRoles);

    @Nullable
    @Override
    @CheckReturnValue
    RuleCreateAction setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);
}
