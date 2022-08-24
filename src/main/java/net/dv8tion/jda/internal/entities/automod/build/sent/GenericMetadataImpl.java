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

package net.dv8tion.jda.internal.entities.automod.build.sent;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.*;
import net.dv8tion.jda.api.entities.automod.build.sent.GenericMetadata;
import net.dv8tion.jda.internal.entities.automod.ActionMetadataImpl;
import net.dv8tion.jda.internal.entities.automod.AutoModerationActionImpl;
import net.dv8tion.jda.internal.entities.automod.AutoModerationRuleImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GenericMetadataImpl implements GenericMetadata
{
    protected final String name;
    protected final EventType eventType;
    protected final TriggerType triggerType;
    protected boolean enabled = true;
    protected List<AutoModerationAction> actions = new ArrayList<>();
    protected List<Role> roles;
    protected List<GuildChannel> channels;
    protected TriggerMetadata triggerMetadata;


    public GenericMetadataImpl(String name, EventType eventType, @Nonnull TriggerType triggerType)
    {
        this.name = name;
        this.eventType = eventType;
        this.triggerType = triggerType;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GenericMetadata setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GenericMetadata setAction(@Nonnull AutoModerationActionType type, @Nullable GuildChannel channel, @Nullable Duration duration)
    {
        List<AutoModerationAction> actions = new ArrayList<>();

        AutoModerationAction action = new AutoModerationActionImpl();

        action.setType(type);

        ActionMetadata metadata = new ActionMetadataImpl();

        if (channel != null)
            metadata.setChannel(channel);

        if (duration != null)
            metadata.setDuration(duration);

        action.setActionMetadata(metadata);

        actions.add(action);

        this.actions = actions;
        return this;
    }

    @Nullable
    @Override
    @CheckReturnValue
    public GenericMetadata setExemptRoles(@Nonnull List<Role> exemptRoles)
    {
        this.roles = exemptRoles;
        return this;
    }

    @Nullable
    @Override
    @CheckReturnValue
    public GenericMetadata setExemptChannels(@Nonnull List<GuildChannel> exemptChannels)
    {
        this.channels = exemptChannels;
        return this;
    }

    @Override
    public AutoModerationRule build()
    {
        AutoModerationRule rule = new AutoModerationRuleImpl(name, eventType, triggerType);
        rule.setEnabled(enabled);

        if (!actions.isEmpty())
            rule.setActions(actions);

        if (roles != null)
            rule.setExemptRoles(roles);

        if (channels != null)
            rule.setExemptChannels(channels);

        if (triggerMetadata != null)
            rule.setTriggerMetadata(triggerMetadata);

        return rule;
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

    @Override
    @Nonnull
    public List<AutoModerationAction> getActions()
    {
        return actions;
    }

    @Override
    @Nullable
    public TriggerMetadata getTriggerMetadata()
    {
        return triggerMetadata;
    }

    @Override
    @Nullable
    public List<Role> getExemptRoles()
    {
        return roles;
    }

    @Override
    @Nullable
    public List<GuildChannel> getExemptChannels()
    {
        return channels;
    }
}
