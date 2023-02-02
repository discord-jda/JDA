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
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.channel.concrete.ThreadChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.MessageChannelMixin;
import net.dv8tion.jda.internal.requests.WebSocketClient;

public class MessageCreateHandler extends SocketHandler
{
    public MessageCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        MessageType type = MessageType.fromId(content.getInt("type"));
        if (type == MessageType.UNKNOWN)
        {
            WebSocketClient.LOG.debug("JDA received a message of unknown type. Type: {}  JSON: {}", type, content);
            return null;
        }

        // Drop ephemeral messages since they are broken due to missing guild_id
        if ((content.getInt("flags", 0) & 64) != 0)
            return null;

        JDAImpl jda = getJDA();
        Guild guild = null;
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getLong("guild_id");
            if (jda.getGuildSetupController().isLocked(guildId))
                return guildId;

            guild = api.getGuildById(guildId);
            if (guild == null)
            {
                api.getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Received message for a guild that JDA does not currently have cached");
                return null;
            }
        }

        Message message;
        try
        {
            message = jda.getEntityBuilder().createMessageWithLookup(content, guild, true);
            if (!message.hasChannel())
                throw new IllegalArgumentException(EntityBuilder.MISSING_CHANNEL);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.getMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    final long channelId = content.getLong("channel_id");

                    // If discord adds message support for unexpected types in the future, drop the event instead of caching it
                    if (guild != null)
                    {
                        GuildChannel actual = guild.getGuildChannelById(channelId);
                        if (actual != null)
                        {
                            WebSocketClient.LOG.debug("Dropping MESSAGE_CREATE for unexpected channel of type {}", actual.getType());
                            return null;
                        }
                    }

                    jda.getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("Received a message for a channel that JDA does not currently have cached");
                    return null;
                }
                case EntityBuilder.MISSING_USER:
                {
                    final long authorId = content.getObject("author").getLong("id");
                    jda.getEventCache().cache(EventCache.Type.USER, authorId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("Received a message for a user that JDA does not currently have cached");
                    return null;
                }
                case EntityBuilder.UNKNOWN_MESSAGE_TYPE:
                {
                    WebSocketClient.LOG.debug("Ignoring message with unknown type: {}", content);
                    return null;
                }
                default:
                    throw e;
            }
        }

        MessageChannel channel = message.getChannel();
        ChannelType channelType = channel.getType();

        //Update the variable that tracks the latest message received in the channel
        ((MessageChannelMixin<?>) channel).setLatestMessageIdLong(message.getIdLong());

        if (channelType.isGuild())
        {
            if (channelType.isThread())
            {
                ThreadChannelImpl gThread = (ThreadChannelImpl) channel;

                gThread.setMessageCount(gThread.getMessageCount() + 1);
                gThread.setTotalMessageCount(gThread.getTotalMessageCount() + 1);
            }
        }
        else
        {
            api.usedPrivateChannel(channel.getIdLong());
        }

        jda.handleEvent(new MessageReceivedEvent( jda, responseNumber, message));
        return null;
    }
}
