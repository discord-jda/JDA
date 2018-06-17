/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import net.dv8tion.jda.core.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONArray;

import javax.annotation.CheckReturnValue;
import java.io.*;
import java.util.*;

/**
 * Represents a Discord channel that can have {@link net.dv8tion.jda.core.entities.Message Messages} and files sent to it.
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
 * <br><b>{@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is a special case which uses {@link IMentionable#getAsMention() IMentionable.getAsMention()}
 * by default and uses the <code>#{@link #getName()}</code> format as <u>alternative</u></b>
 */
public interface MessageChannel extends ISnowflake, Formattable
{

    /**
     * The id for the most recent message sent
     * in this current MessageChannel.
     * <br>This should only be used if {@link #hasLatestMessage()} returns {@code true}!
     *
     * <p>This value is updated on each {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * and <u><b>will be reset to {@code null} if the message associated with this ID gets deleted</b></u>
     *
     * @throws java.lang.IllegalStateException
     *         If no message id is available
     *
     * @return The most recent message's id
     */
    default String getLatestMessageId()
    {
        return Long.toUnsignedString(getLatestMessageIdLong());
    }

    /**
     * The id for the most recent message sent
     * in this current MessageChannel.
     * <br>This should only be used if {@link #hasLatestMessage()} returns {@code true}!
     *
     * <p>This value is updated on each {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
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
    boolean hasLatestMessage();

    /**
     * This method is a shortcut method to return the following information in the following situation:
     * If the MessageChannel is instance of..
     * <ul>
     *     <li><b>TextChannel</b> - Returns {@link TextChannel#getName()}</li>
     *     <li><b>PrivateChannel</b> Returns {@link PrivateChannel#getUser()}{@link net.dv8tion.jda.core.entities.User#getName() .getName()}</li>
     *     <li><b>Group</b> - Returns {@link net.dv8tion.jda.client.entities.Group#getName() Group.getName()}</li>
     * </ul>
     *
     * @return Possibly-null "name" of the MessageChannel. Different implementations determine what the name. Only
     *         {@link net.dv8tion.jda.client.entities.Group Group} could have a {@code null} name.
     */
    String getName();

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of this MessageChannel.
     *
     * @return The ChannelType for this channel
     */
    ChannelType getType();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this MessageChannel
     *
     * @return the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Sends a plain text message to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * use {@link net.dv8tion.jda.core.entities.TextChannel#canTalk() TextChannel.canTalk()}.
     *
     * <p>This method is a shortcut to {@link #sendMessage(Message)} by way of using a {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder}
     * internally to build the provided {@code text} into a Message.
     * <pre>sendMessage(new MessageBuilder().append(text).build())</pre>
     *
     * <p>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param  text
     *         the text to build into a Message to send to the MessageChannel.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does
     *         not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.client.exceptions.VerificationLevelException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel} and
     *         {@link net.dv8tion.jda.core.entities.TextChannel#getGuild() TextChannel.getGuild()}{@link net.dv8tion.jda.core.entities.Guild#checkVerification() .checkVerification()}
     *         returns false.
     * @throws java.lang.IllegalArgumentException
     *         if the provided text is null, empty or longer than 2000 characters
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     *
     * @see net.dv8tion.jda.core.MessageBuilder
     */
    @CheckReturnValue
    default MessageAction sendMessage(CharSequence text)
    {
        Checks.notEmpty(text, "Provided text for message");
        Checks.check(text.length() <= 2000, "Provided text for message must be less than 2000 characters in length");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(getId());
        if (text instanceof StringBuilder)
            return new MessageAction(getJDA(), route, this, (StringBuilder) text);
        else
            return new MessageAction(getJDA(), route, this).append(text);
    }

    /**
     * Sends a formatted text message to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * use {@link net.dv8tion.jda.core.entities.TextChannel#canTalk() TextChannel.canTalk()}.
     *
     * <p>This method is a shortcut to {@link #sendMessage(Message)} by way of using a {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder}
     * and using its {@link net.dv8tion.jda.core.MessageBuilder#appendFormat(String, Object...)} method.
     * <br>For more information on how to format your input, refer to the docs of the method mentioned above.
     *
     * <p>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param  format
     *         The string that should be formatted, if this is {@code null} or empty
     *         the content of the Message would be empty and cause a builder exception.
     * @param  args
     *         The arguments for your format
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does
     *         not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.client.exceptions.VerificationLevelException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel} and
     *         {@link net.dv8tion.jda.core.entities.TextChannel#getGuild() TextChannel.getGuild()}{@link net.dv8tion.jda.core.entities.Guild#checkVerification() .checkVerification()}
     *         returns false.
     * @throws java.lang.IllegalArgumentException
     *         If the provided format text is {@code null}, empty or longer than 2000 characters
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     */
    @CheckReturnValue
    default MessageAction sendMessageFormat(String format, Object... args)
    {
        Checks.notEmpty(format, "Format");
        return sendMessage(String.format(format, args));
    }

    /**
     * Sends a specified {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} as a {@link net.dv8tion.jda.core.entities.Message Message}
     * to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * use {@link net.dv8tion.jda.core.entities.TextChannel#canTalk() TextChannel#canTalk}.
     *
     * <p>This method is a shortcut to {@link #sendMessage(Message)} by way of using a {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder}
     * internally to build the provided {@code embed} into a Message.
     * <pre>sendMessage(new MessageBuilder().setEmbed(embed).build())</pre>
     *
     * <p>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param  embed
     *         the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} to send
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does
     *         not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS}</li>
     *         </ul>
     * @throws net.dv8tion.jda.client.exceptions.VerificationLevelException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel} and
     *         {@link net.dv8tion.jda.core.entities.TextChannel#getGuild() TextChannel.getGuild()}{@link net.dv8tion.jda.core.entities.Guild#checkVerification() .checkVerification()}
     *         returns false.
     * @throws java.lang.IllegalArgumentException
     *         If the provided embed is {@code null} or if the provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     *         is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     *
     * @see    net.dv8tion.jda.core.MessageBuilder
     * @see    net.dv8tion.jda.core.EmbedBuilder
     */
    @CheckReturnValue
    default MessageAction sendMessage(MessageEmbed embed)
    {
        Checks.notNull(embed, "Provided embed");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(getId());
        return new MessageAction(getJDA(), route, this).embed(embed);
    }

    /**
     * Sends a specified {@link net.dv8tion.jda.core.entities.Message Message} to this channel.
     * <br>This will fail if this channel is an instance of {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * use {@link net.dv8tion.jda.core.entities.TextChannel#canTalk() TextChannel#canTalk}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE} in
     *         the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNAUTHORIZED UNAUTHORIZED}
     *     <br>If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} and the recipient User blocked you</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  msg
     *         the {@link net.dv8tion.jda.core.entities.Message Message} to send
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does
     *         not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS} (if this message is only an embed)</li>
     *         </ul>
     * @throws net.dv8tion.jda.client.exceptions.VerificationLevelException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel} and
     *         {@link net.dv8tion.jda.core.entities.TextChannel#getGuild() TextChannel.getGuild()}{@link net.dv8tion.jda.core.entities.Guild#checkVerification() .checkVerification()}
     *         returns false.
     * @throws java.lang.IllegalArgumentException
     *         If the provided message is {@code null} or the provided {@link net.dv8tion.jda.core.entities.Message Message}
     *         contains an {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     *         that is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The newly created Message after it has been sent to Discord.
     *
     * @see    net.dv8tion.jda.core.MessageBuilder
     */
    @CheckReturnValue
    default MessageAction sendMessage(Message msg)
    {
        Checks.notNull(msg, "Message");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(getId());
        return new MessageAction(getJDA(), route, this).apply(msg);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     *
     * <p>This is a shortcut to {@link #sendFile(java.io.File, String, Message)} by way of using {@link java.io.File#getName()}.
     * <pre>sendFile(file, file.getName(), message)</pre>
     *
     * <p>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     *
     * @param  file
     *         The file to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code file} is null.</li>
     *             <li>Provided {@code file} does not exist.</li>
     *             <li>Provided {@code file} is unreadable.</li>
     *             <li>Provided {@code file} is greater than 8MiB for normal and 50MiB for nitro accounts.</li>
     *             <li>Provided {@link net.dv8tion.jda.core.entities.Message Message} is not {@code null} <b>and</b>
     *                 contains a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    @CheckReturnValue
    default MessageAction sendFile(File file)
    {
        return sendFile(file, (Message) null);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     *
     * <p>The {@code fileName} parameter is used to inform Discord about what the file should be called. This is 2 fold:
     * <ol>
     *     <li>The file name provided is the name that is found in {@link net.dv8tion.jda.core.entities.Message.Attachment#getFileName()}
     *          after upload and it is the name that will show up in the client when the upload is displayed.
     *     <br>Note: The fileName does not show up on the Desktop client for images. It does on mobile however.</li>
     *     <li>The extension of the provided fileName also determines who Discord will treat the file. Discord currently only
     *         has special handling for image file types, but the fileName's extension must indicate that it is an image file.
     *         This means it has to end in something like .png, .jpg, .jpeg, .gif, etc. As a note, you can also not provide
     *         a full name for the file and instead ONLY provide the extension like "png" or "gif" and Discord will generate
     *         a name for the upload and append the fileName as the extension.</li>
     * </ol>
     *
     * <p>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     *
     * @param  file
     *         The file to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code file} is null.</li>
     *             <li>Provided {@code file} does not exist.</li>
     *             <li>Provided {@code file} is unreadable.</li>
     *             <li>Provided {@code file} is greater than 8MiB for normal and 50MiB for nitro accounts.</li>
     *             <li>Provided {@link net.dv8tion.jda.core.entities.Message Message} is not {@code null} <b>and</b>
     *                 contains a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    @CheckReturnValue
    default MessageAction sendFile(File file, String fileName)
    {
        return sendFile(file, fileName, null);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     * <br>This allows you to send an {@link java.io.InputStream InputStream} as substitute to a file.
     *
     * <p>For information about the {@code fileName} parameter, Refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     * <br>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     *
     * @param  data
     *         The InputStream data to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     *         <br>Refer to the documentation for {@link #sendFile(java.io.File, String, Message)} for information about this parameter.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided filename is {@code null} or {@code empty}.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    @CheckReturnValue
    default MessageAction sendFile(InputStream data, String fileName)
    {
        return sendFile(data, fileName, null);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     * <br>This allows you to send an {@code byte[]} as substitute to a file.
     *
     * <p>For information about the {@code fileName} parameter, Refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     * <br>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     *
     * @param  data
     *         The {@code byte[]} data to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord.
     *         <br>Refer to the documentation for {@link #sendFile(java.io.File, String, Message)} for information about this parameter.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided filename is {@code null} or {@code empty} or the provided data is larger than 8MiB on normal or 50MiB on nitro accounts.</li>
     *             <li>If the provided {@link net.dv8tion.jda.core.entities.Message Message}
     *                 contains an {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     *                 that is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    @CheckReturnValue
    default MessageAction sendFile(byte[] data, String fileName)
    {
        return sendFile(data, fileName, null);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     *
     * <p>This is a shortcut to {@link #sendFile(java.io.File, String, Message)} by way of using {@link java.io.File#getName()}.
     * <pre>sendFile(file, file.getName(), message)</pre>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u> you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * MessageBuilder message = new MessageBuilder();
     * EmbedBuilder embed = new EmbedBuilder();
     * File file = new File("cat.gif");
     * embed.setImage("attachment://cat.gif")
     *      .setDescription("This is a cute cat :3");
     * message.setEmbed(embed.build());
     * channel.sendFile(file, message.build()).queue();
     * </code></pre>
     *
     * <p>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     *
     * @param  file
     *         The file to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  message
     *         The message to be sent along with the uploaded file. This value can be {@code null}.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code file} is null.</li>
     *             <li>Provided {@code file} does not exist.</li>
     *             <li>Provided {@code file} is unreadable.</li>
     *             <li>Provided {@code file} is greater than 8 MiB on a normal or 50 MiB on a nitro account.</li>
     *             <li>Provided {@link net.dv8tion.jda.core.entities.Message Message} is not {@code null} <b>and</b>
     *                 contains a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    @CheckReturnValue
    default MessageAction sendFile(File file, Message message)
    {
        Checks.notNull(file, "file");

        return sendFile(file, file.getName(), message);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     *
     * <p>The {@code fileName} parameter is used to inform Discord about what the file should be called. This is 2 fold:
     * <ol>
     *     <li>The file name provided is the name that is found in {@link net.dv8tion.jda.core.entities.Message.Attachment#getFileName()}
     *          after upload and it is the name that will show up in the client when the upload is displayed.
     *     <br>Note: The fileName does not show up on the Desktop client for images. It does on mobile however.</li>
     *     <li>The extension of the provided fileName also determines who Discord will treat the file. Discord currently only
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
     * MessageBuilder message = new MessageBuilder();
     * EmbedBuilder embed = new EmbedBuilder();
     * File file = new File("cat_01.gif");
     * embed.setImage("attachment://cat.gif") // we specify this in sendFile as "cat.gif"
     *      .setDescription("This is a cute cat :3");
     * message.setEmbed(embed.build());
     * channel.sendFile(file, "cat.gif", message.build()).queue();
     * </code></pre>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The send request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE} or
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}
     *         in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNAUTHORIZED UNAUTHORIZED}
     *     <br>If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} and the recipient User blocked you</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  file
     *         The file to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     * @param  message
     *         The message to be sent along with the uploaded file. This value can be {@code null}.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code file} is null.</li>
     *             <li>Provided {@code file} does not exist.</li>
     *             <li>Provided {@code file} is unreadable.</li>
     *             <li>Provided {@code file} is greater than 8 MiB on a normal or 50 MiB on a nitro account.</li>
     *             <li>Provided {@link net.dv8tion.jda.core.entities.Message Message} is not {@code null} <b>and</b>
     *                 contains a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    @CheckReturnValue
    default MessageAction sendFile(File file, String fileName, Message message)
    {
        Checks.notNull(file, "file");
        Checks.check(file.exists() && file.canRead(),
            "Provided file is either null, doesn't exist or is not readable!");
        Checks.check(file.length() <= getJDA().getSelfUser().getAllowedFileSize(),
            "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users");
        Checks.notNull(fileName, "fileName");

        try
        {
            return sendFile(new FileInputStream(file), fileName, message);
        }
        catch (FileNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     * <br>This allows you to send an {@link java.io.InputStream InputStream} as substitute to a file.
     *
     * <p>For information about the {@code fileName} parameter, Refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     * <br>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u> you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * MessageBuilder message = new MessageBuilder();
     * EmbedBuilder embed = new EmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
     *      .setDescription("This is a cute cat :3");
     * message.setEmbed(embed.build());
     * channel.sendFile(file, "cat.png", message.build()).queue();
     * </code></pre>
     *
     * @param  data
     *         The InputStream data to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord
     *         <br>Refer to the documentation for {@link #sendFile(java.io.File, String, Message)} for information about this parameter.
     * @param  message
     *         The message to be sent along with the uploaded file. This value can be {@code null}.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided filename is {@code null} or {@code empty}.
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    @CheckReturnValue
    default MessageAction sendFile(InputStream data, String fileName, Message message)
    {
        Checks.notNull(data, "data InputStream");
        Checks.notNull(fileName, "fileName");

        Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(getId());
        return new MessageAction(getJDA(), route, this).apply(message).addFile(data, fileName);
    }

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * Sends the provided {@link net.dv8tion.jda.core.entities.Message Message} with the uploaded file.
     * <br>If you do not wish to send a Message with the uploaded file, you can provide {@code null} for
     * the {@code message} parameter.
     * <br>This allows you to send an {@code byte[]} as substitute to a file.
     *
     * <p>For information about the {@code fileName} parameter, Refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     * <br>For {@link net.dv8tion.jda.core.requests.ErrorResponse} information, refer to the documentation for {@link #sendFile(java.io.File, String, Message)}.
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u> you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * <p><u>Example</u>
     * <pre><code>
     * MessageChannel channel; // = reference of a MessageChannel
     * MessageBuilder message = new MessageBuilder();
     * EmbedBuilder embed = new EmbedBuilder();
     * byte[] file = IOUtil.readFully(new URL("https://http.cat/500").openStream());
     * embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
     *      .setDescription("This is a cute cat :3");
     * message.setEmbed(embed.build());
     * channel.sendFile(file, "cat.png", message.build()).queue();
     * </code></pre>
     *
     * @param  data
     *         The {@code byte[]} data to upload to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * @param  fileName
     *         The name that should be sent to discord.
     *         <br>Refer to the documentation for {@link #sendFile(java.io.File, String, Message)} for information about this parameter.
     * @param  message
     *         The message to be sent along with the uploaded file. This value can be {@code null}.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided filename is {@code null} or {@code empty} or the provided data is larger than 8 MiB on a normal or 50 MiB on a nitro account.</li>
     *             <li>If the provided {@link net.dv8tion.jda.core.entities.Message Message}
     *                 contains an {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     *                 that is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}</li>
     *         </ul>
     * @throws java.lang.UnsupportedOperationException
     *         If this is a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
     *         and both the currently logged in account and the target user are bots.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The {@link net.dv8tion.jda.core.entities.Message Message} created from this upload.
     */
    @CheckReturnValue
    default MessageAction sendFile(byte[] data, String fileName, Message message)
    {
        Checks.notNull(data, "data");
        Checks.notNull(fileName, "fileName");
        final long maxSize = getJDA().getSelfUser().getAllowedFileSize();
        Checks.check(data.length <= maxSize, "File is too big! Max file-size is %d bytes", maxSize);
        return sendFile(new ByteArrayInputStream(data), fileName, message);
    }

    /**
     * Attempts to get a {@link net.dv8tion.jda.core.entities.Message Message} from the Discord's servers that has
     * the same id as the id provided.
     * <br>Note: when retrieving a Message, you must retrieve it from the channel it was sent in!
     *
     * <p><b>Only bots can use this endpoint! A similar behaviour can be simulated using {@link #getHistoryAround(long, int)}!</b>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code id} does not refer to a message sent in this channel or the message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the sought after Message
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#BOT AccountType.BOT}
     * @throws IllegalArgumentException
     *         if the provided {@code messageId} is null or empty.
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Message
     *         <br>The Message defined by the provided id.
     */
    @CheckReturnValue
    default RestAction<Message> getMessageById(String messageId)
    {
        AccountTypeException.check(getJDA().getAccountType(), AccountType.BOT);
        Checks.isSnowflake(messageId, "Message ID");

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getId(), messageId);
        return new RestAction<Message>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Message> request)
            {
                if (response.isOk())
                {
                    Message m = api.getEntityBuilder().createMessage(response.getObject(), MessageChannel.this, false);
                    request.onSuccess(m);
                }
                else
                    request.onFailure(response);

            }
        };
    }

    /**
     * Attempts to get a {@link net.dv8tion.jda.core.entities.Message Message} from the Discord's servers that has
     * the same id as the id provided.
     * <br>Note: when retrieving a Message, you must retrieve it from the channel it was sent in!
     *
     * <p><b>Only bots can use this endpoint! A similar behaviour can be simulated using {@link #getHistoryAround(long, int)}!</b>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code id} does not refer to a message sent in this channel or the message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the sought after Message
     *
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *         If the currently logged in account is not from {@link net.dv8tion.jda.core.AccountType#BOT AccountType.BOT}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Message
     *         <br>The Message defined by the provided id.
     */
    @CheckReturnValue
    default RestAction<Message> getMessageById(long messageId)
    {
        return getMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * Attempts to delete a {@link net.dv8tion.jda.core.entities.Message Message} from the Discord servers that has
     * the same id as the id provided.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request attempted to delete a Message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_DM_ACTION INVALID_DM_ACTION}
     *     <br>Attempted to delete a Message in a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} or
     *         {@link net.dv8tion.jda.client.entities.Group Group} that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code id} does not refer to a message sent in this channel or the message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the Message that should be deleted
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is null
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     */
    @CheckReturnValue
    default AuditableRestAction<Void> deleteMessageById(String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGE.compile(getId(), messageId);
        return new AuditableRestAction<Void>(getJDA(), route) {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Attempts to delete a {@link net.dv8tion.jda.core.entities.Message Message} from the Discord servers that has
     * the same id as the id provided.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request attempted to delete a Message in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_DM_ACTION INVALID_DM_ACTION}
     *     <br>Attempted to delete a Message in a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} or
     *         {@link net.dv8tion.jda.client.entities.Group Group} that was not sent by the currently logged in account.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code id} does not refer to a message sent in this channel or the message has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id of the Message that should be deleted
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is not positive
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     */
    @CheckReturnValue
    default AuditableRestAction<Void> deleteMessageById(long messageId)
    {
        return deleteMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * Creates a new {@link MessageHistory MessageHistory} object for each call of this method.
     * <br>MessageHistory is <b>NOT</b> an internal message cache, but rather it queries the Discord servers for previously sent messages.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the currently logged in account does not have the permission {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY MESSAGE_HISTORY}
     *
     * @return A {@link net.dv8tion.jda.core.entities.MessageHistory MessageHistory} related to this channel.
     */
    default MessageHistory getHistory()
    {
        return new MessageHistory(this);
    }

    /**
     * A {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction} implementation
     * that allows to {@link Iterable iterate} over recent {@link net.dv8tion.jda.core.entities.Message Messages} of
     * this MessageChannel.
     * <br>This is <b>not</b> a cache for received messages and it can only view messages that were sent
     * before. This iterates chronologically backwards (from present to past).
     *
     * <p><b><u>It is recommended not to use this in an enhanced for-loop without end conditions as it might cause memory
     * overflows in channels with a long message history.</u></b>
     *
     * <h1>Examples</h1>
     * <pre><code>
     * public boolean containsMessage(MessageChannel channel, String content, int checkAmount)
     * {
     *     for (Message message : channel.<u>getIterableHistory()</u>)
     *     {
     *         if (message.getContentRaw().equals(content))
     *             return true;
     *         if (checkAmount--{@literal <=} 0) break;
     *     }
     *     return false;
     * }
     *
     * public List{@literal <Message>} getMessagesByUser(MessageChannel channel, User user)
     * {
     *     return channel.<u>getIterableHistory()</u>.stream()
     *         .limit(1000)
     *         .filter(m{@literal ->} m.getAuthor().equals(user))
     *         .collect(Collectors.toList());
     * }
     * </code></pre>
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the currently logged in account does not have the permission {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY MESSAGE_HISTORY}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.pagination.MessagePaginationAction MessagePaginationAction}
     */
    @CheckReturnValue
    default MessagePaginationAction getIterableHistory()
    {
        return new MessagePaginationAction(this);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent around
     * the marker. The {@code limit} determines the amount of message retrieved near the marker. Discord will
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param messageId
     *        The id of the message that will act as a marker.
     * @param limit
     *        The amount of message to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is {@code null} or empty.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message around the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryAround(MessageChannel, String) MessageHistory.getHistoryAround(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(String messageId, int limit)
    {
        return MessageHistory.getHistoryAround(this, messageId).limit(limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages around
     * the marker. The {@code limit} determines the amount of message retrieved near the marker. Discord will
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param messageId
     *        The id of the message that will act as a marker. The id must refer to a message from this MessageChannel.
     * @param limit
     *        The amount of message to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is not positive.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message around the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryAround(MessageChannel, String) MessageHistory.getHistoryAround(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(long messageId, int limit)
    {
        return getHistoryAround(Long.toUnsignedString(messageId), limit );
    }

    /**
     * Uses the provided {@link net.dv8tion.jda.core.entities.Message Message} as a marker and retrieves messages around
     * the marker. The {@code limit} determines the amount of message retrieved near the marker. Discord will
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code message} has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param message
     *        The {@link net.dv8tion.jda.core.entities.Message Message} that will act as a marker. The provided Message
     *        must be from this MessageChannel.
     * @param limit
     *        The amount of message to be retrieved around the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code message} is {@code null}.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message around the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryAround(MessageChannel, String) MessageHistory.getHistoryAround(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(Message message, int limit)
    {
        Checks.notNull(message, "Provided target message");
        return getHistoryAround(message.getId(), limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent after
     * the marker ID. The {@code limit} determines the amount of message retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAfter(messageId, 100)} - This will retrieve 100 messages from history sent after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param messageId
     *        The id of the message that will act as a marker.
     * @param limit
     *        The amount of message to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is {@code null} or empty.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message after the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryAfter(MessageChannel, String) MessageHistory.getHistoryAfter(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(String messageId, int limit)
    {
        return MessageHistory.getHistoryAfter(this, messageId).limit(limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent after
     * the marker ID. The {@code limit} determines the amount of message retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAfter(messageId, 100)} - This will retrieve 100 messages from history sent after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param messageId
     *        The id of the message that will act as a marker.
     * @param limit
     *        The amount of message to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         Provided {@code limit} is less than {@code 1} or greater than {@code 100}.
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message after the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryAfter(MessageChannel, String) MessageHistory.getHistoryAfter(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(long messageId, int limit)
    {
        return getHistoryAfter(Long.toUnsignedString(messageId), limit);
    }

    /**
     * Uses the provided message as a marker and retrieves messages sent after
     * the marker. The {@code limit} determines the amount of message retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAfter(message, 100)} - This will retrieve 100 messages from history sent after the marker.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param message
     *        The message that will act as a marker.
     * @param limit
     *        The amount of message to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code message} is {@code null}.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message after the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryAfter(MessageChannel, String) MessageHistory.getHistoryAfter(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(Message message, int limit)
    {
        Checks.notNull(message, "Message");
        return getHistoryAfter(message.getId(), limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent before
     * the marker ID. The {@code limit} determines the amount of message retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryBefore(messageId, 100)} - This will retrieve 100 messages from history sent before the marker.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param messageId
     *        The id of the message that will act as a marker.
     * @param limit
     *        The amount of message to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is {@code null} or empty.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message before the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryBefore(MessageChannel, String) MessageHistory.getHistoryBefore(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(String messageId, int limit)
    {
        return MessageHistory.getHistoryBefore(this, messageId).limit(limit);
    }

    /**
     * Uses the provided {@code id} of a message as a marker and retrieves messages sent before
     * the marker ID. The {@code limit} determines the amount of message retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryBefore(messageId, 100)} - This will retrieve 100 messages from history sent before the marker.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param messageId
     *        The id of the message that will act as a marker.
     * @param limit
     *        The amount of message to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code messageId} is {@code null} or empty.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message before the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryBefore(MessageChannel, String) MessageHistory.getHistoryBefore(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(long messageId, int limit)
    {
        return getHistoryBefore(Long.toUnsignedString(messageId), limit);
    }

    /**
     * Uses the provided message as a marker and retrieves messages sent before
     * the marker. The {@code limit} determines the amount of message retrieved near the marker.
     *
     * <p><b>Examples:</b>
     * <br>Retrieve 100 messages from the middle of history. {@literal >}100 message exist in history and the marker is {@literal >}50 messages
     * from the edge of history.
     * <br>{@code getHistoryAfter(message, 100)} - This will retrieve 100 messages from history sent before the marker.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted, thus could not be used as a marker.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param message
     *        The message that will act as a marker.
     * @param limit
     *        The amount of message to be retrieved after the marker. Minimum: 1, Max: 100.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>Provided {@code message} is {@code null}.</li>
     *             <li>Provided {@code limit} is less than {@code 1} or greater than {@code 100}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageHistory.MessageRetrieveAction MessageHistory.MessageRetrieveAction}
     *         <br>Provides a MessageHistory object with message before the provided message loaded into it.
     *
     * @see    net.dv8tion.jda.core.entities.MessageHistory#getHistoryBefore(MessageChannel, String) MessageHistory.getHistoryBefore(MessageChannel, String)
     */
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(Message message, int limit)
    {
        Checks.notNull(message, "Message");
        return getHistoryBefore(message.getId(), limit);
    }

    /**
     * Sends the typing status to discord. This is what is used to make the message "X is typing..." appear.
     * <br>The typing status only lasts for 10 seconds or until a message is sent.
     * <br>So if you wish to show continuous typing you will need to call this method once every 10 seconds.
     *
     * <p>The official discord client sends this every 5 seconds even though the typing status lasts 10.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         or {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     */
    @CheckReturnValue
    default RestAction<Void> sendTyping()
    {
        Route.CompiledRoute route = Route.Channels.SEND_TYPING.compile(getId());
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found at
     *         <a href="http://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">http://unicode.org/emoji/charts/full-emoji-list.html</a></li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> addReactionById(String messageId, String unicode)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notEmpty(unicode, "Provided Unicode");
        Checks.noWhitespace(unicode, "Provided Unicode");

        String encoded = MiscUtil.encodeUTF8(unicode);
        Route.CompiledRoute route = Route.Messages.ADD_REACTION.compile(getId(), messageId, encoded);
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found at
     *         <a href="http://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">http://unicode.org/emoji/charts/full-emoji-list.html</a></li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> addReactionById(long messageId, String unicode)
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
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</u></b>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found at
     *         <a href="http://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">http://unicode.org/emoji/charts/full-emoji-list.html</a></li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to attach the reaction to
     * @param  emote
     *         The not-null {@link net.dv8tion.jda.core.entities.Emote} to react with
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emote} is {@code null}.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> addReactionById(String messageId, Emote emote)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(emote, "Emote");

        Route.CompiledRoute route = Route.Messages.ADD_REACTION.compile(getId(), messageId, String.format("%s:%s", emote.getName(), emote.getId()));
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Attempts to react to a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p><b>An Emote is not the same as an emoji!</b>
     * <br>Emotes are custom guild-specific images unlike global unicode emojis!
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</u></b>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The request was attempted after the provided {@link net.dv8tion.jda.core.entities.Emote Emote}
     *         was deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to attach the reaction to
     * @param  emote
     *         The not-null {@link net.dv8tion.jda.core.entities.Emote} to react with
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not positive.</li>
     *             <li>If provided {@code emote} is {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *         and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> addReactionById(long messageId, Emote emote)
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found at
     *         <a href="http://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">http://unicode.org/emoji/charts/full-emoji-list.html</a></li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> removeReactionById(String messageId, String unicode)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.noWhitespace(unicode, "Emoji");

        final String code = MiscUtil.encodeUTF8(unicode);
        final Route.CompiledRoute route = Route.Messages.REMOVE_OWN_REACTION.compile(getId(), messageId, code);
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (!response.isOk())
                    request.onFailure(response);
                else
                    request.onSuccess(null);
            }
        };
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found at
     *         <a href="http://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">http://unicode.org/emoji/charts/full-emoji-list.html</a></li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code unicode} is {@code null} or empty.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> removeReactionById(long messageId, String unicode)
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found at
     *         <a href="http://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">http://unicode.org/emoji/charts/full-emoji-list.html</a></li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emote} is {@code null}.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> removeReactionById(String messageId, Emote emote)
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
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link net.dv8tion.jda.core.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found at
     *         <a href="http://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">http://unicode.org/emoji/charts/full-emoji-list.html</a></li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emote} is {@code null}.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> removeReactionById(long messageId, Emote emote)
    {
        Checks.notNull(emote, "Emote");
        return removeReactionById(messageId, emote.getName() + ":" + emote.getId());
    }

    /**
     * Used to pin a message. Pinned messages are retrievable via {@link #getPinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message to pin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is {@code null} or empty.
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> pinMessageById(String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        Route.CompiledRoute route = Route.Messages.ADD_PINNED_MESSAGE.compile(getId(), messageId);
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Used to pin a message. Pinned messages are retrievable via {@link #getPinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message to pin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is not positive.
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> pinMessageById(long messageId)
    {
        return pinMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * Used to unpin a message. Pinned messages are retrievable via {@link #getPinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message to unpin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is {@code null} or empty.
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> unpinMessageById(String messageId)
    {
        Checks.isSnowflake(messageId, "Message ID");

        Route.CompiledRoute route = Route.Messages.REMOVE_PINNED_MESSAGE.compile(getId(), messageId);
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Used to unpin a message. Pinned messages are retrievable via {@link #getPinnedMessages()}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message to unpin.
     *
     * @throws IllegalArgumentException
     *         if the provided messageId is not positive.
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *             <li>{@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     */
    @CheckReturnValue
    default RestAction<Void> unpinMessageById(long messageId)
    {
        return unpinMessageById(Long.toUnsignedString(messageId));
    }

    /**
     * Retrieves a List of {@link net.dv8tion.jda.core.entities.Message Messages} that have been pinned in this channel.
     * <br>If no messages have been pinned, this retrieves an empty List.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.core.entities.Message}{@literal >}
     *         <br>An immutable list of pinned messages
     */
    @CheckReturnValue
    default RestAction<List<Message>> getPinnedMessages()
    {
        Route.CompiledRoute route = Route.Messages.GET_PINNED_MESSAGES.compile(getId());
        return new RestAction<List<Message>>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<Message>> request)
            {
                if (response.isOk())
                {
                    LinkedList<Message> pinnedMessages = new LinkedList<>();
                    EntityBuilder builder = api.getEntityBuilder();
                    JSONArray pins = response.getArray();

                    for (int i = 0; i < pins.length(); i++)
                    {
                        pinnedMessages.add(builder.createMessage(pins.getJSONObject(i), MessageChannel.this, false));
                    }

                    request.onSuccess(Collections.unmodifiableList(pinnedMessages));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel. The string provided as {@code newContent} must
     * have a length that is greater than 0 and less-than or equal to 2000. This is a Discord message length limitation.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to Discord.
     */
    @CheckReturnValue
    default MessageAction editMessageById(String messageId, CharSequence newContent)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notEmpty(newContent, "Provided message content");
        Checks.check(newContent.length() <= 2000, "Provided newContent length must be 2000 or less characters.");

        Route.CompiledRoute route = Route.Messages.EDIT_MESSAGE.compile(getId(), messageId);
        if (newContent instanceof StringBuilder)
            return new MessageAction(getJDA(), route, this, (StringBuilder) newContent);
        else
            return new MessageAction(getJDA(), route, this).append(newContent);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel. The string provided as {@code newContent} must
     * have a length that is greater than 0 and less-than or equal to 2000. This is a Discord message length limitation.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to Discord.
     */
    @CheckReturnValue
    default MessageAction editMessageById(long messageId, CharSequence newContent)
    {
        return editMessageById(Long.toUnsignedString(messageId), newContent);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     *             <li>If provided {@link net.dv8tion.jda.core.entities.Message Message}
     *                 contains a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @CheckReturnValue
    default MessageAction editMessageById(String messageId, Message newContent)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(newContent, "message");

        Route.CompiledRoute route = Route.Messages.EDIT_MESSAGE.compile(getId(), messageId);
        return new MessageAction(getJDA(), route, this).apply(newContent);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     *             <li>If provided {@link net.dv8tion.jda.core.entities.Message Message}
     *                 contains a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} which
     *                 is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @CheckReturnValue
    default MessageAction editMessageById(long messageId, Message newContent)
    {
        return editMessageById(Long.toUnsignedString(messageId), newContent);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>Shortcut for {@link net.dv8tion.jda.core.MessageBuilder#appendFormat(String, Object...)}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @CheckReturnValue
    default MessageAction editMessageFormatById(String messageId, String format, Object... args)
    {
        Checks.notBlank(format, "Format String");
        return editMessageById(messageId, String.format(format, args));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     * <br>Shortcut for {@link net.dv8tion.jda.core.MessageBuilder#appendFormat(String, Object...)}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
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
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="../util/Formatter.html#detail">Details</a>
     *         section of the formatter class specification.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @CheckReturnValue
    default MessageAction editMessageFormatById(long messageId, String format, Object... args)
    {
        Checks.notBlank(format, "Format String");
        return editMessageById(messageId, String.format(format, args));
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  newEmbed
     *         The new {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws IllegalStateException
     *         If the provided MessageEmbed is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         or {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @CheckReturnValue
    default MessageAction editMessageById(String messageId, MessageEmbed newEmbed)
    {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(newEmbed, "MessageEmbed");

        Route.CompiledRoute route = Route.Messages.EDIT_MESSAGE.compile(getId(), messageId);
        return new MessageAction(getJDA(), route, this).embed(newEmbed);
    }

    /**
     * Attempts to edit a message by its id in this MessageChannel.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#INVALID_AUTHOR_EDIT INVALID_AUTHOR_EDIT}
     *     <br>Attempted to edit a message that was not sent by the currently logged in account.
     *         Discord does not allow editing of other users' Messages!</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         was revoked in the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id referencing the Message that should be edited
     * @param  newEmbed
     *         The new {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} for the edited message
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is not positive.</li>
     *             <li>If provided {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     *                 is not {@link net.dv8tion.jda.core.entities.MessageEmbed#isSendable(net.dv8tion.jda.core.AccountType) sendable}</li>
     *         </ul>
     * @throws IllegalStateException
     *         If the provided MessageEmbed is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this is a TextChannel and this account does not have
     *         {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}
     *         or {@link net.dv8tion.jda.core.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.MessageAction MessageAction}
     *         <br>The modified Message after it has been sent to discord
     */
    @CheckReturnValue
    default MessageAction editMessageById(long messageId, MessageEmbed newEmbed)
    {
        return editMessageById(Long.toUnsignedString(messageId), newEmbed);
    }

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        out = upper ?  getName().toUpperCase(formatter.locale()) : getName();
        if (alt)
            out = "#" + out;

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}
