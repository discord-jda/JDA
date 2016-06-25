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
package net.dv8tion.jda.events.message.priv;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.MessageEmbed;
import net.dv8tion.jda.entities.PrivateChannel;

import java.util.List;

/**
 * <b><u>PrivateMessageEmbedEvent</u></b><br/>
 * Fired if a Message contains {@link net.dv8tion.jda.entities.MessageEmbed Embeds} in a {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel}.<br/>
 * <br/>
 * Use: Retrieve affected PrivateChannel, the ID of the deleted Message and a list of MessageEmbeds.
 */
public class PrivateMessageEmbedEvent extends GenericPrivateMessageEvent
{
    private final String messageId;
    private final List<MessageEmbed> embeds;

    public PrivateMessageEmbedEvent(JDA api, int responseNumber, String messageId, PrivateChannel channel, List<MessageEmbed> embeds)
    {
        super(api, responseNumber, null, channel);
        this.messageId = messageId;
        this.embeds = embeds;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public List<MessageEmbed> getMessageEmbeds()
    {
        return embeds;
    }
}
