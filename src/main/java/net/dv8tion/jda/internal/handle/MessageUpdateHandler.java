/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
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
        Guild guild = null;
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getLong("guild_id");
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;
            guild = api.getGuildById(guildId);
            if (guild == null)
            {
                api.getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Received message for a guild that JDA does not currently have cached");
                return null;
            }
        }

        // Drop ephemeral messages since they are broken due to missing guild_id
        if ((content.getInt("flags", 0) & 64) != 0)
            return null;

        if (content.hasKey("author"))
        {
            if (content.hasKey("type"))
            {
                MessageType type = MessageType.fromId(content.getInt("type"));
                if (!type.isSystem())
                    return handleMessage(content, guild);
                WebSocketClient.LOG.debug("JDA received a message update for an unexpected message type. Type: {} JSON: {}", type, content);
                return null;
            }
            else if (!content.isNull("embeds"))
            {
                //Received update with no "type" field which means its an update for a rich embed message
                handleMessageEmbed(content);
                return null;
            }
        }
        else if (!content.isNull("embeds"))
            return handleMessageEmbed(content);
        return null;
    }

    private Long handleMessage(DataObject content, Guild guild)
    {
        Message message;
        try
        {
            message = getJDA().getEntityBuilder().createMessageWithLookup(content, guild, true);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.getMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    final long channelId = content.getUnsignedLong("channel_id");

                    // If discord adds message support for unexpected types in the future, drop the event instead of caching it
                    if (guild != null)
                    {
                        GuildChannel actual = guild.getGuildChannelById(channelId);
                        if (actual != null)
                        {
                            WebSocketClient.LOG.debug("Dropping MESSAGE_UPDATE for unexpected channel of type {}", actual.getType());
                            return null;
                        }
                    }

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

        if (message.getChannelType() == ChannelType.PRIVATE)
            getJDA().usedPrivateChannel(message.getChannel().getIdLong());

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

        MessageChannel channel = getJDA().getChannelById(MessageChannel.class, channelId);
        if (channel == null)
        {
            Guild guild = getJDA().getGuildById(content.getUnsignedLong("guild_id", 0L));
            if (guild != null)
            {
                GuildChannel guildChannel = guild.getGuildChannelById(channelId);
                if (guildChannel != null)
                {
                    WebSocketClient.LOG.debug("Discarding MESSAGE_UPDATE event for unexpected channel type. Channel: {}", guildChannel);
                    return null;
                }
            }

            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received message update for embeds for a channel/group that JDA does not have cached yet.");
            return null;
        }

        DataArray embedsJson = content.getArray("embeds");
        for (int i = 0; i < embedsJson.length(); i++)
            embeds.add(builder.createMessageEmbed(embedsJson.getObject(i)));

        getJDA().handleEvent(
            new MessageEmbedEvent(
                getJDA(), responseNumber,
                messageId, channel, embeds));
        return null;
    }
}
