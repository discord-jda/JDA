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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

import java.util.Collections;
import java.util.List;

/**
 * Indicates that a Message contains an {@link net.dv8tion.jda.core.entities.MessageEmbed Embed} in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
 * <br>Discord may need to do additional calculations and resizing tasks on messages that embed websites, thus they send the message only with content and link and use this update to add the missing embed later when the server finishes those calculations.
 * 
 * <p>Can be used to retrieve MessageEmbeds from any message. No matter if private or guild.
 */
public class MessageEmbedEvent extends GenericMessageEvent
{
    private final List<MessageEmbed> embeds;

    public MessageEmbedEvent(JDA api, long responseNumber, long messageId, MessageChannel channel, List<MessageEmbed> embeds)
    {
        super(api, responseNumber, messageId, channel);
        this.embeds = Collections.unmodifiableList(embeds);
    }

    /**
     * The list of {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     *
     * @return The list of MessageEmbeds
     */
    public List<MessageEmbed> getMessageEmbeds()
    {
        return embeds;
    }
}
