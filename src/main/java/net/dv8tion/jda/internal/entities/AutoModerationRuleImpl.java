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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.TriggerMetadata;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class AutoModerationRuleImpl implements AutoModerationRule
{

    private final long id;

    private Guild guild;
    private String name;
    private User user;
    private EventType eventType;
    private TriggerType triggerType;
    private TriggerMetadata triggerMetadata;
    private List<AutoModerationAction> actions;
    private boolean enabled;
    private List<Role> roles;
    private List<GuildChannel> channels;

    public AutoModerationRuleImpl(long id)
    {
        this.id = id;
    }

    @NotNull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    public AutoModerationRuleImpl setGuild(Guild guild)
    {
        this.guild = guild;
        return this;
    }

    @NotNull
    @Override
    public String getName()
    {
        return name;
    }

    public AutoModerationRuleImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    @NotNull
    @Override
    public User getUser()
    {
        return user;
    }

    public AutoModerationRuleImpl setUser(User user)
    {
        this.user = user;
        return this;
    }

    @NotNull
    @Override
    public EventType getEventType()
    {
        return eventType;
    }

    public AutoModerationRuleImpl setEventType(EventType eventType)
    {
        this.eventType = eventType;
        return this;
    }

    @NotNull
    @Override
    public TriggerType getTriggerType()
    {
        return triggerType;
    }

    public AutoModerationRuleImpl setTriggerType(TriggerType triggerType)
    {
        this.triggerType = triggerType;
        return this;
    }

    @NotNull
    @Override
    public TriggerMetadata getTriggerMetadata()
    {
        return triggerMetadata;
    }

    public AutoModerationRuleImpl setTriggerMetadata(TriggerMetadata triggerMetadata)
    {
        this.triggerMetadata = triggerMetadata;
        return this;
    }

    @NotNull
    @Override
    public List<AutoModerationAction> getActions()
    {
        return actions;
    }

    public AutoModerationRuleImpl setActions(List<AutoModerationAction> actions)
    {
        this.actions = actions;
        return this;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    public AutoModerationRuleImpl setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    @Override
    public List<Role> getExemptRoles()
    {
        return roles;
    }

    public AutoModerationRuleImpl setExemptRoles(List<Role> roles)
    {
        this.roles = roles;
        return this;
    }

    @Override
    public List<GuildChannel> getExemptChannels()
    {
        return channels;
    }

    public AutoModerationRuleImpl setExemptChannels(List<GuildChannel> channels)
    {
        this.channels = channels;
        return this;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoModerationRuleImpl that = (AutoModerationRuleImpl) o;
        return id == that.id && enabled == that.enabled && Objects.equals(guild, that.guild) && Objects.equals(name, that.name) && Objects.equals(user, that.user) && eventType == that.eventType && triggerType == that.triggerType && Objects.equals(triggerMetadata, that.triggerMetadata) && Objects.equals(actions, that.actions) && Objects.equals(roles, that.roles) && Objects.equals(channels, that.channels);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, guild, name, user, eventType, triggerType, triggerMetadata, actions, enabled, roles, channels);
    }

    @Override
    public String toString()
    {
        return "AutoModerationRuleImpl(" +
                "id=" + id +
                ", guild=" + guild +
                ", name='" + name + '\'' +
                ", user=" + user +
                ", eventType=" + eventType +
                ", triggerType=" + triggerType +
                ", triggerMetadata=" + triggerMetadata +
                ", actions=" + actions +
                ", enabled=" + enabled +
                ", roles=" + roles +
                ", channels=" + channels +
                ')';
    }

    @Override
    public int compareTo(@NotNull AutoModerationRule o)
    {
        return Long.compare(id, o.getIdLong());
    }
}
