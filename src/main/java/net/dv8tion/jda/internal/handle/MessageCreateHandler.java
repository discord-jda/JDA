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

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.ThreadChannelImpl;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.MessageChannelMixin;
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

        JDAImpl jda = getJDA();
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getLong("guild_id");
            if (jda.getGuildSetupController().isLocked(guildId))
                return guildId;
        }

        Message message;
        try
        {
            message = jda.getEntityBuilder().createMessage(content, true);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.getMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    final long channelId = content.getLong("channel_id");
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

                //Discord will only ever allow this property to show up to 50,
                // so we don't want to update it to be over 50 because we don't want users to use it incorrectly.
                int newMessageCount = Math.max(gThread.getMessageCount() + 1, 50);
                gThread.setMessageCount(newMessageCount);
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
