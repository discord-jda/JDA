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
package net.dv8tion.jda.api.managers

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.automod.AutoModResponse
import net.dv8tion.jda.api.entities.automod.build.TriggerConfig
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for an [AutoModRule].
 *
 *
 * **Example**
 * <pre>`manager.setName("Discord Invites")
 * .setEnables(false)
 * .queue();
 * manager.reset(AutoModRuleManager.NAME | AutoModRuleManager.ENABLED)
 * .setName("Invites")
 * .setEnabled(true)
 * .queue();
`</pre> *
 *
 * @see Guild.modifyAutoModRuleById
 * @see Guild.modifyAutoModRuleById
 * @see AutoModRule.getManager
 */
interface AutoModRuleManager : Manager<AutoModRuleManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(AutoModRuleManager.NAME | AutoModRuleManager.RESPONSE);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.ENABLED]
     *  * [.RESPONSE]
     *  * [.EXEMPT_ROLES]
     *  * [.EXEMPT_CHANNELS]
     *  * [.TRIGGER_METADATA]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): AutoModRuleManager?

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(AutoModRuleManager.NAME, AutoModRuleManager.RESPONSE);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.ENABLED]
     *  * [.RESPONSE]
     *  * [.EXEMPT_ROLES]
     *  * [.EXEMPT_CHANNELS]
     *  * [.TRIGGER_METADATA]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): AutoModRuleManager?

    /**
     * Sets the **<u>name</u>** of the selected [AutoModRule].
     *
     *
     * A rule name **must** be between 1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters long!
     *
     * @param  name
     * The new name for the selected [AutoModRule]
     *
     * @throws IllegalArgumentException
     * If the provided name is `null` or not between 1-{@value AutoModRule#MAX_RULE_NAME_LENGTH} characters long
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): AutoModRuleManager?

    /**
     * Sets the **<u>enabled</u>** state of the selected [AutoModRule].
     *
     *
     * When a rule is disabled, it will not be applied to any messages.
     *
     * @param  enabled
     * True, if the selected [AutoModRule] should be enabled
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setEnabled(enabled: Boolean): AutoModRuleManager?
    //    @Nonnull
    //    @CheckReturnValue
    //    AutoModRuleManager setEventType(@Nonnull AutoModEventType type);
    /**
     * Sets what the rule should do upon triggering.
     *
     *
     * Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     * The responses to configure
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` or [AutoModResponse.Type.UNKNOWN] is provided
     *  * If the collection is empty
     *
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setResponses(@Nonnull responses: Collection<AutoModResponse?>?): AutoModRuleManager?

    /**
     * Sets what the rule should do upon triggering.
     *
     *
     * Note that each response type can only be used once.
     * If multiple responses of the same type are provided, the last one is used.
     *
     * @param  responses
     * The responses to configure
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` or [AutoModResponse.Type.UNKNOWN] is provided
     *  * If the collection is empty
     *
     *
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setResponses(@Nonnull vararg responses: AutoModResponse?): AutoModRuleManager? {
        Checks.noneNull(responses, "Responses")
        return setResponses(Arrays.asList(*responses))
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
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setExemptRoles(@Nonnull roles: Collection<Role?>?): AutoModRuleManager?

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
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setExemptRoles(@Nonnull vararg roles: Role?): AutoModRuleManager? {
        Checks.noneNull(roles, "Roles")
        return setExemptRoles(Arrays.asList(*roles))
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
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setExemptChannels(@Nonnull channels: Collection<GuildChannel?>?): AutoModRuleManager?

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
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setExemptChannels(@Nonnull vararg channels: GuildChannel?): AutoModRuleManager? {
        Checks.noneNull(channels, "Channels")
        return setExemptChannels(Arrays.asList(*channels))
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
     * @return AutoModRuleManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setTriggerConfig(@Nonnull config: TriggerConfig?): AutoModRuleManager?

    companion object {
        /** Used to reset the name field.  */
        const val NAME: Long = 1

        /** Used to reset the enabled field.  */
        const val ENABLED = (1 shl 1).toLong()

        /** Used to reset the response field.  */
        const val RESPONSE = (1 shl 2).toLong()

        /** Used to reset the exempt roles field.  */
        const val EXEMPT_ROLES = (1 shl 3).toLong()

        /** Used to reset the exempt channels field.  */
        const val EXEMPT_CHANNELS = (1 shl 4).toLong()

        /** Used to reset the trigger metadata field.  */
        const val TRIGGER_METADATA = (1 shl 5).toLong()
    }
}
