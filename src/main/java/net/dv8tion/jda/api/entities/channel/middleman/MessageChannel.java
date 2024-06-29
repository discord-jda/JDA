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
package net.dv8tion.jda.api.entities.channel.middleman;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.PollVotersPaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl;
import net.dv8tion.jda.internal.requests.restaction.MessageEditActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.MessagePaginationActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.PollVotersPaginationActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a Discord channel that can have {@link net.dv8tion.jda.api.entities.Message Messages} and files sent to it.
 *
 * <p><b>Formattable</b><br>
 * This interface extends {@link java.util.Formattable Formattable} and can be used with a {@link java.util.Formatter Formatter}
 * such as used by {@link String#format(String, Object...) String.format(String, Object...)}
 * or {@link java.io.PrintStream#printf(String, Object...) PrintStream.printf(String, Object...)}.
 *
 * <p>This will use {@link #getName()} rather than {@link Object#toString()}!
 * <br>Supported Features:
 * <ul>
 *     <li><b>Alternative</b>
 *     <br>   - Prepends the name with {@code #}
 *              (Example: {@code %#s} - results in <code>#{@link #getName()}</code>)</li>
 *
 *     <li><b>Width/Left-Justification</b>
 *     <br>   - Ensures the size of a format
 *              (Example: {@code %20s} - uses at minimum 20 chars;
 *              {@code %-10s} - uses left-justified padding)</li>
 *
 *     <li><b>Precision</b>
 *     <br>   - Cuts the content to the specified size
 *              (Example: {@code %.20s})</li>
 * </ul>
 *
 * <p>More information on formatting syntax can be found in the {@link java.util.Formatter format syntax documentation}!
 * <br><b>{@link GuildMessageChannel GuildMessageChannel} is a special case which uses {@link net.dv8tion.jda.api.entities.IMentionable#getAsMention() IMentionable.getAsMention()}
 * by default and uses the <code>#{@link #getName()}</code> format as <u>alternative</u></b>
 *
 * @see TextChannel
 * @see PrivateChannel
 */
public interface MessageChannel extends Channel, Formattable
{
    /**
     * The id for the most recent message sent
     * in this current MessageChannel.
     *
     * <p>This value is updated on each {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}
     * and <u><b>the value might point to an already deleted message since the ID is not cleared when the message is deleted,
     * so calling {@link #retrieveMessageById(long)} with this id can result in an {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE} error</b></u>
     *
     * @return The most recent message's id or "0" if no messages are present
     */
    @Nonnull
    default String getLatestMessageId()
    {
        return Long.toUnsignedString(getLatestMessageIdLong());
    }

    /**
     * The id for the most recent message sent
     * in this current MessageChannel.
     *
     * <p>This value is updated on each {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}
     * and <u><b>the value might point to an already deleted message since the value is not cleared when the message is deleted,
     * so calling {@link #retrieveMessageById(long)} with this id can result in an {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE} error</b></u>
     *
     * @return The most recent message's id or 0 if no messages are present
     */
    long getLatestMessageIdLong();

    /**
     * Whether the currently logged in user can send messages in this channel or not.
     * <br>For {@link GuildMessageChannel} this method checks for
     * both {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * and {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}.
     * <br>For {@link ThreadChannel} this method checks for {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND_IN_THREADS} instead of {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND}.
     * <br>For {@link PrivateChannel} this method checks if the user that this PrivateChannel communicates with is not a bot,
     * but it does <b>not</b> check if the said user blocked the currently logged in user or have their DMs disabled.
     * <br>For {@link GroupChannel} this method returns false.
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return True, if we are able to read and send messages in this channel
     */
    boolean canTalk();

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br>This combines both {@link GuildMessageChannel#deleteMessagesByIds(Collection)} as well as {@link #deleteMessageById(long)}
     * to delete all messages provided. No checks will be done to prevent failures, use {@link java.util.concurrent.CompletionStage#exceptionally(Function)}
     * to handle failures.
     *
     * <p>For possible ErrorResponses see {@link #purgeMessagesById(long...)}.
     *
     * @param  messageIds
     *         The message ids to delete
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return List of futures representing all deletion tasks
     *
     * @see    CompletableFuture#allOf(java.util.concurrent.CompletableFuture[])
     */
    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@Nonnull List<String> messageIds)
    {
        if (messageIds == null || messageIds.isEmpty())
            return Collections.emptyList();
        long[] ids = new long[messageIds.size()];
        for (int i = 0; i < ids.length; i++)
            ids[i] = MiscUtil.parseSnowflake(messageIds.get(i));
        return purgeMessagesById(ids);
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br>This combines both {@link GuildMessageChannel#deleteMessagesByIds(Collection)} as well as {@link #deleteMessageById(long)}
     * to delete all messages provided. No checks will be done to prevent failures, use {@link java.util.concurrent.CompletionStage#exceptionally(Function)}
     * to handle failures.
     *
     * <p>For possible ErrorResponses see {@link #purgeMessagesById(long...)}.
     *
     * @param  messageIds
     *         The message ids to delete
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return List of futures representing all deletion tasks
     *
     * @see    CompletableFuture#allOf(java.util.concurrent.CompletableFuture[])
     */
    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@Nonnull String... messageIds)
    {
        if (messageIds == null || messageIds.length == 0)
            return Collections.emptyList();
        return purgeMessagesById(Arrays.asList(messageIds));
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br>This combines both {@link GuildMessageChannel#deleteMessagesByIds(Collection)} as well as {@link net.dv8tion.jda.api.entities.Message#delete()}
     * to delete all messages provided. No checks will be done to prevent failures, use {@link java.util.concurrent.CompletionStage#exceptionally(Function)}
     * to handle failures.
     *
     * <p>For possible ErrorResponses see {@link #purgeMessagesById(long...)}.
     *
     * @param  messages
     *         The messages to delete
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If one of the provided messages is from another user and cannot be deleted due to permissions
     * @throws IllegalArgumentException
     *         If one of the provided messages is from another user and cannot be deleted because this is not in a guild
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return List of futures representing all deletion tasks
     *
     * @see    CompletableFuture#allOf(java.util.concurrent.CompletableFuture[])
     */
    @Nonnull
    default List<CompletableFuture<Void>> purgeMessages(@Nonnull Message... messages)
    {
        if (messages == null || messages.length == 0)
            return Collections.emptyList();
        return purgeMessages(Arrays.asList(messages));
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br>This combines both {@link GuildMessageChannel#deleteMessagesByIds(Collection)} as well as {@link Message#delete()}
     * to delete all messages provided. No checks will be done to prevent failures, use {@link java.util.concurrent.CompletionStage#exceptionally(Function)}
     * to handle failures.
     *
     * <p>Any messages that cannot be deleted, as suggested by {@link net.dv8tion.jda.api.entities.MessageType#canDelete()}, will be filtered out before making any requests.
     *
     * <p>For possible ErrorResponses see {@link #purgeMessagesById(long...)}.
     *
     * @param  messages
     *         The messages to delete
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If one of the provided messages is from another user and cannot be deleted due to permissions
     * @throws IllegalArgumentException
     *         If one of the provided messages is from another user and cannot be deleted because this is not in a guild
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return List of futures representing all deletion tasks
     *
     * @see    CompletableFuture#allOf(java.util.concurrent.CompletableFuture[])
     */
    @Nonnull
    default List<CompletableFuture<Void>> purgeMessages(@Nonnull List<? extends Message> messages)
    {
        if (messages == null || messages.isEmpty())
            return Collections.emptyList();
        return purgeMessagesById(messages.stream()
                .filter(m -> m.getType().canDelete())
                .mapToLong(Message::getIdLong)
                .toArray());
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br>This combines both {@link GuildMessageChannel#deleteMessagesByIds(Collection)} as well as {@link #deleteMessageById(long)}
     * to delete all messages provided. No checks will be done to prevent failures, use {@link java.util.concurrent.CompletionStage#exceptionally(Function)}
     * to handle failures.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>if any of the provided messages does not exist</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the channel</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.</li>
     * </ul>
     *
     * @param  messageIds
     *         The message ids to delete
     *
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return List of futures representing all deletion tasks
     *
     * @see    CompletableFuture#allOf(java.util.concurrent.CompletableFuture[])
     */
    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@Nonnull long... messageIds)
    {
        if (messageIds == null || messageIds.length == 0)
            return Collections.emptyList();
        List<CompletableFuture<Void>> list = new ArrayList<>(messageIds.length);
        TreeSet<Long> sortedIds = new TreeSet<>(Comparator.reverseOrder());
        for (long messageId : messageIds)
            sortedIds.add(messageId);
        for (long messageId : sortedIds)
            list.add(deleteMessageById(messageId).submit());
        return list;
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  text
     *         The message content
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If the content is null or longer than {@value Message#MAX_CONTENT_LENGTH} characters
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessage(@Nonnull CharSequence text)
    {
        Checks.notNull(text, "Content");
        return new MessageCreateActionImpl(this).setContent(text.toString());
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  msg
     *         The {@link MessageCreateData} to send
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     *
     * @see    net.dv8tion.jda.api.utils.messages.MessageCreateBuilder MessageCreateBuilder
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessage(@Nonnull MessageCreateData msg)
    {
        Checks.notNull(msg, "Message");
        return new MessageCreateActionImpl(this).applyData(msg);
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  format
     *         Format string for the message content
     * @param  args
     *         Format arguments for the content
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If the format string is null or the resulting content is longer than {@value Message#MAX_CONTENT_LENGTH} characters
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax, a format
     *         specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other
     *         illegal conditions.  For specification of all possible
     *         formatting errors, see the <a href="../util/Formatter.html#detail">Details</a> section of the
     *         formatter class specification.
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notEmpty(format, "Format");
        return sendMessage(String.format(format, args));
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * channel.sendMessageEmbeds(embed) // send the embed
     *        .addFiles(file) // add the file as attachment
     *        .queue();
     * }</pre>
     *
     * @param  embed
     *         {@link MessageEmbed} to send
     * @param  other
     *         Additional {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other)
    {
        Checks.notNull(embed, "MessageEmbeds");
        Checks.noneNull(other, "MessageEmbeds");
        List<MessageEmbed> embeds = new ArrayList<>(1 + other.length);
        embeds.add(embed);
        Collections.addAll(embeds, other);
        return new MessageCreateActionImpl(this).setEmbeds(embeds);
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * channel.sendMessageEmbeds(Collections.singleton(embed)) // send the embeds
     *        .addFiles(file) // add the file as attachment
     *        .queue();
     * }</pre>
     *
     * @param  embeds
     *         {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return new MessageCreateActionImpl(this).setEmbeds(embeds);
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  component
     *         {@link LayoutComponent} to send
     * @param  other
     *         Additional {@link LayoutComponent LayoutComponents} to use (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If any of the components is null or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageComponents(@Nonnull LayoutComponent component, @Nonnull LayoutComponent... other)
    {
        Checks.notNull(component, "LayoutComponents");
        Checks.noneNull(other, "LayoutComponents");
        List<LayoutComponent> components = new ArrayList<>(1 + other.length);
        components.add(component);
        Collections.addAll(components, other);
        return new MessageCreateActionImpl(this).setComponents(components);
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * channel.sendMessageEmbeds(Collections.singleton(embed)) // send the embeds
     *        .addFiles(file) // add the file as attachment
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         {@link LayoutComponent LayoutComponents} to use (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If any of the components is null or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageComponents(@Nonnull Collection<? extends LayoutComponent> components)
    {
        return new MessageCreateActionImpl(this).setComponents(components);
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#POLL_INVALID_CHANNEL_TYPE POLL_INVALID_CHANNEL_TYPE}
     *     <br>This channel does not allow polls</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#POLL_WITH_UNUSABLE_EMOJI POLL_WITH_UNUSABLE_EMOJI}
     *     <br>This poll uses an external emoji that the bot is not allowed to use</li>
     * </ul>
     *
     * @param  poll
     *         The poll to send
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If the poll is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessagePoll(@Nonnull MessagePollData poll)
    {
        Checks.notNull(poll, "Poll");
        return new MessageCreateActionImpl(this).setPoll(poll);
    }

    /**
     * Send a message to this channel.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If the total sum of uploaded bytes exceeds the guild's {@link Guild#getMaxFileSize() upload limit}</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * channel.sendFiles(Collections.singleton(file)) // send the file upload
     *        .addEmbeds(embed) // add the embed you want to reference the file with
     *        .queue();
     * }</pre>
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     *
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        Checks.notEmpty(files, "File Collection");
        Checks.noneNull(files, "Files");
        return new MessageCreateActionImpl(this).addFiles(files);
    }

    /**
     * Send a message to this channel.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If the total sum of uploaded bytes exceeds the guild's {@link Guild#getMaxFileSize() upload limit}</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * channel.sendFiles(file) // send the file upload
     *        .addEmbeds(embed) // add the embed you want to reference the file with
     *        .queue();
     * }</pre>
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageCreateAction}
     *
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendFiles(@Nonnull FileUpload... files)
    {
        Checks.notEmpty(files, "File Collection");
        Checks.noneNull(files, "Files");
        return sendFiles(Arrays.asList(files));
    }

    /**
     * Attempts to get a {@link net.dv8tion.jda.api.entities.Message Message} from the Discord's servers that has
     * the same id as the id provided.
     * <br>Note: when retrieving a Message, you must retrieve it from the channel it was sent in!
     *
     * <p>The {@link Message#getMember() Message.getMember()} method will always return null for the resulting message.
     * To retrieve the member you can use {@code getGuild().retrieveMember(message.getAuthor())}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code id} does not refer to a message sent in this channel or the message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the sought after Message
     *
     * @throws IllegalArgumentException
     *         if the provided {@code messageId} is null or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Message
     *         <br>The Message defined by the provided id.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Message> retrieveMessageById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        JDAImpl jda = (JDAImpl) getJDA();
        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getId(), messageId);
        return new RestActionImpl<>(jda, route,
            (response, request) -> jda.getEntityBuilder().createMessageWithChannel(response.getObject(), MessageChannel.this, false));
    }

    /**
     * Attempts to get a {@link net.dv8tion.jda.api.entities.Message Message} from the Discord's servers that has
     * the same id as the id provided.
     * <br>Note: when retrieving a Message, you must retrieve it from the channel it was sent in!
     *
     * <p>The {@link Message#getMember() Message.getMember()} method will always return null for the resulting message.
     * To retrieve the member you can use {@code getGuild().retrieveMember(message.getAuthor())}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code id} does not refer to a message sent in this channel or the message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the sought after Message
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Message
     *         <br>The Message defined by the provided id.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Message> retrieveMessageById(long messageId)
    {
        return retrieveMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * Attempts to delete a {@link net.dv8tion.jda.api.entities.Message Message} from the Discord servers that has
     * the same id as the id provided.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request attempted to delete a Message in a {@link GuildMessageChannel GuildMessageChannel}
     *         that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_DM_ACTION INVALID_DM_ACTION}
     *     <br>Attempted to delete a Message in a {@link PrivateChannel PrivateChannel}
     *         that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code id} does not refer to a message sent in this channel or the message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the Message that should be deleted
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}.
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGE.compile(getId(), messageId);
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    /**
     * Attempts to delete a {@link net.dv8tion.jda.api.entities.Message Message} from the Discord servers that has
     * the same id as the id provided.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request attempted to delete a Message in a {@link GuildMessageChannel GuildMessageChannel}
     *         that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_DM_ACTION INVALID_DM_ACTION}
     *     <br>Attempted to delete a Message in a {@link PrivateChannel PrivateChannel}
     *         that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code id} does not refer to a message sent in this channel or the message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the Message that should be deleted
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is not positive
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}.
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> deleteMessageById(long messageId)
    {
        return deleteMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * End the poll attached to this message.
     *
     * <p><b>A bot cannot expire the polls of other users.</b>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>If the poll was sent by another user</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_EXPIRE_MISSING_POLL CANNOT_EXPIRE_MISSING_POLL}
     *     <br>The message did not have a poll attached</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message no longer exists</li>
     * </ul>
     *
     * @param  messageId
     *         The ID for the poll message
     *
     * @throws IllegalArgumentException
     *         If the provided messageId is not a valid snowflake
     *
     * @return {@link AuditableRestAction} - Type: {@link Message}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Message> endPollById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");
        return new AuditableRestActionImpl<>(getJDA(), Route.Messages.END_POLL.compile(getId(), messageId), (response, request) -> {
            JDAImpl jda = (JDAImpl) getJDA();
            return jda.getEntityBuilder().createMessageWithChannel(response.getObject(), MessageChannel.this, false);
        });
    }

    /**
     * End the poll attached to this message.
     *
     * <p><b>A bot cannot expire the polls of other users.</b>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>If the poll was sent by another user</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_EXPIRE_MISSING_POLL CANNOT_EXPIRE_MISSING_POLL}
     *     <br>The message did not have a poll attached</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message no longer exists</li>
     * </ul>
     *
     * @param  messageId
     *         The ID for the poll message
     *
     * @return {@link AuditableRestAction} - Type: {@link Message}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Message> endPollById(long messageId)
    {
        return endPollById(Long.toUnsignedString(messageId));
    }

    /**
     * Paginate the users who voted for a poll answer.
     *
     * @param  messageId
     *         The message id for the poll
     * @param  answerId
     *         The id of the poll answer, usually the ordinal position of the answer (first is 1)
     *
     * @throws IllegalArgumentException
     *         If the message id is not a valid snowflake
     *
     * @return {@link PollVotersPaginationAction}
     */
    @Nonnull
    @CheckReturnValue
    default PollVotersPaginationAction retrievePollVotersById(@Nonnull String messageId, long answerId)
    {
        return new PollVotersPaginationActionImpl(getJDA(), getId(), messageId, answerId);
    }

    /**
     * Paginate the users who voted for a poll answer.
     *
     * @param  messageId
     *         The message id for the poll
     * @param  answerId
     *         The id of the poll answer, usually the ordinal position of the answer (first is 1)
     *
     * @return {@link PollVotersPaginationAction}
     */
    @Nonnull
    @CheckReturnValue
    default PollVotersPaginationAction retrievePollVotersById(long messageId, long answerId)
    {
        return new PollVotersPaginationActionImpl(getJDA(), getId(), Long.toUnsignedString(messageId), answerId);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object for each call of this method.
     * <br>MessageHistory is <b>NOT</b> an internal message cache, but rather it queries the Discord servers for previously sent messages.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel}
     *         and the currently logged in account does not have the permission {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return A {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} related to this channel.
     */
    default MessageHistory getHistory()
    {
        return new MessageHistory(this);
    }

    /**
     * A {@link PaginationAction PaginationAction} implementation
     * that allows to {@link Iterable iterate} over recent {@link net.dv8tion.jda.api.entities.Message Messages} of
     * this MessageChannel.
     * <br>This is <b>not</b> a cache for received messages and it can only view messages that were sent
     * before. This iterates chronologically backwards (from present to past).
     *
     * <p><b><u>It is recommended not to use this in an enhanced for-loop without end conditions as it might cause memory
     * overflows in channels with a long message history.</u></b>
     *
     * <p><b>Examples</b><br>
     * <pre>{@code
     * public CompletableFuture<List<Message>> getMessagesByUser(MessageChannel channel, User user) {
     *     return channel.getIterableHistory()
     *         .takeAsync(1000) // Collect 1000 messages
     *         .thenApply(list ->
     *             list.stream()
     *                 .filter(m -> m.getAuthor().equals(user)) // Filter messages by author
     *                 .collect(Collectors.toList())
     *         );
     * }
     * }</pre>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel}
     *         and the currently logged in account does not have the permission {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessagePaginationAction MessagePaginationAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessagePaginationAction getIterableHistory()
    {
        return new MessagePaginationActionImpl(this);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent around
     * the marker. The {@code limit} determines the amount of messages retrieved near the marker. Discord will
     * attempt to evenly split the limit between before and after the marker, however in the case that the marker is set
     * near the beginning or near the end of the channel's history the amount of messages on each side of the marker may
     * be different, and their total count may not equal the provided {@code limit}.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAround(messageId, 100)} - This will retrieve 100 messages from history, 50 before the marker
     * and 50 after the marker.
     *
     * <p>Retrieve 10 messages near the end of history. Provided id is for a message that is the 3rd most recent message.
     * <br>{@code getHistoryAround(messageId, 10)} - This will retrieve 10 messages from history, 8 before the marker
     * and 2 after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the message that will act as a marker.
     * @param  limit
     *         The amount of messages to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is {@code null} or empty.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages around the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryAround(MessageChannel, String) MessageHistory.getHistoryAround(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(@Nonnull String messageId, int limit)
    {
        return MessageHistory.getHistoryAround(this, messageId).limit(limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages around
     * the marker. The {@code limit} determines the amount of messages retrieved near the marker. Discord will
     * attempt to evenly split the limit between before and after the marker, however in the case that the marker is set
     * near the beginning or near the end of the channel's history the amount of messages on each side of the marker may
     * be different, and their total count may not equal the provided {@code limit}.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAround(messageId, 100)} - This will retrieve 100 messages from history, 50 before the marker
     * and 50 after the marker.
     *
     * <p>Retrieve 10 messages near the end of history. Provided id is for a message that is the 3rd most recent message.
     * <br>{@code getHistoryAround(messageId, 10)} - This will retrieve 10 messages from history, 8 before the marker
     * and 2 after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the message that will act as a marker. The id must refer to a message from this MessageChannel.
     * @param  limit
     *         The amount of messages to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is not positive.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages around the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryAround(MessageChannel, String) MessageHistory.getHistoryAround(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(long messageId, int limit)
    {
        return getHistoryAround(Long.toUnsignedString(messageId), limit);
    }

    /**
     * Uses the provided {@link net.dv8tion.jda.api.entities.Message Message} as a marker and retrieves messages around
     * the marker. The {@code limit} determines the amount of messages retrieved near the marker. Discord will
     * attempt to evenly split the limit between before and after the marker, however in the case that the marker is set
     * near the beginning or near the end of the channel's history the amount of messages on each side of the marker may
     * be different, and their total count may not equal the provided {@code limit}.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAround(message, 100)} - This will retrieve 100 messages from history, 50 before the marker
     * and 50 after the marker.
     *
     * <p>Retrieve 10 messages near the end of history. Provided message is the 3rd most recent message.
     * <br>{@code getHistoryAround(message, 10)} - This will retrieve 10 messages from history, 8 before the marker
     * and 2 after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code message} has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  message
     *         The {@link net.dv8tion.jda.api.entities.Message Message} that will act as a marker. The provided Message
     *         must be from this MessageChannel.
     * @param  limit
     *         The amount of messages to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code message} is {@code null}.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages around the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryAround(MessageChannel, String) MessageHistory.getHistoryAround(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(@Nonnull Message message, int limit)
    {
        Checks.notNull(message, "Provided target message");
        return getHistoryAround(message.getId(), limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent after
     * the marker ID. The {@code limit} determines the amount of messages retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAfter(messageId, 100)} - This will retrieve 100 messages from history sent after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the message that will act as a marker.
     * @param  limit
     *         The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is {@code null} or empty.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages after the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryAfter(MessageChannel, String) MessageHistory.getHistoryAfter(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@Nonnull String messageId, int limit)
    {
        return MessageHistory.getHistoryAfter(this, messageId).limit(limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent after
     * the marker ID. The {@code limit} determines the amount of messages retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAfter(messageId, 100)} - This will retrieve 100 messages from history sent after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the message that will act as a marker.
     * @param  limit
     *         The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         Provided {@code limit} is less than {@code 1} or greater than {@code 100}.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages after the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryAfter(MessageChannel, String) MessageHistory.getHistoryAfter(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(long messageId, int limit)
    {
        return getHistoryAfter(Long.toUnsignedString(messageId), limit);
    }

    /**
     * Uses the provided message as a marker and retrieves messages sent after
     * the marker. The {@code limit} determines the amount of messages retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAfter(message, 100)} - This will retrieve 100 messages from history sent after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  message
     *         The message that will act as a marker.
     * @param  limit
     *         The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code message} is {@code null}.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages after the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryAfter(MessageChannel, String) MessageHistory.getHistoryAfter(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@Nonnull Message message, int limit)
    {
        Checks.notNull(message, "Message");
        return getHistoryAfter(message.getId(), limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent before
     * the marker ID. The {@code limit} determines the amount of messages retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryBefore(messageId, 100)} - This will retrieve 100 messages from history sent before the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the message that will act as a marker.
     * @param  limit
     *         The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is {@code null} or empty.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages before the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryBefore(MessageChannel, String) MessageHistory.getHistoryBefore(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@Nonnull String messageId, int limit)
    {
        return MessageHistory.getHistoryBefore(this, messageId).limit(limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent before
     * the marker ID. The {@code limit} determines the amount of messages retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryBefore(messageId, 100)} - This will retrieve 100 messages from history sent before the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the message that will act as a marker.
     * @param  limit
     *         The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is {@code null} or empty.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages before the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryBefore(MessageChannel, String) MessageHistory.getHistoryBefore(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(long messageId, int limit)
    {
        return getHistoryBefore(Long.toUnsignedString(messageId), limit);
    }

    /**
     * Uses the provided message as a marker and retrieves messages sent before
     * the marker. The {@code limit} determines the amount of messages retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAfter(message, 100)} - This will retrieve 100 messages from history sent before the marker.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  message
     *         The message that will act as a marker.
     * @param  limit
     *         The amount of messages to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code message} is {@code null}.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with messages before the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryBefore(MessageChannel, String) MessageHistory.getHistoryBefore(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@Nonnull Message message, int limit)
    {
        Checks.notNull(message, "Message");
        return getHistoryBefore(message.getId(), limit);
    }

    /**
     * Retrieves messages from the beginning of this {@link MessageChannel MessageChannel}.
     * The {@code limit} determines the amount of messages being retrieved.
     *
     * <p><b>Example</b><br>
     * <pre><code>
     * public void resendFirstMessage(MessageChannel channel)
     * {
     *     channel.getHistoryFromBeginning(1).queue(history {@literal ->}
     *     {
     *         if (!history.isEmpty())
     *         {
     *             Message firstMsg = history.getRetrievedHistory().get(0);
     *             channel.sendMessage(firstMsg).queue();
     *         }
     *         else
     *             channel.sendMessage("No history for this channel!").queue();
     *     });
     * }
     * </code></pre>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  limit
     *         The amount of messages to be retrieved. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         Provided {@code limit} is less than {@code 1} or greater than {@code 100}.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object with with the first messages of this channel loaded into it.
     *         <br><b>Note: The messages are ordered from the most recent to oldest!</b>
     *
     * @see    net.dv8tion.jda.api.entities.MessageHistory#retrieveFuture(int)                     MessageHistory.retrieveFuture(int)
     * @see    net.dv8tion.jda.api.entities.MessageHistory#getHistoryAfter(MessageChannel, String) MessageHistory.getHistoryAfter(MessageChannel, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryFromBeginning(int limit)
    {
        return MessageHistory.getHistoryFromBeginning(this).limit(limit);
    }

    /**
     * Sends the typing status to discord. This is what is used to make the message "X is typing..." appear.
     * <br>The typing status only lasts for 10 seconds or until a message is sent.
     * <br>So if you wish to show continuous typing you will need to call this method once every 10 seconds.
     *
     * <p>The official discord client sends this every 5 seconds even though the typing status lasts 10.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Void
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> sendTyping()
    {
        Route.CompiledRoute route = Route.Channels.SEND_TYPING.compile(getId());
        return new RestActionImpl<>(getJDA(), route);
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to attach the reaction to
     * @param  emoji
     *         The not-null {@link Emoji} to react with
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emoji} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link GuildMessageChannel GuildMessageChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull Emoji emoji)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(emoji, "Emoji");

        Route.CompiledRoute route = Route.Messages.ADD_REACTION.compile(getId(), messageId, emoji.getAsReactionCode(), "@me");
        return new RestActionImpl<>(getJDA(), route);
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to attach the reaction to
     * @param  emoji
     *         The {@link Emoji} to react with
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided {@code emoji} is {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link GuildMessageChannel GuildMessageChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addReactionById(long messageId, @Nonnull Emoji emoji)
    {
        return addReactionById(Long.toUnsignedString(messageId), emoji);
    }

    /**
     * Attempts to remove the reaction from a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to remove the reaction from
     * @param  emoji
     *         The emoji to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or not a valid snowflake.</li>
     *             <li>If provided {@code emoji} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(@Nonnull String messageId, @Nonnull Emoji emoji)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(emoji, "Emoji");

        Route.CompiledRoute route = Route.Messages.REMOVE_REACTION.compile(getId(), messageId, emoji.getAsReactionCode(), "@me");
        return new RestActionImpl<>(getJDA(), route);
    }

    /**
     * Attempts to remove the reaction from a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to remove the reaction from
     * @param  emoji
     *         The emoji to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided {@code emoji} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(long messageId, @Nonnull Emoji emoji)
    {
        return removeReactionById(Long.toUnsignedString(messageId), emoji);
    }

    /**
     * This obtains the {@link net.dv8tion.jda.api.entities.User users} who reacted to a message using the given {@link Emoji}.
     *
     * <p>Messages maintain a list of reactions, alongside a list of users who added them.
     *
     * <p>Using this data, we can obtain a {@link ReactionPaginationAction ReactionPaginationAction}
     * of the users who've reacted to the given message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The retrieve request was attempted after the account lost access to the {@link GuildMessageChannel GuildMessageChannel}
     *         due to {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} being revoked
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to retrieve the users from.
     * @param  emoji
     *         The {@link Emoji} to retrieve users for.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the
     *         logged in account does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}.
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or not a valid snowflake.</li>
     *             <li>If provided {@code emoji} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return The {@link ReactionPaginationAction} of the emoji's users.
     */
    @Nonnull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(@Nonnull String messageId, @Nonnull Emoji emoji)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(emoji, "Emoji");

        return new ReactionPaginationActionImpl(this, messageId, emoji.getAsReactionCode());
    }

    /**
     * This obtains the {@link net.dv8tion.jda.api.entities.User users} who reacted to a message using the given {@link Emoji}.
     *
     * <p>Messages maintain a list of reactions, alongside a list of users who added them.
     *
     * <p>Using this data, we can obtain a {@link ReactionPaginationAction ReactionPaginationAction}
     * of the users who've reacted to the given message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The retrieve request was attempted after the account lost access to the {@link GuildMessageChannel GuildMessageChannel}
     *         due to {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} being revoked
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emoji was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to retrieve the users from.
     * @param  emoji
     *         The {@link Emoji} to retrieve users for.
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.api.entities.MessageType#DEFAULT MessageType.DEFAULT}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the
     *         logged in account does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}.
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided {@code emoji} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return The {@link ReactionPaginationAction ReactionPaginationAction} of the emoji's users.
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(long messageId, @Nonnull Emoji emoji)
    {
        return retrieveReactionUsersById(Long.toUnsignedString(messageId), emoji);
    }

    /**
     * Used to pin a message. Pinned messages are retrievable via {@link #retrievePinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message to pin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is {@code null} or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> pinMessageById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        Route.CompiledRoute route = Route.Messages.ADD_PINNED_MESSAGE.compile(getId(), messageId);
        return new RestActionImpl<>(getJDA(), route);
    }

    /**
     * Used to pin a message. Pinned messages are retrievable via {@link #retrievePinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message to pin.
     *
     * @throws IllegalArgumentException
     *         if the provided {@code messageId} is not a valid snowflake.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> pinMessageById(long messageId)
    {
        return pinMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * Used to unpin a message. Pinned messages are retrievable via {@link #retrievePinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message to unpin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is {@code null} or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> unpinMessageById(@Nonnull String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        Route.CompiledRoute route = Route.Messages.REMOVE_PINNED_MESSAGE.compile(getId(), messageId);
        return new RestActionImpl<Void>(getJDA(), route);
    }

    /**
     * Used to unpin a message. Pinned messages are retrievable via {@link #retrievePinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link GuildMessageChannel GuildMessageChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message to unpin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is not positive.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> unpinMessageById(long messageId)
    {
        return unpinMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * Retrieves a List of {@link net.dv8tion.jda.api.entities.Message Messages} that have been pinned in this channel.
     * <br>If no messages have been pinned, this retrieves an empty List.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.Message}{@literal >}
     *         <br>Retrieves an immutable list of pinned messages
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<@Unmodifiable List<Message>> retrievePinnedMessages()
    {
        JDAImpl jda = (JDAImpl) getJDA();
        Route.CompiledRoute route = Route.Messages.GET_PINNED_MESSAGES.compile(getId());
        return new RestActionImpl<>(jda, route, (response, request) ->
        {
            EntityBuilder builder = jda.getEntityBuilder();
            DataArray pins = response.getArray();
            List<Message> pinnedMessages = new ArrayList<>(pins.length());

            for (int i = 0; i < pins.length(); i++)
            {
                try
                {
                    pinnedMessages.add(builder.createMessageWithChannel(pins.getObject(i), MessageChannel.this, false));
                }
                catch (ParsingException | NullPointerException e)
                {
                    JDALogger.getLog(getClass()).error("Failed to parse pinned message", e);
                }
            }

            return Collections.unmodifiableList(pinnedMessages);
        });
    }

    /**
     * Attempts to edit a message by its id in this channel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  newContent
     *         The new content for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code newContent} is {@code null} or empty.</li>
     *             <li>If provided {@code newContent} length is greater than {@value Message#MAX_CONTENT_LENGTH} characters.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageById(@Nonnull String messageId, @Nonnull CharSequence newContent)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notEmpty(newContent, "Provided message content");
        Checks.check(newContent.length() <= Message.MAX_CONTENT_LENGTH, "Provided newContent length must be %d or less characters.", Message.MAX_CONTENT_LENGTH);
        return new MessageEditActionImpl(this, messageId).setContent(newContent.toString());
    }

    /**
     * Attempts to edit a message by its id in this channel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  newContent
     *         The new content for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code newContent} is {@code null} or empty.</li>
     *             <li>If provided {@code newContent} length is greater than {@value Message#MAX_CONTENT_LENGTH} characters.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageById(long messageId, @Nonnull CharSequence newContent)
    {
        return editMessageById(Long.toUnsignedString(messageId), newContent);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  data
     *         The new content for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code newContent} is {@code null}.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.Message Message}
     *                 contains a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageById(@Nonnull String messageId, @Nonnull MessageEditData data)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(data, "message");
        return new MessageEditActionImpl(this, messageId).applyData(data);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  data
     *         The new content for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code newContent} is {@code null}.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.Message Message}
     *                 contains a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageById(long messageId, @Nonnull MessageEditData data)
    {
        return editMessageById(Long.toUnsignedString(messageId), data);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  format
     *         Format String used to generate new Content
     * @param  args
     *         The arguments which should be used to format the given format String
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code format} is {@code null} or blank.</li>
     *         </ul>
     * @throws IllegalStateException
     *         If the resulting message is either empty or too long to be sent
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageFormatById(@Nonnull String messageId, @Nonnull String format, @Nonnull Object... args)
    {
        Checks.notBlank(format, "Format String");
        return editMessageById(messageId, String.format(format, args));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  format
     *         Format String used to generate new Content
     * @param  args
     *         The arguments which should be used to format the given format String
     *
     * @throws IllegalArgumentException
     *         If provided {@code format} is {@code null} or blank.
     * @throws IllegalStateException
     *         If the resulting message is either empty or too long to be sent
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageFormatById(long messageId, @Nonnull String format, @Nonnull Object... args)
    {
        Checks.notBlank(format, "Format String");
        return editMessageById(messageId, String.format(format, args));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  newEmbeds
     *         Up to {@value Message#MAX_EMBED_COUNT} new {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageEmbedsById(@Nonnull String messageId, @Nonnull MessageEmbed... newEmbeds)
    {
        Checks.noneNull(newEmbeds, "MessageEmbeds");
        return editMessageEmbedsById(messageId, Arrays.asList(newEmbeds));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  newEmbeds
     *         Up to {@value Message#MAX_EMBED_COUNT} new {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageEmbedsById(long messageId, @Nonnull MessageEmbed... newEmbeds)
    {
        return editMessageEmbedsById(Long.toUnsignedString(messageId), newEmbeds);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  newEmbeds
     *         Up to {@value Message#MAX_EMBED_COUNT} new {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageEmbedsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageEmbed> newEmbeds)
    {
        Checks.isSnowflake(messageId, "Message ID");
        return new MessageEditActionImpl(this, messageId).setEmbeds(newEmbeds);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  newEmbeds
     *         Up to {@value Message#MAX_EMBED_COUNT} new {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageEmbedsById(long messageId, @Nonnull Collection<? extends MessageEmbed> newEmbeds)
    {
        return editMessageEmbedsById(Long.toUnsignedString(messageId), newEmbeds);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>This will replace all the current {@link net.dv8tion.jda.api.interactions.components.Component Components},
     * such as {@link Button Buttons} or {@link SelectMenu SelectMenus} on this message.
     * The provided parameters are {@link LayoutComponent LayoutComponents} such as {@link ActionRow} which contain a list of components to arrange in the respective layout.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * List<ActionRow> rows = Arrays.asList(
     *   ActionRow.of(Button.success("prompt:accept", "Accept"), Button.danger("prompt:reject", "Reject")), // 1st row below message
     *   ActionRow.of(Button.link(url, "Help")) // 2nd row below message
     * );
     * channel.editMessageComponentsById(messageId, rows).queue();
     * }</pre>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  components
     *         Up to 5 new {@link LayoutComponent LayoutComponents} for the edited message, such as {@link ActionRow}
     *
     * @throws UnsupportedOperationException
     *         If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If any of the provided {@link LayoutComponent LayoutComponents} is null</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageComponentsById(@Nonnull String messageId, @Nonnull Collection<? extends LayoutComponent> components)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.noneNull(components, "Components");
        if (components.stream().anyMatch(x -> !(x instanceof ActionRow)))
            throw new UnsupportedOperationException("The provided component layout is not supported");
        List<ActionRow> actionRows = components.stream().map(ActionRow.class::cast).collect(Collectors.toList());
        return new MessageEditActionImpl(this, messageId).setComponents(actionRows);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>This will replace all the current {@link net.dv8tion.jda.api.interactions.components.Component Components},
     * such as {@link Button Buttons} or {@link SelectMenu SelectMenus} on this message.
     * The provided parameters are {@link LayoutComponent LayoutComponents} such as {@link ActionRow} which contain a list of components to arrange in the respective layout.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * List<ActionRow> rows = Arrays.asList(
     *   ActionRow.of(Button.success("prompt:accept", "Accept"), Button.danger("prompt:reject", "Reject")), // 1st row below message
     *   ActionRow.of(Button.link(url, "Help")) // 2nd row below message
     * );
     * channel.editMessageComponentsById(messageId, rows).queue();
     * }</pre>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  components
     *         Up to 5 new {@link LayoutComponent LayoutComponents} for the edited message, such as {@link ActionRow}
     *
     * @throws UnsupportedOperationException
     *         If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *         If any of the provided {@link LayoutComponent LayoutComponents} is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageComponentsById(long messageId, @Nonnull Collection<? extends LayoutComponent> components)
    {
        return editMessageComponentsById(Long.toUnsignedString(messageId), components);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>This will replace all the current {@link net.dv8tion.jda.api.interactions.components.Component Components},
     * such as {@link Button Buttons} or {@link SelectMenu SelectMenus} on this message.
     * The provided parameters are {@link LayoutComponent LayoutComponents} such as {@link ActionRow} which contain a list of components to arrange in the respective layout.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * channel.editMessageComponentsById(messageId,
     *   ActionRow.of(Button.success("prompt:accept", "Accept"), Button.danger("prompt:reject", "Reject")), // 1st row below message
     *   ActionRow.of(Button.link(url, "Help")) // 2nd row below message
     * ).queue();
     * }</pre>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  components
     *         Up to 5 new {@link LayoutComponent LayoutComponents} for the edited message, such as {@link ActionRow}
     *
     * @throws UnsupportedOperationException
     *         If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If any of the provided {@link LayoutComponent LayoutComponents} is null</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageComponentsById(@Nonnull String messageId, @Nonnull LayoutComponent... components)
    {
        Checks.noneNull(components, "Components");
        return editMessageComponentsById(messageId, Arrays.asList(components));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>This will replace all the current {@link net.dv8tion.jda.api.interactions.components.Component Components},
     * such as {@link Button Buttons} or {@link SelectMenu SelectMenus} on this message.
     * The provided parameters are {@link LayoutComponent LayoutComponents} such as {@link ActionRow} which contain a list of components to arrange in the respective layout.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * channel.editMessageComponentsById(messageId,
     *   ActionRow.of(Button.success("prompt:accept", "Accept"), Button.danger("prompt:reject", "Reject")), // 1st row below message
     *   ActionRow.of(Button.link(url, "Help")) // 2nd row below message
     * ).queue();
     * }</pre>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  components
     *         Up to 5 new {@link LayoutComponent LayoutComponents} for the edited message, such as {@link ActionRow}
     *
     * @throws UnsupportedOperationException
     *         If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *         If any of the provided {@link LayoutComponent LayoutComponents} is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageComponentsById(long messageId, @Nonnull LayoutComponent... components)
    {
        Checks.noneNull(components, "Components");
        return editMessageComponentsById(messageId, Arrays.asList(components));
    }


    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If any of the provided files is bigger than {@link Guild#getMaxFileSize()}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction} that can be used to further update the message
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageAttachmentsById(@Nonnull String messageId, @Nonnull Collection<? extends AttachedFile> attachments)
    {
        Checks.isSnowflake(messageId, "Message ID");
        return new MessageEditActionImpl(this, messageId).setAttachments(attachments);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If any of the provided files is bigger than {@link Guild#getMaxFileSize()}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction} that can be used to further update the message
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageAttachmentsById(@Nonnull String messageId, @Nonnull AttachedFile... attachments)
    {
        Checks.noneNull(attachments, "Attachments");
        return editMessageAttachmentsById(messageId, Arrays.asList(attachments));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If any of the provided files is bigger than {@link Guild#getMaxFileSize()}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction} that can be used to further update the message
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageAttachmentsById(long messageId, @Nonnull Collection<? extends AttachedFile> attachments)
    {
        return editMessageAttachmentsById(Long.toUnsignedString(messageId), attachments);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If any of the provided files is bigger than {@link Guild#getMaxFileSize()}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link MessageEditAction} that can be used to further update the message
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageEditAction editMessageAttachmentsById(long messageId, @Nonnull AttachedFile... attachments)
    {
        return editMessageAttachmentsById(Long.toUnsignedString(messageId), attachments);
    }
}
