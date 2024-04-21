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
import net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl
import net.dv8tion.jda.internal.requests.restaction.MessageEditActionImpl
import net.dv8tion.jda.internal.requests.restaction.pagination.MessagePaginationActionImpl
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a Discord channel that can have [Messages][net.dv8tion.jda.api.entities.Message] and files sent to it.
 *
 *
 * **Formattable**<br></br>
 * This interface extends [Formattable][java.util.Formattable] and can be used with a [Formatter][java.util.Formatter]
 * such as used by [String.format(String, Object...)][String.format]
 * or [PrintStream.printf(String, Object...)][java.io.PrintStream.printf].
 *
 *
 * This will use [.getName] rather than [Object.toString]!
 * <br></br>Supported Features:
 *
 *  * **Alternative**
 * <br></br>   - Prepends the name with `#`
 * (Example: `%#s` - results in `#[.getName]`)
 *
 *  * **Width/Left-Justification**
 * <br></br>   - Ensures the size of a format
 * (Example: `%20s` - uses at minimum 20 chars;
 * `%-10s` - uses left-justified padding)
 *
 *  * **Precision**
 * <br></br>   - Cuts the content to the specified size
 * (Example: `%.20s`)
 *
 *
 *
 * More information on formatting syntax can be found in the [format syntax documentation][java.util.Formatter]!
 * <br></br>**[GuildMessageChannel] is a special case which uses [IMentionable.getAsMention()][net.dv8tion.jda.api.entities.IMentionable.getAsMention]
 * by default and uses the `#[.getName]` format as <u>alternative</u>**
 *
 * @see TextChannel
 *
 * @see PrivateChannel
 */
interface MessageChannel : Channel, Formattable {
    @get:Nonnull
    val latestMessageId: String?
        /**
         * The id for the most recent message sent
         * in this current MessageChannel.
         *
         *
         * This value is updated on each [MessageReceivedEvent][net.dv8tion.jda.api.events.message.MessageReceivedEvent]
         * and <u>**the value might point to an already deleted message since the ID is not cleared when the message is deleted,
         * so calling [.retrieveMessageById] with this id can result in an [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE] error**</u>
         *
         * @return The most recent message's id or "0" if no messages are present
         */
        get() = java.lang.Long.toUnsignedString(latestMessageIdLong)

    /**
     * The id for the most recent message sent
     * in this current MessageChannel.
     *
     *
     * This value is updated on each [MessageReceivedEvent][net.dv8tion.jda.api.events.message.MessageReceivedEvent]
     * and <u>**the value might point to an already deleted message since the value is not cleared when the message is deleted,
     * so calling [.retrieveMessageById] with this id can result in an [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE] error**</u>
     *
     * @return The most recent message's id or 0 if no messages are present
     */
    @JvmField
    val latestMessageIdLong: Long

