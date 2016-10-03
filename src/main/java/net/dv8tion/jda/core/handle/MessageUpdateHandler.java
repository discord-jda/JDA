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
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.MessageEmbedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class MessageUpdateHandler extends SocketHandler
{

    public MessageUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (content.has("author"))
        {
            MessageType type = MessageType.fromId(content.getInt("type"));

            switch (type)
            {
                case DEFAULT:
                    return handleDefaultMessage(content);
                default:
                    JDAImpl.LOG.debug("JDA received a message of unknown type. Type: " + type + "  JSON: " + content);
                    return null;
            }
        }
        else
        {
            return handleMessageEmbed(content);
        }
    }

    private String handleDefaultMessage(JSONObject content)
    {
        Message message;
        try
        {
            message = EntityBuilder.get(api).createMessage(content);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.getMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("channel_id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("Received a message update for a channel that JDA does not currently have cached");
                    return null;
                }
                case EntityBuilder.MISSING_USER:
                {
                    EventCache.get(api).cache(EventCache.Type.USER, content.getJSONObject("user").getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("Received a message update for a user that JDA does not currently have cached");
                    return null;
                }
                default:
                    throw e;
            }
        }

        if (!message.isPrivate())
        {
            TextChannel channel = message.getTextChannel();
            if (GuildLock.get(api).isLocked(channel.getGuild().getId()))
            {
                return channel.getGuild().getId();
            }
            api.getEventManager().handle(
                    new GuildMessageUpdateEvent(
                            api, responseNumber,
                            message));
        }
        else
        {
            api.getEventManager().handle(
                    new PrivateMessageUpdateEvent(
                            api, responseNumber,
                            message));
        }
        //Combo event
        api.getEventManager().handle(
                new MessageUpdateEvent(
                        api, responseNumber,
                        message));
        return null;
    }

    private String handleMessageEmbed(JSONObject content)
    {
        EntityBuilder builder = EntityBuilder.get(api);
        String messageId = content.getString("id");
        String channelId = content.getString("channel_id");
        LinkedList<MessageEmbed> embeds = new LinkedList<>();
        MessageChannel channel = api.getTextChannelMap().get(channelId);
        if (channel == null)
            channel = api.getPrivateChannelMap().get(channelId);
        if (channel == null)
            channel = api.getFakePrivateChannelMap().get(channelId);
        if (channel == null)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, channelId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Received message update for embeds for a channel that JDA does not have cached yet.");
            return null;
        }

        JSONArray embedsJson = content.getJSONArray("embeds");
        for (int i = 0; i < embedsJson.length(); i++)
        {
            embeds.add(builder.createMessageEmbed(embedsJson.getJSONObject(i)));
        }

        if (channel instanceof TextChannel)
        {
            TextChannel tChannel = (TextChannel) channel;
            if (GuildLock.get(api).isLocked(tChannel.getGuild().getId()))
            {
                return tChannel.getGuild().getId();
            }
            api.getEventManager().handle(
                    new GuildMessageEmbedEvent(
                            api, responseNumber,
                            messageId, tChannel, embeds));
        }
        else
        {
            api.getEventManager().handle(
                    new PrivateMessageEmbedEvent(
                            api, responseNumber,
                            messageId, (PrivateChannel) channel, embeds));
        }
        //Combo event
        api.getEventManager().handle(
                new MessageEmbedEvent(
                        api, responseNumber,
                        messageId, channel, embeds));
        return null;
    }
}
