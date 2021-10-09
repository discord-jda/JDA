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
import net.dv8tion.jda.internal.entities.BaseGuildMessageChannelImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildThreadImpl;
import net.dv8tion.jda.internal.entities.PrivateChannelImpl;
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
        if (channelType.isGuild())
        {
            if (channelType.isThread())
            {
                GuildThreadImpl gThread = (GuildThreadImpl) channel;
                gThread.setLastMessageId(message.getIdLong());

                //Discord will only ever allow this property to show up to 50,
                // so we don't want to update it to be over 50 because we don't want users to use it incorrectly.
                //TODO-threads: Honestly, should we even expose this? if we're updating it here then we should
                // make sure that it stays correct when a MESSAGE_DELETE is sent for a thread right? but unless the value
                // is currently below we cannot confidently know that it can be decremented.
                // Plus, im not sure that this is actually valuable to _anyone_.
                int newMessageCount = Math.max(gThread.getMessageCount() + 1, 50);
                gThread.setMessageCount(newMessageCount);
            }
            else
            {
                BaseGuildMessageChannelImpl<?, ?> gChannel = (BaseGuildMessageChannelImpl<?, ?>) channel;
                gChannel.setLastMessageId(message.getIdLong());
            }
        }
        else
        {
            PrivateChannelImpl pChannel = (PrivateChannelImpl) channel;
            pChannel.setLastMessageId(message.getIdLong());
            api.usedPrivateChannel(channel.getIdLong());
        }

        jda.handleEvent(new MessageReceivedEvent( jda, responseNumber, message));
        return null;
    }
}
