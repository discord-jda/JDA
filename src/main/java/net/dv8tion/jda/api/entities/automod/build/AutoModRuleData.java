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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModEventType;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Data class used to create new {@link AutoModRule AutoModRules}.
 *
 * <p>Every rule must configure at least one {@link #putResponses(AutoModResponse...) response}.
 *
 * <p><b>Example</b><br>
 *
 * <pre>{@code
 * TriggerConfig config = TriggerConfig.keywordFilter("discord.gg/*").addAllowList("gateway.discord.gg/*");
 * AutoModRuleData data = AutoModRuleData.onMessage("Invite Block", config);
 * data.addExemptRoles(guild.getRolesByName("Moderator", true));
 * data.putResponse(AutoModResponse.blockMessage());
 * }</pre>
 *
 * <ol>
 *   <li>The {@link TriggerConfig} defines under what conditions the rule should be triggered and execute a response.
 *       It should trigger on all invite links, but not trigger on the gateway subdomain.</li>
 *   <li>The rule is then created with this trigger config and we name it {@code "Invite Block"}.</li>
 *   <li>Using {@link #addExemptRoles(Role...)}, the moderator role has been excluded to allow moderators to post links.</li>
 *   <li>With {@link #putResponses(AutoModResponse...)}, an automatic action is enabled to block the message, whenever it triggers the rule.</li>
 * </ol>
 *
 * @see net.dv8tion.jda.api.entities.Guild#createAutoModRule(AutoModRuleData)
 */
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
        this.setName(name);
        this.setTriggerConfig(triggerMetadata);
    }

    /**
     * Create a new {@link AutoModRule} which triggers on a message being sent in a channel.
     *
     * @param  name
     *         The name of the rule (1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters)
     * @param  triggerConfig
     *         The trigger configuration for this rule
     *
     * @throws IllegalArgumentException
     *         If null is provided or the name is not between 1 and {@value AutoModRule#MAX_RULE_NAME_LENGTH} characters
     *
     * @return The new {@link AutoModRuleData} instance
     */
    @Nonnull
    public static AutoModRuleData onMessage(@Nonnull String name, @Nonnull TriggerConfig triggerConfig)
    {
        return new AutoModRuleData(AutoModEventType.MESSAGE_SEND, name, triggerConfig);
    }

//    /**
//     * Create a new {@link AutoModRule} which triggers on a member profile being updated.
//     *
//     * @param  name
//     *         The name of the rule (1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters)
//     * @param  triggerConfig
//     *         The trigger configuration for this rule
//     *
//     * @throws IllegalArgumentException
//     *         If null is provided or the name is not between 1 and {@value AutoModRule#MAX_RULE_NAME_LENGTH} characters
//     *
//     * @return The new {@link AutoModRuleData} instance
//     */
//    @Nonnull
//    public static AutoModRuleData onMemberProfile(@Nonnull String name, @Nonnull TriggerConfig triggerConfig)
//    {
//        return new AutoModRuleData(AutoModEventType.MEMBER_UPDATE, name, triggerConfig)
//                .putResponses(AutoModResponse.blockMemberInteraction());
//    }


    /**
     * Change the name of the rule.
     *
     * @param  name
     *         The new name (1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the name is not between 1 and {@value AutoModRule#MAX_RULE_NAME_LENGTH} characters
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, AutoModRule.MAX_RULE_NAME_LENGTH, "Name");
        this.name = name;
        return this;
    }

    /**
     * Enable or disable the rule.
     *
     * @param  enabled
     *         True, if the rule should be enabled
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Configure what the rule should do upon triggering.
     * <br>This is accumulative and adds ontop of the currently configured responses.
     *
     * <p>Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     *         The responses to configure
     *
     * @throws IllegalArgumentException
     *         If null is provided or any of the responses has an {@link AutoModResponse.Type#UNKNOWN unknown type}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData putResponses(@Nonnull AutoModResponse... responses)
    {
        Checks.noneNull(responses, "Responses");
        for (AutoModResponse response : responses)
        {
            AutoModResponse.Type type = response.getType();
            Checks.check(type != AutoModResponse.Type.UNKNOWN, "Cannot create response with unknown response type");
            actions.put(type, response);
        }
        return this;
    }

    /**
     * Configure what the rule should do upon triggering.
     * <br>This is accumulative and adds ontop of the currently configured responses.
     *
     * <p>Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     *         The responses to configure
     *
     * @throws IllegalArgumentException
     *         If null is provided or any of the responses has an {@link AutoModResponse.Type#UNKNOWN unknown type}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData putResponses(@Nonnull Collection<? extends AutoModResponse> responses)
    {
        Checks.noneNull(responses, "Responses");
        for (AutoModResponse response : responses)
        {
            AutoModResponse.Type type = response.getType();
            Checks.check(type != AutoModResponse.Type.UNKNOWN, "Cannot create response with unknown response type");
            actions.put(type, response);
        }
        return this;
    }

    /**
     * Configure what the rule should do upon triggering.
     * <br>This replaces the currently configured responses, removing all previously configured responses.
     *
     * <p>Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     *         The responses to configure
     *
     * @throws IllegalArgumentException
     *         If null is provided or any of the responses has an {@link AutoModResponse.Type#UNKNOWN unknown type}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData setResponses(@Nonnull Collection<? extends AutoModResponse> responses)
    {
        Checks.noneNull(responses, "Responses");
        actions.clear();
        if (eventType == AutoModEventType.MEMBER_UPDATE)
            actions.put(AutoModResponse.Type.BLOCK_MEMBER_INTERACTION, AutoModResponse.blockMemberInteraction());
        for (AutoModResponse response : responses)
        {
            AutoModResponse.Type type = response.getType();
            Checks.check(type != AutoModResponse.Type.UNKNOWN, "Cannot create response with unknown response type");
            actions.put(type, response);
        }
        return this;
    }

    /**
     * Add roles which can bypass this rule.
     *
     * <p>Roles added to the exemptions will allow all of its members to bypass this rule.
     *
     * @param  roles
     *         The roles to add (up to {@value AutoModRule#MAX_EXEMPT_ROLES} roles)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of roles exceeds {@value AutoModRule#MAX_EXEMPT_ROLES}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData addExemptRoles(@Nonnull Role... roles)
    {
        Checks.noneNull(roles, "Roles");
        Checks.check(roles.length + exemptRoles.size() <= AutoModRule.MAX_EXEMPT_ROLES, "Cannot add more than %d roles", AutoModRule.MAX_EXEMPT_ROLES);
        for (Role role : roles)
            exemptRoles.add(role.getId());
        return this;
    }

    /**
     * Add roles which can bypass this rule.
     *
     * <p>Roles added to the exemptions will allow all of its members to bypass this rule.
     *
     * @param  roles
     *         The roles to add (up to {@value AutoModRule#MAX_EXEMPT_ROLES} roles)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of roles exceeds {@value AutoModRule#MAX_EXEMPT_ROLES}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData addExemptRoles(@Nonnull Collection<? extends Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        Checks.check(roles.size() + exemptRoles.size() <= AutoModRule.MAX_EXEMPT_ROLES, "Cannot add more than %d roles", AutoModRule.MAX_EXEMPT_ROLES);
        for (Role role : roles)
            exemptRoles.add(role.getId());
        return this;
    }

    /**
     * Set which roles can bypass this rule.
     *
     * <p>Roles added to the exemptions will allow all of its members to bypass this rule.
     *
     * @param  roles
     *         The roles to exempt (up to {@value AutoModRule#MAX_EXEMPT_ROLES} roles)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of roles exceeds {@value AutoModRule#MAX_EXEMPT_ROLES}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData setExemptRoles(@Nonnull Collection<? extends Role> roles)
    {
        Checks.noneNull(roles, "Roles");
        Checks.check(roles.size() <= AutoModRule.MAX_EXEMPT_ROLES, "Cannot add more than %d roles", AutoModRule.MAX_EXEMPT_ROLES);
        exemptRoles.clear();
        for (Role role : roles)
            exemptRoles.add(role.getId());
        return this;
    }

    /**
     * Add channels which can bypass this rule.
     *
     * <p>No messages sent in this channel will trigger the rule.
     *
     * @param  channels
     *         The channels to add (up to {@value AutoModRule#MAX_EXEMPT_CHANNELS} channels)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of channels exceeds {@value AutoModRule#MAX_EXEMPT_CHANNELS}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData addExemptChannels(@Nonnull GuildChannel... channels)
    {
        Checks.noneNull(channels, "Channels");
        Checks.check(channels.length + exemptChannels.size() <= AutoModRule.MAX_EXEMPT_CHANNELS, "Cannot add more than %d channels", AutoModRule.MAX_EXEMPT_CHANNELS);
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getId());
        return this;
    }

    /**
     * Add channels which can bypass this rule.
     *
     * <p>No messages sent in this channel will trigger the rule.
     *
     * @param  channels
     *         The channels to add (up to {@value AutoModRule#MAX_EXEMPT_CHANNELS} channels)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of channels exceeds {@value AutoModRule#MAX_EXEMPT_CHANNELS}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData addExemptChannels(@Nonnull Collection<? extends GuildChannel> channels)
    {
        Checks.noneNull(channels, "Channels");
        Checks.check(channels.size() + exemptChannels.size() <= AutoModRule.MAX_EXEMPT_CHANNELS, "Cannot add more than %d channels", AutoModRule.MAX_EXEMPT_CHANNELS);
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getId());
        return this;
    }

    /**
     * Set which channels can bypass this rule.
     *
     * <p>No messages sent in this channel will trigger the rule.
     *
     * @param  channels
     *         The channels to add (up to {@value AutoModRule#MAX_EXEMPT_CHANNELS} channels)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the number of channels exceeds {@value AutoModRule#MAX_EXEMPT_CHANNELS}
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData setExemptChannels(@Nonnull Collection<? extends GuildChannel> channels)
    {
        Checks.noneNull(channels, "Channels");
        Checks.check(channels.size() <= AutoModRule.MAX_EXEMPT_CHANNELS, "Cannot add more than %d channels", AutoModRule.MAX_EXEMPT_CHANNELS);
        exemptChannels.clear();
        for (GuildChannel channel : channels)
            exemptChannels.add(channel.getId());
        return this;
    }

    /**
     * Change the {@link TriggerConfig} for this rule.
     *
     * @param  config
     *         The new config
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same {@link AutoModRuleData} instance
     */
    @Nonnull
    public AutoModRuleData setTriggerConfig(@Nonnull TriggerConfig config)
    {
        Checks.notNull(config, "TriggerConfig");
        Checks.check(config.getType().isEventTypeSupported(eventType), "Cannot use trigger type %s with event type %s", config.getType(), eventType);
        this.triggerMetadata = config;
        return this;
    }

    /**
     * Returns the {@link Permission Permissions} required to create this rule.
     * <br>Certain {@link AutoModResponse.Type Types} require additional permissions, such as {@link AutoModResponse.Type#TIMEOUT}.
     * All rules require {@link Permission#MANAGE_SERVER} to be created.
     *
     * @return The required permissions to create this rule
     */
    @Nonnull
    public EnumSet<Permission> getRequiredPermissions()
    {
        if (actions.containsKey(AutoModResponse.Type.TIMEOUT))
            return EnumSet.of(Permission.MANAGE_SERVER, Permission.MODERATE_MEMBERS);
        else
            return EnumSet.of(Permission.MANAGE_SERVER);
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        AutoModTriggerType triggerType = triggerMetadata.getType();
        if (eventType == AutoModEventType.MEMBER_UPDATE)
        {
            if (triggerType == AutoModTriggerType.KEYWORD)
                triggerType = AutoModTriggerType.MEMBER_PROFILE_KEYWORD;
            else
                throw new IllegalStateException("Cannot create rule of trigger type " + triggerType + " with event type " + eventType);
        }

        for (AutoModResponse response : actions.values())
        {
            if (!response.getType().isSupportedTrigger(triggerType))
                throw new IllegalStateException("Cannot create a rule of trigger type " + triggerType + " with response type " + response.getType());
        }

        if (actions.isEmpty())
            throw new IllegalStateException("Cannot create a rule with no responses. Add at least one response with putResponses(...)");

        DataObject data = DataObject.empty()
                .put("name", name)
                .put("enabled", enabled)
                .put("event_type", eventType.getKey());

        data.put("actions", DataArray.fromCollection(actions.values()));

        data.put("exempt_roles", DataArray.fromCollection(exemptRoles));
        data.put("exempt_channels", DataArray.fromCollection(exemptChannels));

        data.put("trigger_type", triggerType.getKey());
        data.put("trigger_metadata", triggerMetadata.toData());

        return data;
    }
}
