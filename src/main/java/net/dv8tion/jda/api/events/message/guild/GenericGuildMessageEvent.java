/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
package net.dv8tion.jda.api.events.message.guild;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Message Message} event is fired from a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
 * <br>Every GuildMessageEvent is derived from this event and can be casted.
 * 
 * <p>Can be used to detect any GuildMessageEvent.
 */
public abstract class GenericGuildMessageEvent extends GenericGuildEvent
{
    protected final long messageId;
    protected final TextChannel channel;

    public GenericGuildMessageEvent(@Nonnull JDA api, long responseNumber, long messageId, @Nonnull TextChannel channel)
    {
        super(api, responseNumber, channel.getGuild());
        this.messageId = messageId;
        this.channel = channel;
    }

    /**
     * The message id
     *
     * @return The message id
     */
    @Nonnull
    public String getMessageId()
    {
        return Long.toUnsignedString(messageId);
    }

    /**
     * The message id
     *
     * @return The message id
     */
    public long getMessageIdLong()
    {
        return messageId;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} for this message
     *
     * @return The TextChannel for this message
     */
    @Nonnull
    public TextChannel getChannel()
    {
        return channel;
    }
}
