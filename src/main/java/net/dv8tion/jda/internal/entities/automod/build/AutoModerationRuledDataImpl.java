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
import net.dv8tion.jda.api.entities.automod.build.AutoModerationRuledData;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class AutoModerationRuledDataImpl implements AutoModerationRuledData
{
    private final DataArray actions = DataArray.empty();
    private final DataArray exemptRoles = DataArray.empty();
    private final DataArray exemptChannels = DataArray.empty();
    private String name;
    private EventType eventType;
    private TriggerType triggerType;
    private boolean enabled;
    private TriggerMetadata triggerMetadata;


    public AutoModerationRuledDataImpl(@Nonnull String name, @Nonnull EventType eventType, @Nonnull TriggerType triggerType, @Nonnull List<AutoModerationAction> actions, boolean enabled)
    {
        setName(name);
        setEventType(eventType);
        setTriggerType(triggerType);
        setActions(actions);
        setEnabled(enabled);
    }

    @NotNull
    @Override
    public AutoModerationRuledData setName(@NotNull String name)
    {
        Checks.inRange(name, 1, 100, "Name");
        this.name = name;
        return this;
    }

    @NotNull
    @Override
    public AutoModerationRuledData setEventType(@NotNull EventType eventType)
    {
        this.eventType = eventType;
        return this;
    }

    @NotNull
    @Override
    public AutoModerationRuledData setTriggerType(@NotNull TriggerType triggerType)
    {
        this.triggerType = triggerType;
        return this;
    }

    @NotNull
    @Override
    public AutoModerationRuledData setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    @Nullable
    @Override
    public AutoModerationRuledData setTriggerMetadata(@NotNull TriggerMetadata triggerMetaData)
    {
        this.triggerMetadata = triggerMetaData;
        return this;
    }

    @Nullable
    @Override
    public AutoModerationRuledData setExemptRoles(@NotNull List<Role> exemptRoles)
    {
        Checks.notEmpty(exemptRoles, "Exempt Roles");
        this.exemptRoles.addAll(exemptRoles);
        return this;
    }

    @Nullable
    @Override
    public AutoModerationRuledData setExemptChannels(@NotNull List<GuildChannel> exemptChannels)
    {
        Checks.notEmpty(exemptChannels, "Exempt Channels");
        this.exemptChannels.addAll(exemptChannels);
        return this;
    }

    @NotNull
    @Override
    public AutoModerationRuledData setActions(@NotNull List<AutoModerationAction> actions)
    {
        Checks.notEmpty(actions, "Auto Moderation Action");
        this.actions.addAll(actions);
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
                .put("actions", actions)
                .put("enabled", enabled)
                .put("trigger_metadata", triggerMetadata == null ? null : triggerMetadata)
                .put("exempt_roles", exemptRoles)
                .put("exempt_channels", exemptChannels);
    }

    @NotNull
    @Override
    public String getName()
    {
        return name;
    }

    @NotNull
    @Override
    public EventType getEventType()
    {
        return eventType;
    }

    @NotNull
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
}
