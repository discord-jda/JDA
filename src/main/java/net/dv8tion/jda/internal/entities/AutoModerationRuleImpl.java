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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class AutoModerationRuleImpl implements AutoModerationRule {

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
    private List<Channel> channels;

    public AutoModerationRuleImpl(long id) {
        this.id = id;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public User getUser() {
        return user;
    }

    @NotNull
    @Override
    public EventType getEventType() {
        return eventType;
    }

    @NotNull
    @Override
    public TriggerType getTriggerType() {
        return triggerType;
    }

    @NotNull
    @Override
    public TriggerMetadata getTriggerMetadata() {
        return triggerMetadata;
    }

    @NotNull
    @Override
    public List<AutoModerationAction> getActions() {
        return actions;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<Role> getExemptRoles() {
        return roles;
    }

    @Override
    public List<Channel> getExemptChannels() {
        return channels;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    public AutoModerationRuleImpl setGuild(Guild guild) {
        this.guild = guild;
        return this;
    }

    public AutoModerationRuleImpl setName(String name) {
        this.name = name;
        return this;
    }

    public AutoModerationRuleImpl setUser(User user) {
        this.user = user;
        return this;
    }

    public AutoModerationRuleImpl setEventType(EventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public AutoModerationRuleImpl setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
        return this;
    }

    public AutoModerationRuleImpl setTriggerMetadata(TriggerMetadata triggerMetadata) {
        this.triggerMetadata = triggerMetadata;
        return this;
    }

    public AutoModerationRuleImpl setActions(List<AutoModerationAction> actions) {
        this.actions = actions;
        return this;
    }

    public AutoModerationRuleImpl setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public AutoModerationRuleImpl setExampleRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

    public AutoModerationRuleImpl setExampleChannels(List<Channel> channels) {
        this.channels = channels;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoModerationRuleImpl that = (AutoModerationRuleImpl) o;
        return id == that.id && enabled == that.enabled && Objects.equals(guild, that.guild) && Objects.equals(name, that.name) && Objects.equals(user, that.user) && eventType == that.eventType && triggerType == that.triggerType && Objects.equals(triggerMetadata, that.triggerMetadata) && Objects.equals(actions, that.actions) && Objects.equals(roles, that.roles) && Objects.equals(channels, that.channels);
    }

    @Override
    public int hashCode() {
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
}