    /**
     * Whether the currently logged in user can send messages in this channel or not.
     * <br></br>For [GuildMessageChannel] this method checks for
     * both [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * and [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND].
     * <br></br>For [ThreadChannel] this method checks for [net.dv8tion.jda.api.Permission.MESSAGE_SEND_IN_THREADS] instead of [net.dv8tion.jda.api.Permission.MESSAGE_SEND].
     * <br></br>For [PrivateChannel] this method checks if the user that this PrivateChannel communicates with is not a bot,
     * but it does **not** check if the said user blocked the currently logged in user or have their DMs disabled.
     *
     * @return True, if we are able to read and send messages in this channel
     */
    fun canTalk(): Boolean

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br></br>This combines both [GuildMessageChannel.deleteMessagesByIds] as well as [.deleteMessageById]
     * to delete all messages provided. No checks will be done to prevent failures, use [java.util.concurrent.CompletionStage.exceptionally]
     * to handle failures.
     *
     *
     * For possible ErrorResponses see [.purgeMessagesById].
     *
     * @param  messageIds
     * The message ids to delete
     *
     * @return List of futures representing all deletion tasks
     *
     * @see CompletableFuture.allOf
     */
    @Nonnull
    fun purgeMessagesById(@Nonnull messageIds: List<String?>?): List<CompletableFuture<Void?>?>? {
        if (messageIds == null || messageIds.isEmpty()) return emptyList<CompletableFuture<Void?>>()
        val ids = LongArray(messageIds.size)
        for (i in ids.indices) ids[i] = MiscUtil.parseSnowflake(messageIds[i])
        return purgeMessagesById(*ids)
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br></br>This combines both [GuildMessageChannel.deleteMessagesByIds] as well as [.deleteMessageById]
     * to delete all messages provided. No checks will be done to prevent failures, use [java.util.concurrent.CompletionStage.exceptionally]
     * to handle failures.
     *
     *
     * For possible ErrorResponses see [.purgeMessagesById].
     *
     * @param  messageIds
     * The message ids to delete
     *
     * @return List of futures representing all deletion tasks
     *
     * @see CompletableFuture.allOf
     */
    @Nonnull
    fun purgeMessagesById(@Nonnull vararg messageIds: String?): List<CompletableFuture<Void?>?>? {
        return if (messageIds == null || messageIds.size == 0) emptyList<CompletableFuture<Void?>>() else purgeMessagesById(
            Arrays.asList(*messageIds)
        )
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br></br>This combines both [GuildMessageChannel.deleteMessagesByIds] as well as [net.dv8tion.jda.api.entities.Message.delete]
     * to delete all messages provided. No checks will be done to prevent failures, use [java.util.concurrent.CompletionStage.exceptionally]
     * to handle failures.
     *
     *
     * For possible ErrorResponses see [.purgeMessagesById].
     *
     * @param  messages
     * The messages to delete
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If one of the provided messages is from another user and cannot be deleted due to permissions
     * @throws IllegalArgumentException
     * If one of the provided messages is from another user and cannot be deleted because this is not in a guild
     *
     * @return List of futures representing all deletion tasks
     *
     * @see CompletableFuture.allOf
     */
    @Nonnull
    fun purgeMessages(@Nonnull vararg messages: Message?): List<CompletableFuture<Void?>?>? {
        return if (messages == null || messages.size == 0) emptyList<CompletableFuture<Void?>>() else purgeMessages(
            Arrays.asList(*messages)
        )
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br></br>This combines both [GuildMessageChannel.deleteMessagesByIds] as well as [Message.delete]
     * to delete all messages provided. No checks will be done to prevent failures, use [java.util.concurrent.CompletionStage.exceptionally]
     * to handle failures.
     *
     *
     * Any messages that cannot be deleted, as suggested by [net.dv8tion.jda.api.entities.MessageType.canDelete], will be filtered out before making any requests.
     *
     *
     * For possible ErrorResponses see [.purgeMessagesById].
     *
     * @param  messages
     * The messages to delete
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If one of the provided messages is from another user and cannot be deleted due to permissions
     * @throws IllegalArgumentException
     * If one of the provided messages is from another user and cannot be deleted because this is not in a guild
     *
     * @return List of futures representing all deletion tasks
     *
     * @see CompletableFuture.allOf
     */
    @Nonnull
    fun purgeMessages(@Nonnull messages: List<Message>?): List<CompletableFuture<Void?>?>? {
        return if (messages == null || messages.isEmpty()) emptyList<CompletableFuture<Void?>>() else purgeMessagesById(
            *messages.stream()
                .filter { m: Message -> m.type.canDelete() }
                .mapToLong { obj: Message -> obj.idLong }
                .toArray())
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br></br>This combines both [GuildMessageChannel.deleteMessagesByIds] as well as [.deleteMessageById]
     * to delete all messages provided. No checks will be done to prevent failures, use [java.util.concurrent.CompletionStage.exceptionally]
     * to handle failures.
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
     * <br></br>if we were removed from the channel
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The send request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the channel.
     *
     *
     * @param  messageIds
     * The message ids to delete
     *
     * @return List of futures representing all deletion tasks
     *
     * @see CompletableFuture.allOf
     */
    @Nonnull
    fun purgeMessagesById(@Nonnull vararg messageIds: Long): List<CompletableFuture<Void?>?>? {
        if (messageIds == null || messageIds.size == 0) return emptyList<CompletableFuture<Void?>>()
        val list: MutableList<CompletableFuture<Void?>?> = ArrayList(messageIds.size)
        val sortedIds = TreeSet(Comparator.reverseOrder<Long>())
        for (messageId in messageIds) sortedIds.add(messageId)
        for (messageId in sortedIds) list.add(deleteMessageById(messageId).submit())
        return list
    }

    /**
     * Send a message to this channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  text
     * The message content
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If the content is null or longer than {@value Message#MAX_CONTENT_LENGTH} characters
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessage(@Nonnull text: CharSequence): MessageCreateAction? {
        Checks.notNull(text, "Content")
        return MessageCreateActionImpl(this).setContent(text.toString())
    }

    /**
     * Send a message to this channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  msg
     * The [MessageCreateData] to send
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageCreateAction]
     *
     * @see net.dv8tion.jda.api.utils.messages.MessageCreateBuilder MessageCreateBuilder
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessage(@Nonnull msg: MessageCreateData?): MessageCreateAction? {
        Checks.notNull(msg, "Message")
        return MessageCreateActionImpl(this).applyData(msg!!)
    }

    /**
     * Send a message to this channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  format
     * Format string for the message content
     * @param  args
     * Format arguments for the content
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If the format string is null or the resulting content is longer than {@value Message#MAX_CONTENT_LENGTH} characters
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     * @throws java.util.IllegalFormatException
     * If a format string contains an illegal syntax, a format
     * specifier that is incompatible with the given arguments,
     * insufficient arguments given the format string, or other
     * illegal conditions.  For specification of all possible
     * formatting errors, see the [Details](../util/Formatter.html#detail) section of the
     * formatter class specification.
     *
     * @return [MessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageFormat(@Nonnull format: String?, @Nonnull vararg args: Any?): MessageCreateAction? {
        Checks.notEmpty(format, "Format")
        return sendMessage(String.format(format!!, *args))
    }

    /**
     * Send a message to this channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * channel.sendMessageEmbeds(embed) // send the embed
     * .addFiles(file) // add the file as attachment
     * .queue();
    `</pre> *
     *
     * @param  embed
     * [MessageEmbed] to send
     * @param  other
     * Additional [MessageEmbeds][MessageEmbed] to use (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than [MessageEmbed.EMBED_MAX_LENGTH_BOT].
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageEmbeds(@Nonnull embed: MessageEmbed, @Nonnull vararg other: MessageEmbed?): MessageCreateAction? {
        Checks.notNull(embed, "MessageEmbeds")
        Checks.noneNull(other, "MessageEmbeds")
        val embeds: MutableList<MessageEmbed> = ArrayList(1 + other.size)
        embeds.add(embed)
        Collections.addAll(embeds, *other)
        return MessageCreateActionImpl(this).setEmbeds(embeds)
    }

    /**
     * Send a message to this channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * channel.sendMessageEmbeds(Collections.singleton(embed)) // send the embeds
     * .addFiles(file) // add the file as attachment
     * .queue();
    `</pre> *
     *
     * @param  embeds
     * [MessageEmbeds][MessageEmbed] to use (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than [MessageEmbed.EMBED_MAX_LENGTH_BOT].
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): MessageCreateAction? {
        return MessageCreateActionImpl(this).setEmbeds(embeds!!)
    }

    /**
     * Send a message to this channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  component
     * [LayoutComponent] to send
     * @param  other
     * Additional [LayoutComponents][LayoutComponent] to use (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If any of the components is null or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageComponents(
        @Nonnull component: LayoutComponent,
        @Nonnull vararg other: LayoutComponent?
    ): MessageCreateAction? {
        Checks.notNull(component, "LayoutComponents")
        Checks.noneNull(other, "LayoutComponents")
        val components: MutableList<LayoutComponent> = ArrayList(1 + other.size)
        components.add(component)
        Collections.addAll(components, *other)
        return MessageCreateActionImpl(this).setComponents(components)
    }

    /**
     * Send a message to this channel.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * channel.sendMessageEmbeds(Collections.singleton(embed)) // send the embeds
     * .addFiles(file) // add the file as attachment
     * .queue();
    `</pre> *
     *
     * @param  components
     * [LayoutComponents][LayoutComponent] to use (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If any of the components is null or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageComponents(@Nonnull components: Collection<LayoutComponent?>?): MessageCreateAction? {
        return MessageCreateActionImpl(this).setComponents(components!!)
    }

    /**
     * Send a message to this channel.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel][net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If the total sum of uploaded bytes exceeds the guild's [upload limit][Guild.getMaxFileSize]
     *
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * channel.sendFiles(Collections.singleton(file)) // send the file upload
     * .addEmbeds(embed) // add the embed you want to reference the file with
     * .queue();
    `</pre> *
     *
     * @param  files
     * The [FileUploads][FileUpload] to attach to the message
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageCreateAction]
     *
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun sendFiles(@Nonnull files: Collection<FileUpload?>?): MessageCreateAction? {
        Checks.notEmpty(files, "File Collection")
        Checks.noneNull(files, "Files")
        return MessageCreateActionImpl(this).addFiles(files!!)
    }

    /**
     * Send a message to this channel.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>if this channel was deleted
     *
     *  * [CANNOT_SEND_TO_USER][net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER]
     * <br></br>If this is a [PrivateChannel] and the currently logged in account
     * does not share any Guilds with the recipient User
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If the total sum of uploaded bytes exceeds the guild's [upload limit][Guild.getMaxFileSize]
     *
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * channel.sendFiles(file) // send the file upload
     * .addEmbeds(embed) // add the embed you want to reference the file with
     * .queue();
    `</pre> *
     *
     * @param  files
     * The [FileUploads][FileUpload] to attach to the message
     *
     * @throws UnsupportedOperationException
     * If this is a [PrivateChannel] and the recipient is a bot
     * @throws IllegalArgumentException
     * If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageCreateAction]
     *
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun sendFiles(@Nonnull vararg files: FileUpload?): MessageCreateAction? {
        Checks.notEmpty(files, "File Collection")
        Checks.noneNull(files, "Files")
        return sendFiles(Arrays.asList(*files))
    }

    /**
     * Attempts to get a [Message][net.dv8tion.jda.api.entities.Message] from the Discord's servers that has
     * the same id as the id provided.
     * <br></br>Note: when retrieving a Message, you must retrieve it from the channel it was sent in!
     *
     *
     * The [Message.getMember()][Message.getMember] method will always return null for the resulting message.
     * To retrieve the member you can use `getGuild().retrieveMember(message.getAuthor())`.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     * in the [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `id` does not refer to a message sent in this channel or the message has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the sought after Message
     *
     * @throws IllegalArgumentException
     * if the provided `messageId` is null or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Message
     * <br></br>The Message defined by the provided id.
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveMessageById(@Nonnull messageId: String?): RestAction<Message?>? {
        Checks.isSnowflake(messageId, "Message ID")
        val jda = getJDA() as JDAImpl
        val route = Route.Messages.GET_MESSAGE.compile(id, messageId)
        return RestActionImpl(
            jda, route
        ) { response: Response, request: Request<Message?>? ->
            jda.entityBuilder.createMessageWithChannel(
                response.getObject(),
                this@MessageChannel,
                false
            )
        }
    }

    /**
     * Attempts to get a [Message][net.dv8tion.jda.api.entities.Message] from the Discord's servers that has
     * the same id as the id provided.
     * <br></br>Note: when retrieving a Message, you must retrieve it from the channel it was sent in!
     *
     *
     * The [Message.getMember()][Message.getMember] method will always return null for the resulting message.
     * To retrieve the member you can use `getGuild().retrieveMember(message.getAuthor())`.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     * in the [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `id` does not refer to a message sent in this channel or the message has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the sought after Message
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Message
     * <br></br>The Message defined by the provided id.
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveMessageById(messageId: Long): RestAction<Message?>? {
        return retrieveMessageById(java.lang.Long.toUnsignedString(messageId))
    }

    /**
     * Attempts to delete a [Message][net.dv8tion.jda.api.entities.Message] from the Discord servers that has
     * the same id as the id provided.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request attempted to delete a Message in a [GuildMessageChannel]
     * that was not sent by the currently logged in account.
     *
     *  * [INVALID_DM_ACTION][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_DM_ACTION]
     * <br></br>Attempted to delete a Message in a [PrivateChannel]
     * that was not sent by the currently logged in account.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `id` does not refer to a message sent in this channel or the message has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the Message that should be deleted
     *
     * @throws IllegalArgumentException
     * if the provided messageId is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL].
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    fun deleteMessageById(@Nonnull messageId: String?): AuditableRestAction<Void?> {
        Checks.isSnowflake(messageId, "Message ID")
        val route = Route.Messages.DELETE_MESSAGE.compile(id, messageId)
        return AuditableRestActionImpl(getJDA(), route)
    }

    /**
     * Attempts to delete a [Message][net.dv8tion.jda.api.entities.Message] from the Discord servers that has
     * the same id as the id provided.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request attempted to delete a Message in a [GuildMessageChannel]
     * that was not sent by the currently logged in account.
     *
     *  * [INVALID_DM_ACTION][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_DM_ACTION]
     * <br></br>Attempted to delete a Message in a [PrivateChannel]
     * that was not sent by the currently logged in account.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `id` does not refer to a message sent in this channel or the message has already been deleted.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the Message that should be deleted
     *
     * @throws IllegalArgumentException
     * if the provided messageId is not positive
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL].
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    fun deleteMessageById(messageId: Long): AuditableRestAction<Void?> {
        return deleteMessageById(java.lang.Long.toUnsignedString(messageId))
    }

    val history: MessageHistory?
        /**
         * Creates a new [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object for each call of this method.
         * <br></br>MessageHistory is **NOT** an internal message cache, but rather it queries the Discord servers for previously sent messages.
         *
         * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
         * If this is a [GuildMessageChannel]
         * and the currently logged in account does not have the permission [MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
         *
         * @return A [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] related to this channel.
         */
        get() = MessageHistory(this)

    @get:CheckReturnValue
    @get:Nonnull
    val iterableHistory: MessagePaginationAction?
        /**
         * A [PaginationAction] implementation
         * that allows to [iterate][Iterable] over recent [Messages][net.dv8tion.jda.api.entities.Message] of
         * this MessageChannel.
         * <br></br>This is **not** a cache for received messages and it can only view messages that were sent
         * before. This iterates chronologically backwards (from present to past).
         *
         *
         * **<u>It is recommended not to use this in an enhanced for-loop without end conditions as it might cause memory
         * overflows in channels with a long message history.</u>**
         *
         *
         * **Examples**<br></br>
         * <pre>`public CompletableFuture<List<Message>> getMessagesByUser(MessageChannel channel, User user) {
         * return channel.getIterableHistory()
         * .takeAsync(1000) // Collect 1000 messages
         * .thenApply(list ->
         * list.stream()
         * .filter(m -> m.getAuthor().equals(user)) // Filter messages by author
         * .collect(Collectors.toList())
         * );
         * }
        `</pre> *
         *
         * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
         * If this is a [GuildMessageChannel]
         * and the currently logged in account does not have the permission [MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
         *
         * @return [MessagePaginationAction]
         */
        get() = MessagePaginationActionImpl(this)

    /**
     * Uses the provided `id` of a message as a marker and retrieves messages sent around
     * the marker. The `limit` determines the amount of messages retrieved near the marker. Discord will
     * attempt to evenly split the limit between before and after the marker, however in the case that the marker is set
     * near the beginning or near the end of the channel's history the amount of messages on each side of the marker may
     * be different, and their total count may not equal the provided `limit`.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryAround(messageId, 100)` - This will retrieve 100 messages from history, 50 before the marker
     * and 50 after the marker.
     *
     *
     * Retrieve 10 messages near the end of history. Provided id is for a message that is the 3rd most recent message.
     * <br></br>`getHistoryAround(messageId, 10)` - This will retrieve 10 messages from history, 8 before the marker
     * and 2 after the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the message that will act as a marker.
     * @param  limit
     * The amount of messages to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * Provided `messageId` is `null` or empty.
     *  * Provided `limit` is less than `1` or greater than `100`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages around the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryAround
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryAround(@Nonnull messageId: String?, limit: Int): MessageRetrieveAction? {
        return MessageHistory.getHistoryAround(this, messageId!!).limit(limit)
    }

    /**
     * Uses the provided `id` of a message as a marker and retrieves messages around
     * the marker. The `limit` determines the amount of messages retrieved near the marker. Discord will
     * attempt to evenly split the limit between before and after the marker, however in the case that the marker is set
     * near the beginning or near the end of the channel's history the amount of messages on each side of the marker may
     * be different, and their total count may not equal the provided `limit`.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryAround(messageId, 100)` - This will retrieve 100 messages from history, 50 before the marker
     * and 50 after the marker.
     *
     *
     * Retrieve 10 messages near the end of history. Provided id is for a message that is the 3rd most recent message.
     * <br></br>`getHistoryAround(messageId, 10)` - This will retrieve 10 messages from history, 8 before the marker
     * and 2 after the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the message that will act as a marker. The id must refer to a message from this MessageChannel.
     * @param  limit
     * The amount of messages to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * Provided `messageId` is not positive.
     *  * Provided `limit` is less than `1` or greater than `100`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages around the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryAround
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryAround(messageId: Long, limit: Int): MessageRetrieveAction? {
        return getHistoryAround(java.lang.Long.toUnsignedString(messageId), limit)
    }

    /**
     * Uses the provided [Message][net.dv8tion.jda.api.entities.Message] as a marker and retrieves messages around
     * the marker. The `limit` determines the amount of messages retrieved near the marker. Discord will
     * attempt to evenly split the limit between before and after the marker, however in the case that the marker is set
     * near the beginning or near the end of the channel's history the amount of messages on each side of the marker may
     * be different, and their total count may not equal the provided `limit`.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryAround(message, 100)` - This will retrieve 100 messages from history, 50 before the marker
     * and 50 after the marker.
     *
     *
     * Retrieve 10 messages near the end of history. Provided message is the 3rd most recent message.
     * <br></br>`getHistoryAround(message, 10)` - This will retrieve 10 messages from history, 8 before the marker
     * and 2 after the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `message` has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  message
     * The [Message][net.dv8tion.jda.api.entities.Message] that will act as a marker. The provided Message
     * must be from this MessageChannel.
     * @param  limit
     * The amount of messages to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * Provided `message` is `null`.
     *  * Provided `limit` is less than `1` or greater than `100`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages around the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryAround
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryAround(@Nonnull message: Message, limit: Int): MessageRetrieveAction? {
        Checks.notNull(message, "Provided target message")
        return getHistoryAround(message.id, limit)
    }

    /**
     * Uses the provided `id` of a message as a marker and retrieves messages sent after
     * the marker ID. The `limit` determines the amount of messages retrieved near the marker.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryAfter(messageId, 100)` - This will retrieve 100 messages from history sent after the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the message that will act as a marker.
     * @param  limit
     * The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * Provided `messageId` is `null` or empty.
     *  * Provided `limit` is less than `1` or greater than `100`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages after the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryAfter
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryAfter(@Nonnull messageId: String?, limit: Int): MessageRetrieveAction? {
        return MessageHistory.getHistoryAfter(this, messageId!!).limit(limit)
    }

    /**
     * Uses the provided `id` of a message as a marker and retrieves messages sent after
     * the marker ID. The `limit` determines the amount of messages retrieved near the marker.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryAfter(messageId, 100)` - This will retrieve 100 messages from history sent after the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the message that will act as a marker.
     * @param  limit
     * The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     * Provided `limit` is less than `1` or greater than `100`.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages after the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryAfter
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryAfter(messageId: Long, limit: Int): MessageRetrieveAction? {
        return getHistoryAfter(java.lang.Long.toUnsignedString(messageId), limit)
    }

    /**
     * Uses the provided message as a marker and retrieves messages sent after
     * the marker. The `limit` determines the amount of messages retrieved near the marker.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryAfter(message, 100)` - This will retrieve 100 messages from history sent after the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  message
     * The message that will act as a marker.
     * @param  limit
     * The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * Provided `message` is `null`.
     *  * Provided `limit` is less than `1` or greater than `100`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages after the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryAfter
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryAfter(@Nonnull message: Message, limit: Int): MessageRetrieveAction? {
        Checks.notNull(message, "Message")
        return getHistoryAfter(message.id, limit)
    }

    /**
     * Uses the provided `id` of a message as a marker and retrieves messages sent before
     * the marker ID. The `limit` determines the amount of messages retrieved near the marker.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryBefore(messageId, 100)` - This will retrieve 100 messages from history sent before the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the message that will act as a marker.
     * @param  limit
     * The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * Provided `messageId` is `null` or empty.
     *  * Provided `limit` is less than `1` or greater than `100`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages before the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryBefore
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryBefore(@Nonnull messageId: String?, limit: Int): MessageRetrieveAction? {
        return MessageHistory.getHistoryBefore(this, messageId!!).limit(limit)
    }

    /**
     * Uses the provided `id` of a message as a marker and retrieves messages sent before
     * the marker ID. The `limit` determines the amount of messages retrieved near the marker.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryBefore(messageId, 100)` - This will retrieve 100 messages from history sent before the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  messageId
     * The id of the message that will act as a marker.
     * @param  limit
     * The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * Provided `messageId` is `null` or empty.
     *  * Provided `limit` is less than `1` or greater than `100`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages before the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryBefore
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryBefore(messageId: Long, limit: Int): MessageRetrieveAction? {
        return getHistoryBefore(java.lang.Long.toUnsignedString(messageId), limit)
    }

    /**
     * Uses the provided message as a marker and retrieves messages sent before
     * the marker. The `limit` determines the amount of messages retrieved near the marker.
     *
     *
     * **Examples:**
     * <br></br>Retrieve 100 messages from the middle of history. &gt;100 message exist in history and the marker is &gt;50 messages
     * from the edge of history.
     * <br></br>`getHistoryAfter(message, 100)` - This will retrieve 100 messages from history sent before the marker.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted, thus could not be used as a marker.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  message
     * The message that will act as a marker.
     * @param  limit
     * The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * Provided `message` is `null`.
     *  * Provided `limit` is less than `1` or greater than `100`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with messages before the provided message loaded into it.
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryBefore
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryBefore(@Nonnull message: Message, limit: Int): MessageRetrieveAction? {
        Checks.notNull(message, "Message")
        return getHistoryBefore(message.id, limit)
    }

    /**
     * Retrieves messages from the beginning of this [MessageChannel].
     * The `limit` determines the amount of messages being retrieved.
     *
     *
     * **Example**<br></br>
     * <pre>`
     * public void resendFirstMessage(MessageChannel channel)
     * {
     * channel.getHistoryFromBeginning(1).queue(history ->
     * {
     * if (!history.isEmpty())
     * {
     * Message firstMsg = history.getRetrievedHistory().get(0);
     * channel.sendMessage(firstMsg).queue();
     * }
     * else
     * channel.sendMessage("No history for this channel!").queue();
     * });
     * }
    `</pre> *
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @param  limit
     * The amount of messages to be retrieved. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     * Provided `limit` is less than `1` or greater than `100`.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [MessageHistory.MessageRetrieveAction][net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction]
     * <br></br>Provides a [MessageHistory][net.dv8tion.jda.api.entities.MessageHistory] object with with the first messages of this channel loaded into it.
     * <br></br>**Note: The messages are ordered from the most recent to oldest!**
     *
     * @see net.dv8tion.jda.api.entities.MessageHistory.retrieveFuture
     * @see net.dv8tion.jda.api.entities.MessageHistory.getHistoryAfter
     */
    @Nonnull
    @CheckReturnValue
    fun getHistoryFromBeginning(limit: Int): MessageRetrieveAction? {
        return MessageHistory.getHistoryFromBeginning(this).limit(limit)
    }

    /**
     * Sends the typing status to discord. This is what is used to make the message "X is typing..." appear.
     * <br></br>The typing status only lasts for 10 seconds or until a message is sent.
     * <br></br>So if you wish to show continuous typing you will need to call this method once every 10 seconds.
     *
     *
     * The official discord client sends this every 5 seconds even though the typing status lasts 10.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    fun sendTyping(): RestAction<Void?>? {
        val route = Route.Channels.SEND_TYPING.compile(id)
        return RestActionImpl(getJDA(), route)
    }

    /**
     * Attempts to react to a message represented by the specified `messageId`
     * in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     * <br></br>Also can happen if the account lost the [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_ADD_REACTION][net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.
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
     * The messageId to attach the reaction to
     * @param  emoji
     * The not-null [Emoji] to react with
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided `emoji` is `null`.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the MessageChannel this message was sent in was a [GuildMessageChannel]
     * and the logged in account does not have
     *
     *  * [Permission.MESSAGE_ADD_REACTION][net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun addReactionById(@Nonnull messageId: String?, @Nonnull emoji: Emoji): RestAction<Void?>? {
        Checks.isSnowflake(messageId, "Message ID")
        Checks.notNull(emoji, "Emoji")
        val route = Route.Messages.ADD_REACTION.compile(id, messageId, emoji.asReactionCode, "@me")
        return RestActionImpl(getJDA(), route)
    }

    /**
     * Attempts to react to a message represented by the specified `messageId`
     * in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     * <br></br>Also can happen if the account lost the [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_ADD_REACTION][net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.
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
     * The messageId to attach the reaction to
     * @param  emoji
     * The [Emoji] to react with
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided `messageId` is not a valid snowflake.
     *  * If provided `emoji` is `null`
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the MessageChannel this message was sent in was a [GuildMessageChannel]
     * and the logged in account does not have
     *
     *  * [Permission.MESSAGE_ADD_REACTION][net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION]
     *  * [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     * @return [net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun addReactionById(messageId: Long, @Nonnull emoji: Emoji): RestAction<Void?>? {
        return addReactionById(java.lang.Long.toUnsignedString(messageId), emoji)
    }

    /**
     * Attempts to remove the reaction from a message represented by the specified `messageId`
     * in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     * <br></br>Also can happen if the account lost the [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_ADD_REACTION][net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.
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
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or not a valid snowflake.
     *  * If provided `emoji` is `null`.
     *
     *
     * @return [net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeReactionById(@Nonnull messageId: String?, @Nonnull emoji: Emoji): RestAction<Void?>? {
        Checks.isSnowflake(messageId, "Message ID")
        Checks.notNull(emoji, "Emoji")
        val route = Route.Messages.REMOVE_REACTION.compile(id, messageId, emoji.asReactionCode, "@me")
        return RestActionImpl(getJDA(), route)
    }

    /**
     * Attempts to remove the reaction from a message represented by the specified `messageId`
     * in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     * <br></br>Also can happen if the account lost the [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_ADD_REACTION][net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION] in the
     * [GuildMessageChannel].
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.
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
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided `messageId` is not a valid snowflake.
     *  * If provided `emoji` is `null`.
     *
     *
     * @return [net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun removeReactionById(messageId: Long, @Nonnull emoji: Emoji): RestAction<Void?>? {
        return removeReactionById(java.lang.Long.toUnsignedString(messageId), emoji)
    }

    /**
     * This obtains the [users][net.dv8tion.jda.api.entities.User] who reacted to a message using the given [Emoji].
     *
     *
     * Messages maintain a list of reactions, alongside a list of users who added them.
     *
     *
     * Using this data, we can obtain a [ReactionPaginationAction]
     * of the users who've reacted to the given message.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The retrieve request was attempted after the account lost access to the [GuildMessageChannel]
     * due to [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] being revoked
     * <br></br>Also can happen if the account lost the [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted.
     *
     *
     * @param  messageId
     * The messageId to retrieve the users from.
     * @param  emoji
     * The [Emoji] to retrieve users for.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the
     * logged in account does not have [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY].
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or not a valid snowflake.
     *  * If provided `emoji` is `null`.
     *
     *
     * @return The [ReactionPaginationAction] of the emoji's users.
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveReactionUsersById(@Nonnull messageId: String?, @Nonnull emoji: Emoji): ReactionPaginationAction? {
        Checks.isSnowflake(messageId, "Message ID")
        Checks.notNull(emoji, "Emoji")
        return ReactionPaginationActionImpl(this, messageId, emoji.asReactionCode)
    }

    /**
     * This obtains the [users][net.dv8tion.jda.api.entities.User] who reacted to a message using the given [Emoji].
     *
     *
     * Messages maintain a list of reactions, alongside a list of users who added them.
     *
     *
     * Using this data, we can obtain a [ReactionPaginationAction]
     * of the users who've reacted to the given message.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The retrieve request was attempted after the account lost access to the [GuildMessageChannel]
     * due to [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL] being revoked
     * <br></br>Also can happen if the account lost the [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY]
     *
     *  * [UNKNOWN_EMOJI][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_EMOJI]
     * <br></br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted.
     *
     *
     * @param  messageId
     * The messageId to retrieve the users from.
     * @param  emoji
     * The [Emoji] to retrieve users for.
     *
     * @throws java.lang.UnsupportedOperationException
     * If this is not a Received Message from [MessageType.DEFAULT][net.dv8tion.jda.api.entities.MessageType.DEFAULT]
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the
     * logged in account does not have [Permission.MESSAGE_HISTORY][net.dv8tion.jda.api.Permission.MESSAGE_HISTORY].
     * @throws java.lang.IllegalArgumentException
     *
     *  * If provided `messageId` is not a valid snowflake.
     *  * If provided `emoji` is `null`.
     *
     *
     * @return The [ReactionPaginationAction] of the emoji's users.
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveReactionUsersById(messageId: Long, @Nonnull emoji: Emoji): ReactionPaginationAction? {
        return retrieveReactionUsersById(java.lang.Long.toUnsignedString(messageId), emoji)
    }

    /**
     * Used to pin a message. Pinned messages are retrievable via [.retrievePinnedMessages].
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the
     * [GuildMessageChannel].
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
     * The message to pin.
     *
     * @throws IllegalArgumentException
     * if the provided messageId is `null` or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun pinMessageById(@Nonnull messageId: String?): RestAction<Void?>? {
        Checks.isSnowflake(messageId, "Message ID")
        val route = Route.Messages.ADD_PINNED_MESSAGE.compile(id, messageId)
        return RestActionImpl(getJDA(), route)
    }

    /**
     * Used to pin a message. Pinned messages are retrievable via [.retrievePinnedMessages].
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the
     * [GuildMessageChannel].
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
     * The message to pin.
     *
     * @throws IllegalArgumentException
     * if the provided `messageId` is not a valid snowflake.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun pinMessageById(messageId: Long): RestAction<Void?>? {
        return pinMessageById(java.lang.Long.toUnsignedString(messageId))
    }

    /**
     * Used to unpin a message. Pinned messages are retrievable via [.retrievePinnedMessages].
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the
     * [GuildMessageChannel].
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
     * The message to unpin.
     *
     * @throws IllegalArgumentException
     * if the provided messageId is `null` or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun unpinMessageById(@Nonnull messageId: String?): RestAction<Void?>? {
        Checks.isSnowflake(messageId, "Message ID")
        val route = Route.Messages.REMOVE_PINNED_MESSAGE.compile(id, messageId)
        return RestActionImpl(getJDA(), route)
    }

    /**
     * Used to unpin a message. Pinned messages are retrievable via [.retrievePinnedMessages].
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [MISSING_PERMISSIONS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS]
     * <br></br>The request was attempted after the account lost
     * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE] in the
     * [GuildMessageChannel].
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
     * The message to unpin.
     *
     * @throws IllegalArgumentException
     * if the provided messageId is not positive.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and the logged in account does not have
     *
     *  * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *  * [Permission.MESSAGE_MANAGE][net.dv8tion.jda.api.Permission.MESSAGE_MANAGE]
     *
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun unpinMessageById(messageId: Long): RestAction<Void?>? {
        return unpinMessageById(java.lang.Long.toUnsignedString(messageId))
    }

    /**
     * Retrieves a List of [Messages][net.dv8tion.jda.api.entities.Message] that have been pinned in this channel.
     * <br></br>If no messages have been pinned, this retrieves an empty List.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: List&lt;[net.dv8tion.jda.api.entities.Message]&gt;
     * <br></br>Retrieves an immutable list of pinned messages
     */
    @Nonnull
    @CheckReturnValue
    fun retrievePinnedMessages(): RestAction<List<Message>?>? {
        val jda = getJDA() as JDAImpl
        val route = Route.Messages.GET_PINNED_MESSAGES.compile(id)
        return RestActionImpl(jda, route) { response: Response, request: Request<List<Message>?>? ->
            val pinnedMessages = LinkedList<Message>()
            val builder = jda.entityBuilder
            val pins = response.array
            for (i in 0 until pins.length()) {
                pinnedMessages.add(builder.createMessageWithChannel(pins.getObject(i), this@MessageChannel, false))
            }
            Collections.unmodifiableList(pinnedMessages)
        }
    }

    /**
     * Attempts to edit a message by its id in this channel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  newContent
     * The new content for the edited message
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided `newContent` is `null` or empty.
     *  * If provided `newContent` length is greater than {@value Message#MAX_CONTENT_LENGTH} characters.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageById(@Nonnull messageId: String?, @Nonnull newContent: CharSequence): MessageEditAction? {
        Checks.isSnowflake(messageId, "Message ID")
        Checks.notEmpty(newContent, "Provided message content")
        Checks.check(
            newContent.length <= Message.MAX_CONTENT_LENGTH,
            "Provided newContent length must be %d or less characters.",
            Message.MAX_CONTENT_LENGTH
        )
        return MessageEditActionImpl(this, messageId!!).setContent(newContent.toString())
    }

    /**
     * Attempts to edit a message by its id in this channel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  newContent
     * The new content for the edited message
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `newContent` is `null` or empty.
     *  * If provided `newContent` length is greater than {@value Message#MAX_CONTENT_LENGTH} characters.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageById(messageId: Long, @Nonnull newContent: CharSequence): MessageEditAction? {
        return editMessageById(java.lang.Long.toUnsignedString(messageId), newContent)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  data
     * The new content for the edited message
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided `newContent` is `null`.
     *  * If provided [Message][net.dv8tion.jda.api.entities.Message]
     * contains a [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed] which
     * is not [sendable][net.dv8tion.jda.api.entities.MessageEmbed.isSendable]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageById(@Nonnull messageId: String?, @Nonnull data: MessageEditData?): MessageEditAction? {
        Checks.isSnowflake(messageId, "Message ID")
        Checks.notNull(data, "message")
        return MessageEditActionImpl(this, messageId!!).applyData(data!!)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  data
     * The new content for the edited message
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `newContent` is `null`.
     *  * If provided [Message][net.dv8tion.jda.api.entities.Message]
     * contains a [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed] which
     * is not [sendable][net.dv8tion.jda.api.entities.MessageEmbed.isSendable]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageById(messageId: Long, @Nonnull data: MessageEditData?): MessageEditAction? {
        return editMessageById(java.lang.Long.toUnsignedString(messageId), data)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  format
     * Format String used to generate new Content
     * @param  args
     * The arguments which should be used to format the given format String
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided `format` is `null` or blank.
     *
     * @throws IllegalStateException
     * If the resulting message is either empty or too long to be sent
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * @throws java.util.IllegalFormatException
     * If a format string contains an illegal syntax,
     * a format specifier that is incompatible with the given arguments,
     * insufficient arguments given the format string, or other illegal conditions.
     * For specification of all possible formatting errors,
     * see the [Details](../util/Formatter.html#detail)
     * section of the formatter class specification.
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageFormatById(
        @Nonnull messageId: String?,
        @Nonnull format: String?,
        @Nonnull vararg args: Any?
    ): MessageEditAction? {
        Checks.notBlank(format, "Format String")
        return editMessageById(messageId, String.format(format!!, *args))
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  format
     * Format String used to generate new Content
     * @param  args
     * The arguments which should be used to format the given format String
     *
     * @throws IllegalArgumentException
     * If provided `format` is `null` or blank.
     * @throws IllegalStateException
     * If the resulting message is either empty or too long to be sent
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * @throws java.util.IllegalFormatException
     * If a format string contains an illegal syntax,
     * a format specifier that is incompatible with the given arguments,
     * insufficient arguments given the format string, or other illegal conditions.
     * For specification of all possible formatting errors,
     * see the [Details](../util/Formatter.html#detail)
     * section of the formatter class specification.
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageFormatById(
        messageId: Long,
        @Nonnull format: String?,
        @Nonnull vararg args: Any?
    ): MessageEditAction? {
        Checks.notBlank(format, "Format String")
        return editMessageById(messageId, String.format(format!!, *args))
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  newEmbeds
     * Up to {@value Message#MAX_EMBED_COUNT} new [MessageEmbeds][net.dv8tion.jda.api.entities.MessageEmbed] for the edited message
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed]
     * is not [sendable][net.dv8tion.jda.api.entities.MessageEmbed.isSendable]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbedsById(
        @Nonnull messageId: String?,
        @Nonnull vararg newEmbeds: MessageEmbed?
    ): MessageEditAction? {
        Checks.noneNull(newEmbeds, "MessageEmbeds")
        return editMessageEmbedsById(messageId, Arrays.asList(*newEmbeds))
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  newEmbeds
     * Up to {@value Message#MAX_EMBED_COUNT} new [MessageEmbeds][net.dv8tion.jda.api.entities.MessageEmbed] for the edited message
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed]
     * is not [sendable][net.dv8tion.jda.api.entities.MessageEmbed.isSendable]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbedsById(messageId: Long, @Nonnull vararg newEmbeds: MessageEmbed?): MessageEditAction? {
        return editMessageEmbedsById(java.lang.Long.toUnsignedString(messageId), *newEmbeds)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  newEmbeds
     * Up to {@value Message#MAX_EMBED_COUNT} new [MessageEmbeds][net.dv8tion.jda.api.entities.MessageEmbed] for the edited message
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed]
     * is not [sendable][net.dv8tion.jda.api.entities.MessageEmbed.isSendable]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbedsById(
        @Nonnull messageId: String?,
        @Nonnull newEmbeds: Collection<MessageEmbed?>?
    ): MessageEditAction? {
        Checks.isSnowflake(messageId, "Message ID")
        return MessageEditActionImpl(this, messageId!!).setEmbeds(newEmbeds!!)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
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
     * The id referencing the Message that should be edited
     * @param  newEmbeds
     * Up to {@value Message#MAX_EMBED_COUNT} new [MessageEmbeds][net.dv8tion.jda.api.entities.MessageEmbed] for the edited message
     *
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If provided [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed]
     * is not [sendable][net.dv8tion.jda.api.entities.MessageEmbed.isSendable]
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbedsById(messageId: Long, @Nonnull newEmbeds: Collection<MessageEmbed?>?): MessageEditAction? {
        return editMessageEmbedsById(java.lang.Long.toUnsignedString(messageId), newEmbeds)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br></br>This will replace all the current [Components][net.dv8tion.jda.api.interactions.components.Component],
     * such as [Buttons][Button] or [SelectMenus][SelectMenu] on this message.
     * The provided parameters are [LayoutComponents][LayoutComponent] such as [ActionRow] which contain a list of components to arrange in the respective layout.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted. This might also be triggered for ephemeral messages.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * **Example**<br></br>
     * <pre>`List<ActionRow> rows = Arrays.asList(
     * ActionRow.of(Button.success("prompt:accept", "Accept"), Button.danger("prompt:reject", "Reject")), // 1st row below message
     * ActionRow.of(Button.link(url, "Help")) // 2nd row below message
     * );
     * channel.editMessageComponentsById(messageId, rows).queue();
    `</pre> *
     *
     * @param  messageId
     * The id referencing the Message that should be edited
     * @param  components
     * Up to 5 new [LayoutComponents][LayoutComponent] for the edited message, such as [ActionRow]
     *
     * @throws UnsupportedOperationException
     * If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If any of the provided [LayoutComponents][LayoutComponent] is null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageComponentsById(
        @Nonnull messageId: String?,
        @Nonnull components: Collection<LayoutComponent?>
    ): MessageEditAction? {
        Checks.isSnowflake(messageId, "Message ID")
        Checks.noneNull(components, "Components")
        if (components.stream()
                .anyMatch { x: LayoutComponent? -> x !is ActionRow }
        ) throw UnsupportedOperationException("The provided component layout is not supported")
        val actionRows =
            components.stream().map { obj: Any? -> ActionRow::class.java.cast(obj) }.collect(Collectors.toList())
        return MessageEditActionImpl(this, messageId!!).setComponents(actionRows)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br></br>This will replace all the current [Components][net.dv8tion.jda.api.interactions.components.Component],
     * such as [Buttons][Button] or [SelectMenus][SelectMenu] on this message.
     * The provided parameters are [LayoutComponents][LayoutComponent] such as [ActionRow] which contain a list of components to arrange in the respective layout.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted. This might also be triggered for ephemeral messages.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * **Example**<br></br>
     * <pre>`List<ActionRow> rows = Arrays.asList(
     * ActionRow.of(Button.success("prompt:accept", "Accept"), Button.danger("prompt:reject", "Reject")), // 1st row below message
     * ActionRow.of(Button.link(url, "Help")) // 2nd row below message
     * );
     * channel.editMessageComponentsById(messageId, rows).queue();
    `</pre> *
     *
     * @param  messageId
     * The id referencing the Message that should be edited
     * @param  components
     * Up to 5 new [LayoutComponents][LayoutComponent] for the edited message, such as [ActionRow]
     *
     * @throws UnsupportedOperationException
     * If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     * If any of the provided [LayoutComponents][LayoutComponent] is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageComponentsById(
        messageId: Long,
        @Nonnull components: Collection<LayoutComponent?>
    ): MessageEditAction? {
        return editMessageComponentsById(java.lang.Long.toUnsignedString(messageId), components)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br></br>This will replace all the current [Components][net.dv8tion.jda.api.interactions.components.Component],
     * such as [Buttons][Button] or [SelectMenus][SelectMenu] on this message.
     * The provided parameters are [LayoutComponents][LayoutComponent] such as [ActionRow] which contain a list of components to arrange in the respective layout.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted. This might also be triggered for ephemeral messages.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * **Example**<br></br>
     * <pre>`channel.editMessageComponentsById(messageId,
     * ActionRow.of(Button.success("prompt:accept", "Accept"), Button.danger("prompt:reject", "Reject")), // 1st row below message
     * ActionRow.of(Button.link(url, "Help")) // 2nd row below message
     * ).queue();
    `</pre> *
     *
     * @param  messageId
     * The id referencing the Message that should be edited
     * @param  components
     * Up to 5 new [LayoutComponents][LayoutComponent] for the edited message, such as [ActionRow]
     *
     * @throws UnsupportedOperationException
     * If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *
     *  * If provided `messageId` is `null` or empty.
     *  * If any of the provided [LayoutComponents][LayoutComponent] is null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageComponentsById(
        @Nonnull messageId: String?,
        @Nonnull vararg components: LayoutComponent?
    ): MessageEditAction? {
        Checks.noneNull(components, "Components")
        return editMessageComponentsById(messageId, Arrays.asList(*components))
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br></br>This will replace all the current [Components][net.dv8tion.jda.api.interactions.components.Component],
     * such as [Buttons][Button] or [SelectMenus][SelectMenu] on this message.
     * The provided parameters are [LayoutComponents][LayoutComponent] such as [ActionRow] which contain a list of components to arrange in the respective layout.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted. This might also be triggered for ephemeral messages.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * **Example**<br></br>
     * <pre>`channel.editMessageComponentsById(messageId,
     * ActionRow.of(Button.success("prompt:accept", "Accept"), Button.danger("prompt:reject", "Reject")), // 1st row below message
     * ActionRow.of(Button.link(url, "Help")) // 2nd row below message
     * ).queue();
    `</pre> *
     *
     * @param  messageId
     * The id referencing the Message that should be edited
     * @param  components
     * Up to 5 new [LayoutComponents][LayoutComponent] for the edited message, such as [ActionRow]
     *
     * @throws UnsupportedOperationException
     * If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     * If any of the provided [LayoutComponents][LayoutComponent] is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If this is a [GuildMessageChannel] and this account does not have
     * [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * or [Permission.MESSAGE_SEND][net.dv8tion.jda.api.Permission.MESSAGE_SEND]
     *
     * @return [MessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageComponentsById(messageId: Long, @Nonnull vararg components: LayoutComponent?): MessageEditAction? {
        Checks.noneNull(components, "Components")
        return editMessageComponentsById(messageId, Arrays.asList(*components))
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If any of the provided files is bigger than [Guild.getMaxFileSize]
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted. This might also be triggered for ephemeral messages.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  attachments
     * The new attachments of the message (Can be [FileUploads][FileUpload] or [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate])
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [MessageEditAction] that can be used to further update the message
     *
     * @see AttachedFile.fromAttachment
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageAttachmentsById(
        @Nonnull messageId: String?,
        @Nonnull attachments: Collection<AttachedFile?>?
    ): MessageEditAction? {
        Checks.isSnowflake(messageId, "Message ID")
        return MessageEditActionImpl(this, messageId!!).setAttachments(attachments)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If any of the provided files is bigger than [Guild.getMaxFileSize]
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted. This might also be triggered for ephemeral messages.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  attachments
     * The new attachments of the message (Can be [FileUploads][FileUpload] or [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate])
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [MessageEditAction] that can be used to further update the message
     *
     * @see AttachedFile.fromAttachment
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageAttachmentsById(
        @Nonnull messageId: String?,
        @Nonnull vararg attachments: AttachedFile?
    ): MessageEditAction? {
        Checks.noneNull(attachments, "Attachments")
        return editMessageAttachmentsById(messageId, Arrays.asList(*attachments))
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If any of the provided files is bigger than [Guild.getMaxFileSize]
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted. This might also be triggered for ephemeral messages.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  attachments
     * The new attachments of the message (Can be [FileUploads][FileUpload] or [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate])
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [MessageEditAction] that can be used to further update the message
     *
     * @see AttachedFile.fromAttachment
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageAttachmentsById(
        messageId: Long,
        @Nonnull attachments: Collection<AttachedFile?>?
    ): MessageEditAction? {
        return editMessageAttachmentsById(java.lang.Long.toUnsignedString(messageId), attachments)
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     *
     * The following [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] are possible:
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If any of the provided files is bigger than [Guild.getMaxFileSize]
     *
     *  * [INVALID_AUTHOR_EDIT][net.dv8tion.jda.api.requests.ErrorResponse.INVALID_AUTHOR_EDIT]
     * <br></br>Attempted to edit a message that was not sent by the currently logged in account.
     * Discord does not allow editing of other users' Messages!
     *
     *  * [MISSING_ACCESS][net.dv8tion.jda.api.requests.ErrorResponse.MISSING_ACCESS]
     * <br></br>The request was attempted after the account lost access to the [Guild][net.dv8tion.jda.api.entities.Guild]
     * typically due to being kicked or removed, or after [Permission.VIEW_CHANNEL][net.dv8tion.jda.api.Permission.VIEW_CHANNEL]
     * was revoked in the [GuildMessageChannel]
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The provided `messageId` is unknown in this MessageChannel, either due to the id being invalid, or
     * the message it referred to has already been deleted. This might also be triggered for ephemeral messages.
     *
     *  * [UNKNOWN_CHANNEL][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL]
     * <br></br>The request was attempted after the channel was deleted.
     *
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  attachments
     * The new attachments of the message (Can be [FileUploads][FileUpload] or [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate])
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [MessageEditAction] that can be used to further update the message
     *
     * @see AttachedFile.fromAttachment
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageAttachmentsById(messageId: Long, @Nonnull vararg attachments: AttachedFile?): MessageEditAction? {
        return editMessageAttachmentsById(java.lang.Long.toUnsignedString(messageId), *attachments)
    }
}
