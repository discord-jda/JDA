/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.events.message.group.GroupMessageEmbedEvent;
import net.dv8tion.jda.client.events.message.group.GroupMessageUpdateEvent;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.MessageEmbedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageEmbedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.core.requests.WebSocketClient;
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
    protected Long handleInternally(JSONObject content)
    {
        if (content.has("author"))
        {
            if (content.has("type"))
            {
                MessageType type = MessageType.fromId(content.getInt("type"));
                switch (type)
                {
                    case DEFAULT:
                        return handleMessage(content);
                    default:
                        WebSocketClient.LOG.debug("JDA received a message of unknown type. Type: {} JSON: {}", type, content);
                        return null;
                }
            }
            else
            {
                //Received update with no "type" field which means its an update for a rich embed message
                handleMessageEmbed(content);
                return null;
            }
        }
        else if (content.has("call"))
        {
            handleCallMessage(content);
            return null;
        }
        else
            return handleMessageEmbed(content);
    }

    private Long handleMessage(JSONObject content)
    {
        Message message;
        try
        {
            message = api.getEntityBuilder().createMessage(content);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.getMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    final long channelId = content.getLong("channel_id");
                    api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("Received a message update for a channel that JDA does not currently have cached");
                    return null;
                }
                case EntityBuilder.MISSING_USER:
                {
                    final long authorId = content.getJSONObject("author").getLong("id");
                    api.getEventCache().cache(EventCache.Type.USER, authorId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("Received a message update for a user that JDA does not currently have cached");
                    return null;
                }
                default:
                    throw e;
            }
        }

        switch (message.getChannelType())
        {
            case TEXT:
            {
                TextChannel channel = message.getTextChannel();
                if (api.getGuildLock().isLocked(channel.getGuild().getIdLong()))
                {
                    return channel.getGuild().getIdLong();
                }
                api.getEventManager().handle(
                        new GuildMessageUpdateEvent(
                                api, responseNumber,
                                message));
                break;
            }
            case PRIVATE:
            {
                api.getEventManager().handle(
                        new PrivateMessageUpdateEvent(
                                api, responseNumber,
                                message));
                break;
            }
            case GROUP:
            {
                api.getEventManager().handle(
                        new GroupMessageUpdateEvent(
                                api, responseNumber,
                                message));
                break;
            }

            default:
                WebSocketClient.LOG.warn("Received a MESSAGE_UPDATE with a unknown MessageChannel ChannelType. JSON: {}", content);
                return null;
        }

        //Combo event
        api.getEventManager().handle(
                new MessageUpdateEvent(
                        api, responseNumber,
                        message));
        return null;
    }

    private Long handleMessageEmbed(JSONObject content)
    {
        EntityBuilder builder = api.getEntityBuilder();
        final long messageId = content.getLong("id");
        final long channelId = content.getLong("channel_id");
        LinkedList<MessageEmbed> embeds = new LinkedList<>();
        MessageChannel channel = api.getTextChannelMap().get(channelId);
        if (channel == null)
            channel = api.getPrivateChannelMap().get(channelId);
        if (channel == null)
            channel = api.getFakePrivateChannelMap().get(channelId);
        if (channel == null && api.getAccountType() == AccountType.CLIENT)
            channel = api.asClient().getGroupById(channelId);
        if (channel == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received message update for embeds for a channel/group that JDA does not have cached yet.");
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
            if (api.getGuildLock().isLocked(tChannel.getGuild().getIdLong()))
                return tChannel.getGuild().getIdLong();
            api.getEventManager().handle(
                    new GuildMessageEmbedEvent(
                            api, responseNumber,
                            messageId, tChannel, embeds));
        }
        else if (channel instanceof PrivateChannel)
        {
            api.getEventManager().handle(
                    new PrivateMessageEmbedEvent(
                            api, responseNumber,
                            messageId, (PrivateChannel) channel, embeds));
        }
        else
        {
            api.getEventManager().handle(
                    new GroupMessageEmbedEvent(
                            api, responseNumber,
                            messageId, (Group) channel, embeds));
        }
        //Combo event
        api.getEventManager().handle(
                new MessageEmbedEvent(
                        api, responseNumber,
                        messageId, channel, embeds));
        return null;
    }

    public void handleCallMessage(JSONObject content)
    {
        WebSocketClient.LOG.debug("Received a MESSAGE_UPDATE of type CALL: {}", content);
        //Called when someone joins call for first time.
        //  It is not called when they leave or rejoin. That is all dictated by VOICE_STATE_UPDATE.
        //  Probably can ignore the above due to VOICE_STATE_UPDATE
        // Could have a mapping of all users who were participants at one point or another during the call
        //  in comparison to the currently participants.
        // and when the call is ended. Ending defined by ended_timestamp != null
    }
}
