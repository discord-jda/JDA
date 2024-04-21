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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.unions.IWebhookContainerUnion
import java.util.concurrent.*
import java.util.function.BooleanSupplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * [Webhook][net.dv8tion.jda.api.entities.Webhook] Builder system created as an extension of [net.dv8tion.jda.api.requests.RestAction]
 * <br></br>Provides an easy way to gather and deliver information to Discord to create [Webhooks][net.dv8tion.jda.api.entities.Webhook].
 *
 * @see TextChannel.createWebhook
 */
interface WebhookAction : AuditableRestAction<Webhook?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): WebhookAction?
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): WebhookAction?
    @Nonnull
    override fun deadline(timestamp: Long): WebhookAction?

    @get:Nonnull
    val channel: IWebhookContainerUnion

    @get:Nonnull
    val guild: Guild?
        /**
         * The [Guild][net.dv8tion.jda.api.entities.Guild] to create this webhook in
         *
         * @return The guild
         */
        get() = channel.guild

    /**
     * Sets the **Name** for the custom Webhook User
     *
     * @param  name
     * A not-null String name for the new Webhook user.
     *
     * @throws IllegalArgumentException
     * If the specified name is not in the range of 2-100.
     *
     * @return The current WebhookAction for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    fun setName(@Nonnull name: String?): WebhookAction?

    /**
     * Sets the **Avatar** for the custom Webhook User
     *
     * @param  icon
     * An [Icon][net.dv8tion.jda.api.entities.Icon] for the new avatar.
     * Or null to use default avatar.
     *
     * @return The current WebhookAction for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    fun setAvatar(icon: Icon?): WebhookAction?
}
