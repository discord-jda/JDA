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
package net.dv8tion.jda.api.entities.channel.attribute

import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.WebhookAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a [GuildChannel] that is capable of utilizing [webhooks](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks).
 *
 *
 * Webhooks can be used to integrate third-party systems into Discord by way of sending information via messages.
 */
interface IWebhookContainer : GuildChannel {
    /**
     * Retrieves the [Webhooks][net.dv8tion.jda.api.entities.Webhook] attached to this channel.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>if we were removed from the guild
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.MANAGE_WEBHOOKS][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS] in this channel.
     *
     * @return [net.dv8tion.jda.api.requests.RestAction] - Type: List&lt;[Webhook][net.dv8tion.jda.api.entities.Webhook]&gt;
     * <br></br>Retrieved an immutable list of Webhooks attached to this channel
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveWebhooks(): RestAction<List<Webhook?>?>?

    /**
     * Creates a new [Webhook][net.dv8tion.jda.api.entities.Webhook].
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] caused by
     * the returned [RestAction][net.dv8tion.jda.api.requests.RestAction] include the following:
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The webhook could not be created due to a permission discrepancy
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission was removed
     *
     *  * [MAX_WEBHOOKS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_WEBHOOKS]
     * <br></br>If the channel already has reached the maximum capacity for webhooks
     *
     *
     * @param  name
     * The default name for the new Webhook.
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     * If you do not hold the permission [Manage Webhooks][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS]
     * @throws IllegalArgumentException
     * If the provided name is `null`, blank or not
     * between 2-100 characters in length
     *
     * @return A specific [WebhookAction]
     * <br></br>This action allows to set fields for the new webhook before creating it
     */
    @Nonnull
    @CheckReturnValue
    fun createWebhook(@Nonnull name: String?): WebhookAction?

    /**
     * Deletes a [Webhook][net.dv8tion.jda.api.entities.Webhook] attached to this channel
     * by the `id` specified.
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The provided id does not refer to a WebHook present in this channel, either due
     * to it not existing or having already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>if we were removed from the guild
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The send request was attempted after the account lost
     * [Permission.MANAGE_WEBHOOKS][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS] in the channel.
     *
     *
     * @param  id
     * The not-null id for the target Webhook.
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided `id` is `null` or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.MANAGE_WEBHOOKS][net.dv8tion.jda.api.Permission.MANAGE_WEBHOOKS] in this channel.
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteWebhookById(@Nonnull id: String?): AuditableRestAction<Void?>?
}
