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

import net.dv8tion.jda.entities.MessageEmbed;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.message.MessageEmbedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageEmbedEvent;
import net.dv8tion.jda.requests.GuildLock;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class MessageEmbedHandler extends SocketHandler
{

    public MessageEmbedHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        EntityBuilder builder = new EntityBuilder(api);
        String messageId = content.getString("id");
        String channelId = content.getString("channel_id");
        TextChannel channel = api.getChannelMap().get(channelId);
        LinkedList<MessageEmbed> embeds = new LinkedList<>();

        JSONArray embedsJson = content.getJSONArray("embeds");
        for (int i = 0; i < embedsJson.length(); i++)
        {
            embeds.add(builder.createMessageEmbed(embedsJson.getJSONObject(i)));
        }
        if (channel != null)
        {
            if (GuildLock.get(api).isLocked(channel.getGuild().getId()))
            {
                return channel.getGuild().getId();
            }
            api.getEventManager().handle(
                    new GuildMessageEmbedEvent(
                            api, responseNumber,
                            messageId, channel, embeds));
        }
        else
        {
            PrivateChannel privChannel = api.getPmChannelMap().get(channelId);
            if (privChannel == null)
            {
                EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () ->
                {
                    handle(allContent);
                });
                EventCache.LOG.debug("Got unrecognized Channel Id for MessageEmbed! JSON: " + content);
                return null;
            }
            api.getEventManager().handle(
                    new PrivateMessageEmbedEvent(
                            api, responseNumber,
                            messageId, privChannel, embeds));
        }
        //Combo event
        api.getEventManager().handle(
                new MessageEmbedEvent(
                        api, responseNumber,
                        messageId, channelId, embeds, channel == null));
        return null;
    }
}
