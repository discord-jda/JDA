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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

/**
 * Represents a Discord Text GuildChannel.
 * <br>Adds additional functionality and information for text channels in Discord.
 *
 * <p>This is a {@link GuildChannel GuildChannel} capable of sending messages.
 *
 * @see GuildChannel
 * @see MessageChannel
 * @see VoiceChannel
 * @see Category
 *
 * @see   Guild#getTextChannelCache()
 * @see   Guild#getTextChannels()
 * @see   Guild#getTextChannelsByName(String, boolean)
 * @see   Guild#getTextChannelById(long)
 *
 * @see   JDA#getTextChannelCache()
 * @see   JDA#getTextChannels()
 * @see   JDA#getTextChannelsByName(String, boolean)
 * @see   JDA#getTextChannelById(long)
 */
public interface TextChannel extends GuildChannel, MessageChannel, IMentionable
{
    /**
     * The maximum duration of slowmode in seconds
     */
    int MAX_SLOWMODE = 21600;

    /**
     * The topic set for this TextChannel.
     * <br>If no topic has been set, this returns null.
     *
     * @return Possibly-null String containing the topic of this TextChannel.
     */
    @Nullable
    String getTopic();

    /**
     * Whether or not this channel is considered as "NSFW" (Not-Safe-For-Work)
     *
     * @return True, If this TextChannel is considered NSFW by the official Discord Client
     */
    boolean isNSFW();

    /**
     * Whether or not this channel is considered an Announcement-/News-Channel.
     * <br>These channels can be used to crosspost messages to other guilds by using a follower type webhook.
     *
     * @return True, if this is considered a news channel
     */
    boolean isNews();

    /**
     * The slowmode set for this TextChannel.
     * <br>If slowmode is set this returns an {@code int} between 1 and {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}.
     * <br>If not set this returns {@code 0}.
     *
     * <p>Note bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @return The slowmode for this TextChannel, between 1 and {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}, or {@code 0} if no slowmode is set.
     */
    int getSlowmode();

    @Nonnull
    @Override
    ChannelAction<TextChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    ChannelAction<TextChannel> createCopy();

    /**
     * Retrieves the {@link net.dv8tion.jda.api.entities.Webhook Webhooks} attached to this TextChannel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.Webhook Webhook}{@literal >}
     *         <br>Retrieved an immutable list of Webhooks attached to this channel
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Webhook>> retrieveWebhooks();

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Webhook Webhook}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The webhook could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     * </ul>
     *
     * @param  name
     *         The default name for the new Webhook.
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If you do not hold the permission {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS Manage Webhooks}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, blank or not
     *         between 2-100 characters in length
     *
     * @return A specific {@link WebhookAction WebhookAction}
     *         <br>This action allows to set fields for the new webhook before creating it
     */
    @Nonnull
    @CheckReturnValue
    WebhookAction createWebhook(@Nonnull String name);

