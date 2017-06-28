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

package net.dv8tion.jda.core.events.message.react;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;

public class MessageReactionRemoveAllEvent extends GenericMessageEvent
{

    public MessageReactionRemoveAllEvent(JDA api, long responseNumber, long messageId, MessageChannel channel)
    {
        super(api, responseNumber, messageId, channel);
    }

    public Guild getGuild()
    {
        TextChannel channel = getTextChannel();
        return channel != null ? channel.getGuild() : null;
    }

    public TextChannel getTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) getChannel() : null;
    }

    public PrivateChannel getPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) getChannel() : null;
    }

    public Group getGroup()
    {
        return isFromType(ChannelType.GROUP) ? (Group) getChannel() : null;
    }

}
