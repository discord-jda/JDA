/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.Collection;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

/**
 * Represents a Discord Text Channel. See {@link net.dv8tion.jda.core.entities.Channel Channel} and
 * {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} for more information.
 *
 * <p>Internal implementation of this class is available at
 * {@link net.dv8tion.jda.core.entities.impl.TextChannelImpl TextChannelImpl}.
 * <br>Note: Internal implementation should not be used directly.
 */
public interface TextChannel extends Channel, MessageChannel, Comparable<TextChannel>, IMentionable
{
    /**
     * The topic set for this TextChannel.
     * <br>If no topic has been set, this returns null.
     *
     * @return Possibly-null String containing the topic of this TextChannel.
     */
    String getTopic();

    /**
     * Bulk deletes a list of messages.
     * <b>This is not the same as calling {@link net.dv8tion.jda.core.entities.Message#delete()} in a loop.</b>
     * <br>This is much more efficient, but it has a different ratelimit. You may call this once per second per Guild.
     *
     * <p>Must be at least 2 messages and not be more than 100 messages at a time.
     * <br>If you only have 1 message, use the {@link Message#delete()} method instead.
     *
     * <br><p>You must have the Permission {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} in this channel to use
     * this function.
     *
     * <p>This method is best used when using {@link MessageHistory MessageHistory} to delete a large amount
     * of messages. If you have a large amount of messages but only their message Ids, please use {@link #deleteMessagesByIds(Collection)}
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>if any of the provided messages does not exist</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.</li>
     * </ul>
     *
     * @param  messages
     *         The collection of messages to delete.
     *
     * @throws IllegalArgumentException
     *         If the size of the list less than 2 or more than 100 messages.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this account does not have MANAGE_MESSAGES
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     *
     * @see    #deleteMessagesByIds(Collection)
     */
    RestAction<Void> deleteMessages(Collection<Message> messages);

    /**
     * Bulk deletes a list of messages.
     * <b>This is not the same as calling {@link net.dv8tion.jda.core.entities.MessageChannel#deleteMessageById(String)} in a loop.</b>
     * <br>This is much more efficient, but it has a different ratelimit. You may call this once per second per Guild.
     *
     * <p>Must be at least 2 messages and not be more than 100 messages at a time.
     * <br>If you only have 1 message, use the {@link net.dv8tion.jda.core.entities.Message#delete()} method instead.
     *
     * <br><p>You must have {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel to use
     * this function.
     *
     * <p>This method is best used when you have a large amount of messages but only their message Ids. If you are using
     * {@link MessageHistory MessageHistory} or have {@link net.dv8tion.jda.core.entities.Message Message}
     * objects, it would be easier to use {@link #deleteMessages(java.util.Collection)}.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>if any of the provided messages does not exist</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.</li>
     * </ul>
     *
     * @param  messageIds
     *         The message ids for the messages to delete.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the size of the list less than 2 or more than 100 messages.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this account does not have MANAGE_MESSAGES
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     *
     * @see    #deleteMessages(Collection)
     */
    RestAction<Void> deleteMessagesByIds(Collection<String> messageIds);

    /**
     * Retrieves the {@link net.dv8tion.jda.core.entities.Webhook Webhooks} attached to this TextChannel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.core.entities.Webhook Webhook}{@literal >}
     *         <br>An immutable list of Webhook attached to this channel
     */
    RestAction<List<Webhook>> getWebhooks();

    /**
     * Deletes a {@link net.dv8tion.jda.core.entities.Webhook Webhook} attached to this channel
     * by the {@code id} specified.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_WEBHOOK}
     *     <br>The provided id does not refer to a WebHook present in this TextChannel, either due
     *         to it not existing or having already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in the channel.</li>
     * </ul>
     *
     * @param  id
     *         The not-null id for the target Webhook.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code id} is {@code null} or empty.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have
     *         {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in this channel.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction} - Type: Void
     */
    RestAction<Void> deleteWebhookById(String id);

    /**
     * Whether we can send messages in this channel.
     * <br>This is an overload of {@link #canTalk(Member)} with the SelfMember.
     * <br>Checks for both {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} and
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     *
     * @return True, if we are able to read and send messages in this channel
     */
    boolean canTalk();

    /**
     * Whether the specified {@link net.dv8tion.jda.core.entities.Member}
     * can send messages in this channel.
     * <br>Checks for both {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} and
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     *
     * @param  member
     *         The Member to check
     *
     * @return True, if the specified member is able to read and send messages in this channel
     */
    boolean canTalk(Member member);

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        if (alt)
            out = "#" + (upper ?  getName().toUpperCase(formatter.locale()) : getName());
        else
            out = getAsMention();

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}
