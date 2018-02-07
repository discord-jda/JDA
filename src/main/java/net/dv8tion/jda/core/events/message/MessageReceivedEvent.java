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
package net.dv8tion.jda.core.events.message;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

/**
 * <b><u>MessageReceivedEvent</u></b><br>
 * Fired if a Message is received in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
 * <br>This includes {@link net.dv8tion.jda.core.entities.TextChannel TextChannel},
 * {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} and {@link net.dv8tion.jda.client.entities.Group Group}!
 * <p>
 * Use: This event indicates that a Message is received in either a guild, private channel or group. Providing a MessageChannel and Message.
 */
public class MessageReceivedEvent extends GenericMessageEvent
{
    private final Message message;

    public MessageReceivedEvent(JDA api, long responseNumber, Message message)
    {
        super(api, responseNumber, message.getIdLong(), message.getChannel());
        this.message = message;
    }

    /**
     * Returns the received {@link net.dv8tion.jda.core.entities.Message Message} object.
     *
     * @return The received {@link net.dv8tion.jda.core.entities.Message Message} object.
     */
    @NonNull
    public Message getMessage()
    {
        return message;
    }

    /**
     * Returns the Author of the Message received as {@link net.dv8tion.jda.core.entities.User User} object.
     * <br>This will be never-null but might be a fake user if Message was sent via Webhook (Guild only).
     *
     * @return The Author of the Message.
     *
     * @see #isWebhookMessage()
     * @see net.dv8tion.jda.core.entities.User#isFake()
     */
    @NonNull
    public User getAuthor()
    {
        return message.getAuthor();
    }

    /**
     * Returns the Author of the Message received as {@link net.dv8tion.jda.core.entities.Member Member} object.
     * <br>This will be {@code null} in case of Message being received in
     * a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel},
     * a {@link net.dv8tion.jda.client.entities.Group Group}
     * or {@link #isWebhookMessage() isWebhookMessage()} returning {@code true}.
     *
     * @return The Author of the Message as null-able Member object.
     *
     * @see #isWebhookMessage()
     */
    public Member getMember()
    {
        return isFromType(ChannelType.TEXT) && !isWebhookMessage() ? getGuild().getMember(getAuthor()) : null;
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel},
     * this will return {@code null}.
     *
     * @return The PrivateChannel the Message was received in or null if not from a PrivateChannel
     *
     * @see net.dv8tion.jda.core.events.message.GenericMessageEvent#isFromType(ChannelType)
     */
    public PrivateChannel getPrivateChannel()
    {
        return message.getPrivateChannel();
    }

    /**
     * Returns the {@link net.dv8tion.jda.client.entities.Group Group} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.client.entities.Group Group},
     * this will return {@code null}.
     *
     * @return The Group the Message was received in or null if not from a Group
     *
     * @see net.dv8tion.jda.core.events.message.GenericMessageEvent#isFromType(ChannelType)
     */
    public Group getGroup()
    {
        return message.getGroup();
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel},
     * this will return {@code null}.
     *
     * @return The TextChannel the Message was received in or null if not from a TextChannel
     *
     * @see net.dv8tion.jda.core.events.message.GenericMessageEvent#isFromType(ChannelType)
     */
    public TextChannel getTextChannel()
    {
        return message.getTextChannel();
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel},
     * this will return {@code null}.
     *
     * @return The Guild the Message was received in or null if not from a TextChannel
     *
     * @see net.dv8tion.jda.core.events.message.GenericMessageEvent#isFromType(ChannelType)
     */
    public Guild getGuild()
    {
        return message.getGuild();
    }

    /**
     * Returns whether or not the Message received was sent via a Webhook.
     * <br>This is a shortcut for {@code getMessage().isWebhookMessage()}.
     *
     * @return Whether or not the Message was sent via Webhook
     */
    public boolean isWebhookMessage()
    {
        return getMessage().isWebhookMessage();
    }
}
