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

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Message Message} was created/deleted/changed.
 * <br>Every MessageEvent is an instance of this event and can be casted.
 * 
 * <p>Can be used to detect any MessageEvent.
 */
public abstract class GenericMessageEvent extends Event
{
    protected final long messageId;
    protected final MessageChannel channel;

    public GenericMessageEvent(JDA api, long responseNumber, long messageId, MessageChannel channel)
    {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channel = channel;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} for this Message
     *
     * @return The MessageChannel
     */
    public MessageChannel getChannel()
    {
        return channel;
    }

    /**
     * The id for this message
     *
     * @return The id for this message
     */
    public String getMessageId()
    {
        return Long.toUnsignedString(messageId);
    }

    /**
     * The id for this message
     *
     * @return The id for this message
     */
    public long getMessageIdLong()
    {
        return messageId;
    }

    /**
     * Indicates whether the message is from the specified {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     *
     * @param  type
     *         The ChannelType
     *
     * @return True, if the message is from the specified channel type
     */
    public boolean isFromType(ChannelType type)
    {
        return channel.getType() == type;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} for this message
     *
     * @return The ChannelType
     */
    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel},
     * this will return {@code null}.
     *
     * @return The Guild the Message was received in or null if not from a TextChannel
     *
     * @see    net.dv8tion.jda.core.events.message.GenericMessageEvent#isFromType(ChannelType)
     */
    public Guild getGuild()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getGuild() : null;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel},
     * this will return {@code null}.
     *
     * @return The TextChannel the Message was received in or null if not from a TextChannel
     *
     * @see    net.dv8tion.jda.core.events.message.GenericMessageEvent#isFromType(ChannelType)
     */
    public TextChannel getTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) channel : null;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel},
     * this will return {@code null}.
     *
     * @return The PrivateChannel the Message was received in or null if not from a PrivateChannel
     *
     * @see    net.dv8tion.jda.core.events.message.GenericMessageEvent#isFromType(ChannelType)
     */
    public PrivateChannel getPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) channel : null;
    }

    /**
     * The {@link net.dv8tion.jda.client.entities.Group Group} the Message was received in.
     * <br>If this Message was not received in a {@link net.dv8tion.jda.client.entities.Group Group},
     * this will return {@code null}.
     *
     * @return The Group the Message was received in or null if not from a Group
     *
     * @see    net.dv8tion.jda.core.events.message.GenericMessageEvent#isFromType(ChannelType)
     */
    public Group getGroup()
    {
        return isFromType(ChannelType.GROUP) ? (Group) channel : null;
    }
}
