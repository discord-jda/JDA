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

package net.dv8tion.jda.internal.entities.automod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.automod.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AutoModerationRuleImpl implements AutoModerationRule
{

    private final Long id;

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

    public AutoModerationRuleImpl(@Nonnull String name, @Nonnull EventType eventType, @Nonnull TriggerType triggerType)
    {
        id = null;
        setName(name);
        setEventType(eventType);
        setTriggerType(triggerType);
    }

    @Nonnull
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

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Nonnull
    @Override
    public AutoModerationRuleImpl setName(@Nonnull String name)
    {
        Checks.inRange(name, 1, 100, "Name");
        this.name = name;
        return this;
    }

    @Nonnull
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

    @Nonnull
    @Override
    public EventType getEventType()
    {
        return eventType;
    }

    @Nonnull
    @Override
    public AutoModerationRuleImpl setEventType(@Nonnull EventType eventType)
    {
        Checks.notNull(eventType, "Event Type");
        this.eventType = eventType;
        return this;
    }

    @Nonnull
    @Override
    public TriggerType getTriggerType()
    {
        return triggerType;
    }

    @Nonnull
    @Override
    public AutoModerationRuleImpl setTriggerType(@Nonnull TriggerType triggerType)
    {
        Checks.notNull(triggerType, "Trigger Type");
        this.triggerType = triggerType;
        return this;
    }

    @Nullable
    @Override
    public TriggerMetadata getTriggerMetadata()
    {
        return triggerMetadata;
    }

    @Nullable
    @Override
    public AutoModerationRuleImpl setTriggerMetadata(@Nonnull TriggerMetadata triggerMetaData)
    {
        this.triggerMetadata = triggerMetaData;
        return this;
    }

    @Nonnull
    @Override
    public List<AutoModerationAction> getActions()
    {
        return Collections.unmodifiableList(actions);
    }

    @Nonnull
    @Override
    public AutoModerationRuleImpl setActions(@Nonnull List<AutoModerationAction> actions)
    {
        Checks.notEmpty(actions, "Actions");
        Checks.notEmpty(actions, "Auto Moderation Action");
        this.actions.addAll(actions);
        return this;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Nonnull
    @Override
    public AutoModerationRuleImpl setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    @Override
    public @Nullable List<Role> getExemptRoles()
    {
        return Collections.unmodifiableList(roles);
    }

    @Nullable
    @Override
    public AutoModerationRuleImpl setExemptRoles(@Nonnull List<Role> exemptRoles)
    {
        Checks.notEmpty(exemptRoles, "Exempt Roles");
        this.roles.addAll(exemptRoles);
        return this;
    }

    @Override
    public @Nullable List<GuildChannel> getExemptChannels()
    {
        return Collections.unmodifiableList(channels);
    }

    @Nullable
    @Override
    public AutoModerationRuleImpl setExemptChannels(@Nonnull List<GuildChannel> exemptChannels)
    {
        Checks.notEmpty(exemptChannels, "Exempt Channels");
        this.channels.addAll(exemptChannels);
        return this;
    }

    @Override
    public long getIdLong()
    {
        return id;
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
                .put("exempt_roles", DataArray.fromCollection(roles))
                .put("exempt_channels", DataArray.fromCollection(channels));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoModerationRuleImpl that = (AutoModerationRuleImpl) o;
        return Objects.equals(id, that.id) && enabled == that.enabled && Objects.equals(guild, that.guild) && Objects.equals(name, that.name) && Objects.equals(user, that.user) && eventType == that.eventType && triggerType == that.triggerType && Objects.equals(triggerMetadata, that.triggerMetadata) && Objects.equals(actions, that.actions) && Objects.equals(roles, that.roles) && Objects.equals(channels, that.channels);
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
    public int compareTo(@Nonnull AutoModerationRule o)
    {
        return Long.compare(id, o.getIdLong());
    }
}
