/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.message.WelcomeMessage;
import net.dv8tion.jda.core.events.message.GenericSystemMessageEvent;

public class GuildMemberJoinMessageEvent extends GenericSystemMessageEvent
{
    public GuildMemberJoinMessageEvent(JDA api, long responseNumber, WelcomeMessage message, TextChannel channel)
    {
        super(api, responseNumber, message, channel);
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.User User} that joined this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return The joining User
     */
    @Override
    public User getAuthor()
    {
        return super.getAuthor();
    }

    @Override
    public WelcomeMessage getMessage()
    {
        return (WelcomeMessage) message;
    }

    @Override
    public MessageType getType()
    {
        return MessageType.GUILD_MEMBER_JOIN;
    }

    @Override
    public ChannelType getChannelType()
    {
        return ChannelType.TEXT;
    }

    @Override
    public TextChannel getChannel()
    {
        return (TextChannel) channel;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this message was sent in
     *
     * @return The {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return getChannel().getGuild();
    }

    /**
     * {@link net.dv8tion.jda.core.entities.Member Member} instance of the
     * {@link #getAuthor() getAuthor()} user.
     *
     * @return Member instance for message author
     */
    public Member getMember()
    {
        return getGuild().getMember(getAuthor());
    }
}
