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
package net.dv8tion.jda.api.entities.channel.middleman

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents all message channels present in guilds.
 *
 *
 * This includes channels that are not included in [StandardGuildMessageChannel], such as [ThreadChannel].
 *
 * @see StandardGuildMessageChannel
 */
interface GuildMessageChannel : GuildChannel, MessageChannel {
    override fun canTalk(): Boolean {
        return canTalk(getGuild().getSelfMember())
    }

    /**
     * Whether the specified [net.dv8tion.jda.api.entities.Member]
     * can send messages in this channel.
     * <br></br>Checks for both [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] and
     * [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND].
     *
     * @param  member
     * The Member to check
     *
     * @return True, if the specified member is able to read and send messages in this channel
     */
    fun canTalk(@Nonnull member: Member?): Boolean

    /**
     * Attempts to remove the reaction from a message represented by the specified `messageId`
     * in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the
     * [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [TextChannel]
     * <br></br>Also can happen if the account lost the [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_ADD_REACTION][net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION] in the
     * [TextChannel].
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided unicode character does not refer to a known emoji unicode character.
     * <br></br>Proper unicode characters for emojis can be found here:
     * [Emoji Table](https://unicode.org/emoji/charts/full-emoji-list.html)
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
     * The messageId to remove the reaction from
     * @param  emoji
     * The emoji to remove
     * @param  user
     * The target user of which to remove from
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided `emoji` is `null`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in this channel.
     *
     * @return [net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeReactionById(
        @Nonnull messageId: String?,
        @Nonnull emoji: Emoji?,
        @Nonnull user: User?
    ): RestAction<Void?>?

    /**
     * Attempts to remove the reaction from a message represented by the specified `messageId`
     * in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the
     * [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [TextChannel]
     * <br></br>Also can happen if the account lost the [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_ADD_REACTION][net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION] in the
     * [TextChannel].
     *
     *  * [net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided unicode character does not refer to a known emoji unicode character.
     * <br></br>Proper unicode characters for emojis can be found here:
     * [Emoji Table](https://unicode.org/emoji/charts/full-emoji-list.html)
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
     * The messageId to remove the reaction from
     * @param  emoji
     * The emoji to remove
     * @param  user
     * The target user of which to remove from
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided `emoji` is `null`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in this channel.
     *
     * @return [net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeReactionById(messageId: Long, @Nonnull emoji: Emoji?, @Nonnull user: User?): RestAction<Void?>? {
        return removeReactionById(java.lang.Long.toUnsignedString(messageId), emoji, user)
    }

    /**
     * Bulk deletes a list of messages.
     * **This is not the same as calling [net.dv8tion.jda.api.entities.Message.delete] in a loop.**
     * <br></br>This is much more efficient, but it has a different ratelimit. You may call this once per second per Guild.
     *
     *
     * Must be at least 2 messages and not be more than 100 messages at a time.
     * <br></br>If you only have 1 message, use the [net.dv8tion.jda.api.entities.Message.delete] method instead.
     *
     * <br></br>
     *
     *You must have the Permission [MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in this channel to use
     * this function.
     *
     *
     * This method is best used when using [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] to delete a large amount
     * of messages. If you have a large amount of messages but only their message Ids, please use [.deleteMessagesByIds]
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>if any of the provided messages does not exist
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>if we were removed from the guild
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The send request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the channel.
     *
     *
     * @param  messages
     * The collection of messages to delete.
     *
     * @throws IllegalArgumentException
     * If the size of the list less than 2 or more than 100 messages.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this account does not have [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     *
     * @see .deleteMessagesByIds
     * @see .purgeMessages
     */
    @Nonnull
    @CheckReturnValue
    fun deleteMessages(@Nonnull messages: Collection<Message?>): RestAction<Void?>? {
        Checks.notEmpty(messages, "Messages collection")
        return deleteMessagesByIds(messages.stream()
            .map { obj: Message? -> obj!!.id }
            .collect(Collectors.toList()))
    }

    /**
     * Bulk deletes a list of messages.
     * **This is not the same as calling [MessageChannel.deleteMessageById] in a loop.**
     * <br></br>This is much more efficient, but it has a different ratelimit. You may call this once per second per Guild.
     *
     *
     * Must be at least 2 messages and not be more than 100 messages at a time.
     * <br></br>If you only have 1 message, use the [net.dv8tion.jda.api.entities.Message.delete] method instead.
     *
     * <br></br>
     *
     *You must have [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in this channel to use
     * this function.
     *
     *
     * This method is best used when you have a large amount of messages but only their message Ids. If you are using
     * [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] or have [Message][net.dv8tion.jda.api.entities.Message]
     * objects, it would be easier to use [.deleteMessages].
     *
     *
     * Possible ErrorResponses include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>if any of the provided messages does not exist
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>if we were removed from the guild
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The send request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the channel.
     *
     *
     * @param  messageIds
     * The message ids for the messages to delete.
     *
     * @throws java.lang.IllegalArgumentException
     * If the size of the list less than 2 or more than 100 messages.
     * @throws java.lang.NumberFormatException
     * If any of the provided ids cannot be parsed by [Long.parseLong]
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this account does not have [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     *
     * @see .deleteMessages
     * @see .purgeMessagesById
     */
    @Nonnull
    @CheckReturnValue
    fun deleteMessagesByIds(@Nonnull messageIds: Collection<String>?): RestAction<Void?>?

