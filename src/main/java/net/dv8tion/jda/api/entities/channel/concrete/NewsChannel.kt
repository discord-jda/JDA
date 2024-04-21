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
package net.dv8tion.jda.api.entities.channel.concrete

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Webhook.WebhookReference
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.channel.ChannelManager
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents [net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel] that are News Channels.
 *
 *
 * The Discord client may refer to these as Announcement Channels.
 *
 *
 * Members can subscribe channels in their own guilds to receive messages crossposted from this channel.
 * This is referred to as following this channel.
 *
 *
 * Messages sent in this channel can be crossposted, at which point they will be sent (via webhook) to all subscribed channels.
 *
 * @see Message.getFlags
 * @see net.dv8tion.jda.api.entities.Message.MessageFlag.CROSSPOSTED
 */
interface NewsChannel : StandardGuildMessageChannel {
    /**
     * Subscribes to the crossposted messages in this channel.
     * <br></br>This will create a [Webhook] of type [FOLLOWER][WebhookType.FOLLOWER] in the target channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the target channel doesn't exist or is not visible to the currently logged in account
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>If the currently logged in account does not have [Permission.MANAGE_WEBHOOKS] in the **target channel**
     *
     *  * [MAX_WEBHOOKS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_WEBHOOKS]
     * <br></br>If the target channel already has reached the maximum capacity for webhooks
     *
     *
     * @param  targetChannelId
     * The target channel id
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [RestAction]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun follow(@Nonnull targetChannelId: String?): RestAction<WebhookReference?>?

    /**
     * Subscribes to the crossposted messages in this channel.
     * <br></br>This will create a [Webhook] of type [FOLLOWER][WebhookType.FOLLOWER] in the target channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the target channel doesn't exist or not visible to the currently logged in account
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>If the currently logged in account does not have [Permission.MANAGE_WEBHOOKS] in the **target channel**
     *
     *  * [MAX_WEBHOOKS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_WEBHOOKS]
     * <br></br>If the target channel already has reached the maximum capacity for webhooks
     *
     *
     * @param  targetChannelId
     * The target channel id
     *
     * @return [RestAction]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun follow(targetChannelId: Long): RestAction<WebhookReference?>? {
        return follow(java.lang.Long.toUnsignedString(targetChannelId))
    }

    /**
     * Subscribes to the crossposted messages in this channel.
     * <br></br>This will create a [Webhook] of type [FOLLOWER][WebhookType.FOLLOWER] in the target channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>If the target channel doesn't exist or not visible to the currently logged in account
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>If the currently logged in account does not have [Permission.MANAGE_WEBHOOKS] in the **target channel**
     *
     *  * [MAX_WEBHOOKS][net.dv8tion.jda.api.requests.ErrorResponse.MAX_WEBHOOKS]
     * <br></br>If the target channel already has reached the maximum capacity for webhooks
     *
     *
     * @param  targetChannel
     * The target channel
     *
     * @throws MissingAccessException
     * If the currently logged in account does not have [access][Member.hasAccess] in the **target channel**.
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MANAGE_WEBHOOKS] in the **target channel**.
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [RestAction]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun follow(@Nonnull targetChannel: TextChannel): RestAction<WebhookReference?>? {
        Checks.notNull(targetChannel, "Target Channel")
        val selfMember = targetChannel.getGuild().getSelfMember()
        Checks.checkAccess(selfMember, targetChannel)
        if (!selfMember.hasPermission(targetChannel, Permission.MANAGE_WEBHOOKS)) throw InsufficientPermissionException(
            targetChannel,
            Permission.MANAGE_WEBHOOKS
        )
        return follow(targetChannel.id)
    }

    /**
     * Attempts to crosspost the provided message.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [ALREADY_CROSSPOSTED][net.dv8tion.jda.api.requests.ErrorResponse.ALREADY_CROSSPOSTED]
     * <br></br>The target message has already been crossposted.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the
     * [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [TextChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the TextChannel.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The messageId to crosspost
     *
     * @throws java.lang.IllegalArgumentException
     * If provided `messageId` is `null` or empty.
     * @throws MissingAccessException
     * If the currently logged in account does not have [access][Member.hasAccess] in this channel.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] in this channel.
     *
     * @return [net.dv8tion.jda.api.requests.RestAction] - Type: [Message]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun crosspostMessageById(@Nonnull messageId: String?): RestAction<Message?>? {
        Checks.isSnowflake(messageId)
        Checks.checkAccess(getGuild().getSelfMember(), this)
        val route = Route.Messages.CROSSPOST_MESSAGE.compile(id, messageId)
        return RestActionImpl(
            getJDA(), route
        ) { response: Response, request: Request<Message?> ->
            request.jda.entityBuilder.createMessageWithChannel(
                response.getObject(),
                this,
                false
            )
        }
    }

    /**
     * Attempts to crosspost the provided message.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [ALREADY_CROSSPOSTED][net.dv8tion.jda.api.requests.ErrorResponse.ALREADY_CROSSPOSTED]
     * <br></br>The target message has already been crossposted.
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the
     * [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [TextChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the TextChannel.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The messageId to crosspost
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] in this channel.
     *
     * @return [net.dv8tion.jda.api.requests.RestAction] - Type: [Message]
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    fun crosspostMessageById(messageId: Long): RestAction<Message?>? {
        return crosspostMessageById(java.lang.Long.toUnsignedString(messageId))
    }

    @Nonnull
    override fun createCopy(@Nonnull guild: Guild?): ChannelAction<NewsChannel?>?
    @Nonnull
    override fun createCopy(): ChannelAction<NewsChannel?>? {
        return createCopy(getGuild())
    }

    @get:Nonnull
    abstract override val manager: ChannelManager<*, *>?
}
