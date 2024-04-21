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
package net.dv8tion.jda.api.entities.automod.build

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.automod.AutoModEventType
import net.dv8tion.jda.api.entities.automod.AutoModResponse
import net.dv8tion.jda.api.entities.automod.AutoModRule
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

/**
 * Data class used to create new [AutoModRules][AutoModRule].
 *
 *
 * Every rule must configure at least one [response][.putResponses].
 *
 *
 * **Example**<br></br>
 *
 * <pre>`TriggerConfig config = TriggerConfig.keywordFilter("discord.gg/ *").addAllowList("gateway.discord.gg/ *");
 * AutoModRuleData data = AutoModRuleData.onMessage("Invite Block", config);
 * data.addExemptRoles(guild.getRolesByName("Moderator", true));
 * data.putResponse(AutoModResponse.blockMessage());
`</pre> *
 *
 *
 *  1. The [TriggerConfig] defines under what conditions the rule should be triggered and execute a response.
 * It should trigger on all invite links, but not trigger on the gateway subdomain.
 *  1. The rule is then created with this trigger config and we name it `"Invite Block"`.
 *  1. Using [.addExemptRoles], the moderator role has been excluded to allow moderators to post links.
 *  1. With [.putResponses], an automatic action is enabled to block the message, whenever it triggers the rule.
 *
 *
 * @see net.dv8tion.jda.api.entities.Guild.createAutoModRule
 */
