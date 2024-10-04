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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference.MessageReferenceType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.entities.sticker.Sticker;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest;
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Specialized {@link net.dv8tion.jda.api.requests.RestAction RestAction} used for sending messages to {@link MessageChannel MessageChannels}.
 *
 * @see MessageChannel#sendMessage(MessageCreateData) MessageChannel.sendMessage(...)
 */
public interface MessageCreateAction extends MessageCreateRequest<MessageCreateAction>, FluentRestAction<Message, MessageCreateAction>
{
    /**
     * Sets the default value for {@link #failOnInvalidReply(boolean)}
     *
     * <p>Default: <b>false</b>
     *
     * @param fail
     *        True, to throw a exception if the referenced message does not exist
     */
    static void setDefaultFailOnInvalidReply(boolean fail)
    {
        MessageCreateActionImpl.setDefaultFailOnInvalidReply(fail);
    }

    /**
     * Unique string/number used to identify messages using {@link Message#getNonce()} in {@link MessageReceivedEvent}.
     *
     * <p>The nonce can be used for deduping messages and marking them for use with {@link MessageReceivedEvent}.
     * JDA will automatically generate a unique nonce per message, it is not necessary to do this manually.
     *
     * @param  nonce
     *         The nonce string to use, must be unique per message.
     *         A unique nonce will be generated automatically if this is null.
     *
     * @throws IllegalArgumentException
     *         If the provided nonce is longer than {@value Message#MAX_NONCE_LENGTH} characters
     *
     * @return The same instance for chaining
     *
     * @see    <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank">Cryptographic Nonce - Wikipedia</a>
     */
    @Nonnull
    MessageCreateAction setNonce(@Nullable String nonce);

    /**
     * Message reference used for a reply or forwarded message.
     *
     * <p><b>{@link MessageReferenceType#DEFAULT Default Type}</b>
     *
     * <p>You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using {@link #mentionRepliedUser(boolean)}.
     *
     * <p>This also requires {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the channel.
     * If this permission is missing, you receive {@link net.dv8tion.jda.api.requests.ErrorResponse#REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM}.
     *
     * <p>If the target message does not exist, this will result in {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE ErrorResponse.UNKNOWN_MESSAGE}.
     * You can use {@link #failOnInvalidReply(boolean)} to allow unknown or deleted messages.
     *
     * <p><b>{@link MessageReferenceType#FORWARD Forward Type}</b>
     *
     * <p>Creates a snapshot of the referenced message at the current time and sends it in this channel.
     *
     * <p>You cannot forward messages from channels you do not have access to.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} from forwarding include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REFERENCED_MESSSAGE_NOT_FOUND REFERENCED_MESSSAGE_NOT_FOUND}
     *     <br>If the provided reference cannot be resolved to a message</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#FORWARD_CANNOT_HAVE_CONTENT FORWARD_CANNOT_HAVE_CONTENT}
     *     <br>If additional content is sent alongside a forwarded message</li>
     * </ul>
     *
     * @param  type
     *         The type of message reference
     * @param  guildId
     *         The guild id the forwarded message comes from, or null if it is not from a guild
     * @param  channelId
     *         The channel id the forwarded message comes from
     * @param  messageId
     *         The target message id
     *
     * @throws IllegalArgumentException
     *         If null or an invalid snowflake is passed or the reference type is {@link MessageReferenceType#UNKNOWN}
     *
     * @return The same instance for chaining
     */
    @Nonnull
    MessageCreateAction setMessageReference(@Nonnull MessageReferenceType type, @Nullable String guildId, @Nonnull String channelId, @Nonnull String messageId);

