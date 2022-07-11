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

package net.dv8tion.jda.internal.entities.automod.build;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.TriggerMetadata;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import net.dv8tion.jda.api.entities.automod.build.AutoModerationRuleData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AutoModerationRuleDataImpl implements AutoModerationRuleData
{
    private final List<AutoModerationAction> actions = new ArrayList<>();
    private final List<Role> exemptRoles = new ArrayList<>();
    private final List<GuildChannel> exemptChannels = new ArrayList<>();
    private String name;
    private EventType eventType;
    private TriggerType triggerType;
    private boolean enabled;
    private TriggerMetadata triggerMetadata;

    public AutoModerationRuleDataImpl(@Nonnull String name, @Nonnull EventType eventType, @Nonnull TriggerType triggerType, @Nonnull List<AutoModerationAction> actions, boolean enabled)
    {
        setName(name);
        setEventType(eventType);
        setTriggerType(triggerType);
        setActions(actions);
        setEnabled(enabled);
    }

    @Nonnull
    @Override
    public AutoModerationRuleData setName(@Nonnull String name)
    {
        Checks.inRange(name, 1, 100, "Name");
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    public AutoModerationRuleData setEventType(@Nonnull EventType eventType)
    {
        Checks.notNull(eventType, "Event Type");
        this.eventType = eventType;
        return this;
    }

    @Nonnull
    @Override
    public AutoModerationRuleData setTriggerType(@Nonnull TriggerType triggerType)
    {
        Checks.notNull(triggerType, "Trigger Type");
        this.triggerType = triggerType;
        return this;
    }

    @Nonnull
    @Override
    public AutoModerationRuleData setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    @Nonnull
    @Override
    public AutoModerationRuleData setActions(@Nonnull List<AutoModerationAction> actions)
    {
        Checks.notEmpty(actions, "Actions");
        Checks.notEmpty(actions, "Auto Moderation Action");
        this.actions.addAll(actions);
        return this;
    }


    @Nullable
    @Override
    public AutoModerationRuleData setTriggerMetadata(@Nonnull TriggerMetadata triggerMetaData)
    {
        this.triggerMetadata = triggerMetaData;
        return this;
    }

    @Nullable
    @Override
    public AutoModerationRuleData setExemptRoles(@Nonnull List<Role> exemptRoles)
    {
        Checks.notEmpty(exemptRoles, "Exempt Roles");
        this.exemptRoles.addAll(exemptRoles);
        return this;
    }

    @Nullable
    @Override
    public AutoModerationRuleData setExemptChannels(@Nonnull List<GuildChannel> exemptChannels)
    {
        Checks.notEmpty(exemptChannels, "Exempt Channels");
        this.exemptChannels.addAll(exemptChannels);
        return this;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("name", name)
                .put("event_type", eventType.name())
                .put("trigger_type", triggerType.name())
                .put("actions", DataArray.fromCollection(actions))
                .put("enabled", enabled)
                .put("trigger_metadata", triggerMetadata == null ? null : triggerMetadata)
                .put("exempt_roles", DataArray.fromCollection(exemptRoles))
                .put("exempt_channels", DataArray.fromCollection(exemptChannels));
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public EventType getEventType()
    {
        return eventType;
    }

    @Nonnull
    @Override
    public TriggerType getTriggerType()
    {
        return triggerType;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Nullable
    @Override
    public TriggerMetadata getTriggerMetadata()
    {
        return triggerMetadata;
    }

    @Nonnull
    @Override
    public List<AutoModerationAction> getActions()
    {
        return actions;
    }

    @Nullable
    @Override
    public List<Role> getExemptRoles()
    {
        return exemptRoles;
    }

    @Nullable
    @Override
    public List<GuildChannel> getExemptChannels()
    {
        return exemptChannels;
    }
}
