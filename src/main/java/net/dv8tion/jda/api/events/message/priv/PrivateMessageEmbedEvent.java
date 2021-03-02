/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Indicates that a Message contains {@link net.dv8tion.jda.api.entities.MessageEmbed Embeds} in a {@link net.dv8tion.jda.api.entities.PrivateChannel PrivateChannel}.
 * 
 * <p>Can be used to retrieve affected PrivateChannel, the ID of the deleted Message and a list of MessageEmbeds.
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#DIRECT_MESSAGES DIRECT_MESSAGES} intent to be enabled.
 */
public class PrivateMessageEmbedEvent extends GenericPrivateMessageEvent
{
    private final List<MessageEmbed> embeds;

    public PrivateMessageEmbedEvent(@Nonnull JDA api, long responseNumber, long messageId, @Nonnull PrivateChannel channel, @Nonnull List<MessageEmbed> embeds)
    {
        super(api, responseNumber, messageId, channel);
        this.embeds = embeds;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds}
     *
     * @return The MessageEmbeds
     */
    @Nonnull
    public List<MessageEmbed> getMessageEmbeds()
    {
        return embeds;
    }
}
