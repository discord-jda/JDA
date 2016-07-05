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
package net.dv8tion.jda.events.message;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.events.Event;

/**
 * <b><u>MessageUpdateEvent</u></b><br/>
 * Fired if a Message is edited in a {@link net.dv8tion.jda.entities.MessageChannel MessageChannel}.<br/>
 * <br/>
 * Use: This event indicates that a Message is edited in either a private or guild channel. Providing a MessageChannel and Message.
 */
public class MessageUpdateEvent extends Event
{
    private final Message message;

    public MessageUpdateEvent(JDA api, int responseNumber, Message message)
    {
        super(api, responseNumber);
        this.message = message;
    }

    public Message getMessage()
    {
        return message;
    }

    public User getAuthor()
    {
        return message.getAuthor();
    }

    public boolean isPrivate()
    {
        return message.isPrivate();
    }

    public TextChannel getTextChannel()
    {
        return getJDA().getTextChannelById(message.getChannelId());
    }

    public PrivateChannel getPrivateChannel()
    {
        return getJDA().getPrivateChannelById(message.getChannelId());
    }

    public MessageChannel getChannel()
    {
        return isPrivate() ? getPrivateChannel() : getTextChannel();
    }

    public Guild getGuild()
    {
        return isPrivate() ? null : getTextChannel().getGuild();
    }
}
