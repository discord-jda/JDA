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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.exceptions.AccountTypeException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentLayout;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.MessagePaginationActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a Discord channel that can have {@link net.dv8tion.jda.api.entities.Message Messages} and files sent to it.
 *
 * <h1>Formattable</h1>
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
 * <br><b>{@link net.dv8tion.jda.api.entities.TextChannel TextChannel} is a special case which uses {@link IMentionable#getAsMention() IMentionable.getAsMention()}
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
     * <br>This should only be used if {@link #hasLatestMessage()} returns {@code true}!
     *
     * <p>This value is updated on each {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}
     * and <u><b>will be reset to {@code null} if the message associated with this ID gets deleted</b></u>
     *
     * @throws java.lang.IllegalStateException
     *         If no message id is available
     *
     * @return The most recent message's id
     */
    @Nonnull
    //TODO-v5: Revisit this. Surely this should be Nullable instead of throw an exception...
    default String getLatestMessageId()
    {
        return Long.toUnsignedString(getLatestMessageIdLong());
    }


    /**
     * The id for the most recent message sent
     * in this current MessageChannel.
     * <br>This should only be used if {@link #hasLatestMessage()} returns {@code true}!
     *
     * <p>This value is updated on each {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}
     * and <u><b>will be reset to {@code null} if the message associated with this ID gets deleted</b></u>
     *
     * @throws java.lang.IllegalStateException
     *         If no message id is available
     *
     * @return The most recent message's id
     */
    long getLatestMessageIdLong();

    /**
     * Whether this MessageChannel contains a tracked most recent
     * message or not.
     *
     * <p>This does not directly mean that {@link #getHistory()} will be unable to retrieve past messages,
     * it merely means that the latest message is untracked by our internal cache meaning that
     * if this returns {@code false} the {@link #getLatestMessageId()}
     * method will throw an {@link java.util.NoSuchElementException NoSuchElementException}
     *
     * @return True, if a latest message id is available for retrieval by {@link #getLatestMessageId()}
     *
     * @see    #getLatestMessageId()
     */
    default boolean hasLatestMessage()
    {
        return getLatestMessageIdLong() != 0;
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br>This combines both {@link TextChannel#deleteMessagesByIds(Collection)} as well as {@link #deleteMessageById(long)}
     * to delete all messages provided. No checks will be done to prevent failures, use {@link java.util.concurrent.CompletionStage#exceptionally(Function)}
     * to handle failures.
     *
     * <p>For possible ErrorResponses see {@link #purgeMessagesById(long...)}.
     *
     * @param  messageIds
     *         The message ids to delete
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
     * <br>This combines both {@link TextChannel#deleteMessagesByIds(Collection)} as well as {@link #deleteMessageById(long)}
     * to delete all messages provided. No checks will be done to prevent failures, use {@link java.util.concurrent.CompletionStage#exceptionally(Function)}
     * to handle failures.
     *
     * <p>For possible ErrorResponses see {@link #purgeMessagesById(long...)}.
     *
     * @param  messageIds
     *         The message ids to delete
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
     * <br>This combines both {@link TextChannel#deleteMessagesByIds(Collection)} as well as {@link Message#delete()}
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
     * <br>This combines both {@link TextChannel#deleteMessagesByIds(Collection)} as well as {@link Message#delete()}
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
        long[] ids = new long[messages.size()];
        for (int i = 0; i < ids.length; i++)
            ids[i] = messages.get(i).getIdLong();
        return purgeMessagesById(ids);
    }

    /**
     * Convenience method to delete messages in the most efficient way available.
     * <br>This combines both {@link TextChannel#deleteMessagesByIds(Collection)} as well as {@link #deleteMessageById(long)}
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
     * Sends a plain text message to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} use
     * {@link net.dv8tion.jda.api.entities.Member#hasPermission(GuildChannel, net.dv8tion.jda.api.Permission...)
     *  guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND)}.
     *
     * <p>For {@link net.dv8tion.jda.api.requests.ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param  text
     *         the text to send to the MessageChannel.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does
     *         not have {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws java.lang.IllegalArgumentException
     *         if the provided text is null, empty or longer than 2000 characters
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     *
     * @see net.dv8tion.jda.api.MessageBuilder
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessage(@Nonnull CharSequence text)
    {
        Checks.notEmpty(text, "Provided text for message");
        Checks.check(text.length() <= Message.MAX_CONTENT_LENGTH, "Provided text for message must be less than %d characters in length", Message.MAX_CONTENT_LENGTH);

        if (text instanceof StringBuilder)
            return new MessageActionImpl(getJDA(), null, this, (StringBuilder) text);
        else
            return new MessageActionImpl(getJDA(), null, this).append(text);
    }

    /**
     * Sends a formatted text message to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} use
     * {@link net.dv8tion.jda.api.entities.Member#hasPermission(GuildChannel, net.dv8tion.jda.api.Permission...)
     *  guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND)}.
     *
     * <p>For {@link net.dv8tion.jda.api.requests.ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param  format
     *         The string that should be formatted, if this is {@code null} or empty
     *         the content of the Message would be empty and cause a builder exception.
     * @param  args
     *         The arguments for your format
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does
     *         not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *         </ul>
     * @throws java.lang.IllegalArgumentException
     *         If the provided format text is {@code null}, empty or longer than 2000 characters
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notEmpty(format, "Format");
        return sendMessage(String.format(format, args));
    }

    /**
     * Sends up to 10 specified {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} as a {@link net.dv8tion.jda.api.entities.Message Message}
     * to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} use
     * {@link net.dv8tion.jda.api.entities.Member#hasPermission(GuildChannel, net.dv8tion.jda.api.Permission...)
     *  guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND)}.
     *
     * <p>For {@link net.dv8tion.jda.api.requests.ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param  embed
     *         The {@link MessageEmbed MessageEmbed} to send
     * @param  other
     *         Additional {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to send
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does
     *         not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS}</li>
     *         </ul>
     * @throws java.lang.IllegalArgumentException
     *         If null is provided, any of the embeds are not {@link MessageEmbed#isSendable() sendable}, more than 10 embeds are provided,
     *         or the sum of {@link MessageEmbed#getLength()} is greater than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     *
     * @see    net.dv8tion.jda.api.MessageBuilder
     * @see    net.dv8tion.jda.api.EmbedBuilder
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessageEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other)
    {
        Checks.notNull(embed, "MessageEmbeds");
        Checks.noneNull(other, "MessageEmbeds");
        List<MessageEmbed> embeds = new ArrayList<>(1 + other.length);
        embeds.add(embed);
        Collections.addAll(embeds, other);
        return new MessageActionImpl(getJDA(), null, this).setEmbeds(embeds);
    }

    /**
     * Sends up to 10 specified {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} as a {@link net.dv8tion.jda.api.entities.Message Message}
     * to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} use
     * {@link net.dv8tion.jda.api.entities.Member#hasPermission(GuildChannel, net.dv8tion.jda.api.Permission...)
     *  guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND)}.
     *
     * <p>For {@link net.dv8tion.jda.api.requests.ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param  embeds
     *         The {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to send
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does
     *         not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS}</li>
     *         </ul>
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided embeds is {@code null} or if the provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *         is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     *
     * @see    net.dv8tion.jda.api.MessageBuilder
     * @see    net.dv8tion.jda.api.EmbedBuilder
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return new MessageActionImpl(getJDA(), null, this).setEmbeds(embeds);
    }

    /**
     * Sends a specified {@link net.dv8tion.jda.api.entities.Message Message} to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} use
     * {@link net.dv8tion.jda.api.entities.Member#hasPermission(GuildChannel, net.dv8tion.jda.api.Permission...)
     *  guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND)}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND} in
     *         the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  msg
     *         the {@link net.dv8tion.jda.api.entities.Message Message} to send
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does
     *         not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS} (if this message is only an embed)</li>
     *         </ul>
     * @throws java.lang.IllegalArgumentException
     *         If the provided message is {@code null} or the provided {@link net.dv8tion.jda.api.entities.Message Message}
     *         contains a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *         that is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     *
     * @see    net.dv8tion.jda.api.MessageBuilder
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendMessage(@Nonnull Message msg)
    {
        Checks.notNull(msg, "Message");
        return new MessageActionImpl(getJDA(), null, this).apply(msg);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.api.entities.Message Message} with the uploaded file.
     * <br>If you want to send a Message with the uploaded file, you can add the file to the {@link net.dv8tion.jda.api.requests.restaction.MessageAction}
     * returned by {@link #sendMessage(Message)}.
     *
     * <p>This is a shortcut to {@link #sendFile(java.io.File, String, AttachmentOption...)} by way of using {@link java.io.File#getName()}.
     * <pre>sendFile(file, file.getName())</pre>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u> you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * EmbedBuilder embed = new EmbedBuilder();
     * File file = new File("cat.gif");
     * embed.setImage("attachment://cat.gif")
     *      .setDescription("This is a cute cat :3");
     * channel.sendFile(file).setEmbeds(embed.build()).queue();
     * </code></pre>
     *
     * <p>For {@link net.dv8tion.jda.api.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, AttachmentOption...)}.
     *
     * @param  file
     *         The file to upload to the {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code file} is null.</li>
     *             <li>Provided {@code file} does not exist.</li>
     *             <li>Provided {@code file} is unreadable.</li>
     *             <li>Provided {@code file} is greater than 8 MiB on a normal or 50 MiB on a nitro account.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>Providing the {@link net.dv8tion.jda.api.entities.Message Message} created from this upload.
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendFile(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "file");
        return sendFile(file, file.getName(), options);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.api.entities.Message Message} with the uploaded file.
     * <br>If you want to send a Message with the uploaded file, you can add the file to the {@link net.dv8tion.jda.api.requests.restaction.MessageAction}
     * returned by {@link #sendMessage(Message)}.
     *
     * <p>The {@code fileName} parameter is used to inform Discord about what the file should be called. This is 2 fold:
     * <ol>
     *     <li>The file name provided is the name that is found in {@link net.dv8tion.jda.api.entities.Message.Attachment#getFileName()}
     *          after upload and it is the name that will show up in the client when the upload is displayed.
     *     <br>Note: The fileName does not show up on the Desktop client for images. It does on mobile however.</li>
     *     <li>The extension of the provided fileName also determines how Discord will treat the file. Discord currently only
     *         has special handling for image file types, but the fileName's extension must indicate that it is an image file.
     *         This means it has to end in something like .png, .jpg, .jpeg, .gif, etc. As a note, you can also not provide
     *         a full name for the file and instead ONLY provide the extension like "png" or "gif" and Discord will generate
     *         a name for the upload and append the fileName as the extension.</li>
     * </ol>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u> you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * EmbedBuilder embed = new EmbedBuilder();
     * File file = new File("cat_01.gif");
     * embed.setImage("attachment://cat.gif") // we specify this in sendFile as "cat.gif"
     *      .setDescription("This is a cute cat :3");
     * channel.sendFile(file, "cat.gif").setEmbeds(embed.build()).queue();
     * </code></pre>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The send request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND} or
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *         in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  file
     *         The file to upload to the {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code file} is null.</li>
     *             <li>Provided {@code file} does not exist.</li>
     *             <li>Provided {@code file} is unreadable.</li>
     *             <li>Provided {@code file} is greater than 8 MiB on a normal or 50 MiB on a nitro account.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>Providing the {@link net.dv8tion.jda.api.entities.Message Message} created from this upload.
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendFile(@Nonnull File file, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(file, "file");
        Checks.check(file.exists() && file.canRead(),
                    "Provided file doesn't exist or cannot be read!");
        Checks.notNull(fileName, "fileName");

        try
        {
            return sendFile(new FileInputStream(file), fileName, options);
        }
        catch (FileNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.api.entities.Message Message} with the uploaded file.
     * <br>If you want to send a Message with the uploaded file, you can add the file to the {@link net.dv8tion.jda.api.requests.restaction.MessageAction}
     * returned by {@link #sendMessage(Message)}.
     * <br>This allows you to send an {@link java.io.InputStream InputStream} as substitute to a file.
     *
     * <p>For information about the {@code fileName} parameter, Refer to the documentation for {@link #sendFile(java.io.File, String, AttachmentOption...)}.
     * <br>For {@link net.dv8tion.jda.api.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, AttachmentOption...)}.
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u> you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * EmbedBuilder embed = new EmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
     *      .setDescription("This is a cute cat :3");
     * channel.sendFile(file, "cat.png").setEmbeds(embed.build()).queue();
     * </code></pre>
     *
     * @param  data
     *         The InputStream data to upload to the {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     *         <br>Refer to the documentation for {@link #sendFile(java.io.File, String, AttachmentOption...)} for information about this parameter.
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file or filename is {@code null} or {@code empty}.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>Provides the {@link net.dv8tion.jda.api.entities.Message Message} created from this upload.
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendFile(@Nonnull InputStream data, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "data InputStream");
        Checks.notNull(fileName, "fileName");
        return new MessageActionImpl(getJDA(), null, this).addFile(data, fileName, options);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.api.entities.Message Message} with the uploaded file.
     * <br>If you want to send a Message with the uploaded file, you can add the file to the {@link net.dv8tion.jda.api.requests.restaction.MessageAction}
     * returned by {@link #sendMessage(Message)}.
     * <br>This allows you to send an {@code byte[]} as substitute to a file.
     *
     * <p>For information about the {@code fileName} parameter, Refer to the documentation for {@link #sendFile(java.io.File, String, AttachmentOption...)}.
     * <br>For {@link net.dv8tion.jda.api.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, AttachmentOption...)}.
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u> you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * EmbedBuilder embed = new EmbedBuilder();
     * byte[] file = IOUtil.readFully(new URL("https://http.cat/500").openStream());
     * embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
     *      .setDescription("This is a cute cat :3");
     * channel.sendFile(file, "cat.png").setEmbeds(embed.build()).queue();
     * </code></pre>
     *
     * @param  data
     *         The {@code byte[]} data to upload to the {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord.
     *         <br>Refer to the documentation for {@link #sendFile(java.io.File, String, AttachmentOption...)} for information about this parameter.
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided filename is {@code null} or {@code empty}</li>
     *             <li>If the provided data is larger than 8 MiB on a normal or 50 MiB on a nitro account</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>Provides the {@link net.dv8tion.jda.api.entities.Message Message} created from this upload.
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction sendFile(@Nonnull byte[] data, @Nonnull String fileName, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "data");
        Checks.notNull(fileName, "fileName");

        return sendFile(new ByteArrayInputStream(data), fileName, options);
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.api.AccountType#BOT AccountType.BOT}
     * @throws IllegalArgumentException
     *         if the provided {@code messageId} is null or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Message
     *         <br>The Message defined by the provided id.
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Message> retrieveMessageById(@Nonnull String messageId)
    {
        AccountTypeException.check(getJDA().getAccountType(), AccountType.BOT);
        Checks.isSnowflake(messageId, "Message ID");

        JDAImpl jda = (JDAImpl) getJDA();
        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getId(), messageId);
        return new RestActionImpl<>(jda, route,
            (response, request) -> jda.getEntityBuilder().createMessage(response.getObject(), MessageChannel.this, false));
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     * @throws net.dv8tion.jda.api.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.api.AccountType#BOT AccountType.BOT}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request attempted to delete a Message in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_DM_ACTION INVALID_DM_ACTION}
     *     <br>Attempted to delete a Message in a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request attempted to delete a Message in a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_DM_ACTION INVALID_DM_ACTION}
     *     <br>Attempted to delete a Message in a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}.
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
     * Creates a new {@link net.dv8tion.jda.api.entities.MessageHistory MessageHistory} object for each call of this method.
     * <br>MessageHistory is <b>NOT</b> an internal message cache, but rather it queries the Discord servers for previously sent messages.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         and the currently logged in account does not have the permission {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY}
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
     * <h1>Examples</h1>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         and the currently logged in account does not have the permission {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY}
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     * Retrieves messages from the beginning of this {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * The {@code limit} determines the amount of messages being retrieved.
     *
     * <h2>Example</h2>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *         </ul>
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
     * <p>The unicode provided has to be a unicode representation of the emoji
     * that is supposed to be used for the Reaction.
     * <br>To retrieve the characters needed you can use an api or
     * the official discord client by escaping the emoji (\:emoji-name:)
     * and copying the resulting emoji from the sent message.
     *
     * <p>This method encodes the provided unicode for you.
     * <b>Do not encode the emoji before providing the unicode.</b>
     *
     * <h2>Examples</h2>
     * <code>
     * // custom<br>
     * channel.addReactionById(messageId, "minn:245267426227388416").queue();<br>
     * // unicode escape<br>
     * channel.addReactionById(messageId, "&#92;uD83D&#92;uDE02").queue();<br>
     * // codepoint notation<br>
     * channel.addReactionById(messageId, "U+1F602").queue();
     * </code>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found here:
     *         <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a></li>
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
     * @param  unicode
     *         The unicode characters to react with
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or not a valid snowflake.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         and the logged in account does not have:
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull String unicode)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(unicode, "Provided Unicode");
        unicode = unicode.trim();
        Checks.notEmpty(unicode, "Provided Unicode");

        final String encoded = EncodingUtil.encodeReaction(unicode);

        Route.CompiledRoute route = Route.Messages.ADD_REACTION.compile(getId(), messageId, encoded, "@me");
        return new RestActionImpl<>(getJDA(), route);
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The unicode provided has to be a unicode representation of the emoji
     * that is supposed to be used for the Reaction.
     * <br>To retrieve the characters needed you can use an api or
     * the official discord client by escaping the emoji (\:emoji-name:)
     * and copying the resulting emoji from the sent message.
     *
     * <p>This method encodes the provided unicode for you.
     * <b>Do not encode the emoji before providing the unicode.</b>
     *
     * <h2>Examples</h2>
     * <code>
     * // custom<br>
     * channel.addReactionById(messageId, "minn:245267426227388416").queue();<br>
     * // unicode escape<br>
     * channel.addReactionById(messageId, "&#92;uD83D&#92;uDE02").queue();<br>
     * // codepoint notation<br>
     * channel.addReactionById(messageId, "U+1F602").queue();
     * </code>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found here:
     *         <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a></li>
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
     * @param  unicode
     *         The unicode characters to react with
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         and the logged in account does not have:
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addReactionById(long messageId, @Nonnull String unicode)
    {
        return addReactionById(Long.toUnsignedString(messageId), unicode);
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p><b>An Emote is not the same as an emoji!</b>
     * <br>Emotes are custom guild-specific images unlike global unicode emojis!
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</u></b>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emote was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
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
     * @param  emote
     *         The not-null {@link net.dv8tion.jda.api.entities.Emote} to react with
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emote} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addReactionById(@Nonnull String messageId, @Nonnull Emote emote)
    {
        Checks.notNull(emote, "Emote");
        return addReactionById(messageId, emote.getName() + ":" + emote.getId());
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p><b>An Emote is not the same as an emoji!</b>
     * <br>Emotes are custom guild-specific images unlike global unicode emojis!
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</u></b>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emote was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
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
     * @param  emote
     *         The not-null {@link net.dv8tion.jda.api.entities.Emote} to react with
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided {@code emote} is {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> addReactionById(long messageId, @Nonnull Emote emote)
    {
        return addReactionById(Long.toUnsignedString(messageId), emote);
    }

    /**
     * Attempts to remove the reaction from a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The unicode provided has to be a unicode representation of the emoji
     * that is supposed to be represented by the Reaction.
     * <br>To retrieve the characters needed you can use an api or
     * the official discord client by escaping the emoji (\:emoji-name:)
     * and copying the resulting emoji from the sent message.
     *
     * <p>This method encodes the provided unicode for you.
     * <b>Do not encode the emoji before providing the unicode.</b>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found here:
     *         <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a></li>
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
     * @param  unicode
     *         The unicode characters of the emoji
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or not a valid snowflake.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(@Nonnull String messageId, @Nonnull String unicode)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(unicode, "Provided Unicode");
        unicode = unicode.trim();
        Checks.notEmpty(unicode, "Provided Unicode");

        final String encoded = EncodingUtil.encodeReaction(unicode);

        final Route.CompiledRoute route = Route.Messages.REMOVE_REACTION.compile(getId(), messageId, encoded, "@me");
        return new RestActionImpl<>(getJDA(), route);
    }

    /**
     * Attempts to remove the reaction from a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The unicode provided has to be a unicode representation of the emoji
     * that is supposed to be represented by the Reaction.
     * <br>To retrieve the characters needed you can use an api or
     * the official discord client by escaping the emoji (\:emoji-name:)
     * and copying the resulting emoji from the sent message.
     *
     * <p>This method encodes the provided unicode for you.
     * <b>Do not encode the emoji before providing the unicode.</b>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found here:
     *         <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a></li>
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
     * @param  unicode
     *         The unicode characters of the emoji
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(long messageId, @Nonnull String unicode)
    {
        return removeReactionById(Long.toUnsignedString(messageId), unicode);
    }

    /**
     * Attempts to remove the reaction from a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p><b>An Emote is not the same as an emoji!</b>
     * <br>Emotes are custom guild-specific images unlike global unicode emojis!
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emote was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
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
     * @param  emote
     *         The emote to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or not a valid snowflake.</li>
     *             <li>If provided {@code emote} is {@code null}.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(@Nonnull String messageId, @Nonnull Emote emote)
    {
        Checks.notNull(emote, "Emote");
        return removeReactionById(messageId, emote.getName() + ":" + emote.getId());
    }

    /**
     * Attempts to remove the reaction from a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p><b>An Emote is not the same as an emoji!</b>
     * <br>Emotes are custom guild-specific images unlike global unicode emojis!
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emote was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
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
     * @param  emote
     *         The emote to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided {@code emote} is {@code null}.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(long messageId, @Nonnull Emote emote)
    {
        return removeReactionById(Long.toUnsignedString(messageId), emote);
    }

    /**
     * This obtains the {@link net.dv8tion.jda.api.entities.User users} who reacted to a message using the given unicode emoji.
     *
     * <p>Messages maintain a list of reactions, alongside a list of users who added them.
     *
     * <p>Using this data, we can obtain a {@link net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction}
     * of the users who've reacted to this message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The retrieve request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} being revoked
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found here:
     *         <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a></li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     * </ul>
     *
     *
     * @param  messageId
     *         The messageId to retrieve the users from.
     * @param  unicode
     *         The unicode emote to retrieve users for.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the
     *         logged in account does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}.
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or not a valid snowflake.</li>
     *             <li>If the provided unicode emoji is {@code null} or empty.</li>
     *         </ul>
     *
     * @return The {@link net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction} of the emoji's users.
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(@Nonnull String messageId, @Nonnull String unicode)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notEmpty(unicode, "Emoji");
        Checks.noWhitespace(unicode, "Emoji");

        return new ReactionPaginationActionImpl(this, messageId, EncodingUtil.encodeReaction(unicode));
    }

    /**
     * This obtains the {@link net.dv8tion.jda.api.entities.User users} who reacted to a message using the given unicode emoji.
     *
     * <p>Messages maintain a list of reactions, alongside a list of users who added them.
     *
     * <p>Using this data, we can obtain a {@link net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction}
     * of the users who've reacted to this message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The retrieve request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} being revoked
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found here:
     *         <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a></li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     * </ul>
     *
     *
     * @param  messageId
     *         The messageId to retrieve the users from.
     * @param  unicode
     *         The unicode emote to retrieve users for.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the
     *         logged in account does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}.
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided unicode emoji is {@code null} or empty.</li>
     *         </ul>
     *
     * @return The {@link net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction} of the emoji's users.
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(long messageId, @Nonnull String unicode)
    {
        return retrieveReactionUsersById(Long.toUnsignedString(messageId), unicode);
    }

    /**
     * This obtains the {@link net.dv8tion.jda.api.entities.User users} who reacted to a message using the given {@link net.dv8tion.jda.api.entities.Emote emote}.
     *
     * <p>Messages maintain a list of reactions, alongside a list of users who added them.
     *
     * <p>Using this data, we can obtain a {@link net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction}
     * of the users who've reacted to the given message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The retrieve request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} being revoked
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emote was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to retrieve the users from.
     * @param  emote
     *         The {@link net.dv8tion.jda.api.entities.Emote emote} to retrieve users for.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the
     *         logged in account does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}.
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or not a valid snowflake.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.Emote Emote} is {@code null}.</li>
     *         </ul>
     *
     * @return The {@link net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction} of the emote's users.
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(@Nonnull String messageId, @Nonnull Emote emote)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(emote, "Emote");

        return retrieveReactionUsersById(messageId, String.format("%s:%s", emote.getName(), emote.getId()));
    }

    /**
     * This obtains the {@link net.dv8tion.jda.api.entities.User users} who reacted to a message using the given {@link net.dv8tion.jda.api.entities.Emote emote}.
     *
     * <p>Messages maintain a list of reactions, alongside a list of users who added them.
     *
     * <p>Using this data, we can obtain a {@link net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction}
     * of the users who've reacted to the given message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The retrieve request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} being revoked
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided emote was deleted, doesn't exist, or is not available to the currently logged-in account in this channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to retrieve the users from.
     * @param  emote
     *         The {@link net.dv8tion.jda.api.entities.Emote emote} to retrieve users for.
     *
     * @throws java.lang.UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.api.entities.MessageType#DEFAULT MessageType.DEFAULT}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the
     *         logged in account does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}.
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not a valid snowflake.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.Emote Emote} is {@code null}.</li>
     *         </ul>
     *
     * @return The {@link net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction ReactionPaginationAction} of the emote's users.
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(long messageId, @Nonnull Emote emote)
    {
        return retrieveReactionUsersById(Long.toUnsignedString(messageId), emote);
    }

    /**
     * Used to pin a message. Pinned messages are retrievable via {@link #retrievePinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
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
     *         If this is a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.Message}{@literal >}
     *         <br>Retrieves an immutable list of pinned messages
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<List<Message>> retrievePinnedMessages()
    {
        JDAImpl jda = (JDAImpl) getJDA();
        Route.CompiledRoute route = Route.Messages.GET_PINNED_MESSAGES.compile(getId());
        return new RestActionImpl<>(jda, route, (response, request) ->
        {
            LinkedList<Message> pinnedMessages = new LinkedList<>();
            EntityBuilder builder = jda.getEntityBuilder();
            DataArray pins = response.getArray();

            for (int i = 0; i < pins.length(); i++)
            {
                pinnedMessages.add(builder.createMessage(pins.getObject(i), MessageChannel.this, false));
            }

            return Collections.unmodifiableList(pinnedMessages);
        });
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel. The string provided as {@code newContent} must
     * have a length that is greater than 0 and less-than or equal to 2000. This is a Discord message length limitation.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *             <li>If provided {@code newContent} length is greater than {@code 2000} characters.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to Discord.
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageById(@Nonnull String messageId, @Nonnull CharSequence newContent)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notEmpty(newContent, "Provided message content");
        Checks.check(newContent.length() <= Message.MAX_CONTENT_LENGTH, "Provided newContent length must be %d or less characters.", Message.MAX_CONTENT_LENGTH);
        if (newContent instanceof StringBuilder)
            return new MessageActionImpl(getJDA(), messageId, this, (StringBuilder) newContent);
        else
            return new MessageActionImpl(getJDA(), messageId, this).append(newContent);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel. The string provided as {@code newContent} must
     * have a length that is greater than 0 and less-than or equal to 2000. This is a Discord message length limitation.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *             <li>If provided {@code newContent} length is greater than {@code 2000} characters.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to Discord.
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageById(long messageId, @Nonnull CharSequence newContent)
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *             <li>If provided {@code newContent} is {@code null}.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.Message Message}
     *                 contains a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageById(@Nonnull String messageId, @Nonnull Message newContent)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(newContent, "message");
        return new MessageActionImpl(getJDA(), messageId, this).apply(newContent);
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *             <li>If provided {@code messageId} is not positive.</li>
     *             <li>If provided {@code newContent} is {@code null}.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.Message Message}
     *                 contains a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageById(long messageId, @Nonnull Message newContent)
    {
        return editMessageById(Long.toUnsignedString(messageId), newContent);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>Shortcut for {@link net.dv8tion.jda.api.MessageBuilder#appendFormat(String, Object...)}.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageFormatById(@Nonnull String messageId, @Nonnull String format, @Nonnull Object... args)
    {
        Checks.notBlank(format, "Format String");
        return editMessageById(messageId, String.format(format, args));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>Shortcut for {@link net.dv8tion.jda.api.MessageBuilder#appendFormat(String, Object...)}.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *             <li>If provided {@code messageId} is not positive.</li>
     *             <li>If provided {@code format} is {@code null} or blank.</li>
     *         </ul>
     * @throws IllegalStateException
     *         If the resulting message is either empty or too long to be sent
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageFormatById(long messageId, @Nonnull String format, @Nonnull Object... args)
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *         Up to 10 new {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageEmbedsById(@Nonnull String messageId, @Nonnull MessageEmbed... newEmbeds)
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *         Up to 10 new {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageEmbedsById(long messageId, @Nonnull MessageEmbed... newEmbeds)
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *         Up to 10 new {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageEmbedsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageEmbed> newEmbeds)
    {
        Checks.isSnowflake(messageId, "Message ID");
        return new MessageActionImpl(getJDA(), messageId, this).setEmbeds(newEmbeds);
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
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
     *         Up to 10 new {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.api.entities.MessageEmbed#isSendable() sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageEmbedsById(long messageId, @Nonnull Collection<? extends MessageEmbed> newEmbeds)
    {
        return editMessageEmbedsById(Long.toUnsignedString(messageId), newEmbeds);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>This will replace all the current {@link net.dv8tion.jda.api.interactions.components.Component Components},
     * such as {@link net.dv8tion.jda.api.interactions.components.Button Buttons} or {@link net.dv8tion.jda.api.interactions.components.selections.SelectionMenu SelectionMenus} on this message.
     * The provided parameters are {@link ComponentLayout ComponentLayout} such as {@link ActionRow} which contain a list of components to arrange in the respective layout.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <h2>Example</h2>
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
     *         Up to 5 new {@link net.dv8tion.jda.api.interactions.components.ComponentLayout ComponentLayouts} for the edited message, such as {@link ActionRow}
     *
     * @throws UnsupportedOperationException
     *         If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.api.interactions.components.ComponentLayout ComponentLayouts} is null</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageComponentsById(@Nonnull String messageId, @Nonnull Collection<? extends ComponentLayout> components)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.noneNull(components, "Components");
        if (components.stream().anyMatch(x -> !(x instanceof ActionRow)))
            throw new UnsupportedOperationException("The provided component layout is not supported");
        List<ActionRow> actionRows = components.stream().map(ActionRow.class::cast).collect(Collectors.toList());
        return new MessageActionImpl(getJDA(), messageId, this).setActionRows(actionRows);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>This will replace all the current {@link net.dv8tion.jda.api.interactions.components.Component Components},
     * such as {@link net.dv8tion.jda.api.interactions.components.Button Buttons} or {@link net.dv8tion.jda.api.interactions.components.selections.SelectionMenu SelectionMenus} on this message.
     * The provided parameters are {@link ComponentLayout ComponentLayout} such as {@link ActionRow} which contain a list of components to arrange in the respective layout.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <h2>Example</h2>
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
     *         Up to 5 new {@link net.dv8tion.jda.api.interactions.components.ComponentLayout ComponentLayouts} for the edited message, such as {@link ActionRow}
     *
     * @throws UnsupportedOperationException
     *         If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *         If any of the provided {@link net.dv8tion.jda.api.interactions.components.ComponentLayout ComponentLayouts} is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageComponentsById(long messageId, @Nonnull Collection<? extends ComponentLayout> components)
    {
        return editMessageComponentsById(Long.toUnsignedString(messageId), components);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>This will replace all the current {@link net.dv8tion.jda.api.interactions.components.Component Components},
     * such as {@link net.dv8tion.jda.api.interactions.components.Button Buttons} or {@link net.dv8tion.jda.api.interactions.components.selections.SelectionMenu SelectionMenus} on this message.
     * The provided parameters are {@link ComponentLayout ComponentLayout} such as {@link ActionRow} which contain a list of components to arrange in the respective layout.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <h2>Example</h2>
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
     *         Up to 5 new {@link net.dv8tion.jda.api.interactions.components.ComponentLayout ComponentLayouts} for the edited message, such as {@link ActionRow}
     *
     * @throws UnsupportedOperationException
     *         If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.api.interactions.components.ComponentLayout ComponentLayouts} is null</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageComponentsById(@Nonnull String messageId, @Nonnull ComponentLayout... components)
    {
        Checks.noneNull(components, "Components");
        return editMessageComponentsById(messageId, Arrays.asList(components));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>This will replace all the current {@link net.dv8tion.jda.api.interactions.components.Component Components},
     * such as {@link net.dv8tion.jda.api.interactions.components.Button Buttons} or {@link net.dv8tion.jda.api.interactions.components.selections.SelectionMenu SelectionMenus} on this message.
     * The provided parameters are {@link ComponentLayout ComponentLayout} such as {@link ActionRow} which contain a list of components to arrange in the respective layout.
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
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * <h2>Example</h2>
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
     *         Up to 5 new {@link net.dv8tion.jda.api.interactions.components.ComponentLayout ComponentLayouts} for the edited message, such as {@link ActionRow}
     *
     * @throws UnsupportedOperationException
     *         If the component layout is a custom implementation that is not supported by this interface
     * @throws IllegalArgumentException
     *         If any of the provided {@link net.dv8tion.jda.api.interactions.components.ComponentLayout ComponentLayouts} is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @Nonnull
    @CheckReturnValue
    default MessageAction editMessageComponentsById(long messageId, @Nonnull ComponentLayout... components)
    {
        Checks.noneNull(components, "Components");
        return editMessageComponentsById(messageId, Arrays.asList(components));
    }
}
