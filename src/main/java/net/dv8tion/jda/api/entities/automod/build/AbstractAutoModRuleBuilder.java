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

package net.dv8tion.jda.api.entities.automod.build;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModEventType;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;

public abstract class AbstractAutoModRuleBuilder<B extends AbstractAutoModRuleBuilder<B>>
{
    protected final AutoModTriggerType triggerType;
    protected final AutoModEventType eventType;
    protected String name;
    protected boolean enabled = true;

    protected final EnumMap<AutoModResponse.Type, AutoModResponse> actions = new EnumMap<>(AutoModResponse.Type.class);
    protected final TLongList exemptChannels = new TLongArrayList();
    protected final TLongList exemptRoles = new TLongArrayList();

    protected AbstractAutoModRuleBuilder(AutoModTriggerType triggerType, AutoModEventType eventType, String name)
    {
        Checks.notNull(triggerType, "Trigger Type");
        Checks.notNull(eventType, "Event Type");
        this.triggerType = triggerType;
        this.eventType = eventType;
        setName(name);
    }

    @Nonnull
    public B setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, AutoModRule.MAX_RULE_NAME_LENGTH, "Name");
        this.name = name;
        return (B) this;
    }

    @Nonnull
    public B setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return (B) this;
    }

    @Nonnull
    public B putResponses(@Nonnull AutoModResponse... responses)
    {
        Checks.noneNull(responses, "Responses");
        for (AutoModResponse response : responses)
            actions.put(response.getType(), response);
        return (B) this;
    }

    @Nonnull
    public B putResponses(@Nonnull Collection<? extends AutoModResponse> responses)
    {
        Checks.noneNull(responses, "Responses");
        for (AutoModResponse response : responses)
            actions.put(response.getType(), response);
        return (B) this;
    }

    @Nonnull
    public B setResponses(@Nonnull Collection<? extends AutoModResponse> responses)
    {
        Checks.noneNull(responses, "Responses");
        actions.clear();
        for (AutoModResponse response : responses)
            actions.put(response.getType(), response);
        return (B) this;
    }

    @Nonnull
    public B addExemptRoles(@Nonnull Role... roles)
    {
        Checks.noneNull(roles, "Roles");
        for (Role role : roles)
            exemptRoles.add(role.getIdLong());
        return (B) this;
    }

    @Nonnull
    public B addExemptRoles(@Nonnull Collection<? extends Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        for (Role role : roles)
            exemptRoles.add(role.getIdLong());
        return (B) this;
    }

    @Nonnull
    public B setExemptRoles(@Nonnull Collection<? extends Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        exemptRoles.clear();
        for (Role role : roles)
            exemptRoles.add(role.getIdLong());
        return (B) this;
    }

    @Nonnull
    public B addExemptChannels(@Nonnull GuildChannel... channels)
    {
        Checks.noneNull(channels, "Channels");
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getIdLong());
        return (B) this;
    }

    @Nonnull
    public B addExemptChannels(@Nonnull Collection<? extends GuildChannel> channels)
    {
        Checks.noneNull(channels, "Channels");
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getIdLong());
        return (B) this;
    }

    @Nonnull
    public B setExemptChannels(@Nonnull Collection<? extends GuildChannel> channels)
    {
        Checks.noneNull(channels, "Channels");
        exemptChannels.clear();
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getIdLong());
        return (B) this;
    }

    @Nonnull
    public AutoModRuleData build()
    {
        AutoModRuleData rule = new AutoModRuleData(triggerType, eventType, name);
        rule.setEnabled(enabled);
        rule.setExemptChannels(exemptChannels.toArray());
        rule.setExemptRoles(exemptRoles.toArray());
        rule.setActions(new ArrayList<>(actions.values()));
        return rule;
    }
}