class AutoModRuleData protected constructor(
    protected val eventType: AutoModEventType,
    name: String?,
    triggerMetadata: TriggerConfig
) : SerializableData {
    protected var name: String? = null
    protected var enabled = true
    protected var triggerMetadata: TriggerConfig? = null
    protected val actions = EnumMap<AutoModResponse.Type, AutoModResponse?>(
        AutoModResponse.Type::class.java
    )
    protected val exemptChannels: MutableCollection<String?> = ArrayList()
    protected val exemptRoles: MutableCollection<String?> = ArrayList()

    init {
        setName(name)
        setTriggerConfig(triggerMetadata)
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
     * The new name (1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     * If the name is not between 1 and {@value AutoModRule#MAX_RULE_NAME_LENGTH} characters
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun setName(@Nonnull name: String?): AutoModRuleData {
        Checks.notEmpty(name, "Name")
        Checks.notLonger(name, AutoModRule.MAX_RULE_NAME_LENGTH, "Name")
        this.name = name
        return this
    }

    /**
     * Enable or disable the rule.
     *
     * @param  enabled
     * True, if the rule should be enabled
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun setEnabled(enabled: Boolean): AutoModRuleData {
        this.enabled = enabled
        return this
    }

    /**
     * Configure what the rule should do upon triggering.
     * <br></br>This is accumulative and adds ontop of the currently configured responses.
     *
     *
     * Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     * The responses to configure
     *
     * @throws IllegalArgumentException
     * If null is provided or any of the responses has an [unknown type][AutoModResponse.Type.UNKNOWN]
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun putResponses(@Nonnull vararg responses: AutoModResponse): AutoModRuleData {
        Checks.noneNull(responses, "Responses")
        for (response in responses) {
            val type = response.type
            Checks.check(type != AutoModResponse.Type.UNKNOWN, "Cannot create response with unknown response type")
            actions[type] = response
        }
        return this
    }

    /**
     * Configure what the rule should do upon triggering.
     * <br></br>This is accumulative and adds ontop of the currently configured responses.
     *
     *
     * Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     * The responses to configure
     *
     * @throws IllegalArgumentException
     * If null is provided or any of the responses has an [unknown type][AutoModResponse.Type.UNKNOWN]
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun putResponses(@Nonnull responses: Collection<AutoModResponse?>): AutoModRuleData {
        Checks.noneNull(responses, "Responses")
        for (response in responses) {
            val type = response!!.type
            Checks.check(type != AutoModResponse.Type.UNKNOWN, "Cannot create response with unknown response type")
            actions[type] = response
        }
        return this
    }

    /**
     * Configure what the rule should do upon triggering.
     * <br></br>This replaces the currently configured responses, removing all previously configured responses.
     *
     *
     * Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     * The responses to configure
     *
     * @throws IllegalArgumentException
     * If null is provided or any of the responses has an [unknown type][AutoModResponse.Type.UNKNOWN]
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun setResponses(@Nonnull responses: Collection<AutoModResponse?>): AutoModRuleData {
        Checks.noneNull(responses, "Responses")
        actions.clear()
        if (eventType == AutoModEventType.MEMBER_UPDATE) actions[AutoModResponse.Type.BLOCK_MEMBER_INTERACTION] =
            AutoModResponse.blockMemberInteraction()
        for (response in responses) {
            val type = response!!.type
            Checks.check(type != AutoModResponse.Type.UNKNOWN, "Cannot create response with unknown response type")
            actions[type] = response
        }
        return this
    }

    /**
     * Add roles which can bypass this rule.
     *
     *
     * Roles added to the exemptions will allow all of its members to bypass this rule.
     *
     * @param  roles
     * The roles to add (up to {@value AutoModRule#MAX_EXEMPT_ROLES} roles)
     *
     * @throws IllegalArgumentException
     * If null is provided or the number of roles exceeds {@value AutoModRule#MAX_EXEMPT_ROLES}
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun addExemptRoles(@Nonnull vararg roles: Role): AutoModRuleData {
        Checks.noneNull(roles, "Roles")
        Checks.check(
            roles.size + exemptRoles.size <= AutoModRule.MAX_EXEMPT_ROLES,
            "Cannot add more than %d roles",
            AutoModRule.MAX_EXEMPT_ROLES
        )
        for (role in roles) exemptRoles.add(role.id)
        return this
    }

    /**
     * Add roles which can bypass this rule.
     *
     *
     * Roles added to the exemptions will allow all of its members to bypass this rule.
     *
     * @param  roles
     * The roles to add (up to {@value AutoModRule#MAX_EXEMPT_ROLES} roles)
     *
     * @throws IllegalArgumentException
     * If null is provided or the number of roles exceeds {@value AutoModRule#MAX_EXEMPT_ROLES}
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun addExemptRoles(@Nonnull roles: Collection<Role?>): AutoModRuleData {
        Checks.noneNull(roles, "Roles")
        Checks.check(
            roles.size + exemptRoles.size <= AutoModRule.MAX_EXEMPT_ROLES,
            "Cannot add more than %d roles",
            AutoModRule.MAX_EXEMPT_ROLES
        )
        for (role in roles) exemptRoles.add(role!!.id)
        return this
    }

    /**
     * Set which roles can bypass this rule.
     *
     *
     * Roles added to the exemptions will allow all of its members to bypass this rule.
     *
     * @param  roles
     * The roles to exempt (up to {@value AutoModRule#MAX_EXEMPT_ROLES} roles)
     *
     * @throws IllegalArgumentException
     * If null is provided or the number of roles exceeds {@value AutoModRule#MAX_EXEMPT_ROLES}
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun setExemptRoles(@Nonnull roles: Collection<Role?>): AutoModRuleData {
        Checks.noneNull(roles, "Roles")
        Checks.check(
            roles.size <= AutoModRule.MAX_EXEMPT_ROLES,
            "Cannot add more than %d roles",
            AutoModRule.MAX_EXEMPT_ROLES
        )
        exemptRoles.clear()
        for (role in roles) exemptRoles.add(role!!.id)
        return this
    }

    /**
     * Add channels which can bypass this rule.
     *
     *
     * No messages sent in this channel will trigger the rule.
     *
     * @param  channels
     * The channels to add (up to {@value AutoModRule#MAX_EXEMPT_CHANNELS} channels)
     *
     * @throws IllegalArgumentException
     * If null is provided or the number of channels exceeds {@value AutoModRule#MAX_EXEMPT_CHANNELS}
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun addExemptChannels(@Nonnull vararg channels: GuildChannel): AutoModRuleData {
        Checks.noneNull(channels, "Channels")
        Checks.check(
            channels.size + exemptChannels.size <= AutoModRule.MAX_EXEMPT_CHANNELS,
            "Cannot add more than %d channels",
            AutoModRule.MAX_EXEMPT_CHANNELS
        )
        for (channel in channels) exemptChannels.add(channel.id)
        return this
    }

    /**
     * Add channels which can bypass this rule.
     *
     *
     * No messages sent in this channel will trigger the rule.
     *
     * @param  channels
     * The channels to add (up to {@value AutoModRule#MAX_EXEMPT_CHANNELS} channels)
     *
     * @throws IllegalArgumentException
     * If null is provided or the number of channels exceeds {@value AutoModRule#MAX_EXEMPT_CHANNELS}
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun addExemptChannels(@Nonnull channels: Collection<GuildChannel?>): AutoModRuleData {
        Checks.noneNull(channels, "Channels")
        Checks.check(
            channels.size + exemptChannels.size <= AutoModRule.MAX_EXEMPT_CHANNELS,
            "Cannot add more than %d channels",
            AutoModRule.MAX_EXEMPT_CHANNELS
        )
        for (channel in channels) exemptChannels.add(channel!!.id)
        return this
    }

    /**
     * Set which channels can bypass this rule.
     *
     *
     * No messages sent in this channel will trigger the rule.
     *
     * @param  channels
     * The channels to add (up to {@value AutoModRule#MAX_EXEMPT_CHANNELS} channels)
     *
     * @throws IllegalArgumentException
     * If null is provided or the number of channels exceeds {@value AutoModRule#MAX_EXEMPT_CHANNELS}
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun setExemptChannels(@Nonnull channels: Collection<GuildChannel?>): AutoModRuleData {
        Checks.noneNull(channels, "Channels")
        Checks.check(
            channels.size <= AutoModRule.MAX_EXEMPT_CHANNELS,
            "Cannot add more than %d channels",
            AutoModRule.MAX_EXEMPT_CHANNELS
        )
        exemptChannels.clear()
        for (channel in channels) exemptChannels.add(channel!!.id)
        return this
    }

    /**
     * Change the [TriggerConfig] for this rule.
     *
     * @param  config
     * The new config
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same [AutoModRuleData] instance
     */
    @Nonnull
    fun setTriggerConfig(@Nonnull config: TriggerConfig): AutoModRuleData {
        Checks.notNull(config, "TriggerConfig")
        Checks.check(
            config.getType().isEventTypeSupported(eventType),
            "Cannot use trigger type %s with event type %s",
            config.getType(),
            eventType
        )
        triggerMetadata = config
        return this
    }

    @get:Nonnull
    val requiredPermissions: EnumSet<Permission>
        /**
         * Returns the [Permissions][Permission] required to create this rule.
         * <br></br>Certain [Types][AutoModResponse.Type] require additional permissions, such as [AutoModResponse.Type.TIMEOUT].
         * All rules require [Permission.MANAGE_SERVER] to be created.
         *
         * @return The required permissions to create this rule
         */
        get() = if (actions.containsKey(AutoModResponse.Type.TIMEOUT)) EnumSet.of(
            Permission.MANAGE_SERVER,
            Permission.MODERATE_MEMBERS
        ) else EnumSet.of(Permission.MANAGE_SERVER)

    @Nonnull
    override fun toData(): DataObject {
        var triggerType = triggerMetadata.getType()
        if (eventType == AutoModEventType.MEMBER_UPDATE) {
            triggerType =
                if (triggerType == AutoModTriggerType.KEYWORD) AutoModTriggerType.MEMBER_PROFILE_KEYWORD else throw IllegalStateException(
                    "Cannot create rule of trigger type $triggerType with event type $eventType"
                )
        }
        for (response in actions.values) {
            check(
                response!!.type.isSupportedTrigger(triggerType!!)
            ) { "Cannot create a rule of trigger type " + triggerType + " with response type " + response.type }
        }
        check(!actions.isEmpty()) { "Cannot create a rule with no responses. Add at least one response with putResponses(...)" }
        val data = DataObject.empty()
            .put("name", name)
            .put("enabled", enabled)
            .put("event_type", eventType.key)
        data.put("actions", DataArray.fromCollection(actions.values))
        data.put("exempt_roles", DataArray.fromCollection(exemptRoles))
        data.put("exempt_channels", DataArray.fromCollection(exemptChannels))
        data.put("trigger_type", triggerType!!.key)
        data.put("trigger_metadata", triggerMetadata!!.toData())
        return data
    }

    companion object {
        /**
         * Create a new [AutoModRule] which triggers on a message being sent in a channel.
         *
         * @param  name
         * The name of the rule (1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters)
         * @param  triggerConfig
         * The trigger configuration for this rule
         *
         * @throws IllegalArgumentException
         * If null is provided or the name is not between 1 and {@value AutoModRule#MAX_RULE_NAME_LENGTH} characters
         *
         * @return The new [AutoModRuleData] instance
         */
        @JvmStatic
        @Nonnull
        fun onMessage(@Nonnull name: String?, @Nonnull triggerConfig: TriggerConfig): AutoModRuleData {
            return AutoModRuleData(AutoModEventType.MESSAGE_SEND, name, triggerConfig)
        }
    }
}