    /**
     * Message reference used for a reply or forwarded message.
     *
     * <p><b>{@link MessageReferenceType#DEFAULT Default Type}</b>
     *
     * <p>You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using {@link #mentionRepliedUser(boolean)}.
     *
     * <p>This also requires {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the channel.
     * If this permission is missing, you receive {@link net.dv8tion.jda.api.requests.ErrorResponse#REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM}.
     *
     * <p>If the target message does not exist, this will result in {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE ErrorResponse.UNKNOWN_MESSAGE}.
     * You can use {@link #failOnInvalidReply(boolean)} to allow unknown or deleted messages.
     *
     * <p><b>{@link MessageReferenceType#FORWARD Forward Type}</b>
     *
     * <p>Creates a snapshot of the referenced message at the current time and sends it in this channel.
     *
     * <p>You cannot forward messages from channels you do not have access to.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} from forwarding include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REFERENCED_MESSSAGE_NOT_FOUND REFERENCED_MESSSAGE_NOT_FOUND}
     *     <br>If the provided reference cannot be resolved to a message</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#FORWARD_CANNOT_HAVE_CONTENT FORWARD_CANNOT_HAVE_CONTENT}
     *     <br>If additional content is sent alongside a forwarded message</li>
     * </ul>
     *
     * @param  type
     *         The type of message reference
     * @param  guildId
     *         The guild id the forwarded message comes from, or 0 if it is not from a guild
     * @param  channelId
     *         The channel id the forwarded message comes from
     * @param  messageId
     *         The target message id
     *
     * @throws IllegalArgumentException
     *         If the reference type is null or {@link MessageReferenceType#UNKNOWN}
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default MessageCreateAction setMessageReference(@Nonnull MessageReferenceType type, long guildId, long channelId, long messageId)
    {
        return setMessageReference(type, Long.toUnsignedString(guildId), Long.toUnsignedString(channelId), Long.toUnsignedString(messageId));
    }

    /**
     * Message reference used for a reply or forwarded message.
     *
     * <p><b>{@link MessageReferenceType#DEFAULT Default Type}</b>
     *
     * <p>You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using {@link #mentionRepliedUser(boolean)}.
     *
     * <p>This also requires {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the channel.
     * If this permission is missing, you receive {@link net.dv8tion.jda.api.requests.ErrorResponse#REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM}.
     *
     * <p>If the target message does not exist, this will result in {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE ErrorResponse.UNKNOWN_MESSAGE}.
     * You can use {@link #failOnInvalidReply(boolean)} to allow unknown or deleted messages.
     *
     * <p><b>{@link MessageReferenceType#FORWARD Forward Type}</b>
     *
     * <p>Creates a snapshot of the referenced message at the current time and sends it in this channel.
     *
     * <p>You cannot forward messages from channels you do not have access to.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} from forwarding include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REFERENCED_MESSSAGE_NOT_FOUND REFERENCED_MESSSAGE_NOT_FOUND}
     *     <br>If the provided reference cannot be resolved to a message</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#FORWARD_CANNOT_HAVE_CONTENT FORWARD_CANNOT_HAVE_CONTENT}
     *     <br>If additional content is sent alongside a forwarded message</li>
     * </ul>
     *
     * @param  type
     *         The type of message reference
     * @param  message
     *         The target message
     *
     * @throws IllegalArgumentException
     *         If null is provided or the reference type is {@link MessageReferenceType#UNKNOWN}
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default MessageCreateAction setMessageReference(@Nonnull MessageReferenceType type, @Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        return setMessageReference(type, message.getGuildId(), message.getChannel().getId(), message.getId());
    }

    /**
     * Message reference used for a reply.
     * <br>The client will show this message as a reply to the target message.
     *
     * <p>You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using {@link #mentionRepliedUser(boolean)}.
     *
     * <p>This also requires {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the channel.
     * If this permission is missing, you receive {@link net.dv8tion.jda.api.requests.ErrorResponse#REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM}.
     *
     * <p>If the target message does not exist, this will result in {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE ErrorResponse.UNKNOWN_MESSAGE}.
     * You can use {@link #failOnInvalidReply(boolean)} to allow unknown or deleted messages.
     *
     * @param  messageId
     *         The target message id to reply to
     *
     * @throws IllegalArgumentException
     *         If the message id is not a valid snowflake
     *
     * @return The same instance for chaining
     */
    @Nonnull
    MessageCreateAction setMessageReference(@Nullable String messageId);

