/**
 * Copyright 2015-2016 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

import java.util.List;

public class MessageEmbedEvent extends Event {
    private final boolean isPrivate;
    private final String messageId;
    private final String channelId;
    private final List<MessageEmbed> embeds;

    public MessageEmbedEvent(JDA api, int responseNumber, String messageId, String channelId, List<MessageEmbed> embeds, boolean isPrivate) {
        super(api, responseNumber);
        this.messageId = messageId;
        this.channelId = channelId;
        this.embeds = embeds;
        this.isPrivate = isPrivate;
    }

    public String getMessageId() {
        return messageId;
    }

    public TextChannel getTextChannel() {
        return getJDA().getTextChannelById(channelId);
    }

    public PrivateChannel getPrivateChannel() {
        return getJDA().getPrivateChannelById(channelId);
    }

    public MessageChannel getChannel() {
        return isPrivate ? getPrivateChannel() : getTextChannel();
    }

    public Guild getGuild() {
        return isPrivate ? null : getTextChannel().getGuild();
    }

    public List<MessageEmbed> getMessageEmbeds() {
        return embeds;
    }
}
