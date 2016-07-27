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
 * <b><u>MessageReceivedEvent</u></b><br/>
 * Fired if a Message is sent in a {@link net.dv8tion.jda.entities.MessageChannel MessageChannel}.<br/>
 * <br/>
 * Use: This event indicates that a Message is sent in either a private or guild channel. Providing a MessageChannel and Message.
 */
public class MessageReceivedEvent extends Event
{
    private final Message message;

    public MessageReceivedEvent(JDA api, int responseNumber, Message message)
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

    /**
     * Returns the author's nickname in the guild the message was sent in.
     * Returns null if no nickname is set.
     *
     * @return
     *      Author's nickname in guild or null if unset.
     */
    public String getAuthorNick()
    {
        return getGuild().getNicknameForUser(getAuthor());
    }

    /**
     * Returns the Author's effective name in the guild the message was sent in.
     * This returns the nickname if set or the author's username if no nick is set.
     *
     * @return
     *      Author's effective name.
     */
    public String getAuthorName()
    {
        String nickname = isPrivate() ? getAuthor().getUsername() : getAuthorNick();
        return nickname == null ? getAuthor().getUsername() : nickname;
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
