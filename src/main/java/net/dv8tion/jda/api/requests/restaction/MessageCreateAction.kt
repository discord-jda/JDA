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
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.requests.FluentRestAction
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl
import net.dv8tion.jda.internal.utils.*
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Specialized [RestAction][net.dv8tion.jda.api.requests.RestAction] used for sending messages to [MessageChannels][MessageChannel].
 *
 * @see MessageChannel.sendMessage
 */
interface MessageCreateAction : MessageCreateRequest<MessageCreateAction?>,
    FluentRestAction<Message?, MessageCreateAction?> {
    /**
     * Unique string/number used to identify messages using [Message.getNonce] in [MessageReceivedEvent].
     *
     *
     * The nonce can be used for deduping messages and marking them for use with [MessageReceivedEvent].
     * JDA will automatically generate a unique nonce per message, it is not necessary to do this manually.
     *
     * @param  nonce
     * The nonce string to use, must be unique per message.
     * A unique nonce will be generated automatically if this is null.
     *
     * @throws IllegalArgumentException
     * If the provided nonce is longer than {@value Message#MAX_NONCE_LENGTH} characters
     *
     * @return The same instance for chaining
     *
     * @see [Cryptographic Nonce - Wikipedia](https://en.wikipedia.org/wiki/Cryptographic_nonce)
     */
    @Nonnull
    fun setNonce(nonce: String?): MessageCreateAction?

    /**
     * Message reference used for a reply.
     * <br></br>The client will show this message as a reply to the target message.
     *
     *
     * You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using [.mentionRepliedUser].
     *
     *
     * This also requires [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the channel.
     * If this permission is missing, you receive [ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM][net.dv8tion.jda.api.requests.ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM].
     *
     *
     * If the target message does not exist, this will result in [ErrorResponse.UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE].
     * You can use [.failOnInvalidReply] to allow unknown or deleted messages.
     *
     * @param  messageId
     * The target message id to reply to
     *
     * @throws IllegalArgumentException
     * If the message id is not a valid snowflake
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setMessageReference(messageId: String?): MessageCreateAction?

    /**
     * Message reference used for a reply.
     * <br></br>The client will show this message as a reply to the target message.
     *
     *
     * You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using [.mentionRepliedUser].
     *
     *
     * This also requires [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the channel.
     * If this permission is missing, you receive [ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM][net.dv8tion.jda.api.requests.ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM].
     *
     *
     * If the target message does not exist, this will result in [ErrorResponse.UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE].
     * You can use [.failOnInvalidReply] to allow unknown or deleted messages.
     *
     * @param  messageId
     * The target message id to reply to
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setMessageReference(messageId: Long): MessageCreateAction? {
        return setMessageReference(java.lang.Long.toUnsignedString(messageId))
    }

    /**
     * Message reference used for a reply.
     * <br></br>The client will show this message as a reply to the target message.
     *
     *
     * You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using [.mentionRepliedUser].
     *
     *
     * This also requires [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the channel.
     * If this permission is missing, you receive [ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM][net.dv8tion.jda.api.requests.ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM].
     *
     *
     * If the target message does not exist, this will result in [ErrorResponse.UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE].
     * You can use [.failOnInvalidReply] to allow unknown or deleted messages.
     *
     * @param  message
     * The target message to reply to
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setMessageReference(message: Message?): MessageCreateAction? {
        return setMessageReference(message?.id)
    }

    /**
     * Whether to throw a exception if the referenced message does not exist, when replying to a message.
     * <br></br>This only matters in combination with [.setMessageReference] and [.setMessageReference]!
     *
     *
     * This is false by default but can be configured using [.setDefaultFailOnInvalidReply]!
     *
     * @param  fail
     * True, to throw a exception if the referenced message does not exist
     *
     * @return Updated MessageCreateAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun failOnInvalidReply(fail: Boolean): MessageCreateAction?

    /**
     * Set the stickers to send alongside this message.
     * <br></br>This is not supported for message edits.
     *
     * @param  stickers
     * The stickers to send, or null to not send any stickers
     *
     * @throws IllegalStateException
     * If this request is a message edit request
     * @throws IllegalArgumentException
     *
     *  * If any of the provided stickers is a [GuildSticker],
     * which is either [unavailable][GuildSticker.isAvailable] or from a different guild.
     *  * If the collection has more than {@value Message#MAX_STICKER_COUNT} stickers
     *  * If a collection with null entries is provided
     *
     *
     * @return Updated MessageCreateAction for chaining convenience
     *
     * @see Sticker.fromId
     */
    @Nonnull
    @CheckReturnValue
    fun setStickers(stickers: Collection<StickerSnowflake?>?): MessageCreateAction?

    /**
     * Set the stickers to send alongside this message.
     * <br></br>This is not supported for message edits.
     *
     * @param  stickers
     * The stickers to send, or null to not send any stickers
     *
     * @throws IllegalStateException
     * If this request is a message edit request
     * @throws IllegalArgumentException
     *
     *  * If any of the provided stickers is a [GuildSticker],
     * which is either [unavailable][GuildSticker.isAvailable] or from a different guild.
     *  * If the collection has more than {@value Message#MAX_STICKER_COUNT} stickers
     *  * If a collection with null entries is provided
     *
     *
     * @return Updated MessageCreateAction for chaining convenience
     *
     * @see Sticker.fromId
     */
    @Nonnull
    @CheckReturnValue
    fun setStickers(vararg stickers: StickerSnowflake?): MessageCreateAction? {
        if (stickers != null) Checks.noneNull(stickers, "Sticker")
        return setStickers(if (stickers == null) null else Arrays.asList(*stickers))
    }

    companion object {
        /**
         * Sets the default value for [.failOnInvalidReply]
         *
         *
         * Default: **false**
         *
         * @param fail
         * True, to throw a exception if the referenced message does not exist
         */
        fun setDefaultFailOnInvalidReply(fail: Boolean) {
            MessageCreateActionImpl.setDefaultFailOnInvalidReply(fail)
        }
    }
}