    /**
     * Bulk deletes a list of messages.
     * <b>This is not the same as calling {@link net.dv8tion.jda.api.entities.Message#delete()} in a loop.</b>
     * <br>This is much more efficient, but it has a different ratelimit. You may call this once per second per Guild.
     *
     * <p>Must be at least 2 messages and not be more than 100 messages at a time.
     * <br>If you only have 1 message, use the {@link Message#delete()} method instead.
     *
     * <br><p>You must have the Permission {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} in this channel to use
     * this function.
     *
     * <p>This method is best used when using {@link MessageHistory MessageHistory} to delete a large amount
     * of messages. If you have a large amount of messages but only their message Ids, please use {@link #deleteMessagesByIds(Collection)}
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
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.</li>
     * </ul>
     *
     * @param  messages
     *         The collection of messages to delete.
     *
     * @throws IllegalArgumentException
     *         If the size of the list less than 2 or more than 100 messages.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this account does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #deleteMessagesByIds(Collection)
     * @see    #purgeMessages(List)
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> deleteMessages(@Nonnull Collection<Message> messages);

    /**
     * Bulk deletes a list of messages.
     * <b>This is not the same as calling {@link net.dv8tion.jda.api.entities.MessageChannel#deleteMessageById(String)} in a loop.</b>
     * <br>This is much more efficient, but it has a different ratelimit. You may call this once per second per Guild.
     *
     * <p>Must be at least 2 messages and not be more than 100 messages at a time.
     * <br>If you only have 1 message, use the {@link net.dv8tion.jda.api.entities.Message#delete()} method instead.
     *
     * <br><p>You must have {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel to use
     * this function.
     *
     * <p>This method is best used when you have a large amount of messages but only their message Ids. If you are using
     * {@link MessageHistory MessageHistory} or have {@link net.dv8tion.jda.api.entities.Message Message}
     * objects, it would be easier to use {@link #deleteMessages(java.util.Collection)}.
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
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.</li>
     * </ul>
     *
     * @param  messageIds
     *         The message ids for the messages to delete.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the size of the list less than 2 or more than 100 messages.
     * @throws java.lang.NumberFormatException
     *         If any of the provided ids cannot be parsed by {@link Long#parseLong(String)}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this account does not have {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #deleteMessages(Collection)
     * @see    #purgeMessagesById(List)
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> deleteMessagesByIds(@Nonnull Collection<String> messageIds);

    /**
     * Deletes a {@link net.dv8tion.jda.api.entities.Webhook Webhook} attached to this channel
     * by the {@code id} specified.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK}
     *     <br>The provided id does not refer to a WebHook present in this TextChannel, either due
     *         to it not existing or having already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in the channel.</li>
     * </ul>
     *
     * @param  id
     *         The not-null id for the target Webhook.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code id} is {@code null} or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> deleteWebhookById(@Nonnull String id);

    /**
     * Attempts to remove all reactions from a message with the specified {@code messageId} in this TextChannel
     * <br>This is useful for moderator commands that wish to remove all reactions at once from a specific message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The clear-reactions request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the
     *         account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The clear-reactions request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *         in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} when adding the reaction.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The clear-reactions request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The not-empty valid message id
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code id} is {@code null} or empty.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> clearReactionsById(@Nonnull String messageId);

    /**
     * Attempts to remove all reactions from a message with the specified {@code messageId} in this TextChannel
     * <br>This is useful for moderator commands that wish to remove all reactions at once from a specific message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The clear-reactions request was attempted after the account lost access to the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *         due to {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the
     *         account lost access to the {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The clear-reactions request was attempted after the account lost {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *         in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} when adding the reaction.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The clear-reactions request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message id
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> clearReactionsById(long messageId)
    {
        return clearReactionsById(Long.toUnsignedString(messageId));
    }

    /**
     * Removes all reactions for the specified emoji.
     *
     * <h2>Example</h2>
     * <pre><code>
     * // custom
     * channel.clearReactions(messageId, "minn:245267426227388416").queue();
     * // unicode escape
     * channel.clearReactions(messageId, "&#92;uD83D&#92;uDE02").queue();
     * // codepoint notation
     * channel.clearReactions(messageId, "U+1F602").queue();
     * </code></pre>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account lost access to the channel by either being removed from the guild
     *         or losing the {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode emoji doesn't exist. Try using one of the example formats.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id for the target message
     * @param  unicode
     *         The unicode emoji to remove reactions for
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MESSAGE_MANAGE} in the channel
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link RestAction}
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> clearReactionsById(@Nonnull String messageId, @Nonnull String unicode);

    /**
     * Removes all reactions for the specified emoji.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account lost access to the channel by either being removed from the guild
     *         or losing the {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided {@link Emote} was deleted or doesn't exist.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id for the target message
     * @param  emote
     *         The {@link Emote} to remove reactions for
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MESSAGE_MANAGE} in the channel
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link RestAction}
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> clearReactionsById(@Nonnull String messageId, @Nonnull Emote emote);

    /**
     * Removes all reactions for the specified emoji.
     *
     * <h2>Example</h2>
     * <pre><code>
     * // custom
     * channel.clearReactions(messageId, "minn:245267426227388416").queue();
     * // unicode escape
     * channel.clearReactions(messageId, "&#92;uD83D&#92;uDE02").queue();
     * // codepoint notation
     * channel.clearReactions(messageId, "U+1F602").queue();
     * </code></pre>
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account lost access to the channel by either being removed from the guild
     *         or losing the {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided unicode emoji doesn't exist. Try using one of the example formats.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id for the target message
     * @param  unicode
     *         The unicode emoji to remove reactions for
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MESSAGE_MANAGE} in the channel
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link RestAction}
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> clearReactionsById(long messageId, @Nonnull String unicode)
    {
        return clearReactionsById(Long.toUnsignedString(messageId), unicode);
    }

    /**
     * Removes all reactions for the specified emoji.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account lost access to the channel by either being removed from the guild
     *         or losing the {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided {@link Emote} was deleted or doesn't exist.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id for the target message
     * @param  emote
     *         The {@link Emote} to remove reactions for
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MESSAGE_MANAGE} in the channel
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link RestAction}
     *
     * @since  4.2.0
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> clearReactionsById(long messageId, @Nonnull Emote emote)
    {
        return clearReactionsById(Long.toUnsignedString(messageId), emote);
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
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI}
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
     * @param  user
     *         The target user of which to remove from
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> removeReactionById(@Nonnull String messageId, @Nonnull String unicode, @Nonnull User user);

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
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI}
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
     * @param  user
     *         The target user of which to remove from
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(long messageId, @Nonnull String unicode, @Nonnull User user)
    {
        return removeReactionById(Long.toUnsignedString(messageId), unicode, user);
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
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI}
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
     * @param  emote
     *         The emote to remove
     * @param  user
     *         The target user of which to remove from
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emote} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(@Nonnull String messageId, @Nonnull Emote emote, @Nonnull User user)
    {
        Checks.notNull(emote, "Emote");
        return removeReactionById(messageId, emote.getName() + ":" + emote.getId(), user);
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
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI}
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
     * @param  emote
     *         The emote to remove
     * @param  user
     *         The target user of which to remove from
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emote} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(long messageId, @Nonnull Emote emote, @Nonnull User user)
    {
        return removeReactionById(Long.toUnsignedString(messageId), emote, user);
    }

    /**
     * Attempts to crosspost the provided message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#ALREADY_CROSSPOSTED ALREADY_CROSSPOSTED}
     *     <br>The target message has already been crossposted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the TextChannel.</li>
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
     *         The messageId to crosspost
     *
     * @throws IllegalStateException
     *         If this channel is not a news channel. See {@link #isNews()}.
     * @throws java.lang.IllegalArgumentException
     *         If provided {@code messageId} is {@code null} or empty.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction} - Type: {@link Message}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Message> crosspostMessageById(@Nonnull String messageId)
    {
        if (!isNews())
            throw new IllegalStateException("You can only crosspost messages in news channels!");
        Checks.isSnowflake(messageId);
        if (!getGuild().getSelfMember().hasAccess(this))
            throw new MissingAccessException(this, Permission.VIEW_CHANNEL);
        Route.CompiledRoute route = Route.Messages.CROSSPOST_MESSAGE.compile(getId(), messageId);
        return new RestActionImpl<>(getJDA(), route,
            (response, request) -> request.getJDA().getEntityBuilder().createMessage(response.getObject()));
    }

    /**
     * Attempts to crosspost the provided message.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#ALREADY_CROSSPOSTED ALREADY_CROSSPOSTED}
     *     <br>The target message has already been crossposted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the TextChannel.</li>
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
     *         The messageId to crosspost
     *
     * @throws IllegalStateException
     *         If this channel is not a news channel. See {@link #isNews()}.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} in this channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction} - Type: {@link Message}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Message> crosspostMessageById(long messageId)
    {
        return crosspostMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * Whether we can send messages in this channel.
     * <br>This is an overload of {@link #canTalk(Member)} with the SelfMember.
     * <br>Checks for both {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     *
     * @return True, if we are able to read and send messages in this channel
     */
    boolean canTalk();

    /**
     * Whether the specified {@link net.dv8tion.jda.api.entities.Member}
     * can send messages in this channel.
     * <br>Checks for both {@link net.dv8tion.jda.api.Permission#MESSAGE_READ Permission.MESSAGE_READ} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     *
     * @param  member
     *         The Member to check
     *
     * @return True, if the specified member is able to read and send messages in this channel
     */
    boolean canTalk(@Nonnull Member member);

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        if (alt)
            out = "#" + (upper ? getName().toUpperCase(formatter.locale()) : getName());
        else
            out = getAsMention();

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}
