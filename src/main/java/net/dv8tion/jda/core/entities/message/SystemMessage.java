/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.entities.message;

import net.dv8tion.jda.core.entities.*;

/**
 * A message that was sent by Discord directly instead of a User.
 * <br>User messages are {@link net.dv8tion.jda.core.entities.Message Message} instances!
 */
public abstract class SystemMessage implements ISnowflake
{
    protected final User author;
    protected final MessageChannel channel;
    protected final long messageId;
    protected final String content;

    public SystemMessage(User author, MessageChannel channel, long messageId, String content)
    {
        this.author = author;
        this.channel = channel;
        this.messageId = messageId;
        this.content = content;
    }

    /**
     * The author of this SystemMessage.
     * <br>More details may be provided by implementations of this class.
     *
     * @return Author for this message
     */
    public User getAuthor()
    {
        return author;
    }

    /**
     * The type of {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * this message has been sent in.
     *
     * @return {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     */
    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    /**
     * Whether the message was sent in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * of the specified {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}.
     *
     * @param  channelType
     *         The expected {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     *
     * @return True, If this message was sent in a channel of the specified type
     */
    public boolean isFromType(ChannelType channelType)
    {
        return getChannelType() == channelType;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     * this message has been sent in. This may be more specific in implementations of this class.
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} this message was sent in
     */
    public MessageChannel getChannel()
    {
        return channel;
    }

    /**
     * The raw content of this message.
     * <br>Details on meaning may be provided by implementations of this class.
     *
     * <p>This might be empty on certain implementations!
     *
     * @return Content of this message
     */
    public String getContent()
    {
        return content;
    }

    @Override
    public long getIdLong()
    {
        return messageId;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageType MessageType}
     * that specifies what kind of message this is.
     *
     * <p>This is never {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}!
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageType MessageType}
     */
    public abstract MessageType getType();
}
