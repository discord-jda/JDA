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
package net.dv8tion.jda.core.events.message;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.Event;

/**
 * <b><u>GenericMessageEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.core.entities.Message Message} event is fired.<br>
 * Every MessageEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any MessageEvent. <i>(No real use for the JDA user)</i>
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

    public MessageChannel getChannel()
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

    public boolean isFromType(ChannelType type)
    {
        return channel.getType() == type;
    }

    public ChannelType getChannelType()
    {
        return channel.getType();
    }

}