    /**
     * Attempts to remove all reactions from a message with the specified `messageId` in this TextChannel
     * <br></br>This is useful for moderator commands that wish to remove all reactions at once from a specific message.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The clear-reactions request was attempted after the account lost access to the [TextChannel]
     * due to [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] being revoked, or the
     * account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed.
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The clear-reactions request was attempted after the account lost [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     * in the [TextChannel] when adding the reaction.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * The clear-reactions request was attempted after the Message had been deleted.
     *
     *
     * @param  messageId
     * The not-empty valid message id
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in this channel.
     * @throws java.lang.IllegalArgumentException
     * If the provided `id` is `null` or empty.
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun clearReactionsById(@Nonnull messageId: String?): RestAction<Void?>?

    /**
     * Attempts to remove all reactions from a message with the specified `messageId` in this TextChannel
     * <br></br>This is useful for moderator commands that wish to remove all reactions at once from a specific message.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The clear-reactions request was attempted after the account lost access to the [TextChannel]
     * due to [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] being revoked, or the
     * account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed.
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The clear-reactions request was attempted after the account lost [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     * in the [TextChannel] when adding the reaction.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * The clear-reactions request was attempted after the Message had been deleted.
     *
     *
     * @param  messageId
     * The message id
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the currently logged in account does not have
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in this channel.
     *
     * @return [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun clearReactionsById(messageId: Long): RestAction<Void?>? {
        return clearReactionsById(java.lang.Long.toUnsignedString(messageId))
    }

    /**
     * Removes all reactions for the specified emoji.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The currently logged in account lost access to the channel by either being removed from the guild
     * or losing the [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided [Emoji] was deleted or doesn't exist.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message was deleted.
     *
     *
     * @param  messageId
     * The id for the target message
     * @param  emoji
     * The [Emoji] to remove reactions for
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MESSAGE_MANAGE] in the channel
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun clearReactionsById(@Nonnull messageId: String?, @Nonnull emoji: Emoji?): RestAction<Void?>?

    /**
     * Removes all reactions for the specified emoji.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The currently logged in account lost access to the channel by either being removed from the guild
     * or losing the [VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] permission
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided [Emoji] was deleted or doesn't exist.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message was deleted.
     *
     *
     * @param  messageId
     * The id for the target message
     * @param  emoji
     * The [Emoji] to remove reactions for
     *
     * @throws InsufficientPermissionException
     * If the currently logged in account does not have [Permission.MESSAGE_MANAGE] in the channel
     * @throws IllegalArgumentException
     * If provided with null
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun clearReactionsById(messageId: Long, @Nonnull emoji: Emoji?): RestAction<Void?>? {
        return clearReactionsById(java.lang.Long.toUnsignedString(messageId), emoji)
    }

    /**
     * Send up to 3 stickers in this channel.
     * <br></br>Bots can only send [GuildStickers][GuildSticker] from the same [net.dv8tion.jda.api.entities.Guild].
     * Bots cannot use [StandardStickers][net.dv8tion.jda.api.entities.sticker.StandardSticker].
     *
     * @param  stickers
     * Collection of 1-3 stickers to send
     *
     * @throws MissingAccessException
     * If the currently logged in account does not have [Permission.VIEW_CHANNEL] in this channel
     * @throws InsufficientPermissionException
     *
     *  * If this is a [ThreadChannel] and the bot does not have [Permission.MESSAGE_SEND_IN_THREADS]
     *  * If this is not a [ThreadChannel] and the bot does not have [Permission.MESSAGE_SEND]
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the provided stickers is a [GuildSticker],
     * which is either [unavailable][GuildSticker.isAvailable] or from a different guild.
     *  * If the list is empty or has more than 3 stickers
     *  * If null is provided
     *
     *
     * @return [MessageCreateAction]
     *
     * @see Sticker.fromId
     */
    @Nonnull
    @CheckReturnValue
    fun sendStickers(@Nonnull stickers: Collection<StickerSnowflake?>?): MessageCreateAction?

    /**
     * Send up to 3 stickers in this channel.
     * <br></br>Bots can only send [GuildStickers][GuildSticker] from the same [net.dv8tion.jda.api.entities.Guild].
     * Bots cannot use [StandardStickers][net.dv8tion.jda.api.entities.sticker.StandardSticker].
     *
     * @param  stickers
     * The 1-3 stickers to send
     *
     * @throws MissingAccessException
     * If the currently logged in account does not have [Permission.VIEW_CHANNEL] in this channel
     * @throws InsufficientPermissionException
     *
     *  * If this is a [ThreadChannel] and the bot does not have [Permission.MESSAGE_SEND_IN_THREADS]
     *  * If this is not a [ThreadChannel] and the bot does not have [Permission.MESSAGE_SEND]
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the provided stickers is a [GuildSticker],
     * which is either [unavailable][GuildSticker.isAvailable] or from a different guild.
     *  * If the list is empty or has more than 3 stickers
     *  * If null is provided
     *
     *
     * @return [MessageCreateAction]
     *
     * @see Sticker.fromId
     */
    @Nonnull
    @CheckReturnValue
    fun sendStickers(@Nonnull vararg stickers: StickerSnowflake?): MessageCreateAction? {
        Checks.notEmpty(stickers, "Stickers")
        return sendStickers(Arrays.asList(*stickers))
    }
}
