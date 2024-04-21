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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.CompletedRestAction
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.Nonnull

/**
 * An object representing a reference in a Discord message.
 * @see Message.getMessageReference
 */
class MessageReference(
    /**
     * Returns the message id for this reference, or 0 if no message id was provided.
     *
     * @return The message id, or 0.
     */
    val messageIdLong: Long,
    /**
     * Returns the channel id for this reference, or 0 if no channel id was provided.
     *
     * @return The channel id, or 0.
     */
    val channelIdLong: Long,
    /**
     * Returns the guild id for this reference, or 0 if no guild id was provided.
     *
     * @return The guild id, or 0.
     */
    val guildIdLong: Long,
    /**
     * The resolved message, if available.
     *
     *
     * This will have different meaning depending on the [type][Message.getType] of message.
     * Usually, this is a [INLINE_REPLY][MessageType.INLINE_REPLY] reference.
     * This can be null even if the type is [INLINE_REPLY][MessageType.INLINE_REPLY], when the message it references doesn't exist or discord wasn't able to resolve it in time.
     *
     * @return The referenced message, or null if this is not available
     *
     * @see .resolve
     */
    var message: Message?,
    api: JDA
) {

    /**
     * Returns the JDA instance related to this message reference.
     *
     * @return The corresponding JDA instance
     */
    @get:Nonnull
    val jDA: JDA
    private var channel: MessageChannel? = null

    /**
     * The guild for this reference.
     * <br></br>This will be null if the message did not come from a guild, the guild was not provided, or JDA did not have the guild cached
     *
     * @return The guild, or null if this is not available
     *
     * @see .getGuildId
     */
    val guild: Guild?

    init {
        message = message
        if (guildIdLong == 0L) channel = api.getPrivateChannelById(channelIdLong) else channel = api.getChannelById(
            MessageChannel::class.java, channelIdLong
        )
        guild = api.getGuildById(guildIdLong) // is null if guildId = 0 anyway
        jDA = api
    }

    /**
     * Retrieves the referenced message for this message.
     * <br></br>If the message already exists, it will be returned immediately.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [TextChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     * in the [TextChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this reference refers to a [GuildChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.VOICE_CONNECT][net.dv8tion.jda.api.Permission.VOICE_CONNECT] (applicable if `getChannel().getType().isAudio()`)
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @throws java.lang.IllegalStateException
     * If this message reference does not have a channel
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [net.dv8tion.jda.api.entities.Message]
     */
    @Nonnull
    fun resolve(): RestAction<Message> {
        return resolve(true)
    }

    /**
     * Retrieves the referenced message for this message.
     * <br></br>If the message already exists, it will be returned immediately.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [TextChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     * in the [TextChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  update
     * Whether to update the already stored message
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this reference refers to a [GuildChannel][net.dv8tion.jda.api.entities.channel.middleman.GuildChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.VOICE_CONNECT][net.dv8tion.jda.api.Permission.VOICE_CONNECT] (applicable if `getChannel().getType().isAudio()`)
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @throws java.lang.IllegalStateException
     * If this message reference does not have a channel
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [net.dv8tion.jda.api.entities.Message]
     */
    @Nonnull
    fun resolve(update: Boolean): RestAction<Message> {
        checkPermission(Permission.VIEW_CHANNEL)
        checkPermission(Permission.MESSAGE_HISTORY)
        checkNotNull(channel) { "Cannot resolve a message without a channel present." }
        val jda = jDA as JDAImpl
        val referenced = message
        if (referenced != null && !update) return CompletedRestAction(jda, referenced)
        val route = Route.Messages.GET_MESSAGE.compile(getChannelId(), getMessageId())
        return RestActionImpl(jda, route) { response: Response, request: Request<Message>? ->
            // channel can be null for MessageReferences, but we've already checked for that above, so it is nonnull here
            val created: Message = jda.entityBuilder.createMessageWithChannel(response.getObject(), channel, false)
            message = created
            created
        }
    }

    /**
     * The channel from which this message originates.
     * <br></br>Messages from other guilds can be referenced, in which case JDA may not have the channel cached.
     *
     * @return The origin channel for this message reference, or null if this is not available
     *
     * @see .getChannelId
     */
    fun getChannel(): MessageChannelUnion? {
        return channel as MessageChannelUnion?
    }

    /**
     * Returns the message id for this reference, or 0 if no message id was provided.
     *
     * @return The message id, or 0.
     */
    @Nonnull
    fun getMessageId(): String {
        return java.lang.Long.toUnsignedString(messageIdLong)
    }

    /**
     * Returns the channel id for this reference, or 0 if no channel id was provided.
     *
     * @return The channel id, or 0.
     */
    @Nonnull
    fun getChannelId(): String {
        return java.lang.Long.toUnsignedString(channelIdLong)
    }

    /**
     * Returns the guild id for this reference, or 0 if no guild id was provided.
     *
     * @return The guild id, or 0.
     */
    @Nonnull
    fun getGuildId(): String {
        return java.lang.Long.toUnsignedString(guildIdLong)
    }

    private fun checkPermission(permission: Permission) {
        if (guild == null || channel !is GuildChannel) return
        val selfMember = guild.getSelfMember()
        val guildChannel = channel as GuildChannel
        Checks.checkAccess(selfMember, guildChannel)
        if (!selfMember!!.hasPermission(guildChannel, permission)) throw InsufficientPermissionException(
            guildChannel,
            permission
        )
    }
}
