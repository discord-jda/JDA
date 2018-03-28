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
package net.dv8tion.jda.core.events.message.guild;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * Indicates that a Guild Message contains one or more {@link net.dv8tion.jda.core.entities.MessageEmbed Embeds}.
 * 
 * <p>Can be used to retrieve the affected TextChannel, the id of the affected Message and a list of MessageEmbeds.
 */
public class GuildMessageEmbedEvent extends GenericGuildMessageEvent
{
    private final List<MessageEmbed> embeds;

    public GuildMessageEmbedEvent(JDA api, long responseNumber, long messageId, TextChannel channel, List<MessageEmbed> embeds)
    {
        super(api, responseNumber, messageId, channel);
        this.embeds = embeds;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}
     *
     * @return The MessageEmbeds
     */
    public List<MessageEmbed> getMessageEmbeds()
    {
        return embeds;
    }
}
