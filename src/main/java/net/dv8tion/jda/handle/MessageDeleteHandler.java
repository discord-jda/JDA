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

import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.message.MessageDeleteEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageDeleteEvent;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONObject;

public class MessageDeleteHandler extends SocketHandler
{

    public MessageDeleteHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String messageId = content.getString("id");
        String channelId = content.getString("channel_id");
        TextChannel channel = api.getChannelMap().get(channelId);

        if (channel != null)
        {
            if (GuildLock.get(api).isLocked(channel.getGuild().getId()))
            {
                return channel.getGuild().getId();
            }
            api.getEventManager().handle(
                    new GuildMessageDeleteEvent(
                            api, responseNumber,
                            messageId, channel));
        }
        else
        {
            PrivateChannel privChannel = api.getPmChannelMap().get(channelId);
            if (privChannel == null)
                throw new IllegalArgumentException("Message deleted in unknown channel! (unknown channel id). JSON: " + content);
            api.getEventManager().handle(
                    new PrivateMessageDeleteEvent(
                            api, responseNumber,
                            messageId, privChannel));
        }
        //Combo event
        api.getEventManager().handle(
                new MessageDeleteEvent(
                        api, responseNumber,
                        messageId, channelId, channel == null));
        return null;
    }
}
