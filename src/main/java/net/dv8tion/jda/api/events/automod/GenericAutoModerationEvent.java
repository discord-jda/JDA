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

package net.dv8tion.jda.api.events.automod;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.TriggerMetadata;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;
import java.util.List;

public class GenericAutoModerationEvent extends Event
{
    protected final AutoModerationRule rule;

    public GenericAutoModerationEvent(@Nonnull JDA api, long responseNumber, AutoModerationRule rule)
    {
        super(api, responseNumber);
        this.rule = rule;
    }

    public Guild getGuild()
    {
        return rule.getGuild();
    }

    @Nonnull
    public String getName()
    {
        return rule.getName();
    }

    @Nonnull
    public User getUser()
    {
        return rule.getUser();
    }

    @Nonnull
    public EventType getEventType()
    {
        return rule.getEventType();
    }

    @Nonnull
    public TriggerType getTriggerType()
    {
        return rule.getTriggerType();
    }

    @Nonnull
    public TriggerMetadata getTriggerMetadata()
    {
        return rule.getTriggerMetadata();
    }

    @Nonnull
    public List<AutoModerationAction> getActions()
    {
        return rule.getActions();
    }

    public boolean isEnabled()
    {
        return rule.isEnabled();
    }

    public List<Role> getExemptRoles()
    {
        return rule.getExemptRoles();
    }

    public List<Channel> getExemptChannels()
    {
        return rule.getExemptChannels();
    }

    @Nonnull
    public AutoModerationRule getRule()
    {
        return rule;
    }
}
