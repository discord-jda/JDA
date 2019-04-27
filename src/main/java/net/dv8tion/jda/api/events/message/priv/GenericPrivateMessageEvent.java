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
package net.dv8tion.jda.api.events.message.priv;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Message Message} event is fired from a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}.
 * <br>Every PrivateMessageEvent is an instance of this event and can be casted.
 * 
 * <p>Can be used to detect any PrivateMessageEvent.
 */
public abstract class GenericPrivateMessageEvent extends Event
{
    protected final long messageId;
    protected final PrivateChannel channel;

    public GenericPrivateMessageEvent(@Nonnull JDA api, long responseNumber, long messageId, @Nonnull PrivateChannel channel)
    {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channel = channel;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel} for the message
     *
     * @return The PrivateChannel
     */
    @Nonnull
    public PrivateChannel getChannel()
    {
        return channel;
    }

    /**
     * The id for this message
     *
     * @return The id for this message
     */
    @Nonnull
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
}
