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
package net.dv8tion.jda.events.message.guild;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.GenericMessageEvent;

/**
 * <b><u>GenericGuildMessageEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.entities.Message Message} event is fired from a {@link net.dv8tion.jda.entities.TextChannel TextChannel}.<br>
 * Every GuildMessageEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any GuildMessageEvent. <i>(No real use for the JDA user)</i>
 */
public class GenericGuildMessageEvent extends GenericMessageEvent
{
    protected TextChannel channel;

    public GenericGuildMessageEvent(JDA api, int responseNumber, Message message, TextChannel channel)
    {
        super(api, responseNumber, message);
        this.channel = channel;
    }

    public TextChannel getChannel()
    {
        return channel;
    }

    public Guild getGuild()
    {
        return channel.getGuild();
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
        String nickname = getAuthorNick();
        return nickname == null ? getAuthor().getUsername() : nickname;
    }
}
