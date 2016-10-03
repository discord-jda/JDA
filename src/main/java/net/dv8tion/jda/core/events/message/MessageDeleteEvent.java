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
package net.dv8tion.jda.core.events.message;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;

/**
 * <b><u>MessageDeleteEvent</u></b><br/>
 * Fired if a Message was deleted in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.<br/>
 * <br/>
 * Use: Detect when a Message is deleted. No matter if private or guild.
 */
public class MessageDeleteEvent extends Event
{
    private final String messageId;
    private final MessageChannel channel;

    public MessageDeleteEvent(JDA api, long responseNumber, String messageId, MessageChannel channel)
    {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channel = channel;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public boolean isPrivate()
    {
        return channel instanceof PrivateChannel;
    }

    public MessageChannel getChannel()
    {
        return channel;
    }

    public PrivateChannel getPrivatechannel()
    {
        return isPrivate() ? (PrivateChannel) channel : null;
    }

    public TextChannel getTextChannel()
    {
        return !isPrivate() ? (TextChannel) channel : null;
    }

    public Guild getGuild()
    {
        return !isPrivate() ? getTextChannel().getGuild() : null;
    }
}
