/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities;

import net.dv8tion.jda.exceptions.VerificationLevelException;

import java.io.File;
import java.util.function.Consumer;

/**
 * Represents a Discord channel that can have messages and files sent to it.
 */
public interface MessageChannel
{
    /**
     * Sends a plain text {@link net.dv8tion.jda.entities.Message Message} to this channel.
     * This will fail if the account of the api does not have the {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Write-Permission}
     * for this channel set
     * After the Message has been sent, the created {@link net.dv8tion.jda.entities.Message Message} object is returned
     * This Object will be null, if the sending failed.
     * When the Rate-limit is reached (10 Messages in 10 secs), a {@link net.dv8tion.jda.exceptions.RateLimitedException RateLimitedException} is thrown
     *
     * @param text
     *          the text to send
     * @return
     *      the Message created by this function
     * @throws net.dv8tion.jda.exceptions.RateLimitedException
     *      when rate-imit is reached
     * @throws net.dv8tion.jda.exceptions.PermissionException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel} and the logged in account does
     *      not have {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     * @throws net.dv8tion.jda.exceptions.BlockedException
     *      If this is a {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} and PMs are blocked
     * @throws VerificationLevelException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel}
     *      and you do not meet the required verification-level of the guild.
     */
    Message sendMessage(String text);

    /**
     * Sends a given {@link net.dv8tion.jda.entities.Message Message} to this Channel
     * This method only extracts the mentions, text and tts status out of the given Message-Object
     * Therefore this can also be used to resend already received Messages
     * To allow above behaviour, this method returns a new {@link net.dv8tion.jda.entities.Message Message} instance. The passed one is not modified!
     * If the sending of the Message failed (probably Permissions), this method returns null.
     * When the Rate-limit is reached (10 Messages in 10 secs), a {@link net.dv8tion.jda.exceptions.RateLimitedException RateLimitedException} is thrown
     *
     * @param msg
     *          the {@link net.dv8tion.jda.entities.Message Message} to send
     * @return
     *      The created {@link net.dv8tion.jda.entities.Message Message} object or null if it failed
     * @throws net.dv8tion.jda.exceptions.RateLimitedException
     *          when rate-limit is reached
     * @throws net.dv8tion.jda.exceptions.PermissionException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel} and the logged in account does
     *      not have {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     * @throws net.dv8tion.jda.exceptions.BlockedException
     *      If this is a {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel} and PMs are blocked
     * @throws VerificationLevelException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel}
     *      and you do not meet the required verification-level of the guild.
     */
    Message sendMessage(Message msg);

    /**
     * Sents a plain text {@link net.dv8tion.jda.entities.Message Message} to this channel.
     * After the message has been sent, the corresponding {@link net.dv8tion.jda.entities.Message Message} object is passed to the callback-function
     * This method will wait, and send later, if a Rate-Limit occurs.
     *
     * @param msg
     *          the text to send
     * @param callback
     *          the Callback-function that is called upon successful sending with the Message-object of the sent message or null, if sending failed.
     *          You can pass null as callback, if you do not need the created Message-object.
     * @throws net.dv8tion.jda.exceptions.PermissionException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel} and the logged in account does
     *      not have {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     * @throws VerificationLevelException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel}
     *      and you do not meet the required verification-level of the guild.
     */
    void sendMessageAsync(String msg, Consumer<Message> callback);

    /**
     * Sends a given {@link net.dv8tion.jda.entities.Message Message} to this Channel
     * This method only extracts the mentions, text and tts status out of the given Message-Object
     * Therefore this can also be used to resend already received Messages.
     * To allow above behaviour, this method calls the callback with a new {@link net.dv8tion.jda.entities.Message Message} instance. The passed one is not modified!
     * This method will wait, and send later, if a Rate-Limit occurs.
     *
     * @param msg
     *          the {@link net.dv8tion.jda.entities.Message Message} to send
     * @param callback
     *          the Callback-function that is called upon successful sending with the Message-object of the sent message or null, if sending failed.
     *          You can pass null as callback, if you do not need the created Message-object.
     * @throws net.dv8tion.jda.exceptions.PermissionException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel} and the logged in account does
     *      not have {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     * @throws VerificationLevelException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel}
     *      and you do not meet the required verification-level of the guild.
     */
    void sendMessageAsync(Message msg, Consumer<Message> callback);

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * Sends the provided {@link net.dv8tion.jda.entities.Message Message} with the uploaded file.<br>
     * If you do not wish to send a Message with the uploaded file, you can provide <code>null</code> for
     * the <code>message</code> parameter.
     * <p>
     * <b>Note:</b> This method is blocking, which can cause problems when uploading large files.<br>
     * Consider {@link #sendFileAsync(java.io.File, net.dv8tion.jda.entities.Message, java.util.function.Consumer)} for an alternative.
     *
     * @param file
     *          The file to upload to the {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * @param message
     *          The message to be sent along with the uploaded file. This value can be <code>null</code>.
     * @return
     *      The {@link net.dv8tion.jda.entities.Message Message} created from this upload.
     * @throws net.dv8tion.jda.exceptions.PermissionException
     *      <ul>
     *          <li>
     *              If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel} and the logged in account does
     *              not have {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     *          </li>
     *          <li>
     *              If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel} and the logged in account does
     *              not have {@link net.dv8tion.jda.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}.
     *          </li>
     *      </ul>
     * @throws VerificationLevelException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel}
     *      and you do not meet the required verification-level of the guild.
     */
    Message sendFile(File file, Message message);

    /**
     * Asynchronously uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * Sends the provided {@link net.dv8tion.jda.entities.Message Message} with the uploaded file.<br>
     * If you do not wish to send a Message with the uploaded file, you can provide <code>null</code> for
     * the <code>message</code> parameter.
     *
     * @param file
     *          The file to upload to the {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * @param message
     *          The message to be sent along with the uploaded file. This value can be <code>null</code>.
     * @param callback
     *          Function to deal with the returned {@link net.dv8tion.jda.entities.Message Message} after asynchronous uploading completes.
     * @throws net.dv8tion.jda.exceptions.PermissionException
     *      <ul>
     *          <li>
     *              If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel} and the logged in account does
     *              not have {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}.
     *          </li>
     *          <li>
     *              If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel} and the logged in account does
     *              not have {@link net.dv8tion.jda.Permission#MESSAGE_ATTACH_FILES Permission.MESSAGE_ATTACH_FILES}.
     *          </li>
     *      </ul>
     * @throws VerificationLevelException
     *      If this is a {@link net.dv8tion.jda.entities.TextChannel TextChannel}
     *      and you do not meet the required verification-level of the guild.
     */
    void sendFileAsync(File file, Message message, Consumer<Message> callback);

    /**
     * Sends the typing status to discord. This is what is used to make the message "X is typing..." appear.<br>
     * The typing status only lasts for 10 seconds or until a message is sent.
     * So if you wish to show continuous typing you will need to call this method once every 10 seconds.
     */
    void sendTyping();
}
