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
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 * Indicates that a Message was edited in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
 * 
 * <p>Can be used to retrieve affected TextChannel and Message.
 */
public class GuildMessageUpdateEvent extends GenericGuildMessageEvent
{
    private final Message message;

    public GuildMessageUpdateEvent(JDA api, long responseNumber, Message message)
    {
        super(api, responseNumber, message.getIdLong(), message.getTextChannel());
        this.message = message;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Message Message}
     *
     * @return The Message
     */
    public Message getMessage()
    {
        return message;
    }

    /**
     * The author of this message
     *
     * @return The author of this message
     *
     * @see    net.dv8tion.jda.core.entities.User User
     */
    public User getAuthor()
    {
        return message.getAuthor();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Member Member} instance of the author
     *
     * @return The member instance for the author
     */
    public Member getMember()
    {
        return getGuild().getMember(getAuthor());
    }
}
