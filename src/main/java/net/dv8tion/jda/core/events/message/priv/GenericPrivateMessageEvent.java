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
package net.dv8tion.jda.core.events.message.priv;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.Event;

/**
 * <b><u>GenericPrivateMessageEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.core.entities.Message Message} event is fired from a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.<br>
 * Every PrivateMessageEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any PrivateMessageEvent. <i>(No real use for the JDA user)</i>
 */
public abstract class GenericPrivateMessageEvent extends Event
{
    protected final long messageId;
    protected final PrivateChannel channel;

    public GenericPrivateMessageEvent(JDA api, long responseNumber, long messageId, PrivateChannel channel)
    {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channel = channel;
    }

    public PrivateChannel getChannel()
    {
        return channel;
    }

    public String getMessageId()
    {
        return Long.toUnsignedString(messageId);
    }

    public long getMessageIdLong()
    {
        return messageId;
    }

    @Deprecated
    public boolean isFromType(ChannelType type)
    {
        return channel.getType() == type;
    }

    @Deprecated
    public ChannelType getChannelType()
    {
        return channel.getType();
    }
}
