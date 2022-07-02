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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.*;
import net.dv8tion.jda.api.entities.automod.build.AutoModerationRuleData;
import net.dv8tion.jda.api.requests.restaction.RuleModifyAction;
import net.dv8tion.jda.internal.entities.automod.build.AutoModerationRuleDataImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class RuleModifyActionImpl extends RestActionImpl<AutoModerationRule> implements RuleModifyAction
{

    private static final String UNDEFINED = "undefined";
    private static final EventType UNDEFINED_EVENT_TYPE = EventType.UNKNOWN;
    private static final TriggerType UNDEFINED_TRIGGER_TYPE = TriggerType.UNKNOWN;
    private static final List<AutoModerationAction> UNDEFINED_ACTIONS = new ArrayList<>();
    private static final boolean UNDEFINED_ENABLED = false;

    private static final int NAME_SET = 1 << 0;
    private static final int EVENT_TYPE_SET = 1 << 1;
    private static final int TRIGGER_TYPE_SET = 1 << 2;
    private static final int ACTIONS_SET = 1 << 3;
    private static final int ENABLED_SET = 1 << 4;

    private int mask = 0;

    private AutoModerationRuleDataImpl data = new AutoModerationRuleDataImpl(UNDEFINED, UNDEFINED_EVENT_TYPE, UNDEFINED_TRIGGER_TYPE, UNDEFINED_ACTIONS, UNDEFINED_ENABLED);

    public RuleModifyActionImpl(@Nonnull Guild guild, @Nonnull String ruleId)
    {
        super(guild.getJDA(), Route.AutoModeration.MODIFY_AUTO_MODERATION_RULE.compile(guild.getId(), ruleId));
    }

    @Nonnull
    @Override
    public RuleModifyAction setCheck(BooleanSupplier checks)
    {
        return (RuleModifyAction) super.addCheck(checks);
    }

    @NotNull
    @Override
    public RuleModifyAction addCheck(@NotNull BooleanSupplier checks)
    {
        return (RuleModifyAction) super.addCheck(checks);
    }

    @Nonnull
    @Override
    public RuleModifyAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (RuleModifyAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public RuleModifyAction deadline(long timestamp)
    {
        return (RuleModifyAction) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public RuleModifyAction apply(@NotNull AutoModerationRuleData ruleData)
    {
        Checks.notNull(ruleData, "ruleData");
        this.mask = NAME_SET | EVENT_TYPE_SET | TRIGGER_TYPE_SET | ACTIONS_SET | ENABLED_SET;
        this.data = (AutoModerationRuleDataImpl) ruleData;
        return this;
    }

    @Nonnull
    @Override
    public RuleModifyAction setName(@Nullable String name)
    {
        if (name == null)
        {
            mask &= ~NAME_SET;
            return this;
        }
        data.setName(name);
        mask |= NAME_SET;
        return this;
    }

    @Nonnull
    @Override
    public RuleModifyAction setEventType(@Nullable EventType eventType)
    {
        if (eventType == null)
        {
            mask &= ~EVENT_TYPE_SET;
            return this;
        }
        data.setEventType(eventType);
        mask |= EVENT_TYPE_SET;
        return this;
    }

    @Nonnull
    @Override
    public RuleModifyAction setTriggerType(@Nullable TriggerType triggerType)
    {
        if (triggerType == null)
        {
            mask &= ~TRIGGER_TYPE_SET;
            return this;
        }
        data.setTriggerType(triggerType);
        mask |= TRIGGER_TYPE_SET;
        return this;
    }

    @Nonnull
    @Override
    public RuleModifyAction setActions(@Nonnull List<AutoModerationAction> actions)
    {
        if (actions.isEmpty())
        {
            mask &= ~ACTIONS_SET;
            return this;
        }
        data.setActions(actions);
        mask |= ACTIONS_SET;
        return this;
    }

    @Nonnull
    @Override
    public RuleModifyAction setEnabled(@Nullable Boolean enabled)
    {
        if (enabled == null)
        {
            mask &= ~ENABLED_SET;
            return this;
        }
        data.setEnabled(enabled);
        mask |= ENABLED_SET;
        return this;
    }

    @Nonnull
    @Override
    public RuleModifyAction setTriggerMetadata(@Nullable TriggerMetadata triggerMetaData)
    {
        if (triggerMetaData == null)
        {
            mask &= ~TRIGGER_TYPE_SET;
            return this;
        }
        data.setTriggerMetadata(triggerMetaData);
        mask |= TRIGGER_TYPE_SET;
        return this;
    }

    @Nonnull
    @Override
    public RuleModifyAction setExemptRoles(@Nonnull List<Role> exemptRoles)
    {
        if (exemptRoles.isEmpty())
        {
            mask &= ~TRIGGER_TYPE_SET;
            return this;
        }
        data.setExemptRoles(exemptRoles);
        mask |= TRIGGER_TYPE_SET;
        return this;
    }

    @Nonnull
    @Override
    public RuleModifyAction setExemptChannels(@Nonnull List<GuildChannel> exemptChannels)
    {
        if (exemptChannels.isEmpty())
        {
            mask &= ~TRIGGER_TYPE_SET;
            return this;
        }
        data.setExemptChannels(exemptChannels);
        mask |= TRIGGER_TYPE_SET;
        return this;
    }
}
