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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.IWebhookContainerUnion
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Manager providing functionality to update one or more fields for a [Webhook][net.dv8tion.jda.api.entities.Webhook].
 *
 *
 * **Example**
 * <pre>`manager.setName("GitHub Webhook")
 * .setChannel(channel)
 * .queue();
 * manager.reset(WebhookManager.NAME | WebhookManager.AVATAR)
 * .setName("Meme Feed")
 * .setAvatar(null)
 * .queue();
`</pre> *
 *
 * @see net.dv8tion.jda.api.entities.Webhook.getManager
 */
interface WebhookManager : Manager<WebhookManager?> {
    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br></br>Example: `manager.reset(WebhookManager.CHANNEL | WebhookManager.NAME);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.AVATAR]
     *  * [.CHANNEL]
     *
     *
     * @param  fields
     * Integer value containing the flags to reset.
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    override fun reset(fields: Long): WebhookManager?

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br></br>Example: `manager.reset(WebhookManager.CHANNEL, WebhookManager.NAME);`
     *
     *
     * **Flag Constants:**
     *
     *  * [.NAME]
     *  * [.AVATAR]
     *  * [.CHANNEL]
     *
     *
     * @param  fields
     * Integer values containing the flags to reset.
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    override fun reset(vararg fields: Long): WebhookManager?

    @get:Nonnull
    val webhook: Webhook

    @get:Nonnull
    val channel: IWebhookContainerUnion?
        /**
         * The [channel][net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer] that this Manager's
         * [Webhook][net.dv8tion.jda.api.entities.Webhook] is in.
         * <br></br>This is logically the same as calling `getWebhook().getChannel()`
         *
         * @return The parent [net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer] instance.
         */
        get() = webhook.channel

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] this Manager's
         * [Webhook][net.dv8tion.jda.api.entities.Webhook] is in.
         * <br></br>This is logically the same as calling `getWebhook().getGuild()`
         *
         * @return The parent [Guild][net.dv8tion.jda.api.entities.Guild]
         */
        get() = webhook.guild

    /**
     * Sets the **<u>default name</u>** of the selected [Webhook][net.dv8tion.jda.api.entities.Webhook].
     *
     *
     * A webhook name **must not** be `null` or blank!
     *
     * @param  name
     * The new default name for the selected [Webhook][net.dv8tion.jda.api.entities.Webhook]
     *
     * @throws IllegalArgumentException
     * If the provided name is `null` or blank
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): WebhookManager?

    /**
     * Sets the **<u>default avatar</u>** of the selected [Webhook][net.dv8tion.jda.api.entities.Webhook].
     *
     * @param  icon
     * The new default avatar [Icon][net.dv8tion.jda.api.entities.Icon]
     * for the selected [Webhook][net.dv8tion.jda.api.entities.Webhook]
     * or `null` to reset
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setAvatar(icon: Icon?): WebhookManager?

    /**
     * Sets the [TextChannel] of the selected [Webhook][net.dv8tion.jda.api.entities.Webhook].
     *
     *
     * A webhook channel **must not** be `null` and **must** be in the same [Guild][net.dv8tion.jda.api.entities.Guild]!
     *
     * @param  channel
     * The new [TextChannel]
     * for the selected [Webhook][net.dv8tion.jda.api.entities.Webhook]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have the Permission [MANAGE_WEBHOOKS][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS]
     * in the specified TextChannel
     * @throws IllegalArgumentException
     * If the provided channel is `null` or from a different Guild
     *
     * @return WebhookManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setChannel(@Nonnull channel: TextChannel?): WebhookManager?

    companion object {
        /** Used to reset the name field  */
        const val NAME: Long = 1

        /** Used to reset the channel field  */
        const val CHANNEL = (1 shl 1).toLong()

        /** Used to reset the avatar field  */
        const val AVATAR = (1 shl 2).toLong()
    }
}
