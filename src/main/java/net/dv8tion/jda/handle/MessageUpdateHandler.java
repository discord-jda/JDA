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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.message.MessageUpdateEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

public class MessageUpdateHandler extends SocketHandler
{

    public MessageUpdateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        Message message = new EntityBuilder(api).createMessage(content);
        if (!message.isPrivate())
        {
            TextChannel channel = api.getChannelMap().get(message.getChannelId());
            if (GuildLock.get(api).isLocked(channel.getGuild().getId()))
            {
                return channel.getGuild().getId();
            }
            api.getEventManager().handle(
                    new GuildMessageUpdateEvent(
                            api, responseNumber,
                            message, channel));
        }
        else
        {
            api.getEventManager().handle(
                    new PrivateMessageUpdateEvent(
                            api, responseNumber,
                            message, api.getPmChannelMap().get(message.getChannelId())));
        }
        //Combo event
        api.getEventManager().handle(
                new MessageUpdateEvent(
                        api, responseNumber,
                        message));
        return null;
    }
}
