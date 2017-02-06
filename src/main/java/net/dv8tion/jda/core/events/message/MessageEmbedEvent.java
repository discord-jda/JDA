/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;

import java.util.Collections;
import java.util.List;

/**
 * <b><u>MessageEmbedEvent</u></b><br>
 * Fired if a Message contains an {@link net.dv8tion.jda.core.entities.MessageEmbed Embed} in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.<br>
 * <br>
 * Use: Grab MessageEmbeds from any message. No matter if private or guild.
 */
public class MessageEmbedEvent extends Event
{
    private final String messageId;
    private final MessageChannel channel;
    private final List<MessageEmbed> embeds;

    public MessageEmbedEvent(JDA api, long responseNumber, String messageId, MessageChannel channel, List<MessageEmbed> embeds)
    {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channel = channel;
        this.embeds = Collections.unmodifiableList(embeds);
    }

    public String getMessageId()
    {
        return messageId;
    }

    public List<MessageEmbed> getMessageEmbeds()
    {
        return embeds;
    }

    public boolean isFromType(ChannelType type)
    {
        return channel.getType() == type;
    }

    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    public MessageChannel getChannel()
    {
        return channel;
    }

    public PrivateChannel getPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) channel : null;
    }

    public Group getGroup()
    {
        return isFromType(ChannelType.GROUP) ? (Group) channel : null;
    }

    public TextChannel getTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) channel : null;
    }

    public Guild getGuild()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getGuild() : null;
    }

}
