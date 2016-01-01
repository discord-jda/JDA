/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

public class MessageUpdateEvent extends GenericMessageEvent
{
    public MessageUpdateEvent(JDA api, int responseNumber, Message message)
    {
        super(api, responseNumber, message);
    }

    public User getAuthor()
    {
        return getMessage().getAuthor();
    }

    public boolean isPrivate()
    {
        return getMessage().isPrivate();
    }

    public TextChannel getTextChannel()
    {
        return getJDA().getTextChannelById(getMessage().getChannelId());
    }

    public PrivateChannel getPrivateChannel()
    {
        return getJDA().getPrivateChannelById(getMessage().getChannelId());
    }

    public Guild getGuild()
    {
        return isPrivate() ? null : getTextChannel().getGuild();
    }
}
