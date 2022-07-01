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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.*;
import net.dv8tion.jda.api.requests.restaction.RuleCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.automod.build.AutoModerationRuleDataImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RuleCreateActionImpl extends RestActionImpl<AutoModerationRule> implements RuleCreateAction
{
    private final AutoModerationRuleDataImpl data;

    public RuleCreateActionImpl(JDA api, AutoModerationRuleDataImpl data, String guildId)
    {
        super(api, Route.AutoModeration.CREATE_AUTO_MODERATION_RULE.compile(guildId));
        this.data = data;
    }


    @NotNull
    @Override
    public RuleCreateAction setName(@NotNull String name)
    {
        Checks.inRange(name, 1, 100, "Name");
        data.setName(name);
        return this;
    }

    @NotNull
    @Override
    public RuleCreateAction setEventType(@NotNull EventType eventType)
    {
        data.setEventType(eventType);
        return this;
    }

    @NotNull
    @Override
    public RuleCreateAction setTriggerType(@NotNull TriggerType triggerType)
    {
        data.setTriggerType(triggerType);
        return this;
    }

    @NotNull
    @Override
    public RuleCreateAction setActions(@NotNull List<AutoModerationAction> actions)
    {
        data.setActions(actions);
        return this;
    }

    @NotNull
    @Override
    public RuleCreateAction setEnabled(boolean enabled)
    {
        data.setEnabled(enabled);
        return this;
    }

    @Nullable
    @Override
    public RuleCreateAction setTriggerMetadata(@NotNull TriggerMetadata triggerMetaData)
    {
        data.setTriggerMetadata(triggerMetaData);
        return this;
    }

    @Nullable
    @Override
    public RuleCreateAction setExemptRoles(@NotNull List<Role> exemptRoles)
    {
        data.setExemptRoles(exemptRoles);
        return this;
    }

    @Nullable
    @Override
    public RuleCreateAction setExemptChannels(@NotNull List<GuildChannel> exemptChannels)
    {
        data.setExemptChannels(exemptChannels);
        return this;
    }

    @NotNull
    @Override
    public String getName()
    {
        return data.getName();
    }

    @NotNull
    @Override
    public EventType getEventType()
    {
        return data.getEventType();
    }

    @NotNull
    @Override
    public TriggerType getTriggerType()
    {
        return data.getTriggerType();
    }

    @Override
    public boolean isEnabled()
    {
        return data.isEnabled();
    }

    @Nullable
    @Override
    public TriggerMetadata getTriggerMetadata()
    {
        return data.getTriggerMetadata();
    }

    @NotNull
    @Override
    public DataObject toData()
    {
        return data.toData();
    }
}
