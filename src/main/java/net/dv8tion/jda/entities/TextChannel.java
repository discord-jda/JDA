/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.Permission;

import java.util.List;

/**
 * Represents a Discord Text Channel.
 * This should provide all necessary functions for interacting with a channel.
 */
public interface TextChannel
{
    /**
     * The Id of the {@link net.dv8tion.jda.entities.TextChannel TextChannel}. This is typically 18 characters long.
     * @return
     *      The Id of this TextChannel
     */
    String getId();

    /**
     * The human readable name of the  {@link net.dv8tion.jda.entities.TextChannel TextChannel}.<br>
     * If no name has been set, this returns null.
     *
     * @return
     *      The name of this TextChannel
     */
    String getName();

    /**
     * The topic set for the  {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     * Can also be thought of as the description of this {@link net.dv8tion.jda.entities.TextChannel TextChannel}.<br>
     * If no topic has been set, this returns null.
     *
     * @return
     *      Possibly-null String containing the topic of this {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     */
    String getTopic();

    /**
     * Returns the {@link net.dv8tion.jda.entities.Guild Guild} that this {@link net.dv8tion.jda.entities.TextChannel TextChannel} is part of.
     *
     * @return
     *      Never-null {@link net.dv8tion.jda.entities.Guild Guild} that this {@link net.dv8tion.jda.entities.TextChannel TextChannel} is part of.
     */
    Guild getGuild();

    /**
     * A List of all {@link net.dv8tion.jda.entities.User Users} that have the {@link net.dv8tion.jda.Permission#MESSAGE_READ}
     * for this {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     *
     * @return
     *      A List of {@link net.dv8tion.jda.entities.User Users} that can read the messages in this channel.
     */
    List<User> getUsers();

    /**
     * The position the {@link net.dv8tion.jda.entities.TextChannel TextChannel} is displayed at.<br>
     * Useful for displaying a list of {@link net.dv8tion.jda.entities.TextChannel TextChannels}.
     *
     * @return
     *      Zero-based int of position of the {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     */
    int getPosition();

    /**
     * Sents a plain text {@link net.dv8tion.jda.entities.Message Message} to this channel.
     * This will fail if the account of the api does not have the {@link net.dv8tion.jda.Permission#MESSAGE_WRITE Write-Permission}
     * for this channel set
     * After the Message has been sent, the created {@link net.dv8tion.jda.entities.Message Message} object is returned
     * This Object will be null, if the sending failed
     *
     * @param text
     *          the text to send
     * @return
     *      the Message created by this function
     */
    Message sendMessage(String text);

    /**
     * Sends a given {@link net.dv8tion.jda.entities.Message Message} to this Channel
     * This method only extracts the mentions, text and tts status out of the given Message-Object
     * Therefore this can also be used to resend already received Messages
     * To allow above behaviour, this method returns a new {@link net.dv8tion.jda.entities.Message Message} instance. The passed one is not modified!
     * If the sending of the Message failed (probably Permissions), this method returns null
     *
     * @param msg
     *          the {@link net.dv8tion.jda.entities.Message Message} to send
     * @return
     *      The created {@link net.dv8tion.jda.entities.Message Message} object or null if it failed
     */
    Message sendMessage(Message msg);

    /**
     * Sends the typing status to discord. This is what is used to make the message "X is typing..." appear.<br>
     * The typing status only lasts for 5 seconds, so if you wish to show continuous typing you will need to
     * call this method once every 5 seconds.
     */
    void sendTyping();

    /**
     * Checks if the given {@link net.dv8tion.jda.entities.User User} has the given {@link net.dv8tion.jda.Permission Permission}
     * in this {@link net.dv8tion.jda.entities.TextChannel TextChannel}
     *
     * @param user
     *          the User to check the Permission against
     * @param permission
     *          the Permission to check for
     * @return
     *      if the given User has the given Permission in this Channel
     */
    boolean checkPermission(User user, Permission permission);
}
