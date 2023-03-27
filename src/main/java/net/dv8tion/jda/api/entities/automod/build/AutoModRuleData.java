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

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModEventType;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;

public class AutoModRuleData implements SerializableData
{
    protected final AutoModEventType eventType;
    protected String name;
    protected boolean enabled = true;
    protected TriggerConfig triggerMetadata;

    protected final EnumMap<AutoModResponse.Type, AutoModResponse> actions = new EnumMap<>(AutoModResponse.Type.class);
    protected final Collection<String> exemptChannels = new ArrayList<>();
    protected final Collection<String> exemptRoles = new ArrayList<>();

    protected AutoModRuleData(AutoModEventType eventType, String name, TriggerConfig triggerMetadata)
    {
        this.eventType = eventType;
        this.name = name;
        this.triggerMetadata = triggerMetadata;
    }

    @Nonnull
    public static AutoModRuleData onMessage(@Nonnull String name, @Nonnull TriggerConfig triggerConfig)
    {
        return new AutoModRuleData(AutoModEventType.MESSAGE_SEND, name, triggerConfig);
    }

    @Nonnull
    public AutoModRuleData setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, AutoModRule.MAX_RULE_NAME_LENGTH, "Name");
        this.name = name;
        return this;
    }

    @Nonnull
    public AutoModRuleData setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    @Nonnull
    public AutoModRuleData putResponses(@Nonnull AutoModResponse... responses)
    {
        Checks.noneNull(responses, "Responses");
        for (AutoModResponse response : responses)
            actions.put(response.getType(), response);
        return this;
    }

    @Nonnull
    public AutoModRuleData putResponses(@Nonnull Collection<? extends AutoModResponse> responses)
    {
        Checks.noneNull(responses, "Responses");
        for (AutoModResponse response : responses)
            actions.put(response.getType(), response);
        return this;
    }

    @Nonnull
    public AutoModRuleData setResponses(@Nonnull Collection<? extends AutoModResponse> responses)
    {
        Checks.noneNull(responses, "Responses");
        actions.clear();
        for (AutoModResponse response : responses)
            actions.put(response.getType(), response);
        return this;
    }

    @Nonnull
    public AutoModRuleData addExemptRoles(@Nonnull Role... roles)
    {
        Checks.noneNull(roles, "Roles");
        for (Role role : roles)
            exemptRoles.add(role.getId());
        return this;
    }

    @Nonnull
    public AutoModRuleData addExemptRoles(@Nonnull Collection<? extends Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        for (Role role : roles)
            exemptRoles.add(role.getId());
        return this;
    }

    @Nonnull
    public AutoModRuleData setExemptRoles(@Nonnull Collection<? extends Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        exemptRoles.clear();
        for (Role role : roles)
            exemptRoles.add(role.getId());
        return this;
    }

    @Nonnull
    public AutoModRuleData addExemptChannels(@Nonnull GuildChannel... channels)
    {
        Checks.noneNull(channels, "Channels");
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getId());
        return this;
    }

    @Nonnull
    public AutoModRuleData addExemptChannels(@Nonnull Collection<? extends GuildChannel> channels)
    {
        Checks.noneNull(channels, "Channels");
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getId());
        return this;
    }

    @Nonnull
    public AutoModRuleData setExemptChannels(@Nonnull Collection<? extends GuildChannel> channels)
    {
        Checks.noneNull(channels, "Channels");
        exemptChannels.clear();
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getId());
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public AutoModRuleData setTriggerConfig(@Nonnull TriggerConfig config)
    {
        Checks.notNull(config, "TriggerConfig");
        this.triggerMetadata = config;
        return this;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject data = DataObject.empty()
                .put("name", name)
                .put("enabled", enabled)
                .put("event_type", eventType.getKey());

        data.put("actions", DataArray.fromCollection(actions.values()));

        data.put("exempt_roles", DataArray.fromCollection(exemptRoles));
        data.put("exempt_channels", DataArray.fromCollection(exemptChannels));

        data.put("trigger_type", triggerMetadata.getType().getKey());
        data.put("trigger_metadata", triggerMetadata.toData());

        return data;
    }
}