    /**
     * Message reference used for a reply.
     * <br>The client will show this message as a reply to the target message.
     *
     * <p>You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using {@link #mentionRepliedUser(boolean)}.
     *
     * <p>This also requires {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the channel.
     * If this permission is missing, you receive {@link net.dv8tion.jda.api.requests.ErrorResponse#REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM}.
     *
     * <p>If the target message does not exist, this will result in {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE ErrorResponse.UNKNOWN_MESSAGE}.
     * You can use {@link #failOnInvalidReply(boolean)} to allow unknown or deleted messages.
     *
     * @param  messageId
     *         The target message id to reply to
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default MessageCreateAction setMessageReference(long messageId)
    {
        return setMessageReference(Long.toUnsignedString(messageId));
    }

    /**
     * Message reference used for a reply.
     * <br>The client will show this message as a reply to the target message.
     *
     * <p>You can only reply to messages from the same channel.
     * By default, this will mention the author of the target message, this can be disabled using {@link #mentionRepliedUser(boolean)}.
     *
     * <p>This also requires {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the channel.
     * If this permission is missing, you receive {@link net.dv8tion.jda.api.requests.ErrorResponse#REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM ErrorResponse.REPLY_FAILED_MISSING_MESSAGE_HISTORY_PERM}.
     *
     * <p>If the target message does not exist, this will result in {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE ErrorResponse.UNKNOWN_MESSAGE}.
     * You can use {@link #failOnInvalidReply(boolean)} to allow unknown or deleted messages.
     *
     * @param  message
     *         The target message to reply to
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default MessageCreateAction setMessageReference(@Nullable Message message)
    {
        return setMessageReference(message == null ? null : message.getId());
    }

    /**
     * Whether to throw a exception if the referenced message does not exist, when replying to a message.
     * <br>This only matters in combination with {@link #setMessageReference(Message)} and {@link #setMessageReference(long)}!
     *
     * <p>This is false by default but can be configured using {@link #setDefaultFailOnInvalidReply(boolean)}!
     *
     * @param  fail
     *         True, to throw a exception if the referenced message does not exist
     *
     * @return Updated MessageCreateAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction failOnInvalidReply(boolean fail);

    /**
     * Set the stickers to send alongside this message.
     * <br>This is not supported for message edits.
     *
     * @param  stickers
     *         The stickers to send, or null to not send any stickers
     *
     * @throws IllegalStateException
     *         If this request is a message edit request
     * @throws IllegalArgumentException
     *         <ul>
     *           <li>If any of the provided stickers is a {@link GuildSticker},
     *               which is either {@link GuildSticker#isAvailable() unavailable} or from a different guild.</li>
     *           <li>If the collection has more than {@value Message#MAX_STICKER_COUNT} stickers</li>
     *           <li>If a collection with null entries is provided</li>
     *         </ul>
     *
     * @return Updated MessageCreateAction for chaining convenience
     *
     * @see    Sticker#fromId(long)
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction setStickers(@Nullable Collection<? extends StickerSnowflake> stickers);

    /**
     * Set the stickers to send alongside this message.
     * <br>This is not supported for message edits.
     *
     * @param  stickers
     *         The stickers to send, or null to not send any stickers
     *
     * @throws IllegalStateException
     *         If this request is a message edit request
     * @throws IllegalArgumentException
     *         <ul>
     *           <li>If any of the provided stickers is a {@link GuildSticker},
     *               which is either {@link GuildSticker#isAvailable() unavailable} or from a different guild.</li>
     *           <li>If the collection has more than {@value Message#MAX_STICKER_COUNT} stickers</li>
     *           <li>If a collection with null entries is provided</li>
     *         </ul>
     *
     * @return Updated MessageCreateAction for chaining convenience
     *
     * @see    Sticker#fromId(long)
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction setStickers(@Nullable StickerSnowflake... stickers)
    {
        if (stickers != null)
            Checks.noneNull(stickers, "Sticker");
        return setStickers(stickers == null ? null : Arrays.asList(stickers));
    }
}
