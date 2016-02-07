/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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

import java.io.File;
import java.util.function.Consumer;

/**
 * Represents a Discord channel that can have messages and files sent to it.
 */
public interface MessageChannel
{
    /**
     * Sents a plain text {@link net.dv8tion.jda.entities.Message Message} to this channel.
     * This will fail if the account of the api does not have the {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Write-Permission}
     * for this channel set
     * After the Message has been sent, the created {@link net.dv8tion.jda.entities.Message Message} object is returned
     * This Object will be null, if the sending failed.
     * When the Rate-limit is reached (30 Messages in 30 secs), a {@link net.dv8tion.jda.exceptions.RateLimitedException RateLimitedException} is thrown
     *
     * @param text
     *          the text to send
     * @return
     *      the Message created by this function
     * @throws net.dv8tion.jda.exceptions.RateLimitedException when rate-imit is reached
     */
    Message sendMessage(String text);

    /**
     * Sends a given {@link net.dv8tion.jda.entities.Message Message} to this Channel
     * This method only extracts the mentions, text and tts status out of the given Message-Object
     * Therefore this can also be used to resend already received Messages
     * To allow above behaviour, this method returns a new {@link net.dv8tion.jda.entities.Message Message} instance. The passed one is not modified!
     * If the sending of the Message failed (probably Permissions), this method returns null.
     * When the Rate-limit is reached (30 Messages in 30 secs), a {@link net.dv8tion.jda.exceptions.RateLimitedException RateLimitedException} is thrown
     *
     * @param msg
     *          the {@link net.dv8tion.jda.entities.Message Message} to send
     * @return
     *      The created {@link net.dv8tion.jda.entities.Message Message} object or null if it failed
     * @throws net.dv8tion.jda.exceptions.RateLimitedException when rate-imit is reached
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
     *          the Callback-function that is called upon successful sending
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
     *          the Callback-function that is called upon successful sending
     */
    void sendMessageAsync(Message msg, Consumer<Message> callback);

    /**
     * Uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * <p>
     * <b>Note:</b> This method is blocking, which can cause problems when uploading large files.<br>
     * Consider {@link #sendFileAsync(java.io.File, java.util.function.Consumer) sendFileAsync(File, Consumer)} for an alternative.
     *
     * @param file
     *          The file to upload to the {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * @return
     *      The {@link net.dv8tion.jda.entities.Message Message} created from this upload.
     */
    Message sendFile(File file);

    /**
     * Asynchronously uploads a file to the Discord servers and sends it to this {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     *
     * @param file
     *          The file to upload to the {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * @param callback
     *          Function to deal with the returned {@link net.dv8tion.jda.entities.Message Message} after asynchronous uploading completes.
     */
    void sendFileAsync(File file, Consumer<Message> callback);

    /**
     * Sends the typing status to discord. This is what is used to make the message "X is typing..." appear.<br>
     * The typing status only lasts for 5 seconds, so if you wish to show continuous typing you will need to
     * call this method once every 5 seconds.
     */
    void sendTyping();
}
