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
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.Event;

/**
 * <b><u>MessageDeleteEvent</u></b><br/>
 * Fired if a Message was deleted in a {@link net.dv8tion.jda.entities.MessageChannel MessageChannel}.<br/>
 * <br/>
 * Use: Detect when a Message is deleted. No matter if private or guild.
 */
public class MessageDeleteEvent extends Event
{
    private final boolean isPrivate;
    private final String messageId;
    private final String channelId;

    public MessageDeleteEvent(JDA api, int responseNumber, String messageId, String channelId, boolean isPrivate)
    {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channelId = channelId;
        this.isPrivate = isPrivate;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public MessageChannel getChannel()
    {
        return isPrivate ? getPrivateChannel() : getTextChannel();
    }

    public TextChannel getTextChannel()
    {
        return getJDA().getTextChannelById(channelId);
    }

    public PrivateChannel getPrivateChannel()
    {
        return getJDA().getPrivateChannelById(channelId);
    }

    public Guild getGuild()
    {
        return isPrivate ? null : getTextChannel().getGuild();
    }
}
