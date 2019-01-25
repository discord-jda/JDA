/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.events.message;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

/**
 * Indicates that a Message was received in a {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
 * <br>This includes {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} and {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}!
 * 
 * <p>Can be used to detect that a Message is received in either a guild- or private channel. Providing a MessageChannel and Message.
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
     * The received {@link net.dv8tion.jda.api.entities.Message Message} object.
     *
     * @return The received {@link net.dv8tion.jda.api.entities.Message Message} object.
     */
    public Message getMessage()
    {
        return message;
    }

    /**
     * The Author of the Message received as {@link net.dv8tion.jda.api.entities.User User} object.
     * <br>This will be never-null but might be a fake user if Message was sent via Webhook (Guild only).
     *
     * @return The Author of the Message.
     *
     * @see #isWebhookMessage()
     * @see net.dv8tion.jda.api.entities.User#isFake()
     */
    public User getAuthor()
    {
        return message.getAuthor();
    }

    /**
     * The Author of the Message received as {@link net.dv8tion.jda.api.entities.Member Member} object.
     * <br>This will be {@code null} in case of Message being received in
     * a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}
     * or {@link #isWebhookMessage() isWebhookMessage()} returning {@code true}.
     *
     * @return The Author of the Message as null-able Member object.
     *
     * @see    #isWebhookMessage()
     */
    public Member getMember()
    {
        return isFromType(ChannelType.TEXT) && !isWebhookMessage() ? getGuild().getMember(getAuthor()) : null;
    }

    /**
     * Whether or not the Message received was sent via a Webhook.
     * <br>This is a shortcut for {@code getMessage().isWebhookMessage()}.
     *
     * @return True, if the Message was sent via Webhook
     */
    public boolean isWebhookMessage()
    {
        return getMessage().isWebhookMessage();
    }
}
