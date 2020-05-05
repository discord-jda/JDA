/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.WebSocketClient;

import java.util.LinkedList;

public class MessageUpdateHandler extends SocketHandler
{

    public MessageUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getLong("guild_id");
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;
        }

        //TODO: Rewrite this entire handler
        if (content.hasKey("author"))
        {
            if (content.hasKey("type"))
            {
                MessageType type = MessageType.fromId(content.getInt("type"));
                switch (type)
                {
                    case DEFAULT:
                        return handleMessage(content);
                    default:
                        WebSocketClient.LOG.debug("JDA received a message update for an unexpected message type. Type: {} JSON: {}", type, content);
                        return null;
                }
            }
            else if (!content.isNull("embeds"))
            {
                //Received update with no "type" field which means its an update for a rich embed message
                handleMessageEmbed(content);
                return null;
            }
        }
        else if (content.hasKey("call"))
        {
            handleCallMessage(content);
            return null;
        }
        else if (!content.isNull("embeds"))
            return handleMessageEmbed(content);
        return null;
    }

    private Long handleMessage(DataObject content)
    {
        Message message;
        try
        {
            message = getJDA().getEntityBuilder().createMessage(content);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.getMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    final long channelId = content.getLong("channel_id");
                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("Received a message update for a channel that JDA does not currently have cached");
                    return null;
                }
                case EntityBuilder.MISSING_USER:
                {
                    final long authorId = content.getObject("author").getLong("id");
                    getJDA().getEventCache().cache(EventCache.Type.USER, authorId, responseNumber, allContent, this::handle);
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
                if (getJDA().getGuildSetupController().isLocked(channel.getGuild().getIdLong()))
                    return channel.getGuild().getIdLong();
                getJDA().handleEvent(
                        new GuildMessageUpdateEvent(
                                getJDA(), responseNumber,
                                message));
                break;
            }
            case PRIVATE:
            {
                getJDA().handleEvent(
                        new PrivateMessageUpdateEvent(
                                getJDA(), responseNumber,
                                message));
                break;
            }
            case GROUP:
            {
                WebSocketClient.LOG.warn("Received a MESSAGE_UPDATE for a group which is not supported");
                break;
            }

            default:
                WebSocketClient.LOG.warn("Received a MESSAGE_UPDATE with a unknown MessageChannel ChannelType. JSON: {}", content);
                return null;
        }

        //Combo event
        getJDA().handleEvent(
                new MessageUpdateEvent(
                        getJDA(), responseNumber,
                        message));
        return null;
    }

    private Long handleMessageEmbed(DataObject content)
    {
        EntityBuilder builder = getJDA().getEntityBuilder();
        final long messageId = content.getLong("id");
        final long channelId = content.getLong("channel_id");
        LinkedList<MessageEmbed> embeds = new LinkedList<>();
        MessageChannel channel = getJDA().getTextChannelsView().get(channelId);
        if (channel == null)
            channel = getJDA().getPrivateChannelsView().get(channelId);
        if (channel == null)
            channel = getJDA().getFakePrivateChannelMap().get(channelId);
        if (channel == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received message update for embeds for a channel/group that JDA does not have cached yet.");
            return null;
        }

        DataArray embedsJson = content.getArray("embeds");
        for (int i = 0; i < embedsJson.length(); i++)
            embeds.add(builder.createMessageEmbed(embedsJson.getObject(i)));

        switch (channel.getType())
        {
            case TEXT:
                TextChannel tChannel = (TextChannel) channel;
                if (getJDA().getGuildSetupController().isLocked(tChannel.getGuild().getIdLong()))
                    return tChannel.getGuild().getIdLong();
                getJDA().handleEvent(
                    new GuildMessageEmbedEvent(
                        getJDA(), responseNumber,
                        messageId, tChannel, embeds));
                break;
            case PRIVATE:
                getJDA().handleEvent(
                    new PrivateMessageEmbedEvent(
                        getJDA(), responseNumber,
                        messageId, (PrivateChannel) channel, embeds));
                break;
            case GROUP:
                WebSocketClient.LOG.error("Received a message update for a group which should not be possible");
                return null;
            default:
                WebSocketClient.LOG.warn("No event handled for message update of type {}", channel.getType());

        }
        //Combo event
        getJDA().handleEvent(
                new MessageEmbedEvent(
                        getJDA(), responseNumber,
                        messageId, channel, embeds));
        return null;
    }

    public void handleCallMessage(DataObject content)
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
