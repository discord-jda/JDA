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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.entities.automod.build.TriggerConfig;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.AutoModRuleManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class AutoModRuleManagerImpl extends ManagerBase<AutoModRuleManager> implements AutoModRuleManager
{
    protected final Guild guild;
    protected String name;
    protected boolean enabled;
    protected EnumMap<AutoModResponse.Type, AutoModResponse> responses;
    protected List<Role> exemptRoles;
    protected List<GuildChannel> exemptChannels;
    protected TriggerConfig triggerConfig;

    public AutoModRuleManagerImpl(Guild guild, String ruleId)
    {
        super(guild.getJDA(), Route.AutoModeration.UPDATE_RULE.compile(guild.getId(), ruleId));
        this.guild = guild;
    }

    @Nonnull
    @Override
    public AutoModRuleManager setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, AutoModRule.MAX_RULE_NAME_LENGTH, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    public AutoModRuleManager setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        set |= ENABLED;
        return this;
    }

    @Nonnull
    @Override
    public AutoModRuleManager setResponses(@Nonnull Collection<? extends AutoModResponse> responses)
    {
        Checks.noneNull(responses, "Responses");
        Checks.notEmpty(responses, "Responses");
        this.responses = new EnumMap<>(AutoModResponse.Type.class);
        for (AutoModResponse response : responses)
        {
            AutoModResponse.Type type = response.getType();
            Checks.check(type != AutoModResponse.Type.UNKNOWN, "Cannot add response with unknown response type!");
            this.responses.put(type, response);
        }
        set |= RESPONSE;
        return this;
    }

    @Nonnull
    @Override
    public AutoModRuleManager setExemptRoles(@Nonnull Collection<Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        Checks.check(roles.size() <= AutoModRule.MAX_EXEMPT_ROLES, "Cannot have more than %d exempt roles!", AutoModRule.MAX_EXEMPT_ROLES);
        for (Role role : roles)
            Checks.check(role.getGuild().equals(guild), "Role %s is not from the same guild as this rule!", role);
        this.exemptRoles = new ArrayList<>(roles);
        set |= EXEMPT_ROLES;
        return this;
    }

    @Nonnull
    @Override
    public AutoModRuleManager setExemptChannels(@Nonnull Collection<? extends GuildChannel> channels)
    {
        Checks.noneNull(channels, "Channels");
        Checks.check(channels.size() <= AutoModRule.MAX_EXEMPT_CHANNELS, "Cannot have more than %d exempt channels!", AutoModRule.MAX_EXEMPT_CHANNELS);
        for (GuildChannel channel : channels)
            Checks.check(channel.getGuild().equals(guild), "Channel %s is not from the same guild as this rule!", channel);
        this.exemptChannels = new ArrayList<>(channels);
        set |= EXEMPT_CHANNELS;
        return this;
    }

    @Nonnull
    @Override
    public AutoModRuleManager setTriggerConfig(@Nonnull TriggerConfig config)
    {
        Checks.notNull(config, "TriggerConfig");
        Checks.check(config.getType() != AutoModTriggerType.UNKNOWN, "Unknown trigger type!");
        this.triggerConfig = config;
        set |= TRIGGER_METADATA;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject body = DataObject.empty();

        if (shouldUpdate(NAME))
            body.put("name", name);
        if (shouldUpdate(ENABLED))
            body.put("enabled", enabled);
        if (shouldUpdate(RESPONSE))
            body.put("actions", DataArray.fromCollection(responses.values()));
        if (shouldUpdate(EXEMPT_ROLES))
            body.put("exempt_roles", DataArray.fromCollection(exemptRoles.stream().map(Role::getId).collect(Collectors.toList())));
        if (shouldUpdate(EXEMPT_CHANNELS))
            body.put("exempt_channels", DataArray.fromCollection(exemptChannels.stream().map(GuildChannel::getId).collect(Collectors.toList())));
        if (shouldUpdate(TRIGGER_METADATA))
        {
            body.put("trigger_type", triggerConfig.getType().getKey());
            body.put("trigger_metadata", triggerConfig.toData());
        }

        reset();
        return getRequestBody(body);
    }
}
