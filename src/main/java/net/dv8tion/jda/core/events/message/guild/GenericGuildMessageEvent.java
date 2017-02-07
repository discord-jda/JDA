/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;

/**
 * <b><u>GenericGuildMessageEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.core.entities.Message Message} event is fired from a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.<br>
 * Every GuildMessageEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any GuildMessageEvent. <i>(No real use for the JDA user)</i>
 */
public abstract class GenericGuildMessageEvent extends GenericMessageEvent
{
    protected TextChannel channel;

    public GenericGuildMessageEvent(JDA api, long responseNumber, Message message, TextChannel channel)
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

    public Member getMember()
    {
        return getGuild().getMember(getAuthor());
    }
}
