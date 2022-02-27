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

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.ThreadChannelImpl;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.MessageChannelMixin;

public class MessageDeleteHandler extends SocketHandler
{

    public MessageDeleteHandler(JDAImpl api)
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

        final long messageId = content.getLong("id");
        final long channelId = content.getLong("channel_id");

        //TODO-v5-unified-channel-cache
        MessageChannel channel = getJDA().getTextChannelById(channelId);
        if (channel == null)
            channel = getJDA().getNewsChannelById(channelId);
        if (channel == null)
            channel = getJDA().getThreadChannelById(channelId);
        if (channel == null)
            channel = getJDA().getPrivateChannelById(channelId);
        if (channel == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Got message delete for a channel/group that is not yet cached. ChannelId: {}", channelId);
            return null;
        }

        if (channel.getType().isThread())
        {
            ThreadChannelImpl gThread = (ThreadChannelImpl) channel;

            //If we have less than 50 messages then we can still accurately track how many messages are in the message count.
            //Once we exceed 50 messages Discord caps this value, so we cannot confidently decrement it.
            int messageCount = gThread.getMessageCount();
            if (messageCount < 50 && messageCount > 0)
            {
                gThread.setMessageCount(messageCount - 1);
            }
        }

        getJDA().handleEvent(new MessageDeleteEvent(getJDA(), responseNumber, messageId, channel));
        return null;
    }
}
